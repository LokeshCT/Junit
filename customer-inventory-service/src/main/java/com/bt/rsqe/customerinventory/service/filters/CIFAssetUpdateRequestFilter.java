package com.bt.rsqe.customerinventory.service.filters;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.*;

public class CIFAssetUpdateRequestFilter {

    List<UpdateRequestFilter> requestFilters;

    public CIFAssetUpdateRequestFilter() {
        requestFilters = new ArrayList<UpdateRequestFilter>() {{
            add(new CharacteristicReloadRequestFilter());
        }};
    }

    public List<CIFAssetUpdateRequest> filter(List<CIFAssetUpdateRequest> cifAssetUpdateRequests, List<CIFAssetUpdateRequest> dependantUpdates) {
        List<CIFAssetUpdateRequest> filteredRequests = newArrayList();
        for (UpdateRequestFilter updateRequestFilter : requestFilters) {
            filteredRequests.addAll(updateRequestFilter.filter(cifAssetUpdateRequests, dependantUpdates));
        }
        return filteredRequests;
    }

}
