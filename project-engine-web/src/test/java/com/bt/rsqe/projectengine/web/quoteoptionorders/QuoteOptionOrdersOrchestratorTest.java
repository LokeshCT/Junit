package com.bt.rsqe.projectengine.web.quoteoptionorders;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.AssetSourceVersion;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.domain.ProductCategoryMigration;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.inlife.client.ApplicationPropertyResourceClient;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.OfferDTO;
import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.OrderStatus;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.RfoValidDTO;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.projectengine.web.OfferAndOrderValidationResult;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOfferFacade;
import com.bt.rsqe.projectengine.web.InVisibleCreatableLineItemRetriever;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOrderFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.view.QuoteOptionOrdersView;
import com.bt.rsqe.security.UserDTO;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture.aQuoteOptionItemDTO;
import static com.bt.rsqe.projectengine.web.quoteoptionorders.QuoteOptionOrdersOrchestratorTestFixture.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(JMock.class)
public class QuoteOptionOrdersOrchestratorTest {

    private static final String OFFER_LINE_ITEM_1 = "lineItemId1";
    private static final String OFFER_LINE_ITEM_2 = "lineItemId2";
    private static final String ORDER_NAME = "orderName";
    private static final String ORDER_ID_1 = "orderId1";
    private static final String ORDER_ID_2 = "orderId2";
    private static final String ORDER_ID_3 = "orderId3";
    private static final String ORDER_ID_4 = "orderId4";
    private static final String ORDER_NAME_1 = "name1";
    private static final String ORDER_NAME_2 = "name2";
    private static final String ORDER_NAME_3 = "name3";
    private static final String ORDER_NAME_4 = "name4";
    private final Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private final QuoteOptionOrderFacade orderFacade = context.mock(QuoteOptionOrderFacade.class);
    private final ProjectResource projectResource = context.mock(ProjectResource.class);
    private final InVisibleCreatableLineItemRetriever inVisibleCreatableLineItemRetriever = context.mock(InVisibleCreatableLineItemRetriever.class);
    private QuoteOptionOrdersOrchestratorTestFixture quoteOptionOrdersOrchestratorTestFixture;
    private ProductInstanceClient productInstanceClient;
    private QuoteOptionOrderFacade quoteOptionOrderFacade;
    private OrderDTO orderDTO;
    private QuoteOptionItemDTO quoteOptionItemDTO;
    private QuoteOptionOfferFacade quoteOptionOfferFacade;
    private OfferDTO offerDTO;
    private QuoteMigrationDetailsProvider migrationDetailsProvider;
    private ProductInstance productInstance;
    private Pmr pmr;
    private LineItemFacade lineItemFacade;
    private CustomerResource customerResource;
    private ApplicationPropertyResourceClient applicationPropertyResourceClient;

