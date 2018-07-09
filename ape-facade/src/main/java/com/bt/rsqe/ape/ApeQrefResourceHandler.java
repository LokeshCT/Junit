package com.bt.rsqe.ape;

import com.bt.commons.configuration.ConfigurationException;
import com.bt.rsqe.ape.config.ApeMappingConfigLoader;
import com.bt.rsqe.ape.config.AttributeMapping;
import com.bt.rsqe.ape.config.LocalIdentifier;
import com.bt.rsqe.ape.dto.AccessStaffComment;
import com.bt.rsqe.ape.dto.ApeQref;
import com.bt.rsqe.ape.dto.ApeQrefIdentifier;
import com.bt.rsqe.ape.dto.ApeQrefPrices;
import com.bt.rsqe.ape.dto.ApeQrefProductConfiguration;
import com.bt.rsqe.ape.dto.ApeQrefSiteDetails;
import com.bt.rsqe.ape.dto.ApeQrefStencilId;
import com.bt.rsqe.ape.dto.ApeQrefUpdate;
import com.bt.rsqe.ape.dto.FlattenedQref;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.ape.repository.entities.AccessStaffCommentEntity;
import com.bt.rsqe.ape.repository.entities.ApeQrefDetailEntity;
import com.bt.rsqe.ape.repository.entities.ApeQrefErrorEntity;
import com.bt.rsqe.ape.repository.entities.ApeRequestEntity;
import com.bt.rsqe.ape.transformer.ApePricingStatus;
import com.bt.rsqe.ape.workflow.AccessWorkflowStatus;
import com.bt.rsqe.domain.QrefIdFormat;
import com.bt.rsqe.domain.QrefRequestStatus;
import com.bt.rsqe.dto.SearchCriteria;
import com.bt.rsqe.expressionevaluator.BeanEvaluator;
import com.bt.rsqe.expressionevaluator.ExpressionEvaluator;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.BeanUtils;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.xmlbeans.XmlException;
import pricing.ape.bt.com.webservices.APEQuote;
import pricing.ape.bt.com.webservices.ArrayOfAPEQuote;
import pricing.ape.bt.com.webservices.ArrayOfAPEQuoteDocument;
import pricing.ape.bt.com.webservices.ArrayOfError;
import pricing.ape.bt.com.webservices.ArrayOfStaffDetails;
import pricing.ape.bt.com.webservices.Error;
import pricing.ape.bt.com.webservices.ProductPricing;
import pricing.ape.bt.com.webservices.StarsResponse;
import pricing.ape.bt.com.webservices.SiteQuery;
import pricing.ape.bt.com.webservices.StaffDetails;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.expressionevaluator.ContextualEvaluatorTypes.*;
import static com.google.common.collect.Lists.*;

@Path("/rsqe/ape-facade/qref")
@Produces({MediaType.APPLICATION_JSON})
public class ApeQrefResourceHandler {
    private static final String QREF_ID = "qrefId";
    private static final String UNIQUE_ID = "uniqueId";
    private static final String FALSE = "false";
    private Logger logger = LogFactory.createDefaultLogger(Logger.class);

    private final APEQrefRepository apeQrefRepository;

    public ApeQrefResourceHandler(APEQrefRepository apeQrefRepository) {
        this.apeQrefRepository = apeQrefRepository;
    }

    @Path("/{qrefId}")
    @GET
    public Response getQrefById(@PathParam(QREF_ID) String qrefId) {
        try {
            return ResponseBuilder.anOKResponse().withEntity(apeQrefRepository.getApeQref(qrefId)).build();
        } catch (ResourceNotFoundException ex) {
            return ResponseBuilder.notFound().build();
        }
    }


    @Path("/uniqueId/{uniqueId}")
    @POST
    public Response getAvailableQrefIdentifiers(SearchCriteria searchCriteria, @PathParam(UNIQUE_ID) String uniqueId) {
        List<ApeQrefIdentifier> qrefIdentifiers = apeQrefRepository.getQrefIdentifiers(uniqueId, searchCriteria);

        return Response.ok(new GenericEntity<List<ApeQrefIdentifier>>(qrefIdentifiers) {
        }).build();
    }

