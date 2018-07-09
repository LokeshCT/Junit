package com.bt.rsqe;

import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.persistence.BaseDatabaseConfigProvider;
import com.bt.rsqe.persistence.DatabaseConfig;

public class ApeFacadeDatabaseConfigProvider extends BaseDatabaseConfigProvider {
    private DatabaseConfig databaseConfig;

    public ApeFacadeDatabaseConfigProvider(DatabaseConfig databaseConfig, String environment) {
        super(environment);
        this.databaseConfig = databaseConfig;
    }

    @Override
    public DatabaseConfig provide() {
        if (databaseConfig == null) {
            databaseConfig =
                ConfigurationProvider
                    .provide(ApeFacadeEnvironmentTestConfig.class, getEnvironment())
                        .getApeFacadeConfig()
                        .getDatabaseConfig("ApeFacadeDatabase");
        }

        return databaseConfig;
    }
}
