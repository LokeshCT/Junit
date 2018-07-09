package com.bt.rsqe.customerrecord;

import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import javax.ws.rs.core.GenericType;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

import static com.google.common.collect.Iterables.getFirst;

public class PriceBookResource {
/*Customer id part of url
* url = /expedio/customers/customerId/price-book(s?)/sCode
* or
* url = /expedio/customers/customerId/products/sCode/price-book(s?)
* */

    private RestRequestBuilder restRequestBuilder;

    @Deprecated
    /**
    * @deprecated  Use the URI and secret-based constructor instead
    */
    public PriceBookResource(URI baseUri) {
        this(new ProxyAwareRestRequestBuilder(UriBuilder.fromUri(baseUri).path("price-book").build()));
    }

    protected PriceBookResource(RestRequestBuilder restRequestBuilder) {
        this.restRequestBuilder = restRequestBuilder;
    }

    public PriceBookResource(URI baseUri, String secret) {
        this(baseUri);
        restRequestBuilder.withSecret(secret);
    }

    public PriceBookDTO defaultPriceBook(String productHCode) {
        return getFirst(getPriceBooks(productHCode),null);
    }

    @Deprecated
    public List<PriceBookDTO> get(String productSCode) {
        return restRequestBuilder.build("product", productSCode).get().getEntity(new GenericType<List<PriceBookDTO>>() {
        });
    }

    public List<PriceBookDTO> getPriceBooks(String productHCode) {
        return restRequestBuilder.build("productCategory", productHCode).get().getEntity(new GenericType<List<PriceBookDTO>>() {
        });
    }



}
