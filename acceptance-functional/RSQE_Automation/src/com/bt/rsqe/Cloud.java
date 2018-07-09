package com.bt.rsqe;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jxl.read.biff.BiffException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.testng.Assert;

import com.google.common.base.Function;

public class Cloud {
	ObjectMap map;
	String QuoteName = "";
	String result="";
	Common common;
	RSQEWait rsqewait;
	AddProduct addprod;
	private static WebDriver driver;

	public Cloud(WebDriver driver){
		Cloud.driver = driver;
		
	}
	public Cloud() {		
		map = new ObjectMap();
		common= new Common();
		rsqewait = new RSQEWait();
		addprod = new AddProduct();
	}
	
	public void selectAddProduct(String productFamily1,String productVariant1, String productOffering1) throws BiffException, IOException, InterruptedException{		
		driver = Common.getDriver();
		driver.manage().timeouts().pageLoadTimeout(100, TimeUnit.SECONDS);	
		addprod.addProductCentralService(productFamily1,productVariant1,productOffering1);		
		addprod.clickAddProduct();
        driver.manage().timeouts().pageLoadTimeout(100, TimeUnit.SECONDS);	        
        common.screenshot();      				
	}
	 public void selectQuoteDetail() throws InterruptedException{    
	     driver = Common.getDriver();
    	driver.switchTo().defaultContent();
    	WebElement quoteDetaiLink = rsqewait.waitForVisibleElement("QuoteDetails", 8);
    	quoteDetaiLink.click();		
    }
   
   public void selectLineItem(){	
	  driver = Common.getDriver(); 
   Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(2, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		wait.until(new Function <WebDriver,Boolean>() { 
			   public Boolean apply(WebDriver driver) { 
				   WebElement elem1 = driver.findElement(By.xpath("/html/body/div[1]/div[4]/div[2]/div[1]/div/div[3]/div[4]/div[3]/div[2]/table/tbody/tr[1]/td[12]"));
				   return elem1.getText().equalsIgnoreCase("VALID");
			   }
		});
		 WebElement selectAll= rsqewait.waitForVisibleElement("selectAll",2) ;
	     selectAll.click();			
		wait.until(new Function <WebDriver,Boolean>() { 
			   public Boolean apply(WebDriver driver) { 
				   WebElement elem1 = driver.findElement(map.getLocator("calPrice"));
				   return elem1.isEnabled();
			   }
		});
		
		WebElement calculatePrice = driver.findElement(map.getLocator("calPrice"));
		calculatePrice.click();
   }
   

 public void calculatePrice(){
	 Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(2, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
	
	 wait.until(new Function <WebDriver,Boolean>() { 
		   public Boolean apply(WebDriver driver) { 
			   WebElement elem1 = driver.findElement(map.getLocator("calPrice"));
			   return elem1.isEnabled();
		   }
	});
	
	WebElement calculatePrice = driver.findElement(map.getLocator("calPrice"));
	calculatePrice.click();
	 boolean priceValue = wait.until(new Function <WebDriver,Boolean>() { 
		   public Boolean apply(WebDriver driver) { 
			   WebElement elem1 = driver.findElement(By.xpath("/html/body/div[1]/div[4]/div[2]/div[1]/div/div[3]/div[4]/div[3]/div[2]/table/tbody/tr[1]/td[10]"));
			   return elem1.getText().equalsIgnoreCase("N/A")||elem1.getText().equalsIgnoreCase("Firm");
		   }
		   
	});
	 Assert.assertTrue(priceValue);
 }
 
	public void selectHeaderConfig(){
		driver = Common.getDriver();
		driver.findElement(By.xpath("//*[@id='configurator-container']/div[5]/div[3]/div[1]/div[4]/table/thead/tr/th[5]/div/div/a")).click();
	}
}
