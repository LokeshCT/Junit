package com.bt.rsqe;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
//import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class Common {
	private static WebDriver driver;
	Bulkconfiguration bulkconfig;
	ObjectMap map;
	int i;
	public Common(){		
	
		map = new ObjectMap();
		 i =0;
	}
	
	
	public void screenshot() throws IOException{
		 i++;
		//String screen = screenshotName;
	     driver = Common.getDriver(); 
		 File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		 //FileUtils.copyFile(scrFile, new File("C:\\Users\\606522807\\workspace\\RSQE\\ScreenShots\\yy.png"));
		 FileUtils.copyFile(scrFile, new File("C:\\Users\\609023844\\Workspace1\\RSQE_Automation\\ScreenShots\\"+i+".png"));		 
	}
	
	
	public String dateTime(){
		   DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		   //get current date time with Date()
		   Date date = new Date();
		   System.out.println(dateFormat.format(date));
		   String curDate= date.toString();
		   return curDate ;
		   //get current date time with Calendar()
		  // Calendar cal = Calendar.getInstance();
		 //  System.out.println(dateFormat.format(cal.getTime()));
		   
	} 
	
	public static WebDriver getDriver() {
		if (driver == null) {
			throw new RuntimeException("------Driver not found-------");
		}

		return driver;

	}
	
	@BeforeSuite
	public void launchBrowser() {
		 DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
		 capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
		 System.setProperty("webdriver.ie.driver", "Driver\\IEDriverServer.exe");
		 driver = new InternetExplorerDriver();
		 System.out.println("Loading the locator properties");
		 driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		 driver.manage().deleteAllCookies();
	}
	
	
	
	 public void LoadURL(String URL) {
		    driver = Common.getDriver();
	    	driver.manage().window().maximize();
	    	driver.navigate().refresh();
	        driver.get(URL);
	        driver.navigate().refresh();
	        try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String title = driver.getTitle();
			Assert.assertEquals("Customer Project", title);
	    }
	
	 
		public void error1(){
			driver = Common.getDriver();
		String title = driver.findElement(By.xpath("//div[@class='ui-dialog ui-widget ui-widget-content ui-corner-all ui-draggable rsqe-dialog-container set-error-dialog-width']/div[1]/span")).getText();
		if (title.equalsIgnoreCase("Error!")){
			driver.findElement(By.xpath("//div[@class='ui-dialog ui-widget ui-widget-content ui-corner-all ui-draggable rsqe-dialog-container set-error-dialog-width']/div[2]/div[1]/div[2]/div[1]/button[2]")).click();
		}
		else{
			System.out.println("no error is displayed");
		}
			
		}
		
		public void error(WebDriver driver) throws InterruptedException, AWTException, IOException{
			bulkconfig = new Bulkconfiguration();
			Thread.sleep(40000);
			//driver.findElement(By.xpath("//body[@id='ng-app']/div[9]/div[2]/div/div[2]/div/button[1]")).click();
			bulkconfig.accordian("Base Configuration");
			Thread.sleep(5000);
			Thread.sleep(5000);
			Thread.sleep(5000);
			Thread.sleep(5000);
			Thread.sleep(5000);
			Thread.sleep(5000);
			//Runtime.getRuntime().exec("C:\\Users\\606522807\\Desktop\\PressEnter.exe");
			Robot robot;
			robot = new Robot();
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			Thread.sleep(5000);
			Thread.sleep(5000);			
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			Thread.sleep(5000);
			Thread.sleep(5000);
			driver.navigate().refresh();	
			Thread.sleep(20000);
			
					
		}
		
		public void error2() throws InterruptedException, AWTException{		
			driver = Common.getDriver();
			Robot robot;
			robot = new Robot();
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			//driver.navigate().refresh();
			Thread.sleep(5000);
			Thread.sleep(5000);
					
		}
		
		
	
/*	@AfterSuite
	public void closeConnection() {

		driver.close();
		// CFPDBUtils.releaseConnection(rs, stmt, con)
		// Reporter.log("XML -->UI Validation-->Completed");
		try {
			Runtime.getRuntime().exec(
					"rundll32 url.dll,FileProtocolHandler "
							+ "test-output\\html\\index.html");
			Runtime.getRuntime().exec(
					"rundll32 url.dll,FileProtocolHandler "
							+ "test-output\\index.html");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/
}
