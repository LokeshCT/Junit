package com.bt.rsqe.projectengine.web;

public class OfferAndOrderValidationResult {

    public static final OfferAndOrderValidationResult SUCCESS = new OfferAndOrderValidationResult(true, "");

    boolean isValid;
    String errorMessage;

    public OfferAndOrderValidationResult(boolean valid, String errorMessage) {
        isValid = valid;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
