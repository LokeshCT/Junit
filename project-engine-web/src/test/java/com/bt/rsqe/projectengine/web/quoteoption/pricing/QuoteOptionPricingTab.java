package com.bt.rsqe.projectengine.web.quoteoption.pricing;

import com.bt.rsqe.projectengine.web.quoteoption.PaginatedTab;
import com.bt.rsqe.web.entityfinder.CriteriaMessage;
import com.bt.rsqe.web.entityfinder.EntityCommand;
import com.bt.rsqe.web.entityfinder.EntityFinder;
import com.bt.rsqe.web.entityfinder.functions.OptionalEntityChildWebElementResolver;
import com.bt.rsqe.web.entityfinder.functions.WebElementPredicate;
import com.google.common.base.Predicate;
import org.hamcrest.core.Is;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.Selectors.*;
import static com.bt.rsqe.web.entityfinder.EntityMatchers.*;
import static com.bt.rsqe.web.entityfinder.functions.Functions.*;
import static com.google.common.base.Strings.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class QuoteOptionPricingTab extends PaginatedTab {

    private final static String PRICE_LINE = "priceLine";
    private final static String PRODUCT_GROUP = "product_group";

    @FindBy(css = "#priceLines_wrapper")
    private WebElement priceLinesContainer;

    @FindBy(css = "#persistDiscounts")
    private WebElement saveButton;

    @FindBy(css = "#priceLines_length select")
    private WebElement selectFilter;

    @FindBy(css = ".filterPanel")
    private WebElement filterPanel;

    @FindBy(css = "#exportPricingSheet")
    private WebElement exportPricingSheet;

    @FindBy(css = "#productFilter")
    private WebElement productFilter;

    @FindBy(css = "#countryFilter")
    private WebElement countryFilter;

    @FindBy(id = "slideFilter")
    private WebElement slideFilter;

    @FindBy(css = "#applyFilterButton")
    private WebElement applyFilterButton;

    @FindBy(css = "#clearFilterButton")
    private WebElement clearFilterButton;

    @FindBy(css = "#unsavedDiscounts")
    private WebElement unsavedDiscounts;

    @FindBy(css = "#priceLines_processing")
    private WebElement priceLinesProcessing;

    @FindBy(css = "#discardDiscounts")
    private WebElement discardButton;

    @FindBy(css = "#exportPricingSheet")
    private WebElement exportPricingSheetLink;

    @FindBy(css = "#selectAll")
    private WebElement selectAllLink;

    @FindBy(css = "#bulkDiscountOneTimePercent")
    private WebElement bulkDiscountOneTimePercent;

    @FindBy(css = "#bulkDiscountOneTimeNett")
    private WebElement bulkDiscountOneTimeNett;

    @FindBy(css = "#bulkDiscountRecurringPercent")
    private WebElement bulkDiscountRecurringPercent;

    @FindBy(css = "#bulkDiscountRecurringNett")
    private WebElement bulkDiscountRecurringNett;

    @FindBy(css = "#applyBulkDiscount")
    WebElement applyBulkDiscountButton;

    @FindBy(css = "#requestDiscountPopupButton")
    private WebElement requestDiscountButton;

    @FindBy(css = "#unlockPriceLinesButton")
    private WebElement unlockPriceLinesButton;

    @FindBy(xpath = "//th[text()='RRP']")
    private List<WebElement> rrpHeadings;

    @FindBy(css = "#oneTimeGrossTotal")
    private WebElement oneTimeGrossTotal;

    @FindBy(css = "#oneTimeDiscountTotal")
    private WebElement oneTimeDiscountTotal;

    @FindBy(css = "#oneTimeNetTotal")
    private WebElement oneTimeNetTotal;

    @FindBy(css = "#recurringDiscountTotal")
    private WebElement recurringDiscountTotal;

    @FindBy(css = "#recurringGrossTotal")
    private WebElement recurringGrossTotal;

    @FindBy(css = "#recurringNetTotal")
    private WebElement recurringNetTotal;

    @FindBy(css = ".recurringHeading")
    private WebElement totalPriceRecurringHeading;

    @FindBy(css = ".usageHeading")
    private WebElement totalUsageHeading;

    @FindBy(css = "#usageOffNetTotal")
    private WebElement usageOffNetTotal;

    @FindBy(css = "#usageOnNetTotal")
    private WebElement usageOnNetTotal;

    @FindBy(css = "#usageTotal")
    private WebElement usageTotal;

    @FindBy(css = "#pricingDiv #commonError")
    private WebElement errorMessage;

    @FindBy(css = "td.status")
    private WebElement pricingStatus;

    private RequestDiscountDialog requestDiscountDialog;
    private EntityFinder entityFinder;
    private String priceLineRowTracker;
    private WebDriver driver;


    public QuoteOptionPricingTab(WebDriver driver) {
        super(driver);
        this.driver = driver;
        entityFinder = new EntityFinder(driver);
    }

    public void assertPresenceOfPriceLinesTable() {
        assertTrue(priceLinesContainer.isDisplayed());
    }

    public void setPriceLineFilterTo(String filter) {
        Select filterComboBox = new Select(selectFilter);
        filterComboBox.selectByVisibleText(filter);
        try { // mani: driver should wait for the refresh. Since the action takes place in the same page
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public QuoteOptionPricingTab assertNoPriceLineRows() {
        assertThat(entityFinder.find(PRICE_LINE).waitNoLongerThan(100), isNotPresent());
        return this;
    }

    public QuoteOptionPricingTab assertPriceLineRows(Integer expectedNumberOfPriceLines) {
        assertThat(entityFinder.find(PRICE_LINE).count(expectedNumberOfPriceLines), isPresent());
        assertThat(driver.findElements(byCss("." + PRICE_LINE)).size(), is(expectedNumberOfPriceLines));
        return this;
    }


    public QuoteOptionPricingTab assertOneOrMorePriceLines() {
        assertThat(entityFinder.find(PRICE_LINE), isPresent());
        return this;
    }

    public QuoteOptionPricingTab assertThatPriceLinesAreNotReadOnly() {
        assertThat(driver.findElements(byCss("." + PRICE_LINE + ".readOnly")).size(), is(0));
        return this;
    }
    public QuoteOptionPricingTab assertThatPriceLinesAreReadOnly() {
        assertThat(driver.findElements(byCss("." + PRICE_LINE + ".readOnly")).size(), is(52));
        return this;
    }

    public QuoteOptionPricingTab changeDiscountField(String discountValue, String priceLineType) {
        changeDiscounts(discountValue, priceLineType + "_discount");
        return this;
    }

    public QuoteOptionPricingTab changeNetTotalField(String netTotalValue, String priceLineType) {
        changeDiscounts(netTotalValue, priceLineType + "_netTotal");
        return this;
    }

    public QuoteOptionPricingTab clickOnDiscountField(String priceLineType) {
        clickOnDiscount(priceLineType, "_discount");
        return this;
    }

    public QuoteOptionPricingTab clickOnNetTotalField(String priceLineType) {
        clickOnDiscount(priceLineType, "_netTotal");
        return this;
    }

    private void changeDiscounts(String netTotalValue, String entityField) {
        String priceLineType = entityField.substring(0, entityField.indexOf("_"));
        priceLineRowTracker = entityFinder.find(PRICE_LINE).doAction().resolve(attribute(priceLineType + "_id")).from(entity());
        final EntityFinder.Context priceLine = entityFinder.find(PRICE_LINE).with(
            attribute(priceLineType + "_id", priceLineRowTracker));
        priceLine.doAction().click(on(field(entityField)));
        priceLine.doAction().clearReKeyAndSubmit(textBox().in(field(entityField)), netTotalValue);
        priceLineRowTracker = entityFinder.find(PRICE_LINE).doAction().resolve(attribute(priceLineType + "_id")).from(entity());
    }

    public QuoteOptionPricingTab clickOnDiscount(String priceLineType, String field) {
        final String priceLineFieldId = priceLineType + "_id";
        priceLineRowTracker = entityFinder.find(PRICE_LINE).doAction().resolve(attribute(priceLineFieldId)).from(entity());
        final EntityFinder.Context priceLine = entityFinder.find(PRICE_LINE).with(
            attribute(priceLineFieldId, priceLineRowTracker));
        priceLine.doAction().click(on(field(priceLineType + field)));
        priceLineRowTracker = entityFinder.find(PRICE_LINE).doAction().resolve(attribute(priceLineFieldId)).from(entity());
        return this;
    }

    public QuoteOptionPricingTab assertNetTotalHasValue(String expectedNetTotalValue, String netTotalType) {
        assertFieldValue(netTotalType + "_netTotal", expectedNetTotalValue);
        return this;
    }

    public QuoteOptionPricingTab assertDiscountHasValue(String expectedDiscountValue, String priceLineType) {
        assertFieldValue(priceLineType + "_discount", expectedDiscountValue);
        return this;
    }

    private void assertFieldValue(String fieldName, String expectedValue) {
        String priceLineType = fieldName.substring(0, fieldName.indexOf("_"));
        priceLineRowTracker = entityFinder.find(PRICE_LINE).doAction().resolve(attribute(priceLineType + "_id")).from(entity());
        assertThat(entityFinder.find(PRICE_LINE)
                               .with(attribute(priceLineType + "_id", priceLineRowTracker))
                               .with(field(fieldName).containsText(expectedValue)),
                   isPresent());
    }

    public QuoteOptionPricingTab assertDiscountAndNetTotalFieldsAreHighLighted(String priceLineType) {
        assertThat(entityFinder.find(PRICE_LINE)
                               .with(attribute(priceLineType + "_id", priceLineRowTracker))
                               .with(cssClass(priceLineType + "ChangeDiscount").on(entity())),
                   isPresent());
        return this;
    }

    public QuoteOptionPricingTab assertUserIsPromptedToSavePriceLines() {
        assertTrue("User should be prompted to Save PriceLines", saveButton.isDisplayed());
        return this;
    }

    public QuoteOptionPricingTab savePriceLines() {
        saveButton.click();
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return !unsavedDiscounts.isDisplayed();
            }
        });
        driver.navigate().refresh();
        return this;
    }

    public QuoteOptionPricingTab assertDiscountAndNetTotalFieldsAreNotHighLighted(String priceLineType) {
        assertThat(entityFinder.find(PRICE_LINE)
                               .waitNoLongerThan(100)
                               .with(cssClass(priceLineType + "ChangeDiscount").on(entity())),
                   isNotPresent());
        return this;
    }

    public QuoteOptionPricingTab assertSaveButtonIsDisabled() {
        assertFalse("User should not be prompted to Save PriceLines", saveButton.isDisplayed());
        return this;
    }

    public QuoteOptionPricingTab assertRequestDiscountButtonIsDisabled() {
        assertFalse("User should not be able to request discounts", requestDiscountButton.isEnabled());
        return this;
    }

    public QuoteOptionPricingTab assertRequestDiscountButtonIsEnabled() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return driver.findElement(byCss("#requestDiscountPopupButton")).isEnabled();
            }
        }, 40);
        return this;
    }


    public QuoteOptionPricingTab assertThatUnlockPriceLinesButtonIsEnabled() {
        assertTrue("User should be able to unlock pricelines", unlockPriceLinesButton.isEnabled());
        return this;
    }

    public QuoteOptionPricingTab assertThatUnlockPriceLinesButtonIsDisabled() {
        assertFalse("User should not be able to unlock pricelines", unlockPriceLinesButton.isEnabled());
        return this;
    }

    public QuoteOptionPricingTab assertDefaultDiscountIs(int value) {
        assertThat(entityFinder.find(PRICE_LINE)
                               .with(field("oneTime_discount").containsText(Integer.toString(value)))
                               .with(field("recurring_discount").containsText(Integer.toString(value))),
                   isPresent());
        return this;
    }

    public QuoteOptionPricingTab assertDefaultPriceIs(int value) {
        assertThat(entityFinder.find(PRICE_LINE)
                               .with(field("oneTime_value").containsText(Integer.toString(value)))
                               .with(field("recurring_value").containsText(Integer.toString(value))),
                   isPresent());
        return this;
    }

    public QuoteOptionPricingTab assertSiteNameIs(String site) {
        assertThat(entityFinder.find(PRODUCT_GROUP)
                               .with(field("site").containsText(site)),
                   isPresent());
        return this;
    }

    public QuoteOptionPricingTab assertFilterPanelIsDisplayed() {
        assertTrue(filterPanel.isDisplayed());
        return this;
    }

    public QuoteOptionPricingTab assertProductFilterHasOptions(String... optionTexts) {
        List<String> optionsValues = getOptionTextsFrom(productFilter);
        for (String optionText : optionTexts) {
            assertThat(String.format("Product list doesn't have %s", optionText), optionsValues, hasItem(optionText));
        }
        return this;
    }

    public QuoteOptionPricingTab assertCountryFilterHas(String... countries) {
        List<String> optionValues = getOptionTextsFrom(countryFilter);
        for (String country : countries) {
            assertThat(String.format("Country list doesn't have %s", country), optionValues, hasItem(country));
        }
        return this;
    }

    public QuoteOptionPricingTab assertCountryFilterNotHaving(String... countries) {
        List<String> optionValues = getOptionTextsFrom(countryFilter);
        for (String country : countries) {
            assertThat(String.format("Country list doesn't have %s", country), optionValues, not(hasItem(country)));
        }
        return this;
    }

    private List<String> getOptionTextsFrom(WebElement selectBox) {
        List<WebElement> options = new Select(selectBox).getOptions();
        List<String> elementValues = new ArrayList<String>();
        for (WebElement option : options) {
            elementValues.add(option.getText());
        }
        return elementValues;
    }

    public QuoteOptionPricingTab assertHasFilterButtons() {
        assertTrue(applyFilterButton.isDisplayed());
        assertTrue(clearFilterButton.isDisplayed());
        return this;
    }

    public QuoteOptionPricingTab selectProductNameOnFilterOption(String productName) {
        new Select(productFilter).selectByVisibleText(productName);
        return this;
    }

    public QuoteOptionPricingTab clickOnApplyFilter() {
        applyFilterButton.click();
        return this;
    }

    public QuoteOptionPricingTab waitForLoad() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return !isNullOrEmpty(oneTimeGrossTotal.getText());
            }
        }, 40);
        return this;
    }

    public QuoteOptionPricingTab waitForPricingSummaryToLoad() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return oneTimeNetTotal.getText().length() > 0;
            }
        });
        return this;
    }

    public QuoteOptionPricingTab waitForProcessingToComplete() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return !priceLinesProcessing.isDisplayed();
            }
        });
        return this;
    }

    public QuoteOptionPricingTab assertPriceTableIsDisplayingProduct(String productName) {
        assertPriceTableIsDisplaying(productName, "product");
        return this;
    }

    public QuoteOptionPricingTab assertPriceTableIsDisplayingSite(String siteName) {
        assertPriceTableIsDisplaying(siteName, "site");
        return this;
    }

    public QuoteOptionPricingTab assertPriceTableIsNotDisplayingProduct(String productName) {
        assertPriceTableIsNotDisplaying(productName, "product");
        return this;
    }

    public QuoteOptionPricingTab assertPriceTableIsNotDisplayingSite(String site) {
        assertPriceTableIsNotDisplaying(site, "site");
        return this;
    }

    private void assertPriceTableIsDisplaying(String fieldValue, String fieldName) {
        assertThat(entityFinder.find(PRODUCT_GROUP)
                               .with(field(fieldName).containsText(fieldValue)),
                   isPresent());
    }

    private void assertPriceTableIsNotDisplaying(String fieldValue, String fieldName) {
        assertThat(entityFinder.find(PRODUCT_GROUP)
                               .with(field(fieldName).containsText(fieldValue))
                               .waitNoLongerThan(500),
                   isNotPresent());
    }

    public QuoteOptionPricingTab clickOnClearFilter() {
        clearFilterButton.click();
        return this;
    }

    public QuoteOptionPricingTab assertProductFilterHasSelectedOption(String productName) {
        assertThat(new Select(productFilter).getFirstSelectedOption().getText(), Is.is(productName));
        return this;
    }

    public QuoteOptionPricingTab assertPresenceOfPricelineWith(OptionalEntityChildWebElementResolver... fields) {
        final EntityFinder.Context priceLine = entityFinder.find(PRICE_LINE);
        for (OptionalEntityChildWebElementResolver field : fields) {
            priceLine.with(field);
        }
        assertThat(priceLine, isPresent());
        return this;
    }

    public QuoteOptionPricingTab assertAbsenceOfPriceLineValues(String priceLineType) {
        assertThat(entityFinder.find(PRICE_LINE)
                               .with(attribute(priceLineType + "_id", "id_" + priceLineType + "_onlyPriceLine")),
                   isPresent());
        return this;
    }

    public QuoteOptionPricingTab selectCountryOnFilter(String country) {
        new Select(countryFilter).selectByVisibleText(country);
        return this;
    }

    public QuoteOptionPricingTab clickOnDiscardButton() {
        discardButton.click();
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return !unsavedDiscounts.isDisplayed();
            }
        });
        driver.navigate().refresh();
        return this;
    }

    public QuoteOptionPricingTab clickOnExportPricingSheet() {
        exportPricingSheetLink.click();
        return this;
    }

    public String getExportPricingSheetLink() {
        return exportPricingSheetLink.getAttribute("href");
    }

    public QuoteOptionPricingTab selectAllPriceLines() {
        selectAllLink.click();
        return this;
    }

    public QuoteOptionPricingTab enterBulkDiscountOneTimePercentage(String percentage) {
        bulkDiscountOneTimePercent.sendKeys(percentage);
        return this;
    }

    public QuoteOptionPricingTab enterBulkDiscountRecurringPercentage(String percentage) {
        bulkDiscountRecurringPercent.sendKeys(percentage);
        return this;
    }

    public QuoteOptionPricingTab applyBulkDiscount() {
        applyBulkDiscountButton.click();
        return this;
    }

    public QuoteOptionPricingTab enterBulkDiscountOneTimeNett(String nett) {
        bulkDiscountOneTimeNett.sendKeys(nett);
        return this;
    }

    public QuoteOptionPricingTab enterBulkDiscountRecurringNett(String nett) {
        bulkDiscountRecurringNett.sendKeys(nett);
        return this;
    }

    public QuoteOptionPricingTab clickOnRequestDiscountButtonAndWaitForDialogToLoad() {
        requestDiscountButton.click();
        this.requestDiscountDialog = new RequestDiscountDialog(this.driver);
        PageFactory.initElements(driver, requestDiscountDialog);
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return requestDiscountDialog.isDialogLoaded();
            }
        });
        return this;
    }

    public QuoteOptionPricingTab clickOnRequestDiscountButton() {
        requestDiscountButton.click();
        return this;
    }

    public QuoteOptionPricingTab assertBidManagerList(Map bidManagerList) {
        requestDiscountDialog.assertHasExpectedBidManagerList(bidManagerList);
        return this;
    }

    public QuoteOptionPricingTab assertGroupEmailId(String groupEmailId) {
        requestDiscountDialog.assertHasCorrectGroupEmailId(groupEmailId);
        return this;
    }

    public QuoteOptionPricingTab selectABidManager() {
        this.requestDiscountDialog.selectFirstBidManager();
        return this;
    }

    public QuoteOptionPricingTab submitRequestDiscountApproval() {
        this.requestDiscountDialog.submitRequestDiscountApproval();
        return this;
    }

    public QuoteOptionPricingTab closeRequestDiscountDialog() {
        this.requestDiscountDialog.close();
        return this;
    }

    public QuoteOptionPricingTab assertDiscountRequestSuccessMessage() {
        this.requestDiscountDialog.assertDiscountRequestSuccessMessage();
        return this;
    }

    public QuoteOptionPricingTab assertDiscountRequestNotAllowedMessage() {
        this.requestDiscountDialog.assertDiscountRequestNotAllowedMessage();
        return this;
    }

    public QuoteOptionPricingTab assertDiscountRequestFailedMessage() {
        this.requestDiscountDialog.assertDiscountRequestFailedMessage();
        return this;
    }

    public QuoteOptionPricingTab assertNumberOfRRPColumns(int count) {
        assertThat(rrpHeadings.size(), is(count));
        return this;
    }

    public QuoteOptionPricingTab changeDiscountForAllOneTime(final double percentage) {
        entityFinder.find("priceLine").with(notReadOnly()).forEach(new EntityCommand() {
            @Override
            public void apply(WebElement entity) {
                final String field = "oneTime_discount";
                doAction().click(on(field(field)));
                doAction().clearReKeyAndSubmit(textBox().in(field(field)), String.valueOf(percentage));
            }
        });
        return this;
    }

    private WebElementPredicate notReadOnly() {
        return new WebElementPredicate() {
            @Override
            public boolean apply(@Nullable WebElement input) {
                return !input.getAttribute("class").contains("readOnly");
            }

            @Override
            public void buildCriteriaMessage(CriteriaMessage message) {
                message.add(" Not read only ");
            }
        };
    }

    public QuoteOptionPricingTab assertDiscountCannotBeChangedInAllRows() {
        entityFinder.find("priceLine").forEach(new EntityCommand() {
            @Override
            public void apply(WebElement entity) {
                final String[] fields = {"oneTime_discount", "recurring_discount"};
                String oneTimeIdAttribute = "onetime_id";
                String recurringIdAttribute = "recurring_id";
                String oneTimeId = doAction().resolve(attribute(oneTimeIdAttribute)).from(entity());
                String recurringId = doAction().resolve(attribute(recurringIdAttribute)).from(entity());
                for (String field : fields) {
                    doAction().click(on(field(field)));
                    assertThat(new EntityFinder(driver).find("priceLine")
                                                       .with(attribute(oneTimeIdAttribute, oneTimeId))
                                                       .with(attribute(recurringIdAttribute, recurringId))
                                                       .with(textBox().in(field(field)))
                                                       .waitNoLongerThan(100),
                               isNotPresent());
                }
            }
        });
        return this;
    }

    public QuoteOptionPricingTab changeDiscountForAllRecurring(final double net) {
        entityFinder.find("priceLine").with(notReadOnly()).forEach(new EntityCommand() {
            @Override
            public void apply(WebElement entity) {
                final String field = "recurring_discount";
                doAction().click(on(field(field)));
                doAction().clearReKeyAndSubmit(textBox().in(field(field)), String.valueOf(net));
            }
        });
        return this;
    }

    public QuoteOptionPricingTab assertPricingStatus(final String status) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return status.equals(pricingStatus.getText());
            }
        });

        return this;
    }

    public QuoteOptionPricingTab assertOneTimeGrossTotal(final String grossTotal) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return grossTotal.equals(oneTimeGrossTotal.getText());
            }
        });

        return this;
    }

    public QuoteOptionPricingTab assertOneTimeDiscountTotal(final String discountTotal) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return containsString(discountTotal).matches(oneTimeDiscountTotal.getText());
            }
        });

        return this;
    }

    public QuoteOptionPricingTab assertOneTimeNetTotal(final String netTotal) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return netTotal.equals(oneTimeNetTotal.getText());
            }
        });

        return this;
    }

    public QuoteOptionPricingTab assertRecurringGrossPrice(final String grossTotal) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return grossTotal.equals(recurringGrossTotal.getText());
            }
        });
        return this;
    }

    public QuoteOptionPricingTab assertRecurringDiscountPrice(final String discountTotal) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return containsString(discountTotal).matches(recurringDiscountTotal.getText());
            }
        });
        return this;
    }

    public QuoteOptionPricingTab assertRecurringNetPrice(final String netTotal) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return netTotal.equals(recurringNetTotal.getText());
            }
        });
        return this;
    }

    public QuoteOptionPricingTab clickOnSlideFilter() {
        slideFilter.click();
        return this;
    }

    public QuoteOptionPricingTab openPricingRecurringSummary() {
        totalPriceRecurringHeading.click();
        return this;
    }

    public QuoteOptionPricingTab openPricingUsageSummary() {
        totalUsageHeading.click();
        return this;
    }

    public QuoteOptionPricingTab assertUsageOffNetTotal(final String offNetTotal) {

        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return offNetTotal.equals(usageOffNetTotal.getText());
            }
        });

        return this;
    }

    public QuoteOptionPricingTab assertUsageOnNetTotal(final String onNetTotal) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return onNetTotal.equals(usageOnNetTotal.getText());
            }
        });

        return this;
    }

    public QuoteOptionPricingTab assertUsageTotal(final String total) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return total.equals(usageTotal.getText());
            }
        });

        return this;
    }

    public QuoteOptionPricingTab assertErrorMessageShown(String message) {
        assertThat(errorMessage.getText(), is(message));
        return this;
    }

    public QuoteOptionPricingTab clickOnUnlockPriceLines() {
        unlockPriceLinesButton.click();
        return this;
    }

    public void assertAllPriceLinesEnabled() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return driver.findElements(byCss("." + PRICE_LINE + ".readOnly")).size() == 0;
            }
        });

    }

    public int countCurrentPriceLines() {
        return driver.findElements(By.cssSelector("tr.priceLine")).size();
    }

    public void assertPriceFor(String cellValue, String priceTypeClass, String price, int expectedCount) {
        List<WebElement> foundRows = getTableRow(cellValue);
        if (foundRows.isEmpty()) {
            fail(String.format("Could not find row with cell value '%s'", cellValue));
        }
        int matchCount = elementMatchCount(priceTypeClass, price, foundRows);
        if (matchCount != expectedCount) {
            fail(String.format("Found %s row(s) with cellvalue '%s' priceType '%s' and price '%s'. Expected %s.",
                               matchCount, cellValue, priceTypeClass, price, expectedCount));
        }
    }

    public void assertPriceFor(String cellValue, String priceTypeClass, String price) {
        List<WebElement> foundRows = getTableRow(cellValue);
        if (foundRows.isEmpty()) {
            fail(String.format("Could not find row with cell value '%s'", cellValue));
        }
        int matchCount = elementMatchCount(priceTypeClass, price, foundRows);
        if (matchCount == 0) {
            fail(String.format("Could not find row with cellvalue '%s' priceType '%s' and price '%s'", cellValue, priceTypeClass, price));
        }
    }

     public void assertNoPriceFor(String cellValue, String priceTypeClass, String price) {
        List<WebElement> foundRows = getTableRow(cellValue);
        if (!foundRows.isEmpty()) {
            fail(String.format("Could able find row with cell value '%s'", cellValue));
        }
    }

    private int elementMatchCount(String priceTypeClass, String price, List<WebElement> foundRows) {
        int count = 0;
        for (WebElement foundRow : foundRows) {
            if (foundRow.findElement(By.className(priceTypeClass)).getText().equals(price)) {
                count++;
            }
        }
        return count;
    }

    private List<WebElement> getTableRow(String cellValue) {
        List<WebElement> foundRows = newArrayList();
        List<WebElement> tableRows = driver.findElements(By.cssSelector("tr.priceLine"));
        for (WebElement tableRow : tableRows) {
            if (tableRow.findElement(By.className("description")).getText().equals(cellValue)) {
                foundRows.add(tableRow);
            }
        }
        return foundRows;
    }

    public QuoteOptionPricingTab assertCommercialNonStandardEnabled() {
        this.requestDiscountDialog.assertRevenueCheckBoxEnabled();
        return this;
    }

    public QuoteOptionPricingTab clickRevenueRequestCheckBox() {
        this.requestDiscountDialog.clickOnRevenueCheckBox();
         waitUntil(new Predicate<WebDriver>() {
             @Override
             public boolean apply(@Nullable WebDriver input) {
                 return !driver.findElements(byId("revenueTable")).isEmpty();
             }
         });
        return this;
    }

    public void enterProposedRevenue() {
        this.requestDiscountDialog.selectFirstBidManager();
        this.requestDiscountDialog.enterProposedValueAndTriggerMonths("34900", "34");
        this.requestDiscountDialog.clickOnSelectAll();
        this.requestDiscountDialog.clickOnSubmitButton();
        waitUntil(new Predicate<WebDriver>() {
             @Override
             public boolean apply(@Nullable WebDriver input) {
                return !driver.findElement(byId("sendDiscountApprovalMessage")).getText().isEmpty();
             }
         },10);
        this.requestDiscountDialog.assertValidationMessage("Trigger Months should be between 0 to 12.");
        this.requestDiscountDialog.enterProposedValueAndTriggerMonths("34900", "3");
        this.submitRequestDiscountApproval();
    }
}