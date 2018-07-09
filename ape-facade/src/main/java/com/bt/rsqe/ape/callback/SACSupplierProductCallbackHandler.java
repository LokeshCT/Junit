package com.bt.rsqe.ape.callback;

import com.bt.cqm.client.SACAvailabilityCheckerClient;
import com.bt.rsqe.ape.SupplierProductResourceClient;
import com.bt.rsqe.ape.dto.SupplierCheckRequest;
import com.bt.rsqe.ape.dto.sac.SacApeStatus;
import com.bt.rsqe.ape.dto.sac.SacBulkInputDTO;
import com.bt.rsqe.ape.dto.sac.SacBulkUploadStatus;
import com.bt.rsqe.ape.dto.sac.SacSiteDTO;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.ape.repository.entities.SacBulkUploadEntity;
import com.bt.rsqe.ape.repository.entities.SacRequestEntity;
import com.bt.rsqe.ape.source.SupplierProductStore;
import com.bt.rsqe.ape.source.extractor.ResponseExtractorStrategyFactory;
import com.bt.rsqe.ape.source.processor.SupplierCheckRequestProcessor;
import com.bt.rsqe.utils.AssertObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.ape.source.SupplierProductHelper.*;
import static com.bt.rsqe.ape.source.SupplierProductStore.*;
import static javax.ws.rs.core.Response.Status.*;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 04/09/15
 * Time: 13:19
 * To change this template use File | Settings | File Templates.
 */
