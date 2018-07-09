package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.isNull;

public class SheetTypeDecider {
    private Sheet sheet;
    private Map<String, String> controlSheet;

    public SheetTypeDecider(Sheet sheet, Map<String, String> controlSheet) {
        this.sheet = sheet;
        this.controlSheet = controlSheet;
    }

    public SheetTypeStrategy getSheetType() {
        if(null != sheet){
            return SheetTypeStrategy.getStrategy(checkParentMappingAvailable(sheet.getRow(0)), checkRelatedToSheet(), checkSheetNamePresentInControlSheet());
        }
        return null;
    }

    private boolean checkParentMappingAvailable(Row row) {
        if(!isNull(row)){
        for(int i = 0; i < row.getLastCellNum(); i++) {
            if (row.getCell(i).toString().equals(ECRFSheetModelRow.SHEET_PARENT_ID)) {
                return true;
            }
        }
        }
        return false;
    }

    private boolean checkRelatedToSheet() {
        return sheet.getSheetName().equals(ECRFSheet.RELATED_TO_PRODUCT_SHEET);
    }

    private boolean checkSheetNamePresentInControlSheet(){
        return controlSheet.containsKey(sheet.getSheetName());
    }
}
