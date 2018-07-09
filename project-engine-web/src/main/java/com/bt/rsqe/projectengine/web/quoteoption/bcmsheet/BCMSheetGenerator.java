package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMConstants.*;
import static com.bt.rsqe.utils.AssertObject.*;

public class BCMSheetGenerator {

    private static final int HEADER_ROW = 0;
    private static final int DATA_ROW = 1;
    private static final int WORKBOOK_SHEET_NAME_LENGTH = 27;
    private int sheetNameCount = 0;

    public HSSFSheet createBCMSheet(HSSFWorkbook workbook, HeaderRowModel headerRowModel, List<Map<String, String>> dataRowModel, String sheetName) {
        HSSFSheet sheet = workbook.createSheet(sheetName);
        createHeader(headerRowModel,sheet);
        if(sheetName.equalsIgnoreCase(BCM_BID_INFO_SHEET)){
            createBidInfoDataRows(dataRowModel, sheet);
        }else if(sheetName.equalsIgnoreCase(BCM_PRODUCT_PER_SITE_SHEET)){
            createProductPerSiteDataRows(dataRowModel, sheet);
        }else if(sheetName.equalsIgnoreCase(BCM_SPECIAL_BID_INFO_SHEET)){
            createSpecialBidDataRows(dataRowModel, sheet);
        }
        return sheet;
    }

    public HSSFSheet createSiteBasedBCMSheet(HSSFWorkbook workbook, HeaderRowModel headerRowModel, List<Map<String,String>> dataRows, String sheetName) {
        sheetName = generateWorkBookSheetName(workbook,sheetName);
        HSSFSheet sheet = workbook.createSheet(sheetName);
        createHeader(headerRowModel,sheet);
        createSiteBasedRootProductRows(dataRows, headerRowModel, sheet,sheetName);
        return sheet;
    }

    private String generateWorkBookSheetName(HSSFWorkbook workbook, String sheetName) {
        if (WORKBOOK_SHEET_NAME_LENGTH < sheetName.length() ) {
            sheetName = sheetName.substring(0, WORKBOOK_SHEET_NAME_LENGTH);
        }
        if (workbook.getSheetIndex(sheetName) > -1) {
            if (WORKBOOK_SHEET_NAME_LENGTH == sheetName.length()) {
                return sheetName.substring(0, WORKBOOK_SHEET_NAME_LENGTH - 1) + (++sheetNameCount);
            } else {
                return sheetName + (++sheetNameCount);
            }
        } else {
            return sheetName;
        }
    }

    public HSSFSheet createServiceBasedBCMSheet(HSSFWorkbook workbook, HeaderRowModel headerRowModel,  List<Map<String,String>> dataRows, String sheetName) {
        sheetName = generateWorkBookSheetName(workbook,sheetName);
        HSSFSheet sheet = workbook.createSheet(sheetName);
        createHeader(headerRowModel,sheet);
        createServiceBasedRootProductRows(dataRows, sheet,sheetName);
        return sheet;
    }

    public HSSFSheet createSiteManagementBCMSheet(HSSFWorkbook workbook, HeaderRowModel headerRowModel,  List<Map<String,String>> dataRows, String sheetName) {
        sheetName = generateWorkBookSheetName(workbook,sheetName);
        HSSFSheet sheet = workbook.createSheet(sheetName);
        createHeader(headerRowModel,sheet);
        createSiteManagementRows(dataRows, sheet, sheetName);
        return sheet;
    }

    public void createHeader(HeaderRowModel headerModel, HSSFSheet sheet) {
        HSSFRow row = createRow(sheet, HEADER_ROW);
        CellStyle boldStyle = sheet.getWorkbook().createCellStyle();
        Font boldFont = sheet.getWorkbook().createFont();
        boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        boldStyle.setFont(boldFont);
        for (HeaderCell headerCell : headerModel.getHeaderRow()) {
            createCell(row,headerCell.columnIndex, headerCell.columnName, boldStyle,
                       HSSFCell.CELL_TYPE_STRING, headerCell.isReadOnly,headerCell.visibility);
        }
    }

