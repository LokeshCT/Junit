package com.bt.rsqe.inlife.monitoring.client;

import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class CustomerInventoryDiagnosticsResource {
    private URI uri;
    private RestRequestBuilder restRequestBuilder;

    public CustomerInventoryDiagnosticsResource(URI baseUri, String secret) {
        uri = UriBuilder.fromUri(baseUri).path("rsqe/customer-inventory/reader/options").build();
        restRequestBuilder = new ProxyAwareRestRequestBuilder(uri);
        restRequestBuilder.withSecret(secret);
    }

    public String getLineItemDetail(String lineItemId) {
        return restRequestBuilder.build(new String[]{lineItemId}).getAsString();
    }
}
