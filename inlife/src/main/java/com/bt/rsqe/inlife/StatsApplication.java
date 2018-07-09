package com.bt.rsqe.inlife;

import com.bt.rsqe.ComponentNames;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.container.ioc.RestResourceHandlerFactory;
import com.bt.rsqe.inlife.config.InlifeConfig;
import com.bt.rsqe.inlife.geo.locator.GeoLocator;
import com.bt.rsqe.inlife.geo.locator.btdirectory.BTDirectoryBasedGeoLocator;
import com.bt.rsqe.inlife.web.DashboardResourceHandler;
import com.bt.rsqe.inlife.web.InlifeResourceHandler;
import com.bt.rsqe.inlife.web.InlifeStaticResourceHandler;
import com.bt.rsqe.inlife.web.InlifeStaticResourceLoader;
import com.bt.rsqe.inlife.web.QuoteStatsResourceHandler;
import com.bt.rsqe.inlife.web.TransactionTargetResourceHandler;
import com.bt.rsqe.inlife.web.WebMetricsResourceHandler;
import com.bt.rsqe.mis.client.QuoteItemStatsResource;
import com.bt.rsqe.mis.client.QuoteStatsResource;
import com.bt.rsqe.mis.client.TransactionTargetResource;
import com.bt.rsqe.mis.client.WebMetricsResource;
import com.bt.rsqe.monitoring.AggregateMonitoringHealthProvider;
import com.bt.rsqe.monitoring.AlwaysGreenMonitoringHealthProvider;
import com.bt.rsqe.monitoring.DefaultMonitoringInfoProvider;
import com.bt.rsqe.monitoring.MonitoringStatisticsHandler;
import com.bt.rsqe.monitoring.StatisticsCollector;
import com.bt.rsqe.monitoring.StatsMode;
import com.bt.rsqe.web.FaviconHandler;
import com.bt.rsqe.web.Presenter;

import java.net.URISyntaxException;

public class StatsApplication  extends Application {

    private InlifeConfig configuration;
    private StatisticsCollector statisticsCollector;

    public StatsApplication(InlifeConfig config) {
        super(config.getApplicationConfig(InlifeConfig.STATS_APPLICATION_CONFIG), ComponentNames.STATS);
        this.configuration = config;
        statisticsCollector = new StatisticsCollector(ComponentNames.STATS, StatsMode.OFF.getMode());
        applicationContainerInstance().registerBeforeHandler(statisticsCollector);
        applicationContainerInstance().registerAfterHandler(statisticsCollector);
    }

    @Override
    protected ResourceHandlerFactory createResourceHandlerFactory() {
        return new RestResourceHandlerFactory() {
            {
                QuoteStatsResource quoteStatsResource = new QuoteStatsResource(configuration.getMisReportingConfig());
                QuoteItemStatsResource quoteItemStatsResource = new QuoteItemStatsResource(configuration.getMisReportingConfig());
                WebMetricsResource webMetricsResource = new WebMetricsResource(configuration.getMisReportingConfig());
                TransactionTargetResource transactionTargetResource = new TransactionTargetResource(configuration.getMisReportingConfig());
                QuoteStatsResourceHandler quoteStatsResourceHandler = new QuoteStatsResourceHandler(quoteStatsResource, quoteItemStatsResource);
                DashboardResourceHandler dashboardResourceHandler = new DashboardResourceHandler(new Presenter());
                Presenter presenter = new Presenter(new String[]{"com/bt/rsqe/inlife/web/include.ftl"});
                GeoLocator geoLocator;
                try {
                    geoLocator = new BTDirectoryBasedGeoLocator(configuration.getServiceEndPointConfig(InlifeConfig.BT_DIRECTORY_END_POINT));
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }

                withSingleton(quoteStatsResourceHandler);
                withSingleton(dashboardResourceHandler);
                withSingleton(new WebMetricsResourceHandler(webMetricsResource, presenter, geoLocator));
                withSingleton(new TransactionTargetResourceHandler(transactionTargetResource, presenter));
                withSingleton(new FaviconHandler());
                withSingleton(new InlifeResourceHandler(
                    presenter, configuration, null, configuration.getStatsLogsBasePath()));
                withSingleton(new InlifeStaticResourceHandler(new InlifeStaticResourceLoader()));
                informationProvider =  new DefaultMonitoringInfoProvider(ComponentNames.STATS);
                healthProvider = new AggregateMonitoringHealthProvider( new AlwaysGreenMonitoringHealthProvider("On-line"));
                withSingleton(new MonitoringStatisticsHandler(statisticsCollector,
                                                              informationProvider,
                                                             healthProvider
                                                              ));
            }
        };
    }
}
