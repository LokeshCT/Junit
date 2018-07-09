package com.bt.rsqe.projectengine.web.uri;

import com.bt.rsqe.configuration.UrlConfig;

import java.util.HashMap;
import java.util.Map;

public abstract class ConfiguratorProduct {

    protected final Map<String, String> contextUrls;

    public ConfiguratorProduct(UrlConfig[] urls) {
        contextUrls = new HashMap<String, String>();
        for (UrlConfig url : urls) {
            contextUrls.put(url.getContext(), url.getUrl());
        }
    }

    String getBulkUrl(String... args) {
        return getUrl(UriContext.BULK, args);
    }

    String getBulkTemplateUrl(String... args) {
        return getUrl(UriContext.BULK_TEMPLATE, args);
    }

    String getConfigUrl(String... args) {
        return getUrl(UriContext.CONFIG, args);
    }

    String getCreateUrl(String... args) {
        return getUrl(UriContext.CREATE, args);
    }

    String getBulkViewUrl(String... args) {
        return getUrl(UriContext.BULK_VIEW, args);
    }

    String getLocateOnGoogleMapsUrl(String... args) {
        return getUrl(UriContext.LOCATE_ON_GOOGLE_MAPS, args);
    }

    public boolean isDefault() {
        return this instanceof DefaultConfiguratorProduct;
    }

    private String getUrl(UriContext context, String[] args) {
        if (contextUrls.containsKey(context.name())) {
            return String.format(contextUrls.get(context.name()), args);
        }
        return "";
    }

}