    private void createBidInfoDataRows(List<Map<String, String>> dataRowModel, HSSFSheet sheet) {
        int rowIndex = DATA_ROW;
        for(Map<String , String> dataColumn : dataRowModel) {
            HSSFRow row = createRow(sheet, rowIndex++);
            for(BidInfoStaticColumn columnProperty: BidInfoStaticColumn.values()){
                createCell(row, columnProperty.columnIndex,
                           dataColumn.get(columnProperty.retrieveValueFrom),
                           sheet.getWorkbook().createCellStyle(),
                           columnProperty.dataType,
                           columnProperty.isReadOnly, columnProperty.visibility);
            }
        }
    }

    private void createProductPerSiteDataRows(List<Map<String, String>> dataRowModel, HSSFSheet sheet) {
        int rowIndex = DATA_ROW;
        for(Map<String , String> dataColumn : dataRowModel){
            HSSFRow row = createRow(sheet, rowIndex++);
            int columnIndex=0;
            for(ProductPerSiteStaticColumn columnProperty: ProductPerSiteStaticColumn.values()){
                createCell(row, columnProperty.columnIndex,
                           dataColumn.get(columnProperty.retrieveValueFrom),
                           sheet.getWorkbook().createCellStyle(),
                           columnProperty.dataType,
                           columnProperty.isReadOnly, columnProperty.visibility);
                columnIndex++;
            }
            for(RootProductsCategory dynaColumns : RootProductsCategory.values()){
                createCell(row, columnIndex++,
                           dataColumn.get(dynaColumns.retrieveValueFrom),
                           sheet.getWorkbook().createCellStyle(),
                           dynaColumns.dataType,
                           dynaColumns.isReadOnly,dynaColumns.visibility);
            }
        }
    }

    private void createSpecialBidDataRows(List<Map<String, String>> dataRowModel, HSSFSheet sheet) {
        int rowIndex = DATA_ROW;
        for(Map<String , String> dataColumn : dataRowModel){
            HSSFRow row = createRow(sheet, rowIndex++);
            for(SpecialBidInfoStaticColumn columnProperty: SpecialBidInfoStaticColumn.values()){
                createCell(row, columnProperty.columnIndex,
                           dataColumn.get(columnProperty.retrieveValueFrom),
                           sheet.getWorkbook().createCellStyle(),
                           columnProperty.dataType,
                           columnProperty.isReadOnly,columnProperty.visibility);
            }
        }
    }

    private void createSiteBasedRootProductRows(List<Map<String, String>> dataRowModel, HeaderRowModel headerRowModel, HSSFSheet sheet, String sheetName) {
        int rowIndex = DATA_ROW;
        List<HeaderCell> headerCells = headerRowModel.getHeaderRow();
        for(Map<String , String> currentDataRow : dataRowModel){
            HSSFRow row = createRow(sheet, rowIndex++);
            for(HeaderCell currentHeaderCell : headerCells){
                createCell(row, currentHeaderCell.columnIndex,
                           currentDataRow.get(currentHeaderCell.retrieveValueFrom),
                           sheet.getWorkbook().createCellStyle(),
                           currentHeaderCell.dataType,
                           currentHeaderCell.isReadOnly, currentHeaderCell.visibility);

            }
        }
    }

    private void createServiceBasedRootProductRows(List<Map<String, String>> dataRows, HSSFSheet sheet, String sheetName) {
        int rowIndex = DATA_ROW;
        for(Map<String , String> dataColumn : dataRows){
            int columnIndex=0;
            HSSFRow row = createRow(sheet, rowIndex++);
            for(ServiceProductSheetStaticColumn columnProperty : ServiceProductSheetStaticColumn.values()) {
                createCell(row, columnIndex++,
                           dataColumn.get(columnProperty.retrieveValueFrom),
                           sheet.getWorkbook().createCellStyle(),
                           columnProperty.dataType,
                           columnProperty.isReadOnly, columnProperty.visibility);
                //Attributes Section
                if(columnProperty.name().equalsIgnoreCase(ServiceProductSheetStaticColumn.SERVICE_TYPE.name()) &&
                    isNotNull(AttributesMapper.getAttributeMapper(sheetName))){
                    for(String mapKey: AttributesMapper.getAttributeMapper(sheetName).attributesMap().keySet()){
                        createCell(row, columnIndex++,
                                   dataColumn.get(mapKey),
                                   sheet.getWorkbook().createCellStyle(),
                                   1,true, true);
                    }
                }
            }
        }
    }

