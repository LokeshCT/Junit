package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetStencilDetail;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateStencilRequest;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.util.TestWithRules;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;


public class UpdateRelationshipUpdaterTest extends TestWithRules {
    private static final AssetKey ASSET_KEY = new AssetKey("assetId", 2);
    public static final String CHILD_RELATIONSHIP_NAME = "childRelationshipName";
    public static final String RELATED_TO_RELATIONSHIP_NAME = "relatedToRelationship";
    public static final ArrayList<CIFAssetExtension> ASSET_EXTENSIONS = new ArrayList<CIFAssetExtension>();
    private CIFAssetOrchestrator assetOrchestrator;
    public static final String LINE_ITEM_ID = "lineItemId";
    public static final int LOCK_VERSION = 1;
    private CIFAsset ownerAsset;
    private CIFAsset ownerAsset2;
    private CIFAsset ownerAsset3;
    private CIFAsset childAsset;
    private CIFAsset childAsset2;
    private CIFAsset relatedAsset;
    private CIFAssetStencilDetail  cifAssetStencilDetail;
    private CIFAssetStencilDetail  cifAssetStencilDetailWithEmptyAllowedStencils;
    private final List<CIFAssetStencilDetail> emptyAllowedStencils = newArrayList();
    private final List<CIFAssetStencilDetail> allowedStencils = newArrayList();
    CIFAssetOfferingDetail offeringDetail;
    CIFAssetOfferingDetail offeringDetail2;

    @Before
    public void setUp() throws Exception {
        assetOrchestrator = mock(CIFAssetOrchestrator.class);
        offeringDetail= new CIFAssetOfferingDetail(null, null, null, null, true, true, null, true, true, null); // bearer field is true

        CIFAssetStencilDetail cifAssetStencilDetailAllowedStencil = new CIFAssetStencilDetail("AllowedStencilCode", "AllowedStencilVersion", "AllowedProductName", emptyAllowedStencils);
        allowedStencils.add(cifAssetStencilDetailAllowedStencil);

        cifAssetStencilDetail = new CIFAssetStencilDetail("StencilCode", "StencilVersion", "ProductName", allowedStencils);

        childAsset = CIFAssetFixture.aCIFAsset()
                                    .withID("childID")
                                    .withVersion(1l)
                                    .withRelationships(0)
                                    .with(cifAssetStencilDetail)
                                    .withLineItemId("childLineItemID")
                                    .build();
        relatedAsset = CIFAssetFixture.aCIFAsset()
                                    .withID("relatedID")
                                    .withVersion(1l)
                                    .withRelationships(0)
                                    .with(cifAssetStencilDetail)
                                    .withLineItemId("relatedLineItemID")
                                    .build();
        ownerAsset = CIFAssetFixture.aCIFAsset()
                                    .withID("ownerID")
                                    .withVersion(1l)
                                    .withRelationship(childAsset, CHILD_RELATIONSHIP_NAME, RelationshipType.Child)
                                    .withRelationship(relatedAsset, RELATED_TO_RELATIONSHIP_NAME, RelationshipType.RelatedTo)
                                    .with(offeringDetail)
                                    .with(cifAssetStencilDetail)
                                    .withLineItemId("ownerLineItemID")
                                    .build();

        when(assetOrchestrator.getAsset(
            new CIFAssetKey(new AssetKey("updatesToId", 1l),
                            newArrayList(CIFAssetExtension.ProductOfferingRelationshipDetail,
                                         CIFAssetExtension.QuoteOptionItemDetail)))).thenReturn(ownerAsset);
    }

    @Test
    public void shouldReturnUpdateRelationshipResponseFromAssetOrchestratorResponses() {


        UpdateRelationshipRequest request = new UpdateRelationshipRequest(ASSET_KEY, "updatesToId",1l,"newStencilId",
                                                                          LINE_ITEM_ID, LOCK_VERSION);
        UpdateStencilRequest updateStencilRequestChild = new UpdateStencilRequest(childAsset.getAssetKey(),
                                 "newStencilId",
                                 null,
                                 null,
                                 childAsset.getLineItemId(),
                                 1);
        UpdateStencilRequest updateStencilRequestParent = new UpdateStencilRequest(ownerAsset.getAssetKey(),
                                                                                  "newStencilId",
                                                                                  null,
                                                                                  null,
                                                                                  ownerAsset.getLineItemId(),
                                                                                  1);
        List<CIFAssetUpdateRequest> dependantUpdates = newArrayList();
        dependantUpdates.add(updateStencilRequestChild);
        dependantUpdates.add(updateStencilRequestParent);
        UpdateRelationshipResponse expectedResponse = new UpdateRelationshipResponse(request, dependantUpdates);

        final UpdateRelationshipUpdater updateRelationshipUpdater = new UpdateRelationshipUpdater(assetOrchestrator);
        final UpdateRelationshipResponse updateRelationshipResponse = updateRelationshipUpdater.performUpdate(request);

        assertThat(updateRelationshipResponse, is(expectedResponse));
        verify(assetOrchestrator, times(1)).getAsset(any(CIFAssetKey.class));
        verifyNoMoreInteractions(assetOrchestrator);

    }

