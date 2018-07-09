package com.bt.rsqe;
import org.openqa.selenium.WebDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
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

public class Bulkconfiguration {
	ObjectMap map;
	String QuoteName = "";
	String result="";
	Common common;
	private static WebDriver driver;
	
	public Bulkconfiguration(WebDriver driver){
		Bulkconfiguration.driver = driver;
		
	}
	 
	public Bulkconfiguration() {
		// TODO Auto-generated constructor stub
		map = new ObjectMap();
		common = new Common();
	}
	
	public void IPEAccordian(String ipeacc) throws InterruptedException {
		driver = Common.getDriver();
		//WebElement accord = driver.findElement(By.xpath("//li[@class='ng-scope level1 validity-pending']"));
		List<WebElement> accord = driver.findElements(By.xpath("//li[@class='ng-scope level1 validity-pending']/div/div/div[@class='ng-isolate-scope ng-scope']/span"));
		//List<WebElement> allRows = accord.findElements(By.tagName("div"));
		System.out.println("---------->"+ipeacc+"<--------------");
		for (WebElement el1 : accord) {			
			System.out.println(el1.getText());
			if (el1.getText().equalsIgnoreCase(ipeacc)) {
				el1.click();
				break;
			} 			
		}

	}
	
	public void accordian(String accValue) throws InterruptedException {
	
		//WebElement accord = driver.findElement(By.xpath("//a[@class='accordion-toggle ng-binding']"));
		driver = Common.getDriver();
		WebElement accord = driver.findElement(By.className("accordion"));
		List<WebElement> allRows = accord.findElements(By.tagName("div")); 
		outerloop:for(WebElement rowElement:allRows){
		List<WebElement> allRows1 = rowElement.findElements(By.tagName("span")); 
		for (WebElement element : allRows1) {
			System.out.println("********************************"); 
			System.out.println(element.getText());
			System.out.println("********************************");
			if (element.getText().equalsIgnoreCase(accValue)) {
				element.click();
				break outerloop;
			} 
		}
		}
	}
	
	public void accordian1(String accValue) throws InterruptedException {
		//WebElement accord = driver.findElement(By.xpath("//a[@class='accordion-toggle ng-binding']"));
		driver = Common.getDriver();
		WebElement accord = driver.findElement(By.className("accordion"));
		List<WebElement> allRows = accord.findElements(By.tagName("div")); 
		outerloop:for(WebElement el1:allRows){
		List<WebElement> allRows1 = el1.findElements(By.tagName("span")); 
		for (WebElement el2 : allRows1) {
			System.out.println("********************************"); 
			System.out.println(el2.getText());
			System.out.println("********************************");
			if (el2.getAttribute("title").equalsIgnoreCase(accValue)){
				el2.click();
				break outerloop;
			} 
		}
		}
	}
		
	public void selectConfigure(String pro) throws InterruptedException{
		driver = Common.getDriver();
		System.out.println("the value of the product is"+pro);
		WebElement table = driver.findElement(By.xpath("/html/body/div[1]/div[3]/div[1]/div[5]/div[3]/div[1]/div[4]/table"));
		WebElement tbody = table.findElement(By.tagName("tbody"));
		List<WebElement> tRow = tbody.findElements(By.tagName("tr"));
		for(WebElement row :tRow){
		List<WebElement> tCol = row.findElements(By.tagName("td"));			
			for(WebElement col:tCol){
				System.out.println("Label of the button is:- "+ col.getAttribute("field"));
				if(col.getAttribute("field")!=null){
					String field = col.getAttribute("field").toString();
					System.out.println("*********"+field);
					if(field.equalsIgnoreCase(pro)){
					   System.out.println("*********");
					   col.findElement(By.tagName("a")).click();
					}
				}				
				//List<WebElement> tDiv = col.findElements(By.tagName("div"));						
			}
		}
		
	}
	
