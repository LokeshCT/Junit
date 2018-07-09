package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class ContractPage extends SeleniumWebDriverTestContext {
    @FindBy(css = "#contractForm")
    private WebElement form;

    @FindBy(css = "#contractTerm")
    private WebElement contractTerm;

    @FindBy(css = "#eupPriceBook")
    private WebElement eupPriceBook;

    @FindBy(css = "#ptpPriceBook")
    private WebElement ptpPriceBook;

    @FindBy(css = "#submitContractButton")
    private WebElement submitButton;

    @FindBy(css = ".dialog .cancel")
    private WebElement cancelButton;

    @FindBy(css = ".ui-dialog-titlebar a.ui-dialog-titlebar-close")
    private WebElement closeButton;

    private WebDriver driver;

    public ContractPage(WebDriver driver) {
        super(driver);
        this.driver = driver;
    }

    public void waitForFieldsToLoad() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return eupPriceBook.isDisplayed() &&
                       submitButton.isDisplayed();
            }
        });
    }

    public void configureContract() {
        waitForFieldsToLoad();
        checkForErrorsIfDataIsInvalid();
        selectValidValues();
        submit();
    }

    private void selectValidValues() {
        new Select(eupPriceBook).selectByIndex(1);
    }

    private void checkForErrorsIfDataIsInvalid() {
        new Select(eupPriceBook).selectByValue("");
        submit();
        assertThat(form.findElement(By.cssSelector("#eupPriceBook+label.error")).getText(), is("Please select a EUP Price book"));
    }

    public void submit() {
        submitButton.click();
    }

    public void waitForDialogToClose() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                boolean formClosed;
                try {
                    formClosed = !form.isDisplayed();
                } catch (NoSuchElementException e) {
                    formClosed = true;
                }
                return formClosed;
            }
        });
    }

    public void cancel() {
        cancelButton.click();
    }

    public void close() {
        closeButton.click();
    }
}
