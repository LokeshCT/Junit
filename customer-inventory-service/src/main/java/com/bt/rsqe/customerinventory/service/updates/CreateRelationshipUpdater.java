package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.repository.StaleAssetException;
import com.bt.rsqe.customerinventory.service.CIFAssetUtility;
import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingRelationshipDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.domain.updates.AutoDefaultRelationshipsRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesCreationRequest;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.StencilReservedAttributesHelper;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.externals.QuoteEngineHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.UUID;

import static com.bt.rsqe.domain.product.parameters.RelationshipType.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.*;

public class CreateRelationshipUpdater implements CIFAssetUpdater<CreateRelationshipRequest, CreateRelationshipResponse> {
    private final CIFAssetOrchestrator assetOrchestrator;
    private final QuoteEngineHelper quoteEngineHelper;
    private final DependentUpdateBuilderFactory dependentUpdateBuilderFactory;
    private PmrHelper pmrHelper;
    private final StencilReservedAttributesHelper stencilReservedAttributesHelper;

    public CreateRelationshipUpdater(CIFAssetOrchestrator assetOrchestrator,
                                     QuoteEngineHelper quoteEngineHelper,
                                     DependentUpdateBuilderFactory dependentUpdateBuilderFactory, PmrHelper pmrHelper) {
        this.assetOrchestrator = assetOrchestrator;
        this.quoteEngineHelper = quoteEngineHelper;
        this.dependentUpdateBuilderFactory = dependentUpdateBuilderFactory;
        this.pmrHelper = pmrHelper;
        this.stencilReservedAttributesHelper = new StencilReservedAttributesHelper();
    }

    @Override
    public CreateRelationshipResponse performUpdate(CreateRelationshipRequest relationshipRequest) {
        final CIFAsset parentAsset = assetOrchestrator.getAsset(new CIFAssetKey(relationshipRequest.getAssetKey(),
                newArrayList(CIFAssetExtension.ProductOfferingRelationshipDetail,
                        CIFAssetExtension.QuoteOptionItemDetail)));
        List<CIFAssetOfferingRelationshipDetail> relationshipDefinitions = parentAsset.getRelationshipDefinitions();
        final RelationshipType relationshipType = CIFAssetUtility.getRelationshipType(relationshipDefinitions, relationshipRequest.getRelationshipName());
        String productCategoryName = "";
        ProductCategoryCode productCategoryCode = parentAsset.getProductCategoryCode();

        // Create the new line item in project engine
        String lineItemId = (RelatedTo.equals(relationshipType) ? UUID.randomUUID().toString() : parentAsset.getLineItemId());
        String siteId = (RelatedTo.equals(relationshipType) && !isEmpty(relationshipRequest.getSiteId()) ? relationshipRequest.getSiteId() : parentAsset.getSiteId());
        if (RelatedTo.equals(relationshipType)) {
            CIFAssetQuoteOptionItemDetail quoteOptionItemDetail = parentAsset.getQuoteOptionItemDetail();
            ContractDTO contractDTO = new ContractDTO(parentAsset.getContractId(), parentAsset.getContractTerm(), quoteOptionItemDetail.getPriceBooks());
            productCategoryCode = resolveProductCategoryCode(parentAsset, quoteOptionItemDetail, relationshipRequest);
            QuoteOptionItemDTO quoteOptionItem = quoteEngineHelper.createQuoteOptionItem(parentAsset.getProjectId(),
                    parentAsset.getQuoteOptionId(),
                    lineItemId,
                    relationshipRequest.getProductCode(),
                    quoteOptionItemDetail.getLineItemAction(),
                    parentAsset.getContractTerm(),
                    contractDTO,
                    quoteOptionItemDetail.isIfc(),
                    quoteOptionItemDetail.isImportable(),
                    quoteOptionItemDetail.getCustomerRequiredDate(), productCategoryCode, quoteOptionItemDetail.getBundleItemId(), quoteOptionItemDetail.isBundleProduct());

            AssetCacheManager.recordCreatedQuoteOptionItem(quoteOptionItem);

            try {
                assetOrchestrator.createLineItemLockVersion(lineItemId);
            } catch (StaleAssetException staleAssetException) {
                ///CLOVER:OFF - We should never hit this scenario - we are creating a new line item here so lock version cannot exist.
                throw new RuntimeException(staleAssetException.getMessage(), staleAssetException);
                ///CLOVER:ON
            }

            if(SimpleProductOfferingType.BundleProduct == parentAsset.getOfferingDetail().getOfferingType()) {
                productCategoryName = pmrHelper.getProductCategoryName(parentAsset.getProductCode(), productCategoryCode);
            }
        }

        // Create a new asset and relate it to the passed in parent
        CIFAssetRelationship createdRelationship = assetOrchestrator.createAndRelateAsset(parentAsset, relationshipRequest.getRelationshipName(),
                relationshipRequest.getProductCode(), relationshipRequest.getStencilCode(),
                lineItemId, siteId,
                parentAsset.getContractTerm(), parentAsset.getCustomerId(),
                parentAsset.getContractId(), parentAsset.getProjectId(),
                parentAsset.getQuoteOptionId(),
                relationshipRequest.getAlternateCity(), productCategoryCode);

        // Update the asset characteristic with the new stencil id
        if (relationshipRequest.getStencilCode() != null) {
            stencilReservedAttributesHelper.getStencilCharacteristic(createdRelationship.getRelated()).setValue(relationshipRequest.getStencilCode());
        }


        assetOrchestrator.saveAssetAndClearCaches(parentAsset);
        if (RelatedTo.equals(relationshipType)) {
            assetOrchestrator.saveAssetAndClearCaches(createdRelationship.getRelated());
        }

        return new CreateRelationshipResponse(relationshipRequest,
                createdRelationship.getRelationshipName(),
                createdRelationship.getRelationshipType(),
                createdRelationship.getRelationshipStatus(),
                createdRelationship.getRelated().getAssetKey(),
                getDependantUpdates(parentAsset, createdRelationship, relationshipRequest), productCategoryName);
    }

