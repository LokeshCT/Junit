package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.expedio.services.CloseBidManagerActivityDTO;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.facades.BidManagerCommentsFacade;
import com.bt.rsqe.projectengine.web.facades.ExpedioServicesFacade;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.InvalidExportDataException;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmExportPricingSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmPricingSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmSheetExportOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmSheetImportOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMExportOrchestrator;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.BidManagerCommentsView;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.web.AjaxResponseDTO;
import com.bt.rsqe.web.Presenter;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
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
import java.util.List;


@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/bcm")
@Produces(MediaType.TEXT_HTML)
public class QuoteOptionBcmResourceHandler extends QuoteViewFocusedResourceHandler {

    private Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String BID_MANAGER_COMMENTS = "newComment";
    private static final String BID_MANAGER_CAVEATS = "newCaveat";
    private QuoteOptionBcmExportPricingSheetOrchestrator quoteOptionBcmExportPricingSheetOrchestrator;
    private QuoteOptionBcmSheetExportOrchestrator quoteOptionbcmSheetExportOrchestrator;
    private QuoteOptionBcmSheetImportOrchestrator quoteOptionBcmSheetImportOrchestrator;
    private QuoteOptionBcmPricingSheetOrchestrator quoteOptionBcmPricingSheetOrchestrator;
    private BCMExportOrchestrator bcmExportOrchestrator;
    private final ExpedioServicesFacade expedioServices;
    private final ProjectResource projects;
    private static final String BID_TERMS_AND_CONDITIONS = "TODO";
    private final BidManagerCommentsFacade bidManagerCommentsFacade;

    public QuoteOptionBcmResourceHandler(final Presenter presenter,
                                         QuoteOptionBcmExportPricingSheetOrchestrator quoteOptionBcmExportPricingSheetOrchestrator,
                                         QuoteOptionBcmPricingSheetOrchestrator quoteOptionBcmPricingSheetOrchestrator,
                                         ProjectResource projects,
                                         ExpedioServicesFacade expedioServicesFacade,
                                         QuoteOptionBcmSheetExportOrchestrator quoteOptionbcmSheetExportOrchestrator,
                                         QuoteOptionBcmSheetImportOrchestrator quoteOptionBcmSheetImportOrchestrator,
                                         BCMExportOrchestrator bcmExportOrchestrator,
                                         BidManagerCommentsFacade bidManagerCommentsFacade) {
        super(presenter);
        this.quoteOptionBcmExportPricingSheetOrchestrator = quoteOptionBcmExportPricingSheetOrchestrator;
        this.quoteOptionBcmPricingSheetOrchestrator = quoteOptionBcmPricingSheetOrchestrator;
        this.projects = projects;
        this.expedioServices = expedioServicesFacade;
        this.quoteOptionbcmSheetExportOrchestrator=quoteOptionbcmSheetExportOrchestrator;
        this.quoteOptionBcmSheetImportOrchestrator=quoteOptionBcmSheetImportOrchestrator;
        this.bcmExportOrchestrator = bcmExportOrchestrator;
        this.bidManagerCommentsFacade = bidManagerCommentsFacade;
    }

    @GET
    @Path("/validate")
    public Response validateBCMExportAllowed(@PathParam(CUSTOMER_ID) final String customerId,
                                             @PathParam(CONTRACT_ID) final String contractId,
                                             @PathParam(PROJECT_ID) final String projectId,
                                             @PathParam(QUOTE_OPTION_ID) final String quoteOptionId) {
        try {
            quoteOptionbcmSheetExportOrchestrator.canExportBCMSheet(customerId, contractId, projectId, quoteOptionId);
            return ResponseBuilder.anOKResponse().build();
        } catch (UnsupportedOperationException e) {
            return ResponseBuilder.internalServerError().withEntity(e.getMessage()).build();
        }
    }

