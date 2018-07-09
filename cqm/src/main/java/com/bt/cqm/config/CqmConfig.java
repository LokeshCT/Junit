package com.bt.cqm.config;

import com.bt.cqm.config.dsl.DslCheckerSharePointPathConfig;
import com.bt.rsqe.ape.client.config.ApeFacadeConfig;
import com.bt.rsqe.config.EmailServiceConfig;
import com.bt.rsqe.config.StatsClientConfig;
import com.bt.rsqe.configuration.BatchProcessorConfig;
import com.bt.rsqe.configuration.UrlConfig;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.customerinventory.client.CustomerInventoryClientConfig;
import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.emppal.attachmentresource.client.EmpPalFacadeClientConfig;
import com.bt.rsqe.nad.config.NadFacadeClientConfig;
import com.bt.rsqe.persistence.DatabaseConfig;
import com.bt.rsqe.ppsr.config.PpsrFacadeConfig;
import com.bt.rsqe.projectengine.configuration.ProjectEngineClientConfig;
import com.bt.rsqe.security.RestAuthenticationFilterConfig;
import com.bt.rsqe.soap.WebServiceInterfaceConfig;
import com.bt.rsqe.sqefacade.SqeIvpnFacadeConfig;

public interface CqmConfig {
    ApplicationConfig getApplicationConfig();
    DatabaseConfig getDatabaseConfig();
    WebServiceInterfaceConfig getWebServiceInterfaceConfig();
    RestAuthenticationFilterConfig getRestAuthenticationFilterConfig();
    BatchProcessorConfig getBatchProcessorConfig();
    NadFacadeClientConfig getNadFacadeClientConfig();
    ExpedioFacadeConfig getExpedioFacadeConfig();
    ApeFacadeConfig getApeFacadeConfig();
    CustomerInventoryClientConfig getCustomerInventoryClientConfig();
    BundlingAppConfig getBundlingAppConfig();
    SqeAppConfig getSqeAppConfig();
    PpsrFacadeConfig getPpsrFacadeConfig();
    EmpPalFacadeClientConfig getEmpPalFacadeClientConfig();
    EmailServiceConfig getEmailServiceConfig();
    StatsClientConfig getStatsClient();
    ReportAppConfig getReportAppConfig();
    ProjectEngineClientConfig getProjectEngineClientConfig();
    DslCheckerSharePointPathConfig getDslCheckerSharePointPathConfig();
    CQMClientConfig  getCQMClientConfig();
    UrlConfig[] getUrlConfig();
    SqeIvpnFacadeConfig getSqeIvpnFacadeConfig();
}

