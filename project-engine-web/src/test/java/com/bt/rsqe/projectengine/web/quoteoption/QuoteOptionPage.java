package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.ProjectEngineWebEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.projectengine.web.quoteoption.pricing.QuoteOptionPricingTab;
import com.bt.rsqe.projectengine.web.quoteoptionorders.QuoteOptionOrdersTab;
import com.bt.rsqe.seleniumsupport.liftstyle.Wait;
import com.bt.rsqe.seleniumsupport.liftstyle.WebPage;
import com.bt.rsqe.utils.Environment;
import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.annotation.Nullable;

import static com.bt.rsqe.web.WaitingWebElement.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class QuoteOptionPage extends WebPage {

    @FindBy(css = "#breadCrumb span")
    private WebElement breadCrumb;

    @FindBy(css = "#newLineItem")
    private WebElement addProductButton;

    @FindBy(css = ".products")
    private WebElement productsList;

    @FindBy(css = "#QuoteOptionDetailsTab")
    private WebElement quoteOptionDetailsTabElement;

    @FindBy(css = "#QuoteOptionPricingTab")
    private WebElement quoteOptionPricingTabElement;

    @FindBy(css = "#QuoteOptionOffersTab")
    private WebElement quoteOptionOffersTabElement;

    @FindBy(css = "#OrdersTab")
    private WebElement quoteOptionOrdersTabElement;

    @FindBy(css = "#bulkUpload")
    private WebElement bulkUploadButton;

    @FindBy(css = "#downloadBulkTemplate")
    private WebElement downloadBulkTemplate;

    @FindBy(className = "ui-widget-overlay")
    private WebElement uiWidgetOverlay;

    @FindBy(className = "dataTables_empty")
    private WebElement emptyDataTable;

    @FindBy(css = "#selectAll")
    private WebElement selectAllCheckBox;

    @FindBy(css = "#fetchPrices")
    private WebElement fetchPriceButton;

    private static final String PAGE_URI_TEMPLATE = "%s://%s:%d/rsqe/customers/%s/contracts/%s/projects/%s/quote-options/%s";

    private QuoteOptionDetailsTab quoteOptionDetailsTab;
    private QuoteOptionOffersTab quoteOptionOffersTab;
    private QuoteOptionPricingTab quoteOptionPricingTab;
    private QuoteOptionOrdersTab quoteOptionOrdersTab;
    private BulkUploadDialog bulkUploadDialog;
    private ContractPage contractPage;
    private WebDriver browser;
    private final Wait wait;

    public static QuoteOptionPage navigateToQuoteOptionPage(WebDriver browser, String customerId, String contractId, String projectId, String quoteOptionId, String token) {
        QuoteOptionPage page = new QuoteOptionPage(browser, customerId, contractId, projectId, quoteOptionId, token);
        page.goTo();
        page.initWebElements();
        return page;
    }

    public static QuoteOptionPage quoteOptionPageFromRedirect(WebDriver browser) {
        QuoteOptionPage page = new QuoteOptionPage(browser, "", "", "", "", "");
        page.initWebElements();
        return page;
    }

    private QuoteOptionPage(WebDriver browser, String customerId, String contractId, String projectId, String quoteOptionId, String token) {
        super(browser, uri(customerId, contractId, projectId, quoteOptionId), token);
        this.browser = browser;
        wait = new Wait(driver);
    }

    private void initWebElements() {
        PageFactory.initElements(driver, this);
        quoteOptionDetailsTab = PageFactory.initElements(browser, QuoteOptionDetailsTab.class);
        quoteOptionOffersTab = PageFactory.initElements(browser, QuoteOptionOffersTab.class);
        quoteOptionPricingTab = PageFactory.initElements(browser, QuoteOptionPricingTab.class);
        quoteOptionOrdersTab = PageFactory.initElements(browser, QuoteOptionOrdersTab.class);
        contractPage = PageFactory.initElements(browser, ContractPage.class);
        bulkUploadDialog = new BulkUploadDialog(browser);
        PageFactory.initElements(browser, bulkUploadDialog);
        wait.until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                try{
                    return quoteOptionPricingTabElement.isDisplayed();
                }catch(UnhandledAlertException e){
                    if("Modal dialog present".equals(e.getMessage())){
                        return false;
                    }else{
                        throw(e);
                    }
                }
            }
        });
    }

    public ContractPage contractForm() {
        return contractPage;
    }

    public void assertIsForQuoteOption(String quoteOptionName) {
        assertThat(waitOn(breadCrumb).getText(), containsString(quoteOptionName));
    }

    // FIXME Hugh 27/3/12 this should wait until quoteOptionDetailsTabElement is rendered, then return QuoteOptionDetailsTab
    public void clickDetailsTab() {
        wait.until(ExpectedConditions.visibilityOf(quoteOptionDetailsTabElement));
        quoteOptionDetailsTabElement.click();
    }

    public QuoteOptionDetailsTab clickDetailsTabAndWait() {
        quoteOptionDetailsTabElement.click();
        return detailsTab().waitUntilReady();
    }

    public void clickPriceTabAndWait() {
        quoteOptionPricingTabElement.click();
        pricingTab().waitForLoad();
    }

    // FIXME Hugh 27/3/12 this should wait until quoteOptionOffersTabElement is rendered, then return QuoteOptionOffersTab
    public QuoteOptionOffersTab clickOffersTab() {
        quoteOptionOffersTabElement.click();
        return quoteOptionOffersTab;
    }

    public QuoteOptionOffersTab clickOffersTabAndWait() {
        quoteOptionOffersTabElement.click();
        offersTab().waitForRowToAppear();
        return quoteOptionOffersTab;
    }

    public QuoteOptionOrdersTab clickOrdersTab() {
        quoteOptionOrdersTabElement.click();
        ordersTab().waitForRowToAppear();
        return quoteOptionOrdersTab;
    }

    public QuoteOptionDetailsTab detailsTab() {
        return quoteOptionDetailsTab;
    }

    public QuoteOptionOffersTab offersTab() {
        return quoteOptionOffersTab;
    }

    public QuoteOptionPricingTab pricingTab() {
        return quoteOptionPricingTab;
    }

    public QuoteOptionOrdersTab ordersTab() {
        return quoteOptionOrdersTab;
    }

    public static String uri(String customerId, String contractId, String projectId, String quoteOptionId) {
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

    public void assertHasQuoteOptionBreadCrumb() {
        assertThat(breadCrumb.findElement(By.tagName("a")).getText(), containsString("Quote Options"));
    }

    public void navigateBackToProjectPage() {
        breadCrumb.findElement(By.tagName("a")).click();
    }

    public boolean isOrderTabPresent() {
        try {
            quoteOptionOrdersTabElement.isDisplayed();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public BulkUploadDialog clickBulkUploadButton() {
        bulkUploadButton.click();
        return bulkUploadDialog;
    }

    public void assertPresenceOfOverlay() {
        assertTrue(uiWidgetOverlay.isEnabled());
    }

    public void assertHasDiscountStatus(String productName, LineItemDiscountStatus discountStatus) {
        quoteOptionDetailsTab.assertHasDiscountStatus(productName, discountStatus);
    }

     public boolean isOffersTabPresent() {
        try {
            quoteOptionOffersTabElement.isDisplayed();
            return true;
        } catch (Exception e) {
            return false;
}
    }

    public boolean detailsTabDataTableIsEmpty() {
        return emptyDataTable.isDisplayed();
    }

    public void selectAllLineItemsCheckBox() {
        selectAllCheckBox.click();
    }

    public void clickCalculatePriceButton() {
        fetchPriceButton.click();
    }
}
