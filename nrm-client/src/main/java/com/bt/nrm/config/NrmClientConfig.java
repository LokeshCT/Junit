package com.bt.nrm.config;

import com.bt.rsqe.configuration.RestClientConfig;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.persistence.DatabaseConfig;
import com.bt.rsqe.security.RestAuthenticationFilterConfig;

public interface NrmClientConfig extends RestClientConfig {
    ApplicationConfig getApplicationConfig();
    DatabaseConfig getDatabaseConfig();
    RestAuthenticationFilterConfig getRestAuthenticationFilterConfig();
    NrmClientConfig getNrmClientConfig();
}