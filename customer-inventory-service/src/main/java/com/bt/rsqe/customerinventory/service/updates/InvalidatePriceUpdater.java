package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetPriceLine;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.InvalidatePriceRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.InvalidatePriceResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.PriceDelta;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.ChargingScheme;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import javax.persistence.NoResultException;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.domain.updates.InvalidatePriceRequest.ChangeType.*;
import static com.bt.rsqe.domain.project.PricingStatus.*;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.*;

public class InvalidatePriceUpdater implements CIFAssetUpdater<InvalidatePriceRequest, InvalidatePriceResponse> {
    private static Logger LOGGER = LogFactory.createDefaultLogger(Logger.class);

    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private final PmrHelper pmrHelper;
    private final PricingStatusHelper pricingStatusHelper;


    public InvalidatePriceUpdater(CIFAssetOrchestrator cifAssetOrchestrator, PmrHelper pmrHelper, PricingStatusHelper pricingStatusHelper) {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.pmrHelper = pmrHelper;
        this.pricingStatusHelper = pricingStatusHelper;
    }

    @Override
    public InvalidatePriceResponse performUpdate(InvalidatePriceRequest request) {
        CIFAssetKey cifAssetKey = new CIFAssetKey(request.getAssetKey(), CIFAssetExtension.noExtensions());

        CIFAsset cifAsset = cifAssetOrchestrator.getAsset(cifAssetKey);

        List<CIFAsset> updatedAssets = newArrayList();

        if (request.isChangeTypeOf(RelationshipChange)) {
            removeExistingAggregations(cifAsset);
            updatedAssets.add(cifAsset);
        }

        if (request.isChangeTypeOf(PriceAffectingChange) || request.isChangeTypeOf(StencilChange)) {
            invalidatePricingStatus(cifAsset);
            pricingStatusHelper.refreshPricingStatusBasedOnPriceLines(cifAsset);
            updatedAssets.add(cifAsset);

            updatedAssets.addAll(invalidateRelated(cifAsset));
        }

        // Save all the updates assets
        for (CIFAsset updatedAsset : updatedAssets) {
            saveAsset(updatedAsset);
        }


        List<PriceDelta> priceDeltas = producePriceDeltas(updatedAssets);

        // TODO can we actually have any dependant updates ?
        List<CIFAssetUpdateRequest> dependantUpdates = newArrayList();

        return new InvalidatePriceResponse(request, priceDeltas, dependantUpdates);
    }


    Predicate<ProductChargingScheme> aggregationSetPredicate = new Predicate<ProductChargingScheme>() {
        @Override
        public boolean apply(ProductChargingScheme input) {
            return input.getAggregationSet() != null;
        }
    };

    Function<ProductChargingScheme, String> chargingSchemeAggregationSetTransform = new Function<ProductChargingScheme, String>() {
        @Nullable
        @Override
        public String apply(ProductChargingScheme input) {
            return input.getAggregationSet();
        }
    };

    private Function<ProductChargingScheme, String> chargingSchemeNameTransform = new Function<ProductChargingScheme, String>() {
        @Nullable
        @Override
        public String apply(ProductChargingScheme input) {
            return input.getName();
        }
    };

    Predicate<ProductChargingScheme> matchingSetAggregations(final List<String> aggregationSetNames) {
        return new Predicate<ProductChargingScheme>() {
            @Override
            public boolean apply(ProductChargingScheme input) {
                return aggregationSetNames.contains(input.getSetAggregated());
            }
        };
    }


    private List<CIFAsset> invalidateRelated(CIFAsset cifAsset) {
        List<CIFAsset> updateAssets = newArrayList();
        List<ProductChargingScheme> chargingSchemes = pmrHelper.getProductOffering(cifAsset).getProductChargingSchemes();
        List<String> aggregationSetNames = newArrayList(transform(filter(chargingSchemes, aggregationSetPredicate), chargingSchemeAggregationSetTransform));

        // find the list of assets that are related to us

        List<CIFAsset> ownerAssets = cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(cifAsset.getAssetKey(), CIFAssetExtension.noExtensions()));
        // This is the list of assets that are related to us.
        // We need to scan from here upwards checking for any matching
        List<CIFAsset> relatedAndParentAssets = newArrayList();
        for (CIFAsset asset : ownerAssets) {
            relatedAndParentAssets.addAll(allParentsOfAsset(asset));
        }


