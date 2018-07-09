package com.bt.testng;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.bt.rsqe.AddProduct;
import com.bt.rsqe.Bulkconfiguration;
import com.bt.rsqe.Common;
import com.bt.rsqe.QuoteDetail;
import com.bt.rsqe.QuoteOption;
import com.bt.rsqe.RSQEWait;
import com.bt.rsqe.ReadExcel;


public class CATestSuite {
		Workbook wk;
		WritableWorkbook workbookCopy;
		Sheet sh1;
		WritableSheet ws;
		
		Properties properties;	
		ReadExcel readExcel;
		QuoteOption quoteOption;
		QuoteDetail quoteDetail;
		AddProduct addprod;
		Bulkconfiguration bulkconfig;
		RSQEWait rsqewait;
		
		

		int row_sheet1_count;
		int col_sheet1_count;
		int col_sheet = 1;
		String res = "";
		private static WebDriver driver;
		public Common common;
		
		
		public CATestSuite(WebDriver driver){
			CATestSuite.driver = driver;
			
		}

		public CATestSuite() throws BiffException, IOException {
			common = new Common();
			properties = new Properties();
			quoteOption = new QuoteOption(); 
			quoteDetail = new QuoteDetail();
			addprod = new AddProduct();
			readExcel = new ReadExcel();
			bulkconfig = new Bulkconfiguration();
			readExcel = new ReadExcel();
			rsqewait = new RSQEWait();
		
			try {
				InputStream in = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("Config.properties");

				properties.load(in);
				in.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		@BeforeMethod
		public void getData() throws BiffException, IOException {
			/*wk = Workbook
					.getWorkbook(new File(properties.getProperty("C:\\Users\\606522807\\Desktop\\Hybrid\\TestData\\Test_Data.xls")));*/

	        wk = Workbook.getWorkbook(new File(properties.getProperty("Test_Data")));

	        sh1 = wk.getSheet(3);
			row_sheet1_count = sh1.getRows();
			col_sheet1_count = sh1.getColumns();
		}
		
		@Test
		public void launchURL() throws BiffException, IOException, WriteException {
			driver = Common.getDriver();
			System.out.println(row_sheet1_count);
			for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(1, row_sheet).getContents().equals("Launch RSQE")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String url = readExcel.getCellValue2(row_sheet,"Launch RSQE");
						System.out.println(url);
						common.LoadURL(url);
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}

			}
		}

		@Test
		public void createQuoteOption() throws BiffException, IOException,
				InterruptedException, WriteException {
			for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(1, row_sheet).getContents()
						.equals("Create Quote Option")) {
					if ((sh1.getCell(2, row_sheet).getContents()
							.equalsIgnoreCase("Yes"))) {
						String createQuote = readExcel.getCellValue2(row_sheet,
								"Create Quote Option");
						String str1 = createQuote.split(",")[0].trim();
						String str2 = createQuote.split(",")[1].trim();
						String ContractTerm = str1.split(":")[1].trim();
						String Currency = str2.split(":")[1].trim();
						Thread.sleep(5000);
						quoteOption.createQuote(ContractTerm, Currency);
						System.out.println(createQuote);
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}
		}

