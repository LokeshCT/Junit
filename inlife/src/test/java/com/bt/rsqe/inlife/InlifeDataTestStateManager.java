package com.bt.rsqe.inlife;

import com.bt.rsqe.persistence.DatabaseConfig;
import com.bt.rsqe.persistence.DatabaseTestStateManager;

public class InlifeDataTestStateManager extends DatabaseTestStateManager {
    public InlifeDataTestStateManager(DatabaseConfig config, String environment) throws Exception {
        super(new InlifeDatabaseConfigProvider(config, environment));
        begin();
    }
}
