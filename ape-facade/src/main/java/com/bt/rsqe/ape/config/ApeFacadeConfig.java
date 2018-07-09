package com.bt.rsqe.ape.config;

import com.bt.cqm.config.CQMClientConfig;
import com.bt.rsqe.config.EmailServiceConfig;
import com.bt.rsqe.configuration.BatchProcessorConfig;
import com.bt.rsqe.configuration.RestClientConfig;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.persistence.DatabaseConfig;
import com.bt.rsqe.security.RestAuthenticationFilterConfig;

public interface ApeFacadeConfig {
    DatabaseConfig getDatabaseConfig(String database);
    ApplicationConfig getApplicationConfig();
    RestAuthenticationFilterConfig getRestAuthenticationFilterConfig();
    RestClientConfig.RestAuthenticationClientConfig getRestAuthenticationClientConfig();
    CallbackEndpointConfig getCallbackEndpointConfig(String name);
    ExpedioFacadeConfig getExpedioFacadeConfig();
    EmailServiceConfig getEmailServiceConfig();
    ApeServiceEndPointConfig getApeServiceEndPointConfig(String name);
    String getStatsMode();
    SupplierCheckConfig getSupplierCheckConfig();
    CQMClientConfig getCQMClientConfig();
    BatchProcessorConfig getBatchProcessorConfig();
}
