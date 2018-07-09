package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetChoosableCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCreatableCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingRelationshipDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationshipCardinality;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationshipCardinalityType;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetEvaluator;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.service.providers.ChoosableCandidateProvider;
import com.bt.rsqe.customerinventory.service.providers.CreatableCandidateProvider;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.extensions.Expression;
import com.bt.rsqe.domain.product.extensions.RuleDefaulting;
import com.bt.rsqe.domain.product.extensions.StructuredRule;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.product.parameters.ResolvesTo;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import com.bt.rsqe.expressionevaluator.ExpressionEvaluator;
import com.bt.rsqe.expressionevaluator.expr.SyntaxException;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationshipCardinality.NO_CARDINALITY;
import static com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationshipCardinalityType.*;
import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.isEmpty;

public class SalesRelationshipExtender {
    private final ChoosableCandidateProvider choosableCandidateProvider;
    private final CreatableCandidateProvider creatableCandidateProvider;
    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private final CIFAssetCharacteristicEvaluatorFactory cifAssetCharacteristicEvaluatorFactory;
    private final PmrHelper pmrHelper;
    private Logger logger = LogFactory.createDefaultLogger(Logger.class);

    public SalesRelationshipExtender(CreatableCandidateProvider creatableCandidateProvider,
                                     ChoosableCandidateProvider choosableCandidateProvider,
                                     CIFAssetOrchestrator cifAssetOrchestrator,
                                     CIFAssetCharacteristicEvaluatorFactory cifAssetCharacteristicEvaluatorFactory,
                                     PmrHelper pmrHelper) {
        this.creatableCandidateProvider = creatableCandidateProvider;
        this.choosableCandidateProvider = choosableCandidateProvider;
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.cifAssetCharacteristicEvaluatorFactory = cifAssetCharacteristicEvaluatorFactory;
        this.pmrHelper = pmrHelper;
    }

