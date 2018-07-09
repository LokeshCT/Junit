package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.projectengine.IfcAction;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.seleniumsupport.liftstyle.Wait;
import com.bt.rsqe.web.entityfinder.EntityActions;
import com.bt.rsqe.web.entityfinder.EntityCommand;
import com.bt.rsqe.web.entityfinder.EntityFinder;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.hamcrest.Matcher;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.bt.rsqe.projectengine.web.Selectors.*;
import static com.bt.rsqe.projectengine.web.quoteoptiondetails.IfcActionMapper.*;
import static com.bt.rsqe.web.entityfinder.EntityMatchers.*;
import static com.bt.rsqe.web.entityfinder.functions.Functions.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class QuoteOptionDetailsTab extends PaginatedTab {

    private static final String LINE_ITEM_CLASS = "lineItem";
    private static final String ERROR_ROW_CLASS = "errorRow";
    private WebDriver driver;

    @FindBy(css = ".slideFilter")
    private WebElement slideFilter;

    @FindBy(css = "#hideFailedLineItemsCheckbox")
    private WebElement hideFailedLineItemsCheckbox;

    @FindBy(css = "#applyFilterButton")
    private WebElement applyFilterButton;

    @FindBy(css = "tr.odd")
    private WebElement firstLineItemTableRow;

    @FindBy(css = "#lineItems")
    private WebElement lineItemTable;

    @FindBy(css = "#newLineItem")
    private WebElement newLineItemButton;

    @FindBy(css = "#bulkUpload")
    private WebElement bulkUploadButton;

    @FindBy(css = "#createOffer")
    private WebElement createOfferButton;

    @FindBy(css = "#copyOptions")
    private WebElement copyOptionsButton;

    @FindBy(css = "#updateProductConfig")
    private WebElement updateConfigurationButton;

    @FindBy(css = "#fetchPrices")
    private WebElement fetchPricesButton;

    @FindBy(css = "tbody input[type='checkbox']")
    private List<WebElement> listOfProductCheckboxes;

    @FindBy(css = "#commonError")
    private WebElement commonErrorDiv;

    @FindBy(css = "#successMessage")
    private WebElement successMessageDiv;

    private EntityFinder entityFinder;
    @FindBy(css = "#downloadBulkTemplate")
    private WebElement downloadBulkTemplate;

    @FindBy(className = "ui-widget-overlay")
    private WebElement uiWidgetOverlay;

    @FindBy(css = "#raiseIfcs")
    private WebElement raiseIfcs;

    @FindBy(css = "#selectAll")
    private WebElement selectAll;

    @FindBy(css = "#validate")
    private WebElement validate;

    @FindBy(css = "#confirmationDialog")
    private WebElement confirmationDialog;

    @FindBy(css = "#dialogOkButton")
    private WebElement confirmationDialogOkButton;

    @FindBy(css = "#confirmationDialogNoOption")
    private WebElement confirmationDialogNoCheckBox;

    @FindBy(css = "#confirmationDialogYesOption")
    private WebElement confirmationDialogYesCheckBox;

    @FindBy(css = "#lineItems_processing")
    private WebElement processing;

    @FindBy(css = "#lineItems_length select")
    private WebElement lineItemFilter;

    @FindBy(id = "bulkConfiguration")
    private WebElement bulkConfiguration;

    @FindBy(id = "bulkConfigurationButton")
    private WebElement bulkConfigurationButton;

    @FindBy(className = "ModifyProducts")
    private WebElement modifyTab;

    @FindBy(id = "importProduct")
    private WebElement importProductButton;

    @FindBy(id = "locateOnGoogleMaps")
    private WebElement locateOnGoogleMaps;

    @FindBy(css = "#locateOnGoogleMapsUrl")
    private WebElement locateOnGoogleMapsUri;

    private CloneQuoteOptionsDialog cloneQuoteOptionsDialog;
    private HashSet<String> lineItemIds;
    private ImportProductDialog importProductDialog = new ImportProductDialog();

    public HashMap<String, String> getLineItemIdNameMapping() {
        return lineItemIdNameMapping;
    }

    private HashMap<String, String> lineItemIdNameMapping;

    public QuoteOptionDetailsTab(WebDriver driver) {
        super(driver, "entries");
        this.driver = driver;
        entityFinder = new EntityFinder(driver);
    }

    public void assertPresenceOfLineItemTable() {
        assertTrue(tableDisplayed());
    }

    private boolean tableDisplayed() {
        return lineItemTable.isDisplayed();
    }

    public void createOffer(String offerName) {
        QuoteOptionDetailsTab.CreateOfferDialog dialog = openCreateOfferDialog();
        dialog.assertDialogTitleIs("Create Offer");
        dialog.enterOfferName(offerName);
        dialog.submit();
    }

    public void assertHasItem(String itemName, String siteName, String contractTerm, String offerName, QuoteOptionItemStatus status) {
        final EntityFinder.Context entity = entityFinder.find(LINE_ITEM_CLASS)
                                                        .with("name", itemName)
                                                        .with("siteName", siteName)
                                                        .with("contractTerm", contractTerm)

                                                        .with("offerName", offerName)
                                                        .with("status", status.getDescription());
        assertThat(entity, isPresent());
    }


    public void assertHasErrorMessage(String errorMessage) {
        waitForVisible(commonErrorDiv);
        assertThat(commonErrorDiv.getText(), is(errorMessage));
    }

    public void assertHasSuccessMessage(String message) {
        waitForVisible(successMessageDiv);
        assertThat(successMessageDiv.getText(), is(message));
    }

    public AddProductPage openAddProductPage() {
        waitForVisible(newLineItemButton);
        newLineItemButton.click();
        final AddProductPage addProductPage = PageFactory.initElements(driver, AddProductPage.class);
        return addProductPage.assertDisplayed();
    }

    public ModifyCeaseProductPage openModifyCeaseProductPage() {
        waitForVisible(newLineItemButton);
        newLineItemButton.click();
        final ModifyCeaseProductPage modifyCeaseProductPage = PageFactory.initElements(driver, ModifyCeaseProductPage.class);
        return modifyCeaseProductPage.assertDisplayed();
    }

    public QuoteOptionDetailsTab chooseFirstLineItem() {
        chooseLineItem(0);
        return this;
    }

    public void chooseLineItem(final int zeroIndexed) {
        waitUntil(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(@Nullable WebDriver input) {
                return listOfProductCheckboxes.get(zeroIndexed);
            }
        }).click();
    }

    public QuoteOptionDetailsTab clickCreateOfferButton() {
        createOfferButton.click();
        return this;
    }

    public QuoteOptionDetailsTab chooseLineItemsById(String... ids) {
        for (String id : ids) {
            for (WebElement checkBox : listOfProductCheckboxes) {
                if (checkBox.getAttribute("value").equals(id)) {
                    checkBox.click();
                }
            }
        }
        return this;
    }

    public QuoteOptionDetailsTab chooseLineItemByStatus(String pricingStatus) {
        List<WebElement> foundRows = getTableRowByStatus(pricingStatus);
        for (WebElement tableRow : foundRows) {
            tableRow.findElement(By.cssSelector("tbody input[type='checkbox']")).click();
        }
        return this;
    }

    public void chooseAllLineItems() {
        List<WebElement> foundRows = driver.findElements(By.cssSelector("tr.lineItem"));
        for (WebElement tableRow : foundRows) {
            tableRow.findElement(By.cssSelector("tbody input[type='checkbox']")).click();
        }
    }

    public String getFirstLineItemId() {
        return entityFinder.find(LINE_ITEM_CLASS).doAction().resolve(id()).from(entity());
    }

    public void assertPresenceOfOverlay() {
        assertTrue(uiWidgetOverlay.isEnabled());
    }

    public CreateOfferDialog openCreateOfferDialog() {
        clickCreateOfferButton();
        final CreateOfferDialog createOfferDialog = new CreateOfferDialog();
        PageFactory.initElements(driver, createOfferDialog);
        return createOfferDialog;
    }

    public void assertCreateOfferButtonDisabled() {
        waitForButtonDisable("#createOffer");
        createOfferButton.click();
        assertFalse(isCreateOfferDisplayed());
    }

    public void assertImportProductButtonHidden() {
        assertTrue(importProductButton.getAttribute("class").contains("hidden"));
    }

    public void assertImportProductButtonIsNotHidden() {
        assertFalse(importProductButton.getAttribute("class").contains("hidden"));
    }

    public void assertAddProductButtonDisabled() {
        waitForButtonDisable("#newLineItem");
        newLineItemButton.isDisplayed();
    }

    public void assertBulkUploadButtonDisabled() {
        waitForButtonDisable("#bulkUpload");
        bulkUploadButton.isDisplayed();
    }

    public void assertCopyOptionsButtonDisabled() {
        waitForButtonDisable("#copyOptions");
        assertThat(isCopyOptionsButtonEnabled(), is(false));
    }

    private void waitForButtonDisable(final String buttonSelector) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return driver.findElements(By.cssSelector(buttonSelector + ".disabled")).size() > 0;
            }
        });
    }

    private void waitForVisible(final WebElement selector) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return selector.isDisplayed();
            }
        });
    }

    public void assertCreateOfferFormIsDisplayed() {
        final boolean formIsDisplayed = isCreateOfferDisplayed();
        assertTrue(formIsDisplayed);
    }

    private boolean isCreateOfferDisplayed() {
        final CreateOfferDialog createOfferDialog = new CreateOfferDialog();
        PageFactory.initElements(driver, createOfferDialog);
        return createOfferDialog.formIsDisplayed();
    }

    public void assertHasAction(String productSCode, String actionName) {
        final EntityFinder.Context entity = entityFinder.find(LINE_ITEM_CLASS)
                                                        .with(field("sc_" + productSCode))
                                                        .with(field(actionName));
        assertThat(entity, isPresent());
    }

    public void clickActionLink(String productSCode, String actionName) {
        entityFinder.find(LINE_ITEM_CLASS).with(field("sc_" + productSCode)).doAction().click(field(actionName));
    }

    public void assertHasItemStatus(String itemName, QuoteOptionItemStatus status) {
        final EntityFinder.Context entity = entityFinder.find(LINE_ITEM_CLASS)
                                                        .with("name", itemName)
                                                        .with("status", status.getDescription());
        assertThat(entity, isPresent());
    }

    public QuoteOptionDetailsTab assertHasItemValidity(String productName, QuoteOptionItemStatus status, LineItemValidationResultDTO.Status validity) {
        assertThat(entityFinder.find(LINE_ITEM_CLASS)
                               .with(field("name").containsText(productName))
                               .with(field("status").containsText(status.getDescription()))
                               .with(field("validity").textEquals(validity.name())),
                   isPresent());
        return this;
    }

    public void assertHasDiscountStatus(String productName, LineItemDiscountStatus status) {
        assertThat(entityFinder.find(LINE_ITEM_CLASS)
                               .with(field("name").containsText(productName))
                               .with(field("discountStatus").containsText(status.getDescription())),
                   isPresent());
    }

    public void assertHasDiscountStatusForPricingStatus(String productName, LineItemDiscountStatus status, PricingStatus pricingStatus) {
        assertThat(entityFinder.find(LINE_ITEM_CLASS)
                               .with(field("name").containsText(productName))
                               .with(field("discountStatus").containsText(status.getDescription()))
                               .with(field("pricingStatus").containsText(pricingStatus.getDescription())),
                   isPresent());
    }

    public void assertPresenceOfLineItem(String productName) {
        assertThat(entityFinder.find(LINE_ITEM_CLASS)
                               .with(field("name").containsText(productName)),
                   isPresent());
    }

    public void clickOfferLinkOnFirstItem() {
        entityFinder.find(LINE_ITEM_CLASS).doAction().click(field("offerName"));
    }

    public BulkTemplateDialog clickDownloadBulkTemplateButton() {
        downloadBulkTemplate.click();
        BulkTemplateDialog bulkTemplateDialog = new BulkTemplateDialog(driver);
        PageFactory.initElements(driver, bulkTemplateDialog);
        return bulkTemplateDialog;
    }

    public void assertLineItemIsNotConfigurable() {
        EntityFinder.Context entity = entityFinder.find(LINE_ITEM_CLASS)
                                                  .with(field("actions"))
                                                  .with(field("disabled"));
        assertThat(entity, isPresent());
    }

    public QuoteOptionDetailsTab clickOnValidColumn(String productName, QuoteOptionItemStatus status) {
        entityFinder.find(LINE_ITEM_CLASS)
                    .with(field("name").containsText(productName))
                    .with(field("status").containsText(status.getDescription()))
                    .doAction().click(field("validity"));
        return this;
    }

    public void assertErrorIsNotDisplayed(String product, QuoteOptionItemStatus status) {
        eachError(product, status, new EntityCommand() {
            @Override
            public void apply(WebElement entity) {
                assertThat(entity.isDisplayed(), is(false));
            }
        });

    }

    public void assertErrorDoesNotExist(String product, QuoteOptionItemStatus status) {
        lineItem(product, status)
            .count(1)
            .forEach(new EntityCommand() {
                @Override
                public void apply(WebElement entity) {
                    try {
                        driver.findElement(By.cssSelector("#error_" + entity.getAttribute("id") + " .errorMessage"));
                        assert false;
                    } catch (NoSuchElementException e) {

                    }

                }
            });
    }

    private void eachError(final String product, final QuoteOptionItemStatus status, final EntityCommand command) {
        lineItem(product, status)
            .count(1)
            .forEach(new EntityCommand() {
                @Override
                public void apply(WebElement entity) {
                    assertThat(entity.findElement(By.cssSelector(".name")).getText(), containsString(product));
                    assertThat(entity.findElement(By.cssSelector(".status")).getText(), containsString(status.getDescription()));
                    final WebElement errorRow = driver.findElement(By.cssSelector("#error_" + entity.getAttribute("id") + " .errorMessage"));
                    command.apply(errorRow);
                }
            });
    }

    private EntityFinder.Context lineItem(String product, QuoteOptionItemStatus status) {
        return entityFinder.find(LINE_ITEM_CLASS)
                           .with(field("name").containsText(product))
                           .with(field("status").containsText(status.getDescription()));
    }

    public void assertErrorIsDisplayed(final String errorMessage, String product, QuoteOptionItemStatus status) {
        eachError(product, status, new EntityCommand() {
            @Override
            public void apply(WebElement entity) {
                assertThat(entity.isDisplayed(), is(true));
                assertThat(entity.getText(), containsString(errorMessage));
            }
        });
    }

    public void assertErrorIsDisplayed(String lineItemId, String message) {
        assertErrorIsDisplayed(lineItemId, containsString(message));
    }

    private void assertErrorIsDisplayed(String lineItemId, Matcher<String> messageMatcher) {
        final EntityActions rowAction = entityFinder.find(LINE_ITEM_CLASS).with(entityId(lineItemId)).doAction();
        rowAction.click(field("validity"));
        final WebElement errorElement = driver.findElement(By.cssSelector("#error_id_" + lineItemId + " .errorMessage"));
        assertThat(errorElement.isDisplayed(), is(true));
        assertThat(errorElement.getText().trim(), messageMatcher);
        rowAction.click(field("validity"));
        assertThat(errorElement.isDisplayed(), is(false));
    }

    public void assertErrorIsDisplayed(String lineItemId) {
        assertErrorIsDisplayed(lineItemId, not(""));
    }

    public void clickCopyQuoteOptionsButton() {
        copyOptionsButton.click();
        cloneQuoteOptionsDialog = new CloneQuoteOptionsDialog(driver);
    }

    public UpdateProductConfigDialog clickUpdateConfigurationButton() {
        updateConfigurationButton.click();
        UpdateProductConfigDialog updateProductConfigDialog = new UpdateProductConfigDialog(driver);
        PageFactory.initElements(driver, updateProductConfigDialog);
        return updateProductConfigDialog;
    }

    public void waitForCloneQuoteOptionsDialogIsOpened() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return cloneQuoteOptionsDialog.isDisplayed();
            }
        });
    }

    public void waitForCloneQuoteOptionsDialogToClose() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                try {
                    return !cloneQuoteOptionsDialog.isDisplayed();
                } catch (NoSuchElementException e) {
                    return true;
                } catch (StaleElementReferenceException e) {
                    return true;
                }
            }
        }, 45);
    }

    public CloneQuoteOptionsDialog cloneQuoteOptionsDialog() {
        return cloneQuoteOptionsDialog;
    }

    public boolean isCopyOptionsButtonEnabled() {
        return isEnabled(copyOptionsButton);
    }

    private boolean isEnabled(WebElement button) {
        return !newArrayList(button.getAttribute("class").split("\\s")).contains("disabled");
    }

    public void assertHasLineItemOrderStatus(String status) {
        assertThat(entityFinder.find("lineItem").with(field("orderStatus").containsText(status)), isPresent());
    }

    public void waitUntilLineItemsDisplayed() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return driver.findElements(byCss(".lineItem")).size() > 0 || driver.findElements(byCss("tr .dataTables_empty")).size() > 0;

            }
        });
    }

    public void assertThatUpdateConfigurationButtonIsDisabled() {
        assertFalse(isEnabled(updateConfigurationButton));
    }

    public void waitUntilLineItemTableDisplayed() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return lineItemTable.isDisplayed();

            }
        });
    }

    public void assertHasNumberOfLineItems(int expectedNumberOfLineItems) {
        assertThat(entityFinder.find("lineItem").count(expectedNumberOfLineItems), isPresent());
        assertThat(driver.findElements(byCss(".lineItem")).size(), is(expectedNumberOfLineItems));
    }

    public void assertNoLineItems() {
        assertThat(entityFinder.find("lineItem"), isNotPresent());
    }

    public void assertHasItemWithSite(String siteName) {
        assertThat(entityFinder.find(LINE_ITEM_CLASS).with(field("siteName").containsText(siteName)),
                   isVisible());
    }

    public void clickRaiseIfcs() {
        raiseIfcs.click();
    }

    public QuoteOptionDetailsTab selectAllItems() {
        if (!selectAll.isSelected()) {
            selectAll.click();
        }
        return this;
    }

    public QuoteOptionDetailsTab deSelectAllItems() {
        if (selectAll.isSelected()) {
            selectAll.click();
        }
        return this;
    }

    public QuoteOptionDetailsTab waitForValidationAndSelectAllItems() {
        driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return input.findElements(By.cssSelector("td.validation")).size() == 0;
            }
        }, 40);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        return selectAllItems();
    }

    public QuoteOptionDetailsTab waitForValidationAndSelectItem(String pricingStatus) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return input.findElements(By.cssSelector("td.validation")).size() == 0;
            }
        }, 40);
        return chooseLineItemByStatus(pricingStatus);
    }

    public void validate() {
        if (!isEnabled(validate)) {
            throw new RuntimeException("validate button is not enabled!");
        }
        validate.click();
    }

    public void waitForItemWithStatusToBecomeInvalid(final QuoteOptionItemStatus status) {
        assertThat(entityFinder.find(LINE_ITEM_CLASS).with(field("status").containsText(status.getDescription())).with(field("validity").containsText(LineItemValidationResultDTO.Status.INVALID.name())), isPresent());
    }

    public void waitForItemWithStatusToBecomeValid(final QuoteOptionItemStatus status) {
        assertThat(entityFinder.find(LINE_ITEM_CLASS).with(field("status").containsText(status.getDescription())).with(field("validity").containsText(LineItemValidationResultDTO.Status.VALID.name())), isPresent());
    }

    public void waitForAnItemToBecomeValid() {
        waitForALineItem(LineItemValidationResultDTO.Status.VALID);
    }

    public void waitForAnItemToBecomePending() {
        waitForALineItem(LineItemValidationResultDTO.Status.PENDING);
    }

    public void waitForAnItemToBecomeInvalid() {
        waitForALineItem(LineItemValidationResultDTO.Status.INVALID);
    }

    private void waitForALineItem(LineItemValidationResultDTO.Status status) {
        assertThat(entityFinder.find(LINE_ITEM_CLASS).with(field("validity").containsText(status.name())), isPresent());
    }

    public void assertHasRowOfStatus(String status) {
        assertThat(entityFinder.find(LINE_ITEM_CLASS).with(field("status").containsText(status)), isPresent());
    }

    public void assertDoesNotHaveRowOfStatus(String status) {
        assertThat(entityFinder.find(LINE_ITEM_CLASS).with(field("status").containsText(status)), isNotPresent());
    }

    public String getIdForItemWithStatus(String status) {
        return entityFinder.find(LINE_ITEM_CLASS).with(field("status").containsText(status)).doAction().resolve(id()).from(entity());
    }

    public QuoteOptionDetailsTab storeLineItemIds() {
        lineItemIds = newHashSet();
        entityFinder.find(LINE_ITEM_CLASS).forEach(new EntityCommand() {
            @Override
            public void apply(WebElement lineItem) {
                String id2 = doAction().resolve(id()).from(entity());
                lineItemIds.add(id2);
            }
        });

        return this;
    }

    public QuoteOptionDetailsTab storeFilteredLineItemIds() {
        lineItemIds = newHashSet();
        entityFinder.find(LINE_ITEM_CLASS).forEach(new EntityCommand() {
            @Override
            public void apply(WebElement lineItem) {
                if (lineItem.findElement(By.cssSelector("tbody input[type='checkbox']")).isSelected()) {
                    String id2 = doAction().resolve(id()).from(entity());
                    lineItemIds.add(id2);
                }
            }
        });

        return this;
    }

    public void assertItemHasActionRow(String id, IfcAction ifcAction) {
        assertThat(entityFinder.find("lineItemAction").with(entityId("action_" + id)).with(field("action").containsText(viewNameFor(ifcAction))), isPresent());
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

    public QuoteOptionDetailsTab assertFirstFailedItemHasDisabledCheckbox() {
        EntityFinder.Context entity = entityFinder.find(LINE_ITEM_CLASS)
                                                  .with(field("status").containsText("Failed"))
                                                  .with(checkBox().disabled().in(field("checkbox")));
        assertThat(entity, isPresent());
        return this;
    }

    public QuoteOptionDetailsTab clickOnSlideFilter() {
        slideFilter.click();
        return this;
    }

    public QuoteOptionDetailsTab clickOnApplyFilter() {
        applyFilterButton.click();
        return this;
    }

    public QuoteOptionDetailsTab clickOnHideFailedLineItemsCheckbox() {
        hideFailedLineItemsCheckbox.click();
        return this;
    }

    public QuoteOptionDetailsTab waitForLoad() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return firstLineItemTableRow.isDisplayed();
            }
        }, 60);
        return this;
    }

    public QuoteOptionDetailsTab waitForProcessingToComplete() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return !processing.isDisplayed();
            }
        }, 40);
        return this;

    }

    public void setLineItemDisplayFilterTo(final String filter) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                final Select lineItemFilterSelect = new Select(lineItemFilter);
                lineItemFilterSelect.selectByVisibleText(filter);
                waitForProcessingToComplete();
                return true;
            }
        });
    }

    public void clickFetchPrices() {
        fetchPricesButton.click();
    }

    public void waitTillFetchPricesButtonIsEnabled() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return fetchPricesButton.isEnabled();
            }
        });
    }


    public void waitTillCreateOfferButtonIsEnabled() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return createOfferButton.isEnabled();
            }
        });
    }

    public HashSet<String> getLineItemIds() {
        return lineItemIds;
    }

    public void clickBulkConfiguration() {
        bulkConfigurationButton.click();
    }

    public QuoteOptionDetailsTab waitUntilReady() {
        new Wait(driver).until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return bulkConfigurationButton.isDisplayed();
            }
        });
        return this;
    }

    public QuoteOptionDetailsTab clickItemCheckbox(String productName, String site) {
        List<WebElement> foundRows = getTableRowByName(productName, site);
        for (WebElement tableRow : foundRows) {
            tableRow.findElement(By.cssSelector("input[type='checkbox']")).click();
        }
        return null;
    }

    public String getSelectedLineItemId() {
        List<WebElement> foundRows = getTableRows();
        for (WebElement tableRow : foundRows) {
            WebElement checkBox = tableRow.findElement(By.cssSelector("input[type='checkbox']"));
            if (checkBox.isSelected()) {
                return checkBox.getAttribute("value");
            }
        }
        return "";
    }

    public QuoteOptionDetailsTab clickImportButton() {
        importProductButton.click();
        PageFactory.initElements(driver, importProductDialog);
        return this;
    }

    public QuoteOptionDetailsTab assertImportDialogOpen() {
        importProductDialog.assertImportProductDialogDisplayed();
        return this;
    }

    public String getImportDialogActionURL() {
        return importProductDialog.getImportFormActionURI();
    }

    public QuoteOptionDetailsTab clickUploadButton() {
        importProductDialog.clickUploadButton();
        return this;
    }

    public QuoteOptionDetailsTab assetValidationMessageShows() {
        importProductDialog.assertErrorMessageShown();
        return this;
    }

    public QuoteOptionDetailsTab selectCheckBox(String productName, String site) {
        this.clickItemCheckbox(productName, site);
        return this;
    }

    public QuoteOptionDetailsTab assetThatLocateOnGoogleMapsIsNotEnabled() {
        assertFalse(locateOnGoogleMaps.getAttribute("class").contains("enable"));
        return this;
    }

    public QuoteOptionDetailsTab assetThatLocateOnGoogleMapsIsEnabled() {
        assertFalse(locateOnGoogleMaps.getAttribute("class").contains("disable"));
        return this;
    }

    public QuoteOptionDetailsTab clickLocateOnGoogleMapsButton() {
        this.locateOnGoogleMaps.click();
        return this;
    }

    public String getLocateOnGoogleMapsUri() {
        return (String)((JavascriptExecutor)driver).executeScript("return arguments[0].innerHTML;", locateOnGoogleMapsUri);
    }

    public QuoteOptionDetailsTab assertMaxLengthIsShownForCustomerDetails() {
        QuoteOptionDetailsTab.CreateOfferDialog dialog = openCreateOfferDialog();
        dialog.assertDialogTitleIs("Create Offer");
        dialog.enterOfferName("Message Test");
        dialog.enterCustomerDetails("Under 20......");
        dialog.assertMaxLengthMessageIsNotShown();
        dialog.enterCustomerDetails("Just 20.............");
        dialog.assertMaxLengthMessageIsShown();
        dialog.enterCustomerDetails("Just Under 20......");
        dialog.assertMaxLengthMessageIsNotShown();
        return this;
    }

    private class ImportProductDialog {

        @FindBy(id = "importProductDialog")
        private WebElement importProductDialog;

        @FindBy(id = "importProductUpload")
        private WebElement uploadButton;

        @FindBy(id = "importProductUpload")
        private WebElement cancelButton;

        @FindBy(id = "importProductForm")
        private WebElement importProductForm;

        public ImportProductDialog assertImportProductDialogDisplayed() {
            assertTrue(this.importProductDialog.isDisplayed());
            return this;
        }

        public ImportProductDialog clickUploadButton() {
            this.uploadButton.click();
            return this;
        }

        public ImportProductDialog assertErrorMessageShown() {
            assertTrue(this.importProductDialog.findElement(byCss(".error")).isDisplayed());
            return this;
        }

        public String getImportFormActionURI() {
            return new StringBuilder().append(this.importProductForm.getAttribute("action")).append("/").append(getSelectedLineItemId()).toString();
        }
    }


    public class CreateOfferDialog {

        @FindBy(css = "#offerNameText")
        private WebElement offerNameTextField;

        @FindBy(css = "#submitOffer")
        private WebElement submitButton;

        @FindBy(css = "#createOfferForm")
        private WebElement createOfferForm;

        @FindBy(css = "#ui-dialog-title-createOfferDialog")
        private WebElement titleBar;

        @FindBy(css = "#customerOrderRefText")
        private WebElement customerOrderReference;

        @FindBy(css = "#customerOrderRefTextError")
        private WebElement customerOrderReferenceMessage;

        public void enterOfferName(String offerName) {
            offerNameTextField.clear();
            offerNameTextField.sendKeys(offerName);
        }

        public void submit() {
            submitButton.click();
        }

        public boolean formIsDisplayed() {
            return createOfferForm.isDisplayed();

        }

        public void assertDialogTitleIs(String title) {
            assertThat(titleBar.getText(), is(title));
        }

        public void enterCustomerDetails(String customerDetails) {
            customerOrderReference.clear();
            customerOrderReference.sendKeys(customerDetails);
        }

        public void assertMaxLengthMessageIsNotShown() {
            assertThat(customerOrderReferenceMessage.getAttribute("class"), containsString("hidden"));
        }

        public void assertMaxLengthMessageIsShown() {
            assertThat(customerOrderReferenceMessage.getAttribute("class"), not(containsString("hidden")));
        }
    }

    private List<WebElement> getTableRows() {
        List<WebElement> foundRows = newArrayList();
        List<WebElement> tableRows = driver.findElements(By.cssSelector("tr.lineItem"));
        for (WebElement tableRow : tableRows) {
            String productName = tableRow.findElement(By.className("name")).getText();
            String siteName = tableRow.findElement(By.className("siteName")).getText();
            foundRows.add(tableRow);
        }

        return foundRows;
    }

    private List<WebElement> getTableRowByName(String product, String site) {
        List<WebElement> foundRows = newArrayList();
        List<WebElement> tableRows = driver.findElements(By.cssSelector("tr.lineItem"));
        for (WebElement tableRow : tableRows) {
            String productName = tableRow.findElement(By.className("name")).getText();
            String siteName = tableRow.findElement(By.className("siteName")).getText();
            if (productName.equals(product) && siteName.equals(site)) {
                foundRows.add(tableRow);
            }
        }

        return foundRows;
    }

    public List<WebElement> getTableRowByStatus(String pricingStatus) {
        List<WebElement> foundRows = newArrayList();
        List<WebElement> tableRows = driver.findElements(By.cssSelector("tr.lineItem"));
        for (WebElement tableRow : tableRows) {
            String status = tableRow.findElement(By.className("pricingStatus")).getText();
            if (status.equals(pricingStatus)) {
                foundRows.add(tableRow);
            }
        }

        return foundRows;
    }

    public void assertPricingStatus(String product, String site) {
        List<WebElement> foundRows = getTableRowByName(product, site);
        for (WebElement tableRow : foundRows) {
            assertThat(tableRow.findElement(By.className("pricingStatus")).getText(), is("Firm"));
        }
    }

    public void waitUntilAllLineItemsArePriced() {
        final List<WebElement> tableRows = driver.findElements(By.cssSelector("tr.lineItem"));
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                boolean isPriced = false;
                for (WebElement tableRow : tableRows) {
                    if (!tableRow.findElement(By.className("pricingStatus")).getText().equals("Not Priced")) {
                        isPriced = true;
                    } else {
                        return false;
                    }
                }
                return isPriced;
            }
        });
    }

    public void waitUntilAlineItemHasBeenSelected() {
        final List<WebElement> tableRows = driver.findElements(By.cssSelector("tr.lineItem"));
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                boolean isSelected = false;
                for (WebElement tableRow : tableRows) {
                    if (tableRow.findElement(By.cssSelector("tbody input[type='checkbox']")).isSelected()) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return isSelected;
            }
        });
    }

}
