package com.bt.rsqe.ape;

import com.bt.cqm.client.SACAvailabilityCheckerClient;
import com.bt.rsqe.ape.config.RedirectUriConfig;
import com.bt.rsqe.ape.config.SupplierCheckConfig;
import com.bt.rsqe.ape.config.TimeoutConfig;
import com.bt.rsqe.ape.dto.SiteAvailabilityStatus;
import com.bt.rsqe.ape.dto.StatusResponse;
import com.bt.rsqe.ape.dto.SupplierCheckRequest;
import com.bt.rsqe.ape.dto.SupplierProduct;
import com.bt.rsqe.ape.dto.SupplierSite;
import com.bt.rsqe.ape.dto.sac.SacApeStatus;
import com.bt.rsqe.ape.dto.sac.SacBulkInputDTO;
import com.bt.rsqe.ape.dto.sac.SacSiteDTO;
import com.bt.rsqe.ape.dto.sac.SacSupplierProdAvailDTO;
import com.bt.rsqe.ape.exception.DslEfmNotSupportedForRequestedSites;
import com.bt.rsqe.ape.exception.SACUnSupportedCountryException;
import com.bt.rsqe.ape.repository.entities.SacBulkUploadEntity;
import com.bt.rsqe.ape.repository.entities.SacRequestEntity;
import com.bt.rsqe.ape.repository.entities.SacSupplierProdAvailEntity;
import com.bt.rsqe.ape.repository.entities.SupplierCheckClientRequestEntity;
import com.bt.rsqe.ape.repository.entities.SupplierRequestSiteEntity;
import com.bt.rsqe.ape.repository.entities.SupplierSiteEntity;
import com.bt.rsqe.ape.source.OnnetDetailsOrchestrator;
import com.bt.rsqe.ape.source.SupplierProductStore;
import com.bt.rsqe.ape.source.processor.RequestBuilder;
import com.bt.rsqe.ape.source.processor.SupplierCheckRequestProcessor;
import com.bt.rsqe.ape.source.scheduler.SacScheduler;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.Icon;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.utils.AssertObject;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static com.bt.rsqe.ape.config.CallbackUriConfig.*;
import static com.bt.rsqe.ape.constants.SupplierProductConstants.*;
import static com.bt.rsqe.ape.dto.SupplierStatus.*;
import static com.bt.rsqe.ape.source.SupplierProductHelper.*;
import static com.bt.rsqe.ape.source.SupplierProductStore.*;
import static com.bt.rsqe.customerinventory.dto.AvailabilityType.*;
import static com.bt.rsqe.utils.AssertObject.isNull;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;

@Path("/rsqe/ape-facade/supplier-check-service/")
public class SupplierProductResourceHandler {

    private SupplierCheckConfig config;
    private CustomerResource customerResource;
    private SupplierCheckRequestProcessor requestProcessor;
    private SACAvailabilityCheckerClient sacAvailabilityCheckerClient;
    private SupplierProductResourceClient supplierProductResourceClient;
    private ExecutorService executorService;
    private SupplierProductResourceHandlerLogger logger = LogFactory.createDefaultLogger(SupplierProductResourceHandlerLogger.class);
    private static Logger LOG = LoggerFactory.getLogger(SupplierProductResourceHandler.class);
   private OnnetDetailsOrchestrator onnetDetailsOrchestrator;

