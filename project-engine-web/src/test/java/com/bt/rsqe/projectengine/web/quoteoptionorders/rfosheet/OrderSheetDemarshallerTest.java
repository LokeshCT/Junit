package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.domain.bom.parameters.OrderFormSignDate;
import com.bt.rsqe.fixtures.CalendarFixture;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hamcrest.Matchers;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static com.bt.rsqe.matchers.DateTimeMatcher.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

// FIXME Hugh 13/3/12 use RFOWorkbookFixture here
public class OrderSheetDemarshallerTest extends WorkbookTest {
    private Workbook workbook;
    private OrderSheetDemarshaller demarshaller;

    @Before
    public void setUp() throws Exception {
        workbook = new HSSFWorkbook();
        demarshaller = new OrderSheetDemarshaller(workbook);
    }

    @Test
    public void shouldReturnOrderDetailsModel() {
        final Sheet sheet = workbook.createSheet(OrderSheetMarshaller.SHEET_NAME);
        Date initialBillingStartDate = CalendarFixture.aCalendar().day(1).month(CalendarFixture.Month.JAN).year(2014).get().getTime();

        addOrderDetailsSheetRow(sheet, 1, "lineItemId1", "siteId1", "siteName1", "blah product1", "2012-Jan-01", "2012-Jan-01", initialBillingStartDate, "1");
        addOrderDetailsSheetRow(sheet, 2, "lineItemId2", "siteId2", "siteName2", "blah product2", "2015-Nov-11", "2015-Nov-11", initialBillingStartDate, null);
        final List<OrderSheetModel.OrderSheetRow> orderSheetRows = demarshaller.getOrderDetailsModel();

        assertThat(orderSheetRows, Matchers.hasItems(OrderSheetRowMatcher.anOrderRow()
                                                                         .withId("lineItemId1")
                                                                         .withSiteId("siteId1")
                                                                         .withSiteName("siteName1")
                                                                         .withProduct("blah product1")
                                                                         .withInitialBillingStartDate(initialBillingStartDate)
                                                                         .withBillingId("1")
                                                                         .withOrderSignedDate(aDateTime().withDayOfMonth(1).withMonth(1).withYear(2012))
                                                                         .withInitialBillingStartDate(initialBillingStartDate)
                                                                         .withCustomerRequiredDate(DateTimeFormat.forPattern("yyyy-MMM-dd").parseDateTime("2012-JAN-01").toDate()),

                                                     OrderSheetRowMatcher.anOrderRow()
                                                                         .withId("lineItemId2")
                                                                         .withSiteId("siteId2")
                                                                         .withSiteName("siteName2")
                                                                         .withProduct("blah product2")
                                                                         .withBillingId(nullValue())
                                                                         .withOrderSignedDate(aDateTime().withDayOfMonth(1).withMonth(1).withYear(2012))));
    }

    @Test
    public void shouldReturnNullButPresentInitialBillingStartDateWhenColumnIsVisibleButValueIsNull() throws Exception {
        final Sheet sheet = workbook.createSheet(OrderSheetMarshaller.SHEET_NAME);

        addOrderDetailsSheetRow(sheet, 2, "lineItemId2", "siteId2", "siteName2", "blah product2", null, null, null, null);
        final List<OrderSheetModel.OrderSheetRow> orderSheetRows = demarshaller.getOrderDetailsModel();

        assertThat(orderSheetRows.get(0).initialBillingStartDate().isPresent(), is(true));
        assertThat(orderSheetRows.get(0).initialBillingStartDate().get(), is(nullValue()));
    }

    @Test
    public void shouldReturnAbsentInitialBillingStartDateWhenColumnIsNotVisible() throws Exception {
        final Sheet sheet = workbook.createSheet(OrderSheetMarshaller.SHEET_NAME);
        sheet.setColumnHidden(OrderSheetMarshaller.Column.INITIAL_BILLING_START_DATE.column, true);

        Date initialBillingStartDate = CalendarFixture.aCalendar().day(1).month(CalendarFixture.Month.JAN).year(2014).get().getTime();

        addOrderDetailsSheetRow(sheet, 1, "lineItemId1", "siteId1", "siteName1", "blah product1", "2012-JAN-01", "2012-JAN-01", initialBillingStartDate, "1");
        final List<OrderSheetModel.OrderSheetRow> orderSheetRows = demarshaller.getOrderDetailsModel();

        assertThat(orderSheetRows.get(0).initialBillingStartDate().isPresent(), is(false));
        assertThat(orderSheetRows.get(0).initialBillingStartDate().get(), is(nullValue()));
    }
}
