package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.Money;
import com.bt.rsqe.Percentage;
import com.bt.rsqe.customerinventory.dto.PriceDTO;
import com.bt.rsqe.domain.AggregationSet;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;

import java.math.BigDecimal;

import static com.bt.rsqe.utils.AssertObject.isNotNull;

public class PriceModel {
    private PriceDTO priceDTO;
    private PriceType priceType;
    private ProductChargingScheme scheme;

    public PriceModel(PriceDTO priceDTO, ProductChargingScheme scheme) {
        this(priceDTO, PriceType.UNSPECIFIED, scheme);
    }

    public PriceModel(PriceDTO priceDTO, PriceType priceType, ProductChargingScheme scheme) {
        this.priceDTO = priceDTO;
        this.priceType = priceType;
        this.scheme = scheme;
    }

    public Money getPrice() {
        return Money.from(priceDTO.getPrice());
    }

    public Percentage getDiscountPercentage() {
        return priceDTO.getDiscountPercentage()!= null? Percentage.from(priceDTO.getDiscountPercentage()):Percentage.from(0);
    }

    public Money getValue() {
        BigDecimal price = priceDTO.getPrice();
        BigDecimal value = price == null ? BigDecimal.ZERO : price;
        return Money.from(value);
    }

    public Money getNetValue() {
        Money price = Money.from(priceDTO.getPrice());
        return getDiscountPercentage().applyTo(price);
    }

    public PriceType getType() {
        return priceType;
    }

    public ProductChargingScheme getScheme(){
        return scheme;
    }

    public boolean isSatisfiedBy(PriceType priceType, PriceCategory priceCategory) {
        return this.priceType == priceType && this.priceDTO.category == priceCategory;
    }
}
