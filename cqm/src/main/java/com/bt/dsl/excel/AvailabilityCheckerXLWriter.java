package com.bt.dsl.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 31/08/15
 * Time: 15:34
 * To change this template use File | Settings | File Templates.
 */
public class AvailabilityCheckerXLWriter {

    private String path = null;
    private String fileName = null;
    private OutputStream fileOutputStream = null;
    private XSSFWorkbook workbook = null;

    public AvailabilityCheckerXLWriter(String fileName, String path) throws Exception {
        this.fileName = fileName;
        this.path = path;
        this.fileOutputStream = new FileOutputStream(new File(path + "/" + fileName));
        this.workbook = new XSSFWorkbook();
    }


    public AvailabilityCheckerXLWriter(OutputStream fileOutputStream) {

        this.fileOutputStream = fileOutputStream;
        this.workbook = new XSSFWorkbook();

    }


    public void writeToNewExcel(List<SacXlRowDataModel> reportDtoList) throws Exception {

        XSSFSheet sheet = workbook.createSheet("Availability check output");
        sheet.setDefaultColumnWidth(22);

        try {

            if (reportDtoList == null && reportDtoList.size() == 0) {
                throw new Exception("data is empty.");
            }

            int applicabilityRowOffset = 5;  // from which row onwards need to print telephone no and applicability (YES/NO).

            createHeader(sheet, reportDtoList.get(0)); // populate static header

            for (int r = 0; r < reportDtoList.size(); r++) {

                SacXlRowDataModel rowData = reportDtoList.get(r);

                if (r == 0) {

                    populateXLRowWithData(sheet, applicabilityRowOffset, rowData, true); // populate dynamic Header and first row.

                } else {

                    populateXLRowWithData(sheet, applicabilityRowOffset, rowData, false); // populate only row.

                }

                applicabilityRowOffset++;// increasing offset for next row.
            }

            //sheet.createFreezePane(4,1);
            workbook.write(fileOutputStream);

        } catch (Exception e) {

            throw new Exception(e);

        } finally {

            fileOutputStream.close();
        }
    }

    private void createHeader(XSSFSheet sheet, SacXlRowDataModel reportDto) throws Exception {

        String countryHeader = "Country";
        String siteNameHeader = "Site Name";
        String phoneNoHeader = "Phone Number for eligibility check";
        String accessTechHeader = "Access Technology";
        String accessSpeedHeader = "Access Speed";
        String supplierHeader = "Supplier Name";
        String productHeaderIntName = "Supplier Product Name(Internal)";
        String productHeaderDispName = "Supplier Product Name";
        String noCupperPairHeader = "Number of Copper Pairs";

        XSSFRow row0 = sheet.createRow(0);
        XSSFRow row1 = sheet.createRow(1);
        XSSFRow row2 = sheet.createRow(2);
        XSSFRow row3 = sheet.createRow(3);
        XSSFRow row4 = sheet.createRow(4);
        XSSFRow row5 = sheet.createRow(5);

        mergeCell(sheet, 0, 5, 0, 0);
        mergeCell(sheet, 0, 5, 1, 1);
        mergeCell(sheet, 0, 5, 2, 2);


        setHeaderCell(row0, 0, countryHeader);
        setHeaderCell(row0, 1, siteNameHeader);
        setHeaderCell(row0, 2, phoneNoHeader);
        setHeaderCell(row0, 3, accessTechHeader);
        setHeaderCell(row1, 3, accessSpeedHeader);
        setHeaderCell(row2, 3, supplierHeader);
        setHeaderCell(row3, 3, noCupperPairHeader);
        setHeaderCell(row4, 3, productHeaderIntName);
        setHeaderCell(row5, 3, productHeaderDispName);


        SacXlAccessTechAndProductsDetailsMap sacAccTechDto = reportDto.getSacXlAccessAndProductsMapDto();

        int colOffset = 3;// Offset to display different type of  Access Technology.

        // display different type of access technology like DSL STANDARD. and merge cell.
        for (String accessEnum : sacAccTechDto.getAccessTechnologySequence()) {

            int size = sacAccTechDto.get(accessEnum).size();
            mergeCell(sheet, 0, 0, colOffset + 1, colOffset + size); // merging the cell.
            Cell cell = row0.createCell(colOffset + 1);
            setAccessTechnologyHeader(cell, accessEnum);
            colOffset = colOffset + size;

        }


    }


