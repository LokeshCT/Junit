package com.bt.rsqe.inlife.web.export;

import com.bt.rsqe.monitoring.WebMetricsDTO;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.ExcelWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.hamcrest.core.Is;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static com.bt.rsqe.mis.client.fixtures.WebClientFixture.*;
import static com.bt.rsqe.mis.client.fixtures.WebMetricsFixture.*;
import static com.google.common.collect.Lists.*;
import static org.junit.Assert.assertThat;

public class WebMetricToExcelConverterTest {

      @Test
      public void shouldConvertRowsIntoExcelSheet() {

          Date date = new DateTime().toDate();
          List<WebMetricsDTO> webMetricsDTOs = newArrayList(
              aWebMetrics().withEin("1234").withCreatedDate(date).withNavigationName("Nav1").withNavigationType("PageLoad").withTimeTakenInMillis(1200).withUserType("Direct").withNumberOfSites(2).withWebClientFixture(aWebClient().withLocation("IND")).build(),
              aWebMetrics().withEin("2345").withCreatedDate(date).withNavigationName("Nav1").withNavigationType("PageLoad").withTimeTakenInMillis(1400).withUserType("Indirect").withNumberOfSites(3).withWebClientFixture(aWebClient().withLocation("UK")).build(),
              aWebMetrics().withEin("2345").withCreatedDate(date).withNavigationName("Nav2").withNavigationType("PageLoad").withTimeTakenInMillis(2400).withUserType("Direct").withNumberOfSites(2).withWebClientFixture(aWebClient().withLocation("UK")).build()
          );

          ExcelWorkbook excelWorkbook = new WebMetricToExcelConverter().convert("28-11-2014", "29-11-2014", webMetricsDTOs);

          assertThat(excelWorkbook.getName(), Is.is("WebMetrics_28-11-2014_to_29-11-2014.xlsx"));
          assertThat(excelWorkbook.getFile().getSheetName(0), Is.is("Web Metrics"));
          assertThat(excelWorkbook.getFile().getSheetAt(0).getLastRowNum(), Is.is(3));

          XSSFRow row = excelWorkbook.getFile().getSheetAt(0).getRow(1);
          assertThat(row.getCell(0).getNumericCellValue(), Is.is(1D));
          assertThat(row.getCell(1).getStringCellValue(), Is.is("Nav1"));
          assertThat(row.getCell(2).getStringCellValue(), Is.is("PageLoad"));
          assertThat(row.getCell(3).getNumericCellValue(), Is.is(1200D));
          assertThat(row.getCell(4).getStringCellValue(), Is.is("Direct"));
          assertThat(row.getCell(5).getNumericCellValue(), Is.is(2D));
          assertThat(row.getCell(6).getStringCellValue(), Is.is("IND"));
          assertThat(row.getCell(7).getStringCellValue(), Is.is("1234"));
      }

}