    @Deprecated
    @Path("/uniqueId/{uniqueId}")
    @PUT
    public Response updateQrefs(@PathParam(UNIQUE_ID) String uniqueId, String apeQuoteArrayXml) {
        ArrayOfAPEQuote qrefData;

        try {
            qrefData = ArrayOfAPEQuoteDocument.Factory.parse(apeQuoteArrayXml).getArrayOfAPEQuote();
        } catch (XmlException e) {
            return ResponseBuilder.badRequest().withEntity(e.getMessage()).build();
        }

        ApeRequestEntity requestEntity = apeQrefRepository.getAPERequestByUniqueId(uniqueId);

        if(null == requestEntity) {
            return ResponseBuilder.notFound().withEntity("APE Request does not exist for Unique ID.  Perhaps it has been cancelled?").build();
        }

        List<ApeQrefUpdate> apeQrefUpdateList = newArrayList();
        Map<String, Integer> qrefSequences = new HashMap<String, Integer>();
        List<FlattenedQref> flattenedQrefs = FlattenedQref.flattenAndSort(qrefData.getAPEQuoteList());

        for(FlattenedQref flattenedQref : flattenedQrefs) {
            ProductPricing productPricing = flattenedQref.getProductPricing();
            String qrefId = QrefIdFormat.convert(productPricing.getQref());

            if(!qrefSequences.containsKey(qrefId)) {
                qrefSequences.put(qrefId, qrefSequences.size());
            }

            Integer qrefSequence = qrefSequences.get(qrefId);

            apeQrefUpdateList.add(saveFlattenedQref(qrefSequence, flattenedQref));
        }

        if(!qrefSequences.isEmpty()) {
            requestEntity.setStatus(QrefRequestStatus.Status.PROCESSED);
        } else {
            requestEntity.setStatus(QrefRequestStatus.Status.ERROR);
            requestEntity.setErrorMessage("No QREFs available for current site configuration");
        }

        apeQrefRepository.save(requestEntity);

        GenericEntity<List<ApeQrefUpdate>> apeQrefUpdateListEntity =
            new GenericEntity<List<ApeQrefUpdate>>(apeQrefUpdateList) {
            };

        return ResponseBuilder.anOKResponse().withEntity(apeQrefUpdateListEntity).build();
    }

    @Path("/{qrefId}/sequence/{sequence}")
    @PUT
    public Response saveQref(@PathParam(QREF_ID) String qrefId ,@PathParam("sequence") int sequence, FlattenedQref flattenedQref) {
        logger.savingQrefForId(qrefId);
        ApeQrefUpdate apeQrefUpdate = saveFlattenedQref(sequence, flattenedQref);
        return ResponseBuilder.anOKResponse().withEntity(apeQrefUpdate).build();
    }

