package com.bt.rsqe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ReadExcel {
	Properties properties;
	Workbook wk;
	Sheet sh1;

	public ReadExcel() {
		properties = new Properties();

		try {
			InputStream in = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("Config.properties");

			properties.load(in);
			in.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public String getCellValue(String actionkeyword)
			throws BiffException, IOException {
		/*wk = Workbook
				.getWorkbook(new File(properties.getProperty("Test_Data")));*/

        wk = Workbook
                .getWorkbook(new File(
                        "C:\\Users\\606522807\\Desktop\\Hybrid\\TestData\\Test_Data.xls"));
		sh1 = wk.getSheet(2);
		int row_sheet1_count = sh1.getRows();
		int col_sheet1_count = sh1.getColumns();
		String inputData = "";
		 for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
		 for (int col_sheet = 0; col_sheet < col_sheet1_count; col_sheet++) {
		 if (sh1.getCell(col_sheet, row_sheet).getContents()
		 .equalsIgnoreCase(actionkeyword)) {
		inputData = (sh1.getCell(3, row_sheet).getContents());
		 } else {
		 System.out.println("No input data is found");
		 }
		 }
		 }

		return inputData;
	}

	public String getCellValue1(int row, String actionkeyword)
			throws BiffException, IOException {
		wk = Workbook.getWorkbook(new File(properties.getProperty("Test_Data")));
		sh1 = wk.getSheet(2);		
		String inputData = "";		
		inputData = (sh1.getCell(3, row).getContents());
		return inputData;
	}
	
	public String getCellValue2(int row, String actionkeyword)
	throws BiffException, IOException {
          wk = Workbook.getWorkbook(new File(properties.getProperty("Test_Data")));
          sh1 = wk.getSheet(3);		
          String inputData = "";		
          inputData = (sh1.getCell(3, row).getContents());
          return inputData;
}
	public String getCloudCellValue(int row, String actionkeyword)
	throws BiffException, IOException {
          wk = Workbook.getWorkbook(new File(properties.getProperty("Test_Data")));
          sh1 = wk.getSheet(4);		
          String inputData = "";		
          inputData = (sh1.getCell(3, row).getContents());
          return inputData;
}

	public String getTestCase(String actionkeyword) throws BiffException,
			IOException {
		wk = Workbook
				.getWorkbook(new File(properties.getProperty("Test_Data")));
		sh1 = wk.getSheet(2);
		int row_sheet1_count = sh1.getRows();
		int col_sheet1_count = sh1.getColumns();
		String inputData = "";
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			for (int col_sheet = 0; col_sheet < col_sheet1_count; col_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents()
						.equalsIgnoreCase(actionkeyword)) {
					inputData = (sh1.getCell(0, row_sheet).getContents());
				} else {
					// System.out.println("No input data is found");
				}
			}
		}

		return inputData;
	}
	
	
	

}
