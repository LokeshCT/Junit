package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.domain.product.PriceType;
import com.bt.rsqe.domain.project.Price;
import com.bt.rsqe.domain.project.PricingStatus;

import java.math.BigDecimal;

public class BCMPriceLineInfo {

    private String priceLineName;
    private String id;
    private PriceType priceType;
    private Price chargePrice = Price.NIL;
    private Price eupPrice = Price.NIL;
    private PricingStatus status;
    private String pmfId;
    private String tariffType;
    private BigDecimal discountPercentage;

    public BCMPriceLineInfo(String priceLineName, String id, PriceType priceType, Price chargePrice, Price eupPrice, PricingStatus status, String pmfId, String tariffType, BigDecimal discountPercentage) {
        this.priceLineName = priceLineName;
        this.id = id;
        this.priceType = priceType;
        this.chargePrice = chargePrice;
        this.eupPrice = eupPrice;
        this.status = status;
        this.pmfId = pmfId;
        this.tariffType = tariffType;
        this.discountPercentage = discountPercentage;
    }

    public BCMPriceLineInfo() {
        this.priceLineName = "";
        this.id = "";
        this.priceType = null;
        this.chargePrice = new Price();;
        this.eupPrice = new Price();
        this.status = null;
        this.pmfId = "";
        this.tariffType = "";
        this.discountPercentage = new BigDecimal(0);
    }

    public String getPriceLineName() {
        return priceLineName;
    }

    public String getId() {
        return id;
    }

    public PriceType getPriceType() {
        return priceType;
    }

    public Price getChargePrice() {
        return chargePrice;
    }

    public Price getEupPrice() {
        return eupPrice;
    }

    public PricingStatus getStatus() {
        return status;
    }

    public String getPmfId() {
        return pmfId;
    }

    public String getTariffType() {
        return tariffType;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
}
