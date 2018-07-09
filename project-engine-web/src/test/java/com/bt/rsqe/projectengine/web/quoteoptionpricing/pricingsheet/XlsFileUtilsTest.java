package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class XlsFileUtilsTest {

    private Workbook book;
    private InputStream inputStream;

    @Before
    public void setup() throws IOException {
        inputStream = this.getClass().getResourceAsStream("PricingSheetCellMergerInput.xls");
        book = new HSSFWorkbook(inputStream);
    }

    @Test
    public void shouldGetStringCellValue() {
        assertThat(XlsFileUtils.getStringCellValue(book.getSheet("Sheet1"), 1, 1), is("Site Level Charges Start Row"));
        assertThat(XlsFileUtils.getStringCellValue(book.getSheet("Sheet1"), 5, 5), is(""));
    }

    @Test
    public void shouldFindRowIndexForText() {
        assertThat(XlsFileUtils.findRowIndexForText(book.getSheet("Sheet1"), 1, "Site Level Charges Start Row"), is(1));
    }

    @After
    public void tearDown() throws IOException {
        inputStream.close();
    }
}
