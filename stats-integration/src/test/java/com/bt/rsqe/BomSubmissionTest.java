package com.bt.rsqe;

import com.bt.rsqe.configurator.web.BulkConfigurationTestFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.*;

public class BomSubmissionTest extends WebDrivenAcceptanceTest {
    @WebDriverRequirement(WebDriverType.FIREFOX)
    private WebDriver browser;
    private BomSubmissionFixture context;
    private BulkConfigurationTestFixture bulkContext;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        System.out.println("Setup");
        context = BomSubmissionFixture.forAQuoteOption(browser);
        bulkContext = new BulkConfigurationTestFixture(browser);
    }

    @After
    public void tearDown() throws Exception{
        //browser.quit();
    }



    //@Test
    //public void processCAServiceOrder() throws Exception {
    //
    //    //from bulk ui test
    //    //configureConnectAccelerateService(page);
    //
    //    System.out.println("processCAServiceOrder Starting ");
    //    assertNotNull("Context was not initialized", context);
    //    context.navigateFromLaunchProjectLandingPage()
    //           .directUserAddConnectAccelerationServiceAsTheProduct()
    //           .configureConnectAccelerationServiceProduct()
    //           .fetchPriceAndCreateServiceOffer()
    //           .approveTheOfferAndCreatesServiceOrder()
    //           .exportAndImportServiceRFO()
    //           .submitTheOrder();
    //}




    @Test
    public void processCAServiceOrderWithSingleSiteUI() throws Exception {

        System.err.println("processCAServiceOrder Starting ");
        assertNotNull("Context was not initialized", context);
        context.navigateFromLaunchProjectLandingPage()
               .directUserAddConnectAccelerationServiceAsTheProduct()
               .configureConnectAccelerationServiceProduct()
               .fetchPriceAndCreateServiceOffer()
               .approveTheOfferAndCreatesServiceOrder()
               .exportAndImportServiceRFO()
               .submitTheOrder();
    }


}