	public void selectCheckBox() throws InterruptedException{
		driver = Common.getDriver();
		WebElement table = driver.findElement(By.xpath("/html/body/div[1]/div[3]/div[1]/div[5]/div[3]/div[1]/div[4]/table"));
		WebElement tbody = table.findElement(By.tagName("tbody"));
		List<WebElement> tRow = tbody.findElements(By.tagName("tr"));
		for(WebElement row :tRow){
		List<WebElement> tCol = row.findElements(By.tagName("td"));			
			for(WebElement col:tCol){
				System.out.println("Label of the button is:- "+ col.getAttribute("field"));
				if(col.getAttribute("field")!=null){
					String field = col.getAttribute("field").toString();
					System.out.println("*********"+field);
					if(field.equalsIgnoreCase("checkbox")){
					   System.out.println("*********");
					   col.findElement(By.tagName("input")).click();
					}
				}				
				//List<WebElement> tDiv = col.findElements(By.tagName("div"));						
			}
		}
		
	}
	
	public void selectPopup(String strvalue) throws InterruptedException{
		driver = Common.getDriver();
		Select IPEngineConfiguure = new Select(driver.findElement(map.getLocator("ConfigurePopup")));
		System.out.println("---->" +strvalue);
		Thread.sleep(40000);
		driver.findElement(map.getLocator("chosenrelctnr")).click();
		IPEngineConfiguure.selectByVisibleText(strvalue);
		Thread.sleep(20000);
		WebElement add = driver.findElement(By.cssSelector("button[class='btn-mini action-add']"));
		add.click();
		driver.switchTo().defaultContent();
		//Thread.sleep(20000);	
		//driver.findElement(map.getLocator("OkButton")).click();
		WebElement element = driver.findElement(map.getLocator("OkButton"));
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		executor.executeScript("arguments[0].click();", element);
		Thread.sleep(5000);
		Thread.sleep(5000);
		Thread.sleep(5000);
	}
	
	public void selectPopup2(String strvalue) throws InterruptedException{
		driver = Common.getDriver();
		Thread.sleep(10000);
		Select IPEngineConfiguure = new Select(driver.findElement(map.getLocator("popup2")));
		System.out.println("---->" +strvalue);
		Thread.sleep(5000);
		Thread.sleep(5000);
		Thread.sleep(5000);
		IPEngineConfiguure.selectByVisibleText(strvalue);
		driver.findElement(map.getLocator("chosenrelctnr")).click();
		Thread.sleep(20000);
		WebElement add = driver.findElement(By.cssSelector("button[class='btn-mini action-add']"));
		add.click();
		driver.switchTo().defaultContent();
		Thread.sleep(20000);
		//driver.findElement(map.getLocator("OkButton")).click();
		WebElement element = driver.findElement(map.getLocator("OkButton"));
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		executor.executeScript("arguments[0].click();", element);
		
	}
	
	public void selectPopup3(String strvalue) throws InterruptedException{
		driver = Common.getDriver();
		Thread.sleep(10000);
		Select IPEngineConfiguure = new Select(driver.findElement(map.getLocator("popup3")));
		System.out.println("---->" +strvalue);
		Thread.sleep(5000);
		Thread.sleep(5000);
		Thread.sleep(5000);
		IPEngineConfiguure.selectByVisibleText(strvalue);
		driver.findElement(map.getLocator("chosenrelctnr")).click();
		Thread.sleep(20000);
		WebElement add = driver.findElement(By.cssSelector("button[class='btn-mini action-add']"));
		add.click();
		driver.switchTo().defaultContent();
		Thread.sleep(20000);
		//driver.findElement(map.getLocator("OkButton")).click();
		WebElement element = driver.findElement(map.getLocator("OkButton"));
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		executor.executeScript("arguments[0].click();", element);
		
	}
	
	public void clickOk(){
		driver = Common.getDriver();
		driver.findElement(map.getLocator("OkButton")).click();
	}
	
	public void selectDropDown(String pro,String strval) throws InterruptedException{
		driver = Common.getDriver();
		Thread.sleep(10000);
		WebElement table = driver.findElement(By.xpath("/html/body/div[1]/div[3]/div[1]/div[5]/div[3]/div[1]/div[4]/table"));
		WebElement tbody = table.findElement(By.tagName("tbody"));
		List<WebElement> tRow = tbody.findElements(By.tagName("tr"));
		for(WebElement row :tRow){
		List<WebElement> tCol = row.findElements(By.tagName("td"));			
			for(WebElement col:tCol){
				System.out.println("Label of the button is:- "+ col.getAttribute("field"));
				if(col.getAttribute("field")!=null){
					String field = col.getAttribute("field").toString();
					System.out.println("*********"+field);
					if(field.equalsIgnoreCase(pro)){
					   System.out.println("*********");
					   Select dropdown = new Select(col.findElement(By.tagName("select")));
					   dropdown.selectByVisibleText(strval);
					}
				}									
			}
		}
	}
		
