package com.bt.rsqe.matchers.excel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelTemplateParser {
    private static final Pattern PATTERN = Pattern.compile("\\$\\{(.*)\\}");
    private final Workbook template;
    private final ExcelTemplateFields templateFields;

    public ExcelTemplateParser(Workbook template) {
        this.template = template;
        templateFields = new ExcelTemplateFields();
    }

    public ExcelTemplateFields parse() {
        int sheets = template.getNumberOfSheets();
        for (int sheetIndex = 0; sheetIndex < sheets; sheetIndex++) {
            ExcelTemplateSheetFields sheetFields = parseSheet(template.getSheetAt(sheetIndex));
            templateFields.add(sheetIndex, sheetFields);
        }
        return templateFields;
    }

    private ExcelTemplateSheetFields parseSheet(Sheet sheet) {
        ExcelTemplateSheetFields sheetFields = new ExcelTemplateSheetFields();
        Iterator<Row> rowIterator = sheet.rowIterator();
        int countOfJxlLoops = 0;
        while (rowIterator.hasNext()) {
            HSSFRow row = (HSSFRow) rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                HSSFCell cell = (HSSFCell) cellIterator.next();
                if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    String cellValue = cell.getStringCellValue();
                    if (isJXLSLoopConstruct(cellValue)) {
                        countOfJxlLoops++;
                    }
                    Matcher matcher = PATTERN.matcher(cellValue);
                    if (matcher.find()) {
                        String fieldName = matcher.group(1);
                        sheetFields.add(new ExcelTemplateField(fieldName, row.getRowNum() - countOfJxlLoops , cell.getColumnIndex()));
                    }
                }
            }
        }
        return sheetFields;
    }

    private boolean isJXLSLoopConstruct(String cellValue) {
        return cellValue.contains("jx:forEach");
    }
}
