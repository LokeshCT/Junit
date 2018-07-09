package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.domain.AssetKey;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.*;

public class CancellationContributesToRequestBuilder {

    private ContributesToChangeRequestBuilder contributesToChangeRequestBuilder;

    public CancellationContributesToRequestBuilder(ContributesToChangeRequestBuilder contributesToChangeRequestBuilder) {
        this.contributesToChangeRequestBuilder = contributesToChangeRequestBuilder;
    }

    public Set<CIFAssetUpdateRequest> buildRequests(CIFAsset assetToBeCancelled) {
        Set<CIFAssetUpdateRequest> dependantUpdateRequests = newHashSet();
        final Map<AssetKey, CIFAsset> assetsByKey = flattenAssets(assetToBeCancelled);

        for (AssetKey key : assetsByKey.keySet()) {
            dependantUpdateRequests.addAll(contributesToChangeRequestBuilder.buildRequestsOnCancellation(key, assetsByKey.get(key).getProductCode(), 1));
        }
        //Remove if any contributions to flattened assets, because the same would be removed, so not required.
        return newHashSet(Iterables.filter(dependantUpdateRequests, new Predicate<CIFAssetUpdateRequest>() {
            @Override
            public boolean apply(CIFAssetUpdateRequest input) {
                return !assetsByKey.containsKey(input.getAssetKey());
            }
        }));
    }

    public Map<AssetKey, CIFAsset> flattenAssets(CIFAsset cifAsset) {  //This would return current and its descendants asset keys
        Map<AssetKey, CIFAsset> keys = new HashMap<AssetKey, CIFAsset>();
        keys.put(cifAsset.getAssetKey(), cifAsset);
        for (CIFAsset asset : cifAsset.getChildren()) {
            keys.put(asset.getAssetKey(), asset);
            if (!asset.getChildren().isEmpty()) {
                keys.putAll(flattenAssets(asset));
            }
        }
        return keys;
    }
}
