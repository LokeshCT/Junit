package com.bt.rsqe.customerinventory.service.providers;

import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetChoosableCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationshipCardinality;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetEvaluator;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.StencilReservedAttributesHelper;
import com.bt.rsqe.customerinventory.service.filters.CIFAssetFilter;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.extensions.RuleFilter;
import com.bt.rsqe.domain.product.extensions.RuleSiteFilter;
import com.bt.rsqe.domain.product.extensions.StructuredRule;
import com.bt.rsqe.domain.product.extensions.ValidationErrorType;
import com.bt.rsqe.domain.product.parameters.ConsumerCardinality;
import com.bt.rsqe.domain.product.parameters.RelatedProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.perf4j.StopWatch;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.domain.product.extensions.FilterRuleExecutionPoint.*;
import static com.bt.rsqe.enums.AssetVersionStatus.*;
import static com.bt.rsqe.logging.LogLevel.*;
import static com.google.common.collect.Lists.*;
import static java.lang.Integer.*;
import static org.apache.commons.collections.CollectionUtils.*;

public class ChoosableCandidateProvider {
    private final Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private final List<AssetVersionStatus> allowedConsumerStatus = newArrayList(CUSTOMER_ACCEPTED, PROVISIONING, IN_SERVICE);
    private final CIFAssetCharacteristicEvaluatorFactory evaluatorFactory;
    private final StencilReservedAttributesHelper stencilReservedAttributesHelper;

    public ChoosableCandidateProvider(CIFAssetOrchestrator cifAssetOrchestrator, CIFAssetCharacteristicEvaluatorFactory evaluatorFactory, StencilReservedAttributesHelper stencilReservedAttributesHelper) {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.evaluatorFactory = evaluatorFactory;
        this.stencilReservedAttributesHelper = stencilReservedAttributesHelper;
    }

    public List<CIFAssetChoosableCandidate> getChoosableCandidates(CIFAsset baseAsset, List<SalesRelationship> namedRelationships) {
        List<CIFAssetChoosableCandidate> candidates = new ArrayList<CIFAssetChoosableCandidate>();
        if(isEmpty(namedRelationships)) {
            return candidates;
        }

        final CIFAsset ownerAsset = cifAssetOrchestrator.extendAsset(baseAsset, newArrayList(ProductOfferingDetail, QuoteOptionItemDetail, ProductRules));

        // The following information will be the same for all relationships with the same name so we can use get(0)
        final SalesRelationship firstSalesRelationship = namedRelationships.get(0);
        final ConsumerCardinality consumerCardinality = firstSalesRelationship.getConsumerCardinality();
        final int maxConsumerCardinality = consumerCardinality.isMaximumSpecified() ? consumerCardinality.getMaximumCardinality() : MAX_VALUE;
        final String relationshipName = firstSalesRelationship.getRelationshipName().value();

        for (SalesRelationship namedRelationship : namedRelationships) {
            final String productCode = namedRelationship.getProductIdentifier().getProductId();
            candidates.addAll(findExistingCandidates(new ExistingCandidatesKey(productCode, ownerAsset, maxConsumerCardinality, relationshipName)));
        }

        return candidates;
    }

    public List<CIFAssetChoosableCandidate> getAutoChoosableCandidates(CIFAsset baseAsset, List<SalesRelationship> namedRelationships, boolean stencilRelatedProduct, CIFAssetRelationshipCardinality defaultCardinality) {
        List<CIFAssetChoosableCandidate> autoChoosableCandidates = new ArrayList<CIFAssetChoosableCandidate>();

        List<SalesRelationship> defaulatbleCandidates = defaultableCandidates(namedRelationships, stencilRelatedProduct);
        if(isEmpty(namedRelationships) || defaulatbleCandidates.isEmpty()) {
            return autoChoosableCandidates;
        }

        final CIFAsset ownerAsset = cifAssetOrchestrator.extendAsset(baseAsset, newArrayList(ProductOfferingDetail, QuoteOptionItemDetail, ProductRules));

        // The following information will be the same for all relationships with the same name so we can use get(0)
        final SalesRelationship firstSalesRelationship = defaulatbleCandidates.get(0);
        final ConsumerCardinality consumerCardinality = firstSalesRelationship.getConsumerCardinality();
        final int maxConsumerCardinality = consumerCardinality.isMaximumSpecified() ? consumerCardinality.getMaximumCardinality() : MAX_VALUE;
        final String relationshipName = firstSalesRelationship.getRelationshipName().value();

        for (SalesRelationship salesRelationship : defaulatbleCandidates) {
            final String productCode = salesRelationship.getProductIdentifier().getProductId();
            final List<CIFAssetRelationship> relationships = ownerAsset.getRelationships(salesRelationship.getRelationshipName().value());

            if(salesRelationship.getDefault() > relationships.size()) {
                long startTime = System.currentTimeMillis();
                autoChoosableCandidates.addAll(findExistingCandidates( new ExistingCandidatesKey(productCode, ownerAsset, maxConsumerCardinality, relationshipName)));
                long elapsedTime = (System.currentTimeMillis() - startTime)/1000;
                logger.responseTimeForExistingCandidates(baseAsset.getAssetKey(), salesRelationship.getRelatedProductIdentifier(), salesRelationship.getRelationshipName(),
                                                         salesRelationship.hasStencilSet(), elapsedTime);
            }
        }

        return autoChoosableCandidates;
    }

