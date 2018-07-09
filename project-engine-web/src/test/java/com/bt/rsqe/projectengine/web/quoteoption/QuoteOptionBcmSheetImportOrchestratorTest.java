package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.InvalidExportDataException;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMDiscountUpdater;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMExportOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.ImportDiscounts;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

public class QuoteOptionBcmSheetImportOrchestratorTest {

    @Mock
    private LineItemFacade lineItemFacade;

    @Mock
    private PmrClient pmrClient;

    @Mock
    private QuoteOptionBcmSheetExportOrchestrator quoteOptionBcmSheetExportOrchestrator;

    @Mock
    private BCMExportOrchestrator bcmExportOrchestrator;

    @Mock
    private BCMDiscountUpdater updater;

    private QuoteOptionBcmSheetImportOrchestrator quoteOptionBcmSheetImportOrchestrator;
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";

    @Before
    public void setUp() {
        initMocks(this);
        quoteOptionBcmSheetImportOrchestrator = new QuoteOptionBcmSheetImportOrchestrator(lineItemFacade, pmrClient,
                                                                                          quoteOptionBcmSheetExportOrchestrator, bcmExportOrchestrator, updater);
    }

    @Test
    public void shouldImportBCMWorkBook() {
        HSSFWorkbook exportedSheet = createWorkbookForFile("Exported_BCM_Sheet.xls");
        HSSFWorkbook importedSheet = createWorkbookForFile("Import_BCM_Sheet.xls");

        when(quoteOptionBcmSheetExportOrchestrator.renderBcmExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);
        when(bcmExportOrchestrator.renderBCMExportSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);
        quoteOptionBcmSheetImportOrchestrator.importBCMSheetDetails(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID,importedSheet);
        List<ImportDiscounts> discounts = newArrayList();
        Map<String, Double> cpeMap = newHashMap();
        cpeMap.put("7c0127442c80e32a17a50e0c7b", 0d);
        discounts.add(new ImportDiscounts("1d1c074e0597d6b32c00cfc302", 1l, cpeMap));
        Map<String, Double> licenseMap = newHashMap();
        licenseMap.put("bd7b8e409882490b2726ebcf6a", 0.03);
        discounts.add(new ImportDiscounts("1b79694f7886917e178b4bd848", 1l, licenseMap));
        Map<String, Double> licenseMap2 = newHashMap();
        licenseMap2.put("4c40ea436b8e3c6d3c91b1e055", 0.04);
        discounts.add(new ImportDiscounts("cd2d0f4563ad98adb153906dfc", 1l, licenseMap2));
        verify(updater, times(5)).updateDiscountsFrom(anyListOf(ImportDiscounts.class));
    }

    @Test
    public void shouldImportBCMWorkBookWithOutDiscounts() {
        HSSFWorkbook exportedSheet = createWorkbookForFile("Exported_BCM_Sheet_NoDiscounts.xls");
        HSSFWorkbook importedSheet = createWorkbookForFile("Import_BCM_Sheet_NoDiscounts.xls");

        when(quoteOptionBcmSheetExportOrchestrator.renderBcmExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);
        when(bcmExportOrchestrator.renderBCMExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);

        quoteOptionBcmSheetImportOrchestrator.importBCMSheetDetails(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID,importedSheet);

        verify(updater, times(0)).updateDiscountsFrom(null);
    }

    @Test(expected = InvalidExportDataException.class)
    public void shouldValidateForVersion() {
        HSSFWorkbook exportedSheet = createWorkbookForFile("Exported_BCM_Sheet_Version.xls");
        HSSFWorkbook importedSheet = createWorkbookForFile("Import_BCM_Sheet_Version.xls");

        when(quoteOptionBcmSheetExportOrchestrator.renderBcmExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);
        when(bcmExportOrchestrator.renderBCMExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);

        quoteOptionBcmSheetImportOrchestrator.importBCMSheetDetails(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, importedSheet);
    }

    @Test(expected = InvalidExportDataException.class)
    public void shouldCompareSheets() {
        HSSFWorkbook exportedSheet = createWorkbookForFile("Exported_BCM_Sheet_Compare.xls");
        HSSFWorkbook importedSheet = createWorkbookForFile("Import_BCM_Sheet_Compare.xls");

        when(quoteOptionBcmSheetExportOrchestrator.renderBcmExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);
        when(bcmExportOrchestrator.renderBCMExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);

        quoteOptionBcmSheetImportOrchestrator.importBCMSheetDetails(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, importedSheet);

    }

    @Test(expected = InvalidExportDataException.class)
    public void shouldValidateMoreThanHundredDiscount() {
        HSSFWorkbook exportedSheet = createWorkbookForFile("Exported_BCM_Sheet.xls");
        HSSFWorkbook importedSheet = createWorkbookForFile("Import_BCM_Sheet_DiscAbove100.xls");

        when(quoteOptionBcmSheetExportOrchestrator.renderBcmExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);
        when(bcmExportOrchestrator.renderBCMExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);

        quoteOptionBcmSheetImportOrchestrator.importBCMSheetDetails(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, importedSheet);

    }

    @Test
    public void shouldAllowLessThanZeroDiscount() {
        HSSFWorkbook exportedSheet = createWorkbookForFile("Exported_BCM_Sheet.xls");
        HSSFWorkbook importedSheet = createWorkbookForFile("Import_BCM_Sheet_DiscBelowZero.xls");

        when(quoteOptionBcmSheetExportOrchestrator.renderBcmExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);
        when(bcmExportOrchestrator.renderBCMExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);

        quoteOptionBcmSheetImportOrchestrator.importBCMSheetDetails(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, importedSheet);
    }

    @Test
    public void shouldAllowNegativeDiscountGreaterThanAHundredPercent() {
        HSSFWorkbook exportedSheet = createWorkbookForFile("Exported_BCM_Sheet.xls");
        HSSFWorkbook importedSheet = createWorkbookForFile("Import_BCM_Sheet_DiscGreaterThanNegative1.xls");

        when(quoteOptionBcmSheetExportOrchestrator.renderBcmExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);
        when(bcmExportOrchestrator.renderBCMExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);

        quoteOptionBcmSheetImportOrchestrator.importBCMSheetDetails(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, importedSheet);
    }

    @Test
    public void shouldSkipNonDiscountCellsDuringDiscountPercentValidation() {
        HSSFWorkbook exportedSheet = createWorkbookForFile("Exported_BCM_Sheet_SkipNonDiscounts.xls");
        HSSFWorkbook importedSheet = createWorkbookForFile("Import_BCM_Sheet_SkipNonDiscounts.xls");

        when(quoteOptionBcmSheetExportOrchestrator.renderBcmExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);
        when(bcmExportOrchestrator.renderBCMExportSheet(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID, "")).thenReturn(exportedSheet);

        quoteOptionBcmSheetImportOrchestrator.importBCMSheetDetails(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, importedSheet);
    }

    private HSSFWorkbook createWorkbookForFile(String filePath) {
        InputStream inputStream = createInputStreamForFile(filePath);
        try {
            return new HSSFWorkbook(inputStream);
        }
        catch(IOException e) {
            throw new RuntimeException("Could not create workbook for " + filePath, e);
        }
    }

    private InputStream createInputStreamForFile(String filePath) {
        InputStream stream = getClass().getResourceAsStream(filePath);
        if(stream == null) {
            throw new RuntimeException("null input stream for file: " + filePath);
        }
        return stream;
    }
}
