package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ChooseRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ReprovideAssetRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ReprovideAssetResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRequestSource;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;

import java.util.List;
import java.util.UUID;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.Relationships;
import static com.google.common.collect.Lists.newArrayList;

public class ReprovideAssetUpdater implements CIFAssetUpdater<ReprovideAssetRequest, ReprovideAssetResponse> {

    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private final CreateRelationshipUpdater createRelationshipUpdater;
    private final CancelRelationshipUpdater cancelRelationshipUpdater;
    private final DependentUpdateBuilderFactory dependentUpdateBuilderFactory;

    public ReprovideAssetUpdater (CIFAssetOrchestrator cifAssetOrchestrator,
                                  CreateRelationshipUpdater createRelationshipUpdater,
                                  CancelRelationshipUpdater cancelRelationshipUpdater, DependentUpdateBuilderFactory dependentUpdateBuilderFactory) {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.createRelationshipUpdater = createRelationshipUpdater;
        this.cancelRelationshipUpdater = cancelRelationshipUpdater;
        this.dependentUpdateBuilderFactory = dependentUpdateBuilderFactory;
    }

    public ReprovideAssetResponse performUpdate(ReprovideAssetRequest update) {
        DependantUpdatesBuilder dependantUpdatesBuilder = dependentUpdateBuilderFactory.getDependentUpdateBuilderFactory() ;

        // Get the assets & details
        final CIFAsset owningAsset = cifAssetOrchestrator.getAsset(new CIFAssetKey(update.getAssetKey(), newArrayList(Relationships)));
        final CIFAsset reprovidingAsset = cifAssetOrchestrator.getAsset(new CIFAssetKey(update.getReprovideAssetKey()));
        final String relationshipName = findRelationship(owningAsset, reprovidingAsset).getRelationshipName();

        // Get all the current owners of the reproviding asset as we'll need to link them back in again
        final List<CIFAsset> originalOwningAssets = cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(update.getReprovideAssetKey(),
                                                                                                        newArrayList(Relationships)));

        // Cancel the reproviding asset
        final CancelRelationshipRequest cancelRelationshipRequest = new CancelRelationshipRequest(update.getAssetKey(), "", 0,
                                                                                                  update.getReprovideAssetKey(),
                                                                                                  relationshipName,
                                                                                                  reprovidingAsset.getProductCode(), false);
        final CancelRelationshipResponse cancelRelationshipResponse = cancelRelationshipUpdater.performUpdate(cancelRelationshipRequest);
        dependantUpdatesBuilder.withList(cancelRelationshipResponse.getDependantUpdates());

        // Create the new asset
        final CreateRelationshipRequest createRelationshipRequest =
            new CreateRelationshipRequest(UUID.randomUUID().toString(), owningAsset.getAssetKey(), relationshipName,
                                          reprovidingAsset.getProductCode(),
                                          reprovidingAsset.getStencilDetail().getStencilCode(),
                                          reprovidingAsset.getSiteId(),
                                          "", "", 0);
        final CreateRelationshipResponse createRelationshipResponse = createRelationshipUpdater.performUpdate(createRelationshipRequest);
        final AssetKey newReprovidedAssetKey = createRelationshipResponse.getCreatedAssetKey();
        dependantUpdatesBuilder.withList(createRelationshipResponse.getDependantUpdates());

        // Now create choose relationship requests to link all the original owners back to the newly reprovided asset
        for (CIFAsset originalOwningAsset : originalOwningAssets) {
            final CIFAssetRelationship originalRelationship = findRelationship(originalOwningAsset, reprovidingAsset);
            ChooseRelationshipRequest chooseNewRelationshipRequest = new ChooseRelationshipRequest(originalOwningAsset.getAssetKey(),
                                                                                                   newReprovidedAssetKey,
                                                                                                   originalRelationship.getRelationshipName(),
                                                                                                   "", 0, UpdateRequestSource.RelateTo);
            dependantUpdatesBuilder.with(chooseNewRelationshipRequest);
            dependantUpdatesBuilder.withOptional(dependentUpdateBuilderFactory.getInvalidatePriceRequestBuilder().invalidatePriceForRelationshipChange(originalOwningAsset));
        }

        return new ReprovideAssetResponse(update, dependantUpdatesBuilder.dependantRequests());
    }

    private CIFAssetRelationship findRelationship(CIFAsset owningAsset, CIFAsset ownedAsset) {
        for (CIFAssetRelationship cifAssetRelationship : owningAsset.getRelationships()) {
            if (cifAssetRelationship.getRelated().getAssetKey().equals(ownedAsset.getAssetKey())) {
                return cifAssetRelationship;
            }
        }
        throw new RuntimeException(String.format("Unable to find relationship from owner - %s to related - %s", owningAsset.getAssetKey(), ownedAsset.getAssetKey()));
    }
}
