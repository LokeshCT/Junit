package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.util.excel.ExcelWithNoHeadersComparator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.bt.rsqe.security.UserContextBuilder.anIndirectUserContext;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class DirectUserDetailedPricingSheetSiteLevelSectionCellMergerTest {

    private InputStream inputStream, inputStreamIndirect;
    private HSSFWorkbook book, indirectBook;
    private DirectUserDetailedPricingSheetSiteLevelSectionCellMerger siteLevelSectionCellMergerDirectUser;

    @Before
    public void setup() throws IOException {
        inputStream = getClass().getResourceAsStream("CA-direct-detailed sheet-site level charges-input.xls");
        inputStreamIndirect = getClass().getResourceAsStream("CA-indirect-detailed sheet-site level charges-input.xls");
        book = new HSSFWorkbook(inputStream);
        indirectBook = new HSSFWorkbook(inputStreamIndirect);
        siteLevelSectionCellMergerDirectUser = new DirectUserDetailedPricingSheetSiteLevelSectionCellMerger();
    }

    @Test
    public void shouldMergeCellsForSiteLevelSection() throws IOException {

        siteLevelSectionCellMergerDirectUser.mergeCells(book.getSheet("Sheet1"));
        List<String> errors = new ExcelWithNoHeadersComparator(book,
                                                               new HSSFWorkbook(this.getClass().getResourceAsStream("CA-direct-detailed sheet-site level charges-expected-output.xls")),
                                                               true)
            .compare();
        assertThat(errors.size(), is(0));

    }
    @Test
    public void shouldMergeCellsForSiteLevelSectionIndirect() throws IOException {
        withIndirectUser();
        siteLevelSectionCellMergerDirectUser.mergeCells(book.getSheet("Sheet1"));
        List<String> errors = new ExcelWithNoHeadersComparator(book,
                                                               new HSSFWorkbook(this.getClass().getResourceAsStream("CA-indirect-detailed sheet-site level charges-expected-output.xls")),
                                                               true)
            .compare();
        assertThat(errors.size(), is(0));

    }
    private void withIndirectUser() {
        UserContextManager.setCurrent(anIndirectUserContext().build());
    }
    @After
    public void tearDown() throws IOException {
        inputStream.close();
        inputStreamIndirect.close();
    }
}