package com.bt.rsqe.customerinventory.service.filters;

import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class CIFAssetFilter {
    private Predicate<CIFAsset> predicate;

    public CIFAssetFilter(Predicate<CIFAsset> predicate) {
        this.predicate = predicate;
    }

    public List<CIFAsset> filter(List<CIFAsset> assets) {
        return newArrayList(Iterables.filter(assets, predicate));
    }

    /**
     * When the parent is live it only returns live related to assets,
     * when the parent is not live it returns all related to assets
     */
    public static CIFAssetFilter filterByLiveAssetStatus(final CIFAsset parent) {
        final boolean parentIsLive = ProductInstanceState.LIVE.equals(parent.getStatus());

        return new CIFAssetFilter(new Predicate<CIFAsset>() {
            @Override
            public boolean apply(CIFAsset input) {
                return !(parentIsLive && !ProductInstanceState.LIVE.equals(input.getStatus()));
            }
        });
    }

    public static CIFAssetFilter filterByQuoteOptionIdOrInServiceProvisioning(final String quoteOptionId, final AssetVersionStatus... assetVersionStatuses) {
        return new CIFAssetFilter(new Predicate<CIFAsset>() {
            @Override
            public boolean apply(CIFAsset input) {
                boolean isValidStatus = false;
                for(AssetVersionStatus assetVersionStatus : assetVersionStatuses) {
                   isValidStatus = isValidStatus || input.getAssetVersionStatus().equals(assetVersionStatus);
                }
                return input.getQuoteOptionId().equals(quoteOptionId) || isValidStatus;
            }
        });
    }

    public static CIFAssetFilter latestAssetsFilter(final List<CIFAsset> assets) {           // ensure only the latest version of an asset is returned
        return new CIFAssetFilter(new Predicate<CIFAsset>() {

            private boolean laterVersionExists(final CIFAsset asset) {
                return Iterables.tryFind(assets, new Predicate<CIFAsset>() {
                    @Override
                    public boolean apply(CIFAsset input) {
                        return asset.getAssetKey().getAssetId().equals(input.getAssetKey().getAssetId()) &&
                               input.getAssetKey().getAssetVersion() > asset.getAssetKey().getAssetVersion();
                    }
                }).isPresent();
            }

            @Override
            public boolean apply(CIFAsset input) {
                return !laterVersionExists(input);
            }
        });
    }
}
