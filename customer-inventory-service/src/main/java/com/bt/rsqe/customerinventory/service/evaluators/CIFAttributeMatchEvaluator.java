package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.expressionevaluator.ContextualEvaluator;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorTypes;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.isNotNull;
import static com.google.common.collect.Lists.newArrayList;

public class CIFAttributeMatchEvaluator extends ContextualEvaluator {

    private CIFAsset cifAsset;
    private CIFAssetJPARepository cifAssetJPARepository;
    private CIFNewGroupIdCalculator cifNewGroupIdCalculator;

    public CIFAttributeMatchEvaluator(CIFAsset cifAsset, CIFAssetJPARepository cifAssetJPARepository, CIFNewGroupIdCalculator cifNewGroupIdCalculator) {
        this.cifAsset = cifAsset;
        this.cifAssetJPARepository = cifAssetJPARepository;
        this.cifNewGroupIdCalculator = cifNewGroupIdCalculator;
    }

    @Override
    protected List<Object> getValues(List<String> expressionPath, String expression) {
        List<Object> response = new ArrayList<Object>();

        final List<CIFAsset> ownerAssets = cifAssetJPARepository.getOwnerAssets(cifAsset.getAssetKey(), true);

        if (expression.startsWith("CountMySiblings")) {
            final ArrayList<String> siblings = extractExpression(expression);
            final String attrToMatch = siblings.get(0);
            final String attrToUpdate = siblings.get(1);

            if (!ownerAssets.isEmpty()) {
                final ArrayList<CIFAsset> matchingAssets = getMatchingAssets(ownerAssets.get(0), attrToMatch);
                updateMatchingAssets(matchingAssets, attrToUpdate);
                response.add(matchingAssets.size());
            }
        } else if (expression.startsWith("CustomGroupIdValue")) {
            final ArrayList<String> siblings = extractExpression(expression);
            final String attrToMatch = siblings.get(0);
            final String attrToGet = siblings.get(1);
            final String attrToUpdate = siblings.get(2);

            if (!ownerAssets.isEmpty()) {
                final ArrayList<CIFAsset> matchingAssets = getMatchingAssets(ownerAssets.get(0), attrToMatch);
                updateCustomGroupIdValue(matchingAssets, attrToUpdate, attrToGet);
                response.add(cifAsset.getCharacteristic(attrToUpdate).getValue());
            }
        }


        return response;
    }

    private void updateCustomGroupIdValue(ArrayList<CIFAsset> matchingAssets, String attrToUpdate, String attrToGet) {
        final String attrValue = cifAsset.getCharacteristic(attrToGet).getValue();
        cifNewGroupIdCalculator.calculate(matchingAssets, attrValue, attrToUpdate);

        for (CIFAsset asset : matchingAssets) {
            cifAssetJPARepository.saveAsset(asset);
            if (cifAsset.equals(asset)) {
                cifAsset.updateCharacteristicValue(attrToUpdate, asset.getCharacteristic(attrToUpdate).getValue());
            }
        }
    }

    private void updateMatchingAssets(ArrayList<CIFAsset> matchingAssets, String attrToUpdate) {
        String attrValue = String.valueOf(matchingAssets.size());

        for (CIFAsset related : matchingAssets) {
            if (!cifAsset.equals(related)) {
                related.updateCharacteristicValue(attrToUpdate, attrValue);
                cifAssetJPARepository.saveAsset(related);
            }
        }

        cifAsset.updateCharacteristicValue(attrToUpdate, attrValue);
    }

    private ArrayList<CIFAsset> getMatchingAssets(CIFAsset owner, final String attrToMatch) {
        final CIFAssetCharacteristic characteristic = cifAsset.getCharacteristic(attrToMatch);
        if (isNotNull(characteristic.getValue())) {
            final String attrVale = characteristic.getValue();
            final ArrayList<CIFAssetRelationship> cifAssetRelationships = newArrayList(Iterables.filter(owner.getRelationships(), new Predicate<CIFAssetRelationship>() {
                @Override
                public boolean apply(CIFAssetRelationship input) {
                    if (!input.getRelated().isCancelledAsset()) {
                        return input.getRelated().getProductCode().equals(cifAsset.getProductCode()) && attrVale.equals(input.getRelated().getCharacteristic(attrToMatch).getValue()) ;
                    }
                    return false;
                }
            }));

            return newArrayList(Iterables.transform(cifAssetRelationships, new Function<CIFAssetRelationship, CIFAsset>() {
                @Override
                public CIFAsset apply(CIFAssetRelationship input) {
                    return input.getRelated();
                }
            }));
        }
        return newArrayList();

    }


    private ArrayList<String> extractExpression(String expression) {
        final String[] siblings;
        final String expressions = expression.substring(expression.indexOf("[") + 1, expression.lastIndexOf("]"));
        siblings = expressions.split(",");
        return newArrayList(Arrays.asList(siblings));
    }

    @Override
    protected String getBaseConversion() {
        return ContextualEvaluatorTypes.DOUBLE_EVALUATOR.getName();
    }
}
