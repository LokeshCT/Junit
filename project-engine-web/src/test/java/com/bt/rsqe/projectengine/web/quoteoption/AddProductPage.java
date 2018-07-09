package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.ProjectEngineWebEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.bt.rsqe.seleniumsupport.liftstyle.WebPage;
import com.bt.rsqe.utils.Environment;
import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import javax.annotation.Nullable;
import java.util.List;

import static com.bt.rsqe.projectengine.web.Selectors.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class AddProductPage extends SeleniumWebDriverTestContext {

    private static final String PAGE_URI_TEMPLATE = "%s://%s:%d/rsqe/customers/%s/contracts/%s/projects/%s/quote-options/%s/add-product";

    @FindBy(css = "#contractTerm")
    private WebElement contractTermsSelect;
    @FindBy(css = ".projectId")
    private WebElement projectId;
    @FindBy(css = ".quoteOptionId")
    private WebElement quoteOptionId;
    @FindBy(css = "#siteTable")
    private WebElement siteTable;
    @FindBy(css = "#categoryGroupFilter")
    private WebElement productCategoryGroup;
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
    @FindBy(id = "continue-to-quote-details")
    private WebElement continueToQuoteDetailsButton;
    @FindBy(id = "productCounter")
    private WebElement productCounter;
    @FindBy(css=".siteLine .site")
    private List<WebElement> siteNames;

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
    //'Do Create' button in SQE mock page
    @FindBy(css = ".submitOV")
    private WebElement doCreateButton;
    @FindBy(css = "input.invalid-country-for-product")
    private WebElement countryInvalidSiteCheckbox;
    @FindBy(className = "product")
    private WebElement productDropDownList;
    @FindBy(tagName = "option")
    private WebElement productInDropDownList;
    @FindBy(className = "ModifyProducts")
    private WebElement modifyTab;
    @FindBy(id = "Modify-product")
    private WebElement modifyDiv;
    @FindBy(className = "MigrateProducts")
    private WebElement migrateTab;
    @FindBy(id = "Migrate-product")
    private WebElement migrateDiv;
    @FindBy(className = "AddProducts")
    private WebElement addProductTab;
    @FindBy(id = "Add-product")
    private WebElement addProductDiv;
    @FindBy(id = "importProduct")
    private WebElement importProduct;
    @FindBy(id = "creating-product-spinner")
    private WebElement creatingProductSpinner;
    @FindBy(id = "bulkConfigurationButton")
    private WebElement bulkConfigurationButton;

    private WebDriver driver;

    public AddProductPage(WebDriver driver) {
        super(driver);
        this.driver = driver;
    }

    public static AddProductPage navigateToAddProductsPage(WebDriver browser, String customerId, String contractId, String projectId, String quoteOptionId, String token) {
        new WebPage(browser, uri(customerId, contractId, projectId, quoteOptionId), token).goTo();
        return addProductsPage(browser);
    }

    private static AddProductPage addProductsPage(WebDriver driver) {
        AddProductPage page = new AddProductPage(driver);
        PageFactory.initElements(driver, page);
        return page;
    }

    public AddProductPage assertDisplayed() {
        waitForVisible(siteTable);
        assertThat(siteTable.isDisplayed(), is(true));
        return this;
    }

    public AddProductPage assertThatItemsCounterWasIncremented(final int quoteLineItemsCounter) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return Integer.parseInt(productCounter.getText()) > quoteLineItemsCounter;
            }
        });
        return this;
    }

    public int getQuoteLineItemCounterValue() {
        return Integer.parseInt(productCounter.getText());
    }

    public AddProductPage chooseCategoryGroup(final String categoryGroup) {
        return selectItem(categoryGroup, productCategoryGroup);
    }

    public AddProductPage chooseCategory(final String categoryName) {
        return selectItem(categoryName, productCategory);
    }

    public AddProductPage chooseProduct(final String productName) {
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

    private AddProductPage selectItem(final String item, WebElement select) {
        final Select selector = new Select(select);

        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                selector.selectByValue("");
                selector.selectByVisibleText(item);
                return true;
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

    public void assertNumberOfSelectableProductsInDropDown(int numberOfSelectableProducts) {
        Select selectBox = new Select(productDropDownList);
        assertEquals(numberOfSelectableProducts, selectBox.getOptions().size());
    }

    private void addProduct() {
        submitButton.click();
    }

    public AddProductPage clickAdd() {
        addProduct();
        return this;
    }

    public AddProductPage clickAddAndWait() {
        addProduct();

        // Wait for it to be shown (just a 1 sec check, it should be instant)
        try{
            waitUntil(new Predicate<WebDriver>() {
                @Override
                public boolean apply(@Nullable WebDriver input) {
                    return creatingProductSpinner.isDisplayed();
                }
            }, 1);
        }catch (TimeoutException te){
            // Sometimes the dialog will show and hide quicker than we can check
        }

        // Wait for it not to be shown again
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return !creatingProductSpinner.isDisplayed();
            }
        });
        return this;
    }

    public void selectCancel() {
        cancelButton.click();
    }

    public void selectContinueToQuoteDetailsPage() {
        continueToQuoteDetailsButton.click();
    }

    public AddProductPage assertContractTermListIsPresentWithDefaultValue(String defaultValue) {
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

    public void unselectAll(){
        for (WebElement checkbox : listOfSiteCheckboxes) {
            if(checkbox.isSelected()){
                checkbox.click();
            }
        }
    }

    public AddProductPage clickFirstCountryInvalidSite() {
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

    public void navigateToModifyCeaseTab() {
        navigateToTab(modifyTab, modifyDiv);
    }

    public void navigateToMigrateTab() {
        navigateToTab(migrateTab, migrateDiv);
    }

    public void navigateToAddProductTab() {
        navigateToTab(addProductTab, addProductDiv);
    }

    private void navigateToTab(final WebElement tab, final WebElement container) {
        tab.click();
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return container.isDisplayed();
            }
        });
    }

    public void assertHasNumberOfSites(int expectedNumberOfSites) {
        assertThat(driver.findElements(byCss(".siteLine")).size(), is(expectedNumberOfSites));
    }

    private static String uri(String customerId, String contractId, String projectId, String quoteOptionId) {
        ProjectEngineWebConfig configuration = ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig();
        return String.format(PAGE_URI_TEMPLATE,
                             configuration.getApplicationConfig().getScheme(),
                             configuration.getApplicationConfig().getHost(),
                             configuration.getApplicationConfig().getPort(),
                             customerId,
                             contractId,
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

    public AddProductPage assertThatSubmitButtonIsEnabled(final boolean condition) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return condition == submitButton.isEnabled();
            }
        });
        assertThat(submitButton.isEnabled(), is(condition));
        return this;
    }

    public AddProductPage assertThatPrerequisiteUrlIsVisible(final boolean condition) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return condition == complianceCheckPanel.isDisplayed();
            }
        }, 5);
        assertThat(complianceCheckPanel.isDisplayed(), is(condition));
        return this;
    }

    public AddProductPage assertThatCountryListIsNotDisplayed() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return !countryFilter.isDisplayed();
            }
        }, 5);
        assertThat(countryFilter.isDisplayed(), is(false));
        return this;
    }

    public AddProductPage customerAgreesToComply() {
        complianceCheckBox.click();
        return this;
    }

    public AddProductPage assertErrorMessagePresent(String message) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return errorMessage.isDisplayed();
            }
        });

        assertThat(errorMessage.getText(), is(message));
        return this;
    }

    public void selectSiteByName(String siteName) {
        for (WebElement name : siteNames) {
            if(name.getText().equals(siteName)){
                WebElement checkBox = name.findElement(By.xpath("..")).findElement(By.cssSelector("input[type='checkbox']"));
                if(!checkBox.isSelected()){
                    checkBox.click();
                }
            }
        }
    }

    public AddProductPage assetThatCountrySelectIsReset() {
        Select countrySelectBox = new Select(countryFilter);
        assertThat(countrySelectBox.getFirstSelectedOption().getText(), is("--Please Select--"));
        return this;
    }

    public AddProductPage assertConfigureProductButtonIsDisabled() {
        assertTrue(bulkConfigurationButton.getAttribute("class").contains("disabled"));
        return this;
    }

    public AddProductPage assertConfigureProductButtonIsEnabled() {
        assertFalse(bulkConfigurationButton.getAttribute("class").contains("disabled"));
        return this;
    }
}