    @Path("/{qrefId}/prices")
    @GET
    public Response getPricesForQref(@PathParam(QREF_ID) String qrefId) {
        ApeQref apeQref = apeQrefRepository.getApeQref(qrefId);
        String responseType = apeQref.getResponseType();
        String oneTimePrice;
        String recurringPrice;
        QrefAttributeExtractor attributeExtractor = new QrefAttributeExtractor(apeQref);

        String workflowStatus = attributeExtractor.getAttributeValue(LocalIdentifier.WORKFLOW_STATUS);
        AccessWorkflowStatus accessWorkflowStatus = !Strings.isNullOrEmpty(workflowStatus)
                                                        ? AccessWorkflowStatus.workflowFromStatus(attributeExtractor.getAttributeValue(LocalIdentifier.WORKFLOW_STATUS))
                                                        : null;

        // If QREF has been rejected then return reject reasons
        if(AccessWorkflowStatus.REJECTED.equals(accessWorkflowStatus)) {
            return ResponseBuilder.anOKResponse().withEntity(new ApeQrefPrices(qrefId, accessWorkflowStatus, apeQref.getErrors())).build();
        }

        if(APEQrefRepository.MARKET_BASED_PRICE.equals(responseType)){
            oneTimePrice = attributeExtractor.getAttributeValue(LocalIdentifier.BT_INSTALL_PRICE);
            recurringPrice = attributeExtractor.getAttributeValue(LocalIdentifier.BT_MONTHLY_PRICE);
        }else{
            oneTimePrice = attributeExtractor.getAttributeValue(LocalIdentifier.INSTALL_PRICE);
            recurringPrice = attributeExtractor.getAttributeValue(LocalIdentifier.MONTHLY_PRICE);
        }

        ApeQrefPrices apeQrefPrices = new ApeQrefPrices(qrefId,
                                                        attributeExtractor.getAttributeValue(LocalIdentifier.PRICE_STATUS),
                                                        accessWorkflowStatus,
                                                        attributeExtractor.getAttributeValue(LocalIdentifier.CURRENCY),
                                                        recurringPrice,
                                                        oneTimePrice,
                                                        attributeExtractor.getAttributeValue(LocalIdentifier.SUPPLIER_COST),
                                                        attributeExtractor.getAttributeValue(LocalIdentifier.INSTALL_COST),
                                                        recurringPrice,
                                                        oneTimePrice,
                                                        attributeExtractor.getAttributeDoubleValue(LocalIdentifier.USD_EXCHANGE_RATE),
                                                        attributeExtractor.getAttributeDoubleValue(LocalIdentifier.EUR_EXCHANGE_RATE),
                                                        attributeExtractor.getAttributeDoubleValue(LocalIdentifier.GBP_EXCHANGE_RATE),
                                                        attributeExtractor.getAttributeDateValue(LocalIdentifier.EXPIRY_DATE)); // Reuse EUP prices for PTP

        String status = apeQrefPrices.getStatus();

        if(!Strings.isNullOrEmpty(status)
                && !Strings.isNullOrEmpty(apeQrefPrices.getCurrency())
                && (!Strings.isNullOrEmpty(apeQrefPrices.getRecurringPrice())
                        || !Strings.isNullOrEmpty(apeQrefPrices.getOneTimePrice()))
                        || !Strings.isNullOrEmpty(apeQrefPrices.getOneTimePrice())
                        || !Strings.isNullOrEmpty(apeQrefPrices.getRecurringCost())
                        || !Strings.isNullOrEmpty(apeQrefPrices.getOneTimeCost())) {
            return ResponseBuilder.anOKResponse().withEntity(apeQrefPrices).build();
        } else if (!Strings.isNullOrEmpty(status) && ApePricingStatus.NoPrice.getStatus().equals(status)) {
            return ResponseBuilder.anOKResponse().withEntity(apeQrefPrices).build();
        } else {
            return ResponseBuilder.notFound().withEntity("Prices do not exist for QREF").build();
        }
    }

    @Path("/{qrefId}/staff-comments")
    @GET
    public Response getStaffCommentsForQref(@PathParam(QREF_ID) String qrefId) {
        List<AccessStaffComment> staffComments = Lists.transform(apeQrefRepository.getStaffComments(qrefId), new Function<AccessStaffCommentEntity, AccessStaffComment>() {
            @Override
            public AccessStaffComment apply(@Nullable AccessStaffCommentEntity input) {
                return input.toDto();
            }
        });

        GenericEntity<List<AccessStaffComment>> entity =
            new GenericEntity<List<AccessStaffComment>>(staffComments){
        };

        return Response.ok().entity(entity).build();
    }

    private ApeQrefUpdate saveFlattenedQref(int sequence, FlattenedQref flattenedQref) {
        APEQuote apeQuote = flattenedQref.getApeQuote();
        ProductPricing productPricing = flattenedQref.getProductPricing();
        SiteQuery siteQuery = flattenedQref.getSiteQuery();
        String genericCaveats = getValue(genericCaveatMapping, apeQuote);
        String requestId = apeQuote.getRequestId();
        String responseType = apeQuote.getResponseType();
        StarsResponse starsResponse = flattenedQref.getStarsResponse();

        String accessTechnologyMapping = ApeMappingConfigLoader.getAccessTechnologyLocator();
        String accessTechnology = (String) BeanUtils.getValues(productPricing, accessTechnologyMapping).get(0);
        String baseAccessTechnology = ApeMappingConfigLoader.getBaseAccessTechnology(accessTechnology);
        List<AttributeMapping> apeQuoteMappings = ApeMappingConfigLoader.getApeQuoteMappings(baseAccessTechnology);
        List<AttributeMapping> siteMappings = ApeMappingConfigLoader.getApeSiteQueryMappings(baseAccessTechnology);
        List<AttributeMapping> accessMappings = ApeMappingConfigLoader.getApeProductPricingMappings(baseAccessTechnology);
        List<AttributeMapping> responseTypeMappings = ApeMappingConfigLoader.getApeResponseTypeMappings(responseType);
        List<AttributeMapping> starsResponseMappings = ApeMappingConfigLoader.getApeStarsResponseMappings(responseType);

        String caveats = getValue(caveatMapping, productPricing);
        String qrefId = QrefIdFormat.convert(productPricing.getQref());

        saveCaveats(requestId, qrefId, sequence, genericCaveats, caveats);
        saveStaffComments(qrefId, productPricing.getWorkFlowStaff());
        saveErrors(qrefId, siteQuery.getErrorDetails());

        mapValues(requestId, qrefId, sequence, apeQuote, apeQuoteMappings);
        mapValues(requestId, qrefId, sequence, siteQuery, siteMappings);
        mapValues(requestId, qrefId, sequence, productPricing, accessMappings);
        mapValues(requestId, qrefId, sequence, productPricing, responseTypeMappings);
        mapValues(requestId, qrefId, sequence, starsResponse, starsResponseMappings);

        return generateQrefUpdateObject(qrefId, productPricing, siteQuery, responseType);
    }

