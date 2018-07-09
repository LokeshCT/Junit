package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class ECRFWorkBookFixture {

    private ECRFWorkBook ecrfworkBook;

    public ECRFWorkBookFixture() {
        this.ecrfworkBook = new ECRFWorkBook();
    }

    public static ECRFWorkBookFixture aECRFWorkBook() {
        return new ECRFWorkBookFixture();
    }

    public ECRFWorkBookFixture withECRFSheets(List<ECRFSheet> ecrfSheetModels) {
        this.ecrfworkBook.setWorksheets(ecrfSheetModels);
        return this;
    }

    public ECRFWorkBookFixture withNonProductECRFSheets(List<ECRFSheet> ecrfSheetModels) {
        this.ecrfworkBook.setNonProductSheet(ecrfSheetModels);
        return this;
    }

    public ECRFWorkBook build() {
        return this.ecrfworkBook;
    }

    public ECRFWorkBookFixture withIsRelatedToSheet() {
        this.ecrfworkBook.setHasRelatedToSheet(true);
        return this;
    }

    public ECRFWorkBookFixture withControlSheet() {
        //Must be called after adding sheets to Workbook
        Map<String, String> controlSheet = newHashMap();
        for (ECRFSheet ecrfSheet : this.ecrfworkBook.getSheets()) {
            controlSheet.put(ecrfSheet.getSheetName(), ecrfSheet.getProductCode());
        }
        this.ecrfworkBook.setControlSheet(controlSheet);
        return this;
    }
}
