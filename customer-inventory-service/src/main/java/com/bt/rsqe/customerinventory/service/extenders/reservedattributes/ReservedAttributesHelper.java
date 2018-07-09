package com.bt.rsqe.customerinventory.service.extenders.reservedattributes;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;

import java.util.Arrays;
import java.util.List;

public class ReservedAttributesHelper {
    public CIFAssetCharacteristic getFirstCharacteristicByNames(CIFAsset cifAsset, String... characteristicNames) {
        for (String characteristicName : characteristicNames)
        {
            CIFAssetCharacteristic characteristic = cifAsset.getCharacteristic(characteristicName) ;
            if (characteristic != null)
            {
                return characteristic ;
            }
        }
        return null;
    }
}
