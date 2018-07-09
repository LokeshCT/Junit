package com.bt.dsl.tasks;

import com.bt.dsl.Constants;
import com.bt.dsl.handler.AvailabilityCheckerResourceHandler;
import com.bt.rsqe.ape.SupplierProductResourceClient;
import com.bt.rsqe.ape.dto.SupplierStatus;
import com.bt.rsqe.ape.dto.sac.SacBulkInputDTO;
import com.bt.rsqe.ape.dto.sac.SacBulkUploadStatus;
import com.bt.rsqe.ape.dto.sac.SacEmailType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 05/10/15
 * Time: 16:07
 * To change this template use File | Settings | File Templates.
 */
public class SacLoadSiteRequestTask implements Callable {
    private SacBulkInputDTO sacBulkInputDTO;
    private SupplierProductResourceClient supplierProductResourceClient;
    private AvailabilityCheckerResourceHandler availabilityCheckerResourceHandler;
    private byte[] fileContent;
    private static Logger LOG = LoggerFactory.getLogger(SacLoadSiteRequestTask.class);

    public SacLoadSiteRequestTask(SacBulkInputDTO sacBulkInputDTO, byte[] fileContent, SupplierProductResourceClient supplierProductResourceClient, AvailabilityCheckerResourceHandler availabilityCheckerResourceHandler) {
        this.sacBulkInputDTO = sacBulkInputDTO;
        this.supplierProductResourceClient = supplierProductResourceClient;
        this.fileContent = fileContent;
        this.availabilityCheckerResourceHandler = availabilityCheckerResourceHandler;
    }


    @Override
    public Object call() throws Exception {
        supplierProductResourceClient.createSacSiteEntries(sacBulkInputDTO);

        String orgDocumentId = availabilityCheckerResourceHandler.uploadFileToSharePointFolder(fileContent, sacBulkInputDTO.getFileName(), sacBulkInputDTO.getSalesChannel(), "Sales", Constants.FOLDER_TYPE_IMPORT);
        sacBulkInputDTO.setSharePointOrgDocId(orgDocumentId);
        sacBulkInputDTO.setValidationStatus(SupplierStatus.Success.value());
        sacBulkInputDTO.setAvailabilityStatus(SacBulkUploadStatus.PROCESSING.getStatus());
        supplierProductResourceClient.updateSacBulkUpload(sacBulkInputDTO);
        try {
            Long startTime = new Date().getTime();
            LOG.info(String.format("Post initiateSacSupplierFetchAsynch [%s] start time :%s ",sacBulkInputDTO.getFileName(),startTime));
            Future<Response> responseFuture = supplierProductResourceClient.initiateSacSupplierFetchAsynch(sacBulkInputDTO);
            while(!responseFuture.isDone()){
               Thread.sleep(20000); // Sleep for 20 sec...
            }
            Long endTime = new Date().getTime();
            LOG.info(String.format("Post initiateSacSupplierFetchAsynch [%s] End time : %s",sacBulkInputDTO.getFileName(),endTime));
            LOG.info(String.format("initiateSacSupplierFetchAsynch [%s] Total Time Taken (ms): %s",sacBulkInputDTO.getFileName(),(endTime-startTime)));

           /* Response response = responseFuture.get();

            if(Response.Status.OK.getStatusCode() == response.getStatus()){
                LOG.info("Submitted for initiateSacSupplierFetch(). File Name : "+sacBulkInputDTO.getFileName());
            }else{
                LOG.info("Failed to process initiateSacSupplierFetch(). File Name : "+sacBulkInputDTO.getFileName());
                sacBulkInputDTO.setAvailabilityStatus(SacBulkUploadStatus.FAILED.getStatus());
                supplierProductResourceClient.updateSacBulkUpload(sacBulkInputDTO);
                availabilityCheckerResourceHandler.sendEmailNotification(sacBulkInputDTO, SacEmailType.REPORT_GENERATION_FAILURE.value());
            }*/
            LOG.info("Submitted for initiateSacSupplierFetch(). File Name : "+sacBulkInputDTO.getFileName());
        } catch (Exception ex) {
            LOG.error("Exception on initiateSacSupplierFetch submit. File Name :"+sacBulkInputDTO.getFileName(),ex);
            sacBulkInputDTO.setAvailabilityStatus(SacBulkUploadStatus.FAILED.getStatus());
            supplierProductResourceClient.updateSacBulkUpload(sacBulkInputDTO);
            availabilityCheckerResourceHandler.sendEmailNotification(sacBulkInputDTO, SacEmailType.REPORT_GENERATION_FAILURE.value());
            throw ex;
        }
        return null;
    }
}
