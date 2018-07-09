package com.bt.rsqe.customerinventory.service.entities;

import com.bt.rsqe.customerinventory.parameter.AssetSourceVersion;
import com.bt.rsqe.customerinventory.parameter.BfgAssetId;
import com.bt.rsqe.customerinventory.parameter.City;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.ContractTerm;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.ProjectId;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerinventory.repository.jpa.entities.AbstractAssetEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.ExternalIdentifierEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetAuxiliaryAttributeEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetCaveatEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetCharacteristicEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetErrorEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetPriceLineEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetProjectedUsageEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetRelationshipEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.details.AssetDetails;
import com.bt.rsqe.customerinventory.repository.jpa.keys.FutureAssetRelationshipKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAuxiliaryAttribute;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetError;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetExternalIdentifier;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetPriceLine;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetProjectedUsage;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.repository.ExternalAssetReader;
import com.bt.rsqe.domain.AssetUniqueId;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.PricingCaveat;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.enums.AssetType;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class CIFAssetTransformer {
    public static CIFAsset fromAssetEntity(AbstractAssetEntity assetEntity, Boolean loadRelationships, ExternalAssetReader externalAssetReader) {
        if (AssetType.STUB == assetEntity.getDetails().getAssetType()) {
            return getStubAsset(assetEntity, externalAssetReader, loadRelationships);
        }

        List<CIFAssetCharacteristic> characteristics = new ArrayList<CIFAssetCharacteristic>();
        for (FutureAssetCharacteristicEntity characteristicEntity : assetEntity.getCharacteristics()) {
            characteristics.add(CIFAssetCharacteristicTransformer.fromCharacteristicEntity(characteristicEntity));
        }

        List<CIFAssetError> errors = new ArrayList<CIFAssetError>();
        for (FutureAssetErrorEntity futureAssetErrorEntity : assetEntity.getAssetErrors()) {
            errors.add(CIFAssetErrorTransformer.fromErrorEntity(futureAssetErrorEntity));
        }

        List<PricingCaveat> pricingCaveats = new ArrayList<PricingCaveat>();
        for (FutureAssetCaveatEntity futureAssetCaveatEntity : assetEntity.getPricingCaveats()) {
            pricingCaveats.add(CIFAssetPricingCaveatTransformer.fromPricingCaveatEntity(assetEntity, futureAssetCaveatEntity));
        }

        List<CIFAssetPriceLine> priceLines = new ArrayList<CIFAssetPriceLine>();
        for (FutureAssetPriceLineEntity priceLineEntity : assetEntity.getPriceLines()) {
            priceLines.add(CIFAssetPriceLineTransformer.fromPriceLineEntity(priceLineEntity));
        }

        Set<CIFAssetExternalIdentifier> externalIdentifiers = new HashSet<CIFAssetExternalIdentifier>();
        for (ExternalIdentifierEntity externalIdentifierEntity : assetEntity.getExternalIdentifiers()) {
            externalIdentifiers.add(CIFAssetExternalIdentifierTransformer.fromExternalIdentifierEntity(externalIdentifierEntity));
        }

        List<CIFAssetProjectedUsage> projectedUsages = new ArrayList<CIFAssetProjectedUsage>();
        for (FutureAssetProjectedUsageEntity futureAssetProjectedUsageEntity : assetEntity.getProjectedUsages()) {
            projectedUsages.add(CIFAssetProjectedUsageTransformer.fromProjectedUsageEntity(futureAssetProjectedUsageEntity));
        }

        List<CIFAssetAuxiliaryAttribute> auxiliaryAttributes = new ArrayList<CIFAssetAuxiliaryAttribute>();
        for (FutureAssetAuxiliaryAttributeEntity futureAssetAuxiliaryAttributeEntity : assetEntity.getAuxiliaryAttributes()) {
            auxiliaryAttributes.add(CIFAssetAuxiliaryAttributeTransformer.fromAuxiliaryAttributeEntity(futureAssetAuxiliaryAttributeEntity));
        }

        List<CIFAssetRelationship> relationships = new ArrayList<CIFAssetRelationship>();
        if (loadRelationships) {
            for (FutureAssetRelationshipEntity relationshipEntity : assetEntity.getRelationships()) {
                relationships.add(new CIFAssetRelationship(fromAssetEntity(relationshipEntity.getRelated(), true, externalAssetReader),
                        relationshipEntity.getRelationshipName(),
                        RelationshipType.fromString(relationshipEntity.getRelationshipType()),
                        ProductInstanceState.valueOf(relationshipEntity.getRelationshipStatus())));
            }
        }

        AssetDetails details = assetEntity.getDetails();
        return new CIFAsset(new LengthConstrainingProductInstanceId(assetEntity.getKey().getAssetId()),
                new ProductInstanceVersion(assetEntity.getKey().getAssetVersion()),
                new LineItemId(details.getLineItemId()),
                new ProductCode(details.getProductCode()),
                new ProductVersion(details.getProductVersion()),
                ProductInstanceState.valueOf(details.getStatus()),
                PricingStatus.valueOf(details.getPricingStatus()),
                new SiteId(details.getSiteId()),
                new ContractTerm(details.getContractTerm()),
                new CustomerId(details.getCustomerId()),
                new ContractId(details.getContractId()),
                new QuoteOptionId(details.getQuoteOptionId()),
                details.getAssetType(),
                new ProjectId(details.getProjectId()),
                new BfgAssetId(details.getBfgAssetId()),
                new AssetSourceVersion(details.getAssetSourceVersion()),
                details.getAssetVersionStatus(),
                new City(details.getAlternateCity()),
                details.getAssetProcessType(),
                details.getAssetSubProcessType(),
                details.getMovesTo() == null ? null :
                        new LengthConstrainingProductInstanceId(details.getMovesTo().getValue()),
                new AssetUniqueId(details.getAssetUniqueId()),
                characteristics,
                loadRelationships ? Optional.of(relationships) : Optional.<List<CIFAssetRelationship>>absent(),
                errors, pricingCaveats,
                externalIdentifiers, priceLines,
                projectedUsages,
                auxiliaryAttributes,
                isNotEmpty(details.getProductCategoryCode()) ? new ProductCategoryCode(details.getProductCategoryCode()) : ProductCategoryCode.NIL,
                details.getSsvId(),
                details.getSlaId(),
                details.getMagId(),
                details.getContractResignStatus(),
                details.getInitialBillingStartDate());
    }

    private static CIFAsset getStubAsset(AbstractAssetEntity assetEntity, ExternalAssetReader externalAssetReader, boolean loadRelationships) {
        AssetDetails details = assetEntity.getDetails();
        String assetId = assetEntity.getKey().getAssetId();
        ProductInstanceState productInstanceState = ProductInstanceState.valueOf(assetEntity.getDetails().getStatus());

        SortedSet<ExternalIdentifierEntity> externalIdentifiers = assetEntity.getExternalIdentifiers();
        if (externalIdentifiers.size() != 1) {
            throw new IllegalArgumentException(String.format("External Asset Identifier not found for asset %s ", assetId));
        }

        ExternalIdentifierEntity identifierEntity = externalIdentifiers.iterator().next();
        Optional<CIFAsset> cifAssetOptional = externalAssetReader.read(new CustomerId(details.getCustomerId()),
                new SiteId(details.getSiteId()),
                new ProductCode(details.getProductCode()),
                new LengthConstrainingProductInstanceId(assetId),
                assetEntity.getKey().getAssetVersion(),
                productInstanceState,
                identifierEntity.getValue(),
                identifierEntity.getIdentifierType(),
                loadRelationships
        );
        if (!cifAssetOptional.isPresent()) {
            throw new IllegalArgumentException(String.format("External Asset not found for asset %s ", assetId));
        }

        return cifAssetOptional.get();
    }

    public static AbstractAssetEntity toAssetEntity(CIFAsset cifAsset) {
        SortedSet<FutureAssetCharacteristicEntity> characteristics = new TreeSet<FutureAssetCharacteristicEntity>();
        for (CIFAssetCharacteristic cifAssetCharacteristic : cifAsset.getCharacteristics()) {
            characteristics.add(CIFAssetCharacteristicTransformer.toCharacteristicEntity(cifAsset, cifAssetCharacteristic));
        }

        List<FutureAssetErrorEntity> errors = new ArrayList<FutureAssetErrorEntity>();
        for (CIFAssetError cifAssetError : cifAsset.getErrors()) {
            errors.add(CIFAssetErrorTransformer.toErrorEntity(cifAsset, cifAssetError));
        }

        SortedSet<FutureAssetCaveatEntity> pricingCaveats = new TreeSet<FutureAssetCaveatEntity>();
        for (PricingCaveat pricingCaveat : cifAsset.getPricingCaveats()) {
            pricingCaveats.add(CIFAssetPricingCaveatTransformer.toPricingCaveatEntity(cifAsset, pricingCaveat));
        }

        SortedSet<FutureAssetPriceLineEntity> priceLines = new TreeSet<FutureAssetPriceLineEntity>();
        for (CIFAssetPriceLine cifAssetPriceLine : cifAsset.getPriceLines()) {
            priceLines.add(CIFAssetPriceLineTransformer.toPriceLineEntity(cifAsset, cifAssetPriceLine));
        }

        SortedSet<ExternalIdentifierEntity> externalIdentifierEntities = new TreeSet<ExternalIdentifierEntity>();
        for (CIFAssetExternalIdentifier externalIdentifier : cifAsset.getExternalIdentifiers()) {
            externalIdentifierEntities.add(CIFAssetExternalIdentifierTransformer.toExternalIdentifierEntity(cifAsset, externalIdentifier));
        }

        SortedSet<FutureAssetProjectedUsageEntity> projectedUsageEntities = new TreeSet<FutureAssetProjectedUsageEntity>();
        for (CIFAssetProjectedUsage cifAssetProjectedUsage : cifAsset.getProjectedUsages()) {
            projectedUsageEntities.add(CIFAssetProjectedUsageTransformer.toProjectedUsageEntity(cifAsset, cifAssetProjectedUsage));
        }

        SortedSet<FutureAssetAuxiliaryAttributeEntity> auxiliaryAttributeEntities = new TreeSet<FutureAssetAuxiliaryAttributeEntity>();
        for (CIFAssetAuxiliaryAttribute cifAssetAuxiliaryAttribute : cifAsset.getAuxiliaryAttributes()) {
            auxiliaryAttributeEntities.add(CIFAssetAuxiliaryAttributeTransformer.fromAuxiliaryAttributeEntity(cifAsset, cifAssetAuxiliaryAttribute));
        }

        FutureAssetEntity futureAssetEntity = new FutureAssetEntity(cifAsset.getAssetKey().getAssetId(),
                                                                    cifAsset.getAssetKey().getAssetVersion(),
                                                                    cifAsset.getLineItemId(),
                                                                    cifAsset.getProductCode(),
                                                                    cifAsset.getProductVersion(),
                                                                    cifAsset.getStatus().name(),
                                                                    cifAsset.getPricingStatus().name(),
                                                                    cifAsset.getSiteId(),
                                                                    cifAsset.getContractTerm(),
                                                                    cifAsset.getCustomerId(),
                                                                    cifAsset.getContractId(),
                                                                    cifAsset.getQuoteOptionId(),
                                                                    cifAsset.getAssetType(),
                                                                    cifAsset.getProjectId(),
                                                                    cifAsset.getBfgAssetId(),
                                                                    cifAsset.getAssetSourceVersion(),
                                                                    cifAsset.getAssetVersionStatus(),
                                                                    cifAsset.getAlternateCity(),
                                                                    cifAsset.getAssetProcessType(),
                                                                    cifAsset.getAssetSubProcessType(),
                                                                    cifAsset.getMovesTo(),
                                                                    cifAsset.getAssetUniqueId(),
                                                                    characteristics,
                                                                    auxiliaryAttributeEntities,
                                                                    errors,
                                                                    pricingCaveats,
                                                                    externalIdentifierEntities,
                                                                    priceLines,
                                                                    projectedUsageEntities,
                                                                    cifAsset.getProductCategoryCode(),
                                                                    null,
                                                                    cifAsset.getSlaId(),
                                                                    cifAsset.getMagId(),
																	cifAsset.getSsvId(),
                                                                    cifAsset.getContractResignStatus());

        SortedSet<FutureAssetRelationshipEntity> relationships = new TreeSet<FutureAssetRelationshipEntity>();
        if (cifAsset.hasExtension(CIFAssetExtension.Relationships)) {
            for (CIFAssetRelationship cifAssetRelationship : cifAsset.getRelationships()) {
                CIFAsset relatedCIFAsset = cifAssetRelationship.getRelated();
                FutureAssetRelationshipKey futureAssetRelationshipKey = new FutureAssetRelationshipKey(futureAssetEntity, toAssetEntity(relatedCIFAsset), RelationshipName.newInstance(cifAssetRelationship.getRelationshipName()));
                relationships.add(new FutureAssetRelationshipEntity(futureAssetRelationshipKey, cifAssetRelationship.getRelationshipType(), cifAsset.getLineItemId(), relatedCIFAsset.getLineItemId(), cifAssetRelationship.getRelationshipStatus().name()));
            }
        }

        futureAssetEntity.setRelationships(relationships);

        return futureAssetEntity;
    }
}
