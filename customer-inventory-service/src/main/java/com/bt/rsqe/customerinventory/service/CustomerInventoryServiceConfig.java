package com.bt.rsqe.customerinventory.service;

import com.bt.rsqe.ape.client.config.ApeFacadeConfig;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.customerinventory.client.CustomerInventoryClientConfig;
import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.persistence.DatabaseConfig;
import com.bt.rsqe.pmr.client.PmrClientConfig;
import com.bt.rsqe.projectengine.configuration.ProjectEngineClientConfig;
import com.bt.rsqe.security.RestAuthenticationFilterConfig;
import com.bt.rsqe.sqefacade.SqeIvpnFacadeConfig;
import com.bt.rsqe.tpe.config.TpeConfig;

public interface CustomerInventoryServiceConfig {
    ApplicationConfig getApplicationConfig();
    RestAuthenticationFilterConfig getRestAuthenticationFilterConfig();
    DatabaseConfig getDatabaseConfig(String s);
    PmrClientConfig getPmrClientConfig();
    ProjectEngineClientConfig getProjectEngineClientConfig();
    SqeIvpnFacadeConfig getSqeIvpnFacadeConfig();
    ExpedioFacadeConfig getExpedioFacadeConfig();
    String getStatsMode();
    ApeFacadeConfig getApeFacadeConfig();
    TpeConfig getTpeConfig();
    CustomerInventoryClientConfig getCustomerInventoryClientConfig();

}
