package com.bt.rsqe;
import org.openqa.selenium.WebDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import jxl.read.biff.BiffException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.By;
import org.testng.Assert;
import com.bt.rsqe.Common;
import com.bt.rsqe.ObjectMap;
import com.google.common.base.Function;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class AddProduct {
	ObjectMap map;
	String QuoteName = "";
	String result="";
	Common common;
	RSQEWait rsqewait;
	private static WebDriver driver;
	
	public AddProduct(WebDriver driver){
		AddProduct.driver = driver;
		
	}

	public AddProduct() {
		map = new ObjectMap();
		common= new Common();
		rsqewait = new RSQEWait();
	}
	
	public void createAddProduct(String productFamily,String productVariant,String productOffering,String county)throws InterruptedException, AWTException, IOException {
		driver = Common.getDriver();
		//Thread.sleep(4000);	
		productDetail(productFamily, productVariant, productOffering, county);
		common.screenshot();	
	}

	public void createMigrateProduct(String productFamily, String productVariant,String productOffering, String county)throws InterruptedException, AWTException, IOException {
		driver = Common.getDriver();
		driver.findElement(map.getLocator("Migrateproduct")).click();
		productDetail(productFamily, productVariant, productOffering, county);
		common.screenshot();
	}
	 
	public void productDetail(String productFamily, String productVariant,
			String productOffering, String country)throws InterruptedException, AWTException {
		//Thread.sleep(5000);
		driver = Common.getDriver();	
		WebElement Family = rsqewait.waitForVisibleElement("ProductFamily",5);  
		Select prodFamily = new Select(Family);
		//Select prodFamily = new Select(driver.findElement(map.getLocator("ProductFamily")));
		prodFamily.selectByVisibleText(productFamily);
		WebElement option = prodFamily.getFirstSelectedOption();
		System.out.println("option.getText()"+option.getText());
		
		Select prodVariant = new Select(driver.findElement(map.getLocator("ProductVariant")));
		prodVariant.selectByVisibleText(productVariant);
		WebElement option2 = prodVariant.getFirstSelectedOption();
		System.out.println("option2.getText()"+option2.getText());
		
		Select prodOffering = new Select(driver.findElement(map.getLocator("ProductOffering")));
		prodOffering.selectByVisibleText(productOffering);
		WebElement option3 = prodOffering.getFirstSelectedOption();
		System.out.println("option3.getText()"+option3.getText());

		WebElement Country = driver.findElement(map.getLocator("Country"));
		Country.click();
		Country.sendKeys(country);
		Robot robot;
		robot = new Robot();
		robot.keyPress(KeyEvent.VK_ENTER);
		robot.keyRelease(KeyEvent.VK_ENTER);
		System.out.println(">>>>"+Country.getText());
		
	//  Assert.assertEquals(option.getText(),productFamily);
	//	Assert.assertEquals(option2.getText(), prodVariant);
	//	Assert.assertEquals(option3.getText(), prodOffering);
		
	}
	
	public void selectSiteID(String siteID) throws InterruptedException, IOException{
		//Thread.sleep(15000);
		driver = Common.getDriver();
		boolean status = false;
		WebElement table = rsqewait.waitForVisibleElement("SiteTable", 2);
		//WebElement table = driver.findElement(By.id("siteTable"));
		List<WebElement> rows = table.findElements(By.tagName("tr"));
        int rowsize = rows.size();
        System.out.println("Total row counts"+rowsize);
        outerloop:for (WebElement row : rows ) {		      
        	List<WebElement>cells =	row.findElements(By.tagName("td"));		            
        	int colsize = cells.size();
		    System.out.println("Total column counts"+colsize);			            
		    		for (WebElement col : cells ) {
			           System.out.println("site values are "+col.getText());
			           String sitevalue  =col.getText();
			            	 if(sitevalue.equalsIgnoreCase(siteID)){	
			            		  row.findElement(By.tagName("input")).click();                                                                                                              
			            		  break outerloop;
			            	 	}
			             	}
					}
            common.screenshot();
            WebElement AddProduct = rsqewait.waitForVisibleElement("SubmitProduct", 2);
          //  WebElement AddProduct = driver.findElement(map.getLocator("SubmitProduct"));
            status = AddProduct.isEnabled();
            Assert.assertEquals(status, true);	           
            AddProduct.click();	
		}
	
	public void selectCentralServices(String productFamily1,String productVariant1, String productOffering1,String counterValue) throws BiffException, IOException, InterruptedException{
		driver = Common.getDriver();
		rsqewait.waitForVisibleValue("productCounter", counterValue);
		boolean status = false;
		addProductCentralService(productFamily1,productVariant1,productOffering1);
		WebElement customerAgreed = rsqewait.waitForVisibleElement("CustomerAgreed", 2);
		customerAgreed.click();
		//driver.findElement(map.getLocator("CustomerAgreed")).click();
		//Thread.sleep(2000);   
		WebElement AddProduct = rsqewait.waitForVisibleElement("SubmitProduct", 2);
      //  WebElement AddProduct = driver.findElement(map.getLocator("SubmitProduct"));
        status = AddProduct.isEnabled();
        AddProduct.click();       
        Assert.assertEquals(status, true);	                      
        common.screenshot();      					
	}
	
	
     
	 
    public void addProductCentralService(String productFamily1,String productVariant1, String productOffering1) throws BiffException, IOException,
	InterruptedException {
try {		
	driver = Common.getDriver();
	System.out.println("--->" + productFamily1);
	WebElement Family = rsqewait.waitForVisibleElement("ProductFamily",5);  
	Select prodFamily = new Select(Family);	
	//Select productFamily = new Select(driver.findElement(map.getLocator("ProductFamily")));
	prodFamily.selectByVisibleText(productFamily1);

	Select productVariant = new Select(driver.findElement(map.getLocator("ProductVariant")));
	productVariant.selectByVisibleText(productVariant1);

	Select productOffering = new Select(driver.findElement(map.getLocator("ProductOffering")));
	productOffering.selectByVisibleText(productOffering1);

	} 
	catch (Exception e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
	}
    }
    
    
    public void clickConfigProduct(String counterValue){
		driver = Common.getDriver();
		rsqewait.waitForVisibleValue("productCounter", counterValue);
		WebElement ConfigureProd =  driver.findElement(map.getLocator("ConfigureProduct"));		
		ConfigureProd.click();		
	}
	public void clickAddProduct() throws IOException{
		boolean status = false;
		driver = Common.getDriver();
		WebElement AddProduct = rsqewait.waitForVisibleElement("SubmitProduct", 10);			
	    // WebElement AddProduct = driver.findElement(map.getLocator("SubmitProduct"));
	    common.screenshot();
	    status = AddProduct.isEnabled();
	    AddProduct.click();       
	    Assert.assertEquals(status, true);	  				
	}
	public void clickConfigProduct() throws InterruptedException{
		driver = Common.getDriver();
		Thread.sleep(10000);
		 driver.manage().timeouts().pageLoadTimeout(100, TimeUnit.SECONDS);		
		 WebDriverWait wait = new WebDriverWait(driver,20); 	 
		 WebElement ConfigureProd = wait.until(ExpectedConditions.elementToBeClickable(map.getLocator("ConfigureProduct"))); 
		//WebElement ConfigureProd = common.getVisible("ConfigureProduct", 20);
		//WebElement ConfigureProd =  driver.findElement(map.getLocator("ConfigureProduct"));
		 ConfigureProd.click();		
	}
    
	public void selectLicencePack(String productOffer) throws BiffException, IOException, InterruptedException{
		driver = Common.getDriver();		
		Thread.sleep(5000);	
		Thread.sleep(5000);	
		Select productOffering = new Select(driver.findElement(map.getLocator("ProductOffering")));
		productOffering.selectByVisibleText(productOffer);
		Thread.sleep(4000);		
        driver.findElement(map.getLocator("SubmitProduct")).click();
        Thread.sleep(4000);
        Thread.sleep(4000);
        Thread.sleep(4000);
       	common.screenshot();    		
		Thread.sleep(5000);
		Thread.sleep(5000);		
	}
	
	public void clickContinueToQuoteDetails() throws InterruptedException{
		driver = Common.getDriver();
		int size =0;
		Wait <WebDriver> wait1 = new FluentWait<WebDriver>(driver).withTimeout(2, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		driver.manage().timeouts().pageLoadTimeout(100, TimeUnit.SECONDS);						
		WebElement continueToQuoteDetail =  driver.findElement(map.getLocator("ContinueToQuoteDetails"));
		
		 boolean testElement = wait1.until(new Function <WebDriver,Boolean>() { 
			    public Boolean apply(WebDriver driver) { 
			     WebElement elem= driver.findElement(map.getLocator("productCounter"));
			     return elem.getText().equals("1") ;
			     }
			});
			
	    Assert.assertTrue(testElement);
	    WebElement productCount = driver.findElement(map.getLocator("productCounter"));
		size = Integer.parseInt( productCount.getText());
		if (size>0){		
		if(continueToQuoteDetail.isEnabled()){
		continueToQuoteDetail.click();
		}
		}
	}
	

}
