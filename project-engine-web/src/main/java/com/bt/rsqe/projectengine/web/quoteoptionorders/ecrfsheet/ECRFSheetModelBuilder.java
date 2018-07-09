package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.*;

public class ECRFSheetModelBuilder {

    private static final String CONTROL_SHEET = "Control Sheet";

    public ECRFWorkBook build(String productCodeImportable, Workbook importProductWorkBook) {
        Sheet sheet = importProductWorkBook.getSheet(CONTROL_SHEET);
        checkControlSheetExists(sheet);
        if (!controlSheetContainsSCode(productCodeImportable, sheet)) {
            throw new ECRFImportException(ECRFImportException.controlSheetDoesNotContainSCode);
        }
        Map<String, String> sheetNameToSCode = buildSheetNameToSCodeMap(importProductWorkBook.getSheet(CONTROL_SHEET));
        return generateECRFModelsFromWorkBook(importProductWorkBook, sheetNameToSCode);
    }

    private void checkControlSheetExists(Sheet sheet) {
        if (sheet == null) {
            throw new ECRFImportException(ECRFImportException.controlSheetNotFound);
        }
    }

    private Map<String, String> buildSheetNameToSCodeMap(Sheet importProductWorkBookSheet) {
        Map<String, String> controlMapping = new HashMap<String, String>();
        // Set i to 1 to skip the table header
        for(int i = 1; i < importProductWorkBookSheet.getLastRowNum()+1; i++) {
            controlMapping.put(importProductWorkBookSheet.getRow(i).getCell(1).toString(),
                               importProductWorkBookSheet.getRow(i).getCell(0).toString());
        }
        return controlMapping;
    }

    private ECRFWorkBook generateECRFModelsFromWorkBook(Workbook importProductWorkBook,
                                                                       Map<String, String> sheetNameToSCode) {
        ECRFWorkBook workBook = new ECRFWorkBook();
        workBook.setControlSheet(sheetNameToSCode);
        workBook.setControlSheetIndex(importProductWorkBook.getSheetIndex(CONTROL_SHEET));
        for (Map.Entry<String, String> sheetNameScode : sheetNameToSCode.entrySet()) {
            if (!sheetNameScode.getKey().equals("")) {
                Sheet sheetToModel = importProductWorkBook.getSheet(sheetNameScode.getKey());
                if (sheetToModel == null) {
                    throw new ECRFImportException(format(ECRFImportException.workSheetNotFound, sheetNameScode.getKey()));
                }
                workBook.addSheet(buildModelFromSheet(sheetNameScode.getValue(), importProductWorkBook.getSheet(sheetNameScode.getKey()), importProductWorkBook.getSheetIndex(sheetNameScode.getKey()), sheetNameToSCode));
            }
        }
        includeNonProductSheets(importProductWorkBook, sheetNameToSCode, workBook);
        workBook.setHasRelatedToSheet(workBookContainsRelatedToSheet(workBook));
        return workBook;
    }

    private void includeNonProductSheets(Workbook importProductWorkBook, Map<String, String> sheetNameToSCode, ECRFWorkBook workBook) {
        SheetTypeDecider sheetTypeDecider;
        for(int sheetIndex = workBook.getControlSheetIndex();sheetIndex < importProductWorkBook.getNumberOfSheets();sheetIndex++){
            Sheet sheet = importProductWorkBook.getSheetAt(sheetIndex);
            sheetTypeDecider = new SheetTypeDecider(sheet, sheetNameToSCode);
            if(sheetTypeDecider.getSheetType().equals(SheetTypeStrategy.NonProduct)){
                ECRFSheet ecrfSheetModel = new ECRFSheet();
                ecrfSheetModel.setSheetName(sheet.getSheetName());
                ecrfSheetModel.setSheetTypeStrategy(sheetTypeDecider.getSheetType());
                ecrfSheetModel.buildRowsFromSheet(sheet);
                ecrfSheetModel.setSheetIndex(sheetIndex);
                workBook.addNonProductSheet(ecrfSheetModel);
            }
        }
    }

    private boolean workBookContainsRelatedToSheet(ECRFWorkBook workBook) {
        for (ECRFSheet sheet : workBook.getSheets()) {
            if(sheet.isRelatedProductSheet()){
                return true;
            }
        }
        return false;
    }

    private ECRFSheet buildModelFromSheet(String sCode, Sheet sheet, int sheetIndex, Map<String, String> sheetNameToSCode) {
        ECRFSheet ecrfSheetModel = new ECRFSheet();
        ecrfSheetModel.setProductCode(sCode);
        ecrfSheetModel.setSheetName(sheet.getSheetName());
        SheetTypeDecider sheetTypeDecider = new SheetTypeDecider(sheet, sheetNameToSCode);
        ecrfSheetModel.setSheetTypeStrategy(sheetTypeDecider.getSheetType());

        ecrfSheetModel.buildRowsFromSheet(sheet);
        ecrfSheetModel.setSheetIndex(sheetIndex);
        return ecrfSheetModel;
    }

    private boolean controlSheetContainsSCode(String productCodeImportable, Sheet sheet) {
        for(int i = 0; i < sheet.getLastRowNum()+1; i++) {
            if (sheet.getRow(i).getCell(0).toString().equals(productCodeImportable)) {
                return true;
            }
        }
        return false;
    }
}
