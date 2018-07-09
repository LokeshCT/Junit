package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.domain.bom.parameters.OrderFormSignDate;
import com.bt.rsqe.excel.ExcelStyler;
import org.apache.cxf.common.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;

import java.util.List;

import static com.bt.rsqe.excel.ExcelUtil.*;
import static com.bt.rsqe.utils.AssertObject.*;

public class OrderSheetMarshaller implements ExportExcelMarshaller.ExcelMarshaller {
    public static final String SHEET_NAME = "Order Details";
    private final OrderSheetModel orderSheetModel;
    private Workbook workbook;
    private ExcelStyler styler;
    private final XSSFSheet sheet;

    private OrderSheetColumnManager orderSheetColumnManager;

    private enum DataType {
        TEXT,
        DATE
    }

    public enum Column {
        LINE_ITEM_ID("Line Item ID", 0, DataType.TEXT,5000),
        SITE_ID("Site Id", 1, DataType.TEXT,5000),
        SITE_NAME("Site Name", 2, DataType.TEXT,5000),
        SUMMARY("Summary", 3, DataType.TEXT,5000),
        PRODUCT_NAME("Product Name", 4, DataType.TEXT, 5000),
        SUBLOCATION_NAME("Sublocation Name (M)", 5, DataType.TEXT, 5000),
        SUBLOCATION_NAME_VALUE("Sublocation Name ", 6, DataType.TEXT, 5000),
        ROOM("Room (M)", 7, DataType.TEXT, 5000),
        ROOM_VALUE("Room ", 8, DataType.TEXT, 5000),
        FLOOR("Floor (M)", 9, DataType.TEXT, 5000),
        FLOOR_VALUE("Floor", 10, DataType.TEXT, 5000),
        SIGNED_DATE("Order Form Signed Date [YYYY-MMM-DD] (M)", 11, DataType.DATE, 5000),
        CUSTOMER_REQUIRED_DATE("Customer Required Date [YYYY-MMM-DD] (M)", 12, DataType.DATE, 5000),
        INITIAL_BILLING_START_DATE("Contract Start Date [YYYY-MMM-DD] (M)", 13, DataType.DATE, 5000),
        BILLING_ID("Billing Id - Billing Account Name (M)", 14, DataType.TEXT, 5000),
        BILLING_ID_VALUE("Billing Id - Billing Account Name", 15, DataType.TEXT, 5000),
		EXPEDIO_REFERENCE("Expedio Reference", 16, DataType.TEXT, 5000);

        public final String header;
        public final int column;
        public final DataType dataType;
        public final Integer width;

        Column(String header, int column, DataType dataType) {
            this(header, column, dataType, null);
        }

        Column(String header, int column, DataType dataType, Integer width) {
            this.header = header;
            this.column = column;
            this.dataType = dataType;
            this.width = width;
        }
    }

    public OrderSheetMarshaller(OrderSheetModel orderSheetModel, XSSFWorkbook workbook, ExcelStyler styler, OrderSheetColumnManager orderSheetColumnManager) {
        this.orderSheetModel = orderSheetModel;
        this.workbook = workbook;
        this.styler = styler;
        this.sheet = workbook.createSheet(SHEET_NAME);
        this.orderSheetColumnManager = orderSheetColumnManager;
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
        setTextCellStyles();
        setDateCellStyles();
        setCellWidths();
        setHiddenColumns();
        createCells();
    }

