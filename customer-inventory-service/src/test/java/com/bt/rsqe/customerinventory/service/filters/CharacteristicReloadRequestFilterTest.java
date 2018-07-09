package com.bt.rsqe.customerinventory.service.filters;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ValidationImpactChangeRequest;
import com.bt.rsqe.domain.AssetKey;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

public class CharacteristicReloadRequestFilterTest {

    @Test
    public void shouldFilterReloadRequestIfCancellingRequestIsAlreadyAvailable() {
        //Given
        final CIFAssetUpdateRequest cancelRelationshipRequest = new CancelRelationshipRequest(new AssetKey("assetIdTwo", 1l), "lineItemId", 15,
                                                                                                  new AssetKey("cancellingId", 1), "aRelationshipName", "S123", true);

        final CIFAssetUpdateRequest reloadRequest = new CharacteristicReloadRequest(new AssetKey("cancellingId", 1), "anAttribute", 1);
        final CIFAssetUpdateRequest otherReloadRequest = new CharacteristicReloadRequest(new AssetKey("someOtherId", 1), "anAttribute", 1);

        //When
        CharacteristicReloadRequestFilter reloadRequestFilter = new CharacteristicReloadRequestFilter();
        List<CIFAssetUpdateRequest> requests = reloadRequestFilter.filter(newArrayList(cancelRelationshipRequest), newArrayList(reloadRequest, otherReloadRequest));

        ///Then
        assertThat(requests.size(), is(1));
        assertThat(requests, hasItem(otherReloadRequest));
    }

    @Test
    public void shouldNotFilterReloadRequestIfCancellingRequestIsAlreadyAvailable() {
        //Given
        final CIFAssetUpdateRequest cancelRelationshipRequest = new CancelRelationshipRequest(new AssetKey("assetIdTwo", 1l), "lineItemId", 15,
                                                                                              new AssetKey("cancellingId", 1), "aRelationshipName", "S123", true);

        final CIFAssetUpdateRequest reloadRequest = new CharacteristicReloadRequest(new AssetKey("anId", 1), "anAttribute", 1);
        final CIFAssetUpdateRequest otherReloadRequest = new CharacteristicReloadRequest(new AssetKey("someOtherId", 1), "anAttribute", 1);
        final CIFAssetUpdateRequest impactRequest = new ValidationImpactChangeRequest(new AssetKey("someOtherId", 1));

        //When
        CharacteristicReloadRequestFilter reloadRequestFilter = new CharacteristicReloadRequestFilter();
        List<CIFAssetUpdateRequest> requests = reloadRequestFilter.filter(newArrayList(cancelRelationshipRequest), newArrayList(reloadRequest, otherReloadRequest, impactRequest));

        ///Then
        assertThat(requests.size(), is(3));
        assertThat(requests, hasItems(reloadRequest, otherReloadRequest, impactRequest));
    }

}