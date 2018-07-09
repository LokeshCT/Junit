package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.expressionevaluator.ExpressionEvaluatorUnknownExpressionException;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.*;

public class CIFAssetCharacteristicValueEvaluator extends CIFAssetCharacteristicEvaluator{
    public CIFAssetCharacteristicValueEvaluator(String characteristicName) {
        super(characteristicName);
    }

    @Override
    public Object evaluate(CIFAsset cifAsset){
        CIFAssetCharacteristic characteristic = cifAsset.getCharacteristic(getCharacteristicName());
        if(characteristic==null  || characteristic.getValue()==null || characteristic.getValue().trim().length()==0){
            return "";
        }
        return characteristic.getValue();
    }

    @Override
    public List<CIFAssetExtension> getCIFAssetExtensions() {
        return new ArrayList<CIFAssetExtension>();
    }
}
