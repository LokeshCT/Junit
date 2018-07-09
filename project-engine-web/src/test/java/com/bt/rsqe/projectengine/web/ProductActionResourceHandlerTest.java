package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.domain.ErrorNotificationDTO;
import com.bt.rsqe.domain.ErrorNotificationEvent;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.enums.MoveConfigurationTypeEnum;
import com.bt.rsqe.enums.ProductAction;
import com.bt.rsqe.matchers.CompositeMatcher;
import com.bt.rsqe.pc.client.ConfiguratorProductClient;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.PriceBookFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.AddProductOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.MigrateProductOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.ModifyProductOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.MoveProductOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.ProductOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.ProductOrchestratorFactory;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.SelectNewSiteProductOrchestrator;
import com.bt.rsqe.projectengine.web.view.AddOrModifyProductView;
import com.bt.rsqe.projectengine.web.view.PageView;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.mappers.ProductCreationJsonObject;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.ws.rs.core.MultivaluedHashMap;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.TypeSafeMatcher;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.bt.rsqe.enums.ProductAction.*;
import static com.bt.rsqe.matchers.ResponseMatcher.*;
import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Lists.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(JMock.class)
public class ProductActionResourceHandlerTest {

    private final static String CUSTOMER_ID = "CUSTOMER_ID";
    private final static String CONTRACT_ID = "CONTRACT_ID";
    private final static String PROJECT_ID = "projectId";
    private final static String QUOTE_OPTION_ID = "quoteOptionId";
    private final static String SITE_ID = "aSiteId";
    private final static String PRODUCT_VERSION = "A.1";
    private final static String ORDER_TYPE = "orderType";
    private final static String SUB_ORDER_TYPE = "subOrderType";
    private final static List<String> PRODUCT_SITES_SELECTED_LIST = new ArrayList<String>() {{add("bfgSiteId");}};
    private final static String PRODUCT_ID = "productId";
    private String NEW_SITE_ID = "newSiteId";
    private Mockery context;
    private SelectNewSiteProductOrchestrator selectNewSiteProductOrchestrator;
    private AddProductOrchestrator addProductOrchestrator;
    private ModifyProductOrchestrator modifyProductOrchestrator;
    private MoveProductOrchestrator moveProductOrchestrator;
    private MigrateProductOrchestrator migrateProductOrchestrator;
    private ProductActionResourceHandler resourceHandler;
    private BreadCrumbFactory breadCrumbFactory;
    private UriInfo uriInfo;
    private PriceBookFacade priceBookFacade;
    private SiteFacade siteFacade;
    private LineItemFacade lineItemFacade;
    private Presenter presenter;
    private ProjectResource projects;
    private CustomerResource customerResource;
    private ConfiguratorProductClient configuratorProductClient;
    private PmrClient pmrClient;
    private ProductOrchestratorFactory productOrchestratorFactory;
    private String submitWebMetricsUri = "/rsqe/web-metrics";
    private final String helpLinkUri = "";

    @Before
    public void before() throws Exception {
        context = new JUnit4Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        siteFacade = context.mock(SiteFacade.class);
        lineItemFacade = context.mock(LineItemFacade.class);
        configuratorProductClient = context.mock(ConfiguratorProductClient.class);

        selectNewSiteProductOrchestrator = context.mock(SelectNewSiteProductOrchestrator.class);
        addProductOrchestrator = context.mock(AddProductOrchestrator.class);
        modifyProductOrchestrator = context.mock(ModifyProductOrchestrator.class);
        moveProductOrchestrator = context.mock(MoveProductOrchestrator.class);
        migrateProductOrchestrator = context.mock(MigrateProductOrchestrator.class);
        productOrchestratorFactory = mock(ProductOrchestratorFactory.class);

        when(productOrchestratorFactory.getOrchestratorFor(ProductAction.Provide)).thenReturn(addProductOrchestrator);
        when(productOrchestratorFactory.getOrchestratorFor(ProductAction.Modify)).thenReturn(modifyProductOrchestrator);
        when(productOrchestratorFactory.getOrchestratorFor(ProductAction.Move)).thenReturn(moveProductOrchestrator);
        when(productOrchestratorFactory.getOrchestratorFor(ProductAction.Migrate)).thenReturn(migrateProductOrchestrator);
        when(productOrchestratorFactory.getOrchestratorFor(ProductAction.SelectNewSite)).thenReturn(selectNewSiteProductOrchestrator);

        uriInfo = context.mock(UriInfo.class);
        breadCrumbFactory = context.mock(BreadCrumbFactory.class);
        priceBookFacade = mock(PriceBookFacade.class);
        presenter = mock(Presenter.class);
        pmrClient = mock(PmrClient.class);
        resourceHandler = new ProductActionResourceHandler(presenter, siteFacade, lineItemFacade, productOrchestratorFactory, breadCrumbFactory,
                                                           priceBookFacade, projects, customerResource, configuratorProductClient, pmrClient, submitWebMetricsUri, helpLinkUri);
        projects = mock(ProjectResource.class);
        customerResource = mock(CustomerResource.class);
    }

