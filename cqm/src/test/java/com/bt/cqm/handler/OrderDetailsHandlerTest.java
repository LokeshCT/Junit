package com.bt.cqm.handler;

import com.bt.rsqe.domain.AttachmentDTO;
import com.bt.rsqe.emppal.attachmentresource.EmpPalResource;
import com.bt.rsqe.expedio.order.OrderDTO;
import com.bt.rsqe.expedio.order.OrderLineItemDTO;
import com.bt.rsqe.expedio.order.OrderSearchResource;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 8/4/14
 * Time: 6:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class OrderDetailsHandlerTest {

    OrderDetailsHandler orderDetailsHandler ;

    @Mock
    OrderSearchResource orderSearchResourceMock;

    @Mock
    EmpPalResource empPalResourceMock;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        //CqmEnvironmentConfig config = ConfigurationProvider.provide(CqmEnvironmentConfig.class, "dev");
        orderDetailsHandler = new OrderDetailsHandler(orderSearchResourceMock, empPalResourceMock);

    }

    @Test
    public void shouldSearchOrderDetailsOnNull() throws Exception {
        String salesChannel =null;
        String custID=null;

        Response resp =orderDetailsHandler.searchOrderDetails(salesChannel,custID);

        assert(Response.Status.NOT_FOUND.getStatusCode()==resp.getStatus());

    }

    @Test
    public void shouldSearchOrderDetails() throws Exception {
        String salesChannel ="BT AMERICAS";
        String custID="1001";

        List<OrderDTO> orders = new ArrayList<OrderDTO>();
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderID("2001");
        orders.add(orderDTO);

        Mockito.when(orderSearchResourceMock.getOrder(anyString(),anyString())).thenReturn(orders);
        Response resp =orderDetailsHandler.searchOrderDetails(salesChannel,custID);

        assert(Response.Status.OK.getStatusCode()==resp.getStatus());
       // Assert.assertEquals(orders,resp.getEntity());
}

    @Test
    public void shouldSearchOrderDetailsReturnNOTFound() throws Exception {
        String salesChannel ="BT AMERICAS";
        String custID="1001";

        List<OrderDTO> orders = new ArrayList<OrderDTO>();
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderID("2001");
        orders.add(orderDTO);

        Mockito.when(orderSearchResourceMock.getOrder(anyString(),anyString())).thenReturn(null);
        Response resp =orderDetailsHandler.searchOrderDetails(salesChannel,custID);

        assert(Response.Status.NOT_FOUND.getStatusCode()==resp.getStatus());
        // Assert.assertEquals(orders,resp.getEntity());
    }

    @Test
    public void shouldGetOrderLineItemsReturnNOTFoundOnNullInput(){
          String orderID=null;

        Response resp=orderDetailsHandler.getOrderLineItems(orderID);

        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(),resp.getStatus());

    }

    @Test
    public void shouldGetOrderLineItems(){
        String orderID="1001";

        List<OrderLineItemDTO> orderLineItemDTOs = new ArrayList<OrderLineItemDTO>();
        OrderLineItemDTO orderLineItemDTO = new OrderLineItemDTO();
        orderLineItemDTO.setOrderID(orderID);
        orderLineItemDTOs.add(orderLineItemDTO);

        when(orderSearchResourceMock.getOrderDetail(anyString())).thenReturn(orderLineItemDTOs);

        Response resp=orderDetailsHandler.getOrderLineItems(orderID);

        Assert.assertEquals(Response.Status.OK.getStatusCode(),resp.getStatus());

    }

    @Test
    public void shouldGetOrderLineItemsNOTFoundOnNullOrderItems(){
        String orderID="1001";

        List<OrderLineItemDTO> orderLineItemDTOs = null;

        when(orderSearchResourceMock.getOrderDetail(anyString())).thenReturn(orderLineItemDTOs);

        Response resp=orderDetailsHandler.getOrderLineItems(orderID);

        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(),resp.getStatus());

    }

    @Test
    public void shouldUploadAttachmentReturnNOTFoundOnNullArg(){

        InputStream fileInputStream =null;
        FormDataContentDisposition contentDispositionHeader=null;
        String salesChannel=null;

        String customerId=null;
         String attachmentType=null;
         String quoteId =null;

        Response resp = orderDetailsHandler.uploadAttachment(fileInputStream,contentDispositionHeader,salesChannel,customerId,attachmentType,quoteId);

        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(),resp.getStatus());

    }

    @Test
    public void shouldUploadAttachment(){

        InputStream fileInputStream =new StringBufferInputStream("test");
        FormDataContentDisposition contentDispositionHeader=Mockito.mock(FormDataContentDisposition.class);
        String salesChannel="BT AMERICAS";

        String customerId="1001";
        String attachmentType="I";
        String quoteId ="1002";

        when(contentDispositionHeader.getFileName()).thenReturn("someFileName");
        when(empPalResourceMock.uploadAttachment(any(AttachmentDTO.class), Matchers.<String>any())).thenReturn("Success");

        Response resp = orderDetailsHandler.uploadAttachment(fileInputStream,contentDispositionHeader,salesChannel,customerId,attachmentType,quoteId);

        Assert.assertEquals(Response.Status.OK.getStatusCode(),resp.getStatus());

    }

    @Test
    public void shouldListOrderAttachmentsReturnNotFoundOnNullArg(){
        String salesChannel = "BT AMERICAS";
        String custID="1001";
        String attachType="I";
        String quoteID="1002";

        List<AttachmentDTO> attachmentDTOs = new ArrayList<AttachmentDTO>();
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTOs.add(attachmentDTO);


        when(empPalResourceMock.getAvailableAttachments(anyString())).thenReturn(attachmentDTOs);

        Response resp = orderDetailsHandler.listOrderAttachments(salesChannel,custID,attachType,quoteID);

        Assert.assertEquals(Response.Status.OK.getStatusCode(),resp.getStatus());
    }

    @Test
    public void shouldGetOrderAttachmentReturnNOTFoundOnNullArgs(){
        String salesChannel = "BT AMERICAS";
        String custID="1001";
        String attachType="I";
        String quoteID="1002";
        String docID =null;
        String fileName=null;

        Response resp = orderDetailsHandler.getOrderAttachment(salesChannel,custID,attachType,quoteID,docID,fileName);

        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(),resp.getStatus());


    }

    @Test
    public void shouldGetOrderAttachment(){
        String salesChannel = "BT AMERICAS";
        String custID="1001";
        String attachType="I";
        String quoteID="1002";
        String docID ="1001";
        String fileName="soneFileName";
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTO.setFileContent("Test".getBytes());
        attachmentDTO.setFileName("someFileName");


        when(empPalResourceMock.getAttachment(anyString(),anyString())).thenReturn(attachmentDTO);
        Response resp = orderDetailsHandler.getOrderAttachment(salesChannel,custID,attachType,quoteID,docID,fileName);

        Assert.assertEquals(Response.Status.OK.getStatusCode(),resp.getStatus());


    }

}
