package com.bt.rsqe.projectengine;

public enum QuoteOptionTier {
    Tier3,Tier4,Tier5;

    public String getDescription() {
        return this.toString();
    }

    public String getValue() {
        String tier = this.toString();
        tier = tier.substring(0,4)+"-"+tier.substring(4);
        return tier;
    }

}
