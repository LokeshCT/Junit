package com.bt.rsqe.projectengine.web.js;

import com.bt.rsqe.ProjectEngineWebEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.domain.bom.fixtures.SellableProductFixture;
import com.bt.rsqe.enums.MoveConfigurationTypeEnum;
import com.bt.rsqe.enums.ProductAction;
import com.bt.rsqe.enums.RollOnContractEnum;
import com.bt.rsqe.projectengine.OrderItemSite;
import com.bt.rsqe.projectengine.OrderStatus;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.projectengine.web.QuoteOptionDTOFixture;
import com.bt.rsqe.projectengine.web.fixtures.ProductsFixture;
import com.bt.rsqe.projectengine.web.model.OfferDetailsModel;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.AddOrModifyProductView;
import com.bt.rsqe.projectengine.web.view.AttachmentDialogView;
import com.bt.rsqe.projectengine.web.view.OfferDetailsTabView;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.projectengine.web.view.QuoteOptionDetailsView;
import com.bt.rsqe.projectengine.web.view.QuoteOptionDialogView;
import com.bt.rsqe.projectengine.web.view.QuoteOptionOrdersView;
import com.bt.rsqe.projectengine.web.view.filtering.PricingTabView;
import com.bt.rsqe.selenium.WebDriverImplementation;
import com.bt.rsqe.utils.Environment;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.View;
import org.dom4j.Document;
import org.dom4j.Element;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.bt.rsqe.projectengine.web.fixtures.QuoteOptionDetailsViewFixture.*;
import static com.bt.rsqe.web.XHtmlDocumentHelper.*;
import static com.google.common.collect.Lists.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JavascriptTest {
    private static final String CAT_CODE = "C1";
    private static final String CAT_NAME = "Product Category 1";

    private static Presenter presenter;
    private static JavaScriptTestApplication application;
    private static WebDriver webDriver;
    private static ProjectEngineWebConfig config;
    private static String applicationBaseUri = "http://127.0.0.1:9994";
    private List<OrderItemSite> orderItemSite;

    @BeforeClass
    public static void setUpClass() throws Exception {
        config = ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env())
                                      .getProjectEngineWebConfig();
        application = new JavaScriptTestApplication(config.getApplicationConfig());
        applicationBaseUri = String.format("http://%s:%s",config.getApplicationConfig().getHost(),String.valueOf(config.getApplicationConfig().getPort()));
        application.start();
        presenter = new Presenter();
        webDriver = WebDriverImplementation.FIREFOX.create();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (null != webDriver) {
            webDriver.close();
        }
        if (null != application) {
            application.stop();
        }

    }

    @Test
    public void runQuoteOptionFormSuite() throws Exception {
        String htmlUnderTest = presenter.render(View.viewUsingTemplate("com/bt/rsqe/projectengine/web/QuoteOptionForm.ftl")
                                                    .withContext("view", new QuoteOptionDialogView("123", "312", "321")));

        run(htmlUnderTest/*,WebDriverImplementation.FIREFOX.create()*/,
            "/com/bt/rsqe/web/staticresources/scripts/Navigate.js",
            "/com/bt/rsqe/web/staticresources/scripts/QuoteOptionForm.js",
            "/com/bt/rsqe/web/staticresources/scripts/QuoteOptionFormTest.js");
    }

    @Test
    public void runAddProductSuite() throws Exception {
        AddOrModifyProductView view = new AddOrModifyProductView("54", "123", "projectId", "quoteOptionId", Collections.EMPTY_LIST, new UriFactoryImpl(ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig()), "GBP", "NAME", "revenue owner", false, ProductAction.Provide.description(), "1", "orderType", "subOrderType");
        final Products products = ProductsFixture.aProducts().withProduct("1", "RSQE X", true, CAT_CODE, CAT_NAME, false, "orderPreRequisiteUrl1", MoveConfigurationTypeEnum.NOT_MOVEABLE, null)
                                                             .withProduct("2", "RSQE Y", false, CAT_CODE, CAT_NAME, false, "orderPreRequisiteUrl1", MoveConfigurationTypeEnum.NOT_MOVEABLE, null)
                                                             .withProduct("3", "RSQE Z", true, CAT_CODE, CAT_NAME, false, "orderPreRequisiteUrl1", MoveConfigurationTypeEnum.COPY_ALL, RollOnContractEnum.ALWAYS)
                                                             .withProduct("4", "RSQE A", true, "C2", "Product Category 2", false, "", MoveConfigurationTypeEnum.ROOT_ONLY_COPY, RollOnContractEnum.SAME_SITE_ONLY)
                                                             .build();
        view.setProducts(products);

        String htmlUnderTest = presenter.render(View.viewUsingTemplate("com/bt/rsqe/projectengine/web/AddProductPage.ftl")
                                                    .withContext("view", view));

        run(htmlUnderTest,/*WebDriverImplementation.FIREFOX.create(), */"/com/bt/rsqe/web/staticresources/scripts/AddProducts.js",
            "/com/bt/rsqe/web/staticresources/scripts/CheckBoxGroup.js",
            "/com/bt/rsqe/web/staticresources/scripts/DataTable.js",
            "/com/bt/rsqe/web/staticresources/scripts/Dialog.js",
            "/com/bt/rsqe/web/staticresources/scripts/ProgressDialog.js",
            "/com/bt/rsqe/web/staticresources/scripts/NewSiteForm.js",
            "/com/bt/rsqe/web/staticresources/scripts/dataTables.fnUpdateRowWithNewSiteData.js",
            "/com/bt/rsqe/web/staticresources/scripts/AddProductTest.js");
    }

    @Test
    public void runRemoveProductSuite() throws Exception {
        AddOrModifyProductView view = new AddOrModifyProductView("54", "123", "projectId", "quoteOptionId", Collections.EMPTY_LIST, new UriFactoryImpl(ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig()), "GBP", "NAME", "revenue owner", false, ProductAction.Modify.description(), "1", "orderType", "subOrderType");
        final Products products = ProductsFixture.aProducts().withProduct("1", "RSQE X", true, CAT_CODE, CAT_NAME, false, "orderPreRequisiteUrl1", MoveConfigurationTypeEnum.NOT_MOVEABLE, null)
                                                 .withProduct("2", "RSQE Y", false, CAT_CODE, CAT_NAME, false, "orderPreRequisiteUrl1", MoveConfigurationTypeEnum.NOT_MOVEABLE, null)
                                                 .withProduct("3", "RSQE Z", true, CAT_CODE, CAT_NAME, false, "orderPreRequisiteUrl1", MoveConfigurationTypeEnum.COPY_ALL, RollOnContractEnum.ALWAYS)
                                                 .withProduct("4", "RSQE A", true, "C2", "Product Category 2", false, "", MoveConfigurationTypeEnum.ROOT_ONLY_COPY, RollOnContractEnum.SAME_SITE_ONLY)
                                                 .build();
        view.setProducts(products);

        String htmlUnderTest = presenter.render(View.viewUsingTemplate("com/bt/rsqe/projectengine/web/AddProductPage.ftl")
                                                    .withContext("view", view));

        run(htmlUnderTest,/*WebDriverImplementation.FIREFOX.create(), */"/com/bt/rsqe/web/staticresources/scripts/AddProducts.js",
            "/com/bt/rsqe/web/staticresources/scripts/CheckBoxGroup.js",
            "/com/bt/rsqe/web/staticresources/scripts/DataTable.js",
            "/com/bt/rsqe/web/staticresources/scripts/Dialog.js",
            "/com/bt/rsqe/web/staticresources/scripts/ProgressDialog.js",
            "/com/bt/rsqe/web/staticresources/scripts/NewSiteForm.js",
            "/com/bt/rsqe/web/staticresources/scripts/dataTables.fnUpdateRowWithNewSiteData.js",
            "/com/bt/rsqe/web/staticresources/scripts/RemoveProductTest.js");
    }

    @Test
    public void runMoveProductSuite() throws Exception {
        AddOrModifyProductView view = new AddOrModifyProductView("54", "123", "projectId", "quoteOptionId", Collections.EMPTY_LIST, new UriFactoryImpl(ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig()), "GBP", "NAME", "revenue owner", false, ProductAction.Move.description(), "1", "orderType", "subOrderType");
        final Products products = ProductsFixture.aProducts().withProduct("1", "RSQE X", true, CAT_CODE, CAT_NAME, MoveConfigurationTypeEnum.NOT_MOVEABLE, null)
                                                 .withProduct("2", "RSQE Y", false, CAT_CODE, CAT_NAME, MoveConfigurationTypeEnum.NOT_MOVEABLE, null).build();
        view.setProducts(products);

        String htmlUnderTest = presenter.render(View.viewUsingTemplate("com/bt/rsqe/projectengine/web/AddProductPage.ftl")
                                                    .withContext("view", view));

        run(htmlUnderTest,/*WebDriverImplementation.FIREFOX.create(), */"/com/bt/rsqe/web/staticresources/scripts/AddProducts.js",
            "/com/bt/rsqe/web/staticresources/scripts/CheckBoxGroup.js",
            "/com/bt/rsqe/web/staticresources/scripts/DataTable.js",
            "/com/bt/rsqe/web/staticresources/scripts/Dialog.js",
            "/com/bt/rsqe/web/staticresources/scripts/ProgressDialog.js",
            "/com/bt/rsqe/web/staticresources/scripts/NewSiteForm.js",
            "/com/bt/rsqe/web/staticresources/scripts/dataTables.fnUpdateRowWithNewSiteData.js",
            "/com/bt/rsqe/web/staticresources/scripts/MoveProductTest.js");
    }

    @Test
    public void runAttachmentSuite() throws Exception {
        final AttachmentDialogView view = new AttachmentDialogView("customerId", "contractId", "projectId", "quoteOptionid", false);

        String htmlUnderTest = presenter.render(View.viewUsingTemplate("com/bt/rsqe/projectengine/web/AttachmentForm.ftl")
                                                    .withContext("view", view));
         run(htmlUnderTest,"/com/bt/rsqe/web/staticresources/scripts/QuoteOptionDetailsTab.js",
                           "/com/bt/rsqe/web/staticresources/scripts/StatusMessage.js",
                           "/com/bt/rsqe/web/staticresources/scripts/BulkTemplateDialog.js",
                           "/com/bt/rsqe/web/staticresources/scripts/CheckboxGroup.js",
                           "/com/bt/rsqe/web/staticresources/scripts/ProgressDialog.js",
                           "/com/bt/rsqe/web/staticresources/scripts/LineItemValidation.js",
                           "/com/bt/rsqe/web/staticresources/scripts/ContractForm.js",
                           "/com/bt/rsqe/web/staticresources/scripts/AttachmentForm.js",
                           "/com/bt/rsqe/web/staticresources/scripts/AttachmentDialogTest.js");

    }

    @Test
    @Ignore("Dodgy test - talk to David next week")
    public void runQuoteOptionPricingTabSuite() throws Exception {
        String htmlUnderTest = presenter.render(View.viewUsingTemplate("com/bt/rsqe/projectengine/web/QuoteOptionPricingTab.ftl")
                                                    .withContext("customerId", "customerId")
                                                    .withContext("projectId", "projectId")
                                                    .withContext("quoteOptionId", "quoteOptionId")
                                                    .withContext("view", new PricingTabView(Arrays.asList("Product1"),
                                                                                            "GBP",
                                                                                            newArrayList(""),
                                                                                            "",
                                                                                            "",
                                                                                            false,
                                                                                            true,
                                                                                            "",
                                                                                            UriFactoryImpl.quoteOptionBcm("customerId", "contractId", "projectId", "quoteOptionId").toString(),
                                                                                            "", QuoteOptionDTOFixture.aQuoteOptionDTO().withId("quoteOptionId").build(), null,false)));

        run(htmlUnderTest, "/com/bt/rsqe/web/staticresources/scripts/QuoteOptionPricingTab.js",
            "/com/bt/rsqe/web/staticresources/scripts/CheckBoxGroup.js",
            "/com/bt/rsqe/web/staticresources/scripts/QuoteOptionPricingTabTest.js");
    }

    @Test
    public void runBulkDiscountSuite() throws Exception {
        String htmlUnderTest = presenter.render(View.viewUsingTemplate("com/bt/rsqe/projectengine/web/QuoteOptionPricingTab.ftl")
                                                    .withContext("customerId", "customerId")
                                                    .withContext("contractId", "contractId")
                                                    .withContext("projectId", "projectId")
                                                    .withContext("quoteOptionId", "quoteOptionId")
                                                    .withContext("view", new PricingTabView(Arrays.asList("Product1"),
                                                                                            "GBP",
                                                                                            newArrayList(""),
                                                                                            "",
                                                                                            "",
                                                                                            false,
                                                                                            true,
                                                                                            "",
                                                                                            UriFactoryImpl.quoteOptionBcm("customerId", "contractId", "projectId", "quoteOptionId").toString(),
                                                                                            "", QuoteOptionDTOFixture.aQuoteOptionDTO().withId("quoteOptionId").build(), null,false)));

        run(htmlUnderTest, "/com/bt/rsqe/web/staticresources/scripts/PricingDataTable.js",
            "/com/bt/rsqe/web/staticresources/scripts/ButtonGroupTabView.js",
            "/com/bt/rsqe/web/staticresources/scripts/QuoteOptionPricingTab.js",
            "/com/bt/rsqe/web/staticresources/scripts/CheckBoxGroup.js",
            "/com/bt/rsqe/web/staticresources/scripts/StatusMessage.js",
            "/com/bt/rsqe/web/staticresources/scripts/Discounts.js",
            "/com/bt/rsqe/web/staticresources/scripts/QuoteOptionPricingBulkDiscountTestTableJson.js",
            "/com/bt/rsqe/web/staticresources/scripts/QuoteOptionPricingBulkDiscountTest.js");
    }




    @Test
    public void runRSQESuite() throws Exception {
        String htmlUnderTest = "<div></div>";
        run(htmlUnderTest, "/com/bt/rsqe/web/staticresources/scripts/RsqeTest.js");
    }

    @Test
    public void runDialogTestSuite() {
        String htmlUnderTest = presenter.render(View.viewUsingTemplate("com/bt/rsqe/web/staticresources/scripts/Dialog.ftl"));
        run(htmlUnderTest,
            "/com/bt/rsqe/web/staticresources/scripts/DialogTest.js");
    }

    @Test
    public void runProgressDialogTestSuite() {
        String htmlUnderTest = presenter.render(View.viewUsingTemplate("com/bt/rsqe/web/staticresources/scripts/ProgressDialog_test.ftl"));
        run(htmlUnderTest,
            "/com/bt/rsqe/web/staticresources/scripts/ProgressDialog.js",
            "/com/bt/rsqe/web/staticresources/images/spinning.gif",
            "/com/bt/rsqe/web/staticresources/scripts/ProgressDialogTest.js");
    }

    @Test
    public void runCheckboxGroupSuite() {
        String htmlUnderTest = presenter.render(View.viewUsingTemplate("com/bt/rsqe/web/staticresources/scripts/Checkbox_test.ftl"));
        run(htmlUnderTest,
            "/com/bt/rsqe/web/staticresources/scripts/CheckBoxGroup.js",
            "/com/bt/rsqe/web/staticresources/scripts/CheckboxGroupTest.js"
        );
    }

    @Test
    public void runDisableableSuite() {
        String htmlUnderTest = presenter.render(View.viewUsingTemplate("com/bt/rsqe/web/staticresources/scripts/link_test.ftl"));
        run(htmlUnderTest,
            "/com/bt/rsqe/web/staticresources/scripts/DisableableTest.js"
        );
    }

    @Test
    public void googleMaps() {
        final QuoteOptionDetailsView model = aQuoteOptionDetailsView()
            .withIdentifiers("1", "2", "3", "4")
            .withProducts(new Products(asList(SellableProductFixture.aProduct().withId("prod1").withName("name1").withCategory(CAT_CODE, CAT_NAME).withIsImportable(false).build(),
                                              SellableProductFixture.aProduct().withId("blah-code").withName("blah-name").withCategory(CAT_CODE, CAT_NAME).withIsImportable(true).build())))
            .withUriFactory(new UriFactoryImpl(config)).build();
        final Document detailsTab = parseHtml(new Presenter().render(View.viewUsingTemplate("/com/bt/rsqe/projectengine/web/QuoteOptionDetailsTab.ftl").withContext("view", model)));
        run(detailsTab.asXML(),
            "/com/bt/rsqe/web/staticresources/scripts/DataTable.js",
            "/com/bt/rsqe/web/staticresources/scripts/CheckBoxGroup.js",
            "/com/bt/rsqe/web/staticresources/scripts/ProductPricing.js",
            "/com/bt/rsqe/web/staticresources/scripts/LineItemValidation.js",
            "/com/bt/rsqe/web/staticresources/scripts/StatusMessage.js",
            "/com/bt/rsqe/web/staticresources/scripts/BulkTemplateDialog.js",
            "/com/bt/rsqe/web/staticresources/scripts/CheckboxGroup.js",
            "/com/bt/rsqe/web/staticresources/scripts/ProgressDialog.js",
            "/com/bt/rsqe/web/staticresources/scripts/LineItemValidation.js",
            "/com/bt/rsqe/web/staticresources/scripts/ContractForm.js",
            "/com/bt/rsqe/web/staticresources/scripts/AttachmentForm.js",
            "/com/bt/rsqe/web/staticresources/scripts/ImportProduct.js",
            "/com/bt/rsqe/web/staticresources/scripts/QuoteOptionDetailsTab.js",
            "/com/bt/rsqe/web/staticresources/scripts/GoogleMapsTest.js"
        );
    }

    @Test
    public void dataTableSuite() {
        run(lineItemsTable().asXML(), "/com/bt/rsqe/web/staticresources/scripts/DataTable.js", "/com/bt/rsqe/web/staticresources/scripts/DataTableTest.js");
    }

    @Test
    public void orderTabSuite() {
        final QuoteOptionOrdersView model = new QuoteOptionOrdersView("customerId1", "contractId1", "projectId1", "quoteOptionId1");
        boolean migrationQuote = false;
        model.setOrder(model.new Order("orderId1", "orderName1", "created", OrderStatus.CREATED.getValue(), "offerName1", migrationQuote, false, null, false,orderItemSite));
        String render = new Presenter().render(View.viewUsingTemplate("/com/bt/rsqe/projectengine/web/OrdersTab.ftl")
                                                         .withContext("view", model));

        Document document = parseHtml(render);
        final Element element = (Element) document.selectSingleNode("//table[@id='orders']//*[contains(@class,'actions')]/a[contains(@class,'importRFO')]");
        assertThat(element.attributeValue("href"), is("/rsqe/customers/customerId1/contracts/contractId1/projects/projectId1/quote-options/quoteOptionId1/orders/orderId1/rfo"));
        run(document.asXML(),
            "/com/bt/rsqe/web/staticresources/scripts/OrdersTab.js",
            "/com/bt/rsqe/web/staticresources/scripts/OrdersTabTest.js");

        // Test Migration Order Journey
        migrationQuote = true;
        model.getOrders().clear();
        model.setOrder(model.new Order("orderId1", "orderName1", "created", OrderStatus.CREATED.getValue(), "offerName1", migrationQuote, true, null, false,orderItemSite));
        render = new Presenter().render(View.viewUsingTemplate("/com/bt/rsqe/projectengine/web/OrdersTab.ftl")
                                                         .withContext("view", model));
        document = parseHtml(render);
        run(document.asXML(),
            "/com/bt/rsqe/web/staticresources/scripts/OrdersTab.js",
            "/com/bt/rsqe/web/staticresources/scripts/MigrateProductTest.js");

        //Test BOM ASync Submission
        migrationQuote = false;
        model.getOrders().clear();
        model.setOrder(model.new Order("orderId1", "orderName1", "created", OrderStatus.IN_PROGRESS.getValue(), "offerName1", migrationQuote, true, null, false,orderItemSite));
        render = new Presenter().render(View.viewUsingTemplate("/com/bt/rsqe/projectengine/web/OrdersTab.ftl")
                                            .withContext("view", model));
        document = parseHtml(render);
        run(document.asXML(),
            "/com/bt/rsqe/web/staticresources/scripts/OrdersTab.js",
            "/com/bt/rsqe/web/staticresources/scripts/BomSubmissionButtonHideTest.js");
    }

    @Test
    public void offerTabSuite() {
        final OfferDetailsModel offerDetailsModel =  mock(OfferDetailsModel.class);
        when(offerDetailsModel.getId()).thenReturn("offerId");
        when(offerDetailsModel.getCreatedDate()).thenReturn(new DateTime().toString());
        when(offerDetailsModel.isApproved()).thenReturn(false);
        when(offerDetailsModel.isActive()).thenReturn(false);
        final OfferDetailsTabView offerDetailsTabView = new OfferDetailsTabView("customer", "contractId", "projectId", "quoteOptionId",
                                                                                offerDetailsModel, "customerName", "exportPricingSheet");
        final String render = new Presenter().render(View.viewUsingTemplate("/com/bt/rsqe/projectengine/web/OfferDetails.ftl")
                                                         .withContext("view", offerDetailsTabView));

        final Document document = parseHtml(render);
        run(document.asXML(),
            "/com/bt/rsqe/web/staticresources/scripts/LineItemValidation.js",
            "/com/bt/rsqe/web/staticresources/scripts/CheckBoxGroup.js",
            "/com/bt/rsqe/web/staticresources/scripts/OfferDetails.js",
            "/com/bt/rsqe/web/staticresources/scripts/OfferDetailsTest.js");
    }

    @Test
    public void runSuccessMessageTestSuite() {
        String htmlUnderTest = presenter.render(View.viewUsingTemplate("com/bt/rsqe/web/staticresources/scripts/StatusMessage_test.ftl"));
        run(htmlUnderTest,
            "/com/bt/rsqe/web/staticresources/scripts/StatusMessage.js",
            "/com/bt/rsqe/web/staticresources/scripts/StatusMessageTest.js"
        );
    }

    @Test
    public void runSelectNewSiteFormSuite() throws Exception {
        AddOrModifyProductView view = new AddOrModifyProductView("54", "123", "projectId", "quoteOptionId", Collections.EMPTY_LIST, new UriFactoryImpl(ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig()), "GBP", "NAME", "revenue owner", false, ProductAction.SelectNewSite.description(), "1", "orderType", "subOrderType");
        final Products products = ProductsFixture.aProducts()
            .withProduct("2", "RSQE Y", false, CAT_CODE, CAT_NAME, MoveConfigurationTypeEnum.NOT_MOVEABLE, null)
            .build();
        view.setProducts(products);

        String htmlUnderTest = presenter.render(View.viewUsingTemplate("com/bt/rsqe/projectengine/web/AddProductPage.ftl")
                                                    .withContext("view", view));

        htmlUnderTest += presenter.render(View.viewUsingTemplate("com/bt/rsqe/projectengine/web/NewSiteForm.ftl")
                                              .withContext("view", view));

        run(htmlUnderTest/*,WebDriverImplementation.FIREFOX.create()*/,
            "/com/bt/rsqe/web/staticresources/scripts/Navigate.js",
            "/com/bt/rsqe/web/staticresources/scripts/NewSiteForm.js",
            "/com/bt/rsqe/web/staticresources/scripts/dataTables.fnUpdateRowWithNewSiteData.js",
            "/com/bt/rsqe/web/staticresources/scripts/NewSiteFormTest.js");
    }

    @Test
    public void ProductPricing() {
        final QuoteOptionDetailsView model = aQuoteOptionDetailsView()
            .withIdentifiers("1", "2", "3", "4")
            .withProducts(new Products(asList(SellableProductFixture.aProduct().withId("prod1").withName("name1").withCategory(CAT_CODE, CAT_NAME).withIsImportable(false).build(),
                                              SellableProductFixture.aProduct().withId("blah-code").withName("blah-name").withCategory(CAT_CODE, CAT_NAME).withIsImportable(true).build())))
            .withUriFactory(new UriFactoryImpl(config)).build();
        final Document detailsTab = parseHtml(new Presenter().render(View.viewUsingTemplate("/com/bt/rsqe/projectengine/web/QuoteOptionDetailsTab.ftl").withContext("view", model)));
        insertLineItemsTable(detailsTab);
        run(detailsTab.asXML(),
            "/com/bt/rsqe/web/staticresources/scripts/DataTable.js",
            "/com/bt/rsqe/web/staticresources/scripts/CheckBoxGroup.js",
            "/com/bt/rsqe/web/staticresources/scripts/ProductPricing.js",
            "/com/bt/rsqe/web/staticresources/scripts/ProductPricingTest.js"
        );
    }

    @Test
    public void ImportProduct() {
        final QuoteOptionDetailsView model = aQuoteOptionDetailsView()
            .withIdentifiers("1", "2", "3", "4")
            .withProducts(new Products(asList(SellableProductFixture.aProduct().withId("prod1").withName("name1").withCategory(CAT_CODE, CAT_NAME).withIsImportable(false).build(),
                                              SellableProductFixture.aProduct().withId("blah-code").withName("blah-name").withCategory(CAT_CODE, CAT_NAME).withIsImportable(true).build())))
            .withUriFactory(new UriFactoryImpl(config)).build();
        final Document detailsTab = parseHtml(new Presenter().render(View.viewUsingTemplate("/com/bt/rsqe/projectengine/web/QuoteOptionDetailsTab.ftl").withContext("view", model)));
        insertLineItemsTable(detailsTab);
        run(detailsTab.asXML(),
            "/com/bt/rsqe/web/staticresources/scripts/DataTable.js",
            "/com/bt/rsqe/web/staticresources/scripts/CheckBoxGroup.js",
            "/com/bt/rsqe/web/staticresources/scripts/ImportProduct.js",
            "/com/bt/rsqe/web/staticresources/scripts/ImportProductTest.js"
        );
    }

    private void insertLineItemsTable(Document detailsTab) {
        ((Element) detailsTab.selectSingleNode("//div[contains(@class,'leftPaneContainer')]")).appendContent(lineItemsTable());
    }

    private Document lineItemsTable() {
        return parseTemplate("com/bt/rsqe/web/staticresources/scripts/line_items.ftl");
    }

    private void run(String htmlUnderTest, String... jsPaths) {
        run(htmlUnderTest, webDriver, jsPaths);
    }

    private void run(String htmlUnderTest, WebDriver driver, String... jsPaths) {
        try {
            JsTestPage jsTestPage = new JsTestPage(driver,
                                                   applicationBaseUri+"/com/bt/rsqe/web/staticresources/Test.ftl?to_disable_caching=" + new Random().nextInt());
            assertThat(jsPaths.length, greaterThan(0));
            assertTrue("Ensure that the last file included is the test js file", jsPaths[jsPaths.length - 1].endsWith("Test.js"));
            application.setupContextForResponse(htmlUnderTest, jsPaths);
            jsTestPage.runJsTests();
            assertTrue("No specs were run. Have you written specs? Does the file " + jsPaths[jsPaths.length - 1] + " exist?", jsTestPage.ranAtleastOneSpec());
            if (!jsTestPage.testPassed()) {
                fail(String.format("JS test failed: %s\nDetails: %s", jsTestPage.specResult(), jsTestPage.specOutput()));//lazy eval of specResult,specOutput - costly operations
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