    @Before
    public void before() {
        quoteOptionOrdersOrchestratorTestFixture = new QuoteOptionOrdersOrchestratorTestFixture();
        quoteOptionOrderFacade = mock(QuoteOptionOrderFacade.class);
        orderDTO = mock(OrderDTO.class);
        offerDTO = mock(OfferDTO.class);
        quoteOptionItemDTO = mock(QuoteOptionItemDTO.class);
        quoteOptionOfferFacade = mock(QuoteOptionOfferFacade.class);
        migrationDetailsProvider = mock(QuoteMigrationDetailsProvider.class);
        productInstance = mock(ProductInstance.class);
        productInstanceClient = mock(ProductInstanceClient.class);
        pmr = mock(Pmr.class);
        lineItemFacade = mock(LineItemFacade.class);
        customerResource =mock(CustomerResource.class);
        when(productInstance.getProjectId()).thenReturn(PROJECT_ID);
        when(productInstance.getQuoteOptionId()).thenReturn(QUOTE_OPTION_ID);
        when(productInstance.getProductIdentifier()).thenReturn(new ProductIdentifier("PRODUCT_ID", "1"));
        when(productInstanceClient.get(Matchers.<LineItemId>any())).thenReturn(productInstance);
        when(quoteOptionOfferFacade.getOffer(PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID)).thenReturn(offerDTO);
        when(migrationDetailsProvider.conditionalFor(productInstance)).thenCallRealMethod();
        when(migrationDetailsProvider.isMigrationQuote(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(Optional.of(true));
        ProductCategoryMigration productCategoryMigration =  new ProductCategoryMigration(true, true, false);
        when(migrationDetailsProvider.getMigrationDetailsForProductCategory(Matchers.<String>any())).thenReturn(Optional.of(productCategoryMigration));
        when(migrationDetailsProvider.getMigrationDetailsForProductCode(Matchers.<String>any())).thenReturn(Optional.of(productCategoryMigration));
        ArrayList<OrderDTO> orders = new ArrayList<OrderDTO>();
        orders.add(orderDTO);
        when(quoteOptionOrderFacade.getAll(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(orders);
        ArrayList<QuoteOptionItemDTO> quoteOptionItemDTOs = new ArrayList<QuoteOptionItemDTO>();
        quoteOptionItemDTO.offerId = OFFER_ID;
        when(quoteOptionItemDTO.getId()).thenReturn(QUOTE_OPTION_ITEM_ID);
        quoteOptionItemDTOs.add(quoteOptionItemDTO);
        when(orderDTO.getOrderItems()).thenReturn(quoteOptionItemDTOs);
        when(pmr.getBomDownloadableProducts()).thenReturn(newArrayList(new ProductIdentifier("PROD_100", "A.1")));
    }

    @Test
    public void shouldBuildView() throws Exception {

        QuoteOptionOrdersOrchestrator orchestrator =
            quoteOptionOrdersOrchestratorTestFixture.withOneOrder()
                                                    .withRfoValidDto(false)
                                                    .withQuoteMigrationDetailsProvider(migrationDetailsProvider)
                                                    .withProductInstanceClient(productInstanceClient)
                                                    .withPMR(pmr)
                                                    .build();

        QuoteOptionOrdersView view = orchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, USER_TOKEN);

        final OrderDTO storedOrder = quoteOptionOrdersOrchestratorTestFixture.getSingleOrder();
        final List<QuoteOptionOrdersView.Order> orders = view.getOrders();
        assertThat(orders.size(), is(1));
        final QuoteOptionOrdersView.Order actualOrder = orders.get(0);
        assertThat(actualOrder.getId(), is(storedOrder.id));
        assertTrue(actualOrder.getCreated().matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}"));
        assertThat(actualOrder.getStatus(), is(storedOrder.status));
        assertThat(actualOrder.getOfferName(), is(OFFER_NAME));
        assertThat(actualOrder.getSubmitLink(), is("/rsqe/customers/contracts/projects/quote-options/orders/OrderId/submit"));
        assertThat(actualOrder.isRfoValid(), is(false));
        assertThat(actualOrder.isSubmitButtonDisabled(), is(true));
    }

    @Test
    public void shouldBuildViewWithSubmitButtonEnabledWithRfoSheetIsValid() {
        QuoteOptionOrdersOrchestrator orchestrator =
            quoteOptionOrdersOrchestratorTestFixture.withOneOrder()
                                                    .withRfoValidDto(true)
                                                    .withQuoteMigrationDetailsProvider(migrationDetailsProvider)
                                                    .withProductInstanceClient(productInstanceClient)
                                                    .withPMR(pmr)
                                                    .build();

        QuoteOptionOrdersView view = orchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, USER_TOKEN);

        final List<QuoteOptionOrdersView.Order> orders = view.getOrders();
        assertThat(orders.size(), is(1));
        final QuoteOptionOrdersView.Order actualOrder = orders.get(0);
        assertThat(actualOrder.isRfoValid(), is(true));
        assertThat(actualOrder.isSubmitButtonDisabled(), is(false));
    }

    @Test
    public void shouldBuildViewWithMigrationQuoteTrueWhenOrderAssetsQualifyAsMigration() throws Exception {

        QuoteOptionOrdersOrchestrator orchestrator = new QuoteOptionOrdersOrchestrator(quoteOptionOrderFacade, quoteOptionOfferFacade, null,
                                                                                       migrationDetailsProvider, productInstanceClient, inVisibleCreatableLineItemRetriever, pmr, lineItemFacade,customerResource,applicationPropertyResourceClient);
        RfoValidDTO rfoValidDTO = new RfoValidDTO();
        rfoValidDTO.setValue(false);
        when(quoteOptionOrderFacade.isRfoValid("", "", null)).thenReturn(rfoValidDTO);
        QuoteOptionOrdersView view = orchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, USER_TOKEN);

        final List<QuoteOptionOrdersView.Order> viewOrders = view.getOrders();
        assertThat(viewOrders.size(), is(1));
        final QuoteOptionOrdersView.Order actualOrder = viewOrders.get(0);
        assertThat(actualOrder.isMigrationQuote(), is(true));
        assertThat(actualOrder.isRfoValid(), is(false));
    }

    @Test
    public void shouldBuildViewWithMigrationQuoteFalseWhenOrderAssetsDoNotQualifyAsMigration() throws Exception {

        when(migrationDetailsProvider.isMigrationQuote(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(Optional.of(false));

        QuoteOptionOrdersOrchestrator orchestrator = new QuoteOptionOrdersOrchestrator(quoteOptionOrderFacade, quoteOptionOfferFacade, null,
                                                                                       migrationDetailsProvider, productInstanceClient, inVisibleCreatableLineItemRetriever, pmr, lineItemFacade,customerResource,applicationPropertyResourceClient);
        RfoValidDTO rfoValidDTO = new RfoValidDTO();
        rfoValidDTO.setValue(false);
        when(quoteOptionOrderFacade.isRfoValid("", "", null)).thenReturn(rfoValidDTO);
        QuoteOptionOrdersView view = orchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, USER_TOKEN);

        final List<QuoteOptionOrdersView.Order> viewOrders = view.getOrders();
        assertThat(viewOrders.size(), is(1));
        final QuoteOptionOrdersView.Order actualOrder = viewOrders.get(0);
        assertThat(actualOrder.isMigrationQuote(), is(false));
    }

    @Test
    public void shouldSortOrdersByDateCreatedDescending() throws Exception {

        QuoteOptionOrdersOrchestrator orchestrator =
            quoteOptionOrdersOrchestratorTestFixture
                .withOrder(ORDER_ID_1, ORDER_NAME_1, new DateTime("2011-01-01T12:30:55.000+00:00").toString(), OrderStatus.CREATED.toString())
                .withOrder(ORDER_ID_2, ORDER_NAME_2, new DateTime("2011-03-01T12:30:55.000+00:00").toString(), OrderStatus.CREATED.toString())
                .withOrder(ORDER_ID_3, ORDER_NAME_3, new DateTime("2011-03-01T12:30:59.000+00:00").toString(), OrderStatus.CREATED.toString())
                .withOrder(ORDER_ID_4, ORDER_NAME_4, new DateTime("2011-04-07T13:30:55.000+00:00").toString(), OrderStatus.CREATED.toString())
                .withQuoteMigrationDetailsProvider(migrationDetailsProvider)
                .withProductInstanceClient(productInstanceClient)
                .withRfoValidDto(ORDER_ID_1, false)
                .withRfoValidDto(ORDER_ID_2, false)
                .withRfoValidDto(ORDER_ID_3, false)
                .withRfoValidDto(ORDER_ID_4, false)
                .withPMR(pmr)
                .build();

        QuoteOptionOrdersView view = orchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, USER_TOKEN);

        final List<QuoteOptionOrdersView.Order> orders = view.getOrders();
        assertThat(orders.size(), is(4));
        assertThat(orders.get(0).getId(), is(ORDER_ID_4));
        assertThat(orders.get(1).getId(), is(ORDER_ID_3));
        assertThat(orders.get(2).getId(), is(ORDER_ID_2));
        assertThat(orders.get(3).getId(), is(ORDER_ID_1));

    }

    @Test
    public void shouldBuildAnOrderWithMultipleLineItems() {
        final OrderDTO orderDTO = OrderDTO.newInstance("", "", new ArrayList<QuoteOptionItemDTO>());

        context.checking(new Expectations() {{
            oneOf(inVisibleCreatableLineItemRetriever).whatInVisibleLineItemsIHaveCreated(OFFER_LINE_ITEM_1);
            will(returnValue(Collections.emptySet()));
            oneOf(inVisibleCreatableLineItemRetriever).whatInVisibleLineItemsIHaveCreated(OFFER_LINE_ITEM_2);
            will(returnValue(Collections.emptySet()));
            oneOf(orderFacade).createOrder(ORDER_NAME, PROJECT_ID, QUOTE_OPTION_ID, asList(OFFER_LINE_ITEM_1, OFFER_LINE_ITEM_2));
            will(returnValue(orderDTO));
            ignoring(projectResource).updateStatus(PROJECT_ID);

        }});

        QuoteOptionOrdersOrchestrator orchestrator = quoteOptionOrdersOrchestratorTestFixture.withOrderFacade(orderFacade).withProjectResource(projectResource)
                                                                                             .withinVisibleCreatableLineItemResolver(inVisibleCreatableLineItemRetriever).build();

        final String quoteOptionItemIds = OFFER_LINE_ITEM_1 + "," + OFFER_LINE_ITEM_2;
        orchestrator.buildOrder(ORDER_NAME, PROJECT_ID, QUOTE_OPTION_ID, quoteOptionItemIds);
    }

    @Test
    public void shouldBuildAnOrderWithInVisibleLineItems() {
        final OrderDTO orderDTO = OrderDTO.newInstance("", "", new ArrayList<QuoteOptionItemDTO>());

        context.checking(new Expectations() {{
            oneOf(inVisibleCreatableLineItemRetriever).whatInVisibleLineItemsIHaveCreated(OFFER_LINE_ITEM_1);
            will(returnValue(newHashSet("anInVisibleLineItemId")));
            oneOf(orderFacade).createOrder(ORDER_NAME, PROJECT_ID, QUOTE_OPTION_ID, asList("anInVisibleLineItemId", OFFER_LINE_ITEM_1));
            will(returnValue(orderDTO));
            ignoring(projectResource).updateStatus(PROJECT_ID);

        }});

        QuoteOptionOrdersOrchestrator orchestrator = quoteOptionOrdersOrchestratorTestFixture.withOrderFacade(orderFacade).withProjectResource(projectResource)
                                                                                             .withinVisibleCreatableLineItemResolver(inVisibleCreatableLineItemRetriever).build();

        orchestrator.buildOrder(ORDER_NAME, PROJECT_ID, QUOTE_OPTION_ID, OFFER_LINE_ITEM_1);
    }

    @Test
    public void shouldSubmitAnOrder() throws Exception {
        context.checking(new Expectations() {{
            oneOf(orderFacade).submitOrder(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, CUSTOMER_ID, false, "anUser");
        }});

        final QuoteOptionOrdersOrchestrator orchestrator = quoteOptionOrdersOrchestratorTestFixture.withOrderFacade(orderFacade).build();
        orchestrator.submitOrderAndCreateAssets(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, CUSTOMER_ID, false, "anUser");

        context.assertIsSatisfied();
    }


    @Test
    public void shouldGetOrderStatus() throws Exception {
      QuoteOptionOrdersOrchestrator orchestrator =
            quoteOptionOrdersOrchestratorTestFixture.withOneOrder()
                                                    .withOrderFacade(orderFacade).build();

        final OrderDTO storedOrder = quoteOptionOrdersOrchestratorTestFixture.getSingleOrder();

        context.checking(new Expectations(){{
            oneOf(orderFacade).get(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID);
            will(returnValue(storedOrder));
        }});
        String orderStatus = orchestrator.getOrderStatus(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID);
        assertThat(orderStatus, is(OrderStatus.CREATED.getValue()));
    }

    @Test
    public void shouldGetOrder() throws Exception {
      QuoteOptionOrdersOrchestrator orchestrator =
            quoteOptionOrdersOrchestratorTestFixture.withOneOrder()
                                                    .withOrderFacade(orderFacade).build();

        final OrderDTO storedOrder = quoteOptionOrdersOrchestratorTestFixture.getSingleOrder();

        context.checking(new Expectations(){{
            oneOf(orderFacade).get(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID);
            will(returnValue(storedOrder));
        }});
        OrderDTO orderStatus = orchestrator.getOrder(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID);
        assertThat(orderStatus.getOrderItems().size(), is(1));
    }

    @Test
    public void shouldSendOrderSubmissionEmail() throws Exception {
        final UserDTO userDTO = mock(UserDTO.class);
        context.checking(new Expectations() {{
            oneOf(orderFacade).sendOrderSubmissionEmail(PROJECT_ID, QUOTE_OPTION_ID, "orderId", userDTO, orderDTO.status);
        }});

        final QuoteOptionOrdersOrchestrator orchestrator = quoteOptionOrdersOrchestratorTestFixture.withOrderFacade(orderFacade).build();
        orchestrator.sendOrderSubmissionEmail(PROJECT_ID, QUOTE_OPTION_ID, "orderId", userDTO, orderDTO.status);

        context.assertIsSatisfied();
    }

    @Test
    public void shouldSendOrderSubmissionFailedEmail() throws Exception {
        final UserDTO userDTO = mock(UserDTO.class);
        final String errorLogs="Order submission failed";
        context.checking(new Expectations() {{
            oneOf(orderFacade).sendOrderSubmissionFailedEmail(PROJECT_ID, QUOTE_OPTION_ID, "orderId", userDTO, errorLogs);
        }});

        final QuoteOptionOrdersOrchestrator orchestrator = quoteOptionOrdersOrchestratorTestFixture.withOrderFacade(orderFacade).build();
        orchestrator.sendOrderSubmissionFailedEmail(PROJECT_ID, QUOTE_OPTION_ID, "orderId", userDTO, errorLogs);

        context.assertIsSatisfied();
    }

    @Test
    public void shouldFailValidationForDiscountStatus(){

        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        ProjectResource projectResource = mock(ProjectResource.class);
        when(projectResource.quoteOptionResource("aProjectId")).thenReturn(quoteOptionResource);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(quoteOptionResource.quoteOptionItemResource("aQuoteOptionId")).thenReturn(quoteOptionItemResource);
        String aLineItemId = "aLineItemId";
        when(quoteOptionItemResource.get(aLineItemId)).thenReturn(aQuoteOptionItemDTO().withId(aLineItemId).withDiscountStatus(LineItemDiscountStatus.NEEDS_APPROVAL).build());
        QuoteOptionOrdersOrchestrator orchestrator = quoteOptionOrdersOrchestratorTestFixture.withProjectResource(projectResource)
                                                                                             .withProductInstanceClient(productInstanceClient)
                                                                                             .withLineItemFacade(lineItemFacade).build();
        OfferAndOrderValidationResult validationResult = orchestrator.checkValidation(newArrayList(aLineItemId), "aProjectId", "aQuoteOptionId", CUSTOMER_ID, CONTRACT_ID);
        assertFalse(validationResult.isValid());
        assertThat(validationResult.getErrorMessage(),is("The discount status is invalid."));
    }

    @Test
     public void shouldFailValidationForPricingStatus(){

        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        ProjectResource projectResource = mock(ProjectResource.class);
        when(projectResource.quoteOptionResource("aProjectId")).thenReturn(quoteOptionResource);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(quoteOptionResource.quoteOptionItemResource("aQuoteOptionId")).thenReturn(quoteOptionItemResource);
        String aLineItemId = "aLineItemId";
        when(quoteOptionItemResource.get(aLineItemId)).thenReturn(aQuoteOptionItemDTO().withId(aLineItemId).withDiscountStatus(LineItemDiscountStatus.APPROVED).build());
        LineItemModel model = mock(LineItemModel.class);
        when(model.getId()).thenReturn(aLineItemId);
        when(model.getPricingStatusOfTree()).thenReturn(PricingStatus.NOT_PRICED);
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, "aProjectId", "aQuoteOptionId", PriceSuppressStrategy.None)).thenReturn(newArrayList(model));
        QuoteOptionOrdersOrchestrator orchestrator = quoteOptionOrdersOrchestratorTestFixture.withProjectResource(projectResource)
                                                                                             .withProductInstanceClient(productInstanceClient)
                                                                                             .withLineItemFacade(lineItemFacade).build();

