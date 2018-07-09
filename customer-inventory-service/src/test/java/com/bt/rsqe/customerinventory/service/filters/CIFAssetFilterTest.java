package com.bt.rsqe.customerinventory.service.filters;

import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.enums.AssetVersionStatus;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.matchers.JUnitMatchers.hasItems;

public class CIFAssetFilterTest {
    @Test
    public void shouldOnlyReturnLiveAssetsWhenParentIsLive() throws Exception {
        CIFAsset parent = CIFAssetFixture.aCIFAsset().withStatus(ProductInstanceState.LIVE).build();
        List<CIFAsset> assets = newArrayList(CIFAssetFixture.aCIFAsset().withStatus(ProductInstanceState.LIVE).build(),
                                             CIFAssetFixture.aCIFAsset().withStatus(ProductInstanceState.REMOVED).build(),
                                             CIFAssetFixture.aCIFAsset().withStatus(ProductInstanceState.LIVE).build(),
                                             CIFAssetFixture.aCIFAsset().withStatus(ProductInstanceState.CEASED).build(),
                                             CIFAssetFixture.aCIFAsset().withStatus(ProductInstanceState.CANCELLED).build());

        List<CIFAsset> results = CIFAssetFilter.filterByLiveAssetStatus(parent).filter(assets);
        assertThat(results.isEmpty(), is(false));
        for(CIFAsset asset : results) {
            assertThat(asset.getStatus(), is(ProductInstanceState.LIVE));
        }
    }

    @Test
    public void shouldReturnAllRelatedAssetsWhenParentIsNotLive() throws Exception {
        CIFAsset parent = CIFAssetFixture.aCIFAsset().withStatus(ProductInstanceState.CANCELLED).build();
        List<CIFAsset> assets = newArrayList(CIFAssetFixture.aCIFAsset().withStatus(ProductInstanceState.LIVE).build(),
                                             CIFAssetFixture.aCIFAsset().withStatus(ProductInstanceState.REMOVED).build(),
                                             CIFAssetFixture.aCIFAsset().withStatus(ProductInstanceState.LIVE).build(),
                                             CIFAssetFixture.aCIFAsset().withStatus(ProductInstanceState.CEASED).build(),
                                             CIFAssetFixture.aCIFAsset().withStatus(ProductInstanceState.CANCELLED).build());

        List<CIFAsset> results = CIFAssetFilter.filterByLiveAssetStatus(parent).filter(assets);
        assertThat(results.size(), is(5));
    }

    @Test
    public void shouldFilterOwnerAssetsByQuoteOptionIdOrInServiceProvisioning() {
        String quoteOptionId = "quoteOptionId";
        List<CIFAsset> ownerAssets = newArrayList(CIFAssetFixture.aCIFAsset().withQuoteOptionId("quoteOptionId").withAssetVersionStatus(AssetVersionStatus.DRAFT).build(),
                                                  CIFAssetFixture.aCIFAsset().withQuoteOptionId("quoteOptionId1").withAssetVersionStatus(AssetVersionStatus.PROVISIONING).build(),
                                                  CIFAssetFixture.aCIFAsset().withQuoteOptionId("quoteOptionId2").withAssetVersionStatus(AssetVersionStatus.IN_SERVICE).build());

        List<CIFAsset> results = CIFAssetFilter.filterByQuoteOptionIdOrInServiceProvisioning(quoteOptionId, AssetVersionStatus.PROVISIONING, AssetVersionStatus.IN_SERVICE).filter(ownerAssets);
        assertThat(results.size(), is(3));
    }

    @Test
    public void shouldFilterLatestAssets() throws Exception {
        CIFAsset asset1 = CIFAssetFixture.aCIFAsset().withVersion(1L).withID("aProductInstance").build();
        CIFAsset asset2 = CIFAssetFixture.aCIFAsset().withVersion(2L).withID("aProductInstance").build();
        CIFAsset asset3 = CIFAssetFixture.aCIFAsset().withVersion(3L).withID("aProductInstance").build();
        CIFAsset asset4 = CIFAssetFixture.aCIFAsset().withVersion(1L).withID("anotherProductInstance").build();
        List<CIFAsset> assets = newArrayList(asset1, asset2, asset3, asset4);

        List<CIFAsset> filteredAssets = CIFAssetFilter.latestAssetsFilter(assets).filter(assets);
        assertThat(filteredAssets.size(), is(2));
        assertThat(filteredAssets, hasItems(asset3, asset4));
    }
}
