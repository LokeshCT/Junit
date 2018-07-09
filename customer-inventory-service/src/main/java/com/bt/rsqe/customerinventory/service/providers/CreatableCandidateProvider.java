package com.bt.rsqe.customerinventory.service.providers;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCreatableCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetEvaluator;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.extensions.RuleFilter;
import com.bt.rsqe.domain.product.extensions.StructuredRule;
import com.bt.rsqe.domain.product.extensions.ValidationErrorType;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import com.bt.rsqe.expressionevaluator.ProductOfferingEvaluator;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.domain.product.extensions.FilterRuleExecutionPoint.*;
import static com.bt.rsqe.domain.product.parameters.RelationshipType.*;
import static com.bt.rsqe.domain.product.parameters.ResolvesTo.*;
import static com.google.common.collect.Lists.*;

public class CreatableCandidateProvider {
    private final PmrHelper pmrHelper;
    private CIFAssetOrchestrator cifAssetOrchestrator;
    private CIFAssetCharacteristicEvaluatorFactory evaluatorFactory;

    public CreatableCandidateProvider(PmrHelper pmrHelper, CIFAssetOrchestrator cifAssetOrchestrator, CIFAssetCharacteristicEvaluatorFactory evaluatorFactory) {
        this.pmrHelper = pmrHelper;
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.evaluatorFactory = evaluatorFactory;
    }

    public List<CIFAssetCreatableCandidate> getCreatableCandidates(CIFAsset cifAsset, ProductIdentifier ownerIdentifier, List<SalesRelationship> namedRelationships, SimpleProductOfferingType ownerProductType) {
        List<CIFAssetCreatableCandidate> candidates = new ArrayList<CIFAssetCreatableCandidate>();
        for (SalesRelationship namedRelationship : namedRelationships) {
            if (!namedRelationship.isTargetAFeatureSpecification() && (Child == namedRelationship.getType() || isCreatableCandidate(ownerIdentifier, namedRelationship, ownerProductType))){
                final ProductIdentifier productIdentifier = namedRelationship.getRelatedProductIdentifier().getProductIdentifier();
                final StencilId stencilId = namedRelationship.getRelatedProductIdentifier().getStencilId();
                final String productId = productIdentifier.getProductId();
                final String stencilCode = stencilId.getCCode().getValue();

                final ProductOffering creatableOffering = pmrHelper.getProductOffering(productId, stencilCode);
                if(rulesSatisfied(cifAsset, namedRelationship, creatableOffering)) {

                    CIFAssetCreatableCandidate creatableCandidate = new CIFAssetCreatableCandidate(productId,
                                                                                                   productIdentifier.getVersionNumber(),
                                                                                                   stencilCode,
                                                                                                   stencilId.getProductName().getValue(),
                                                                                                   creatableOffering.getVisibleInSummaryText(true),
                                                                                                   creatableOffering.requiresSiteSelection());
                    candidates.add(creatableCandidate);
                }
            }
        }
        return candidates;
    }

    private boolean isCreatableCandidate(ProductIdentifier ownerIdentifier, SalesRelationship namedRelationship, SimpleProductOfferingType ownerProductType) {
        //Safe check not to fire creatable calls for all the relationships
        if(namedRelationship.isRelatedToRelationship()) {
            if(!namedRelationship.isViewable() && !namedRelationship.hasStencilSet()) {
                List<ProductIdentifier> productIdentifiers = pmrHelper.creatableCandidates(ownerIdentifier,
                        namedRelationship.getRelationshipName(), namedRelationship.getLinkedIdentifiers(), ownerProductType);
                return !productIdentifiers.isEmpty();
            }
        }
        return false;
    }


