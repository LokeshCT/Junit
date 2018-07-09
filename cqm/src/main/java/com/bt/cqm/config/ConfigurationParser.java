package com.bt.cqm.config;


import java.io.IOException;

public interface ConfigurationParser {
    ConfigurationElement parse() throws ConfigurationException, IOException;
}
