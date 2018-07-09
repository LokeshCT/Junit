package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOrderFacade;
import com.bt.rsqe.projectengine.web.quoteoptionoffers.OfferDetailsOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionoffers.QuoteOptionOffersOrchestrator;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.validators.BundleProductValidator;
import com.bt.rsqe.projectengine.web.view.OfferDetailsDTO;
import com.bt.rsqe.projectengine.web.view.OfferDetailsTabView;
import com.bt.rsqe.projectengine.web.view.PageView;
import com.bt.rsqe.projectengine.web.view.QuoteOptionOffersView;
import com.bt.rsqe.projectengine.web.view.pagination.DefaultPagination;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.rest.exception.ForbiddenException;
import com.bt.rsqe.customerrecord.CustomerResource;

import javax.ws.rs.FormParam;
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

@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/offers")
@Produces(MediaType.TEXT_HTML)
public class OfferResourceHandler extends QuoteViewFocusedResourceHandler {

    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String PROJECT_ID = "projectId";
    private static final String OFFER_ID = "offerId";
    private ProjectResource projects;
    private BreadCrumbFactory breadCrumbFactory;
    private final QuoteOptionOffersOrchestrator offersOrchestrator;
    private final OfferDetailsOrchestrator offerDetailsOrchestrator;
    private CustomerResource customerResource;
    private final String submitWebMetricsUri;
    private  QuoteOptionOrderFacade orderFacade;
    private final String helpLinkUri;
    private BundleProductValidator bundleProductValidator;

    public OfferResourceHandler(final Presenter presenter,
                                ProjectResource projects,
                                QuoteOptionOffersOrchestrator offersOrchestrator,
                                OfferDetailsOrchestrator offerDetailsOrchestrator,
                                BreadCrumbFactory breadCrumbFactory,
                                CustomerResource customerResource,
                                String submitWebMetricsUri,
                                QuoteOptionOrderFacade orderFacade,
                                String helpLinkUri,
                                BundleProductValidator bundleProductValidator) {
        super(presenter);
        this.offersOrchestrator = offersOrchestrator;
        this.projects = projects;
        this.offerDetailsOrchestrator = offerDetailsOrchestrator;
        this.breadCrumbFactory = breadCrumbFactory;
        this.customerResource = customerResource;
        this.submitWebMetricsUri = submitWebMetricsUri;
        this.orderFacade = orderFacade;
        this.helpLinkUri = helpLinkUri;
        this.bundleProductValidator = bundleProductValidator;
    }

