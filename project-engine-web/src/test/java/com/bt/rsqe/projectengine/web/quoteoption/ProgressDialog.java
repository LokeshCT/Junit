package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.google.common.base.Predicate;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.annotation.Nullable;

public class ProgressDialog extends SeleniumWebDriverTestContext {

    @FindBy(css = "#progressText")
    private WebElement productSelect;

    @FindBy(css = "#errorMessages")
    private WebElement errorMessages;


    public ProgressDialog(WebDriver driver) {
        super(driver);
    }

    public boolean isSuccessTextDisplayed() {
        return "Upload Successful".equals(productSelect.getText());
    }

    public void assertErrorMessagesAreDisplayed() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return productSelect.getText().contains("Upload unsuccessful, please try again.")
                       && errorMessages.getText().contains("The attribute smith is required")
                       && errorMessages.getText().contains("The attribute bob is required");
            }
        });
    }


}
