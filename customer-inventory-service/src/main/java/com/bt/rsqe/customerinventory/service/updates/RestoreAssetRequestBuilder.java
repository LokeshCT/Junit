package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RestoreAssetRequest;
import com.bt.rsqe.domain.AssetKey;

/**
 * Created by 802998369 on 05/12/2015.
 */
public class RestoreAssetRequestBuilder
{

    public RestoreAssetRequest restoreAssetRequest(AssetKey assetKey, String lineItemId, int lockVersion)
    {
        return new RestoreAssetRequest(assetKey, lineItemId, lockVersion);
    }

    public RestoreAssetRequest restoreAssetRequest(CIFAsset cifAsset)
    {
        return new RestoreAssetRequest(cifAsset.getAssetKey(), cifAsset.getLineItemId(), cifAsset.getQuoteOptionItemDetail().getLockVersion());
    }
}
