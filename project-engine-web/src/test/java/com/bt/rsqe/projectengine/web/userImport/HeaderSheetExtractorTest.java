package com.bt.rsqe.projectengine.web.userImport;

import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.FileOutputStream;

public class HeaderSheetExtractorTest {

    @Test
    public void shouldConstructHeaderSheet() {
        HeaderDetailDTO headerDetailDTO = new HeaderDetailDTO();
        headerDetailDTO.setContractId("123");
        headerDetailDTO.setContractTerm("34");
        headerDetailDTO.setCurrency("GBP");
        headerDetailDTO.setCustomerName("Rajan");
        headerDetailDTO.setQuoteId("someQuote");
        XLSTransformer transformer = new XLSTransformer();
        transformer.setJexlInnerCollectionsAccess(true);

        try {
           /* XSSFWorkbook workbook = new XSSFWorkbook();
            HeaderSheetExtractor extractor = new HeaderSheetExtractor(workbook);
            extractor.setContractId("11556622");
            extractor.setContractTerm("12");
            extractor.currency = ;
            extractor.customerName = "British Telecom";
            extractor.quoteId = "someQuote";
            extractor.quoteName = "MyFirstQuote";
            extractor.quoteStatus = "In_Service";
            extractor.templateVersion = "A.51";
            String time = String.valueOf(System.currentTimeMillis());
            write(extractor.constructSheet(), "firstImport"+time+".xls");*/
        } catch (Exception e) {

        }
    }

    @SuppressWarnings("unused")
    private void write(Workbook workbook, String fileName) {
        FileOutputStream out;
        try {
            out = new FileOutputStream("C:\\Users\\607118528\\Suyambu\\" + fileName);
            workbook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
