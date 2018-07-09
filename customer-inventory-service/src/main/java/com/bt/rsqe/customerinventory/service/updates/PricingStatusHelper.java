package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetPriceLine;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.SpecialBidReservedAttributesHelper;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstanceStatus;
import com.google.common.base.Predicate;

import javax.validation.constraints.NotNull;
import java.util.List;

import static com.bt.rsqe.domain.project.PricingStatus.*;
import static com.bt.rsqe.domain.project.ProductInstanceStatus.CEASED;
import static com.bt.rsqe.utils.AssertObject.isNotNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

public class PricingStatusHelper {
    private final PmrHelper pmrHelper;

    public PricingStatusHelper(PmrHelper pmrHelper) {
        this.pmrHelper = pmrHelper;
    }

    public void refreshPricingStatusBasedOnPriceLines(CIFAsset cifAsset) {
        for (CIFAssetPriceLine cifAssetPriceLine : cifAsset.getPriceLines()) {
            cifAssetPriceLine.setStatus(PriceLineStatus.IN_VALIDATED);
        }

        if (!isPricingStatus(cifAsset, NOT_APPLICABLE) && !(isSpecialBid(cifAsset) && isPricingStatus(cifAsset, WITHDRAWN)) && isPriceable(cifAsset)) {
            cifAsset.setPricingStatus(NOT_PRICED);
        } else {
            if (isSpecialBid(cifAsset) && !hasSpecialBidPriceline(cifAsset) && !isProductInstanceStatus(cifAsset, CEASED)) {
                if (!isPricingStatus(cifAsset, WITHDRAWN)) {
                    cifAsset.setPricingStatus(PROGRESSING);
                }
            } else {
                PricingStatus minStatus = minimumPricingStatus(cifAsset);
                cifAsset.setPricingStatus(minStatus);
            }
        }
    }

    private boolean isProductInstanceStatus(@NotNull CIFAsset cifAsset, @NotNull ProductInstanceStatus status) {
        return status.equals(ProductInstanceState.toProductInstanceStatus(cifAsset.getStatus()));
    }


    private boolean isPricingStatus(@NotNull CIFAsset cifAsset, @NotNull PricingStatus pricingStatus) {
        return pricingStatus.equals(cifAsset.getPricingStatus());
    }

    private boolean isSpecialBid(CIFAsset cifAsset) {
        SpecialBidReservedAttributesHelper helper = new SpecialBidReservedAttributesHelper();
        CIFAssetCharacteristic specialBidCharacteristic = helper.getSpecialBidCharacteristic(cifAsset);

        return isNotNull(specialBidCharacteristic) && ("Yes".equals(specialBidCharacteristic.getValue()) || "Y".equals(specialBidCharacteristic.getValue())) && !isCOTC(cifAsset);
    }

    private boolean isCOTC(CIFAsset cifAsset) {
        return pmrHelper.getProductOffering(cifAsset).getProductIdentifier().getProductName().contains("COTC");
    }

    private boolean isPriceable(CIFAsset cifAsset) {
        return isSpecialBid(cifAsset) || (containsNonContributesToPricingStrategy(cifAsset) && !isCustomerOwnedCpe(cifAsset));
    }

    private boolean isCustomerOwnedCpe(CIFAsset cifAsset) {
        ProductOffering productOffering = pmrHelper.getProductOffering(cifAsset);
        AttributeName attributeName = new AttributeName(ProductOffering.CPE_OPTION);
        if (productOffering.isSimpleTypeOf(SimpleProductOfferingType.NetworkNode) && productOffering.hasAttribute(attributeName)) {
            Attribute attribute = productOffering.getAttribute(attributeName);
            String value = attribute.hasDefaultValue() ? attribute.getDefaultValue().getValue().toString() : attribute.getAllowedValuesAsString();
            return ProductOffering.CUSTOMER_OWNED_CPE.equalsIgnoreCase(value);
        }

        return false;
    }

    Predicate<ProductChargingScheme> unsupportChargingSchemeFilter = new Predicate<ProductChargingScheme>() {
        @Override
        public boolean apply(ProductChargingScheme input) {
            return !PricingStrategy.ContributesTo.equals(input.getPricingStrategy());
        }
    };

    private boolean containsNonContributesToPricingStrategy(CIFAsset cifAsset) {
        ProductOffering productOffering = pmrHelper.getProductOffering(cifAsset);
        List<ProductChargingScheme> productChargingSchemes = newArrayList(filter(productOffering.getProductChargingSchemes(), unsupportChargingSchemeFilter));
        return !productChargingSchemes.isEmpty();
    }

    private boolean hasSpecialBidPriceline(CIFAsset cifAsset) {
        for (CIFAssetPriceLine priceLine : cifAsset.getPriceLines()) {
            if (priceLine.getChargingSchemeName().contains("Special Bid") || priceLine.getChargingSchemeName().contains("Non-Std")) {
                return true;
            }
        }

        return false;
    }

    private PricingStatus minimumPricingStatus(@NotNull CIFAsset cifAsset) {
        PricingStatus minimumStatus = NOT_APPLICABLE;
        for (CIFAssetPriceLine priceLine : cifAsset.getPriceLines()) {
            PricingStatus status = PriceLineStatus.toPricingStatus(priceLine.getStatus());
            if (status.rank() < minimumStatus.rank()) {
                minimumStatus = status;
            }
        }
        return minimumStatus;
    }

}
