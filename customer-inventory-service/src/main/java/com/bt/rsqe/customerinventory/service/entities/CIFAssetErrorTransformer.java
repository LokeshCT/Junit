package com.bt.rsqe.customerinventory.service.entities;

import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetErrorEntity;
import com.bt.rsqe.customerinventory.repository.jpa.keys.AssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetError;

public class CIFAssetErrorTransformer {
    public static FutureAssetErrorEntity toErrorEntity(CIFAsset cifAsset, CIFAssetError cifAssetError) {
        return new FutureAssetErrorEntity(new AssetKey(cifAsset.getAssetKey().getAssetId(), cifAsset.getAssetKey().getAssetVersion()),
                                   cifAssetError.getId(),
                                   cifAssetError.getErrorType(),
                                   cifAssetError.getSeverity(),
                                   cifAssetError.getMessage());
    }

    public static CIFAssetError fromErrorEntity(FutureAssetErrorEntity futureAssetErrorEntity) {
        return new CIFAssetError(futureAssetErrorEntity.getId(), futureAssetErrorEntity.getErrorType(),
                                 futureAssetErrorEntity.getSeverity(), futureAssetErrorEntity.getMessage());
    }
}
