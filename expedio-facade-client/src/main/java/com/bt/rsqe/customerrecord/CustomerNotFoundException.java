package com.bt.rsqe.customerrecord;

public class CustomerNotFoundException extends Exception {
    public CustomerNotFoundException(final String message) {
        super(message);
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