		public void selectInput(String product,String strvalue) throws InterruptedException{
			driver = Common.getDriver();
			Thread.sleep(10000);
			System.out.println("inside the select Input");
			System.out.println("the given product is "+product);
			Thread.sleep(5000);
			WebElement table = driver.findElement(By.xpath("/html/body/div[1]/div[3]/div[1]/div[5]/div[3]/div[1]/div[4]/table"));
			WebElement tbody = table.findElement(By.tagName("tbody"));
			List<WebElement> tRow = tbody.findElements(By.tagName("tr"));
			for(WebElement row :tRow){
			List<WebElement> tCol = row.findElements(By.tagName("td"));			
				for(WebElement col:tCol){
					System.out.println("Label of the button is:- "+ col.getAttribute("field"));
					if(col.getAttribute("field")!=null){
						String field = col.getAttribute("field").toString();
						System.out.println("*********"+field);
						if(field.equalsIgnoreCase(product)){
						   System.out.println("*********");
							List<WebElement> tdiv = col.findElements(By.tagName("div"));	
							int i=0;
							for(WebElement divi :tdiv){
								if(i==2){
									break;
								}
								else{
								divi.click();
								System.out.println(i);
								i++;
								}
								
							}
						  WebElement quantity = col.findElement(By.tagName("input"));
						  quantity.sendKeys(strvalue);
						   
						}
					}									
				}
			}		
	}
		
		public void selectDropDown1(String pro,String strval) throws InterruptedException{
			driver = Common.getDriver();
			System.out.println("inside the select Drop down");
			WebElement table = driver.findElement(By.xpath("/html/body/div[1]/div[3]/div[1]/div[5]/div[3]/div[1]/div[4]/table"));
			WebElement tbody = table.findElement(By.tagName("tbody"));
			List<WebElement> tRow = tbody.findElements(By.tagName("tr"));
			for(WebElement row :tRow){
			List<WebElement> tCol = row.findElements(By.tagName("td"));			
				for(WebElement col:tCol){
					System.out.println("Label of the button is:- "+ col.getAttribute("field"));
					if(col.getAttribute("field")!=null){
						String field = col.getAttribute("field").toString();
						System.out.println("*********"+field);
						if(field.equalsIgnoreCase(pro)){
						   System.out.println("*********");
						   List<WebElement> tdiv = col.findElements(By.tagName("div"));	
							int i=0;
							for(WebElement divi :tdiv){
								if(i==2){
									break;
								}
								else{
								divi.click();
								System.out.println(i);
								i++;
								}
								
							}
						   Select dropdown = new Select(col.findElement(By.tagName("select")));
						   dropdown.selectByVisibleText(strval);
						}
					}									
				}
			}
		}	
		
	
		public void selectServiceCentralDetail() throws InterruptedException{
			driver = Common.getDriver();
			Thread.sleep(10000);
			Thread.sleep(5000);
			Thread.sleep(5000);
			driver.findElement(map.getLocator("ok")).click();
		}	
		
		public void selectSteelHeadMangLicPack(){
			driver = Common.getDriver();
			driver.findElement(By.xpath(".//*[@id='configurator-container']/div[5]/div[2]/div/accordion/div/div[4]/div[1]/a/span")).click();
		}
		
		public void selectLicBaseconfig(){
			driver = Common.getDriver();
			driver.findElement(By.xpath(".//*[@id='configurator-container']/div[5]/div[2]/div/accordion/div/div[4]/div[2]/div/div/div/ul/li[1]/div/div/span")).click();
		}
		public void selectLiConsumer(){
			driver = Common.getDriver();
			driver.findElement(By.xpath(".//*[@id='configurator-container']/div[5]/div[2]/div/accordion/div/div[4]/div[2]/div/div/div/ul/li[2]/div/div/span")).click();
		}
}
