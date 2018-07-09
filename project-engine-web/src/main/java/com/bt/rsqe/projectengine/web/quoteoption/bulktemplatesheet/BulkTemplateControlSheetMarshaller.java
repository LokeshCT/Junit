package com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet;

import com.bt.rsqe.excel.ExcelStyler;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.ExportExcelMarshaller;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

import static com.bt.rsqe.excel.ExcelUtil.*;

public class BulkTemplateControlSheetMarshaller implements ExportExcelMarshaller.ExcelMarshaller {
    public static final String CONTROL_SHEET_NAME = "Control Sheet";
    private final BulkTemplateControlSheetModel bulkTemplateControlSheetModel;
    private Workbook workbook;
    private ExcelStyler styler;
    private final XSSFSheet sheet;


    public enum HeaderColumn {
        PRODUCT_CODE("SCode",0, XSSFCell.CELL_TYPE_STRING),
        PRODUCT_NAME("Sheet Name",1, XSSFCell.CELL_TYPE_STRING);

        public final String header;
        public final int columnIndex;
        public final int dataType;

        HeaderColumn(String header, int columnIndex, int dataType) {
            this.header = header;
            this.columnIndex = columnIndex;
            this.dataType = dataType;
        }

    }

    public BulkTemplateControlSheetMarshaller(BulkTemplateControlSheetModel bulkTemplateControlSheetModel, XSSFWorkbook workbook, ExcelStyler styler) {
        this.bulkTemplateControlSheetModel = bulkTemplateControlSheetModel;
        this.workbook = workbook;
        this.styler = styler;
        this.sheet = workbook.createSheet(CONTROL_SHEET_NAME);
    }

    @Override
    public void marshall() {
        createHeaders();
        addData();
        styleCells();
    }

    private void styleCells() {
        styler.styleCells(sheet, HeaderColumn.values().length, bulkTemplateControlSheetModel.getRows().size());
    }

    private void addData() {
        createCells();
        setCellWidths();
    }

    private void createCells() {
        CellStyle cellStyle = textFormat(workbook);
        styler.defaultStyle(cellStyle);

        final List<BulkTemplateControlSheetModel.BulkTemplateControlSheetRow> rows = bulkTemplateControlSheetModel.getRows();
        int rowIndex = 1;

        for (BulkTemplateControlSheetModel.BulkTemplateControlSheetRow rowData : rows) {
            final Row row = sheet.createRow(rowIndex++);
            row.createCell(HeaderColumn.PRODUCT_CODE.columnIndex).setCellValue(rowData.getsCode());
            row.createCell(HeaderColumn.PRODUCT_NAME.columnIndex).setCellValue(rowData.getProductName());
        }
    }

    private void setCellWidths() {
        for(HeaderColumn column : HeaderColumn.values()) {
            sheet.autoSizeColumn(column.columnIndex);
        }
    }

    private void createHeaders() {
        final Row row = sheet.createRow(0);
        for (HeaderColumn column : HeaderColumn.values()) {
            final Cell cell = row.createCell(column.columnIndex);
            cell.setCellValue(column.header);
            styler.styleHeaderCell(cell);
        }
    }
}
