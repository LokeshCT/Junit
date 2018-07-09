package com.bt.rsqe.inlife;

import com.bt.rsqe.persistence.BaseDatabaseConfigProvider;
import com.bt.rsqe.persistence.DatabaseConfig;

public class InlifeDatabaseConfigProvider extends BaseDatabaseConfigProvider {
    private DatabaseConfig databaseConfig;

    public InlifeDatabaseConfigProvider(DatabaseConfig databaseConfig, String environment) {
        super(environment);
        this.databaseConfig = databaseConfig;
    }

    @Override
    public DatabaseConfig provide() {
        return databaseConfig;
    }
}