package com.bt.cqm.config;

public class ConfigurationException extends RuntimeException {
    public ConfigurationException(Throwable throwable) {
        super(throwable);
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable t) {
        super(message, t);
    }
}