        OfferAndOrderValidationResult validationResult = orchestrator.checkValidation(newArrayList(aLineItemId), "aProjectId", "aQuoteOptionId", CUSTOMER_ID, CONTRACT_ID);
        assertFalse(validationResult.isValid());
        assertThat(validationResult.getErrorMessage(),is("The Pricing status is invalid."));
    }

    @Test
     public void shouldFailValidationForRelationStatus(){

        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        ProjectResource projectResource = mock(ProjectResource.class);
        when(projectResource.quoteOptionResource("aProjectId")).thenReturn(quoteOptionResource);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(quoteOptionResource.quoteOptionItemResource("aQuoteOptionId")).thenReturn(quoteOptionItemResource);
        String aLineItemId = "aLineItemId";
        when(quoteOptionItemResource.get(aLineItemId)).thenReturn(aQuoteOptionItemDTO().withId(aLineItemId).withDiscountStatus(LineItemDiscountStatus.APPROVED).build());
        LineItemModel model = mock(LineItemModel.class);
        when(model.getId()).thenReturn(aLineItemId);
        when(model.getPricingStatusOfTree()).thenReturn(PricingStatus.FIRM);
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, "aProjectId", "aQuoteOptionId", PriceSuppressStrategy.None)).thenReturn(newArrayList(model));
        QuoteOptionOrdersOrchestrator orchestrator = quoteOptionOrdersOrchestratorTestFixture.withProjectResource(projectResource)
                                                                                             .withProductInstanceClient(productInstanceClient)
                                                                                             .withLineItemFacade(lineItemFacade).build();
        productInstance = mock(ProductInstance.class);
        ProductInstance related = mock(ProductInstance.class);
        when(related.getAssetVersionStatus()).thenReturn(AssetVersionStatus.DRAFT);
        when(productInstance.getRelatedToInstances()).thenReturn(newHashSet(related));
        when(productInstanceClient.get(any(LineItemId.class))).thenReturn(productInstance);
        OfferAndOrderValidationResult validationResult = orchestrator.checkValidation(newArrayList(aLineItemId), "aProjectId", "aQuoteOptionId", CUSTOMER_ID, CONTRACT_ID);
        assertFalse(validationResult.isValid());
        assertThat(validationResult.getErrorMessage(),is("The status of related asset is invalid."));
    }

}
