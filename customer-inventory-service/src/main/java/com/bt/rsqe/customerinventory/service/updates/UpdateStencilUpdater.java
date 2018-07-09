package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.AutoDefaultRelationshipsRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateStencilRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateStencilResponse;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.StencilReservedAttributesHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.google.common.collect.Lists.*;

public class UpdateStencilUpdater implements CIFAssetUpdater<UpdateStencilRequest, UpdateStencilResponse>
{
    private final CIFAssetOrchestrator assetOrchestrator;
    private final DependentUpdateBuilderFactory dependentUpdateBuilderFactory;
    private final StencilReservedAttributesHelper stencilReservedAttributesHelper;

    public UpdateStencilUpdater (CIFAssetOrchestrator assetOrchestrator, DependentUpdateBuilderFactory dependentUpdateBuilderFactory) {
        this.assetOrchestrator = assetOrchestrator;
        this.dependentUpdateBuilderFactory = dependentUpdateBuilderFactory;
        this.stencilReservedAttributesHelper = new StencilReservedAttributesHelper();

    }

    @Override
    public UpdateStencilResponse performUpdate(UpdateStencilRequest update) {
        final String newStencilId = update.getStencilId() ;

        CIFAsset asset = assetOrchestrator.getAsset(new CIFAssetKey(update.getAssetKey(), newArrayList(StencilDetails, Relationships)));

        // Update the asset characteristic with the new stencil id
        stencilReservedAttributesHelper.getStencilCharacteristic(asset).setValue(newStencilId);

        // Update the asset
        assetOrchestrator.saveAssetAndClearCaches(asset);
        List<CIFAssetUpdateRequest> dependantRequests = getDependantUpdates(asset, newStencilId);

        // Retrieve the current stencil id
        final String oldStencilId = asset.getStencilDetail().getStencilCode() ;
        return new UpdateStencilResponse(update, oldStencilId, dependantRequests);
    }

    private List<CIFAssetUpdateRequest> getDependantUpdates(CIFAsset asset, String newStencilId) {

        // All the characteristics need to be re-defaulted
        DependantUpdatesBuilder dependantUpdatesBuilder = DependantUpdatesBuilder.dependantUpdatesBuilder() ;
        final CharacteristicChangeRequest characteristicDefaultDependencies = dependentUpdateBuilderFactory.getCharacteristicChangeRequestBuilder().defaultForAllCharacteristics(asset, newStencilId, null);
        dependantUpdatesBuilder.with(characteristicDefaultDependencies);

        // We need to remove any invalid relationships
        assetOrchestrator.extendAsset(asset, newArrayList(RelationshipCardinality));
        final List<CancelRelationshipRequest> cancelRelationshipDependencies = dependentUpdateBuilderFactory.getCancelRelationshipRequestBuilder().removeInvalidRelationships(asset);
        dependantUpdatesBuilder.withList(cancelRelationshipDependencies);

        // Finally, auto-default the relationships.  We don't know what needs to be re-defaulted
        // until the removals happen (queued above) so just add a request to the end of the queue
        // to do this.
        dependantUpdatesBuilder.with(new AutoDefaultRelationshipsRequest(asset.getAssetKey(), asset.getLineItemId(), asset.getQuoteOptionItemDetail().getLockVersion(), asset.getProductCode(), true));

        // Trigger pricing invalidation
        // TODO or can we just rely on it happening due to other dependants?
        dependantUpdatesBuilder.withOptional(dependentUpdateBuilderFactory.getInvalidatePriceRequestBuilder().invalidatePriceForStencilChange(asset));

        return dependantUpdatesBuilder.dependantRequests();
    }
}
