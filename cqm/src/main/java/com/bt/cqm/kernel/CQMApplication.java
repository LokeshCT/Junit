package com.bt.cqm.kernel;

import com.bt.ClasspathStaticResourceLoader;
import com.bt.cqm.client.SACAvailabilityCheckerClient;
import com.bt.cqm.config.CqmConfig;
import com.bt.cqm.handler.ActivityHandler;
import com.bt.cqm.handler.AuditTrailHandler;
import com.bt.cqm.handler.BillingAccountHandler;
import com.bt.cqm.handler.BranchSiteContactResourceHandler;
import com.bt.cqm.handler.CQMBasePageResourceHandler;
import com.bt.cqm.handler.ChannelHierarchyResource;
import com.bt.cqm.handler.ChannelHierarchyResourceHandler;
import com.bt.cqm.handler.ContactResourceHandler;
import com.bt.cqm.handler.CqmUserResourceHandler;
import com.bt.cqm.handler.CustomerResourceHandler;
import com.bt.cqm.handler.LegalEntityHandler;
import com.bt.cqm.handler.NADSearchAddressHandler;
import com.bt.cqm.handler.OrderDetailsHandler;
import com.bt.cqm.handler.PriceBookHandler;
import com.bt.cqm.handler.QuoteResourceHandler;
import com.bt.cqm.handler.SiteResourceHandler;
import com.bt.cqm.handler.TabBuilder;
import com.bt.cqm.handler.UserDashboardHandler;
import com.bt.cqm.handler.UserManagementHandler;
import com.bt.cqm.handler.VPNHandler;
import com.bt.cqm.ldap.LdapRepository;
import com.bt.cqm.ldap.SearchBTDirectoryHandler;
import com.bt.cqm.repository.user.UserManagementRepository;
import com.bt.cqm.repository.user.UserManagementRepositoryJPA;
import com.bt.cqm.web.CQMAuthenticationRequestFilter;
import com.bt.cqm.web.StaticResourceHandler;
import com.bt.dsl.handler.AvailabilityCheckerResourceHandler;
import com.bt.rsqe.ComponentNames;
import com.bt.rsqe.EmailService;
import com.bt.rsqe.ape.SupplierProductResourceClient;
import com.bt.rsqe.configuration.UrlConfig;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.RestApplication;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.container.ioc.RestResourceHandlerFactory;
import com.bt.rsqe.customerinventory.client.resource.BfgPricebookResourceClient;
import com.bt.rsqe.customerinventory.client.resource.BillingAccountResourceClient;
import com.bt.rsqe.customerinventory.client.resource.ContactResourceClient;
import com.bt.rsqe.customerinventory.client.resource.ContractResourceClient;
import com.bt.rsqe.customerinventory.client.resource.LegalEntityResourceClient;
import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.customerinventory.resources.CustomerResource;
import com.bt.rsqe.customerinventory.resources.SiteLocationResource;
import com.bt.rsqe.customerinventory.resources.VPNResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.emppal.attachmentresource.EmpPalClientResources;
import com.bt.rsqe.emppal.attachmentresource.EmpPalResource;
import com.bt.rsqe.expedio.audit.AuditTrailResource;
import com.bt.rsqe.expedio.order.OrderSearchResource;
import com.bt.rsqe.expedio.services.ActivityResource;
import com.bt.rsqe.expedio.services.AssignedToContactResource;
import com.bt.rsqe.expedio.services.quote.QuoteResource;
import com.bt.rsqe.expedio.usermanagement.UserManagementResource;
import com.bt.rsqe.factory.RestRequestBuilderFactory;
import com.bt.rsqe.logging.LoggingHandler;
import com.bt.rsqe.monitoring.AggregateMonitoringHealthProvider;
import com.bt.rsqe.monitoring.DbDeployChangelogAwareMonitoringInfoProvider;
import com.bt.rsqe.monitoring.JPADataBaseConnectionHealthProvider;
import com.bt.rsqe.monitoring.MonitoringStatisticsHandler;
import com.bt.rsqe.monitoring.StatisticsCollector;
import com.bt.rsqe.monitoring.StatsMode;
import com.bt.rsqe.monitoring.WebMetricsResourceHandler;
import com.bt.rsqe.nad.client.AddressSearchResource;
import com.bt.rsqe.persistence.JPAEntityManagerProvider;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.ppsr.client.PriceBookResource;
import com.bt.rsqe.ppsr.client.ProductResource;
import com.bt.rsqe.ppsr.client.pop.PpsrPOPDetailClient;
import com.bt.rsqe.projectengine.SiteModifiedResource;
import com.bt.rsqe.security.RestAuthenticationFilterConfig;
import com.bt.rsqe.sqefacade.IPCG2UserQuoteStatisticsResource;
import com.bt.rsqe.sqefacade.UserQuoteStatisticsResource;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.collect.Lists.*;

