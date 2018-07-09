package com.bt.rsqe.customerinventory.service.filters;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class CharacteristicReloadRequestFilter implements UpdateRequestFilter {
    @Override
    public List<CIFAssetUpdateRequest> filter(final List<CIFAssetUpdateRequest> cifAssetUpdateRequests, List<CIFAssetUpdateRequest> dependantUpdates) {
        return newArrayList(Iterables.filter(dependantUpdates, new Predicate<CIFAssetUpdateRequest>() {
            @Override
            public boolean apply(CIFAssetUpdateRequest input) {
                return !(input instanceof CharacteristicReloadRequest && reloadOfCancellingAsset(cifAssetUpdateRequests, input));
            }
        }));
    }

    private boolean reloadOfCancellingAsset(List<CIFAssetUpdateRequest> updateRequests, final CIFAssetUpdateRequest reloadRequest) {
        return Iterables.tryFind(updateRequests, new Predicate<CIFAssetUpdateRequest>() {
            @Override
            public boolean apply(CIFAssetUpdateRequest input) {
                if(input instanceof CancelRelationshipRequest) {
                    CancelRelationshipRequest cancelRelationshipRequest = (CancelRelationshipRequest) input;
                    return cancelRelationshipRequest.getCancellingAssetId().equals(reloadRequest.getAssetKey());
                }
                return false;
            }
        }).isPresent();
    }

}
