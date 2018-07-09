package com.bt.rsqe.customerinventory.service.cache;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetChoosableCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetLineItemKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateResponse;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator.MigratedCustomerKey;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator.OwnerAssetKey;
import com.bt.rsqe.customerinventory.service.providers.AssetCandidateProvider;
import com.bt.rsqe.customerinventory.service.providers.AssetCandidateProvider.ChoosableAssetKey;
import com.bt.rsqe.customerinventory.service.providers.AssociatedAssetKeyProvider;
import com.bt.rsqe.customerinventory.service.providers.ChoosableCandidateProvider;
import com.bt.rsqe.customerinventory.service.updates.ContributesToChangeRequestBuilder.AssociatedAssetKey;
import com.bt.rsqe.domain.DetailedAssetKey;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.google.common.base.Optional;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.customerinventory.service.cache.CacheAwareTransaction.*;

public class AssetCacheManager {

    private static ThreadLocal<Set<QuoteOptionItemDTO>> CREATED_LINE_ITEM_CACHE = new ThreadLocal<Set<QuoteOptionItemDTO>>();
    private static ThreadLocal<Map<String, QuoteOptionItemDTO>> REMOVED_LINE_ITEM_CACHE = new ThreadLocal<Map<String, QuoteOptionItemDTO>>();
    private static ThreadLocalResourceCache<ChoosableAssetKey, List<AssetDTO>> CHOOSABLE_ASSET_CACHE = new ThreadLocalResourceCache<ChoosableAssetKey, List<AssetDTO>>();
    private static ThreadLocalResourceCache<ChoosableCandidateProvider.ExistingCandidatesKey, List<CIFAssetChoosableCandidate>> EXISTING_CANDIDATES_CACHE = new ThreadLocalResourceCache<ChoosableCandidateProvider.ExistingCandidatesKey, List<CIFAssetChoosableCandidate>>();
    private static ThreadLocalResourceCache<CIFAssetKey, Optional<CIFAsset>> IN_SERVICE_ASSET_CACHE = new ThreadLocalResourceCache<CIFAssetKey, Optional<CIFAsset>>();
    private static ThreadLocalResourceCache<CIFAssetKey, CIFAsset> ASSET_CACHE = new ThreadLocalResourceCache<CIFAssetKey, CIFAsset>();
    private static ThreadLocalResourceCache<CIFAssetLineItemKey, CIFAsset> ASSET_LINE_ITEM_CACHE = new ThreadLocalResourceCache<CIFAssetLineItemKey, CIFAsset>();
    private static ThreadLocalResourceCache<CIFAssetKey, CIFAsset> PARENT_CACHE = new ThreadLocalResourceCache<CIFAssetKey, CIFAsset>();
    private static ThreadLocalResourceCache<OwnerAssetKey, List<CIFAsset>> OWNER_CACHE = new ThreadLocalResourceCache<OwnerAssetKey, List<CIFAsset>>();
    private static ThreadLocalResourceCache<AssociatedAssetKey, Set<DetailedAssetKey>> ASSET_ASSOCIATIONS_CACHE = new ThreadLocalResourceCache<AssociatedAssetKey, Set<DetailedAssetKey>>();
    private static ThreadLocalResourceCache<MigratedCustomerKey, Boolean> MIGRATED_CUSTOMER_CACHE = new ThreadLocalResourceCache<MigratedCustomerKey, Boolean>();


    public static CIFAsset getAsset(CIFAssetKey cifAssetKey, final CIFAssetOrchestrator cifAssetOrchestrator) {
        if (isCacheAware()) {
            CacheFetcher<CIFAssetKey, CIFAsset> fetcher = new CacheFetcher<CIFAssetKey, CIFAsset>(ASSET_CACHE) {
                @Override
                public CIFAsset fetch(CIFAssetKey key) {
                    return cifAssetOrchestrator.getCifAsset(key);
                }
            };
            return fetcher.get(cifAssetKey);
        }
        return null;
    }

    public static CIFAsset getAsset(CIFAssetLineItemKey cifAssetLineItemKey, final CIFAssetOrchestrator cifAssetOrchestrator) {
        if (isCacheAware()) {
            CacheFetcher<CIFAssetLineItemKey, CIFAsset> fetcher = new CacheFetcher<CIFAssetLineItemKey, CIFAsset>(ASSET_LINE_ITEM_CACHE) {
                @Override
                public CIFAsset fetch(CIFAssetLineItemKey cifAssetLineItemKey) {
                    return cifAssetOrchestrator.getCifRootAsset(cifAssetLineItemKey);
                }
            };
            return fetcher.get(cifAssetLineItemKey);
        }
        return null;
    }

