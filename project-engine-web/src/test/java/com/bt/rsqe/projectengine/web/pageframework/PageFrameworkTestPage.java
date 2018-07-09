package com.bt.rsqe.projectengine.web.pageframework;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.text.StringContains.*;
import static org.junit.Assert.*;

public class PageFrameworkTestPage {

    private final String BASE_URL;
    private final String TAB_B_URL;
    private WebDriver driver;

    @FindBy(css = "#breadCrumb span")
    private WebElement breadCrumb;

    @FindBy(css="#content div.tabA")
    private WebElement tabAContent;

    @FindBy(css="#content div.tabB")
    private WebElement tabBContent;

    @FindBy(css="#tabBSource")
    private WebElement tabBSource;

    @FindBy(css="#tabBDestination")
    private WebElement tabBDestination;

    @FindBy(css="#tabBCopy")
    private WebElement tabBCopy;

    @FindBy(css = "#PageFrameworkTestTabA")
    private WebElement tabA;

    @FindBy(css = "#PageFrameworkTestTabB")
    private WebElement tabB;

    public PageFrameworkTestPage(WebDriver driver) {
        this.driver = driver;
        BASE_URL = "http://localhost:" + PageFrameworkTest.port() + "/page-framework";
        TAB_B_URL = BASE_URL + "#PageFrameworkTestTabB";
    }

    public void assertOnBasePage() {
        assertThat(breadCrumb.getText(), containsString(PageFrameworkTest.HEADER));
    }

    public void openDefaultTab() {
        driver.get(BASE_URL);
    }

    public void openTabB() {
        driver.get(TAB_B_URL);
    }

    public void assertOnTabA() {
        assertTrue(tabAContent.isDisplayed());
    }

    public void assertOnTabB() {
        assertTrue(tabBContent.isDisplayed());
    }

    public void selectTabA() {
        tabA.click();
    }

    public void selectTabB() {
        tabB.click();
    }

    public void copyDivContent() {
        tabBCopy.click();
    }

    public void assertContentHasBeenCopied() {
        assertThat(tabBDestination.getText(), is(tabBSource.getText()));
    }
}
