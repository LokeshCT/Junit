package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class XlsFileUtils {

    public static String getStringCellValue(Sheet sheet, int rowIndex, int cellIndex) {
        Row row = sheet.getRow(rowIndex);
        if(row == null) {
            return "";
        }

        Cell cell = row.getCell(cellIndex);
        return cell == null ? "" : cell.toString();
    }

    public static int findRowIndexForText(Sheet sheet, int columnIndex, String text) {
        int lastRowIndex = sheet.getLastRowNum();
        for (int rowIndex = 0; rowIndex <= lastRowIndex; rowIndex++) {
            if( text.equals(getStringCellValue(sheet, rowIndex, columnIndex))) {
                return rowIndex;
            }
        }
        throw new PricingSheetExportException(String.format("Unable to find '%s'", text));
    }

}
