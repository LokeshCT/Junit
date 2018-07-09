package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.externals.QuoteEngineHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.utils.Constants;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.*;

public class CancelRelationshipUpdater implements CIFAssetUpdater<CancelRelationshipRequest, CancelRelationshipResponse> {
    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private final QuoteEngineHelper quoteEngineHelper;
    private DependentUpdateBuilderFactory dependentUpdateBuilderFactory;
    private PmrHelper pmrHelper;

    public CancelRelationshipUpdater(CIFAssetOrchestrator cifAssetOrchestrator, QuoteEngineHelper quoteEngineHelper, DependentUpdateBuilderFactory dependentUpdateBuilderFactory, PmrHelper pmrHelper) {
        super();
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.quoteEngineHelper = quoteEngineHelper;
        this.dependentUpdateBuilderFactory = dependentUpdateBuilderFactory;
        this.pmrHelper = pmrHelper;
    }

    @Override
    public CancelRelationshipResponse performUpdate(CancelRelationshipRequest request) {

        // CancellingAssetId is the asset to be deleted
        // assetKey is the one that has the relationship to this for which the relationship is to be deleted as well
        CIFAsset assetToBeCancelled = cifAssetOrchestrator.getAsset(new CIFAssetKey(request.getCancellingAssetId(), newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.QuoteOptionItemDetail)));

        if (assetToBeCancelled.hasSharedAttributes()) {
            assetToBeCancelled.setStatus(ProductInstanceState.CANCELLED);
            assetToBeCancelled.setContractResignStatus(Constants.NO);
            cifAssetOrchestrator.saveAsset(assetToBeCancelled);
            final ProductOffering productOffering = pmrHelper.getProductOffering(assetToBeCancelled);
            for (String attribute : CIFAssetOfferingDetail.SHARED_ATTRIBUTES) {
                pmrHelper.getRuleSourcedValues(assetToBeCancelled, productOffering.getAttribute(new AttributeName(attribute)));
            }
        }

        // Lookup the relationship type  && its required only when the remove relationship is triggered from UI, otherwise do not seed the relationshipType.
        RelationshipType relationshipType = null;

        if (!request.isDependantCancellation()) {
            CIFAsset ownerAsset = cifAssetOrchestrator.getAsset(new CIFAssetKey(request.getAssetKey(), newArrayList(CIFAssetExtension.Relationships)));
            relationshipType = lookupRelationShipType(ownerAsset, assetToBeCancelled, request.getRelationshipName());
        }

        DependantUpdatesBuilder dependantUpdatesBuilder = DependantUpdatesBuilder.dependantUpdatesBuilder();

        //Iterate through the current asset being cancelled and its children and identify the associated assets, the deletable asset contributes To.
        CancellationContributesToRequestBuilder contributesToRequestBuilder = dependentUpdateBuilderFactory.getCancellationContributesToRequestBuilder();
        dependantUpdatesBuilder.withSet(contributesToRequestBuilder.buildRequests(assetToBeCancelled));

        // Note this now since we are about to remove stuff and need to know the state of play now for later
        boolean isARootAsset = cifAssetOrchestrator.isRootAsset(assetToBeCancelled.getAssetKey());

        //Cancel Assets and add Its dependant cancel Assets
        Set<? extends CIFAssetUpdateRequest> dependantCancellationRequests = cifAssetOrchestrator.cancelAssetTree(request.getAssetKey(), request.getRelationshipName(), assetToBeCancelled);
        dependantUpdatesBuilder.withSet(dependantCancellationRequests);

        if (!assetToBeCancelled.isStub()) {
            if (isARootAsset) {
                boolean hasProvisioningOrInServiceAsset = cifAssetOrchestrator.hasProvisiongOrInServiceAsset(assetToBeCancelled.getAssetKey());
                if (!hasProvisioningOrInServiceAsset) {
                    QuoteOptionItemDTO quoteOptionItem = quoteEngineHelper.getQuoteOptionItem(assetToBeCancelled.getProjectId(), assetToBeCancelled.getQuoteOptionId(), assetToBeCancelled.getLineItemId());
                    AssetCacheManager.recordRemovedQuoteOptionItem(assetToBeCancelled.getLineItemId(), quoteOptionItem);
                    quoteEngineHelper.removeQuoteOptionItem(assetToBeCancelled.getProjectId(), assetToBeCancelled.getQuoteOptionId(), assetToBeCancelled.getLineItemId());
                }
            }
        }

        return new CancelRelationshipResponse(request, relationshipType, dependantUpdatesBuilder.dependantRequests());
    }

    private RelationshipType lookupRelationShipType(CIFAsset relatedFromAsset, CIFAsset assetToBeCancelled, String relationshipName) {

        List<CIFAssetRelationship> relationships = relatedFromAsset.getRelationships(relationshipName);
        if (relationships != null) {
            for (CIFAssetRelationship cifAssetRelationship : relationships) {
                if (cifAssetRelationship.getRelated().getAssetKey().equals(assetToBeCancelled.getAssetKey())) {
                    return cifAssetRelationship.getRelationshipType();
                }
            }
        }

        return null;
    }
}
