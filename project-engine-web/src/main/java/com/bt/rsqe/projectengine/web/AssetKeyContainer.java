package com.bt.rsqe.projectengine.web;


import com.bt.rsqe.domain.AssetKey;
import com.google.common.base.Optional;

import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.isNotNull;
import static com.google.common.collect.Maps.*;

public class AssetKeyContainer {
    Map<String, AssetKey> assetKeys = newHashMap();

    public void addKey(String rowId, AssetKey key) {
        assetKeys.put(rowId, key);
    }

    public Optional<AssetKey> getAssetKey(String rowId) {
        AssetKey assetKey = assetKeys.get(rowId);
        return isNotNull(assetKey) ? Optional.of(assetKey) : Optional.<AssetKey>absent();
    }
}
