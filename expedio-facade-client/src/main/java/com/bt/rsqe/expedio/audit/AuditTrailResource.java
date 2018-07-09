package com.bt.rsqe.expedio.audit;

import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.factory.RestRequestBuilderFactory;
import com.bt.rsqe.web.rest.RestRequestBuilder;

import javax.ws.rs.core.GenericType;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/27/15
 * Time: 2:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuditTrailResource {

    private static final String[] uriSegments = {"rsqe","expedio","auditTrail"};
    private static final String CUSTOMER_ID = "customerID";
    private static final String TYPE = "type";
    private static final String ID = "id";
    private RestRequestBuilder restRequestBuilder;


    public AuditTrailResource(URI baseUri, String secret, RestRequestBuilderFactory restRequestBuilderFactory) {
        URI uri = com.bt.rsqe.utils.UriBuilder.buildUri(baseUri, uriSegments);
        this.restRequestBuilder = restRequestBuilderFactory.createProxyAwareRestRequestBuilder(uri).withSecret(secret);
    }

    public AuditTrailResource(ExpedioFacadeConfig clientConfig ) {
        this(com.bt.rsqe.utils.UriBuilder.buildUri(clientConfig.getApplicationConfig()), clientConfig.getRestAuthenticationClientConfig().getSecret(), new RestRequestBuilderFactory());
    }

    public List<AuditSummaryDTO> getQuoteAuditSummary(String customerId){

        Map<String, String > qParam = new HashMap<String, String>();
        qParam.put(CUSTOMER_ID,customerId);
        qParam.put(TYPE,"Quote");

        return restRequestBuilder.build("summary",qParam).get().getEntity(new GenericType<List<AuditSummaryDTO>>(){});
    }

    public List<AuditSummaryDTO> getOrderAuditSummary(String customerId){

        Map<String, String > qParam = new HashMap<String, String>();
        qParam.put(CUSTOMER_ID,customerId);
        qParam.put(TYPE,"Order");

        return restRequestBuilder.build("summary",qParam).get().getEntity(new GenericType<List<AuditSummaryDTO>>(){});
    }

    public List<AuditDetailDTO> getQuoteAuditDetail(String customerId,String quoteId){

        Map<String, String > qParam = new HashMap<String, String>();
        qParam.put(CUSTOMER_ID,customerId);
        qParam.put(TYPE,"Quote");
        qParam.put(ID,quoteId);

        return restRequestBuilder.build("detail",qParam).get().getEntity(new GenericType<List<AuditDetailDTO>>(){});
    }

    public List<AuditDetailDTO> getOrderAuditDetail(String customerId,String orderId){

        Map<String, String > qParam = new HashMap<String, String>();
        qParam.put(CUSTOMER_ID,customerId);
        qParam.put(TYPE,"Order");
        qParam.put(ID,orderId);

        return restRequestBuilder.build("detail",qParam).get().getEntity(new GenericType<List<AuditDetailDTO>>(){});
    }
}
