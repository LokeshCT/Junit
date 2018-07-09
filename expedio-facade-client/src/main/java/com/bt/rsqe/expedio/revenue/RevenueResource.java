package com.bt.rsqe.expedio.revenue;

import com.bt.rsqe.domain.RevenueDTO;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.dto.ErrorDTO;
import com.bt.rsqe.web.rest.RestResponse;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class RevenueResource {

    private RestRequestBuilder restRequestBuilder;
    private String secret;

    public RevenueResource(URI baseUri, String secret) {
        this.secret = secret;
        restRequestBuilder = new ProxyAwareRestRequestBuilder(UriBuilder.fromUri(baseUri).path("rsqe").path("expedio").path("revenue").build()).withSecret(secret);
    }

    public ErrorDTO submit(RevenueDTO revenueDTO) {
        final RestResponse restResponse = restRequestBuilder.build("submit").post(revenueDTO);
        return restResponse.getEntity(ErrorDTO.class);
    }


}
