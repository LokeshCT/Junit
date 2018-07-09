package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.LazyValue;
import com.bt.rsqe.cleanordervalidation.CleanOrderValidationException;
import com.bt.rsqe.cleanordervalidation.CleanOrderValidationResourceClient;
import com.bt.rsqe.customerinventory.client.CustomerInventoryClientConfig;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.customerinventory.driver.CustomerInventoryDriverManager;
import com.bt.rsqe.customerinventory.driver.CustomerInventoryDriverManagerFactory;
import com.bt.rsqe.customerinventory.dto.site.SiteDetailsDTO;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.SiteId;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.pc.client.ConfiguratorCloneToClient;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.OrderItemStatus;
import com.bt.rsqe.projectengine.OrderStatus;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.projectengine.web.facades.FutureProductInstanceFacade;
import com.bt.rsqe.projectengine.web.quoteoptionorders.QuoteOptionOrdersOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.QuoteOptionPricingOrchestrator;
import com.bt.rsqe.projectengine.web.tpe.TpeStatusManager;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.validators.BundleProductValidator;
import com.bt.rsqe.projectengine.web.view.OrderSubmissionDTO;
import com.bt.rsqe.projectengine.web.view.QuoteOptionOrdersView;
import com.bt.rsqe.projectengine.web.view.UpdateSiteView;
import com.bt.rsqe.quoteengine.domain.OrderItem;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.security.Credentials;
import com.bt.rsqe.security.ExpedioUserContextResolver;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.google.common.base.Optional;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import javax.ws.rs.CookieParam;
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
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.bt.rsqe.inlife.client.ApplicationCapabilityProvider.Capability.*;
import static com.google.common.collect.Lists.*;

@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/orders")
@Produces(MediaType.TEXT_HTML)
public class OrderResourceHandler extends QuoteViewFocusedResourceHandler {

    private Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private QuoteOptionOrdersOrchestrator ordersOrchestrator;
    private ProductInstanceClient productInstanceClient;
    private ExpedioUserContextResolver expedioUserContextResolver;
    private UserResource expedioUserResource;
    private ConfiguratorCloneToClient cloneToClient;
    private PmrClient pmr;
    private BundleProductValidator bundleProductValidator;
    private ProjectEngineWebConfig configuration;
    private RestRequestBuilder restRequestBuilder;
    private QRefExpiryDateValidator qRefExpiryDateValidator;
    private TpeStatusManager tpeStatusManager;
    private final CleanOrderValidationResourceClient cleanOrderValidationResourceClient;
    private ApplicationCapabilityProvider capabilityProvider;
    private FutureProductInstanceFacade futureProductInstanceFacade;
    private final ExecutorService executorService;
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String ORDER_ID = "orderId";
    private static final String SITE_ID = "siteId";
    private ExpedioProjectResource expedioProjectsResource;
    private SiteResourceClient siteResourceClient;

    public OrderResourceHandler(final Presenter presenter,
                                ProductInstanceClient productInstanceClient,
                                TpeStatusManager tpeStatusManager,
                                QuoteOptionOrdersOrchestrator ordersOrchestrator,
                                QuoteOptionPricingOrchestrator pricingOrchestrator,
                                ProjectEngineWebConfig configuration,
                                ExpedioUserContextResolver expedioUserContextResolver,
                                UserResource expedioUserResource,
                                ConfiguratorCloneToClient cloneToClient,
                                CleanOrderValidationResourceClient cleanOrderValidationResourceClient,
                                ApplicationCapabilityProvider capabilityProvider,
                                FutureProductInstanceFacade futureProductInstanceFacade,
                                ExecutorService executorService,
                                PmrClient pmr,
                                ExpedioProjectResource expedioProjectsResource,SiteResourceClient siteResourceClient,
                                BundleProductValidator bundleProductValidator) {
        super(presenter);
        this.ordersOrchestrator = ordersOrchestrator;
        this.tpeStatusManager = tpeStatusManager;
        this.productInstanceClient = productInstanceClient;
        this.configuration = configuration;
        this.expedioUserContextResolver = expedioUserContextResolver;
        this.expedioUserResource = expedioUserResource;
        this.cloneToClient = cloneToClient;
        this.pmr = pmr;
        this.bundleProductValidator = bundleProductValidator;
        this.qRefExpiryDateValidator = new QRefExpiryDateValidator(configuration, pricingOrchestrator, productInstanceClient);
        this.cleanOrderValidationResourceClient = cleanOrderValidationResourceClient;
        this.capabilityProvider = capabilityProvider;
        this.futureProductInstanceFacade = futureProductInstanceFacade;
        this.executorService = executorService;
        this.expedioProjectsResource=expedioProjectsResource;
        this.siteResourceClient=siteResourceClient;

    }


