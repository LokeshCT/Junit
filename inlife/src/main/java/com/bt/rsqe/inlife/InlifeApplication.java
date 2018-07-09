package com.bt.rsqe.inlife;

import com.bt.rsqe.ComponentNames;
import com.bt.rsqe.ape.ApeFacade;
import com.bt.rsqe.ape.ApeFacadeSingleton;
import com.bt.rsqe.ape.ApeOnnetBuildingResourceHandlerClient;
import com.bt.rsqe.ape.AvailabilityCheckClient;
import com.bt.rsqe.ape.InterimSiteClient;
import com.bt.rsqe.ape.OnnetBuildingAvailabilityCheckClient;
import com.bt.rsqe.ape.OnnetCheckClient;
import com.bt.rsqe.client.LookupHandler;
import com.bt.rsqe.client.OrderClient;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.container.ioc.RestResourceHandlerFactory;
import com.bt.rsqe.customerinventory.client.CustomerInventoryClientManagerFactory;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.dataarchiving.DataArchivingScheduledTask;
import com.bt.rsqe.domain.product.lookup.HandlerAlreadyRegistered;
import com.bt.rsqe.domain.product.lookup.LookupHandlerRegistry;
import com.bt.rsqe.domain.product.lookup.LookupStrategies;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.inlife.config.InlifeConfig;
import com.bt.rsqe.inlife.repository.ApplicationPropertyStore;
import com.bt.rsqe.inlife.repository.ErrorFactJPARepository;
import com.bt.rsqe.inlife.repository.ExceptionPointJPARepository;
import com.bt.rsqe.inlife.repository.UserJPARepository;
import com.bt.rsqe.inlife.web.ApplicationCapabilityHandler;
import com.bt.rsqe.inlife.web.ApplicationPropertyResourceHandler;
import com.bt.rsqe.inlife.web.CPEUpliftResourceHandler;
import com.bt.rsqe.inlife.web.DataUpliftHandler;
import com.bt.rsqe.inlife.web.ErrorMetricsResourceHandler;
import com.bt.rsqe.inlife.web.InlifeResourceHandler;
import com.bt.rsqe.inlife.web.InlifeStaticResourceHandler;
import com.bt.rsqe.inlife.web.InlifeStaticResourceLoader;
import com.bt.rsqe.inlife.web.PriceUpdateHandler;
import com.bt.rsqe.inlife.web.RequestResponseResourceHandler;
import com.bt.rsqe.monitoring.AggregateMonitoringHealthProvider;
import com.bt.rsqe.monitoring.AlwaysGreenMonitoringHealthProvider;
import com.bt.rsqe.monitoring.DefaultMonitoringInfoProvider;
import com.bt.rsqe.monitoring.MonitoringStatisticsHandler;
import com.bt.rsqe.monitoring.StatisticsCollector;
import com.bt.rsqe.persistence.JPAEntityManagerProvider;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.persistence.JPATransactionalContext;
import com.bt.rsqe.persistence.store.RequestResponseStore;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrClientSingleton;
import com.bt.rsqe.pmr.client.PmrLookupClient;
import com.bt.rsqe.ppsr.client.PpsrFacade;
import com.bt.rsqe.ppsr.client.PpsrFacadeSingleton;
import com.bt.rsqe.ppsr.client.PpsrLookupClient;
import com.bt.rsqe.pricing.AutoPriceAggregator;
import com.bt.rsqe.pricing.PricingClientManager;
import com.bt.rsqe.projectengine.ProjectEngineClientResources;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOrderFacade;
import com.bt.rsqe.taskscheduler.TaskSchedulerOrchestrator;
import com.bt.rsqe.web.FaviconHandler;
import com.bt.rsqe.web.Presenter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.bt.rsqe.factory.ServiceLocator.*;

public class InlifeApplication extends Application {

    private InlifeConfig configuration;
    private static String DATA_ARCHIVING_TASK_NAME = "data-archiving";
    private ScheduledExecutorService dataArchivingScheduler;
    private StatisticsCollector statisticsCollector;
    private JPAPersistenceManager persistenceManager;