    private void createCells() {
        CellStyle cellStyle = textFormat(workbook);
        styler.defaultStyle(cellStyle);

        final CellStyle dateCellStyle = textFormat(workbook);
        dateCellStyle.setDataFormat(workbook.createDataFormat().getFormat("YYYY-MMM-DD"));
        styler.defaultStyle(dateCellStyle);

        final List<OrderSheetModel.OrderSheetRow> rows = orderSheetModel.rows();
        int rowIndex = 1;
        int subLocationNameStartColumn = 2;
        int subLocationNameStartRow = 1;
        int subLocationNameEndColumn = 1;
        int roomStartColumn = 2;
        int roomStartRow = 1;
        int roomEndColumn = 1;
        int floorStartColumn = 2;
        int floorStartRow = 1;
        int floorEndColumn = 1;
        sheet.protectSheet(sheet.getSheetName());

        for (OrderSheetModel.OrderSheetRow rowData : rows) {
            Row row ;
            row = sheet.getRow(rowIndex);
            if(isNull(row)){
               row = sheet.createRow(rowIndex);
            }
            String lineItemId = rowData.lineItemId();
            row.createCell(Column.LINE_ITEM_ID.column).setCellValue(rowData.lineItemId());
            row.createCell(Column.SITE_ID.column).setCellValue(rowData.siteId());
            row.createCell(Column.SITE_NAME.column).setCellValue(rowData.siteName());
            row.createCell(Column.SUMMARY.column).setCellValue(rowData.summary());
            row.createCell(Column.PRODUCT_NAME.column).setCellValue(rowData.productName());

            final Cell sublocationNameCell = row.createCell(Column.SUBLOCATION_NAME.column, Cell.CELL_TYPE_STRING);
            sublocationNameCell.setCellValue(rowData.sublocationName());
            sublocationNameCell.setCellStyle(cellStyle);
            subLocationNameEndColumn = subLocationNameEndColumn + orderSheetModel.subLocationNames(lineItemId).size();
            String subLocNameHiddenColumnValues = "'!$G$"+subLocationNameStartColumn+":$G$"+subLocationNameEndColumn;
            subLocationNameStartRow = createDropDownCells(orderSheetModel.subLocationNames(lineItemId),subLocationNameStartRow,Column.SUBLOCATION_NAME_VALUE.column,Column.SUBLOCATION_NAME.column,subLocNameHiddenColumnValues,rowIndex,rowIndex,false);
            subLocationNameStartColumn = subLocationNameEndColumn+1;

            final Cell roomCell = row.createCell(Column.ROOM.column, Cell.CELL_TYPE_STRING);
            roomCell.setCellValue(rowData.room());
            roomCell.setCellStyle(cellStyle);
            roomEndColumn = roomEndColumn + orderSheetModel.rooms(lineItemId).size();
            String roomHiddenColumnValues = "'!$I$"+roomStartColumn+":$I$"+roomEndColumn;
            roomStartRow = createDropDownCells(orderSheetModel.rooms(lineItemId),roomStartRow,Column.ROOM_VALUE.column,Column.ROOM.column,roomHiddenColumnValues,rowIndex,rowIndex,false);
            roomStartColumn = roomEndColumn+1;


            final Cell floorCell = row.createCell(Column.FLOOR.column, Cell.CELL_TYPE_STRING);
            floorCell.setCellValue(rowData.floor());
            floorCell.setCellStyle(cellStyle);
            floorEndColumn = floorEndColumn + orderSheetModel.floors(lineItemId).size();
            String floorHiddenColumnValues = "'!$K$"+floorStartColumn+":$K$"+floorEndColumn;
            floorStartRow = createDropDownCells(orderSheetModel.floors(lineItemId),floorStartRow,Column.FLOOR_VALUE.column,Column.FLOOR.column,floorHiddenColumnValues,rowIndex,rowIndex,false);
            floorStartColumn = floorEndColumn+1;

            final Cell billingIdCell = row.createCell(Column.BILLING_ID.column, Cell.CELL_TYPE_STRING);
            final String billingId = rowData.billingId();
            if("Provide".equalsIgnoreCase(rowData.getLineItemAction()) || (!"Provide".equalsIgnoreCase(rowData.getLineItemAction()) && StringUtils.isEmpty(billingId))) {
                billingIdCell.setCellStyle(cellStyle);
            }
            billingIdCell.setCellValue(billingId);

            final OrderFormSignDate orderFormSignDate = rowData.orderSignedDate();
            if (orderFormSignDate != null) {
                final DateTime orderFormSignDateValue = orderFormSignDate.getValue();
                Cell orderFormSignDateCell  = row.createCell(Column.SIGNED_DATE.column);
                orderFormSignDateCell.setCellStyle(dateCellStyle);
                if (orderFormSignDateValue != null) {
                    orderFormSignDateCell.setCellValue(orderFormSignDateValue.toDate());
                }
            }

            Cell customerRequiredDateCell = row.createCell(Column.CUSTOMER_REQUIRED_DATE.column);
            customerRequiredDateCell.setCellStyle(dateCellStyle);
            if(null != rowData.getCustomerRequiredDate() && null != rowData.getCustomerRequiredDate().get()) {
                customerRequiredDateCell.setCellValue(rowData.getCustomerRequiredDate().get().toDate());
            }

            Cell initialBillingStartDateCell = row.createCell(Column.INITIAL_BILLING_START_DATE.column);
            initialBillingStartDateCell.setCellStyle(dateCellStyle);
            if(null != rowData.initialBillingStartDate()
                && rowData.initialBillingStartDate().isPresent()
                && null != rowData.initialBillingStartDate().get()) {
                initialBillingStartDateCell.setCellValue(rowData.initialBillingStartDate().get());
            }

            row.createCell(Column.EXPEDIO_REFERENCE.column).setCellValue(rowData.getExpRef());
            rowIndex++;
        }
        String hiddenColumnValues = "'!$P$2:$P$"+(orderSheetModel.billingIds().size()+1);
        createDropDownCells(orderSheetModel.billingIds(),1,Column.BILLING_ID_VALUE.column,Column.BILLING_ID.column,hiddenColumnValues,rows.size(),1,true);
    }
    private int createDropDownCells(List<String> inputList, int startRow, int valueColumn,int column,String hiddenColumnValues, int endRow, int rowIndex, boolean lock){
        CellStyle unlockedCellStyle = workbook.createCellStyle();
        unlockedCellStyle.setLocked(false);
        for(int j=0; j<inputList.size(); j++ ){
            Row row;
            row = sheet.getRow(startRow);
            if(isNull(row)){
                  row = sheet.createRow(startRow);
            }
            Cell cell = row.createCell(valueColumn);
            cell.setCellValue(inputList.get(j));
            if(!lock){
                cell.setCellStyle(unlockedCellStyle);
            }
            startRow++;
        }
        String formulaTemplate = "'"+sheet.getSheetName()+hiddenColumnValues;
        addConstraint(sheet, formulaConstraint(formulaTemplate,sheet),column,rowIndex,true,endRow);
        if(inputList.size() == 1){
           Row row = sheet.getRow(rowIndex);
           Cell cell = row.createCell(column);
           cell.setCellValue(inputList.get(0));
           if(!lock){
               cell.setCellStyle(unlockedCellStyle);

           }
        }
        return startRow;
    }
    private void setTextCellStyles() {
        CellStyle cellStyle = textFormat(workbook);
        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);
        styler.defaultStyle(cellStyle);

