package com.bt.rsqe.projectengine.server;

import com.bt.rsqe.ComponentNames;
import com.bt.rsqe.cleanordervalidation.CleanOrderValidationResourceClient;
import com.bt.rsqe.client.QuoteOptionClient;
import com.bt.rsqe.config.CookieConfig;
import com.bt.rsqe.configuration.FileExtensionsConfig;
import com.bt.rsqe.configuration.SharePointUrlConfig;
import com.bt.rsqe.configuration.SqeAppUrlConfig;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ioc.DefaultResourceHandlerFactory;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.customerinventory.client.CustomerInventoryClientConfig;
import com.bt.rsqe.customerinventory.client.CustomerInventoryClientManager;
import com.bt.rsqe.customerinventory.client.CustomerInventoryClientManagerFactory;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.client.SpecialPriceBookClient;
import com.bt.rsqe.customerinventory.client.resource.ContractResourceClient;
import com.bt.rsqe.customerinventory.client.resource.ProductAgreementResourceClient;
import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.customerinventory.driver.CustomerInventoryDriverManager;
import com.bt.rsqe.customerinventory.driver.CustomerInventoryDriverManagerFactory;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.ExpedioServicesResource;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.domain.product.AttributeAssociationProcessor;
import com.bt.rsqe.domain.product.ContributesToCharacteristicUpdater;
import com.bt.rsqe.domain.product.RuleAwareInstanceCharacteristicUpdater;
import com.bt.rsqe.domain.project.CreatableRelationshipInstanceFilter;
import com.bt.rsqe.domain.project.ExpedioCountryResolver;
import com.bt.rsqe.domain.project.ProductInstanceBuilder;
import com.bt.rsqe.domain.project.ProductInstanceFactory;
import com.bt.rsqe.emppal.attachmentresource.AttachmentManager;
import com.bt.rsqe.emppal.attachmentresource.AttachmentUtil;
import com.bt.rsqe.emppal.attachmentresource.EmpPalClientResources;
import com.bt.rsqe.error.web.WebAuthenticationExceptionMapper;
import com.bt.rsqe.error.web.WebExceptionMapper;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.expedio.services.quote.QuoteResource;
import com.bt.rsqe.factory.RestRequestBuilderFactory;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.inlife.client.ApplicationPropertyResourceClient;
import com.bt.rsqe.inlife.client.RequestResponseResourceClient;
import com.bt.rsqe.logging.LoggingHandler;
import com.bt.rsqe.monitoring.AggregateMonitoringHealthProvider;
import com.bt.rsqe.monitoring.AlwaysGreenMonitoringHealthProvider;
import com.bt.rsqe.monitoring.DefaultMonitoringInfoProvider;
import com.bt.rsqe.monitoring.MonitoringStatisticsHandler;
import com.bt.rsqe.monitoring.StatisticsCollector;
import com.bt.rsqe.monitoring.WebMetricsResourceHandler;
import com.bt.rsqe.pc.client.ConfiguratorClient;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrClientSingleton;
import com.bt.rsqe.pmr.client.PmrLookupClient;
import com.bt.rsqe.pricing.AutoPriceAggregator;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.PricingClientManager;
import com.bt.rsqe.pricing.PricingFacadeService;
import com.bt.rsqe.pricing.PricingStatusNADecider;
import com.bt.rsqe.pricing.PricingStrategyDecider;
import com.bt.rsqe.projectengine.CaveatResource;
import com.bt.rsqe.projectengine.LineItemNoteResource;
import com.bt.rsqe.projectengine.ProjectEngineClientResources;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionHelper;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.projectengine.web.AttachmentDialogResourceHandler;
import com.bt.rsqe.projectengine.web.BreadCrumbFactory;
import com.bt.rsqe.projectengine.web.BulkTemplateExportResourceHandler;
import com.bt.rsqe.projectengine.web.ContractDialogResourceHandler;
import com.bt.rsqe.projectengine.web.CustomerProjectResourceHandler;
import com.bt.rsqe.projectengine.web.InVisibleCreatableLineItemRetriever;
import com.bt.rsqe.projectengine.web.LineItemResourceHandler;
import com.bt.rsqe.projectengine.web.OfferAndOrderValidator;
import com.bt.rsqe.projectengine.web.OfferResourceHandler;
import com.bt.rsqe.projectengine.web.OrderRFOResourceHandler;
import com.bt.rsqe.projectengine.web.OrderResourceHandler;
import com.bt.rsqe.projectengine.web.PriceHandler;
import com.bt.rsqe.projectengine.web.PriceHandlerProcessor;
import com.bt.rsqe.projectengine.web.PriceHandlerService;
import com.bt.rsqe.projectengine.web.PricingTaskFactory;
import com.bt.rsqe.projectengine.web.ProductActionResourceHandler;
import com.bt.rsqe.projectengine.web.ProductAgreementResourceHandler;
import com.bt.rsqe.projectengine.web.QuoteOptionBcmResourceHandler;
import com.bt.rsqe.projectengine.web.QuoteOptionDialogsResourceHandler;
import com.bt.rsqe.projectengine.web.QuoteOptionResourceHandler;
import com.bt.rsqe.projectengine.web.ValidationResourceHandler;
import com.bt.rsqe.projectengine.web.facades.BfgContactsFacade;
import com.bt.rsqe.projectengine.web.facades.BidManagerCommentsFacade;
import com.bt.rsqe.projectengine.web.facades.BulkUploadFacade;
import com.bt.rsqe.projectengine.web.facades.CustomerFacade;
import com.bt.rsqe.projectengine.web.facades.ExpedioServicesFacade;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.FutureProductInstanceFacade;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.LineItemNotesFacade;
import com.bt.rsqe.projectengine.web.facades.PriceBookFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionNoteFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOfferFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOrderFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.facades.SpecialPriceBookFacade;
import com.bt.rsqe.projectengine.web.facades.UserFacade;
import com.bt.rsqe.projectengine.web.model.DiscountUpdater;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitorFactory;
import com.bt.rsqe.projectengine.web.model.modelfactory.FutureAssetPricesModelFactory;
import com.bt.rsqe.projectengine.web.model.modelfactory.FutureAssetPricesModelFactoryImpl;
import com.bt.rsqe.projectengine.web.model.modelfactory.LineItemModelFactory;
import com.bt.rsqe.projectengine.web.model.modelfactory.LineItemModelFactoryImpl;
import com.bt.rsqe.projectengine.web.model.modelfactory.OfferDetailsModelFactory;
import com.bt.rsqe.projectengine.web.model.modelfactory.OrderModelFactory;
import com.bt.rsqe.projectengine.web.model.modelfactory.ProjectedUsageModelFactory;
import com.bt.rsqe.projectengine.web.productconfigurator.BulkConfigDetailModelBuilder;
import com.bt.rsqe.projectengine.web.productconfigurator.BulkConfigSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmExportBidInfoSheetFactory;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmExportChannelInformationSheetFactory;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmExportPricingSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmExportProductLevelInfoSheetFactory;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmExportProductPerSiteSheetFactory;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmExportSiteDetailsSheetFactory;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmExportSpecialBidSheetFactory;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmExportSpecialPriceBookSheetFactory;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmExportUsageSheetFactory;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmPricingSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmSheetExportOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmSheetImportOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBulkUploadOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionPricingSummaryOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMBidInfoFactory;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMDataRowModelFactory;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMDiscountUpdater;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMExportOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMInformerFactory;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMPriceLineInfoFactory;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMProductInstanceInfoFactory;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMProductPerSiteFactory;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMProductSheetGenerator;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMSheetFactory;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMSheetGenerator;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMSiteDetailsFactory;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.HeaderRowModelFactory;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.ProductsBCMSheetFactory;
import com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet.BulkTemplateExportSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.lineitems.LineItemNoteInteractor;
import com.bt.rsqe.projectengine.web.quoteoption.lineitems.LineItemNoteResourceHandler;
import com.bt.rsqe.projectengine.web.quoteoption.lineitems.LineItemNoteViewRenderer;
import com.bt.rsqe.projectengine.web.quoteoption.priceupdater.FutureAssetPriceUpdaterFactory;
import com.bt.rsqe.projectengine.web.quoteoption.validation.QuoteOptionBillingAccountValidatorRule;
import com.bt.rsqe.projectengine.web.quoteoption.validation.QuoteOptionDependency;
import com.bt.rsqe.projectengine.web.quoteoption.validation.QuoteOptionDependencyValidator;
import com.bt.rsqe.projectengine.web.quoteoption.validation.SiteValidator;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.ProductAgreementOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.ProductOrchestratorFactory;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.QuoteOptionDetailsOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionoffers.OfferDetailsOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionoffers.QuoteOptionOffersOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionorders.QuoteOptionOrdersOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet.CardinalityValidator;
import com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet.ECRFSheetModelBuilder;
import com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet.ECRFSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet.LineItemBasedImporter;
import com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet.ProductBasedImporter;
import com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet.ProductRelationshipService;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.OrderRFOSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.RFOSheetModelBuilder;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.RFOUpdater;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.DiscountHandler;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingActionsHandler;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.QuoteOptionPricingOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.QuoteOptionRevenueOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModelFactory;
import com.bt.rsqe.projectengine.web.security.CustomerAuthorizationFilter;
import com.bt.rsqe.projectengine.web.security.MethodNotAllowedExceptionMapper;
import com.bt.rsqe.projectengine.web.security.RestOrWebAuthenticationRequestFilter;
import com.bt.rsqe.projectengine.web.security.WebAuthenticationRequestFilter;
import com.bt.rsqe.projectengine.web.security.WebAuthenticationResponseFilter;
import com.bt.rsqe.projectengine.web.tpe.TpeStatusManager;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.userImport.UserImportResourceHandler;
import com.bt.rsqe.projectengine.web.validators.OfferCreationValidator;
import com.bt.rsqe.projectengine.web.validators.OrderCreationValidator;
import com.bt.rsqe.security.ExpedioUserContextResolver;
import com.bt.rsqe.security.RestAuthenticationFilterConfig;
import com.bt.rsqe.security.RestAuthenticationRequestFilter;
import com.bt.rsqe.security.UserContextProvider;
import com.bt.rsqe.security.WebAuthenticationUserContextResolver;
import com.bt.rsqe.session.client.SessionServiceClientResources;
import com.bt.rsqe.tpe.client.PricingTpeClient;
import com.bt.rsqe.utils.DateProvider;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.utils.countries.Countries;
import com.bt.rsqe.web.FaviconHandler;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import com.bt.rsqe.web.rest.WebResponseCodeConversionFilter;
import com.bt.rsqe.web.staticresources.StaticResourceLoaderFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.bt.rsqe.factory.ServiceLocator.*;


