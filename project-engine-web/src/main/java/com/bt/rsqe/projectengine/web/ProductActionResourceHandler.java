package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.enums.ProductAction;
import com.bt.rsqe.pc.client.ConfiguratorProductClient;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.PriceBookFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.ProductOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.ProductOrchestratorFactory;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.AddOrModifyProductView;
import com.bt.rsqe.projectengine.web.view.PageView;
import com.bt.rsqe.projectengine.web.view.filtering.DataTableFilterValues;
import com.bt.rsqe.projectengine.web.view.filtering.FilterValues;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedAddProductFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.pagination.DefaultPagination;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.mappers.ProductCreationJsonObject;
import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Date;
import java.util.List;

import static com.bt.rsqe.enums.ProductAction.*;
import static com.bt.rsqe.projectengine.web.uri.UriFactoryImpl.*;
import static com.bt.rsqe.utils.AssertObject.isNull;
import static com.google.common.collect.Lists.*;
import static java.lang.System.*;

@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/add-product")
@Produces(MediaType.TEXT_HTML)
public class ProductActionResourceHandler extends QuoteViewFocusedResourceHandler {

    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String PRODUCT_ACTION = "productAction";
    private ProductOrchestratorFactory productOrchestratorFactory;
    private BreadCrumbFactory breadCrumbFactory;
    private PriceBookFacade priceBookFacade;
    private SiteFacade siteFacade;
    private LineItemFacade lineItemFacade;
    private ProjectResource projects;
    private CustomerResource customerResource;
    private ConfiguratorProductClient configuratorProductClient;
    private PmrClient pmrClient;
    private final String submitWebMetricsUri;
    private final String helpLinkUri;


    public ProductActionResourceHandler(final Presenter presenter,
                                        SiteFacade siteFacade,
                                        LineItemFacade lineItemFacade,
                                        ProductOrchestratorFactory productOrchestratorFactory,
                                        BreadCrumbFactory breadCrumbFactory,
                                        PriceBookFacade priceBookFacade,
                                        ProjectResource projects,
                                        CustomerResource customerResource,
                                        ConfiguratorProductClient configuratorProductClient, PmrClient pmrClient,
                                        String submitWebMetricsUri,String helpLinkUri) {
        super(presenter);
        this.lineItemFacade = lineItemFacade;
        this.siteFacade = siteFacade;
        this.productOrchestratorFactory = productOrchestratorFactory;
        this.breadCrumbFactory = breadCrumbFactory;
        this.priceBookFacade = priceBookFacade;
        this.projects = projects;
        this.customerResource = customerResource;
        this.configuratorProductClient = configuratorProductClient;
        this.pmrClient = pmrClient;
        this.submitWebMetricsUri = submitWebMetricsUri;
        this.helpLinkUri = helpLinkUri;
    }

