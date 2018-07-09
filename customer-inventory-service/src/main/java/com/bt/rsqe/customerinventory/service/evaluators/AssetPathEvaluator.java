package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.filters.CIFAssetFilter;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class AssetPathEvaluator {
    private CIFAsset initialAsset;
    private CIFAssetOrchestrator cifAssetOrchestrator;
    private CIFAssetCharacteristicEvaluatorFactory evaluatorFactory;

    public AssetPathEvaluator(CIFAsset initialAsset, CIFAssetOrchestrator cifAssetOrchestrator, CIFAssetCharacteristicEvaluatorFactory evaluatorFactory) {
        this.initialAsset = initialAsset;
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.evaluatorFactory = evaluatorFactory;
    }

    public List<CIFAsset> getMatchingAssets(List<String> expressionPath, List<CIFAsset> startAssets,
                                            String contextQuoteOptionId, List<CIFAssetExtension> leafExtensions) {
        if (expressionPath.isEmpty()) {
            return startAssets;
        }

        List<CIFAsset> matchingAssets = newArrayList();

        EvaluatorPathExpression path = new EvaluatorPathExpression(expressionPath.get(0), initialAsset, cifAssetOrchestrator, evaluatorFactory);
        expressionPath.remove(0);

        for (CIFAsset cifAsset : startAssets) {
            List<CIFAssetExtension> pathExtensions;
            if(expressionPath.isEmpty()){
                pathExtensions = leafExtensions;
            }else{
                pathExtensions = EvaluatorPathExpression.getExtensions(expressionPath);
            }
            final List<CIFAsset> allMatchingAssets = path.getMatchingAssets(cifAsset, contextQuoteOptionId, pathExtensions);
            matchingAssets.addAll(CIFAssetFilter.filterByLiveAssetStatus(cifAsset).filter(allMatchingAssets));
        }

        return getMatchingAssets(expressionPath, matchingAssets, contextQuoteOptionId, leafExtensions);
    }
}
