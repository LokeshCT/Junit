package com.bt.cqm.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 05/03/14
 * Time: 18:04
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ChannelHierarchyDTO {


    @XmlElement
    private String customerID;
    @XmlElement
    private String parentCustomerName;
    @XmlElement
    private String parentAccountReference;
    @XmlElement
    private String billingAccount;
    @XmlElement
    private String tradeLevel;
    @XmlElement
    private String yearlyCommittedRevenue;
    @XmlElement
    private String salesChannelType;
    @XmlElement
    private String accountType;
    private String productName;

    public ChannelHierarchyDTO(String parentCustomerName) {
        this.parentCustomerName = parentCustomerName;
    }


    public ChannelHierarchyDTO(String customerID, String parentAccountReference, String parentCustomerName, String billingAccount, String tradeLevel) {
        this.customerID = customerID;
        this.parentCustomerName = parentCustomerName;
        this.parentAccountReference = parentAccountReference;
        this.billingAccount = billingAccount;
        this.tradeLevel = tradeLevel;
    }

    ///CLOVER:OFF

    public ChannelHierarchyDTO() {
        // required by jaxb
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getParentCustomerName() {
        return parentCustomerName;
    }

    public void setParentCustomerName(String parentCustomerName) {
        this.parentCustomerName = parentCustomerName;
    }

    public String getParentAccountReference() {
        return parentAccountReference;
    }

    public void setParentAccountReference(String parentAccountReference) {
        this.parentAccountReference = parentAccountReference;
    }

    public String getBillingAccount() {
        return billingAccount;
    }

    public void setBillingAccount(String billingAccount) {
        this.billingAccount = billingAccount;
    }

    public String getTradeLevel() {
        return tradeLevel;
    }

    public void setTradeLevel(String tradeLevel) {
        this.tradeLevel = tradeLevel;
    }

    public String getYearlyCommittedRevenue() {
        return yearlyCommittedRevenue;
    }

    public void setYearlyCommittedRevenue(String yearlyCommittedRevenue) {
        this.yearlyCommittedRevenue = yearlyCommittedRevenue;
    }

    public String getSalesChannelType() {
        return salesChannelType;
    }

    public void setSalesChannelType(String salesChannelType) {
        this.salesChannelType = salesChannelType;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    ///CLOVER:ON
}
