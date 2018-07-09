package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.Money;
import com.bt.rsqe.Percentage;
import com.bt.rsqe.customerinventory.dto.PriceDTO;
import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.enums.CostDiscountType;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.config.dto.BillingTariffRulesetConfig;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;
import com.bt.rsqe.projectengine.web.view.QuoteOptionPricingDTO;
import com.bt.rsqe.security.UserContextManager;

import java.util.List;

import static com.bt.rsqe.domain.product.chargingscheme.PricingStrategy.*;
import static com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme.PriceVisibility.*;
import static com.bt.rsqe.enums.PriceCategory.*;
import static com.bt.rsqe.enums.PriceType.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.newArrayList;

//FixMe: should use Money instead BigDecimal for all price amounts.
public class PriceLineModel {
    private PriceLineDTO oneTimePriceLine;
    private PriceLineDTO recurringPriceLine;
    private ProductChargingScheme scheme;
    private PricingClient pricingClient;
    private boolean isProvideAsset;
    List<PricingStrategy> manualPricingStrategies = newArrayList(PricingStrategy.ManualPricing, PricingStrategy.ManualWithApproval);

    public PriceLineModel(PriceLineDTO oneTimePriceLine, PriceLineDTO recurringPriceLine, ProductChargingScheme scheme, PricingClient pricingClient, boolean isProvideAsset) {
        if (oneTimePriceLine == null && recurringPriceLine == null) {
            throw new IllegalStateException("Must provide at least one of oneTimePriceLine or Recurring Price Line");
        }
        this.oneTimePriceLine = oneTimePriceLine;
        this.recurringPriceLine = recurringPriceLine;
        this.scheme = scheme;
        this.pricingClient = pricingClient;
        this.isProvideAsset = isProvideAsset;
    }

    public String getStatus() {
        return getPriceLineDTO().getStatus().getDescription();
    }

    public String getDescription() {
        return getPriceLineDTO().getDescription();
    }

    public String getUserEntered() {
        String userEntered = null;
        if (oneTimePriceLine != null) {
            userEntered = oneTimePriceLine.getUserEntered();
        }
        if (userEntered == null && recurringPriceLine != null) {
            userEntered = recurringPriceLine.getUserEntered();
        }
        return userEntered;
    }

    public PriceType getPriceType() {
        return getPriceLineDTO().getPriceType();
    }

    public QuoteOptionPricingDTO.PriceLineDTO getRecurringDto() {
        return dto(this.recurringPriceLine, scheme);
    }

    public QuoteOptionPricingDTO.PriceLineDTO getOneTimeDto() {
        return dto(this.oneTimePriceLine, scheme);
    }

    public QuoteOptionPricingDTO.PriceLineDTO createPriceFor(PriceDTO price) {
        final PriceModel priceModel = new PriceModel(price, scheme);
        final PriceLineDTO priceLineDTO = getPriceLineDTO();
        return new QuoteOptionPricingDTO.PriceLineDTO(priceLineDTO.getId(),
                                                      "",
                                                      priceModel.getPrice().toString(),
                                                      priceModel.getDiscountPercentage().toString(),
                                                      priceModel.getNetValue().toString(),
                                                      isDiscountEnabled(priceModel.getType(), scheme),
                                                      priceLineDTO.getVendorDiscountRef());
    }

    public boolean isDiscountApplicable(PriceLineDTO priceLineDTO, ProductChargingScheme scheme) {
        if(scheme == null) {
            return false;
        }
        String pmfId = getPriceLineDTO().getPmfId();
        final PricingConfig pricingConfig = pricingClient.getPricingConfig();
        List<BillingTariffRulesetConfig> billingTariffRulesetConfigList = pricingConfig.chargingSchemes()
                                                                                       .forName(scheme.getName())
                                                                                       .billingTariffRulesetConfigs();

        for(BillingTariffRulesetConfig rulesetConfig: billingTariffRulesetConfigList) {
            if(null != pmfId && pmfId.equalsIgnoreCase(rulesetConfig.getId())) {
                if(null != rulesetConfig.getCostDiscountApplicable()) {
                    return isProvideAsset
                           && (priceLineDTO.getPriceType().toString().equalsIgnoreCase(rulesetConfig.getCostDiscountApplicable().description()) || CostDiscountType.BOTH.equals(rulesetConfig.getCostDiscountApplicable()));
                }
            }
        }
        return (scheme.getPriceVisibility().equals(Sales) || scheme.getPriceVisibility().equals(Customer) && !scheme.getPricingStrategy().equals(Aggregation));
    }

