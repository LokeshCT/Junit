package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CardinalityImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.domain.AssetKey;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.*;
import static com.bt.rsqe.domain.product.parameters.RelationshipType.*;
import static com.google.common.collect.Sets.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class CancellationContributesToRequestBuilderTest {
    @Test
    public void shouldConstructContributesToRequestForAssetsWhichAreNotCancelled() {
        //Given
        CIFAsset grandChildAsset = aCIFAsset().withID("grandChild").withProductIdentifier("grandChildCode", "A.1").withVersion(1).withRelationships(0).withNullMovesToId().build();
        CIFAsset childAsset = aCIFAsset().withID("child").withProductIdentifier("childCode", "A.1").withVersion(1).withRelationship(grandChildAsset, "grandChildRelation", Child).withNullMovesToId().build();
        CIFAsset parentAsset = aCIFAsset().withID("parent").withProductIdentifier("parentCode", "A.1").withVersion(1).withRelationship(childAsset, "childRelation", Child).build();

        ContributesToChangeRequestBuilder changeRequestBuilder = mock(ContributesToChangeRequestBuilder.class);

        Set<CIFAssetUpdateRequest> childContributedRequests = newHashSet();
        final CharacteristicReloadRequest parentReloadRequest = new CharacteristicReloadRequest(new AssetKey("parent", 1L), "someParentAttribute", 1);
        final CharacteristicReloadRequest otherAssetReloadRequest = new CharacteristicReloadRequest(new AssetKey("someOtherAsset", 1L), "someAttribute", 1);
        final CardinalityImpactChangeRequest cardinalityImpactToOtherAssetRequest = new CardinalityImpactChangeRequest(new AssetKey("someOtherAsset", 1L));
        childContributedRequests.add(parentReloadRequest);
        childContributedRequests.add(otherAssetReloadRequest);
        childContributedRequests.add(cardinalityImpactToOtherAssetRequest);

        Set<CIFAssetUpdateRequest> parentContributedRequests = newHashSet();
        final CharacteristicReloadRequest childReloadRequest = new CharacteristicReloadRequest(new AssetKey("child", 1L), "someChildAttribute", 1);
        parentContributedRequests.add(childReloadRequest);

        when(changeRequestBuilder.buildRequestsOnCancellation(new AssetKey("grandChild", 1L), "grandChildCode", 1)).thenReturn(Collections.<CIFAssetUpdateRequest>emptySet());
        when(changeRequestBuilder.buildRequestsOnCancellation(new AssetKey("child", 1L), "childCode", 1)).thenReturn(childContributedRequests);
        when(changeRequestBuilder.buildRequestsOnCancellation(new AssetKey("parent", 1L), "parentCode", 1)).thenReturn(parentContributedRequests);

        //When
        final Set<CIFAssetUpdateRequest> cifAssetUpdateRequests = new CancellationContributesToRequestBuilder(changeRequestBuilder).buildRequests(parentAsset);

        //Then
        assertThat(cifAssetUpdateRequests.size(), is(2));
        assertThat(cifAssetUpdateRequests, hasItems(otherAssetReloadRequest, cardinalityImpactToOtherAssetRequest));
    }
}