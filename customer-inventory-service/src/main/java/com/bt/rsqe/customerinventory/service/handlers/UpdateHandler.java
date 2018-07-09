package com.bt.rsqe.customerinventory.service.handlers;

import com.bt.rsqe.customerinventory.service.AssetSaveExceptionManager;
import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.cache.CacheAwareTransaction;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateResponseList;
import com.bt.rsqe.customerinventory.service.client.domain.updates.QuoteOptionContext;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRequests;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UserDetailsManager;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetUpdateResource;
import com.bt.rsqe.customerinventory.service.client.resource.CustomerInventoryServiceResource;
import com.bt.rsqe.customerinventory.service.orchestrators.AssetUpdateOrchestrator;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.web.rest.exception.BadRequestException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path(CustomerInventoryServiceResource.BASE_PATH + CIFAssetUpdateResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class UpdateHandler implements CIFAssetUpdateResource<Response> {
    private final AssetUpdateOrchestrator assetUpdateOrchestrator;
    private AssetSaveExceptionManager assetSaveExceptionManager;

    public UpdateHandler(AssetUpdateOrchestrator assetUpdateOrchestrator, AssetSaveExceptionManager assetSaveExceptionManager) {
        this.assetUpdateOrchestrator = assetUpdateOrchestrator;
        this.assetSaveExceptionManager = assetSaveExceptionManager;
    }

    @Override
    @POST
    public Response performUpdates(UpdateRequests updates) {
        UserDetailsManager.set(updates.getUserDetails());
        QuoteOptionContext.set(updates.getQuoteOptionId());
        CacheAwareTransaction.set(true);

        List<CIFAssetUpdateResponse> cifAssetUpdateResponses = null;
        try {
            cifAssetUpdateResponses = assetUpdateOrchestrator.update(updates.getUpdates());
        } catch (BadRequestException exception) {
            return Response.status(Response.Status.BAD_REQUEST).entity(exception.errorDto().description).build();
        } catch (RuntimeException e) {
           assetSaveExceptionManager.handleQuoteOptionItems(updates.getProjectId(), updates.getQuoteOptionId());
            throw e;
        } finally {
            AssetCacheManager.clearAllCaches();
            CacheAwareTransaction.remove();
            QuoteOptionContext.remove();
            UserDetailsManager.remove();
        }

        return ResponseBuilder.anOKResponse().withEntity(CIFAssetUpdateResponseList.fromList(cifAssetUpdateResponses)).build();
    }
}
