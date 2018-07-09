package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.ProductAgreementOrchestrator;
import com.bt.rsqe.projectengine.web.view.ProductAgreementDetailsDTO;
import com.bt.rsqe.projectengine.web.view.ProductAgreementDetailsView;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.web.Presenter;
import com.google.common.base.Strings;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static javax.ws.rs.core.Response.Status.*;


@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/line-items/{lineItemId}/lineItem-array/{lineItemArray}/productAgreements")
@Produces(MediaType.TEXT_HTML + ";charset=ISO-8859-15")
public class ProductAgreementResourceHandler extends QuoteViewFocusedResourceHandler {

    private static final Logger LOGGER = LogFactory.createDefaultLogger(Logger.class);

    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String QUOTE_OPTION_ITEM = "lineItemId";
    private static final String PRODUCT_SLA_APPLICABLE = "Product SLA applicable";
    private static final String PMF_LOOKUP_AVAILABLE = "Yes";
    private static final String PMF_LOOKUP_NOT_AVAILABLE = "No";
    private static final String DEFAULT_SLA = "Default SLA";
    private static final String PRODUCT_NAME = "Product Name";
    private static final String SLA_ID_ATTRIBUTE = "SLA ID";
    private static final String LINE_ITEM_ARRAY="lineItemArray";

    private final ProjectResource projectResource;
    private final ProductInstanceClient productInstanceClient;
    private final ProductAgreementOrchestrator productAgreementOrchestrator;
    private JSONSerializer jsonSerializer;

    public ProductAgreementResourceHandler(final Presenter presenter,
                                           ProjectResource projectResource,
                                           JSONSerializer jsonSerializer, ProductInstanceClient productInstanceClient, ProductAgreementOrchestrator productAgreementOrchestrator) {
        super(presenter);
        this.projectResource = projectResource;
        this.jsonSerializer = jsonSerializer;
        this.productInstanceClient = productInstanceClient;
        this.productAgreementOrchestrator = productAgreementOrchestrator;
    }

    @GET
    @Path("/serviceLevelAgreementValidation")
    public Response serviceLevelAgreementValidation(@PathParam(CUSTOMER_ID) String customerId,
                                              @PathParam(CONTRACT_ID) String contractId,
                                              @PathParam(PROJECT_ID) String projectId,
                                              @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                              @PathParam(QUOTE_OPTION_ITEM) String quoteOptionItemId,
                                              @PathParam(LINE_ITEM_ARRAY) String lineItemArray) {

        String excludedProductsFromSlaBulkConfig = excludedProductsFromSlaBulkConfig(lineItemArray).trim();
        AssetDTO parentDto = productInstanceClient.getAssetDTO(new LineItemId(quoteOptionItemId));
        final String productCode = parentDto.getProductCode();
        boolean isComplexContractProduct = productAgreementOrchestrator.isComplexContractCustomer(Long.parseLong(contractId));
        String isPMFLookupAvailable = productAgreementOrchestrator.getValueFromDefaultSlaMatrix(productCode, PRODUCT_SLA_APPLICABLE);
        LOGGER.slaDecider(isComplexContractProduct, isPMFLookupAvailable, productCode);
        if (excludedProductsFromSlaBulkConfig.length() > 0) {
            return Response.ok().entity("Few of the below products cannot be configured in bulk.\n \n[" + excludedProductsFromSlaBulkConfig.replaceAll(" ", ",")+" ]\n\n").build();

        } else if (!isComplexContractProduct
                && (PMF_LOOKUP_NOT_AVAILABLE.equalsIgnoreCase(isPMFLookupAvailable)
                || Strings.isNullOrEmpty(isPMFLookupAvailable))) {

            LOGGER.slaDecider(isComplexContractProduct, isPMFLookupAvailable, productCode);
            return Response.ok().entity("Service Level Agreement are not defined for this product\n\n\n").build();

        } else {
            return Response.ok().entity("").build();
        }

    }


