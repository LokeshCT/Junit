package com.bt.rsqe.customerinventory.service.entities;

import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetAuxiliaryAttributeEntity;
import com.bt.rsqe.customerinventory.repository.jpa.keys.AssetKey;
import com.bt.rsqe.customerinventory.repository.jpa.keys.FutureAssetAuxiliaryAttributeKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAuxiliaryAttribute;

public class CIFAssetAuxiliaryAttributeTransformer {
    public static CIFAssetAuxiliaryAttribute fromAuxiliaryAttributeEntity(FutureAssetAuxiliaryAttributeEntity futureAssetAuxiliaryAttributeEntity) {
        return new CIFAssetAuxiliaryAttribute(futureAssetAuxiliaryAttributeEntity.getName(),
                                              futureAssetAuxiliaryAttributeEntity.getValue());
    }

    public static FutureAssetAuxiliaryAttributeEntity fromAuxiliaryAttributeEntity(CIFAsset cifAsset, CIFAssetAuxiliaryAttribute cifAssetAuxiliaryAttribute) {
        AssetKey assetKey = new AssetKey(cifAsset.getAssetKey().getAssetId(), cifAsset.getAssetKey().getAssetVersion());
        FutureAssetAuxiliaryAttributeKey key = new FutureAssetAuxiliaryAttributeKey(assetKey, cifAssetAuxiliaryAttribute.getName());
        return new FutureAssetAuxiliaryAttributeEntity(key, cifAssetAuxiliaryAttribute.getValue());
    }
}
