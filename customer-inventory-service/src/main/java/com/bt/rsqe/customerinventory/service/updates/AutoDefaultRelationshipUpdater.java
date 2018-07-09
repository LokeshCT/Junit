package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetChoosableCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCreatableCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingRelationshipDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.domain.updates.AutoDefaultRelationshipsRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.AutoDefaultRelationshipsResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ChooseRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRequestSource;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCandidateEvaluator;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetEvaluator;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.extensions.RuleDefaulting;
import com.bt.rsqe.domain.product.extensions.StructuredRule;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.product.parameters.ResolvesTo;
import com.bt.rsqe.expressionevaluator.ContextualEvaluator;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import com.bt.rsqe.expressionevaluator.SingleValueEvaluator;
import com.bt.rsqe.expressionevaluator.expr.SyntaxException;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.google.common.collect.Lists.newArrayList;

public class AutoDefaultRelationshipUpdater implements CIFAssetUpdater<AutoDefaultRelationshipsRequest, AutoDefaultRelationshipsResponse> {
    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private final CIFAssetCharacteristicEvaluatorFactory evaluatorFactory;
    private PmrHelper pmrHelper;

    public AutoDefaultRelationshipUpdater(CIFAssetOrchestrator cifAssetOrchestrator, CIFAssetCharacteristicEvaluatorFactory evaluatorFactory, PmrHelper pmrHelper) {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.evaluatorFactory = evaluatorFactory;
        this.pmrHelper = pmrHelper;
    }

    @Override    //TODO: refactor to use cardinalities from each and every relationships as stencil can have its own cardinalities.
    public AutoDefaultRelationshipsResponse performUpdate(AutoDefaultRelationshipsRequest request) {

        ProductOffering productOffering = pmrHelper.getProductOffering(request.getProductCode());

        if(productOffering.getSalesRelationships().isEmpty()) {
            return new AutoDefaultRelationshipsResponse(request, Collections.<CIFAssetUpdateRequest>emptyList());

        }

        CIFAsset baseAsset = cifAssetOrchestrator.getAsset(new CIFAssetKey(request.getAssetKey(), newArrayList(ProductOfferingRelationshipDetail,
                Relationships,
                RelationshipCardinality,
                AutoCreatableCandidates,
                AutoChoosableCandidates)));
        List<CIFAssetUpdateRequest> dependantRequests = newArrayList();

        List<CIFAssetOfferingRelationshipDetail> relationshipDefinitions = baseAsset.getRelationshipDefinitions();

        for (CIFAssetOfferingRelationshipDetail relationshipDetail : relationshipDefinitions) {
            final String relationshipName = relationshipDetail.getRelationshipName();
            final List<CIFAssetRelationship> relationships = baseAsset.getRelationships(relationshipName);
            List<CIFAssetCandidate> candidatesForThisRelationship = new ArrayList<CIFAssetCandidate>();


            if (relationshipDetail.isAutoAddable() && relationshipDetail.getDefaultCardinality().getCardinality() > relationships.size()) {
                if (relationshipDetail.getResolvesTo() != ResolvesTo.ExistingOnly) {
                    candidatesForThisRelationship.addAll(relationshipDetail.getAutoCreatableCandidates());
                }

                if (relationshipDetail.getResolvesTo() != ResolvesTo.NewOnly && RelationshipType.RelatedTo.equals(relationshipDetail.getRelationshipType())) {
                    candidatesForThisRelationship.addAll(relationshipDetail.getAutoChoosableCandidates());
                }

                candidatesForThisRelationship = filterCandidatesByDefaultingRules(baseAsset, candidatesForThisRelationship, relationshipName);

                if (candidatesForThisRelationship.size() == 1) {
                    final CIFAssetUpdateRequest candidateRequest = candidateToRequest(candidatesForThisRelationship.get(0), request,
                                                                                      relationshipName);
                    for (int i = 0; i < relationshipDetail.getDefaultCardinality().getCardinality(); i++) {
                        dependantRequests.add(candidateRequest);
                    }
                }
            }
        }

        return new AutoDefaultRelationshipsResponse(request, dependantRequests);
    }

