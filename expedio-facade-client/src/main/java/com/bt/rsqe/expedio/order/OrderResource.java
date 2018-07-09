package com.bt.rsqe.expedio.order;

import com.bt.rsqe.collection.NameValueCollection;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import javax.ws.rs.core.GenericType;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class OrderResource {

    private RestRequestBuilder restRequestBuilder;

    @Deprecated
    /**
    * @deprecated  Use the URI and secret-based constructor instead
    */
    public OrderResource(URI baseUri) {
        this.restRequestBuilder = new ProxyAwareRestRequestBuilder(UriBuilder.fromUri(baseUri).path("rsqe").path("expedio").segment("order").build());
    }

    public OrderResource(URI baseUri, String secret) {
        this(baseUri);
        this.restRequestBuilder.withSecret(secret);
    }

    public List<ManageOrderResponseDTO> submit(final NameValueCollection billOfMaterials){
        return restRequestBuilder.build("submit").post(billOfMaterials).getEntity(new GenericType<List<ManageOrderResponseDTO>>(){});
    }

    public Map<String, String> downloadBillOfMaterials(final NameValueCollection billOfMaterials){
        return restRequestBuilder.build("downloadBomXml").post(billOfMaterials).getEntity(new GenericType<Map<String, String>>(){});
    }

}
