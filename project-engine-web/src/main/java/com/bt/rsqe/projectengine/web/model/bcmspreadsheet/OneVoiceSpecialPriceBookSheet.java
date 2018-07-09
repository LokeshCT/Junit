package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.List;

public class OneVoiceSpecialPriceBookSheet extends BcmSpreadSheet {

    private HSSFSheet specialPriceBookSheet;

    public OneVoiceSpecialPriceBookSheet(HSSFSheet specialPriceBookSheet) {
        this.specialPriceBookSheet = specialPriceBookSheet;
    }

    public List<OneVoiceSpecialPriceBookRow> getSpecialPriceBookFor(String priceBookName, String sourceCountry) {

        List<OneVoiceSpecialPriceBookRow> specialPriceBookRows = new ArrayList<OneVoiceSpecialPriceBookRow>();
        for (Row row : specialPriceBookSheet) {
            if (!isHeader(row)) {
                if (priceBookName.equals(row.getCell(0).getStringCellValue()) && sourceCountry.equals(row.getCell(1).getStringCellValue())) {
                    specialPriceBookRows.add(new OneVoiceSpecialPriceBookRow(row));
                }
            }
        }
        return specialPriceBookRows;
    }

}
