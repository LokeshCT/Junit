package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.configuration.UrlConfig;
import com.bt.rsqe.cookie.JsonCookie;
import com.bt.rsqe.cookie.LineItemFilterCookie;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.domain.DateFormats;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.dto.NoteDto;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider.Capability;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.OfferDTO;
import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.web.view.Offer;
import com.bt.rsqe.projectengine.web.view.Order;
import com.bt.rsqe.projectengine.web.view.QuoteOption;
import com.bt.rsqe.projectengine.web.view.ViewConfigurationDialogView;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemCloneDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.QuoteOptionStatus;
import com.bt.rsqe.projectengine.migration.QuoteMigrationFlagManager;
import com.bt.rsqe.projectengine.migration.QuoteMigrationFlagValidationException;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.projectengine.web.facades.ExpedioServicesFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionNoteFacade;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBulkUploadOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionPricingSummaryDTO;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionPricingSummaryOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.QuoteOptionDetailsOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.QuoteOptionPricingOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.QuoteOptionRevenueOrchestrator;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.CloneTargetOptionsListView;
import com.bt.rsqe.projectengine.web.view.NotesView;
import com.bt.rsqe.projectengine.web.view.PageView;
import com.bt.rsqe.projectengine.web.view.QuoteOptionDetailsView;
import com.bt.rsqe.projectengine.web.view.QuoteOptionDialogView;
import com.bt.rsqe.projectengine.web.view.QuoteOptionPricingDTO;
import com.bt.rsqe.projectengine.web.view.QuoteOptionRevenueDTO;
import com.bt.rsqe.projectengine.web.view.QuoteOptionUsagePricingDTO;
import com.bt.rsqe.projectengine.web.view.filtering.DataTableFilterValues;
import com.bt.rsqe.projectengine.web.view.filtering.FilterValues;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedPricingTabViewFilter;
import com.bt.rsqe.projectengine.web.view.filtering.pricing.PaginatedPricingTabViewFilterNew;
import com.bt.rsqe.projectengine.web.view.filtering.PricingTabView;
import com.bt.rsqe.projectengine.web.view.filtering.PricingTabViewNew;
import com.bt.rsqe.projectengine.web.view.pagination.DefaultPagination;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.security.Credentials;
import com.bt.rsqe.security.ExpedioRsqeCredentials;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.session.client.ExpedioSessionResource;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.web.AjaxResponseDTO;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.rest.exception.BadRequestException;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

import static com.bt.rsqe.inlife.client.ApplicationCapabilityProvider.Capability.FILTER_CEASED_BFG_SITES;
import static javax.ws.rs.core.Response.Status.*;

