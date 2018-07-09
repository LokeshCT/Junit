package com.bt.rsqe.inlife.config;

import com.bt.rsqe.ape.client.config.ApeFacadeConfig;
import com.bt.rsqe.configuration.RestClientConfig;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.customerinventory.client.CustomerInventoryClientConfig;
import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.dataarchiving.DataArchivingConfig;
import com.bt.rsqe.mis.client.MisReportingConfig;
import com.bt.rsqe.persistence.DatabaseConfig;
import com.bt.rsqe.pmr.client.PmrClientConfig;
import com.bt.rsqe.ppsr.config.PpsrFacadeConfig;
import com.bt.rsqe.pricing.config.PricingFacadeClientConfig;
import com.bt.rsqe.projectengine.configuration.ProjectEngineClientConfig;
import com.bt.rsqe.taskscheduler.TaskSchedulerConfig;

public interface InlifeConfig {
    ApplicationConfig getApplicationConfig(String name);
    DatabaseConfig getDatabaseConfig(String database);
    RestClientConfig.RestAuthenticationClientConfig getRestAuthenticationClientConfig();
    MonitoredApplicationsConfig getMonitoredApplications();
    String getStoredBomsPath();
    String getGrabstateArchivesPath();
    String getLogsBasePath();
    String getStatsLogsBasePath();
    String getApacheLogsBasePath();
    MisReportingConfig getMisReportingConfig();
    CustomerInventoryClientConfig getCustomerInventoryClient();
    PmrClientConfig getPmrClientConfig();
    ProjectEngineClientConfig getProjectEngineClientConfig();
    PricingFacadeClientConfig getPricingFacadeClientConfig();
    DataArchivingConfig getDataArchivingConfig();
    TaskSchedulerConfig getTaskSchedulerConfig();
    ServiceEndPointConfig getServiceEndPointConfig(String id);
    ExpedioFacadeConfig getExpedioFacadeConfig();
    PpsrFacadeConfig getPpsrFacadeConfig();
    ApeFacadeConfig getApeFacadeConfig();
    String getStatsMode();

    public static final String INLIFE_APPLICATION_CONFIG = "inlifeApplication";
    public static final String STATS_APPLICATION_CONFIG = "statsApplication";
    public static final String BT_DIRECTORY_END_POINT = "btDirectorySearch";
}
