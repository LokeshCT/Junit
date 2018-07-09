package com.bt.usermanagement.kernel;

import com.bt.rsqe.ComponentNames;
import com.bt.rsqe.configuration.UrlConfig;
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
import com.bt.rsqe.web.staticresources.ClasspathStaticResourceLoader;
import com.bt.rsqe.web.staticresources.StaticResourceHandler;
import com.bt.usermanagement.config.UserManagementConfig;
import com.bt.usermanagement.handler.UserManagementHandler;
import com.bt.usermanagement.handler.rSQEHomePageHandler;
import com.bt.usermanagement.repository.UserManagementRepository;
import com.bt.usermanagement.repository.UserManagementRepositoryJPA;
import com.bt.usermanagement.web.UserManagementClasspathStaticResourceLoader;
import com.bt.usermanagement.web.UserManagementStaticResourceHandler;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static com.google.common.collect.Lists.*;

public class UserManagementApplication extends RestApplication {
    private JPAPersistenceManager persistenceManagerUserMgmt;
    private JPAEntityManagerProvider userManagementProvider;
    private StatisticsCollector statsCollector;
    private UserManagementConfig userManagementConfig;

    public UserManagementApplication(UserManagementConfig userManagementConfig) {
        super(userManagementConfig.getApplicationConfig(), userManagementConfig.getRestAuthenticationFilterConfig(), ComponentNames.USERMGMT);
        this.userManagementConfig = userManagementConfig;
        persistenceManagerUserMgmt = new JPAPersistenceManager();
        userManagementProvider = new JPAEntityManagerProvider(userManagementConfig.getDatabaseConfig(), "userManagement");
        statsCollector = new StatisticsCollector(ComponentNames.USERMGMT, StatsMode.ON.getMode());
        applicationContainerInstance().setTransactionAware(persistenceManagerUserMgmt, userManagementProvider);
        applicationContainerInstance().registerBeforeHandler(new LoggingHandler());
        applicationContainerInstance().registerBeforeHandler(statsCollector);
        applicationContainerInstance().registerAfterHandler(new LoggingHandler());
        applicationContainerInstance().registerAfterHandler(statsCollector);
        applicationContainerInstance().addStandardHandlersForComponent(ComponentNames.USERMGMT);
    }

    @Override
    protected ResourceHandlerFactory createResourceHandlerFactory() {
        return new RestResourceHandlerFactory() {
            {
                UserManagementRepository userManagementRepository = new UserManagementRepositoryJPA(persistenceManagerUserMgmt);
                withSingleton(new StaticResourceHandler(new ClasspathStaticResourceLoader("")));
                informationProvider = new DbDeployChangelogAwareMonitoringInfoProvider(ComponentNames.USERMGMT, userManagementProvider);
                healthProvider = new AggregateMonitoringHealthProvider(new JPADataBaseConnectionHealthProvider(persistenceManagerUserMgmt, "userManagement"));
                withSingleton(new MonitoringStatisticsHandler(statsCollector,
                                                              informationProvider,
                                                              healthProvider
                ));
                withSingleton(new UserManagementStaticResourceHandler(new UserManagementClasspathStaticResourceLoader("")));
                withSingleton(new UserManagementHandler(userManagementRepository));
                withSingleton(new rSQEHomePageHandler(getContextUrl("cqm"), getContextUrl("nrm"), userManagementRepository));
            }
        };
    }

    private String getContextUrl(final String context) {
        return Iterables.find(newArrayList(userManagementConfig.getUrlConfig()), new Predicate<UrlConfig>() {
            @Override
            public boolean apply(UrlConfig urlConfig) {
                return context.equals(urlConfig.getContext());
            }
        }).getUrl();
    }
}
