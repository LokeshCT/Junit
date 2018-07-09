package com.bt.cqm;

import com.bt.rsqe.persistence.DatabaseConfig;
import com.bt.rsqe.persistence.DatabaseTestStateManager;

public class CqmDatabaseTestStateManager extends DatabaseTestStateManager {

    public CqmDatabaseTestStateManager(DatabaseConfig config, String environment) {
        super(new CqmDatabaseConfigProvider(config, environment));
    }

    public void seedData(String path) throws Exception {
        super.cleanInsert(this.getClass().getResource(path));
    }
}
