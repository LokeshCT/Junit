package com.bt.nrm.resources;

import com.bt.nrm.config.NrmClientConfig;
import com.bt.nrm.dto.request.NonStandardRequestDTO;
import com.bt.nrm.dto.response.CheckNonStandardRequestStatusResponseDTO;
import com.bt.nrm.dto.response.NonStandardRequestResponseDTO;
import com.bt.rsqe.factory.RestRequestBuilderFactory;
import com.bt.rsqe.utils.UriBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;

import javax.ws.rs.core.GenericType;
import java.net.URI;
import java.util.HashMap;

/**
 * Created by 608143048 on 21/01/2016.
 *
 */
public class NRMResource {

    private RestRequestBuilder restRequestBuilder;

    public NRMResource(URI baseURI, String secret, RestRequestBuilderFactory restRequestBuilderFactory) {
        URI uri = UriBuilder.buildUri(baseURI, "nrm");
        this.restRequestBuilder = restRequestBuilderFactory.createProxyAwareRestRequestBuilder(uri).withSecret(secret);
    }

    public NRMResource(NrmClientConfig nrmClientConfig) {
        this(UriBuilder.buildUri(nrmClientConfig.getApplicationConfig()), nrmClientConfig.getRestAuthenticationClientConfig().getSecret(), new RestRequestBuilderFactory());
    }

    /*
       This method is used by SQE/rSQE to create non-standard requests.
    */
    public NonStandardRequestResponseDTO createNonStandardRequest(NonStandardRequestDTO requestDTO) {
        return this.restRequestBuilder.build("createNonStandardRequest")
                .post(requestDTO)
                .getEntity(new GenericType<NonStandardRequestResponseDTO>(){});
    }

    /*
       This method is used by SQE/rSQE to check non-standard request status.
    */
    public CheckNonStandardRequestStatusResponseDTO checkNonStandardRequestStatus(String quoteId, String quoteOptionId, String requestId) {
        HashMap<String, String> qParams = new HashMap<String, String>();
        qParams.put("quoteId", quoteId);
        qParams.put("quoteOptionId", quoteOptionId);
        qParams.put("requestId", requestId);
        return this.restRequestBuilder.build("checkNonStandardRequestStatus")
                .get()
                .getEntity(new GenericType<CheckNonStandardRequestStatusResponseDTO>(){});
    }

}
