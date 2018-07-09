package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.domain.AttachmentDTO;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.projectengine.AttachmentViewDTO;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.QuoteOptionDetailsOrchestrator;
import com.bt.rsqe.projectengine.web.view.AttachmentDialogView;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedAttachmentDialogFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.pagination.DefaultPagination;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.web.AjaxResponseDTO;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.rest.exception.BadRequestException;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.IOUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.bt.rsqe.utils.AssertObject.*;

@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/attachments")
@Produces(MediaType.TEXT_HTML + ";charset=ISO-8859-15")
public class AttachmentDialogResourceHandler extends QuoteViewFocusedResourceHandler {

    private static final AttachmentDialogResourceHandlerLogger LOG = LogFactory.createDefaultLogger(AttachmentDialogResourceHandlerLogger.class);

    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String FILE_NAME = "fileName";
    private static final String CATOGORY_ID = "categoryId";
    private static final String DOCUMENT_ID = "documentId";
    private static final String ATTACHMENT = "attachmentName";
    private static final String COST_ATTACHMENT = "isCostAttachmentDialog";

    private final QuoteOptionDetailsOrchestrator detailsOrchestrator;

    public AttachmentDialogResourceHandler(final Presenter presenter,
                                           QuoteOptionDetailsOrchestrator detailsOrchestrator) {
        super(presenter);
        this.detailsOrchestrator = detailsOrchestrator;
    }

