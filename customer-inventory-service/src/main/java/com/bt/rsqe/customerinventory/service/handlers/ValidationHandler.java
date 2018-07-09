package com.bt.rsqe.customerinventory.service.handlers;

import com.bt.rsqe.customerinventory.service.client.resource.CustomerInventoryServiceResource;
import com.bt.rsqe.customerinventory.service.client.resource.ValidationResource;
import com.bt.rsqe.customerinventory.service.orchestrators.ValidationOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.rest.ResponseBuilder;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(CustomerInventoryServiceResource.BASE_PATH + ValidationResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class ValidationHandler implements ValidationResource<Response> {
    private ValidationOrchestrator validationOrchestrator;

    public ValidationHandler(ValidationOrchestrator validationOrchestrator) {
        this.validationOrchestrator = validationOrchestrator;
    }

    @Override
    @POST
    public Response validate(AssetKey assetKey) {
        return ResponseBuilder.anOKResponse().withEntity(validationOrchestrator.validate(assetKey)).build();
    }
}
