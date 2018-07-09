package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.google.common.base.Optional;

public class OrderSheetColumnManager {
    private String projectId;
    private String quoteOptionId;
    private QuoteMigrationDetailsProvider migrationDetailsProvider;

    public OrderSheetColumnManager(String projectId, String quoteOptionId, QuoteMigrationDetailsProvider migrationDetailsProvider) {
        this.projectId = projectId;
        this.quoteOptionId = quoteOptionId;
        this.migrationDetailsProvider = migrationDetailsProvider;
    }

    public boolean isColumnVisible(OrderSheetMarshaller.Column column) {
        switch (column) {
            case INITIAL_BILLING_START_DATE:
                return isMigrationQuote();
            case LINE_ITEM_ID:
            case BILLING_ID_VALUE:
            case SUBLOCATION_NAME_VALUE:
            case ROOM_VALUE:
            case FLOOR_VALUE:
                return false;
            default:
                return true;
        }
    }

    private boolean isMigrationQuote() {
        Optional<Boolean> migrationQuote = migrationDetailsProvider.isMigrationQuote(projectId, quoteOptionId);
        boolean isMigrationQuote = migrationQuote.isPresent() ? migrationQuote.get() : false;
        return isMigrationQuote;
    }

}
