package com.bt.rsqe.projectengine;

public enum QuoteOptionContractTerm {
    TWELVE("12","12"),
    TWENTY_FOUR("24","24"),
    THIRTY_SIX("36","36"),
    FORTY_EIGHT("48","48"),
    SIXTY("60","60");

    private String value;
    private String description;

    QuoteOptionContractTerm(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
