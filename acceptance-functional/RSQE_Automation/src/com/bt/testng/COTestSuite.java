package com.bt.testng;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.bt.rsqe.AddProduct;
import com.bt.rsqe.Bulkconfiguration;
import com.bt.rsqe.Common;
import com.bt.rsqe.QuoteDetail;
import com.bt.rsqe.QuoteOption;
import com.bt.rsqe.RSQEWait;
import com.bt.rsqe.ReadExcel;



public class COTestSuite {
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
	
	public COTestSuite(WebDriver driver){
		COTestSuite.driver = driver;
		
	}
	
	public COTestSuite() throws BiffException, IOException {
		common = new Common();
		properties = new Properties();
		quoteOption = new QuoteOption(); 
		quoteDetail = new QuoteDetail();
		addprod = new AddProduct();
		readExcel = new ReadExcel();
		bulkconfig = new Bulkconfiguration();
		rsqewait = new RSQEWait();
		
		try {
			InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("Config.properties");
			properties.load(in);
			in.close();
			} catch (Exception e) {
			// TODO: handle exception
			}
		}
	
	@BeforeMethod
	public void getData() throws BiffException, IOException {	
        wk = Workbook.getWorkbook(new File(properties.getProperty("Test_Data")));
        sh1 = wk.getSheet(2);
		row_sheet1_count = sh1.getRows();
		col_sheet1_count = sh1.getColumns();
	}
	
	
	@Test
	public void launchURL() throws BiffException, IOException, WriteException {
		driver = Common.getDriver();
		//common.count();
		System.out.println(row_sheet1_count);
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(1, row_sheet).getContents().equals("Launch RSQE")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String url = readExcel.getCellValue1(row_sheet,"Launch RSQE");
					System.out.println("The Given URL ::"+url);
					common.LoadURL(url);
				} else {
					break;
				}
			}

		}
	}

	@Test
	public void createQuoteOption() throws BiffException, IOException,
			InterruptedException, WriteException {
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(1, row_sheet).getContents().equals("Create Quote Option")) {
				if ((sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes"))) {
					String createQuote = readExcel.getCellValue1(row_sheet,"Create Quote Option");
					String str1 = createQuote.split(",")[0].trim();
					String str2 = createQuote.split(",")[1].trim();
					String ContractTerm = str1.split(":")[1].trim();
					String Currency = str2.split(":")[1].trim();				
					quoteOption.createQuote(ContractTerm, Currency);
					common.screenshot();
					System.out.println(createQuote);
				} else {
					break;
				}
			}
		}
	}
	

	@Test
	public void selectQuoteOption() throws BiffException, IOException,
			InterruptedException {
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("quoteDetails")) {
				if ((sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes"))) {
					String quoteName = readExcel.getCellValue1(row_sheet,"quoteDetails");
					System.out.println("inside journey" + quoteName);
					quoteOption.getQuoteName(quoteName);
					quoteDetail.selectAddProduct();
				} else {
					// break mainloop;
					break;
				}
			}
		}
	}

	@Test
	public void addProduct() throws BiffException, IOException,
			InterruptedException, AWTException {
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("addProduct")) {
				if ((sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes"))) {
					String migrateProd = readExcel.getCellValue1(row_sheet,"addProduct");
					String str3 = migrateProd.split(",")[0].trim();
					String str4 = migrateProd.split(",")[1].trim();
					String str5 = migrateProd.split(",")[2].trim();
					String str6 = migrateProd.split(",")[3].trim();
					String productFamily = str3.split(":")[1].trim();
					String productVariant = str4.split(":")[1].trim();
					String productOffering = str5.split(":")[1].trim();
					String county = str6.split(":")[1].trim();					
					addprod.createAddProduct(productFamily,productVariant,productOffering, county);
					System.out.println("the Product values are "+ productFamily + "--->" + productVariant + "--->"+ productOffering + "--->" + county);
				   } else {
					break;
				}
			}
		}
	}
	
	
	
	//@Test(dependsOnMethods = { "addProduct" })
	@Test
	public void selectSiteID() throws BiffException, IOException,
			InterruptedException {
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("selectingSiteID")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str = readExcel.getCellValue1(row_sheet,"selectingSiteID");
					String siteName = str.split(":")[1].trim();
					rsqewait.waitForSiteTable(siteName);
					addprod.selectSiteID(siteName);
					
				} else {
					break;
				}
			}
		}
	}

	//@Test(dependsOnMethods = { "selectSiteID" })
	@Test
	public void addCentralServices() throws BiffException, IOException,
			InterruptedException {
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("CentralServices")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String serviceProduct = readExcel.getCellValue1(row_sheet,"CentralServices");
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
					break;
				}
			}
		}
	}
	
	//@Test(dependsOnMethods = { "addCentralServices" })
	@Test
	public void selectConfigProduct() throws BiffException, IOException{
		
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Configure Product")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {		
					String serviceProduct = readExcel.getCellValue1(row_sheet,"Configure Product");
					String counterValue = serviceProduct.split(":")[1].trim();
					addprod.clickConfigProduct(counterValue);
				}
			}
		}
	}
	
	

	@Test(dependsOnMethods = { "selectConfigProduct" })
	public void selectConnectOptimisationSite() throws InterruptedException, BiffException, IOException {
		outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Connect Optimisation Site")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str1 = readExcel.getCellValue1(row_sheet,"Connect Optimisation Site");	
					String Node = str1.split(":")[1].trim();						
					System.out.println("The value--->" + Node);
					//rsqewait.waitForPriceEnable();
					Thread.sleep(10000);
					bulkconfig.accordian(Node);
					//common.error1();
					break outerloop;
				} else {
					break;
				}
			}
		}
	}

	
	@Test(dependsOnMethods = { "selectConnectOptimisationSite" })
	public void selectTopology() throws InterruptedException, BiffException,IOException, AWTException {
		//Thread.sleep(5000);
		//Thread.sleep(5000);
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Topology")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str = readExcel.getCellValue1(row_sheet,"Topology");								
					String str7 = str.split(",")[0].trim();
					String str8 = str.split(",")[1].trim();
					String str9 = str.split(",")[2].trim();
					String str10 = str.split(",")[3].trim();
					
					String Node = str7.split(":")[1].trim();
					String product = str8.split(":")[1].trim();
					String candidate  = str9.split(":")[1].trim();	
					String fieldValue = str10.split(":")[1].trim();	
					
					System.out.println("The value--->" + Node);
					rsqewait.waitToLoad1(fieldValue);
					bulkconfig.accordian(Node);
					bulkconfig.selectDropDown(product, candidate);
					bulkconfig.selectCheckBox();
					//rsqewait.waitForPriceEnable();
					Thread.sleep(10000);
					common.error(driver);						
					//driver.navigate().refresh();
					//Alert alert = driver.switchTo().alert();
					//alert.accept();
					
				} else {
					break;
				}
			}
		}
	}

	@Test(dependsOnMethods = { "selectTopology" })
	public void selectBaseConfiguration() throws InterruptedException,
			AWTException, IOException, BiffException {
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);	
		bulkconfig.selectCheckBox();
		selectConnectOptimisationSite();
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Base Configuration")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {					
					String str = readExcel.getCellValue1(row_sheet,"Base Configuration");
					
					String str7 = str.split(",")[0].trim();
					String str8 = str.split(",")[1].trim();
					String str9 = str.split(",")[2].trim();
					
					String Node = str7.split(":")[1].trim();
					String product = str8.split(":")[1].trim();
					String candidate  = str9.split(":")[1].trim();
					
					System.out.println("The value--->" + Node);	
					
					bulkconfig.accordian(Node);
					common.error1();
					//bulkconfig.selectDropDown1(product, candidate);								
					bulkconfig.selectCheckBox();	
					
				} else {
					break;
				}
			}
		}
	}

	@Test(dependsOnMethods = { "selectBaseConfiguration" })
	public void selectIPCgGatewaySpecification() throws InterruptedException, IOException, BiffException {
		//Thread.sleep(2000);
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("IPCg Gateway Specification")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str = readExcel.getCellValue1(row_sheet,"IPCg Gateway Specification");					
					String str7 = str.split(",")[0].trim();
					String str8 = str.split(",")[1].trim();			
					String str9 = str.split(",")[2].trim();
					String str10 = str.split(",")[3].trim();	
					
					String Node = str7.split(":")[1].trim();						
					String product = str8.split(":")[1].trim();
					String candidate  = str9.split(":")[1].trim();	
					String fieldvalue = str10.split(":")[1].trim();
					
					System.out.println("The value--->" + Node);
					//rsqewait.waitForPriceEnable();
					Thread.sleep(5000);
					bulkconfig.accordian(Node);
					rsqewait.waitToLoadCO(fieldvalue);
					bulkconfig.selectConfigure(product);
					bulkconfig.selectPopup(candidate);	
					bulkconfig.selectCheckBox();									
				  } else {
					break;
				}
			}
		}
	}

	@Test(dependsOnMethods = { "selectIPCgGatewaySpecification" })
	public void selectWANConnection() throws InterruptedException, IOException, BiffException {
		//Thread.sleep(2000);
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("WAN Connection")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str = readExcel.getCellValue1(row_sheet,"WAN Connection");
					String str7 = str.split(",")[0].trim();
					String str8 = str.split(",")[1].trim();
					String str9 = str.split(",")[2].trim();
					String str10 = str.split(",")[3].trim();									
					String Node = str7.split(":")[1].trim();					
					String product = str8.split(":")[1].trim();
					String candidate  = str9.split(":")[1].trim();	
					String fieldvalue = str10.split(":")[1].trim();					
					System.out.println("The value--->" + Node);				
					//rsqewait.waitForPriceEnable();
					Thread.sleep(10000);
					bulkconfig.accordian(Node);
					rsqewait.waitToLoadCO(fieldvalue);
				//	bulkconfig.selectConfigure(product);
				//	bulkconfig.selectPopup3(candidate);									
					//bulkconfig.selectCheckBox();	
					//fop.selectWanConnection();
				} else {
					break;
				}
			}
		}
	}

	@Test(dependsOnMethods = { "selectWANConnection" })
	public void selectIPEngine() throws InterruptedException, BiffException,IOException {
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("IPEngine")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str = readExcel.getCellValue1(row_sheet,"IPEngine");
					String str7 = str.split(",")[0].trim();
					String str8 = str.split(",")[1].trim();
					String str9 = str.split(",")[2].trim();
					String str10 = str.split(",")[3].trim();
					
					String Node = str7.split(":")[1].trim();
					String product = str8.split(":")[1].trim();
					String candidate  = str9.split(":")[1].trim();
					String fieldvalue = str10.split(":")[1].trim();
					
					System.out.println("The value--->" + Node);
					Thread.sleep(5000);					
					bulkconfig.accordian(Node);
					rsqewait.waitToLoadCO(fieldvalue);
					bulkconfig.selectConfigure(product);
					bulkconfig.selectPopup(candidate);	
					Thread.sleep(5000);
					Thread.sleep(5000);
					bulkconfig.selectCheckBox();
					//fop.selectIPEngine(IPEngineConfig);
				} else {
					break;
				}
			}
		}
	}

	@Test(dependsOnMethods = { "selectIPEngine" })
	public void selectIPEngBaseConfiguration() throws BiffException,
			IOException, InterruptedException {
		System.out.println("inside the IPEBase Configuration");
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("IPEBase Configuration")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str = readExcel.getCellValue1(row_sheet,"IPEBase Configuration");
					String str7 = str.split(",")[0].trim();
					String str8 = str.split(",")[1].trim();
					String str9 = str.split(",")[2].trim();
					String str10 = str.split(",")[3].trim();
					String str11 = str.split(",")[4].trim();	
					String str12 = str.split(",")[5].trim();	
					
					String Node = str7.split(":")[1].trim();
					String product = str8.split(":")[1].trim();
					String candidate  = str9.split(":")[1].trim();	
					String product1 = str10.split(":")[1].trim();
					String candidate1  = str11.split(":")[1].trim();
					String fieldvalue = str12.split(":")[1].trim();
					System.out.println("The value--->" + Node);
					/*Thread.sleep(5000);
					Thread.sleep(5000);*/
			/*		Thread.sleep(5000);
					Thread.sleep(5000);
					Thread.sleep(5000);*/
					//rsqewait.waitForPriceEnable();
					Thread.sleep(10000);
					bulkconfig.IPEAccordian(Node);
					rsqewait.waitToLoadCO(fieldvalue);
				  //  caf.selectDropDown1(product, candidate);
				   bulkconfig.selectDropDown1(product,candidate);
					bulkconfig.selectDropDown1(product1,candidate1);	
					bulkconfig.selectCheckBox();
					//res = fop.selectIPEBaseConfig(SoftwareCapacity, driver);

				} else {
					break;
				}
			}
		}

	}

	@Test(dependsOnMethods = { "selectIPEngBaseConfiguration" })
	public void selectCPESupplierMaintenance() throws InterruptedException,BiffException, IOException {
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("CPE Supplier Maintenance")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str = readExcel.getCellValue1(row_sheet,"CPE Supplier Maintenance");
					String str7 = str.split(",")[0].trim();
					String str8 = str.split(",")[1].trim();
					String str9 = str.split(",")[2].trim();
					String str10 = str.split(",")[3].trim();
					
					String Node = str7.split(":")[1].trim();
					String product = str8.split(":")[1].trim();
					String candidate  = str9.split(":")[1].trim();	
					String fieldvalue = str10.split(":")[1].trim();
					
					System.out.println("The value--->" + Node);
				/*	Thread.sleep(5000);
					Thread.sleep(5000);
					Thread.sleep(5000);*/
					//rsqewait.waitForPriceEnable();
					Thread.sleep(5000);
					bulkconfig.IPEAccordian(Node);
					//rsqewait.waitToLoadCO(fieldvalue);
					Thread.sleep(15000);
					bulkconfig.selectConfigure(product);
				    Thread.sleep(10000);
				    bulkconfig.selectPopup(candidate);	
				    bulkconfig.selectCheckBox();
					
					//res = fop.selecCPESupplierMaint(SupplierMaint, driver);
				} else {
					break;
				}
			}
		}
	}

	             
	@Test(dependsOnMethods = { "selectCPESupplierMaintenance" })
	public void selectConnectOptimisationCentralService()
			throws InterruptedException, BiffException, IOException {
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Connect Optimisation Central Service")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str = readExcel.getCellValue1(row_sheet,"Connect Optimisation Central Service");
					String str7 = str.split(",")[0].trim();
					String str8 = str.split(",")[1].trim();
					String str9 = str.split(",")[2].trim();
					String str10 = str.split(",")[3].trim();
					
					String Node = str7.split(":")[1].trim();
					String product = str8.split(":")[1].trim();
					String candidate  = str9.split(":")[1].trim();
					String fieldvalue = str10.split(":")[1].trim();
					
					System.out.println("The value--->" + Node);
					//Thread.sleep(5000);
					Thread.sleep(5000);
					Thread.sleep(10000);
					//rsqewait.waitForPriceEnable();
					bulkconfig.accordian1(Node);
					rsqewait.waitToLoadCO(fieldvalue);
				  //  caf.selectConfigure(product);
					//caf.selectPopup(candidate);	
					bulkconfig.selectCheckBox();
					//res = fop.selectConnectOptCentralServ(driver);
				} else {
					break;
				}
			}
		}
		Thread.sleep(5000);
		Thread.sleep(5000);
		selectConnectOptimisationSite();
	}

	@Test(dependsOnMethods = { "selectConnectOptimisationCentralService" })
	public void selectOffnetAccess() throws InterruptedException,
			BiffException, IOException {
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Offnet Access")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str = readExcel.getCellValue1(row_sheet,"Offnet Access");								
					String str7  = str.split(",")[0].trim();
					String str8  = str.split(",")[1].trim();
					String str9  = str.split(",")[2].trim();
					String str10 = str.split(",")[3].trim();
					String str11 = str.split(",")[4].trim();	
					String str12 = str.split(",")[5].trim();
					String Node = str7.split(":")[1].trim();
					String product = str8.split(":")[1].trim();
					String candidate  = str9.split(":")[1].trim();
					String product1 = str10.split(":")[1].trim();
					String candidate1  = str11.split(":")[1].trim();
					String fieldvalue  = str12.split(":")[1].trim();
					System.out.println("The value--->" + Node);
					Thread.sleep(5000);
					//rsqewait.waitForPriceEnable();
					bulkconfig.accordian(Node);
					rsqewait.waitToLoad(fieldvalue);
					Thread.sleep(5000);
					bulkconfig.selectInput(product, candidate);
				    Thread.sleep(5000);
				 // bulkconfig.selectDropDown1(product1, candidate1);
				    bulkconfig.selectCheckBox();					
					Thread.sleep(5000);
					//res = fop.offnetAccess(accessTech, connectionRole, driver);
				} else {
					break;
				}
			}
		}
		Thread.sleep(5000);
		Thread.sleep(5000);
		//selectConnectOptimisationSite();
		//caf.selectQuoteDetail();		
	}
	
	
	
	@Test(dependsOnMethods = { "selectOffnetAccess" })
	public void selectPrice() throws InterruptedException, IOException{
		quoteDetail.selectQuoteDetail();
		quoteDetail.selectlineItems();
		quoteDetail.selectCalcPrice();
	/*	quoteDetail.refresh();
		quoteDetail.selectlineItems();
		quoteDetail.selectCalcPrice();*/
		
		
	}
	
	
	@Test(dependsOnMethods = { "selectPrice" })
	public void createOffer() throws BiffException, IOException, InterruptedException{
		outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Offer")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str = readExcel.getCellValue1(row_sheet,"Offer");								
					String str7 = str.split(",")[0].trim();
					String str8 = str.split(",")[1].trim();
					String offerName = str7.split(":")[1].trim();
					String OrderRef = str8.split(":")[1].trim();
					quoteDetail.selectCreateOffer(offerName, OrderRef);							
					}
				}
			}
	
		
	}
	
	@Test(dependsOnMethods = { "createOffer" })
	public void createOrder() throws InterruptedException, IOException{
		quoteDetail.selectCreateOrder();
	}
	
	@Test(dependsOnMethods = { "createOrder" })
	public void submitOrder() throws BiffException, IOException, InterruptedException{
		outerloop: for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("SubmitOrder")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str = readExcel.getCellValue1(row_sheet,"SubmitOrder");														
					String orderName = str.split(":")[1].trim();
					quoteDetail.selectSubmitOrder(orderName);						
					}
				}
			}
		
	}
	
	
	@Test
	public void selectCentralServiceBaseConfig()
			throws InterruptedException, BiffException, IOException {
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Central Service BaseConfig")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str = readExcel.getCellValue1(row_sheet,"Central Service BaseConfig");
					String str7 = str.split(",")[0].trim();
					String str8 = str.split(",")[1].trim();
					String str9 = str.split(",")[2].trim();
					String str10 = str.split(",")[3].trim();
					
					String Node = str7.split(":")[1].trim();
					String product = str8.split(":")[1].trim();
					String candidate  = str9.split(":")[1].trim();	
					String fieldvalue = str10.split(":")[1].trim();					
					System.out.println("The value--->" + Node);
					rsqewait.waitToLoad(fieldvalue);
					bulkconfig.accordian(Node);
					bulkconfig.selectDropDown1(product, candidate);
					Thread.sleep(10000);
					//bulkconfig.selectCheckBox();
					//caf.refresh();
					//res = fop.selectConnectOptCentralServ(driver);
				} else {
					break;
				}
			}
		}
	}

	
	
	@Test
	public void selectCentralServiceTeleEngine()
			throws InterruptedException, BiffException, IOException, AWTException {
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("Central Service TeleEngine")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str = readExcel.getCellValue1(row_sheet,"Central Service TeleEngine");
					String str7 = str.split(",")[0].trim();
					String str8 = str.split(",")[1].trim();
					String str9 = str.split(",")[2].trim();
					String str10 = str.split(",")[3].trim();
					
					String Node = str7.split(":")[1].trim();
					String product = str8.split(":")[1].trim();
					String candidate  = str9.split(":")[1].trim();	
					String fieldvalue  = str10.split(":")[1].trim();
					
					System.out.println("The value--->" + Node);
					
					//rsqewait.waitForPriceEnable();
					Thread.sleep(5000);
					bulkconfig.accordian(Node);
					rsqewait.waitToLoad1(fieldvalue);
					Thread.sleep(5000);
					bulkconfig.selectConfigure(product);
					bulkconfig.selectPopup(candidate);		
					common.error2();
					bulkconfig.selectCheckBox();					
					quoteDetail.refresh();
				} else {
					break;
				}
			}
		}
	}
	
	@Test
	public void selectTeleEngineBaseConfig()
			throws InterruptedException, BiffException, IOException {
		
		for (int row_sheet = 1; row_sheet < row_sheet1_count; row_sheet++) {
			if (sh1.getCell(col_sheet, row_sheet).getContents().equals("TeleEngine BaseConfig")) {
				if (sh1.getCell(2, row_sheet).getContents().equalsIgnoreCase("Yes")) {
					String str = readExcel.getCellValue1(row_sheet,"TeleEngine BaseConfig");
					String str7 = str.split(",")[0].trim();
					String str8 = str.split(",")[1].trim();
					String str9 = str.split(",")[2].trim();
					String str10 = str.split(",")[3].trim();
					String str11 = str.split(",")[4].trim();
					
					String Node = str7.split(":")[1].trim();
					String product = str8.split(":")[1].trim();
					String candidate  = str9.split(":")[1].trim();
					String fieldvalue  = str10.split(":")[1].trim();
					String fieldvalue1  = str11.split(":")[1].trim();
					
					System.out.println("The value--->" + Node);
					
					rsqewait.waitToLoad(fieldvalue);
					bulkconfig.accordian("TeleEngine");
					//rsqewait.waitForPriceEnable();
					Thread.sleep(5000);
					bulkconfig.IPEAccordian(Node);	
					rsqewait.waitToLoad1(fieldvalue1);
					bulkconfig.selectInput(product, candidate);
					bulkconfig.selectCheckBox();					
				} else {
					break;
				}
			}
		}
	}
	
	@Test
	public void close(){
		driver.close();
	}

}