public class ProjectEngineApplication extends Application {

    private ProjectEngineWebConfig configuration;
    private Object[] resourceHandlers;
    private StatisticsCollector statsCollector;
    private ProductInstanceClient productInstanceClient;
    private ProductInstanceClient futureProductInstanceClient;
    private ExpedioUserContextResolver expedioUserContextResolver;
    private UserResource expedioUserResource;
    private SessionServiceClientResources sessionServiceClientResources;
    private CustomerResource customerResource;
    private CaveatResource caveatResource;
    private ApplicationPropertyResourceClient applicationPropertyResourceClient;
    private ApplicationCapabilityProvider capabilityProvider;


    public ProjectEngineApplication(ProjectEngineWebConfig configuration, SessionServiceClientResources sessionServiceClientResources,
                                    CookieConfig cookieConfig, ViewFocusedResourceHandler... resourceHandlers) {
        super(configuration.getApplicationConfig(), ComponentNames.PEW);
        this.configuration = configuration;
        this.resourceHandlers = resourceHandlers;
        statsCollector = new StatisticsCollector(ComponentNames.PEW, configuration.getStatsMode());
        this.sessionServiceClientResources = sessionServiceClientResources;
        this.applicationPropertyResourceClient = new ApplicationPropertyResourceClient(configuration.getInlifeClientConfig());
        this.capabilityProvider = new ApplicationCapabilityProvider(applicationPropertyResourceClient);
        registerFilters(sessionServiceClientResources, cookieConfig);
    }