    private void saveCaveats(String requestId, String qrefId, int orderSequence, String genericCaveats, String caveats) {
        if(genericCaveats.length()>0){
            saveQrefDetailEntity(requestId, qrefId, orderSequence, genericCaveats+","+caveats, "CAVEATS");
        }else{
            saveQrefDetailEntity(requestId, qrefId, orderSequence, caveats, "CAVEATS");
        }
    }

    private void mapValues(String requestId, String qref, int orderSequence, Object sourceObject, List<AttributeMapping> mappings) {
        if(sourceObject!=null){
            for(AttributeMapping attributeMapping : mappings){
                String attributeName = attributeMapping.getName();
                if(!attributeName.equalsIgnoreCase("CAVEATS")){
                    String attributeValue = getValue(attributeMapping, sourceObject);
                    saveQrefDetailEntity(requestId, qref, orderSequence, attributeValue, attributeName);
                }
            }
        }
    }

    private void saveQrefDetailEntity(String requestId, String qref, int orderSequence, String attributeValue, String attributeName) {
        ApeQrefDetailEntity apeQrefDetailEntity = new ApeQrefDetailEntity(requestId,
                                                                          qref,
                                                                          attributeName,
                                                                          attributeValue,
                                                                          orderSequence);
        apeQrefRepository.save(apeQrefDetailEntity);
    }

    private String getValue(AttributeMapping attributeMapping, Object sourceObject) {
        String attributeValue = "";

        try {
            if(!Strings.isNullOrEmpty(attributeMapping.getMapping())) {
                BeanEvaluator beanEvaluator = new BeanEvaluator(sourceObject, STRING_EVALUATOR.getName());
                try {
                    attributeValue = (String) ExpressionEvaluator.evaluate(attributeMapping.getMapping(), beanEvaluator);
                } catch (Exception e) {
                    logger.attributeCouldNotBeMapped(attributeMapping.getName(), e.getMessage());
                }
            }
        } catch (ConfigurationException e) {
            // keep calm, the mapping attribute is not mandatory!
        }

        return attributeValue;
    }

    private void saveErrors(String qrefId, ArrayOfError errorDetails) {
        if(null != errorDetails && !com.bt.rsqe.utils.Lists.isNullOrEmpty(errorDetails.getErrorList())) {
            List<ApeQrefErrorEntity> existingErrors = apeQrefRepository.getApeQrefErrors(qrefId);

            for(Error error : errorDetails.getErrorList()) {
                ApeQrefErrorEntity errorEntity = new ApeQrefErrorEntity(qrefId, error.getCode(), error.getDescription());

                // Only save new errors!
                if(!existingErrors.contains(errorEntity)) {
                    apeQrefRepository.save(errorEntity);
                }
            }
        }
    }

