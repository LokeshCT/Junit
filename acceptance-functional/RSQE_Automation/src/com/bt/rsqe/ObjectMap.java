package com.bt.rsqe;

import java.io.InputStream;
import java.util.Properties;

import org.openqa.selenium.By;

public class ObjectMap {

	Properties properties;

	public ObjectMap() {
		properties = new Properties();

		try {
			InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("locator.properties");

			properties.load(in);
			in.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public By getLocator(String element) {
		String locator = properties.getProperty(element);
		String locatorType = locator.split(">")[0].trim();
		String locatorValue = locator.split(">")[1].trim();
		System.out.println("CreateQuoteName::" + locatorValue);
		if (locatorType.toLowerCase().equals("id"))
			return By.id(locatorValue);
		else if (locatorType.toLowerCase().equals("xpath"))
			return By.xpath(locatorValue);
		else if (locatorType.toLowerCase().equals("className"))
			return By.className(locatorValue);
		else if (locatorType.toLowerCase().equals("cssSelector"))
		     return By.cssSelector(locatorValue);
		return null;
	}
}
