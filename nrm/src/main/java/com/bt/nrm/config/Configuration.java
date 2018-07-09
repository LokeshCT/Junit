package com.bt.nrm.config;

import java.io.IOException;

// Java's dynamic proxy API is not yet generified, so unchecked casts are unavoidable
@SuppressWarnings({"unchecked"})
public class Configuration<T> {
    private final T configurationRoot;

    public Configuration(Class<T> rootElementClass, ConfigurationParser configurationSource) throws IOException {
        configurationRoot = ConfigurationInvocationHandler.createElementInstance(rootElementClass, configurationSource.parse());
    }

    public T getConfigurationRoot() {
        return configurationRoot;
    }
}