    private ApeQrefUpdate generateQrefUpdateObject(String qrefStencilId,
                                                   ProductPricing productPricing,
                                                   SiteQuery siteQuery, String responseType){
        AccessWorkflowStatus accessWorkflowStatus = AccessWorkflowStatus.workflowFromStatus(productPricing.getWorkflowStatus());

        ApeQrefSiteDetails apeQrefSiteDetails = new ApeQrefSiteDetails(siteQuery.getSiteName(),
                                                    siteQuery.getSiteAddress().getStreetName(),
                                                    siteQuery.getSiteAddress().getCity(),
                                                    siteQuery.getSiteAddress().getState(),
                                                    siteQuery.getSiteAddress().getPostCode(),
                                                    siteQuery.getSiteAddress().getCountryName());

        ApeQrefProductConfiguration apeQrefProductConfiguration = new ApeQrefProductConfiguration(productPricing.getProductName(), productPricing.getAccessTechnology(),
                                                                      productPricing.getAccessSpeedValue(), productPricing.getSupplierName(),
                                                                      productPricing.getSupplierProduct()== null ? "" : productPricing.getSupplierProduct().toString(), accessWorkflowStatus);

        String installPrice, monthlyPrice;
        if(APEQrefRepository.MARKET_BASED_PRICE.equals(responseType)){
            installPrice = String.valueOf(productPricing.getBTInstallprice());
            monthlyPrice = String.valueOf(productPricing.getBTmonthlyprice());
        } else{
            installPrice =  productPricing.getInstall();
            monthlyPrice =  productPricing.getMonthly();
        }
        ApeQrefPrices apeQrefPrices = new ApeQrefPrices(productPricing.getCurrencyCode(),installPrice,monthlyPrice , productPricing.getAvailability().toString());

        return new ApeQrefUpdate(new ApeQrefStencilId(qrefStencilId), apeQrefSiteDetails, apeQrefProductConfiguration, apeQrefPrices);
    }

    private void saveStaffComments(String qrefId, ArrayOfStaffDetails staffDetails) {
        if(null == staffDetails || com.bt.rsqe.utils.Lists.isNullOrEmpty(staffDetails.getStaffDetailsList())) {
            return;
        }

        List<AccessStaffCommentEntity> existingStaffComments = apeQrefRepository.getStaffComments(qrefId);

        for(StaffDetails staffComment : staffDetails.getStaffDetailsList()) {
            if(Strings.isNullOrEmpty(staffComment.getComments())) {
                continue; // don't save empty comments!
            }

            AccessStaffCommentEntity staffCommentEntity = new AccessStaffCommentEntity(qrefId,
                                                                                       staffComment.getComments(),
                                                                                       staffComment.getStaffEmail(),
                                                                                       staffComment.getStaffName(),
                                                                                       new java.sql.Date(staffComment.getCreatedDate().getTimeInMillis()));

            // only save new staff comments
            if(!existingStaffComments.contains(staffCommentEntity)) {
                apeQrefRepository.save(staffCommentEntity);
            }
        }
    }

     private AttributeMapping caveatMapping= new AttributeMapping() {
        @Override
        public String getName() {
            return "NA";
        }

        @Override
        public String getMapsToOffering() {
            return FALSE;
        }

        @Override
        public String getMapping() {
            return "Join(caveats.caveatList.id,',')";
        }

        @Override
        public String getUserVisible() {
            return FALSE;
        }

         @Override
         public String getTransformer() {
             return null;
         }

         @Override
         public String getPriority() {
             return null;
         }

         @Override
         public String getDefaultValue() {
             return null;
         }

         @Override
         public boolean isApeMapping() {
             return true;
         }
     };

    private AttributeMapping genericCaveatMapping= new AttributeMapping() {
        @Override
        public String getName() {
            return "NA";
        }

        @Override
        public String getMapsToOffering() {
            return FALSE;
        }

        @Override
        public String getMapping() {
            return "Join(genericCaveats.clsGenericCaveatList.id,',')";
        }

        @Override
        public String getUserVisible() {
            return FALSE;
        }

        @Override
        public String getTransformer() {
            return null;
        }

         @Override
         public String getPriority() {
             return null;
         }

        @Override
        public String getDefaultValue() {
            return null;
        }

        @Override
        public boolean isApeMapping() {
            return true;
        }
    };

    public static interface Logger {
        @Log(level = LogLevel.INFO, format = "Attribute '%s' could not be mapped. %s")
        void attributeCouldNotBeMapped(String attributeName, String errorMessage);

        @Log(level = LogLevel.INFO, format = "Saving qref with id '%s'")
        void savingQrefForId(String qrefId);
    }
}
