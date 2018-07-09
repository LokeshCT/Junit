package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RestoreAssetRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RestoreAssetResponse;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import org.junit.Test;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by 802998369 on 05/12/2015.
 */
public class RestoreAssetUpdaterTest {

    CIFAssetOrchestrator cifAssetOrchestrator = mock (CIFAssetOrchestrator.class);
    DependentUpdateBuilderFactory dependentUpdateBuilderFactory = new DependentUpdateBuilderFactoryBuilder().with(new RestoreAssetRequestBuilder()).build();
    RestoreAssetUpdater updater = new RestoreAssetUpdater(cifAssetOrchestrator, dependentUpdateBuilderFactory);

    @Test (expected = UpdateException.class )
    public void shouldPerformUpdate() throws Exception {
        AssetKey assetKey = new AssetKey ("assetId", 1L);
        String lineItemId = "lineItemId" ;
        int lockVersion = 1 ;
        RestoreAssetRequest request = dependentUpdateBuilderFactory.getRestoreAssetRequestBuilder().restoreAssetRequest(assetKey, lineItemId, lockVersion) ;

        CIFAsset toBeAsset = mock (CIFAsset.class);
        when (toBeAsset.getCharacteristics()).thenReturn(new ArrayList<CIFAssetCharacteristic>()) ;
        when (cifAssetOrchestrator.getCifAsset(new CIFAssetKey(request.getAssetKey(), newArrayList (CIFAssetExtension.AsIsAsset, CIFAssetExtension.CharacteristicValue, CIFAssetExtension.Relationships)))).thenReturn(toBeAsset) ;

        RestoreAssetResponse response = updater.performUpdate(request);

        assertThat (response.getDependantUpdates().isEmpty(), is(true)) ;
        assertThat (response.getRequest(), is(request)) ;
    }
}