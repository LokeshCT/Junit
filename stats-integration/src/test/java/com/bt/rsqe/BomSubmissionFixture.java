package com.bt.rsqe;

import com.bt.rsqe.enums.ProductCategoryCodes;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.pc.web.pages.ProductConfiguratorPage;
import com.bt.rsqe.projectengine.launchproject.LaunchProjectLandingPageTestFixture;
import com.bt.rsqe.projectengine.quoteoptions.details.AddProductTestFixture;
import com.bt.rsqe.projectengine.web.projects.CustomerProjectPage;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionDetailsTab;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionPage;
import com.bt.rsqe.projectengine.web.quoteoption.quoteoptionoffers.OfferDetailsPage;
import com.bt.rsqe.projectengine.web.quoteoption.quoteoptionoffers.OfferDetailsTab;
import com.bt.rsqe.projectengine.web.quoteoptionorders.QuoteOptionOrdersTab;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.OrderSheetMarshaller;
import com.bt.rsqe.utils.Clock;
import com.google.common.base.Strings;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.openqa.selenium.WebDriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static com.bt.rsqe.projectengine.fixtures.config.ProjectEngineConfiguration.*;

public class BomSubmissionFixture {

    private WebDriver browser;
    private ProductConfiguratorPage productConfiguratorPage;
    private String offerName = "VCMC_Offer";
    private String orderName = "VCMC_Order";
    private String projectEngineWindowHandle;
    private String token;
    public ServerDetails details = ServerDetails.BLANK;
    private LaunchProjectLandingPageTestFixture landingPageFixture;

    private BomSubmissionFixture(WebDriver browser) {
        System.err.println("Get test fixture for " + browser);
        this.browser = browser;
        FileInputStream fis = null;
        BufferedReader reader = null;

        String filename = System.getProperty("test.input", StatsIntegrationSuite.USEFIXTURES);//have we test input supplied or should we use fixtures
        String projectId;
        int customerId;
        dumpEnvironment();




        if (StatsIntegrationSuite.USEFIXTURES.equals(filename)) {
            System.err.println("Test is to use internal fixtures");
            projectId= UUID.randomUUID().toString();
            customerId = 1000 + (int)(Math.random()*999999);
            System.err.println("Test is to use internal fixtures");
            landingPageFixture = LaunchProjectLandingPageTestFixture.withoutExistingProjectForBomSubmission(browser,
                                                                                                            projectId,
                                                                                                            customerId);
            projectId = landingPageFixture.getExpedioFixture().getProjectId();
            customerId = Integer.parseInt(landingPageFixture.getExpedioFixture().getCustomerId());
            token = landingPageFixture.getExpedioFixture().getTokenForSingleUser();
            details = ServerDetails.BLANK;
        } else {

            System.err.println("Test is to use supplied data and remote server");

            details = ServerDetails.serverDetailsFromFile(filename);

            landingPageFixture = LaunchProjectLandingPageTestFixture.toRemoteInstanceWithGivenProject(browser,
                                                                                                      details.getProject(),
                                                                                                      details.getCustomerAsInt(),
                                                                                                      details);
            token = details.getExpedioKey();

        }
    }

    private int getPortForUrl(URL theHost) {

        int port = 80;
        port = theHost.getDefaultPort();

        if (theHost.getPort() != -1) {
            port = theHost.getPort();
        }
        return port;
    }


    public boolean isUsingRemoteInstance() {
        return !ServerDetails.BLANK.equals(details);
    }

    public static BomSubmissionFixture forAQuoteOption(WebDriver browser) {
        return new BomSubmissionFixture(browser);
    }









    public BomSubmissionFixture navigateFromLaunchProjectLandingPage() {
        CustomerProjectPage page = null;
        //CustomerQuoteOptionDialogPage details = null;
        //CustomerProjectPageDetailsTab detailsTab = null;
        page = landingPageFixture.openCustomerProjectPage(details);

        page.assertHasQuoteOptionWithEditButton();

        //CustomerQuoteOptionDialogPage dialog = page.getOptionsForQuoteOptionLink(0);

        //dialog.enterQuoteOptionDetails(loadedConfig.getProduct().getQuoteName(),"GBP","36");
        //dialog.submit();

        page.navigateToQuoteOption(projectEngineConfig().getDefaultQuoteOptionName());
        return this;
    }


    public BomSubmissionFixture directUserAddConnectAccelerationServiceAsTheProduct() {

        projectEngineWindowHandle = browser.getWindowHandle();

        AddProductTestFixture.aPageThatHasTheAddProductButton(browser,
                                                              landingPageFixture.getExpedioFixture(),
                                                              landingPageFixture.getProjectFixture())
                             .openQuoteOptionDetailsTab(QuoteOptionPage.quoteOptionPageFromRedirect(browser))
                             .clickAddProduct()
                             .chooseProduct(ProductCodes.ConnectAccelerationService)
                             .assertThatProductIsNotSiteInstallable()
                             .clickSubmit();

        return this;
    }

