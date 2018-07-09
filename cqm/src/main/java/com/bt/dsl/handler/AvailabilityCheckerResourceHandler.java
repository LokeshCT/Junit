package com.bt.dsl.handler;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 17/08/15
 * Time: 17:04
 * To change this template use File | Settings | File Templates.
 */

import com.bt.cqm.client.SACAvailabilityCheckerClient;
import com.bt.cqm.config.dsl.DslCheckerSharePointPathConfig;
import com.bt.cqm.ldap.LDAPConstants;
import com.bt.cqm.ldap.SearchBTDirectoryHandler;
import com.bt.cqm.ldap.model.LdapSearchModel;
import com.bt.cqm.repository.user.UserEntity;
import com.bt.cqm.repository.user.UserManagementRepository;
import com.bt.dsl.Constants;
import com.bt.dsl.excel.AvailabilityCheckerXLWriter;
import com.bt.dsl.excel.DslImportRow;
import com.bt.dsl.excel.ExcelReader;
import com.bt.dsl.excel.ExcelWritter;
import com.bt.dsl.excel.SacXlRowDataModel;
import com.bt.dsl.excel.UploadFailureRow;
import com.bt.dsl.exception.EmpPalDeleteException;
import com.bt.dsl.exception.EmpPalUploadException;
import com.bt.dsl.tasks.SacLoadSiteRequestTask;
import com.bt.dsl.util.Utility;
import com.bt.rsqe.EmailService;
import com.bt.rsqe.ape.SupplierProductResourceClient;
import com.bt.rsqe.ape.dto.SupplierStatus;
import com.bt.rsqe.ape.dto.sac.SacBulkInputDTO;
import com.bt.rsqe.ape.dto.sac.SacBulkUploadStatus;
import com.bt.rsqe.ape.dto.sac.SacEmailType;
import com.bt.rsqe.ape.dto.sac.SacSiteDTO;
import com.bt.rsqe.ape.dto.sac.SacUserUploadFileDetailDTO;
import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.domain.AttachmentDTO;
import com.bt.rsqe.emppal.attachmentresource.EmpPalResource;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;
import com.bt.rsqe.web.ClasspathConfiguration;
import com.bt.rsqe.web.rest.dto.ErrorDTO;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.bt.rsqe.web.rest.exception.RestException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.bt.cqm.utils.Utility.*;


