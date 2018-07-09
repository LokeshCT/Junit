package com.bt.rsqe.customerinventory.service.cache;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetChoosableCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetLineItemKey;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.service.providers.AssetCandidateProvider;
import com.bt.rsqe.customerinventory.service.providers.AssociatedAssetKeyProvider;
import com.bt.rsqe.customerinventory.service.providers.ChoosableCandidateProvider;
import com.bt.rsqe.customerinventory.service.updates.ContributesToChangeRequestBuilder;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.DetailedAssetKey;
import com.bt.rsqe.domain.product.Association;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.customerinventory.service.providers.ChoosableCandidateProvider.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class AssetCacheManagerTest {

    private CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private AssociatedAssetKeyProvider associatedAssetKeyProvider = mock(AssociatedAssetKeyProvider.class);
    private AssetCandidateProvider assetCandidateProvider = mock(AssetCandidateProvider.class);
    private ChoosableCandidateProvider choosableCandidateProvider = mock(ChoosableCandidateProvider.class);
    private CIFAsset cifAsset = mock(CIFAsset.class);

    @Test
    public void shouldGetAssetUsingAssetKeyWhenCacheAwareSetToTrue() {
        CacheAwareTransaction.set(true);
        CIFAssetKey cifAssetKey = new CIFAssetKey(new AssetKey("assetId", 1l), CIFAssetExtension.noExtensions());
        when(cifAssetOrchestrator.getCifAsset(cifAssetKey)).thenReturn(cifAsset);

        CIFAsset asset = AssetCacheManager.getAsset(cifAssetKey, cifAssetOrchestrator);
        assertThat(asset, is(cifAsset));

         asset = AssetCacheManager.getAsset(cifAssetKey, cifAssetOrchestrator);
        assertThat(asset, is(cifAsset));

        verify(cifAssetOrchestrator, times(1)).getCifAsset(cifAssetKey);
    }

    @Test
    public void shouldNotGetAssetUsingAssetKeyWhenCacheAwareSetToFalse() {
        CacheAwareTransaction.set(false);
        CIFAssetKey cifAssetKey = new CIFAssetKey(new AssetKey("assetId", 1l), CIFAssetExtension.noExtensions());

        CIFAsset asset = AssetCacheManager.getAsset(cifAssetKey, cifAssetOrchestrator);
        assertThat(asset, nullValue());
        verify(cifAssetOrchestrator, times(0)).getAsset(cifAssetKey);
    }

    @Test
    public void shouldGetAssetUsingAssetLineItemKeyWhenCacheAwareSetToTrue() {
        CacheAwareTransaction.set(true);
        CIFAssetLineItemKey cifAssetLineItemKey = new CIFAssetLineItemKey("aLineItemId", CIFAssetExtension.noExtensions(), "someUserToken", "someLoginName");
        when(cifAssetOrchestrator.getCifRootAsset(cifAssetLineItemKey)).thenReturn(cifAsset);

        CIFAsset asset = AssetCacheManager.getAsset(cifAssetLineItemKey, cifAssetOrchestrator);
        assertThat(asset, is(cifAsset));

        asset = AssetCacheManager.getAsset(cifAssetLineItemKey, cifAssetOrchestrator);
        assertThat(asset, is(cifAsset));

        verify(cifAssetOrchestrator, times(1)).getCifRootAsset(cifAssetLineItemKey);
    }

    @Test
    public void shouldNotGetAssetUsingAssetLineItemKeyWhenCacheAwareSetToFalse() {
        CacheAwareTransaction.set(false);
        CIFAssetLineItemKey cifAssetLineItemKey = new CIFAssetLineItemKey("aLineItemId", CIFAssetExtension.noExtensions(), "someUserToken", "someLoginName");

        CIFAsset asset = AssetCacheManager.getAsset(cifAssetLineItemKey, cifAssetOrchestrator);
        assertThat(asset, nullValue());
    }

    @Test
    public void shouldGetParentAssetWhenCacheAwareSetToTrue() {
        CacheAwareTransaction.set(true);
        CIFAssetKey cifAssetKey = new CIFAssetKey(new AssetKey("assetId", 1l), CIFAssetExtension.noExtensions());
        when(cifAssetOrchestrator.getCifParentAsset(cifAssetKey)).thenReturn(cifAsset);

        CIFAsset asset = AssetCacheManager.getParentAsset(cifAssetKey, cifAssetOrchestrator);
        assertThat(asset, is(cifAsset));

        asset = AssetCacheManager.getParentAsset(cifAssetKey, cifAssetOrchestrator);
        assertThat(asset, is(cifAsset));

        verify(cifAssetOrchestrator, times(1)).getCifParentAsset(cifAssetKey);
    }

    @Test
    public void shouldNotGetParentAssetWhenCacheAwareSetToFalse() {
        CacheAwareTransaction.set(false);
        CIFAssetKey cifAssetKey = new CIFAssetKey(new AssetKey("assetId", 1l), CIFAssetExtension.noExtensions());

        CIFAsset asset = AssetCacheManager.getParentAsset(cifAssetKey, cifAssetOrchestrator);
        assertThat(asset, nullValue());
    }

    @Test
    public void shouldGetOwnerAssetWhenCacheAwareSetToTrue() {
        CacheAwareTransaction.set(true);
        CIFAssetKey cifAssetKey = new CIFAssetKey(new AssetKey("assetId", 1l), CIFAssetExtension.noExtensions());
        CIFAssetOrchestrator.OwnerAssetKey ownerAssetKey = new CIFAssetOrchestrator.OwnerAssetKey(cifAssetKey, Collections.<AssetVersionStatus>emptyList(), Optional.<String>absent(), Optional.<String>absent());
        when(cifAssetOrchestrator.getCifOwnerAssets(ownerAssetKey)).thenReturn(newArrayList(cifAsset));

        List<CIFAsset> assets = AssetCacheManager.getOwnerAssets(ownerAssetKey, cifAssetOrchestrator);
        assertThat(assets, hasItem(cifAsset));

        assets = AssetCacheManager.getOwnerAssets(ownerAssetKey, cifAssetOrchestrator);
        assertThat(assets, hasItem(cifAsset));

        verify(cifAssetOrchestrator, times(1)).getCifOwnerAssets(ownerAssetKey);
    }

    @Test
    public void shouldNotGetOwnerAssetWhenCacheAwareSetToFalse() {
        CacheAwareTransaction.set(false);
        CIFAssetKey cifAssetKey = new CIFAssetKey(new AssetKey("assetId", 1l), CIFAssetExtension.noExtensions());
        CIFAssetOrchestrator.OwnerAssetKey ownerAssetKey = new CIFAssetOrchestrator.OwnerAssetKey(cifAssetKey, Collections.<AssetVersionStatus>emptyList(), Optional.<String>absent(), Optional.<String>absent());

        List<CIFAsset> assets = AssetCacheManager.getOwnerAssets(ownerAssetKey, cifAssetOrchestrator);
        assertThat(assets, nullValue());
    }

    @Test
    public void shouldGetAssociatedAssetKeyWhenCacheAwareSetToTrue() {
        CacheAwareTransaction.set(true);
        AssetKey assetKey = new AssetKey("assetId", 1l);
        Association association = mock(Association.class);

        ContributesToChangeRequestBuilder.AssociatedAssetKey associatedAssetKey = new ContributesToChangeRequestBuilder.AssociatedAssetKey(assetKey, association);

        DetailedAssetKey associatedAssetId = new DetailedAssetKey("associatedAssetId", 1L, "");
        when(associatedAssetKeyProvider.getAssetKeys(associatedAssetKey)).thenReturn(newHashSet(associatedAssetId));

        Set<DetailedAssetKey> assetKeys = AssetCacheManager.getAssociatedAssetKeys(associatedAssetKey, associatedAssetKeyProvider);
        assertThat(assetKeys, hasItem(associatedAssetId));

        assetKeys = AssetCacheManager.getAssociatedAssetKeys(associatedAssetKey, associatedAssetKeyProvider);
        assertThat(assetKeys, hasItem(associatedAssetId));

        verify(associatedAssetKeyProvider, times(1)).getAssetKeys(associatedAssetKey);
    }



    @Test
    public void shouldNotGetAssociatedAssetKeyWhenCacheAwareSetToFalse() {
        CacheAwareTransaction.set(false);
        AssetKey assetKey = new AssetKey("assetId", 1l);
        Association association = mock(Association.class);

        ContributesToChangeRequestBuilder.AssociatedAssetKey associatedAssetKey = new ContributesToChangeRequestBuilder.AssociatedAssetKey(assetKey, association);


        Set<DetailedAssetKey> assetKeys = AssetCacheManager.getAssociatedAssetKeys(associatedAssetKey, associatedAssetKeyProvider);
        assertThat(assetKeys, nullValue());
    }

    @Test
    public void shouldGetMigrationCustomerFlagWhenCacheAwareSetToTrue() {
        CacheAwareTransaction.set(true);

        CIFAssetOrchestrator.MigratedCustomerKey migratedCustomerKey = new CIFAssetOrchestrator.MigratedCustomerKey("aCustomerId", "aContractId",
                newArrayList("aProductCode"));
        when(cifAssetOrchestrator.getMigratedCustomer(migratedCustomerKey)).thenReturn(true);

        Boolean migratedCustomer = AssetCacheManager.isMigratedCustomer(migratedCustomerKey, cifAssetOrchestrator);
        assertThat(migratedCustomer, is(true));

        migratedCustomer = AssetCacheManager.isMigratedCustomer(migratedCustomerKey, cifAssetOrchestrator);
        assertThat(migratedCustomer, is(true));

        verify(cifAssetOrchestrator, times(1)).getMigratedCustomer(migratedCustomerKey);

    }

    @Test
    public void shouldNotGetMigrationCustomerFlagWhenCacheAwareSetToFalse() {
        CIFAssetOrchestrator.MigratedCustomerKey migratedCustomerKey = new CIFAssetOrchestrator.MigratedCustomerKey("aCustomerId", "aContractId",
                newArrayList("aProductCode"));

        Boolean migratedCustomer = AssetCacheManager.isMigratedCustomer(migratedCustomerKey, cifAssetOrchestrator);
        assertThat(migratedCustomer, nullValue());

        verify(cifAssetOrchestrator, times(0)).getMigratedCustomer(migratedCustomerKey);
    }

    @Test
    public void shouldGetChoosableCandidatesWhenCacheAwareSetToTrue() {
        CacheAwareTransaction.set(true);
        AssetKey assetKey = new AssetKey("assetId", 1l);
        AssetCandidateProvider.ChoosableAssetKey choosableAssetKey = new AssetCandidateProvider.ChoosableAssetKey(assetKey, RelationshipName.newInstance("aRelationship"));
        AssetDTO assetDTO = new AssetDTO();
        when(assetCandidateProvider.choosableCandidates(choosableAssetKey)).thenReturn(newArrayList(assetDTO));

        List<AssetDTO> assetDTOs = AssetCacheManager.getChoosableCandidates(choosableAssetKey, assetCandidateProvider);
        assertThat(assetDTOs, hasItem(assetDTO));

        assetDTOs = AssetCacheManager.getChoosableCandidates(choosableAssetKey, assetCandidateProvider);
        assertThat(assetDTOs, hasItem(assetDTO));

        verify(assetCandidateProvider, times(1)).choosableCandidates(choosableAssetKey);
    }

    @Test
    public void shouldNotGetChoosableCandidatesWhenCacheAwareSetToFalse() {
        AssetKey assetKey = new AssetKey("assetId", 1l);
        AssetCandidateProvider.ChoosableAssetKey choosableAssetKey = new AssetCandidateProvider.ChoosableAssetKey(assetKey, RelationshipName.newInstance("aRelationship"));

        List<AssetDTO> assetDTOs = AssetCacheManager.getChoosableCandidates(choosableAssetKey, assetCandidateProvider);
        assertThat(assetDTOs, nullValue());
    }

    @Test
    public void shouldGetExistingCandidatesWhenCacheAwareSetToTrue() {
        CacheAwareTransaction.set(true);
        CIFAsset owner = CIFAssetFixture.aCIFAsset().withID("parent").build();
        final ExistingCandidatesKey existingCandidatesKey = new ExistingCandidatesKey("aProductCode", owner, 22, "aRelationship");

        final CIFAssetChoosableCandidate cifAssetChoosableCandidate = new CIFAssetChoosableCandidate();
        when(choosableCandidateProvider.existingCandidates(existingCandidatesKey)).thenReturn(newArrayList(cifAssetChoosableCandidate));

        List<CIFAssetChoosableCandidate> existingCandidates = AssetCacheManager.findExistingCandidates(existingCandidatesKey, choosableCandidateProvider);
        assertThat(existingCandidates, hasItem(cifAssetChoosableCandidate));

        existingCandidates = AssetCacheManager.findExistingCandidates(existingCandidatesKey, choosableCandidateProvider);
        assertThat(existingCandidates, hasItem(cifAssetChoosableCandidate));

        verify(choosableCandidateProvider, times(1)).existingCandidates(existingCandidatesKey);
    }

    @Test
    public void shouldNotGetExistingCandidatesWhenCacheAwareSetToFalse() {
        CIFAsset owner = CIFAssetFixture.aCIFAsset().withID("parent").build();
        final ExistingCandidatesKey existingCandidatesKey = new ExistingCandidatesKey("aProductCode", owner, 22, "aRelationship");

        List<CIFAssetChoosableCandidate> existingCandidates = AssetCacheManager.findExistingCandidates(existingCandidatesKey, choosableCandidateProvider);
        assertThat(existingCandidates, nullValue());
    }

    @Test
    public void shouldGetInServiceAssetWhenCacheAwareSetToTrue() {
        CacheAwareTransaction.set(true);
        CIFAsset inServiceAsset = CIFAssetFixture.aCIFAsset().build();

        final CIFAssetKey cifAssetKey = new CIFAssetKey(new AssetKey("assetId", 1l), CIFAssetExtension.noExtensions());
        when(cifAssetOrchestrator.inServiceAsset(cifAssetKey)).thenReturn(Optional.of(inServiceAsset));


        Optional<CIFAsset> assetOptional =  AssetCacheManager.getInServiceAsset(cifAssetKey, cifAssetOrchestrator);
        assertThat(assetOptional.get(), is(inServiceAsset));

        assetOptional = AssetCacheManager.getInServiceAsset(cifAssetKey, cifAssetOrchestrator);
        assertThat(assetOptional.get(), is(inServiceAsset));

        verify(cifAssetOrchestrator, times(1)).inServiceAsset(cifAssetKey);
    }

    @Test
    public void shouldNotGetInServiceAssetWhenCacheAwareSetToFalse() {
        CacheAwareTransaction.set(false);

        final CIFAssetKey cifAssetKey = new CIFAssetKey(new AssetKey("assetId", 1l), CIFAssetExtension.noExtensions());
        Optional<CIFAsset> assetOptional =  AssetCacheManager.getInServiceAsset(cifAssetKey, cifAssetOrchestrator);
        assertThat(assetOptional.isPresent(), is(false));
    }

    @Test
    public void shouldRecordAndReadCreateLineItemWhenCacheIsEnabled() {
        CacheAwareTransaction.set(true);
        QuoteOptionItemDTO optionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("aLineItemId").build();
        QuoteOptionItemDTO anotherItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("anotherLineItemId").build();
        AssetCacheManager.recordCreatedQuoteOptionItem(optionItemDTO);
        AssetCacheManager.recordCreatedQuoteOptionItem(anotherItemDTO);

        Set<QuoteOptionItemDTO> createdQuoteOptionItems = AssetCacheManager.getCreatedQuoteOptionItems();
        assertThat(createdQuoteOptionItems.size(), is(2));
        assertThat(createdQuoteOptionItems, hasItems(optionItemDTO, anotherItemDTO));
    }

    @Test
    public void shouldNotRecordAndReadCreateLineItemWhenCacheIsDisabled() {
        CacheAwareTransaction.set(false);
        QuoteOptionItemDTO optionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("aLineItemId").build();
        QuoteOptionItemDTO anotherItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("anotherLineItemId").build();
        AssetCacheManager.recordCreatedQuoteOptionItem(optionItemDTO);
        AssetCacheManager.recordCreatedQuoteOptionItem(anotherItemDTO);

        Set<QuoteOptionItemDTO> createdQuoteOptionItems = AssetCacheManager.getCreatedQuoteOptionItems();
        assertThat(createdQuoteOptionItems.size(), is(0));
    }

    @Test
    public void shouldRecordAndReadRemovedLineItemWhenCacheIsEnabled() {
        CacheAwareTransaction.set(true);
        QuoteOptionItemDTO anItemDTO = new QuoteOptionItemDTO();
        QuoteOptionItemDTO anotherItemDTO = new QuoteOptionItemDTO();
        AssetCacheManager.recordRemovedQuoteOptionItem("aLineItemId", anItemDTO);
        AssetCacheManager.recordRemovedQuoteOptionItem("anotherLineItemId", anotherItemDTO);

        Map<String, QuoteOptionItemDTO> items = AssetCacheManager.getRemovedQuoteOptionItems();
        assertThat(items.size(), is(2));
        assertThat(items.keySet(), hasItems("aLineItemId", "anotherLineItemId"));
        assertThat(items.get("aLineItemId"), is(anItemDTO));
        assertThat(items.get("anotherLineItemId"), is(anotherItemDTO));
    }

    @Test
    public void shouldNotRecordAndReadRemovedLineItemWhenCacheIsDisabled() {
        CacheAwareTransaction.set(false);
        QuoteOptionItemDTO anItemDTO = new QuoteOptionItemDTO();
        QuoteOptionItemDTO anotherItemDTO = new QuoteOptionItemDTO();
        AssetCacheManager.recordRemovedQuoteOptionItem("aLineItemId", anItemDTO);
        AssetCacheManager.recordRemovedQuoteOptionItem("anotherLineItemId", anotherItemDTO);

        Map<String, QuoteOptionItemDTO> items = AssetCacheManager.getRemovedQuoteOptionItems();
        assertThat(items.size(), is(0));
    }

    @After
    public void removeCache() {
        CacheAwareTransaction.remove();
        AssetCacheManager.clearAllCaches();
    }
}