    public static CIFAsset getParentAsset(CIFAssetKey cifAssetKey, final CIFAssetOrchestrator cifAssetOrchestrator) {
        if (isCacheAware()) {
            CacheFetcher<CIFAssetKey, CIFAsset> fetcher = new CacheFetcher<CIFAssetKey, CIFAsset>(PARENT_CACHE) {
                @Override
                public CIFAsset fetch(CIFAssetKey cifAssetKey) {
                    return cifAssetOrchestrator.getCifParentAsset(cifAssetKey);
                }
            };
            return fetcher.get(cifAssetKey);
        }
        return null;
    }

    public static List<CIFAsset> getOwnerAssets(OwnerAssetKey key, final CIFAssetOrchestrator cifAssetOrchestrator) {
        if (isCacheAware()) {
            CacheFetcher<OwnerAssetKey, List<CIFAsset>> fetcher = new CacheFetcher<OwnerAssetKey, List<CIFAsset>>(OWNER_CACHE) {
                @Override
                public List<CIFAsset> fetch(OwnerAssetKey key) {
                    return cifAssetOrchestrator.getCifOwnerAssets(key);
                }

            };
            return fetcher.get(key);
        }
        return null;
    }

    public static Set<DetailedAssetKey> getAssociatedAssetKeys(final AssociatedAssetKey associatedAssetKey, final AssociatedAssetKeyProvider assetKeyProvider) {

        if (isCacheAware()) {
            CacheFetcher<AssociatedAssetKey, Set<DetailedAssetKey>> fetcher = new CacheFetcher<AssociatedAssetKey, Set<DetailedAssetKey>>(ASSET_ASSOCIATIONS_CACHE) {
                @Override
                public Set<DetailedAssetKey> fetch(AssociatedAssetKey key) {
                    return assetKeyProvider.getAssetKeys(associatedAssetKey);
                }
            };
            return fetcher.get(associatedAssetKey);
        }
        return null;
    }

    public static Boolean isMigratedCustomer(final MigratedCustomerKey migratedCustomerKey, final CIFAssetOrchestrator cifAssetOrchestrator) {
        if (isCacheAware()) {
            CacheFetcher<MigratedCustomerKey, Boolean> fetcher = new CacheFetcher<MigratedCustomerKey, Boolean>(MIGRATED_CUSTOMER_CACHE) {
                @Override
                public Boolean fetch(MigratedCustomerKey key) {
                    return cifAssetOrchestrator.getMigratedCustomer(migratedCustomerKey);
                }
            };
            return fetcher.get(migratedCustomerKey);
        }
        return null;
    }

    public static List<AssetDTO> getChoosableCandidates(final ChoosableAssetKey choosableAssetKey, final AssetCandidateProvider assetCandidateProvider) {
        if (isCacheAware()) {
            CacheFetcher<ChoosableAssetKey, List<AssetDTO>> fetcher = new CacheFetcher<ChoosableAssetKey, List<AssetDTO>>(CHOOSABLE_ASSET_CACHE) {
                @Override
                public List<AssetDTO> fetch(ChoosableAssetKey key) {
                    return assetCandidateProvider.choosableCandidates(choosableAssetKey);
                }
            };
            return fetcher.get(choosableAssetKey);
        }
        return null;
    }

    public static List<CIFAssetChoosableCandidate> findExistingCandidates(final ChoosableCandidateProvider.ExistingCandidatesKey existingCandidatesKey, final ChoosableCandidateProvider choosableCandidateProvider) {
        if (isCacheAware()) {
            CacheFetcher<ChoosableCandidateProvider.ExistingCandidatesKey, List<CIFAssetChoosableCandidate>> fetcher = new CacheFetcher<ChoosableCandidateProvider.ExistingCandidatesKey, List<CIFAssetChoosableCandidate>>(EXISTING_CANDIDATES_CACHE) {
                @Override
                public List<CIFAssetChoosableCandidate> fetch(ChoosableCandidateProvider.ExistingCandidatesKey key) {
                    return choosableCandidateProvider.existingCandidates(existingCandidatesKey);
                }
            };
            return fetcher.get(existingCandidatesKey);
        }
        return null;
    }

