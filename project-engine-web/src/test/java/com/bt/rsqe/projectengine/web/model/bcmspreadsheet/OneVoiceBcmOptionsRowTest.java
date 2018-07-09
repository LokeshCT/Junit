package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import com.bt.rsqe.Money;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class OneVoiceBcmOptionsRowTest {

    private static OneVoiceBcmOptionsRow oneVoiceBcmOptionsRow;
    private static OneVoiceBcmOptionsRow rowWithNonNumericData;
    private static OneVoiceBcmOptionsRow rowWithBlankData;
    private static OneVoiceBcmOptionsRow rowWithNullCells;
    private static OneVoiceBcmOptionsRow rowWithNullData;

    @BeforeClass
    public static void beforeClass() {
        HSSFSheet sheet = new HSSFWorkbook().createSheet();

        HSSFRow numericRow = sheet.createRow(0);
        numericRow.createCell(OneVoiceBcmOptionsRow.SITE_ID, Cell.CELL_TYPE_NUMERIC).setCellValue(3);
        numericRow.createCell(OneVoiceBcmOptionsRow.VPN_CONFIG_DISCOUNT, Cell.CELL_TYPE_NUMERIC).setCellValue(2.3);
        numericRow.createCell(OneVoiceBcmOptionsRow.VPN_SUBSCRIPTION_DISCOUNT, Cell.CELL_TYPE_NUMERIC).setCellValue(1);
        numericRow.createCell(OneVoiceBcmOptionsRow.DIALPLAN_CHANGE_DISCOUNT, Cell.CELL_TYPE_NUMERIC).setCellValue(2);
        numericRow.createCell(OneVoiceBcmOptionsRow.MMAC_CONFIG_DISCOUNT, Cell.CELL_TYPE_NUMERIC).setCellValue(3);
        numericRow.createCell(OneVoiceBcmOptionsRow.AMENDMENT_CHARGE, Cell.CELL_TYPE_NUMERIC).setCellValue(3.6);
        numericRow.createCell(OneVoiceBcmOptionsRow.CANCELLATION_CHARGE, Cell.CELL_TYPE_NUMERIC).setCellValue(4.6);

        oneVoiceBcmOptionsRow = new OneVoiceBcmOptionsRow(numericRow);


        HSSFRow nonNumericRow = sheet.createRow(1);
        nonNumericRow.createCell(OneVoiceBcmOptionsRow.SITE_ID, Cell.CELL_TYPE_STRING).setCellValue("text");
        nonNumericRow.createCell(OneVoiceBcmOptionsRow.VPN_CONFIG_DISCOUNT, Cell.CELL_TYPE_STRING).setCellValue("String");
        nonNumericRow.createCell(OneVoiceBcmOptionsRow.VPN_SUBSCRIPTION_DISCOUNT, Cell.CELL_TYPE_STRING).setCellValue("String");
        nonNumericRow.createCell(OneVoiceBcmOptionsRow.DIALPLAN_CHANGE_DISCOUNT, Cell.CELL_TYPE_STRING).setCellValue("String");
        nonNumericRow.createCell(OneVoiceBcmOptionsRow.MMAC_CONFIG_DISCOUNT, Cell.CELL_TYPE_STRING).setCellValue("String");
        nonNumericRow.createCell(OneVoiceBcmOptionsRow.AMENDMENT_CHARGE, Cell.CELL_TYPE_STRING).setCellValue("String");
        nonNumericRow.createCell(OneVoiceBcmOptionsRow.CANCELLATION_CHARGE, Cell.CELL_TYPE_STRING).setCellValue("String");
        rowWithNonNumericData = new OneVoiceBcmOptionsRow(nonNumericRow);


        HSSFRow nullRow = sheet.createRow(2);
        rowWithNullCells = new OneVoiceBcmOptionsRow(nullRow);

        HSSFRow nullDataRow = sheet.createRow(3);
        nullDataRow.createCell(OneVoiceBcmOptionsRow.SITE_ID, Cell.CELL_TYPE_STRING);
        nullDataRow.createCell(OneVoiceBcmOptionsRow.VPN_CONFIG_DISCOUNT, Cell.CELL_TYPE_STRING);
        nullDataRow.createCell(OneVoiceBcmOptionsRow.VPN_SUBSCRIPTION_DISCOUNT, Cell.CELL_TYPE_STRING);
        nullDataRow.createCell(OneVoiceBcmOptionsRow.DIALPLAN_CHANGE_DISCOUNT, Cell.CELL_TYPE_STRING);
        nullDataRow.createCell(OneVoiceBcmOptionsRow.MMAC_CONFIG_DISCOUNT, Cell.CELL_TYPE_STRING);
        nullDataRow.createCell(OneVoiceBcmOptionsRow.AMENDMENT_CHARGE, Cell.CELL_TYPE_STRING);
        nullDataRow.createCell(OneVoiceBcmOptionsRow.CANCELLATION_CHARGE, Cell.CELL_TYPE_STRING);
        rowWithNullData = new OneVoiceBcmOptionsRow(nullDataRow);

        HSSFRow blankDataRow = sheet.createRow(4);
        blankDataRow.createCell(OneVoiceBcmOptionsRow.SITE_ID, Cell.CELL_TYPE_BLANK);
        blankDataRow.createCell(OneVoiceBcmOptionsRow.VPN_CONFIG_DISCOUNT, Cell.CELL_TYPE_BLANK);
        blankDataRow.createCell(OneVoiceBcmOptionsRow.VPN_SUBSCRIPTION_DISCOUNT, Cell.CELL_TYPE_BLANK);
        blankDataRow.createCell(OneVoiceBcmOptionsRow.DIALPLAN_CHANGE_DISCOUNT, Cell.CELL_TYPE_BLANK);
        blankDataRow.createCell(OneVoiceBcmOptionsRow.MMAC_CONFIG_DISCOUNT, Cell.CELL_TYPE_BLANK);
        blankDataRow.createCell(OneVoiceBcmOptionsRow.AMENDMENT_CHARGE, Cell.CELL_TYPE_BLANK);
        blankDataRow.createCell(OneVoiceBcmOptionsRow.CANCELLATION_CHARGE, Cell.CELL_TYPE_BLANK);
        rowWithBlankData = new OneVoiceBcmOptionsRow(blankDataRow);
    }

    @Test
    public void shouldReturnSiteId() throws Exception {
        assertThat(oneVoiceBcmOptionsRow.siteId(), is("3"));
    }

    @Test(expected = InvalidExportDataException.class)
    public void shouldThrowExceptionWhenSiteIdIsNonNumeric() throws Exception {
        rowWithNonNumericData.siteId();
    }

    @Test
    public void shouldReturnEmptyStringWhenSiteIdCellIsNull() throws Exception {
        assertThat(rowWithNullCells.siteId(), is(""));
    }

    @Test
    public void shouldReturnEmptyStringWhenSiteIdIsNull() throws Exception {
        assertThat(rowWithNullData.siteId(), is(""));
    }

    @Test
    public void shouldReturnEmptyStringWhenSiteIdCellIsBlank() throws Exception {
        assertThat(rowWithBlankData.siteId(), is(""));
    }

    @Test
    public void shouldReturnVpnConfigDiscount() throws Exception {
        assertThat(oneVoiceBcmOptionsRow.vpnConfigDiscount(), is(BigDecimal.valueOf(230)));
    }

    @Test(expected = InvalidExportDataException.class)
    public void shouldThrowExceptionWhenVpnConfigDiscountIsNonNumeric() throws Exception {
        rowWithNonNumericData.vpnConfigDiscount();
    }

    @Test
    public void shouldReturnZeroWhenVpnConfigDiscountCellIsNull() throws Exception {
        assertThat(rowWithNullCells.vpnConfigDiscount(), is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnZeroWhenVpnConfigDiscountIsNull() throws Exception {
        assertThat(rowWithNullData.vpnConfigDiscount(), is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnZeroWhenVpnConfigDiscountCellIsBlank() throws Exception {
        assertThat(rowWithBlankData.vpnConfigDiscount(), is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnSubscriptionDiscount() throws Exception {
        assertThat(oneVoiceBcmOptionsRow.vpnSubscriptionDiscount(), is(BigDecimal.valueOf(100)));
    }

    @Test(expected = InvalidExportDataException.class)
    public void shouldThrowExceptionWhenSubscriptionDiscountIsNonNumeric() throws Exception {
        rowWithNonNumericData.vpnSubscriptionDiscount();
    }

    @Test
    public void shouldReturnZeroWhenSubscriptionDiscountCellIsNull() throws Exception {
        assertThat(rowWithNullCells.vpnSubscriptionDiscount(), is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnZeroWhenSubscriptionDiscountIsNull() throws Exception {
        assertThat(rowWithNullData.vpnSubscriptionDiscount(), is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnZeroWhenSubscriptionDiscountCellIsBlank() throws Exception {
        assertThat(rowWithBlankData.vpnSubscriptionDiscount(), is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnDialplanChangeConfigDiscount() throws Exception {
        assertThat(oneVoiceBcmOptionsRow.dialplanChangeConfigDiscount(), is(BigDecimal.valueOf(200)));
    }

    @Test(expected = InvalidExportDataException.class)
    public void shouldThrowExceptionWhenDialplanChangeConfigDiscountIsNonNumeric() throws Exception {
        rowWithNonNumericData.dialplanChangeConfigDiscount();
    }

    @Test
    public void shouldReturnZeroWhenDialplanChangeConfigDiscountCellIsNull() throws Exception {
        assertThat(rowWithNullCells.dialplanChangeConfigDiscount(), is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnZeroWhenDialplanChangeConfigDiscountIsNull() throws Exception {
        assertThat(rowWithNullData.dialplanChangeConfigDiscount(), is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnZeroWhenDialplanChangeConfigDiscountCellIsBlank() throws Exception {
        assertThat(rowWithBlankData.dialplanChangeConfigDiscount(), is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnMmacConfigDiscount() throws Exception {
        assertThat(oneVoiceBcmOptionsRow.mmacConfigDiscount(), is(BigDecimal.valueOf(300)));
    }

    @Test(expected = InvalidExportDataException.class)
    public void shouldThrowExceptionWhenMmacConfigDiscountIsNonNumeric() throws Exception {
        rowWithNonNumericData.mmacConfigDiscount();
    }

    @Test
    public void shouldReturnZeroWhenMmacConfigDiscountCellIsNull() throws Exception {
        assertThat(rowWithNullCells.mmacConfigDiscount(), is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnZeroWhenMmacConfigDiscountIsNull() throws Exception {
        assertThat(rowWithNullData.mmacConfigDiscount(), is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnZeroWhenMmacConfigDiscountCellIsBlank() throws Exception {
        assertThat(rowWithBlankData.mmacConfigDiscount(), is(BigDecimal.ZERO));
    }

    @Test
    public void shouldReturnAmendmentCharge() throws Exception {
        assertThat(oneVoiceBcmOptionsRow.amendmentCharge(), is(Money.from("3.6")));
    }

    @Test(expected = InvalidExportDataException.class)
    public void shouldThrowExceptionWhenAmendmentChargeIsNonNumeric() throws Exception {
        rowWithNonNumericData.amendmentCharge();
    }

    @Test
    public void shouldThrowExceptionWhenAmendmentChargeCellIsNull() throws Exception {
        assertThat(rowWithNullCells.amendmentCharge(), is(Money.ZERO));
    }

    @Test
    public void shouldThrowExceptionWhenAmendmentChargeIsNull() throws Exception {
        assertThat(rowWithNullData.amendmentCharge(), is(Money.ZERO));
    }

    @Test
    public void shouldThrowExceptionWhenAmendmentChargeCellIsBlank() throws Exception {
        assertThat(rowWithBlankData.amendmentCharge(), is(Money.ZERO));
    }

    @Test
    public void shouldReturnCancellationCharge() throws Exception {
        assertThat(oneVoiceBcmOptionsRow.cancellationCharge(), is(Money.from("4.6")));
    }

    @Test(expected = InvalidExportDataException.class)
    public void shouldThrowExceptionWhenCancellationChargeIsNonNumeric() throws Exception {
        rowWithNonNumericData.cancellationCharge();
    }

    @Test
    public void shouldThrowExceptionWhenCancellationChargeCellIsNull() throws Exception {
        assertThat(rowWithNullCells.cancellationCharge(), is(Money.ZERO));
    }

    @Test
    public void shouldThrowExceptionWhenCancellationChargeIsNull() throws Exception {
        assertThat(rowWithNullData.cancellationCharge(), is(Money.ZERO));
    }

    @Test
    public void shouldThrowExceptionWhenCancellationChargeCellIsBlank() throws Exception {
        assertThat(rowWithBlankData.cancellationCharge(), is(Money.ZERO));
    }

    @Test
    public void shouldHaveAmendmentCharge() throws Exception {
        assertTrue(oneVoiceBcmOptionsRow.hasAmendmentCharge());
    }

    @Test
    public void shouldNotHaveAmendmentChargeWhenIsNonNumeric() throws Exception {
        assertFalse(rowWithNonNumericData.hasAmendmentCharge());
    }

    @Test
    public void shouldNotHaveAmendmentChargeWhenCellIsNull() throws Exception {
        assertTrue(rowWithNullCells.hasAmendmentCharge());
    }

    @Test
    public void shouldHaveAmendmentChargeWhenIsNull() throws Exception {
        assertTrue(rowWithNullData.hasAmendmentCharge());
    }

    @Test
    public void shouldNotHaveAmendmentChargeWhenCellIsBlank() throws Exception {
        assertTrue(rowWithBlankData.hasAmendmentCharge());
    }

    @Test
    public void shouldHaveCancellationCharge() throws Exception {
        assertTrue(oneVoiceBcmOptionsRow.hasCancellationCharge());
    }

    @Test
    public void shouldNotHaveCancellationChargeWhenIsNonNumeric() throws Exception {
        assertFalse(rowWithNonNumericData.hasCancellationCharge());
    }

    @Test
    public void shouldNotHaveCancellationChargeWhenCellIsNull() throws Exception {
        assertTrue(rowWithNullCells.hasCancellationCharge());
    }

    @Test
    public void shouldHaveCancellationChargeWhenIsNull() throws Exception {
        assertTrue(rowWithNullData.hasCancellationCharge());
    }

     @Test
    public void shouldNotHaveCancellationChargeWhenCellIsBlank() throws Exception {
        assertTrue(rowWithBlankData.hasCancellationCharge());
    }

}
