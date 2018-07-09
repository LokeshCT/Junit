package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.Money;

public class NoSpecialPriceBookPricePointModel extends PricePointModel {
    private Money netPrice;

    public NoSpecialPriceBookPricePointModel(Money netPrice) {
        super(null); // NOTE: technically PricePointModel should be an interface instead of an concrete class
        this.netPrice = netPrice;
    }

    @Override
    public Money getNetPrice() {
        return netPrice;
    }
}