    @GET
    public Response addProduct(@PathParam(CUSTOMER_ID) final String customerId,
                               @PathParam(CONTRACT_ID) final String contractId,
                               @PathParam(PROJECT_ID) final String projectId,
                               @PathParam(QUOTE_OPTION_ID) final String quoteOptionId) {
        return new QuoteViewFocusedResourceHandler.HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                final PageView view = new PageView("Showing Add Product", "Add Product",
                                                   breadCrumbFactory.createBreadCrumbsForOfferResource(projectId, quoteOptionId))
                    .addTab("AddProducts", "Add Product", productTabURI(customerId, contractId, projectId, quoteOptionId, Provide.description()).toString())
                    .addTab("ModifyProducts", "Remove/Modify Product", productTabURI(customerId, contractId, projectId, quoteOptionId, Modify.description()).toString())
                    .addTab("MoveProducts", "Move Product", productTabURI(customerId, contractId, projectId, quoteOptionId, Move.description()).toString())
                    .addTab("MigrateProducts", "Migrate Product", productTabURI(customerId, contractId, projectId, quoteOptionId, Migrate.description()).toString());

                final QuoteOptionResource quoteOptions = projects.quoteOptionResource(projectId);
                final QuoteOptionDTO quoteOption = quoteOptions.get(quoteOptionId);
                UserContext userContext = UserContextManager.getCurrent();
                final CustomerDTO customerDTO = customerResource.getByToken(customerId, userContext.getRsqeToken());

                String page = presenter.render(view("BasePage.ftl")
                                                   .withContext("view", view)
                                                   .withContext("customerDetails", customerDTO)
                                                   .withContext("quoteDetails", quoteOption)
                                                   .withContext("submitWebMetricsUri", submitWebMetricsUri)
                                                   .withContext("viewConfigurationDialogUri", UriFactoryImpl.viewConfigurationDialog(customerId, contractId, projectId).toString())
                                                   .withContext("helpLinkUri", helpLinkUri));
                return responseOk(page);
            }
        }.tryToPerformAction(quoteOptionId);

    }

    @GET
    @Path("/product-tab/{productAction}")
    public Response productTab(@PathParam(CUSTOMER_ID) final String customerId,
                               @PathParam(CONTRACT_ID) final String contractId,
                               @PathParam(PROJECT_ID) final String projectId,
                               @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                               @PathParam(PRODUCT_ACTION) final String productAction) {
        return new QuoteViewFocusedResourceHandler.HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                final ProductAction action = ProductAction.getAction(productAction);
                UserContext userContext = UserContextManager.getCurrent();

                final AddOrModifyProductView view = productOrchestratorFactory.getOrchestratorFor(action)
                                                                              .buildView(customerId,
                                                                                         contractId,
                                                                                         projectId,
                                                                                         quoteOptionId,
                                                                                         userContext.getPermissions().indirectUser,
                                                                                         action.description());

                String page = presenter.render(view("AddProductPage.ftl").withContext("view", view));
                return responseOk(page);
            }
        }.tryToPerformAction(quoteOptionId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("sites")
    public Response getSites(
        @PathParam(CUSTOMER_ID) final String customerId,
        @PathParam(PROJECT_ID) final String projectId,
        @QueryParam("iDisplayStart") final int pageStart,
        @QueryParam("iDisplayLength") final int pageSize,
        @QueryParam("sEcho") final int pageNumber,
        @QueryParam("forProduct") final String sCode,
        @QueryParam(PRODUCT_ACTION) final String productAction,
        @QueryParam("existingSiteId") final List<String> existingSiteIds,
        @QueryParam("newSiteId") final String newSiteId,
        @QueryParam("productVersion") final String productVersion,
        @Context final UriInfo uriInfo) {

        return new QuoteViewFocusedResourceHandler.HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                Pagination pagination = new DefaultPagination(pageNumber, pageStart, pageSize);

                final FilterValues filterValues = DataTableFilterValues.parse(uriInfo.getQueryParameters(true));
                final ProductAction action = ProductAction.getAction(productAction);
                final PaginatedFilter paginatedFilter = new PaginatedAddProductFilter(filterValues, pagination, action);

                final ProductOrchestrator productOrchestrator = productOrchestratorFactory.getOrchestratorFor(action);
                return Response.ok()
                               .entity(productOrchestrator.buildSitesView(customerId, projectId, paginatedFilter, sCode, newSiteId, existingSiteIds,
                                       isNull(productVersion) ? Optional.<String>absent() : Optional.of(productVersion)))
                               .build();
            }
        }.tryToPerformAction(customerId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("services")
    public Response getServices(
        @PathParam(CUSTOMER_ID) final String customerId,
        @QueryParam("iDisplayStart") final int pageStart,
        @QueryParam("iDisplayLength") final int pageSize,
        @QueryParam("sEcho") final int pageNumber,
        @QueryParam("forProduct") final String sCode,
        @QueryParam(PRODUCT_ACTION) final String productAction,
        @PathParam(CONTRACT_ID) final String contractId,
        @QueryParam("productVersion") final String productVersion
        ) {

        return new QuoteViewFocusedResourceHandler.HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                Pagination pagination = new DefaultPagination(pageNumber, pageStart, pageSize);

                final ProductAction action = ProductAction.getAction(productAction);

                final ProductOrchestrator productOrchestrator = productOrchestratorFactory.getOrchestratorFor(action);
                return Response.ok()
                               .entity(productOrchestrator.buildServicesView(customerId, sCode, contractId, productVersion, pagination))
                               .build();
            }
        }.tryToPerformAction(customerId);
    }

    @GET
    @Path("/{productSCode}/priceBooks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPriceBook(@PathParam(CUSTOMER_ID) String customerId,
                                 @PathParam("productSCode") String productScode) {
        List<PriceBookDTO> priceBookDTOs = newArrayList();
        UserContext userContext = UserContextManager.getCurrent();
        if (userContext.getPermissions().indirectUser) {
            priceBookDTOs = priceBookFacade.inDirectPriceBooks(customerId, productScode, ProductCategoryCode.NIL);  //TODO:HCode
        }
        GenericEntity<List<PriceBookDTO>> entity = new GenericEntity<List<PriceBookDTO>>(priceBookDTOs) {
        };
        return Response.ok().entity(entity).build();
    }

    @GET
    @Path("getLaunched")
    public Response getLaunched(
        @QueryParam("salesChannel") final String salesChannel,
        @QueryParam("productSCode") final String productSCode) {
        String launched = getDefaultProductOrchestrator().getLaunched(salesChannel, productSCode);
        return Response.ok().entity(launched).build();
    }

    @POST
    @Path("createProduct")
    public Response createProduct(@FormParam("quoteOptionContext") String jsonString,
                                  @PathParam(CUSTOMER_ID) String customerId,
                                  @PathParam(CONTRACT_ID) String contractId,
                                  @PathParam(PROJECT_ID) String projectId) {
        String quoteOptionItemSize = configuratorProductClient.createProduct(customerId, contractId, projectId, jsonString);
        return Response.ok().entity(quoteOptionItemSize).build();
    }

    @POST
    @Path("cardinalityCheck")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cardinalityCheck(@PathParam(PROJECT_ID) String projectId, @PathParam(CUSTOMER_ID) String customerId,
                                     @PathParam(CONTRACT_ID) String contractId, String jsonString) {
        ProductCreationJsonObject jsonObject = JSONSerializer.getInstance().deSerialize(jsonString, ProductCreationJsonObject.class);

        ProductOrchestrator productOrchestrator = productOrchestratorFactory.getOrchestratorFor(ProductAction.Provide);

        Notification notification = productOrchestrator.contractCardinalityCheck(customerId, contractId, jsonObject.productCode, jsonObject.productVersion,
                                                                                 jsonObject.quoteOptionId, jsonObject.lineItems.size());
        if ((jsonObject.action.equals(Move.toString()) && !isSameSiteMove(jsonObject.lineItems, jsonObject.newSiteId)) || !jsonObject.action.equals(Move.toString())) {
            for (ProductCreationJsonObject.ActionDTO lineItem : jsonObject.lineItems) {
                notification.add(productOrchestrator.siteCardinalityCheck(projectId,
                                                                          customerId,
                                                                          jsonObject.action.equals(Move.toString()) ? jsonObject.newSiteId : lineItem.siteId,
                                                                          jsonObject.productCode,
                                                                          jsonObject.productVersion,
                                                                          jsonObject.quoteOptionId));
            }
        }

        return Response.ok().entity(notification.toJson().toString()).build();
    }

    private boolean isSameSiteMove(List<ProductCreationJsonObject.ActionDTO> lineItems, String newSiteId) {
        return lineItems.size() > 0 && lineItems.get(0).siteId.equals(newSiteId);
    }

    @GET
    @Path("/selectNewSiteForm")
    public Response selectNewSiteForm(@PathParam(CUSTOMER_ID) String customerId,
                                      @PathParam(CONTRACT_ID) String contractId,
                                      @PathParam(PROJECT_ID) String projectId,
                                      @PathParam(QUOTE_OPTION_ID) String quoteOptionId) {
        UserContext userContext = UserContextManager.getCurrent();

        AddOrModifyProductView view = productOrchestratorFactory.getOrchestratorFor(ProductAction.Provide).buildView(customerId,
                                                                                                                     contractId,
                                                                                                                     projectId,
                                                                                                                     quoteOptionId,
                                                                                                                     userContext.getPermissions().indirectUser,
                                                                                                                     Provide.description());
        String page = presenter.render(view("NewSiteForm.ftl")
                               .withContext("view", view));

        return Response.ok().entity(page).build();
    }

    @GET
    @Path("/service-attributes")
    public Response getVisibleInSummaryAttributes(@QueryParam("productCode")String sCode) {
        List<String> attributeNames = pmrClient.productOffering(ProductSCode.newInstance(sCode)).get().getVisibleInSummaryAttributeDisplayNames();
        JsonArray names = new JsonArray();
        for (String attribute : attributeNames) {
            names.add(new JsonPrimitive(attribute));
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("names", names);

        return Response.ok().entity(jsonObject.toString()).build();
    }

    @POST
    @Path("endOfLifeValidation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response endOfLifeValidation(String jsonString) {
        ProductCreationJsonObject jsonObject = JSONSerializer.getInstance().deSerialize(jsonString, ProductCreationJsonObject.class);

        ProductOrchestrator productOrchestrator = productOrchestratorFactory.getOrchestratorFor(ProductAction.getAction(jsonObject.action));

        Notification notification = new Notification();
        for (ProductCreationJsonObject.ActionDTO lineItem : jsonObject.lineItems) {
            notification.add(productOrchestrator.endOfLifeCheck(lineItem.siteId,
                                                                jsonObject.productCode,
                                                                jsonObject.productVersion,
                                                                new Date(currentTimeMillis()),
                                                                jsonObject.contractResignStatus, lineItem.lineItemId));
        }
        return Response.ok().entity(notification.toJson().toString()).build();
    }

    private ProductOrchestrator getDefaultProductOrchestrator() {
        return productOrchestratorFactory.getOrchestratorFor(ProductAction.Provide);
    }

    @POST
    @Path("/sCode/{sCode}/availability-check")
    @Produces(MediaType.APPLICATION_JSON)
    public Response productAvailabilityCheck(@PathParam(PROJECT_ID) final String projectId,
                                             @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                             @PathParam("sCode") final String productCode,
                                             @QueryParam(PRODUCT_ACTION) final String productAction) {
        ProductOrchestrator productOrchestrator = productOrchestratorFactory.getOrchestratorFor(ProductAction.Provide);
        Notification notification = productOrchestrator.checkProductAvailability(projectId, quoteOptionId, productCode);
        return Response.ok().entity(notification.toJson().toString()).build();
    }
}