    public SupplierProductResourceHandler(SupplierCheckConfig config,
                                          ExecutorService executorService,
                                          SupplierProductResourceClient supplierProductResourceClient,
                                          CustomerResource customerResource,
                                          SACAvailabilityCheckerClient sacAvailabilityCheckerClient,
                                          RequestBuilder requestBuilder,
                                          ApeOnnetBuildingResourceHandlerClient apeOnnetBuildingResourceHandlerClient,
                                          OnnetDetailsOrchestrator onnetDetailsOrchestrator) {
        this.config = config;
        this.customerResource = customerResource;
        this.executorService = executorService;
        this.supplierProductResourceClient = supplierProductResourceClient;
        this.sacAvailabilityCheckerClient = sacAvailabilityCheckerClient;
        this.onnetDetailsOrchestrator=onnetDetailsOrchestrator;
        this.requestProcessor = new SupplierCheckRequestProcessor(config, customerResource, requestBuilder, apeOnnetBuildingResourceHandlerClient, onnetDetailsOrchestrator);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("initiate-supplier-check-process/request")
    public Response initiateSupplierCheckProcess(SupplierCheckRequest request) {
        logger.initiateSupplierCheckProcess(request);
        long startTime = System.currentTimeMillis();

        logger.printRequest("initiate-supplier-check-process", getAsJsonString(request));

        StatusResponse statusResponse = validateRequest(request);
        String parentRequestId = null;

        try {
            parentRequestId = generateClientRequestId();
            request.setParentRequestId(parentRequestId);
            storeLog(request.getParentRequestId(), REQUEST, CLIENT_REQUEST_GET_PRODUCT_LIST, request.toString(), request.getUser(), EMPTY_STRING);
            if (statusResponse != null) {
                return ResponseBuilder.badRequest().withEntity(statusResponse).build();
            }
            SupplierCheckRequest OnNetRequest = cloneRequest(request);
            if ("system".equalsIgnoreCase(request.getTriggerType())) {
                prepareDslRequestData(request);
                onnetDetailsOrchestrator.prepareOnNetRequestData(OnNetRequest);
                if (request.getSupplierSites().size() > 0
                    && OnNetRequest.getSupplierSites().size() > 0) {
                    requestProcessor.initiateOnNetAvailabilityRequest(OnNetRequest);
                    requestProcessor.initiateSupplierProductRequest(request);

                } else {
                    statusResponse = new StatusResponse(EMPTY_STRING, SUCCESS, "All the sites in request are already processed", FAILURE_CODE);
                    logger.totalTimeTaken("initiate-supplier-check-process", String.valueOf(System.currentTimeMillis() - startTime));
                    return ResponseBuilder.anOKResponse().withEntity(statusResponse).build();
                }
            } else if ("user".equalsIgnoreCase(request.getTriggerType())) {

                if ("Yes".equalsIgnoreCase(request.getOnNetManual())) {
                    onnetDetailsOrchestrator.prepareOnNetRequestData(OnNetRequest);
                    if (OnNetRequest.getSupplierSites().size() > 0) {
                        requestProcessor.initiateOnNetAvailabilityRequest(OnNetRequest);

                    }
                } else {
                    prepareDslRequestData(request);
                    if (request.getSupplierSites().size() > 0) {
                        requestProcessor.initiateSupplierProductRequest(request);
                    }

                }

            }
        } catch (DslEfmNotSupportedForRequestedSites e) {
            logger.error(e);
            statusResponse = new StatusResponse(parentRequestId, FAILED, e.getMessage(), FAILURE_CODE);
            return ResponseBuilder.anOKResponse().withEntity(statusResponse).build();
        } catch (Exception e) {
            logger.error(e);
            statusResponse = new StatusResponse(parentRequestId, FAILED, "Unable to process the request", FAILURE_CODE);
            return ResponseBuilder.notFound().withEntity(statusResponse).build();
        }
        statusResponse = new StatusResponse(parentRequestId, SUCCESS, "Supplier check request initiated successfully. Please click on refresh button for latest supplier data", SUCCESS_CODE);
        logger.info(statusResponse.toString());
        logger.totalTimeTaken("initiate-supplier-check-process", String.valueOf(System.currentTimeMillis() - startTime));
        return ResponseBuilder.anOKResponse().withEntity(statusResponse).build();
    }

    public String getAsJsonString(SupplierCheckRequest request) {
        try {
            return new GsonBuilder().create().toJson(request);
        } catch (Exception e) {
            return "Error in Parsing Request as Json";
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("create-sac-bulk-upload")
    public Response createSACBulkUpload(SacBulkInputDTO bulkInputDTO) {
        String fileName;

        if (AssertObject.anyEmpty(bulkInputDTO, bulkInputDTO.getSystem(), bulkInputDTO.getUserId(), bulkInputDTO.getSalesChannel())) {

            String errorDesc = String.format("Invalid data. [User Id :%s , System :%s, File Description : %s , Sales Channel :%s", bulkInputDTO.getUserId(), bulkInputDTO.getSystem(), bulkInputDTO.getFileDesc(), bulkInputDTO.getSalesChannel());
            LOG.info("Initiate DSL Supplier Check . Bad SacBulkInputDTO Request ->" + errorDesc);
            return ResponseBuilder.badRequest().withEntity(errorDesc).build();
        }


        try {
            fileName = createSacBulkUpload(bulkInputDTO);
        } catch (Exception ex) {
            LOG.error("Failed to save SAC Bulk Upload Data ..", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

        return Response.status(Response.Status.OK).entity(fileName).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("create-sac-site-entries")
    public Response insertSiteEntries(SacBulkInputDTO bulkInputDTO) {
        String fileName = null;

        if (AssertObject.anyEmpty(bulkInputDTO, bulkInputDTO.getFileName(), bulkInputDTO.getSites())) {

            String errorDesc = String.format("Invalid data. [File Name :%s ,Sites : %s]", bulkInputDTO.getFileName(), bulkInputDTO.getSites());
            LOG.info("Initiate DSL Supplier Check . Bad SacBulkInputDTO Request ->" + errorDesc);
            return ResponseBuilder.badRequest().withEntity(errorDesc).build();
        }

        if (bulkInputDTO.getSites().size() < 1) {
            return ResponseBuilder.badRequest().withEntity("No Sites to persist").build();
        }

        try {
            createSacSiteRequest(bulkInputDTO);
            LOG.info(String.format("Inserted sites to Table for bulk upload file [%s]. Number of sites -> %s ", bulkInputDTO.getFileName(), bulkInputDTO.getSites().size()));

        } catch (Exception ex) {
            LOG.error("Failed to save SAC Bulk Site Entries ..", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

        return Response.status(Response.Status.OK).entity(fileName).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("delete-sac-bulk-upload")
    public Response deleteSACBulkUpload(@QueryParam("fileName") String fileName, @QueryParam("userId") String userId) {
        if (AssertObject.anyEmpty(fileName, userId)) {
            String msg = String.format("Invalid Input. File Name :[%s] , User ID :[%s]", fileName, userId);
            LOG.info(msg);
            return ResponseBuilder.badRequest().withEntity(msg).build();
        }

        if (fileName.contains(userId)) {

            try {
                deleteSacBulkUpload(fileName);
                return Response.status(Response.Status.OK).build();
            } catch (Exception ex) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update-sac-bulk-upload")
    public Response updateSACBulkUpload(SacBulkInputDTO bulkInputDTO) {
        String fileName = null;
        LOG.info("Inside UpdateSACBulkUpload WS ..");
        if (AssertObject.anyEmpty(bulkInputDTO, bulkInputDTO.getSystem(), bulkInputDTO.getUserId())) {
            String errorDesc = String.format("Invalid data. [User Id :%s , System :%s]", bulkInputDTO.getUserId(), bulkInputDTO.getSystem());
            LOG.info("Initiate DSL Supplier Check . Bad SacBulkInputDTO Request ->" + errorDesc);
            return ResponseBuilder.badRequest().withEntity(errorDesc).build();
        }

        try {
            updateSacBulkUpload(bulkInputDTO);
        } catch (Exception ex) {
            LOG.error("Failed to save SAC Bulk Upload Data ..", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

        return Response.status(Response.Status.OK).entity(fileName).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("sac-uploads-in-progress")
    public Response getInValidationSacUploads(@QueryParam("userId") String userId) {
        if (AssertObject.isEmpty(userId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid UserId.").build();
        }

        List<SacBulkUploadEntity> myUploadsEntities = getInProgressUploads(userId);
        List<SacBulkInputDTO> myUploadDtos = null;

        if (myUploadsEntities != null && myUploadsEntities.size() > 0) {
            myUploadDtos = newArrayList();

            for (SacBulkUploadEntity entity : myUploadsEntities) {
                myUploadDtos.add(entity.toShallowDto());
            }

            return Response.status(Response.Status.OK).entity(new GenericEntity<List<SacBulkInputDTO>>(myUploadDtos) {
            }).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("sac-all-user-reports")
    public Response getAllSacReports() {

        int visbileMonths = config.getServiceConfig().getTimeoutConfig(TimeoutConfig.SAC_RECORDS_VISBILE_MONTHS).getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - visbileMonths);

        List<SacBulkUploadEntity> myUploadsEntities = getAllProcessingReports(calendar.getTime());
        List<SacBulkInputDTO> myUploadDtos = null;

        if (myUploadsEntities != null && myUploadsEntities.size() > 0) {
            myUploadDtos = new ArrayList<SacBulkInputDTO>();

            for (SacBulkUploadEntity entity : myUploadsEntities) {
                myUploadDtos.add(entity.toShallowDto());
            }

            return Response.status(Response.Status.OK).entity(new GenericEntity<List<SacBulkInputDTO>>(myUploadDtos) {
            }).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("initiate-sac-supplier-fetch")
    public Response initiateSACSupplierFetch(SacBulkInputDTO bulkInputDTO) throws Exception {
        List<SacRequestEntity> siteRequestsEntities = null;
        List<SacSiteDTO> siteRequestsEntitiesFor3rdReq = null;

        if (AssertObject.anyEmpty(bulkInputDTO.getSystem(), bulkInputDTO.getUserId(), bulkInputDTO.getFileName())) {
            String errorDesc = String.format("Invalid data. [User Id :%s , System : %s, File Name : %s]", bulkInputDTO.getUserId(), bulkInputDTO.getSystem(), bulkInputDTO.getFileName());
            LOG.info("Initiate DSL Supplier Check . Bad SacBulkInputDTO Request ->" + errorDesc);
            return ResponseBuilder.badRequest().withEntity(errorDesc).build();
        }
        LOG.info(String.format("Inside SAC Supplier Fetch Service initiateSACSupplierFetch(). File Name : [%s]....", bulkInputDTO.getFileName()));

        try {

            boolean isProcessing2ndRequest = processSac2ndRequest(bulkInputDTO.getFileName(), bulkInputDTO.getUserId(), bulkInputDTO.getSystem());

            if (!isProcessing2ndRequest) {
                siteRequestsEntities = getAllSacRequestEntity(bulkInputDTO.getFileName());

                if (siteRequestsEntities == null || siteRequestsEntities.size() < 1) {
                    String errorDesc = String.format("No records to be processed for File :", bulkInputDTO.getFileName());
                    return ResponseBuilder.anOKResponse().withEntity(errorDesc).build();
                }

                LOG.info(String.format("Going to process [%s] SAC Site Requests. File Name [%s] ..", siteRequestsEntities.size(), bulkInputDTO.getFileName()));

                Map<String, List<SacSupplierProdAvailDTO>> prodListInLast24HrsMap = new HashMap<>();
                List<SacSiteDTO> siteDTOs = new ArrayList<>();
                for (SacRequestEntity sacRequestEntity : siteRequestsEntities) {
                    siteDTOs.add(sacRequestEntity.toDto());
                }


                for (SacSiteDTO siteDTO : siteDTOs) {
                    List<SacSupplierProdAvailDTO> prodListLast24Hrs;
                    SacSiteDTO sacSiteDTO = siteDTO;

                    if (prodListInLast24HrsMap.get(sacSiteDTO.getCountryIsoCode()) == null) {
                        List<SacSupplierProdAvailEntity> tempProdListLast24Hrs = getUpdatedProductListInLast24Hrs(sacSiteDTO.getCountryIsoCode());
                        if (tempProdListLast24Hrs != null) {
                            LOG.info("SAC :: Fetched Supplier Product received in last 24 hrs for country -> " + sacSiteDTO.getCountryIsoCode());
                            List<SacSupplierProdAvailDTO> sacSupplierProdAvailDTOs = newArrayList();
                            for (SacSupplierProdAvailEntity prodAvailEntity : tempProdListLast24Hrs) {
                                sacSupplierProdAvailDTOs.add(prodAvailEntity.toDto());
                            }
                            prodListInLast24HrsMap.put(sacSiteDTO.getCountryIsoCode(), sacSupplierProdAvailDTOs);
                        }
                    }

                    prodListLast24Hrs = prodListInLast24HrsMap.get(sacSiteDTO.getCountryIsoCode());

                    if (prodListLast24Hrs != null) {
                        if (siteRequestsEntitiesFor3rdReq == null) {
                            siteRequestsEntitiesFor3rdReq = new ArrayList<>();
                        }

                        updateSacSupplierProdToSite(Long.parseLong(sacSiteDTO.getSiteId()), prodListLast24Hrs);
                        Long[] siteIds = new Long[1];
                        siteIds[0] = Long.parseLong(sacSiteDTO.getSiteId());
                        updateSacRequestStatus(siteIds, SacApeStatus.APE_RESPONSE_SUCCESS.getStatus(), sacSiteDTO.getApe2ReqId(), null);
                        sacSiteDTO.setSacSupplierProdAvailDTOs(prodListLast24Hrs);
                        siteRequestsEntitiesFor3rdReq.add(sacSiteDTO);
                    }

                }

                if (siteRequestsEntitiesFor3rdReq != null && siteRequestsEntitiesFor3rdReq.size() > 0) {
                    try {
                        LOG.info(String.format("Going to initiate SAC 3rd Request for fileName :[%s], userId :[%s]", bulkInputDTO.getFileName(), bulkInputDTO.getUserId()));
                        initiateSac3rdRequest(siteRequestsEntitiesFor3rdReq, bulkInputDTO.getUserId(), bulkInputDTO.getSystem());

                        SacScheduler.trackApeResponse(bulkInputDTO.getFileName());
                        LOG.info("Supplier availability check initiated successfully !!");
                        return ResponseBuilder.anOKResponse().withEntity("Supplier availability check initiated successfully").build();

                    } catch (Exception ex) {
                        LOG.error("Failed to initiate SAC 3rd Request :", ex);
                        throw ex;
                    }
                } else {
                    LOG.info("No Valid Sites available to initiate a request.");
                    bulkInputDTO.setAvailabilityStatus(SacApeStatus.APE_RESPONSE_FAILED.getStatus());
                    updateSACBulkUpload(bulkInputDTO);
                    sacAvailabilityCheckerClient.generateAvailabilityReport(bulkInputDTO);
                    return ResponseBuilder.anOKResponse().withEntity("No Valid Sites available to initiate a request.").build();
                }


            } else {
                String msg = String.format("Waiting for the SAC APE 2nd Request to process ..File Name [%s]", bulkInputDTO.getFileName());
                LOG.info(msg);
                return ResponseBuilder.anOKResponse().withEntity(msg).build();
            }
        } catch (Exception ex) {
            LOG.error("Failed to initiate SAC request", ex);
            bulkInputDTO.setAvailabilityStatus(SacApeStatus.APE_RESPONSE_FAILED.getStatus());
            updateSACBulkUpload(bulkInputDTO);
            sacAvailabilityCheckerClient.generateAvailabilityReport(bulkInputDTO);
            return ResponseBuilder.internalServerError().withEntity(ex.getMessage()).build();
        }

    }

    private boolean processSac2ndRequest(String fileName, String userId, String system) throws Exception {
        boolean hasProcessed = false;

        SacRequestEntity sacRequestEntity = getFirstAvailableForProcessingSacRequests(fileName);
        if (sacRequestEntity != null) {
            SacSiteDTO sacSiteDTO = sacRequestEntity.toDto();
            List<String> supportedCountries = getDslEfmSupportedCountries();
            SacApeStatus countryApplicabilityStatus = (supportedCountries.contains(sacSiteDTO.getCountryIsoCode()) ? null : SacApeStatus.COUNTTRY_NOT_SUPPORTED);
            if (countryApplicabilityStatus != null && SacApeStatus.COUNTTRY_NOT_SUPPORTED.equals(countryApplicabilityStatus)) {
                updateSacSiteErrorDesc(fileName, "Country not supported.", SacApeStatus.COUNTTRY_NOT_SUPPORTED.getStatus());
                throw new SACUnSupportedCountryException();
            }

            if (!hasUpdatedProductListInLast24Hrs(sacSiteDTO.getCountryIsoCode())) {
                SupplierCheckRequest request = new SupplierCheckRequest();
                request.setAutoTrigger(YES);
                request.setSourceSystemName(system);
                request.setUser(userId);
                request.setCustomerId("999999");

                SupplierSite supplierObj = new SupplierSite();
                supplierObj.setSiteId(Long.parseLong(sacSiteDTO.getSiteId()));
                supplierObj.setSiteName(sacSiteDTO.getSiteName());
                supplierObj.setAvailabilityTelephoneNumber(sacSiteDTO.getTelephoneNo());
                supplierObj.setCountryISOCode(sacSiteDTO.getCountryIsoCode());
                supplierObj.setCountryName(sacSiteDTO.getCountryName());

                List<SupplierSite> supplierSites = new ArrayList<>();
                supplierSites.add(supplierObj);

                request.setSupplierSites(supplierSites);
                LOG.info("Initiate DSL Supplier Check . Request ->" + request);

                try {

                    String clientReqId = generateClientRequestId();
                    String ape2ReqId = generateApeRequestId();
                    request.setRequestId(ape2ReqId);
                    request.setParentRequestId(clientReqId);

                    request.setSyncUri(config.getCallbackUriConfig(SAC_CALLBACK_URI).getUri());
                    request.setStatus(InProgress.value());
                    storeClientRequestEntity(request);

                    LOG.info(String.format("Going to initiate SAC 2nd Request for country :%s. Request initiated by [%s]", sacSiteDTO.getCountryName(), userId));


                    requestProcessor.initiateDSLSupplierProductRequest(request);
                    Long[] site = {Long.parseLong(sacSiteDTO.getSiteId())};
                    updateSacRequestStatus(site, SacApeStatus.APE_REQUEST_INITIATED.getStatus(), ape2ReqId, null);
                    hasProcessed = true;
                } catch (Exception e) {
                    LOG.error("Failed to Initiate APE Webservice call for 2nd Request ", e);
                    updateSacSiteErrorDesc(fileName, e.getMessage(), SacApeStatus.APE_RESPONSE_FAILED.getStatus());
                    throw e;
                }
            }
        }


        return hasProcessed;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("processSacSupplierProductRequest")
    public Response processSACSupplierProductRequest(SupplierCheckRequest supplierCheckRequest) {
        try {
            requestProcessor.initiateSACAvailabilityRequest(supplierCheckRequest);
            LOG.info(String.format("Processed SAC 3rd Request for APE Request ID %s", supplierCheckRequest.getRequestId()));
        } catch (Exception ex) {
            LOG.error(String.format("Exception while processing SAC 3rd Request for APE Request ID %s", supplierCheckRequest.getRequestId()));
            return ResponseBuilder.internalServerError().build();
        }
        return ResponseBuilder.anOKResponse().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("isReportGenInProgress")
    public Response isSacReportGenInProgress(@QueryParam("userId") String userId) {
        if (AssertObject.isEmpty(userId)) {
            String msg = String.format("Invalid Data. UserId =[%s]", userId);
            return ResponseBuilder.badRequest().withEntity(msg).build();
        }

        Boolean inProg = isAnyReportGenInprogress(userId);

        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<Boolean>(inProg) {
        }).build();
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("initiate-sac-product-availability-check")
    public Response initiateSACProductAvailabilityCheck(SacBulkInputDTO bulkInputDTO) {
        //logger.initiateAvailabilityCheck(request);
        StatusResponse statusResponse;
        String parentRequestId = null;
        LOG.info(String.format("Going to initiate initiateSACProductAvailabilityCheck(). File Name :%s", bulkInputDTO.getFileName()));
        try {
            SupplierCheckRequest request = new SupplierCheckRequest();
            request.setAutoTrigger(YES);
            request.setSourceSystemName(bulkInputDTO.getSystem());
            request.setUser(bulkInputDTO.getUserId());

            parentRequestId = generateClientRequestId();
            request.setParentRequestId(parentRequestId);
            //List<SacRequestEntity> siteRequestsEntities = getAllAvailableSacRequestsForAvailCheck(bulkInputDTO.getFileName());
            List<SacSiteDTO> sacSiteDTOs = bulkInputDTO.getSites();


            initiateSac3rdRequest(sacSiteDTOs, bulkInputDTO.getUserId(), bulkInputDTO.getSystem());

        } catch (Exception e) {
            LOG.error(String.format("Failed to Initiate Availability Webservice call to APE. File Name :%s", bulkInputDTO.getFileName()), e);
            statusResponse = new StatusResponse(parentRequestId, FAILED, "Unable to process the request", FAILURE_CODE);
            return ResponseBuilder.notFound().withEntity(statusResponse).build();
        }

        statusResponse = new StatusResponse(parentRequestId, SUCCESS, "Supplier availability check initiated successfully", SUCCESS_CODE);
        logger.info(statusResponse.toString());
        return ResponseBuilder.anOKResponse().withEntity(statusResponse).build();
    }

    private void initiateSac3rdRequest(List<SacSiteDTO> siteReqDto, String userId, String sourceSys) throws Exception {

        if (siteReqDto == null || siteReqDto.size() < 1) {
            return;
        }
        String parentRequestId = generateClientRequestId();

        List<SupplierSite> supplierSites = convertSacToSupplierSite(siteReqDto);

        List<List<SupplierSite>> subSupplierList = splitSupplierList(supplierSites, 8);

        for (List<SupplierSite> splitSites : subSupplierList) {
            if (splitSites != null && splitSites.size() > 0) {
                String ape3ReqId = null;
                try {
                    Set<Long> siteIds = newHashSet();
                    Long[] uniqueSiteIds = null;
                    final SupplierCheckRequest request = new SupplierCheckRequest();
                    request.setAutoTrigger(YES);
                    request.setSourceSystemName(sourceSys);
                    request.setUser(userId);
                    request.setCustomerId("999999");


                    ape3ReqId = generateApeRequestId();
                    request.setParentRequestId(parentRequestId);
                    request.setRequestId(ape3ReqId);

                    request.setSupplierSites(splitSites);
                    //storeLog(request.getParentRequestId(), REQUEST, CLIENT_REQUEST_AVAILABILITY_CHECK, request.toString(), request.getUser(), EMPTY_STRING);
                    request.setStatus(InProgress.value());
                    request.setSyncUri(config.getCallbackUriConfig(SAC_CALLBACK_URI).getUri());
                    storeClientRequestEntity(request);

                    for (SupplierSite suppSite : splitSites) {
                        siteIds.add(suppSite.getSiteId());
                    }

                    if (siteIds.size() > 0) {
                        uniqueSiteIds = new Long[siteIds.size()];
                    }

                    siteIds.toArray(uniqueSiteIds);

                    updateSacRequestStatus(uniqueSiteIds, null, null, ape3ReqId);
                    LOG.info(String.format("Spawn thread to submit SAC APE 3rd Request. APE Request Id :%s ...", ape3ReqId));

                    /*This ensures multiple threads are spawned without loosing access to datasource*/
                    executorService.submit(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            supplierProductResourceClient.processSacAvailabilityRequestAsynch(request);
                            return null;
                        }
                    });
                    //supplierProductResourceClient.processSacAvailabilityRequestAsynch(request);
                    //Thread.sleep(1000); // Not that fast ..
                } catch (Exception ex) {
                    LOG.error("Exception while processing SAC APE 3rd Request. APE Request ID :" + ape3ReqId, ex);
                }
            }
        }
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("processSacAvailabilityRequest")
    public Response processSACAvailabilityRequest(SupplierCheckRequest supplierCheckRequest) {
        try {
            requestProcessor.initiateSACAvailabilityRequest(supplierCheckRequest);
            LOG.info(String.format("Processed SAC 3rd Request for APE Request ID %s", supplierCheckRequest.getRequestId()));
        } catch (Exception ex) {
            LOG.error(String.format("Exception while processing SAC 3rd Request for APE Request ID %s", supplierCheckRequest.getRequestId()));
            return ResponseBuilder.internalServerError().build();
        }
        return ResponseBuilder.anOKResponse().build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("initiate-availability-check/request")
    public Response initiateAvailabilityCheck(SupplierCheckRequest request) {
        long startTime = System.currentTimeMillis();

        logger.printRequest("initiate-availability-check", getAsJsonString(request));

        StatusResponse statusResponse = validateRequest(request);
        String parentRequestId;
        try {
            parentRequestId = generateClientRequestId();
            request.setParentRequestId(parentRequestId);
            storeLogLatest(request.getParentRequestId(), REQUEST, CLIENT_REQUEST_AVAILABILITY_CHECK, request.toString(), request.getUser(), EMPTY_STRING);
            //check selected sites status = Available/SP Available->Yes
            if (statusResponse != null) {
                return ResponseBuilder.badRequest().withEntity(statusResponse).build();
            }
            request.setStatus(InProgress.value());
            storeClientRequestEntityLatest(request);
            if (!requestProcessor.initiateAvailabilityRequest(request)) {
                statusResponse = new StatusResponse(parentRequestId, FAILED, "One or more manual supplier product where centralized availability supported is NO, Hence request can not be placed.", FAILURE_CODE);
                return ResponseBuilder.anOKResponse().withEntity(statusResponse).build();
            }

        } catch (Exception e) {
            logger.error(e);
            statusResponse = new StatusResponse(EMPTY_STRING, FAILED, "Unable to process the request", FAILURE_CODE);
            return ResponseBuilder.notFound().withEntity(statusResponse).build();
        }

        statusResponse = new StatusResponse(parentRequestId, SUCCESS, "Supplier availability check initiated successfully, please click on refresh button for latest availability status", SUCCESS_CODE);
        logger.info(statusResponse.toString());
        logger.totalTimeTaken("initiate-availability-check", String.valueOf(System.currentTimeMillis() - startTime));
        return ResponseBuilder.anOKResponse().withEntity(statusResponse).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("initiate-availability/request")
    public Response initiateAvailability(SupplierCheckRequest request) {
        logger.initiateAvailabilityCheck(request);
        try {
            if(AssertObject.isNull(request.getParentRequestId())){
                request.setParentRequestId(generateClientRequestId());
            }
            requestProcessor.initiateAvailabilityRequest(request);
        } catch (Exception e) {
            return ResponseBuilder.internalServerError().build();
        }
        return ResponseBuilder.anOKResponse().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("get-supplier-product-sites-for-customer/customer/{customerId}")
    public Response getSupplierProductSitesForCustomer(@PathParam("customerId") String customerId) {
        logger.getSupplierProductSites(customerId);
        long startTime = System.currentTimeMillis();
        if (AssertObject.anyEmpty(customerId)) {
            return ResponseBuilder.badRequest().build();
        }
        List<SupplierSite> results = null;
        try {
            List<SupplierSiteEntity> siteEntityList = getSupplierSitesByCustomerId(Long.parseLong(customerId));
            if (!siteEntityList.isEmpty()) {
                results = convertToSupplierSite(siteEntityList);
            } else {
                return ResponseBuilder.notFound().withEntity(new StatusResponse(ERROR, FAILED, "No site found for this Customer Id", FAILURE_CODE)).build();
            }
        } catch (Exception e) {
            logger.error(e);
        }
        logger.totalTimeTaken("get-supplier-product-sites-for-customer", String.valueOf(System.currentTimeMillis() - startTime));
        return Response.ok().entity(new GenericEntity<List<SupplierSite>>(results) {
        }).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("get-dsl-efm-supported-sites/customer/{customerId}/quoteId/{quoteId}")
    public Response getDslEfmSupportedSites(@PathParam("customerId") String customerId, @PathParam("quoteId") String quoteId) {
        logger.getSupplierProductSites(customerId);
        long startTime = System.currentTimeMillis();
        if (AssertObject.anyEmpty(customerId)) {
            return ResponseBuilder.badRequest().build();
        }
        List<SupplierSite> results = null;
        try {
            SiteResource siteResource = customerResource.siteResource(customerId);
            List<String> supportedCountries = getDslEfmSupportedCountries();

            if (supportedCountries.size() == 0) {
                return ResponseBuilder.anOKResponse().withEntity(new StatusResponse(ERROR, FAILED, "No supported country available at this moment.", FAILURE_CODE)).build();
            }
            List<SiteDTO> supportedSites = filterOutNonSupportedSites(siteResource.getBranchSites(quoteId), supportedCountries);
            storeBatchSupplierSiteUsingSiteDto(getNonExistingSites(supportedSites, getExistingSites(Long.parseLong(customerId))), Long.parseLong(customerId));
            List<SupplierSiteEntity> siteEntityList = getSupplierSitesByCustomerId(Long.parseLong(customerId));
            if (siteEntityList != null) {
                try {
                    results = convertToSupplierSite(siteEntityList);
                } catch (Exception e) {
                    logger.error(e);
                }
            } else {
                return ResponseBuilder.anOKResponse().withEntity(new StatusResponse(ERROR, FAILED, "No DSL/EFM supported site exist for this customer", FAILURE_CODE)).build();
            }
        } catch (Exception e) {
            logger.error(e);
        }
        logger.totalTimeTaken("get-dsl-efm-supported-sites", String.valueOf(System.currentTimeMillis() - startTime));
        return Response.ok().entity(new GenericEntity<List<SupplierSite>>(results) {
        }).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("get-site-availability-status/customer/{customerId}")
    public Response getSiteAvailabilityStatusForCustomer(@PathParam("customerId") String customerId) {
        List<SiteAvailabilityStatus> statuses = newArrayList();
        try {
            statuses = getSiteAvailabilityStatus(Long.parseLong(customerId));
        } catch (Exception e) {
            logger.error(e);
        }
        return Response.ok().entity(new GenericEntity<List<SiteAvailabilityStatus>>(statuses) {
        }).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("get-supplier-product-sites/request")
    public Response getSupplierProductSites(SupplierCheckRequest request) {
        logger.getSupplierProductData(request);
        long startTime = System.currentTimeMillis();
        StatusResponse statusResponse = validateRequest(request);
        if (statusResponse != null) {
            return ResponseBuilder.badRequest().withEntity(statusResponse).build();
        }

        List<SupplierSite> results = null;
        List<SupplierSite> sites = request.getSupplierSites();
        try {
            List<Long> siteIds = siteIds(sites);
            results = convertToSupplierSite(getSupplierSitesBySiteIds(siteIds));
        } catch (Exception e) {
            logger.error(e);
        }
        logger.totalTimeTaken("get-supplier-product-sites", String.valueOf(System.currentTimeMillis() - startTime));
        return Response.ok().entity(new GenericEntity<List<SupplierSite>>(results) {
        }).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update-supplier-product-site-data/request")
    public Response updateSupplierProductSiteData(SupplierCheckRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        StatusResponse statusResponse = validateRequest(request);
        if (statusResponse != null) {
            return ResponseBuilder.badRequest().withEntity(statusResponse).build();
        }
        logger.updateSupplierProductSiteData(request);
        try {
            if (SITE.equalsIgnoreCase(request.getLevel())) {
                //update telephone number
                for (SupplierSite site : request.getSupplierSites()) {
                    updateTelephoneNumber(site.getSiteId().longValue(), site.getAvailabilityTelephoneNumber());
                }
            } else if (PRODUCT.equalsIgnoreCase(request.getLevel())) {
                //update telephone number + product availability  + check availability
                for (SupplierSite site : request.getSupplierSites()) {
                    updateTelephoneNumber(site.getSiteId().longValue(), site.getAvailabilityTelephoneNumber());
                    for (com.bt.rsqe.ape.dto.Supplier supplier : site.getSupplierList()) {
                        for (SupplierProduct product : supplier.getSupplierProductList()) {
                            SupplierProductStore.updateSupplierProductInformation(site.getSiteId().longValue(), product.getSpacId(), product.getProductAvailable(), product.getCheckedReference());

                        }
                    }
                }
            } else {
                return ResponseBuilder.anOKResponse().withEntity(new StatusResponse(ERROR, FAILED, "Unknown Level type", FAILURE_CODE)).build();
            }
        } catch (Exception e) {
            logger.error(e);
            return ResponseBuilder.anOKResponse().withEntity(new StatusResponse(ERROR, FAILED, "Unable to update the requested change", FAILURE_CODE)).build();
        }
        logger.totalTimeTaken("update-supplier-product-site-data", String.valueOf(System.currentTimeMillis() - startTime));
        return ResponseBuilder.anOKResponse().withEntity(new StatusResponse(SUCCESS, "Update", "Requested data updated successfully, please click on refresh button for latest availability status", SUCCESS_CODE)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("get-site-availability/{siteId}")
    public Response getSiteAvailability(@PathParam("siteId") String siteId) throws Exception {
        Icon icon = new Icon();
        icon.setId(getSupplierSiteBySiteId(Long.parseLong(siteId)).getAvailabilityTypeId());

        String url = null;
        if (Green.getId().equals(icon.getId()) || Blue.getId().equals(icon.getId())) {
            url = config.getRedirectUriConfig(RedirectUriConfig.SUPPLIER_PRODUCT_PAGE).getUri() + siteId + "&_dc=" + currentDate().getTime();
        } else if (Red.getId().equals(icon.getId()) || Orange.getId().equals(icon.getId())) {
            String token = UserContextManager.getCurrent().getRsqeToken();
            url = config.getRedirectUriConfig(RedirectUriConfig.SUPPLIER_CHECK_PAGE).getUri() + token;
        }

        icon.setRedirectUri(url);
        icon.setTitle(getById(icon.getId()).getHelpText());

        return ResponseBuilder.anOKResponse().withEntity(icon).build();
    }

    private void prepareDslRequestData(SupplierCheckRequest request) throws Exception {

        SupplierCheckClientRequestEntity clientRequest = new SupplierCheckClientRequestEntity(request.getParentRequestId(), request.getClientCallbackUri(), request.getTriggerType(), request.getAutoTrigger(),
                                                                                              request.getSourceSystemName(), request.getUser(), Long.parseLong(request.getCustomerId()), InProgress.value(), timestamp(), timestamp());

        List<String> supportedCountries = getDslEfmSupportedCountries();
        List<SupplierRequestSiteEntity> siteEntities = newArrayList();
        List<SupplierSite> supportedSites = newArrayList();
        List<SupplierSiteEntity> siteEntityList = newArrayList();
        for (SupplierSite supplierSite : request.getSupplierSites()) {

            String siteLevelStatus = supportedCountries.contains(supplierSite.getCountryISOCode()) ? InProgress.value() : NotSupported.value();
            if (InProgress.value().equalsIgnoreCase(siteLevelStatus)) {
                supportedSites.add(supplierSite);
            }
            siteEntities.add(new SupplierRequestSiteEntity(String.valueOf(supplierSite.getSiteId()), NotSupported.value().equalsIgnoreCase(siteLevelStatus) ? Completed.value() : InProgress.value(), siteLevelStatus, timestamp(), timestamp(), clientRequest));
            siteEntityList.add(new SupplierSiteEntity(supplierSite.getSiteId(), Long.parseLong(request.getCustomerId()), supplierSite.getSiteName(), supplierSite.getCountryISOCode(),
                                                      supplierSite.getCountryName(), null, Red.getId(), supplierSite.getAvailabilityTelephoneNumber(), "Error while placing GetSupplierProductList call to APE, Please trigger it manually", null, null));
        }
        clientRequest.setSupplierRequestSiteEntities(siteEntities);
        storeClientRequest(clientRequest);
        List<Long> existingSites = getExistingSitesButExcludeFailedSites(Long.parseLong(request.getCustomerId()));
        if (siteEntityList.size() > 0) {
            storeSupplierSiteList(getNonExistingSupplierSites(siteEntityList, existingSites));
        }

        if (SYSTEM.equalsIgnoreCase(request.getTriggerType())) {
            filterExistingSitesFromRequest(request, existingSites);
        }
    }

    private StatusResponse validateRequest(SupplierCheckRequest request) {
        StatusResponse statusResponse = null;
        if (AssertObject.anyEmpty(request.getCustomerId())) {
            statusResponse = new StatusResponse(ERROR, INVALID_REQUEST, "Customer Id is empty", FAILURE_CODE);
        } else if (AssertObject.anyEmpty(request.getSupplierSites())) {
            statusResponse = new StatusResponse(ERROR, INVALID_REQUEST, "There are no Site to initiate the Supplier Check process", FAILURE_CODE);
        } else if (YES.equalsIgnoreCase(request.getAutoTrigger())) {
            for (SupplierSite site : request.getSupplierSites()) {
                if (AssertObject.anyEmpty(site.getAvailabilityTelephoneNumber())) {
                    statusResponse = new StatusResponse(ERROR, INVALID_REQUEST, "If request is placed with Auto-trigger option then site should be having Availability Telephone number", FAILURE_CODE);
                }
            }
        } else if (AssertObject.anyEmpty(request.getLevel())) {
            statusResponse = new StatusResponse(ERROR, INVALID_REQUEST, "Level [Site] or [Product] is not specified", FAILURE_CODE);
        } else if (AssertObject.anyEmpty(request.getTriggerType())) {
            statusResponse = new StatusResponse(ERROR, INVALID_REQUEST, "Trigger type [System] or [User] is not specified", FAILURE_CODE);
        }

        return statusResponse;
    }

    private void storeClientRequestEntity(SupplierCheckRequest request) {
        try {
            Long custId = request.getCustomerId() == null ? null : Long.parseLong(request.getCustomerId());
            SupplierCheckClientRequestEntity clientRequest = new SupplierCheckClientRequestEntity(request.getParentRequestId(), request.getClientCallbackUri(), request.getTriggerType(), request.getAutoTrigger(), request.getSourceSystemName(),
                                                                                                  request.getUser(), custId, request.getStatus(), timestamp(), timestamp());
            List<SupplierRequestSiteEntity> siteEntities = newArrayList();
            for (SupplierSite supplierSite : request.getSupplierSites()) {
                siteEntities.add(new SupplierRequestSiteEntity(String.valueOf(supplierSite.getSiteId()), InProgress.value(), InProgress.value(), timestamp(), timestamp(), clientRequest));
            }
            clientRequest.setSupplierRequestSiteEntities(siteEntities);
            storeClientRequest(clientRequest);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void storeClientRequestEntityLatest(SupplierCheckRequest request) {
        try {
            Long custId = request.getCustomerId() == null ? null : Long.parseLong(request.getCustomerId());
            SupplierCheckClientRequestEntity clientRequest = new SupplierCheckClientRequestEntity(request.getParentRequestId(), request.getClientCallbackUri(), request.getTriggerType(), request.getAutoTrigger(), request.getSourceSystemName(),
                    request.getUser(), custId, request.getStatus(), timestamp(), timestamp());
            List<SupplierRequestSiteEntity> siteEntities = newArrayList();
            for (SupplierSite supplierSite : request.getSupplierSites()) {
                siteEntities.add(new SupplierRequestSiteEntity(String.valueOf(supplierSite.getSiteId()), InProgress.value(), InProgress.value(), timestamp(), timestamp(), clientRequest));
            }
            clientRequest.setSupplierRequestSiteEntities(siteEntities);
            storeClientRequestlatest(clientRequest);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private List<List<SupplierSite>> splitSupplierList(List<SupplierSite> supplierSites, int splitSize) {
        if (supplierSites != null && supplierSites.size() > 0) {
            List<List<SupplierSite>> subSupplierList = newArrayList();
            int size = supplierSites.size();
            int start = 0;
            int end = 0;
            do {
                if ((start + splitSize) > size) {
                    end = size;
                } else {
                    end += splitSize;
                }

                subSupplierList.add(supplierSites.subList(start, end));
                start += splitSize;

            } while (end < size);

            return subSupplierList;
        }

        return null;
    }

    private interface SupplierProductResourceHandlerLogger {

        @Log(level = LogLevel.INFO, format = "Initiating Supplier Check process for '%s'")
        void initiateSupplierCheckProcess(SupplierCheckRequest request);

        @Log(level = LogLevel.INFO, format = "Initiating availability check for for '%s'")
        void initiateAvailabilityCheck(SupplierCheckRequest request);

        @Log(level = LogLevel.INFO, format = "Fetching Supplier Product Sites for Customer Id '%s'")
        void getSupplierProductSites(String bfgCustomerId);

        @Log(level = LogLevel.INFO, format = "Fetching Supplier Product data for Request '%s'")
        void getSupplierProductData(SupplierCheckRequest request);

        @Log(level = LogLevel.INFO, format = "Updating Supplier Product data for Request '%s'")
        void updateSupplierProductSiteData(SupplierCheckRequest request);

        @Log(level = LogLevel.ERROR, format = "SupplierProductResource : '%s'")
        void error(Exception error);

        @Log(level = LogLevel.INFO, format = "SupplierProductResource : '%s'")
        void info(String error);

        @Log(level = LogLevel.INFO, format = "Type - %s, Request - %s")
        void printRequest(String type, String request);

        @Log(level = LogLevel.INFO, format = "Total time taken by : '%s' service is : '%s' milliseconds")
        void totalTimeTaken(String serviceName, String timeTaken);
    }
}
