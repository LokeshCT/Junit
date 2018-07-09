package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.google.common.base.Optional;
import org.junit.Test;

import java.util.ArrayList;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;

public class AsIsAssetExtenderTest {
    private final CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private final AsIsAssetExtender asIsAssetExtender = new AsIsAssetExtender(cifAssetOrchestrator);
    private final CIFAsset cifAsset = mock(CIFAsset.class);
    private final CIFAsset inServiceAsset = mock(CIFAsset.class);

    @Test
    public void shouldLoadAsIsAssetWhenRequested() {
        AssetKey baseAssetKey = new AssetKey("ASSET_ID", 1);
        when(cifAsset.getAssetKey()).thenReturn(baseAssetKey);
        when(cifAssetOrchestrator.getInServiceAsset(new CIFAssetKey(baseAssetKey))).thenReturn(Optional.of(inServiceAsset));

        asIsAssetExtender.extend(newArrayList(AsIsAsset), cifAsset);

        verify(cifAsset, times(1)).loadAsIsAsset(inServiceAsset);
    }

    @Test
    public void shouldLoadNullAsIsAssetWhenItDoesNotExist() {
        AssetKey baseAssetKey = new AssetKey("ASSET_ID", 1);
        when(cifAsset.getAssetKey()).thenReturn(baseAssetKey);
        when(cifAssetOrchestrator.getInServiceAsset(new CIFAssetKey(baseAssetKey))).thenReturn(Optional.<CIFAsset>absent());

        asIsAssetExtender.extend(newArrayList(AsIsAsset), cifAsset);

        verify(cifAsset, times(1)).loadAsIsAsset(null);
    }

    @Test
    public void shouldNotLoadAsIsAssetWhenNotRequested() {
        asIsAssetExtender.extend(new ArrayList<CIFAssetExtension>(), cifAsset);

        verifyNoMoreInteractions(cifAsset);
    }
}