package com.bt.rsqe.customerinventory.service.handlers;

import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.cache.CacheAwareTransaction;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetLineItemKey;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetResource;
import com.bt.rsqe.customerinventory.service.client.resource.CustomerInventoryServiceResource;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.rest.ResponseBuilder;

import javax.persistence.NoResultException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path(CustomerInventoryServiceResource.BASE_PATH + CIFAssetResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class CIFAssetHandler implements CIFAssetResource<Response> {
    private CIFAssetOrchestrator cifAssetOrchestrator;

    public CIFAssetHandler(CIFAssetOrchestrator cifAssetOrchestrator) {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
    }

    @Override
    @POST
    @Path(CIFAssetResource.LINE_ITEM_PATH)
    public Response loadCIFAsset(CIFAssetLineItemKey CIFAssetLineItemKey) {
        try {
            CacheAwareTransaction.set(true);
            CIFAsset asset = cifAssetOrchestrator.getAsset(CIFAssetLineItemKey);
            return ResponseBuilder.anOKResponse().withEntity(asset).build();
        } catch (NoResultException noResultException) {
            return ResponseBuilder.notFound().build();
        } finally {
            AssetCacheManager.clearAllCaches();
            CacheAwareTransaction.remove();
        }
    }

    @Override
    @POST
    public Response loadCIFAsset(CIFAssetKey assetKey) {
        try {
            CacheAwareTransaction.set(true);
            CIFAsset asset = cifAssetOrchestrator.getAsset(assetKey);
            return ResponseBuilder.anOKResponse().withEntity(asset).build();
        } catch (NoResultException noResultException) {
            return ResponseBuilder.notFound().build();
        } finally {
            AssetCacheManager.clearAllCaches();
            CacheAwareTransaction.remove();
        }
    }

    @GET
    @Path("bEndSites")
    public Response getBEndSitesInUse(@QueryParam("assetId") String ownerAssetId, @QueryParam("relationshipName") String relationshipName,
                                      @QueryParam("characteristicName") String characteristicName, @QueryParam("ownerStencilId") String ownerStencilId) {
        List<String> bEndSiteIds = cifAssetOrchestrator.getBEndSiteIds(ownerAssetId, relationshipName, characteristicName, ownerStencilId);
        return Response.ok().entity(new GenericEntity<List<String>>(bEndSiteIds){}).build();
    }

    @GET
    @Path("aEndSite")
    public Response getAEndSiteByBendSiteId(@QueryParam("assetId") String ownerAssetId, @QueryParam("relationshipName") String relationshipName,
                                      @QueryParam("bEndCharName") String bEndCharName, @QueryParam("aEndCharName") String aEndCharName, @QueryParam("ownerStencilId") String ownerStencilId, @QueryParam("bEndSiteId") String bEndSiteId) {
        List<String> aEndSiteId = cifAssetOrchestrator.getAendSiteIdByBEndSiteId(ownerAssetId, relationshipName, bEndCharName, aEndCharName, ownerStencilId, bEndSiteId);
        return Response.ok().entity(new GenericEntity<List<String>>(aEndSiteId){}).build();
    }

    @POST
    @Path(CIFAssetResource.PARENT_ASSET_PATH)
    public Response loadCIFParentOrOwnerAsset(CIFAssetKey assetKey) {
        try {
            CIFAsset asset = cifAssetOrchestrator.getCifParentOrOwnerAsset(assetKey);
            return ResponseBuilder.anOKResponse().withEntity(asset).build();
        } catch (NoResultException noResultException) {
            return ResponseBuilder.notFound().build();
        }
    }

}