    @Test
         public void shouldReturnNoDependantUpdatesForNonBearerAssetFromAssetOrchestratorResponses() {

        offeringDetail2= new CIFAssetOfferingDetail(null, null, null, null, false, true, null, true, true, null); // bearer field is false
        ownerAsset2 = CIFAssetFixture.aCIFAsset()
                                     .withID("ownerID2")
                                     .withVersion(1l)
                                     .with(offeringDetail2)
                                     .withRelationships(0)
                                     .with(cifAssetStencilDetail)
                                     .withLineItemId("ownerLineItemID2")
                                     .build();

        UpdateRelationshipRequest request = new UpdateRelationshipRequest(ASSET_KEY, "updatesToId",1l,"newStencilId",
                                                                          LINE_ITEM_ID, LOCK_VERSION);

        List<CIFAssetUpdateRequest> dependantUpdates = newArrayList();
        UpdateRelationshipResponse expectedResponse = new UpdateRelationshipResponse(request, dependantUpdates);

        when(assetOrchestrator.getAsset(
            new CIFAssetKey(new AssetKey("updatesToId", 1l),
                            newArrayList(CIFAssetExtension.ProductOfferingRelationshipDetail,
                                         CIFAssetExtension.QuoteOptionItemDetail)))).thenReturn(ownerAsset2);

        final UpdateRelationshipUpdater updateRelationshipUpdater = new UpdateRelationshipUpdater(assetOrchestrator);
        final UpdateRelationshipResponse updateRelationshipResponse = updateRelationshipUpdater.performUpdate(request);

        assertThat(updateRelationshipResponse, is(expectedResponse));
        verify(assetOrchestrator, times(1)).getAsset(any(CIFAssetKey.class));
        verifyNoMoreInteractions(assetOrchestrator);
    }

    @Test
    public void shouldReturnNoDependantUpdateForChildThatIsNotStencilFromAssetOrchestratorResponses() {

        cifAssetStencilDetailWithEmptyAllowedStencils = new CIFAssetStencilDetail("StencilCode", "StencilVersion", "ProductName", emptyAllowedStencils);

        childAsset2 = CIFAssetFixture.aCIFAsset()
                                    .withID("childID2")
                                    .withVersion(1l)
                                    .withRelationships(0)
                                    .with(cifAssetStencilDetailWithEmptyAllowedStencils)
                                    .withLineItemId("childLineItemID2")
                                    .build();

        offeringDetail2= new CIFAssetOfferingDetail(null, null, null, null, true, true, null, true, true, null); // bearer field is true
        ownerAsset3 = CIFAssetFixture.aCIFAsset()
                                     .withID("ownerID3")
                                     .withVersion(1l)
                                     .with(offeringDetail)
                                     .withRelationship(childAsset2, CHILD_RELATIONSHIP_NAME, RelationshipType.Child)
                                     .with(cifAssetStencilDetail)
                                     .withLineItemId("ownerLineItemID3")
                                     .build();

        UpdateRelationshipRequest request = new UpdateRelationshipRequest(ASSET_KEY, "updatesToId",1l,"newStencilId",
                                                                          LINE_ITEM_ID, LOCK_VERSION);

        UpdateStencilRequest updateStencilRequestParent = new UpdateStencilRequest(ownerAsset3.getAssetKey(),
                                                                                   "newStencilId",
                                                                                   null,
                                                                                   null,
                                                                                   ownerAsset3.getLineItemId(),
                                                                                   1);

        List<CIFAssetUpdateRequest> dependantUpdates = newArrayList();
        dependantUpdates.add(updateStencilRequestParent);
        UpdateRelationshipResponse expectedResponse = new UpdateRelationshipResponse(request, dependantUpdates);

        when(assetOrchestrator.getAsset(
            new CIFAssetKey(new AssetKey("updatesToId", 1l),
                            newArrayList(CIFAssetExtension.ProductOfferingRelationshipDetail,
                                         CIFAssetExtension.QuoteOptionItemDetail)))).thenReturn(ownerAsset3);

        final UpdateRelationshipUpdater updateRelationshipUpdater = new UpdateRelationshipUpdater(assetOrchestrator);
        final UpdateRelationshipResponse updateRelationshipResponse = updateRelationshipUpdater.performUpdate(request);

        assertThat(updateRelationshipResponse, is(expectedResponse));
        verify(assetOrchestrator, times(1)).getAsset(any(CIFAssetKey.class));
        verifyNoMoreInteractions(assetOrchestrator);
    }
}