    private QuoteOptionPricingDTO.PriceLineDTO dto(PriceLineDTO priceLineDTO, ProductChargingScheme scheme) {
        if (priceLineDTO == null) {
            return new QuoteOptionPricingDTO.PriceLineDTO();
        }
        final PriceModel priceModel = new PriceModel(priceLineDTO.getPrice(CHARGE_PRICE), scheme);
        return new QuoteOptionPricingDTO.PriceLineDTO(priceLineDTO.getId(),
                                                      getEUPPrice(priceLineDTO).toString(),
                                                      priceModel.getPrice().toString(),
                                                      priceModel.getDiscountPercentage().toString(),
                                                      priceModel.getNetValue().toString(),
                                                      isDiscountEnabled(priceLineDTO.getPriceType(), scheme),
                                                      priceLineDTO.getVendorDiscountRef());
    }

    private boolean isDiscountEnabled(PriceType priceType, ProductChargingScheme scheme) {
        if(scheme == null) {
            return false;
        }
        String pmfId = getPriceLineDTO().getPmfId();
        final PricingConfig pricingConfig = pricingClient.getPricingConfig();
        List<BillingTariffRulesetConfig> billingTariffRulesetConfigList = pricingConfig.chargingSchemes()
                                                                                       .forName(scheme.getName())
                                                                                       .billingTariffRulesetConfigs();

        for(BillingTariffRulesetConfig rulesetConfig: billingTariffRulesetConfigList) {
            if(null != pmfId && pmfId.equalsIgnoreCase(rulesetConfig.getId())) {
                if(null != rulesetConfig.getCostDiscountApplicable()) {
                    return isProvideAsset
                           && (priceType.getName().equalsIgnoreCase(rulesetConfig.getCostDiscountApplicable().description()) || CostDiscountType.BOTH.equals(rulesetConfig.getCostDiscountApplicable()));
                }
            }
        }
        return (scheme.getPriceVisibility().equals(Sales) || scheme.getPriceVisibility().equals(Customer) && !scheme.getPricingStrategy().equals(Aggregation))
               || (scheme.getPriceVisibility().equals(Sales) && scheme.getPricingStrategy().equals(Aggregation));
    }

    private Money getEUPPrice(PriceLineDTO priceLineDTO) {
        Money rrp = Money.ZERO;

        if (UserContextManager.getCurrent().getPermissions().eupAccess
            && priceLineDTO.getPrice(PriceCategory.END_USER_PRICE) != null) {
            rrp = Money.from(priceLineDTO.getPrice(PriceCategory.END_USER_PRICE).getPrice());
        }
        return rrp;
    }

    public Money getGrossOneTimeEUP() {
        if (oneTimePriceLine != null) {
            return new PriceModel(this.oneTimePriceLine.getPrice(PriceCategory.END_USER_PRICE), scheme).getPrice();
        }
        return Money.ZERO;
    }

    public Money getGrossRecurringEUP() {
        if (recurringPriceLine != null) {
            return new PriceModel(this.recurringPriceLine.getPrice(PriceCategory.END_USER_PRICE), scheme).getPrice();
        }
        return Money.ZERO;
    }

    public Money getOneTimeNetCPValue() {
        if (oneTimePriceLine != null) {
            return new PriceModel(this.oneTimePriceLine.getPrice(CHARGE_PRICE), scheme).getNetValue();
        }
        return Money.ZERO;
    }

    public Money getOneTimeCPValue() {
        if (oneTimePriceLine == null) {
            return Money.ZERO;
        }
        return new PriceModel(this.oneTimePriceLine.getPrice(CHARGE_PRICE), scheme).getValue();
    }

    public void setOneTimeCPValue(Money chargePrice) {
        oneTimePriceLine.getPrice(CHARGE_PRICE).price = chargePrice.toBigDecimal();
    }

