package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.customerrecord.BillingAccountDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture;
import com.bt.rsqe.domain.bom.parameters.OrderFormSignDate;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.excel.ExcelStyler;
import com.bt.rsqe.excel.ExcelUtil;
import com.bt.rsqe.fixtures.CalendarFixture;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.utils.NullableOptional;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.bt.rsqe.matchers.ReflectionEqualsMatcher.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OrderSheetMarshallerTest {
    private static final String PROJECT_ID = "aProjectId";
    private static final String QUOTE_OPTION_ID = "aQuoteOptionId";

    private LineItemModel lineItemModel1;
    private LineItemModel lineItemModel2;
    private DateTime signedOnDate;
    private OrderSheetColumnManager orderSheetColumnManager;
    private QuoteMigrationDetailsProvider quoteMigrationDetailsProvider;
    private ProductOffering productOffering;

    @Before
    public void before() throws Exception {
        lineItemModel1 = mock(LineItemModel.class);
        lineItemModel2 = mock(LineItemModel.class);
        productOffering = mock(ProductOffering.class);
        final SiteDTO site1 = new SiteDTO() {{
            bfgSiteID = "blahSiteId1";
            name = "blahSiteName1";
        }};
        final SiteDTO site2 = new SiteDTO() {{
            bfgSiteID = "blahSiteId2";
            name = "blahSiteName2";
        }};

        when(lineItemModel1.getId()).thenReturn("lineItemId1");
        when(lineItemModel1.getSite()).thenReturn(site1);
        when(lineItemModel1.getSummary()).thenReturn("summary");
        when(lineItemModel1.getBillingId()).thenReturn("billingId1");
        when(lineItemModel1.getProductName()).thenReturn("blahProduct1");
        when(lineItemModel1.getDisplayName()).thenReturn("blahProductDisplay");
        when(lineItemModel1.getCustomerRequiredDate()).thenReturn(null);
        when(lineItemModel1.getAction()).thenReturn("Provide");
        when(lineItemModel1.getInitialBillingStartDate()).thenReturn(CalendarFixture.aCalendar().day(1).month(CalendarFixture.Month.JAN).year(2014).get().getTime());

        when(lineItemModel2.getId()).thenReturn("lineItemId2");
        when(lineItemModel2.getSite()).thenReturn(site2);
        when(lineItemModel2.getSummary()).thenReturn("summary");
        when(lineItemModel2.getBillingId()).thenReturn("billingId2");
        when(lineItemModel2.getProductName()).thenReturn("blahProduct2");
        when(lineItemModel2.getDisplayName()).thenReturn("blahProductDisplay");
        when(lineItemModel2.getAction()).thenReturn("Provide");
        when(lineItemModel2.getCustomerRequiredDate()).thenReturn(JaxbDateTime.valueOf(new DateTime()));

        signedOnDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime("2001-12-12 00:00:00");

        quoteMigrationDetailsProvider = mock(QuoteMigrationDetailsProvider.class);
        when(quoteMigrationDetailsProvider.isMigrationQuote(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(Optional.of(false));
        orderSheetColumnManager = new OrderSheetColumnManager(PROJECT_ID, QUOTE_OPTION_ID, quoteMigrationDetailsProvider);
    }

    @Test
    public void shouldMarshallModelIntoAWorkbook() throws Exception {
        SalesRelationshipFixture salesRelationshipFixture = new SalesRelationshipFixture();
        SalesRelationship salesRelationship = salesRelationshipFixture.build();
        List<SalesRelationship> salesRelationshipList = new ArrayList<SalesRelationship>();
        salesRelationshipList.add(salesRelationship);
        when(lineItemModel1.getProductOffering()).thenReturn(productOffering);
        when(lineItemModel2.getProductOffering()).thenReturn(productOffering);
        when(productOffering.getSalesRelationships()).thenReturn(salesRelationshipList);
        final XSSFWorkbook workbook = new XSSFWorkbook();
        final HashMap<String,String> billingIdMap = newHashMap();
        billingIdMap.put("lineItemId1","billingId1");
        billingIdMap.put("lineItemId2","billingId2");
        final OrderSheetModel model = new OrderSheetModel(newArrayList(lineItemModel1, lineItemModel2),
                                                          asList(new BillingAccountDTO("billingId1", "A1", "USD"), new BillingAccountDTO("billingId2", "A2", "USD")),
                                                          signedOnDate,null, billingIdMap, "");
        new OrderSheetMarshaller(model, workbook, new ExcelStyler(workbook), orderSheetColumnManager).marshall();

        final XSSFSheet sheet = workbook.getSheet(OrderSheetMarshaller.SHEET_NAME);
        assertThat(sheet.getLastRowNum(), is(model.rows().size()));
        assertThat(sheet, is(notNullValue()));

        final Iterator<Row> rowIterator = sheet.rowIterator();
        validateHeaders(rowIterator.next());
        validateBody(rowIterator, model);
        validateColumnStyle(sheet);
        validateHiddenColumn(sheet, OrderSheetMarshaller.Column.LINE_ITEM_ID.column, false);
        validateHiddenColumn(sheet, OrderSheetMarshaller.Column.INITIAL_BILLING_START_DATE.column, false);
    }

    @Test
    public void shouldShowInitialBillingStartDateColumnWhenQuoteIsAMigrationQuote() throws Exception {
        SalesRelationshipFixture salesRelationshipFixture = new SalesRelationshipFixture();
        SalesRelationship salesRelationship = salesRelationshipFixture.build();
        List<SalesRelationship> salesRelationshipList = new ArrayList<SalesRelationship>();
        salesRelationshipList.add(salesRelationship);
        when(lineItemModel1.getProductOffering()).thenReturn(productOffering);
        when(productOffering.getSalesRelationships()).thenReturn(salesRelationshipList);
        when(quoteMigrationDetailsProvider.isMigrationQuote(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(Optional.of(true));

        final XSSFWorkbook workbook = new XSSFWorkbook();
        final OrderSheetModel model = new OrderSheetModel(newArrayList(lineItemModel1),
                                                          asList(new BillingAccountDTO("billingId1", "A1", "USD")),
                                                          signedOnDate,null, Maps.<String, String>newHashMap(), "");
        new OrderSheetMarshaller(model, workbook, new ExcelStyler(workbook), orderSheetColumnManager).marshall();

        final XSSFSheet sheet = workbook.getSheet(OrderSheetMarshaller.SHEET_NAME);
        validateHiddenColumn(sheet, OrderSheetMarshaller.Column.INITIAL_BILLING_START_DATE.column, true);
    }

    private void validateColumnStyle(XSSFSheet sheet) {
        assertThat(sheet.getColumnStyle(5).getDataFormatString(), is("YYYY-MMM-DD"));
        assertThat(sheet.getColumnStyle(6).getDataFormatString(), is("YYYY-MMM-DD"));
    }

    private void validateHeaders(Row row) {
        for (OrderSheetMarshaller.Column column : OrderSheetMarshaller.Column.values()) {
            assertThat(row.getCell(column.column).getStringCellValue(), is(column.header));
        }
    }

    private void validateBody(Iterator<Row> rows, OrderSheetModel model) {
        while (rows.hasNext()) {
            final Row data = rows.next();
            final OrderSheetModel.OrderSheetRow expected = new OrderSheetModel.OrderSheetRow(data.getCell(OrderSheetMarshaller.Column.LINE_ITEM_ID.column).getStringCellValue(),
                                                                                             data.getCell(OrderSheetMarshaller.Column.SITE_ID.column).getStringCellValue(),
                                                                                             data.getCell(OrderSheetMarshaller.Column.SITE_NAME.column).getStringCellValue(),
                                                                                             data.getCell(OrderSheetMarshaller.Column.SUMMARY.column).getStringCellValue(),
                                                                                             data.getCell(OrderSheetMarshaller.Column.PRODUCT_NAME.column).getStringCellValue(),
                                                                                             data.getCell(OrderSheetMarshaller.Column.SUBLOCATION_NAME.column).getStringCellValue(),
                                                                                             data.getCell(OrderSheetMarshaller.Column.ROOM.column).getStringCellValue(),
                                                                                             data.getCell(OrderSheetMarshaller.Column.FLOOR.column).getStringCellValue(),
                                                                                             OrderFormSignDate.newInstance(ExcelUtil.getDateValueAtCell(data, OrderSheetMarshaller.Column.SIGNED_DATE.column)),
                                                                                             data.getCell(OrderSheetMarshaller.Column.BILLING_ID.column).toString(),
                                                                                             ExcelUtil.getDateValueAtCell(data, OrderSheetMarshaller.Column.CUSTOMER_REQUIRED_DATE.column) != null
                                                                                                 ? JaxbDateTime.valueOf(ExcelUtil.getDateValueAtCell(data, OrderSheetMarshaller.Column.CUSTOMER_REQUIRED_DATE.column))
                                                                                                 : null,
                                                                                             NullableOptional.of(ExcelUtil.getSimpleDateValueAtCell(data, OrderSheetMarshaller.Column.INITIAL_BILLING_START_DATE.column)), "Provide", "");
            assertThat(model.rows(), hasItem(reflectionEquals(expected)));
        }
    }

    private void validateHiddenColumn(XSSFSheet sheet, int columnIndex, boolean expectVisible) {
        assertThat(!sheet.isColumnHidden(columnIndex), is(expectVisible));
    }
}