    private List<SalesRelationship> defaultableCandidates(List<SalesRelationship> namedRelationships, final boolean stencilRelatedProduct) {
        return newArrayList(Iterables.filter(namedRelationships, new Predicate<SalesRelationship>() {
            @Override
            public boolean apply(SalesRelationship input) {
                return input.getDefault() > 0 && RelationshipType.RelatedTo.equals(input.getType()) && isAutoAddable(stencilRelatedProduct, input);
            }
        }));
    }

    private boolean isAutoAddable(boolean stencilRelatedProduct, SalesRelationship namedRelationship) {
        return !stencilRelatedProduct || namedRelationship.hasStencilSet();
    }

    private List<CIFAssetChoosableCandidate> findExistingCandidates(ExistingCandidatesKey existingCandidatesKey) {
        StopWatch stopWatch = new StopWatch("findExistingCandidates");

        List<CIFAssetChoosableCandidate> candidates = AssetCacheManager.findExistingCandidates(existingCandidatesKey, this);
        if(candidates != null) {
            logger.findExistingCandidates(existingCandidatesKey, true, stopWatch);
            return candidates;
        }

        List<CIFAssetChoosableCandidate> existingCandidates = existingCandidates(existingCandidatesKey);
        logger.findExistingCandidates(existingCandidatesKey, false, stopWatch);
        return existingCandidates;

    }

    public List<CIFAssetChoosableCandidate> existingCandidates(ExistingCandidatesKey existingCandidatesKey) {
        final CIFAsset owner = existingCandidatesKey.getOwner();
        List<CIFAsset> assets = cifAssetOrchestrator.eligibleExistingCandidates(owner.getCustomerId(), owner.getContractId(), existingCandidatesKey.getProductCode(),
                newArrayList(ProductOfferingDetail, ProductRules, QuoteOptionItemDetail), owner.getQuoteOptionId(), owner.getSiteId(), siteMatters(owner, existingCandidatesKey.getRelationshipName()));

        assets = CIFAssetFilter.latestAssetsFilter(assets).filter(assets);
        assets = filterCandidatesBasedOnProductAvailability(assets);
        assets = filterCandidatesByConsumerCardinality(assets, existingCandidatesKey.getMaxConsumerCardinality());
        assets = filterCandidatesByRules(owner, assets, existingCandidatesKey.getRelationshipName());

        List<CIFAssetChoosableCandidate> candidates = new ArrayList<CIFAssetChoosableCandidate>();
        for (CIFAsset asset : assets) {
            final CIFAssetCharacteristic stencilCharacteristic = stencilReservedAttributesHelper.getStencilCharacteristic(asset);
            String stencilCode = stencilCharacteristic != null ? stencilCharacteristic.getValue() : "";
            candidates.add(new CIFAssetChoosableCandidate(asset.getAssetKey(), asset.getProductCode(), stencilCode));
        }
        return candidates;
    }