    @Test
    public void shouldCreateAddProductPage() throws Exception {
        final List<BreadCrumb> breadCrumbList = newArrayList();
        final PageView pageView = new PageView("GBP", "NAME", breadCrumbList);
        final UserContext userContext = anIndirectUserContext().withIndirectUser().build();
        UserContextManager.setCurrent(userContext);

        context.checking(new Expectations() {{
            oneOf(breadCrumbFactory).createBreadCrumbsForOfferResource(PROJECT_ID, QUOTE_OPTION_ID);
            will(returnValue(pageView));
        }});

        resourceHandler.addProduct(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);

        context.assertIsSatisfied();
    }

    @Test
    public void shouldAddProductTabs() throws Exception {
        assertProductTabCreated(addProductOrchestrator, Provide);
        assertProductTabCreated(modifyProductOrchestrator, Modify);
        assertProductTabCreated(moveProductOrchestrator, Move);
        assertProductTabCreated(migrateProductOrchestrator, Migrate);
    }

    @Test
    public void shouldGetLaunchedValueWithSalesChannel() {
        final String salesChannel = "Test Channel";
        final String returnVal = "Yes";
        context.checking(new Expectations() {{

            oneOf(addProductOrchestrator).getLaunched(salesChannel, "SCODE");
            will(returnValue("Yes"));
        }});

        Response response = resourceHandler.getLaunched(salesChannel, "SCODE");
        assertThat(response, is(aResponse().withStatus(OK)));
        assertThat(response.getEntity().toString(), is(returnVal));
    }

    @Test
    public void shouldCreateProduct() {
        final String returnVal = "1";
        context.checking(new Expectations() {{
            oneOf(configuratorProductClient).createProduct(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, "jsonString");
            will(returnValue(returnVal));
        }});

        Response response = resourceHandler.createProduct("jsonString", CUSTOMER_ID, CONTRACT_ID, PROJECT_ID);
        assertThat(response, is(aResponse().withStatus(OK)));
        assertThat(response.getEntity().toString(), is(returnVal));
        context.assertIsSatisfied();
    }

    @Test
    public void shouldGetSitesForProvideJourney() throws Exception {
        final String sCode = "sCode";

        context.checking(new Expectations() {{

            oneOf(addProductOrchestrator).buildSitesView(with(CUSTOMER_ID), with(PROJECT_ID), with(any(PaginatedFilter.class)), with(sCode), with(""), with(any(List.class)), with(any(Optional.class)));
            will(returnValue(null));
            oneOf(uriInfo).getQueryParameters(true);
            will(returnValue(new MultivaluedHashMap()));
        }});

        resourceHandler.getSites(CUSTOMER_ID, PROJECT_ID, 0, 1, 1, sCode, Provide.description(), new ArrayList<String>(), "", "", uriInfo);

        context.assertIsSatisfied();
    }

    @Test
    public void shouldGetSitesForModifyCeaseJourneyWithProductVersion() throws Exception {
        final String sCode = "sCode";

        context.checking(new Expectations() {{

            oneOf(modifyProductOrchestrator).buildSitesView(with(CUSTOMER_ID), with(PROJECT_ID), with(any(PaginatedFilter.class)), with(sCode), with(""), with(any(List.class)), with(Optional.of(PRODUCT_VERSION)));
            will(returnValue(null));
            oneOf(uriInfo).getQueryParameters(true);
            will(returnValue(new MultivaluedHashMap()));
        }});

        resourceHandler.getSites(CUSTOMER_ID, PROJECT_ID, 0, 1, 1, sCode, Modify.description(), new ArrayList<String>(), "", PRODUCT_VERSION, uriInfo);

        context.assertIsSatisfied();
    }

