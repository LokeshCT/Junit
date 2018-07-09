package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.seleniumsupport.liftstyle.Wait;
import com.google.common.base.Predicate;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import javax.annotation.Nullable;

public class BulkTemplateDialog extends ProductDialog {

    @FindBy(css = "#downloadForProduct")
    private WebElement download;

    @FindBy(css = "#bulkTemplateDialog .product")
    private WebElement productSelect;


    private final WebDriver browser;

    public BulkTemplateDialog(WebDriver browser) {
        super(browser, "bulkTemplateDialog", "downloadForProduct");
        this.browser = browser;

    }

    public BulkTemplateDialog clickDownload() {
        download.click();
        return this;
    }


    public void assertProductValue(final String product) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                final Select productSelector = new Select(productSelect);
                return productSelector.getFirstSelectedOption().getText().equals(product);
            }
        });
    }


    public void waitForLoad() {
        Wait.until(browser,new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return productSelect.isDisplayed();
            }
        });
    }
}
