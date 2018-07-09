package com.bt.rsqe.projectengine.web.uri;

import com.bt.rsqe.configuration.UrlConfig;

public class DefaultConfiguratorProduct extends ConfiguratorProduct {
    public DefaultConfiguratorProduct(UrlConfig[] urls) {
        super(urls);
    }

    @Override
    String getCreateUrl(String... args) {
        return String.format(contextUrls.get(UriContext.CREATE.name()), args);
    }
}
