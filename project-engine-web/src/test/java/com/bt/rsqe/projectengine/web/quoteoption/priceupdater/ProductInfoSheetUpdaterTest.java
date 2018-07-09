package com.bt.rsqe.projectengine.web.quoteoption.priceupdater;

import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.InvalidExportDataException;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.ProductInfoSheet;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import static com.bt.rsqe.projectengine.web.fixtures.LineItemModelFixture.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

public class ProductInfoSheetUpdaterTest {

    ProductInfoSheetUpdater updater;
    @Mock
    private LineItemFacade lineItemFacade;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    public void setUpWithValidValues() throws Exception {
        ProductInfoSheet productInfoSheet = createProductInfoSheet();
        updater = new ProductInfoSheetUpdater(productInfoSheet, lineItemFacade);
    }

    private ProductInfoSheet createProductInfoSheet() {
        HSSFSheet sheet = new HSSFWorkbook().createSheet();
        sheet.createRow(0);
        final HSSFRow row = sheet.createRow(1);
        HSSFCell firstCell = row.createCell(0);
        firstCell.setCellValue("Connect Acceleration");
        HSSFCell secondCell = row.createCell(1);
        secondCell.setCellValue(10000);
        return new ProductInfoSheet(sheet);
    }

    @Test
    public void shouldUpdateNumericValue() throws Exception {
        setUpWithValidValues();
        final String customerId = "customerId";
        LineItemModel lineItem = aLineItemModel().withCustomerId(customerId).withQuoteOptionItemDTOId("lineItemId").forProductCategory("Connect Acceleration").build();
        updater.update(lineItem);
        verify(lineItemFacade).persistMinimumRevenueCommitment("projectId", "quoteOptionId", new LineItemId("lineItemId"), "10000.0", null);
    }

    @Test
    public void shouldNotUpdateValueForDifferentProductNames() throws Exception {
        setUpWithValidValues();
        final String customerId = "customerId";
        LineItemModel lineItem = aLineItemModel().withCustomerId(customerId).withQuoteOptionItemDTOId("lineItemId").forProductCategory("Connect Optimisation").build();
        updater.update(lineItem);
        verify(lineItemFacade, never()).persistMinimumRevenueCommitment("projectId", "quoteOptionId", new LineItemId("lineItemId"), "10000.0", null);
    }

    @Test(expected = InvalidExportDataException.class)
    public void shouldThrowExceptionForNonNumeric() {
        setUpWithInvalidValues();
        final String customerId = "customerId";
        LineItemModel lineItem = aLineItemModel().withCustomerId(customerId).withQuoteOptionItemDTOId("lineItemId").forProductCategory("Connect Acceleration").build();
        updater.update(lineItem);
    }

    private void setUpWithInvalidValues() {
        HSSFSheet sheet = new HSSFWorkbook().createSheet();
        final HSSFRow row = sheet.createRow(1);
        HSSFCell firstCell = row.createCell(0);
        firstCell.setCellValue("Connect Acceleration");
        HSSFCell secondCell = row.createCell(1);
        secondCell.setCellValue("100A0");
        ProductInfoSheet productInfoSheet = new ProductInfoSheet(sheet);
        updater = new ProductInfoSheetUpdater(productInfoSheet, lineItemFacade);
    }

}
