package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.domain.project.PricePoint;
import com.bt.rsqe.Money;

public class PricePointModel {

    private PricePoint pricePoint;

    public PricePointModel(PricePoint pricePoint) {
        this.pricePoint = pricePoint;
    }

    public Money getNetPrice() {
        return Money.from(pricePoint.getBasePrice().subtract(pricePoint.getDiscountValue()));
    }
}
