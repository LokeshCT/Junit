package com.bt.rsqe.matchers.excel.pricing;

import com.bt.rsqe.matchers.CompositeMatcher;
import com.bt.rsqe.matchers.excel.ExcelSheetCompositeMatcher;
import com.bt.rsqe.matchers.excel.ExcelTemplateFields;
import com.bt.rsqe.matchers.excel.ExcelTemplateParser;
import com.bt.rsqe.matchers.excel.ExcelTemplateSheetFields;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetStrategy;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;

public class PricingSheetMatcher extends CompositeMatcher<Workbook> {
    private static final Integer SHEET_INDEX_FRONT_PAGE = 0;
    private static final Integer SHEET_INDIRECT_CA_SUMMARY_PRICING_PAGE = 1;
    private static final Integer SHEET_INDIRECT_CA_DETAILED_PRICING_PAGE = 2;
    private static final Integer SHEET_INDEX_TRAFFIC_MATRIX_PAGE = 3;
    private static final Integer SHEET_INDEX_SITES_PAGE = 3;
    private static final Integer SHEET_CONFIG_PRICING_PAGE = 2;
    private final String templateFilename;
    private Workbook pricingSheet;
    private ExcelTemplateFields templateFields;
    private PricingSheetFrontPageMatcher frontPageMatcher;
    private PricingSheetSitesPageMatcher sitesPageMatcher;
    private PricingSheetTrafficMatrixPageMatcher trafficMatrixPageMatcher;
    private PricingConfigSheetMatcher configPricingMatcher;
    private IndirectPricingCADetailedPageMatcher indirectCADetailedMatcher;
    private IndirectPricingCASummaryPageMatcher indirectCASummaryPageMatcher;

    public static PricingSheetMatcher aDirectPricingSheet() {
        return new PricingSheetMatcher(PricingSheetStrategy.DIRECT.template());
    }

    public static PricingSheetMatcher anIndirectPricingSheet() {
        return new PricingSheetMatcher(PricingSheetStrategy.INDIRECT.template());
    }

    private PricingSheetMatcher(String templateFilename) {
        this.templateFilename = templateFilename;
    }

    protected void doMatchesSafely(Workbook pricingSheet) {
        this.pricingSheet = pricingSheet;
        super.doMatchesSafely(pricingSheet);
        parseTemplate();
        doMatchFrontPage();
        doMatchSitesPage();
        doMatchConfigPricingPage();
        doMatchTrafficMatrixPage();
        doMatchIndirectCADetailedPricingPage();
        doMatchIndirectCASummaryPricingPage();
    }

    private void parseTemplate() {
        try {
            Workbook template = new HSSFWorkbook(getClass().getClassLoader().getResourceAsStream(templateFilename));
            this.templateFields = new ExcelTemplateParser(template).parse();
        } catch (IOException e) {
            throw new AssertionError(String.format("Pricing Sheet template [%s] not found!", templateFilename));
        }
    }

    private void doMatchFrontPage() {
        runSheetMatcher(SHEET_INDEX_FRONT_PAGE, frontPageMatcher);
    }

    private void doMatchSitesPage() {
        runSheetMatcher(SHEET_INDEX_SITES_PAGE, sitesPageMatcher);
    }

    private void doMatchConfigPricingPage() {
        runSheetMatcher(SHEET_CONFIG_PRICING_PAGE, configPricingMatcher);
    }

    private void doMatchTrafficMatrixPage() {
        runSheetMatcher(SHEET_INDEX_TRAFFIC_MATRIX_PAGE, trafficMatrixPageMatcher);
    }

    private void doMatchIndirectCADetailedPricingPage() {
        runSheetMatcher(SHEET_INDIRECT_CA_DETAILED_PRICING_PAGE, indirectCADetailedMatcher);
    }

    private void doMatchIndirectCASummaryPricingPage() {
        runSheetMatcher(SHEET_INDIRECT_CA_SUMMARY_PRICING_PAGE, indirectCASummaryPageMatcher);
    }

    private void runSheetMatcher(int sheetIndex, ExcelSheetCompositeMatcher sheetCompositeMatcher) {
        if (sheetCompositeMatcher != null) {
            Sheet sheet = pricingSheet.getSheetAt(sheetIndex);
            ExcelTemplateSheetFields sheetFields = templateFields.getSheetFields(sheetIndex);
            sheetCompositeMatcher.setExcelTemplateSheetFields(sheetFields);
            sheetCompositeMatcher.matchesSafely(sheet);
            failures.addAll(sheetCompositeMatcher.failures());
        }
    }

    public PricingSheetMatcher with(PricingSheetFrontPageMatcher frontPageMatcher) {
        this.frontPageMatcher = frontPageMatcher;
        return this;
    }

    public PricingSheetMatcher with(PricingSheetSitesPageMatcher sitesPageMatcher) {
        this.sitesPageMatcher = sitesPageMatcher;
        return this;
    }

     public PricingSheetMatcher withConfigSheet(PricingConfigSheetMatcher configPricingMatcher) {
        this.configPricingMatcher = configPricingMatcher;
        return this;
    }

    public PricingSheetMatcher with(PricingSheetTrafficMatrixPageMatcher trafficMatrixPageMatcher) {
        this.trafficMatrixPageMatcher = trafficMatrixPageMatcher;
        return this;
    }

    public PricingSheetMatcher with(IndirectPricingCADetailedPageMatcher indirectCaDetailedPageMatcher) {
        this.indirectCADetailedMatcher = indirectCaDetailedPageMatcher;
        return this;
    }

    public PricingSheetMatcher with(IndirectPricingCASummaryPageMatcher indirectCASummaryPageMatcher) {
        this.indirectCASummaryPageMatcher = indirectCASummaryPageMatcher;
        return this;
    }
}
