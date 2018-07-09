package com.bt.rsqe.projectengine.web.quoteoptionorders;

import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.OrderSheetMarshaller;
import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.bt.rsqe.utils.Clock;
import com.bt.rsqe.web.entityfinder.EntityFinder;
import com.google.common.base.Predicate;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static com.bt.rsqe.web.entityfinder.EntityMatchers.*;
import static com.bt.rsqe.web.entityfinder.functions.Functions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class QuoteOptionOrdersTab extends SeleniumWebDriverTestContext {

    private static final String EXPORT_RFO = "Export RFO";
    private static final String IMPORT_RFO = "Import RFO";
    private static final String ORDER_SUBMITTED_SUCCESSFULLY = "Order submitted successfully.";
    @FindBy(css = "#orders")
    private WebElement ordersTable;

    @FindBy(css = "#commonError")
    private WebElement orderSubmitFailureMessage;

    @FindBy(css = "#successMessage")
    private WebElement orderSubmitSuccessMessage;

    @FindBy(css = "#rfoImportDialog")
    private WebElement importRFODialog;

    @FindBy(css = "#progressDialog")
    private WebElement importRFOProgressDialog;

    @FindBy(css = "#progressText")
    private WebElement importRFOProgressMessage;

    @FindBy(css = " #rfoImport")
    private WebElement rfoImportLink;

    @FindBy(css = " #ok")
    private WebElement finalOkButton;

    @FindBy(css = "#uploadButton")
    private WebElement rfoUploadButton;

    private WebDriver driver;
    private static final String IMPORT_RFO_FAILURE = "RFO mandatory values are missing ";

    public QuoteOptionOrdersTab(WebDriver driver) {
        super(driver);
        this.driver = driver;
    }

    public void assertPresenceOfOrdersTable() {
        assertTrue(ordersTable.isDisplayed());
    }


    public void assertHasOrders(String orderName, String status, String offerName) {
        final EntityFinder.Context entity = new EntityFinder(driver).find("order")
                                                                    .with("name", containingText(orderName))
                                                                    .with("status", containingText(status))
                                                                    .with("offerName", containingText(offerName));
        assertThat(entity, isPresent());
    }

    public void assertOrderHasSubmitLink(String orderName) {
        assertLinkIsPresent(orderName, "Submit Order");
    }

    public void assertOrderSubmitLinkIsDisabled(String orderName) {
        assertLinkIsDisabled(orderName, "Submit Order");
    }

    public void assertOrderHasExportRFOLink(String orderName) {
        assertImgLinkIsPresent(orderName, "exportRFO");
    }

    public void assertOrderHasNotExportRFOLink(String orderName) {
        assertImgLinkIsNotPresent(orderName, "exportRFO");
    }

    public void clickSubmitLinkForOrder(String orderName) {
        findOrder(orderName).doAction().click(field("submitOrder"));
        waitForAnyResponseToOrderSubmission();
    }

    public void submitOrder(String orderName) {
        findOrder(orderName).doAction().click(field("submitOrder"));
        final CeasedExistingInventoryDialog ceasedExistingInventoryDialog = openCeasedExistingInventoryDialog();
        ceasedExistingInventoryDialog.assertDialogTitleIs("Ceased Existing Inventory?");
        ceasedExistingInventoryDialog.submit();
        waitForAnyResponseToOrderSubmission();
    }

    public CeasedExistingInventoryDialog openCeasedExistingInventoryDialog() {
        final CeasedExistingInventoryDialog ceasedExistingInventoryDialog = new CeasedExistingInventoryDialog();
        PageFactory.initElements(driver, ceasedExistingInventoryDialog);
        return ceasedExistingInventoryDialog;
    }

    public void assertOrderRFOLinksAreNotVisible(String orderName) {
        assertLinkIsNotVisible(orderName, "exportRFO");
        assertLinkIsNotVisible(orderName, "importRFO");
    }

    public void assertOrderRFOLinksAreVisible(String orderName) {
        assertLinkIsVisible(orderName, "exportRFO");
        assertLinkIsVisible(orderName, "importRFO");
    }

    private void assertLinkIsDisabled(String orderName, String link) {
        final EntityFinder.Context entity = findOrder(orderName)
            .with("actions", containsDisabledLink(link));
        assertThat(entity, isPresent());
    }

    private void assertLinkIsPresent(String orderName, String link) {
        final EntityFinder.Context entity = findOrder(orderName)
            .with("actions", containsLink(link));
        assertThat(entity, isPresent());
    }

    private void assertLinkIsNotVisible(String orderName, String link) {
        final EntityFinder.Context entity = findOrder(orderName)
            .with("actions", containsHiddenLink(link));
        assertThat(entity, isPresent());
    }

    private void assertLinkIsVisible(String orderName, String link) {
        final EntityFinder.Context entity = findOrder(orderName)
            .with("actions", containsVisibleLink(link));
        assertThat(entity, isPresent());
    }

    private void assertImgLinkIsPresent(String orderName, String actionField) {
        final EntityFinder.Context entity = findOrder(orderName)
            .with(image().in(field(actionField)));
        assertThat(entity, isPresent());
    }

    private void assertImgLinkIsNotPresent(String orderName, String actionField) {
        final EntityFinder.Context entity = findOrder(orderName)
            .with(image().in(field(actionField)))
            .waitNoLongerThan(1000);
        assertThat(entity, isNotPresent());
    }

    private EntityFinder.Context findOrder(String orderName) {
        return new EntityFinder(driver).find("order").with("name", containingText(orderName));
    }

    public String getExportRFOSheetLinkFor(String orderName) {
        return findOrder(orderName)
            .doAction()
            .resolve(attribute("href")).from(field("exportRFO"));
    }

    public URL getExportRFOLink(String currentToken, String orderName) throws MalformedURLException {
        return new URL(getExportRFOSheetLinkFor(orderName) + "?guid=" + currentToken);
    }

    public void assertOrderSubmissionFailed(final String expectedMessage) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                waitForVisible(orderSubmitFailureMessage);
                assertThat(orderSubmitFailureMessage.getText(), containsString(expectedMessage));
                return true;
            }
        }, 20);
    }

    public void assertOrderSubmitted(String orderName) {
        final String expectedMessage = String.format("Order %s successfully submitted.", orderName);
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                waitForVisible(orderSubmitSuccessMessage);
                assertThat(orderSubmitSuccessMessage.getText(), is(expectedMessage));
                return true;
            }
        }, 30);
    }

    public QuoteOptionOrdersTab waitForRowToAppear() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                final EntityFinder.Context entity = new EntityFinder(driver).find("order").
                    with(field("submitOrder"));
                return entity.isVisible();
            }
        }, 20);
        return this;
    }


    private void waitForAnyResponseToOrderSubmission() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                if (orderSubmitSuccessMessage.isDisplayed() || orderSubmitFailureMessage.isDisplayed()) {
                    return true;
                }
                return false;
            }
        }, 40);
    }

    private void waitForSuccessResponseToOrderSubmission() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                if (ORDER_SUBMITTED_SUCCESSFULLY.equals(orderSubmitSuccessMessage.getText())) {
                    return true;
                }
                return false;
            }
        }, 40);
    }

    private void waitForVisible(final WebElement selector) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return selector.isDisplayed();
            }
        });
    }

    public void importDownloadedRHOSheetAndAssertItWasSuccessful(Workbook rfoSheet, URL rfoUrl) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        rfoSheet.write(outputStream);

        PostMethod rfoSheetPost = new PostMethod(rfoUrl.toString());
        Part[] parts = {
            new FilePart("rfoSheet", new ByteArrayPartSource("rfoSheet", outputStream.toByteArray()))
        };
        rfoSheetPost.setRequestEntity(
            new MultipartRequestEntity(parts, rfoSheetPost.getParams())
        );
        HttpClient client = new HttpClient();
        int status = client.executeMethod(rfoSheetPost);
        assertThat(status, is(200));
    }

    public void importDownloadedRHOSheetAndAssertItWasSuccessful(Workbook rfoSheet, String orderName, String token) throws IOException {
        importDownloadedRHOSheetAndAssertItWasSuccessful(rfoSheet, getExportRFOLink(token, orderName));
    }

    public void provideRfoInfo(String token, String orderName) throws IOException {
        InputStream inputStream = null;
        HSSFWorkbook rfoSheet;
        URL exportRFOUrl = getExportRFOLink(token, orderName);
        try {
            URL resource = exportRFOUrl;
            URLConnection connection = resource.openConnection();
            inputStream = connection.getInputStream();
            rfoSheet = new HSSFWorkbook(inputStream);
            final Sheet orderSheet = rfoSheet.getSheet(OrderSheetMarshaller.SHEET_NAME);
            for (Row currentRow : orderSheet) {
                currentRow.createCell(OrderSheetMarshaller.Column.SIGNED_DATE.column).setCellValue(Clock.now().toString("dd/MM/yyyy"));
                currentRow.createCell(OrderSheetMarshaller.Column.BILLING_ID.column).setCellValue("1");
            }

            importDownloadedRHOSheetAndAssertItWasSuccessful(rfoSheet, resource);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public void provideICRRfoInfo(String token, String orderName, String accessCircuitName) throws IOException {
        InputStream inputStream = null;
        HSSFWorkbook rfoSheet;
        URL exportRFOUrl = getExportRFOLink(token, orderName);
        try {
            URL resource = exportRFOUrl;
            URLConnection connection = resource.openConnection();
            inputStream = connection.getInputStream();
            rfoSheet = new HSSFWorkbook(inputStream);
            final Sheet orderSheet = rfoSheet.getSheet(OrderSheetMarshaller.SHEET_NAME);
            final Sheet accessCircuitSheet = rfoSheet.getSheet(accessCircuitName);
            for (Row currentRow : orderSheet) {
                currentRow.createCell(OrderSheetMarshaller.Column.SIGNED_DATE.column).setCellValue(Clock.now().toString("dd/MM/yyyy"));
                currentRow.createCell(OrderSheetMarshaller.Column.BILLING_ID.column).setCellValue("1");
            }

            int currentCellNum = 0;
            for (Row currentRow : accessCircuitSheet) {
                for (int cellNum = 0; cellNum < currentRow.getLastCellNum(); cellNum++) {
                    Cell cell = currentRow.getCell(cellNum);
                    if (cell.getStringCellValue().equals("ROOM/FLOOR (M)")) {
                        currentCellNum = cellNum;
                    }
                }
            }
            for (Row currentRow : accessCircuitSheet) {
                Cell cell = currentRow.getCell(currentCellNum);
                //TODO:there is a read-only cell in rfo sheet in modify journey for access circuit,not sure if this is correct
                if ((cell.getStringCellValue().equals("")) && (!cell.getCellStyle().getLocked())) {
                    currentRow.getCell(currentCellNum).setCellValue("1");
                }
            }
            importDownloadedRHOSheetAndAssertItWasSuccessful(rfoSheet, resource);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public class CeasedExistingInventoryDialog {

        @FindBy(css = "#migrationYesButton")
        private WebElement migrationYesButton;

        @FindBy(css = "#migrationNoButton")
        private WebElement migrationNoButton;

        @FindBy(css = "#ui-dialog-title-migrationConfirmationDialog")
        private WebElement titleBar;

        public void submit() {
            migrationYesButton.click();
        }

        public void cancel() {
            migrationNoButton.click();
        }

        public void assertDialogTitleIs(String title) {
            assertThat(titleBar.getText(), is(title));
        }
    }
}
