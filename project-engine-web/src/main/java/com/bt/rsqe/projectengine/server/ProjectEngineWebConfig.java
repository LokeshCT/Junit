package com.bt.rsqe.projectengine.server;

import com.bt.rsqe.config.StatsClientConfig;
import com.bt.rsqe.configuration.BatchProcessorConfig;
import com.bt.rsqe.configuration.ProductConfiguratorConfig;
import com.bt.rsqe.configuration.RestClientConfig;
import com.bt.rsqe.configuration.SharePointUrlConfig;
import com.bt.rsqe.configuration.SqeAppUrlConfig;
import com.bt.rsqe.configuration.UrlConfig;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.customerinventory.client.CustomerInventoryClientConfig;
import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.emppal.attachmentresource.client.EmpPalFacadeClientConfig;
import com.bt.rsqe.inlife.client.InlifeClientConfig;
import com.bt.rsqe.pc.client.ConfiguratorClientConfig;
import com.bt.rsqe.pmr.client.PmrClientConfig;
import com.bt.rsqe.pricing.config.PricingFacadeClientConfig;
import com.bt.rsqe.projectengine.configuration.ProjectEngineClientConfig;
import com.bt.rsqe.projectengine.web.CustomerProjectResourceHandler;
import com.bt.rsqe.security.RestAuthenticationFilterConfig;
import com.bt.rsqe.session.client.SessionServiceClientConfig;
import com.bt.rsqe.tpe.config.TpeConfig;

public interface ProjectEngineWebConfig extends RestClientConfig {
    ApplicationConfig getApplicationConfig();
    CustomerProjectResourceHandler.CustomerProjectResourceHandlerConfig getCustomerProjectResourceHandlerConfig();
    ProductConfiguratorConfig getProductConfiguratorConfig();
    UrlConfig[] getUrls();
    RestAuthenticationFilterConfig getRestAuthenticationFilterConfig();
    CustomerInventoryClientConfig getCustomerInventoryConfig();
    ProjectEngineClientConfig getProjectEngineClientConfig();
    ExpedioFacadeConfig getExpedioFacadeConfig();
    PmrClientConfig getPmrClientConfig();
    SessionServiceClientConfig getSessionServiceClientConfig();
    PricingFacadeClientConfig getPricingFacadeClientConfig();
    TpeConfig getTpeConfig();
    InlifeClientConfig getInlifeClientConfig();
    CustomerInventoryClientConfig getCustomerInventoryClient();
    EmpPalFacadeClientConfig getEmpPalFacadeClientConfig();
    SharePointUrlConfig getSharePointUrlConfig();
    ConfiguratorClientConfig getConfiguratorClientConfig();
    UrlConfig getUrl(String id);
    StatsClientConfig getStatsClientConfig();
    String getStatsMode();
    BatchProcessorConfig getBatchProcessorConfig();
    SqeAppUrlConfig getSqeAppUrlConfig();

    static final String SUBMIT_WEB_METRICS_URI = "submitWebMetricsUri";
    static final String HELP_LINK_URI = "helpLinkUri";
}
