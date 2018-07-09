package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;

public abstract class CIFAssetCharacteristicEvaluator extends CIFAssetExtensionDependant {
    private String characteristicName;

    public CIFAssetCharacteristicEvaluator(String characteristicName) {
        this.characteristicName = characteristicName;
    }

    public abstract Object evaluate(CIFAsset cifAsset);

    public String getCharacteristicName() {
        return characteristicName;
    }
}
