package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateStencilRequest;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.*;

public class UpdateRelationshipUpdater implements CIFAssetUpdater<UpdateRelationshipRequest, UpdateRelationshipResponse> {
    private final CIFAssetOrchestrator assetOrchestrator;


    public UpdateRelationshipUpdater(CIFAssetOrchestrator assetOrchestrator) {
        this.assetOrchestrator = assetOrchestrator;
    }

    @Override
    public UpdateRelationshipResponse performUpdate(UpdateRelationshipRequest update) {
        final CIFAsset asset = assetOrchestrator.getAsset(
            new CIFAssetKey(new AssetKey(update.getUpdatesToId(), update.getUpdatesToVersion()),
                            newArrayList(CIFAssetExtension.ProductOfferingRelationshipDetail,
                                         CIFAssetExtension.QuoteOptionItemDetail)));

        //
        // Does assetHierarchy Share Same Stencil i.e. a bearer?
        //
        // If so then add the child assets that are stencilable to a CIFAssetUpdateRequest list
        // of Stencil updates. Pricing refreshes will be worked out later.
        ArrayList<CIFAssetUpdateRequest> updateStencilRequestList = new ArrayList<CIFAssetUpdateRequest>();

        if (asset.getOfferingDetail().isBearer()) {
            addToRequestListIfAssetIsStencilable(asset,
                                                 update.getNewStencilId(),
                                                 updateStencilRequestList);
        }

        return new UpdateRelationshipResponse(update,
                                              updateStencilRequestList);
    }

    public void addToRequestListIfAssetIsStencilable( CIFAsset asset,
                                                      String newStencilId,
                                                      List<CIFAssetUpdateRequest> updateStencilRequestList){
        for (CIFAsset childAsset : asset.getChildren()) {
            addToRequestListIfAssetIsStencilable(childAsset,
                                                 newStencilId,
                                                 updateStencilRequestList);
        }

        if (isStencilable(asset)){
            UpdateStencilRequest newRequest = new UpdateStencilRequest(asset.getAssetKey(),
                                                                       newStencilId,
                                                                       null,
                                                                       null,
                                                                       asset.getLineItemId(),
                                                                       1);
            updateStencilRequestList.add(newRequest);
        }

    }

    public boolean isStencilable(CIFAsset asset){
        return !asset.getStencilDetail().getAllowedStencils().isEmpty();
    }
}
