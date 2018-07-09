package com.bt.rsqe.customerinventory.service.providers;

import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerinventory.service.AttributeSorter;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAuxiliaryAttribute;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetError;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetExternalIdentifier;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetPriceLine;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetProjectedUsage;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.repository.UniqueIdJPARepository;
import com.bt.rsqe.domain.product.AssetProcessType;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.PricingCaveat;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.enums.AssetType;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.CIFAssetUtility.*;
import static com.bt.rsqe.domain.product.AttributeOwner.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

public class CIFAssetCreator {

    private final PmrHelper pmrHelper;
    private final UniqueIdJPARepository uniqueIdJPARepository;
    private AttributeSorter attributeSorter;
    public static final Predicate<Attribute> PRODUCT_ATTRIBUTE_PREDICATE = new Predicate<Attribute>() {
        @Override
        public boolean apply(Attribute input) {
            return isNull(input.getAttributeOwner()) || Offering.equals(input.getAttributeOwner());
        }
    };

    public CIFAssetCreator(PmrHelper pmrHelper, UniqueIdJPARepository uniqueIdJPARepository, AttributeSorter attributeSorter) {
        this.pmrHelper = pmrHelper;
        this.uniqueIdJPARepository = uniqueIdJPARepository;
        this.attributeSorter = attributeSorter;
    }

    public CIFAsset createAsset(String productCode, String stencilCode, String lineItemId, String siteId, String contractTerm,
                                String customerId, String contractId, String projectId, String quoteOptionId, String alternateCity, ProductCategoryCode productCategoryCode,
                                String slaId, String magId, String ssvId,String contractResignStatus) {
        final ProductOffering productOffering = pmrHelper.getProductOffering(productCode, stencilCode);

        return new CIFAsset(LengthConstrainingProductInstanceId.newProductInstanceIdAsString(),
                            ProductInstanceVersion.DEFAULT_VALUE.value(),
                            lineItemId,
                            productCode,
                            productOffering.getProductIdentifier().getVersionNumber(),
                            ProductInstanceState.LIVE,
                            getPricingStatusFromPMR(productOffering),
                            siteId,
                            contractTerm,
                            customerId,
                            contractId,
                            quoteOptionId,
                            AssetType.REAL,
                            projectId,
                            null, // bfgAssetId
                            null, // assetSourceVersion
                            AssetVersionStatus.DRAFT,
                            alternateCity,
                            AssetProcessType.NOT_APPLICABLE,
                            AssetProcessType.NOT_APPLICABLE,
                            null, // movesTo
                            uniqueIdJPARepository.getNextUniqueId(UniqueIdJPARepository.GLOBAL_TYPE),
                            getCharacteristicsFromPmr(productOffering),
                            Optional.of((List<CIFAssetRelationship>) new ArrayList<CIFAssetRelationship>()),
                            new ArrayList<CIFAssetError>(),
                            new ArrayList<PricingCaveat>(),
                            new HashSet<CIFAssetExternalIdentifier>(),
                            new ArrayList<CIFAssetPriceLine>(),
                            new ArrayList<CIFAssetProjectedUsage>(),
                            new ArrayList<CIFAssetAuxiliaryAttribute>(),
                            productCategoryCode,
                            ssvId,
                            slaId,
                            magId,
                            contractResignStatus, null);
    }

    private PricingStatus getPricingStatusFromPMR (ProductOffering productOffering)
    {
        PricingStatus pricingStatus =  PricingStatus.NOT_PRICED ;

        if (productOffering.getProductChargingSchemes().isEmpty())
        {
            pricingStatus = PricingStatus.NOT_APPLICABLE ;
        }

        return pricingStatus;
    }

    private List<CIFAssetCharacteristic> getCharacteristicsFromPmr(ProductOffering productOffering) {
        List<Attribute> attributes = newArrayList(Iterables.filter(productOffering.getAttributes(), PRODUCT_ATTRIBUTE_PREDICATE));

        return newArrayList(Iterables.transform(attributeSorter.sort(attributes), new Function<Attribute, CIFAssetCharacteristic>() {
            @Override
            public CIFAssetCharacteristic apply(Attribute input) {
                return new CIFAssetCharacteristic(input.getName().getName(), null, true);
            }
        }));
    }

    public CIFAssetRelationship relateAssets(CIFAsset fromAsset, CIFAsset toAsset, String relationshipName) {
        RelationshipType relationshipType = getRelationshipType(fromAsset.getRelationshipDefinitions(), relationshipName);

        final CIFAssetRelationship createdRelationship = new CIFAssetRelationship(toAsset,
                                                                                  relationshipName,
                                                                                  relationshipType,
                                                                                  ProductInstanceState.LIVE);
        fromAsset.getRelationships().add(createdRelationship);

        return createdRelationship;
    }
}