    @Override
    protected ResourceHandlerFactory createResourceHandlerFactory() {
        return new DefaultResourceHandlerFactory() {
            {
                ProjectEngineClientResources projectEngineClientResources = new ProjectEngineClientResources(configuration.getProjectEngineClientConfig());
                ProjectResource projects = projectEngineClientResources.projectResource();
                RestRequestBuilderFactory restRequestBuilderFactory = new RestRequestBuilderFactory();

                final String submitWebMetricsUri = configuration.getUrl(ProjectEngineWebConfig.SUBMIT_WEB_METRICS_URI).getUrl();
                final String helpLinkUri = configuration.getUrl(ProjectEngineWebConfig.HELP_LINK_URI).getUrl();
                final PmrClient pmr = PmrClientSingleton.getPmrClient(configuration.getPmrClientConfig());
                final EmpPalClientResources empPalClientResources = new EmpPalClientResources(configuration.getEmpPalFacadeClientConfig());
                final CustomerInventoryClientConfig cifConfig = configuration.getCustomerInventoryConfig();
                final CustomerInventoryDriverManager driverManager = CustomerInventoryDriverManagerFactory.getDriverManager(cifConfig);
                // For handling Site related operations
                ContractResourceClient contractResource = new ContractResourceClient(configuration.getCustomerInventoryClient(),restRequestBuilderFactory);
                ExpedioClientResources expedioClientResources = new ExpedioClientResources(configuration.getExpedioFacadeConfig());
                customerResource = expedioClientResources.getCustomerResource();
                caveatResource = projectEngineClientResources.caveatResource();
                Countries countries = new Countries();
                ExpedioCountryResolver countryResolver = new ExpedioCountryResolver(expedioClientResources, countries);
                final CustomerInventoryClientManager clientManager = CustomerInventoryClientManagerFactory.getClientManager(cifConfig, pmr, countryResolver);
                final ProductInstanceBuilder productBuilder = ProductInstanceFactory.getProductInstanceFactory(pmr, clientManager, countryResolver);

                BfgContactsFacade bfgContactsFacade = new BfgContactsFacade(expedioClientResources, pmr, driverManager);
                serviceLocatorInstance().registerIfNotAlreadyRegistered(bfgContactsFacade);

                final CustomerResource customers = expedioClientResources.getCustomerResource();
                final UserResource users = expedioClientResources.getUserResource();
                final ExpedioServicesResource expedioServices = expedioClientResources.getExpedioServicesResource();
                final ExpedioProjectResource expedioProjectsResource = expedioClientResources.projectResource();
                final ProductAgreementResourceClient productAgreementResourceClient =new ProductAgreementResourceClient(configuration.getCustomerInventoryClient(),restRequestBuilderFactory);
                final PmrLookupClient pmrLookupClient = new PmrLookupClient(configuration.getPmrClientConfig());
                final ProductIdentifierFacade productIdentifierFacade = new ProductIdentifierFacade(pmr, productAgreementResourceClient, pmrLookupClient);
                final SiteFacade siteFacade = new SiteFacade(customers);


                final QuoteOptionFacade quoteOptionFacade = new QuoteOptionFacade(projects);
                final CustomerFacade customerFacade = new CustomerFacade(customers);
                final UserFacade userFacade = new UserFacade(users);
                final ExpedioServicesFacade expedioServicesFacade = new ExpedioServicesFacade(expedioServices, expedioProjectsResource, userFacade);
                final QuoteOptionNoteFacade quoteOptionNoteFacade = new QuoteOptionNoteFacade(projects);
                final BidManagerCommentsFacade bidManagerCommentsFacade = new BidManagerCommentsFacade(projects, userFacade);
                final PriceBookFacade priceBookFacade = new PriceBookFacade(customers, pmr);
                final BreadCrumbFactory breadCrumbFactory = BreadCrumbFactory.getInstance(projects);
                final SpecialPriceBookClient specialPriceBookClient = clientManager.getSpecialPriceBookClient();
                final ProjectedUsageModelFactory projectedUsageModelFactory = new ProjectedUsageModelFactory(new SpecialPriceBookFacade(specialPriceBookClient));

                productInstanceClient = CustomerInventoryClientManagerFactory.getClientManager(configuration.getCustomerInventoryClient(), configuration.getPmrClientConfig())
                                                                             .getProductInstanceClient();

                final QuoteMigrationDetailsProvider migrationDetailsProvider = new QuoteMigrationDetailsProvider(pmr, projects);

                final PricingClient pricingClient = new PricingClient(configuration.getPricingFacadeClientConfig(), pmr);
                final FutureAssetPricesModelFactory futureAssetPricesModelFactory = new FutureAssetPricesModelFactoryImpl(productInstanceClient,
                                                                                                                          siteFacade,
                                                                                                                          productIdentifierFacade,
                                                                                                                          projectedUsageModelFactory,
                                                                                                                          new DiscountUpdater(),
                                                                                                                          new PricingStrategyDecider(productInstanceClient,
                                                                                                                                                     migrationDetailsProvider,
                                                                                                                                                     pricingClient),
                                                                                                                          pricingClient,
                                                                                                                          projects);
                final UriFactory productConfiguratorUriFactory = new UriFactoryImpl(configuration);
                final SharePointUrlConfig sharePointUrlConfig = configuration.getSharePointUrlConfig();
                final FileExtensionsConfig fileExtensionsConfig = sharePointUrlConfig.getFileExtensionsConfig();
                final FutureAssetPricesFacade productInstancePricesFacade = new FutureAssetPricesFacade(driverManager,
                                                                                                        futureAssetPricesModelFactory);
                SqeAppUrlConfig sqeAppUrlConfig = configuration.getSqeAppUrlConfig();
                final LineItemModelFactory lineItemModelFactory = new LineItemModelFactoryImpl(expedioProjectsResource,
                                                                                               productInstancePricesFacade,
                                                                                               productIdentifierFacade,
                                                                                               productConfiguratorUriFactory,
                                                                                               pmr,
                                                                                               clientManager.getProductInstanceClient(),
                                                                                               productAgreementResourceClient, sqeAppUrlConfig);
                final QuoteOptionOrderFacade orderFacade = new QuoteOptionOrderFacade(projects, new OrderModelFactory(lineItemModelFactory));
                final OfferDetailsModelFactory offerDetailsModelFactory = new OfferDetailsModelFactory(lineItemModelFactory);
                final QuoteOptionOfferFacade quoteOptionOfferFacade = new QuoteOptionOfferFacade(projects, offerDetailsModelFactory);

                final SiteResourceClient siteResourceClient = new SiteResourceClient(configuration.getCustomerInventoryClient(),restRequestBuilderFactory);
                final LineItemFacade lineItemFacade = new LineItemFacade(projects, lineItemModelFactory);
                final QuoteOptionOffersOrchestrator offersOrchestrator = new QuoteOptionOffersOrchestrator(
                    quoteOptionOfferFacade, new OfferAndOrderValidator(productInstanceClient, projects, lineItemFacade, pmr));

                final ProductInstanceClient futureProductInstanceClient = clientManager.getProductInstanceClient();

                final FutureProductInstanceFacade futureProductInstanceFacade = new FutureProductInstanceFacade(futureProductInstanceClient, productBuilder);

                final PricingClientManager pricingClientManager = new PricingClientManager(configuration.getPricingFacadeClientConfig(), pmr);


                withSingleton(new FaviconHandler());
                withSingleton(new WebExceptionMapper());
                withSingleton(new WebAuthenticationExceptionMapper());
                withSingleton(new MethodNotAllowedExceptionMapper());
                final Presenter presenter = new Presenter();
                final QuoteOptionDependencyValidator quoteDependencyValidator = new QuoteOptionDependencyValidator(
                    new QuoteOptionDependency[]{
                        new QuoteOptionBillingAccountValidatorRule(customerResource)
                    }
                );
                withSingleton(new CustomerProjectResourceHandler(
                    presenter,
                    projects,
                    new QuoteOptionOrchestrator(quoteOptionFacade, customerFacade, quoteDependencyValidator,productConfiguratorUriFactory, projects),
                    configuration,
                    expedioServicesFacade,
                    customerResource,
                    applicationPropertyResourceClient, pmr, quoteOptionFacade, futureProductInstanceClient, new QuoteResource(configuration.getExpedioFacadeConfig())));

                final QuoteOptionPricingOrchestrator quoteOptionPricingOrchestrator = new QuoteOptionPricingOrchestrator(
                    productIdentifierFacade, lineItemFacade, quoteOptionFacade, siteFacade, bidManagerCommentsFacade);
                final AttachmentUtil attachmentUtil = new AttachmentUtil(expedioProjectsResource, expedioClientResources, sharePointUrlConfig);
                final QuoteOptionDetailsOrchestrator detailsOrchestrator = new QuoteOptionDetailsOrchestrator(lineItemFacade, productConfiguratorUriFactory, productIdentifierFacade, userFacade,
                                                                                                              quoteOptionFacade, expedioProjectsResource,
                                                                                                              new AttachmentManager(attachmentUtil, empPalClientResources.attachmentResource()),
                                                                                                              capabilityProvider,
                                                                                                              applicationPropertyResourceClient);

                final QuoteOptionBcmExportChannelInformationSheetFactory channelInformationSheetFactory = new QuoteOptionBcmExportChannelInformationSheetFactory(futureProductInstanceFacade);
                final QuoteOptionBcmExportBidInfoSheetFactory bidInfoSheetFactory = new QuoteOptionBcmExportBidInfoSheetFactory(quoteOptionFacade,
                                                                                                                                expedioProjectsResource, customerFacade,
                                                                                                                                lineItemFacade, futureProductInstanceFacade);
                final LineItemVisitorFactory lineItemVisitorFactory = new LineItemVisitorFactory(futureProductInstanceClient);

                final QuoteOptionBcmExportUsageSheetFactory usageSheetFactory = new QuoteOptionBcmExportUsageSheetFactory();
                final QuoteOptionBcmExportSpecialPriceBookSheetFactory specialPriceBookSheetFactory = new QuoteOptionBcmExportSpecialPriceBookSheetFactory(specialPriceBookClient, countries);
                final QuoteOptionBcmExportSiteDetailsSheetFactory siteDetailsSheetFactory = new QuoteOptionBcmExportSiteDetailsSheetFactory();
                final QuoteOptionBcmExportProductLevelInfoSheetFactory productLevelInfoSheetFactory = new QuoteOptionBcmExportProductLevelInfoSheetFactory();
                PricingSheetDataModelFactory pricingSheetDataModelFactory = new PricingSheetDataModelFactory(expedioClientResources, projects, futureProductInstanceClient, caveatResource, pricingClientManager.pricingClient());
                final HeaderRowModelFactory headerRowModelFactory = new HeaderRowModelFactory(pmr);
                final BCMDataRowModelFactory bcmDataRowModelFactory = new BCMDataRowModelFactory(futureProductInstanceClient);
                final ProductsBCMSheetFactory productsBCMSheetFactory = new ProductsBCMSheetFactory(new BCMProductSheetGenerator(),
                                                                                                    headerRowModelFactory,
                                                                                                    bcmDataRowModelFactory,
                                                                                                    pricingSheetDataModelFactory,
                                                                                                    pmr, pricingClientManager.pricingClient());

                final QuoteOptionBcmExportProductPerSiteSheetFactory productPerSiteFactory = new QuoteOptionBcmExportProductPerSiteSheetFactory();

                final QuoteOptionBcmExportSpecialBidSheetFactory specialBidSheetFactory = new QuoteOptionBcmExportSpecialBidSheetFactory();

                final BCMSheetFactory bcmSheetFactory = new BCMSheetFactory(headerRowModelFactory, bcmDataRowModelFactory, pmr,
                                                                            new BCMSheetGenerator(), pricingClientManager.pricingClient());

                final AutoPriceAggregator autoPriceAggregator = new AutoPriceAggregator(productInstanceClient,
                                                                                        pricingClientManager.pricingClient().priceGathererFactory());

                final BCMDiscountUpdater bcmDiscountUpdater = new BCMDiscountUpdater(futureProductInstanceClient, autoPriceAggregator, lineItemFacade);

                final BCMBidInfoFactory bcmBidInfoFactory = new BCMBidInfoFactory();

                final BCMPriceLineInfoFactory bcmPriceLineInfoFactory = new BCMPriceLineInfoFactory();

                final BCMSiteDetailsFactory bcmSiteDetailsFactory = new BCMSiteDetailsFactory();

                final BCMProductInstanceInfoFactory bcmProductInstanceInfoFactory =
                    new BCMProductInstanceInfoFactory(futureProductInstanceClient, bcmPriceLineInfoFactory, bcmSiteDetailsFactory);

                final BCMProductPerSiteFactory bcmProductPerSiteFactory = new BCMProductPerSiteFactory(pmr);

                final BCMInformerFactory bcmInformerFactory = new BCMInformerFactory(quoteOptionFacade,
                                                                                     expedioProjectsResource,
                                                                                     customerFacade,
                                                                                     expedioClientResources,
                                                                                     projects,
                                                                                     productInstanceClient);

                final BCMExportOrchestrator bcmExportOrchestrator =
                    new BCMExportOrchestrator(bcmInformerFactory, bcmBidInfoFactory, bcmProductInstanceInfoFactory, bcmProductPerSiteFactory);

                final QuoteOptionBcmSheetExportOrchestrator quoteOptionBcmSheetExportOrchestrator =
                    new QuoteOptionBcmSheetExportOrchestrator(lineItemFacade,
                                                              bidInfoSheetFactory,
                                                              productPerSiteFactory,
                                                              productsBCMSheetFactory,
                                                              specialBidSheetFactory,
                                                              bcmSheetFactory

                    );

                final QuoteOptionBcmSheetImportOrchestrator quoteOptionBcmSheetImportOrchestrator =
                    new QuoteOptionBcmSheetImportOrchestrator(lineItemFacade, pmr, quoteOptionBcmSheetExportOrchestrator, bcmExportOrchestrator, bcmDiscountUpdater);

                QuoteOptionRevenueOrchestrator revenueOrchestrator = new QuoteOptionRevenueOrchestrator(lineItemFacade, priceBookFacade);

                QuoteOptionClient quoteOptionClient = new QuoteOptionHelper(projects, productInstanceClient, pmr);

                withSingleton(new QuoteOptionResourceHandler(
                    configuration,
                    presenter,
                    projects,
                    detailsOrchestrator,
                    quoteOptionPricingOrchestrator,
                    breadCrumbFactory, quoteOptionNoteFacade,
                    new PricingSheetOrchestrator(pricingSheetDataModelFactory),
                    new QuoteOptionPricingSummaryOrchestrator(lineItemFacade, lineItemVisitorFactory),
                    new QuoteOptionBulkUploadOrchestrator(productConfiguratorUriFactory, new BulkUploadFacade()),
                    revenueOrchestrator, JSONSerializer.getInstance(),
                    customerResource,
                    expedioServicesFacade,
                    new ExpedioClientResources(configuration.getExpedioFacadeConfig()),
                    sessionServiceClientResources.getExpedioSessionResource(),
                    submitWebMetricsUri,
                    capabilityProvider,
                    productInstanceClient,
                    helpLinkUri
                        )
                );

                withSingleton(new ProductAgreementResourceHandler(
                    presenter,
                    projects,
                    JSONSerializer.getInstance(),
                    futureProductInstanceClient,new ProductAgreementOrchestrator(lineItemFacade, productConfiguratorUriFactory, productIdentifierFacade, userFacade,
                                                                                  quoteOptionFacade, expedioProjectsResource, productAgreementResourceClient,contractResource,
                                                                                 pmrLookupClient))
                );
                withSingleton(new ContractDialogResourceHandler(
                    presenter,
                    projects,
                    JSONSerializer.getInstance(),
                    priceBookFacade,
                    futureProductInstanceClient)
                );

                withSingleton(new AttachmentDialogResourceHandler(presenter, detailsOrchestrator
                ));

                withSingleton(new QuoteOptionBcmResourceHandler(
                    presenter,
                    new QuoteOptionBcmExportPricingSheetOrchestrator(lineItemFacade,
                                                                     channelInformationSheetFactory,
                                                                     bidInfoSheetFactory,
                                                                     usageSheetFactory,
                                                                     specialPriceBookSheetFactory,
                                                                     siteDetailsSheetFactory,
                                                                     productLevelInfoSheetFactory, productsBCMSheetFactory),
                    new QuoteOptionBcmPricingSheetOrchestrator(productInstancePricesFacade,
                                                               lineItemFacade,
                                                               specialPriceBookClient,
                                                               new FutureAssetPriceUpdaterFactory(),
                                                               pmr),
                    projects,
                    expedioServicesFacade,
                    quoteOptionBcmSheetExportOrchestrator,
                    quoteOptionBcmSheetImportOrchestrator,
                    bcmExportOrchestrator,
                    bidManagerCommentsFacade
                ));

                final ConfiguratorClient configuratorClient = new ConfiguratorClient(configuration.getConfiguratorClientConfig());
                withSingleton(new ProductActionResourceHandler(presenter,
                                                               siteFacade,
                                                               lineItemFacade,
                                                               new ProductOrchestratorFactory(siteFacade,
                                                                                              productIdentifierFacade,
                                                                                              productConfiguratorUriFactory,
                                                                                              quoteOptionFacade,
                                                                                              pmr,
                                                                                              expedioClientResources,
                                                                                              expedioProjectsResource,
                                                                                              productInstanceClient,
                                                                                              driverManager.getSiteDriver(),
                                                                                              new SiteValidator(new Countries())),
                                                               breadCrumbFactory,
                                                               priceBookFacade,
                                                               projects,
                                                               customerResource,
                                                               configuratorClient, pmr,
                                                               submitWebMetricsUri, helpLinkUri));

                withSingleton(new OfferResourceHandler(
                    presenter,
                    projects,
                    offersOrchestrator,
                    new OfferDetailsOrchestrator(quoteOptionOfferFacade, customers),
                    breadCrumbFactory,
                    customerResource,
                    submitWebMetricsUri,
                    orderFacade, helpLinkUri, new OfferCreationValidator(capabilityProvider, projects, productInstanceClient)));

                expedioUserContextResolver = new ExpedioUserContextResolver(sessionServiceClientResources.getExpedioSessionResource(),
                                                                            new ExpedioClientResources(configuration.getExpedioFacadeConfig()).projectResource());
                expedioUserResource = new ExpedioClientResources(configuration.getExpedioFacadeConfig()).getUserResource();

                ContributesToCharacteristicUpdater contributesToCharacteristicUpdater = new RuleAwareInstanceCharacteristicUpdater(productInstanceClient, new AttributeAssociationProcessor(productInstanceClient));

                final ExecutorService executorService = Executors.newFixedThreadPool(configuration.getBatchProcessorConfig().getMaxThreads());

                final TpeStatusManager tpeStatusManager = new TpeStatusManager(new PricingTpeClient(configuration.getTpeConfig(), new RequestResponseResourceClient(configuration.getInlifeClientConfig())), productInstanceClient, projects);
                withSingleton(new OrderResourceHandler(
                    presenter,
                    productInstanceClient,
                    tpeStatusManager,
                    new QuoteOptionOrdersOrchestrator(orderFacade, quoteOptionOfferFacade, projects, migrationDetailsProvider, productInstanceClient, new InVisibleCreatableLineItemRetriever(productInstanceClient, new CreatableRelationshipInstanceFilter(pmr)), pmr, lineItemFacade,expedioClientResources.getCustomerResource(),applicationPropertyResourceClient),
                    quoteOptionPricingOrchestrator,
                    configuration,
                    expedioUserContextResolver,
                    expedioUserResource,
                    configuratorClient,
                    new CleanOrderValidationResourceClient(configuration.getExpedioFacadeConfig()),
                    capabilityProvider,
                    futureProductInstanceFacade,
                    executorService,
                    pmr,expedioProjectsResource,siteResourceClient,
                    new OrderCreationValidator(capabilityProvider, projects, productInstanceClient)
                ));

                withSingleton(new OrderRFOResourceHandler(
                    presenter,
                    new OrderRFOSheetOrchestrator(orderFacade,
                                                  customers,
                                                  new RFOUpdater(orderFacade, futureProductInstanceClient, contributesToCharacteristicUpdater, pmr),
                                                  new RFOSheetModelBuilder(futureProductInstanceClient, migrationDetailsProvider, contributesToCharacteristicUpdater),
                                                  expedioClientResources,
                                                  migrationDetailsProvider,
                                                  projects,
                                                  siteResourceClient,productInstanceClient),
                    projects,
                    quoteDependencyValidator
                ));

                withSingleton(
                    new QuoteOptionDialogsResourceHandler(productConfiguratorUriFactory, productIdentifierFacade, quoteOptionFacade));

                LineItemNoteResource lineItemNoteResource = new LineItemNoteResource(projectEngineClientResources);
                LineItemNotesFacade lineItemNotesFacade = new LineItemNotesFacade(lineItemNoteResource, userFacade);
                LineItemNoteViewRenderer viewRenderer = new LineItemNoteViewRenderer(presenter);
                LineItemNoteInteractor lineItemNoteInteractor = new LineItemNoteInteractor(lineItemNotesFacade, viewRenderer);
                UserContextProvider userContextProvider = new UserContextProvider();

                DateProvider dateProvider = new DateProvider();
                LineItemNoteResourceHandler lineItemNoteResourceHandler = new LineItemNoteResourceHandler(lineItemNoteInteractor, userContextProvider, dateProvider);
                withSingleton(lineItemNoteResourceHandler);
                PricingFacadeService pricingFacadeService = new PricingFacadeService(pricingClientManager.pricingClient(), futureProductInstanceClient, autoPriceAggregator, projects);
                PriceHandlerService priceHandlerService = new PriceHandlerService(customerResource, pmr, pricingFacadeService,
                                                                                  siteFacade, futureProductInstanceClient,
                                                                                  pricingClientManager.pricingClient(),
                                                                                  projects, new PricingStatusNADecider(futureProductInstanceClient));
                withSingleton(new PriceHandler(futureProductInstanceClient,
                                               new PriceHandlerProcessor(executorService, new PricingTaskFactory(priceHandlerService,
                                                                                                                 futureProductInstanceClient,
                                                                                                                 new ConfiguratorClient(configuration.getConfiguratorClientConfig()),
                                                                                                                 migrationDetailsProvider,
                                                                                                                 pmr,
                                                                                                                 capabilityProvider))));
                withSingleton(resourceHandlers);
                ProductBasedImporter productBasedImporter = new ProductBasedImporter(productInstanceClient, quoteOptionClient, pmr,
                                                                                     new CardinalityValidator(productInstanceClient, siteFacade),
                                                                                     customerResource, configuratorClient, projects, new ProductRelationshipService(futureProductInstanceClient, pmr));
                LineItemBasedImporter lineItemBasedImporter = new LineItemBasedImporter(productInstanceClient, quoteOptionClient, pmr,
                                                                                        new CardinalityValidator(productInstanceClient, siteFacade),
                                                                                        customerResource, configuratorClient, projects, new ProductRelationshipService(futureProductInstanceClient, pmr));

                final ECRFSheetOrchestrator ecrfSheetOrchestrator = new ECRFSheetOrchestrator(new ECRFSheetModelBuilder(), productBasedImporter, lineItemBasedImporter);

                withSingleton(new DiscountHandler(productInstancePricesFacade, quoteOptionFacade, autoPriceAggregator, projects, priceHandlerService, productInstanceClient));
                withSingleton(new PricingActionsHandler(userFacade, projects, expedioServicesFacade, quoteOptionFacade, productInstancePricesFacade, revenueOrchestrator, bidManagerCommentsFacade));
                withSingleton(new ProjectEngineStaticResourceHandler(new StaticResourceLoaderFactory().create()));
                informationProvider = new DefaultMonitoringInfoProvider(ComponentNames.PEW);
                healthProvider = new AggregateMonitoringHealthProvider(new AlwaysGreenMonitoringHealthProvider("On-line"));
                withSingleton(new MonitoringStatisticsHandler(statsCollector,
                                                              informationProvider,
                                                              healthProvider));

                withSingleton(new LineItemResourceHandler(presenter, detailsOrchestrator, projectEngineClientResources, futureProductInstanceFacade,
                                                          ecrfSheetOrchestrator, projects, productInstanceClient, expedioServicesFacade, priceHandlerService, tpeStatusManager, capabilityProvider));
                withSingleton(new ValidationResourceHandler(projects, productInstanceClient, pmr));
                final BulkTemplateExportSheetOrchestrator bulkTemplateExportSheetOrchestrator = new BulkTemplateExportSheetOrchestrator(pmr);
                withSingleton(new BulkTemplateExportResourceHandler(bulkTemplateExportSheetOrchestrator, projects));
                withSingleton(new WebMetricsResourceHandler(configuration.getStatsClientConfig()));
                withSingleton(new UserImportResourceHandler(projects, productInstanceClient, expedioClientResources, pmr, new BulkConfigSheetOrchestrator(new BulkConfigDetailModelBuilder(), productInstanceClient, pmr, quoteOptionClient), expedioServicesFacade));
            }
        };
    }

