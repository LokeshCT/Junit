package com.bt.cqm.model;

import com.bt.rsqe.customerinventory.pagination.QueryResult;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CustomerListModel {

    private boolean displayErrors;
    private String customerName;
    private String salesChannel;
    private String errorMessage;
    private QueryResult customers;

    public boolean isDisplayErrors() {
        return displayErrors;
    }

    public void setDisplayErrors(boolean displayErrors) {
        this.displayErrors = displayErrors;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(String salesChannel) {
        this.salesChannel = salesChannel;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public QueryResult getCustomers() {
        return customers;
    }

    public void setCustomers(QueryResult customers) {
        this.customers = customers;
    }


    public CustomerListModel(boolean displayErrors, String customerName, String salesChannel, String errorMessage, QueryResult customers) {
        this.displayErrors = displayErrors;
        this.customerName = customerName;
        this.salesChannel = salesChannel;
        this.errorMessage = errorMessage;
        this.customers = customers;
    }

    public CustomerListModel(){

    }

}
