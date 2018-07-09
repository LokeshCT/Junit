package com.bt.rsqe.customerinventory.service.handlers;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.cache.CacheAwareTransaction;
import com.bt.rsqe.customerinventory.service.client.resource.AssetCandidateResource;
import com.bt.rsqe.customerinventory.service.client.resource.CustomerInventoryServiceResource;
import com.bt.rsqe.customerinventory.service.providers.AssetCandidateProviderFactory;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.rest.ResponseBuilder;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.bt.rsqe.domain.product.parameters.RelationshipName.*;

@Path(CustomerInventoryServiceResource.BASE_PATH + AssetCandidateResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class AssetCandidateHandler implements AssetCandidateResource<Response> {

    private AssetCandidateProviderFactory candidateProviderFactory;

    public AssetCandidateHandler(AssetCandidateProviderFactory candidateProviderFactory) {
        this.candidateProviderFactory = candidateProviderFactory;
    }

    @Path(AssetCandidateResource.CHOOSABLE + "/{relationshipName}")
    @POST
    public Response getChoosableCandidates(@PathParam(value = "relationshipName") String relationshipName, AssetKey assetKey) {
        CacheAwareTransaction.set(true);
        List<AssetDTO> choosableCandidates;
        try {
            choosableCandidates = candidateProviderFactory.choosableProvider().getChoosableCandidates(assetKey, newInstance(relationshipName));
        } finally {
            AssetCacheManager.clearAllCaches();
            CacheAwareTransaction.remove();
        }
        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<List<AssetDTO>>(choosableCandidates){}).build();
    }
}
