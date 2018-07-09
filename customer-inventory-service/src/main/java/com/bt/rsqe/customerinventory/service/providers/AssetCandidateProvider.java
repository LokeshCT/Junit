package com.bt.rsqe.customerinventory.service.providers;

import com.bt.rsqe.LazyValue;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.filter.AssetFilter;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.orchestrators.AssetModelOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.keys.ProjectQuoteKey;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.perf4j.StopWatch;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.logging.LogLevel.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.*;

public class AssetCandidateProvider {
    private final Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private AssetModelOrchestrator assetModelOrchestrator;
    private Map<ProjectQuoteKey, Boolean> migrationDetails = newHashMap();
    private Map<String, ProductOffering> baseOfferings = newHashMap();

    public AssetCandidateProvider(AssetModelOrchestrator assetModelOrchestrator) {
        this.assetModelOrchestrator = assetModelOrchestrator;
    }

    public List<AssetDTO> getChoosableCandidates(AssetKey assetKey, RelationshipName relationshipName) {
        StopWatch stopWatch = new StopWatch("getChoosableCandidates");
        ChoosableAssetKey choosableAssetKey = new ChoosableAssetKey(assetKey, relationshipName);
        List<AssetDTO> candidates = AssetCacheManager.getChoosableCandidates(choosableAssetKey, this);
        if(candidates != null) {
            logger.findChoosableCandidates(choosableAssetKey, true, stopWatch);
            return candidates;
        }
        final List<AssetDTO> assetDTOs = choosableCandidates(choosableAssetKey);
        logger.findChoosableCandidates(choosableAssetKey, false, stopWatch);
        return assetDTOs;
    }

    public List<AssetDTO> choosableCandidates(ChoosableAssetKey choosableAssetKey) {
        final AssetDTO ownerAsset = assetModelOrchestrator.fetchAsset(choosableAssetKey.getAssetKey());
        ProductOffering ownerOffering = assetModelOrchestrator.fetchOffering(ownerAsset);
        LazyValue<String> quoteOptionName = getQuoteOptionName(ownerAsset);

        List<SalesRelationship> applicableRelationships = getApplicableRelationships(ownerOffering, choosableAssetKey.getRelationshipName());

        // TODO filter relationships through rules

        Set<AssetDTO> candidates = newHashSet();

        for(ProductIdentifier productIdentifier : getUniqueLinkedIdentifiers(applicableRelationships)) {
            candidates.addAll(findExistingCandidates(productIdentifier, ownerAsset));
            candidates.addAll(findExistingExternalCandidates(productIdentifier, choosableAssetKey.getRelationshipName(), ownerAsset, ownerOffering, quoteOptionName));

            // TODO filter candidates by consumer cardinality
        }

        // TODO filter candidates through rules

        return newArrayList(candidates);
    }

    private Set<ProductIdentifier> getUniqueLinkedIdentifiers(List<SalesRelationship> applicableRelationships) {
        Set<ProductIdentifier> productIdentifiers = newHashSet();

        for(SalesRelationship salesRelationship : applicableRelationships) {
            productIdentifiers.addAll(salesRelationship.getLinkedIdentifiers());
        }

        return productIdentifiers;
    }

    private LazyValue<String> getQuoteOptionName(final AssetDTO ownerAsset) {
        return new LazyValue<String>() {
            @Override
            protected String initValue() {
                return assetModelOrchestrator.fetchQuoteOption(ownerAsset).getName();
            }
        };
    }

    private List<AssetDTO> findExistingCandidates(ProductIdentifier linkedIdentifier, final AssetDTO owner) {
        List<AssetDTO> assetList = newArrayList();

        try {
            List<AssetDTO> assets = assetModelOrchestrator.fetchAssets(new CustomerId(owner.getCustomerId()),
                                                                       new ContractId(owner.getContractId()),
                                                                       linkedIdentifier);

            assets = filterCandidatesOnSameQuoteOrInService(owner, assets);

            assets = filterCandidatesBasedOnProductAvailability(assets);

            assetList.addAll(assets);
        } catch (ResourceNotFoundException exception) {
            //keep calm and carry on
        }

        return assetList;
    }