    public InlifeApplication(InlifeConfig config) {
        super(config.getApplicationConfig(InlifeConfig.INLIFE_APPLICATION_CONFIG), ComponentNames.INLIFE);
        this.configuration = config;
        persistenceManager = new JPAPersistenceManager();
        JPAEntityManagerProvider jpaEntityManagerProvider = new JPAEntityManagerProvider(configuration.getDatabaseConfig("InlifeDatabase"), "inlife");
        statisticsCollector = new StatisticsCollector(ComponentNames.INLIFE, config.getStatsMode());
        applicationContainerInstance().setTransactionAware(persistenceManager, jpaEntityManagerProvider);
        applicationContainerInstance().registerBeforeHandler(statisticsCollector);
        applicationContainerInstance().registerAfterHandler(statisticsCollector);
    }

    @Override
    protected ResourceHandlerFactory createResourceHandlerFactory() {

        return new RestResourceHandlerFactory() {
            {
                Presenter presenter = new Presenter(new String[]{"com/bt/rsqe/inlife/web/include.ftl"});
                final PmrClient pmr = PmrClientSingleton.getPmrClient(configuration.getPmrClientConfig());
                ProductInstanceClient productInstanceClient = CustomerInventoryClientManagerFactory.getClientManager(configuration.getCustomerInventoryClient(), configuration.getPmrClientConfig())
                                                                                                   .getProductInstanceClient();

                ProjectEngineClientResources projectEngineClientResources = new ProjectEngineClientResources(configuration.getProjectEngineClientConfig());

                ProjectResource projectResource = new ProjectEngineClientResources(configuration.getDataArchivingConfig().getProjectEngineClientConfig()).projectResource();
                ExpedioProjectResource expedioProjectResource = new ExpedioClientResources(configuration.getDataArchivingConfig().getExpedioFacadeConfig()).projectResource();
                final ApeFacade apeFacade = ApeFacadeSingleton.get(configuration.getApeFacadeConfig());
                JPAEntityManagerProvider provider = new JPAEntityManagerProvider(configuration.getTaskSchedulerConfig().getDatabaseConfig("TaskSchedulerDb"), "taskScheduler");
                JPATransactionalContext jpaTransactionalContext = new JPATransactionalContext(provider);

                DataArchivingScheduledTask scheduledTask = new DataArchivingScheduledTask(DATA_ARCHIVING_TASK_NAME,
                                                                                          configuration.getDataArchivingConfig(),
                                                                                          projectResource,
                                                                                          productInstanceClient,
                                                                                          expedioProjectResource,
                                                                                          jpaTransactionalContext);

                dataArchivingScheduler = Executors.newScheduledThreadPool(1);

                TaskSchedulerOrchestrator taskSchedulerOrchestrator = new TaskSchedulerOrchestrator(dataArchivingScheduler, scheduledTask);
                final PricingClientManager pricingClientManager = new PricingClientManager(configuration.getPricingFacadeClientConfig(),pmr);

                ErrorMetricsResourceHandler errorMetricsResourceHandler = new ErrorMetricsResourceHandler(new ErrorFactJPARepository(persistenceManager), new UserJPARepository(persistenceManager), new ExceptionPointJPARepository(persistenceManager));

                withSingleton(new FaviconHandler());
                withSingleton(new DataUpliftHandler(presenter, productInstanceClient, projectEngineClientResources));
                withSingleton(new CPEUpliftResourceHandler(productInstanceClient,new AutoPriceAggregator(productInstanceClient, pricingClientManager.pricingClient().priceGathererFactory()), projectResource));
                withSingleton(new PriceUpdateHandler(productInstanceClient, pricingClientManager.pricingClient(), projectResource, apeFacade));
                withSingleton(new InlifeResourceHandler(presenter, configuration, taskSchedulerOrchestrator, configuration.getLogsBasePath()));
                withSingleton(new InlifeStaticResourceHandler(new InlifeStaticResourceLoader()));
                withSingleton(errorMetricsResourceHandler);
                informationProvider = new DefaultMonitoringInfoProvider(ComponentNames.INLIFE);
                healthProvider =   new AggregateMonitoringHealthProvider(new AlwaysGreenMonitoringHealthProvider("On-line"));

                withSingleton(new MonitoringStatisticsHandler(statisticsCollector,
                                                              informationProvider,
                                                               healthProvider
                                                              ));
                withSingleton(new RequestResponseResourceHandler(new RequestResponseStore(persistenceManager, 20000)));
                ApplicationPropertyStore applicationPropertyStore = new ApplicationPropertyStore(persistenceManager) ;
                withSingleton(new ApplicationPropertyResourceHandler(applicationPropertyStore));
                withSingleton(new ApplicationCapabilityHandler(presenter, configuration, applicationPropertyStore));
                PmrLookupClient pmrLookupClient = new PmrLookupClient(configuration.getPmrClientConfig());
                ExpedioClientResources expedioClientResources = new ExpedioClientResources(configuration.getExpedioFacadeConfig());
                ApeOnnetBuildingResourceHandlerClient apeOnnetBuildingResourceHandlerClient =new ApeOnnetBuildingResourceHandlerClient(configuration.getApeFacadeConfig());
                createServiceLocator(productInstanceClient,
                                     pmrLookupClient,
                                     pmr,
                                     new QuoteOptionOrderFacade(projectResource, null),
                                     expedioClientResources
                                     );
                initiateLookupHandlerFactory(pmrLookupClient,
                                             PpsrFacadeSingleton.get(configuration.getPpsrFacadeConfig()),
                                             apeFacade,
                                             expedioClientResources,
                                             apeOnnetBuildingResourceHandlerClient);
            }
        };
    }

