package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import com.bt.rsqe.Percentage;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import static com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceChannelInformationRow.*;
import static org.apache.poi.ss.usermodel.Cell.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class OneVoiceChannelInformationRowTest {

    private OneVoiceChannelInformationRow oneVoiceNumericRow;
    private OneVoiceChannelInformationRow oneVoiceNonNumericRow;
    private OneVoiceChannelInformationRow oneVoiceBlankRow;
    private OneVoiceChannelInformationRow oneVoiceNullRow;
    private OneVoiceChannelInformationRow oneVoiceStringNullValueRow;

    @Before
    public void before() throws Exception {
        HSSFSheet sheet = new HSSFWorkbook().createSheet();

        HSSFRow numericRow = sheet.createRow(0);
        numericRow.createCell(DISCOUNT_CELL_INDEX_FOR_PTP_OR_RRP_CONFIG, CELL_TYPE_NUMERIC).setCellValue(0.12345678);
        numericRow.createCell(DISCOUNT_CELL_INDEX_FOR_PTP_OR_RRP_SUBSCRIPTION, CELL_TYPE_NUMERIC).setCellValue(0.22345678);

        this.oneVoiceNumericRow = new OneVoiceChannelInformationRow(numericRow);

        HSSFRow nonNumericRow = sheet.createRow(0);
        nonNumericRow.createCell(DISCOUNT_CELL_INDEX_FOR_PTP_OR_RRP_CONFIG, CELL_TYPE_STRING).setCellValue("string");
        nonNumericRow.createCell(DISCOUNT_CELL_INDEX_FOR_PTP_OR_RRP_SUBSCRIPTION, CELL_TYPE_STRING).setCellValue("string");

        this.oneVoiceNonNumericRow = new OneVoiceChannelInformationRow(nonNumericRow);

        HSSFRow blankRow = sheet.createRow(0);
        blankRow.createCell(DISCOUNT_CELL_INDEX_FOR_PTP_OR_RRP_CONFIG, CELL_TYPE_BLANK);
        blankRow.createCell(DISCOUNT_CELL_INDEX_FOR_PTP_OR_RRP_SUBSCRIPTION, CELL_TYPE_BLANK);

        this.oneVoiceBlankRow = new OneVoiceChannelInformationRow(blankRow);

        HSSFRow nullRow = sheet.createRow(0);
        this.oneVoiceNullRow = new OneVoiceChannelInformationRow(nullRow);

        HSSFRow stringWithNullDataRow = sheet.createRow(0);
        blankRow.createCell(DISCOUNT_CELL_INDEX_FOR_PTP_OR_RRP_CONFIG, CELL_TYPE_STRING);
        blankRow.createCell(DISCOUNT_CELL_INDEX_FOR_PTP_OR_RRP_SUBSCRIPTION, CELL_TYPE_STRING);

        this.oneVoiceStringNullValueRow = new OneVoiceChannelInformationRow(stringWithNullDataRow);
    }

    @Test
    public void shouldReturnNilIfPtpConfigDiscountNonNumericCell() throws Exception {
        assertThat(oneVoiceNonNumericRow.getPTPConfigDiscount(), is(Percentage.NIL));
    }

    @Test
    public void shouldReturnZeroIfPTPConfigDiscountIsNull() throws Exception {
        assertThat(oneVoiceNullRow.getPTPConfigDiscount(), is(Percentage.ZERO));
    }

    @Test
    public void shouldReturnZeroIfPTPConfigDiscountIsBlank() throws Exception {
        assertThat(oneVoiceBlankRow.getPTPConfigDiscount(), is(Percentage.ZERO));
    }

    @Test
    public void shouldReturnZeroIfPTPConfigDiscountIsStringButNullValue() throws Exception {
        assertThat(oneVoiceStringNullValueRow.getPTPConfigDiscount(), is(Percentage.ZERO));
    }

    @Test
    public void shouldGetConfigDiscount() throws Exception {
        Percentage roundedExpected = Percentage.from("12.34568");
        assertThat(oneVoiceNumericRow.getPTPConfigDiscount(), is(roundedExpected));
    }

    @Test
    public void shouldGetSubscriptionDiscount() throws Exception {
        Percentage roundedExpected = Percentage.from("22.34568");
        assertThat(oneVoiceNumericRow.getPTPSubscriptionDiscount(), is(roundedExpected));
    }

    @Test
    public void shouldReturnNilIfSubscriptionDiscountNonNumericCell() throws Exception {
        assertThat(oneVoiceNonNumericRow.getPTPSubscriptionDiscount(), is(Percentage.NIL));
    }

    @Test
    public void shouldReturnZeroIfSubscriptionDiscountIsNull() throws Exception {
        assertThat(oneVoiceNullRow.getPTPSubscriptionDiscount(), is(Percentage.ZERO));
    }

    @Test
    public void shouldReturnZeroIfSubscriptionDiscountIsBlank() throws Exception {
        assertThat(oneVoiceBlankRow.getPTPSubscriptionDiscount(), is(Percentage.ZERO));
    }

    @Test
    public void shouldReturnZeroIfSubscriptionDiscountIsStringButNullValue() throws Exception {
        assertThat(oneVoiceStringNullValueRow.getPTPSubscriptionDiscount(), is(Percentage.ZERO));
    }


}
