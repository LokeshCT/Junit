package com.bt.rsqe.cleanordervalidation;

import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.utils.UriBuilder;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.exception.PreconditionFailedException;

import java.net.URI;

public class CleanOrderValidationResourceClient implements CleanOrderValidationResource<Void>{
     private RestRequestBuilder requestBuilder;

    public CleanOrderValidationResourceClient(URI baseUri, String secret) {
        URI uri = UriBuilder.buildUri(baseUri, "rsqe", "expedio", "clean-order-validation");
        this.requestBuilder = new ProxyAwareRestRequestBuilder(uri).withSecret(secret);
    }

     public CleanOrderValidationResourceClient(ExpedioFacadeConfig config) {
        this( UriBuilder.buildUri(config.getApplicationConfig()),
            config.getRestAuthenticationClientConfig().getSecret());
    }

    @Override
    public Void validateExpedioAccount(int bfgSiteId, int billingAccountId, String salesChannelType, String quoteId) throws CleanOrderValidationException{
        try {
            requestBuilder.build(String.valueOf(bfgSiteId),
                                 String.valueOf(billingAccountId),
                                 salesChannelType,
                                 quoteId).post();
        } catch (PreconditionFailedException ex) {
            throw new CleanOrderValidationException(ex.getMessage());
        }
        return null;
    }
}
