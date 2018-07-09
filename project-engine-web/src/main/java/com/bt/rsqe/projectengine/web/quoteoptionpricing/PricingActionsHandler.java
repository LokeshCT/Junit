package com.bt.rsqe.projectengine.web.quoteoptionpricing;

import com.bt.rsqe.customerrecord.UserRole;
import com.bt.rsqe.customerrecord.UsersDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.expedio.services.BidManagerApprovalRequestDTO;
import com.bt.rsqe.expedio.services.BidManagerApprovalResponseDTO;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.RequestDiscountResponseDTO;
import com.bt.rsqe.projectengine.web.facades.BidManagerCommentsFacade;
import com.bt.rsqe.projectengine.web.facades.ExpedioServicesFacade;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.UserFacade;
import com.bt.rsqe.projectengine.web.view.QuoteOptionRevenueDTO;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.web.rest.exception.BadRequestException;
import com.bt.rsqe.web.rest.exception.InternalServerErrorException;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;

@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/pricing-actions")
public class PricingActionsHandler {

    private Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private static final String CUSTOMER_ID = "customerId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String CONTRACT_ID = "contractId";
    private static final String DISCOUNT_APPROVAL_STATUS_SUCCESS = "success";
    private static final String DISCOUNT_APPROVAL_STATUS_FAIL = "fail";
    private static final String DISCOUNT_APPROVAL_CREATOR_REASON = "Discount Approval Request";
    private static final String DISCOUNT_APPROVAL_SUCCESS_MESSAGE = "Request has been sent successfully";
    private static final String DISCOUNT_APPROVAL_FAIL_MESSAGE = "Discount approval request failed";
    private final ProjectResource projects;
    private final UserFacade userFacade;
    private final ExpedioServicesFacade expedioServicesFacade;
    private final QuoteOptionFacade quoteOptionFacade;
    private FutureAssetPricesFacade productInstancePricesFacade;
    private QuoteOptionRevenueOrchestrator revenueOrchestrator;
    private BidManagerCommentsFacade bidManagerCommentsFacade;
    private static final String FIRST_QUOTE_VERSION = "1.0";

    public PricingActionsHandler(UserFacade userFacade,
                                 ProjectResource projects,
                                 ExpedioServicesFacade expedioServicesFacade,
                                 QuoteOptionFacade quoteOptionFacade,
                                 FutureAssetPricesFacade productInstancePricesFacade, QuoteOptionRevenueOrchestrator revenueOrchestrator,
                                 BidManagerCommentsFacade bidManagerCommentsFacade) {
        this.userFacade = userFacade;
        this.projects = projects;
        this.expedioServicesFacade = expedioServicesFacade;
        this.quoteOptionFacade = quoteOptionFacade;
        this.productInstancePricesFacade = productInstancePricesFacade;
        this.revenueOrchestrator = revenueOrchestrator;
        this.bidManagerCommentsFacade = bidManagerCommentsFacade;
    }

    @GET
    @Path("bid-managers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findBidManagers(@PathParam(CUSTOMER_ID) String customerId) {
        UsersDTO response = userFacade.findUsers(customerId, UserRole.BID_MANAGER);
        return Response.ok().entity(response).build();
    }


    @POST
    @Path("request-discount-approval")
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestDiscountApproval(@PathParam(CUSTOMER_ID) String customerId,
                                            @PathParam(PROJECT_ID) String projectId,
                                            @PathParam(CONTRACT_ID) String contractId,
                                            @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                            @QueryParam("bidManagerEmail") String bidManagerEmail,
                                            @QueryParam("groupEmailId") String groupEmailId,
                                            @QueryParam("comment") String salesUserComment,
                                            String jsonString){

        logger.approvalRequestInitiated(customerId, projectId, contractId, quoteOptionId, bidManagerEmail, groupEmailId);
        saveSalesUserCommentIfExist(projectId, quoteOptionId, salesUserComment);
        boolean isCommercialNonStandardRequested = false;
        QuoteOptionRevenueDTO revenueDTO = StringUtils.isEmpty(jsonString)? null : JSONSerializer.getInstance().deSerialize(jsonString, QuoteOptionRevenueDTO.class);
        if (isNotNull(revenueDTO) && isNotNull(revenueDTO.getItemDTOs())) {
            try {
                revenueOrchestrator.persistRevenueDetails(projectId, quoteOptionId, customerId, contractId, revenueDTO);
                isCommercialNonStandardRequested = true;
            } catch (Exception ex) {
                //Do Nothing
            }
        }
        //todo: move this workflow to  the REST layer
        final ProjectDTO project = projects.get(projectId);
        final com.bt.rsqe.expedio.project.ProjectDTO expedioProject = expedioServicesFacade.getExpedioProject(projectId);
        UserDTO userDTO = expedioServicesFacade.getUserDetails(UserContextManager.getCurrent().getLoginName());
        QuoteOptionDTO quoteOptionDTO = quoteOptionFacade.get(projectId, quoteOptionId);
        List<BidManagerCommentsDTO> bidManagerComments = bidManagerCommentsFacade.getBidManagerComments(projectId, quoteOptionId);
        BidManagerApprovalRequestDTO discountApprovalRequest = new BidManagerApprovalRequestDTO(bidManagerComments, bidManagerEmail,
                                                                              expedioProject.expRef,
                                                                              DISCOUNT_APPROVAL_CREATOR_REASON,
                                                                              expedioProject.orderType,
                                                                              project.id,
                                                                              FIRST_QUOTE_VERSION,
                                                                              groupEmailId,
                                                                              userDTO.email,
                                                                              userDTO.loginName);

        RequestDiscountResponseDTO dto;
        try {
            final BidManagerApprovalResponseDTO responseDTO = expedioServicesFacade.requestDiscountApproval(discountApprovalRequest);
            persistDiscountApprovalRequestState(projectId, quoteOptionId, responseDTO, isCommercialNonStandardRequested);
            dto = new RequestDiscountResponseDTO(DISCOUNT_APPROVAL_STATUS_SUCCESS,
                                                 DISCOUNT_APPROVAL_SUCCESS_MESSAGE);
            logger.approvalRequestCompleted(customerId, projectId, contractId, quoteOptionId, bidManagerEmail, groupEmailId);

        } catch (BadRequestException bre) {
            dto = new RequestDiscountResponseDTO(DISCOUNT_APPROVAL_STATUS_FAIL,
                                                 bre.getMessage());
            logger.approvalRequestFailed(customerId, projectId, contractId, quoteOptionId, bidManagerEmail, groupEmailId);

        } catch (InternalServerErrorException ise) {
            dto = new RequestDiscountResponseDTO(DISCOUNT_APPROVAL_STATUS_FAIL,
                                                 ise.getMessage());
            logger.approvalRequestFailed(customerId, projectId, contractId, quoteOptionId, bidManagerEmail, groupEmailId);
        }
        return Response.ok().entity(dto).build();
    }