    @GET
    @Path("/form")
    public Response contractForm(@PathParam(CUSTOMER_ID) String customerId,
                                 @PathParam(CONTRACT_ID) String contractId,
                                 @PathParam(PROJECT_ID) String projectId,
                                 @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                 @QueryParam(COST_ATTACHMENT) boolean isCostAttachment) {

        AttachmentDialogView view = new AttachmentDialogView(customerId, contractId, projectId, quoteOptionId, isCostAttachment);
        String page = presenter.render(view("AttachmentForm.ftl").withContext("view", view));
        return Response.ok().entity(page).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/load-attachment")
    public Response loadAttachmentTable(@PathParam(CUSTOMER_ID) final String customerId,
                                        @PathParam(CONTRACT_ID) final String contractId,
                                        @PathParam(PROJECT_ID) final String projectId,
                                        @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                        @QueryParam(CATOGORY_ID) final String categoryId,
                                        @QueryParam("iDisplayStart") final int pageStart,
                                        @QueryParam("iDisplayLength") final int pageSize,
                                        @QueryParam("sEcho") final int pageNumber) {
        LOG.logAttachmentLoadRequest(customerId, contractId, projectId, quoteOptionId, categoryId);
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                AttachmentViewDTO viewDto;
                Pagination pagination = new DefaultPagination(pageNumber, pageStart, pageSize);
                final PaginatedFilter paginatedFilter = new PaginatedAttachmentDialogFilter(pagination);
                try {
                    viewDto = detailsOrchestrator.loadAttachmentTable(customerId, projectId, categoryId, paginatedFilter);
                } catch (RuntimeException e) {
                    AjaxResponseDTO dto = new AjaxResponseDTO(false, e.getMessage());
                    return Response.status(Response.Status.OK).entity(JSONSerializer.getInstance().serialize(dto)).build();
                }
                return Response.ok().entity(viewDto).build();
            }
        }.tryToPerformAction(quoteOptionId);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    @Path("/upload-attachment")
    public Response uploadAttachment(@PathParam(CUSTOMER_ID) final String customerId,
                                     @PathParam(PROJECT_ID) final String projectId,
                                     @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                     @QueryParam(CATOGORY_ID) final String categoryId,
                                     @QueryParam(FILE_NAME) final String fileName,
                                     @FormDataParam(ATTACHMENT) final InputStream inputStream) {
        LOG.logAttachmentUploadRequest(customerId, projectId, quoteOptionId, categoryId, fileName);
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                String response;
                try {
                    byte[] attachmentContent = IOUtils.toByteArray(inputStream);
                    response = detailsOrchestrator.uploadAttachment(customerId, projectId, categoryId,
                                                         fileName,
                                                         attachmentContent
                    );
                }
                catch (Exception e) {
                    AjaxResponseDTO dto = new AjaxResponseDTO(false, e.getMessage());
                    return Response.status(Response.Status.OK).entity(JSONSerializer.getInstance().serialize(dto)).build();
                }
                if(StringUtils.isEmpty(response) || (response.equalsIgnoreCase("SUCCESS"))){
                  return Response.ok().entity(JSONSerializer.getInstance().serialize(new AjaxResponseDTO(true, ""))).build();
                } else {
                    return Response.ok().entity(JSONSerializer.getInstance().serialize(new AjaxResponseDTO(false, response))).build();
                }
            }
        }.tryToPerformAction(quoteOptionId);
    }

    @GET
    @Path("/download-attachment")
    @Produces({"application/*.*"})
    public Response downloadAttachment(@PathParam(CUSTOMER_ID) final String customerId,
                                       @PathParam(PROJECT_ID) final String projectId,
                                       @QueryParam(CATOGORY_ID) final String categoryId,
                                       @QueryParam(FILE_NAME) final String fileName,
                                       @QueryParam(DOCUMENT_ID) final String documentId) {
        LOG.logAttachmentDownloadRequest(customerId, projectId, categoryId, fileName, documentId);
        AttachmentDTO attachmentDTO = detailsOrchestrator.downloadAttachment(documentId, categoryId, customerId, projectId);

        final byte[] bytes = isNotNull(attachmentDTO)?attachmentDTO.getFileContent():null;
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                output.write(bytes);
            }
        }
        ).header("Content-Disposition", "attachment; filename=" + fileName).build();
    }

    @POST
    @Path("/delete-attachment")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteAttachment(@PathParam(CUSTOMER_ID) final String customerId,
                                     @PathParam(PROJECT_ID) final String projectId,
                                     @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                     @QueryParam(CATOGORY_ID) final String categoryId,
                                     @QueryParam(DOCUMENT_ID) final String documentId) {
        LOG.logAttachmentDeleteRequest(customerId, projectId, quoteOptionId, categoryId, documentId);
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                String responseMessage;
                try {
                    responseMessage = detailsOrchestrator.deleteAttachment(documentId, categoryId, customerId, projectId);
                }
                catch (BadRequestException exception) {
                    AjaxResponseDTO dto = new AjaxResponseDTO(false, "");
                    return Response.status(Response.Status.BAD_REQUEST).entity(JSONSerializer.getInstance().serialize(dto)).build();
                }
                 if(StringUtils.isEmpty(responseMessage) || (responseMessage.equalsIgnoreCase("SUCCESS"))){
                  return Response.ok().entity(JSONSerializer.getInstance().serialize(new AjaxResponseDTO(true, ""))).build();
                } else {
                     return Response.ok().entity(JSONSerializer.getInstance().serialize(new AjaxResponseDTO(false, responseMessage))).build();
                 }
            }
        }.tryToPerformAction(quoteOptionId);
    }

    interface  AttachmentDialogResourceHandlerLogger {

        @Log(level = LogLevel.INFO, loggerName = "AttachmentDialogResourceHandlerLogger", format = "Request for the Upload Attachment customerId [%s], ProjectId [%s], " +
                                                                                                   "QuoteOptionId [%s], CategoryId [%s] and FileName [%s]")
        void logAttachmentUploadRequest(String customerId, String projectId, String quoteOptionId, String categoryId, String fileName);

        @Log(level = LogLevel.INFO, loggerName = "AttachmentDialogResourceHandlerLogger", format = "Request for the Download Attachment customerId [%s], ProjectId [%s], " +
                                                                                                   "CategoryId [%s], FileName [%s] and documentId [%s]")
        void logAttachmentDownloadRequest(String customerId, String projectId, String categoryId, String fileName, String documentId);

        @Log(level = LogLevel.INFO, loggerName = "AttachmentDialogResourceHandlerLogger", format = "Request for the Delete Attachment customerId [%s], projectId [%s], quoteOptionId [%s], " +
                                                                                                   "CategoryId [%s] and documentId [%s]")
        void logAttachmentDeleteRequest(String customerId, String projectId, String quoteOptionId, String categoryId, String documentId);

        @Log(level = LogLevel.INFO, loggerName = "AttachmentDialogResourceHandlerLogger", format = "Get Attachment Load document List for customerId [%s], ContractId [%s], ProjectId [%s]," +
                                                                                                   "QuoteOptionId [%s] and CategoryId [%s]")
        void logAttachmentLoadRequest(String customerId, String contractId, String projectId, String quoteOpitonId, String categoryId);

    }
}