    @Test
    public void shouldGetSitesForModifyCeaseJourneyWhenProductVersionNotSet() throws Exception {
        final String sCode = "sCode";

        context.checking(new Expectations() {{

            oneOf(modifyProductOrchestrator).buildSitesView(with(CUSTOMER_ID), with(PROJECT_ID), with(any(PaginatedFilter.class)), with(sCode), with(""), with(any(List.class)), with(Optional.<String>absent()));
            will(returnValue(null));
            oneOf(uriInfo).getQueryParameters(true);
            will(returnValue(new MultivaluedHashMap()));
        }});

        resourceHandler.getSites(CUSTOMER_ID, PROJECT_ID, 0, 1, 1, sCode, Modify.description(), new ArrayList<String>(), "", null, uriInfo);

        context.assertIsSatisfied();
    }

    @Test
    public void shouldGetInitialSitesForMoveJourney() throws Exception {
        final String sCode = "sCode";

        context.checking(new Expectations() {{

            oneOf(moveProductOrchestrator).buildSitesView(with(CUSTOMER_ID), with(PROJECT_ID), with(any(PaginatedFilter.class)), with(sCode), with(aNull(String.class)), with(any(List.class)), with(any(Optional.class)));
            will(returnValue(null));
            oneOf(uriInfo).getQueryParameters(true);
            will(returnValue(new MultivaluedHashMap()));
        }});

        resourceHandler.getSites(CUSTOMER_ID, PROJECT_ID, 0, 1, 1, sCode, Move.description(), new ArrayList<String>(), null, "", uriInfo);

        context.assertIsSatisfied();
    }

    @Test
    public void shouldReturnExistingAndNewSitesForMoveJourney() throws Exception {
        final String sCode = "sCode";
        final List<String> existingSiteIds = newArrayList();
        final String newSiteId = "newSiteId";

        context.checking(new Expectations() {{
            oneOf(moveProductOrchestrator).buildSitesView(with(CUSTOMER_ID), with(PROJECT_ID), with(any(PaginatedFilter.class)), with(sCode), with(newSiteId), with(existingSiteIds), with(Optional.of("")));
            will(returnValue(null));
            oneOf(uriInfo).getQueryParameters(true);
            will(returnValue(new MultivaluedHashMap()));
        }});

        resourceHandler.getSites(CUSTOMER_ID, PROJECT_ID, 0, 1, 1, sCode, Move.description(), new ArrayList<String>(), newSiteId, "", uriInfo);

        context.assertIsSatisfied();
    }

    @Test
    public void shouldGetNewSitesForMoveJourney() throws Exception {
        final String sCode = "sCode";

        context.checking(new Expectations() {{

            oneOf(selectNewSiteProductOrchestrator).buildSitesView(with(CUSTOMER_ID), with(PROJECT_ID), with(any(PaginatedFilter.class)), with(sCode), with(""), with(any(List.class)), with(Optional.of("")));
            will(returnValue(null));
            oneOf(uriInfo).getQueryParameters(true);
            will(returnValue(new MultivaluedHashMap()));
        }});

        resourceHandler.getSites(CUSTOMER_ID, PROJECT_ID, 0, 1, 1, sCode, SelectNewSite.description(), new ArrayList<String>(), "", "", uriInfo);

        context.assertIsSatisfied();
    }

    @Test
    public void shouldFetchPriceBooksForInDirectUser() throws Exception {
        //Given
        UserContext userContext = anIndirectUserContext().withIndirectUser().build();
        UserContextManager.setCurrent(userContext);

        List<PriceBookDTO> priceBooks = newArrayList();
        priceBooks.add(new PriceBookDTO("id", "someRequestId", "R7", "R8", null, null));
        when(priceBookFacade.inDirectPriceBooks(CUSTOMER_ID, PRODUCT_ID, ProductCategoryCode.NIL)).thenReturn(priceBooks);

        //When
        Response response = resourceHandler.getPriceBook(CUSTOMER_ID, PRODUCT_ID);

        //Then
        verify(priceBookFacade).inDirectPriceBooks(CUSTOMER_ID, PRODUCT_ID, ProductCategoryCode.NIL);
        assertThat(response, aResponse().withStatus(OK));
        assertThat((List<PriceBookDTO>) response.getEntity(), is(priceBooks));
    }

