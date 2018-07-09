package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.projectengine.web.AssetKeyContainer;
import com.bt.rsqe.projectengine.web.ImportResults;
import com.google.common.base.Optional;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.google.common.collect.Lists.*;
import static org.mockito.Mockito.*;

public class ECRFSheetOrchestratorTest {
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String QUOTE_OPTION_ID = "QUOTE_OPTION_ID";
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String CONTRACT_TERM = "12";
    private static Workbook importXLSWorkBook;
    private static Workbook importXlSXWorkBook;
    private static ECRFSheetOrchestrator ecrfSheetOrchestrator;
    private static ECRFSheetModelBuilder ecrfSheetModelBuilder;
    private static ProductBasedImporter productBasedImporter;
    private static LineItemBasedImporter lineItemBasedImporter;
    private static ProductInstanceClient productInstanceClient;

    private static final LineItemId LINE_ITEM_ID = new LineItemId("LineItemId");
    private static final String PRODUCT_CODE_IMPORTABLE = "S0308545";

    @Before
    public void before() throws IOException, InvalidFormatException {
        ecrfSheetModelBuilder = mock(ECRFSheetModelBuilder.class);
        productInstanceClient = mock(ProductInstanceClient.class);
        productBasedImporter = mock(ProductBasedImporter.class);
        lineItemBasedImporter = mock(LineItemBasedImporter.class);
        ecrfSheetOrchestrator = new ECRFSheetOrchestrator(ecrfSheetModelBuilder, productBasedImporter, lineItemBasedImporter);
        importXLSWorkBook = WorkbookFactory.create(ECRFSheetOrchestratorTest.class.getResourceAsStream("occ_test_upload.xls"));
        importXlSXWorkBook = WorkbookFactory.create(ECRFSheetOrchestratorTest.class.getResourceAsStream("occ_test_upload.xlsx"));
    }