    private boolean rulesSatisfied(CIFAsset cifAsset, SalesRelationship namedRelationship, ProductOffering creatableOffering) {
        final String relationshipName = namedRelationship.getRelationshipName().value();
        final List<StructuredRule> applicableRules =  newArrayList(Iterables.filter(cifAsset.getProductRules(), new Predicate<StructuredRule>() {
            @Override
            public boolean apply(StructuredRule rule) {
                if (!rule.isFilterRule()) {
                    return false;
                }
                RuleFilter filterRule = (RuleFilter) rule;
                return FilterCandidateProducts.equals(filterRule.getExecutionPoint()) &&
                       relationshipName.equals(filterRule.getRelationshipName());
            }
        }));

        for (StructuredRule rule : applicableRules) {
            RuleFilter filterRule = (RuleFilter)rule;

            final CIFAssetEvaluator baseAssetEvaluator = new CIFAssetEvaluator(cifAsset, cifAssetOrchestrator, evaluatorFactory);
            final ProductOfferingEvaluator offeringEvaluator = new ProductOfferingEvaluator(creatableOffering);

            List<ContextualEvaluatorMap> evaluators = ContextualEvaluatorMap.defaultEvaluator(baseAssetEvaluator);
            evaluators.add(new ContextualEvaluatorMap(relationshipName, offeringEvaluator));

            if(filterRule.getSatisfaction(evaluators, relationshipName)!= ValidationErrorType.Satisfied){
                return false;
            }
        }
        return true;
    }

    public List<CIFAssetCreatableCandidate> getAutoCreatableCandidates(CIFAsset cifAsset, ProductIdentifier ownerIdentifier, List<SalesRelationship> namedRelationships, int defaultCardinality, SimpleProductOfferingType ownerProductType) {
        List<CIFAssetCreatableCandidate> candidates = new ArrayList<CIFAssetCreatableCandidate>();
        for (SalesRelationship namedRelationship : namedRelationships) {
            final List<CIFAssetRelationship> relationships = cifAsset.getRelationships(namedRelationship.getRelationshipName().value());
            if (!namedRelationship.isTargetAFeatureSpecification()
                && defaultCardinality > 0
                &&  defaultCardinality > relationships.size()
                && (Child == namedRelationship.getType() || isAutoCreatableCandidate(ownerIdentifier, namedRelationship, ownerProductType))){

                final ProductIdentifier productIdentifier = namedRelationship.getRelatedProductIdentifier().getProductIdentifier();
                final StencilId stencilId = namedRelationship.getRelatedProductIdentifier().getStencilId();
                final String productId = productIdentifier.getProductId();
                final String stencilCode = stencilId.getCCode().getValue();

                final ProductOffering creatableOffering = pmrHelper.getProductOffering(productId, stencilCode);

                if(creatableOffering.isNonExpiredOffering() && rulesSatisfied(cifAsset, namedRelationship, creatableOffering)) {
                    CIFAssetCreatableCandidate creatableCandidate = new CIFAssetCreatableCandidate(productId,
                                                                                                   productIdentifier.getVersionNumber(),
                                                                                                   stencilCode,
                                                                                                   stencilId.getProductName().getValue(),
                                                                                                   creatableOffering.getVisibleInSummaryText(true),
                                                                                                   creatableOffering.requiresSiteSelection());
                    candidates.add(creatableCandidate);
                }
            }
        }
        return candidates;
    }

    //TODO: Refactor this to have a common method with creatable candidate
    private boolean isAutoCreatableCandidate(ProductIdentifier ownerIdentifier, SalesRelationship namedRelationship, SimpleProductOfferingType ownerProductType) {
        //Safe check not to fire creatable calls for all the relationships
        if(namedRelationship.isRelatedToRelationship()) {
            if(!namedRelationship.isViewable() && !namedRelationship.hasStencilSet() || NewOnly == namedRelationship.getResolvesToValue()) {
                List<ProductIdentifier> productIdentifiers = pmrHelper.creatableCandidates(ownerIdentifier,
                        namedRelationship.getRelationshipName(), namedRelationship.getLinkedIdentifiers(), ownerProductType);
                return !productIdentifiers.isEmpty();
            }
        }
        return false;
    }


}
