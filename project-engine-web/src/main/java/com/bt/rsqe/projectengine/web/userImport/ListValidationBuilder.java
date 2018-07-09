package com.bt.rsqe.projectengine.web.userImport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public class ListValidationBuilder {

    private XSSFWorkbook workbook;
    public static final String HIDDEN_SHEET = "Hidden";
    public static final String FORMULA_TEMPLATE = "sheetName!$startIndex$rowNumber:$finishIndex$rowNumber";
    private int index = 0;
    private String DEFAULT_START_INDEX = "A";

    public ListValidationBuilder(XSSFWorkbook workbook) {
        this.workbook = workbook;
    }

    public DataValidation buildHiddenCell(List<String> listOfValues, int index, Row row) {
        XSSFSheet sheet = workbook.getSheet(HIDDEN_SHEET);
        if (null == sheet) {
            sheet = workbook.createSheet(HIDDEN_SHEET);
            workbook.setSheetHidden(workbook.getSheetIndex(HIDDEN_SHEET), true);
        }
        String formula = buildFormula(listOfValues,sheet.getLastRowNum() + 1);
        return createDataValidation(sheet, formula, index, row);
    }

    private DataValidation createDataValidation(XSSFSheet sheet, String formula, int index, Row row) {
        DataValidationHelper validationHelper;
        DataValidationConstraint constraint;
        DataValidation dataValidation;
        validationHelper = new XSSFDataValidationHelper(sheet);
        CellRangeAddressList addressList = new CellRangeAddressList(row.getRowNum(), row.getRowNum(), index, index);
        constraint = validationHelper.createFormulaListConstraint(formula);
        dataValidation = validationHelper.createValidation(constraint, addressList);
        return dataValidation;
    }

    private String buildFormula(List<String> listOfValues, int i) {
        int rowIndex = incrementIndex();
        copyValueToHiddenSheet(listOfValues, rowIndex);
        String formula = FORMULA_TEMPLATE.replace("sheetName", HIDDEN_SHEET);
        formula = formula.replace("rowNumber", String.valueOf(rowIndex));
        return formula.replace("finishIndex", constructIndex(listOfValues.size())).replace("startIndex", DEFAULT_START_INDEX);
    }

    private int incrementIndex() {
        return ++index;
    }

    private void copyValueToHiddenSheet(List<String> listOfValues, int index) {
        XSSFSheet hiddenSheet = workbook.getSheet(HIDDEN_SHEET);
        if (null == hiddenSheet) {
            hiddenSheet = workbook.createSheet(HIDDEN_SHEET);
        }
        Row row = hiddenSheet.createRow(index - 1);
        for (int i = 0, length = listOfValues.size(); i < length; i++) {
            String name = listOfValues.get(i);
            Cell cell = row.createCell(i);
            cell.setCellValue(name);
        }
    }

    private String constructIndex(int id) {
        String returnId;
        if (id <= 26) {
            returnId = getChar(id).toString();
        } else {
            int a = id / 26;
            int re = id % 26;
            returnId = constructIndex(a).toString() + constructIndex(re).toString();
        }
        return returnId;
    }

    private Character getChar(int i) {
        return new Character((char) (64 + i));
    }
}
