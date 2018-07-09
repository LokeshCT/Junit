package com.bt.nrm.kernel;

import com.bt.nrm.config.NrmConfig;
import com.bt.nrm.handler.BasePageResourceHandler;
import com.bt.nrm.handler.EvaluatorActionResourceHandler;
import com.bt.nrm.handler.NRMEmailHandler;
import com.bt.nrm.handler.NRMUserManagementHandler;
import com.bt.nrm.handler.ProductTemplateResourceHandler;
import com.bt.nrm.handler.QuoteOptionRequestResourceHandler;
import com.bt.nrm.handler.interfaces.NonStandardRequestResponseHandler;
import com.bt.nrm.repository.EvaluatorActionRepository;
import com.bt.nrm.repository.EvaluatorActionRepositoryJPA;
import com.bt.nrm.repository.NRMUserManagementRepositoryJPA;
import com.bt.nrm.repository.ProductTemplateRepository;
import com.bt.nrm.repository.ProductTemplateRepositoryJPA;
import com.bt.nrm.repository.QuoteOptionRequestRepository;
import com.bt.nrm.repository.QuoteOptionRequestRepositoryJPA;
import com.bt.nrm.web.NRMAuthenticationRequestFilter;
import com.bt.nrm.web.NrmClasspathStaticResourceLoader;
import com.bt.nrm.web.NrmStaticResourceHandler;
import com.bt.pms.resources.PMSResource;
import com.bt.rsqe.ComponentNames;
import com.bt.rsqe.EmailService;
import com.bt.rsqe.container.RestApplication;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.container.ioc.RestResourceHandlerFactory;
import com.bt.rsqe.logging.LoggingHandler;
import com.bt.rsqe.monitoring.AggregateMonitoringHealthProvider;
import com.bt.rsqe.monitoring.DbDeployChangelogAwareMonitoringInfoProvider;
import com.bt.rsqe.monitoring.JPADataBaseConnectionHealthProvider;
import com.bt.rsqe.monitoring.MonitoringStatisticsHandler;
import com.bt.rsqe.monitoring.StatisticsCollector;
import com.bt.rsqe.monitoring.StatsMode;
import com.bt.rsqe.persistence.JPAEntityManagerProvider;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.usermanagement.resources.UserResource;

public class NRMApplication extends RestApplication {
    private JPAPersistenceManager persistenceManagerNRM;
    private JPAEntityManagerProvider nrmProvider;
    private StatisticsCollector statsCollector;
    private NrmConfig nrmConfig;


    public NRMApplication(NrmConfig nrmConfig) {
        super(nrmConfig.getApplicationConfig(), nrmConfig.getRestAuthenticationFilterConfig(),ComponentNames.NRM);
        this.nrmConfig = nrmConfig;
        persistenceManagerNRM = new JPAPersistenceManager();
        nrmProvider = new JPAEntityManagerProvider(nrmConfig.getDatabaseConfig(), "nrm");
        statsCollector = new StatisticsCollector(ComponentNames.NRM, StatsMode.ON.getMode());
        applicationContainerInstance().setTransactionAware(persistenceManagerNRM, nrmProvider);
        applicationContainerInstance().registerBeforeHandler(new LoggingHandler());
        applicationContainerInstance().registerBeforeHandler(statsCollector);
        applicationContainerInstance().registerAfterHandler(new LoggingHandler());
        applicationContainerInstance().registerAfterHandler(statsCollector);
        applicationContainerInstance().addStandardHandlersForComponent(ComponentNames.NRM);
        applicationContainerInstance().addContainerRequestFilter(new NRMAuthenticationRequestFilter());
    }

    @Override
    protected ResourceHandlerFactory createResourceHandlerFactory() {
        return new RestResourceHandlerFactory() {
            {
                //withSingleton(new XMLBeansJSONProvider());
                withSingleton(new NrmStaticResourceHandler(new NrmClasspathStaticResourceLoader("")));
                informationProvider = new DbDeployChangelogAwareMonitoringInfoProvider(ComponentNames.NRM, nrmProvider);
                healthProvider = new AggregateMonitoringHealthProvider(new JPADataBaseConnectionHealthProvider(persistenceManagerNRM, "NRM"));
                withSingleton(new MonitoringStatisticsHandler(statsCollector,
                                                              informationProvider,
                                                              healthProvider
                ));
                EmailService emailService = new EmailService(nrmConfig.getEmailServiceConfig().getHost(), nrmConfig.getEmailServiceConfig().getPort());
                UserResource userResource = new UserResource(nrmConfig.getUserManagementClientConfig());
                PMSResource pmsResource = new PMSResource(nrmConfig.getPMSClientConfig());


                withSingleton(new BasePageResourceHandler(userResource));

                ProductTemplateRepository productTemplateRepository = new ProductTemplateRepositoryJPA(persistenceManagerNRM);
                withSingleton(new ProductTemplateResourceHandler(pmsResource,productTemplateRepository));

                QuoteOptionRequestRepository requestRepository = new QuoteOptionRequestRepositoryJPA(persistenceManagerNRM, productTemplateRepository);
                withSingleton(new QuoteOptionRequestResourceHandler(requestRepository));

                withSingleton(new NonStandardRequestResponseHandler(pmsResource, requestRepository, emailService));

                NRMUserManagementRepositoryJPA userManagementRepository = new NRMUserManagementRepositoryJPA(productTemplateRepository,persistenceManagerNRM);
                withSingleton(new NRMUserManagementHandler(userResource, userManagementRepository, productTemplateRepository,pmsResource));


                withSingleton(new NRMEmailHandler(emailService));

                EvaluatorActionRepository evaluatorActionRepository = new EvaluatorActionRepositoryJPA(persistenceManagerNRM);
                withSingleton(new EvaluatorActionResourceHandler(requestRepository, evaluatorActionRepository));

            }
        };
    }
}
