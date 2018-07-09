package com.bt.rsqe.projectengine.web.projects;

import com.bt.rsqe.ProjectEngineWebEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.projectengine.QuoteOptionContractTerm;
import com.bt.rsqe.projectengine.QuoteOptionCurrency;
import com.bt.rsqe.projectengine.web.quoteoption.CustomerQuoteOptionDialogPage;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionPage;
import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.bt.rsqe.seleniumsupport.liftstyle.WebPage;
import com.bt.rsqe.utils.Environment;
import com.bt.rsqe.web.entityfinder.EntityFinder;
import com.google.common.base.Predicate;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static com.bt.rsqe.web.entityfinder.EntityMatchers.*;
import static com.bt.rsqe.web.entityfinder.functions.Functions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class CustomerProjectPage {

    private static final String PAGE_TITLE = "Customer Project";
    private static final String PAGE_URI_TEMPLATE = "%s://%s:%d/rsqe/customers/%s/projects/%s";
    private static final String QUOTE_OPTION = "quoteOption";
    private static final String PROJECT_NAME = "Project 1";

    private CustomerQuoteOptionDialogPage customerQuoteOptionDialog;
    private CustomerProjectPageDetailsTab details;

    @FindBy(css = "#header img")
    private WebElement header;

    @FindBy(css = "#newQuoteOptionLink")
    private WebElement newQuoteOptionLink;

    @FindBy(css = "a[rel='edit']")
    private List<WebElement> editQuoteOptionLinks;

    @FindBy(css = "a[rel='note']")
    private List<WebElement> noteLinks;

    @FindBy(css = "a[rel='bcmExport']")
    private List<WebElement> bcmExportLinks;

    @FindBy(css = "a[rel='bcmRejectDiscounts']")
    private List<WebElement> bcmRejectDiscountsLinks;

    @FindBy(id = "errorMessage")
    private WebElement errorMessage;

    @FindBy(className = "ui-widget-overlay")
    private WebElement uiWidgetOverlay;

    @FindBy(css = ".note .text")
    private WebElement noteField;

    @FindBy(css = "a[rel='bcmImport']")
    private WebElement bcmImportLink;

    @FindBy(css = "td.id")
    private WebElement quoteId;

    @FindBy(css = "img[title='Delete Quote Option']")
    private List<WebElement> deleteQuoteOptionLinks;

    @FindBy(css = "tr.quoteOption")
    private List<WebElement> quoteOptionRows;

    @FindBy(id = "deleteDialogOkButton")
    private WebElement deleteDialogOkButton;

    private WebDriver browser;
    private ConfirmationDialog confirmationDialog;

    public static CustomerProjectPage customerProjectPageFromRedirect(WebDriver browser) {
        CustomerProjectPage page = new CustomerProjectPage(browser);
        page.initWebElements();
        return page;
    }
    public static CustomerProjectPage navigateToCustomerProjectPage(WebDriver browser, String customerId, String projectId, String token,String scheme,String host,String port) {
           new WebPage(browser, uriToSpecifiedHost(customerId, projectId,scheme,host,port), token).goTo();
           CustomerProjectPage page = new CustomerProjectPage(browser);
           page.initWebElements();
           return page;
       }

    public static CustomerProjectPage navigateToCustomerProjectPage(WebDriver browser, String customerId, String projectId, String token) {
        new WebPage(browser, uri(customerId, projectId), token).goTo();
        CustomerProjectPage page = new CustomerProjectPage(browser);
        page.initWebElements();
        return page;
    }

    private CustomerProjectPage(WebDriver browser) {
        this.browser = browser;
    }

    private void initWebElements() {
        PageFactory.initElements(browser, this);
        customerQuoteOptionDialog = PageFactory.initElements(browser, CustomerQuoteOptionDialogPage.class);
        details = PageFactory.initElements(browser, CustomerProjectPageDetailsTab.class);
        confirmationDialog = PageFactory.initElements(browser, ConfirmationDialog.class);
    }

    public void createNewQuoteOption(String name,
                                     QuoteOptionCurrency currency,
                                     QuoteOptionContractTerm contractTerm) {
        clickNewQuoteOptionLink();
        quoteOptionForm().enterQuoteOptionDetails(name,
                                                  currency.getValue(),
                                                  contractTerm.getDescription());
        quoteOptionForm().submit();
    }

    public CustomerQuoteOptionDialogPage quoteOptionForm() {
        return customerQuoteOptionDialog;
    }

    public CustomerProjectPageDetailsTab detailsTab() {
        return details;
    }

    public void clickNewQuoteOptionLink() {
        newQuoteOptionLink.click();
    }

    public void clickFirstEditQuoteOptionLink() {
        editQuoteOptionLinks.get(0).click();
    }

    public String getQuoteOptionId(String quoteOptionName){
        return browser.findElement(By.xpath("//tr[contains(td[@class=\"name\"], \""+quoteOptionName+"\")]")).getAttribute("id").replace("id_", "");
    }

    public CustomerQuoteOptionDialogPage getOptionsForQuoteOptionLink(int index){
        editQuoteOptionLinks.get(index).click();
        return quoteOptionForm();
    }


    public void assertIsExpectedPage() {
        assertTrue("Page presented: "+browser.getTitle()+" was not "+PAGE_TITLE, browser.getTitle().equals(PAGE_TITLE));
    }

    public void assertHasQuoteOptionBreadcrumb() {
        assertThat(header.getAttribute("alt"), containsString("Quote Options"));
    }

    public void assertDefaultProjectForCustomer(String quoteOptionName, String customerName, String currency) {
        assertIsExpectedPage();
        detailsTab().assertQuoteOptionPresent(quoteOptionName, currency);
    }



    public void assertHasQuoteOptionWithEditButton() {
        EntityFinder entityFinder = new EntityFinder(browser);
        assertThat(entityFinder.find(QUOTE_OPTION).with(image().attribute("alt", "Edit").in(field("actions"))), isPresent());
    }

    public void assertDoesNotHaveQuoteOptionWithEditButton() {
        EntityFinder entityFinder = new EntityFinder(browser);
        assertThat(entityFinder.find(QUOTE_OPTION).waitNoLongerThan(500).with(image().attribute("alt", "Edit").in(field("action"))),
                   isNotPresent());
    }
    private static String uriToSpecifiedHost(String customerId, String projectId,String scheme,String host,String port) {
        Integer portInteger = Integer.parseInt(port);
        return String.format(PAGE_URI_TEMPLATE,
                             scheme,
                             host,
                             portInteger,
                             customerId,
                             projectId);
    }
    private static String uri(String customerId, String projectId) {
        ApplicationConfig applicationConfig = ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class,
                                                                             Environment.env()).getProjectEngineWebConfig().getApplicationConfig();
        return String.format(PAGE_URI_TEMPLATE,
                             applicationConfig.getScheme(),
                             applicationConfig.getHost(),
                             applicationConfig.getPort(),
                             customerId,
                             projectId);
    }

    public CustomerProjectPageDetailsTab clickFirstEditLinkOnQuoteOption() {
        editQuoteOptionLinks.get(0).click();
        return details;
    }

    public QuoteOptionPage navigateToQuoteOption(String quoteOptionName) {
        EntityFinder entityFinder = new EntityFinder(browser);
        entityFinder
            .find(QUOTE_OPTION)
            .with("name", quoteOptionName)
            .doAction().click(entity());
        return QuoteOptionPage.quoteOptionPageFromRedirect(browser);
    }

    public String getQuoteId()
    {
        return quoteId.toString();
    }

    public AddNoteDialog clickFirstAddNoteLink() {
        noteLinks.get(0).click();
        final AddNoteDialog addNoteDialog = new AddNoteDialog(browser);
        PageFactory.initElements(browser, addNoteDialog);
        return addNoteDialog;
    }

    public void assertPresenceOfOverlay() {
        assertTrue(uiWidgetOverlay.isEnabled());
    }

    public void assertIsForbidden() {
        assertTrue(errorMessage.isDisplayed());
    }

    public void assertIsUnauthorized() {
        assertTrue(errorMessage.isDisplayed());
    }

    public void assertHasQuoteOptionWithBCMImportExportButtons() {
        EntityFinder entityFinder = new EntityFinder(browser);
        assertThat(entityFinder.find(QUOTE_OPTION).with(image().attribute("alt", "BCM Export")), isPresent());
    }

    public void assertDoesNotHaveQuoteOptionWithBCMImportExportButtons() {
        EntityFinder entityFinder = new EntityFinder(browser);
        assertThat(entityFinder.find(QUOTE_OPTION).with(image().attribute("alt", "BCM Export")).waitNoLongerThan(1000), isNotPresent());
    }

    public void assertIfcPending(String optionName, boolean ifcPending) {
        EntityFinder entityFinder = new EntityFinder(browser);
        String ifcPendingStr = ifcPending ? "Yes" : "No";
        assertThat(entityFinder.find(QUOTE_OPTION).with("name", optionName).with("ifcPending", ifcPendingStr), isPresent());
    }

    public String getFirstExportBCMSheetLink() {
        return bcmExportLinks.get(0).getAttribute("href");
    }

    public HSSFWorkbook downloadBcmSheet(String token) throws IOException {
        InputStream inputStream = null;
        HSSFWorkbook bcmSheet;
        try {
            URL resource = new URL(getFirstExportBCMSheetLink() + "?guid=" + token);
            URLConnection connection = resource.openConnection();
            inputStream = connection.getInputStream();
            bcmSheet = new HSSFWorkbook(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return bcmSheet;
    }

    public void uploadBcmSheet(HSSFWorkbook bcmSheet, String approved, String token) throws IOException {
        URL url = new URL(getFirstExportBCMSheetLink() + "?guid=" + token);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bcmSheet.write(outputStream);

        PostMethod bcmSheetPost = new PostMethod(url.toString());
        Part[] parts = {
            new StringPart("approved", approved),
            new FilePart("bcmSheet", new ByteArrayPartSource("bcmSheet", outputStream.toByteArray()))
        };
        bcmSheetPost.setRequestEntity(
            new MultipartRequestEntity(parts, bcmSheetPost.getParams())
        );
        HttpClient client = new HttpClient();
        int status = client.executeMethod(bcmSheetPost);
    }

    public void clickOnRejectDiscountLink() {
        clickTheFirstRejectDiscountLink();
    }

    private void clickTheFirstRejectDiscountLink() {
        bcmRejectDiscountsLinks.get(0).click();
    }


    public void assertRejectDiscountSuccessMessagedIsDisplayed() {
        details.assertSuccessMessageIsDisplayed(true);
    }


    public void assertRejectDiscountSuccessMessagedIsNotDisplayed() {
        details.assertSuccessMessageIsDisplayed(false);
    }

    public void clickOnYesOnConfirmationDialog() {
        confirmationDialog.clickOnYesOnConfirmationDialog();
    }

    public void assertConfirmationDialogIsDisplayed(boolean displayed) {
        confirmationDialog.assertConfirmationDialogIsDisplayed(displayed);
    }

    public void selectNoOptionOnConfirmationDialog() {
        confirmationDialog.clickOnNoOnConfirmationDialog();
    }

    public CustomerProjectPage clickFirstDeleteButton() {
        deleteQuoteOptionLinks.get(0).click();
        deleteDialogOkButton.click();
        return this;
    }

    public void assertThatQuotesAreShown(int numberOfOptions) {
        assertThat(getNumberOfVisibleRow(), is(numberOfOptions));
    }

    public int getNumberOfVisibleRow() {
        int count = 0;
        for(WebElement row : this.quoteOptionRows) {
            if (!row.getAttribute("style").equals("display: none;")) {
                count++;
            }
        }
        return count;
    }

    public void waitForQuoteToHide() {
        int attempts = 0;
        while(!isRowHidden(this.quoteOptionRows.get(0)) && attempts < 10) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private boolean isRowHidden(WebElement row) {
        return row.getAttribute("style").equals("display: none;");
    }


    public class AddNoteDialog extends SeleniumWebDriverTestContext {
        @FindBy(css = "#newText")
        private WebElement newTextTextField;

        @FindBy(css = "#notesDialog .submit")
        private WebElement addButton;

        @FindBy(css = "#notesDialog .cancel")
        private WebElement cancelButton;

        @FindBy(css = "#ui-dialog-title-notesDialog")
        private WebElement titleBar;

        @FindBy(css = "#notesForm")
        private WebElement addNoteForm;


        public AddNoteDialog(WebDriver driver) {
            super(driver);
        }

        public void formIsDisplayed() {
            waitUntil(new Predicate<WebDriver>() {
                @Override
                public boolean apply(@Nullable WebDriver input) {
                    return addNoteForm.isDisplayed();
                }
            });
            assertTrue("Add note dialog is not displayed", addNoteForm.isDisplayed());
        }

        public void typeNote(String note) {
            newTextTextField.clear();
            newTextTextField.sendKeys(note);
        }

        public void clickSubmit() {
            addButton.click();
        }

        public void formIsNotDisplayed() {
            waitUntil(new Predicate<WebDriver>() {
                @Override
                public boolean apply(@Nullable WebDriver input) {
                    return !addNoteForm.isDisplayed();
                }
            });
            assertFalse("Add note dialog is displayed!", addNoteForm.isDisplayed());
        }

        public void clickCancel() {
            cancelButton.click();
        }

        public void assertTitleIs(String title) {
            assertThat(titleBar.getText(), is(title));
        }

        public boolean containsNoteWithText(final String noteText) {
            EntityFinder entityFinder = new EntityFinder(browser);
            assertThat(entityFinder.find("note").with("text", containingText(noteText)), isPresent());
            return true;
        }

        public boolean containsCreatedBy(final String fullName) {
            assertThat(new EntityFinder(browser).find("note").with("createdBy", containingText(fullName)), isPresent());
            return true;
        }
    }


}