        for(Column column : Column.values()) {
            if(DataType.TEXT.equals(column.dataType)) {
                sheet.setDefaultColumnStyle(column.column, cellStyle);
            }
        }
    }

    private void setDateCellStyles() {
        final CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setAlignment(CellStyle.ALIGN_LEFT);
        dateCellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);
        styler.defaultStyle(dateCellStyle);
        dateCellStyle.setDataFormat(workbook.createDataFormat().getFormat("YYYY-MMM-DD"));

        for(Column column : Column.values()) {
            if(DataType.DATE.equals(column.dataType)) {
                sheet.setDefaultColumnStyle(column.column, dateCellStyle);
                makeDateColumn(sheet, column.column);
            }
        }
    }

    private void setCellWidths() {
        for(Column column : Column.values()) {
            if(null != column.width) {
                sheet.setColumnWidth(column.column, column.width);
            }
        }
    }

    private void setHiddenColumns() {
        for (Column column : Column.values()) {
            if (!orderSheetColumnManager.isColumnVisible(column)) {
                sheet.setColumnHidden(column.column, true);
            }
        }
    }

    private void createHeaders() {
        final Row row = sheet.createRow(0);
        for (Column column : Column.values()) {
            final Cell cell = row.createCell(column.column);
            cell.setCellValue(column.header);
            styler.styleHeaderCell(cell);
        }
    }
}
