package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.expedio.services.quote.QuoteCreationDTO;
import com.bt.rsqe.expedio.services.quote.QuoteResource;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import static com.bt.rsqe.inlife.client.ApplicationCapabilityProvider.Capability.*;


import com.bt.rsqe.inlife.client.ApplicationPropertyResourceClient;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectIdDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionContractTerm;
import com.bt.rsqe.projectengine.QuoteOptionCurrency;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.projectengine.web.facades.ExpedioServicesFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.model.ViewConfigurationJsonObject;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionOrchestrator;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.CustomerProjectQuoteOptionsTab;
import com.bt.rsqe.projectengine.web.view.PageView;
import com.bt.rsqe.projectengine.web.view.ViewConfigurationTreeBuilder;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import com.bt.rsqe.web.mappers.ProductCreationJsonObject;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.google.common.base.Optional;
import com.google.gson.JsonObject;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/rsqe/customers/{customerId}")
public class CustomerProjectResourceHandler extends ViewFocusedResourceHandler {

    private static final String CUSTOMER_ID = "customerId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String OFFER_ID = "offerId";
    private static final String ORDER_ID = "orderId";
    private static final String SITES_BY_PRODUCT_TREE = "sitesByProductTree";
    private static final String PRODUCTS_BY_SITE_TREE = "productsBySiteTree";

    private ProjectResource projectResource;
    private CustomerProjectResourceHandlerConfig config;
    private final QuoteOptionOrchestrator quoteOptionOrchestrator;
    private ExpedioServicesFacade expedioServicesFacade;
    private CustomerResource customerResource;
    private final String webMetricsUrl;
    private final String helpLinkUri;
    private ApplicationCapabilityProvider applicationCapabilityProvider;
    private ApplicationPropertyResourceClient applicationPropertyResourceClient;
    private PmrClient pmrClient;
    private QuoteOptionFacade quoteOptionFacade;
    private ProductInstanceClient productInstanceClient;
    private QuoteResource quoteResource;

    private static Logger LOG = LoggerFactory.getLogger(CustomerProjectResourceHandler.class);

    public CustomerProjectResourceHandler(Presenter presenter,
                                          ProjectResource projectResource,
                                          QuoteOptionOrchestrator quoteOptionOrchestrator,
                                          ProjectEngineWebConfig projectEngineWebConfiguration,
                                          ExpedioServicesFacade expedioServicesFacade,
                                          CustomerResource customerResource,
                                          ApplicationPropertyResourceClient applicationPropertyResourceClient,
                                          PmrClient pmrClient, QuoteOptionFacade quoteOptionFacade, ProductInstanceClient productInstanceClient,
                                          QuoteResource quoteResource) {
        super(presenter);
        this.projectResource = projectResource;
        this.quoteOptionOrchestrator = quoteOptionOrchestrator;
        this.expedioServicesFacade = expedioServicesFacade;
        this.config = projectEngineWebConfiguration.getCustomerProjectResourceHandlerConfig();
        this.webMetricsUrl = projectEngineWebConfiguration.getUrl(ProjectEngineWebConfig.SUBMIT_WEB_METRICS_URI).getUrl();
        this.customerResource = customerResource;
        this.helpLinkUri = projectEngineWebConfiguration.getUrl(ProjectEngineWebConfig.HELP_LINK_URI).getUrl();
        this.applicationPropertyResourceClient = applicationPropertyResourceClient;
        this.applicationCapabilityProvider =new ApplicationCapabilityProvider(applicationPropertyResourceClient);
        this.pmrClient = pmrClient;
        this.quoteOptionFacade = quoteOptionFacade;
        this.productInstanceClient = productInstanceClient;
        this.quoteResource = quoteResource;
    }

    private boolean getAllowQuoteCreation()
    {
        return applicationCapabilityProvider.isFunctionalityEnabled(ALLOW_QUOTE_CREATION, false, Optional.<String>absent());
    }

    @GET
    @Path("/contracts/{contractId}/projects/{projectId}")
    @Produces(MediaType.TEXT_HTML)
    public Response getProject(@PathParam(CUSTOMER_ID) String customerId,
                               @PathParam(PROJECT_ID) String projectId) {

        ProjectDTO projectDTO = null;
        CustomerDTO customerDTO = null;

        try {
            projectDTO = projectResource.get(projectId);
            UserContext userContext = UserContextManager.getCurrent();
            customerDTO = customerResource.getByToken(customerId, userContext.getRsqeToken());
        } catch (ResourceNotFoundException e) {
            createNewProject(projectId, customerId);
            projectDTO = projectResource.get(projectId);
        }

        final PageView view = new PageView("Customer Project", "Quote Options")
            .addTab("ProjectQuoteOptions", "Quote options", quoteOptionsTabUri(customerId, projectDTO.contractId, projectId));

        String page = presenter.render(view("BasePage.ftl")
                                           .withContext("view", view)
                                           .withContext("customerDetails", customerDTO)
                                           .withContext("submitWebMetricsUri", webMetricsUrl)
                                           .withContext("viewConfigurationDialogUri", UriFactoryImpl.viewConfigurationDialog(customerId, projectDTO.contractId, projectId).toString())
                                           .withContext("helpLinkUri", helpLinkUri));
        return responseOk(page);

    }

