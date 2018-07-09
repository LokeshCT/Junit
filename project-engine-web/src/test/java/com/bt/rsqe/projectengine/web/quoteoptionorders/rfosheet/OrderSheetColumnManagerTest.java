package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.google.common.base.Optional;
import org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class OrderSheetColumnManagerTest {
    private String projectId = "projectId";
    private String quoteOptionId = "quoteOptionId";
    private QuoteMigrationDetailsProvider migrationDetailsProvider;
    private OrderSheetColumnManager orderSheetColumnManager;

    @Before
    public void before() {
        migrationDetailsProvider = mock(QuoteMigrationDetailsProvider.class);
        orderSheetColumnManager = new OrderSheetColumnManager(projectId, quoteOptionId, migrationDetailsProvider);
    }

    @Test
    public void shouldReturnTrueForMigrationQuoteWithContractStartDateNotHidden() {
        when(migrationDetailsProvider.isMigrationQuote(projectId, quoteOptionId)).thenReturn(Optional.of(true));
        assertTrue(orderSheetColumnManager.isColumnVisible(OrderSheetMarshaller.Column.INITIAL_BILLING_START_DATE));
    }

    @Test
    public void shouldReturnFalseForNonMigrationQuoteWithContractStartDateNotHidden() {
        when(migrationDetailsProvider.isMigrationQuote(projectId, quoteOptionId)).thenReturn(Optional.of(false));
        assertFalse(orderSheetColumnManager.isColumnVisible(OrderSheetMarshaller.Column.INITIAL_BILLING_START_DATE));
    }

    @Test
    public void shouldReturnTrueForDefaultColumns() {
        assertTrue(orderSheetColumnManager.isColumnVisible(OrderSheetMarshaller.Column.PRODUCT_NAME));
    }

    @Test
    public void shouldReturnFalseForLineItemIdColumn() {
        assertFalse(orderSheetColumnManager.isColumnVisible(OrderSheetMarshaller.Column.LINE_ITEM_ID));
    }
}