    public Money getRecurringNetCPValue() {
        if (recurringPriceLine != null) {
            return new PriceModel(this.recurringPriceLine.getPrice(CHARGE_PRICE), scheme).getNetValue();
        }
        return Money.ZERO;
    }

    public Money getRecurringCPValue() {
        if (recurringPriceLine == null) {
            return Money.ZERO;
        }
        return new PriceModel(this.recurringPriceLine.getPrice(CHARGE_PRICE), scheme).getValue();
    }


    public Long getPpsrId() {
        return getPriceLineDTO().getPpsrId();
    }

    public Money getTotalChargePrice(int contractTerm) {
        return getOneTimeNetCPValue().add(getRecurringNetCPValue().multiplyBy(contractTerm));
    }

    public Money getTotalEUP(int contractTerm) {
        return getGrossOneTimeEUP().add(getGrossRecurringEUP().multiplyBy(contractTerm));
    }

    public Money getChargePricePerChannel(int numberOfChannelsRequired, int contractTerm) {

        return getTotalChargePrice(contractTerm).divideBy(numberOfChannelsRequired);

    }

    public Money getEUPPerChannel(int numberOfChannelsRequired, int contractTerm) {
        return getTotalEUP(contractTerm).divideBy(numberOfChannelsRequired);
    }

    public void setDiscount(Percentage discount, PriceType type) {
        if (discount != Percentage.NIL) {
            if (type == ONE_TIME && isNotNull(oneTimePriceLine) && isNotNull(oneTimePriceLine.getPrice(CHARGE_PRICE))) {
                PriceDTO oneTimePriceLinePrice = oneTimePriceLine.getPrice(CHARGE_PRICE);
                oneTimePriceLinePrice.discountPercentage = discount.toBigDecimal();
            } else if (type == RECURRING && isNotNull(recurringPriceLine) && isNotNull(recurringPriceLine.getPrice(CHARGE_PRICE)) ) {
                PriceDTO recurringPriceLinePrice = recurringPriceLine.getPrice(CHARGE_PRICE);
                recurringPriceLinePrice.discountPercentage = discount.toBigDecimal();
            } else {
                throw new UnsupportedOperationException(String.format("setDiscount for PriceType: %s not supported", type));
            }
        }
    }

    public void accept(LineItemVisitor priceVisitor) {
        priceVisitor.visit(this);
        if (oneTimePriceLine != null) {
            for (PriceDTO priceDTO : oneTimePriceLine.getPrices()) {
                priceVisitor.visit(new PriceModel(priceDTO, ONE_TIME, scheme));
            }
        }
        if (recurringPriceLine != null) {
            for (PriceDTO priceDTO : recurringPriceLine.getPrices()) {
                priceVisitor.visit(new PriceModel(priceDTO, RECURRING, scheme));
            }
        }
    }

    public PriceLineDTO getPriceLineDTO() {
        if (oneTimePriceLine != null) {
            return oneTimePriceLine;
        }
        return recurringPriceLine;
    }

    public boolean isCustomerAggregatedPrice() {
        return scheme != null && Customer.equals(scheme.getPriceVisibility()) && Aggregation.equals(scheme.getPricingStrategy());
    }

    public boolean isCustomerOrSalesAggregatedPrice() {
        if(scheme != null) {
            if(Customer.equals(scheme.getPriceVisibility()) || Sales.equals(scheme.getPriceVisibility())){
                return true;
            }
        }
        return false;
    }

    public String getAggregationSet() {
        return scheme != null ? scheme.getAggregationSet() : "";
    }

    public String getSetAggregation() {
        return scheme != null ? scheme.getSetAggregated() : "";
    }

    public PriceLineDTO getPriceLineDTO(PriceType type) {
        if (ONE_TIME.equals(type)) {
            return oneTimePriceLine;
        } else if (RECURRING.equals(type)) {
            return recurringPriceLine;
        }
        return null;
    }

    public ProductChargingScheme getScheme() {
        return scheme;
    }

    public boolean isManualPricing() {
        return scheme != null ? manualPricingStrategies.contains(scheme.getPricingStrategy()) : false;
    }
}
