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

public class QuoteOption {
	
	ObjectMap map;
	String QuoteName = "";
	String result="";
	Common common;
	RSQEWait rsqewait;
	
	private static WebDriver driver;
	
	public QuoteOption(WebDriver driver){
		QuoteOption.driver = driver;
		
	}

	public QuoteOption() {
		map = new ObjectMap();
		common= new Common();
		rsqewait = new RSQEWait();
	}
	
	public void createQuote(String contractTerm, String currency) {
		try {
			driver = Common.getDriver();
			boolean status = false;
			Date date = new Date();
			System.out.println("Creating New Quote Option");
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);	
			
			WebElement table1 = rsqewait.waitForVisibleElement("QuoteOptionTable",1);		
			
			WebElement createQuote = rsqewait.waitForVisibleElement("createQuoteOption", 1);
			createQuote.click();			
			//driver.findElement(map.getLocator("createQuoteOption")).click();			
			//Thread.sleep(5000);
			//WebElement Name = driver.findElement(map.getLocator("QuoteName"));
			WebElement Name = rsqewait.waitForVisibleElement("QuoteName", 1);
			Name.click();		
			Assert.assertEquals(Name.isDisplayed(), true,"the element is not displayed");
			Name.sendKeys("Test" + date.getTime());
			QuoteName = Name.getAttribute("value");
			Thread.sleep(1000);
			System.out.println("------>" + QuoteName);
			Select ContractTerm = new Select(driver.findElement(map.getLocator("QuoteContractTerm")));
			ContractTerm.selectByValue(contractTerm);
			Select Currency = new Select(driver.findElement(map.getLocator("QuoteCurrency")));
			Currency.selectByValue(currency);
			WebElement quoteSave = driver.findElement(map.getLocator("QuoteSave"));
			quoteSave.click();
			System.out.println("done the createQuoteOption");			
			common.screenshot();			
			WebElement table = rsqewait.waitForVisibleElement("QuoteOptionTable", 1);
			//WebElement table = driver.findElement(map.getLocator("QuoteOptionTable"));
			List<WebElement> allRows = table.findElements(By.tagName("tr"));
			outerloop: for (WebElement row : allRows) {
				List<WebElement> cells = row.findElements(By.tagName("td"));
					for (WebElement cell : cells) {
						System.out.println(cell.getText());
							if (cell.getText().equalsIgnoreCase(QuoteName)){
								status = true;
								break outerloop;
								}
						}
						}
			Assert.assertEquals(status, true);				
		}
		catch (Exception e) {
		 System.out.println("The Exception in the createQuote is "+e);
			// TODO: handle exception
		}
	
	}

	public void getQuoteName(String qName)throws InterruptedException, IOException {
		boolean status = false;
		if (qName != "") {
			QuoteName = qName;
		}
		driver = Common.getDriver();
		System.out.println("the name of the quote" + QuoteName);		
		//Thread.sleep(10000);
		//WebElement table = driver.findElement(map.getLocator("QuoteOptionTable"));
		WebElement table = rsqewait.waitForVisibleElement("QuoteOptionTable", 2);
		List<WebElement> allRows = table.findElements(By.tagName("tr"));
		outerloop: for (WebElement row : allRows) {
			List<WebElement> cells = row.findElements(By.tagName("td"));
			for (WebElement cell : cells) {
				System.out.println(cell.getText());
				if (cell.getText().equalsIgnoreCase(QuoteName))
				{				
					status = true;
					cell.click();
					common.screenshot();					
					break outerloop;
				}
			}
		}
		
		Assert.assertEquals(status, true);	
	}
}