public class CQMApplication extends RestApplication {
    private JPAPersistenceManager persistenceManagerCQM;
    private JPAEntityManagerProvider cqmProvider;
    private StatisticsCollector statsCollector;
    private ExecutorService executorService;
    private SACAvailabilityCheckerClient sacAvailabilityCheckerClient;
    private CqmConfig cqmConfig;

    public CQMApplication(CqmConfig cqmConfig) {
        this(cqmConfig,cqmConfig.getApplicationConfig(), cqmConfig.getRestAuthenticationFilterConfig(), ComponentNames.CQM, Executors.newFixedThreadPool(cqmConfig.getBatchProcessorConfig().getMaxThreads()));
    }

    public CQMApplication(CqmConfig cqmConfig, ApplicationConfig appConfig, RestAuthenticationFilterConfig restAuthenticationFilterConfig, ComponentNames name,ExecutorService executorService) {
        super(appConfig, restAuthenticationFilterConfig, name);
        this.executorService = executorService;
        this.cqmConfig = cqmConfig;
        sacAvailabilityCheckerClient = new SACAvailabilityCheckerClient(cqmConfig.getCQMClientConfig());


        persistenceManagerCQM = new JPAPersistenceManager();
        cqmProvider = new JPAEntityManagerProvider(cqmConfig.getDatabaseConfig(), "cqm");

        statsCollector = new StatisticsCollector(ComponentNames.CQM, StatsMode.ON.getMode());   //for CQM, stats collection is ON.
        applicationContainerInstance().setTransactionAware(persistenceManagerCQM, cqmProvider);

        applicationContainerInstance().registerBeforeHandler(new LoggingHandler());
        applicationContainerInstance().registerBeforeHandler(statsCollector);

        applicationContainerInstance().registerAfterHandler(new LoggingHandler());
        applicationContainerInstance().registerAfterHandler(statsCollector);

        applicationContainerInstance().addStandardHandlersForComponent(ComponentNames.CQM);

        applicationContainerInstance().addContainerRequestFilter(new CQMAuthenticationRequestFilter());
        //applicationContainerInstance().addContainerResponseFilter(new ResponseLogFilter());
    }


