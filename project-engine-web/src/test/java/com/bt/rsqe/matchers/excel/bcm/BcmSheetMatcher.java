package com.bt.rsqe.matchers.excel.bcm;

import com.bt.rsqe.excel.ExcelMerge;
import com.bt.rsqe.matchers.CompositeMatcher;
import com.bt.rsqe.matchers.excel.ExcelSheetCompositeMatcher;
import com.bt.rsqe.matchers.excel.ExcelTemplateFields;
import com.bt.rsqe.matchers.excel.ExcelTemplateParser;
import com.bt.rsqe.matchers.excel.ExcelTemplateSheetFields;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.IOException;

public class BcmSheetMatcher extends CompositeMatcher<HSSFWorkbook> {
    private static final Integer SHEET_INDEX_BID_INFO = 1;
    private static final Integer SHEET_INDEX_USAGE = 4;
    private static final Integer SHEET_INDEX_SPECIAL_PRICEBOOK = 5;
    private static final Integer SHEET_INDEX_ONEVOICE_CHANNEL_INFORMATION = 6;
    private static final Integer SHEET_INDEX_ONEVOICE_OPTIONS = 7;
    private final String templateFilename;
    private HSSFWorkbook pricingSheet;
    private ExcelTemplateFields templateFields;
    private BcmSheetBidInfoPageMatcher bidInfoPageMatcher;
    private BcmSheetOnevoiceOptionsPageMatcher optionsPageMatcher;
    private BcmSheetOnevoiceChannelInformationPageMatcher channelInformationPageMatcher;
    private BcmSheetUsagePageMatcher usagePageMatcher;
    private BcmSheetSpecialPriceBookPageMatcher bcmSheetSpecialPriceBookPageMatcher;

    public static BcmSheetMatcher aBcmSheet() {
        return new BcmSheetMatcher("BCM-Details.xls");
    }

    private BcmSheetMatcher(String templateFilename) {
        this.templateFilename = templateFilename;
    }

    @Override
    protected void doMatchesSafely(HSSFWorkbook workbook) {
        this.pricingSheet = workbook;
        super.doMatchesSafely(workbook);
        parseTemplate();
        doMatchPages();
    }

    private void parseTemplate() {
        try {
            HSSFWorkbook template = new HSSFWorkbook(ExcelMerge.class.getResourceAsStream(templateFilename));
            this.templateFields = new ExcelTemplateParser(template).parse();
        } catch (IOException e) {
            throw new AssertionError(String.format("Pricing Sheet template [%s] not found!", templateFilename));
        }
    }

    private void doMatchPages() {
        runSheetMatcher(SHEET_INDEX_ONEVOICE_OPTIONS, optionsPageMatcher);
        runSheetMatcher(SHEET_INDEX_USAGE, usagePageMatcher);
        runSheetMatcher(SHEET_INDEX_ONEVOICE_CHANNEL_INFORMATION, channelInformationPageMatcher);
        runSheetMatcher(SHEET_INDEX_BID_INFO, bidInfoPageMatcher);
        runSheetMatcher(SHEET_INDEX_SPECIAL_PRICEBOOK, bcmSheetSpecialPriceBookPageMatcher);
    }

    private void runSheetMatcher(int sheetIndex, ExcelSheetCompositeMatcher sheetCompositeMatcher) {
        if (sheetCompositeMatcher != null) {
            HSSFSheet sheet = pricingSheet.getSheetAt(sheetIndex);
            ExcelTemplateSheetFields sheetFields = templateFields.getSheetFields(sheetIndex);
            sheetCompositeMatcher.setExcelTemplateSheetFields(sheetFields);
            sheetCompositeMatcher.matchesSafely(sheet);
            failures.addAll(sheetCompositeMatcher.failures());
        }
    }

    public BcmSheetMatcher with(BcmSheetOnevoiceOptionsPageMatcher optionsPageMatcher) {
        this.optionsPageMatcher = optionsPageMatcher;
        return this;
    }

    public BcmSheetMatcher with(BcmSheetOnevoiceChannelInformationPageMatcher channelInformationPageMatcher) {
        this.channelInformationPageMatcher = channelInformationPageMatcher;
        return this;
    }

    public BcmSheetMatcher with(BcmSheetBidInfoPageMatcher bidInfoPageMatcher) {
        this.bidInfoPageMatcher = bidInfoPageMatcher;
        return this;
    }

    public BcmSheetMatcher with(BcmSheetUsagePageMatcher usagePageMatcher) {
        this.usagePageMatcher = usagePageMatcher;
        return this;
    }

    public BcmSheetMatcher with(BcmSheetSpecialPriceBookPageMatcher bcmSheetSpecialPriceBookPageMatcher) {
        this.bcmSheetSpecialPriceBookPageMatcher = bcmSheetSpecialPriceBookPageMatcher;
        return this;
    }
}
