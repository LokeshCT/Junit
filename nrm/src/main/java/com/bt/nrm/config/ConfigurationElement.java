package com.bt.nrm.config;

import java.util.List;

public interface ConfigurationElement {
    String getAttributeValue(String attributeName);

    boolean isSetAttributeValue(String attributeName);

    ConfigurationElement getChildElement(String name) throws ConfigurationException;

    List<? extends ConfigurationElement> getChildElements(String name);

    ConfigurationElement getChildElementById(String name, String id) throws ConfigurationException;

    String getName();

    ConfigurationElement dereference();

    boolean isReference();
}
