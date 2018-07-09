package com.bt.rsqe;
import org.openqa.selenium.WebDriver;
import org.apache.commons.io.FileUtils;
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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.By;
import org.testng.Assert;
import com.bt.rsqe.Common;
import com.bt.rsqe.ObjectMap;

public class QuoteDetail {
	ObjectMap map;
	String QuoteName = "";
	String result="";
	Common common;
	RSQEWait rsqewait;
	private static WebDriver driver;
	
	public QuoteDetail(WebDriver driver){
		QuoteDetail.driver = driver;
		
	}

	public QuoteDetail() {
		map = new ObjectMap();
		common= new Common();
		rsqewait = new RSQEWait();
	}
	
	
    public void selectAddProduct() throws InterruptedException{
    	driver = Common.getDriver();
    	boolean status = false;
    	//Thread.sleep(4000);
    	WebElement quoteDetailAddProduct = rsqewait.waitForVisibleElement("QuoteDetailAddproduct", 2);
	  //  WebElement quoteDetailAddProduct = driver.findElement(map.getLocator("QuoteDetailAddproduct"));
	    status =quoteDetailAddProduct.isEnabled();
	    if (status == true){
	    	quoteDetailAddProduct.click();
	    }
	    Assert.assertEquals(status, true);	
		Thread.sleep(5000);
    }
    
    
    public void selectQuoteDetail() throws InterruptedException{    	
    	driver = Common.getDriver();
    	driver.switchTo().defaultContent();
		driver.findElement(map.getLocator("QuoteDetails")).click();		
    }
    
	public void selectlineItems() throws InterruptedException{
		System.out.println("inside the selectLine items");
		driver = Common.getDriver();
		Thread.sleep(20000);
		//WebElement table = driver.findElement(By.id("lineItems"));
	//	List<WebElement> allRows = table.findElements(By.tagName("tr"));
		//for (int i=1;i<allRows.size();i++) {
			//System.out.println("total rows in the lineItems"+allRows.size());
		//	String pricingStatus = driver.findElement(By.xpath("/html/body/div[1]/div[4]/div[1]/div[1]/div/div[3]/div[4]/div[3]/div[2]/table/tbody/tr["+i+"]/td[10]")).getText();
		//	System.out.println("The values are"+pricingStatus);
				//if(pricingStatus.equalsIgnoreCase("Firm")|| pricingStatus.equalsIgnoreCase("N/A")){
					// driver.findElement(By.xpath("/html/body/div[1]/div[4]/div[1]/div[1]/div/div[3]/div[4]/div[3]/div[2]/table/tbody/tr["+i+"]/td[1]/input")).click();
					 //break;
					 
				//}
			//		}		
		driver.findElement(map.getLocator("selectAll")).click();
	}
	
	public void selectCalcPrice() throws InterruptedException, IOException{
		System.out.println("inside the caluculate price");
		driver = Common.getDriver();		
		driver.findElement(map.getLocator("calPrice")).click();								
		common.screenshot(); 
	}
	
	public void refresh(){
		driver = Common.getDriver();
		driver.navigate().refresh();
	}

	
	 public void selectCreateOffer(String offName,String orderRef) throws InterruptedException, IOException {
		 System.out.println("inside the select CreateOffer");
		    boolean status = false;
		    driver = Common.getDriver();
			Thread.sleep(30000);
			Thread.sleep(30000);
			Thread.sleep(30000);
			Thread.sleep(30000);
			//Thread.sleep(35000);	 		
			WebElement createOffer = driver.findElement(map.getLocator("offer"));
			status = createOffer.isEnabled();
			Assert.assertEquals(status, true,"The create offer button is not enabled");	
			driver.findElement(map.getLocator("offer")).click();
			WebElement OfferName = driver.findElement(map.getLocator("offerName"));
			OfferName.click();
			OfferName.sendKeys(offName); 
			WebElement CustOrderRef = driver.findElement(map.getLocator("cusOrderRef"));
			CustOrderRef.click();
			CustOrderRef.sendKeys(orderRef);
			common.screenshot();	
			driver.findElement(map.getLocator("save")).click();
			Thread.sleep(10000);	
			driver = Common.getDriver();
			// selecting the value from the offer table
			//driver.findElement(By.xpath("//table[@id='offers']/tbody/tr/td[1]")).click();
			//--------//New changes for R38
			Thread.sleep(20000);
			driver.findElement(By.id("QuoteOptionOffersTab")).click();
			Thread.sleep(5000);
			driver.findElement(By.xpath("//img[@alt ='Customer Approve']")).click();
			Thread.sleep(15000);
			selectOffer(offName);		
			Thread.sleep(8000);	
		Thread.sleep(8000);
		Thread.sleep(8000);
			// selcting the checkbox from the offerDetails
			driver.findElement(By.xpath("/html/body/div[1]/div[4]/div[2]/div[1]/div/div[2]/div[3]/div[3]/div[2]/table/tbody/tr[1]/td[1]/input")).click();
			Thread.sleep(8000);			
	        }
			
	 
	 public void selectOffer(String strOffer){
		 
		// WebElement table1=  rsqewait.waitForVisibleElement("offerTable", 3);		
		    driver = Common.getDriver();		  		    
			WebElement table1 = driver.findElement(By.id("offers"));
			List<WebElement> allRows = table1.findElements(By.tagName("tr"));
			outerloop: for (WebElement row : allRows) {
				List<WebElement> cells = row.findElements(By.tagName("td"));
				for (WebElement cell : cells) {
					System.out.println(cell.getText());
					if (cell.getText().equalsIgnoreCase(strOffer))
						
					{
						cell.click();
						break outerloop;
					}
				}				
			}    
		}
	  
	 public void selectCreateOrder() throws InterruptedException, IOException{		
		 System.out.println("inside the select selectCreateOrder");
		 driver = Common.getDriver();
		//driver.findElement(map.getLocator("customerApprove")).click();
		// Creating the order
		// slecting the order
		Thread.sleep(8000);
		Thread.sleep(8000);
		common.screenshot();	
		//driver.findElement(By.xpath("/html/body/div[1]/div[4]/div/div[1]/div/div[2]/div[3]/div[3]/div[2]/table/tbody/tr/td[1]/input")).click();
		//Thread.sleep(8000);	
		//Thread.sleep(8000);
		driver.findElement(map.getLocator("createOrder")).click();
		common.screenshot();		
	}
	
	 public void selectSubmitOrder(String orderName) throws InterruptedException, IOException{
		    System.out.println("inside the select selectSubmitOrder");
			driver = Common.getDriver();
			Thread.sleep(8000);
			Thread.sleep(8000);
			WebElement ordName = driver.findElement(map.getLocator("orderName"));
			ordName.click();
			common.screenshot();	
			ordName.sendKeys(orderName);
			WebElement SubmitOrd = driver.findElement(map.getLocator("submitOrder"));
			SubmitOrd.click();	
			common.screenshot();	
			//driver.findElement(map.getLocator("submitOrder")).click();		
		}
	
	
	
	

}