    private void createSiteManagementRows(List<Map<String, String>> dataRows, HSSFSheet sheet, String sheetName) {
        int rowIndex = DATA_ROW;
        for(Map<String , String> dataColumn : dataRows){
            int columnIndex=0;
            HSSFRow row = createRow(sheet, rowIndex++);
            for(SiteManagementStaticColumns columnProperty : SiteManagementStaticColumns.values()){
                createCell(row, columnIndex++,
                           dataColumn.get(columnProperty.retrieveValueFrom),
                           sheet.getWorkbook().createCellStyle(),
                           columnProperty.dataType,
                           columnProperty.isReadOnly, columnProperty.visibility);
                //Attributes Section
                if(columnProperty.name().equalsIgnoreCase(SiteManagementStaticColumns.PRODUCT_INSTANCE_VERSION.name()) &&
                   isNotNull(AttributesMapper.getAttributeMapper(sheetName))){
                    for(String mapKey: AttributesMapper.getAttributeMapper(sheetName).attributesMap().keySet()){
                        createCell(row, columnIndex++,
                                   dataColumn.get(mapKey),
                                   sheet.getWorkbook().createCellStyle(),
                                   1,true, true);
                    }
                }
            }
        }
    }


    public HSSFRow createRow(HSSFSheet sheet, int index) {
        return sheet.createRow(index);
    }

    public HSSFCell createCell(HSSFRow row, int index, Object value, CellStyle cellStyle, int cellType, boolean isReadOnly,boolean visible) {
        HSSFCell cell = row.createCell(index);
        cell.setCellStyle(cellStyle);
        String stringValue = value == null ? "" : value.toString();
        HSSFRow headerRow = row.getSheet().getRow(0);
        if (value != null && !"".equals(value)) {
            cell.setCellType(cellType);
        }else if( headerRow.getCell(index).getStringCellValue().contains("Discount")){
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
        }else {
            cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
        }

        if (cellType == HSSFCell.CELL_TYPE_STRING && cell.getCellType() != HSSFCell.CELL_TYPE_BLANK) {
            cell.setCellValue(stringValue);
        } else if (cellType == HSSFCell.CELL_TYPE_NUMERIC && cell.getCellType() != HSSFCell.CELL_TYPE_BLANK) {
            if(!isEmpty(stringValue)){
                cell.setCellValue(Double.parseDouble(stringValue));
            }
            cell.getCellStyle().setAlignment( CellStyle.ALIGN_RIGHT);
            HSSFDataFormat hssfDataFormat = row.getSheet().getWorkbook().createDataFormat();
            cellStyle.setDataFormat(hssfDataFormat.getFormat("#0.00"));
            if(row.getRowNum()>0){
                if(headerRow.getCell(index).getStringCellValue().contains("Discount")){
                    if((headerRow.getCell(index).getStringCellValue().contains("One time Discount") ||
                       headerRow.getCell(index).getStringCellValue().contains("Monthly Discount")) &&
                       isEmpty(stringValue) ){
                        cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
                    } else{
                        cellStyle.setDataFormat(hssfDataFormat.getFormat("#0.00"));
                    }
                    cell.setCellStyle(cellStyle);
                }
            }
        } else if(cellType==BCMConstants.CELL_TYPE_INTEGER && cell.getCellType() != HSSFCell.CELL_TYPE_BLANK){
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            if(!isEmpty(stringValue)){
                cell.setCellValue(Integer.parseInt(stringValue));
            }
        }

        if (!visible) {
            row.getSheet().setColumnHidden(index, true);
        }
        cell.getCellStyle().setLocked(isReadOnly);

        //Auto Resize of Column
        cell.getSheet().autoSizeColumn(index);
        return cell;
    }
}
