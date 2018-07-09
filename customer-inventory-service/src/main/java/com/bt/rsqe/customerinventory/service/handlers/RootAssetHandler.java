package com.bt.rsqe.customerinventory.service.handlers;


import com.bt.rsqe.customerinventory.service.client.resource.CustomerInventoryServiceResource;
import com.bt.rsqe.customerinventory.service.client.resource.RootAssetUpadterResource;

import com.bt.rsqe.customerinventory.service.rootAssetUpdater.RootAssetOrchestrator;
import com.bt.rsqe.rest.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path(CustomerInventoryServiceResource.BASE_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class RootAssetHandler implements RootAssetUpadterResource<Response> {

    private final RootAssetOrchestrator rootAssetOrchestrator;
    private static final Logger LOG = LoggerFactory.getLogger(RootAssetHandler.class);
    public RootAssetHandler(RootAssetOrchestrator rootAssetOrchestrator) {
        this.rootAssetOrchestrator = rootAssetOrchestrator;
    }

    @Path("/rootAssetDetailUpdater")
    @Override
    @POST
    public javax.xml.ws.Response rootAssetDetailUpdater(String expref) {
        boolean flag =  rootAssetOrchestrator.rootAssetDetailUpdater(expref);

        if(flag){
            return (javax.xml.ws.Response) ResponseBuilder.anOKResponse();
        }else{
            LOG.info("Error While Updating Delta");
            return (javax.xml.ws.Response) ResponseBuilder.notFound().build();

        }
    }
}
