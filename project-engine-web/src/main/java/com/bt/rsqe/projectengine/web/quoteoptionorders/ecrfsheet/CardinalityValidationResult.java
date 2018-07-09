package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CardinalityValidationResult {

    private boolean validationPass;
    private String errorMessage;

    public CardinalityValidationResult(boolean validationPass, String errorMessage) {
        this.validationPass = validationPass;
        this.errorMessage = errorMessage;
    }

    public static CardinalityValidationResult success() {
        return new CardinalityValidationResult(true, "");
    }

    public static CardinalityValidationResult failed(String errorMessage) {
        return new CardinalityValidationResult(false, errorMessage);
    }

    public boolean isFailed() {
        return !validationPass;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }


}
