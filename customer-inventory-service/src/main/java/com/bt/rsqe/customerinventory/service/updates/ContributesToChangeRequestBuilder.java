package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.QuoteOptionContext;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetEvaluator;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.service.providers.AssociatedAssetKeyProvider;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.DetailedAssetKey;
import com.bt.rsqe.domain.product.Association;
import com.bt.rsqe.domain.product.LocalAssociation;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.exception.MaxDepthReachedException;
import com.bt.rsqe.expressionevaluator.ExpressionEvaluator;
import com.bt.rsqe.expressionevaluator.expr.SyntaxException;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.customerinventory.service.updates.ContributesToRequestEnum.*;
import static com.bt.rsqe.domain.product.Association.AssociationType.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class ContributesToChangeRequestBuilder {
    private static final int MAX_DEPTH = 5;
    private PmrHelper pmrHelper;
    private AssociatedAssetKeyProvider associatedAssetKeyProvider;

    private CIFAssetOrchestrator cifAssetOrchestrator;
    private CIFAssetCharacteristicEvaluatorFactory evaluatorFactory;

    public ContributesToChangeRequestBuilder(PmrHelper pmrHelper, AssociatedAssetKeyProvider associatedAssetKeyProvider,
                                             CIFAssetOrchestrator cifAssetOrchestrator, CIFAssetCharacteristicEvaluatorFactory evaluatorFactory) {
        this.pmrHelper = pmrHelper;
        this.associatedAssetKeyProvider = associatedAssetKeyProvider;
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.evaluatorFactory = evaluatorFactory;
    }

    public Set<CIFAssetUpdateRequest> buildRequests(AssetKey assetKey, String productCode, String attributeName, int contributesToExecutionDepth) {
        validateContributesToDepth(contributesToExecutionDepth);
        Set<Association> attributeAssociations = pmrHelper.getProductOffering(productCode).getAttributeAssociations(attributeName);
        return buildRequests(assetKey, contributesToExecutionDepth, filterOutSelfAssociations(attributeName, attributeAssociations));
    }

    public Set<CIFAssetUpdateRequest> buildRequests(AssetKey assetKey, String productCode, RelationshipName relationshipName, int contributesToExecutionDepth) {
        validateContributesToDepth(contributesToExecutionDepth);
        Set<Association> associations = pmrHelper.getProductOffering(productCode).getAttributeAssociations(relationshipName);
        return buildRequests(assetKey, contributesToExecutionDepth, associations);
    }

    public Set<CIFAssetUpdateRequest> buildRequestsOnCancellation(AssetKey assetKey, String productCode, int contributesToExecutionDepth) {
        validateContributesToDepth(contributesToExecutionDepth);
        Set<Association> attributeAssociations = pmrHelper.getProductOffering(productCode).getDirectAssociations();  //Local Association are not required as asset is cancelled.
        return buildRequests(assetKey, contributesToExecutionDepth, attributeAssociations);
    }

    private Set<CIFAssetUpdateRequest> buildRequests(AssetKey assetKey, int contributesToExecutionDepth, Set<Association> attributeAssociations) {
        if (attributeAssociations.isEmpty()) {
            return Collections.emptySet();
        }

        String currentQuoteOptionContext = QuoteOptionContext.get();
        Set<CIFAssetUpdateRequest> contributesToRequest = newLinkedHashSet();

        for (Association association : attributeAssociations) {
            AssociatedAssetKey associatedAssetKey = new AssociatedAssetKey(assetKey, association);
            Set<DetailedAssetKey> associatedDetailedAssetKeys = associatedAssetKeyProvider.getKeys(associatedAssetKey);
            associatedDetailedAssetKeys = filterOnlyCurrentQuoteOptionAssociatedAssets(associatedDetailedAssetKeys, currentQuoteOptionContext);   //Filter only the current quote option associated assets.
            contributesToRequest.addAll(contributesToRequest(association, filterAssociations(associatedDetailedAssetKeys, association), contributesToExecutionDepth));
        }
        return contributesToRequest;
    }

    private Set<DetailedAssetKey> filterOnlyCurrentQuoteOptionAssociatedAssets(Set<DetailedAssetKey> associatedDetailedAssetKeys, final String quoteOptionIdContext) {
        if (isNotEmpty(quoteOptionIdContext)) {
            return newHashSet(Iterables.filter(associatedDetailedAssetKeys, new Predicate<DetailedAssetKey>() {
                @Override
                public boolean apply(DetailedAssetKey input) {
                    return quoteOptionIdContext.equals(input.getQuoteOptionId());
                }
            }));
        }
        return associatedDetailedAssetKeys;
    }

    private Set<Association> filterOutSelfAssociations(final String attributeName, Set<Association> attributeAssociations) {
        return newHashSet(Iterables.filter(attributeAssociations, new Predicate<Association>() {
            @Override
            public boolean apply(Association input) {
                return !(input instanceof LocalAssociation && ATTRIBUTE_SOURCE == input.getAssociationType() && input.getLinkName().equals(attributeName));
            }
        }));
    }

    private void validateContributesToDepth(int contributesToExecutionDepth) {
        if (contributesToExecutionDepth > MAX_DEPTH) {
            throw new MaxDepthReachedException("Stopping the ContributesTo RuleAttributeSource execution as recursion reaches max configured limit 5. There might be infinite loop.");
        }
    }

    private Set<AssetKey> filterAssociations(Set<DetailedAssetKey> associatedAssetKeys, Association association) {
        Set<AssetKey> assetsNeedsReload = newHashSet();
        for (DetailedAssetKey detailedAssetKey : associatedAssetKeys) {
            AssetKey assetKey = detailedAssetKey.assetKey();

            if (ATTRIBUTE_SOURCE != association.getAssociationType() || !association.isFilterExecutionRequired()) {
                assetsNeedsReload.add(assetKey);
            } else {
                CIFAsset asset = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(Relationships, CharacteristicAllowedValues)));
                if (isFilterSatisfied(asset, association)) {
                    assetsNeedsReload.add(assetKey);
                }
            }
        }
        return assetsNeedsReload;
    }

    private boolean isFilterSatisfied(CIFAsset asset, Association association) {
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(asset, cifAssetOrchestrator, evaluatorFactory);
        try {
            Object evaluate = ExpressionEvaluator.evaluate(association.getFilterExpression(), cifAssetEvaluator);
            return evaluate.toString().equals("1.0");
        } catch (SyntaxException e) {
            return false;
        }
    }

    public Set<CIFAssetUpdateRequest> buildRequests(AssetKey assetKey, String productCode, List<String> attributes, int contributesToExecutionDepth) {
        Set<CIFAssetUpdateRequest> cifAssetUpdateRequests = newHashSet();
        for (String attributeName : attributes) {
           cifAssetUpdateRequests.addAll(buildRequests(assetKey, productCode, attributeName, contributesToExecutionDepth));
        }
        return cifAssetUpdateRequests;
    }

    public static class AssociatedAssetKey {

        private final AssetKey assetKey;
        private final Association association;

        public AssociatedAssetKey(AssetKey assetKey, Association association) {
            this.assetKey = assetKey;
            this.association = association;
        }

        public AssetKey getAssetKey() {
            return assetKey;
        }

        public Association getAssociation() {
            return association;
        }

        @Override
        public boolean equals(Object o) {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }
}
