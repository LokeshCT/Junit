package com.bt.rsqe.projectengine.web.js;

import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import javax.annotation.Nullable;

public class JsTestPage extends SeleniumWebDriverTestContext {
    private WebDriver driver;
    private String baseUri;

    public JsTestPage(WebDriver driver, String baseUri) {
        super(driver);
        this.driver = driver;
        this.baseUri = baseUri;
    }

    public void runJsTests() {
        goTo(baseUri);

        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return testsFinished(input);
            }
        });
    }

    private boolean testsFinished(WebDriver input) {
        return input.findElement(By.cssSelector("span.finished-at")) != null;
    }

    public boolean ranAtleastOneSpec() {
        return specResult().matches("^[1-9]\\d*\\s+spec.*");
    }

    public String specResult() {
        return driver.findElement(By.cssSelector(".runner .description")).getText();
    }
    public String specOutput() {
        return driver.findElement(By.cssSelector(".suite")).getText();
    }

    public boolean testPassed() {
        return driver.findElements(By.cssSelector(".runner.passed")).size() != 0;
    }
}
