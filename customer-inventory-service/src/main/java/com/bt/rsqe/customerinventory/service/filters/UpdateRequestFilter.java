package com.bt.rsqe.customerinventory.service.filters;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;

import java.util.List;

public interface UpdateRequestFilter {
    List<CIFAssetUpdateRequest> filter(final List<CIFAssetUpdateRequest> cifAssetUpdateRequests, List<CIFAssetUpdateRequest> dependantUpdates);
}
