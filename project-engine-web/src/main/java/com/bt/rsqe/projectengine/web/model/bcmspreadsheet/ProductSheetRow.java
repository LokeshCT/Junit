package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import com.bt.rsqe.Percentage;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.ProductSheetStaticColumn;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class ProductSheetRow extends BcmRow {

    private Row row;

    public ProductSheetRow(Row row) {
        this.row = row;
    }

    public String oneTimePriceLineId() {
        return getPriceLineIdFor(ProductSheetStaticColumn.ONE_TIME_PRICE_LINE_ID);
    }

    private String getPriceLineIdFor(ProductSheetStaticColumn column) {
        final Cell priceLineIdCell = row.getCell(column.columnIndex);
        return priceLineIdCell.getStringCellValue();
    }

    public String monthlyPriceLineId() {
        return getPriceLineIdFor(ProductSheetStaticColumn.MONTHLY_RECURRING_PRICE_LINE_ID);
    }

    public Percentage getOneTimeDiscount() {
        ProductSheetStaticColumn oneTimeDiscount = ProductSheetStaticColumn.ONE_TIME_DISCOUNT;
        return Percentage.from(getPercentageCellValue(oneTimeDiscount.columnIndex,oneTimeDiscount.columnName,row));
    }

    public Percentage getMonthlyDiscount() {
        ProductSheetStaticColumn monthlyRecurringDiscount = ProductSheetStaticColumn.MONTHLY_RECURRING_DISCOUNT;
        return Percentage.from(getPercentageCellValue(monthlyRecurringDiscount.columnIndex,monthlyRecurringDiscount.columnName,row));
    }

}
