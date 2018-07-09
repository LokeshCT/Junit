package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.google.common.base.Predicate;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import javax.annotation.Nullable;

import static com.bt.rsqe.projectengine.web.Selectors.*;
import static com.bt.rsqe.seleniumsupport.liftstyle.finders.OptionFinder.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class CustomerQuoteOptionDialogPage extends SeleniumWebDriverTestContext {

    public static final String VALUE_ATTRIBUTE_NAME = "value";
    @FindBy(css = "#quoteOptionForm")
    private WebElement form;

    @FindBy(css = "#quoteOptionName")
    private WebElement quoteNameField;

    @FindBy(css = "#contractTerm")
    private WebElement contractTermsSelect;

    @FindBy(css = "#currency")
    private WebElement currencySelect;

    @FindBy(css = "#submitOptionButton")
    private WebElement submitButton;

    @FindBy(css = ".dialog .cancel")
    private WebElement cancelButton;

    @FindBy(css = ".ui-dialog-titlebar a.ui-dialog-titlebar-close")
    private WebElement closeButton;
    private WebDriver driver;

    public CustomerQuoteOptionDialogPage(WebDriver driver) {
        super(driver);
        this.driver = driver;
    }

    public void assertHasExpectedFields() {
        try {
            assertTrue(quoteNameField.isDisplayed() &&
                       contractTermsSelect.isDisplayed() &&
                       currencySelect.isDisplayed());
            cancelButton.isDisplayed();
            submitButton.isDisplayed();
        } catch (NoSuchElementException e) {
            fail(e.getMessage());
        }
    }

    public void enterQuoteOptionDetails(String name, String currency, String term) {
        quoteNameField.clear();
        quoteNameField.sendKeys(name);
        given(currencySelect).clickOn(option(currency));
        given(contractTermsSelect).clickOn(option(term));
    }

    public void submit() {
        submitButton.click();
    }

    public void waitForDialogToClose() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                boolean formClosed = false;
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

    public void assertNotVisible() {
        assertFalse(form.isDisplayed());
    }

    public void close() {
        closeButton.click();
    }

    public void assertFieldsEmpty() {
        assertThat(quoteNameField.getAttribute(VALUE_ATTRIBUTE_NAME), is(""));
        assertThat(new Select(contractTermsSelect).getAllSelectedOptions().size(), is(0));
        assertThat(new Select(currencySelect).getAllSelectedOptions().size(), is(0));
    }

    public void assertPopulatedFields(String quoteOptionName, String currency, String term) {
        assertThat(quoteNameField.getAttribute(VALUE_ATTRIBUTE_NAME), is(quoteOptionName));
        assertThat(new Select(currencySelect).getFirstSelectedOption().getAttribute(VALUE_ATTRIBUTE_NAME), is(currency));
        assertThat(new Select(contractTermsSelect).getFirstSelectedOption().getAttribute(VALUE_ATTRIBUTE_NAME), is(term));
    }

    public void assertIsDisplayed() {
        assertTrue(form.isDisplayed());
    }

    public void assertHasDuplicateErrorMessageForQuoteOptionName(String quoteOptionName) {
        String errorMessage = String.format("Quote option with name '%s' already exists.", quoteOptionName);
         waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return (!driver.findElement(byCss(".commonError")).getText().isEmpty());
            }
        });
        assertThat(driver.findElement(byCss(".commonError")).getText(), is(errorMessage));
    }
}
