package com.bt.rsqe.projectengine.web.projects;

import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.google.common.base.Predicate;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.annotation.Nullable;

import static org.junit.Assert.*;

public class ConfirmationDialog extends SeleniumWebDriverTestContext {

        @FindBy(css = "#confirmationDialog")
        private WebElement confirmationDialog;

        @FindBy(css = "#dialogOkButton")
        private WebElement confirmationDialogOkButton;

        @FindBy(css = "#confirmationDialogNoOption")
        private WebElement confirmationDialogNoCheckBox;

        @FindBy(css = "#confirmationDialogYesOption")
        private WebElement confirmationDialogYesCheckBox;

        public ConfirmationDialog(WebDriver driver) {
            super(driver);
        }

        public void assertConfirmationDialogIsDisplayed(final boolean displayed) {
            waitUntil(new Predicate<WebDriver>() {
                @Override
                public boolean apply(@Nullable WebDriver input) {
                    if (displayed) {
                        return confirmationDialog.isDisplayed();
                    } else {
                        return !confirmationDialog.isDisplayed();
                    }
                }
            });
            assertTrue(displayed ? confirmationDialog.isDisplayed() : !confirmationDialog.isDisplayed());
        }

        public void clickOnYesOnConfirmationDialog() {
            waitUntil(new Predicate<WebDriver>() {
                @Override
                public boolean apply(@Nullable WebDriver input) {
                    return confirmationDialogOkButton.isDisplayed();
                }
            });
            confirmationDialogYesCheckBox.click();
            confirmationDialogOkButton.click();
        }

        public void clickOnNoOnConfirmationDialog() {
            waitUntil(new Predicate<WebDriver>() {
                @Override
                public boolean apply(@Nullable WebDriver input) {
                    return confirmationDialogOkButton.isDisplayed();
                }
            });
            confirmationDialogNoCheckBox.click();
            confirmationDialogOkButton.click();
        }
    }
