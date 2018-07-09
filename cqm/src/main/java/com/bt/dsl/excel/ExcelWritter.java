package com.bt.dsl.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 27/08/15
 * Time: 14:44
 * To change this template use File | Settings | File Templates.
 */
public class ExcelWritter<T extends ExcelRow> {
    private String path = null;
    private String fileName = null;
    private OutputStream fileOutputStream = null;
    private XSSFWorkbook workbook = null;

    private ExcelWritter(String fileName, String path) throws Exception {
        this.fileName = fileName;
        this.path = path;
        this.fileOutputStream = new FileOutputStream(new File(path + "/" + fileName));
        this.workbook = new XSSFWorkbook();
    }

    public ExcelWritter(OutputStream fileOutputStream) {
        this.fileOutputStream = fileOutputStream;
        this.workbook = new XSSFWorkbook();
    }

    public void writeToNewExcel(List<T> rowList) throws Exception {

        if (rowList == null || rowList.size() == 0) {
            throw new Exception("List is empty or null");
        }
        Class clazz = rowList.get(0).getClass();
        XSSFSheet sheet = workbook.createSheet();
        writeToExcel(rowList, clazz, sheet);
    }

    public void writeToExistingExcel(List<T> rowList, Class clazz) throws Exception {
        XSSFSheet sheet = workbook.getSheetAt(1);
        writeToExcel(rowList, clazz, sheet);
    }

    private void writeToExcel(List<T> rowList, Class clazz, XSSFSheet sheet) throws Exception {

        if (fileOutputStream == null) {
            throw new Exception("FileName or path or FileOutputStream is null.");
        }
        int rowNumbe = 0;
        List<KeyValueMap> list = ((T) clazz.newInstance()).getFieldsWitGetMethod(); // retrive all the Field name.


        for (T rowData : rowList) {
            Row row = sheet.createRow(rowNumbe++);

            for (KeyValueMap keyValueMap : list) {
                Cell cell = row.createCell(keyValueMap.getColumnIndex());
                String cellValue = getValueFrmObject(rowData, keyValueMap.getMethodName());
                XLCellStyle xlCellStyle = rowData.getCellFormat(keyValueMap.getColumnIndex(), cellValue);
                if (xlCellStyle != null) {
                    CellStyle style = workbook.createCellStyle();
                    Font font = workbook.createFont();
                    font.setColor(xlCellStyle.getFontColor());
                    font.setBoldweight(xlCellStyle.getFontStyle());
                    style.setFont(font);
                    cell.setCellStyle(style);
                }
                cell.setCellValue(cellValue);
            }
        }

        this.workbook.write(fileOutputStream);
    }

    public String getValueFrmObject(T o, String methodNmae) throws Exception {
        String value = null;
        Method method = o.getClass().getMethod(methodNmae);
        value = (String) method.invoke(o);
        return value;

    }

    //Test the code

    /*
    public static void main(String[] args) throws Exception {
        List<UploadFailureRow> failureRows = new ArrayList<UploadFailureRow>();
        UploadFailureRow objHeader = new UploadFailureRow();
        objHeader.setSrNo("Sr No.");
        objHeader.setSiteName("Site Name (Optional)");
        objHeader.setTelephoneNo("Telephone Number (Mandatory)");
        objHeader.setCountry("Country");
        objHeader.setValidationStatus("Validation  Result");
        objHeader.setFailureReason("Reason for Failure");

        UploadFailureRow row1 = new UploadFailureRow();
        row1.setSrNo("1");
        row1.setSiteName("Site 1");
        row1.setTelephoneNo("1111111111");
        row1.setCountry("India");
        row1.setValidationStatus("Failed");
        row1.setFailureReason("Phone no not correct");

        UploadFailureRow row2 = new UploadFailureRow();
        row2.setSrNo("2");
        row2.setSiteName("Site 2");
        row2.setTelephoneNo("9742570015");
        row2.setCountry("India");
        row2.setValidationStatus("Sucess");

        UploadFailureRow row3 = new UploadFailureRow();
        row3.setSrNo("3");
        row3.setSiteName("Site 3");
        row3.setTelephoneNo("1111111111");
        row3.setCountry("India");
        row3.setValidationStatus("Failed");
        row3.setFailureReason("Phone no not correct");

        UploadFailureRow row4 = new UploadFailureRow();
        row4.setSrNo("4");
        row4.setSiteName("Site 4");
        row4.setTelephoneNo("9742570015");
        row4.setCountry("India");
        row4.setValidationStatus("Sucess");

        // row1.setFailureReason("Phone no not correct");
        failureRows.add(objHeader);
        failureRows.add(row1);
        failureRows.add(row2);
        failureRows.add(row3);
        failureRows.add(row4);

        FileOutputStream fos = new FileOutputStream("C:\\Goutam\\story\\CQM\\GSCE-162988\\Out\\Failure_rusult_3.xlsx");
        ExcelWritter<UploadFailureRow> writter = new ExcelWritter<UploadFailureRow>(fos);
        writter.writeToNewExcel(failureRows);

    }
    */


}
