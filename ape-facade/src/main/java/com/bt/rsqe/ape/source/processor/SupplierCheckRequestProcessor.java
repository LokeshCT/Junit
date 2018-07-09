package com.bt.rsqe.ape.source.processor;

import com.bt.rsqe.ape.ApeOnnetBuildingResourceHandlerClient;
import com.bt.rsqe.ape.config.CallbackUriConfig;
import com.bt.rsqe.ape.config.SupplierCheckConfig;
import com.bt.rsqe.ape.config.TimeoutConfig;
import com.bt.rsqe.ape.dto.AvailabilityParam;
import com.bt.rsqe.ape.dto.AvailabilitySet;
import com.bt.rsqe.ape.dto.OnnetBuildingAvailabilityPerSiteDTO;
import com.bt.rsqe.ape.dto.OnnetCheckRequestDTO;
import com.bt.rsqe.ape.dto.Supplier;
import com.bt.rsqe.ape.dto.SupplierCheckRequest;
import com.bt.rsqe.ape.dto.SupplierProduct;
import com.bt.rsqe.ape.dto.SupplierSite;
import com.bt.rsqe.ape.exception.DslEfmNotSupportedForRequestedSites;
import com.bt.rsqe.ape.repository.entities.SupplierCheckClientRequestEntity;
import com.bt.rsqe.ape.repository.entities.SupplierRequestSiteEntity;
import com.bt.rsqe.ape.repository.entities.SupplierSiteEntity;
import com.bt.rsqe.ape.source.OnnetDetailsOrchestrator;
import com.bt.rsqe.ape.source.SupplierProductStore;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.utils.AssertObject;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.perf4j.StopWatch;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.ape.config.TimeoutConfig.*;
import static com.bt.rsqe.ape.constants.SupplierProductConstants.*;
import static com.bt.rsqe.ape.dto.SupplierStatus.*;
import static com.bt.rsqe.ape.source.SupplierCheckRequestInvoker.*;
import static com.bt.rsqe.ape.source.SupplierProductHelper.*;
import static com.bt.rsqe.ape.source.SupplierProductStore.*;
import static com.bt.rsqe.customerinventory.dto.AvailabilityType.*;
import static com.google.common.collect.Lists.*;

public class SupplierCheckRequestProcessor {


    private SupplierCheckRequestLogger logger = LogFactory.createDefaultLogger(SupplierCheckRequestLogger.class);
    private CustomerResource customerResource;
    private RequestBuilder requestBuilder;
    private SupplierCheckConfig config;
    private ApeOnnetBuildingResourceHandlerClient apeOnnetBuildingResourceHandlerClient;
    private OnnetDetailsOrchestrator onnetDetailsOrchestrator;

    public SupplierCheckRequestProcessor(SupplierCheckConfig config,
                                         CustomerResource customerResource,
                                         RequestBuilder requestBuilder,
                                         ApeOnnetBuildingResourceHandlerClient apeOnnetBuildingResourceHandlerClient,
                                         OnnetDetailsOrchestrator onnetDetailsOrchestrator) {
        this.config = config;
        this.customerResource = customerResource;
        this.requestBuilder = requestBuilder;
        this.apeOnnetBuildingResourceHandlerClient = apeOnnetBuildingResourceHandlerClient;
        this.onnetDetailsOrchestrator=onnetDetailsOrchestrator;
    }

    public void initiateSupplierProductRequest(SupplierCheckRequest request) throws DslEfmNotSupportedForRequestedSites, Exception {
        logger.processingSupplierProductRequest(request.getParentRequestId());
        String requestMessage = requestBuilder.build(getRequestWithSupportedSites(request), SUPPLIER_LIST_TEMPLATE);
        storeLog(request.getRequestId(), REQUEST, OPERATION_GET_SUPPLIER_PRODUCT_LIST, requestMessage, request.getUser(), "");
        String responseMessage = sendMessage(config.getEndpointUriConfig().getUri(), SOAP_ACTION_GET_SUPPLIER_PRODUCT_LIST, requestMessage);
        storeLog(request.getRequestId(), REQUEST, OPERATION_GET_SUPPLIER_PRODUCT_LIST, responseMessage, request.getUser(), "");
        validateAcknowledgementResponse(responseMessage, request, RESPONSE_GET_GET_SUPPLIER_PRODUCT_LIST);
    }

