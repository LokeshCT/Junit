package com.bt.rsqe;

import com.bt.rsqe.excel.ExcelStyler;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.ExportExcelMarshaller;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.OrderSheetModel;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;

import java.util.List;

import static com.bt.rsqe.excel.ExcelUtil.*;

public class ConnectAccelerationSheetMarshaller implements ExportExcelMarshaller.ExcelMarshaller {
    public static final String SHEET_NAME = "Order Details";
    private final OrderSheetModel orderSheetModel;
    private HSSFWorkbook workbook;
    private ExcelStyler styler;
    private final HSSFSheet sheet;

    public enum Column {
        LINE_ITEM_ID("Line Item ID", 0),
        SITE_ID("Site Id", 1),
        SITE_NAME("Site Name", 2),
        REPORTING_REQUIEMENTS("REPORTING REQUIREMENTS (O)", 3),
        CUSTOMER_REQUIREMENTS("CUSTOMER REQUIREMENTS (O)", 4),
        PORTAL_CUSTOMER_ID("PORTAL CUSTOMER ID (M)", 5),
        PRODUCT_INSTANCE_ID("Product Instance Id", 6),
        PRODUCT_NAME("Product Name", 7);
        public final String header;
        public final int column;

        Column(String header, int column) {
            this.header = header;
            this.column = column;
        }
    }

    public ConnectAccelerationSheetMarshaller(OrderSheetModel orderSheetModel, HSSFWorkbook workbook, ExcelStyler styler) {
        this.orderSheetModel = orderSheetModel;
        this.workbook = workbook;
        this.styler = styler;
        this.sheet = workbook.createSheet(SHEET_NAME);
    }

    @Override
    public void marshall() {
        createHeaders();
        addData();
        styleCells();
    }

    private void styleCells() {
        styler.styleCells(sheet, Column.values().length, orderSheetModel.rows().size());
    }

    private void addData() {
        final List<OrderSheetModel.OrderSheetRow> rows = orderSheetModel.rows();
        int rowIndex = 1;
        CellStyle cellStyle = textFormat(workbook);
        //sheet.setDefaultColumnStyle(Column.BILLING_ID.column, cellStyle);

        final HSSFCellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(workbook.createDataFormat().getFormat("dd mmmm yyyy"));
        //sheet.setDefaultColumnStyle(Column.SIGNED_DATE.column, dateCellStyle);
        //sheet.setColumnWidth(Column.SIGNED_DATE.column, 5000);

        for (OrderSheetModel.OrderSheetRow rowData : rows) {
            final HSSFRow row = sheet.createRow(rowIndex++);
            row.createCell(Column.LINE_ITEM_ID.column).setCellValue(rowData.lineItemId());
            row.createCell(Column.SITE_ID.column).setCellValue(rowData.siteId());
            row.createCell(Column.SITE_NAME.column).setCellValue(rowData.siteName());
            row.createCell(Column.REPORTING_REQUIEMENTS.column).setCellValue(rowData.productName());
            row.createCell(Column.CUSTOMER_REQUIREMENTS.column).setCellValue(rowData.productName());
            row.createCell(Column.PORTAL_CUSTOMER_ID.column).setCellValue(rowData.productName());
            row.createCell(Column.PRODUCT_INSTANCE_ID.column).setCellValue(rowData.productName());
            row.createCell(Column.PRODUCT_NAME.column).setCellValue(rowData.productName());

            //final HSSFCell billingIdCell = row.createCell(Column.BILLING_ID.column, Cell.CELL_TYPE_STRING);
            //billingIdCell.setCellStyle(cellStyle);
            //billingIdCell.setCellValue(rowData.billingId());
            /*final OrderFormSignDate orderFormSignDate = rowData.orderSignedDate();
            if (orderFormSignDate != null) {
                final DateTime orderFormSignDateValue = orderFormSignDate.getValue();
                if (orderFormSignDateValue != null) {
                    row.createCell(Column.SIGNED_DATE.column).setCellValue(orderFormSignDateValue.toDate());
                }
            }   */
        }
        sheet.setColumnHidden(Column.LINE_ITEM_ID.column, true);
        //makeDateColumn(sheet, Column.SIGNED_DATE.column);
        //addConstraint(sheet, listConstraint(orderSheetModel.billingIds()), Column.BILLING_ID.column, 1);
    }

    private void createHeaders() {
        final HSSFRow row = sheet.createRow(0);
        for (Column column : Column.values()) {
            final HSSFCell cell = row.createCell(column.column);
            cell.setCellValue(column.header);
            styler.styleHeaderCell(cell);
        }
    }
}

