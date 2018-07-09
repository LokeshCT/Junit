package com.bt.rsqe.customerinventory.service.filters;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateResponse;

import java.util.List;

public interface UpdateResponseFilter {
    List<CIFAssetUpdateResponse> filter(final List<CIFAssetUpdateResponse> updateResponses);
}
