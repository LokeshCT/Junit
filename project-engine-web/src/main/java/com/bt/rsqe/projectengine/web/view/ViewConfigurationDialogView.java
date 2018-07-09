package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;

import java.util.List;

public class ViewConfigurationDialogView {
    private String customerId;
    private String projectId;
    private String formAction;
    private String contractId;
    private String quoteName;
    private String expRef;

    public String getViewConfigurationURI() {
        return viewConfigurationURI;
    }

    private String viewConfigurationURI;
    private List<QuoteOption> quoteOptions;
    private List<Offer> offers;
    private List<Order> orders;

    public ViewConfigurationDialogView(String customerId, String contractId, String projectId) {
        this.customerId = customerId;
        this.projectId = projectId;
        this.contractId = contractId;
        this.formAction = UriFactoryImpl.quoteOptions(customerId, contractId, projectId).toString();
        this.viewConfigurationURI = UriFactoryImpl.viewConfigurationURI(customerId, contractId, projectId).toString();
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getFormAction() {
        return formAction;
    }

    public String getContractId() {
        return contractId;
    }

    public void setQuoteOptions(List<QuoteOption> quoteOptions){
        this.quoteOptions = quoteOptions;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<QuoteOption> getQuoteOptions(){
        return quoteOptions;
    }

    public List<Offer> getOffers() {
        return offers;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public String getQuoteName() {
        return quoteName;
    }

    public String getExpRef() {
        return expRef;
    }

    public void setQuoteName(String quoteName) {
        this.quoteName = quoteName;
    }

    public void setExpRef(String expRef) {
        this.expRef = expRef;
    }
}
