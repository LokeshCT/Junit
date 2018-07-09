package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public enum NonCharacteristicEvaluableExpressions {
    AssetUniqueId,
    SubscriberId,
    AssetId,
    ProductCategoryCode,
    ProductCategoryName,
    ProductCode,
    SalesChannel,
    SalesChannelType,
    CustomerName,
    CountryISOCode,
    CountryName,
    CityName,
    SiteName,
    AssetSiteId,
    FixedAction,
    Action(newArrayList(CIFAssetExtension.AsIsAsset)),
    BillingId,
    LegacyIdentifier(newArrayList(CIFAssetExtension.ProductOfferingDetail)),
    MoveType,
    Proposition(newArrayList(CIFAssetExtension.ProductOfferingDetail)),
    ProjectId,
    CustomerId,
    InstallationHours,
    ProductName(newArrayList(CIFAssetExtension.ProductOfferingDetail)),
    AssetType,
    FeatureSpecProviderCode,
    MyRelationshipWithParent,
    ContractResignFlag,
    ContextAction,
    DeviceRoleAttr;

    private final List<CIFAssetExtension> requiredCIFAssetExtensions;

    NonCharacteristicEvaluableExpressions(List<CIFAssetExtension> cifAssetExtensions) {
        requiredCIFAssetExtensions = cifAssetExtensions;
    }

    NonCharacteristicEvaluableExpressions() {
        this(new ArrayList<CIFAssetExtension>());
    }

    public List<CIFAssetExtension> getRequiredCIFAssetExtensions() {
        return requiredCIFAssetExtensions;
    }

    public static boolean containsExpression(String expression) {
        for(NonCharacteristicEvaluableExpressions nonCharacteristicEvaluableExpression : values()) {
            if (nonCharacteristicEvaluableExpression.name().equalsIgnoreCase(expression)) {
                return true;
            }
        }
        return false;
    }
}
