package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.ProjectEngineWebEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.bt.rsqe.seleniumsupport.liftstyle.WebPage;
import com.bt.rsqe.utils.Environment;
import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import javax.annotation.Nullable;
import java.util.List;

import static com.bt.rsqe.projectengine.web.Selectors.*;
import static com.bt.rsqe.seleniumsupport.liftstyle.finders.OptionFinder.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class ModifyCeaseProductPage extends SeleniumWebDriverTestContext {

    private static final String PAGE_URI_TEMPLATE = "%s://%s:%d/rsqe/customers/%s/projects/%s/quote-options/%s/add-product";

    @FindBy(css = "#contractTerm")
    private WebElement contractTermsSelect;
    @FindBy(css = ".projectId")
    private WebElement projectId;
    @FindBy(css = ".quoteOptionId")
    private WebElement quoteOptionId;
    @FindBy(css = "#siteTable")
    private WebElement siteTable;
    @FindBy(css = "#categoryFilter")
    private WebElement productCategory;
    @FindBy(css = ".product")
    private WebElement productCode;
    @FindBy(css = "#countryFilter")
    private WebElement countryFilter;
    @FindBy(css = ".submit")
    private WebElement submitButton;
    @FindBy(css = "#complianceCheckPanel")
    private WebElement complianceCheckPanel;
    @FindBy(css = "#complianceCheckBox")
    private WebElement complianceCheckBox;
    @FindBy(css = ".cancel")
    private WebElement cancelButton;
    @FindBy(id = "productCounter")
    private WebElement productCounter;
    @FindBy(id = "rrpPriceBookSelect")
    private WebElement rrpPriceBook;
    @FindBy(id = "ptpPriceBookSelect")
    private WebElement ptpPriceBook;
    @FindBy(id = "commonError")
    private WebElement errorMessage;
    @FindBy(css = ".siteLine input[type='checkbox']")
    private List<WebElement> listOfSiteCheckboxes;
    @FindBy(css = "input[type='radio']")
    private List<WebElement> listOfOrderType;
    @FindBy(css = ".submitOV")
    private WebElement doCreateButton;
    @FindBy(css = "input.invalid-country-for-product")
    private WebElement countryInvalidSiteCheckbox;
    @FindBy(id = "continue-to-quote-details")
    private WebElement continueToQuoteDetailsButton;

    private WebDriver driver;

    public ModifyCeaseProductPage(WebDriver driver) {
        super(driver);
        this.driver = driver;
    }

    public static ModifyCeaseProductPage navigateToAddProductsPage(WebDriver browser, String customerId, String projectId, String quoteOptionId, String token) {
        new WebPage(browser, uri(customerId, projectId, quoteOptionId), token).goTo();
        return modifyCeaseProductPage(browser);
    }

    private static ModifyCeaseProductPage modifyCeaseProductPage(WebDriver driver) {
        ModifyCeaseProductPage page = new ModifyCeaseProductPage(driver);
        PageFactory.initElements(driver, page);
        return page;
    }

    public ModifyCeaseProductPage assertDisplayed() {
        waitForVisible(siteTable);
        assertThat(siteTable.isDisplayed(), is(true));
        return this;
    }

    public ModifyCeaseProductPage assertNotDisplayed() {
        waitForVisible(siteTable);
        assertThat(siteTable.isDisplayed(), is(false));
        return this;
    }

    public ModifyCeaseProductPage assertProductIdIs(String projectId) {
        assertThat(this.projectId.getAttribute("value"), is(projectId));
        return this;
    }

    public ModifyCeaseProductPage assertQuoteOptionIdIs(String quoteOptionId) {
        assertThat(this.quoteOptionId.getAttribute("value"), is(quoteOptionId));
        return this;
    }

    public ModifyCeaseProductPage assertProductPresent(String id, String name) {
        given(productCode).assertPresenceOf(option(id, name));
        return this;
    }

    public ModifyCeaseProductPage chooseCategory(final String categoryName) {
        final Select categorySelector = new Select(productCategory);

        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                categorySelector.selectByValue("");
                categorySelector.selectByVisibleText(categoryName);
                return true;
            }
        });

        return this;
    }

    public int getQuoteLineItemCounterValue() {
        return Integer.parseInt(productCounter.getText());
    }

    public ModifyCeaseProductPage chooseProduct(final String productName) {
        final Select productSelector = new Select(productCode);

        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                productSelector.selectByValue("");
                productSelector.selectByVisibleText(productName);
                return tryFor(2, countryFilterOrComplianceCheckOrSubmitButtonDisplayed());
            }
        });

        return this;
    }

    private boolean tryFor(int timeInSeconds, Predicate<WebDriver> predicate) {
        try {
            waitUntil(predicate, timeInSeconds);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Predicate<WebDriver> countryFilterOrComplianceCheckOrSubmitButtonDisplayed() {
        return new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return submitButton.isEnabled() || countryFilter.isDisplayed() || complianceCheckPanel.isDisplayed();
            }
        };
    }

    public void chooseProductByValue(String productSCode) {
        Select productSelector = new Select(this.productCode);
        productSelector.selectByValue(productSCode);
    }

    public void chooseContractTerm(String contractTerm) {
        Select selectBox = new Select(contractTermsSelect);
        selectBox.selectByVisibleText(contractTerm);
    }

    public void submit() {
        submitButton.click();
    }

    public void selectCancel() {
        cancelButton.click();
    }

    public void selectContinueToQuoteDetailsPage() {
        continueToQuoteDetailsButton.click();
    }

    public ModifyCeaseProductPage assertThatItemsCounterWasIncremented(final int quoteLineItemsCounter) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return Integer.parseInt(productCounter.getText()) > quoteLineItemsCounter;
            }
        });
        return this;
    }

    public ModifyCeaseProductPage assertContractTermListIsPresentWithDefaultValue(String defaultValue) {
        assertTrue(contractTermsSelect.isDisplayed());
        WebElement contractTermSelectedElement = contractTermsSelect.findElement(new By.ById(defaultValue));
        assertNotNull(contractTermSelectedElement);
        assertTrue(contractTermSelectedElement.isSelected());
        return this;
    }

    public void selectFirstSite() {
        selectSite(0);
    }

    public void unselectFirstSite() {
        unselectSite(0);
    }

    public ModifyCeaseProductPage clickFirstCountryInvalidSite() {
        countryInvalidSiteCheckbox.click();
        return this;
    }

    public void selectSite(int index) {
        if (!listOfSiteCheckboxes.get(index).isSelected()) {
            listOfSiteCheckboxes.get(index).click();
        }
    }

    public void unselectSite(int index) {
        if (listOfSiteCheckboxes.get(index).isSelected()) {
            listOfSiteCheckboxes.get(index).click();
        }
    }

    public void assertOrderTypeDisplayed() {
        assertThat(listOfOrderType.size(), is(1));
        assertTrue(listOfOrderType.get(0).isSelected());
    }

    public void clickDoCreateInSQEStub() {
        doCreateButton.click();
    }

    public void selectCountry(String country) {
        Select countrySelectBox = new Select(countryFilter);
        countrySelectBox.selectByVisibleText(country);
    }

    public void assertHasNumberOfSites(int expectedNumberOfSites) {
        assertThat(driver.findElements(byCss(".siteLine")).size(), is(expectedNumberOfSites));
    }

    private static String uri(String customerId, String projectId, String quoteOptionId) {
        ProjectEngineWebConfig configuration = ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig();
        return String.format(PAGE_URI_TEMPLATE,
                             configuration.getApplicationConfig().getScheme(),
                             configuration.getApplicationConfig().getHost(),
                             configuration.getApplicationConfig().getPort(),
                             customerId,
                             projectId,
                             quoteOptionId);
    }

    public void selectAllCountries() {
        selectCountry("All Countries");
    }

    private void waitForVisible(final WebElement selector) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return selector.isDisplayed();
            }
        });
    }

    public ModifyCeaseProductPage assertThatSubmitButtonIsEnabled(final boolean condition) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return condition == submitButton.isEnabled();
            }
        });
        assertThat(submitButton.isEnabled(), is(condition));
        return this;
    }

    public ModifyCeaseProductPage assertThatPrerequisiteUrlIsVisible(final boolean condition) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return condition == complianceCheckPanel.isDisplayed();
            }
        }, 5);
        assertThat(complianceCheckPanel.isDisplayed(), is(condition));
        return this;
    }

    public ModifyCeaseProductPage assertThatCountryListIsNotDisplayed() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return !countryFilter.isDisplayed();
            }
        }, 5);
        assertThat(countryFilter.isDisplayed(), is(false));
        return this;
    }

    public ModifyCeaseProductPage customerAgreesToComply() {
        complianceCheckBox.click();
        return this;
    }

    public ModifyCeaseProductPage assertErrorMessagePresent(String message) {
        assertThat(errorMessage.isDisplayed(), is(true));
        assertThat(errorMessage.getText(), is(message));
        return this;
    }
}
