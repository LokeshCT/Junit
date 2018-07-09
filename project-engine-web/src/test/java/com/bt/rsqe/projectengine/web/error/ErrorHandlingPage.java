package com.bt.rsqe.projectengine.web.error;

import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ErrorHandlingPage extends SeleniumWebDriverTestContext {

    @FindBy(css = "div.leftPaneContainer h1")
    private WebElement header;

    @FindBy(id = "errorMessage")
    private WebElement errorMessage;

    public ErrorHandlingPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public void assertTitleIs(String rsqeErrorPageTitle) {
        assertThat(context.getTitle(), is(rsqeErrorPageTitle));
    }

    public void assertHeaderIs(String errorPageHeading) {
        assertThat(header.getText(), is(errorPageHeading));
    }

    public void assertErrorMessage(String testExceptionMessage) {
        assertThat(errorMessage.getText(), is("Error Message : Error : " + testExceptionMessage));
    }
}
