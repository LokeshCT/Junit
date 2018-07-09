package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class PricingSheetCellMergerTest {
    private Workbook book;
    private InputStream inputStream;
    private PricingSheetCellMerger pricingSheetCellMerger;

    @Before
    public void setup() throws IOException {
        inputStream = getClass().getResourceAsStream("PricingSheetCellMergerInput.xls");
        book = new HSSFWorkbook(inputStream);

        pricingSheetCellMerger = new PricingSheetCellMerger() {
            @Override
            void mergeCells(Sheet sheet) {}

            @Override
            boolean canIMerge(Sheet sheet) {
                return true;
            }
        };
    }

    @Test
    public void shouldReturnTrueIfInputRegionCellsHaveSameValue() {
        Sheet sheet = book.getSheet("Sheet1");
        boolean validForMerge = pricingSheetCellMerger.isValidForMerge(sheet, 2, 4, 1);
        assertThat(validForMerge, is(true));
    }

    @Test
    public void shouldReturnFalseIfInputRegionCellsHaveDifferentValues() {
        Sheet sheet = book.getSheet("Sheet1");
        boolean validForMerge = pricingSheetCellMerger.isValidForMerge(sheet, 2, 5, 1);
        assertThat(validForMerge, is(false));
    }

    @Test
    public void shouldReturnRowsToMerge() {
        Sheet sheet = book.getSheet("Sheet1");
        List<FromTo> fromTos = pricingSheetCellMerger.rowsToMerge(sheet, 2, 8, 1);
        assertThat(fromTos, hasItem(new FromTo(2, 4)));
        assertThat(fromTos, hasItem(new FromTo(6, 8)));
        assertThat(fromTos.size(), is(2));
    }

    @After
    public void tearDown() throws IOException {
        inputStream.close();
    }
}
