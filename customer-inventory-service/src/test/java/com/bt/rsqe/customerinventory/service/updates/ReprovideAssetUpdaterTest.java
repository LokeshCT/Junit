package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetStencilDetail;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ChooseRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ReprovideAssetRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ReprovideAssetResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRequestSource;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.matchers.ReflectionEqualsMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.aCIFAsset;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class ReprovideAssetUpdaterTest {
    private static final String RELATIONSHIP_NAME = "relationshipName";
    private final CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private final CreateRelationshipUpdater createRelationshipUpdater = mock(CreateRelationshipUpdater.class);
    private final CancelRelationshipUpdater cancelRelationshipUpdater = mock(CancelRelationshipUpdater.class);
    private final InvalidatePriceRequestBuilder invalidatePriceRequestBuilder = mock(InvalidatePriceRequestBuilder.class) ;
    private final DependentUpdateBuilderFactory dependentUpdateBuilderFactory = new DependentUpdateBuilderFactoryBuilder().with(invalidatePriceRequestBuilder).build();
    private final ReprovideAssetUpdater reprovideAssetUpdater = new ReprovideAssetUpdater(cifAssetOrchestrator, createRelationshipUpdater,
                                                                                          cancelRelationshipUpdater, dependentUpdateBuilderFactory);
    private final AssetKey reprovideAssetKey = new AssetKey("assetId2", 1);
    private final CIFAsset reprovideAsset = aCIFAsset().withID(reprovideAssetKey.getAssetId()).withVersion(reprovideAssetKey.getAssetVersion())
                                                       .withProductIdentifier("productCode", "v1")
                                                       .with(new CIFAssetStencilDetail("stencilCode", "v1", "prodName",
                                                                                       new ArrayList<CIFAssetStencilDetail>()))
                                                       .withSiteId("1235")
                                                       .build();
    private final AssetKey owningAssetKey = new AssetKey("assetId", 1);
    private final AssetKey otherOwner1AssetKey = new AssetKey("assetId4", 1);
    private final AssetKey otherOwner2AssetKey = new AssetKey("assetId5", 1);
    private final CIFAsset someOtherAsset = aCIFAsset().build();
    private final CIFAsset otherOwner1 = aCIFAsset().withID(otherOwner1AssetKey.getAssetId()).withVersion(otherOwner1AssetKey.getAssetVersion())
                                                    .withRelationship(reprovideAsset, "otherRelationshipName1", RelationshipType.Child)
                                                    .build();
    private final CIFAsset otherOwner2 = aCIFAsset().withID(otherOwner2AssetKey.getAssetId()).withVersion(otherOwner2AssetKey.getAssetVersion())
                                                    .withRelationship(reprovideAsset, "otherRelationshipName2", RelationshipType.Child)
                                                    .build();
    private final CIFAsset owningAsset1 = aCIFAsset().withID(owningAssetKey.getAssetId()).withVersion(owningAssetKey.getAssetVersion())
                                                     .withRelationship(someOtherAsset, RELATIONSHIP_NAME, RelationshipType.Child)
                                                     .withRelationship(reprovideAsset, RELATIONSHIP_NAME, RelationshipType.RelatedTo).build();
    private final AssetKey newReprovidedAssetKey = new AssetKey("assetId3", 1);
    private final CancelRelationshipRequest expectedCancelRequest = new CancelRelationshipRequest(owningAssetKey, "", 0, reprovideAssetKey,
                                                                                                  RELATIONSHIP_NAME, reprovideAsset.getProductCode(), false);
    private final CIFAssetUpdateRequest mockCancelDependency = mock(CIFAssetUpdateRequest.class);
    private final List<CIFAssetUpdateRequest> cancelDependencies = newArrayList(mockCancelDependency);
    private final CreateRelationshipRequest expectedCreateRequest = new CreateRelationshipRequest("", owningAssetKey, RELATIONSHIP_NAME,
                                                                                                  reprovideAsset.getProductCode(),
                                                                                                  reprovideAsset.getStencilDetail().getStencilCode(),
                                                                                                  reprovideAsset.getSiteId(),
                                                                                                  "", "", 0);
    private final CIFAssetUpdateRequest mockCreateDependency = mock(CIFAssetUpdateRequest.class);
    private final List<CIFAssetUpdateRequest> createDependencies = newArrayList(mockCreateDependency);
    private ReprovideAssetResponse reprovideAssetResponse;

    @Before
    public void setUp() throws Exception {
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(owningAssetKey, newArrayList(CIFAssetExtension.Relationships))))
            .thenReturn(owningAsset1);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(reprovideAssetKey))).thenReturn(reprovideAsset);
        when(cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(reprovideAssetKey, newArrayList(CIFAssetExtension.Relationships))))
            .thenReturn(newArrayList(otherOwner1, otherOwner2));

        CancelRelationshipResponse cancelResponse = new CancelRelationshipResponse(expectedCancelRequest, RelationshipType.RelatedTo, cancelDependencies);
        when(cancelRelationshipUpdater.performUpdate(org.mockito.Matchers.any(CancelRelationshipRequest.class))).thenReturn(cancelResponse);
        CreateRelationshipResponse createResponse = new CreateRelationshipResponse(expectedCreateRequest, RELATIONSHIP_NAME,
                                                                                   RelationshipType.Child, ProductInstanceState.LIVE,
                                                                                   newReprovidedAssetKey, createDependencies, "");
        when(createRelationshipUpdater.performUpdate(org.mockito.Matchers.any(CreateRelationshipRequest.class))).thenReturn(createResponse);

        ReprovideAssetRequest request = new ReprovideAssetRequest(owningAssetKey, reprovideAssetKey, "", 0);
        reprovideAssetResponse = reprovideAssetUpdater.performUpdate(request);
    }

    @Test
    public void shouldCallAssetCancelUpdaterToCancelAssetAndRespondWithItsResponses() {
        verify(cancelRelationshipUpdater).performUpdate(expectedCancelRequest);
        assertThat(reprovideAssetResponse.getDependantUpdates(), hasItem(mockCancelDependency));
    }

    @Test
    public void shouldCallCreateRelationshipUpdaterToCreateNewRelationshipAndRespondWithItsResponses() {
        ArgumentCaptor<CreateRelationshipRequest> argumentCaptor  = ArgumentCaptor.forClass(CreateRelationshipRequest.class);
        verify(createRelationshipUpdater).performUpdate(argumentCaptor.capture());

        CreateRelationshipRequest createRelationshipRequest = argumentCaptor.getValue();
        assertThat(createRelationshipRequest, ReflectionEqualsMatcher.reflectionEquals(expectedCreateRequest, "clientIdentifier"));
        assertThat(createRelationshipRequest.getClientIdentifier(), notNullValue());
        assertThat(reprovideAssetResponse.getDependantUpdates(), hasItem(mockCreateDependency));
    }

    @Test
    public void shouldCreateChooseRequestsForEachOfThePreviouslyOwningAssets() {
        CIFAssetUpdateRequest chooseDependency1 = new ChooseRelationshipRequest(otherOwner1.getAssetKey(), newReprovidedAssetKey,
                                                                                "otherRelationshipName1", "", 0, UpdateRequestSource.RelateTo);
        CIFAssetUpdateRequest chooseDependency2 = new ChooseRelationshipRequest(otherOwner2.getAssetKey(), newReprovidedAssetKey,
                                                                                "otherRelationshipName2", "", 0, UpdateRequestSource.RelateTo);
        assertThat(reprovideAssetResponse.getDependantUpdates(), hasItem(chooseDependency1));
        assertThat(reprovideAssetResponse.getDependantUpdates(), hasItem(chooseDependency2));
    }
}