    @GET
    @Produces({"application/vnd.ms-excel"})
    public Response getBCMExportSheet(@PathParam(CUSTOMER_ID) final String customerId,
                                      @PathParam(CONTRACT_ID) final String contractId,
                                      @PathParam(PROJECT_ID) final String projectId,
                                      @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                      @QueryParam("newBcmExportVersion") final String bcmExportVersion,
                                      @QueryParam("offerName") final String offerName) {
        final HSSFWorkbook workbook;
        if (bcmExportVersion.equalsIgnoreCase("Yes")) {
            workbook = bcmExportOrchestrator.renderBCMExportSheet(customerId, contractId, projectId, quoteOptionId, offerName);
        } else {
            workbook = quoteOptionbcmSheetExportOrchestrator.renderBcmExportSheet(customerId, contractId, projectId, quoteOptionId, offerName);
        }

        ProjectDTO projectDTO = projects.get(projectId);
        final QuoteOptionResource quoteOptions = projects.quoteOptionResource(projectId);
        QuoteOptionDTO quoteOptionDTO = quoteOptions.get(quoteOptionId);
        String bcmSheetName = "SQE_" + projectDTO.name + "_" + quoteOptionDTO.getName() + "_" + "BCM" + ".xls";
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                workbook.write(output);
            }
        }).header("Content-Disposition", "attachment; filename=" + bcmSheetName.replaceAll(" ", "")).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response importBcmSheet(@PathParam(CUSTOMER_ID) final String customerId,
                                   @PathParam(CONTRACT_ID) final String contractId,
                                   @PathParam(PROJECT_ID) final String projectId,
                                   @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                   @FormDataParam("bcmSheet") final InputStream bcmSheet) {
        return new HandlerActionAttempt(true) {
            @Override
            protected Response action() throws Exception {
                try {

                    if (isDiscountStatusValid(projectId, quoteOptionId)) {
                        return ResponseBuilder.badRequest().withEntity("The discount status is no longer Approval Requested. Please request discount approval before uploading the BCM sheet.").build();
                    }

                    quoteOptionBcmSheetImportOrchestrator.importBCMSheetDetails(customerId,
                                                                                contractId,
                                                                                projectId,
                                                                                quoteOptionId,
                                                                                new HSSFWorkbook(bcmSheet));

                } catch (InvalidExportDataException e) {
                    AjaxResponseDTO dto = new AjaxResponseDTO(false, e.getMessage());
                    return Response.status(Response.Status.OK).entity(JSONSerializer.getInstance().serialize(dto)).build();
                } catch (OfficeXmlFileException e) {
                    AjaxResponseDTO dto = new AjaxResponseDTO(false, "Please upload the BCM sheet in the same format/version as it was downloaded.");
                    return Response.status(Response.Status.OK).entity(JSONSerializer.getInstance().serialize(dto)).build();
                }

                return Response.ok().entity(JSONSerializer.getInstance().serialize(new AjaxResponseDTO(true, ""))).build();
            }
        }.tryToPerformAction(quoteOptionId);
    }

    @POST
    @Path("/approve-discounts")
    @Produces(MediaType.TEXT_HTML)
    public Response approveDiscounts(@PathParam(CUSTOMER_ID) final String customerId,
                                     @PathParam(PROJECT_ID) final String projectId,
                                     @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                     @FormParam(BID_MANAGER_COMMENTS) final String comments,
                                     @FormParam(BID_MANAGER_CAVEATS) final String caveats) {
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {

                logger.discountApprovalInitiated(customerId, projectId, quoteOptionId);
                final String loginName = UserContextManager.getCurrent().getLoginName();
                UserDTO userDTO = bidManagerCommentsFacade.getUserFacade().findUser(loginName);
                final String createdBy = String.format("%s %s", userDTO.forename, userDTO.surname);
                bidManagerCommentsFacade.saveCommentsAndCaveats(projectId, quoteOptionId, comments, caveats);
                List<BidManagerCommentsDTO> bidManagerComments = bidManagerCommentsFacade.getBidManagerComments(projectId, quoteOptionId);
                closeExpedioRequestDiscountActivity(bidManagerComments, caveats, projectId, quoteOptionId);
                //Update Discount Status as Approved for all applicable line items
                updateDiscountStatus(projectId, quoteOptionId);

                logger.discountApproved(customerId, projectId, quoteOptionId);


                return ResponseBuilder.anOKResponse().build();
            }
        }.tryToPerformAction(quoteOptionId);
    }

    @POST
    @Path("/reject-discounts")
    @Produces(MediaType.TEXT_HTML)
    public Response rejectDiscounts(@PathParam(CUSTOMER_ID) final String customerId,
                                    @PathParam(PROJECT_ID) final String projectId,
                                    @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                    @FormParam(BID_MANAGER_COMMENTS) final String comments) {
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                logger.discountRejectionInitiated(customerId, projectId, quoteOptionId);
                bidManagerCommentsFacade.saveCommentsAndCaveats(projectId, quoteOptionId, comments, null);
                quoteOptionBcmPricingSheetOrchestrator.rejectDiscounts(projectId, quoteOptionId);
                List<BidManagerCommentsDTO> bidManagerComments = bidManagerCommentsFacade.getBidManagerComments(projectId, quoteOptionId);
                closeExpedioRequestDiscountActivity(bidManagerComments, BID_TERMS_AND_CONDITIONS, projectId, quoteOptionId);
                logger.discountRejectionCompleted(customerId, projectId, quoteOptionId);
                return Response.ok().build();
            }
        }.tryToPerformAction(quoteOptionId);
    }

    @GET
    @Path("/commentsandcaveats")
    public Response getCommentsAndCaveats(@PathParam(CUSTOMER_ID) final String customerId,
                                          @PathParam(PROJECT_ID) final String projectId,
                                          @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                          @PathParam(CONTRACT_ID) final String contractId) {

        final List<BidManagerCommentsDTO> commentsAndCaveats = bidManagerCommentsFacade.getBidManagerComments(projectId, quoteOptionId);
        final BidManagerCommentsView bidManagerCommentsView = new BidManagerCommentsView(commentsAndCaveats, UriFactoryImpl.quoteOptionBcm(customerId, contractId, projectId, quoteOptionId).toString());
        String page = presenter.render(view("BidManagerCommentsAndCaveats.ftl")
                                           .withContext("newCommentAndCaveatURI", UriFactoryImpl.bidManagerCommentsAndCaveats(customerId, contractId, projectId, quoteOptionId))
                                           .withContext("view", bidManagerCommentsView));

        return Response.ok().entity(page).build();
    }


    //todo: move this to  the REST layer
    private void closeExpedioRequestDiscountActivity(List<BidManagerCommentsDTO> bidManagerComments, String caveats, String projectId, String quoteOptionId) {
        final QuoteOptionResource quoteOptionResource = projects.quoteOptionResource(projectId);
        final QuoteOptionDTO quoteOptionDTO = quoteOptionResource.get(quoteOptionId);
        if (quoteOptionDTO.activityId != null) {
            expedioServices.closeBidManagerDiscountApprovalRequestActivity(new CloseBidManagerActivityDTO(bidManagerComments, caveats, quoteOptionDTO.activityId, projectId));
            quoteOptionDTO.activityId = null;
            quoteOptionResource.put(quoteOptionDTO);
        }
    }

    private void updateDiscountStatus(String projectId, String quoteOptionId) {
        QuoteOptionItemResource quoteOptionItemResource = projects.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId);
        List<QuoteOptionItemDTO> quoteItems = quoteOptionItemResource.get();
        for (QuoteOptionItemDTO quoteItem : quoteItems) {
            if (LineItemDiscountStatus.APPROVAL_REQUESTED.equals(quoteItem.discountStatus)) {
                quoteItem.discountStatus = LineItemDiscountStatus.APPROVED;
                quoteOptionItemResource.put(quoteItem);
            }
        }
    }

    private boolean isDiscountStatusValid(String projectId, String quoteOptionId) {
        QuoteOptionItemResource quoteOptionItemResource = projects.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId);
        for (QuoteOptionItemDTO orderItem : quoteOptionItemResource.get()) {
            if (LineItemDiscountStatus.NEEDS_APPROVAL == orderItem.discountStatus) {
                return true;
            }
        }
        return false;
    }

    interface Logger {
        @Log(level = LogLevel.INFO, format = "Discount Approval Initiated for customer - %s, project - %s, quoteOption - %s")
        void discountApprovalInitiated(String customerId, String projectId, String quoteOptionId);

        @Log(level = LogLevel.INFO, format = "Discount Approved for customer - %s, project - %s, quoteOption - %s")
        void discountApproved(String customerId, String projectId, String quoteOptionId);

        @Log(level = LogLevel.INFO, format = "Discount Rejection Initiated for customer - %s, project - %s, quoteOption - %s")
        void discountRejectionInitiated(String customerId, String projectId, String quoteOptionId);

        @Log(level = LogLevel.INFO, format = "Discount Rejection Done for customer - %s, project - %s, quoteOption - %s")
        void discountRejectionCompleted(String customerId, String projectId, String quoteOptionId);
    }
}

