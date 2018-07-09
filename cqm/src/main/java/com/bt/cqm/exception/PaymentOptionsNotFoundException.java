package com.bt.cqm.exception;

public class PaymentOptionsNotFoundException extends Exception{

  public PaymentOptionsNotFoundException(final String message) {
        super(message);
    }

  /*  public PaymentOptionsNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public PaymentOptionsNotFoundException(final Throwable cause) {
        super(cause);
    }*/
}
