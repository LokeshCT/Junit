package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetRelationshipDTO;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.web.AssetKeyContainer;
import com.bt.rsqe.projectengine.web.ImportResults;
import com.bt.rsqe.utils.AssertObject;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static java.lang.String.*;

public class ProductRelationshipService {

    private final ProductInstanceClient productInstanceClient;
    private PmrClient pmrClient;

    public ProductRelationshipService(ProductInstanceClient futureProductInstanceClient, PmrClient pmrClient) {
        this.productInstanceClient = futureProductInstanceClient;
        this.pmrClient = pmrClient;
    }

    public void createRelations(ECRFSheet ecrfSheet, AssetKeyContainer assetKeyContainer, ImportResults importResults) {
        for (ECRFSheetModelRow row : ecrfSheet.getRows()) {
            try {
                Optional<AssetKey> ownerId = assetKeyContainer.getAssetKey(row.getOwnerId());
                Optional<AssetKey> relatedToId = assetKeyContainer.getAssetKey(row.getRelatedToId());
                RelationshipName relationshipName = RelationshipName.newInstance(row.getRelationshipName());
                RelationshipType relationType = RelationshipType.RelatedTo;

                if(ownerId.isPresent() && relatedToId.isPresent()) {
                    Long ownerAssetVersion = ownerId.get().getAssetVersion();
                    AssetDTO ownerAsset = productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId(ownerId.get().getAssetId()),
                                                                                      new ProductInstanceVersion(ownerAssetVersion));
                    Long relatedAssetVersion = relatedToId.get().getAssetVersion();
                    AssetDTO relatedAsset = productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId(relatedToId.get().getAssetId()),
                                                                                        new ProductInstanceVersion(relatedAssetVersion));

                    ownerAsset.addRelationship(new FutureAssetRelationshipDTO(relationshipName, relationType, relatedAsset));

                    validate(ownerAsset, relatedAsset, relationshipName, relationType);

                    productInstanceClient.putAsset(ownerAsset);
                }

            } catch (ECRFImportException exception) {
                importResults.addError("RelatedMapping", exception.getMessage());
            }
        }
    }

    private void validate(AssetDTO owner, final AssetDTO related, final RelationshipName relationshipName, final RelationshipType relationType) {
        if(AssertObject.isNull(owner))  {
            throw new ECRFImportException(ECRFImportException.ownerInstanceNotFound);
        }

        if(AssertObject.isNull(related))  {
            throw new ECRFImportException(ECRFImportException.relatedInstanceNotFound);
        }

        ProductOffering productOffering = pmrClient.productOffering(ProductSCode.newInstance(owner.getProductCode())).get();
        Optional<SalesRelationship> salesRelationshipOptional = Iterables.tryFind(productOffering.getSalesRelationships(), new Predicate<SalesRelationship>() {
            @Override
            public boolean apply(SalesRelationship input) {
                return input.getRelatedProductIdentifier().getProductId().equals(related.getProductCode()) && relationshipName.equals(input.getRelationshipName()) && (input.getType().equals(relationType));
            }
        });

        if(!salesRelationshipOptional.isPresent())  {
            throw new ECRFImportException(format(ECRFImportException.relationshipNotExist, relationshipName));
        }
    }
}
