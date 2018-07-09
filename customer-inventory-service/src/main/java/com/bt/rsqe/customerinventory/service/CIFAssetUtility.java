package com.bt.rsqe.customerinventory.service;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingRelationshipDetail;
import com.bt.rsqe.customerinventory.service.updates.UpdateException;
import com.bt.rsqe.domain.product.parameters.RelationshipType;

import java.util.List;

public class CIFAssetUtility {
    public static RelationshipType getRelationshipType(List<CIFAssetOfferingRelationshipDetail> relationshipDefinitions, String relationshipName) {
        for (CIFAssetOfferingRelationshipDetail relationshipDefinition : relationshipDefinitions) {
            if (relationshipDefinition.getRelationshipName().equals(relationshipName)) {
                return relationshipDefinition.getRelationshipType();
            }
        }
        throw new UpdateException("Relationship name \"" + relationshipName + "\" not valid.");
    }
}
