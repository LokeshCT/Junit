package com.bt.cqm.repository.customer;

public class CustomerNotFoundException extends Exception {

    public CustomerNotFoundException(final String message) {
        super(message);
    }

    public CustomerNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CustomerNotFoundException(final Throwable cause) {
        super(cause);
    }
}
