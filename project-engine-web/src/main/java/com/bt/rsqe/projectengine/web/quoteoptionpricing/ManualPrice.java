package com.bt.rsqe.projectengine.web.quoteoptionpricing;

import java.math.BigDecimal;

public class ManualPrice {

    private final BigDecimal gross;
    private final String type;
    private final String productDescription;
    private final String id;

    public ManualPrice(String id, BigDecimal gross, String type, String productDescription) {
        this.gross = gross;
        this.type = type;
        this.productDescription = productDescription;
        this.id = id;
    }

    public BigDecimal getGross() {
        return gross;
    }

    public String getType() {
        return type;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public String getId() {
        return id;
    }
}
