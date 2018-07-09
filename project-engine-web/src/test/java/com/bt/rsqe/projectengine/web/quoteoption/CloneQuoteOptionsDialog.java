package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.google.common.base.Predicate;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CloneQuoteOptionsDialog extends SeleniumWebDriverTestContext {
    @FindBy(css = "#copyOptionsDialog .targetQuoteOption")
    private WebElement targetOptionsDropDown;

    @FindBy(css = "#copyOptionsDialog .submit")
    private WebElement submit;

    @FindBy(css = "#copyOptionsDialog .cancel")
    private WebElement cancel;

    @FindBy(css = "#quoteOptionId")
    private WebElement sourceQuoteOptionId;

    @FindBy(css = "#copyOptionsMessage")
    private WebElement submitError;

    @FindBy(css = "#progressText")
    private WebElement progressText;


    private final WebDriver browser;

    public CloneQuoteOptionsDialog(WebDriver browser) {
        super(browser);
        this.browser = browser;
        PageFactory.initElements(browser, this);
    }


    public boolean isDisplayed() {
        return targetOptionsDropDown.isDisplayed();
    }

    public Iterable<String> getDisplayedOptions() {
        final Select select = new Select(targetOptionsDropDown);
        List<String> optionNames = new ArrayList<String>();
        for (WebElement option : select.getOptions()) {
            optionNames.add(option.getText());
        }
        return optionNames;
    }

    public void selectTargetQuoteOption(String quoteOptionName) {
        final Select select = new Select(targetOptionsDropDown);
        select.selectByVisibleText(quoteOptionName);
    }

    public void submit() {
        this.submit.click();
    }

    public void assertDialogProgressFailureMessageDisplayed(final String expectedError) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                String text = progressText.getText();
                return text.contains("Copy was unsuccessful.");
            }
        });
    }

    public boolean isErrorMessageDisplayed() {
        return !submitError.getAttribute("class").contains("hidden");
    }

    public String getSourceQuoteOptionId() {
        return sourceQuoteOptionId.getAttribute("value");
    }

    public String getTargetQuoteOptionId() {
        return targetOptionsDropDown.getAttribute("value");
    }
}
