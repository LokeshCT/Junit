package com.bt.rsqe.expedio.order;

import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.factory.RestRequestBuilderFactory;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import javax.ws.rs.core.GenericType;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderSearchResource {

    private static final String SALES_CHANNEL_QUERY_PARAM="salesChannel";
    private static final String CUST_ID_QUERY_PARAM="customerID";
    private static final String ORDER_ID_QUERY_PARAM="orderID";

    private static final String[] uriSegments = {"rsqe","expedio","orders"};
    private RestRequestBuilder restRequestBuilder;


    public OrderSearchResource(URI baseUri, String secret, RestRequestBuilderFactory restRequestBuilderFactory) {
        URI uri = com.bt.rsqe.utils.UriBuilder.buildUri(baseUri, uriSegments);
        this.restRequestBuilder = restRequestBuilderFactory.createProxyAwareRestRequestBuilder(uri).withSecret(secret);
    }

    public OrderSearchResource(ExpedioFacadeConfig clientConfig ) {
        this(com.bt.rsqe.utils.UriBuilder.buildUri(clientConfig.getApplicationConfig()),clientConfig.getRestAuthenticationClientConfig().getSecret(),new RestRequestBuilderFactory() );
    }

    public List<OrderDTO> getOrder(String salesChannel, String customerID){
        Map<String,String> qParams = new HashMap<String,String>();
        qParams.put(SALES_CHANNEL_QUERY_PARAM,salesChannel);
        qParams.put(CUST_ID_QUERY_PARAM,customerID);
        return restRequestBuilder.build("getOrders",qParams).get().getEntity(new GenericType<List<OrderDTO>>(){});
    }


    public List<OrderLineItemDTO> getOrderDetail(String orderId){
        Map<String,String> qParams = new HashMap<String,String>();
        qParams.put(ORDER_ID_QUERY_PARAM,orderId);
        return restRequestBuilder.build("getOrderLineItems",qParams).get().getEntity(new GenericType<List<OrderLineItemDTO>>(){});
    }

}