    @Test
    public void shouldImportLineItemBasedXLSECRFSheet() throws Exception {

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel().withSheetIndex(1).withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                                   .withAttributes(newArrayList(
                                                                                                                       new ECRFSheetModelAttribute("", "")))
                                                                                                                   .build())
                                                   .build();
        ECRFWorkBook workBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults results = new ImportResults();
        when(ecrfSheetModelBuilder.build(PRODUCT_CODE_IMPORTABLE, importXLSWorkBook)).thenReturn(workBook);
        ecrfSheetOrchestrator.importUsingLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID.toString(), importXLSWorkBook, results, PRODUCT_CODE_IMPORTABLE, false, ProductCategoryCode.NIL);
        verify(ecrfSheetModelBuilder).build(PRODUCT_CODE_IMPORTABLE, importXLSWorkBook);
        verify(lineItemBasedImporter).importFromSheet(eq(CUSTOMER_ID), eq(CONTRACT_ID), anyString(), eq(PROJECT_ID), eq(QUOTE_OPTION_ID),
                                                      any(ECRFWorkBook.class), any(ImportResults.class), any(AssetKeyContainer.class), eq(PRODUCT_CODE_IMPORTABLE), any(Optional.class), any(Boolean.class), any(ProductCategoryCode.class));
    }


    @Test
    public void shouldImportProductBasedBulkXLSECRFSheet() throws Exception {
        ImportResults results = new ImportResults();
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel().withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                 .withAttributes(newArrayList(
                                                                                                     new ECRFSheetModelAttribute("", "")))
                                                                                                 .build())
                                                   .build();
        ECRFWorkBook workBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();

        when(ecrfSheetModelBuilder.build(PRODUCT_CODE_IMPORTABLE, importXLSWorkBook)).thenReturn(workBook);
        ecrfSheetOrchestrator.importUsingProduct(CUSTOMER_ID, CONTRACT_ID, CONTRACT_TERM, PROJECT_ID, QUOTE_OPTION_ID, importXLSWorkBook, PRODUCT_CODE_IMPORTABLE, results, false, ProductCategoryCode.NIL);
        verify(ecrfSheetModelBuilder).build(PRODUCT_CODE_IMPORTABLE, importXLSWorkBook);
        verify(productBasedImporter).importFromSheet(eq(CUSTOMER_ID), eq(CONTRACT_ID),eq(CONTRACT_TERM), eq(PROJECT_ID), eq(QUOTE_OPTION_ID),
                                              eq(workBook), eq(results), any(AssetKeyContainer.class), eq(PRODUCT_CODE_IMPORTABLE), any(Optional.class), any(Boolean.class), any(ProductCategoryCode.class));
    }

    @Test
    public void shouldImportXLSXECRFSheet() throws Exception {

        ImportResults results = new ImportResults();
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel().withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                 .withAttributes(newArrayList(
                                                                                                     new ECRFSheetModelAttribute("", "")))
                                                                                                 .build())
                                                   .build();
        ECRFWorkBook workBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();

        when(ecrfSheetModelBuilder.build(PRODUCT_CODE_IMPORTABLE, importXlSXWorkBook)).thenReturn(workBook);
        ecrfSheetOrchestrator.importUsingLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID.toString(), importXlSXWorkBook, results, PRODUCT_CODE_IMPORTABLE, false, ProductCategoryCode.NIL);
        verify(ecrfSheetModelBuilder).build(PRODUCT_CODE_IMPORTABLE, importXlSXWorkBook);
        verify(lineItemBasedImporter).importFromSheet(eq(CUSTOMER_ID), eq(CONTRACT_ID), anyString(), eq(PROJECT_ID), eq(QUOTE_OPTION_ID),
                                                      eq(workBook), eq(results), any(AssetKeyContainer.class), eq(PRODUCT_CODE_IMPORTABLE), any(Optional.class), any(Boolean.class), any(ProductCategoryCode.class));
    }

    @Test
    public void shouldImportBulkXLSXECRFSheet() throws Exception {

        ImportResults results = new ImportResults();
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel().withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                 .withAttributes(newArrayList(
                                                                                                     new ECRFSheetModelAttribute("", "")))
                                                                                                 .build())
                                                   .build();
        ECRFWorkBook workBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();

        when(ecrfSheetModelBuilder.build(PRODUCT_CODE_IMPORTABLE, importXlSXWorkBook)).thenReturn(workBook);
        ecrfSheetOrchestrator.importUsingProduct(CUSTOMER_ID, CONTRACT_ID, CONTRACT_TERM, PROJECT_ID, QUOTE_OPTION_ID, importXlSXWorkBook, PRODUCT_CODE_IMPORTABLE, results, false, ProductCategoryCode.NIL);
        verify(ecrfSheetModelBuilder).build(PRODUCT_CODE_IMPORTABLE, importXlSXWorkBook);
        verify(productBasedImporter).importFromSheet(eq(CUSTOMER_ID), eq(CONTRACT_ID),eq(CONTRACT_TERM), eq(PROJECT_ID), eq(QUOTE_OPTION_ID),
                                                      eq(workBook), eq(results), any(AssetKeyContainer.class), eq(PRODUCT_CODE_IMPORTABLE), any(Optional.class), any(Boolean.class), any(ProductCategoryCode.class));
    }

    @Test
    public void shouldImportBulkXLSXECRFSheetForMigration() throws Exception {

        ImportResults results = new ImportResults();
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel().withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                 .withAttributes(newArrayList(
                                                                                                     new ECRFSheetModelAttribute("", "")))
                                                                                                 .build())
                                                   .build();
        ECRFWorkBook workBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();

        when(ecrfSheetModelBuilder.build(PRODUCT_CODE_IMPORTABLE, importXlSXWorkBook)).thenReturn(workBook);
        ecrfSheetOrchestrator.importUsingProduct(CUSTOMER_ID, CONTRACT_ID, CONTRACT_TERM, PROJECT_ID, QUOTE_OPTION_ID, importXlSXWorkBook, PRODUCT_CODE_IMPORTABLE, results, true, ProductCategoryCode.NIL);
        verify(ecrfSheetModelBuilder).build(PRODUCT_CODE_IMPORTABLE, importXlSXWorkBook);
        verify(productBasedImporter).importFromSheet(eq(CUSTOMER_ID), eq(CONTRACT_ID),eq(CONTRACT_TERM), eq(PROJECT_ID), eq(QUOTE_OPTION_ID),
                                                     eq(workBook), eq(results), any(AssetKeyContainer.class), eq(PRODUCT_CODE_IMPORTABLE), any(Optional.class), any(Boolean.class), any(ProductCategoryCode.class));
    }
}
