package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.filters.CIFAssetFilter;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.product.extensions.Expression;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.expressionevaluator.expr.Scanner;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.customerinventory.service.evaluators.EvaluatorPathExpressionType.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.*;

public class EvaluatorPathExpression {
    private EvaluatorPathExpressionType type;
    private EvaluatorPathFilter filter;
    private String path;
    private CIFAssetOrchestrator cifAssetOrchestrator;
    private CIFAssetCharacteristicEvaluatorFactory evaluatorFactory;
    private CIFAsset initialAsset;

    public EvaluatorPathExpression(String fullPath, CIFAsset initialAsset, CIFAssetOrchestrator cifAssetOrchestrator, CIFAssetCharacteristicEvaluatorFactory evaluatorFactory) {
        this.evaluatorFactory = evaluatorFactory;
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        splitOutPathAndFilter(fullPath);
        this.type = EvaluatorPathExpressionType.fromPath(path);
        this.initialAsset = initialAsset;
    }

    private void splitOutPathAndFilter(String fullPath) {
        if(fullPath.contains("[")){
            String[] split = fullPath.split("\\[");
            path = split[0];
            filter = new EvaluatorPathFilter(split[1].substring(0, split[1].length()-1), cifAssetOrchestrator, evaluatorFactory);
        }else{
            path = fullPath;
            filter = EvaluatorPathFilter.NoFilter;
        }
    }

    public List<CIFAsset> getMatchingAssets(CIFAsset cifAsset, String contextQuoteOptionId, List<CIFAssetExtension> pathExtensions) {
        List<CIFAsset> matchingAssets = newArrayList();

        switch(type) {
            case Parent:
                if (!cifAsset.isStub()) {
                    CIFAsset parentCifAsset = cifAssetOrchestrator.getParentAsset(new CIFAssetKey(cifAsset.getAssetKey(), pathExtensions));
                    if (parentCifAsset != null) {
                        matchingAssets.add(parentCifAsset);
                    }
                }
                break;
            case Owner:
            case AssetOwnerRelations:
                Optional<String> productCodeOptional = getProductCode(filter);
                Optional<String> quoteStringOptional = Optional.of(contextQuoteOptionId);
                List<AssetVersionStatus> statusList = newArrayList(AssetVersionStatus.PROVISIONING, AssetVersionStatus.IN_SERVICE);
                List<CIFAsset> allOwnerAssets = cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(cifAsset.getAssetKey(), pathExtensions), statusList, productCodeOptional, quoteStringOptional);
                matchingAssets.addAll(getFilteredAssets(newArrayList(allOwnerAssets), contextQuoteOptionId));
                break;
            case RelationshipPathType:
                for (CIFAssetRelationship relationship : cifAsset.getRelationships()) {
                    if (relationship.getRelationshipName().equalsIgnoreCase(path)) {
                        CIFAsset relatedAsset = relationship.getRelated();
                        if(!relatedAsset.hasExtensions(pathExtensions)){
                            relatedAsset = cifAssetOrchestrator.getAsset(new CIFAssetKey(relatedAsset.getAssetKey(), pathExtensions));
                        }
                        matchingAssets.add(relatedAsset);
                    }
                }
                break;
        }

        return filter.execute(matchingAssets, initialAsset);
    }

    private Optional<String> getProductCode(EvaluatorPathFilter filter) {
        try {
            Expression expression = filter.getFilterExpression().getExpression();
            if(expression != null && isNotEmpty(expression.getExpressionText())) {
                Scanner scanner = new Scanner(expression.getExpressionText(), "=");
                final List<String> tokens = scanner.getTokens();   ///TODO: Use regular exprsession to get product code
               if(!tokens.isEmpty() && tokens.size() == 3) {
                   final String productCodeIdentifier = tokens.get(0);
                   final String equalsOperator = tokens.get(1);
                   final String productCode = tokens.get(2);
                   if("ProductCode".equals(productCodeIdentifier) && "=".equals(equalsOperator)&& isNotEmpty(productCode)) {
                       return Optional.of(tokens.get(2));
                   }
               }

            }
        } catch (Throwable e) {
            return Optional.absent();
        }
        return Optional.absent();
    }

    private List<CIFAsset> getFilteredAssets(List<CIFAsset> allOwnerAssets, String contextQuoteOptionId) {
        final List<CIFAsset> ownerAssets = newArrayList();
        List<CIFAsset> filteredOwnerAssets = CIFAssetFilter.filterByQuoteOptionIdOrInServiceProvisioning(contextQuoteOptionId, AssetVersionStatus.PROVISIONING, AssetVersionStatus.IN_SERVICE).filter(allOwnerAssets);
        Map<String, List<CIFAsset>> groups = groupAssetsByAssetId(filteredOwnerAssets);

        for (List<CIFAsset> cifAssets : groups.values()) {
            CIFAsset sameQuoteOptionAsset = null;
            CIFAsset provisioningAsset = null;
            CIFAsset inServiceAsset = null;

            for (CIFAsset asset : cifAssets) {
                if (asset.getQuoteOptionId().equals(contextQuoteOptionId)) {
                    sameQuoteOptionAsset = asset;
                    break;
                } else if (asset.getAssetVersionStatus().equals(AssetVersionStatus.PROVISIONING)) {
                    provisioningAsset = asset;
                } else { /*If the asset is neither in the same quote option with context quote option nor its asset version status is provisioning,
                           given the method of filterByQuoteOptionIdOrInServiceProvisioning, this asset must have the asset version status to be IN_SERVICE */
                    inServiceAsset = asset;
                }
            }

            if (sameQuoteOptionAsset != null) {
                ownerAssets.add(sameQuoteOptionAsset);
            } else if (provisioningAsset != null) {
                ownerAssets.add(provisioningAsset);
            } else { /*If the asset is neither in sameQuoteOptionAsset nor in provisioningAsset,
                       given the method of filterByQuoteOptionIdOrInServiceProvisioning, this asset must be in inServiceAsset*/
                ownerAssets.add(inServiceAsset);
            }
        }
        return ownerAssets;
    }

    private Map<String, List<CIFAsset>> groupAssetsByAssetId(List<CIFAsset> assets) {
        Map<String, List<CIFAsset>> groups = new HashMap<String, List<CIFAsset>>();
        for (CIFAsset asset : assets) {
            String assetId = asset.getAssetKey().getAssetId();
            if (groups.containsKey(assetId)) {
                groups.get(assetId).add(asset);
            } else {
                groups.put(assetId, newArrayList(asset));
            }
        }
        return groups;
    }

    public static List<CIFAssetExtension> getExtensions(List<String> expressionPath) {
        if(expressionPath.size()>0 &&
           RelationshipPathType.equals(EvaluatorPathExpressionType.fromPath(expressionPath.get(0)))){
            return newArrayList(Relationships);
        }
        return new ArrayList<CIFAssetExtension>();
    }
}