    public void initiateOnNetAvailabilityRequest(SupplierCheckRequest request) throws Exception {
        logger.processingOnNetAvailabilityRequest(request.getParentRequestId());
        List<OnnetBuildingAvailabilityPerSiteDTO> getOnnetBuildingAvailabilityStatusList=getRequestWithSupportedSitesForOnNet(request);
        onnetDetailsOrchestrator.updateOnNetAvailability(getOnnetBuildingAvailabilityStatusList);
    }

    public void initiateDSLSupplierProductRequest(SupplierCheckRequest request) throws Exception {
        storeSacApeRequestLog(request, OPERATION_GET_SUPPLIER_PRODUCT_LIST, null);
        //storeSupplierSiteUsingRequest(request, config);
        String requestMessage = requestBuilder.build(request, SUPPLIER_LIST_TEMPLATE);
        storeLog(request.getRequestId(), REQUEST, OPERATION_GET_SUPPLIER_PRODUCT_LIST, requestMessage, request.getUser(), "");
        String responseMessage = sendMessage(config.getEndpointUriConfig().getUri(), SOAP_ACTION_GET_SUPPLIER_PRODUCT_LIST, requestMessage);
        storeLog(request.getRequestId(), REQUEST, OPERATION_GET_SUPPLIER_PRODUCT_LIST, responseMessage, request.getUser(), "");
    }