    private void populateXLRowWithData(XSSFSheet sheet, int rowOffset, SacXlRowDataModel reportDto, boolean shouldPopulateDynamicHeader) throws Exception {


        //First XSSFRow should be XSSFRow[5].
        XSSFRow row = getNewRow(rowOffset + 1, sheet);

        setValueAtNewCell(row, 0, reportDto.getCountry()); // country Name
        setValueAtNewCell(row, 1, reportDto.getSiteName());// Site Name
        setValueAtNewCell(row, 2, reportDto.getPhoneNo()); //Phone Number for eligibility check
        setValueAtNewCell(row, 3, "-");

        SacXlAccessTechAndProductsDetailsMap accTechDto = reportDto.getSacXlAccessAndProductsMapDto();

        int startColOffset = 3;
        for (String accTechEnum : accTechDto.getAccessTechnologySequence()) {

            List<SacXlSupplierProductDto> accTechDetailList = accTechDto.get(accTechEnum);

            for (int i = 0; i < accTechDetailList.size(); i++) {
                SacXlSupplierProductDto detailedDto = accTechDetailList.get(i);
                startColOffset = startColOffset + 1;
                if (shouldPopulateDynamicHeader == true) {
                    XSSFRow row1 = sheet.getRow(1);  //Access Speed Header. Example:512K/128K
                    XSSFRow row2 = sheet.getRow(2);  //Supplier Name  Header. Example:  SFR
                    XSSFRow row3 = sheet.getRow(3);  //Number of Copper Pairs Header. Example:1
                    XSSFRow row4 = sheet.getRow(4); //Supplier Product Name Header. Example: Surfer 512
                    XSSFRow row5 = sheet.getRow(5); //Supplier Product Display Name Header. Example: Surfer 512

                    setValueAtNewdataHeaderCell(row1, startColOffset, detailedDto.getAccessSpeed() +" "+detailedDto.getAccessSpeedUnit());
                    setValueAtNewdataHeaderCell(row2, startColOffset, detailedDto.getSupplierName());
                    setValueAtNewdataHeaderCell(row3, startColOffset, detailedDto.getNoOfCopperPairs());
                    setValueAtNewdataHeaderCell(row4, startColOffset, detailedDto.getSupplierProductName());
                    setValueAtNewdataHeaderCell(row5, startColOffset, detailedDto.getSupplierProductDispName());
                }
                setApplicabilityValueAtNewCell(row, startColOffset, detailedDto.getApplicability().toString());
            }
        }


    }

    private XSSFRow getNewRow(int rowNo, XSSFSheet sheet) {
        XSSFRow row = sheet.createRow(rowNo);
        return row;
    }


    private void setValueAtNewCell(XSSFRow row, int colNo, String value) {
        XLCellStyle style = new XLCellStyle();
        Cell cell = row.createCell(colNo);
        setCellFormat(cell, style);
        cell.setCellValue(value);

    }

    private void setHeaderCell(XSSFRow row, int colNo, String value) {
        XLCellStyle style = new XLCellStyle();
        Cell cell = row.createCell(colNo);
        style.setBgColor(new XSSFColor(new java.awt.Color(220, 230, 241)));
        style.setFontStyle(Font.BOLDWEIGHT_BOLD);
        setCellFormat(cell, style);
        cell.setCellValue(value);
    }


    private void setValueAtNewdataHeaderCell(XSSFRow row, int colNo, String value) {

        Cell cell = row.createCell(colNo);
        XLCellStyle style = new XLCellStyle();
        style.setBgColor(new XSSFColor(new java.awt.Color(149, 179, 215)));
        setCellFormat(cell, style);
        cell.setCellValue(value);

    }

    private void setAccessTechnologyHeader(Cell cell, String accessTechnology) {
        XLCellStyle style = new XLCellStyle();
        style.setBgColor(new XSSFColor(new java.awt.Color(149, 179, 215)));
        setCellFormat(cell, style);
        cell.setCellValue(accessTechnology);

    }

    private void setApplicabilityValueAtNewCell(XSSFRow row, int colNo, String value) {

        Cell cell = row.createCell(colNo);
        XLCellStyle style = new XLCellStyle();

        style.setBgColor(new XSSFColor(new java.awt.Color(191, 191, 191)));// For no response.
        if ("Yes".equalsIgnoreCase(value)) {
            style.setBgColor(new XSSFColor(new java.awt.Color(0, 255, 0)));
        }
        if ("No".equalsIgnoreCase(value)) {
            style.setBgColor(new XSSFColor(new java.awt.Color(255, 0, 0)));
        }

        setCellFormat(cell, style);
        cell.setCellValue(value);

    }


    private void mergeCell(XSSFSheet sheet, int startRow, int endRow, int startCol, int endCol) {

        CellRangeAddress cellRangeAddress = new CellRangeAddress(startRow, endRow, startCol, endCol);
        sheet.addMergedRegion(cellRangeAddress);
        RegionUtil.setBorderTop(BorderStyle.THIN.ordinal(), cellRangeAddress, sheet, workbook);
        RegionUtil.setBorderLeft(BorderStyle.THIN.ordinal(), cellRangeAddress, sheet, workbook);
        RegionUtil.setBorderRight(BorderStyle.THIN.ordinal(), cellRangeAddress, sheet, workbook);
        RegionUtil.setBorderBottom(BorderStyle.THIN.ordinal(), cellRangeAddress, sheet, workbook);

    }

    private void setCellFormat(Cell cell, XLCellStyle style) {


        XSSFCellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();

        font.setBoldweight(style.getFontStyle());
        cellStyle.setFont(font);

        cellStyle.setVerticalAlignment(style.getFontVerticalAlign());
        cellStyle.setAlignment(style.getFontHorizontalAlign());
        cellStyle.setFillForegroundColor(style.getBgColor());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        cellStyle.setWrapText(true);

        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);

        cell.setCellStyle(cellStyle);

    }

}
