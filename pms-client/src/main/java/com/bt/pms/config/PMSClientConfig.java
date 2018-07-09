package com.bt.pms.config;

import com.bt.rsqe.configuration.RestClientConfig;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.persistence.DatabaseConfig;
import com.bt.rsqe.security.RestAuthenticationFilterConfig;

/**
 * Created by 608143048 on 11/12/2015.
 */
public interface PMSClientConfig extends RestClientConfig{
    ApplicationConfig getApplicationConfig();
    DatabaseConfig getDatabaseConfig();
    RestAuthenticationFilterConfig getRestAuthenticationFilterConfig();
    PMSClientConfig getPMSClientConfig();
}
