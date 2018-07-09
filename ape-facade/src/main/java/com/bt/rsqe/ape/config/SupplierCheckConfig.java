package com.bt.rsqe.ape.config;

/**
 * Created by 605783162 on 21/08/2015.
 */
public interface SupplierCheckConfig {
    SchedulerConfig getSchedulerConfig();
    ServiceConfig getServiceConfig();
    CallbackUriConfig getCallbackUriConfig(String name);
    EndpointUriConfig getEndpointUriConfig();
    DataExpiryConfig getDataExpiryConfig();
    RedirectUriConfig getRedirectUriConfig(String name);
}