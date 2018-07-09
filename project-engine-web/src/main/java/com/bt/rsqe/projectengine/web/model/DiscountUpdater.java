package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.Money;
import com.bt.rsqe.Percentage;
import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.enums.PriceType;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.sun.istack.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.Percentage.*;
import static com.bt.rsqe.enums.PriceCategory.*;
import static com.bt.rsqe.enums.PriceType.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.*;
import static java.util.Arrays.asList;

public class DiscountUpdater {

    public void applyDiscount(Map<String, DiscountDelta> discounts, final FutureAssetPricesDTO futureAssetPricesDTO, @NotNull List<PriceLineModel> priceLineModels) {
        recursiveApplyDiscount(discounts, asList(futureAssetPricesDTO));
        reCalculateAggregationDiscount(priceLineModels);
    }

    private void recursiveApplyDiscount(Map<String, DiscountDelta> discounts, List<FutureAssetPricesDTO> futureAssetPricesDTOs) {
        for (FutureAssetPricesDTO dto : futureAssetPricesDTOs) {
            for (PriceLineDTO priceLineDTO : dto.getPriceLines()) {
                if (discounts.containsKey(priceLineDTO.getId())) {
                    final DiscountDelta discountDelta = discounts.get(priceLineDTO.getId());
                    if(discountDelta.getDiscount().isPresent()) {
                        priceLineDTO.getPrice(CHARGE_PRICE).discountPercentage = discountDelta.getDiscount().get();
                    }
                    if(discountDelta.getVendorDiscountRef().isPresent()) {
                        priceLineDTO.setVendorDiscountRef(discountDelta.getVendorDiscountRef().get());
                    } else {
                        priceLineDTO.setVendorDiscountRef(null);
                    }

                    if(discountDelta.getStatus().isPresent()) {
                        priceLineDTO.setStatus(discountDelta.getStatus().get());
                    }
                }
            }
            recursiveApplyDiscount(discounts, dto.getChildren());
        }
    }

    public void reCalculateAggregationDiscount(List<PriceLineModel> priceLineModels) {
        if (!isEmpty(priceLineModels)) {
            PriceLineModel aggregatedPriceModel = getCustomerAggregatedPriceModel(priceLineModels);
            if (aggregatedPriceModel == null) {
                return;
            }
            List<PriceLineModel> childPriceModels = getChildPriceModelsForAggregated(aggregatedPriceModel.getSetAggregation(), priceLineModels);
            setDiscount(aggregatedPriceModel, childPriceModels, RECURRING);
            setDiscount(aggregatedPriceModel, childPriceModels, ONE_TIME);
        }
    }

    public void setDiscount(PriceLineModel aggregatedPriceModel, List<PriceLineModel> childPriceModels, PriceType type) {
        if (!childPriceModels.isEmpty() && isNotNull(aggregatedPriceModel.getPriceLineDTO(type))) {
            aggregatedPriceModel.setDiscount(getAverageDiscountFor(childPriceModels, type), type);
        }
    }

    protected PriceLineModel getCustomerAggregatedPriceModel(List<PriceLineModel> priceLineModels) {
        Optional<PriceLineModel> optionalPriceModel = tryFind(priceLineModels, new Predicate<PriceLineModel>() {
            @Override
            public boolean apply(PriceLineModel model) {
                return model.isCustomerAggregatedPrice();
            }
        });
        return optionalPriceModel.isPresent() ? optionalPriceModel.get() : null;
    }

    protected List<PriceLineModel> getChildPriceModelsForAggregated(final String setAggregation, @NotNull List<PriceLineModel> priceLineModels) {
        return newArrayList(filter(priceLineModels, new Predicate<PriceLineModel>() {
            @Override
            public boolean apply(PriceLineModel model) {
                return setAggregation.equals(model.getAggregationSet()) && model.isCustomerOrSalesAggregatedPrice();
            }
        }));
    }

    public Percentage getAverageDiscountFor(List<PriceLineModel> childPriceModels, final PriceType type) {
        final List<PriceLineDTO> priceLineDTOs = getPriceLinesBasedOnPriceType(childPriceModels, type);
        return calculateDiscount(priceLineDTOs);
    }

    public Percentage calculateDiscount(List<PriceLineDTO> priceLineDTOs) {
        Money originalPrice = Money.ZERO;
        Money netPrice = Money.ZERO;
        for (PriceLineDTO dto : priceLineDTOs) {
            Money price = Money.from(dto.getPrice(CHARGE_PRICE).price);
            BigDecimal discount = dto.getPrice(CHARGE_PRICE).discountPercentage;
            originalPrice = originalPrice.add(price);
            netPrice = netPrice.add(from(discount).applyTo(price));
        }
        return Percentage.from(originalPrice, netPrice);
    }

    public List<PriceLineDTO> getPriceLinesBasedOnPriceType(List<PriceLineModel> priceModels, final PriceType type) {
        List<PriceLineDTO> priceLineDTOs = newArrayList();
        for (PriceLineModel priceLineModel : priceModels) {
            PriceLineDTO priceLineDTO = priceLineModel.getPriceLineDTO(type);
            if (isNotNull(priceLineDTO)) {
                priceLineDTOs.add(priceLineDTO);
            }
        }
        return priceLineDTOs;
    }
}