    private ProductCategoryCode resolveProductCategoryCode(CIFAsset parentAsset, CIFAssetQuoteOptionItemDetail quoteOptionItemDetail, final CreateRelationshipRequest relationshipRequest) {
        if (SimpleProductOfferingType.BundleProduct == parentAsset.getOfferingDetail().getOfferingType()) {
            ProductOffering productOffering = pmrHelper.getProductOffering(parentAsset.getProductCode());
            List<SalesRelationship> salesRelationships = productOffering.getSalesRelationships(RelationshipName.newInstance(relationshipRequest.getRelationshipName()));
            Optional<SalesRelationship> salesRelationshipOptional = Iterables.tryFind(salesRelationships, new Predicate<SalesRelationship>() {
                @Override
                public boolean apply(SalesRelationship input) {
                    return input.getRelatedProductIdentifier().getProductId().equals(relationshipRequest.getProductCode());
                }
            });

            if (salesRelationshipOptional.isPresent()) {
                boolean creatableCatalogueProductRelation = salesRelationshipOptional.get().isCreatableCatalogueProductRelation();
                if (creatableCatalogueProductRelation) {
                    Optional<ProductIdentifier> productCategoryCode = pmrHelper.getProductCategoryCode(relationshipRequest.getProductCode());
                    if (productCategoryCode.isPresent()) {
                        return new ProductCategoryCode(productCategoryCode.get().getProductId());
                    }
                }
            }
        }
        return ProductCategoryCode.catCodeSet(parentAsset.getProductCategoryCode()) ? parentAsset.getProductCategoryCode() : quoteOptionItemDetail.getProductCategoryCode();
    }

    private List<CIFAssetUpdateRequest> getDependantUpdates(CIFAsset parentAsset, CIFAssetRelationship createdRelationship, CreateRelationshipRequest relationshipRequest) {
        CIFAsset related = createdRelationship.getRelated();

        DependantUpdatesBuilder dependantUpdatesBuilder = dependentUpdateBuilderFactory.getDependentUpdateBuilderFactory();


        //Add a request to set the value for each characteristic
        dependantUpdatesBuilder.with(dependentUpdateBuilderFactory.getCharacteristicChangeRequestBuilder()
                .defaultForAllCharacteristics(related, relationshipRequest.getStencilCode(), relationshipRequest.getRelationshipName()));

        //Auto-default the relationships
        dependantUpdatesBuilder.with(new AutoDefaultRelationshipsRequest(related.getAssetKey(), related.getLineItemId(), related.getQuoteOptionItemDetail().getLockVersion(), related.getProductCode()));

        // Fire the relate to rules for the new asset and add the dependencies it comes back with
        assetOrchestrator.extendAsset(related, newArrayList(CIFAssetExtension.ProductRules));
        dependantUpdatesBuilder.withList(dependentUpdateBuilderFactory.getExecutionRequestBuilder().buildFor(related));

        //Calculate contributesTo dependant requests for  newly added relationship
        int contributesToExecutionDepth = 1;
        dependantUpdatesBuilder.withSet(dependentUpdateBuilderFactory.getContributesToChangeRequestBuilder().buildRequests(related.getAssetKey(),
                related.getProductCode(),
                RelationshipName.newInstance(relationshipRequest.getRelationshipName()),
                contributesToExecutionDepth));

        //Create SpecialBid creation request
        dependantUpdatesBuilder.with(new SpecialBidAttributesCreationRequest(related.getAssetKey()));

        dependantUpdatesBuilder.withOptional(dependentUpdateBuilderFactory.getInvalidatePriceRequestBuilder().invalidatePriceForRelationshipChange(parentAsset));


        return dependantUpdatesBuilder.dependantRequests();
    }
}
