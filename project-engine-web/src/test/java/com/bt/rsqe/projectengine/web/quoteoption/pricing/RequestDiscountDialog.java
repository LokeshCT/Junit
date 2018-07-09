package com.bt.rsqe.projectengine.web.quoteoption.pricing;

import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.bt.rsqe.web.entityfinder.EntityCommand;
import com.bt.rsqe.web.entityfinder.EntityFinder;
import com.bt.rsqe.web.entityfinder.functions.Functions;
import com.google.common.base.Predicate;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.web.entityfinder.functions.Functions.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class RequestDiscountDialog extends SeleniumWebDriverTestContext {

    public static final String VALUE_ATTRIBUTE_NAME = "value";
    @FindBy(css = "#requestDiscountDialogForm")
    private WebElement form;

    @FindBy(css = "#customerGroupEmailId")
    private WebElement customerGroupEmailId;

    @FindBy(css = "#bidManagerList")
    private WebElement bidManagerList;

    @FindBy(css = "#requestDiscountDialog")
    private WebElement dialog;

    @FindBy(css = "#sendDiscountApprovalButton")
    private WebElement sendDiscountApprovalButton;

    @FindBy(css = "#sendDiscountApprovalMessage")
    private WebElement sendDiscountApprovalMessage;

    @FindBy(css = ".dialog .submit")
    private WebElement requestDiscountButton;

    @FindBy(css = ".dialog label.error")
    private WebElement dialogError;

    @FindBy(css = "#okDiscountApprovalSuccess")
    private WebElement closeButton;
    private WebDriver driver;

    @FindBy(css = "#commercialNonStandardRequest")
    private WebElement commercialNonStandardCheckBox;

    @FindBy(css = "#revenueSelectAll")
    private WebElement revenueSelectAll;

    private EntityFinder entityFinder;

    public RequestDiscountDialog(WebDriver driver) {
        super(driver);
        this.driver = driver;
        entityFinder = new EntityFinder(driver);
    }

    public void assertHasExpectedFields() {
        try {
            assertTrue(customerGroupEmailId.isDisplayed() &&
                       bidManagerList.isDisplayed());
            requestDiscountButton.isDisplayed();
            closeButton.isDisplayed();
        } catch (NoSuchElementException e) {
            fail(e.getMessage());
        }
    }

    public boolean isDialogLoaded() {
        return dialog.isDisplayed() && !(new Select(bidManagerList).getOptions().isEmpty());
    }

    public void assertNotVisible() {
        assertFalse(form.isDisplayed());
    }

    public void close() {
        closeButton.click();
    }

    public void assertHasCorrectGroupEmailId(String groupEmailId) {
        assertThat(customerGroupEmailId.getAttribute(VALUE_ATTRIBUTE_NAME), is(groupEmailId));
    }

    public void assertIsDisplayed() {
        assertTrue(form.isDisplayed());
    }

    public void assertHasExpectedBidManagerList(Map expectedBidManagerEmailData) {
        Select bidManagerDropDown = new Select(bidManagerList);
        assertEmailIdsExist(bidManagerDropDown.getOptions(), expectedBidManagerEmailData.keySet());
        assertNamesWithEmailsExist(bidManagerDropDown.getOptions(), expectedBidManagerEmailData.values());
    }

    private void assertNamesWithEmailsExist(List<WebElement> options, Collection expectedNamesWithEmails) {
        Set<String> actualNames = new HashSet<String>();
        for (WebElement option : options) {
            actualNames.add(option.getText());
        }
        assertTrue(actualNames.containsAll(expectedNamesWithEmails));
    }

    private void assertEmailIdsExist(List<WebElement> options, Set<String> expectedEmailIds) {
        Set<String> actualEmailIds = new HashSet<String>();
        for (WebElement option : options) {
            actualEmailIds.add(option.getAttribute("value"));
        }
        assertTrue(actualEmailIds.containsAll(expectedEmailIds));

    }

    public void selectFirstBidManager() {
        Select bidManagerDropDown = new Select(bidManagerList);
        bidManagerDropDown.selectByIndex(1);
    }

    public void submitRequestDiscountApproval() {
        this.sendDiscountApprovalButton.click();
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                boolean dialogErrorDisplayed = false;
                try {
                    dialogErrorDisplayed = dialogError.isDisplayed();
                } catch(NoSuchElementException e){
                    //ignore
                }
                return !sendDiscountApprovalMessage.getText().isEmpty()
                            || dialogErrorDisplayed;
            }
        });
    }

    public void assertDiscountRequestSuccessMessage() {
        assertThat(sendDiscountApprovalMessage.getText(), containsString("Request has been sent successfully"));
    }

    public void assertDiscountRequestNotAllowedMessage() {
        assertThat(dialogError.getText(), is("Please select a Bid Manager"));
    }
    public void assertValidationMessage(String message) {
        assertThat(sendDiscountApprovalMessage.getText(), is(message));
    }

    public void assertDiscountRequestFailedMessage() {
        assertThat(sendDiscountApprovalMessage.getText(), is("Discount approval request failed"));
    }

    public void assertRevenueCheckBoxEnabled() {
        assertTrue(commercialNonStandardCheckBox.isEnabled());
    }

    public void clickOnRevenueCheckBox() {
        commercialNonStandardCheckBox.click();
    }

    public void enterProposedValueAndTriggerMonths(final String proposedRevenue, final String triggerMonths) {
        entityFinder.find("revenueTable").forEach(new EntityCommand() {
            @Override
            public void apply(WebElement entity) {
                final String revenue = "proposedRevenue";
                doAction().click(on(Functions.field(revenue)));
                doAction().clearReKeyAndSubmit(textBox().in(field(revenue)), proposedRevenue);
                final String month = "triggerMonths";
                doAction().click(on(Functions.field(month)));
                doAction().clearReKeyAndSubmit(textBox().in(field(month)), triggerMonths);
            }
        });
    }

    public void clickOnSelectAll() {
        revenueSelectAll.click();
    }

    public void clickOnSubmitButton() {
        sendDiscountApprovalButton.click();
    }
}
