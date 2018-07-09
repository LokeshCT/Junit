package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;


import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModel;
import com.bt.rsqe.util.excel.ExcelWithNoHeadersComparator;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class PricingSheetTest {

    PricingSheetDataModel pricingSheetDataModel;

    @Before
    public void before() throws Exception {
        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestData();
    }

    @Test
    @Ignore("This test needs to be fixed to not have a hard coded path")
    public void shouldGenerateDirectUserPricingSheet() throws IOException, InvalidFormatException {
        XLSTransformer transformer = new XLSTransformer();
        Workbook workbook = transformer.transformXLS(getClass().getClassLoader().getResource("PricingSheetDirect.xls").openStream(), pricingSheetDataModel.map());
        FileOutputStream out = new FileOutputStream("C:\\Apps\\PricingSheet\\expected-pricing-sheet.xls");
        workbook.write(out);

        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream("expected-pricing-sheet.xls")),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }
}
