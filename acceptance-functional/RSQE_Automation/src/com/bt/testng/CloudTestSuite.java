 package com.bt.testng;
	import java.awt.AWTException;
	import java.io.File;
	import java.io.IOException;
	import java.io.InputStream;
	import java.util.List;
	import java.util.Properties;

	import jxl.Sheet;
	import jxl.Workbook;
	import jxl.read.biff.BiffException;
	import jxl.write.WritableSheet;
	import jxl.write.WritableWorkbook;
	import jxl.write.WriteException;

	import org.openqa.selenium.By;
	import org.openqa.selenium.WebDriver;
	import org.openqa.selenium.WebElement;
	import org.testng.Assert;
	import org.testng.SkipException;
	import org.testng.annotations.BeforeMethod;
	import org.testng.annotations.Test;
    
	import com.bt.rsqe.AddProduct;
	import com.bt.rsqe.Bulkconfiguration;
import com.bt.rsqe.Cloud;
	import com.bt.rsqe.Common;
	import com.bt.rsqe.QuoteDetail;
	import com.bt.rsqe.QuoteOption;
	import com.bt.rsqe.RSQEWait;
import com.bt.rsqe.ReadExcel;


	public class CloudTestSuite {
		Workbook wk;
		WritableWorkbook workbookCopy;
		Sheet cloudSheet;
		Properties properties;
		ReadExcel readExcel;
		QuoteOption quoteOption;
		QuoteDetail quoteDetail;
		AddProduct addprod;
		Bulkconfiguration bulkconfig;
		RSQEWait rsqewait;
		Cloud cloud;
		int row_sheet1_count;
		int col_sheet1_count;
		int col_sheet = 1;
		String res = "";
		private static WebDriver driver;
		public Common common;
		
		public CloudTestSuite(WebDriver driver){
			CloudTestSuite.driver = driver;		
		}
		
		public CloudTestSuite() throws BiffException, IOException {		
			common = new Common();
			properties = new Properties();
			quoteOption = new QuoteOption(); 
			quoteDetail = new QuoteDetail();
			addprod = new AddProduct();
			readExcel = new ReadExcel();
			bulkconfig = new Bulkconfiguration();
			rsqewait = new RSQEWait();
			cloud = new Cloud();
			
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

	        cloudSheet = wk.getSheet(4);
			row_sheet1_count = cloudSheet.getRows();
			col_sheet1_count = cloudSheet.getColumns();
		}
		
		
		@Test 
		public void launchURL() throws BiffException, IOException, WriteException, InterruptedException {
			driver = Common.getDriver();
			System.out.println(row_sheet1_count);
			for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(1, row_sheet).getContents().equals("Launch RSQE")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String url = readExcel.getCloudCellValue(row_sheet,"Launch RSQE");
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
				if (cloudSheet.getCell(1, row_sheet).getContents().equals("Create Quote Option")) {
					if ((cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes"))) {
						String createQuote = readExcel.getCloudCellValue(row_sheet,"Create Quote Option");
						String str1 = createQuote.split(",")[0].trim();
						String str2 = createQuote.split(",")[1].trim();
						String ContractTerm = str1.split(":")[1].trim();
						String Currency = str2.split(":")[1].trim();
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
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("quoteDetails")) {
					if ((cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes"))) {
						String quoteName = readExcel.getCloudCellValue(row_sheet,"quoteDetails");
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
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("addProduct")) {
					if ((cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes"))) {
						String migrateProd = readExcel.getCloudCellValue(row_sheet,"addProduct");
						String str3 = migrateProd.split(",")[0].trim();
						String str4 = migrateProd.split(",")[1].trim();
						String str5 = migrateProd.split(",")[2].trim();
						String productFamily = str3.split(":")[1].trim();
						String productVariant = str4.split(":")[1].trim();
						String productOffering = str5.split(":")[1].trim();					
						cloud.selectAddProduct(productFamily, productVariant,productOffering);
						System.out.println("the Product values are "+ productFamily + "--->" + productVariant + "--->"+ productOffering);
						} else {
							throw new SkipException("Skipping this testcase");
					}
				}
			}
		}
		
		@Test
		public void selectContinueToQuoteDetails() throws BiffException, IOException,
				InterruptedException, AWTException {
			for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("ContinueToQuoteDetails")) {
					if ((cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes"))) {
						addprod.clickContinueToQuoteDetails();
						} else {
							throw new SkipException("Skipping this testcase");
					}
				}
			}
		}
		
		@Test 
		void selectConfigProduct() throws InterruptedException{
			//addprod.clickConfigProduct(counterValue)
			addprod.clickConfigProduct();
		}
		
		@Test
		void selectDirectConnectServiceProvider() throws BiffException, IOException, InterruptedException{
			System.out.println("DirectConnectServiceProvider");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("DirectConnectServiceProvider")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"DirectConnectServiceProvider");								
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();						
						System.out.println("The value--->" + Node);
						bulkconfig.accordian(Node);
						bulkconfig.selectDropDown(product, candidate);
						Thread.sleep(5000);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		@Test
		void selectBaseConfig() throws BiffException, IOException, InterruptedException{
			System.out.println("BaseConfiguration");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("BaseConfiguration")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"BaseConfiguration");								
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();
						String str10 = centManag.split(",")[3].trim();
						String str11 = centManag.split(",")[4].trim();
						String str12 = centManag.split(",")[5].trim();
						String str13 = centManag.split(",")[6].trim();	
						String str14 = centManag.split(",")[7].trim();
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();	
						String product1 = str10.split(":")[1].trim();
						String candidate1  = str11.split(":")[1].trim();	
						String product2 = str12.split(":")[1].trim();
						String candidate2  = str13.split(":")[1].trim();
						String fieldvalue  = str14.split(":")[1].trim();
						System.out.println("The value--->" + Node);
						bulkconfig.accordian(Node);
						//cloudFun.waitForBaseConfig();
						rsqewait.waitToLoad(fieldvalue);
						bulkconfig.selectDropDown1(product, candidate);
						bulkconfig.selectInput(product1,candidate1);
						bulkconfig.selectDropDown1(product2, candidate2);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		} 
		
		@Test
		void selectVPNSpecification() throws BiffException, IOException, InterruptedException{
			System.out.println("selectVPNSpecification");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("VPNSpecification")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"VPNSpecification");
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();				
						String Node = str7.split(":")[1].trim();	
						String fieldValue = str8.split(":")[1].trim();	
						System.out.println("The value--->" + Node);
						bulkconfig.accordian(Node);
						//cloudFun.waitForVPNSpec();
						rsqewait.waitToLoad(fieldValue);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		} 
		
		@Test
		void selectProviderConnection() throws BiffException, IOException, InterruptedException{
			System.out.println("selectProviderConnection");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("Provider Connection")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"Provider Connection");
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();					
						String Node = str7.split(":")[1].trim();	
						String fieldValue = str8.split(":")[1].trim();					
						System.out.println("The value--->" + Node);
						bulkconfig.accordian1(Node);
						//cloudFun.waitForProviderConnection();
						rsqewait.waitToLoad(fieldValue);
						bulkconfig.selectCheckBox();
						Thread.sleep(10000);
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		} 
		
		@Test
		void selectProviderConnectionBaseConfiguration() throws BiffException, IOException, InterruptedException{
			System.out.println("selectProviderConnectionBaseConfiguration");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("Base Configuration")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"Base Configuration");												
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();	
						String str10 = centManag.split(",")[3].trim();	
						
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();	
						String fieldValue = str10.split(":")[1].trim();
						
						System.out.println("The value--->" + Node);
						bulkconfig.IPEAccordian(Node);					
						//cloudFun.waitForProviderConnectionBaseConfig();
						rsqewait.waitToLoad(fieldValue);
						bulkconfig.selectDropDown1(product,candidate);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		} 
		
		
		@Test
		void selectProviderConnectionBaseConfigurationBTCloud() throws BiffException, IOException, InterruptedException{
			System.out.println("selectProviderConnectionBaseConfiguration");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("BTCloudBaseConfiguration")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"BTCloudBaseConfiguration");												
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();	
						String str10 = centManag.split(",")[3].trim();	
						
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();	
						String fieldValue = str10.split(":")[1].trim();
						
						System.out.println("The value--->" + Node);
						bulkconfig.IPEAccordian(Node);					
						//cloudFun.waitForProviderConnectionBaseConfig();
						rsqewait.waitToLoad(fieldValue);
						bulkconfig.selectDropDown1(product,candidate);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		} 
		@Test
		void selectDirectConnectServiceProvider_Public() throws BiffException, IOException, InterruptedException{
			System.out.println("DirectConnectServiceProvider_Public(");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("DirectConnectServiceProvider_Public")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"DirectConnectServiceProvider_Public");								
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();						
						System.out.println("The value--->" + Node);
						bulkconfig.accordian(Node);
						bulkconfig.selectDropDown(product, candidate);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		
		@Test
		void selectCloudFirewallService() throws BiffException, IOException, InterruptedException{
			System.out.println("CloudFirewallService");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("Cloud Firewall Service")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"Cloud Firewall Service");								
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();
						String str10 = centManag.split(",")[3].trim();
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();		
						String fieldValue = str10.split(":")[1].trim();
						System.out.println("The value Node--->" + Node);
						bulkconfig.accordian(Node);
						rsqewait.waitForCloudFirewallServices();
						System.out.println("The value fieldValue--->" + fieldValue);
						//cloudFun.waitToLoad(fieldValue);
						bulkconfig.selectConfigure(product);
						bulkconfig.selectPopup(candidate);	
						Thread.sleep(25000);
						Thread.sleep(25000);
						Thread.sleep(25000);
						bulkconfig.selectCheckBox();
						//cloudFun.waitForCloudFirewallServicesUpload();
						//Thread.sleep(20000);
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		@Test
		void selectCloudServiceLeg() throws BiffException, IOException, InterruptedException{
			System.out.println("CloudFirewallService");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("Cloud Service Leg")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"Cloud Service Leg");								
						//String str7 = centManag.split(",")[0].trim();					
						String Node = centManag.split(":")[1].trim();										
						System.out.println("The value--->" + Node);		
						//rsqewait.waitForPriceEnable();
						bulkconfig.accordian(Node);
						rsqewait.waitForCloudServicesLeg();
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		
		@Test
		void selectCloudLegFirewallService() throws BiffException, IOException, InterruptedException{
			System.out.println("selectCloudLegFirewallService");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("Firewall Service")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"Firewall Service");								
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();						
						System.out.println("The value--->" + Node);
						bulkconfig.accordian1(Node);
						rsqewait.waitForFirewallServices();
						//cloudFun.selectConfigurePopup(product, candidate);
						cloud.selectHeaderConfig();
						//cloudFun.selectConfigure(product);
						bulkconfig.selectPopup(candidate);
						Thread.sleep(25000);
						Thread.sleep(25000);
						Thread.sleep(25000);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		
		@Test
		void selectCloudConnectDirect () throws BiffException, IOException, InterruptedException{
			
			System.out.println("selectCloudConnectDirect");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("Cloud Connect Direct")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"Cloud Connect Direct");																		
						String Node = centManag.split(":")[1].trim();	
						Thread.sleep(5000);
						//cloudFun.waitForRestoreisEnabled();
						bulkconfig.accordian(Node);	
						//caf.selectCheckBox();
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		
		@Test
		void selectCloudFirewallServices() throws BiffException, IOException, InterruptedException{
			
			System.out.println("selectCloudFirewallServices");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("Cloud Firewall Services")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"Cloud Firewall Services");																		
						String Node = centManag.split(":")[1].trim();													
						//cloudFun.waitForCloudFirewallServicesUpload();
						bulkconfig.accordian(Node);
						//caf.selectCheckBox();
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		@Test
		void selectCFSBaseConfiguration() throws BiffException, IOException, InterruptedException{
			
			System.out.println("selectCFSBaseConfiguration");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("CFSBaseConfiguration")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"CFSBaseConfiguration");
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();					
						String str10 = centManag.split(",")[3].trim();
						String str11 = centManag.split(",")[4].trim();					
						String str12 = centManag.split(",")[5].trim();
						String str13 = centManag.split(",")[6].trim();					
						String str14 = centManag.split(",")[7].trim();
															
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();
						String product1 = str10.split(":")[1].trim();
						String candidate1  = str11.split(":")[1].trim();
						String product2 = str12.split(":")[1].trim();
						String candidate2  = str13.split(":")[1].trim();
						String fieldValue = str14.split(":")[1].trim();			
										
						bulkconfig.accordian(Node);
						rsqewait.waitToLoad(fieldValue);
						bulkconfig.selectDropDown1(product, candidate);
						bulkconfig.selectDropDown1(product1, candidate1);
						bulkconfig.selectInput(product2, candidate2);		
						bulkconfig.selectCheckBox();
						
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		@Test
		void selectCFSUser() throws BiffException, IOException, InterruptedException{
			
			System.out.println("selectCFSUser");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("CFSUser")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"CFSUser");
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();		
						String str10 = centManag.split(",")[3].trim();
						
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();
						String fieldValue = str10.split(":")[1].trim();									
						rsqewait.waitForPriceEnable();
						bulkconfig.accordian(Node);
						rsqewait.waitToLoad(fieldValue);
						bulkconfig.selectConfigure(product);
						bulkconfig.selectPopup(candidate);
						Thread.sleep(25000);
						Thread.sleep(25000);
						bulkconfig.selectCheckBox();
						
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		@Test
		void selectCFSUserBaseConfig() throws BiffException, IOException, InterruptedException{
			
			System.out.println("CFSUserBaseConfiguration");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("CFSUserBaseConfiguration")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"CFSUserBaseConfiguration");
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();		
						String str10 = centManag.split(",")[3].trim();
						
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();
						String fieldValue = str10.split(":")[1].trim();									
						rsqewait.waitForPriceEnable();
						bulkconfig.IPEAccordian(Node);
						rsqewait.waitToLoad(fieldValue);
						bulkconfig.selectInput(product, candidate);				
						bulkconfig.selectCheckBox();
						
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		
		@Test
		void selectDirectConnectServiceProvider_BTCloud() throws BiffException, IOException, InterruptedException{
			System.out.println("DirectConnectServiceProvider_BTCloud(");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("DirectConnectServiceProvider_BTCloud")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"DirectConnectServiceProvider_BTCloud");								
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();						
						System.out.println("The value--->" + Node);
						bulkconfig.accordian(Node);
						bulkconfig.selectDropDown(product, candidate);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		@Test
		void selectBaseConfiguration_BTCloud() throws BiffException, IOException, InterruptedException{
			System.out.println("BaseConfig_BTCloud(");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("BaseConfig_BTCloud")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"BaseConfig_BTCloud");								
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();
						String str10 = centManag.split(",")[3].trim();
						String str11 = centManag.split(",")[4].trim();
						String str12 = centManag.split(",")[5].trim();
														
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();	
						String product1 = str10.split(":")[1].trim();
						String candidate1  = str11.split(":")[1].trim();
						String fieldValue  = str12.split(":")[1].trim();	
						
						System.out.println("The value--->" + Node);
						rsqewait.waitForPriceEnable();
						bulkconfig.accordian(Node);
						rsqewait.waitToLoad(fieldValue);
						bulkconfig.selectDropDown1(product, candidate);
						bulkconfig.selectDropDown1(product1, candidate1);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		

		@Test
		void selectDirectConnectServiceProvider_Azure() throws BiffException, IOException, InterruptedException{
			System.out.println("DirectConnectServiceProvider_Azure(");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("DirectConnectServiceProvider_Azure")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"DirectConnectServiceProvider_Azure");								
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();						
						System.out.println("The value--->" + Node);
						bulkconfig.accordian(Node);
						bulkconfig.selectDropDown(product, candidate);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		@Test
		void selectBaseConfiguration_Azure() throws BiffException, IOException, InterruptedException{
			System.out.println("BaseConfig_Azure(");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("BaseConfig_Azure")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"BaseConfig_Azure");								
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();
						String str10 = centManag.split(",")[3].trim();
						String str11 = centManag.split(",")[4].trim();
						String str12 = centManag.split(",")[5].trim();
						String str13 = centManag.split(",")[6].trim();
						String str14 = centManag.split(",")[7].trim();
						
						String Node = str7.split(":")[1].trim();
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();	
						String product1 = str10.split(":")[1].trim();
						String candidate1  = str11.split(":")[1].trim();
						String product2 = str12.split(":")[1].trim();
						String candidate2  = str13.split(":")[1].trim();
						String fieldValue  = str14.split(":")[1].trim();	
						
						System.out.println("The value--->" + Node);
						rsqewait.waitForPriceEnable();
						bulkconfig.accordian(Node);
						rsqewait.waitToLoad(fieldValue);
						bulkconfig.selectDropDown1(product, candidate);
						bulkconfig.selectDropDown1(product1, candidate1);
						bulkconfig.selectDropDown1(product2, candidate2);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		@Test
		void selectMSExpressRouteServiceConnection() throws BiffException, IOException, InterruptedException{
			System.out.println("selectMSExpressRouteServiceConnection");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("MS Express Route Service Connection")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"MS Express Route Service Connection");								
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();	
						
						String Node = str7.split(":")[1].trim();			
						String fieldValue  = str8.split(":")[1].trim();						
						System.out.println("The value--->" + Node);
						rsqewait.waitForPriceEnable();
						bulkconfig.accordian(Node);
						rsqewait.waitToLoad(fieldValue);				
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		@Test
		void selectMSExpressRouteServiceConnectionBaseConfig() throws BiffException, IOException, InterruptedException{
			System.out.println("selectMSExpressRouteServiceConnection");
			outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
				if (cloudSheet.getCell(col_sheet, row_sheet).getContents().equals("MS Express BaseConfig")) {
					if (cloudSheet.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
						String centManag = readExcel.getCloudCellValue(row_sheet,"MS Express BaseConfig");								
						String str7 = centManag.split(",")[0].trim();
						String str8 = centManag.split(",")[1].trim();
						String str9 = centManag.split(",")[2].trim();
						String str10 = centManag.split(",")[3].trim();					
						String Node = str7.split(":")[1].trim();	
						String product = str8.split(":")[1].trim();
						String candidate  = str9.split(":")[1].trim();						
						String fieldValue  = str10.split(":")[1].trim();																	
						System.out.println("The value--->" + Node);
						rsqewait.waitForPriceEnable();
						bulkconfig.IPEAccordian(Node);
						rsqewait.waitToLoad(fieldValue);	
						bulkconfig.selectDropDown1(product, candidate);
						bulkconfig.selectCheckBox();
						break outerloop;
					} else {
						throw new SkipException("Skipping this testcase");
					}
				}
			}		
		}
		
		@Test
		public void selectQutoeDetail() throws InterruptedException{
			//cloudFun.waitForRestoreisEnabled();
			Thread.sleep(2500);
			cloud.selectQuoteDetail();
			cloud.selectLineItem();
			cloud.calculatePrice();		
		}
		
		
	}



