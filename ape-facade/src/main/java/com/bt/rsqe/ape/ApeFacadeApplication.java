package com.bt.rsqe.ape;

import com.bt.cqm.client.SACAvailabilityCheckerClient;
import com.bt.rsqe.ComponentNames;
import com.bt.rsqe.EmailService;
import com.bt.rsqe.ape.callback.SACSupplierProductCallbackHandler;
import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.client.ApeOnNetBuildingClient;
import com.bt.rsqe.ape.config.ApeFacadeConfig;
import com.bt.rsqe.ape.monitoring.APEHealthProvider;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.ape.source.OnnetDetailsOrchestrator;
import com.bt.rsqe.ape.source.SupplierProductStore;
import com.bt.rsqe.ape.source.extractor.ResponseExtractorStrategyFactory;
import com.bt.rsqe.ape.source.processor.RequestBuilder;
import com.bt.rsqe.ape.source.processor.SupplierCheckRequestProcessor;
import com.bt.rsqe.ape.source.scheduler.AvailabilityCheckScheduler;
import com.bt.rsqe.ape.source.scheduler.AvailabilityRequestProcessor;
import com.bt.rsqe.ape.source.scheduler.CountryApplicabilityUpdater;
import com.bt.rsqe.ape.source.scheduler.CountryApplicabilityUpdaterScheduler;
import com.bt.rsqe.ape.source.scheduler.RequestCompletionNotifier;
import com.bt.rsqe.ape.source.scheduler.RequestCompletionNotifierScheduler;
import com.bt.rsqe.ape.source.scheduler.SacScheduler;
import com.bt.rsqe.ape.source.scheduler.SupplierProductAvailabilityRequestRetryProcessor;
import com.bt.rsqe.ape.source.scheduler.SupplierProductAvailabilityRequestRetryScheduler;
import com.bt.rsqe.ape.source.scheduler.SupplierStatusUpdater;
import com.bt.rsqe.ape.source.scheduler.SupplierStatusUpdaterScheduler;
import com.bt.rsqe.container.RestApplication;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.container.ioc.RestResourceHandlerFactory;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.logging.LoggingHandler;
import com.bt.rsqe.monitoring.AggregateMonitoringHealthProvider;
import com.bt.rsqe.monitoring.DbDeployChangelogAwareMonitoringInfoProvider;
import com.bt.rsqe.monitoring.JPADataBaseConnectionHealthProvider;
import com.bt.rsqe.monitoring.MonitoringStatisticsHandler;
import com.bt.rsqe.monitoring.StatisticsCollector;
import com.bt.rsqe.persistence.JPAEntityManagerProvider;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.security.ExpedioUserContextResolver;
import com.bt.rsqe.session.client.SessionServiceClientResources;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.bt.rsqe.ape.config.ApeServiceEndPointConfig.*;
import static com.bt.rsqe.ape.config.CallbackEndpointConfig.*;

public class ApeFacadeApplication extends RestApplication {
    private StatisticsCollector statsCollector;
    private APEQrefJPARepository apeQrefRepository;
    private APEClient apeClient;
    private JPAEntityManagerProvider jpaEntityManagerProvider;
    private ApeFacadeConfig apeFacadeConfig;
    private ExpedioUserContextResolver expedioUserContextResolver;
    private UserResource expedioUserResource;
    private EmailService emailService;
    private CustomerResource customerResource;
    private SACAvailabilityCheckerClient sacAvailabilityCheckerClient;
    private SupplierProductResourceClient supplierProductResourceClient;
    private ApeOnnetBuildingResourceHandlerClient apeOnnetBuildingResourceHandlerClient;
    private JPAPersistenceManager persistenceManager;
    private ExecutorService executorService;

    public ApeFacadeApplication(ApeFacadeConfig apeFacadeConfig, SessionServiceClientResources sessions) {
        super(apeFacadeConfig.getApplicationConfig(), apeFacadeConfig.getRestAuthenticationFilterConfig(), ComponentNames.AF);
        this.apeFacadeConfig = apeFacadeConfig;
        persistenceManager = new JPAPersistenceManager();
        jpaEntityManagerProvider = new JPAEntityManagerProvider(apeFacadeConfig.getDatabaseConfig("ApeFacadeDatabase"), "apeFacade");
        apeQrefRepository = new APEQrefJPARepository(persistenceManager);
        apeClient = new APEClient(apeFacadeConfig.getApeServiceEndPointConfig(APE_END_POINT_NAME));
        executorService = Executors.newFixedThreadPool(apeFacadeConfig.getBatchProcessorConfig().getMaxThreads());
        expedioUserContextResolver = new ExpedioUserContextResolver(sessions.getExpedioSessionResource(), new ExpedioClientResources(apeFacadeConfig.getExpedioFacadeConfig()).projectResource());
        expedioUserResource = new ExpedioClientResources(apeFacadeConfig.getExpedioFacadeConfig()).getUserResource();
        customerResource = new ExpedioClientResources(apeFacadeConfig.getExpedioFacadeConfig()).getCustomerResource();
        sacAvailabilityCheckerClient =new SACAvailabilityCheckerClient(apeFacadeConfig.getCQMClientConfig());
        emailService = new EmailService(apeFacadeConfig.getEmailServiceConfig().getHost(), apeFacadeConfig.getEmailServiceConfig().getPort());
        supplierProductResourceClient = new SupplierProductResourceClient(apeFacadeConfig.getApplicationConfig(),apeFacadeConfig.getRestAuthenticationClientConfig());
        apeOnnetBuildingResourceHandlerClient =new ApeOnnetBuildingResourceHandlerClient(apeFacadeConfig.getApplicationConfig(),apeFacadeConfig.getRestAuthenticationClientConfig());

        statsCollector = new StatisticsCollector(ComponentNames.AF, apeFacadeConfig.getStatsMode());

        registerRestAuthenticationFilterIfEnabled();

        applicationContainerInstance().setTransactionAware(persistenceManager, jpaEntityManagerProvider);
        applicationContainerInstance().registerBeforeHandler(new LoggingHandler());
        applicationContainerInstance().registerBeforeHandler(statsCollector);
        applicationContainerInstance().registerAfterHandler(new LoggingHandler());
        applicationContainerInstance().registerAfterHandler(statsCollector);

        applicationContainerInstance().addStandardHandlersForComponent(ComponentNames.AF);
    }

