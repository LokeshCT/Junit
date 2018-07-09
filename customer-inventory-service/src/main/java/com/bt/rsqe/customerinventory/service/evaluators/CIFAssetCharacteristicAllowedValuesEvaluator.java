package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.expressionevaluator.ExpressionEvaluatorUnknownExpressionException;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.CharacteristicAllowedValues;
import static com.bt.rsqe.customerinventory.service.extenders.CIFAssetCharacteristicValueConverter.*;
import static com.google.common.collect.Lists.newArrayList;

public class CIFAssetCharacteristicAllowedValuesEvaluator extends CIFAssetCharacteristicEvaluator {
    public CIFAssetCharacteristicAllowedValuesEvaluator(String characteristicName) {
        super(characteristicName);
    }

    @Override
    public Object evaluate(CIFAsset cifAsset) {
        CIFAssetCharacteristic characteristic = cifAsset.getCharacteristic(getCharacteristicName());
        if (characteristic == null) {
            throw new ExpressionEvaluatorUnknownExpressionException("Could not find attribute named '" + getCharacteristicName() + "'");
        }
        return toStrings(characteristic.getAllowedValues());
    }

    @Override
    public List<CIFAssetExtension> getCIFAssetExtensions() {
        return newArrayList(CharacteristicAllowedValues);
    }
}
