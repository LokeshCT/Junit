package com.asidua.statsintegration.services;

public class TestInvocationException extends Throwable {

    private static final long serialVersionUID = -6383074077374333822L;

    public TestInvocationException(String s, Exception e) {
        super(s,e);
    }
}
