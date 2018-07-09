package com.bt.cqm;

import com.bt.rsqe.CqmEnvironmentConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.persistence.BaseDatabaseConfigProvider;
import com.bt.rsqe.persistence.DatabaseConfig;

public class CqmDatabaseConfigProvider extends BaseDatabaseConfigProvider{

    private DatabaseConfig databaseConfig;

    public CqmDatabaseConfigProvider(String environment) {
        super(environment);
    }

    public CqmDatabaseConfigProvider(DatabaseConfig config, String environment) {
        super(environment);
        this.databaseConfig = config;
    }

    @Override
    public DatabaseConfig provide() {
        if (databaseConfig == null) {
            databaseConfig =
                ConfigurationProvider
                    .provide(CqmEnvironmentConfig.class, getEnvironment())
                    .getCqmConfig().getDatabaseConfig();
        }
        return databaseConfig;
    }
}
