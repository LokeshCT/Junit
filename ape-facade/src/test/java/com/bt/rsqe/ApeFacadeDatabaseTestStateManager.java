package com.bt.rsqe;

import com.bt.rsqe.persistence.DatabaseConfig;
import com.bt.rsqe.persistence.DatabaseTestStateManager;

public class ApeFacadeDatabaseTestStateManager extends DatabaseTestStateManager {
    public ApeFacadeDatabaseTestStateManager(DatabaseConfig config, String environment) {
        super(new ApeFacadeDatabaseConfigProvider(config, environment));
    }

    public void seedDataForModify() throws Exception {
        beginClean();
        cleanInsert(this.getClass().getResource("/database/dbunit/ape/facade/"));
        end();
    }
}
