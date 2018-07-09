package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMProductSheetProperty;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.io.InputStream;

public class BcmSpreadSheet {
    private HSSFWorkbook workbook;

    public BcmSpreadSheet() {
    }

    public BcmSpreadSheet(InputStream workbook) throws IOException {
        this.workbook = new HSSFWorkbook(workbook);
    }

    public OneVoiceBcmOptionsSheet getOneVoiceOptionsSheet() {
        return new OneVoiceBcmOptionsSheet(workbook.getSheet("Onevoice Options"));
    }

    public OneVoiceChannelInformationSheet getOneVoiceChannelInformationSheet(){
        return new OneVoiceChannelInformationSheet(workbook.getSheet("Onevoice Channel Information"));
    }

    public OneVoiceSpecialPriceBookSheet getSpecialPriceBookSheet() {
        return new OneVoiceSpecialPriceBookSheet(workbook.getSheet("Special Pricebook"));
    }

    protected boolean isHeader(Row row) {
        return row.getRowNum() == 0;
    }

    protected boolean isEmpty(Row row) {
        final Cell cell = row.getCell(1);
        return (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK);
    }

    public ProductSheet getSheet(String productFamilyName, BCMProductSheetProperty sheetProperty) {
        return new ProductSheet(workbook.getSheet(sheetProperty.getSheetNameFor(productFamilyName)));
    }

    public ProductSheet getCASiteSheet(String productFamilyName) {
        return getSheet(productFamilyName, BCMProductSheetProperty.SiteInstallable);

    }
    public ProductSheet getCAServiceSheet(String productFamilyName) {
        return getSheet(productFamilyName, BCMProductSheetProperty.SiteAgnostic);

    }
    public ProductSheet getSpecialBidServiceSheet() {
        return new ProductSheet(workbook.getSheet(BCMProductSheetProperty.SpecialBid.sheetName));

    }

    public ProductInfoSheet getProductInfoSheet() {
        return new ProductInfoSheet(workbook.getSheet(BCMProductSheetProperty.ProductInfo.sheetName));
    }
}