@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options")
@Produces(MediaType.TEXT_HTML + ";charset=ISO-8859-15")
public class QuoteOptionResourceHandler extends QuoteViewFocusedResourceHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(QuoteOptionResourceHandler.class);

    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String EXPEDIO_QUOTE_OPTION_ID = "expedioQuoteOptionId";
    private static final String QUOTE_OPTION_ITEM_IDS = "quoteOptionItemIdsToCopy";
    private static final String OFFER_ID = "offerId";
    private final QuoteOptionDetailsOrchestrator detailsOrchestrator;
    private final QuoteOptionPricingOrchestrator pricingOrchestrator;
    private QuoteOptionPricingSummaryOrchestrator quoteOptionPricingSummaryOrchestrator;
    private final BreadCrumbFactory breadCrumbFactory;
    private final QuoteOptionNoteFacade quoteOptionNoteFacade;
    private PricingSheetOrchestrator pricingSheetOrchestrator;
    private QuoteOptionBulkUploadOrchestrator bulkUploadOrchestrator;
    private final ProjectResource projects;
    private CustomerResource customerResource;
    private QuoteOptionRevenueOrchestrator revenueOrchestrator;
    private JSONSerializer jsonSerializer;
    private ProjectEngineWebConfig config;
    private ExpedioServicesFacade expedioServicesFacade;
    private ExpedioClientResources expedioClientResources;
    private ExpedioSessionResource expedioSessionResource;
    private final String submitWebMetricsUri;
    private ApplicationCapabilityProvider applicationCapabilityProvider;
    private ProductInstanceClient productInstanceClient;
    private final String helpLinkUri;

    public QuoteOptionResourceHandler(ProjectEngineWebConfig config,
                                      final Presenter presenter,
                                      ProjectResource projects,
                                      QuoteOptionDetailsOrchestrator detailsOrchestrator,
                                      QuoteOptionPricingOrchestrator pricingOrchestrator,
                                      BreadCrumbFactory breadCrumbFactory,
                                      QuoteOptionNoteFacade quoteOptionNoteFacade,
                                      PricingSheetOrchestrator pricingSheetOrchestrator,
                                      QuoteOptionPricingSummaryOrchestrator quoteOptionPricingSummaryOrchestrator,
                                      QuoteOptionBulkUploadOrchestrator bulkUploadOrchestrator,
                                      QuoteOptionRevenueOrchestrator revenueOrchestrator, JSONSerializer jsonSerializer,
                                      CustomerResource customerResource, ExpedioServicesFacade expedioServicesFacade,
                                      ExpedioClientResources expedioClientResources,
                                      ExpedioSessionResource expedioSessionResource,
                                      String submitWebMetricsUri,
                                      ApplicationCapabilityProvider applicationCapabilityProvider,
                                      ProductInstanceClient productInstanceClient, String helpLinkUri) {
        super(presenter);
        this.detailsOrchestrator = detailsOrchestrator;
        this.pricingOrchestrator = pricingOrchestrator;
        this.breadCrumbFactory = breadCrumbFactory;
        this.quoteOptionNoteFacade = quoteOptionNoteFacade;
        this.pricingSheetOrchestrator = pricingSheetOrchestrator;
        this.quoteOptionPricingSummaryOrchestrator = quoteOptionPricingSummaryOrchestrator;
        this.bulkUploadOrchestrator = bulkUploadOrchestrator;
        this.projects = projects;
        this.revenueOrchestrator = revenueOrchestrator;
        this.jsonSerializer = jsonSerializer;
        this.config = config;
        this.customerResource = customerResource;
        this.expedioServicesFacade = expedioServicesFacade;
        this.expedioClientResources = expedioClientResources;
        this.expedioSessionResource = expedioSessionResource;
        this.submitWebMetricsUri = submitWebMetricsUri;
        this.applicationCapabilityProvider = applicationCapabilityProvider;
        this.productInstanceClient = productInstanceClient;
        this.helpLinkUri = helpLinkUri;
    }

    /**
     * Builds up the Quote Options page and each of it's tabs.
     * @param customerId ID of the Customer requesting the page.
     * @param contractId ID of the Customer's contract.
     * @param projectId ID of the project.
     * @param quoteOptionId ID of the quote option.
     * @return
     */
    @GET
    @Path("/{quoteOptionId}")
    public Response getQuoteOption(
        @PathParam(CUSTOMER_ID) String customerId,
        @PathParam(CONTRACT_ID) String contractId,
        @PathParam(PROJECT_ID) String projectId,
        @PathParam(QUOTE_OPTION_ID) String quoteOptionId)
    {
        LOGGER.info("Generating Quote Option page.");
        QuoteOptionResource quoteOptions = projects.quoteOptionResource(projectId);
        QuoteOptionDTO quoteOption       = quoteOptions.get(quoteOptionId);
        CustomerDTO customerDTO          = customerResource.getByToken(customerId, UserContextManager.getCurrent().getRsqeToken());

        if(applicationCapabilityProvider.isFunctionalityEnabled(FILTER_CEASED_BFG_SITES, true, Optional.of(quoteOptionId))) {
            projects.quoteOptionResource(projectId).checkAndCeaseLineItems(customerId, quoteOptionId);
        }

        // Create the Quote Options Details page.
        PageView view = new PageView("Showing Quote Option", "Quote Option Details", breadCrumbFactory.createBreadCrumbsForQuoteOptionResource(projectId));
            view.addTab("QuoteOptionDetailsTab", "Details", quoteOptionDetailsTabUri(customerId, contractId, projectId, quoteOptionId));

        // Add the tab to the Quote Options Page.
        view = addPricingTab(view, customerId, contractId, projectId, quoteOptionId);
        view = addOptionTabs(view, customerId, contractId, projectId, quoteOption);

        String page = presenter.render(view("BasePage.ftl")
                                           .withContext("view", view)
                                           .withContext("customerDetails", customerDTO)
                                           .withContext("quoteDetails", quoteOption)
                                           .withContext("submitWebMetricsUri", submitWebMetricsUri)
                                           .withContext("viewConfigurationDialogUri", UriFactoryImpl.viewConfigurationDialog(customerId, contractId, projectId).toString())
                                           .withContext("helpLinkUri", helpLinkUri));
        return responseOk(page);
    }

    /**
     * Determines which Pricing Tab (new or old) should be added to the Quote Options page and then adds it.
     * This decision is based upon the value of the 'useNewPricingTab' flag in the Inlife database's APPLICATION_PROPERTY_STORE table.
     *
     * Note: The first parameter given in the view.addTab calls determines the name of the JavaScript class that will be instansiated when
     * the Free Marker template is loaded. See the eval function call in tabLoaded function of the BasePage.js file for more details.
     *
     * @param view The Page View to add the selected tabs to.
     * @param customerId ID of the Customer requesting the page.
     * @param contractId ID of the Customer's contract.
     * @param projectId ID of the project.
     * @param quoteOptionId ID of the quote option.
     * @return The Page View with the correct Pricing tabs appended.
     */
    public PageView addPricingTab(PageView view,
                                  String customerId,
                                  String contractId,
                                  String projectId,
                                  String quoteOptionId)
    {
        LOGGER.info("Determining which pricing tab (new or old) to add to the Quote Options Page.");
        if (applicationCapabilityProvider.isFunctionalityEnabled(Capability.USE_NEW_PRICING_TAB, false, Optional.of(quoteOptionId)))
        {
            view.addTab("PricingTab", "Pricing", quoteOptionPricingTabUriNew(customerId, contractId, projectId, quoteOptionId));
            LOGGER.info("Added new QuoteOptionPricingTab to the Quote Options Page.");
        }
        else
        {
            view.addTab("QuoteOptionPricingTab", "Pricing", quoteOptionPricingTabUri(customerId, contractId, projectId, quoteOptionId));
            LOGGER.info("Added old QuoteOptionPricingTab to the Quote Options Page.");
        }
        return view;
    }

    /**
     * Determines which Optional tabs to display on the Quote Options page.
     * Both additional tabs require that there are orders to display.
     * The Offers tab is only displayed if there are non-obsolete line items to display.
     * @param view The Page View to add the selected tabs to.
     * @param customerId ID of the Customer requesting the page.
     * @param contractId ID of the Customer's contract.
     * @param projectId ID of the project.
     * @param quoteOption The quote option.
     * @return The Page View with the relevant Option tabs appended.
     */
    public PageView addOptionTabs(PageView view,
                                  String customerId,
                                  String contractId,
                                  String projectId,
                                  QuoteOptionDTO quoteOption)
    {
        String quoteOptionId = quoteOption.getId();
        LOGGER.info("Determining which optional tabs to add to the Quote Options page with quote option id = {}", quoteOptionId);

        if (quoteOption.hasOffers && !quoteOption.lineItemsAreObsolete)
        {
            view.addTab("QuoteOptionOffersTab", "Offers", offersTabUri(customerId, contractId, projectId, quoteOptionId));
            LOGGER.info("Added QuoteOptionOffersTab to the Quote Options Page.");
        }

        if (quoteOption.hasOrders)
        {
            view.addTab("OrdersTab", "Orders", ordersTabUri(customerId, contractId, projectId, quoteOptionId));
            LOGGER.info("Added OrdersTab to the Quote Options Page.");
        }
        return view;
    }

    /**
     * Load the existing Pricing tab.
     * This Tab will be removed after the Pricing re-write has been completed.
     * @param customerId ID of the Customer requesting the page.
     * @param contractId ID of the Customer's contract.
     * @param projectId ID of the project.
     * @param quoteOptionId ID of the quote option.
     * @return
     */
    @GET
    @Path("/{quoteOptionId}/pricing-tab")
    public Response getQuoteOptionPricingTab(@PathParam(CUSTOMER_ID) final String customerId,
                                             @PathParam(CONTRACT_ID) final String contractId,
                                             @PathParam(PROJECT_ID) final String projectId,
                                             @PathParam(QUOTE_OPTION_ID) final String quoteOptionId)
    {
        LOGGER.info("Returning Pricing Tab for REST end point /{quoteOptionId}/pricing-tab.");
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                final PricingTabView pricingTabView = pricingOrchestrator.getPricingTabView(customerId, contractId, projectId, quoteOptionId);
                String page = presenter.render(view("QuoteOptionPricingTab.ftl")
                        .withContext(CUSTOMER_ID, customerId)
                        .withContext(CONTRACT_ID, contractId)
                        .withContext(PROJECT_ID, projectId)
                        .withContext(QUOTE_OPTION_ID, quoteOptionId)
                        .withContext("view", pricingTabView));
                return responseOk(page);
            }
        }.tryToPerformAction(quoteOptionId);
    }

    /**
     * Load the new Pricing Tab.
     * This tab is a replacement for the existing Pricing Tab as part of the Pricing re-write.
     * @param customerId ID of the Customer requesting the page.
     * @param contractId ID of the Customer's contract.
     * @param projectId ID of the project.
     * @param quoteOptionId ID of the quote option.
     * @return
     */
    @GET
    @Path("/{quoteOptionId}/pricing-tab-new")
    public Response getQuoteOptionPricingTabNew(@PathParam(CUSTOMER_ID) final String customerId,
                                                @PathParam(CONTRACT_ID) final String contractId,
                                                @PathParam(PROJECT_ID) final String projectId,
                                                @PathParam(QUOTE_OPTION_ID) final String quoteOptionId) {

        LOGGER.info("Returning Pricing Tab for REST end point /{quoteOptionId}/pricing-tab-new.");
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                final PricingTabViewNew pricingTabView = pricingOrchestrator.getPricingTabViewNew(customerId, contractId, projectId, quoteOptionId);
                String page = presenter.render(view("QuoteOptionPricingTabNew.ftl")
                        .withContext(CUSTOMER_ID, customerId)
                        .withContext(CONTRACT_ID, contractId)
                        .withContext(PROJECT_ID, projectId)
                        .withContext(QUOTE_OPTION_ID, quoteOptionId)
                        .withContext("view", pricingTabView));
                return responseOk(page);
            }
        }.tryToPerformAction(quoteOptionId);
    }

    @GET
    @Path("/{quoteOptionId}/details-tab")
    public Response getQuoteOptionDetailsTab(@PathParam(CUSTOMER_ID) final String customerId,
                                             @PathParam(CONTRACT_ID) final String contractId,
                                             @PathParam(PROJECT_ID) final String projectId,
                                             @PathParam(QUOTE_OPTION_ID) final String quoteOptionId) {
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                // if this starts to cause a performance issue then we should disable the functionality until we can improve on the performance.
                if(applicationCapabilityProvider.isFunctionalityEnabled(ApplicationCapabilityProvider.Capability.EXPIRE_PRICE_LINES_ENABLED, true, Optional.of(quoteOptionId))) {
                    productInstanceClient.setExpiredPriceLines(quoteOptionId);
                }
                final QuoteOptionDetailsView view = detailsOrchestrator.buildView(customerId, contractId, projectId, quoteOptionId);
                String page = presenter.render(view("QuoteOptionDetailsTab.ftl").withContext("view", view));
                return responseOk(page);
            }
        }.tryToPerformAction(quoteOptionId);
    }

    public URI getConfiguratorBaseUrl() {
        try {
            for (UrlConfig urlConfig : config.getUrls()) {
                if ("configurator".equals(urlConfig.getContext())) {
                    return new URI(urlConfig.getUrl());
                }
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("cpe url not configured");
    }

    private boolean hasSpecialBidId(ProductInstance productInstance) {

        if (productInstance.hasInstanceCharacteristic(new AttributeName("Special Bid Id"))) {
            try {
                return (!StringUtils.isEmpty(productInstance.getInstanceCharacteristic("Special Bid Id").getStringValue()));
            } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
                instanceCharacteristicNotFound.printStackTrace();
            }
        }
        if (productInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.CPE_SPECIAL_BID_ID_ATTRIBUTE_NAME))) {
            try {
                return (!StringUtils.isEmpty(productInstance.getInstanceCharacteristic(ProductOffering.CPE_SPECIAL_BID_ID_ATTRIBUTE_NAME).getStringValue()));
            } catch (InstanceCharacteristicNotFound e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // TODO: Document this.
    // This is the end point called to retrieve the product prices displayed on the pricing tab.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{quoteOptionId}/product-prices")
    public Response getQuoteOptionPricingTabProductPrices(@PathParam(CUSTOMER_ID) final String customerId,
                                                          @PathParam(CONTRACT_ID) final String contractId,
                                                          @PathParam(PROJECT_ID) final String projectId,
                                                          @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                                          @QueryParam("iDisplayStart") final int pageStart,
                                                          @QueryParam("iDisplayLength") final int pageSize,
                                                          @QueryParam("sEcho") final int pageNumber,
                                                          @Context final UriInfo uriInfo) {
        return buildPriceLineResponse(customerId, contractId, projectId, quoteOptionId, pageStart, pageSize, pageNumber, uriInfo, PriceSuppressStrategy.UI_PRICES);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{quoteOptionId}/product-usage-charges")
    public Response getQuoteOptionPricingTabUsageCharges(@PathParam(CUSTOMER_ID) final String customerId,
                                                         @PathParam(CONTRACT_ID) final String contractId,
                                                         @PathParam(PROJECT_ID) final String projectId,
                                                         @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                                         @QueryParam("iDisplayStart") final int pageStart,
                                                         @QueryParam("iDisplayLength") final int pageSize,
                                                         @QueryParam("sEcho") final int pageNumber,
                                                         @Context final UriInfo uriInfo) {
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                final PaginatedFilter paginatedFilter = getPaginatedFilter(pageNumber, pageStart, pageSize, uriInfo, quoteOptionId);
                final QuoteOptionUsagePricingDTO  dto = pricingOrchestrator.buildUsageResponse(customerId, contractId, projectId, quoteOptionId, paginatedFilter, PriceSuppressStrategy.UI_PRICES);
                return Response.ok().entity(dto).build();
            }
        }.tryToPerformAction(quoteOptionId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{quoteOptionId}/product-cost-charges")
    public Response getQuoteOptionPricingTabCostCharges(@PathParam(CUSTOMER_ID) final String customerId,
                                                        @PathParam(CONTRACT_ID) final String contractId,
                                                        @PathParam(PROJECT_ID) final String projectId,
                                                        @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                                        @QueryParam("iDisplayStart") final int pageStart,
                                                        @QueryParam("iDisplayLength") final int pageSize,
                                                        @QueryParam("sEcho") final int pageNumber,
                                                        @Context final UriInfo uriInfo) {
        return buildPriceLineResponse(customerId, contractId, projectId, quoteOptionId, pageStart, pageSize, pageNumber, uriInfo, PriceSuppressStrategy.UI_COSTS);
    }

    private Response buildPriceLineResponse(final String customerId,
                                            final String contractId,
                                            final String projectId,
                                            final String quoteOptionId,
                                            final int pageStart,
                                            final int pageSize,
                                            final int pageNumber,
                                            final UriInfo uriInfo,
                                            final PriceSuppressStrategy priceSuppressStrategy) {
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                final PaginatedFilter paginatedFilter = getPaginatedFilter(pageNumber, pageStart, pageSize, uriInfo, quoteOptionId);
                final QuoteOptionPricingDTO dto = pricingOrchestrator.buildStandardResponse(customerId, contractId, projectId, quoteOptionId, paginatedFilter, priceSuppressStrategy);
                return Response.ok().entity(dto).build();
            }
        }.tryToPerformAction(quoteOptionId);
    }

    @GET
    @Path("/form")
    public Response newQuoteOptionForm(@PathParam(CUSTOMER_ID) String customerId,
                                       @PathParam(CONTRACT_ID) String contractId,
                                       @PathParam(PROJECT_ID) String projectId,
                                       @QueryParam(QUOTE_OPTION_ID) String quoteOptionId) {
        QuoteOptionDialogView view;
        if (StringUtils.isEmpty(quoteOptionId)) {
            view = new QuoteOptionDialogView(customerId, contractId, projectId);
        } else {
            final QuoteOptionResource quoteOptionResource = projects.quoteOptionResource(projectId);
            QuoteOptionDTO dto = quoteOptionResource.get(quoteOptionId);
            view = new QuoteOptionDialogView(customerId, contractId, projectId, dto);
        }

        String page = presenter.render(view("QuoteOptionForm.ftl")
                                           .withContext("view", view)
                                           .withContext("contractTermChangeValidationUri", String.format("/rsqe/customers/%s/contracts/%s/projects/%s/quote-options/%s/validate-contract-term-change", customerId, contractId, projectId, quoteOptionId))
        );
        return Response.ok().entity(page).build();
    }

    @GET
    @Path("{quoteOptionId}/validate-contract-term-change")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateContractTermChange(@PathParam(QUOTE_OPTION_ID) String quoteOptionId) {

        boolean validationResult = !"null".equals(quoteOptionId) && productInstanceClient.hasFirmAssets(quoteOptionId);
        return Response.ok().entity(validationResult).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveQuoteOption(@PathParam(CUSTOMER_ID) String customerId,
                                    @PathParam(PROJECT_ID) String projectId,
                                    @FormParam(QUOTE_OPTION_ID) String quoteOptionId,
                                    @FormParam(EXPEDIO_QUOTE_OPTION_ID) String expedioQuoteOptionId,
                                    @FormParam("quoteOptionName") String quoteOptionName,
                                    @FormParam("contractTerm") String contractTerm,
                                    @FormParam("currency") String currency) {
        com.bt.rsqe.expedio.project.ProjectDTO projectDTO = expedioServicesFacade.getExpedioProject(projectId);
        // (martin) The "if" statement is necessary for PUT and POST scenario. For POST scenario the friendly quote option ID won't exist so we need to load this ID from expedio database
        if (StringUtils.isEmpty(expedioQuoteOptionId)) {
            expedioQuoteOptionId = projectDTO.projectId;
        }
        final UserDTO userDTO = expedioClientResources.getUserResource().findUser(UserContextManager.getCurrent().getLoginName());
        QuoteOptionDTO quoteOption = QuoteOptionDTO.newInstance(quoteOptionId, expedioQuoteOptionId, quoteOptionName.trim(), currency, contractTerm, projectDTO.salesRepName, userDTO.ein);
        if (StringUtils.isEmpty(quoteOption.id)) {
            try {
                quoteOption.id = UUID.randomUUID().toString();
                projects.quoteOptionResource(projectId).post(quoteOption);
            } catch (BadRequestException exception) {
                return Response.status(Response.Status.BAD_REQUEST).entity(exception.errorDto().description).build();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            projects.quoteOptionResource(projectId).put(quoteOption);
        }
        return Response.ok().build();
    }

    @GET
    @Path("/notes")
    public Response noteForm(@PathParam(CUSTOMER_ID) String customerId,
                             @PathParam(CONTRACT_ID) String contractId,
                             @PathParam(PROJECT_ID) String projectId,
                             @QueryParam(QUOTE_OPTION_ID) String quoteOptionId) {

        final List<NoteDto> notes = quoteOptionNoteFacade.getNotes(projectId, quoteOptionId);
        final NotesView notesView = detailsOrchestrator.buildNoteView(notes);

        String page = presenter.render(view("../../web/custom_controls/NotesForm.ftl")
                .withContext("newNoteURI", UriFactoryImpl.quoteOptionNotes(customerId, contractId, projectId, quoteOptionId))
                .withContext("view", notesView));
        return Response.ok().entity(page).build();
    }

    @POST
    @Path("/{quoteOptionId}/notes")
    public Response createNote(@PathParam(PROJECT_ID) String projectId,
                               @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                               @FormParam("newText") String noteText) {

        quoteOptionNoteFacade.saveNote(projectId, quoteOptionId, noteText);
        return Response.ok().build();
    }

    @GET
    @Path("/{quoteOptionId}/pricing-sheet")
    @Produces({"application/vnd.ms-excel"})
    public Response getPricingSheet(
        @PathParam(CUSTOMER_ID) final String customerId,
        @PathParam(PROJECT_ID) final String projectId,
        @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
        @QueryParam(OFFER_ID) final String offerId) {
        final Workbook pricingSheet;
        if (offerId.isEmpty()) {
            pricingSheet = pricingSheetOrchestrator.renderPricingSheet(customerId, projectId, quoteOptionId, Optional.<String>absent());
        } else {
            pricingSheet = pricingSheetOrchestrator.renderPricingSheet(customerId, projectId, quoteOptionId, Optional.of(offerId));
        }
        ProjectDTO projectDTO = projects.get(projectId);
        final QuoteOptionResource quoteOptions = projects.quoteOptionResource(projectId);
        QuoteOptionDTO quoteOptionDTO = quoteOptions.get(quoteOptionId);
        String pricingSheetName = "SQE_"+projectDTO.name+"_"+quoteOptionDTO.getName()+"_"+"Pricing Sheet"+".xls";
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                pricingSheet.write(output);
            }
        }).header("Content-Disposition", "attachment; filename=" + pricingSheetName.replaceAll(" ", "")).build();

    }

    @POST
    @Path("/{quoteOptionId}/clones")
    public Response cloneQuoteOptionItems(@PathParam(PROJECT_ID) final String projectId,
                                          @PathParam(QUOTE_OPTION_ID) final String targetQuoteOptionId,
                                          @FormParam(QUOTE_OPTION_ID) final String originalQuoteOptionId,
                                          @FormParam(QUOTE_OPTION_ITEM_IDS) final String quoteOptionItemIds) {
        return cloneItems(projectId, targetQuoteOptionId, originalQuoteOptionId, quoteOptionItemIds, new LineItemCloner() {
            @Override
            public void clone(QuoteOptionResource quoteOptions, String targetQuoteOptionId, QuoteOptionItemCloneDTO dto) {
                quoteOptions.cloneItems(targetQuoteOptionId, dto);
            }
        });
    }

    @POST
    @Path("/{quoteOptionId}/ifc-line-items")
    public Response raiseIfcItems(@PathParam(PROJECT_ID) final String projectId,
                                  @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                  @FormParam(QUOTE_OPTION_ITEM_IDS) final String quoteOptionItemIds) {
        return cloneItems(projectId, quoteOptionId, quoteOptionId, quoteOptionItemIds, new LineItemCloner() {
            @Override
            public void clone(QuoteOptionResource quoteOptions, String targetQuoteOptionId, QuoteOptionItemCloneDTO dto) {
                quoteOptions.raiseIfcs(targetQuoteOptionId, dto);
            }
        });
    }

    /**
     * TODO: Complete JavaDoc
     * Returns the calculated totals for each priceLine.
     * @param customerId
     * @param contractId
     * @param projectId
     * @param quoteOptionId
     * @param suppressStrategy
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{quoteOptionId}/product-price-summary")
    public Response getPricingSummary(@PathParam(CUSTOMER_ID) String customerId,
                                      @PathParam(CONTRACT_ID) String contractId,
                                      @PathParam(PROJECT_ID) String projectId,
                                      @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                      @QueryParam("suppressStrategy") String suppressStrategy) {
        PriceSuppressStrategy priceSuppressStrategy = PriceSuppressStrategy.OFFERS_UI;
        if(!Strings.isNullOrEmpty(suppressStrategy)) {
            priceSuppressStrategy = PriceSuppressStrategy.valueOf(suppressStrategy);
        }

        final QuoteOptionPricingSummaryDTO pricingSummary = quoteOptionPricingSummaryOrchestrator.getPricingSummary(projectId,
                quoteOptionId,
                customerId,
                contractId,
                priceSuppressStrategy);
        return Response.ok().entity(pricingSummary).build();
    }

    @GET
    @Path("/{quoteOptionId}/clone-target-options")
    public Response getCloneTargetQuoteOptionsList(@PathParam(PROJECT_ID) String projectId,
                                                   @PathParam(QUOTE_OPTION_ID) String quoteOptionId) {


        final List<QuoteOptionDTO> quoteOptionDTOs = projects.quoteOptionResource(projectId).getCloneCandidateFor(quoteOptionId);

        final CloneTargetOptionsListView targetListView = new CloneTargetOptionsListView(quoteOptionDTOs);

        String selectView = presenter.render(view("CopyTargetOptionsList.ftl")
                .withContext("view", targetListView));

        return Response.ok().entity(selectView).build();
    }

    @POST
    @Path("/bulk-upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response bulkUpload(@FormDataParam("product") String productCode,
                               FormDataMultiPart multiPartFormData) {
        AjaxResponseDTO bulkUploadResponse = bulkUploadOrchestrator.upload(productCode, multiPartFormData);
        return Response.ok().entity(jsonSerializer.serialize(bulkUploadResponse)).build();
    }

    @GET
    @Path("/{quoteOptionId}/revenue")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRevenueDetails(@PathParam("customerId") final String customerId,
                                      @PathParam("contractId") final String contractId,
                                      @PathParam("projectId") final String projectId,
                                      @PathParam("quoteOptionId") final String quoteOptionId,
                                      @QueryParam("iDisplayStart") final int pageStart,
                                      @QueryParam("iDisplayLength") final int pageSize,
                                      @QueryParam("sEcho") final int pageNumber) {
        return new HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                Pagination pagination = new DefaultPagination(pageNumber, pageStart, pageSize);
                QuoteOptionRevenueDTO revenueDTO = revenueOrchestrator.getRevenueFor(customerId, contractId, projectId, quoteOptionId, pagination);
                return Response.ok().entity(revenueDTO).build();
            }
        }.tryToPerformAction(quoteOptionId);
    }

    @POST
    @Path("/delete/{quoteOptionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQuoteOption(@PathParam(PROJECT_ID) String projectId, @PathParam(QUOTE_OPTION_ID) String quoteOptionId) {
        QuoteOptionResource quoteOptionResource = projects.quoteOptionResource(projectId);
        QuoteOptionDTO quoteOptionDTO = quoteOptionResource.get(quoteOptionId);
        quoteOptionDTO.setDeleteStatus(true);
        quoteOptionResource.put(quoteOptionDTO);
        return Response.ok().build();
    }

    /**
     * @deprecated we are now creating this cookie from the front end.
     * If that works correctly on live then we can go ahead and remove
     * this once and for all.
     */
    @POST
    @Path("/filter-line-items")
    @Produces(MediaType.APPLICATION_JSON)
    public Response filterLineItems(String lineItemFilter) {
        return ResponseBuilder.anOKResponse().withCookie(JsonCookie.fromCookie(lineItemFilter, LineItemFilterCookie.class).toCookie()).build();
    }

    private Response cloneItems(String projectId, String targetQuoteOptionId, String sourceQuoteOptionId, String quoteOptionItemIds, LineItemCloner cloner) {
        try {
            if (projectId == null || targetQuoteOptionId == null) {
                return Response.status(BAD_REQUEST).build();
            }

            if (quoteOptionItemIds != null) {
                QuoteMigrationFlagManager.newMigrationFlagDelegate(projects).prepareQuoteOptionsForItemCopy(projectId, sourceQuoteOptionId, targetQuoteOptionId);
                final QuoteOptionItemCloneDTO dto = QuoteOptionItemCloneDTO.newInstance(sourceQuoteOptionId, quoteOptionItemIds.split(","));
                final QuoteOptionResource quoteOptions = projects.quoteOptionResource(projectId);
                cloner.clone(quoteOptions, targetQuoteOptionId, dto);
            }
            return Response.ok().build();
        } catch (BadRequestException exception) {
            return Response.status(Response.Status.BAD_REQUEST).entity(exception.errorDto().description).build();
        } catch (QuoteMigrationFlagValidationException e) {
            return ResponseBuilder.badRequest().withEntity(e.getMessage()).build();
        }
    }

    private String ordersTabUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return UriFactoryImpl.orders(customerId, contractId, projectId, quoteOptionId).toString();
    }

    private String offersTabUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return UriFactoryImpl.offers(customerId, contractId, projectId, quoteOptionId).toString();
    }

    private String quoteOptionPricingTabUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return UriFactoryImpl.quoteOptionPricingTab(customerId, contractId, projectId, quoteOptionId).toString();
    }

    private String quoteOptionPricingTabUriNew(String customerId, String contractId, String projectId, String quoteOptionId) {
        return UriFactoryImpl.quoteOptionPricingTabNew(customerId, contractId, projectId, quoteOptionId).toString();
    }

    private String quoteOptionDetailsTabUri(String customerId, String contractId, String projectId, String quoteOptionId) {
        return UriFactoryImpl.quoteOptionDetailsTab(customerId, contractId, projectId, quoteOptionId).toString();
    }

    private void sendQuoteDetailsToExpedio(String projectId, String quoteOptionId, com.bt.rsqe.expedio.project.ProjectDTO projectDTO) throws ParseException {
        QuoteOptionDTO quoteOptionDTO = projects.quoteOptionResource(projectId).get(quoteOptionId);
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DateFormats.TIMESTAMP_6_DATE_FORMAT);
        org.joda.time.DateTime creationDateTime = null;
        try {
            creationDateTime = dateTimeFormatter.parseDateTime(quoteOptionDTO.creationDate);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        final UserDTO userDTO = expedioClientResources.getUserResource().findUser(UserContextManager.getCurrent().getLoginName());
        projectDTO.setCurrency(quoteOptionDTO.getCurrency());
        projectDTO.setContractTerm(quoteOptionDTO.contractTerm);
        projectDTO.setModifiedDate(creationDateTime.toDate());
        projectDTO.setQuoteOptionName(quoteOptionDTO.getName());
        projectDTO.setQuoteOptionExpiryDate(null);
        projectDTO.setCreatedDate(creationDateTime.toDate());
        projectDTO.setQuoteIndicativeFlag(QuoteOptionStatus.FIRM.getValue());
        projectDTO.setSalesRepName(userDTO.getSalesRepName());
        expedioServicesFacade.postExpedioProject(projectId, projectDTO);
    }

    private interface LineItemCloner {
        void clone(QuoteOptionResource quoteOptions, String targetQuoteOptionId, QuoteOptionItemCloneDTO dto);
    }

    /**
     * Determines which Pricing filter should be added to the Pricing tab and then adds it.
     * This decision is based upon the value of the 'useNewPricingTab' flag in the Inlife database's APPLICATION_PROPERTY_STORE table.
     * @param pageNumber The current page number.
     * @param pageStart The start of the pages to return.
     * @param pageSize The number of pages to return, starting from pageStart.
     * @param uriInfo An object holding URI information from a REST call. This contains the values we will filter on.
     * @param quoteOptionId Required to switch the functionality between the new and old pricing filter.
     * @return The paginated filter for the Pricing Tab. Either the new or the old filter. Based on the value set in the Inlife database.
     */
    private PaginatedFilter getPaginatedFilter(int pageNumber, int pageStart, int pageSize, UriInfo uriInfo, String quoteOptionId)
    {
        LOGGER.info("Retrieving pricing filter.");
        LOGGER.debug("Quote Option ID = {}, Page Number = {}, Page Start = {}, Page Size = {}, URL Information = {}",
                      quoteOptionId, pageNumber, pageStart, pageSize, uriInfo);

        final DefaultPagination pagination = new DefaultPagination(pageNumber, pageStart, pageSize);
        final FilterValues filterValues = DataTableFilterValues.parse(uriInfo.getQueryParameters(true));

        LOGGER.info("Determining which pricing filter (new or old) to add to the Quote Options Page.");
        if (applicationCapabilityProvider.isFunctionalityEnabled(Capability.USE_NEW_PRICING_TAB, false, Optional.of(quoteOptionId)))
        {
            final PaginatedFilter paginatedFilter = new PaginatedPricingTabViewFilterNew(filterValues, pagination);
            LOGGER.info("Added new Pricing filter to the Quote Options Page.");
            return paginatedFilter;
        }
        else
        {
            final PaginatedFilter paginatedFilter = new PaginatedPricingTabViewFilter(filterValues, pagination);
            LOGGER.info("Added old Pricing filter to the Quote Options Page.");
            return paginatedFilter;
        }
    }

    @GET
    @Path("/viewConfiguration")
    public Response viewConfigurationForm(@PathParam(CUSTOMER_ID) String customerId,
                                          @PathParam(CONTRACT_ID) String contractId,
                                          @PathParam(PROJECT_ID) String projectId,
                                          @CookieParam(Credentials.RSQE_TOKEN) String userToken,
                                          @QueryParam(QUOTE_OPTION_ID) String quoteOptionId) {

        /**
         * Initialise the view.
         */
        ViewConfigurationDialogView view;
        view = new ViewConfigurationDialogView(customerId, contractId, projectId);

        QuoteOptionResource quoteOptionResource = projects.quoteOptionResource(projectId);
        ProjectDTO quoteDTO = projects.get(projectId);
        view.setQuoteName(quoteDTO.getName());

        ExpedioRsqeCredentials credentials = new ExpedioRsqeCredentials(userToken);
        UserContext userContext =  expedioSessionResource.get(credentials);
        view.setExpRef(userContext.getExpedioReference()== null ? "" : userContext.getExpedioReference());

        /**
         * Populate all QuoteOptions from QuoteOptionDTO
         */
        List<QuoteOption> quoteOptions = new ArrayList<>();
        QuoteOption quoteOption;

        if(Strings.isNullOrEmpty(quoteOptionId)){
            for (QuoteOptionDTO quoteOptionsDTO : quoteOptionResource.get()) {
                quoteOption = new QuoteOption(quoteOptionsDTO.getId(), quoteOptionsDTO.getName());
                quoteOptions.add(quoteOption);
            }
        }
        else {
            QuoteOptionDTO quoteOptionDTO = quoteOptionResource.get(quoteOptionId);
            quoteOption = new QuoteOption(quoteOptionDTO.getId(), quoteOptionDTO.getName());
            quoteOptions.add(quoteOption);
        }

        //Set the quote option List
        view.setQuoteOptions(quoteOptions);

        /**
         * Populate all Offers from OfferDTO
         */
        List<Offer> offersList = new ArrayList<>();
        Offer offer;
        for(OfferDTO offerDTO : projects.getOffersByProject(projectId)){
            offer = new Offer(offerDTO.getId(), offerDTO.getName(), offerDTO.getQuoteOptionId());
            offersList.add(offer);
        }

        //Set the Offer List.
        view.setOffers(offersList);

        /**
         * Populate all Offers from OfferDTO
         */
        List<Order> ordersList = new ArrayList<>();
        Order order;
        for(OrderDTO orderDTO : projects.getOrdersByProject(projectId)){
            order = new Order(orderDTO.id, orderDTO.getName(), orderDTO.getOfferId());
            ordersList.add(order);
        }

        //Set the Offer List.
        view.setOrders(ordersList);

        String page = presenter.render(view("ViewConfigurationForm.ftl")
                .withContext("view", view));
        return Response.ok().entity(page).build();
    }

}