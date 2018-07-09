package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import static com.bt.rsqe.excel.ExcelUtil.*;

public class ProductInfoSheetRow extends BcmRow {

      public static int MINIMUM_REVENUE_COMMITMENT = 1;
      public static int PRODUCT_CATEGORY_NAME = 0;
    private Row row;

    public ProductInfoSheetRow(Row row) {
        this.row = row;
    }

    public String getMinimumRevenueCommitment(){
        final Cell cell = row.getCell(MINIMUM_REVENUE_COMMITMENT);
        validateNumericCell(cell, "Monthly Revenue Commitment",row);
         if (cell == null || isCellString(cell) || isCellBlank(cell)) {
            return "";
        }
        return String.valueOf(cell.getNumericCellValue());
    }

    public String getProductCategoryName(){
        return row.getCell(PRODUCT_CATEGORY_NAME).getStringCellValue();
    }
}
