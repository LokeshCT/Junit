package com.bt.rsqe.expedio.contact;

import com.bt.rsqe.domain.bom.parameters.BfgContact;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.exception.InternalServerErrorException;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Optional;
import com.bt.rsqe.web.rest.RestResponse;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class BFGContactsResource {
    private RestRequestBuilder restRequestBuilder;

    public BFGContactsResource(URI baseUri) {
        this.restRequestBuilder = new ProxyAwareRestRequestBuilder(UriBuilder.fromUri(baseUri).path("rsqe").path("expedio").segment("bfgContact").build());
    }

    public BFGContactsResource(URI baseUri, String secret) {
        this(baseUri);
        this.restRequestBuilder.withSecret(secret);
    }

    public ContactDTO submit(final ContactDTO contactDTO) throws BFGContactCreationFailureException {
        try {
            RestResponse response = restRequestBuilder.build("submit").post(contactDTO);
            return response.getEntity(ContactDTO.class);
        } catch (InternalServerErrorException ex) {
            throw new BFGContactCreationFailureException();
        }
    }

    public Optional<BfgContact> get(long contactId) {
        try {
            return Optional.of(restRequestBuilder.build(String.valueOf(contactId)).get().getEntity(BfgContact.class));
        } catch (ResourceNotFoundException exception) {
            return Optional.absent();
        }
    }
}
