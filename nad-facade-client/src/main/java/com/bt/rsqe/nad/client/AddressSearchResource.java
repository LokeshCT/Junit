package com.bt.rsqe.nad.client;


import com.bt.rsqe.nad.config.NadFacadeClientConfig;
import com.bt.rsqe.nad.dto.SearchAddressRequestDTO;
import com.bt.rsqe.nad.dto.SearchAddressResponseDTO;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class AddressSearchResource {

    private RestRequestBuilder restRequestBuilder;

    public AddressSearchResource(URI baseUri, String secret) {
        this.restRequestBuilder = new ProxyAwareRestRequestBuilder(
             UriBuilder.fromUri(baseUri).path("rsqe").path("nad").build()
        ).withSecret(secret);

    }

    public AddressSearchResource(NadFacadeClientConfig clientConfig) {
        this(com.bt.rsqe.utils.UriBuilder.buildUri(clientConfig.getApplicationConfig()), clientConfig.getRestAuthenticationClientConfig().getSecret());
    }

    public SearchAddressResponseDTO searchAddress(SearchAddressRequestDTO request) {
        return restRequestBuilder.build("search-address").put(request).getEntity(SearchAddressResponseDTO.class);
    }

    public SearchAddressResponseDTO matchAddress(SearchAddressRequestDTO request) {
        return restRequestBuilder.build("match-address").put(request).getEntity(SearchAddressResponseDTO.class);
    }
}
