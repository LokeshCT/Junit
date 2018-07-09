package com.bt.rsqe;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.testng.Assert;
import com.google.common.base.Function;
import com.bt.rsqe.Common;
import com.bt.rsqe.ObjectMap;


public class RSQEWait {	
	ObjectMap map;
	Common common;	
	private static WebDriver driver;
	
	public RSQEWait() {
		map = new ObjectMap();
		common= new Common();		
	}

	public RSQEWait(WebDriver driver){
		RSQEWait.driver = driver;
	}
	
	
	public void waitToLoad(String fieldvalue){
		driver = Common.getDriver();
		//WebTextWait wait22 = new WebTextWait(fieldvalue, 5);
		//Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(15, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		Wait <String> wait = new FluentWait<String>(fieldvalue).withTimeout(5, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		 boolean testElement = wait.until(new Function <String,Boolean>() { 
			     public Boolean apply(String fieldvalue ) {
			     WebElement elem= driver.findElement(By.xpath("//div[@id='configurator-container']/div[5]/div[3]/div[1]/div[4]/table/tbody/tr[1]/td[6]"));		                                                  
			      return elem.getAttribute("field").equalsIgnoreCase(fieldvalue);
			     }
			});	
		 Assert.assertTrue(testElement);
	}
	
	public void waitToLoad1(String fieldvalue){
		driver = Common.getDriver();
		//WebTextWait wait22 = new WebTextWait(fieldvalue, 5);
		//Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(15, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		Wait <String> wait = new FluentWait<String>(fieldvalue).withTimeout(5, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		 boolean testElement = wait.until(new Function <String,Boolean>() { 
			     public Boolean apply(String fieldvalue ) {
			     WebElement elem= driver.findElement(By.xpath("//div[@id='configurator-container']/div[5]/div[3]/div[1]/div[4]/table/tbody/tr[1]/td[5]"));		                                                  
			      return elem.getAttribute("field").equalsIgnoreCase(fieldvalue);
			     }
			});	
		 Assert.assertTrue(testElement);
	}
	
	public void waitToLoadCO(String fieldvalue){
		driver = Common.getDriver();
		//WebTextWait wait22 = new WebTextWait(fieldvalue, 5);
		//Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(15, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		Wait <String> wait = new FluentWait<String>(fieldvalue).withTimeout(5, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		 boolean testElement = wait.until(new Function <String,Boolean>() { 
			     public Boolean apply(String fieldvalue ) {
			     WebElement elem= driver.findElement(By.xpath("//div[@id='configurator-container']/div[5]/div[3]/div[1]/div[4]/table/tbody/tr[1]/td[7]"));		                                                  
			      return elem.getAttribute("field").equalsIgnoreCase(fieldvalue);
			     }
			});	
		 Assert.assertTrue(testElement);
	}
	
	public void waitForPriceEnable(){
		driver = Common.getDriver();
		Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(5, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		 boolean testElement = wait.until(new Function <WebDriver,Boolean>() { 
			    public Boolean apply(WebDriver driver) { 			         
			         WebElement elem= driver.findElement(map.getLocator("price"));
			         return  elem.isEnabled();
			     }			    
			});	
		 Assert.assertTrue(testElement);
	}	
	
	public void waitForRestoreisEnabled(){
		driver = Common.getDriver();
		Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(5, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		 boolean testElement = wait.until(new Function <WebDriver,Boolean>() { 
			    public Boolean apply(WebDriver driver) { 			         
			         WebElement elem= driver.findElement(map.getLocator("restore"));
			         return  elem.isEnabled();
			     }			    
			});	
		 Assert.assertTrue(testElement);
	}
	
	public WebElement waitForVisibleElement(final String elementName ,int timeout){
		driver = Common.getDriver();
		Wait <WebDriver> wait1 = new FluentWait<WebDriver>(driver).withTimeout(timeout, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		WebElement  element = wait1.until(new Function <WebDriver, WebElement>() { 
			    public WebElement apply(WebDriver driver) { 
			    return driver.findElement(map.getLocator(elementName)); 
			  }
			});
		
		return element;				
	}
	
	public void waitForConfiguredStatus(){
		 driver = Common.getDriver();
		 Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(5, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		 wait.until(new Function <WebDriver,Boolean>() { 
				   	public Boolean apply(WebDriver driver) { 
				   		WebElement elem1 = driver.findElement(By.xpath("/html/body/div[1]/div[4]/div[2]/div[1]/div/div[3]/div[4]/div[3]/div[2]/table/tbody/tr[1]/td[12]"));
				   		return elem1.getText().equalsIgnoreCase("VALID");
				   }
			});
	}
	
	public void waitForQuoteDetailPriceEnable(){
		driver = Common.getDriver();
		 Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(5, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		 wait.until(new Function <WebDriver,Boolean>() { 
			   public Boolean apply(WebDriver driver) { 
				   WebElement elem1 = driver.findElement(map.getLocator("calPrice"));
				   return elem1.isEnabled();
			   }
		});
	}

	public void waitForPricingStatus(){
	   driver = Common.getDriver();
		 Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(5, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);		
		 boolean priceValue = wait.until(new Function <WebDriver,Boolean>() { 
			   public Boolean apply(WebDriver driver) { 
				   WebElement elem1 = driver.findElement(By.xpath("/html/body/div[1]/div[4]/div[2]/div[1]/div/div[3]/div[4]/div[3]/div[2]/table/tbody/tr[1]/td[10]"));
				   return elem1.getText().equalsIgnoreCase("N/A")||elem1.getText().equalsIgnoreCase("Firm");
			   }
			   
		});
		 Assert.assertTrue(priceValue);
		
	}
	
	public void waitForVisibleValue(final String elementName,final String expectedValue){
		driver = Common.getDriver();
		 Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(5, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);			
		 boolean testElement = wait.until(new Function <WebDriver,Boolean>() { 
			    public Boolean apply(WebDriver driver) { 
			     WebElement elem= driver.findElement(map.getLocator(elementName));
			     return elem.getText().equals(expectedValue) ;
			     }
			});
			
	    Assert.assertTrue(testElement);
	}
	
	public void waitForSiteTable(final String expectedValue){
		driver = Common.getDriver();
		 Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(5, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);			
		 boolean testElement = wait.until(new Function <WebDriver,Boolean>() { 
			    public Boolean apply(WebDriver driver) { 
			     WebElement elem= driver.findElement(By.xpath("/html/body/div[1]/div[4]/div[2]/div/div[3]/div[2]/div[3]/div[2]/table/tbody/tr[1]/td[2]"));
			     return elem.getText().equals(expectedValue) ;
			     }
			});
			
	    Assert.assertTrue(testElement);
		
	}
	
	public void waitForCloudFirewallServices(){
		driver = Common.getDriver();
		Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(5, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		 boolean testElement = wait.until(new Function <WebDriver,Boolean>() { 
			    public Boolean apply(WebDriver driver) { 
			     WebElement elem= driver.findElement(By.xpath("//div[@id='configurator-container']/div[5]/div[3]/div[1]/div[4]/table/tbody/tr[1]/td[6]"));			                                                  
			     return elem.getAttribute("field").equalsIgnoreCase("FirewallConfig") ;
			     }			    
			});	
		 Assert.assertTrue(testElement);
	}
			
	
	
	public void waitForCloudServicesLeg(){
		driver = Common.getDriver();
		Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(5, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		 boolean testElement = wait.until(new Function <WebDriver,Boolean>() { 
			    public Boolean apply(WebDriver driver) { 
			     WebElement elem= driver.findElement(By.xpath("//div[@id='configurator-container']/div[5]/div[3]/div[1]/div[4]/table/tbody/tr[1]/td[6]"));			                                                  
			     return elem.getAttribute("field").equalsIgnoreCase("Primary") ;
			     }			    
			});	
		 Assert.assertTrue(testElement);
	}
	
	public void waitForFirewallServices(){
		driver = Common.getDriver();
		Wait <WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(5, TimeUnit.MINUTES).pollingEvery(1,TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		 boolean testElement = wait.until(new Function <WebDriver,Boolean>() { 
			    public Boolean apply(WebDriver driver) { 
			     WebElement elem= driver.findElement(By.xpath("//div[@id='configurator-container']/div[5]/div[3]/div[1]/div[4]/table/tbody/tr[1]/td[5]"));			                                                  
			     return elem.getAttribute("field").equalsIgnoreCase("FirewallService") ;
			     }			    
			});	
		 Assert.assertTrue(testElement);
	}
}