    @POST
    @Path("unlock-price-lines")
    public Response unlockPriceLines(@PathParam(PROJECT_ID) String projectId,
                                     @PathParam(QUOTE_OPTION_ID) String quoteOptionId) {
        quoteOptionFacade.unlockApprovedPriceLines(projectId, quoteOptionId);
        return Response.ok().build();
    }

    @POST
    @Path("minimumRevenue")
    public Response saveMinimumRevenue(@PathParam(PROJECT_ID) String projectId,
                                     @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                     @PathParam(CUSTOMER_ID) String customerId,
                                     @PathParam(CONTRACT_ID) String contractId,
                                     String jsonString){
        QuoteOptionRevenueDTO dto = JSONSerializer.getInstance().deSerialize(jsonString, QuoteOptionRevenueDTO.class);
        revenueOrchestrator.persistRevenueDetails(projectId,quoteOptionId, customerId, contractId, dto);
        return Response.ok().build();
    }

    private void persistDiscountApprovalRequestState(String projectId, String quoteOptionId, BidManagerApprovalResponseDTO responseDTO, boolean isCommercialNonStandardRequested) {
        final QuoteOptionResource quoteOptionResource = projects.quoteOptionResource(projectId);
        QuoteOptionItemResource quoteOptionItemResource = quoteOptionResource.quoteOptionItemResource(
            quoteOptionId);
        List<QuoteOptionItemDTO> quoteOptionItemDTOs = quoteOptionItemResource.get();
        for (QuoteOptionItemDTO quoteOptionItemDTO : quoteOptionItemDTOs) {
            if(!quoteOptionItemDTO.readOnly) {
                quoteOptionItemDTO.discountStatus = LineItemDiscountStatus.APPROVAL_REQUESTED;
                if(isCommercialNonStandardRequested){
                    quoteOptionItemDTO.status = QuoteOptionItemStatus.COMMERCIAL_NON_STANDARD_REQUESTED;
                }
                quoteOptionItemResource.put(quoteOptionItemDTO);
            }
        }

        QuoteOptionDTO quoteOptionDTO = quoteOptionResource.get(quoteOptionId);
        quoteOptionDTO.activityId = responseDTO.activityId;
        quoteOptionResource.put(quoteOptionDTO);
    }

    private void saveSalesUserCommentIfExist(String projectId, String quoteOptionId, String salesUserComment) {
        if (!isEmpty(salesUserComment)) {
            bidManagerCommentsFacade.saveCommentsAndCaveats(projectId, quoteOptionId, salesUserComment, "");
        }
    }

    interface Logger {
        @Log(level = LogLevel.INFO, format = "Discount approval request initiated for customer - %s, project - %s, contract - %s, quoteOption - %s, bidManagerEmail - %s, groupMailId - %s")
        void approvalRequestInitiated(String customerId, String projectId, String contractId, String quoteOptionId, String bidManagerEmail, String groupEmailId);

        @Log(level = LogLevel.INFO, format = "Discount approval request completed for customer - %s, project - %s, contract - %s, quoteOption - %s, bidManagerEmail - %s, groupMailId - %s")
        void approvalRequestCompleted(String customerId, String projectId, String contractId, String quoteOptionId, String bidManagerEmail, String groupEmailId);

        @Log(level = LogLevel.INFO, format = "Discount approval Request Failed for customer - %s, project - %s, contract - %s, quoteOption - %s, bidManagerEmail - %s, groupMailId - %s")
        void approvalRequestFailed(String customerId, String projectId, String contractId, String quoteOptionId, String bidManagerEmail, String groupEmailId);
    }
}