    public static Optional<CIFAsset> getInServiceAsset(final CIFAssetKey cifAssetKey, final CIFAssetOrchestrator cifAssetOrchestrator) {
        if (isCacheAware()) {
            CacheFetcher<CIFAssetKey, Optional<CIFAsset>> fetcher = new CacheFetcher<CIFAssetKey, Optional<CIFAsset>>(IN_SERVICE_ASSET_CACHE) {
                @Override
                public Optional<CIFAsset> fetch(CIFAssetKey key) {
                    return cifAssetOrchestrator.inServiceAsset(key);
                }
            };
            return fetcher.get(cifAssetKey);
        }
        return Optional.absent();
    }

    //This method is called when a save impacts the all asset, so its currently used after every request being handled
    public static void clearCaches(CIFAssetUpdateResponse updateResponse) {
        ASSET_CACHE.remove();
        ASSET_LINE_ITEM_CACHE.remove();
        ASSET_ASSOCIATIONS_CACHE.remove();
        if (updateResponse != null && updateResponse.relationshipImpactResponse()) {
            OWNER_CACHE.remove();
            PARENT_CACHE.remove();
        }
    }

    //This method is called when a save impacts only the current asset, and NOT any of its owners and its parent
    public static void clearAssetCaches() {
        ASSET_CACHE.remove();
        ASSET_LINE_ITEM_CACHE.remove();
        ASSET_ASSOCIATIONS_CACHE.remove();
    }

    //This method is called after completion of all saves sent to CIF service
    public static void clearAllCaches() {
        ASSET_CACHE.remove();
        ASSET_LINE_ITEM_CACHE.remove();
        OWNER_CACHE.remove();
        PARENT_CACHE.remove();
        ASSET_ASSOCIATIONS_CACHE.remove();
        MIGRATED_CUSTOMER_CACHE.remove();
        EXISTING_CANDIDATES_CACHE.remove();
        CHOOSABLE_ASSET_CACHE.remove();
        IN_SERVICE_ASSET_CACHE.remove();
        REMOVED_LINE_ITEM_CACHE.remove();
        CREATED_LINE_ITEM_CACHE.remove();
    }

    public static void recordCreatedQuoteOptionItem(QuoteOptionItemDTO quoteOptionItemDTO) {
        if (isCacheAware()) {
            Set<QuoteOptionItemDTO> createdItems = CREATED_LINE_ITEM_CACHE.get();
            if(createdItems == null) {
              CREATED_LINE_ITEM_CACHE.set(new HashSet<QuoteOptionItemDTO>());
            }
            CREATED_LINE_ITEM_CACHE.get().add(quoteOptionItemDTO);
        }
    }

    public static Set<QuoteOptionItemDTO> getCreatedQuoteOptionItems() {
        if (isCacheAware()) {
            Set<QuoteOptionItemDTO> createdItems = CREATED_LINE_ITEM_CACHE.get();
            if(createdItems == null) {
                return Collections.emptySet();
            }
            return CREATED_LINE_ITEM_CACHE.get();
        }
        return Collections.emptySet();
    }

    public static void recordRemovedQuoteOptionItem(String lineItemId, QuoteOptionItemDTO quoteOptionItemDTO) {
        if (isCacheAware()) {
            Map<String, QuoteOptionItemDTO> removedItems = REMOVED_LINE_ITEM_CACHE.get();
            if(removedItems == null) {
                REMOVED_LINE_ITEM_CACHE.set(new HashMap<String, QuoteOptionItemDTO>());
            }
            REMOVED_LINE_ITEM_CACHE.get().put(lineItemId, quoteOptionItemDTO);
        }
    }

    public static Map<String, QuoteOptionItemDTO> getRemovedQuoteOptionItems() {
        if (isCacheAware()) {
            Map<String, QuoteOptionItemDTO> removedItems = REMOVED_LINE_ITEM_CACHE.get();
            if(removedItems == null) {
                return Collections.emptyMap();
            }
            return REMOVED_LINE_ITEM_CACHE.get();
        }
        return Collections.emptyMap();
    }

    public static void clearCreatedAndRemovedLineItems() {
        CREATED_LINE_ITEM_CACHE.remove();
        REMOVED_LINE_ITEM_CACHE.remove();
    }
}
