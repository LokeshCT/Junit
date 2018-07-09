package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.isNull;
import static org.apache.commons.collections.CollectionUtils.*;

public class HeaderRowModel {

    private static final int SHEET_COLUMN_SIZE = 256;
    private static final int HEADER_ROW = 0;

    private List<HeaderCell> headerRow;
    public List<HSSFSheet> requiredSheets = new ArrayList<HSSFSheet>();

    public HeaderRowModel(List<HeaderCell> headerRow) {
        this.headerRow = headerRow;
    }

    public List<HeaderCell> getHeaderRow() {
        return headerRow;
    }

    public List<HSSFSheet> getRequiredSheets() {
        return isEmpty(requiredSheets) ? new ArrayList<HSSFSheet>() : requiredSheets;
    }

    public int getCellIndexFor(String columnName){
        for(HeaderCell cell: headerRow){
            if(cell.columnName.equalsIgnoreCase(columnName)){
                return cell.columnIndex;
            }
        }
        return -1;
    }

    public HeaderCell getCellFor(String columnName){
        for(HeaderCell cell: headerRow){
            if(cell.columnName.equalsIgnoreCase(columnName)){
                return cell;
            }
        }
        return null;
    }

    public List<HeaderCell> getColumnsByGroupName(String ... groupNames){
        List<HeaderCell> columns = new ArrayList<HeaderCell>();
        for(HeaderCell cell: headerRow){
            if(Arrays.asList(groupNames).contains(cell.groupName)){
                columns.add(cell);
            }
        }
        return columns;
    }

    public HSSFWorkbook getWorkBook(){
        return requiredSheets.get(0).getWorkbook();
    }

    public void generateSheetsBasedOnHeaderSize(HSSFWorkbook workbook, String sheetName) {
        requiredSheets.add(workbook.getSheet(sheetName));
        int requiredSheetSize = headerRow.size() / SHEET_COLUMN_SIZE;
        int currentSheetPos = workbook.getSheetIndex(sheetName);
        for(int sheetIndex = 1 ; requiredSheetSize >= sheetIndex ; sheetIndex++){
            HSSFSheet sheet = workbook.createSheet(sheetName + "_" + sheetIndex);
            workbook.setSheetOrder(sheet.getSheetName(),++currentSheetPos);
            requiredSheets.add(sheet);
        }
    }

    public HSSFSheet getSheet(int sheetIndex){
        return requiredSheets.get(sheetIndex);
    }

    public HSSFRow getHeaderRow(int sheetIndex){
        return requiredSheets.get(sheetIndex).getRow(HEADER_ROW);
    }

    public HSSFRow getRowFor(int sheetIndex, int rowNum) {
        HSSFSheet sheet = requiredSheets.get(sheetIndex);
        HSSFRow row = sheet.getRow(rowNum);
        if (isNull(row)){
            HSSFRow newRow = sheet.createRow(rowNum);
            newRow.setHeight(BCMConstants.HEADER_ROW_HEIGHT);
            return newRow;
        }
        return row;
    }


}
