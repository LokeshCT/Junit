package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingRelationshipDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.domain.product.parameters.RelationshipType;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;

public class CancelRelationshipRequestBuilder {

    public List<CancelRelationshipRequest> removeInvalidRelationships (CIFAsset cifAsset) {
        List<CancelRelationshipRequest> cancelRelationshipRequests = new ArrayList<CancelRelationshipRequest>();

        for (CIFAssetRelationship cifAssetRelationship : cifAsset.getRelationships()) {
            boolean relationshipDefinitionFound = false;
            final String assetRelationshipName = cifAssetRelationship.getRelationshipName();
            final RelationshipType assetRelationshipType = cifAssetRelationship.getRelationshipType();
            final CIFAsset related = cifAssetRelationship.getRelated();
            final String assetProductCode = related.getProductCode();


            for (CIFAssetOfferingRelationshipDetail offeringRelationshipDetail : cifAsset.getRelationshipDefinitions()) {
                if (assetRelationshipName.equals(offeringRelationshipDetail.getRelationshipName()) &&
                    assetRelationshipType.equals(offeringRelationshipDetail.getRelationshipType()) &&
                    (assetProductCode.equals(offeringRelationshipDetail.getRootProductIdentifier()) ||  offeringRelationshipDetail.getLinkedIdentifiers().contains(assetProductCode)) &&
                    offeringRelationshipDetail.getMaxCardinality().getCardinality() > 0) {       //some times the relationship will be available, but max cardinality will be set to zero.

                    final String stencilId = offeringRelationshipDetail.getStencilId();
                    if (isNull(stencilId) || stencilId.equals(related.getStencilDetail().getStencilCode())) {  //check only if relationship is a stencilled one
                        relationshipDefinitionFound = true;
                        break;
                    }
                }
            }

            if(!relationshipDefinitionFound) {
                cancelRelationshipRequests.add(new CancelRelationshipRequest(cifAsset.getAssetKey(),
                                                                             cifAsset.getLineItemId(),
                                                                             cifAsset.getQuoteOptionItemDetail().getLockVersion(),
                                                                             related.getAssetKey(),
                                                                             assetRelationshipName,
                                                                             related.getProductCode(),
                                                                             true));
            }
        }

        return cancelRelationshipRequests;
    }
}
