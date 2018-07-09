package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.projectengine.web.uri.UriFactory;

public class BulkTemplateDialogView {
    private Products products;
    private UriFactory uriFactory;
    private String customerId;
    private String defaultSCode;
    private String projectId;
    private String quoteOptionId;
    private String currency;

    public BulkTemplateDialogView(String customerId, String projectId, String quoteOptionId, Products products, UriFactory uriFactory, String defaultSCode, String currency) {
        this.products = products;
        this.uriFactory = uriFactory;
        this.quoteOptionId = quoteOptionId;
        this.projectId = projectId;
        this.customerId = customerId;
        this.defaultSCode = defaultSCode;
        this.currency = currency;
    }

    public Products getProducts() {
        return products;
    }

    public String getBulkTemplateUri(String productId){
        return uriFactory.getBulkTemplateUri(productId, customerId, projectId, quoteOptionId, currency);
    }

    public String getDefaultSCode() {
        return defaultSCode;
    }
}