    @GET
    public Response getQuoteOptionOrdersTab(@PathParam(CUSTOMER_ID) final String customerId,
                                            @PathParam(CONTRACT_ID) final String contractId,
                                            @PathParam(PROJECT_ID) final String projectId,
                                            @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                            @CookieParam(Credentials.RSQE_TOKEN) final String userToken) {
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                final QuoteOptionOrdersView view;
                view = ordersOrchestrator.buildView(customerId, contractId, projectId, quoteOptionId, userToken);
                String page = presenter.render(view("OrdersTab.ftl").withContext("view", view));
                return responseOk(page);
            }
        }.tryToPerformAction(quoteOptionId);
    }

    @POST
    public Response createOrder(@PathParam(CUSTOMER_ID) final String customerId,
                                @PathParam(CONTRACT_ID) final String contractId,
                                @PathParam(PROJECT_ID) final String projectId,
                                @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                @FormParam("orderName") final String orderName,
                                @FormParam("offerItemIds") final String selectedLineItems,
                                @CookieParam(Credentials.RSQE_TOKEN) final String userToken) {

        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                List<String> lineItems = Arrays.asList(selectedLineItems.split(","));
                OfferAndOrderValidationResult validationResult = ordersOrchestrator.checkValidation(lineItems,
                                                                                                    projectId, quoteOptionId, customerId, contractId);
                if (!validationResult.isValid()) {
                    return ResponseBuilder.badRequest().withEntity(validationResult.getErrorMessage()).build();
                }

                OfferAndOrderValidationResult bundleProductValidationResult = bundleProductValidator.validate(projectId, quoteOptionId, lineItems);
                if(!bundleProductValidationResult.isValid) {
                    return ResponseBuilder.badRequest().withEntity(bundleProductValidationResult.getErrorMessage()).build();
                }
                OrderDTO orderDTO = ordersOrchestrator.buildOrder(orderName, projectId, quoteOptionId, selectedLineItems);
                if (orderDTO.status.equals(OrderStatus.CREATED.getValue())) {
                    tpeStatusManager.checkStatusAndSendTpeStatusChangeIfRequired(getUserEin(userToken, projectId), orderDTO, PricingStatus.ACTIVATE);
                }
                return responseRedirect(UriFactoryImpl.ordersTab(customerId, contractId, projectId, quoteOptionId));
            }
        }.tryToPerformAction(quoteOptionId);
    }

    @POST
    @Path("/{orderId}/cancel")
    public Response cancelOrder(@PathParam(CUSTOMER_ID) final String customerId,
                                @PathParam(CONTRACT_ID) final String contractId,
                                @PathParam(PROJECT_ID) final String projectId,
                                @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                @PathParam(ORDER_ID) final String orderId) {
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                ordersOrchestrator.cancelOrder(projectId, quoteOptionId, orderId);
                return Response.ok().build();
            }
        }.tryToPerformAction(quoteOptionId);

    }

    @POST
    @Path("/{orderId}/submit")
    @Produces(MediaType.APPLICATION_JSON)
    public Response submitOrder(@PathParam(CUSTOMER_ID) final String customerId,
                                @PathParam(CONTRACT_ID) final String contractId,
                                @PathParam(PROJECT_ID) final String projectId,
                                @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                @PathParam(ORDER_ID) final String orderId,
                                @CookieParam(Credentials.RSQE_TOKEN) final String userToken) {
        return new HandlerActionAttempt() {

            @Override
            protected Response action() throws Exception {
                logger.bomSubmissionRequestReceivedFor(customerId, contractId, projectId, quoteOptionId, orderId);
                boolean isFailedEarlier = ordersOrchestrator.getOrder(projectId, quoteOptionId, orderId).isFailed();
                ordersOrchestrator.updateOrderStatus(projectId, quoteOptionId, orderId, OrderItemStatus.IN_PROGRESS);

                UserContext userContext = expedioUserContextResolver.resolve(userToken, projectId);
                UserDTO user = expedioUserResource.findUser(userContext.getLoginName());
                ExpedioClientResources expedioClientResources = new ExpedioClientResources(configuration.getExpedioFacadeConfig());
                String centralSiteId = expedioClientResources.getCustomerResource().siteResource(customerId).getCentralSite(projectId).bfgSiteID;

                OrderSubmissionDTO orderEntity = new OrderSubmissionDTO();
                orderEntity.projectId = projectId;
                orderEntity.orderId = orderId;
                orderEntity.quoteOptionId = quoteOptionId;

                try {
                    OrderDTO order = ordersOrchestrator.getOrder(projectId, quoteOptionId, orderId);
                    List<String> lineItemIds = newArrayList(Iterables.transform(order.getOrderItems(), new Function<QuoteOptionItemDTO, String>() {
                        @Override
                        public String apply(QuoteOptionItemDTO item) {
                            return item.getId();
                        }
                    }));
                    OfferAndOrderValidationResult validationResult = ordersOrchestrator.checkValidation(lineItemIds, projectId, quoteOptionId, "", "");
                    if (!validationResult.isValid()) {
                        return ResponseBuilder.badRequest().withEntity(validationResult.getErrorMessage()).build();
                    }


                    if (capabilityProvider.isFunctionalityEnabled(IS_CLEAN_ORDER_VALIDATION_ENABLED, false, Optional.of(quoteOptionId)) && !ordersOrchestrator.isMigrationQuote(projectId, quoteOptionId)){
                        List<QuoteOptionItemDTO> orderItems = order.getOrderItems();
                        Map<Integer, List<Integer>> distinctBillingAndSiteIds = getDistinctBillingAndSiteIdFromOrder(excludeMopOrderItems(orderItems));
                        Map<Integer, List<Integer>> distinctCentralBillingAndSiteIds = getDistinctCentralBillingAndSiteIdFromOrder(excludeMopOrderItems(orderItems), centralSiteId);

                        final List<String> cleanOrderValidationErrors = Collections.synchronizedList(new LinkedList<String>());
                        final String salesChannelType = user.getUserType().properCase();
                        List<Callable<Object>> todo = new LinkedList<Callable<Object>>();

                        for (final Integer siteId : distinctBillingAndSiteIds.keySet()) {
                            for (final Integer billingAccountId : distinctBillingAndSiteIds.get(siteId)) {
                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            cleanOrderValidationResourceClient.validateExpedioAccount(siteId, billingAccountId, salesChannelType, projectId);
                                        } catch (CleanOrderValidationException ex) {
                                            cleanOrderValidationErrors.add("\nError: " + ex);
                                        }
                                    }
                                };
                                todo.add(Executors.callable(runnable));
                            }
                        }

                        if (distinctCentralBillingAndSiteIds.keySet() != null) {
                            for (final Integer siteId : distinctCentralBillingAndSiteIds.keySet()) {
                                for (final Integer billingAccountId : distinctCentralBillingAndSiteIds.get(siteId)) {
                                    Runnable runnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                cleanOrderValidationResourceClient.validateExpedioAccount(siteId, billingAccountId, salesChannelType, projectId);
                                            } catch (CleanOrderValidationException ex) {
                                                cleanOrderValidationErrors.add("\nError: " + ex);
                                            }
                                        }
                                    };
                                    todo.add(Executors.callable(runnable));
                                }
                            }
                        }



                        try {
                            executorService.invokeAll(todo);
                        } catch (InterruptedException e) {
                            logger.cleanOrderValidationFailed(orderId, e.getMessage());
                            ordersOrchestrator.updateOrderStatus(projectId, quoteOptionId, orderId, OrderItemStatus.ORDER_SUBMISSION_FAILED);
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(orderEntity).build();
                        }

                        if (!cleanOrderValidationErrors.isEmpty()) {
                            ordersOrchestrator.updateOrderStatus(projectId, quoteOptionId, orderId, OrderItemStatus.ORDER_SUBMISSION_FAILED);
                            StringBuilder errorList = new StringBuilder();
                            for (String error : cleanOrderValidationErrors) {
                                errorList.append(error + "\n");
                            }
                            sendOrderSubmissionFailedEmail(projectId, quoteOptionId, orderId, user, errorList.toString());
                            logger.emailNotificationFailed(orderId, "Clean Order Validation Failure");
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(orderEntity).build();
                        }
                    }
                    if (!isFailedEarlier) {
                        exerciseCloneToRelationships(projectId, quoteOptionId, orderId);
                    }
                    ordersOrchestrator.submitOrderAndCreateAssets(projectId, quoteOptionId, orderId, customerId, userContext.getPermissions().indirectUser, userContext.getLoginName());
                    order = ordersOrchestrator.getOrder(projectId, quoteOptionId, orderId);
                    makeStatusRefreshTpeCall(user, order);
                    sendEmailNotification(user, order, projectId, quoteOptionId, orderId, customerId, contractId);
                    return Response.status(Response.Status.OK).entity(orderEntity).build();
                } catch (Exception e) {
                    e.printStackTrace();   //Added as we just send the error message in the email, but stack trace is required for any analysis.
                    ordersOrchestrator.updateOrderStatus(projectId, quoteOptionId, orderId, OrderItemStatus.ORDER_SUBMISSION_FAILED);
                    sendOrderSubmissionFailedEmail(projectId, quoteOptionId, orderId, user, e.getMessage());
                    logger.emailNotificationFailed(orderId, "submitOrder(): " + e.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(orderEntity).build();
                }

            }
        }.tryToPerformAction(quoteOptionId);
    }

    private List<QuoteOptionItemDTO> excludeMopOrderItems(List<QuoteOptionItemDTO> orderItems) { //Mop Order items are exempted from clean order validation
        final List<String> mopProducts = pmr.mopRequiringProductCodes();
        return newArrayList(Iterables.filter(orderItems, new Predicate<QuoteOptionItemDTO>() {
            @Override
            public boolean apply(QuoteOptionItemDTO input) {
                return !mopProducts.contains(input.getProductCode());
            }
        }));
    }

    private void makeStatusRefreshTpeCall(UserDTO user, OrderDTO orderDTO) {
        try {
            if (orderDTO.isSubmitted()) {
                tpeStatusManager.checkStatusAndSendTpeStatusChangeIfRequired(LazyValue.eagerValue(user.getEin()), orderDTO, PricingStatus.WON);
            }
        } catch (Exception e) {
            logger.tpeStatusRefreshCallFailed(orderDTO.id);
        }
    }

    private void sendEmailNotification(UserDTO user, OrderDTO orderDTO, String projectId, String quoteOptionId, String orderId, String customerId, String contractId) {
        try {
            if (orderDTO.isSubmitted() && !shouldWaitForMopSubmission(orderDTO)) {   //The mop submission is an asynchronous activity, email will be triggered once done.
                sendOrderSubmissionEmail(projectId, quoteOptionId, orderId, user);
                logger.bomSubmittedSuccessfullyFor(customerId, contractId, projectId, quoteOptionId, orderId);
            } else if (orderDTO.isFailed()) {
                sendOrderSubmissionFailedEmail(projectId, quoteOptionId, orderId, user, OrderStatus.FAILED.getValue());
                logger.bomSubmissionFailedFor(customerId, contractId, projectId, quoteOptionId, orderId);
            }
        } catch (Exception e) {
            logger.emailNotificationFailed(orderId, "sendEmailNotification() :" + OrderStatus.FAILED.getValue());

        }
    }

    private boolean shouldWaitForMopSubmission(OrderDTO orderDTO) {
        List<String> mopProducts = pmr.mopRequiringProductCodes();
        return orderDTO.hasOrderItemOfProducts(mopProducts);
    }

    private void sendOrderSubmissionEmail(String projectId, String quoteOptionId, String orderId, UserDTO user) {
        OrderDTO orderDTO = ordersOrchestrator.getOrder(projectId, quoteOptionId, orderId);
        ordersOrchestrator.sendOrderSubmissionEmail(projectId, quoteOptionId, orderId, user, orderDTO.status);
    }

    private void sendOrderSubmissionFailedEmail(String projectId, String quoteOptionId, String orderId, UserDTO user, String errorLogs) {
        ordersOrchestrator.sendOrderSubmissionFailedEmail(projectId, quoteOptionId, orderId, user, errorLogs);
    }

    public Map<Integer, List<Integer>> getDistinctBillingAndSiteIdFromOrder(List<QuoteOptionItemDTO> orderItems) {
        HashMap<Integer, List<Integer>> distinctIdsMap = new HashMap<Integer, List<Integer>>(); //Key = Site Id, Value = List of Billing Account Ids

        for (QuoteOptionItemDTO orderItem : orderItems) {
            LineItemId lineItemId = new LineItemId(orderItem.getId());
            String siteId = futureProductInstanceFacade.getSiteId(lineItemId);
            boolean isCeaseLineItem = futureProductInstanceFacade.isCeased(lineItemId);
            if (!Strings.isNullOrEmpty(siteId) && !Strings.isNullOrEmpty(orderItem.billingId) && !isCeaseLineItem) {
                Integer billingId = Integer.parseInt(orderItem.billingId);
                Integer siteIdInteger = Integer.parseInt(siteId);

                if (distinctIdsMap.containsKey(siteIdInteger)) {
                    if (!distinctIdsMap.get(siteIdInteger).contains(billingId)) {
                        distinctIdsMap.get(siteIdInteger).add(billingId);
                    }
                } else {
                    distinctIdsMap.put(siteIdInteger, new LinkedList<Integer>(Arrays.asList(billingId)));
                }
            }
        }
        return distinctIdsMap;
    }

    private LazyValue<String> getUserEin(final String userToken, final String projectId) {
        return new LazyValue<String>() {
            @Override
            protected String initValue() {
                UserContext userContext = expedioUserContextResolver.resolve(userToken, projectId);
                UserDTO user = expedioUserResource.findUser(userContext.getLoginName());
                return user.getEin();
            }
        };
    }

    @GET
    @Path("/{orderId}/status")
    public Response getOrderStatus(@PathParam(PROJECT_ID) final String projectId,
                                   @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                   @PathParam(ORDER_ID) final String orderId) {
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                final String orderStatus = ordersOrchestrator.getOrderStatus(projectId, quoteOptionId, orderId);
                return Response.status(Response.Status.OK).entity(orderStatus).build();
            }
        }.tryToPerformAction(quoteOptionId);
    }


    @GET
    @Path("/siteId/{siteId}")
    @Produces(MediaType.TEXT_HTML)
    public Response getOrderSites(@PathParam(SITE_ID) final String siteId,
                                  @PathParam(QUOTE_OPTION_ID)final String quoteOptionId,
                                  @QueryParam("customerId") final String customerId,
                                  @QueryParam("siteType") final String siteType) {
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                SiteDTO siteDTO = ordersOrchestrator.getCustomerDatail(customerId, siteId, siteType);
                final UpdateSiteView view = new UpdateSiteView(siteDTO);
                view.setSiteField(view.new SiteCharDetail(view));
                String page = presenter.render(view("UpdateSite.ftl").withContext("view", view));
                return responseOk(page);
            }
        }.tryToPerformAction(quoteOptionId);
    }

    @POST
    @Path("siteDetail")
    public Response updateOrderSites(@FormParam("bfgSiteID") final String bfgSiteID,
                                     @FormParam("name") final String name,
                                     @FormParam("building") final String building,
                                     @FormParam("postCode") final String postCode,
                                     @FormParam("city") final String city,
                                     @FormParam("country") final String country,
                                     @FormParam("subLocality") final String subLocality,
                                     @FormParam("buildingNumber") final String buildingNumber,
                                     @FormParam("subBuilding") final String subBuilding,
                                     @FormParam("subPremise") final String subPremise,
                                     @FormParam("locality") final String locality,
                                     @FormParam("streetName") final String streetName,
                                     @FormParam("subStreet") final String subStreet,
                                     @FormParam("subStateCountyProvince") final String subStateCountyProvince,
                                     @FormParam("stateCountySProvince") final String stateCountySProvince,
                                     @FormParam("postBox") final String postBox,
                                     @FormParam("localCompanyName") final String localCompanyName) {
        try {
            SiteDetailsDTO siteDetailsDTO = new SiteDetailsDTO(bfgSiteID, name, building, country, postCode, city, subLocality, buildingNumber, subBuilding, subPremise, locality, streetName, subStreet, subStateCountyProvince, stateCountySProvince, postBox, localCompanyName);
            siteResourceClient.updateSiteDetails(siteDetailsDTO);
            logger.updateSiteDetail(bfgSiteID);
        } catch (Exception e) {
            logger.updateSitesFor(bfgSiteID);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.OK).build();
    }


    @GET
    @Path("/{orderId}/downloadBomXml")
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadBillOfMaterials(@PathParam(CUSTOMER_ID) final String customerId,
                                            @PathParam(CONTRACT_ID) final String contractId,
                                            @PathParam(PROJECT_ID) final String projectId,
                                            @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                            @PathParam(ORDER_ID) final String orderId,
                                            @CookieParam(Credentials.RSQE_TOKEN) final String userToken) {
        return new HandlerActionAttempt() {

            @Override
            protected Response action() throws Exception {
                logger.bomSubmissionRequestReceivedFor(customerId, contractId, projectId, quoteOptionId, orderId);
                UserContext userContext = expedioUserContextResolver.resolve(userToken, projectId);
                UserDTO user = expedioUserResource.findUser(userContext.getLoginName());
                OrderSubmissionDTO orderEntity = new OrderSubmissionDTO();
                orderEntity.projectId = projectId;
                orderEntity.orderId = orderId;
                orderEntity.quoteOptionId = quoteOptionId;

                try {
                    File zipFile = File.createTempFile(quoteOptionId, ".zip");
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    final ZipOutputStream zipos = new ZipOutputStream(baos);
                    Map<String, String> boms = ordersOrchestrator.downloadBomXML(projectId, quoteOptionId, orderId, customerId, userContext.getPermissions().indirectUser, userContext.getLoginName());
                    for (String lineItemId : boms.keySet()) {
                        File bomXml = File.createTempFile(lineItemId, ".xml");
                        FileWriter fileWritter = new FileWriter(bomXml);
                        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                        bufferWritter.write(boms.get(lineItemId));
                        bufferWritter.close();
                        FileInputStream fis = new FileInputStream(bomXml);
                        addToZipFile(fis, lineItemId + ".xml", zipos);
                    }
                    baos.flush();
                    baos.close();
                    zipos.flush();
                    zipos.close();
                    return Response.ok(new StreamingOutput() {
                        @Override
                        public void write(OutputStream output) throws IOException, WebApplicationException {
                            baos.writeTo(output);
                        }
                    }).header("Content-Disposition", "attachment; filename=" + quoteOptionId + ".zip".replaceAll(" ", "")).build();
                } catch (Exception e) {
                    e.printStackTrace();   //Added as we just send the error message in the email, but stack trace is required for any analysis.
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(orderEntity).build();
                }
            }
        }.tryToPerformAction(quoteOptionId);
    }

    private void exerciseCloneToRelationships(String projectId, String quoteOptionId, String orderId) {
        OrderDTO order = ordersOrchestrator.getOrder(projectId, quoteOptionId, orderId);
        for (QuoteOptionItemDTO quoteOptionItem : order.getOrderItems()) {
            logger.exerciseCloneToRelationshipsForLineItem(quoteOptionItem.getId());
            cloneToClient.exerciseCloneToRelationships(quoteOptionItem.getId());
        }
    }

    public void addToZipFile(FileInputStream fis, String fileName, ZipOutputStream zipos) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(fis);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipos.putNextEntry(zipEntry);
        byte bytes[] = new byte[2048];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipos.write(bytes, 0, length);
        }
        zipos.closeEntry();
        bis.close();
        fis.close();
    }

    public Map<Integer, List<Integer>> getDistinctCentralBillingAndSiteIdFromOrder(List<QuoteOptionItemDTO> orderItems, String centralSite) {
        HashMap<Integer, List<Integer>> distinctIdsMap = new HashMap<Integer, List<Integer>>(); //Key = Site Id, Value = List of Billing Account Ids


        for (QuoteOptionItemDTO orderItem : orderItems) {
            LineItemId lineItemId = new LineItemId(orderItem.getId());

            boolean isCeaseLineItem = futureProductInstanceFacade.isCeased(lineItemId);
            if (!Strings.isNullOrEmpty(centralSite) && !Strings.isNullOrEmpty(orderItem.billingId) && !isCeaseLineItem) {
                Integer billingId = Integer.parseInt(orderItem.billingId);
                Integer siteIdInteger = Integer.parseInt(centralSite);

                if (distinctIdsMap.containsKey(siteIdInteger)) {
                    if (!distinctIdsMap.get(siteIdInteger).contains(billingId)) {
                        distinctIdsMap.get(siteIdInteger).add(billingId);
                    }
                } else {
                    distinctIdsMap.put(siteIdInteger, new LinkedList<Integer>(Arrays.asList(billingId)));
                }
            }
        }
        return distinctIdsMap;
    }


    public static interface Logger {
        @Log(level = LogLevel.INFO, format = "Exercising cloneTo relationships for line item %s.")
        void exerciseCloneToRelationshipsForLineItem(String lineItemId);

        @Log(level = LogLevel.INFO, format = "Order submission email failed for order '%s'.  Caused by: %s")
        void emailNotificationFailed(String attributeName, String errorMessage);

        @Log(level = LogLevel.INFO, format = "Clean Order Validation failed for order '%s'.  Caused by: %s")
        void cleanOrderValidationFailed(String order, String errorMessage);

        @Log(level = LogLevel.INFO, format = "Order submission request received for customer '%s', contract '%s', project '%s', quoteOptionId '%s', orderId '%s'")
        void bomSubmissionRequestReceivedFor(String customerId, String contractId, String projectId, String quoteOptionId, String orderId);

        @Log(level = LogLevel.INFO, format = "Order submitted for customer '%s', contract '%s', project '%s', quoteOptionId '%s', orderId '%s'")
        void bomSubmittedSuccessfullyFor(String customerId, String contractId, String projectId, String quoteOptionId, String orderId);

        @Log(level = LogLevel.INFO, format = "Order submission failed for customer '%s', contract '%s', project '%s', quoteOptionId '%s', orderId '%s'")
        void bomSubmissionFailedFor(String customerId, String contractId, String projectId, String quoteOptionId, String orderId);

        @Log(level = LogLevel.ERROR, format = "Tpe status refresh attempt failed during BOM submission - orderId '%s'")
        void tpeStatusRefreshCallFailed(String orderId);

        @Log(level = LogLevel.ERROR, format = "Error while update the site details, SiteId: %s")
        void updateSitesFor(String bfgSiteID);

        @Log(level = LogLevel.INFO, format = "Site details updated for the siteId : %s.")
        void updateSiteDetail(String bfgSiteID);

    }
}
