package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

public class RFOImportException extends RuntimeException {
    public static final String DEFAULT_ERROR_MESSAGE = "RFO mandatory values are missing ";

    public RFOImportException(String message) {
        super(message);
    }
}
