package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateResponse;

public interface CIFAssetUpdater<updateType extends CIFAssetUpdateRequest, responseType extends CIFAssetUpdateResponse> {
    responseType performUpdate(updateType update);
}
