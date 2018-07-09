package com.bt.rsqe.expedio.product;

import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.expedio.pricebook.PriceBookDTO;
import com.bt.rsqe.utils.UriBuilder;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResponse;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductResource {

    private RestRequestBuilder restRequestBuilder;

    public ProductResource(ExpedioFacadeConfig expedioFacadeConfig) {
        this(UriBuilder.buildUri(expedioFacadeConfig.getApplicationConfig()),
             expedioFacadeConfig.getRestAuthenticationClientConfig().getSecret());
    }

    public ProductResource(URI baseUri, String secret) {
        URI uri = UriBuilder.buildUri(priceBookURI(baseUri));
        restRequestBuilder = new ProxyAwareRestRequestBuilder(uri).withSecret(secret);
    }

    private URI priceBookURI(URI baseURI) {
        return javax.ws.rs.core.UriBuilder.fromUri(baseURI).path("rsqe").path("expedio").path("pricebook").build();
    }


    public List<PriceDetails> getPriceBookDetails(String customerID) {
        Map<String, String> qParam = new HashMap<String, String>();

        if (customerID != null) {
            qParam.put("customerID", customerID);
        } else {
            return null;
        }

        RestResponse restResponse = this.restRequestBuilder.build("getPriceBookDetails", qParam).get();


        List<PriceDetails> priceBookDetailResult = restResponse.getEntity(new GenericType<List<PriceDetails>>() {
        });
        return priceBookDetailResult;

    }

    public boolean saveBookDetails(PriceBookDTO pBook) {
        RestResponse restResponse = this.restRequestBuilder.build("savePriceBookDetails").post(pBook);

        if (restResponse.getStatus() == Response.Status.OK.getStatusCode()) {
            return true;
        }else{
            return false;
        }
    }


}
