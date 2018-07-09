package com.bt.nrm.config;

import com.bt.pms.config.PMSClientConfig;
import com.bt.rsqe.config.EmailServiceConfig;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.persistence.DatabaseConfig;
import com.bt.rsqe.security.RestAuthenticationFilterConfig;
import com.bt.usermanagement.config.UserManagementClientConfig;

public interface NrmConfig {
    ApplicationConfig getApplicationConfig();
    DatabaseConfig getDatabaseConfig();
    RestAuthenticationFilterConfig getRestAuthenticationFilterConfig();
    UserManagementClientConfig getUserManagementClientConfig();
    PMSClientConfig getPMSClientConfig();
    EmailServiceConfig getEmailServiceConfig();
}