    @Test
    public void shouldNotInvokePriceBookFacadeForIndirectPriceBooksForDirectUser() throws Exception {
        //Given
        UserContext userContext = aDirectUserContext().build();
        UserContextManager.setCurrent(userContext);

        //When
        Response response = resourceHandler.getPriceBook(CUSTOMER_ID, PRODUCT_ID);

        //Then
        verify(priceBookFacade, never()).inDirectPriceBooks(CUSTOMER_ID, PRODUCT_ID, ProductCategoryCode.NIL);
        assertThat(response, aResponse().withStatus(OK));
        assertThat(((List<PriceBookDTO>) response.getEntity()).size(), is(0));
    }

    @Test
    public void shouldReturnErrorNotificationIfContractCardinalityCheckFails() {
        //Given
        final Notification contractNotification = new Notification();
        final Notification siteNotification = new Notification();
        contractNotification.addEvent(new ErrorNotificationEvent("Contract Cardinality Error"));
        siteNotification.addEvent(new ErrorNotificationEvent("Site Cardinality Error"));
        context.checking(new Expectations() {{
            oneOf(addProductOrchestrator).contractCardinalityCheck(CUSTOMER_ID, CONTRACT_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID, 1);
            will(returnValue(contractNotification));
            oneOf(addProductOrchestrator).siteCardinalityCheck(PROJECT_ID, CUSTOMER_ID, SITE_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID);
            will(returnValue(siteNotification));
        }});

        //When
        Response response = resourceHandler.cardinalityCheck(PROJECT_ID, CUSTOMER_ID, CONTRACT_ID, formInput());

        //Then
        assertThat(response, aResponse().withStatus(OK));
        final ErrorNotificationDTO notificationDTO = JSONSerializer.getInstance().deSerialize((String)response.getEntity(), ErrorNotificationDTO.class);
        Notification expectedNotification = new Notification();
        expectedNotification.add(contractNotification);
        expectedNotification.add(siteNotification);
        assertThat(notificationDTO, is(new ErrorNotificationDTO(expectedNotification)));
    }

    @Test
    public void shouldReturnNoErrorWhenDoingMoveSameSite() throws Exception {
        //Given
        final Notification contractNotification = new Notification();
        context.checking(new Expectations() {{
            oneOf(addProductOrchestrator).contractCardinalityCheck(CUSTOMER_ID, CONTRACT_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID, 1);
            will(returnValue(contractNotification));
        }});

        //When
        Response response = resourceHandler.cardinalityCheck(PROJECT_ID, CUSTOMER_ID, CONTRACT_ID, formInput("Move", SITE_ID));

        //Then
        assertThat(response, aResponse().withStatus(OK));
        final ErrorNotificationDTO notificationDTO = JSONSerializer.getInstance().deSerialize((String)response.getEntity(), ErrorNotificationDTO.class);
        Notification expectedNotification = new Notification();
        assertThat(notificationDTO, is(new ErrorNotificationDTO(expectedNotification)));
    }

    @Test
    public void shouldReturnErrorForNewSiteCardinalityCheckWhenInMoveSite() throws Exception {
        //Given
        final Notification contractNotification = new Notification();
        final Notification siteNotification = new Notification();
        siteNotification.addEvent(new ErrorNotificationEvent("Site Cardinality Error"));
        context.checking(new Expectations() {{
            oneOf(addProductOrchestrator).contractCardinalityCheck(CUSTOMER_ID, CONTRACT_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID, 1);
            will(returnValue(contractNotification));
            oneOf(addProductOrchestrator).siteCardinalityCheck(PROJECT_ID, CUSTOMER_ID, NEW_SITE_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID);
            will(returnValue(siteNotification));
        }});

        //When
        Response response = resourceHandler.cardinalityCheck(PROJECT_ID, CUSTOMER_ID, CONTRACT_ID, formInput("Move", NEW_SITE_ID));

        //Then
        assertThat(response, aResponse().withStatus(OK));
        final ErrorNotificationDTO notificationDTO = JSONSerializer.getInstance().deSerialize((String)response.getEntity(), ErrorNotificationDTO.class);
        Notification expectedNotification = new Notification();
        expectedNotification.add(contractNotification);
        expectedNotification.add(siteNotification);
        assertThat(notificationDTO, is(new ErrorNotificationDTO(expectedNotification)));
    }