    //TODO: Leela to remove this method once bundling app deploy rsqe landing page URL change
    ///CLOVER:OFF
    @GET
    @Path("/projects/{projectId}")
    @Produces(MediaType.TEXT_HTML)
    public Response getProjectWithoutContractId(@PathParam(CUSTOMER_ID) String customerId,
                               @PathParam(PROJECT_ID) String projectId) {
       return getProject(customerId, projectId);
    }
    ///CLOVER:ON

    private String quoteOptionsTabUri(String customerId, String contractId, String projectId) {
        return UriFactoryImpl.quoteOptionsTab(customerId, contractId, projectId).toString();
    }

    @GET
    @Path("/contracts/{contractId}/projects/{projectId}/quote-options-tab")
    @Produces(MediaType.TEXT_HTML)
    public Response getQuoteOptionsTab(@PathParam(CUSTOMER_ID) String customerId,
                                       @PathParam(PROJECT_ID) String projectId) {
        ProjectDTO projectDTO = projectResource.get(projectId);
        ProjectIdDTO projectIdDTO = projectResource.getProjectForCustomer(customerId);
        CustomerProjectQuoteOptionsTab view = quoteOptionOrchestrator.buildResponse(customerId, projectDTO.contractId, projectId, projectIdDTO);
        String page = presenter.render(view("ProjectQuoteOptionsTab.ftl")
                                           .withContext("view", view).withContext("allowQuoteCreation", String.valueOf(getAllowQuoteCreation())));
        return responseOk(page);

    }

    //TODO: Leela to remove this method once bundling app deploy rsqe landing page URL change
    ///CLOVER:OFF
    @GET
    @Path("/projects/{projectId}/quote-options-tab")
    @Produces(MediaType.TEXT_HTML)
    public Response getQuoteOptionsTabWithoutContractId(@PathParam(CUSTOMER_ID) String customerId,
                                       @PathParam(PROJECT_ID) String projectId) {
        return getQuoteOptionsTab(customerId, projectId);
    }
    ///CLOVER:ON

    private void createNewProject(String projectId,
                                  String customerId) {
        com.bt.rsqe.expedio.project.ProjectDTO expedioProject = expedioServicesFacade.getExpedioProject(projectId);
        projectResource.put(projectId, expedioProject.quoteName, customerId, expedioProject.contractId);
        projectResource.quoteOptionResource(projectId).post(expedioProject.projectId,
                expedioProject.quoteName,
                expedioProject.currency,
                expedioProject.contractTerm,
                expedioProject.salesRepName);
    }

    public interface CustomerProjectResourceHandlerConfig {
        QuoteOptionCurrency getDefaultCurrency();

        QuoteOptionContractTerm getDefaultContractTerm();

        String getDefaultQuoteOptionName();

        String getDefaultCreatedBy();
    }

    @POST
    @Path("/contracts/{contractId}/projects/{projectId}/buildConfigurationTree")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductsBySiteTree(@PathParam(CUSTOMER_ID) String customerId, @PathParam(PROJECT_ID) String projectId, String json) {

        ViewConfigurationJsonObject input = JSONSerializer.getInstance().deSerialize(json, ViewConfigurationJsonObject.class);

        Map<String, String> params = new HashMap<>();
        params.put(CUSTOMER_ID, customerId);
        params.put(PROJECT_ID, projectId);
        params.put(QUOTE_OPTION_ID, input.getQuoteOptionId());
        params.put(OFFER_ID, input.getOfferId());
        params.put(ORDER_ID, input.getOrderId());

        ViewConfigurationTreeBuilder viewConfigurationTreeBuilder = new ViewConfigurationTreeBuilder(quoteOptionFacade, customerResource, productInstanceClient, pmrClient, params);
        JsonObject jsonObject = null;
        if(input.getTreeViewType().equals(PRODUCTS_BY_SITE_TREE)){
            jsonObject = viewConfigurationTreeBuilder.buildProductsBySiteTree();
        }
        else if(input.getTreeViewType().equals(SITES_BY_PRODUCT_TREE)){
            jsonObject = viewConfigurationTreeBuilder.buildSitesByProductTree();
        }
        return Response.ok(jsonObject.toString()).build();
    }

    @POST
    @Path("/createQuoteAndDefaultQuoteOption")
    public Response createQuoteAndDefaultQuoteOption(QuoteCreationDTO quoteCreationDTO){
        try {
            String projectId = quoteCreationDTO.getQuoteId();
            LOG.info("Inside CRPH.createQuoteAndDefaultQuoteOption() -  QuoteCreationDTO => " + (new ObjectMapper()).writeValueAsString(quoteCreationDTO));
            /**
             *  If quote ID is not present, create one
             */
            if (projectId == null) {
                LOG.info("Quote Id is null... Creating a New Quote..");
                //Get quote ID
                projectId = getQuoteId(quoteResource.createQuote(quoteCreationDTO));
                LOG.info("New Quote created with id "+projectId);
                quoteCreationDTO.setQuoteId(projectId);
            }

            //Call postQuoteOption
            LOG.info("Requesting QORH to create default quote option");
            projectResource.quoteOptionResource(projectId).post(quoteCreationDTO);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
        return Response.ok().build();
    }

    private String getQuoteId(String guID) {
        String GUID;
        try{
            GUID = guID.substring(guID.length()-15, guID.length());
        }
        catch (Exception e) {
            GUID = "1";
        }
        return GUID;
    }
}
