package com.bt.rsqe.inlife.web;

import com.bt.rsqe.inlife.client.dto.RequestResponseLog;
import com.bt.rsqe.inlife.client.resource.RequestResponseResource;
import com.bt.rsqe.persistence.store.RequestResponseStore;
import com.bt.rsqe.rest.ResponseBuilder;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path(RequestResponseResource.PATH)
public class RequestResponseResourceHandler implements RequestResponseResource<Response> {
    private RequestResponseStore store;

    public RequestResponseResourceHandler(RequestResponseStore store) {
        this.store = store;
    }

    @PUT
    @Override
    public Response log(RequestResponseLog log) {
        store.save(log.getType(),
                   log.getOrigin(),
                   log.getOperationName(),
                   log.getIdentifier(),
                   log.getPayload());

        return ResponseBuilder.anOKResponse().build();
    }
}