    private List<CIFAssetCandidate> filterCandidatesByDefaultingRules(final CIFAsset cifAsset,
                                                                      List<CIFAssetCandidate> candidatesForThisRelationship,
                                                                      final String relationshipName) {
        final List<StructuredRule> applicableRules = newArrayList(Iterables.filter(cifAsset.getProductRules(), new Predicate<StructuredRule>() {
            @Override
            public boolean apply(StructuredRule rule) {
                if (!rule.isDefaultingRule()) {
                    return false;
                }
                RuleDefaulting defaultingRule = (RuleDefaulting) rule;
                return relationshipName.equals(defaultingRule.getRelationshipName());
            }
        }));

        return newArrayList(Iterables.filter(candidatesForThisRelationship, new Predicate<CIFAssetCandidate>() {
            @Override
            public boolean apply(CIFAssetCandidate candidate) {
                for (StructuredRule rule : applicableRules) {
                    RuleDefaulting defaultingRule = (RuleDefaulting) rule;

                    final CIFAssetEvaluator baseAssetEvaluator = new CIFAssetEvaluator(cifAsset, cifAssetOrchestrator, evaluatorFactory);
                    final CIFAssetCandidateEvaluator candidateEvaluator = new CIFAssetCandidateEvaluator(candidate);

                    CIFAsset grandParentAsset;
                    ContextualEvaluator grandParentEvaluator = new SingleValueEvaluator("");
                    SingleValueEvaluator relationNameEvaluator = new SingleValueEvaluator("");
                    try {
                        grandParentAsset = cifAssetOrchestrator.getParentAsset(new CIFAssetKey(cifAsset.getAssetKey(),
                                                                                               newArrayList(Relationships)));
                        grandParentEvaluator = new CIFAssetEvaluator(grandParentAsset, cifAssetOrchestrator, evaluatorFactory);
                        for (CIFAssetRelationship cifAssetRelationship : grandParentAsset.getRelationships()) {
                            if (cifAssetRelationship.getRelated().getAssetKey().equals(cifAsset.getAssetKey())) {
                                relationNameEvaluator = new SingleValueEvaluator(cifAssetRelationship.getRelationshipName());
                            }
                        }
                    } catch (NoResultException nre) {
                        // Happy to ignore this - we've already defaulted the evaluators to return empty string
                    }

                    List<ContextualEvaluatorMap> evaluators = new ArrayList<ContextualEvaluatorMap>();
                    evaluators.add(new ContextualEvaluatorMap(RuleDefaulting.CANDIDATE_IDENTIFIER, candidateEvaluator));
                    evaluators.add(new ContextualEvaluatorMap(RuleDefaulting.PARENT_IDENTIFIER, baseAssetEvaluator));
                    evaluators.add(new ContextualEvaluatorMap(RuleDefaulting.GRAND_PARENT_IDENTIFIER, grandParentEvaluator));
                    evaluators.add(new ContextualEvaluatorMap(RuleDefaulting.GRAND_PARENT_RELATION_NAME_IDENTIFIER, relationNameEvaluator));

                    try {
                        return defaultingRule.evaluateCandidate(evaluators);
                    } catch (SyntaxException e) {
                        return false;
                    }
                }
                return true;
            }
        }));
    }

    private CIFAssetUpdateRequest candidateToRequest(CIFAssetCandidate candidate, AutoDefaultRelationshipsRequest request,
                                                     String relationshipName) {
        if (candidate instanceof CIFAssetCreatableCandidate) {
            CIFAssetCreatableCandidate creatableCandidate = (CIFAssetCreatableCandidate) candidate;
            return new CreateRelationshipRequest(UUID.randomUUID().toString(), request.getAssetKey(), relationshipName, creatableCandidate.getProductCode(),
                                                 creatableCandidate.getStencilCode(), "", "", request.getLineItemId(),
                                                 request.getLockVersion());
        } else {
            CIFAssetChoosableCandidate choosableCandidate = (CIFAssetChoosableCandidate) candidate;
            return new ChooseRelationshipRequest(request.getAssetKey(), choosableCandidate.getChoosableAssetKey(), relationshipName,
                                                 request.getLineItemId(), request.getLockVersion(), UpdateRequestSource.AutoDefault);
        }
    }
}
