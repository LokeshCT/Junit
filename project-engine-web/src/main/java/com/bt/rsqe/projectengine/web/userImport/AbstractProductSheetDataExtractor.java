package com.bt.rsqe.projectengine.web.userImport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public abstract class AbstractProductSheetDataExtractor implements ProductSheetDataExtractor {

    protected final XSSFWorkbook workbook;
    protected final ListValidationBuilder listValidationBuilder;
    protected final UserImportExcelStyler styler;

    protected String sheetName;

    protected AbstractProductSheetDataExtractor(XSSFWorkbook workbook, ListValidationBuilder listValidationBuilder, UserImportExcelStyler styler) {
        this.workbook = workbook;
        this.listValidationBuilder = listValidationBuilder;
        this.styler = styler;
    }

    @Override
    public void constructSheet() {

        Sheet sheet = workbook.getSheet(getSheetName());
        if (null == sheet) {
            sheet = workbook.createSheet(getSheetName());
            createHeaderRows(sheet);
        }
        createDataRows(sheet);
    }

    protected abstract void createHeaderRows(Sheet sheet);

    protected abstract void createDataRows(Sheet sheet);

    protected void createCellComment(Row row, int cellIndex, String commentValue) {

        CreationHelper factory = workbook.getCreationHelper();
        Drawing drawing = row.getSheet().createDrawingPatriarch();

        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(row.getCell(cellIndex).getColumnIndex());
        anchor.setCol2(row.getCell(cellIndex).getColumnIndex() + 2);
        anchor.setRow1(row.getRowNum());
        anchor.setRow2(row.getRowNum() + 1);

        Comment comment = drawing.createCellComment(anchor);
        comment.setString(factory.createRichTextString(commentValue));
    }

    protected Cell createCell(Row row, int cellIndex, String cellValue) {
        return createCell(row, cellIndex, cellValue, null);
    }

    protected Cell createCell(Row row, int cellIndex, String cellValue, CellStyle style) {

        Cell cell = row.createCell(cellIndex);
        cell.setCellValue(cellValue);
        cell.setCellStyle(style);
        return cell;
    }

    protected void createMergeCell(Row row, String cellValue, int cellStart, int cellEnd, int rowStart, int rowEnd) {

        Cell cell = row.createCell(cellStart);
        cell.setCellValue(cellValue);
        row.getSheet().addMergedRegion(new CellRangeAddress(rowStart, rowEnd, cellStart, cellEnd - 1));
    }
}