    private void registerFilters(SessionServiceClientResources sessionServiceClientResources, CookieConfig cookieConfig) {

        final ExpedioProjectResource expedioProjectsResource = new ExpedioClientResources(configuration.getExpedioFacadeConfig()).projectResource();
        final WebAuthenticationUserContextResolver userContextResolver = new WebAuthenticationUserContextResolver(sessionServiceClientResources.getExpedioSessionResource(), expedioProjectsResource, capabilityProvider);
        final WebAuthenticationRequestFilter webAuthenticationRequestFilter = new WebAuthenticationRequestFilter(userContextResolver);
        final WebAuthenticationResponseFilter webAuthenticationResponseFilter = new WebAuthenticationResponseFilter(cookieConfig);
        final RestAuthenticationFilterConfig restAuthenticationFilterConfig = this.configuration.getRestAuthenticationFilterConfig();
        final CustomerAuthorizationFilter customerAuthorizationFilter = new CustomerAuthorizationFilter(sessionServiceClientResources.getPermissionResource());

        if (Boolean.valueOf(restAuthenticationFilterConfig.getEnabled())) {
            RestOrWebAuthenticationRequestFilter restOrWebAuthenticationRequestFilter = new RestOrWebAuthenticationRequestFilter(
                new RestAuthenticationRequestFilter(restAuthenticationFilterConfig),
                webAuthenticationRequestFilter);
            applicationContainerInstance().addContainerRequestFilter(restOrWebAuthenticationRequestFilter);
        } else {
            applicationContainerInstance().addContainerRequestFilter(webAuthenticationRequestFilter);
        }
        applicationContainerInstance().addContainerRequestFilter(customerAuthorizationFilter);

        applicationContainerInstance().addContainerResponseFilter(webAuthenticationResponseFilter);
        applicationContainerInstance().addContainerResponseFilter(new WebResponseCodeConversionFilter());

        applicationContainerInstance().registerBeforeHandler(new LoggingHandler());
        applicationContainerInstance().registerBeforeHandler(statsCollector);
        applicationContainerInstance().registerAfterHandler(new LoggingHandler());
        applicationContainerInstance().registerAfterHandler(statsCollector);
        applicationContainerInstance().addStandardHandlersForComponent(ComponentNames.PEW);
    }
}
