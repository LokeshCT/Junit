package com.bt.usermanagement.config;

import com.bt.rsqe.configuration.UrlConfig;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.persistence.DatabaseConfig;
import com.bt.rsqe.security.RestAuthenticationFilterConfig;
import com.bt.usermanagement.config.UserManagementClientConfig;

public interface UserManagementConfig {
    ApplicationConfig getApplicationConfig();
    DatabaseConfig getDatabaseConfig();
    RestAuthenticationFilterConfig getRestAuthenticationFilterConfig();
    UserManagementClientConfig getUserManagementClientConfig();
    UrlConfig[] getUrlConfig();
}