    @GET
    @Path("/serviceLevelAgreementForm")
    public Response serviceLevelAgreementForm(@PathParam(CUSTOMER_ID) String customerId,
                                              @PathParam(CONTRACT_ID) String contractId,
                                              @PathParam(PROJECT_ID) String projectId,
                                              @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                              @PathParam(QUOTE_OPTION_ITEM) String quoteOptionItemId,
                                              @PathParam(LINE_ITEM_ARRAY) String lineItemArray) {


        AssetDTO parentDto = productInstanceClient.getAssetDTO(new LineItemId(quoteOptionItemId));
        final String productCode = parentDto.getProductCode();
        boolean isComplexContractProduct = productAgreementOrchestrator.isComplexContractCustomer(Long.parseLong(contractId));
        String isPMFLookupAvailable = productAgreementOrchestrator.getValueFromDefaultSlaMatrix(productCode, PRODUCT_SLA_APPLICABLE);
        LOGGER.slaDecider(isComplexContractProduct, isPMFLookupAvailable, productCode);


        ProductAgreementDetailsView view = productAgreementOrchestrator.buildServiceLevelAgreementView(customerId, contractId, projectId, quoteOptionId,
                quoteOptionItemId, isComplexContractProduct, lineItemArray);
        String page = presenter.render(view("ServiceLevelAgreementForm.ftl").withContext("view", view));

        if (page != null) {
            return Response.ok().entity(page).build();
        } else {
            LOGGER.readError(quoteOptionItemId);
            return Response.status(INTERNAL_SERVER_ERROR).build();
        }

    }

