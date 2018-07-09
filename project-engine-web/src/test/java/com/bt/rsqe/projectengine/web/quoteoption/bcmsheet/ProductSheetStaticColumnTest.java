package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import org.junit.Test;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class ProductSheetStaticColumnTest {

    @Test
    public void shouldReturnCorrectColumnBasedOnColumnName() {
        ProductSheetStaticColumn priceBookColumn = ProductSheetStaticColumn.getColumn("Price book version");
        ProductSheetStaticColumn RecPTPColumn = ProductSheetStaticColumn.getColumn("Monthly Recurring PTP price");
        assertThat(priceBookColumn.columnIndex, is(10));
        assertThat(priceBookColumn.retrieveValueFrom, is("PriceBook"));
        assertThat(priceBookColumn.type, is("common"));
        assertThat(priceBookColumn.visible, is(true));

        assertThat(RecPTPColumn.columnIndex, is(20));
        assertThat(RecPTPColumn.retrieveValueFrom, is("PriceLine.RecurringPTPPrice"));
        assertThat(RecPTPColumn.type, is("price"));
        assertThat(RecPTPColumn.visible, is(true));
    }

    @Test
    public void shouldReturnCorrectColumnBasedOnColumnIndex() {
        ProductSheetStaticColumn priceBookColumn = ProductSheetStaticColumn.getColumn(10);
        ProductSheetStaticColumn RecPTPColumn = ProductSheetStaticColumn.getColumn(20);
        assertThat(priceBookColumn.columnIndex, is(10));
        assertThat(priceBookColumn.retrieveValueFrom, is("PriceBook"));
        assertThat(priceBookColumn.type, is("common"));
        assertThat(priceBookColumn.visible, is(true));
        assertThat(priceBookColumn.columnName, is("Price book version"));

        assertThat(RecPTPColumn.columnIndex, is(20));
        assertThat(RecPTPColumn.retrieveValueFrom, is("PriceLine.RecurringPTPPrice"));
        assertThat(RecPTPColumn.type, is("price"));
        assertThat(RecPTPColumn.visible, is(true));
        assertThat(RecPTPColumn.columnName, is("Monthly Recurring PTP price"));
    }

    @Test
    public void shouldReturnNullValueWhenColumnIndexNotPresent() {
        ProductSheetStaticColumn column = ProductSheetStaticColumn.getColumn(100);
        assertNull(column);
    }

    @Test
    public void shouldReturnNullValueWhenColumnNameNotPresent() {
        ProductSheetStaticColumn column = ProductSheetStaticColumn.getColumn("Test");
        assertNull(column);
    }

    @Test
    public void validateAllColumnIndex() {
        assertThat(ProductSheetStaticColumn.SITE_ID.columnIndex, is(0));
        assertThat(ProductSheetStaticColumn.SITE.columnIndex, is(1));
        assertThat(ProductSheetStaticColumn.COUNTRY.columnIndex, is(2));
        assertThat(ProductSheetStaticColumn.CITY.columnIndex, is(3));
        assertThat(ProductSheetStaticColumn.PRODUCT_CATEGORY_NAME.columnIndex, is(4));
        assertThat(ProductSheetStaticColumn.ROOT_PRODUCT_ID.columnIndex, is(5));
        assertThat(ProductSheetStaticColumn.ROOT_PRODUCT_NAME.columnIndex, is(6));
        assertThat(ProductSheetStaticColumn.LINE_ITEM_ACTION.columnIndex, is(7));
        assertThat(ProductSheetStaticColumn.LINE_ITEM_ORDER_STATUS.columnIndex, is(8));
        assertThat(ProductSheetStaticColumn.CONTRACT_TERM.columnIndex, is(9));
        assertThat(ProductSheetStaticColumn.PRICE_BOOK_VERSION.columnIndex, is(10));
        assertThat(ProductSheetStaticColumn.PRIMARY_TARIFF_ZONE.columnIndex, is(11));
        assertThat(ProductSheetStaticColumn.PRICE_DESCRIPTION.columnIndex, is(12));
        assertThat(ProductSheetStaticColumn.VISIBILITY.columnIndex, is(13));
        assertThat(ProductSheetStaticColumn.ONE_TIME_PRICE_LINE_ID.columnIndex, is(14));
        assertThat(ProductSheetStaticColumn.ONE_TIME_EUP_PRICE.columnIndex, is(15));
        assertThat(ProductSheetStaticColumn.ONE_TIME_PTP_PRICE.columnIndex, is(16));
        assertThat(ProductSheetStaticColumn.ONE_TIME_DISCOUNT.columnIndex, is(17));
        assertThat(ProductSheetStaticColumn.MONTHLY_RECURRING_PRICE_LINE_ID.columnIndex, is(18));
    }
}