		@Test
		public void selectQuoteOption() throws BiffException, IOException,
				InterruptedException {
			for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents()
						.equals("quoteDetails")) {
					if ((sh1.getCell(2, row_sheet).getContents()
							.equalsIgnoreCase("Yes"))) {
						String quoteName = readExcel.getCellValue2(row_sheet,
								"quoteDetails");
						System.out.println("inside journey" + quoteName);
						quoteOption.getQuoteName(quoteName);
						quoteDetail.selectAddProduct();
						
					} else {
						// break mainloop;
						throw new SkipException("Skipping this testcase");
					}
				}
			}
		}

		@Test
		public void addProduct() throws BiffException, IOException,
				InterruptedException, AWTException {
			for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents()
						.equals("addProduct")) {
					if ((sh1.getCell(2, row_sheet).getContents()
							.equalsIgnoreCase("Yes"))) {
						String migrateProd = readExcel.getCellValue2(row_sheet,
								"addProduct");
						String str3 = migrateProd.split(",")[0].trim();
						String str4 = migrateProd.split(",")[1].trim();
						String str5 = migrateProd.split(",")[2].trim();
						String str6 = migrateProd.split(",")[3].trim();
						String productFamily = str3.split(":")[1].trim();
						String productVariant = str4.split(":")[1].trim();
						String productOffering = str5.split(":")[1].trim();
						String county = str6.split(":")[1].trim();
						addprod.createAddProduct(productFamily, productVariant,productOffering,county);
						System.out.println("the Product values are "+ productFamily + "--->" + productVariant + "--->"+ productOffering + "--->" + county);
						} else {
							throw new SkipException("Skipping this testcase");
					}
				}
			}
		}
		
		@Test
		public void selectSiteID() throws BiffException, IOException,
				InterruptedException {
			for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("selectingSiteID")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String sName = readExcel.getCellValue2(row_sheet,"selectingSiteID");
						String siteName = sName.split(":")[1].trim();
						rsqewait.waitForSiteTable(siteName);
						addprod.selectSiteID(siteName);
						
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}
		}
		
		
		@Test
		public void addCentralServices() throws BiffException, IOException,
				InterruptedException {
			for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("CentralServices")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String serviceProduct = readExcel.getCellValue2(row_sheet,"CentralServices");
						String str7 = serviceProduct.split(",")[0].trim();
						String str8 = serviceProduct.split(",")[1].trim();
						String str9 = serviceProduct.split(",")[2].trim();
						String str10 = serviceProduct.split(",")[3].trim();
						String productFamily1 = str7.split(":")[1].trim();
						String productVariant1 = str8.split(":")[1].trim();
						String productOffering1 = str9.split(":")[1].trim();
						String counterValue = str10.split(":")[1].trim();
						addprod.selectCentralServices(productFamily1,productVariant1, productOffering1,counterValue);
						System.out.println("the Product values are "+ productFamily1 + "--->" + productVariant1+ "--->" + productOffering1);
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}
		}
		
		@Test
		public void addLicencePack() throws BiffException, IOException,
				InterruptedException {
			for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("LicencePack")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String serviceProduct = readExcel.getCellValue2(row_sheet,"LicencePack");
						String productOffering1 = serviceProduct.split(":")[1].trim();						
						addprod.selectLicencePack(productOffering1);
						System.out.println("the Product values are "+ productOffering1);
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}
		}
       
		@Test 
		void selectConfigProduct() throws BiffException, IOException, InterruptedException{
			
			for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Configure Product")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {		
						String serviceProduct = readExcel.getCellValue2(row_sheet,"Configure Product");
						String counterValue = serviceProduct.split(":")[1].trim();
						addprod.clickConfigProduct(counterValue);
						Thread.sleep(5000);
						Thread.sleep(5000);
						Thread.sleep(5000);
					}
					else{
						throw new SkipException("Skipping this testcase");
					}
				}
			}
		}
	
		@Test
		void selectConnectAcceServ() throws BiffException, IOException, InterruptedException{
			System.out.println("Connect Acceleration Service");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Connect Acceleration Service")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String connAccSer = readExcel.getCellValue2(row_sheet,"Connect Acceleration Service");	
						String str1 = connAccSer.split(",")[0].trim();
						String str2 = connAccSer.split(",")[1].trim();
						String Node = str1.split(":")[1].trim();
						String fieldvalue = str2.split(":")[1].trim();
						System.out.println("The value--->" + connAccSer);
						Thread.sleep(25000);	
						Thread.sleep(25000);
						Thread.sleep(25000);
						Thread.sleep(25000);
						Thread.sleep(25000);
						//bulkconfig.accordian(Node);
						rsqewait.waitToLoad1(fieldvalue);
						//Thread.sleep(5000);
						break outerloop;
					} else {
						//boolean a = false;
						//Assert.assertEquals(true, a);
						//break;
						throw new SkipException("Skipping this testcase");
					}
				}
			}			
		}
		
		@Test(dependsOnMethods = { "selectConnectAcceServ" })
		void selectCentralMang() throws BiffException, IOException, InterruptedException{
			System.out.println("Central Management Console Virtual Edition");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("CentralManagement")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCellValue2(row_sheet,"CentralManagement");								
						String str7  = centManag.split(",")[0].trim();
						String str8  = centManag.split(",")[1].trim();
						String str9  = centManag.split(",")[2].trim();
						String str10 = centManag.split(",")[3].trim();
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();	
						String fieldvalue = str10.split(":")[1].trim();	
						System.out.println("The value--->" + Node);
						//Thread.sleep(5000);
						//Thread.sleep(5000);
					//	Thread.sleep(5000);
					    Thread.sleep(5000);					
						bulkconfig.accordian(Node);
						rsqewait.waitToLoad1(fieldvalue);
						common.error1();
						bulkconfig.selectConfigure(product);
						Thread.sleep(10000);
						bulkconfig.selectPopup(candidate);
						//Thread.sleep(20000);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						//boolean a = false;
						//Assert.assertEquals(true, a);
						//break;
						throw new SkipException("Skipping this testcase");
					}
				}
			}
			
		}
		
		@Test (dependsOnMethods = { "selectCentralMang" })
		void selectElementMang() throws BiffException, IOException, InterruptedException{
			System.out.println("Central Management Console Virtual Edition");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("ElementManager")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCellValue2(row_sheet,"ElementManager");								
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();
						String str10 = centManag.split(",")[3].trim();
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();		
						String fieldvalue = str10.split(":")[1].trim();	
						System.out.println("The value--->" + Node);
						Thread.sleep(5000);
					//	Thread.sleep(5000);
					//	Thread.sleep(5000);
					//	Thread.sleep(5000);
						bulkconfig.accordian(Node);
						rsqewait.waitToLoad1(fieldvalue);
						common.error1();
						bulkconfig.selectConfigure(product);
						Thread.sleep(20000);
						bulkconfig.selectPopup2(candidate);					
						Thread.sleep(5000);
						Thread.sleep(5000);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						//boolean a = false;
						//Assert.assertEquals(true, a);
						//break;
						throw new SkipException("Skipping this testcase");
					}
				}
			}
			
		}
		
		@Test (dependsOnMethods = { "selectElementMang" })
		void selectCentralMangLicencePack() throws BiffException, IOException, InterruptedException{
			System.out.println("CentralManagementLicencePack");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("CentralManagementLicencePack")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centralLicence = readExcel.getCellValue2(row_sheet,"CentralManagementLicencePack");											
						String str7 = centralLicence.split(",")[0].trim();
						String str8 = centralLicence.split(",")[1].trim();
						String Node = str7.split(":")[1].trim();		
						String fieldvalue = str8.split(":")[1].trim();	
						System.out.println("The value--->" + Node);
						Thread.sleep(20000);
						//bulkconfig.accordian(Node);
						bulkconfig.selectSteelHeadMangLicPack();
						Thread.sleep(20000);
						//rsqewait.waitToLoad1(fieldvalue);						
						break outerloop;
					} else {
						//boolean a = false;
						//Assert.assertEquals(true, a);
						//break;
						throw new SkipException("Skipping this testcase");
					}
				}
			}			
		}
		
		@Test (dependsOnMethods = { "selectCentralMangLicencePack" })
		void selectCentralMangBaseConfig() throws BiffException, IOException, InterruptedException{
			System.out.println("CentralManagementBaseConfig");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("CentralManagementBaseConfig")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String baseconfig = readExcel.getCellValue2(row_sheet,"CentralManagementBaseConfig");																
						String str7 = baseconfig.split(",")[0].trim();
						String str8 = baseconfig.split(",")[1].trim();
						String str9 = baseconfig.split(",")[2].trim();
						String str10 =baseconfig.split(",")[3].trim();
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();	
						String fieldvalue  = str10.split(":")[1].trim();
						System.out.println("The value--->" + Node);						
						Thread.sleep(5000);				
						//bulkconfig.accordian(Node);
						bulkconfig.selectLicBaseconfig();
						rsqewait.waitToLoad1(fieldvalue);
						//driver.findElement(By.xpath("//div[@id='configurator-container']/div[5]/div[3]/div[1]/div[4]/table/tbody/tr[1]/td[7]/div/div[2]")).click();
						bulkconfig.selectInput(product, candidate);	
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						//boolean a = false;
						//Assert.assertEquals(true, a);
						//break;
						throw new SkipException("Skipping this testcase");
					}
				}
			}			
		}
		
		@Test (dependsOnMethods = { "selectCentralMangBaseConfig" })
		void selectLicenceConsumer() throws BiffException, IOException, InterruptedException{		
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("CentralManagementLicenceConsumer")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String licenceCons = readExcel.getCellValue2(row_sheet,"CentralManagementLicenceConsumer");								
						String str7 = licenceCons.split(",")[0].trim();
						String str8 = licenceCons.split(",")[1].trim();
						String str9 = licenceCons.split(",")[2].trim();
						String str10 = licenceCons.split(",")[3].trim();
						
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();	
						String fieldvalue  = str10.split(":")[1].trim();
						
						System.out.println("The value--->" + Node);
						Thread.sleep(10000);
						Thread.sleep(5000);
						Thread.sleep(5000);
						Thread.sleep(5000);
						//bulkconfig.accordian(Node);
						bulkconfig.selectLiConsumer();
						//common.error1();
						Thread.sleep(10000);
						rsqewait.waitToLoad1(fieldvalue);
						bulkconfig.selectConfigure(product);
						Thread.sleep(20000);
						bulkconfig.selectPopup2(candidate);
						Thread.sleep(10000);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						//boolean a = false;
						//Assert.assertEquals(true, a);
						//break;
						throw new SkipException("Skipping this testcase");
					}
				}
			}
			
		}
		
		@Test (dependsOnMethods = { "selectLicenceConsumer" })
		void selectConnectAccelerationSite() throws BiffException, IOException, InterruptedException{
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Connect Acceleration Site ")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String str1 = readExcel.getCellValue2(row_sheet,"Connect Acceleration Site ");	
						String str7 = str1.split(",")[0].trim();
						String str8 = str1.split(",")[1].trim();
						
						String Node = str7.split(":")[1].trim();
						String fieldvalue = str8.split(":")[1].trim();
						System.out.println("The value--->" + Node);
						Thread.sleep(20000);						
						bulkconfig.accordian(Node);	
						//rsqewait.waitToLoad1(fieldvalue);						
						common.error1();
						break outerloop;
					} else {
						//boolean a = false;
						//Assert.assertEquals(true, a);
						//break;
						throw new SkipException("Skipping this testcase");
					}
				}
			}			
		}
		
		
		@Test (dependsOnMethods = { "selectConnectAccelerationSite" })
		void selectTopology() throws BiffException, IOException, InterruptedException, AWTException{		
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Topology")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String str = readExcel.getCellValue2(row_sheet,"Topology");								
						String str7 = str.split(",")[0].trim();
						String str8 = str.split(",")[1].trim();
						String str9 = str.split(",")[2].trim();
						String str10 = str.split(",")[3].trim();
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();	
						String fieldvalue  = str10.split(":")[1].trim();	
						System.out.println("The value--->" + Node);						
						Thread.sleep(5000);					
						bulkconfig.accordian(Node);
						rsqewait.waitToLoad1(fieldvalue);
						bulkconfig.selectDropDown(product, candidate);
						bulkconfig.selectCheckBox();
						common.error(driver);
						break outerloop;
					} else {
						//boolean a = false;
						//Assert.assertEquals(true, a);
						//break;
						throw new SkipException("Skipping this testcase");
					}
				}
			}
			
		}
		
		@Test (dependsOnMethods = { "selectTopology" })
		void selectBaseConfig() throws BiffException, IOException, InterruptedException, AWTException{		
			selectConnectAccelerationSite();
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Base Configuration")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String str = readExcel.getCellValue2(row_sheet,"Base Configuration");								
						String str7 = str.split(",")[0].trim();
						String str8 = str.split(",")[1].trim();
						String str9 = str.split(",")[2].trim();
						String str10 = str.split(",")[3].trim();
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();		
						String fieldvalue  = str10.split(":")[1].trim();		
						System.out.println("The value--->" + Node);
						Thread.sleep(40000);							
						bulkconfig.accordian(Node);
						rsqewait.waitToLoadCO(fieldvalue);
						bulkconfig.selectDropDown1(product, candidate);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						//boolean a = false;
						//Assert.assertEquals(true, a);
						//break;
						throw new SkipException("Skipping this testcase");
					}
				}
			}
			
		}
		
		@Test (dependsOnMethods = { "selectBaseConfig" })
		void selectWanConnection() throws BiffException, IOException, InterruptedException, AWTException{		
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("WAN Connection")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String str = readExcel.getCellValue2(row_sheet,"WAN Connection");								
						String str7 = str.split(",")[0].trim();
						String str8 = str.split(",")[1].trim();
						String str9 = str.split(",")[2].trim();
						String str10 = str.split(",")[3].trim();
						
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();	
						String fieldvalue  = str10.split(":")[1].trim();	
						
						System.out.println("The value--->" + Node);
						Thread.sleep(5000);					
						bulkconfig.accordian(Node);
						rsqewait.waitToLoadCO(fieldvalue);
						
						common.error1();
						//bulkconfig.selectConfigure(product);
						//bulkconfig.selectPopup3(candidate);
						Thread.sleep(10000);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						//boolean a = false;
						//Assert.assertEquals(true, a);
						//break;
						throw new SkipException("Skipping this testcase");
					}
				}
			}
			
		}
		
		@Test (dependsOnMethods = { "selectWanConnection" })
		void selectSteelhead() throws BiffException, IOException, InterruptedException, AWTException{					
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Steelhead")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String str = readExcel.getCellValue2(row_sheet,"Steelhead");								
						String str7 = str.split(",")[0].trim();
						String str8 = str.split(",")[1].trim();
						String str9 = str.split(",")[2].trim();
						String str10 = str.split(",")[3].trim();
						
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();	
						String fieldvalue  = str10.split(":")[1].trim();	
						
						System.out.println("The value--->" + Node);
						Thread.sleep(5000);					
						bulkconfig.accordian(Node);
						rsqewait.waitToLoadCO(fieldvalue);
						
						bulkconfig.selectConfigure(product);
						Thread.sleep(10000);						
						bulkconfig.selectPopup(candidate);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						//boolean a = false;
						//Assert.assertEquals(true, a);
						//break;
						throw new SkipException("Skipping this testcase");
					}
				}
			}
			
		}
		
		@Test (dependsOnMethods = { "selectSteelhead" })
		void selectSteelheadBaseConfig() throws BiffException, IOException, InterruptedException, AWTException{					
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("SteelheadBaseConfiguration")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String str = readExcel.getCellValue2(row_sheet,"SteelheadBaseConfiguration");								
						String str7 = str.split(",")[0].trim();
						String str8 = str.split(",")[1].trim();
						String str9 = str.split(",")[2].trim();
						String str10 = str.split(",")[3].trim();
						
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();	
						String fieldvalue  = str10.split(":")[1].trim();
						
						System.out.println("The value--->" + Node);
						Thread.sleep(10000);						
						//bulkconfig.accordian1(Node);
						bulkconfig.IPEAccordian(Node);
						Thread.sleep(15000);
						//rsqewait.waitToLoadCO(fieldvalue);		
						
						bulkconfig.selectDropDown1(product, candidate);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						//boolean a = false;
						//Assert.assertEquals(true, a);
						//break;
						throw new SkipException("Skipping this testcase");
					}
				}
			}
			
		}
		
		
		
		
		@Test (dependsOnMethods = { "selectSteelheadBaseConfig" })
		void selectCPESuppMain() throws BiffException, IOException, InterruptedException, AWTException{					
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("CPE Supplier Maintenance")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String str = readExcel.getCellValue2(row_sheet,"CPE Supplier Maintenance");								
						String str7 = str.split(",")[0].trim();
						String str8 = str.split(",")[1].trim();
						String str9 = str.split(",")[2].trim();
						String str10 = str.split(",")[3].trim();
						
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();
						String fieldvalue  = str10.split(":")[1].trim();
						
						System.out.println("The value--->" + Node);
						Thread.sleep(40000);						
						bulkconfig.accordian1(Node);
						Thread.sleep(5000);Thread.sleep(5000);
						rsqewait.waitToLoadCO(fieldvalue);
						bulkconfig.selectConfigure(product);
						Thread.sleep(15000);
						//bulkconfig.selectServiceCentralDetail();
						Thread.sleep(20000);
						bulkconfig.selectPopup(candidate);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						//boolean a = false;
						//Assert.assertEquals(true, a);
						//break;
						throw new SkipException("Skipping this testcase");
					}
				}
			}
			
		}
		
		
		@Test (dependsOnMethods = { "selectCPESuppMain" })
		public void selectOffnetAccess() throws InterruptedException,BiffException, IOException {
			selectConnectAccelerationSite();
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Offnet Access")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String str = readExcel.getCellValue2(row_sheet,"Offnet Access");								
						String str7 = str.split(",")[0].trim();
						String str8 = str.split(",")[1].trim();
						String str9 = str.split(",")[2].trim();
						String str10 = str.split(",")[3].trim();
						String str11 = str.split(",")[4].trim();
						
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();
						String product1 = str10.split(":")[1].trim();
						String candidate1  = str11.split(":")[1].trim();	
						System.out.println("The value--->" + Node);
						Thread.sleep(5000);
						Thread.sleep(5000);
						Thread.sleep(5000);
						//Thread.sleep(5000);
						bulkconfig.accordian(Node);
						Thread.sleep(5000);
						Thread.sleep(5000);
						bulkconfig.selectInput(product, candidate);
						bulkconfig.selectDropDown1(product1, candidate1);
						bulkconfig.selectCheckBox();
						quoteDetail.selectQuoteDetail();
						Thread.sleep(20000);
						break outerloop;
					} else {
						//boolean a = false;
						//Assert.assertEquals(true, a);
						//break;
						throw new SkipException("Skipping this testcase");
					}
				}
			}
		
		
		}
		
		@Test (dependsOnMethods = { "selectOffnetAccess" })
	//	@Test 
		public void selectPrice() throws InterruptedException, IOException{
			//quoteDetail.selectQuoteDetail();		
			quoteDetail.selectlineItems();
			quoteDetail.selectCalcPrice();
			//quoteDetail.refresh();
			//quoteDetail.selectlineItems();
			//quoteDetail.selectCalcPrice();
			
			
		}
		
		
		@Test (dependsOnMethods = { "selectPrice" })
		public void createOffer() throws BiffException, IOException, InterruptedException{
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Offer")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String str = readExcel.getCellValue2(row_sheet,"Offer");								
						String str7 = str.split(",")[0].trim();
						String str8 = str.split(",")[1].trim();
						String offerName = str7.split(":")[1].trim();
						String OrderRef = str8.split(":")[1].trim();
						quoteDetail.selectCreateOffer(offerName, OrderRef);							
						}
					else{
						throw new SkipException("Skipping this testcase");
					}
					}
				}
		
			
		}
		
		@Test (dependsOnMethods = { "createOffer" })
		public void createOrder() throws InterruptedException, IOException{
			quoteDetail.selectCreateOrder();
		}
		
		@Test (dependsOnMethods = { "createOrder" })
		public void submitOrder() throws BiffException, IOException, InterruptedException{
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (sh1.getCell(col_sheet, row_sheet).getContents().equals("SubmitOrder")) {
					if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String str = readExcel.getCellValue2(row_sheet,"SubmitOrder");														
						String orderName = str.split(":")[1].trim();
						quoteDetail.selectSubmitOrder(orderName);						
						}
					else{
						throw new SkipException("Skipping this testcase");
					}
					}
				}
			
		}
		@Test
		public void close(){
			driver.close();
		}
}