@Path("/cqm/dslchecker")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class AvailabilityCheckerResourceHandler {

    private EmpPalResource empPalResource = null;
    private DslCheckerSharePointPathConfig dslSharePointConfig = null;
    private SupplierProductResourceClient supplierProductResourceClient;
    private SiteResourceClient siteResourceClient;
    private Map<String, String> countryIsoCache = null;
    ExecutorService executorService = null;
    private EmailService emailService;
    private UserManagementRepository userManagementRepository;
    private SACAvailabilityCheckerClient sacAvailabilityCheckerClient;

    private static final Logger LOG = LoggerFactory.getLogger(AvailabilityCheckerResourceHandler.class);


    private static final String RESULT_SHEET_FAIL_NOTIFICATION_TEMPLATE = "com/bt/cqm/standalone-availability-checker_mail_result_sheet_fail_notification.ftl";
    private static final String RESULT_SHEET_GENERATION_NOTIFICATION_TEMPLATE = "com/bt/cqm/standalone-availability-checker_mail_result_sheet_generation_notification.ftl";
    private static final String EMPTY_INPUT_ARG = "Empty or Null input argument !!";

    public AvailabilityCheckerResourceHandler(EmpPalResource empPalResource, UserManagementRepository repository, DslCheckerSharePointPathConfig dslSharePointConfig, SupplierProductResourceClient supplierProductResourceClient, SiteResourceClient siteResourceClient,SACAvailabilityCheckerClient sacAvailabilityCheckerClient, ExecutorService executorService, EmailService emailService) {
        this.empPalResource = empPalResource;
        this.dslSharePointConfig = dslSharePointConfig;
        this.supplierProductResourceClient = supplierProductResourceClient;
        this.siteResourceClient = siteResourceClient;
        this.executorService = executorService;
        this.emailService=emailService;
        this.userManagementRepository = repository;
        this.sacAvailabilityCheckerClient = sacAvailabilityCheckerClient;
    }


    @POST
    @Path("/uploadFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@FormDataParam("file") InputStream fileInputStream,
                               @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
                               @QueryParam("fileDesc") String fileDescription, @HeaderParam("SM_USER") String userId,@HeaderParam("USER_NAME") String userName, @HeaderParam("SALES_CHANNEL") String salesChannel) {

        if (AssertObject.anyEmpty(fileInputStream, contentDispositionHeader, fileDescription, salesChannel)) {
            String msg = String.format("Invalid Input. [Inputstream :%s, File Description :%s, UserID :%s, Sales Channel :%s", fileInputStream, fileDescription, userId, salesChannel);
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError(msg)).build();
        }

        String fileName = null;
        String attachmentType = "Sale";

        SacBulkInputDTO sacBulkInputDTO = new SacBulkInputDTO();
        sacBulkInputDTO.setFileDesc(fileDescription);
        sacBulkInputDTO.setSalesChannel(salesChannel);
        sacBulkInputDTO.setSystem("SAC");
        sacBulkInputDTO.setUserId(userId);


        try {
            byte[] bytes = toByteArray(fileInputStream);

            ExcelReader<DslImportRow> excelReader = ExcelReader.getInstance(toInputStream(bytes));
            List<DslImportRow> xlRows = excelReader.getAllRows(DslImportRow.class);

            if (xlRows == null || xlRows.size() == 0) {
                String msg = String.format("The uploaded file :[%s] contain no records ", contentDispositionHeader.getFileName());
                LOG.warn(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError(msg)).build();
            } else if (xlRows.size() > 1001) {
                String msg = String.format("The uploaded file :[%s] contain more than 1000 records.", contentDispositionHeader.getFileName());
                LOG.warn(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError(msg)).build();
            }


            sacBulkInputDTO.setValidationStatus(SupplierStatus.InProgress.value());
            sacBulkInputDTO.setHostName(InetAddress.getLocalHost().getHostName());
            sacBulkInputDTO.setUserName(userName);
            fileName = supplierProductResourceClient.createSacBulkUpload(sacBulkInputDTO);//need to implement with failure status.
            LOG.info("Generated SAC BulkUpload FileName :"+fileName);
            List<UploadFailureRow> failureList = validateFileContent(xlRows);  // File content validation.

            if (failureList == null || failureList.size() == 0) {  // No validation error.
                sacBulkInputDTO.setFileName(fileName);
                sacBulkInputDTO.setSites(toSiteDtos(xlRows));
                try {
                    SacLoadSiteRequestTask task =new SacLoadSiteRequestTask(sacBulkInputDTO,bytes,supplierProductResourceClient,this);
                    executorService.submit(task);
                    } catch (Exception ex) {
                   LOG.error("Failed to insert SAC Site Entries to DB. ",ex);
                }
            } else { // Upload original and Error file to share point.

                ByteArrayOutputStream oStream = new ByteArrayOutputStream();

                ExcelWritter<UploadFailureRow> excelWritter = new ExcelWritter<UploadFailureRow>(oStream);

                excelWritter.writeToNewExcel(failureList);

                String orgDocumentId = uploadFileToSharePointFolder(bytes, fileName, salesChannel, attachmentType, Constants.FOLDER_TYPE_IMPORT);
                String failDocumentId = uploadFileToSharePointFolder(oStream.toByteArray(), fileName, salesChannel, attachmentType, Constants.FOLDER_TYPE_FAILURE);

                sacBulkInputDTO.setFileName(fileName);
                sacBulkInputDTO.setSharePointFailDocId(failDocumentId);
                sacBulkInputDTO.setSharePointOrgDocId(orgDocumentId);
                sacBulkInputDTO.setValidationStatus(SupplierStatus.Failed.value());
                LOG.info("SAC Bulk Upload Validation Failed -" + fileName);
                supplierProductResourceClient.updateSacBulkUpload(sacBulkInputDTO);
            }


        } catch (EmpPalUploadException empEx) {
            LOG.error(String.format("Failed to upload file[%s] to Sharepoint.", fileName), empEx);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildGenericError(empEx.getMessage())).build();

        } catch (Exception ex) {
            LOG.error(String.format("Exception in SAC File upload. File Name:[%s]", contentDispositionHeader.getFileName()), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildGenericError(ex.getMessage())).build();

        }
        return Response.status(Response.Status.OK).entity(buildGenericError(fileName)).build();
    }


    @POST
    @Path("/generateResultSheet")
    public Response generateResultSheet(SacBulkInputDTO sacBulkInputDTO) {
        LOG.info("Inside generateResultSheet()..");
        if (AssertObject.anyEmpty(sacBulkInputDTO)) {

            LOG.error(String.format("Invalid Input.  SacBulkInputDTO:[%s]", sacBulkInputDTO));

            return Response.status(Response.Status.BAD_REQUEST).entity(String.format("Invalid Input")).build();
        }

        if (AssertObject.anyEmpty(sacBulkInputDTO.getFileName(), sacBulkInputDTO.getSites())) {

            LOG.error(String.format("Invalid Input. File name :[%s], list of sites:[%s]",
                                    sacBulkInputDTO.getFileName(), sacBulkInputDTO.getSites()));

            return Response.status(Response.Status.BAD_REQUEST).entity(String.format("Invalid Input. File name :[%s], list of sites:[%s]",
                                                                                     sacBulkInputDTO.getFileName(), sacBulkInputDTO.getSites())).build();
        }
        LOG.info("Going to generate SAC Report . File Name :"+sacBulkInputDTO.getFileName());
        List<SacXlRowDataModel> xlRowDataModelList = convertToXlRowDataModel(sacBulkInputDTO.getSites());

        if (xlRowDataModelList == null || xlRowDataModelList.size() == 0) {
            LOG.error("siteDTOList is empty.");
            return Response.status(Response.Status.BAD_REQUEST).entity("siteDTOList is empty.").build();

        }

        try {
            String fileName = sacBulkInputDTO.getFileName() + Utility.getSharePointFileName(Constants.FOLDER_TYPE_RESULT);
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            AvailabilityCheckerXLWriter writer = new AvailabilityCheckerXLWriter(byteOutputStream);
            writer.writeToNewExcel(xlRowDataModelList);

            String resultDoctId = uploadFileToSharePointFolder(byteOutputStream.toByteArray(), sacBulkInputDTO.getFileName(), sacBulkInputDTO.getSalesChannel(), "Sale", Constants.FOLDER_TYPE_RESULT);


            sacBulkInputDTO.setFileName(sacBulkInputDTO.getFileName());
            sacBulkInputDTO.setSharePointResultDocId(resultDoctId);
            sacBulkInputDTO.setAvailabilityStatus(SacBulkUploadStatus.COMPLETED.getStatus());
            sacBulkInputDTO.setSystem("CQM");
            supplierProductResourceClient.updateSacBulkUpload(sacBulkInputDTO);
            sacAvailabilityCheckerClient.sendEmailNotification(sacBulkInputDTO, SacEmailType.REPORT_GENERATION_SUCCESS);
            LOG.info("SAC Report Generated Successfully !! File Name :"+sacBulkInputDTO.getFileName());
        } catch (EmpPalUploadException empEx) {
            sacBulkInputDTO.setAvailabilityStatus(SacBulkUploadStatus.FAILED.getStatus());
            supplierProductResourceClient.updateSacBulkUpload(sacBulkInputDTO);
            sacAvailabilityCheckerClient.sendEmailNotification(sacBulkInputDTO, SacEmailType.REPORT_GENERATION_FAILURE);
            LOG.error(String.format("FAILED to Upload SAC Report [%s] to Share point.", sacBulkInputDTO.getFileName()), empEx);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildGenericError(empEx.getMessage())).build();

        } catch (Exception e) {
            sacBulkInputDTO.setAvailabilityStatus(SacBulkUploadStatus.FAILED.getStatus());
            supplierProductResourceClient.updateSacBulkUpload(sacBulkInputDTO);
            String msg = String.format("FAILED to Generate SAC Report [%s] !!",sacBulkInputDTO.getFileName());
            LOG.error(msg, e);
            sacAvailabilityCheckerClient.sendEmailNotification(sacBulkInputDTO, SacEmailType.REPORT_GENERATION_FAILURE);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildGenericError(msg)).build();

        }

        return Response.status(Response.Status.OK).entity(buildGenericError(sacBulkInputDTO.getFileName())).build();

    }


    @GET
    @Path("/userUploadedFileList")
    public Response getUserUploadedFileList(@HeaderParam("SM_USER") String userId) {
        if (AssertObject.anyEmpty(userId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(String.format("Invalid Input. User Id :[%s]", userId)).build();
        }
        try {
            List<SacBulkInputDTO> fileList = null;
            try{
                fileList = supplierProductResourceClient.getAllInProgressUploads(userId);
            }catch (ResourceNotFoundException ex){
                fileList = new ArrayList<SacBulkInputDTO>(); // Send zero records a 200 OK
            }

            Boolean anyReportInProcessing = supplierProductResourceClient.isReportGenInProgress(userId);

            SacUserUploadFileDetailDTO sacUserUploadFileDetailDTO = new SacUserUploadFileDetailDTO(anyReportInProcessing,fileList);


            return Response.status(Response.Status.OK).entity(sacUserUploadFileDetailDTO).build();


        } catch (RestException e) {
            throw e;
        } catch (Exception e) {

            LOG.error("Exception while Getting User upload files ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildGenericError(e.getMessage())).build();

        }

    }

    @GET
    @Path("/allUploadedFileList")
    public Response getAllUserUploadedFileList() {
        try {
            List<SacBulkInputDTO> fileList = supplierProductResourceClient.getAllUserReports();
            return Response.status(Response.Status.OK).entity(fileList).build();

        }catch (RestException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Exception while Getting All upload files ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildGenericError(e.getMessage())).build();

        }

    }


    @GET
    @Path("/fileList")
    public Response getFileList(@QueryParam("salesChannel") String salesChannel, @QueryParam("fromFolder") String fromFolder) {

        List<AttachmentDTO> docList = null;

        if (AssertObject.anyEmpty(salesChannel, fromFolder)) {
            String msg = String.format("Invalid Input. Sales Channel =[%s] , From Folder =[%s]", salesChannel, fromFolder);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildGenericError(msg)).build();
        }

        try {
            String path = getSharePointPath(salesChannel, fromFolder);

            docList = getDocs(path);

            return Response.status(Response.Status.OK).entity(docList).build();

        } catch (Exception ex) {
            LOG.error("Exception to get Template list. ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

    }


    @GET
    @Path("/downloadTemplate")
    @Produces("application/*.*")
    public Response downloadTemplate(@QueryParam("countryName") String countryName, @HeaderParam("SM_USER") String userId, @HeaderParam("SALES_CHANNEL") String salesChannel3) {
        String tempFileName = null;
        String tempFilePath = null;
        List<AttachmentDTO> docList = null;
        String docId = null;
        AttachmentDTO doc;

        try {

            tempFileName = dslSharePointConfig.getTemplateFileNameConfig().getFileName();

            tempFilePath = dslSharePointConfig.getTemplatePathConfig().getPath();

            docList = getDocs(tempFilePath);

            if (docList.size() > 0) {

                for (AttachmentDTO attachmentDTO : docList) {

                    if (attachmentDTO.getFileName().equalsIgnoreCase(tempFileName)) {

                        docId = attachmentDTO.getDocumentId();
                    }
                }

            }

            if (docId != null) {
                doc = getDocument(tempFilePath, docId);
                if (doc != null) {
                    return Response.status(Response.Status.OK).entity(doc.getFileContent()).header("Content-Disposition",
                                                                                                   "attachment; filename=" + tempFileName).build();
                }
            }

            return Response.status(Response.Status.NOT_FOUND).build();

        } catch (Exception ex) {

            LOG.error("Exception to download. ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildGenericError(ex.getMessage())).build();
        }
    }


    @GET
    @Path("/downloadFile")
    @Produces("application/*.*")
    public Response downloadFile(@QueryParam("docId") String docId, @QueryParam("fileName") String fileName, @QueryParam("salesChannel") String salesChannel,
                                 @QueryParam("docType") String docType) {
        String path = "";
        AttachmentDTO doc;

        if (AssertObject.anyEmpty(docId, salesChannel, docType, fileName)) {
            String msg = String.format("Invalid Input.File Name :[%s], DocId :[%s] , SalesChannel :[%s], Doc Type :[%s] ", fileName, docId, salesChannel, docType);
            LOG.info(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError(msg)).build();
        }


        try {
            path = getSharePointPath(salesChannel, docType);
            doc = getDocument(path, docId);

            if (doc != null) {
                fileName = fileName + Utility.getSharePointFileName(docType);
                return Response.status(Response.Status.OK).entity(doc.getFileContent()).header("Content-Disposition",
                                                                                               "attachment; filename=" + fileName).build();
            }

            return Response.status(Response.Status.NOT_FOUND).build();


        } catch (Exception e) {
            LOG.error("Exception to get the result path. Document Id :" + docId + " sales channel:" + salesChannel, e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildGenericError(e.getMessage())).build();

        }
    }


    public String uploadFileToSharePointFolder(byte[] fileInputStream,
                                                String fileName,
                                                String salesChannel,
                                                String attachmentType, String folderType) throws EmpPalUploadException {

        String path = getSharePointPath(salesChannel, folderType);

        try {

            fileName = fileName + Utility.getSharePointFileName(folderType);

            ErrorDTO empPalResponse = empPalResource.uploadAttachmentOfSAC(Utility.createAttachmentDTO(fileInputStream, fileName, path), attachmentType);

            //Creating folder if path does not exist.
            if (Constants.INVALID_FOLDER.equalsIgnoreCase(empPalResponse.errorId) || Constants.CONFLICT.equalsIgnoreCase(empPalResponse.errorId)) {

                ErrorDTO folderRes = createListOfFolder(path.split("/"));

                if (folderRes != null && Constants.SUCCESS.equalsIgnoreCase(folderRes.description)) {

                    empPalResponse = empPalResource.uploadAttachmentOfSAC(Utility.createAttachmentDTO(fileInputStream, fileName, getSharePointPath(salesChannel, folderType)), attachmentType);
                }
            }

            if (Constants.SUCCESS.equalsIgnoreCase(empPalResponse.description)) {

                return empPalResponse.documentId;

            } else {

                throw new EmpPalUploadException(empPalResponse.description);
            }

        } catch (Exception ex) {

            LOG.error("Exception uploading Document to share point.", ex);
            throw new EmpPalUploadException(ex);
        }

    }

    @POST
    @Path("/deleteBulkUpload")
    public Response deleteFileFromSharepoint(@HeaderParam("SM_USER") String userId, SacBulkInputDTO bulkInputDto) {

        if (AssertObject.anyEmpty(userId, bulkInputDto, bulkInputDto.getSalesChannel())) {
            String msg = String.format("Invalid Input. User ID : %s , BulkUploadDto : %s", userId, bulkInputDto);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } else if (AssertObject.anyEmpty(bulkInputDto.getFileName(), bulkInputDto.getSalesChannel())) {
            String msg = String.format("Invalid Input. Sales Channel : %s , FileName : %s", bulkInputDto.getSalesChannel(), bulkInputDto.getFileName());
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        String salesChannel = bulkInputDto.getSalesChannel();


        try {
            if (!AssertObject.isEmpty(bulkInputDto.getSharePointFailDocId())) {
                deleteSharePointFile(bulkInputDto.getSharePointFailDocId(), getSharePointPath(salesChannel, Constants.FOLDER_TYPE_FAILURE));
            }

            if (!AssertObject.isEmpty(bulkInputDto.getSharePointOrgDocId())) {
                deleteSharePointFile(bulkInputDto.getSharePointOrgDocId(), getSharePointPath(salesChannel, Constants.FOLDER_TYPE_IMPORT));
            }

            supplierProductResourceClient.deleteSacUpload(bulkInputDto.getFileName(), userId);
            return Response.status(Response.Status.OK).build();

        } catch (Exception ex) {

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildGenericError(ex.getMessage())).build();
        }


    }

    @POST
    @Path("/sendEmailNotification")
    public Response sendEmailNotification(SacBulkInputDTO sacBulkInputDTO,@QueryParam("emailType") String emailType) {
        if(AssertObject.anyEmpty(emailType,sacBulkInputDTO,sacBulkInputDTO.getFileName(),sacBulkInputDTO.getSalesChannel()) || (SacEmailType.getType(emailType)==null)){
            String msg;

            if(sacBulkInputDTO==null){
               msg =  String.format("Invalid Input.  BulkUploadDto is null");
            }else{
                msg =  String.format("Invalid Input. Email Type : %s , File Name : %s, Sales Channel : %s", emailType, sacBulkInputDTO.getFileName(),sacBulkInputDTO.getSalesChannel());
            }

            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError(msg)).build();
        }

        SacEmailType emailTyp = SacEmailType.getType(emailType);
        String mailSubject;
        Configuration config = new ClasspathConfiguration();;
        Template emailTemplate;
        Writer out = new StringWriter();
        Map<String, Object> map = new HashMap<String, Object>();
        UserEntity userEntity;
        LOG.info("Going to send SAC Notification Email :"+sacBulkInputDTO.getFileName());

        try {
            switch (emailTyp) {
                case REPORT_GENERATION_SUCCESS:
                    mailSubject = "BT Availability Check Update– "+ sacBulkInputDTO.getFileName() +" - Result Generation Completed";
                    emailTemplate = config.getTemplate(RESULT_SHEET_GENERATION_NOTIFICATION_TEMPLATE);
                    break;
                case REPORT_GENERATION_FAILURE:
                    mailSubject = "BT Availability Check Update – "+ sacBulkInputDTO.getFileName() +" - Result Generation Failed.";
                    emailTemplate = config.getTemplate(RESULT_SHEET_FAIL_NOTIFICATION_TEMPLATE);
                    break;
                default :
                    LOG.error(String.format("No Email Type [%s] found ..",emailType));
                    return com.bt.rsqe.rest.ResponseBuilder.notFound().withEntity(buildGenericError(sacBulkInputDTO.getFileName())).build();

            }

            userEntity = getUserDetails(sacBulkInputDTO.getUserId());
            map.put("userName",userEntity.getUserName());
            map.put("salesChannel", sacBulkInputDTO.getSalesChannel());
            map.put("fileDesc",sacBulkInputDTO.getFileDesc());
            emailTemplate.process(map, out);
            sendEmail(mailSubject, out.toString(),userEntity.getEmailId());
            String msg =String.format("SAC Notification Email successfully send to mailId : %s, Email Type :%s",userEntity.getEmailId(),emailType);
            LOG.info(msg);
            return com.bt.rsqe.rest.ResponseBuilder.anOKResponse().withEntity(buildGenericError(msg)).build();
        } catch (Exception e) {
            LOG.error(String.format("Failed to send SAC Email Notification. File Name:%s , Email Type :%s ",sacBulkInputDTO.getFileName(),emailType),e);
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(e.getMessage())).build();
        }
    }


    private void deleteSharePointFile(String documentId, String path) throws Exception {
        String res = empPalResource.deleteAttachment(path, documentId);

        if (!(Constants.SUCCESS.equalsIgnoreCase(res) || (res.indexOf("did not match any document in the library") > -1))) {
            String msg = String.format("Failed to delete file from share point. documentId : %s , path: %s", documentId, path);
            throw new EmpPalDeleteException(msg);
        }
    }


    private ErrorDTO createListOfFolder(String... folders) {
        String parentPath = "";
        ErrorDTO res = null;
        for (String folder : folders) {
            res = createFld(folder, parentPath);
            parentPath = parentPath + "/" + folder;
            if (!(res.description.equals(Constants.SUCCESS) || Constants.FOLDER_ALREADY_EXISTING_MESSAGE.equalsIgnoreCase(res.description))) {
                return res;
            }
        }
        return res;
    }


    private ErrorDTO createFld(String folderName, String path) {

        try {
            AttachmentDTO attachmentDTO = null;
            if (path == null || path.trim().equals("")) {
                attachmentDTO = new AttachmentDTO();
            } else {
                attachmentDTO = new AttachmentDTO(path);
            }

            attachmentDTO.setFolderName(folderName);
            attachmentDTO.setCreatedDate(JaxbDateTime.valueOf(new DateTime(new Date())));
            ErrorDTO res = empPalResource.createSacFolder(attachmentDTO);
            return res;

        } catch (Exception ex) {

            LOG.error("Exception to create folder to EMP PAL. Path:" + path + "SalesChannel: " + folderName, ex);
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setDescription("Exception to create folder at EMP PAL");
            return errorDTO;
        }
    }


    private List<AttachmentDTO> getDocs(String path) throws Exception {

        List<AttachmentDTO> docList = null;
        String parentPath = null;

        try {

            docList = empPalResource.getAvailableAttachments(path);

            return docList;

        } catch (Exception ex) {

            throw ex;
        }
    }

    private AttachmentDTO getDocument(String path, String docId) throws Exception {

        if (docId != null && path != null) {
            return empPalResource.getAttachment(path, docId);
        } else {
            throw new Exception("Fail to get the document. Document Id: " + docId + " File Path: " + path);
        }
    }


    private List<UploadFailureRow> validateFileContent(List<DslImportRow> importRows) {
        List<UploadFailureRow> failureRows = new ArrayList<UploadFailureRow>();
        String status = null;

        for (int i = 0; i < importRows.size(); i++) {
            String failureMessage = "";
            DslImportRow dslImportRow = importRows.get(i);
            UploadFailureRow failureRow = new UploadFailureRow(dslImportRow);

            if (i == 0) {   // Forming The Excel Header.
                failureRow.setValidationStatus("Validation Status");
                failureRow.setFailureReason("Failure Reason");
                failureRows.add(failureRow);
                continue;
            }

            if (!AssertObject.isEmpty(dslImportRow.getSiteName()) && dslImportRow.getSiteName().length() >100) {
                failureRow.setValidationStatus("Failed");
                failureMessage = failureMessage + "Site Name should not be more than 100 Characters. ";
                failureRow.setFailureReason(failureMessage);
                status = "Failed";
            }
            if (AssertObject.isEmpty(dslImportRow.getCountry())) {
                failureRow.setValidationStatus("Failed");
                failureMessage = failureMessage + "Country is empty. ";
                failureRow.setFailureReason(failureMessage);
                status = "Failed";
            }

            if (!AssertObject.isEmpty(dslImportRow.getCountry()) && AssertObject.isEmpty(siteResourceClient.getCountryIsoCode(dslImportRow.getCountry()))) {
                failureRow.setValidationStatus("Failed");
                failureMessage = failureMessage + "Invalid Country. ";
                failureRow.setFailureReason(failureMessage);
                status = "Failed";
            }

            if (AssertObject.isEmpty(dslImportRow.getTelephoneNo())) {
                failureRow.setValidationStatus("Failed");
                failureMessage = failureMessage + "Telephone number cannot be null. ";
                failureRow.setFailureReason(failureMessage);
                status = "Failed";
            }

            if (!AssertObject.isEmpty(dslImportRow.getTelephoneNo()) && !(isValidPhoneNo(dslImportRow.getTelephoneNo()))) {
                failureRow.setValidationStatus("Failed");
                failureMessage = failureMessage + "Telephone number format is not correct. It should start with any one combination - 01 or 02 or 03 or 04 or 05 and Telephone number cell format should always be text";
                failureRow.setFailureReason(failureMessage);
                status = "Failed";
            }

            if (hasDuplicateTelephoneNo(failureRows, failureRow)) {
                failureRow.setValidationStatus("Failed");
                failureMessage = failureMessage + "Duplicate Telephone number. ";
                failureRow.setFailureReason(failureMessage);
                status = "Failed";
            }
            failureRows.add(failureRow);
        }
        if (status == null) {
            return null;
        }
        return failureRows;
    }

    private boolean isValidPhoneNo(String phoneNo) {
        String regex = "^(01|02|03|04|05)+\\d{8}";
        if (phoneNo.matches(regex)) {
            return true;
        }

        return false;
    }

    private boolean hasDuplicateTelephoneNo(List<UploadFailureRow> list, UploadFailureRow ufrObject) {
        if (list != null && list.size() > 0) {
            if (AssertObject.isEmpty(ufrObject.getTelephoneNo())) {
                return false;
            }
            return list.contains(ufrObject);
        }
        return false;
    }


    private byte[] toByteArray(InputStream iStream) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int reads = iStream.read();

        while (reads != -1) {
            baos.write(reads);
            reads = iStream.read();
        }

        return baos.toByteArray();

    }

    private InputStream toInputStream(byte[] bytes) {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        return stream;
    }


    private List<SacSiteDTO> toSiteDtos(List<DslImportRow> rows) {
        List<SacSiteDTO> sacSiteDTOs = new ArrayList<SacSiteDTO>();
        for (int i = 1; i < rows.size(); i++) {
            SacSiteDTO sacSiteDTO = rows.get(i).toDto();
            sacSiteDTO.setCountryIsoCode(siteResourceClient.getCountryIsoCode(sacSiteDTO.getCountryName()));
            sacSiteDTO.setCountryName(sacSiteDTO.getCountryName());
            sacSiteDTOs.add(sacSiteDTO);
        }
        return sacSiteDTOs;
    }




    private String getSharePointPath(String salesChannel, String type) {
        String path = "";
        if (Constants.FOLDER_TYPE_RESULT.equalsIgnoreCase(type)) {
            path = salesChannel + "/" + dslSharePointConfig.getResultPathConfig().getPath();
        } else if (Constants.FOLDER_TYPE_FAILURE.equalsIgnoreCase(type)) {
            path = salesChannel + "/" + dslSharePointConfig.getFailurePathConfig().getPath();
        } else if (Constants.FOLDER_TYPE_TEMPLATE.equalsIgnoreCase(type)) {
            path = dslSharePointConfig.getTemplatePathConfig().getPath();
        } else if (Constants.FOLDER_TYPE_IMPORT.equalsIgnoreCase(type)) {
            path = salesChannel + "/" + dslSharePointConfig.getImportPathConfig().getPath();
        }
        return path;

    }


    private List<SacXlRowDataModel> convertToXlRowDataModel(List<SacSiteDTO> siteDTOs) {

        List<SacXlRowDataModel> sacXlRowDataModelList = new ArrayList<SacXlRowDataModel>();

        if (AssertObject.anyEmpty(siteDTOs)) {
            return null;
        }

        for (SacSiteDTO sacSiteDTO : siteDTOs) {
            SacXlRowDataModel sacXlRowDataModel = SacXlRowDataModel.getInstance(sacSiteDTO);
            sacXlRowDataModelList.add(sacXlRowDataModel);
        }

        return sacXlRowDataModelList;
    }

    private void sendEmail(String mailSubject, String mailBody,String toEmailId) throws Exception {
        try {

            emailService.sendEmail(EmailService.DEFAULT_FROM_MAIL_ACCOUNT, mailSubject, mailBody, toEmailId);
        } catch (Exception e) {
            LOG.error("Failed to send Email. EMAIL_ID :" + toEmailId);
            throw e;
        }
    }

    private UserEntity getUserDetails(String ein) throws Exception{
        if(AssertObject.isEmpty(ein) ){
            throw new Exception("EIN is empty.");
        }

        UserEntity userEntity = userManagementRepository.findUserByUserId(ein);

        if(userEntity == null || AssertObject.isEmpty(userEntity.getEmailId())){
            userEntity = new UserEntity();
            LdapSearchModel userModel = findUserInBTDirectory(ein);

            if(userModel == null ){
                throw new Exception("user details does not found.");
            }

            if(AssertObject.isEmpty(userModel.getMailId()) ){
                throw new Exception("EmailID is empty.");
            }

            if(AssertObject.isEmpty(userModel.getFirstName()) && AssertObject.isEmpty(userModel.getFullName()) ){
                throw new Exception("User Name is empty.");
            }

            userEntity.setEmailId(userModel.getMailId());

            if( !AssertObject.isEmpty(userModel.getFullName())){
            userEntity.setUserName(userModel.getFullName());
              return userEntity;
            }

            if( !AssertObject.isEmpty(userModel.getFirstName())){
                userEntity.setUserName(userModel.getFirstName());
                return userEntity;
            }
        }

        return userEntity;
    }

    private LdapSearchModel findUserInBTDirectory(String ein) {
        Map<String, String> args = new HashMap<String, String>();
        args.put(LDAPConstants.EIN, ein);
        List<LdapSearchModel> resultList = new SearchBTDirectoryHandler().searchBTDirectory(args);
        LdapSearchModel ldapSearchResult = null;
        if (resultList != null && !resultList.isEmpty()) {
            ldapSearchResult = resultList.get(0);
        }
        return ldapSearchResult;
    }

}