    @POST
    @Path("/persistServiceLevelAgreementId")
    @Produces(MediaType.APPLICATION_JSON)
    public Response persistServiceLevelAgreementId(@PathParam(QUOTE_OPTION_ITEM) String quoteOptionItemId,
                                                   @PathParam(LINE_ITEM_ARRAY) String lineItemArray,
                                                   @QueryParam("selectedSlaId") String selectedSlaId
                                                   ) {


        List<String> lineItemIds = Arrays.asList(lineItemArray.split(","));
        if (lineItemIds.size() > 1) {
            boolean writeError = true;
            for (String lineItemId : lineItemIds) {
                if (!productAgreementOrchestrator.persistServiceLevelAgreementId(lineItemId, selectedSlaId)) {
                    writeError = false;
                }
            }
            if (writeError) {
                return Response.ok().build();
            } else {
                LOGGER.writeError(selectedSlaId);
                return Response.status(INTERNAL_SERVER_ERROR).build();
            }

        } else if (productAgreementOrchestrator.persistServiceLevelAgreementId(quoteOptionItemId, selectedSlaId)) {
            return Response.ok().build();
        } else {
            LOGGER.writeError(selectedSlaId);
            return Response.status(INTERNAL_SERVER_ERROR).build();
        }

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/serviceLevelAgreements")
    public Response getServiceLevelAgreements(@PathParam(CUSTOMER_ID) String customerId,
                                              @PathParam(CONTRACT_ID) String contractId,
                                              @PathParam(QUOTE_OPTION_ITEM) String quoteOptionItemId) {

        ProductAgreementDetailsDTO productAgreementDetailsDTO = productAgreementsDetailsDTO(quoteOptionItemId, contractId, customerId);

        if (productAgreementDetailsDTO != null) {
            return Response.ok().entity(productAgreementDetailsDTO).build();
        } else {
            LOGGER.readError(quoteOptionItemId);
            return Response.status(INTERNAL_SERVER_ERROR).build();

        }

    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/slaListForFilters")
    public Response getFilteredResultsBasedOnCountry(@QueryParam("cityId") final String cityId,
                                                     @QueryParam("typeName") final String typeName,
                                                     @PathParam(CUSTOMER_ID) String customerId) {

        ProductAgreementDetailsDTO productAgreementDetailsDTO = productAgreementOrchestrator.buildSlaJsonResponse(typeName, cityId, customerId);

        return Response.ok().entity(productAgreementDetailsDTO).build();


    }

    @GET
    @Path("/maintainerAgreementForm")
    public Response maintainerAgreementForm(@PathParam(CUSTOMER_ID) String customerId,
                                              @PathParam(CONTRACT_ID) String contractId,
                                              @PathParam(PROJECT_ID) String projectId,
                                              @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                              @PathParam(QUOTE_OPTION_ITEM) String quoteOptionItemId,
                                              @PathParam(LINE_ITEM_ARRAY) String lineItemArray) {

        boolean isComplexContractProduct = productAgreementOrchestrator.isComplexContractCustomer(Long.parseLong(contractId));
        ProductAgreementDetailsView view = productAgreementOrchestrator.buildMaintainerAgreementAgreementView(customerId, contractId, projectId, quoteOptionId,
                                                                                  quoteOptionItemId, isComplexContractProduct, lineItemArray);
        String page = presenter.render(view("MaintainerAgreementForm.ftl").withContext("view", view));

        if (page != null) {
            return Response.ok().entity(page).build();
        } else {
            LOGGER.readError(quoteOptionItemId);
            return Response.status(INTERNAL_SERVER_ERROR).build();
        }

    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/maintainerAgreements")
    public Response getMaintainerAgreements(@PathParam(CUSTOMER_ID) String customerId,
                                              @PathParam(CONTRACT_ID) String contractId,
                                              @PathParam(QUOTE_OPTION_ITEM) String quoteOptionItemId) {

        ProductAgreementDetailsDTO productAgreementDetailsDTO;
        AssetDTO parentDto = productInstanceClient.getAssetDTO(new LineItemId(quoteOptionItemId));
        String existingMagId = parentDto.getMagId();

        if (!Strings.isNullOrEmpty(existingMagId)) {
            productAgreementDetailsDTO = productAgreementOrchestrator.buildMagJsonResponseForMagId(existingMagId);
        } else {
            productAgreementDetailsDTO = productAgreementOrchestrator.buildMagJsonResponse(customerId);
        }

        if (productAgreementDetailsDTO != null) {
            return Response.ok().entity(productAgreementDetailsDTO).build();
        } else {
            LOGGER.readError(quoteOptionItemId);
            return Response.status(INTERNAL_SERVER_ERROR).build();

        }

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/magListForFilters")
    public Response getMaintainerAgreementListForFilters(@PathParam(CUSTOMER_ID) String customerId,
                                                         @QueryParam("typeSelected") String typeSelected,
                                                         @QueryParam("maintainerSelected") String maintainerSelected,
                                                         @QueryParam("countrySelected") String countrySelected) {

        ProductAgreementDetailsDTO productAgreementDetailsDTO = productAgreementOrchestrator.buildMagJsonResponse(typeSelected,maintainerSelected,countrySelected, customerId);

        return Response.ok().entity(productAgreementDetailsDTO).build();
    }

    @POST
    @Path("/persistMaintainerAgreementId")
    @Produces(MediaType.APPLICATION_JSON)
    public Response persistMaintainerAgreementId(@PathParam(QUOTE_OPTION_ITEM) String quoteOptionItemId,
                                                 @QueryParam("selectedMagId") String selectedMagId,
                                                 @PathParam(LINE_ITEM_ARRAY) String lineItemArray) {

        List<String> lineItemIds = Arrays.asList(lineItemArray.split(","));
        if (lineItemIds.size() > 1) {
            boolean writeError = true;
            for (String lineItemId : lineItemIds) {
                if (!productAgreementOrchestrator.persistMaintainerAgreementId(lineItemId, selectedMagId)) {
                    writeError = false;
                }
            }
            if (writeError) {
                return Response.ok().build();
            } else {
                LOGGER.writeError(selectedMagId);
                return Response.status(INTERNAL_SERVER_ERROR).build();
            }

        } else if (productAgreementOrchestrator.persistMaintainerAgreementId(quoteOptionItemId, selectedMagId)) {
            return Response.ok().build();
        } else {
            LOGGER.writeError(selectedMagId);
            return Response.status(INTERNAL_SERVER_ERROR).build();
        }

    }

    private ProductAgreementDetailsDTO productAgreementsDetailsDTO(String quoteOptionItemId, String contractId, String customerId) {
        ProductAgreementDetailsDTO productAgreementDetailsDTO = null;
        try {
            AssetDTO parentDto = productInstanceClient.getAssetDTO(new LineItemId(quoteOptionItemId));

            String slaIdFromPPSR;
            final String productCode = parentDto.getProductCode();

            boolean isComplexContractProduct = productAgreementOrchestrator.isComplexContractCustomer(Long.parseLong(contractId));
            String isPMFLookupAvailable = productAgreementOrchestrator.getValueFromDefaultSlaMatrix(productCode, PRODUCT_SLA_APPLICABLE);
            String defaultSlaIdFromRuleSetMatrix = productAgreementOrchestrator.getValueFromDefaultSlaMatrix(productCode, DEFAULT_SLA);
            String existingSlaId = parentDto.getSlaId();

            if (!Strings.isNullOrEmpty(existingSlaId)) {
                productAgreementDetailsDTO = productAgreementOrchestrator.buildSlaJsonResponseForSlaId(existingSlaId);
            } else {

                if (!Strings.isNullOrEmpty(defaultSlaIdFromRuleSetMatrix)) {
                    LOGGER.slaIdValue(defaultSlaIdFromRuleSetMatrix);
                    productAgreementOrchestrator.persistServiceLevelAgreementId(quoteOptionItemId, defaultSlaIdFromRuleSetMatrix);
                    productAgreementDetailsDTO = productAgreementOrchestrator.buildSlaJsonResponseForSlaId(defaultSlaIdFromRuleSetMatrix);
                }
                if (!isComplexContractProduct) {

                    if (PMF_LOOKUP_AVAILABLE.equalsIgnoreCase(isPMFLookupAvailable)) {
                        slaIdFromPPSR = parentDto.getCharacteristic(SLA_ID_ATTRIBUTE).getValue();
                        LOGGER.slaIdValueFromPPSR(slaIdFromPPSR);
                        if (!Strings.isNullOrEmpty(slaIdFromPPSR)) {
                            productAgreementOrchestrator.persistServiceLevelAgreementId(quoteOptionItemId, slaIdFromPPSR);

                            return productAgreementOrchestrator.buildSlaJsonResponseForSlaId(slaIdFromPPSR);
                        }
                    } else if (PMF_LOOKUP_NOT_AVAILABLE.equalsIgnoreCase(isPMFLookupAvailable)|| Strings.isNullOrEmpty(isPMFLookupAvailable)) {

                        return productAgreementDetailsDTO;
                    }


                } else {
                    if (PMF_LOOKUP_AVAILABLE.equalsIgnoreCase(isPMFLookupAvailable)) {
                        slaIdFromPPSR = parentDto.getCharacteristic(SLA_ID_ATTRIBUTE).getValue();
                        LOGGER.slaIdValueFromPPSR(slaIdFromPPSR);
                        if (!Strings.isNullOrEmpty(slaIdFromPPSR)) {
                            productAgreementOrchestrator.persistServiceLevelAgreementId(quoteOptionItemId, slaIdFromPPSR);

                            return productAgreementOrchestrator.buildSlaJsonResponseForSlaId(slaIdFromPPSR);
                        }
                    } else if (PMF_LOOKUP_NOT_AVAILABLE.equalsIgnoreCase(isPMFLookupAvailable) || Strings.isNullOrEmpty(isPMFLookupAvailable)) {
                        if (!Strings.isNullOrEmpty(defaultSlaIdFromRuleSetMatrix)) {
                            return productAgreementDetailsDTO;
                        } else {
                            return productAgreementOrchestrator.buildSlaJsonResponse(customerId);
                        }

                    }

                }
            }
        } catch (Exception e) {
            LOGGER.errorWhileBuildingProductAgreementDetailsDTO(e);
        }
        return productAgreementDetailsDTO;
    }

    private String excludedProductsFromSlaBulkConfig(String commaSeparatedListOfSelectedLineItemIds) {
        StringBuilder productNames = new StringBuilder("");
        List<String> lineItemIds = Arrays.asList(commaSeparatedListOfSelectedLineItemIds.split(","));

        if (lineItemIds.size() > 1) {
            for (String lineItem : lineItemIds) {
                AssetDTO parentDto = productInstanceClient.getAssetDTO(new LineItemId(lineItem));
                final String productCode = parentDto.getProductCode();
                String productName = productAgreementOrchestrator.getValueFromDefaultSlaMatrix(productCode, PRODUCT_NAME);
                if (!Strings.isNullOrEmpty(productName)) {
                    productNames.append(" ").append(productName);
                }
            }
        }
        return productNames.toString();
    }

    public class Info {
        private String message;

        public Info(String message) {
            this.message = message;
        }


        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    interface Logger {


        @Log(level = LogLevel.INFO, format = "SLA 's not defined :isComplexContractCustomer value ==> [%s], isPMFLookupAvailable Value ==> [%s] productCode==>[%s] ")
        void slaDecider(boolean isComplexContractProduct, String isPMFLookupAvailable, String productCode);


        @Log(level = LogLevel.ERROR, format = " Error While Retriving Sla Details From Bfg For lineItem==> [%s]")
        void readError(String quoteOptionItemId);

        @Log(level = LogLevel.ERROR, format = " Error While persisting the slaId into CIF For slaId ==> [%s]")
        void writeError(String slaId);

        @Log(level = LogLevel.INFO, format = " slaId value from PPSR is ==> [%s]")
        void slaIdValueFromPPSR(String slaIdFromPPSR);

        @Log(level = LogLevel.INFO, format = " slaId value is ==> [%s]")
        void slaIdValue(String slaId);

        @Log(level = LogLevel.ERROR, format = "error While Building ProductAgreementDetailsDTO")
        void errorWhileBuildingProductAgreementDetailsDTO(Exception e);


    }


}