    private List<AssetDTO> filterCandidatesOnSameQuoteOrInService(final AssetDTO owner, List<AssetDTO> assets) {
        final List<AssetVersionStatus> acceptedStatuses = newArrayList(AssetVersionStatus.CUSTOMER_ACCEPTED, AssetVersionStatus.PROVISIONING, AssetVersionStatus.IN_SERVICE);

        // allow assets on the same quote option or ones that are Provisioning or In Service.
        List<AssetDTO> allowedInstances = newArrayList(Iterables.filter(assets, new Predicate<AssetDTO>() {
            @Override
            public boolean apply(AssetDTO input) {
                return owner.getQuoteOptionId().equals(input.getQuoteOptionId())
                            || acceptedStatuses.contains(input.getAssetVersionStatus());
            }
        }));

        // ensure only the latest version of an asset is returned
        allowedInstances = AssetFilter.latestAssetsFilter(allowedInstances).filter(allowedInstances);

        return allowedInstances;
    }

    private List<AssetDTO> findExistingExternalCandidates(final ProductIdentifier linkedIdentifier,
                                                          final RelationshipName relationshipName,
                                                          final AssetDTO owner,
                                                          final ProductOffering productOffering,
                                                          final LazyValue<String> quoteOptionName) {
        boolean siteMatters = productOffering.isRelationshipSiteSpecific(relationshipName);
        return assetModelOrchestrator.fetchExternalAssets(owner, linkedIdentifier, quoteOptionName, siteMatters, relationshipName, productOffering.isBundleProduct());
    }

    private List<SalesRelationship> getApplicableRelationships(ProductOffering ownerOffering, RelationshipName relationshipName) {
        return newArrayList(Iterables.filter(ownerOffering.getSalesRelationships(relationshipName), new Predicate<SalesRelationship>() {
            @Override
            public boolean apply(SalesRelationship input) {
                return RelationshipType.RelatedTo.equals(input.getType())
                        && !input.isUpgradeRelationship()
                        && !input.isProviderSalesRelationship()
                        && !input.newOnly();
            }
        }));
    }


    private List<AssetDTO> filterCandidatesBasedOnProductAvailability(List<AssetDTO> assets) {
        return newArrayList(Iterables.filter(assets, new Predicate<AssetDTO>() {
            @Override
            public boolean apply(@Nullable AssetDTO input) {
                return isProductAvailable(input);
            }
        }));
    }

    private boolean isProductAvailable(AssetDTO asset) {
        if(!isMigrationQuote(asset)) {
            return fetchBaseOffering(asset).isAvailable();
        }

        return true;
    }

    private ProductOffering fetchBaseOffering(AssetDTO asset) {
        String productCode = asset.getProductCode();

        ProductOffering baseOffering = baseOfferings.get(productCode);

        if(null == baseOffering) {
            baseOffering = assetModelOrchestrator.fetchBaseOffering(asset);
            baseOfferings.put(productCode, baseOffering);
        }

        return baseOffering;
    }

    private boolean isMigrationQuote(AssetDTO asset) {
        final ProjectQuoteKey key = new ProjectQuoteKey(asset.getProjectId(), asset.getQuoteOptionId());
        Boolean isMigrationQuote = migrationDetails.get(key);

        if(null == isMigrationQuote) {
            final Boolean migrationQuote = assetModelOrchestrator.fetchQuoteOption(asset).getMigrationQuote();
            isMigrationQuote = null != migrationQuote && migrationQuote;
            migrationDetails.put(key, isMigrationQuote);
        }

        return isMigrationQuote;
    }

    public void putAsset(AssetDTO assetDTO) {
        try {
            assetModelOrchestrator.put(assetDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    interface Logger {
        @Log(level = DEBUG, format = "Getting Choosable Candidates for key=%s cacheHit=%s stopwatch=%s")
        void findChoosableCandidates(ChoosableAssetKey choosableAssetKey, boolean isCacheHit, StopWatch stopWatch);
    }

    public static class ChoosableAssetKey {

        private AssetKey assetKey;
        private RelationshipName relationshipName;

        public ChoosableAssetKey(AssetKey assetKey, RelationshipName relationshipName) {
            this.assetKey = assetKey;
            this.relationshipName = relationshipName;
        }

        public AssetKey getAssetKey() {
            return assetKey;
        }

        public RelationshipName getRelationshipName() {
            return relationshipName;
        }

        ///CLOVER:OFF
        @Override
        public boolean equals(Object o) {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
        ///CLOVER:ON

    }


}
