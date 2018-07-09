package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import org.mockito.ArgumentMatcher;

class AssetKeyMatcher extends ArgumentMatcher<CIFAssetKey>
{
    private final CIFAsset asset;

    public AssetKeyMatcher (CIFAsset asset)
    {
        this.asset = asset;
    }

    @Override
    public boolean matches (Object argument)
    {
        CIFAssetKey key = (CIFAssetKey)argument ;
        if (key == null && (asset != null && asset.getAssetKey() != null))
        {
            return false ;
        }
        return key.getAssetKey().equals(asset.getAssetKey()) ;
    }

    static public ArgumentMatcher<CIFAssetKey> assetKeyMatcher (CIFAsset asset)
    {
        return new AssetKeyMatcher(asset);
    }
}