    public BomSubmissionFixture configureConnectAccelerationServiceProduct() {
        final QuoteOptionDetailsTab quoteOptionDetailsTab = QuoteOptionPage.quoteOptionPageFromRedirect(browser).detailsTab();

        quoteOptionDetailsTab.clickActionLink(ProductCodes.ConnectAccelerationService.productCode(), "configure");

        browser.switchTo().window((String) browser.getWindowHandles().toArray()[1]);
        productConfiguratorPage = new ProductConfiguratorPage(browser);
        String usersToAdd = isUsingRemoteInstance() ? details.getConfig().getProduct().getCmc().getNumberOfUsers() : "5";
        addNumberOfMobileUsers(usersToAdd);
        addCMCVirtualEdition();
        addElementManager();

        productConfiguratorPage.fetchPrices();

        return this;
    }

    private void addNumberOfMobileUsers(String number) {

        String users = "5";
        if (!Strings.isNullOrEmpty(number)) {
            users = number;
        }

        productConfiguratorPage.configureAttribute("NUMBER OF MOBILE USERS", number)
                               .submitForm();
    }


    private void addElementManager() {
        productConfiguratorPage.openAddRelationDialog("ElementManager")
                                     .selectProduct(ProductCodes.CMC_VE.productName());
    }

    private void addCMCVirtualEdition() {
            productConfiguratorPage.openAddRelationDialog("CMC-VirtualEdition")
                                   .addProduct(ProductCodes.CMC_VE.productName());

        }

    public BomSubmissionFixture fetchPriceAndCreateServiceOffer() {
        browser.switchTo().window(projectEngineWindowHandle);
        browser.navigate().refresh();
        QuoteOptionPage quoteOptionPage = QuoteOptionPage.quoteOptionPageFromRedirect(browser);
        quoteOptionPage.detailsTab()
                       .waitForLoad();
        browser.navigate().refresh();

        quoteOptionPage.clickPriceTabAndWait();
        quoteOptionPage.clickDetailsTabAndWait();

        quoteOptionPage.detailsTab()
                       .waitForLoad()
                       .waitForValidationAndSelectAllItems()
                       .createOffer(offerName);
        return this;
    }

    public BomSubmissionFixture approveTheOfferAndCreatesServiceOrder() throws InterruptedException {
        QuoteOptionPage.quoteOptionPageFromRedirect(browser).offersTab().approveOfferAndAssumeSuccess(offerName);

        OfferDetailsPage offerDetailsPage = QuoteOptionPage.quoteOptionPageFromRedirect(browser).clickOffersTab().clickOnOffer(offerName);
        final OfferDetailsTab offerDetailsTab = offerDetailsPage.detailsTab();
        offerDetailsTab.waitUntilOfferItemsDisplayed();
        offerDetailsTab.waitForCreateOrderButtonIsDisabledToComplete();
        offerDetailsTab.selectAllItems();
        offerDetailsTab.assertCreateOrderShown();
        offerDetailsTab.createOrder(orderName);
        return this;
    }

    public BomSubmissionFixture submitTheOrder() {
        final QuoteOptionPage quoteOptionPage = QuoteOptionPage.quoteOptionPageFromRedirect(browser);
        quoteOptionPage.ordersTab().clickSubmitLinkForOrder(orderName);
        return this;
    }

    public BomSubmissionFixture exportAndImportServiceRFO() throws IOException {
        final QuoteOptionOrdersTab ordersTab = QuoteOptionPage.quoteOptionPageFromRedirect(browser).ordersTab();

        final URL rfoUrl;
        rfoUrl = ordersTab.getExportRFOLink(token, orderName);
        InputStream inputStream = null;
        String ein = isUsingRemoteInstance() ? details.getConfig().getProduct().getCmc().getEinNumber() : "987654321";
        try {

            URLConnection connection = rfoUrl.openConnection();
            inputStream = connection.getInputStream();
            HSSFWorkbook rfoSheet = new HSSFWorkbook(inputStream);

            final Sheet connectAccelerationServiceSheet = rfoSheet.getSheet(ConnectAccelerationServiceSheet.NAME);
            for (Row currentRow : connectAccelerationServiceSheet) {
                if (currentRow.getRowNum() == 2) {
                    currentRow.createCell(ConnectAccelerationSheetMarshaller.Column.PORTAL_CUSTOMER_ID.column)
                              .setCellValue(ein);
                }
            }

            final Sheet orderSheet = rfoSheet.getSheet(OrderSheetMarshaller.SHEET_NAME);
            for (Row currentRow : orderSheet) {
                if (currentRow.getRowNum() == 1) {
                    currentRow.createCell(OrderSheetMarshaller.Column.SIGNED_DATE.column).setCellValue(Clock.now().toString("dd/MM/yyyy"));
                    currentRow.createCell(OrderSheetMarshaller.Column.BILLING_ID.column).setCellValue("1");
                }
            }

            ordersTab.importDownloadedRHOSheetAndAssertItWasSuccessful(rfoSheet, rfoUrl);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return this;
    }


    private void dumpEnvironment() {


        try {
            PrintWriter dump = new PrintWriter(new FileWriter(File.createTempFile("Environment", "txt")));
            Properties sys = System.getProperties();
            dump.println(" Environment");
            for (Object o : sys.keySet()) {
                dump.println("     Key " + o.toString() + " = " + sys.get(o));
            }
            Map props = System.getenv();
            dump.println(" Environment");
            for (Object o : props.keySet()) {
                dump.println("     Key " + o.toString() + " = " + props.get(o));
            }

            dump.println("Finished");
            dump.close();


        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

}

