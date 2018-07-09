package com.bt.rsqe.customerinventory.service.entities;

import com.bt.rsqe.customerinventory.repository.jpa.entities.ExternalIdentifierEntity;
import com.bt.rsqe.customerinventory.repository.jpa.keys.ExternalIdentifierKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetExternalIdentifier;

public class CIFAssetExternalIdentifierTransformer {
    public static ExternalIdentifierEntity toExternalIdentifierEntity(CIFAsset cifAsset, CIFAssetExternalIdentifier externalIdentifier) {
        return new ExternalIdentifierEntity(new ExternalIdentifierKey(cifAsset.getAssetKey().getAssetId(),
                                                                                              cifAsset.getAssetKey().getAssetVersion(),
                                                                                              externalIdentifier.getIdentifierType()),
                                                                    externalIdentifier.getExternalId());
    }

    public static CIFAssetExternalIdentifier fromExternalIdentifierEntity(ExternalIdentifierEntity externalIdentifierEntity) {
        return new CIFAssetExternalIdentifier(externalIdentifierEntity.getIdentifierType(), externalIdentifierEntity.getValue());
    }
}
