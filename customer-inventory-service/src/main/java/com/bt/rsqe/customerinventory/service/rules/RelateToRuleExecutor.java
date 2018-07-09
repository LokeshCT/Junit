package com.bt.rsqe.customerinventory.service.rules;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ChooseRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ReprovideAssetRequest;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.evaluators.AssetPathEvaluator;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetEvaluator;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.product.Association;
import com.bt.rsqe.domain.product.DirectAssociation;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.extensions.Expression;
import com.bt.rsqe.domain.product.extensions.RuleRelateTo;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRequestSource.*;
import static com.bt.rsqe.domain.product.Association.AssociationType.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.newHashSet;

public class RelateToRuleExecutor {
    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private final CIFAssetCharacteristicEvaluatorFactory evaluatorFactory;
    private PmrHelper pmrHelper;

    public RelateToRuleExecutor(CIFAssetOrchestrator cifAssetOrchestrator, CIFAssetCharacteristicEvaluatorFactory evaluatorFactory, PmrHelper pmrHelper) {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.evaluatorFactory = evaluatorFactory;
        this.pmrHelper = pmrHelper;
    }

    public List<CIFAssetUpdateRequest> execute(RuleRelateTo ruleRelateTo, CIFAsset initialAsset) {
        List<CIFAssetUpdateRequest> responseUpdates = new ArrayList<CIFAssetUpdateRequest>();
        CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(initialAsset, cifAssetOrchestrator, evaluatorFactory);
        final ContextualEvaluatorMap contextualEvaluatorMap = new ContextualEvaluatorMap("", cifAssetEvaluator);
        if(ruleRelateTo.isFilterSatisfied(newArrayList(contextualEvaluatorMap))) {
            AssetPathEvaluator assetPathEvaluator = new AssetPathEvaluator(initialAsset, cifAssetOrchestrator, evaluatorFactory);
            final List<CIFAsset> matchingAssets = assetPathEvaluator.getMatchingAssets(Expression.splitExpressionText(ruleRelateTo.getPathToRelationshipOwner()),
                                                                                       newArrayList(initialAsset),
                                                                                       initialAsset.getQuoteOptionId(),
                                                                                       new ArrayList<CIFAssetExtension>());

            for (CIFAsset matchingAsset : matchingAssets) {
                responseUpdates.add(new ChooseRelationshipRequest(matchingAsset.getAssetKey(), initialAsset.getAssetKey(), ruleRelateTo.getRelationshipName(), "", 0, RelateTo));
                responseUpdates.addAll(constructReloadRequest(matchingAsset, initialAsset));
            }

            if (ruleRelateTo.isReprovide()) {
                for (CIFAsset matchingAsset : matchingAssets) {
                    responseUpdates.add(new ReprovideAssetRequest(initialAsset.getAssetKey(), matchingAsset.getAssetKey(), "", 0));
                }
            }
        }

        return responseUpdates;
    }

    private Collection<? extends CIFAssetUpdateRequest> constructReloadRequest(CIFAsset matchingAsset, CIFAsset initialAsset) {
        Set<CharacteristicReloadRequest> reloadRequests = newHashSet();
        ProductOffering productOffering = pmrHelper.getProductOffering(initialAsset.getProductCode());
        Set<Association> directAssociations = productOffering.getDirectAssociations();
        for (Association directAssociation : directAssociations) {
            if(ATTRIBUTE_SOURCE == directAssociation.getAssociationType()) {
                DirectAssociation association = (DirectAssociation) directAssociation;
                if(matchingAsset.getProductCode().equals(association.getContributedToOffering().getProductId())) {
                    reloadRequests.add(new CharacteristicReloadRequest(matchingAsset.getAssetKey(), association.getLinkName(), 1));
                }
            }
        }
        return reloadRequests;
    }
}