    public String validateAcknowledgementResponse(String response, SupplierCheckRequest request, String acknowledgementFor) throws Exception {
        String status = null;

        Document doc = getDocument(response);
        NodeList supplierProductListResult = doc.getElementsByTagName(acknowledgementFor);
        for (int inputNodeIndex = 0; inputNodeIndex < supplierProductListResult.getLength(); inputNodeIndex++) {
            Node requestNode = supplierProductListResult.item(inputNodeIndex);
            if (requestNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) requestNode;
                status = getElementValue(element, "a:status");
                if (status.contains("[Failure]") && request!=null) {
                    updateSupplierSiteStatusAsFailure(request, status, getElementValue(element, "a:description"));
                }
            }
        }
        return status;
    }

    public String validateAcknowledgementResponseLatest(String response, SupplierCheckRequest request, String acknowledgementFor) throws Exception {
        String status = null;

        Document doc = getDocument(response);
        NodeList supplierProductListResult = doc.getElementsByTagName(acknowledgementFor);
        for (int inputNodeIndex = 0; inputNodeIndex < supplierProductListResult.getLength(); inputNodeIndex++) {
            Node requestNode = supplierProductListResult.item(inputNodeIndex);
            if (requestNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) requestNode;
                status = getElementValue(element, "a:status");
                if (status.contains("[Failure]") && request!=null) {
                    updateSupplierSiteStatusAsFailureLatest(request, status, getElementValue(element, "a:description"));
                }
            }
        }
        return status;
    }

    private SupplierCheckRequest getRequestWithSupportedSites(SupplierCheckRequest request) throws DslEfmNotSupportedForRequestedSites, Exception {
        logger.filteringNonSupportedSites(request.getParentRequestId());
        SiteResource siteResource = customerResource.siteResource(request.getCustomerId());
        SupplierCheckClientRequestEntity clientRequestEntity = getClientRequestById(request.getParentRequestId());
        List<SupplierSite> siteToProcess = updateSupplierSites(convertToSupplierSite(getSupplierSitesBySiteIds(getSiteIds(filterNotSupportedSites(clientRequestEntity.getSupplierRequestSiteEntities())))));
        request.setSupplierSites(siteToProcess);
        if (siteToProcess.isEmpty()) {
            throw new DslEfmNotSupportedForRequestedSites("DSL/EFM is not supported for requested site(s)");
        }
        String requestId = SupplierProductStore.generateApeRequestId();
        request.setRequestId(requestId);
        storeApeRequestLog(request, OPERATION_GET_SUPPLIER_PRODUCT_LIST, null);
        storeSupplierSiteUsingRequest(request, config);
        return new SupplierCheckRequest(requestId, config.getCallbackUriConfig(CallbackUriConfig.CALLBACK_URI).getUri(), siteToProcess);
    }


    private List<OnnetBuildingAvailabilityPerSiteDTO> getRequestWithSupportedSitesForOnNet(SupplierCheckRequest request) throws Exception {
        List<OnnetCheckRequestDTO> onnetCheckRequestDTOList =newArrayList();
        for (SupplierSite site : request.getSupplierSites()) {
            OnnetCheckRequestDTO onnetCheckRequestDTO;

            SiteDTO siteDto = customerResource.siteResource(request.getCustomerId()).getSiteDetails(site.getSiteId().toString());
            if (siteDto !=null){
             onnetCheckRequestDTO =new OnnetCheckRequestDTO(
                siteDto.getSiteId().toString(),
                siteDto.getLatitude(),
                siteDto.getLongitude(),
                siteDto.getCountryName(),
                siteDto.getAccuracyLevel(),
                siteDto.bfgSiteID,
                siteDto.city,
                siteDto.streetName,
                siteDto.postCode,
                siteDto.phoneNumber,
                siteDto.stateCountySProvince,
                siteDto.stateCode
                 ) ;
            }else{
                onnetCheckRequestDTO =new OnnetCheckRequestDTO(
                    site.getSiteId().toString(),null,null,null,0,null,null,null,null,null,null,null) ;
            }
            onnetCheckRequestDTOList.add(onnetCheckRequestDTO);
        }
        List<OnnetBuildingAvailabilityPerSiteDTO> onnetBuildingAvailabilityPerSiteDTOList= apeOnnetBuildingResourceHandlerClient.getOnNetAvailabilityList(onnetCheckRequestDTOList);

          return onnetBuildingAvailabilityPerSiteDTOList;
        }


        private List<SupplierRequestSiteEntity> filterNotSupportedSites(List<SupplierRequestSiteEntity> sites) {
        return newArrayList(Iterables.filter(sites, new Predicate<SupplierRequestSiteEntity>() {
            @Override
            public boolean apply(@Nullable SupplierRequestSiteEntity input) {
                return !NotSupported.name().equalsIgnoreCase(input.getSubStatus());
            }
        }));
    }

    private List<SupplierSite> getSupplierSites(List<SupplierRequestSiteEntity> list, final SiteResource siteResource) {
        return newArrayList(Lists.transform(list, new Function<SupplierRequestSiteEntity, SupplierSite>() {
            @Override
            public SupplierSite apply(@Nullable SupplierRequestSiteEntity input) {
                SiteDTO siteDTO = siteResource.getSiteDetails(input.getSiteId());
                return new SupplierSite(Long.parseLong(input.getSiteId()), siteDTO.getSiteName(), siteDTO.getCountryISOCode(), siteDTO.getCity(), siteDTO.getCountryName(), Grey.getId());
            }
        }));
    }

    private List<SupplierSite> updateSupplierSites(List<SupplierSite> list) {
        return newArrayList(Lists.transform(list, new Function<SupplierSite, SupplierSite>() {
            @Override
            public SupplierSite apply(@Nullable SupplierSite input) {
                return new SupplierSite(input.getSiteId(), input.getSiteName(), input.getCountryISOCode(), input.getCity(), input.getCountryName(), input.getExpiryDate(),
                        Grey.getId(), input.getAvailabilityTelephoneNumber(), input.getErrorDescription(), "GetSupplierList Call Initiated", null);
            }
        }));
    }

    public boolean initiateAvailabilityRequest(SupplierCheckRequest request) throws Exception {
        request.setSyncUri(config.getCallbackUriConfig(CallbackUriConfig.CALLBACK_URI).getUri());
        request.setRequestId(generateApeRequestId());

        List<SupplierSiteEntity> siteEntityList = newArrayList();
        if (SITE.equalsIgnoreCase(request.getLevel())) {
            //process all sites
            siteEntityList = getSupplierSitesBySiteIds(siteIds(request.getSupplierSites()));
        } else {
            //process only requested products
            siteEntityList = getSupplierSitesWithSelectedProducts(request);
        }

        boolean isAutoDone = processAvailabilityRequestForAutoSupplier(request, siteEntityList);
        boolean isManualDone = processAvailabilityRequestForManualSupplier(request, siteEntityList);

        return isAutoDone || isManualDone;
    }


    public void initiateSACAvailabilityRequest(SupplierCheckRequest request) throws Exception {
        request.setSyncUri(config.getCallbackUriConfig(CallbackUriConfig.SAC_CALLBACK_URI).getUri());
        if (AssertObject.isEmpty(request.getRequestId())) {
            request.setRequestId(generateApeRequestId());
        }
        sendSACAvailabilityRequest(request, AUTO);
    }

    private boolean processAvailabilityRequestForAutoSupplier(SupplierCheckRequest request,List<SupplierSiteEntity> siteEntityListDb) throws Exception {
        logger.startedProcessingAutoSupplierRequest(request.getParentRequestId());
        SupplierCheckRequest requestToProcess = cloneRequest(request);
        List<SupplierSiteEntity> siteEntityList = newArrayList();
        List<SupplierSite> supplierSites = newArrayList();

        siteEntityList = getSiteWithAutoSupplier(siteEntityListDb);
        supplierSites = convertToSupplierSite(siteEntityList);
        requestToProcess.setSupplierSites(supplierSites);

        if (requestToProcess.getSupplierSites().size() > 0) {
            if (!"rSQEScheduler".equalsIgnoreCase(request.getSourceSystemName())) {
                excludeInValidProducts(productAttributeValidator(requestToProcess), requestToProcess);
            }
            sendAvailabilityRequest(requestToProcess, AUTO);
            return true;
        } else {
            logger.noValidSiteFoundInARequestForAvailabilityCheckForAuto(request.getParentRequestId());
            return false;
        }

    }

    private boolean processAvailabilityRequestForManualSupplier(SupplierCheckRequest request,List<SupplierSiteEntity> siteEntityListDb) throws Exception {
        logger.startedProcessingManualSupplierRequest(request.getParentRequestId());
        SupplierCheckRequest requestToProcess = cloneRequest(request);
        List<SupplierSiteEntity> siteEntityList = newArrayList();
        List<SupplierSite> supplierSites = newArrayList();

        siteEntityList = getSiteWithManualSupplier(siteEntityListDb);
        supplierSites = convertToSupplierSite(siteEntityList);
        requestToProcess.setSupplierSites(supplierSites);
        if (requestToProcess.getSupplierSites().size() > 0) {
            if (!"rSQEScheduler".equalsIgnoreCase(request.getSourceSystemName())) {
                excludeInValidProducts(productAttributeValidator(requestToProcess), requestToProcess);
            }
            sendAvailabilityRequest(requestToProcess, MANUAL);
            return true;
        } else {
            logger.noValidSiteFoundInARequestForAvailabilityCheckForManual(request.getParentRequestId());
            return false;
        }
    }

    private HashMap<String, Boolean> productAttributeValidator(SupplierCheckRequest request) throws Exception {
        logger.startedValidatingTheRequestIfProductIsHavingAllTheMandatoryAttribute(request.getParentRequestId());
        HashMap<String, Boolean> validationMapForPrduct = new HashMap<String, Boolean>();
        HashMap<String, List<Boolean>> validationMapForSet = new HashMap<String, List<Boolean>>();

        for (SupplierSite site : request.getSupplierSites()) {
            SiteDTO siteDto = customerResource.siteResource(request.getCustomerId()).getSiteDetails(site.getSiteId().toString());
            for (Supplier supplier : site.getSupplierList()) {
                for (SupplierProduct product : supplier.getSupplierProductList()) {
                    List<Boolean> validParamList = newArrayList();
                    for (AvailabilitySet set : product.getAvailabilitySets()) {
                        String key = product.getSpacId();
                        for (AvailabilityParam param : set.getParameterList()) {
                            param.setValue(getParamValue(siteDto, param.getName(), site));
                            validParamList.add(param.getValue() != null ? true : false);
                        }
                        validationMapForSet.put(key, validParamList);
                    }

                    Iterator<Map.Entry<String, List<Boolean>>> entries = validationMapForSet.entrySet().iterator();
                    while (entries.hasNext()) {
                        Map.Entry<String, List<Boolean>> entry = entries.next();
                        validationMapForPrduct.put(product.getSpacId(), entry.getValue().contains(true) ? true : false);
                    }
                }
            }
        }
        logger.finishedValidatingTheRequestIfProductIsHavingAllTheMandatoryAttribute();
        return validationMapForPrduct;
    }

    private String getParamValue(SiteDTO siteDTO, String parameter, SupplierSite site) throws Exception {
        if (TELEPHONE_NUMBER.equalsIgnoreCase(parameter)) {
            return site.getAvailabilityTelephoneNumber();
        } else if (CITY.equalsIgnoreCase(parameter)) {
            return siteDTO.getCity();
        } else if (HOUSE_NUMBER.equalsIgnoreCase(parameter)) {
            return siteDTO.getBuildingNumber();
        } else if (POSTAL_CODE.equalsIgnoreCase(parameter)) {
            return siteDTO.getPostCode();
        } else if (STREET.equalsIgnoreCase(parameter)) {
            return siteDTO.getStreetName();
        } else if (AREA_CODE.equalsIgnoreCase(parameter)) {
            return siteDTO.getTelephoneAreaCode();
        }
        return null;
    }

    private void sendAvailabilityRequest(SupplierCheckRequest request, String availCheckType) throws Exception {
        storeApeRequestLogLatest(request, OPERATION_GET_AVAILABILTY, availCheckType);
        updateTimeoutForSupplierProducts(request, getTimeout(availCheckType));
        String requestMessage = requestBuilder.build(request, AVAILABILITY_TEMPLATE);
        storeLogLatest(request.getRequestId(), REQUEST, OPERATION_GET_AVAILABILTY, "", request.getUser(), "");
        logger.printAvailabilityCheckRequest(requestMessage);
        StopWatch stopwatch = new StopWatch("APE Interaction - sendAvailabilityRequest");
        String responseMessage = sendMessage(config.getEndpointUriConfig().getUri(), SOAP_ACTION_GET_AVAILABILTY, requestMessage);
        logger.printAvailabilityCheckResponse(responseMessage, stopwatch);
        storeLogLatest(request.getRequestId(), RESPONSE, OPERATION_GET_AVAILABILTY, "", request.getUser(), "");
        validateAcknowledgementResponseLatest(responseMessage, request, RESPONSE_GET_AVAILABILTY);
    }

    private void sendSACAvailabilityRequest(SupplierCheckRequest requestToProcess, String availCheckType) throws IOException {
        storeSacApeRequestLog(requestToProcess, OPERATION_GET_AVAILABILTY, availCheckType);
        //storeApeRequestLog(requestToProcess, OPERATION_GET_AVAILABILTY, availCheckType);
        String requestMessage = requestBuilder.build(requestToProcess, AVAILABILITY_TEMPLATE);
        storeLog(requestToProcess.getRequestId(), REQUEST, OPERATION_GET_AVAILABILTY, requestMessage, requestToProcess.getUser(), "");
        String responseMessage = sendMessage(config.getEndpointUriConfig().getUri(), SOAP_ACTION_GET_AVAILABILTY, requestMessage);
        storeLog(requestToProcess.getRequestId(), RESPONSE, OPERATION_GET_AVAILABILTY, responseMessage, requestToProcess.getUser(), "");
    }

    private int getTimeout(String availCheckType) {
        return AUTO.equalsIgnoreCase(availCheckType) ? config.getServiceConfig().getTimeoutConfig(AVAILABILITY_AUTO).getValue() : config.getServiceConfig().getTimeoutConfig(TimeoutConfig.AVAILABILITY_MANUAL).getValue();
    }

    private interface SupplierCheckRequestLogger {
        @Log(level = LogLevel.INFO, format = "Supplier check request builder Info : '%s'")
        void info(String info);

        @Log(level = LogLevel.INFO, format = "processing SupplierProductRequest : '%s'")
        void processingSupplierProductRequest(String parentRequestId);

        @Log(level = LogLevel.INFO, format = "processing On net Availability Request : '%s'")
        void processingOnNetAvailabilityRequest(String parentRequestId);

        @Log(level = LogLevel.INFO, format = "filtering Non Supported Sites : '%s'")
        void filteringNonSupportedSites(String parentRequestId);

        @Log(level = LogLevel.INFO, format = "started Processing Auto Supplier Request : '%s'")
        void startedProcessingAutoSupplierRequest(String parentRequestId);

        @Log(level = LogLevel.INFO, format = "started Processing Manual Supplier Request : '%s'")
        void startedProcessingManualSupplierRequest(String parentRequestId);

        @Log(level = LogLevel.INFO, format = "no Valid Site Found In A Request For Availability Check For Auto : '%s'")
        void noValidSiteFoundInARequestForAvailabilityCheckForAuto(String parentRequestId);

        @Log(level = LogLevel.INFO, format = "no Valid Site Found In A Request For Availability Check For Manual : '%s'")
        void noValidSiteFoundInARequestForAvailabilityCheckForManual(String parentRequestId);

        @Log(level = LogLevel.INFO, format = "Started Validating The Request If Product Is Having All The Mandatory Attribute : '%s'")
        void startedValidatingTheRequestIfProductIsHavingAllTheMandatoryAttribute(String parentRequestId);

        @Log(level = LogLevel.INFO, format = "Validation finished for Mandatory Attribute")
        void finishedValidatingTheRequestIfProductIsHavingAllTheMandatoryAttribute();

        @Log(level = LogLevel.INFO, format = "Availability Check Request, requestMessage - %s")
        void printAvailabilityCheckRequest(String requestMessage);

        @Log(level = LogLevel.INFO, format = "Availability Check Response, responseMessage - %s, stopwatch = %s")
        void printAvailabilityCheckResponse(String responseMessage, StopWatch stopwatch);
    }
}
