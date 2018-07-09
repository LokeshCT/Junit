package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;


import com.bt.rsqe.Money;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.*;

public class OneVoiceBcmOptionsSheetTest {

    private OneVoiceBcmOptionsSheet sheet;

    @Test(expected = InvalidExportDataException.class)
    public void shouldThrowInvalidDataExceptionIfSiteIdIsNotNumeric() throws Exception {
        sheet = new OneVoiceBcmOptionsSheet(mockBcmSheet().getSheet("Non Numeric Site Id"));
        sheet.containsSiteId("3");
    }

    @Test
    public void shouldNotReturnAnEmptyRow() throws Exception {
        sheet = new OneVoiceBcmOptionsSheet(mockBcmSheet().getSheet("Empty row"));
        sheet.containsSiteId("3");
        sheet.containsSiteId("4");
        sheet.containsSiteId("5");
    }

    @Test
    public void shouldReturnZeroIfVpnConfigDiscountIsEmpty() throws Exception {
        sheet = new OneVoiceBcmOptionsSheet(mockBcmSheet().getSheet("Invalid Row"));
        final OneVoiceBcmOptionsRow oneVoiceBcmOptionsRow = sheet.rowForSiteId("3");
        assertThat(oneVoiceBcmOptionsRow.vpnConfigDiscount(), Is.is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnZeroIfDialplanChangeConfigDiscountIsEmpty() throws Exception {
        sheet = new OneVoiceBcmOptionsSheet(mockBcmSheet().getSheet("Invalid Row"));
        final OneVoiceBcmOptionsRow oneVoiceBcmOptionsRow = sheet.rowForSiteId("3");
        assertThat(oneVoiceBcmOptionsRow.dialplanChangeConfigDiscount(), Is.is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnZeroIfmmacConfigDiscountIsEmpty() throws Exception {
        sheet = new OneVoiceBcmOptionsSheet(mockBcmSheet().getSheet("Invalid Row"));
        final OneVoiceBcmOptionsRow oneVoiceBcmOptionsRow = sheet.rowForSiteId("3");
        assertThat(oneVoiceBcmOptionsRow.mmacConfigDiscount(), Is.is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnZeroIfAmendmentChargeIsEmpty() throws Exception {
        sheet = new OneVoiceBcmOptionsSheet(mockBcmSheet().getSheet("Invalid Row"));
        final OneVoiceBcmOptionsRow oneVoiceBcmOptionsRow = sheet.rowForSiteId("3");
        assertThat(oneVoiceBcmOptionsRow.amendmentCharge(), Is.is(Money.ZERO));
    }

    @Test
    public void shouldReturnZeroIfCancellationChargeIsEmpty() throws Exception {
        sheet = new OneVoiceBcmOptionsSheet(mockBcmSheet().getSheet("Invalid Row"));
        final OneVoiceBcmOptionsRow oneVoiceBcmOptionsRow = sheet.rowForSiteId("3");
        assertThat(oneVoiceBcmOptionsRow.cancellationCharge(), Is.is(Money.ZERO));
    }

    @Test
    public void shouldReturnTrueIfAmendmentChargeIsEmpty() throws Exception {
        sheet = new OneVoiceBcmOptionsSheet(mockBcmSheet().getSheet("Invalid Row"));
        final OneVoiceBcmOptionsRow oneVoiceBcmOptionsRow = sheet.rowForSiteId("3");
        assertThat(oneVoiceBcmOptionsRow.hasAmendmentCharge(), Is.is(true));
    }

    @Test
    public void shouldReturnTrueIfCancellationChargeIsEmpty() throws Exception {
        sheet = new OneVoiceBcmOptionsSheet(mockBcmSheet().getSheet("Invalid Row"));
        final OneVoiceBcmOptionsRow oneVoiceBcmOptionsRow = sheet.rowForSiteId("3");
        assertThat(oneVoiceBcmOptionsRow.hasCancellationCharge(), Is.is(true));
    }

    @Test
    public void shouldGetCorrectValueFromAmendmentCharge() throws Exception {
        sheet = new OneVoiceBcmOptionsSheet(mockBcmSheet().getSheet("Invalid Row"));
        final OneVoiceBcmOptionsRow oneVoiceBcmOptionsRow = sheet.rowForSiteId("5");
        assertThat(oneVoiceBcmOptionsRow.amendmentCharge(), Is.is(Money.from("1")));
    }

    @Test
    public void shouldGetCorrectValueFromCancellationCharge() throws Exception {
        sheet = new OneVoiceBcmOptionsSheet(mockBcmSheet().getSheet("Invalid Row"));
        final OneVoiceBcmOptionsRow oneVoiceBcmOptionsRow = sheet.rowForSiteId("5");
        assertThat(oneVoiceBcmOptionsRow.cancellationCharge(), Is.is(Money.from("2")));
    }

    private HSSFWorkbook mockBcmSheet() throws IOException {
        return new HSSFWorkbook(this.getClass().getResourceAsStream("test-onevoice-options.xls"));
    }

}
