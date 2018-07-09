package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;

public class AssetUniqueId {
    static String getAssetUniqueId(CIFAsset cifAsset) {
        if(cifAsset.getAssetUniqueId()==null) {
            int assetIdLength = cifAsset.getAssetKey().getAssetId().length();
            return cifAsset.getAssetKey().getAssetId().substring(assetIdLength-5, assetIdLength);
        }else{
            return cifAsset.getAssetUniqueId();
        }
    }
}
