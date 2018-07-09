package com.bt.dsl.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 25/08/15
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
public class ExcelReader<T extends ExcelRow> {
    private String path = null;
    private String fileName = null;
    private InputStream fileInputStream = null;
    private XSSFWorkbook workbook = null;


    private ExcelReader(InputStream fileInputStream) {
        this.fileInputStream = fileInputStream;
    }


    private ExcelReader(String fileName, String path) throws Exception {
        this.fileName = fileName;
        this.path = path;
        this.fileInputStream = new FileInputStream(new File(path + "/" + fileName));
    }


    public static ExcelReader getInstance(String fileName, String path) throws Exception {
        try {
            return new ExcelReader(fileName, path);
        } catch (Exception e) {
            throw new Exception("Please check the file name and path" + e);
        }
    }


    public static ExcelReader getInstance(InputStream fileInputStream) {
        return new ExcelReader(fileInputStream);
    }


    public List<T> getAllRows(Class clazz) throws Exception {

        List<T> excelRowList = new ArrayList<T>();
        if (fileInputStream == null) {
            throw new Exception("FileName or path or FileInputStream is null.");
        }

        List<KeyValueMap> list = ((T) clazz.newInstance()).getFields(); // retrive all the Field name.

        try {

            workbook = new XSSFWorkbook(this.fileInputStream);
            XSSFSheet sheet = workbook.getSheetAt(1);
            Iterator<Row> rowIterator = sheet.iterator();


            while (rowIterator.hasNext()) {

                Row row = rowIterator.next();
                T rowObj = (T) clazz.newInstance();

                for (KeyValueMap keyValueMap : list) {

                    String value = getCellValue(row.getCell(keyValueMap.getColumnIndex()));
                    setValueToObject(rowObj, keyValueMap.getMethodName(), value);

                }

                if (!rowObj.isRowEmpty()) {
                    excelRowList.add(rowObj);
                }
                System.out.println("");
            }

        } catch (Exception e) {

            throw e;

        }

        return excelRowList;
    }


    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                return new BigDecimal(cell.getNumericCellValue()).toPlainString();
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_BLANK:
                return "";
            case Cell.CELL_TYPE_BOOLEAN:
                return "" + cell.getBooleanCellValue();
        }
        return new String();
    }


    public void setValueToObject(T o, String methodNmae, String value) throws Exception {

        Method method = o.getClass().getMethod(methodNmae, String.class);
        method.invoke(o, value);

    }

}
