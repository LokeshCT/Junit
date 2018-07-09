package com.bt.rsqe.customerinventory.service.comparisons;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.productinstancemerge.ChangeType;
import org.apache.commons.lang.ObjectUtils;

import static com.bt.rsqe.enums.AssetVersionStatus.CEASED;
import static com.bt.rsqe.productinstancemerge.ChangeType.*;

public class ActionCalculator {
    public ChangeType getAction(CIFAsset baseAsset, CIFAsset compareTo) {
        if(compareTo==null) {
            return ADD;
        }
        if(baseAsset.getAssetVersionStatus().equals(CEASED)) {
            return DELETE;
        }
        if(characteristicsChanged(baseAsset, compareTo)) {
            return UPDATE;
        }

        return NONE;
    }

    private boolean characteristicsChanged(CIFAsset baseAsset, CIFAsset compareTo) {
        for (CIFAssetCharacteristic cifAssetCharacteristic : baseAsset.getCharacteristics()) {
            final CIFAssetCharacteristic compareToCharacteristic = compareTo.getCharacteristic(cifAssetCharacteristic.getName());
            if(compareToCharacteristic!=null && !ObjectUtils.equals(cifAssetCharacteristic.getValue(), compareToCharacteristic.getValue())) {
                return true;
            }
        }

        return false;
    }
}