    public void extend(List<CIFAssetExtension> cifAssetExtensions, CIFAsset cifAsset, ProductOffering productOffering) {
        if(ProductOfferingRelationshipDetail.isInList(cifAssetExtensions)) {
            final List<CIFAssetOfferingRelationshipDetail> relationshipDefinitions = new ArrayList<CIFAssetOfferingRelationshipDetail>();
            final Map<RelationshipName, List<SalesRelationship>> salesRelationships = mergeSalesRelationshipsByName(productOffering.getSalesRelationships());

            for (RelationshipName relationshipName : salesRelationships.keySet()) {
                final List<SalesRelationship> namedRelationships = salesRelationships.get(relationshipName);

                // The following information will be the same for all relationships with the same name so we can use get(0)
                final SalesRelationship firstSalesRelationship = namedRelationships.get(0);
                final boolean targetAFeatureSpecification = firstSalesRelationship.isTargetAFeatureSpecification();
                final RelationshipType relationshipType = firstSalesRelationship.getType();
                final String productId = firstSalesRelationship.getRootProductIdentifier().getProductId();
                final String stencilId = firstSalesRelationship.getRelatedProductIdentifier().getStencilId().getCCode().getValue();
                final String groupName = firstSalesRelationship.getGroup().getName().getValue();

                List<String> linkedIdentifiers = new ArrayList<String>();
                if (targetAFeatureSpecification) {
                    for (SalesRelationship salesRelationship : namedRelationships) {
                        for (ProductIdentifier productIdentifier : salesRelationship.getLinkedIdentifiers()) {
                            linkedIdentifiers.add(productIdentifier.getProductId());
                        }
                    }
                    // This is to ensure equals works as expected on the cifAssetRelationship - it expects lists to be ordered equally.
                    Collections.sort(linkedIdentifiers);
                }

                CIFAssetRelationshipCardinality minCardinality = NO_CARDINALITY;
                CIFAssetRelationshipCardinality maxCardinality = NO_CARDINALITY;
                CIFAssetRelationshipCardinality defaultCardinality = NO_CARDINALITY;

                if (RelationshipCardinality.isInList(cifAssetExtensions)) {
                    minCardinality = getCardinality(cifAsset, firstSalesRelationship, Minimum);
                    maxCardinality = getCardinality(cifAsset, firstSalesRelationship, Maximum);
                    defaultCardinality = getCardinality(cifAsset, firstSalesRelationship, Default);
                }

                boolean stencilUpdatable = false;
                boolean stencillableRelatedProduct = false;

                if(!targetAFeatureSpecification) {
                    ProductOffering pmrHelperProductOffering = pmrHelper.getProductOffering(productId);
                    if (relationshipType.equals(RelationshipType.Child)) {
                        stencilUpdatable = pmrHelperProductOffering.isStencilUpdatable() &&
                                           maxCardinality.getCardinality() == 1;
                    }

                    if(pmrHelperProductOffering.isStencilable()) {
                        stencillableRelatedProduct = true;
                    }
                }


                ResolvesTo resolvesToValue = firstSalesRelationship.getResolvesToValue();

                List<CIFAssetCreatableCandidate> autoCreatableCandidates = newArrayList();

                if (AutoCreatableCandidates.isInList(cifAssetExtensions)){  //TODO: Cardinality needs to be resolved for all relationships as it may differ for each stencil.
                    long creatableStartTime = System.currentTimeMillis();
                    List<SalesRelationship> filteredRelationships = newArrayList();

                    for (SalesRelationship salesRelationship : namedRelationships) {
                        CIFAssetRelationshipCardinality cifAssetRelationshipCardinality = getCardinality(cifAsset, salesRelationship, Default);
                        if(!CIFAssetRelationshipCardinality.NO_CARDINALITY.equals(cifAssetRelationshipCardinality) && cifAssetRelationshipCardinality.getCardinality() > 0) {
                            defaultCardinality = cifAssetRelationshipCardinality;
                            filteredRelationships.add(salesRelationship);
                        }
                    }

                    if(filteredRelationships.size() == 1 || hasDefaultingRules(cifAsset, relationshipName)) {  //If there are more candidates available for a relationship name , then its not eligible for auto add, so filtering up front
                                                                                                               //But if defaulting rule is available, then need to allow all relationships, because required relation will be filtered later .
                        autoCreatableCandidates = creatableCandidateProvider.getAutoCreatableCandidates(cifAsset, productOffering.getProductIdentifier(), filteredRelationships,
                                defaultCardinality.getCardinality(), productOffering.getSimpleProductOfferingType());
                    }
                    long creatableElapsedTime = System.currentTimeMillis() - creatableStartTime;
                    logger.creatableLoadTime(cifAsset.getAssetKey(), cifAsset.getProductCode(), relationshipName, creatableElapsedTime);
                }

                final CIFAssetOfferingRelationshipDetail offeringRelationshipDetail =
                        new CIFAssetOfferingRelationshipDetail(minCardinality, maxCardinality, defaultCardinality,
                                relationshipName.value(), relationshipType,
                                stencilId, productId, groupName,
                                linkedIdentifiers, stencilUpdatable, resolvesToValue, stencillableRelatedProduct);

                if(AutoCreatableCandidates.isInList(cifAssetExtensions) || !autoCreatableCandidates.isEmpty()) {
                    offeringRelationshipDetail.loadAutoCreatableCandidates(autoCreatableCandidates);
                }

                if(RelationshipCreatableCandidates.isInList(cifAssetExtensions)) {
                    offeringRelationshipDetail.loadCreatableCandidates(creatableCandidateProvider.getCreatableCandidates(cifAsset,
                            productOffering.getProductIdentifier(),
                            namedRelationships, productOffering.getSimpleProductOfferingType()));
                }

                if(RelationshipChoosableCandidates.isInList(cifAssetExtensions)) {
                    offeringRelationshipDetail.loadChoosableCandidates(choosableCandidateProvider.getChoosableCandidates(cifAsset, namedRelationships));
                }

                if(AutoChoosableCandidates.isInList(cifAssetExtensions)) {
                    long choosableStartTime = System.currentTimeMillis();
                    List<CIFAssetChoosableCandidate> autoChoosableCandidates = choosableCandidateProvider.getAutoChoosableCandidates(cifAsset,
                            namedRelationships,
                            stencillableRelatedProduct,
                            offeringRelationshipDetail.getDefaultCardinality());
                    offeringRelationshipDetail.loadAutoChoosableCandidates(autoChoosableCandidates);
                    long choosableElapsedTime = System.currentTimeMillis() - choosableStartTime;
                    logger.choosableLoadTime(cifAsset.getAssetKey(), cifAsset.getProductCode(), relationshipName, choosableElapsedTime);
                }

                relationshipDefinitions.add(offeringRelationshipDetail);
            }

            cifAsset.loadRelationshipDefinitions(relationshipDefinitions);
        }
    }

