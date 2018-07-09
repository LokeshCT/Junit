package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import static com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys.*;
import static com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.XlsFileUtils.*;
import static com.bt.rsqe.utils.Channels.*;

public class DirectUserDetailedPricingSheetServiceLevelSectionCellMerger extends PricingSheetCellMerger{

    private static final String SERVICE_LEVEL_CHARGES_START_TEXT = "Service Level Charges Start";
    private static final String SERVICE_LEVEL_CHARGES_END_TEXT = "Service Level Charges End";
    private static final int ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX = 1;
    private static final int PRODUCT_NAME_CELL_INDEX = 2;
    private static final int PRICE_BOOK_VERSION_CELL_INDEX = 5;

    private static final int SUMMARY_PRODUCT_NAME_CELL_INDEX = 1;
    private static final int SUMMARY_PRICE_BOOK_VERSION_CELL_INDEX = 3;
    private static final int DIRECT_SUMMARY_SHEET_ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX = 23;
    private static final int INDIRECT_SUMMARY_SHEET_ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX = 29;
    private int summarySheetRootProductInstanceIdCellIndex = DIRECT_SUMMARY_SHEET_ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX;
    @Override
    public void mergeCells(Sheet sheet) {
        // Adjusting summary sheet index for indirect user
        if (isIndirectUser()) {
            summarySheetRootProductInstanceIdCellIndex = INDIRECT_SUMMARY_SHEET_ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX;
        } else {
            summarySheetRootProductInstanceIdCellIndex = DIRECT_SUMMARY_SHEET_ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX;
        }

        int fromRow = rowFrom(sheet);
        int toRow = rowTo(sheet);
        final String priceSheetName =sheet.getSheetName();

        if (priceSheetName.equalsIgnoreCase(CA_DIRECT_USER_DETAILED_PRICING_SHEET_NAME)){
            for( int i=fromRow;i<=toRow;i++){
                if( isValidForMergeForServiceName(sheet, fromRow, toRow, ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX)) {
                    sheet.addMergedRegion(new CellRangeAddress(fromRow,serviceNameCount-1, PRODUCT_NAME_CELL_INDEX, PRODUCT_NAME_CELL_INDEX));
                }
                fromRow=serviceNameCount;
            }
            fromRow = rowFrom(sheet);
            if( isValidForMerge(sheet, fromRow, toRow, PRICE_BOOK_VERSION_CELL_INDEX)) {
                sheet.addMergedRegion(new CellRangeAddress(fromRow,toRow, PRICE_BOOK_VERSION_CELL_INDEX, PRICE_BOOK_VERSION_CELL_INDEX));
            }
        }else if (priceSheetName.equalsIgnoreCase(CA_DIRECT_USER_SUMMARY_PRICING_SHEET_NAME)){

            if( isValidForMerge(sheet, fromRow, toRow,ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX)) {
                sheet.addMergedRegion(new CellRangeAddress(fromRow,toRow, SUMMARY_PRODUCT_NAME_CELL_INDEX, SUMMARY_PRODUCT_NAME_CELL_INDEX));
            }

            if( isValidForMerge(sheet, fromRow, toRow, SUMMARY_PRICE_BOOK_VERSION_CELL_INDEX)) {
                sheet.addMergedRegion(new CellRangeAddress(fromRow,toRow, SUMMARY_PRICE_BOOK_VERSION_CELL_INDEX, SUMMARY_PRICE_BOOK_VERSION_CELL_INDEX));
            }
        }
    }

    @Override
    boolean canIMerge(Sheet sheet) {
        return (sheet.getSheetName().equals(CA_DIRECT_USER_DETAILED_PRICING_SHEET_NAME)
                || sheet.getSheetName().equals(CA_DIRECT_USER_SUMMARY_PRICING_SHEET_NAME) ) ;
    }

    private int rowFrom(Sheet sheet) {
        int cellIndex=ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX;
        if(sheet.getSheetName().equalsIgnoreCase(CA_DIRECT_USER_DETAILED_PRICING_SHEET_NAME)){
            cellIndex= ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX;
        }else if(sheet.getSheetName().equalsIgnoreCase(CA_DIRECT_USER_SUMMARY_PRICING_SHEET_NAME)){
            cellIndex= summarySheetRootProductInstanceIdCellIndex;
        }
        return findRowIndexForText(sheet,cellIndex, SERVICE_LEVEL_CHARGES_START_TEXT) + 1;
    }

    private int rowTo(Sheet sheet) {
        int cellIndex=ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX;
        if(sheet.getSheetName().equalsIgnoreCase(CA_DIRECT_USER_DETAILED_PRICING_SHEET_NAME)){
            cellIndex= ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX;
        }else if(sheet.getSheetName().equalsIgnoreCase(CA_DIRECT_USER_SUMMARY_PRICING_SHEET_NAME)){
            cellIndex= summarySheetRootProductInstanceIdCellIndex;
        }
        return findRowIndexForText(sheet, cellIndex, SERVICE_LEVEL_CHARGES_END_TEXT) - 1;
    }

}