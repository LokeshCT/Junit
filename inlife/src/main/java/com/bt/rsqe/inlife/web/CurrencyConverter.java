package com.bt.rsqe.inlife.web;

import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.keys.PriceUpdateKey;

import java.math.BigDecimal;
import java.util.Map;

public interface CurrencyConverter {
    Map<PriceUpdateKey, BigDecimal> getConvertedPrices(ProductInstance productInstance, String currency);
    public String getPmfid();
}