    private void createServiceLocator(ProductInstanceClient productInstanceClient,
                                      PmrLookupClient pmrLookupClient,
                                      PmrClient pmrClient,
                                      OrderClient orderClient,
                                      ExpedioClientResources expedioClientResources) {
        serviceLocatorInstance().registerIfNotAlreadyRegistered(pmrClient);
        serviceLocatorInstance().registerIfNotAlreadyRegistered(pmrLookupClient);
        serviceLocatorInstance().registerIfNotAlreadyRegistered(productInstanceClient);
        serviceLocatorInstance().registerIfNotAlreadyRegistered(orderClient);
        serviceLocatorInstance().registerIfNotAlreadyRegistered(expedioClientResources);
    }

    private void initiateLookupHandlerFactory(PmrLookupClient pmrLookupClient,
                                              PpsrFacade ppsrFacade,
                                              ApeFacade apeFacade,
                                              ExpedioClientResources expedioClientResources,
                                              ApeOnnetBuildingResourceHandlerClient apeOnnetBuildingResourceHandlerClient) {

        registerLookupStrategy(LookupStrategies.RULESET_STRATEGY, pmrLookupClient);
        registerLookupStrategy(LookupStrategies.CPE_STRATEGY, pmrLookupClient);
        registerLookupStrategy(LookupStrategies.PPSR_RULE_STRATEGY, new PpsrLookupClient(ppsrFacade));
        registerLookupStrategy(LookupStrategies.APE_ONNET_CHECK_STRATEGY, new OnnetCheckClient(apeFacade, expedioClientResources));
        registerLookupStrategy(LookupStrategies.APE_INTERIM_SITE_STRATEGY, new InterimSiteClient(apeFacade, expedioClientResources));
        registerLookupStrategy(LookupStrategies.APE_AVAILABILITY_CHECK_STRATEGY, new AvailabilityCheckClient(apeFacade));
        registerLookupStrategy(LookupStrategies.APE_ONNET_BUILDING_AVAILABILITY_CHECK_STRATEGY, new OnnetBuildingAvailabilityCheckClient(apeOnnetBuildingResourceHandlerClient));
    }

    void registerLookupStrategy(LookupStrategies strategy, LookupHandler handler) {
        try {
            LookupHandlerRegistry.get().register(strategy, handler);
        } catch (HandlerAlreadyRegistered ex) {}
    }

    @Override
    protected void doStop() {
        dataArchivingScheduler.shutdown();
        super.doStop();
    }
}
