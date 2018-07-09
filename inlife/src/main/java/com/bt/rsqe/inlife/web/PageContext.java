package com.bt.rsqe.inlife.web;

import com.google.gson.GsonBuilder;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class PageContext {
    private final Map<String, Object> values = newHashMap();

    private PageContext() {
    }

    public static PageContext pageContext() {
        return new PageContext();
    }

    public PageContext withNavigationListUri(String uri) {
        values.put("navigationListUri", uri);
        return this;
    }

    public PageContext withLocationListUri(String uri) {
        values.put("locationListUri", uri);
        return this;
    }

    public PageContext withTransactionTargetsUri(String uri) {
        values.put("transactionTargetsUri", uri);
        return this;
    }

    public PageContext withNavigationWebMetricsUri(String uri) {
        values.put("navigationWebMetricsUri", uri);
        return this;
    }

    public PageContext withPercentileWebMetricsUri(String uri) {
        values.put("percentileWebMetricsUri", uri);
        return this;
    }

    public PageContext withCountryWisePercentageUri(String uri) {
        values.put("countryWisePercentageUri", uri);
        return this;
    }

    public PageContext withKeyTransactionTargetUri(String uri) {
        values.put("updateKeyTransactionTargetUri", uri);
        return this;
    }

    public PageContext withExportRawDateUri(String uri) {
        values.put("dataExportUri", uri);
        return this;
    }

    @Override
    public String toString() {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(values);
    }
}
