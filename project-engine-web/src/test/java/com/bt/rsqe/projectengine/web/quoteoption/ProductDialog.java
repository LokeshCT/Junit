package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.google.common.base.Predicate;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.annotation.Nullable;

import static com.bt.rsqe.projectengine.web.Selectors.*;
import static org.junit.Assert.*;

public abstract class ProductDialog extends SeleniumWebDriverTestContext {

    private WebDriver driver;
    private String dialogId;
    private String submitButtonId;

    public ProductDialog(WebDriver driver, String dialogId, String submitButtonId) {
        super(driver);
        this.driver = driver;
        this.dialogId = dialogId;
        this.submitButtonId = submitButtonId;
    }

    public void clickSubmit() {
        final WebElement submit = driver.findElement(byCss("#" + dialogId + " #" + submitButtonId));
        submit.click();

    }

    public void clickCancelAndWaitForClose() {
        final WebElement cancel = driver.findElement(byCss("#" + dialogId + " .cancel"));
        cancel.click();
        new WebDriverWait(driver, 5).until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return !cancel.isDisplayed();
            }
        });
    }

    public void assertIsNotDisplayed() {
        final WebElement cancel = driver.findElement(byCss("#" + dialogId + " .cancel"));
        assertFalse(cancel.isDisplayed());
    }


    public void assertIsDisplayed() {
        final WebElement cancel = driver.findElement(byCss("#" + dialogId + " .cancel"));
        assertTrue(cancel.isDisplayed());
    }

    public void chooseProduct(String productName) {
        final WebElement productSelect = driver.findElement(byCss("#" + dialogId + " .product"));
        Select productSelector = new Select(productSelect);
        productSelector.selectByVisibleText(productName);
    }
}
