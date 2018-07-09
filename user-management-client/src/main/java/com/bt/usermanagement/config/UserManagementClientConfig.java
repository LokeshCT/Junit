package com.bt.usermanagement.config;
import com.bt.rsqe.configuration.RestClientConfig;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.persistence.DatabaseConfig;
import com.bt.rsqe.security.RestAuthenticationFilterConfig;

public interface UserManagementClientConfig extends RestClientConfig {
    ApplicationConfig getApplicationConfig();
    DatabaseConfig getDatabaseConfig();
    RestAuthenticationFilterConfig getRestAuthenticationFilterConfig();
    UserManagementClientConfig getUserManagementClientConfig();
}
