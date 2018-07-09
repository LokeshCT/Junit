package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

import static com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys.*;
import static com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.XlsFileUtils.*;
import static com.bt.rsqe.utils.Channels.*;

public class DirectUserDetailedPricingSheetSiteLevelSectionCellMerger extends PricingSheetCellMerger {

    private static final String SITE_LEVEL_CHARGES_START_ROW_TEXT = "Site Level Charges Start";
    private static final String SITE_LEVEL_CHARGES_END_ROW_TEXT = "Site Level Charges End";
    private static final int ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX = 1;
    private static final int FROM_COLUMN = 2;
    private static final int DIRECT_TO_COLUMN = 18;
    private static final int DIRECT_FROM_COLUMN_FOR_CHILD = 19;
    private static final int DIRECT_TO_COLUMN_FOR_CHILD = 39;
    private static final int INDIRECT_TO_COLUMN = 23;
    private static final int INDIRECT_FROM_COLUMN_FOR_CHILD = 24;
    private static final int INDIRECT_TO_COLUMN_FOR_CHILD = 45;

    //private static final int toColumnUntilPriceType = 8;
    private static final int PRICE_TYPE_CELL_INDEX_PLUS_ONE = 9;
    private static int CHILD_PRODUCT_INSTANCE_ID_CELL_INDEX = 19;
    private static final int PRICE_TYPE_CELL_INDEX = 8;
    private int toColumn = DIRECT_TO_COLUMN;
    private int fromColumnForChild = DIRECT_FROM_COLUMN_FOR_CHILD;
    private int toColumnForChild = DIRECT_TO_COLUMN_FOR_CHILD;

    @Override
    void mergeCells(Sheet sheet) {
        // Adjusting summary/detail sheet index for indirect user
        if (isIndirectUser()) {
            toColumn = INDIRECT_TO_COLUMN;
            fromColumnForChild = INDIRECT_FROM_COLUMN_FOR_CHILD;
            toColumnForChild = INDIRECT_TO_COLUMN_FOR_CHILD;
            CHILD_PRODUCT_INSTANCE_ID_CELL_INDEX = fromColumnForChild;
        } else {
            toColumn = DIRECT_TO_COLUMN;
            fromColumnForChild = DIRECT_FROM_COLUMN_FOR_CHILD;
            toColumnForChild = DIRECT_TO_COLUMN_FOR_CHILD;
            CHILD_PRODUCT_INSTANCE_ID_CELL_INDEX = fromColumnForChild;
        }

        int rowFrom = rowFrom(sheet);
        int rowTo = rowTo(sheet);
        List<FromTo> fromToList = rowsToMerge(sheet, rowFrom, rowTo, ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX);
        for (int cellIndex = FROM_COLUMN; cellIndex <= PRICE_TYPE_CELL_INDEX; cellIndex++) {
            for (FromTo fromTo : fromToList) {
                if (isValidForMerge(sheet, fromTo.from(), fromTo.to(), cellIndex)) {
                    sheet.addMergedRegion(new CellRangeAddress(fromTo.from(), fromTo.to(), cellIndex, cellIndex));
                }
            }
        }
        List<FromTo> fromToListWithType = rowsToMerge(sheet, rowFrom, rowTo,PRICE_TYPE_CELL_INDEX);
        for (int cellIndex =PRICE_TYPE_CELL_INDEX_PLUS_ONE; cellIndex <= toColumn; cellIndex++) {
            for (FromTo fromTo : fromToListWithType) {
                if (isValidForMerge(sheet, fromTo.from(), fromTo.to(), cellIndex)) {
                    sheet.addMergedRegion(new CellRangeAddress(fromTo.from(), fromTo.to(), cellIndex, cellIndex));
                }
            }
        }

        // check for child
        List<FromTo> fromToListForChild = rowsToMerge(sheet, rowFrom, rowTo, CHILD_PRODUCT_INSTANCE_ID_CELL_INDEX);
        for (int cellIndex = fromColumnForChild; cellIndex <= toColumnForChild; cellIndex++) {
            for (FromTo fromTo : fromToListForChild) {
                if (isValidForMerge(sheet, fromTo.from(), fromTo.to(), cellIndex)) {
                    sheet.addMergedRegion(new CellRangeAddress(fromTo.from(), fromTo.to(), cellIndex, cellIndex));
                }
            }
        }
    }

    @Override
    boolean canIMerge(Sheet sheet) {
        return sheet.getSheetName().equals(CA_DIRECT_USER_DETAILED_PRICING_SHEET_NAME);
    }

    private int rowFrom(Sheet sheet) {
        return findRowIndexForText(sheet, ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX, SITE_LEVEL_CHARGES_START_ROW_TEXT) + 1;
    }

    private int rowTo(Sheet sheet) {
        return findRowIndexForText(sheet, ROOT_PRODUCT_INSTANCE_ID_CELL_INDEX, SITE_LEVEL_CHARGES_END_ROW_TEXT) - 1;
    }
}

