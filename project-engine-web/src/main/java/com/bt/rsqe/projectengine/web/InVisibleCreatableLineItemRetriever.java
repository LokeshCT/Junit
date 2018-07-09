package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetRelationshipDTO;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.product.ProductSalesRelationshipInstance;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.CreatableRelationshipInstanceFilter;
import com.bt.rsqe.domain.project.ProductInstance;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Set;

import static com.google.common.collect.Sets.*;

public class InVisibleCreatableLineItemRetriever {

    private ProductInstanceClient productInstanceClient;
    private CreatableRelationshipInstanceFilter creatableRelationshipInstanceFilter;

    public InVisibleCreatableLineItemRetriever(ProductInstanceClient productInstanceClient, CreatableRelationshipInstanceFilter creatableRelationshipInstanceFilter) {
        this.productInstanceClient = productInstanceClient;
        this.creatableRelationshipInstanceFilter = creatableRelationshipInstanceFilter;
    }

    public Set<String> whatInVisibleLineItemsIHaveCreated(String lineItemId) {
        final AssetDTO asset = productInstanceClient.getAssetDTO(new LineItemId(lineItemId));

        if(!eligibleForChecking(asset)) {
            return newHashSet();
        }

        final ProductInstance productInstance = productInstanceClient.convertAssetToLightweightInstance(asset);

        return newHashSet(FluentIterable
                              .from(productInstance.getCreatableProductRelationshipInstances(productInstanceClient, creatableRelationshipInstanceFilter))
                              .filter(new Predicate<ProductSalesRelationshipInstance>() {
                                  @Override
                                  public boolean apply(ProductSalesRelationshipInstance input) {
                                      ProductInstance relatedProductInstance = input.getRelatedProductInstance();
                                      return ( productInstance.getQuoteOptionId().equals(relatedProductInstance.getQuoteOptionId()) &&
                                              (!relatedProductInstance.isVisibleAsset() || !relatedProductInstance.getProductOffering().isInFrontCatalogue));
                                  }
                              }).transform(new Function<ProductSalesRelationshipInstance, String>() {
                @Override
                public String apply(final ProductSalesRelationshipInstance input) {
                    return input.getRelatedProductInstance().getLineItemId();
                }
            }));
    }

    private boolean eligibleForChecking(AssetDTO asset) {
        final Optional<FutureAssetRelationshipDTO> foundRelation = Iterables.tryFind(asset.getRelationships(), new Predicate<FutureAssetRelationshipDTO>() {
            @Override
            public boolean apply(@Nullable FutureAssetRelationshipDTO input) {
                return RelationshipType.RelatedTo.equals(input.getRelationshipType());
            }
        });

        if(foundRelation.isPresent()) {
            return true;
        }

        for(AssetDTO child : asset.getChildren()) {
            if(eligibleForChecking(child)) {
                return true;
            }
        }

        return false;
    }
}
