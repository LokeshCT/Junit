package com.bt.rsqe.inlife.web.export;

import com.bt.rsqe.monitoring.WebMetricsDTO;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.ExcelWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.text.SimpleDateFormat;
import java.util.List;

public class WebMetricToExcelConverter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy");

    public ExcelWorkbook convert(String fromDate, String toDate, List<WebMetricsDTO> webMetricsDTOs) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Web Metrics");
        transformToRows(sheet, webMetricsDTOs);
        return new ExcelWorkbook(workbook, String.format("WebMetrics_%s_to_%s.xlsx", fromDate, toDate));
    }

    private void transformToRows(XSSFSheet sheet, List<WebMetricsDTO> webMetricsDTOs) {
        WebMetricsDTO dto;
        XSSFRow row;

        row = sheet.createRow(0);
        row.createCell(0).setCellValue("S.No.");
        row.createCell(1).setCellValue("Navigation Name");
        row.createCell(2).setCellValue("Navigation Type");
        row.createCell(3).setCellValue("Time taken (ms)");
        row.createCell(4).setCellValue("User Type");
        row.createCell(5).setCellValue("Number of Sites");
        row.createCell(6).setCellValue("Location");
        row.createCell(7).setCellValue("EIN");
        row.createCell(8).setCellValue("Date");

        for (int i = 0; i < webMetricsDTOs.size(); i++) {
            dto = webMetricsDTOs.get(i);
            row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(i+1);
            row.createCell(1).setCellValue(dto.getNavigationName());
            row.createCell(2).setCellValue(dto.getNavigationType());
            row.createCell(3).setCellValue(dto.getTimeTakenInMillis());
            row.createCell(4).setCellValue(dto.getUserType());
            row.createCell(5).setCellValue(dto.getNumberOfSites());
            row.createCell(6).setCellValue(dto.getClientDetail().getLocation());
            row.createCell(7).setCellValue(dto.getEin());
            row.createCell(8).setCellValue(DATE_FORMAT.format(dto.getCreatedDate()));
        }
    }
}
