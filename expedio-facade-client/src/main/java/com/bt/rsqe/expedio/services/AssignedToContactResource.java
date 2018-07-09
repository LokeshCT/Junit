package com.bt.rsqe.expedio.services;

import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.utils.UriBuilder;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import javax.ws.rs.core.GenericType;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

public class AssignedToContactResource {

    public static final String SALES_CHANNEL_QUERY_PARAM = "salesChannel";
    public static final String USER_ROLE_QUERY_PARAM = "userRole";
    public static final String CUSTOMER_ID_QUERY_PARAM = "customerId";
    private RestRequestBuilder restRequestBuilder;

    public AssignedToContactResource(URI baseUri, String secret) {
        URI uri = UriBuilder.buildUri(baseUri, "rsqe", "expedio-services", "get-assigned-to-contacts");
        restRequestBuilder = new ProxyAwareRestRequestBuilder(uri).withSecret(secret);
    }

    public AssignedToContactResource(ExpedioFacadeConfig clientConfig) {
        this(UriBuilder.buildUri(clientConfig.getApplicationConfig()), clientConfig.getRestAuthenticationClientConfig().getSecret());
    }

    public List<ActivityAssignedToContactDTO> getAssignedToContacts(final String salesChannel, final String userRole, final String customerId) {
        return restRequestBuilder.build(new HashMap<String, String>() {{
            put(SALES_CHANNEL_QUERY_PARAM, salesChannel);
            put(USER_ROLE_QUERY_PARAM, userRole);
            put(CUSTOMER_ID_QUERY_PARAM, customerId);
        }}).get().getEntity(new GenericType<List<ActivityAssignedToContactDTO>>() {
        });
    }


}