    @Test
    public void shouldCreateSelectNewSiteForm() throws Exception {
        final UserContext userContext = anIndirectUserContext().withIndirectUser().build();
        UserContextManager.setCurrent(userContext);

        context.checking(new Expectations() {{
            oneOf(addProductOrchestrator).buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, userContext.getPermissions().indirectUser, Provide.description());
            will(returnValue(new AddOrModifyProductView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, Collections.EMPTY_LIST, null, "GBP", "NAME", "BT", userContext.getPermissions().indirectUser, ProductAction.Provide.description(), "1", ORDER_TYPE, SUB_ORDER_TYPE)));
        }});

        resourceHandler.selectNewSiteForm(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldGetServicesForModifyJourney() throws Exception {
        final String sCode = "sCode";

        context.checking(new Expectations() {{
            oneOf(modifyProductOrchestrator).buildServicesView(with(CUSTOMER_ID), with(sCode), with(CONTRACT_ID), with(PRODUCT_VERSION),
                    with(any(Pagination.class)));
            will(returnValue(null));
        }});

        resourceHandler.getServices(CUSTOMER_ID, 0, 1, 1, sCode, Modify.description(), CONTRACT_ID, PRODUCT_VERSION);

        context.assertIsSatisfied();
    }

    @Test
    public void shouldGetListOfVisibleInSummaryAttributesForAService() throws Exception {
        final String sCode = "sCode";
        //When
        Pmr.ProductOfferings productOfferings = mock(Pmr.ProductOfferings.class);
        ProductOffering productOffering = mock(ProductOffering.class);
        List<String> attributes = newArrayList("attr 1", "attr 2", "attr 3");
        when(productOffering.getVisibleInSummaryAttributeDisplayNames()).thenReturn(attributes);
        when(productOfferings.get()).thenReturn(productOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(sCode))).thenReturn(productOfferings);
        Response response = resourceHandler.getVisibleInSummaryAttributes(sCode);

        //Then
        assertThat(response, aResponse().withStatus(OK));
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = (JsonObject)parser.parse((String) response.getEntity());

        assertThat(jsonObject.getAsJsonArray("names").size(), is(3));
        verify(pmrClient, times(1)).productOffering(ProductSCode.newInstance(sCode));
    }

    @Test
    public void shouldReturnNoEndOfLifeErrorWhenDoingMoveSite() throws Exception {
        final Notification endOfLifeNotification = new Notification();

        moveProductOrchestrator = mock(MoveProductOrchestrator.class);
        when(moveProductOrchestrator.endOfLifeCheck(eq("aSiteId"), eq("productId"), eq("A.1"), any(Date.class), any(String.class), any(String.class))).thenReturn(endOfLifeNotification);
        when(productOrchestratorFactory.getOrchestratorFor(ProductAction.Move)).thenReturn(moveProductOrchestrator);
        resourceHandler = new ProductActionResourceHandler(presenter, siteFacade, lineItemFacade, productOrchestratorFactory, breadCrumbFactory,
                                                           priceBookFacade, projects, customerResource, configuratorProductClient, pmrClient, submitWebMetricsUri, helpLinkUri);

        Response response = resourceHandler.endOfLifeValidation(formInput("Move", "sameSite"));

        assertThat(response, aResponse().withStatus(OK));
        final ErrorNotificationDTO notificationDTO = JSONSerializer.getInstance().deSerialize((String)response.getEntity(), ErrorNotificationDTO.class);
        Notification expectedNotification = new Notification();
        assertThat(notificationDTO, is(new ErrorNotificationDTO(expectedNotification)));
    }

    @Test
    public void shouldReturnEndOfLifeErrorWhenDoingMoveSite() throws Exception {
        final Notification endOfLifeNotification = new Notification();
        endOfLifeNotification.addEvent(new ErrorNotificationEvent("End of Life Error"));

        moveProductOrchestrator = mock(MoveProductOrchestrator.class);
        when(moveProductOrchestrator.endOfLifeCheck(eq("aSiteId"), eq("productId"), eq("A.1"), any(Date.class), any(String.class), any(String.class))).thenReturn(endOfLifeNotification);
        when(productOrchestratorFactory.getOrchestratorFor(ProductAction.Move)).thenReturn(moveProductOrchestrator);
        resourceHandler = new ProductActionResourceHandler(presenter, siteFacade, lineItemFacade, productOrchestratorFactory, breadCrumbFactory,
                                                           priceBookFacade, projects, customerResource, configuratorProductClient, pmrClient, submitWebMetricsUri, helpLinkUri);

        Response response = resourceHandler.endOfLifeValidation(formInput("Move", "sameSite"));

        assertThat(response, aResponse().withStatus(OK));
        final ErrorNotificationDTO notificationDTO = JSONSerializer.getInstance().deSerialize((String)response.getEntity(), ErrorNotificationDTO.class);
        Notification expectedNotification = new Notification();
        expectedNotification.addEvent(new ErrorNotificationEvent("End of Life Error"));
        assertThat(notificationDTO, is(new ErrorNotificationDTO(expectedNotification)));
    }


    @After
    public void tearDown() throws Exception {
        UserContextManager.clear();
    }

    private String formInput(String action, String newSite) {
        ArrayList<ProductCreationJsonObject.ActionDTO> actionDTOs = newArrayList(
            new ProductCreationJsonObject.ActionDTO(SITE_ID, "", "")
        );
        ProductCreationJsonObject jsonObject = new ProductCreationJsonObject("quoteOptionId", PRODUCT_ID, CUSTOMER_ID, CONTRACT_ID, actionDTOs,
                                                                             PRODUCT_ID, PRODUCT_VERSION, "url", action, "status", newSite,
                                                                             "false", MoveConfigurationTypeEnum.NOT_MOVEABLE, null, "aProductCategoryCode", null, null, null);
        return JSONSerializer.getInstance().serialize(jsonObject);
    }
    private String formInput() {
        return formInput("action", null);
    }



    private class NotificationMatcher extends CompositeMatcher<JsonObject> {

        public NotificationMatcher with(String key, Object value) {
            assertions.add(new ErrorStatusMatcher(key, value));
            return this;
        }

    }


    private class ErrorEventMessageMatcher extends TypeSafeMatcher<JsonObject> {

        private String message;

        ErrorEventMessageMatcher(String message) {
            this.message = message;
        }

        @Override
        public boolean matchesSafely(JsonObject notifications) {
            JsonArray errors = notifications.getAsJsonArray("errors");
            return Iterables.any(errors, new Predicate<JsonElement>() {
                @Override
                public boolean apply(JsonElement input) {
                    return input.getAsString().equals(message);
                }
            });
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Description").appendValue(message);
        }
    }

    private void assertProductTabCreated(final ProductOrchestrator productOrchestrator, final ProductAction action) {
        final UserContext userContext = anIndirectUserContext().withIndirectUser().build();
        UserContextManager.setCurrent(userContext);

        context.checking(new Expectations() {{
            oneOf(productOrchestrator).buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, userContext.getPermissions().indirectUser, action.description());
            will(returnValue(new AddOrModifyProductView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, Collections.EMPTY_LIST, null, "GBP", "NAME", "BT", userContext.getPermissions().indirectUser, action.description(), "1", ORDER_TYPE, SUB_ORDER_TYPE)));
        }});

        resourceHandler.productTab(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, action.description());

        context.assertIsSatisfied();
    }

    private class ErrorStatusMatcher extends TypeSafeMatcher<JsonObject> {
        private final String key;
        private final Object value;

        public ErrorStatusMatcher(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean matchesSafely(JsonObject jsonObject) {
            return jsonObject.has(key) && value.equals(jsonObject.get(key).getAsBoolean());
        }

        @Override
        public void describeTo(Description description) {}
    }
}