@Path("/rsqe/ape-facade/sac-supplier-product-callback/")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class SACSupplierProductCallbackHandler {


    public static final String SUPPLIER_PRODUCT_LIST_RESPONSE = "SupplierProductListResponse";
    public static final String RESPONSE = "RESPONSE";
    public static final String APE_CALLBACK = "APE Callback";
    public static final String UTF_8 = "utf-8";
    public static final String SITE = "Site";
    private APEQrefJPARepository repository;
    private Logger LOG = LoggerFactory.getLogger(SACSupplierProductCallbackHandler.class);
    private ResponseExtractorStrategyFactory strategyFactory;
    private SupplierCheckRequestProcessor requestProcessor;
    private SACAvailabilityCheckerClient sacAvailabilityCheckerClient;
    private SupplierProductResourceClient supplierProductResourceClient;
    private String availRespSiteId;

    public SACSupplierProductCallbackHandler(ResponseExtractorStrategyFactory strategyFactory, SupplierCheckRequestProcessor requestProcessor, SACAvailabilityCheckerClient sacAvailabilityCheckerClient, SupplierProductResourceClient supplierProductResourceClient) {
        this.sacAvailabilityCheckerClient = sacAvailabilityCheckerClient;
        this.strategyFactory = strategyFactory;
        this.requestProcessor = requestProcessor;
        this.supplierProductResourceClient = supplierProductResourceClient;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Path("async/requestId/{requestId}")
    public Response supplierProductCallback(@PathParam("requestId") String requestId, String payload) {
        LOG.info(String.format("Received APE Callback SAC Response. Request ID :%s", requestId));
        String fileName = null;
        try {
            String operation = getOperation(payload);

            storeLog(requestId, RESPONSE, operation, payload, APE_CALLBACK, "");
            if (!isRequestIdValid(requestId)) {
                LOG.error(String.format("Invalid Request :%s", requestId));
                return Response.status(BAD_REQUEST).build();
            }

            strategyFactory.getExtractorStrategy(operation, "Telephone").extractResponse(payload, null);

            if (SUPPLIER_PRODUCT_LIST_RESPONSE.equalsIgnoreCase(operation)) {
                /*SupplierCheckRequest request = null;*/
                /*Get original Client Request Entry*/
                String ape2ndReqId = requestId;
                SacRequestEntity clientReq = SupplierProductStore.getSacSiteRequests(ape2ndReqId, null);
                fileName = clientReq.getFileName();
                //SupplierCheckClientRequestEntity supplierCheckClientRequestEntity =getClientRequestByApeRequestId(ape2ndReqId);
                if (clientReq != null) {
                    /* Build Request for Supplier Product Availability*/
                    //request = buildSupplierCheckRequest(clientReq);
                    /*String ape3ReqId = generateApeRequestId();

                    request = new SupplierCheckRequest(ape3ReqId, supplierCheckClientRequestEntity.getId(), null, clientReq.getCreateUser(),
                                                                            null, null, null, SITE, "Yes", "Site", "CQM", null, null);*/
                    SacBulkUploadEntity sacBulkUploadEntity = clientReq.getUploadFile();
                    SacBulkInputDTO sacBulkInputDTO = sacBulkUploadEntity.toShallowDto();
                    sacBulkInputDTO.setSystem("SAC");
                    supplierProductResourceClient.initiateSacSupplierFetchAsynch(sacBulkInputDTO);
                    /* List<SacRequestEntity> siteRequestsEntities = getAllSacSitesWithStatus(sacBulkUploadEntity.getFileName(), SacApeStatus.APE_RESPONSE_SUCCESS);
                    List<SacSiteDTO> sacSiteDTOs =null;
                    if(siteRequestsEntities!=null){
                      sacSiteDTOs = new ArrayList<SacSiteDTO>();
                        for(SacRequestEntity sacRequestEntity : siteRequestsEntities){
                            sacSiteDTOs.add(sacRequestEntity.toDto());
                        }
                    }
                    request.setSupplierSites(convertSacToSupplierSite(sacSiteDTOs));

                    Long[] siteIdsForUpdate = new Long[siteRequestsEntities.size()];
                    for(int i = 0;i<siteRequestsEntities.size();i++){
                        siteIdsForUpdate[i] = siteRequestsEntities.get(i).getSiteId();
                    }

                    updateSacRequestStatus(sacBulkUploadEntity.getFileName(),null,null,ape3ReqId);
                    *//*Make APE Service Call to perform availability Check*//*
                    requestProcessor.initiateSACAvailabilityRequest(request);*/


                }

            } else {
                String ape3rdReqId = requestId;
                SacRequestEntity sacRequestEntity = getSacSiteRequests(null, ape3rdReqId);
                if (sacRequestEntity != null) {
                    fileName = sacRequestEntity.getFileName();
                    if (isAllAvailabilityCheckCompleted(sacRequestEntity.getFileName())) {
                        LOG.info("Going to Generate SAC Report for file name :" + sacRequestEntity.getFileName());
                        generateAvailabilityReport(sacRequestEntity.getFileName());
                        LOG.info("Successfully Invoked Generate SAC Report WS.");
                    }
                }
            }
        } catch (Exception e) {
            String msg = String.format("Failed to Process APE Callback SAC Response !!. File Name :%s , Request Id : %s, Payload : %s", fileName, requestId, payload);
            LOG.error(msg, e);
            return Response.ok(new GenericEntity<String>(msg) {
            }).build();
        }
        String msg = String.format("Successfully Processed APE Callback SAC Response !!.File Name : %s , Request Id : %s", fileName, requestId);
        LOG.info(msg);
        return Response.ok(new GenericEntity<String>(msg) {
        }).build();
    }

    @POST
    @Produces(MediaType.TEXT_XML)
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Path("generateAvailabilityReport")
    public Response generateAvailabilityReportWS(@QueryParam("fileName") String fileName) {
        if (AssertObject.isEmpty(fileName)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new GenericEntity<String>(String.format("Invalid Data. fileName :%s", fileName)) {
            }).build();
        }

        try {
            LOG.info("Going to invoke Generate SAC Report WS...");
            generateAvailabilityReport(fileName);
            LOG.info("Successfully invoked Generate SAC Report WS...");
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new GenericEntity<String>(ex.getMessage()) {
            }).build();
        }

        return Response.ok(new GenericEntity<String>("Report Generated successfully!!") {
        }).build();
    }

    private void generateAvailabilityReport(String fileName) {
        SacBulkUploadEntity sacBulkUploadEntity = getSacBulkUploadEntity(fileName);
        if (sacBulkUploadEntity != null) {

            if (!(SacBulkUploadStatus.COMPLETED.getStatus().equals(sacBulkUploadEntity.getAvailabilityStatus()) || SacBulkUploadStatus.FAILED.getStatus().equals(sacBulkUploadEntity.getAvailabilityStatus()))) {
                SacBulkInputDTO sacBulkInputDTO = sacBulkUploadEntity.toDto();
                sacBulkInputDTO.setSystem("SAC");
                sacAvailabilityCheckerClient.generateAvailabilityReport(sacBulkInputDTO);
            } else {
                String msg = String.format("File - %s already in completed state. File Status  -> %s ", fileName, sacBulkUploadEntity.getAvailabilityStatus());
                LOG.info(msg);
                throw new RuntimeException(msg);
            }
        } else {
            throw new RuntimeException("No File exist by the name - " + fileName);
        }
    }

    private SupplierCheckRequest buildSupplierCheckRequest(SacRequestEntity clientRequest) throws Exception {
        SupplierCheckRequest request = new SupplierCheckRequest(generateApeRequestId(), clientRequest.getApe2ReqId(), null, clientRequest.getCreateUser(),
                                                                null, null, null, SITE, "Yes", "Site", "CQM", null, null, null);
        SacBulkUploadEntity sacBulkUploadEntity = clientRequest.getUploadFile();
        List<SacRequestEntity> siteRequestsEntities = getAllSacSitesWithStatus(sacBulkUploadEntity.getFileName(), SacApeStatus.APE_RESPONSE_SUCCESS);
        List<SacSiteDTO> sacSiteDTO = null;
        if (siteRequestsEntities != null) {
            List<SacSiteDTO> sacSiteDTOs = new ArrayList<SacSiteDTO>();
            for (SacRequestEntity sacRequestEntity : siteRequestsEntities) {
                sacSiteDTOs.add(sacRequestEntity.toDto());
            }
        }
        request.setSupplierSites(convertSacToSupplierSite(sacSiteDTO));
        return request;
    }

    private String getOperation(String payload) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(payload.getBytes(UTF_8))));
        doc.getDocumentElement().normalize();
        String operation = doc.getFirstChild().getNodeName();
        return operation;
    }

}
