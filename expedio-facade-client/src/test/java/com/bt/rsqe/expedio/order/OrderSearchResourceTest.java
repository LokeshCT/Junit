package com.bt.rsqe.expedio.order;

import com.bt.rsqe.factory.RestRequestBuilderFactory;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.RestResponse;
import javax.ws.rs.core.GenericType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 8/14/14
 * Time: 12:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class OrderSearchResourceTest {

    OrderSearchResource orderSearchResource = null;

    @Mock
    RestResponse clientResponseMock = null;

    @Mock
    RestResource restResourceMock = null;

    @Mock
    RestRequestBuilder restRequestBuilderMock = null;

    @Mock
    RestRequestBuilderFactory restRequestBuilderFactoryMock =null;


    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        Mockito.when(restRequestBuilderFactoryMock.createProxyAwareRestRequestBuilder(any(URI.class))).thenReturn(restRequestBuilderMock);
        when(restRequestBuilderMock.withSecret(anyString())).thenReturn(restRequestBuilderMock);
        orderSearchResource = new OrderSearchResource(new URI("http://localhost:9999"),null,restRequestBuilderFactoryMock);
    }

    @Test
       public void shouldGetOrder() throws Exception {
        String salesChannel = "BT AMERICAS";
        String custId = "1001";

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setCustomerName("TEST Cust");

        List<OrderDTO> ordersRet = new ArrayList<OrderDTO>();
        ordersRet.add(orderDTO);


        when(restRequestBuilderMock.build(anyString(), anyMap())).thenReturn(restResourceMock);
        when(restResourceMock.get()).thenReturn(clientResponseMock);
        when(clientResponseMock.getEntity(any(GenericType.class))).thenReturn(ordersRet);

        List<OrderDTO> orders = orderSearchResource.getOrder(salesChannel, custId);


        assert (orders.equals(ordersRet));
     }

    @Test
    public void shouldGetOrderNotThrowExceptionOnNullArgs() throws Exception {
        String salesChannel = null;
        String custId = null;



        when(restRequestBuilderMock.build(anyString(), anyMap())).thenReturn(restResourceMock);
        when(restResourceMock.get()).thenReturn(clientResponseMock);
        when(clientResponseMock.getEntity(any(GenericType.class))).thenReturn(null);

        List<OrderDTO> orders = orderSearchResource.getOrder(salesChannel, custId);


        assert (orders==null);
     }

    @Test
    public void shouldGetOrderDetail() throws Exception {
        {
            String orderID = "10001";

            OrderLineItemDTO orderLineItem = new OrderLineItemDTO();
            orderLineItem.setOrderID("10001");

            List<OrderLineItemDTO> ordersLineItemRet = new ArrayList<OrderLineItemDTO>();
            ordersLineItemRet.add(orderLineItem);


            when(restRequestBuilderMock.build(anyString(), anyMap())).thenReturn(restResourceMock);
            when(restResourceMock.get()).thenReturn(clientResponseMock);
            when(clientResponseMock.getEntity(any(GenericType.class))).thenReturn(ordersLineItemRet);

            List<OrderLineItemDTO> orderLineItems = orderSearchResource.getOrderDetail(orderID);


            assert (orderLineItems.equals(ordersLineItemRet));
        }
    }
}
