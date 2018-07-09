package com.bt.rsqe.customerrecord;

import com.bt.rsqe.expedio.services.CloseBidManagerActivityDTO;
import com.bt.rsqe.expedio.services.BidManagerApprovalRequestDTO;
import com.bt.rsqe.expedio.services.BidManagerApprovalResponseDTO;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResponse;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class ExpedioServicesResource {

    private URI uri;
    private String secret;

    public ExpedioServicesResource(URI baseUri, String secret) {
        this.secret = secret;
        uri = UriBuilder.fromUri(baseUri).path("rsqe").path("expedio-services")
                        .build();
    }

    public BidManagerApprovalResponseDTO postBidManagerDiscountApprovalRequest(BidManagerApprovalRequestDTO bidManagerApprovalRequestDTO) {
        final URI uri = UriBuilder.fromUri(this.uri)
                                  .path("discount-approval-create")
                                  .build();
        RestRequestBuilder restRequestBuilder = new ProxyAwareRestRequestBuilder(uri).withSecret(secret);
        final RestResponse response = restRequestBuilder.build().post(bidManagerApprovalRequestDTO);
        return response.getEntity(BidManagerApprovalResponseDTO.class);
    }

    public void postBidManagerDiscountApprovalCloseRequest(CloseBidManagerActivityDTO closeRequest) {
        final URI uri = UriBuilder.fromUri(this.uri)
                                  .path("discount-approval-close")
                                  .build();
        RestRequestBuilder restRequestBuilder = new ProxyAwareRestRequestBuilder(uri).withSecret(secret);
        restRequestBuilder.build().post(closeRequest);
    }

    public BidManagerApprovalResponseDTO postBidManagerIcbApprovalRequest(BidManagerApprovalRequestDTO bidManagerApprovalRequestDTO) {
        final URI uri = UriBuilder.fromUri(this.uri)
                                  .path("bid-manager-ICB-approval-create")
                                  .build();
        RestRequestBuilder restRequestBuilder = new ProxyAwareRestRequestBuilder(uri).withSecret(secret);
        final RestResponse response = restRequestBuilder.build().post(bidManagerApprovalRequestDTO);
        return response.getEntity(BidManagerApprovalResponseDTO.class);
    }

    public void postBidManagerIcbApprovalCloseRequest(CloseBidManagerActivityDTO closeRequest) {
        final URI uri = UriBuilder.fromUri(this.uri)
                                  .path("bid-manager-ICB-approval-close")
                                  .build();
        RestRequestBuilder restRequestBuilder = new ProxyAwareRestRequestBuilder(uri).withSecret(secret);
        restRequestBuilder.build().post(closeRequest);
    }
}
