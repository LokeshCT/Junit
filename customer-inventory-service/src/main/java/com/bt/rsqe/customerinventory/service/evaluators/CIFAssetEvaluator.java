package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.expressionevaluator.ContextualEvaluator;

import java.util.List;

import static com.bt.rsqe.expressionevaluator.ContextualEvaluatorTypes.*;
import static com.google.common.collect.Lists.*;

public class CIFAssetEvaluator extends ContextualEvaluator {
    private final AssetKey assetKey;
    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private final CIFAssetCharacteristicEvaluatorFactory evaluatorFactory;
    private CIFAsset initialAsset;

    public CIFAssetEvaluator(AssetKey assetKey,
                             CIFAssetOrchestrator cifAssetOrchestrator,
                             CIFAssetCharacteristicEvaluatorFactory evaluatorFactory) {
        this.assetKey = assetKey;
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.evaluatorFactory = evaluatorFactory;
    }

    public CIFAssetEvaluator(CIFAsset asset,
                             CIFAssetOrchestrator cifAssetOrchestrator,
                             CIFAssetCharacteristicEvaluatorFactory evaluatorFactory) {
        this.assetKey = asset.getAssetKey();
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.evaluatorFactory = evaluatorFactory;
        this.initialAsset = asset;
    }

    @Override
    protected List<Object> getValues(List<String> expressionPath, String expression) {
        CIFAssetCharacteristicEvaluator characteristicEvaluator = evaluatorFactory.getCharacteristicEvaluator(expression);
        List<CIFAssetExtension> leafExtensions = characteristicEvaluator.getCIFAssetExtensions();
        List<CIFAssetExtension> startingExtensions = EvaluatorPathExpression.getExtensions(expressionPath);

        List<String> expressionPathCopy = newArrayList(expressionPath);
        List<CIFAsset> startAssets = getStartAsset(expressionPathCopy, startingExtensions, leafExtensions);
        String contextQuoteOptionId = startAssets.get(0).getQuoteOptionId();
        AssetPathEvaluator assetPathEvaluator = new AssetPathEvaluator(initialAsset, cifAssetOrchestrator, evaluatorFactory);
        List<CIFAsset> matchingAssets = assetPathEvaluator.getMatchingAssets(expressionPathCopy, startAssets, contextQuoteOptionId, leafExtensions);

        List<Object> characteristicValues = newArrayList();
        for (CIFAsset matchingAsset : matchingAssets) {
            characteristicValues.add(characteristicEvaluator.evaluate(matchingAsset));
        }
        return characteristicValues;
    }

    private List<CIFAsset> getStartAsset(List<String> expressionPath, List<CIFAssetExtension> nonLeafAssetExtensions, List<CIFAssetExtension> leafAssetExtensions) {
        if (expressionPath.isEmpty()) {
            fetchOrExtendInitialAsset(leafAssetExtensions);
        }else{
            fetchOrExtendInitialAsset(nonLeafAssetExtensions);
        }
        return newArrayList(initialAsset);
    }

    private void fetchOrExtendInitialAsset(List<CIFAssetExtension> extensions) {
        if (initialAsset == null || !initialAsset.hasExtensions(extensions)) {
            if(initialAsset != null) {
                initialAsset = cifAssetOrchestrator.extendAsset(initialAsset, extensions);
            }else{
                initialAsset = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, extensions));
            }
        }
    }

    @Override
    protected String getBaseConversion() {
        return DOUBLE_EVALUATOR.getName();
    }
}
