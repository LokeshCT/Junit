package com.bt.nrm.config;


import java.io.IOException;

public interface ConfigurationParser {
    ConfigurationElement parse() throws ConfigurationException, IOException;
}