    @Override
    protected ResourceHandlerFactory createResourceHandlerFactory() {
        return new RestResourceHandlerFactory() {
            {
                UserManagementRepository userManagementRepository = new UserManagementRepositoryJPA(persistenceManagerCQM);
                ExpedioClientResources expedioClientResource = new ExpedioClientResources(cqmConfig.getExpedioFacadeConfig());
                RestRequestBuilderFactory restRequestBuilderFactory = new RestRequestBuilderFactory();
                ActivityResource activityResource = new ActivityResource(cqmConfig.getExpedioFacadeConfig());
                SiteModifiedResource siteModifiedResource = new SiteModifiedResource(cqmConfig.getProjectEngineClientConfig(),restRequestBuilderFactory);
                SupplierProductResourceClient supplierProductResourceClient = new SupplierProductResourceClient(cqmConfig.getApeFacadeConfig());
                EmpPalResource empPalResource = new EmpPalClientResources(cqmConfig.getEmpPalFacadeClientConfig()).attachmentResource();
                PpsrPOPDetailClient ppsrPOPDetailClient = new PpsrPOPDetailClient(cqmConfig.getPpsrFacadeConfig());
                withSingleton(new StaticResourceHandler(new ClasspathStaticResourceLoader("")));

                UserManagementResource userManagementResource = new UserManagementResource(cqmConfig.getExpedioFacadeConfig());

                withSingleton(new CQMBasePageResourceHandler(userManagementRepository, new TabBuilder(), new LdapRepository(),userManagementResource));
                withSingleton(new CqmUserResourceHandler(userManagementRepository));

                // For handling Site related operations
                SiteResourceClient siteResourceClient = new SiteResourceClient(cqmConfig.getCustomerInventoryClientConfig(), restRequestBuilderFactory);

                // For handling Site related operations
                CustomerResource customerResource = new CustomerResource(cqmConfig.getCustomerInventoryClientConfig());

                // For handling Site related operations
                ContractResourceClient contractResource = new ContractResourceClient(cqmConfig.getCustomerInventoryClientConfig(),restRequestBuilderFactory);

                // For handling Contacts related operations
                ContactResourceClient contactResourceClient = new ContactResourceClient(cqmConfig.getCustomerInventoryClientConfig(),restRequestBuilderFactory);
                SiteLocationResource siteLocationResource = new SiteLocationResource(cqmConfig.getCustomerInventoryClientConfig());

                withSingleton(new CustomerResourceHandler(expedioClientResource.getCustomerResource(),customerResource, siteResourceClient, siteLocationResource, contractResource,activityResource,empPalResource));
                withSingleton(new ContactResourceHandler(contactResourceClient, siteResourceClient));

                NADSearchAddressHandler nadSearchAddressHandler = null;
                AddressSearchResource addressSearchResource = new AddressSearchResource(cqmConfig.getNadFacadeClientConfig());
                nadSearchAddressHandler = new NADSearchAddressHandler(addressSearchResource);
                withSingleton(nadSearchAddressHandler);

                SiteResourceHandler siteUpdateResourceHandler = new SiteResourceHandler(customerResource, siteResourceClient,contractResource,siteModifiedResource,ppsrPOPDetailClient);
                withSingleton(siteUpdateResourceHandler);


                

                // For handling Quote related operations
                QuoteResource quoteResource = new QuoteResource(cqmConfig.getExpedioFacadeConfig());
                BillingAccountResourceClient billingAccountResourceClient = new BillingAccountResourceClient(cqmConfig.getCustomerInventoryClientConfig(), restRequestBuilderFactory);
                VPNResource vpnResource = new VPNResource(cqmConfig.getCustomerInventoryClientConfig());
                EmailService emailService = new EmailService(cqmConfig.getEmailServiceConfig().getHost(), cqmConfig.getEmailServiceConfig().getPort());
                QuoteResourceHandler quoteResourceHandler = new QuoteResourceHandler(userManagementRepository, quoteResource, contractResource, cqmConfig.getBundlingAppConfig(), cqmConfig.getSqeAppConfig(), emailService,customerResource,cqmConfig.getReportAppConfig(),siteResourceClient, empPalResource);
                withSingleton(quoteResourceHandler);
                // BillingAccountHandler
                BillingAccountHandler billingAccountHandler = new BillingAccountHandler(billingAccountResourceClient, siteResourceClient,customerResource, contactResourceClient,userManagementRepository, expedioClientResource);
                withSingleton(billingAccountHandler);
                // For BT search
                withSingleton(new SearchBTDirectoryHandler());

                //Activity Handler
                AssignedToContactResource assignedToContactResource = new AssignedToContactResource(cqmConfig.getExpedioFacadeConfig());
                withSingleton(new ActivityHandler(assignedToContactResource, activityResource,emailService,cqmConfig));

                // Channel Hierarchy
                ChannelHierarchyResource channelHierarchyResource = new ChannelHierarchyResource(cqmConfig.getCustomerInventoryClientConfig());
                withSingleton(new ChannelHierarchyResourceHandler(channelHierarchyResource));

                // Added by AB
                // User Management Handler
                withSingleton(new UserManagementHandler(userManagementRepository,userManagementResource));



                // Order Search
                OrderSearchResource orderSearchResource = new OrderSearchResource(cqmConfig.getExpedioFacadeConfig());
                OrderDetailsHandler orderDetailsHandler = new OrderDetailsHandler(orderSearchResource,empPalResource);
                withSingleton(orderDetailsHandler);

                ProductResource productResource = new ProductResource(cqmConfig.getPpsrFacadeConfig());
                PriceBookResource priceBookResource = new PriceBookResource(cqmConfig.getPpsrFacadeConfig());
                com.bt.rsqe.expedio.product.ProductResource productExpedioResource=new com.bt.rsqe.expedio.product.ProductResource(cqmConfig.getExpedioFacadeConfig());
                BfgPricebookResourceClient bfgPricebookResourceClient = new BfgPricebookResourceClient(cqmConfig.getCustomerInventoryClientConfig(), restRequestBuilderFactory);
                withSingleton(new PriceBookHandler(productResource,priceBookResource,productExpedioResource,channelHierarchyResource,bfgPricebookResourceClient));

                informationProvider =    new DbDeployChangelogAwareMonitoringInfoProvider(ComponentNames.CQM, cqmProvider);
                healthProvider = new AggregateMonitoringHealthProvider(new JPADataBaseConnectionHealthProvider(persistenceManagerCQM, "CQM"));
                withSingleton(new MonitoringStatisticsHandler(statsCollector,
                                                              informationProvider,
                                                              healthProvider
                ));

                BranchSiteContactResourceHandler branchSiteContactResourceHandler = new BranchSiteContactResourceHandler(siteResourceClient, contactResourceClient);
                withSingleton(branchSiteContactResourceHandler);

                VPNHandler vpnHandler = new VPNHandler(vpnResource);
                withSingleton(vpnHandler);

                /*Legal Entity Handler*/
                LegalEntityResourceClient legalEntityResourceClient = new LegalEntityResourceClient(cqmConfig.getCustomerInventoryClientConfig(), restRequestBuilderFactory);
                LegalEntityHandler legalEntityHandler = new LegalEntityHandler(legalEntityResourceClient, userManagementRepository);
                withSingleton(legalEntityHandler);
                withSingleton(new WebMetricsResourceHandler(cqmConfig.getStatsClient()));

                /*Audit Trail*/
                AuditTrailResource auditTrailResource = new AuditTrailResource(cqmConfig.getExpedioFacadeConfig());
                AuditTrailHandler auditTrailHandler = new AuditTrailHandler(auditTrailResource);
                withSingleton(auditTrailHandler);

                /*DSL Checker*/
                AvailabilityCheckerResourceHandler dslCheckerResourceHandler=new AvailabilityCheckerResourceHandler(empPalResource, userManagementRepository,cqmConfig.getDslCheckerSharePointPathConfig(),supplierProductResourceClient,siteResourceClient,sacAvailabilityCheckerClient,executorService, emailService);
                withSingleton(dslCheckerResourceHandler);

                /* Dashboard */
                UserQuoteStatisticsResource userQuoteStatisticsResource = new IPCG2UserQuoteStatisticsResource(cqmConfig.getSqeIvpnFacadeConfig());
                withSingleton(new UserDashboardHandler(userQuoteStatisticsResource));

			}

        };
    }

}