    private boolean hasDefaultingRules(CIFAsset cifAsset, final RelationshipName relationshipName) {
        return Iterables.tryFind(cifAsset.getProductRules(), new Predicate<StructuredRule>() {
            @Override
            public boolean apply(StructuredRule rule) {
                if (!rule.isDefaultingRule()) {
                    return false;
                }
                RuleDefaulting defaultingRule = (RuleDefaulting) rule;
                return relationshipName.value().equals(defaultingRule.getRelationshipName());
            }
        }).isPresent();
    }

    private CIFAssetRelationshipCardinality getCardinality(CIFAsset cifAsset, SalesRelationship salesRelationship, CIFAssetRelationshipCardinalityType type) {
        switch (type){
            case Maximum:
                return getCardinality(cifAsset, salesRelationship.getMaximumExpression(), salesRelationship.getMaximum());
            case Minimum:
                return getCardinality(cifAsset, salesRelationship.getMinimumExpression(), salesRelationship.getMinimum());
            case Default:
                return getCardinality(cifAsset, salesRelationship.getDefaultExpression(), salesRelationship.getDefault());
        }
        // Currently unreachable as no other values in the enum
        ///CLOVER:OFF
        return NO_CARDINALITY;
        ///CLOVER:ON
    }

    private CIFAssetRelationshipCardinality getCardinality(CIFAsset cifAsset, Expression cardinalityExpression,
                                                           int cardinalityIfNoExpression) {
        if(cardinalityExpression==null) {
            return new CIFAssetRelationshipCardinality(cardinalityIfNoExpression);
        }else{
            CIFAssetEvaluator cifAssetEvaluator = new CIFAssetEvaluator(cifAsset, cifAssetOrchestrator, cifAssetCharacteristicEvaluatorFactory);
            try {
                String cardinalityAsString = ExpressionEvaluator.evaluate(cardinalityExpression.getExpressionText(),
                                                                          ContextualEvaluatorMap.defaultEvaluator(cifAssetEvaluator)).toString();
                return isEmpty(cardinalityAsString) ? NO_CARDINALITY : new CIFAssetRelationshipCardinality((int)(Double.parseDouble(cardinalityAsString)));
            } catch (SyntaxException e) {
                logger.ruleCouldNotBeInterpreted(cardinalityExpression.getExpressionText(), e.getMessage());
                return NO_CARDINALITY;
            }
        }
    }


    private Map<RelationshipName, List<SalesRelationship>> mergeSalesRelationshipsByName(List<SalesRelationship> salesRelationships) {
        Map<RelationshipName, List<SalesRelationship>> mappedRelationships = new HashMap<RelationshipName, List<SalesRelationship>>();
        for (SalesRelationship salesRelationship : salesRelationships) {
            final RelationshipName relationshipName = salesRelationship.getRelationshipName();
            if(mappedRelationships.containsKey(relationshipName)) {
                mappedRelationships.get(relationshipName).add(salesRelationship);
            }else{
                mappedRelationships.put(relationshipName, newArrayList(salesRelationship));
            }
        }
        return mappedRelationships;
    }

    public interface Logger {
        @Log(level = LogLevel.INFO, format = "Rule Expression %s has failed with the following error: %s")
        void ruleCouldNotBeInterpreted(String ruleExpression, String errorMessage);

        @Log(level = LogLevel.DEBUG, format = "Creatable Candidate load - Asset - %s, product - %s, Relation - %s, Response Time - %s")
        void creatableLoadTime(AssetKey assetKey, String productCode, RelationshipName relationshipName, long creatableElapsedTime);

        @Log(level = LogLevel.DEBUG, format = "Choosable Candidate load - Asset - %s, product - %s, Relation - %s, Response Time - %s")
        void choosableLoadTime(AssetKey assetKey, String productCode, RelationshipName relationshipName, long choosableElapsedTime);
    }
}
