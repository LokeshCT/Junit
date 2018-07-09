package com.bt.rsqe.matchers.excel;

import com.bt.rsqe.excel.ExcelUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExcelTemplateSheetFields {
    public static final String DATE_PATTERN = "dd/MM/yy";

    private Multimap<String, ExcelTemplateField> fields = HashMultimap.create();

    public void add(ExcelTemplateField field) {
        fields.put(field.getName(), field);
    }

    public List<ExcelCellMatchFailure> matchValueOrValues(Sheet actualSheet, String fieldName, String expected, int rowOffset) {
        List<ExcelCellMatchFailure> failures = new ArrayList<ExcelCellMatchFailure>();
        // fieldName may be used multiple times on a sheet, so we have to check each have same value
        Collection<ExcelTemplateField> templateFields = fields.get(fieldName);

        if (templateFields == null || templateFields.isEmpty()) {
            final String error = String.format("Field '%s' does not exist in template for sheet '%s'!", fieldName, actualSheet.getSheetName());
            throw new IllegalArgumentException(error);
        }

        for (ExcelTemplateField templateField : templateFields) {
            String cellValueWithoutOffset = getCellValueWithOffset(actualSheet, templateField, 0);
            String cellValueWithOffset = getCellValueWithOffset(actualSheet, templateField, rowOffset);

            if (expected.trim().equals("") && (!cellValueWithoutOffset.trim().equals("") && !cellValueWithOffset.trim().equals(""))) {
                failures.add(new ExcelCellMatchFailure(templateField, cellValueWithOffset));
            } else if (!cellValueWithoutOffset.contains(expected) && !cellValueWithOffset.contains(expected)) {
                failures.add(new ExcelCellMatchFailure(templateField, cellValueWithOffset));
            }
        }
        return failures;
    }

    public List<ExcelCellMatchFailure> matchValueOrValues(Sheet sheet, int row, int column, String expected) {
        List<ExcelCellMatchFailure> failures = new ArrayList<ExcelCellMatchFailure>();

        if(sheet.getRow(row) == null || sheet.getRow(row).getCell(column) == null) {
            failures.add(new ExcelCellMatchFailure(new ExcelTemplateField(String.format("[%s,%s]", row, column), row, column), " NOT FOUND"));
            return failures;
        }
        
        String cellValue = sheet.getRow(row).getCell(column).toString();
        if (!cellValue.equals(expected)) {
            failures.add(new ExcelCellMatchFailure(new ExcelTemplateField(String.format("[%s,%s]", row, column), row, column), cellValue));
        }
        return failures;
    }

    public boolean matchRowValues(Sheet actualSheet, String[] fieldNames, String[] expected, ColumnType[] types) {
        int totalFields = fieldNames.length;
        int lastRowNum = actualSheet.getLastRowNum();

        for (int row = 0; row <= lastRowNum; row++) {
            int columnsThatMatch = 0;
            int fieldIndex = 0;
            for (String fieldName : fieldNames) {
                List<ExcelTemplateField> templateFields = new ArrayList<ExcelTemplateField>(fields.get(fieldName));
                if (templateFields.isEmpty()) {
                    throw new IllegalStateException(String.format("No values in sheet for fieldName [%s]!", fieldName));
                }
                if (templateFields.size() > 1) {
                    throw new IllegalStateException(String.format("fieldName [%s] is not unique in sheet! Must be unique when matching rows!", fieldName));
                }
                ExcelTemplateField field = templateFields.get(0);
                String expectedValue = expected[fieldIndex];
                String actualValue;

                if (ColumnType.DATE == types[fieldIndex]) {
                    actualValue = cellDateValue(actualSheet, field, row);
                } else {
                    actualValue = getCellValueWithOffset(actualSheet, field, row);
                }

                    if (areEqual(expectedValue, actualValue)) {
                    columnsThatMatch++;
                } else {
                    break;
                }
                fieldIndex++;
            }
            if (columnsThatMatch == totalFields) {
                return true;
            }
        }
        return false;
    }

    private boolean areEqual(String expected, String actual) {
        if(expected == null) {
            return true;
        }

        expected = expected.trim();
        actual = actual.trim();
        boolean result = expected.equals(actual);
        if (!result) {
            try {
                BigDecimal expectedValue = new BigDecimal(expected).setScale(7, BigDecimal.ROUND_HALF_UP);
                BigDecimal actualValue = new BigDecimal(actual).setScale(7, BigDecimal.ROUND_HALF_UP);
                result = expectedValue.equals(actualValue);
            } catch (NumberFormatException e) {
                // fail
            }
        }
        return result;
    }

    private String getCellValueWithOffset(Sheet actualSheet, ExcelTemplateField templateField, int rowOffset) {
        String cellValue = "[null]";
        Row row = actualSheet.getRow(templateField.getRowIndex() + rowOffset);
        if (row != null) {
            Cell cell = row.getCell(templateField.getColumnIndex());
            if (cell != null) {
                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_BOOLEAN:
                        cellValue = String.valueOf(cell.getBooleanCellValue());
                        break;
                    case Cell.CELL_TYPE_ERROR:
                        cellValue = "[error]";
                        break;
                    case Cell.CELL_TYPE_FORMULA:
                        cellValue = "[formula]";
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        cellValue = NumberToTextConverter.toText(cell.getNumericCellValue());
                        break;
                    case Cell.CELL_TYPE_STRING:
                        cellValue = cell.getStringCellValue();
                        break;
                    default:
                        cellValue = "";
                }
            }
        }
        return cellValue;
    }

    private String cellDateValue(Sheet actualSheet, ExcelTemplateField templateField, int rowOffset) {
        String cellValue = "[null]";
        Row row = actualSheet.getRow(templateField.getRowIndex() + rowOffset);
        if (row != null) {
            DateTime date = ExcelUtil.getDateValueAtCell(row, templateField.getColumnIndex());
            cellValue = date.toString(DATE_PATTERN);
        }
        return cellValue;
    }
}
