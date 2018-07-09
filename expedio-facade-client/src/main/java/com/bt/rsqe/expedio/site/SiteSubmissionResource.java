package com.bt.rsqe.expedio.site;

import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.exception.InternalServerErrorException;
import javax.ws.rs.core.GenericType;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

public class SiteSubmissionResource {

    private RestRequestBuilder restRequestBuilder;

    @Deprecated
    /**
     * @deprecated Use the URI and secret-based constructor instead
     */
    public SiteSubmissionResource(URI baseUri) {
        this.restRequestBuilder = new ProxyAwareRestRequestBuilder(UriBuilder.fromUri(baseUri).path("rsqe").path("expedio").segment("site").build());
    }

    public SiteSubmissionResource(URI baseUri, String secret) {
        this(baseUri);
        this.restRequestBuilder.withSecret(secret);
    }

    public List<ExpedioSiteDetailsDTO> submit(final List<SiteSubmissionRequestDTO> siteSubmissionRequestDTO) throws SiteSubmissionFailureException {
        try {
            ExpedioSiteDetailsDTOList expedioSiteDetailsDTOs =  restRequestBuilder.build("submit").post(new SiteSubmissionRequestDTOs(siteSubmissionRequestDTO))
                                                                                  .getEntity(new GenericType <ExpedioSiteDetailsDTOList>(){});
            return expedioSiteDetailsDTOs.getExpedioSiteDetailsDTOList();
        } catch (InternalServerErrorException ex) {
            throw new SiteSubmissionFailureException();
        }
    }

}