    @GET
    public Response getQuoteOptionOffersTab(@PathParam(CUSTOMER_ID) final String customerId,
                                            @PathParam(CONTRACT_ID) final String contractId,
                                            @PathParam(PROJECT_ID) final String projectId,
                                            @PathParam(QUOTE_OPTION_ID) final String quoteOptionId) {
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                QuoteOptionOffersView view = offersOrchestrator.buildView(customerId, contractId, projectId, quoteOptionId);
                List<OrderDTO> orderDtoList =  orderFacade.getAll(projectId, quoteOptionId);
                                      for(OrderDTO orderDto : orderDtoList) {
                                          view.setOrderSubmittedFlag(orderDto.isSubmitted());
                                          view.setOrderCreatedFlag(orderDto.isCreated());

                                      }

                String page = presenter.render(view("QuoteOptionOffersTab.ftl").withContext("view", view));
                return responseOk(page);
            }
        }.tryToPerformAction(customerId);

    }

    @GET
    @Path("/{offerId}")
    public Response getQuoteOptionOfferDetails(@PathParam(CUSTOMER_ID) final String customerId,
                                               @PathParam(CONTRACT_ID) final String contractId,
                                               @PathParam(PROJECT_ID) final String projectId,
                                               @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                               @PathParam(OFFER_ID) final String offerId) {
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                final QuoteOptionResource quoteOptions = projects.quoteOptionResource(projectId);
                final QuoteOptionDTO quoteOption = quoteOptions.get(quoteOptionId);

                UserContext userContext = UserContextManager.getCurrent();
                final CustomerDTO customerDTO = customerResource.getByToken(customerId, userContext.getRsqeToken());

                final PageView view = offerDetailsOrchestrator.buildDetailsView(projectId, quoteOptionId, offerId,
                                                                                breadCrumbFactory.createBreadCrumbsForOfferResource(
                                                                                    projectId, quoteOptionId))
                                                              .addTab("OfferDetailsTab", "Offer Details",
                                                                      offerDetailsTabUri(customerId, contractId, projectId, quoteOptionId, offerId));
                String page = presenter.render(view("BasePage.ftl")
                                                   .withContext("view", view)
                                                   .withContext("customerDetails", customerDTO)
                                                   .withContext("quoteDetails", quoteOption)
                                                   .withContext("submitWebMetricsUri", submitWebMetricsUri)
                                                   .withContext("viewConfigurationDialogUri", UriFactoryImpl.viewConfigurationDialog(customerId, contractId, projectId).toString())
                        .withContext("helpLinkUri", helpLinkUri));
                return responseOk(page);
            }
        }.tryToPerformAction(offerId);
    }

    private String offerDetailsTabUri(String customerId, String contractId, String projectId, String quoteOptionId, String offerId) {
        return UriFactoryImpl.offerDetailsTab(customerId, contractId, projectId, quoteOptionId, offerId).toString();
    }

    @GET
    @Path("/{offerId}/offer-details-tab")
    public Response getQuoteOptionOfferDetailsTab(@PathParam(CUSTOMER_ID) final String customerId,
                                                  @PathParam(CONTRACT_ID) final String contractId,
                                                  @PathParam(PROJECT_ID) final String projectId,
                                                  @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                                  @PathParam(OFFER_ID) final String offerId) {
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                final OfferDetailsTabView view = offerDetailsOrchestrator.buildDetailsTabView(customerId, contractId, projectId, quoteOptionId,
                                                                                              offerId);
                String page = presenter.render(view("OfferDetails.ftl").withContext("view", view));
                return responseOk(page);
            }
        }.tryToPerformAction(offerId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{offerId}/offer-details")
    public Response getQuoteOptionOfferDetails(@PathParam(CUSTOMER_ID) final String customerId,
                                               @PathParam(CONTRACT_ID) final String contractId,
                                               @PathParam(PROJECT_ID) final String projectId,
                                               @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                               @PathParam(OFFER_ID) final String offerId,
                                               @QueryParam("iDisplayStart") final int pageStart,
                                               @QueryParam("iDisplayLength") final int pageSize,
                                               @QueryParam("sEcho") final int pageNumber) {
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                Pagination pagination = new DefaultPagination(pageNumber, pageStart, pageSize);

                final OfferDetailsDTO dto = offerDetailsOrchestrator.buildJsonResponse(customerId, contractId, projectId, quoteOptionId, offerId,
                                                                                       pagination);
                return Response.ok().entity(dto).build();
            }
        }.tryToPerformAction(offerId);
    }

    @POST
    public Response createOffer(@PathParam(CUSTOMER_ID) String customerId,
                                @PathParam(CONTRACT_ID) String contractId,
                                @PathParam(PROJECT_ID) String projectId,
                                @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                @FormParam("offerName") String offerName,
                                @FormParam("quoteOptionItemIds") String quoteOptionItemIds,
                                @FormParam("customerOrderReference") String customerOrderReference) {


        try{
            List<String> lineItems = Arrays.asList(quoteOptionItemIds.split(","));
            OfferAndOrderValidationResult validationResult = offersOrchestrator.validateStatusForOfferCreation(projectId, quoteOptionId, customerId, contractId, lineItems);
            if(validationResult.isValid()){
                offersOrchestrator.validateProxyAssetConfigurationDetails(Arrays.asList(quoteOptionItemIds.split(",")));
            }
            if(!validationResult.isValid()){
              return ResponseBuilder.badRequest().withEntity(validationResult.getErrorMessage()).build();
            }
            OfferAndOrderValidationResult bundleProductValidationResult = bundleProductValidator.validate(projectId, quoteOptionId, lineItems);
            if(!bundleProductValidationResult.isValid) {
                return ResponseBuilder.badRequest().withEntity(bundleProductValidationResult.getErrorMessage()).build();
            }
            offersOrchestrator.buildOffer(projectId, quoteOptionId, offerName, quoteOptionItemIds,customerOrderReference);
           return Response.status(Response.Status.OK).entity(UriFactoryImpl.quoteOptionOffersTab(customerId, contractId, projectId, quoteOptionId).toString()).build();
        }catch (ForbiddenException e){
            return Response.status(Response.Status.FORBIDDEN).entity(e.errorDto().description).build();
        }

    }

    @POST
    @Path("/{offerId}/approve")
    public Response approveOfferPost(@PathParam(CUSTOMER_ID) String customerId,
                                     @PathParam(PROJECT_ID) String projectId,
                                     @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                     @PathParam(OFFER_ID) String offerId,
                                     @PathParam(CONTRACT_ID) String contractId) {
        try {
            OfferAndOrderValidationResult validationResult = offersOrchestrator.validateStatusForOfferApproval(projectId, quoteOptionId, customerId, contractId);
            if (!validationResult.isValid()) {
                return ResponseBuilder.badRequest().withEntity(validationResult.getErrorMessage()).build();
            }
            offersOrchestrator.approveOffer(projectId, quoteOptionId, offerId);
            return Response.ok().build();
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.errorDto().description).build();
        }
    }

    @GET
    @Path("/{offerId}/reject")
    public Response rejectOfferGet(@PathParam(CUSTOMER_ID) String customerId,
                                   @PathParam(CONTRACT_ID) String contractId,
                                   @PathParam(PROJECT_ID) String projectId,
                                   @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                   @PathParam(OFFER_ID) String offerId) {
        offersOrchestrator.rejectOffer(projectId, quoteOptionId, offerId);
        return responseRedirect(UriFactoryImpl.quoteOptionOffersTab(customerId, contractId, projectId, quoteOptionId));
    }

    @POST
    @Path("/{offerId}/reject")
    public Response rejectOfferPost(@PathParam(CUSTOMER_ID) String customerId,
                                    @PathParam(CONTRACT_ID) String contractId,
                                    @PathParam(PROJECT_ID) String projectId,
                                    @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                    @PathParam(OFFER_ID) String offerId) {
        offersOrchestrator.rejectOffer(projectId, quoteOptionId, offerId);
        return responseRedirect(UriFactoryImpl.quoteOptionOffersTab(customerId, contractId, projectId, quoteOptionId));
    }

    @GET
    @Path("/{offerId}/cancel-approval")
    public Response cancelOfferApproval(@PathParam(CUSTOMER_ID) String customerId,
                                        @PathParam(CONTRACT_ID) String contractId,
                                        @PathParam(PROJECT_ID) String projectId,
                                        @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                        @PathParam(OFFER_ID) String offerId) {
        offersOrchestrator.cancelOfferApproval(projectId, quoteOptionId, offerId);
        return responseRedirect(UriFactoryImpl.quoteOptionOffersTab(customerId, contractId, projectId, quoteOptionId));
    }
}
