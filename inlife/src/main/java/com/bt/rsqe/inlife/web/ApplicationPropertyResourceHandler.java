package com.bt.rsqe.inlife.web;

import com.bt.rsqe.inlife.client.dto.ApplicationProperty;
import com.bt.rsqe.inlife.client.resource.ApplicationPropertyResource;
import com.bt.rsqe.inlife.repository.ApplicationPropertyStore;
import com.bt.rsqe.rest.ResponseBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path(ApplicationPropertyResource.PATH)
public class ApplicationPropertyResourceHandler implements ApplicationPropertyResource<Response> {
    private ApplicationPropertyStore applicationPropertyStore;

    public ApplicationPropertyResourceHandler(ApplicationPropertyStore applicationPropertyStore) {
        this.applicationPropertyStore = applicationPropertyStore;
    }

    @GET
    @Path("/{name}")
    public Response getApplicationProperty(@PathParam("name") String name, @QueryParam("default") String defaultValue, @QueryParam("quoteOptionId") String quoteOptionId) {
        ApplicationProperty property = applicationPropertyStore.getQuoteOptionProperty(quoteOptionId, name);
        if(property == null) {
            property = applicationPropertyStore.getProperty(name);
            if(property == null) {
                property = new ApplicationProperty(name, defaultValue);
                applicationPropertyStore.createProperty(property);
            }
        }
        return ResponseBuilder.anOKResponse().withEntity(property).build();
    }
}
