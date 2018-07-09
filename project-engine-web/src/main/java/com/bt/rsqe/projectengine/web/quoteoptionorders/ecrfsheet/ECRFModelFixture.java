package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

public class ECRFModelFixture {


    private ECRFSheet ecrfSheetModel;

    public static ECRFModelFixture aECRFModel() {
         return new ECRFModelFixture();
    }

    public ECRFModelFixture() {
       this.ecrfSheetModel  = new ECRFSheet();
    }

    public ECRFSheet build(){
        return this.ecrfSheetModel;
    }

    public ECRFModelFixture withScode(String sCode) {
        this.ecrfSheetModel.setProductCode(sCode);
        return this;
    }

    public ECRFModelFixture withSheetName(String sheetName) {
        this.ecrfSheetModel.setSheetName(sheetName);
        return this;
    }

    public ECRFModelFixture withRow(ECRFSheetModelRow row) {
        this.ecrfSheetModel.addRow(row);
        return this;
    }

    public ECRFModelFixture withSheetIndex(int index) {
        this.ecrfSheetModel.setSheetIndex(index);
        return this;
    }

    public ECRFModelFixture withSheetTypeStrategy(SheetTypeStrategy strategy) {
        this.ecrfSheetModel.setSheetTypeStrategy(strategy);
        return this;
    }
}
