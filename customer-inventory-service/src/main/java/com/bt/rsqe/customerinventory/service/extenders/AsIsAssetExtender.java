package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.google.common.collect.Lists.newArrayList;

public class AsIsAssetExtender {
    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private static final List<CIFAssetExtension> nonAsIsRequiredExtensions = newArrayList(ProductOfferingRelationshipDetail, RelationshipCreatableCandidates, RelationshipChoosableCandidates, RelationshipCardinality, AsIsAsset, Action, JourneySpecificDetail);

    public AsIsAssetExtender(CIFAssetOrchestrator cifAssetOrchestrator) {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
    }

    public void extend(List<CIFAssetExtension> extensionsList, CIFAsset cifAsset) {
        if(AsIsAsset.isInList(extensionsList)) {
            final List<CIFAssetExtension> asIsExtensions = newArrayList(extensionsList);
            asIsExtensions.removeAll(nonAsIsRequiredExtensions);
            final Optional<CIFAsset> inServiceAsset = cifAssetOrchestrator.getInServiceAsset(new CIFAssetKey(cifAsset.getAssetKey(), asIsExtensions));
            cifAsset.loadAsIsAsset(inServiceAsset.orNull());
        }
    }
}
