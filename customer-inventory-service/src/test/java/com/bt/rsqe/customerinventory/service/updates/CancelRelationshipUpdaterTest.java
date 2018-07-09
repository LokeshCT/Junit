package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.cache.CacheAwareTransaction;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.externals.QuoteEngineHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.enums.AssetType;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import org.junit.After;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class CancelRelationshipUpdaterTest {

    private CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private QuoteEngineHelper quoteEngineHelper = mock(QuoteEngineHelper.class);
    private PmrHelper pmrHelper = mock(PmrHelper.class);
    private ContributesToChangeRequestBuilder contributesToChangeRequestBuilder = mock(ContributesToChangeRequestBuilder.class);
    private DependentUpdateBuilderFactory dependentUpdateBuilderFactory = new DependentUpdateBuilderFactory(null, null, null, null, null,
                                                                                                            new CancellationContributesToRequestBuilder(contributesToChangeRequestBuilder), null);

    @Test
    public void shouldCancelGivenRelationAndConstructContributesToDependantRequest() {

        //Given
        CIFAsset relatedAsset = aCIFAsset().withID("relatedAssetId").withAssetVersionStatus(AssetVersionStatus.DRAFT).build();
        CIFAsset childAsset = aCIFAsset().withID("cancellingAssetId").withVersion(1L).withRelationship(relatedAsset, "relatedTo", RelationshipType.RelatedTo).withNullMovesToId().withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                         .build();
        CIFAsset parentAsset = aCIFAsset().withID("assetId").withVersion(1L).withRelationship(childAsset, "childRelation", RelationshipType.Child)
                                          .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                          .build();

        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("assetId", 1), newArrayList(CIFAssetExtension.Relationships)))).thenReturn(parentAsset) ;
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("assetId", 1), newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.QuoteOptionItemDetail)))).thenReturn(parentAsset) ;
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("cancellingAssetId", 1), newArrayList(CIFAssetExtension.Relationships)))).thenReturn(childAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("cancellingAssetId", 1), newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.QuoteOptionItemDetail)))).thenReturn(childAsset);

        Set<CancelRelationshipRequest> cifAssetUpdateRequests = newHashSet();
        CancelRelationshipRequest cancelRelationshipRequest = new CancelRelationshipRequest(childAsset.getAssetKey(), childAsset.getLineItemId(), 0, relatedAsset.getAssetKey(), "relatedTo", relatedAsset.getProductCode(), false);
        cifAssetUpdateRequests.add(cancelRelationshipRequest);
        when(cifAssetOrchestrator.cancelAssetTree(parentAsset.getAssetKey(), "childRelation", childAsset)).thenReturn(cifAssetUpdateRequests);

        Set<CIFAssetUpdateRequest> reloadAttributeRequests = newHashSet();
        CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest(parentAsset.getAssetKey(), "someAttribute", 1);
        reloadAttributeRequests.add(characteristicReloadRequest);
        when(contributesToChangeRequestBuilder.buildRequestsOnCancellation(childAsset.getAssetKey(), childAsset.getProductCode(), 1)).thenReturn(reloadAttributeRequests);

        when(cifAssetOrchestrator.isRootAsset(childAsset.getAssetKey())).thenReturn(false);

        //When
        CancelRelationshipUpdater updater = new CancelRelationshipUpdater(cifAssetOrchestrator, quoteEngineHelper, dependentUpdateBuilderFactory, pmrHelper);
        CancelRelationshipRequest request = new CancelRelationshipRequest(new AssetKey("assetId", 1), "lineItemId", 1, new AssetKey("cancellingAssetId", 1), "childRelation", childAsset.getProductCode(), false);
        final CancelRelationshipResponse response = updater.performUpdate(request);

        //Then
        assertThat(response.getRequest().getAssetKey(), is(new AssetKey("assetId", 1)));
        assertThat(response.getRequest().getLineItemId(), is("lineItemId"));
        assertThat(response.getRequest().getCancellingAssetId(), is(new AssetKey("cancellingAssetId", 1)));
        assertThat(response.getRequest().getRelationshipName(), is("childRelation"));

        assertThat(response.getDependantUpdates().size(), is(2));
        assertThat(response.getDependantUpdates(), hasItems(characteristicReloadRequest, cancelRelationshipRequest));

        verify(quoteEngineHelper, never()).removeQuoteOptionItem(childAsset.getProjectId(), childAsset.getQuoteOptionId(), childAsset.getLineItemId());
    }

    @Test
    public void shouldCancelGivenRootAssetAndConstructDependantCancellationRequest() {

        //Given
        CacheAwareTransaction.set(true);
        CIFAsset relatedAsset = aCIFAsset().withID("relatedAssetId").withAssetVersionStatus(AssetVersionStatus.DRAFT).build();
        CIFAsset parentAsset = aCIFAsset().withID("cancellingRootAssetId").withVersion(1L).withRelationship(relatedAsset, "relatedToRelation", RelationshipType.RelatedTo)
                                          .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                          .build();
        CIFAsset anotherRootAsset = aCIFAsset().withID("anotherRootAsset").withRelationship(relatedAsset, "someRelatedToRelation", RelationshipType.RelatedTo).withAssetVersionStatus(AssetVersionStatus.DRAFT).build();

        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("anotherRootAsset", 1), newArrayList(CIFAssetExtension.Relationships)))).thenReturn(anotherRootAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("anotherRootAsset", 1), newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.QuoteOptionItemDetail)))).thenReturn(anotherRootAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("relatedAssetId", 1), newArrayList(CIFAssetExtension.Relationships)))).thenReturn(relatedAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("relatedAssetId", 1), newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.QuoteOptionItemDetail)))).thenReturn(relatedAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("cancellingRootAssetId", 1), newArrayList(CIFAssetExtension.Relationships)))).thenReturn(parentAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("cancellingRootAssetId", 1), newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.QuoteOptionItemDetail)))).thenReturn(parentAsset);

        Set<CancelRelationshipRequest> cifAssetUpdateRequests = newHashSet();
        CancelRelationshipRequest cancelRelationshipRequest = new CancelRelationshipRequest(parentAsset.getAssetKey(), parentAsset.getLineItemId(), 0, relatedAsset.getAssetKey(), "relatedTo", relatedAsset.getProductCode(), false);
        cifAssetUpdateRequests.add(cancelRelationshipRequest);
        when(cifAssetOrchestrator.cancelAssetTree(org.mockito.Matchers.any(AssetKey.class), anyString(), eq(parentAsset))).thenReturn(cifAssetUpdateRequests);

        when(contributesToChangeRequestBuilder.buildRequestsOnCancellation(parentAsset.getAssetKey(), parentAsset.getProductCode(), 1)).thenReturn(Collections.<CIFAssetUpdateRequest>emptySet());
        when(cifAssetOrchestrator.isRootAsset(parentAsset.getAssetKey())).thenReturn(true);
        when(cifAssetOrchestrator.hasProvisiongOrInServiceAsset(parentAsset.getAssetKey())).thenReturn(false);
        QuoteOptionItemDTO optionItemDTO = new QuoteOptionItemDTO();
        when(quoteEngineHelper.getQuoteOptionItem(anyString(), anyString(), anyString())).thenReturn(optionItemDTO);

        //When
        CancelRelationshipUpdater updater = new CancelRelationshipUpdater(cifAssetOrchestrator, quoteEngineHelper, dependentUpdateBuilderFactory, pmrHelper);
        CancelRelationshipRequest request = new CancelRelationshipRequest(new AssetKey("anotherRootAsset", 1), "lineItemId", 1, new AssetKey("cancellingRootAssetId", 1), "someRelatedToRelation", parentAsset.getProductCode(), false);
        final CancelRelationshipResponse response = updater.performUpdate(request);

        //Then
        assertThat(response.getRequest().getAssetKey(), is(new AssetKey("anotherRootAsset", 1)));
        assertThat(response.getRequest().getLineItemId(), is("lineItemId"));
        assertThat(response.getRequest().getCancellingAssetId(), is(new AssetKey("cancellingRootAssetId", 1)));
        assertThat(response.getRequest().getRelationshipName(), is("someRelatedToRelation"));

        assertThat(response.getDependantUpdates().size(), is(1));
        final List<CIFAssetUpdateRequest> dependantUpdates = response.getDependantUpdates();
        assertThat((CancelRelationshipRequest) dependantUpdates.get(0), is(cancelRelationshipRequest));

        verify(quoteEngineHelper, times(1)).removeQuoteOptionItem(parentAsset.getProjectId(), parentAsset.getQuoteOptionId(), parentAsset.getLineItemId());
        verify(quoteEngineHelper, times(1)).getQuoteOptionItem(parentAsset.getProjectId(), parentAsset.getQuoteOptionId(), parentAsset.getLineItemId());

        Map<String, QuoteOptionItemDTO> removedQuoteOptionItems = AssetCacheManager.getRemovedQuoteOptionItems();
        assertThat(removedQuoteOptionItems.size(), is(1));
        assertThat(removedQuoteOptionItems.values(), hasItem(optionItemDTO));
    }

    @Test
    public void shouldCancelGivenRootAssetAndTriggerRuleForSharedAttributesAndConstructDependantCancellationRequest() {

        //Given
        CacheAwareTransaction.set(true);
        CIFAsset relatedAsset = aCIFAsset().withID("relatedAssetId").withAssetVersionStatus(AssetVersionStatus.DRAFT).build();
        CIFAsset parentAsset = aCIFAsset().withID("cancellingRootAssetId").withVersion(1L).withRelationship(relatedAsset, "relatedToRelation", RelationshipType.RelatedTo)
                .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                .withCharacteristic("SPLIT ORDER QUANTITY", "1")
                .withCharacteristic("NEW GROUP ID", "Quad")
                .build();
        CIFAsset anotherRootAsset = aCIFAsset().withID("anotherRootAsset").withRelationship(relatedAsset, "someRelatedToRelation", RelationshipType.RelatedTo).withAssetVersionStatus(AssetVersionStatus.DRAFT).build();

        ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withAttribute("SPLIT ORDER QUANTITY").withAttribute("NEW GROUP ID").build();

        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("anotherRootAsset", 1), newArrayList(CIFAssetExtension.Relationships)))).thenReturn(anotherRootAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("anotherRootAsset", 1), newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.QuoteOptionItemDetail)))).thenReturn(anotherRootAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("relatedAssetId", 1), newArrayList(CIFAssetExtension.Relationships)))).thenReturn(relatedAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("relatedAssetId", 1), newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.QuoteOptionItemDetail)))).thenReturn(relatedAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("cancellingRootAssetId", 1), newArrayList(CIFAssetExtension.Relationships)))).thenReturn(parentAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("cancellingRootAssetId", 1), newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.QuoteOptionItemDetail)))).thenReturn(parentAsset);

        when(pmrHelper.getProductOffering(parentAsset)).thenReturn(productOffering);

        Set<CancelRelationshipRequest> cifAssetUpdateRequests = newHashSet();
        CancelRelationshipRequest cancelRelationshipRequest = new CancelRelationshipRequest(parentAsset.getAssetKey(), parentAsset.getLineItemId(), 0, relatedAsset.getAssetKey(), "relatedTo", relatedAsset.getProductCode(), false);
        cifAssetUpdateRequests.add(cancelRelationshipRequest);
        when(cifAssetOrchestrator.cancelAssetTree(org.mockito.Matchers.any(AssetKey.class), anyString(), eq(parentAsset))).thenReturn(cifAssetUpdateRequests);

        when(contributesToChangeRequestBuilder.buildRequestsOnCancellation(parentAsset.getAssetKey(), parentAsset.getProductCode(), 1)).thenReturn(Collections.<CIFAssetUpdateRequest>emptySet());
        when(cifAssetOrchestrator.isRootAsset(parentAsset.getAssetKey())).thenReturn(true);
        QuoteOptionItemDTO optionItemDTO = new QuoteOptionItemDTO();
        when(quoteEngineHelper.getQuoteOptionItem(anyString(), anyString(), anyString())).thenReturn(optionItemDTO);

        //When
        CancelRelationshipUpdater updater = new CancelRelationshipUpdater(cifAssetOrchestrator, quoteEngineHelper, dependentUpdateBuilderFactory, pmrHelper);
        CancelRelationshipRequest request = new CancelRelationshipRequest(new AssetKey("anotherRootAsset", 1), "lineItemId", 1, new AssetKey("cancellingRootAssetId", 1), "someRelatedToRelation", parentAsset.getProductCode(), false);
        final CancelRelationshipResponse response = updater.performUpdate(request);

        //Then
        assertThat(response.getRequest().getAssetKey(), is(new AssetKey("anotherRootAsset", 1)));
        assertThat(response.getRequest().getLineItemId(), is("lineItemId"));
        assertThat(response.getRequest().getCancellingAssetId(), is(new AssetKey("cancellingRootAssetId", 1)));
        assertThat(response.getRequest().getRelationshipName(), is("someRelatedToRelation"));

        assertThat(response.getDependantUpdates().size(), is(1));
        final List<CIFAssetUpdateRequest> dependantUpdates = response.getDependantUpdates();
        assertThat((CancelRelationshipRequest) dependantUpdates.get(0), is(cancelRelationshipRequest));

        verify(pmrHelper, times(1)).getRuleSourcedValues(parentAsset, productOffering.getAttribute(new AttributeName("SPLIT ORDER QUANTITY")));
        verify(pmrHelper, times(1)).getRuleSourcedValues(parentAsset, productOffering.getAttribute(new AttributeName("NEW GROUP ID")));

        verify(quoteEngineHelper, times(1)).removeQuoteOptionItem(parentAsset.getProjectId(), parentAsset.getQuoteOptionId(), parentAsset.getLineItemId());
        verify(quoteEngineHelper, times(1)).getQuoteOptionItem(parentAsset.getProjectId(), parentAsset.getQuoteOptionId(), parentAsset.getLineItemId());

        Map<String, QuoteOptionItemDTO> removedQuoteOptionItems = AssetCacheManager.getRemovedQuoteOptionItems();
        assertThat(removedQuoteOptionItems.size(), is(1));
        assertThat(removedQuoteOptionItems.values(), hasItem(optionItemDTO));
    }


    @Test
    public void shouldNotAttemptToRemoveQuoteOptionItemDuringStubAssetCancellation() {

        //Given
        CIFAsset relatedAsset = aCIFAsset().withID("cancellingRootAssetId").withVersion(1L).withAssetVersionStatus(AssetVersionStatus.DRAFT).withAssetType(AssetType.STUB)
                .withRelationships(0).build();
        CIFAsset ownerAsset = aCIFAsset().withID("anotherRootAsset").withVersion(1L).withRelationship(relatedAsset, "relatedToRelation", RelationshipType.RelatedTo)
                .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                .build();

        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("cancellingRootAssetId", 1), newArrayList(CIFAssetExtension.Relationships)))).thenReturn(relatedAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("cancellingRootAssetId", 1), newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.QuoteOptionItemDetail)))).thenReturn(relatedAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("anotherRootAsset", 1), newArrayList(CIFAssetExtension.Relationships)))).thenReturn(ownerAsset);
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("anotherRootAsset", 1), newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.QuoteOptionItemDetail)))).thenReturn(ownerAsset);

        Set<CancelRelationshipRequest> cifAssetUpdateRequests = newHashSet();
        CancelRelationshipRequest cancelRelationshipRequest = new CancelRelationshipRequest(ownerAsset.getAssetKey(), ownerAsset.getLineItemId(), 0, relatedAsset.getAssetKey(), "relatedTo", relatedAsset.getProductCode(), false);
        cifAssetUpdateRequests.add(cancelRelationshipRequest);
        when(cifAssetOrchestrator.cancelAssetTree(ownerAsset.getAssetKey(), "relatedToRelation", relatedAsset)).thenReturn(cifAssetUpdateRequests);

        when(contributesToChangeRequestBuilder.buildRequestsOnCancellation(ownerAsset.getAssetKey(), ownerAsset.getProductCode(), 1)).thenReturn(Collections.<CIFAssetUpdateRequest>emptySet());
        when(cifAssetOrchestrator.isRootAsset(ownerAsset.getAssetKey())).thenReturn(true);

        //When
        CancelRelationshipUpdater updater = new CancelRelationshipUpdater(cifAssetOrchestrator, quoteEngineHelper, dependentUpdateBuilderFactory, pmrHelper);
        CancelRelationshipRequest request = new CancelRelationshipRequest(new AssetKey("anotherRootAsset", 1), "lineItemId", 1, new AssetKey("cancellingRootAssetId", 1), "relatedToRelation", ownerAsset.getProductCode(), false);
        final CancelRelationshipResponse response = updater.performUpdate(request);

        //Then
        assertThat(response.getRequest().getAssetKey(), is(new AssetKey("anotherRootAsset", 1)));
        assertThat(response.getRequest().getLineItemId(), is("lineItemId"));
        assertThat(response.getRequest().getCancellingAssetId(), is(new AssetKey("cancellingRootAssetId", 1)));
        assertThat(response.getRequest().getRelationshipName(), is("relatedToRelation"));

        assertThat(response.getDependantUpdates().size(), is(1));
        final List<CIFAssetUpdateRequest> dependantUpdates = response.getDependantUpdates();
        assertThat((CancelRelationshipRequest) dependantUpdates.get(0), is(cancelRelationshipRequest));

        verify(quoteEngineHelper, times(0)).removeQuoteOptionItem(ownerAsset.getProjectId(), ownerAsset.getQuoteOptionId(), ownerAsset.getLineItemId());
        verify(quoteEngineHelper, times(0)).getQuoteOptionItem(ownerAsset.getProjectId(), ownerAsset.getQuoteOptionId(), ownerAsset.getLineItemId());
    }

    @After
    public void after() {
        AssetCacheManager.clearCreatedAndRemovedLineItems();
    }
}