    @Override
    protected ResourceHandlerFactory createResourceHandlerFactory() {
        return new RestResourceHandlerFactory() {
            {
                RequestBuilder requestBuilder = new RequestBuilder();
                OnnetDetailsOrchestrator onnetDetailsOrchestrator= new OnnetDetailsOrchestrator(apeQrefRepository);
                withSingleton(new ApeEmailHandler(emailService, apeQrefRepository, expedioUserResource));
                withSingleton(new ApeRequestHandler(apeQrefRepository, apeClient, apeFacadeConfig.getCallbackEndpointConfig(APE_URI), customerResource));
                withSingleton(new ApeOnnetBuildingResourceHandler(new ApeOnNetBuildingClient(apeFacadeConfig.getApeServiceEndPointConfig(APE_ON_NET_CHECK_END_POINT_NAME)),
                                                                  apeFacadeConfig.getSupplierCheckConfig(),
                                                                  customerResource,requestBuilder,
                                                                  apeOnnetBuildingResourceHandlerClient,
                                                                  onnetDetailsOrchestrator));
                withSingleton(new APEInteractionsHandler(apeClient, apeQrefRepository, expedioUserContextResolver, expedioUserResource, customerResource));
                withSingleton(new ApeConfigHandler(apeQrefRepository));
                withSingleton(new ApeQrefResourceHandler(apeQrefRepository));
                informationProvider = new DbDeployChangelogAwareMonitoringInfoProvider(ComponentNames.AF, jpaEntityManagerProvider);
                healthProvider = new AggregateMonitoringHealthProvider(new JPADataBaseConnectionHealthProvider(persistenceManager, "Ape-Facade"),
                        new APEHealthProvider(apeFacadeConfig));
                withSingleton(new MonitoringStatisticsHandler(statsCollector,
                        informationProvider,
                        healthProvider
                ));
                withSingleton(new ApeInterimSiteResourceHandler(apeClient));
                withSingleton(new SupplierProductStore(apeQrefRepository));
                withSingleton(new SupplierProductResourceHandler(apeFacadeConfig.getSupplierCheckConfig(),
                                                                 executorService,supplierProductResourceClient,
                                                                 customerResource,sacAvailabilityCheckerClient,
                                                                 requestBuilder,
                                                                 apeOnnetBuildingResourceHandlerClient,
                                                                 onnetDetailsOrchestrator));
                withSingleton(new SupplierProductCallbackHandler(customerResource, new ResponseExtractorStrategyFactory()));

                SupplierCheckRequestProcessor supplierCheckRequestProcessor=new SupplierCheckRequestProcessor(apeFacadeConfig.getSupplierCheckConfig(),
                                                                                                              customerResource, requestBuilder,
                                                                                                              apeOnnetBuildingResourceHandlerClient,
                                                                                                              onnetDetailsOrchestrator);
                withSingleton(new SACSupplierProductCallbackHandler(new ResponseExtractorStrategyFactory(),
                                                                    supplierCheckRequestProcessor,
                                                                    sacAvailabilityCheckerClient,
                                                                    supplierProductResourceClient));
                withSingleton(new CountryApplicabilityUpdaterScheduler(apeFacadeConfig.getSupplierCheckConfig().getSchedulerConfig(), new CountryApplicabilityUpdater(apeFacadeConfig.getSupplierCheckConfig(), jpaEntityManagerProvider)));
                withSingleton(new SacScheduler(sacAvailabilityCheckerClient, supplierProductResourceClient, jpaEntityManagerProvider,apeFacadeConfig.getSupplierCheckConfig().getSchedulerConfig()));
                withSingleton(new SupplierStatusUpdaterScheduler(apeFacadeConfig.getSupplierCheckConfig().getSchedulerConfig(), new SupplierStatusUpdater(apeFacadeConfig.getSupplierCheckConfig(), jpaEntityManagerProvider)));
                withSingleton(new RequestCompletionNotifierScheduler(apeFacadeConfig.getSupplierCheckConfig().getSchedulerConfig(), new RequestCompletionNotifier(apeFacadeConfig.getSupplierCheckConfig(), jpaEntityManagerProvider)));
                withSingleton(new AvailabilityCheckScheduler(apeFacadeConfig.getSupplierCheckConfig().getSchedulerConfig(),
                                                             new AvailabilityRequestProcessor(apeFacadeConfig ,
                                                                                              jpaEntityManagerProvider,
                                                                                              supplierCheckRequestProcessor)));
                withSingleton(new SupplierProductAvailabilityRequestRetryScheduler(apeFacadeConfig.getSupplierCheckConfig().getSchedulerConfig(),
                                                             new SupplierProductAvailabilityRequestRetryProcessor(apeFacadeConfig ,
                                                                                              jpaEntityManagerProvider,
                                                                                              supplierCheckRequestProcessor)));
            }
        };
    }

    @Override
    protected void doStop() {
        persistenceManager.undo();
        persistenceManager.unbind();
        jpaEntityManagerProvider.close();
    }
}