        for (CIFAsset relatedAsset : relatedAndParentAssets) {
            boolean updated = false;
            // Find the ChargingSchemes that have a setAggregated that matches any of the AggregationSet values from the asset already invalidated
            List<ProductChargingScheme> relatedChargingSchemes = pmrHelper.getProductOffering(relatedAsset).getProductChargingSchemes();
            List<ProductChargingScheme> relatedAssetChargingSchemes = newArrayList(filter(relatedChargingSchemes, matchingSetAggregations(aggregationSetNames)));

            // Get the ChargingSchemeName for all of these
            List<String> relatedAssetChargingSchemeNames = newArrayList(transform(relatedAssetChargingSchemes, chargingSchemeNameTransform));

            // Now find the priceLines that have these Charging Scheme names
            for (CIFAssetPriceLine cifAssetPriceLine : relatedAsset.getPriceLines()) {
                if (relatedAssetChargingSchemeNames.contains(cifAssetPriceLine.getChargingSchemeName())) {
                    cifAssetPriceLine.setStatus(PriceLineStatus.IN_VALIDATED);
                    updated = true;
                }
            }
            if (updated) {
                // refresh the pricing status on this related asset as well since we have just tweaked the pricelines
                pricingStatusHelper.refreshPricingStatusBasedOnPriceLines(relatedAsset);
                updateAssets.add(relatedAsset);
                pricingStatusHelper.refreshPricingStatusBasedOnPriceLines(cifAsset);
            }
        }
        return updateAssets;
    }

    private List<CIFAsset> allParentsOfAsset(CIFAsset asset) {
        List<CIFAsset> result = newArrayList();
        try {
            CIFAsset parent = asset;
            while (parent != null) {
                result.add(parent);
                parent = cifAssetOrchestrator.getParentAsset(new CIFAssetKey(parent.getAssetKey(), CIFAssetExtension.noExtensions()));
            }
        } catch (NoResultException e) {
            // Catch this to indicate we have reached the root
        }
        return result;
    }

    private List<PriceDelta> producePriceDeltas(List<CIFAsset> updatedAssets) {
        List<PriceDelta> priceDeltas = newArrayList();
        for (CIFAsset updatedAsset : updatedAssets) {
            // Extend the asset here now since we need to have the quote option item details on it
            cifAssetOrchestrator.extendAsset(updatedAsset, newArrayList(CIFAssetExtension.QuoteOptionItemDetail));
            PriceDelta priceDelta = new PriceDelta(
                    updatedAsset.getPricingStatus(),
                    0.0,
                    0.0,
                    updatedAsset.getQuoteOptionItemDetail().getLockVersion(),
                    updatedAsset.getLineItemId(),
                    updatedAsset.getAssetKey()
            );
            priceDeltas.add(priceDelta);
        }

        return priceDeltas;
    }

    public void removeExistingAggregations(CIFAsset cifAsset) {
        cifAsset.getPriceLines().removeAll(getAggregationPriceLines(cifAsset));
    }

    private List<CIFAssetPriceLine> getAggregationPriceLines(CIFAsset cifAsset) {
        final ProductOffering productOffering = pmrHelper.getProductOffering(cifAsset);

        Predicate<CIFAssetPriceLine> aggregationPriceLinePredicate = new Predicate<CIFAssetPriceLine>() {
            @Override
            public boolean apply(CIFAssetPriceLine input) {
                Optional<ProductChargingScheme> productChargingSchemeOptional = productOffering.getProductChargingSchemeOf(ChargingScheme.newInstance(input.getChargingSchemeName()));
                return productChargingSchemeOptional.isPresent() && PricingStrategy.Aggregation.equals(productChargingSchemeOptional.get().getPricingStrategy());
            }
        };

        return newArrayList(Iterables.filter(cifAsset.getPriceLines(), aggregationPriceLinePredicate));
    }

    private void invalidatePricingStatus(CIFAsset cifAsset) {

        LOGGER.invalidatePricingStatus(cifAsset.getAssetKey());
        ProductOffering productOffering = pmrHelper.getProductOffering(cifAsset);

        if (NOT_APPLICABLE.equals(cifAsset.getPricingStatus()) && !productOffering.getProductChargingSchemes().isEmpty()) {
            cifAsset.setPricingStatus(null);
        }
    }

    private void saveAsset(CIFAsset cifAsset) {
        // Extend the asset here with the child relationships so we don't lose them
        cifAssetOrchestrator.extendAsset(cifAsset, newArrayList(CIFAssetExtension.Relationships));
        cifAssetOrchestrator.saveAssetAndClearCaches(cifAsset);
    }

    interface Logger {
        @Log(level = LogLevel.DEBUG, format = "assetKey=%s")
        void invalidatePricingStatus(AssetKey assetKey);

        @Log(level = LogLevel.DEBUG, format = "assetKey=%s")
        void parent(AssetKey assetKey);
    }
}
