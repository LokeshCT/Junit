package com.bt.rsqe.expedio.services.quote;

public class QuoteNotFoundException extends Exception{

  public QuoteNotFoundException(final String message) {
        super(message);
    }

  /*  public QuoteNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public QuoteNotFoundException(final Throwable cause) {
        super(cause);
    }*/
}
