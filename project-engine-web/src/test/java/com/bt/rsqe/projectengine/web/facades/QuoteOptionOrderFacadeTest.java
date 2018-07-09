package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.OrderItemStatus;
import com.bt.rsqe.projectengine.OrderResource;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.RfoUpdateDTO;
import com.bt.rsqe.projectengine.web.model.modelfactory.OrderModelFactory;
import com.bt.rsqe.security.UserContextBuilder;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.utils.RSQEMockery;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import com.google.common.base.Predicate;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(JMock.class)
public class QuoteOptionOrderFacadeTest {

    private static final String QUOTE_OPTION_ID = "QuoteOptionId";
    private static final String ORDER_NAME = "orderName";
    private static final String PROJECT_ID = "ProjectId";
    private static final String ORDER_ID = "OrderId";
    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static final String CONTRACT_ID = "ContractId";
    private static final String SALES_CHANNEL_TYPE = "Direct";

    private QuoteOptionResource quoteOptionResource;
    private OrderResource orderResource;
    private QuoteOptionOrderFacade orderFacade;
    private ProjectResource projectResource;
    private JUnit4Mockery context;
    private OrderModelFactory orderModelFactory;
    private QuoteOptionItemResource quoteOptionItemResource;

    @Before
    public void setUp() {
        context = new RSQEMockery();
        orderResource = context.mock(OrderResource.class);
        quoteOptionItemResource = context.mock(QuoteOptionItemResource.class);
        quoteOptionResource = context.mock(QuoteOptionResource.class);
        projectResource = context.mock(ProjectResource.class);
        orderModelFactory = context.mock(OrderModelFactory.class);
        UserContextManager.setCurrent(UserContextBuilder.aUserContext().build());
        orderFacade = new QuoteOptionOrderFacade(projectResource, orderModelFactory);
        context.checking(new Expectations() {{
            allowing(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            allowing(quoteOptionResource).quoteOptionOrderResource(QUOTE_OPTION_ID);
            will(returnValue(orderResource));
            allowing(quoteOptionResource).quoteOptionItemResource(QUOTE_OPTION_ID);
            will(returnValue(quoteOptionItemResource));
        }});
    }

    @Test
    public void shouldSubmitOrder() throws Exception {
        context.checking(new Expectations() {{
            oneOf(orderResource).submitOrder(ORDER_ID, SALES_CHANNEL_TYPE, CUSTOMER_ID, false, "anUser");
        }});

        orderFacade.submitOrder(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, CUSTOMER_ID, false, "anUser");
        context.assertIsSatisfied();
    }

    @Test
    public void shouldGetOrderDto() {
        context.checking(new Expectations() {{
            oneOf(orderResource).get(ORDER_ID);
        }});

        orderFacade.get(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldUpdateOrderStatus() throws Exception {
        context.checking(new Expectations() {{
            oneOf(orderResource).updateOrderStatus(ORDER_ID, OrderItemStatus.IN_PROGRESS);
        }});

        orderFacade.updateOrderStatus(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, OrderItemStatus.IN_PROGRESS);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldGetRfoValidDto() {
        context.checking(new Expectations() {{
            oneOf(orderResource).getIsRfoValid(ORDER_ID);
        }});

        orderFacade.isRfoValid(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldGetOrderModel() {
        context.checking(new Expectations() {{
            allowing(orderResource).get(ORDER_ID);
            OrderDTO orderDTO = OrderDTO.newInstance("name", "created", new ArrayList<QuoteOptionItemDTO>());
            will(returnValue(orderDTO));
            oneOf(orderModelFactory).create(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, orderDTO);
        }});

        orderFacade.getModel(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldCreateAnOrder() throws Exception {
        final List<String> lineItems = asList("1", "2", "3");
        context.checking(new Expectations() {{
            oneOf(orderResource).post(with(orderWithItems(lineItems)));
        }});

        orderFacade.createOrder(ORDER_NAME, PROJECT_ID, QUOTE_OPTION_ID, lineItems);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldCreateAnOrderWithCorrectNameFormatting() throws Exception {
        context.checking(new Expectations() {{
            oneOf(orderResource).post(with(trimmedOrderName("Order Name")));
        }});

        orderFacade.createOrder(" Order    Name   ", PROJECT_ID, QUOTE_OPTION_ID, Collections.<String>emptyList());
    }

    @Test
    public void shouldUpdateRfo() throws Exception {
        final QuoteOptionItemDTO item1 = context.mock(QuoteOptionItemDTO.class);
        final QuoteOptionItemDTO item2 = context.mock(QuoteOptionItemDTO.class);
        final JaxbDateTime item1CustomerDate = JaxbDateTime.valueOf(new DateTime());
        final JaxbDateTime item2CustomerDate = JaxbDateTime.valueOf(new DateTime());
        final RfoUpdateDTO dto = new RfoUpdateDTO(new DateTime(2012, 1, 1, 0, 0, 0, 0).toString(), newArrayList(new RfoUpdateDTO.ItemBillingDTO("item1", null, item1CustomerDate),
                                                                           new RfoUpdateDTO.ItemBillingDTO("item2", null, item2CustomerDate)));
        context.checking(new Expectations() {{
            oneOf(orderResource).updateRfo(ORDER_ID, dto);
            oneOf(quoteOptionItemResource).get("item1");
            will(returnValue(item1));
            oneOf(item1).setCustomerRequiredDate(item1CustomerDate);
            oneOf(quoteOptionItemResource).get("item2");
            will(returnValue(item2));
            oneOf(item2).setCustomerRequiredDate(item2CustomerDate);
            oneOf(quoteOptionItemResource).put(item1, false);
            oneOf(quoteOptionItemResource).put(item2, true);
        }});
        orderFacade.updateWithRfo(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, dto);
    }

    @Test
    public void shouldSendOrderSubmissionEmail() throws Exception {
        final UserDTO userDTO = mock(UserDTO.class);
        final String orderStatus="Submitted";
        context.checking(new Expectations() {{
            oneOf(orderResource).sendOrderSubmissionEmail(ORDER_ID, userDTO, orderStatus);
        }});

        orderFacade.sendOrderSubmissionEmail(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, userDTO, orderStatus);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldSendOrderSubmissionFailedEmail() throws Exception {
        final UserDTO userDTO = mock(UserDTO.class);
        final String errorLogs = "Order submission failed";
        context.checking(new Expectations() {{
            oneOf(orderResource).sendOrderSubmissionFailedEmail(ORDER_ID, userDTO, errorLogs);
        }});

        orderFacade.sendOrderSubmissionFailedEmail(PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, userDTO, errorLogs);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldGetBillingIdForLineItem() throws Exception {
        final QuoteOptionItemDTO orderItem = QuoteOptionItemDTO.fromId("aLineItemId");
        orderItem.billingId = "aBillingId";
        final OrderDTO order = OrderDTO.newInstance("anOrder", null, newArrayList(orderItem));

        context.checking(new Expectations() {{
            oneOf(orderResource).getAll();
            will(returnValue(newArrayList(order)));
        }});

        String billingId = orderFacade.getBillingId(PROJECT_ID, QUOTE_OPTION_ID, "aLineItemId");
        assertThat(billingId, is("aBillingId"));
    }

    @Test
    public void shouldReturnNullForBillingIdWhenNoneExistsOnLineItem() throws Exception {
        final QuoteOptionItemDTO orderItem = QuoteOptionItemDTO.fromId("aLineItemId");
        orderItem.billingId = null;
        final OrderDTO order = OrderDTO.newInstance("anOrder", null, newArrayList(orderItem));

        context.checking(new Expectations() {{
            oneOf(orderResource).getAll();
            will(returnValue(newArrayList(order)));
        }});

        String billingId = orderFacade.getBillingId(PROJECT_ID, QUOTE_OPTION_ID, "aLineItemId");
        assertThat(billingId, is(nullValue()));
    }

    @Test
    public void shouldReturnNullForBillingIdWhenNoOrderExistsForQuoteOption() throws Exception {
        context.checking(new Expectations() {{
            oneOf(orderResource).getAll();
            will(returnValue(newArrayList()));
        }});

        String billingId = orderFacade.getBillingId(PROJECT_ID, QUOTE_OPTION_ID, "aLineItemId");
        assertThat(billingId, is(nullValue()));
    }

    private Matcher<OrderDTO> trimmedOrderName(final String orderName) {
        return new TypeSafeMatcher<OrderDTO>() {
            @Override
            public boolean matchesSafely(OrderDTO orderDTO) {
                assertTrue(format("order name expected: '%s'.\nObtained: '%s'", orderName, orderDTO.name), orderName.equals(orderDTO.name));
                return true;
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }

    private Matcher<OrderDTO> orderWithItems(final List<String> lineItems) {
        return new TypeSafeMatcher<OrderDTO>() {
            @Override
            public boolean matchesSafely(OrderDTO orderDTO) {
                for (final String lineItem : lineItems) {
                    assertTrue(format("line item with id '%s' not found", lineItem), any(orderDTO.getOrderItems(), new Predicate<QuoteOptionItemDTO>() {
                        @Override
                        public boolean apply(@Nullable QuoteOptionItemDTO input) {
                            return input.id.equals(lineItem);
                        }
                    }));
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }
}
