package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;

import static com.bt.rsqe.excel.ExcelUtil.*;

public class BcmRow {

    protected boolean isCellValid(Cell cell) {
        if (cell == null || isCellNumeric(cell) || isCellBlank(cell)) {
            return true;
        }

        if (isCellString(cell)) {
            final RichTextString richStringCellValue = cell.getRichStringCellValue();
            if (richStringCellValue == null || StringUtils.isEmpty(cell.getStringCellValue())) {
                return true;
            }
        }

        return false;
    }
    protected void validateNumericCell(Cell cell, String column, Row row) {
        if (!isCellValid(cell)) {
            throw new InvalidExportDataException(String.format("sheet [%s], row [%s], column [%s], contains non numeric data",
                                                               row.getSheet().getSheetName(),
                                                               row.getRowNum() + 1,
                                                               column));
        }
    }
    protected BigDecimal getPercentageCellValue(int cellIndex, String columnName, Row row) {
        final Cell cell = row.getCell(cellIndex);
        validateNumericCell(cell, columnName,row);

        if (cell == null || isCellString(cell) || isCellBlank(cell)) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(Double.toString(cell.getNumericCellValue())).movePointRight(2);
    }

}
