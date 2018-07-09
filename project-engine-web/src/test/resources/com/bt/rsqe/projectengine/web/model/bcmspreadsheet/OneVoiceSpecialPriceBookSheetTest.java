package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import com.bt.rsqe.domain.project.TerminationType;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import static com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceSpecialPriceBookRow.*;
import static org.apache.poi.ss.usermodel.Cell.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

public class OneVoiceSpecialPriceBookSheetTest {

    public static final String PRICEBOOK_NAME = "Pricebook";
    public static final String SOURCE_COUNTRY = "BRAZIL";
    public static final String TERMINATING_COUNTRY = "UNITED KINGDOM";
    private OneVoiceSpecialPriceBookSheet oneVoiceSpecialPriceBookSheet;
    private HSSFRow specialPriceBookRow;
    private HSSFSheet specialPriceBookSheet;

    @Before
    public void before() throws Exception {
        specialPriceBookSheet = new HSSFWorkbook().createSheet();

        specialPriceBookRow = specialPriceBookSheet.createRow(1);
        specialPriceBookRow.createCell(SPECIAL_PRICEBOOK_CELL_INDEX, CELL_TYPE_STRING).setCellValue(PRICEBOOK_NAME);
        specialPriceBookRow.createCell(ORIGINATING_COUNTRY_CELL_INDEX, CELL_TYPE_STRING).setCellValue(SOURCE_COUNTRY);
        specialPriceBookRow.createCell(TERMINATING_COUNTRY_CELL_INDEX, CELL_TYPE_STRING).setCellValue(TERMINATING_COUNTRY);
        specialPriceBookRow.createCell(TERMINATION_TYPE_CELL_INDEX, CELL_TYPE_STRING).setCellValue(TerminationType.OFF_NET.name());
    }

    @Test
    public void shouldGetDiscountGivenDiscountValue() throws Exception {
        specialPriceBookRow.createCell(DISCOUNT_CELL_INDEX, CELL_TYPE_NUMERIC).setCellValue(0.12345678);
        oneVoiceSpecialPriceBookSheet = new OneVoiceSpecialPriceBookSheet(specialPriceBookSheet);

        OneVoiceSpecialPriceBookRow specialPriceBook = oneVoiceSpecialPriceBookSheet.getSpecialPriceBookFor(PRICEBOOK_NAME, SOURCE_COUNTRY).get(0);
        String discount = specialPriceBook.getDiscount();
        assertThat(discount, is("0.12345678"));
    }

    @Test
    public void shouldGetZeroGivenDiscountIsEmpty() throws Exception {
        specialPriceBookRow.createCell(DISCOUNT_CELL_INDEX, CELL_TYPE_BLANK);
        oneVoiceSpecialPriceBookSheet = new OneVoiceSpecialPriceBookSheet(specialPriceBookSheet);

        OneVoiceSpecialPriceBookRow specialPriceBook = oneVoiceSpecialPriceBookSheet.getSpecialPriceBookFor(PRICEBOOK_NAME, SOURCE_COUNTRY).get(0);
        String discount = specialPriceBook.getDiscount();
        assertThat(discount, is("0"));
    }

    @Test
    public void shouldGetZeroGivenDiscountIsString() throws Exception {
        specialPriceBookRow.createCell(DISCOUNT_CELL_INDEX, CELL_TYPE_NUMERIC).setCellValue("abcde");
        oneVoiceSpecialPriceBookSheet = new OneVoiceSpecialPriceBookSheet(specialPriceBookSheet);

        OneVoiceSpecialPriceBookRow specialPriceBook = oneVoiceSpecialPriceBookSheet.getSpecialPriceBookFor(PRICEBOOK_NAME, SOURCE_COUNTRY).get(0);
        String discount = specialPriceBook.getDiscount();
        assertThat(discount, is("0"));
    }

    @Test
    public void shouldGetZeroGivenDiscountIsNull() throws Exception {
        oneVoiceSpecialPriceBookSheet = new OneVoiceSpecialPriceBookSheet(specialPriceBookSheet);

        OneVoiceSpecialPriceBookRow specialPriceBook = oneVoiceSpecialPriceBookSheet.getSpecialPriceBookFor(PRICEBOOK_NAME, SOURCE_COUNTRY).get(0);
        String discount = specialPriceBook.getDiscount();
        assertThat(discount, is("0"));
    }
}
