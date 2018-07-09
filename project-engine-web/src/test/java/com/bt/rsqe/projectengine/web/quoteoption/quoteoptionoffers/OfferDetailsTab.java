package com.bt.rsqe.projectengine.web.quoteoption.quoteoptionoffers;


import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.bt.rsqe.web.WaitingWebElement;
import com.bt.rsqe.web.entityfinder.EntityFinder;
import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import javax.annotation.Nullable;
import java.util.List;

import static com.bt.rsqe.projectengine.web.Selectors.*;
import static com.bt.rsqe.web.entityfinder.EntityMatchers.*;
import static com.bt.rsqe.web.entityfinder.functions.Functions.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class OfferDetailsTab extends SeleniumWebDriverTestContext {
    @FindBy(css = "#offerDetails")
    private WebElement offerDetailsTable;
    @FindBy(css = "#createOrder")
    private WebElement createOrder;
    @FindBy(css = "#customerApproved")
    private WebElement customerApproved;
    @FindBy(css = "#customerRejected")
    private WebElement customerRejected;
    @FindBy(css = ".dataTables_scrollBody input[type='checkbox']")
    private List<WebElement> listOfOffersCheckboxes;
    @FindBy(css = "#commonError")
    private WebElement commonErrorDiv;
    @FindBy(css = "#validate")
    private WebElement validateButton;
    @FindBy(css = "#selectAll")
    private WebElement selectAll;

    private WebDriver driver;
    private EntityFinder entityFinder;

    public OfferDetailsTab(WebDriver driver) {
        super(driver);
        this.driver = driver;
        entityFinder = new EntityFinder(driver);
    }

    public void assertPresenceOfDetailsTable() {
        assertTrue(offerDetailsTable.isDisplayed());
    }

    public void assertHasOfferStatus(String product, String site, QuoteOptionItemStatus status) {
        final EntityFinder.Context entity = new EntityFinder(context()).find("offerItem")
                                                                       .with("product", product)
                                                                       .with("site", site)
                                                                       .with("status", status.getDescription());
        assertThat(entity, isPresent());
    }

    public void assertHasOfferDetail(String product, String site, String status, String discountStatus,
                                     String pricingStatus, String oneTimeNetValue, String recurringNetValue, String valid) {
        final EntityFinder.Context entity = new EntityFinder(context()).find("offerItem")
                                                                       .with("product", product)
                                                                       .with("site", site)
                                                                       .with("status", status)
                                                                       .with("discountStatus", discountStatus)
                                                                       .with("pricingStatus", pricingStatus)
                                                                       .with("oneTimeNetValue", oneTimeNetValue)
                                                                       .with("recurringNetValue", recurringNetValue)
                                                                       .with("validity", valid);
        assertThat(entity, isPresent());
    }

    public void chooseLineItem(int zeroIndexed) {
        listOfOffersCheckboxes.get(zeroIndexed).click();
    }

    public void chooseAllLineItems() {
        for (WebElement offersCheckbox : listOfOffersCheckboxes) {
            offersCheckbox.click();
        }
    }

    public void clickCreateOrderButton() {
        createOrder.click();
    }

    public void assertHasErrorMessage(String errorMessage) {
        assertThat(commonErrorDiv.getText(), is(errorMessage));
    }

    public void clickCustomerAcceptedButton() {
        customerApproved.click();
    }

    public void clickCustomerRejectedButton() {
        customerRejected.click();
    }

    public void assertAcceptOfferButtonShown() {
        assertThat(customerApproved.isDisplayed(), is(true));
    }

    public void assertAcceptOfferButtonNotShown() {
        assertElementDoesNotExist("customerApproved");
    }

    public void assertRejectOfferButtonShown() {
        assertThat(customerRejected.isDisplayed(), is(true));
    }

    public void assertRejectOfferButtonNotShown() {
        assertElementDoesNotExist("customerRejected");
    }

    public void assertCreateOrderShown() {
        assertThat(createOrder.isDisplayed(), is(true));
    }

    public void assertCreateOrderButtonNotShown() {
        assertElementDoesNotExist("createOrder");
    }

    private void assertElementDoesNotExist(String elementId) {
        try {
            /* Hugh : had to do it this way as the Customer Approved or Create Order buttons may not be on page.
             * If such an element is not on the page and we access the Selenium annotated field directly,
             * Selenium will time out waiting for something which will never appear :-/
             */
            context().findElement(By.id(elementId));
            throw new AssertionError(elementId + " element should not appear on page!");
        } catch (Exception e) {
        }
    }

    public void assertCreateOrderButtonDisabled() {
        assertThat(createOrder.getAttribute("class").contains("disabled"), is(true));
    }

    public void assertCreateOrderDialogIsNotDisplayed() {
        final CreateOrderDialog createOrderDialog = new CreateOrderDialog();
        PageFactory.initElements(context(), createOrderDialog);
        assertFalse(createOrderDialog.titleBar.isDisplayed());
    }

    public OfferDetailsTab waitForCreateOrderButtonIsDisabledToComplete() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return createOrder.getAttribute("class").contains("disabled");
            }
        });
        return this;
    }

    public CreateOrderDialog createOrder(String orderName) {
        final CreateOrderDialog orderDialog = openCreateOrderDialog();
        orderDialog.assertDialogTitleIs("Create Order");
        orderDialog.enterOrderName(orderName);
        orderDialog.submit();
        return orderDialog;
    }

    public CreateOrderDialog openCreateOrderDialog() {
        clickCreateOrderButton();
        final CreateOrderDialog createOrderDialog = new CreateOrderDialog();
        PageFactory.initElements(context(), createOrderDialog);
        return createOrderDialog;
    }

    public void assertHasNumberOfOfferItems(int expectedNumberOfOfferItems) {
        assertThat(driver.findElements(byCss(".offerItem")).size(), is(expectedNumberOfOfferItems));
    }

    public void assertHasOfferItemWithSite(String siteName) {
        assertThat(entityFinder.find("offerItem").with(field("site").containsText(siteName)),
                   isVisible());
    }

    public OfferDetailsTab waitUntilOfferItemsDisplayed() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return driver.findElements(byCss(".offerItem")).size() > 0 || driver.findElements(byCss("tr .dataTables_empty")).size() > 0;

            }
        }, 10);
        return this;
    }

    public OfferDetailsTab waitUntilOfferItemFirstCheckboxDisplayed() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return driver.findElements(byCss(".dataTables_scrollBody input[type='checkbox']")).size() > 0;
            }
        }, 10);
        return this;
    }

    public OfferDetailsTab selectAllItems() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return entityFinder.find("offerItem").isVisible();
            }
        });
        final WebElement select = WaitingWebElement.waitOn(selectAll);
        if (!select.isSelected()) {
            select.click();
        }
        return this;
    }

    public void validate() {
        if (!isEnabled(validateButton)) {
            throw new RuntimeException("validate button is not enabled!");
        }
        validateButton.click();
    }

    private boolean isEnabled(WebElement button) {
        return !newArrayList(button.getAttribute("class").split("\\s")).contains("disabled");
    }

    public OfferDetailsTab waitForItemToBecomeValid() {
        assertThat(entityFinder.find("offerItem").with(field("validity").containsText(LineItemValidationResultDTO.Status.VALID.name())), isPresent());
        return this;
    }


    public class CreateOrderDialog {

        @FindBy(css = "#orderName")
        private WebElement orderNameTextField;

        @FindBy(css = "#submitCreateOrder")
        private WebElement submitButton;

        @FindBy(css = "#createOrderForm")
        private WebElement createOrderForm;

        @FindBy(css = "#dialogError")
        private WebElement error;

        @FindBy(css = "#ui-dialog-title-createOrderDialog")
        private WebElement titleBar;

        public void enterOrderName(String orderName) {
            orderNameTextField.clear();
            orderNameTextField.sendKeys(orderName);
        }

        public void submit() {
            submitButton.click();
        }

        public void assertDialogTitleIs(String title) {
            assertThat(titleBar.getText(), is(title));
        }

        public void assertHasErrorMessage(String errorMessage) {
            assertThat(error.getText(), is(errorMessage));
        }
    }

}
