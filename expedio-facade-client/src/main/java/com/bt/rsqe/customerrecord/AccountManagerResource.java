package com.bt.rsqe.customerrecord;

import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Optional;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class AccountManagerResource {

    private RestRequestBuilder restRequestBuilder;

    public AccountManagerResource(URI baseUri) {
        restRequestBuilder = new ProxyAwareRestRequestBuilder(UriBuilder.fromUri(baseUri).path("account-manager").build());
    }

    public AccountManagerResource(URI baseUri, String secret) {
        this(baseUri);
        restRequestBuilder.withSecret(secret);
    }

    protected AccountManagerResource(RestRequestBuilder restRequestBuilder, String secret) {
        this.restRequestBuilder = restRequestBuilder.withSecret(secret);
    }

    public AccountManagerDTO get() {
        return restRequestBuilder.build().get().getEntity(AccountManagerDTO.class);
    }

    public Optional<AccountManagerDTO> getByRole(String role) {
        try {
            AccountManagerDTO accountManagerDTO = restRequestBuilder.build("role", role).get().getEntity(AccountManagerDTO.class);
            return Optional.of(accountManagerDTO);
        } catch (ResourceNotFoundException exception) {
            return Optional.absent();
        }
    }
}