    private boolean siteMatters(CIFAsset owner, String relationshipName) {
        for (StructuredRule structuredRule : owner.getProductRules()) {
            if (structuredRule instanceof RuleSiteFilter) {
                if (((RuleSiteFilter) structuredRule).getRelationshipName().equals(relationshipName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<CIFAsset> filterCandidatesByRules(final CIFAsset owner, List<CIFAsset> assets, final String relationshipName) {
        final List<StructuredRule> applicableRules = newArrayList(Iterables.filter(owner.getProductRules(), new Predicate<StructuredRule>() {
            @Override
            public boolean apply(StructuredRule rule) {
                if (!rule.isFilterRule()) {
                    return false;
                }
                RuleFilter filterRule = (RuleFilter) rule;
                return FilterCandidateInstances.equals(filterRule.getExecutionPoint()) &&
                       relationshipName.equals(filterRule.getRelationshipName());
            }
        }));

        return newArrayList(Iterables.filter(assets, new Predicate<CIFAsset>() {
            @Override
            public boolean apply(CIFAsset asset) {
                for (StructuredRule rule : applicableRules) {
                    RuleFilter filterRule = (RuleFilter) rule;

                    final CIFAssetEvaluator baseAssetEvaluator = new CIFAssetEvaluator(owner, cifAssetOrchestrator, evaluatorFactory);
                    final CIFAssetEvaluator candidateAssetEvaluator = new CIFAssetEvaluator(asset, cifAssetOrchestrator, evaluatorFactory);

                    List<ContextualEvaluatorMap> evaluators = ContextualEvaluatorMap.defaultEvaluator(baseAssetEvaluator);
                    evaluators.add(new ContextualEvaluatorMap(relationshipName, candidateAssetEvaluator));

                    if (filterRule.getSatisfaction(evaluators, relationshipName) != ValidationErrorType.Satisfied) {
                        return false;
                    }
                }
                return true;
            }
        }));
    }

    private List<CIFAsset> filterCandidatesByConsumerCardinality(List<CIFAsset> assets, final int maxConsumerCardinality) {
        return newArrayList(Iterables.filter(assets, new Predicate<CIFAsset>() {
            @Override
            public boolean apply(CIFAsset asset) {
                final List<CIFAsset> ownerAssets = cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(asset.getAssetKey()), allowedConsumerStatus, Optional.<String>absent(), Optional.<String>absent());
                return ownerAssets.size() < maxConsumerCardinality;
            }
        }));
    }

    private List<CIFAsset> filterCandidatesOnSameQuoteOrInService(final CIFAsset owner, List<CIFAsset> assets) {
        final List<AssetVersionStatus> acceptedStatuses = newArrayList(PROVISIONING, IN_SERVICE);

        // allow assets on the same quote option or ones that are Provisioning or In Service.
        List<CIFAsset> allowedInstances = newArrayList(Iterables.filter(assets, new Predicate<CIFAsset>() {
            @Override
            public boolean apply(CIFAsset input) {
                return owner.getQuoteOptionId().equals(input.getQuoteOptionId())
                       || acceptedStatuses.contains(input.getAssetVersionStatus());
            }
        }));

        // ensure only the latest version of an asset is returned
        allowedInstances = CIFAssetFilter.latestAssetsFilter(allowedInstances).filter(allowedInstances);

        return allowedInstances;
    }

    private List<CIFAsset> filterCandidatesBasedOnProductAvailability(List<CIFAsset> assets) {
        return newArrayList(Iterables.filter(assets, new Predicate<CIFAsset>() {
            @Override
            public boolean apply(@Nullable CIFAsset input) {
                return isProductAvailable(input);
            }
        }));
    }

    private boolean isProductAvailable(CIFAsset asset) {
        return asset.getQuoteOptionItemDetail().isMigrationQuoteOption() || asset.getOfferingDetail().isAvailable();
    }

    interface Logger {
        @Log(level = DEBUG, format = "existing candidates read for asset %s, for relation %s, relation name - %s, hasStencil - %s, response in - %s")
        void responseTimeForExistingCandidates(AssetKey assetKey, RelatedProductIdentifier productIdentifier, RelationshipName relationshipName, boolean hasStencilSet, long elaspsedTime);

        @Log(level = DEBUG, format = "Getting Existing Candidates for key=%s cacheHit=%s stopwatch=%s")
        void findExistingCandidates(ExistingCandidatesKey existingCandidatesKey, boolean isCacheHit, StopWatch stopWatch);
    }

    public static class ExistingCandidatesKey {
        private final String productCode;
        private final CIFAsset owner;
        private final int maxConsumerCardinality;
        private final String relationshipName;

        public ExistingCandidatesKey(String productCode, CIFAsset owner, int maxConsumerCardinality, String relationshipName) {
            this.productCode = productCode;
            this.owner = owner;
            this.maxConsumerCardinality = maxConsumerCardinality;
            this.relationshipName = relationshipName;
        }

        public String getProductCode() {
            return productCode;
        }

        public CIFAsset getOwner() {
            return owner;
        }

        public int getMaxConsumerCardinality() {
            return maxConsumerCardinality;
        }

        public String getRelationshipName() {
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


        @Override
        public String toString() {
            return "ExistingCandidatesKey{" +
                    "productCode='" + productCode + '\'' +
                    ", owner=" + owner.getAssetKey() +
                    ", maxConsumerCardinality=" + maxConsumerCardinality +
                    ", relationshipName='" + relationshipName + '\'' +
                    '}';
        }
    }
}
