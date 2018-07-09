package com.bt.rsqe.customerinventory.service.orchestrators;

import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.repository.StaleAssetException;
import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetLineItemKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.cache.CacheAwareTransaction;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.extenders.CIFAssetExtender;
import com.bt.rsqe.customerinventory.service.providers.CIFAssetCreator;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.*;
import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.enums.AssetVersionStatus.CANCELLED;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class CIFAssetOrchestratorTest{
    private final CIFAssetJPARepository cifAssetRepository = mock(CIFAssetJPARepository.class);
    private final CIFAssetExtender cifAssetExtender = mock(CIFAssetExtender.class);
    private final CIFAssetOrchestrator cifAssetOrchestrator = new CIFAssetOrchestrator(cifAssetRepository);
    private final CIFAssetCreator cifAssetCreator = mock(CIFAssetCreator.class);

    @Before
    public void setUp() throws Exception {
        cifAssetOrchestrator.setCifAssetExtender(cifAssetExtender);
        cifAssetOrchestrator.setCifAssetProvider(cifAssetCreator);
    }

    @Test
    public void shouldFetchAssetFromRepository(){
        CIFAsset expectedAsset = aCIFAsset().build();
        AssetKey assetKey = new AssetKey("ASSET_ID", 1l);
        when(cifAssetRepository.getAsset(assetKey, true)).thenReturn(expectedAsset);

        CIFAsset cifAsset = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(Relationships)));

        assertThat(cifAsset, is(expectedAsset));
        verify(cifAssetRepository).getAsset(assetKey, true);
    }

    @Test
    public void shouldFetchAssetFromRepositoryFromCache(){
        CacheAwareTransaction.set(true);
        CIFAsset expectedAssetA = aCIFAsset().withID("ID_A").build();
        AssetKey assetKeyA = expectedAssetA.getAssetKey();
        when(cifAssetRepository.getAsset(assetKeyA, true)).thenReturn(expectedAssetA);

        CIFAsset expectedAssetB = aCIFAsset().withID("ID_B").build();
        AssetKey assetKeyB = expectedAssetB.getAssetKey();
        when(cifAssetRepository.getAsset(assetKeyB, true)).thenReturn(expectedAssetB);

        CIFAsset cifAssetA1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
        assertThat(cifAssetA1, is(expectedAssetA));

        CIFAsset cifAssetB1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
        assertThat(cifAssetB1, is(expectedAssetB));

        CIFAsset cifAssetA2 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
        assertThat(cifAssetA2, is(expectedAssetA));

        CIFAsset cifAssetB2 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
        assertThat(cifAssetB2, is(expectedAssetB));

        verify(cifAssetRepository, times(1)).getAsset(assetKeyA, true);
        verify(cifAssetRepository, times(1)).getAsset(assetKeyB, true);
    }

    @Test
    @Ignore("need to try and simulate different caches in different threads")
    public void shouldFetchAssetFromRepositoryFromCacheDifferentThreads(){
        final CIFAsset expectedAssetA = aCIFAsset().withID("ID_A").build();
        final AssetKey assetKeyA = expectedAssetA.getAssetKey();
        when(cifAssetRepository.getAsset(assetKeyA, true)).thenReturn(expectedAssetA);

        final CIFAsset expectedAssetB = aCIFAsset().withID("ID_B").build();
        final AssetKey assetKeyB = expectedAssetB.getAssetKey();
        when(cifAssetRepository.getAsset(assetKeyB, true)).thenReturn(expectedAssetB);


        ThreadGroup threadGroup = new ThreadGroup ("group") ;
        Thread thread1 = new Thread (threadGroup,"thread1")
        {
            @Override
            public void run() {
                CIFAsset cifAssetA1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
                assertThat(cifAssetA1, is(expectedAssetA));

                CIFAsset cifAssetB1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
                assertThat(cifAssetB1, is(expectedAssetB));

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {

                }
            }
        } ;

        Thread thread2 = new Thread (threadGroup,"thread2")
        {
            @Override
            public void run() {
                CIFAsset cifAssetA2 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
                assertThat(cifAssetA2, is(expectedAssetA));

                CIFAsset cifAssetB2 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
                assertThat(cifAssetB2, is(expectedAssetB));

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {

                }
            }
        } ;

        thread1.start ();
        thread2.start();

        int count=0 ;
        while (threadGroup.activeCount() > 0)
        {
            if (count ++ > 20)
            {
                fail ("Threads didn't complete") ;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }


        verify(cifAssetRepository, times(1)).getAsset(assetKeyA, true);
        verify(cifAssetRepository, times(1)).getAsset(assetKeyB, true);
    }

    @Test
    public void shouldCreateCacheAndFetchAssetFromItWhenSameAssetBeingCalledInAThreadExecution() {
        final CIFAsset expectedAssetA = aCIFAsset().withID("ID_A").build();
            final AssetKey assetKeyA = expectedAssetA.getAssetKey();
            when(cifAssetRepository.getAsset(assetKeyA, true)).thenReturn(expectedAssetA);

            final CIFAsset expectedAssetB = aCIFAsset().withID("ID_B").build();
            final AssetKey assetKeyB = expectedAssetB.getAssetKey();
            when(cifAssetRepository.getAsset(assetKeyB, true)).thenReturn(expectedAssetB);

            Thread thread1 = new Thread("thread1") {
                @Override
                public void run() {
                    CacheAwareTransaction.set(true);
                    CIFAsset cifAssetA1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
                    assertThat(cifAssetA1, is(expectedAssetA));

                    CIFAsset cifAssetB1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
                    assertThat(cifAssetB1, is(expectedAssetB));

                    cifAssetA1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
                    assertThat(cifAssetA1, is(expectedAssetA));

                    cifAssetB1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
                    assertThat(cifAssetB1, is(expectedAssetB));
                }
            };

            thread1.start();

            try {
                Thread.sleep(3000L);   //Time delay for allowing thread execution.
            } catch (InterruptedException e) {
                //Do nothing.
            }

            verify(cifAssetRepository, times(1)).getAsset(assetKeyA, true);
            verify(cifAssetRepository, times(1)).getAsset(assetKeyB, true);
    }


    @Test
    public void shouldCreateOwnCacheForEveryThreadAndFetchAssetFromItsOwnCache() {

        final CIFAsset expectedAssetA = aCIFAsset().withID("ID_A").build();
        final AssetKey assetKeyA = expectedAssetA.getAssetKey();
        when(cifAssetRepository.getAsset(assetKeyA, true)).thenReturn(expectedAssetA);

        final CIFAsset expectedAssetB = aCIFAsset().withID("ID_B").build();
        final AssetKey assetKeyB = expectedAssetB.getAssetKey();
        when(cifAssetRepository.getAsset(assetKeyB, true)).thenReturn(expectedAssetB);

        Thread thread1 = new Thread("thread1") {
            @Override
            public void run() {
                CacheAwareTransaction.set(true);
                CIFAsset cifAssetA1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
                assertThat(cifAssetA1, is(expectedAssetA));

                CIFAsset cifAssetB1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
                assertThat(cifAssetB1, is(expectedAssetB));

                cifAssetA1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
                assertThat(cifAssetA1, is(expectedAssetA));

                cifAssetB1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
                assertThat(cifAssetB1, is(expectedAssetB));
            }
        };

        Thread thread2 = new Thread("thread2") {
            @Override
            public void run() {
                CacheAwareTransaction.set(true);
                CIFAsset cifAssetA1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
                assertThat(cifAssetA1, is(expectedAssetA));

                CIFAsset cifAssetB1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
                assertThat(cifAssetB1, is(expectedAssetB));
            }
        };

        thread1.start();
        thread2.start();

        try {
            Thread.sleep(3000L);   //Time delay for allowing thread execution.
        } catch (InterruptedException e) {
            //Do nothing.
        }

        verify(cifAssetRepository, times(2)).getAsset(assetKeyA, true);
        verify(cifAssetRepository, times(2)).getAsset(assetKeyB, true);
    }


    @Test
    public void shouldHitDBAndGetAssetWhenTransactionIsNotCacheAware() {

        final CIFAsset expectedAssetA = aCIFAsset().withID("ID_A").build();
        final AssetKey assetKeyA = expectedAssetA.getAssetKey();
        when(cifAssetRepository.getAsset(assetKeyA, true)).thenReturn(expectedAssetA);

        final CIFAsset expectedAssetB = aCIFAsset().withID("ID_B").build();
        final AssetKey assetKeyB = expectedAssetB.getAssetKey();
        when(cifAssetRepository.getAsset(assetKeyB, true)).thenReturn(expectedAssetB);

        Thread thread1 = new Thread("thread1") {
            @Override
            public void run() {
                CacheAwareTransaction.set(false);
                CIFAsset cifAssetA1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
                assertThat(cifAssetA1, is(expectedAssetA));

                CIFAsset cifAssetB1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
                assertThat(cifAssetB1, is(expectedAssetB));

                cifAssetA1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
                assertThat(cifAssetA1, is(expectedAssetA));

                cifAssetB1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
                assertThat(cifAssetB1, is(expectedAssetB));
            }
        };

        Thread thread2 = new Thread("thread2") {
            @Override
            public void run() {
                CacheAwareTransaction.set(false);
                CIFAsset cifAssetA1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
                assertThat(cifAssetA1, is(expectedAssetA));

                CIFAsset cifAssetB1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
                assertThat(cifAssetB1, is(expectedAssetB));
            }
        };

        thread1.start();
        thread2.start();

        try {
            Thread.sleep(3000L);   //Time delay for allowing thread execution.
        } catch (InterruptedException e) {
            //Do nothing.
        }

        verify(cifAssetRepository, times(3)).getAsset(assetKeyA, true);
        verify(cifAssetRepository, times(3)).getAsset(assetKeyB, true);
    }

    @Test
    public void shouldFetchAssetFromRepositoryWithClearCache(){
        CIFAsset expectedAssetA = aCIFAsset().withID("ID_A").build();
        AssetKey assetKeyA = expectedAssetA.getAssetKey();
        when(cifAssetRepository.getAsset(assetKeyA, true)).thenReturn(expectedAssetA);

        CIFAsset expectedAssetB = aCIFAsset().withID("ID_B").build();
        AssetKey assetKeyB = expectedAssetB.getAssetKey();
        when(cifAssetRepository.getAsset(assetKeyB, true)).thenReturn(expectedAssetB);

        CIFAsset cifAssetA1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
        assertThat(cifAssetA1, is(expectedAssetA));

        CIFAsset cifAssetB1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
        assertThat(cifAssetB1, is(expectedAssetB));

        AssetCacheManager.clearAssetCaches();

        CIFAsset cifAssetA2 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
        assertThat(cifAssetA2, is(expectedAssetA));

        CIFAsset cifAssetB2 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
        assertThat(cifAssetB2, is(expectedAssetB));

        verify(cifAssetRepository, times(2)).getAsset(assetKeyA, true);
        verify(cifAssetRepository, times(2)).getAsset(assetKeyB, true);
    }

    @Test
    public void shouldFetchAssetFromRepositoryWithSave(){
        CIFAsset expectedAssetA = aCIFAsset().withID("ID_A").build();
        AssetKey assetKeyA = expectedAssetA.getAssetKey();
        when(cifAssetRepository.getAsset(assetKeyA, true)).thenReturn(expectedAssetA);

        CIFAsset expectedAssetB = aCIFAsset().withID("ID_B").build();
        AssetKey assetKeyB = expectedAssetB.getAssetKey();
        when(cifAssetRepository.getAsset(assetKeyB, true)).thenReturn(expectedAssetB);

        CIFAsset cifAssetA1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
        assertThat(cifAssetA1, is(expectedAssetA));

        CIFAsset cifAssetB1 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
        assertThat(cifAssetB1, is(expectedAssetB));

        cifAssetOrchestrator.saveAssetAndClearCaches(cifAssetA1);

        CIFAsset cifAssetA2 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyA, newArrayList(Relationships)));
        assertThat(cifAssetA2, is(expectedAssetA));

        CIFAsset cifAssetB2 = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKeyB, newArrayList(Relationships)));
        assertThat(cifAssetB2, is(expectedAssetB));

        verify(cifAssetRepository, times(2)).getAsset(assetKeyA, true);
        verify(cifAssetRepository, times(2)).getAsset(assetKeyB, true);
    }

    @Test
    public void shouldFetchRootAssetFromRepository(){
        CIFAsset expectedAsset = aCIFAsset().build();
        final CIFAssetLineItemKey CIFAssetLineItemKey = new CIFAssetLineItemKey(expectedAsset.getLineItemId(), new ArrayList<CIFAssetExtension>(), "", "");
        when(cifAssetRepository.getRootAsset(CIFAssetLineItemKey.getLineItemId(), false)).thenReturn(expectedAsset);

        CIFAsset cifAsset = cifAssetOrchestrator.getAsset(CIFAssetLineItemKey);

        assertThat(cifAsset, is(expectedAsset));
        verify(cifAssetRepository).getRootAsset(CIFAssetLineItemKey.getLineItemId(), false);
    }

    @Test
    public void shouldFetchAssetFromRepositoryIncludingRelatedAssets() {
        CIFAsset childAsset = aCentralSiteCIFAsset().withCharacteristic("childRelationshipCharacteristic", "childVal").build();
        CIFAsset relatedToAsset = aCentralSiteCIFAsset().withCharacteristic("relatedToRelationshipCharacteristic", "relatedToVal").build();
        CIFAsset expectedAsset = aCentralSiteCIFAsset().withRelationship(childAsset, "childRelationshipName", RelationshipType.Child)
                                            .withRelationship(relatedToAsset, "relatedToRelationshipName", RelationshipType.RelatedTo).build();
        when(cifAssetRepository.getAsset(expectedAsset.getAssetKey(), true)).thenReturn(expectedAsset);

        CIFAsset cifAsset = cifAssetOrchestrator.getAsset(new CIFAssetKey(expectedAsset.getAssetKey(), newArrayList(CharacteristicAllowedValues, Relationships)));

        assertThat(cifAsset.getRelationships().get(0).getRelated(), is(childAsset));
        assertThat(cifAsset.getRelationships().get(1).getRelated(), is(relatedToAsset));
    }

    @Test
    public void shouldSaveAssetToRepository(){
        CIFAsset savedAsset = aCIFAsset().build();

        cifAssetOrchestrator.saveAssetAndClearCaches(savedAsset);

        verify(cifAssetRepository).saveAsset(savedAsset);
    }

    @Test
    public void shouldGetParentAsset() {
        CIFAsset expectedParentCifAsset = aCIFAsset().withRelationships(1).build();
        final AssetKey relatedAssetKey = expectedParentCifAsset.getRelationships().get(0).getRelated().getAssetKey();
        when(cifAssetRepository.getParentAsset(relatedAssetKey, false)).thenReturn(expectedParentCifAsset);

        CIFAsset cifAsset = cifAssetOrchestrator.getParentAsset(new CIFAssetKey(relatedAssetKey));

        assertThat(cifAsset, is(expectedParentCifAsset));
    }

    @Test
    public void shouldGetInServiceAssetFromRepository() {
        final CIFAsset baseAsset = aCIFAsset().build();
        final CIFAsset expectedInServiceAsset = aCIFAsset().build();
        when(cifAssetRepository.getInServiceAsset(baseAsset.getAssetKey(), false)).thenReturn(Optional.of(expectedInServiceAsset));

        final Optional<CIFAsset> inServiceAsset = cifAssetOrchestrator.getInServiceAsset(new CIFAssetKey(baseAsset.getAssetKey()));

        assertThat(inServiceAsset.get(), is(expectedInServiceAsset));
    }

    @Test
    public void shouldGetAbsentInServiceAssetFromRepository() {
        final CIFAsset baseAsset = aCIFAsset().build();
        when(cifAssetRepository.getInServiceAsset(baseAsset.getAssetKey(), false)).thenReturn(Optional.<CIFAsset>absent());

        final Optional<CIFAsset> inServiceAsset = cifAssetOrchestrator.getInServiceAsset(new CIFAssetKey(baseAsset.getAssetKey()));

        assertThat(inServiceAsset, is(Optional.<CIFAsset>absent()));
    }

    @Test
    public void shouldExtendInServiceAsset() {
        final CIFAsset baseAsset = aCIFAsset().build();
        final CIFAsset expectedInServiceAsset = aCIFAsset().build();
        when(cifAssetRepository.getInServiceAsset(baseAsset.getAssetKey(), true)).thenReturn(Optional.of(expectedInServiceAsset));

        List<CIFAssetExtension> extensions = newArrayList(Relationships);
        cifAssetOrchestrator.getInServiceAsset(new CIFAssetKey(baseAsset.getAssetKey(), extensions));

        verify(cifAssetExtender, times(1)).extend(expectedInServiceAsset, extensions);

    }

    @Test
    public void shouldGetAllOwnerAssets() {
        CIFAsset parentCifAsset = aCIFAsset().withRelationships(1).build();
        CIFAsset basicCifAsset = parentCifAsset.getRelationships().get(0).getRelated();
        CIFAsset relatedToCifAsset = aCIFAsset().withRelationship(basicCifAsset, "relatedTo", RelationshipType.RelatedTo).build();
        when(cifAssetRepository.getOwnerAssets(basicCifAsset.getAssetKey(), false)).thenReturn(newArrayList(parentCifAsset, relatedToCifAsset));
        List<CIFAssetExtension> expectedExtensions = newArrayList(CIFAssetExtension.ProductRules, CIFAssetExtension.ProductOfferingDetail);

        List<CIFAsset> cifAssets = cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(basicCifAsset.getAssetKey(), expectedExtensions));

        assertThat(cifAssets.size(), is(2));
        assertThat(cifAssets.get(0), is(parentCifAsset));
        assertThat(cifAssets.get(1), is(relatedToCifAsset));
        verify(cifAssetExtender).extend(parentCifAsset, expectedExtensions);
        verify(cifAssetExtender).extend(relatedToCifAsset, expectedExtensions);
    }

    @Test
    public void shouldGetOwnerAssetsByStatus() {
        CIFAsset parentCifAsset = aCIFAsset().withRelationships(1).build();
        CIFAsset basicCifAsset = parentCifAsset.getRelationships().get(0).getRelated();
        CIFAsset relatedToCifAsset = aCIFAsset().withRelationship(basicCifAsset, "relatedTo", RelationshipType.RelatedTo).build();
        when(cifAssetRepository.getOwnerAssets(basicCifAsset.getAssetKey(), false, newArrayList(CANCELLED))).thenReturn(newArrayList(parentCifAsset, relatedToCifAsset));
        List<CIFAssetExtension> expectedExtensions = newArrayList(CIFAssetExtension.ProductRules, CIFAssetExtension.ProductOfferingDetail);

        List<CIFAsset> cifAssets = cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(basicCifAsset.getAssetKey(), expectedExtensions), newArrayList(CANCELLED), Optional.<String>absent(), Optional.<String>absent());

        assertThat(cifAssets.size(), is(2));
        assertThat(cifAssets.get(0), is(parentCifAsset));
        assertThat(cifAssets.get(1), is(relatedToCifAsset));
        verify(cifAssetExtender).extend(parentCifAsset, expectedExtensions);
        verify(cifAssetExtender).extend(relatedToCifAsset, expectedExtensions);
    }

    @Test
    public void shouldExtendAssetIfNecessary() {
        CIFAsset baseCifAsset = aCIFAsset().build();
        when(cifAssetRepository.getRelationships(baseCifAsset)).thenReturn(baseCifAsset);

        cifAssetOrchestrator.extendAsset(baseCifAsset, newArrayList(ProductOfferingDetail));

        verify(cifAssetExtender, times(1)).extend(baseCifAsset, newArrayList(ProductOfferingDetail));
        verifyNoMoreInteractions(cifAssetExtender);
    }

    @Test
    public void shouldForceExtendAsset() {
        CIFAsset baseCifAsset = aCIFAsset().with(new CIFAssetOfferingDetail("Name", "DisplayName", "GroupName", "LegacyId", true, false, "proposition", true, true, null)).build();
        when(cifAssetRepository.getRelationships(baseCifAsset)).thenReturn(baseCifAsset);

        cifAssetOrchestrator.forceExtendAsset(baseCifAsset, newArrayList(ProductOfferingDetail));

        verify(cifAssetExtender, times(1)).extend(baseCifAsset, newArrayList(ProductOfferingDetail));
        verifyNoMoreInteractions(cifAssetExtender);
    }

    @Test
    public void shouldNotExtendAssetWhichHasRequestedExtensions() {
        CIFAsset baseCifAsset = aCIFAsset().with(new CIFAssetOfferingDetail("Name", "DisplayName", "GroupName", "LegacyId", true, false, "proposition", true, true, null)).build();
        when(cifAssetRepository.getRelationships(baseCifAsset)).thenReturn(baseCifAsset);

        cifAssetOrchestrator.extendAsset(baseCifAsset, newArrayList(ProductOfferingDetail));

        verifyNoMoreInteractions(cifAssetExtender);
    }

    @Test
    public void shouldExtendAssetWithRelationships() {
        CIFAsset relatedCifAsset = aCIFAsset().build();
        CIFAsset baseCifAsset = aCIFAsset().withID("ID1").build();
        CIFAsset baseCifAssetWithRelationships = aCIFAsset().withID("ID1")
                                                            .withRelationship(relatedCifAsset, "name", RelationshipType.Child).build();
        when(cifAssetRepository.getRelationships(baseCifAsset)).thenReturn(baseCifAssetWithRelationships);

        cifAssetOrchestrator.extendAsset(baseCifAsset, newArrayList(Relationships, ProductOfferingDetail));

        verify(cifAssetRepository, times(1)).getRelationships(baseCifAsset);
        verify(cifAssetExtender, times(1)).extend(baseCifAssetWithRelationships, newArrayList(Relationships, ProductOfferingDetail));
    }

    @Test
    public void shouldGetRootAssetAndExtend() throws Exception {
        final String lineItemId = "aLineItemId";
        CIFAsset rootAsset = aCIFAsset().withLineItemId(lineItemId).build();
        when(cifAssetRepository.getRootAsset(lineItemId, true)).thenReturn(rootAsset);

        CIFAsset actualAsset = cifAssetOrchestrator.getAsset(new CIFAssetLineItemKey(lineItemId, newArrayList(Relationships, ProductOfferingDetail), "", ""));

        verify(cifAssetExtender).extend(rootAsset, "", "", newArrayList(Relationships, ProductOfferingDetail));
        assertThat(actualAsset.getLineItemId(), is(lineItemId));
    }

    @Test
    public void shouldGetAsetsWithCorrectSCodeAndCharacteristicValue(){
        CIFAsset rootAsset1 = aCIFAsset().withCustomerId("foundCustId").withContractId("foundContId").withProductIdentifier("foundCode1", "version1")
                                         .withCharacteristic("foundChar", "foundVal").build();
        when(cifAssetRepository.getAssets("foundCustId", "foundContId", newArrayList("foundCode1", "foundCode2"),
                                          "foundChar", "foundVal", false)).thenReturn(newArrayList(rootAsset1));
        when(cifAssetRepository.getRelationships(rootAsset1)).thenReturn(rootAsset1);

        final List<CIFAsset> assets = cifAssetOrchestrator.getAssets("foundCustId", "foundContId", newArrayList("foundCode1", "foundCode2"),
                "foundChar", "foundVal", new ArrayList<CIFAssetExtension>());

        assertThat(assets.size(), is(1));
        assertThat(assets, hasItem(rootAsset1));
    }

    @Test
    public void shouldGetAsetsWithCorrectSCode(){
        CIFAsset rootAsset1 = aCIFAsset().withCustomerId("foundCustId").withContractId("foundContId")
                                         .withProductIdentifier("foundCode1", "version1").build();
        when(cifAssetRepository.getAssets("foundCustId", "foundContId", "foundCode1", false)).thenReturn(newArrayList(rootAsset1));
        when(cifAssetRepository.getRelationships(rootAsset1)).thenReturn(rootAsset1);

        final List<CIFAsset> assets = cifAssetOrchestrator.getAssets("foundCustId", "foundContId", "foundCode1",
                                                                     new ArrayList<CIFAssetExtension>());

        assertThat(assets.size(), is(1));
        assertThat(assets, hasItem(rootAsset1));
    }

    @Test
    public void shouldCallCIFAssetProviderToCreateAndRelateANewAsset() {
        CIFAsset ownerAsset = aCIFAsset().build();
        CIFAsset relatedAsset = aCIFAsset().build();

        when(cifAssetRepository.getRelationships(ownerAsset)).thenReturn(ownerAsset);
        when(cifAssetCreator.createAsset("productCode", "stencilCode", "lineItemId", "siteId", "contractTerm", "customerId",
                                         "contractId", "projectId", "quoteOptionId", "alternateCity", ownerAsset.getProductCategoryCode(), null, null, null, null)).thenReturn(relatedAsset);
        final CIFAssetRelationship cifAssetRelationship = new CIFAssetRelationship(relatedAsset,
                                                                               "relationshipName",
                                                                               RelationshipType.Child,
                                                                               ProductInstanceState.LIVE);
        when(cifAssetCreator.relateAssets(ownerAsset, relatedAsset, "relationshipName")).thenReturn(cifAssetRelationship);

        final CIFAssetRelationship returnedRelationship = cifAssetOrchestrator.createAndRelateAsset(ownerAsset, "relationshipName", "productCode", "stencilCode", "lineItemId", "siteId",
                                                                                              "contractTerm", "customerId", "contractId", "projectId", "quoteOptionId",
                                                                                                    "alternateCity", ownerAsset.getProductCategoryCode());

        assertThat(returnedRelationship, is(cifAssetRelationship));
    }

    @Test
    public void shouldCallCIFAssetProviderToRelateAssets() {
        CIFAsset ownerAsset = aCIFAsset().build();
        CIFAsset relatedAsset = aCIFAsset().build();

        when(cifAssetRepository.getRelationships(ownerAsset)).thenReturn(ownerAsset);
        when(cifAssetCreator.createAsset("productCode", "stencilCode", "lineItemId", "siteId", "contractTerm", "customerId",
                                         "contractId", "projectId", "quoteOptionId", "alternateCity", ownerAsset.getProductCategoryCode(), null, null, null, null)).thenReturn(relatedAsset);
        final CIFAssetRelationship cifAssetRelationship = new CIFAssetRelationship(relatedAsset,
                                                                                   "relationshipName",
                                                                                   RelationshipType.Child,
                                                                                   ProductInstanceState.LIVE);
        when(cifAssetCreator.relateAssets(ownerAsset, relatedAsset, "relationshipName")).thenReturn(cifAssetRelationship);

        final CIFAssetRelationship returnedRelationship = cifAssetOrchestrator.relateAssets(ownerAsset, relatedAsset, "relationshipName");

        assertThat(returnedRelationship, is(cifAssetRelationship));
    }

    @Test
    public void shouldCallRepositoryToCreateNewLineItemLockVersion() throws StaleAssetException {
        cifAssetOrchestrator.createLineItemLockVersion("lineItemId");
        verify(cifAssetRepository, times(1)).saveLineItemLockVersion("lineItemId", 0);
    }

    @Test
    public void shouldReturnTrueWhenAnAssetIsARootAsset() {
        when(cifAssetRepository.isRootAsset(new AssetKey("parentAssetId", 1L))).thenReturn(true);
        assertThat(cifAssetOrchestrator.isRootAsset(new AssetKey("parentAssetId", 1L)), is(true));
    }

    @Test
    public void shouldReturnFalseWhenAnAssetIsNotARootAsset() {
        when(cifAssetRepository.isRootAsset(new AssetKey("childAssetId", 1L))).thenReturn(false);
        assertThat(cifAssetOrchestrator.isRootAsset(new AssetKey("childAssetId", 1L)), is(false));
    }

    @Test
    public void shouldCancelAnAsset() {
        CIFAsset anAsset = aCIFAsset().build();
        cifAssetOrchestrator.cancelAssetTree(new AssetKey("anId", 1L), "aRelationName", anAsset);
        verify(cifAssetRepository, times(1)).cancelAssetTree(new AssetKey("anId", 1L), "aRelationName", anAsset);
    }

    @Test
    public void shouldExtendAssetCharacteristicWithGivenExtensions() {
        CIFAsset anAsset = aCIFAsset().withCharacteristic("A", "aValue").build();
        cifAssetOrchestrator.forceExtendAsset(anAsset, anAsset.getCharacteristic("A"), CIFAssetExtension.allExtensions());
        verify(cifAssetExtender, times(1)).extend(anAsset, anAsset.getCharacteristic("A"), CIFAssetExtension.allExtensions());
    }

    @After
    public void clearCache() {
        AssetCacheManager.clearAssetCaches();
    }
}