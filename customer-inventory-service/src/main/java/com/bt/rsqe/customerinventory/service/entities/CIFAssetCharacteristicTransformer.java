package com.bt.rsqe.customerinventory.service.entities;

import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetCharacteristicEntity;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;

public class CIFAssetCharacteristicTransformer {
    public static FutureAssetCharacteristicEntity toCharacteristicEntity(CIFAsset cifAsset, CIFAssetCharacteristic cifAssetCharacteristic) {
        return new FutureAssetCharacteristicEntity(cifAsset.getLineItemId(),
                                            cifAsset.getAssetKey().getAssetId(),
                                            cifAsset.getAssetKey().getAssetVersion(),
                                            cifAssetCharacteristic.getName(),
                                            cifAssetCharacteristic.getValue());
    }

    public static CIFAssetCharacteristic fromCharacteristicEntity(FutureAssetCharacteristicEntity characteristicEntity) {
        return new CIFAssetCharacteristic(characteristicEntity.getName(), characteristicEntity.getValue(), true);
    }
}
