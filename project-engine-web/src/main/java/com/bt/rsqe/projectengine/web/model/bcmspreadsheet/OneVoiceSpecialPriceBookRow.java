package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import static com.bt.rsqe.excel.ExcelUtil.*;

public class OneVoiceSpecialPriceBookRow extends BcmRow {

    public static final int DISCOUNT_CELL_INDEX = 7;
    public static final int SPECIAL_PRICEBOOK_CELL_INDEX = 0;
    public static final int ORIGINATING_COUNTRY_CELL_INDEX = 1;
    public static final int TERMINATING_COUNTRY_CELL_INDEX = 2;
    public static final int TERMINATION_TYPE_CELL_INDEX = 3;
    public static final int TARIFF_TYPE_CELL_INDEX = 4;
    private Row row;

    public OneVoiceSpecialPriceBookRow(Row row) {
        this.row = row;
    }

    public String getSpecialPriceBookName() {
        return row.getCell(SPECIAL_PRICEBOOK_CELL_INDEX).getStringCellValue();
    }

    public String getDiscount() {
        Cell discountCell = row.getCell(DISCOUNT_CELL_INDEX);
        if(isCellBlank(discountCell) || !isCellNumeric(discountCell)) {
            return "0";
        }

        return Double.toString(discountCell.getNumericCellValue());
    }

    public String getOriginatingCountry() {
        return row.getCell(ORIGINATING_COUNTRY_CELL_INDEX).getStringCellValue();
    }

    public String getTerminatingCountry() {
        return row.getCell(TERMINATING_COUNTRY_CELL_INDEX).getStringCellValue();
    }

    public String getTerminationType() {
        return row.getCell(TERMINATION_TYPE_CELL_INDEX).getStringCellValue();
    }

    public String getTariffType() {
        return row.getCell(TARIFF_TYPE_CELL_INDEX).getStringCellValue();
}
}
