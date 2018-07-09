package com.bt.rsqe.customerinventory.service.orchestrators;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetRelationshipDTO;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.enums.AssetType;
import com.bt.rsqe.enums.IdentifierType;
import com.bt.rsqe.enums.ProductCodes;
import com.google.common.base.Optional;

import java.util.List;

import static org.apache.commons.lang.StringUtils.*;

//TODO; Once PMF has added required attributes at Bundled Product, hard codings of relations will be removed. Design is in progress to get it.
public class ServiceInstanceIdResolver {
    private AssetDTO owner;

    public ServiceInstanceIdResolver(AssetDTO owner) {
        this.owner = owner;
    }

    public Optional<String> get(ProductIdentifier relatedProductId, RelationshipName relationshipName) {
        if (ProductCodes.IpConnectGlobalCpe.productCode().equals(relatedProductId.getProductId())) {
            List<FutureAssetRelationshipDTO> relationships = owner.getRelationships();
            for (FutureAssetRelationshipDTO relationship : relationships) {
                AssetDTO relatedAsset = relationship.getRelatedAsset();
                if (AssetType.STUB == relatedAsset.getAssetType()) {
                    if (ProductCodes.IpConnectGlobalLeg.productCode().equals(relatedAsset.getProductCode())) {
                        String salesRelationshipName = relationship.getRelationshipName().value();
                        if ("WAN Connection Primary".equals(salesRelationshipName) && "Primary CE Router".equals(relationshipName.value())) {
                            return getClassicId(relatedAsset);
                        } else if ("WAN Connection Secondary".equals(salesRelationshipName) && "Secondary CE Router".equals(relationshipName.value())) {
                            return getClassicId(relatedAsset);
                        } else if ("WAN Connection Tertiary".equals(salesRelationshipName) && "Tertiary CE Router".equals(relationshipName.value())) {
                            return getClassicId(relatedAsset);
                        } else if ("WAN Connection Quaternary".equals(salesRelationshipName) && "Quaternary CE Router".equals(relationshipName.value())) {
                            return getClassicId(relatedAsset);
                        }
                    }
                }
            }
        }

        return Optional.absent();
    }

    private Optional<String> getClassicId(AssetDTO relatedAsset) {
        String externalIdentifier = relatedAsset.getExternalIdentifier(IdentifierType.CLASSIC);
        if (isNotEmpty(externalIdentifier)) {
            return Optional.of(externalIdentifier);
        }
        return Optional.absent();
    }
}
