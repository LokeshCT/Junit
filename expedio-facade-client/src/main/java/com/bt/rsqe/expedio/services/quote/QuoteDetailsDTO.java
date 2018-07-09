package com.bt.rsqe.expedio.services.quote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class QuoteDetailsDTO {

    @XmlElement (name = "quoteRefID")
    private String quoteReferenceId;

    @XmlElement (name = "customerName")
    private String customerName;

    @XmlElement (name = "customerID")
    private String customerId;

    @XmlElement (name = "salesOrganization")
    private String salesChannel;

    @XmlElement (name = "salesRepName")
    private String salesUserName;

    @XmlElement (name = "orderType")
    private String orderType;

    @XmlElement (name = "quoteVersion")
    private String quoteVersion;

    @XmlElement (name = "quoteStatus")
    private String quoteStatus;

    @XmlElement (name = "quoteName")
    private String quoteName;

    @XmlElement (name = "contractTerm")
    private String contractTerm;

    @XmlElement (name = "bidNumber")
    private String bidNumber;

    @XmlElement (name = "siebelID")
    private String siebelId;

    @XmlElement (name = "currency")
    private String currency;

    @XmlElement (name = "comments")
    private String comments;

    @XmlElement (name = "expRefNo")
    private String expedioReferenceNumber;

    @XmlElement (name = "quoteVersionStatus")
    private String quoteVersionStatus;

    @XmlElement (name = "createdDate")
    private String createDate;

    @XmlElement (name = "modifiedBy")
    private String modifiedBy;

    @XmlElement (name = "modifiedDate")
    private String modifiedDate;

    @XmlElement (name = "productType")
    private String productType;

    @XmlElement (name = "subOrderType")
    private String subOrderType;

    @XmlElement (name = "salesRepLoginID")
    private String salesUserId;

    @XmlElement (name = "tradeLevel")
    private String tradeLevel;

    @XmlElement (name = "currencyID")
    private String currencyId;

    @XmlElement (name = "contractID")
    private String contractId;

    @XmlElement (name = "contractReference")
    private String contractReference;

    @XmlElement (name = "orderStatus")
    private String orderStatus;

    @XmlElement (name = "quoteIndicativeFlag")
    private String quoteIndicativeFlag;

    @XmlElement (name = "quoteOptionName")
    private String quoteOptionName;

    @XmlElement (name = "quoteExpiryDate")
    private String quoteExpiryDate;


    @XmlElement (name = "subGroup")
    private String subGroup;

    @XmlElement (name = "expired")
    private boolean isExpired;


    public QuoteDetailsDTO() {
    }

    public String getQuoteReferenceId() {
        return quoteReferenceId;
    }

    public void setQuoteReferenceId(String quoteReferenceId) {
        this.quoteReferenceId = quoteReferenceId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(String salesChannel) {
        this.salesChannel = salesChannel;
    }

    public String getSalesUserName() {
        return salesUserName;
    }

    public void setSalesUserName(String salesUserName) {
        this.salesUserName = salesUserName;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getQuoteVersion() {
        return quoteVersion;
    }

    public void setQuoteVersion(String quoteVersion) {
        this.quoteVersion = quoteVersion;
    }

    public String getQuoteStatus() {
        return quoteStatus;
    }

    public void setQuoteStatus(String quoteStatus) {
        this.quoteStatus = quoteStatus;
    }

    public String getQuoteName() {
        return quoteName;
    }

    public void setQuoteName(String quoteName) {
        this.quoteName = quoteName;
    }

    public String getContractTerm() {
        return contractTerm;
    }

    public void setContractTerm(String contractTerm) {
        this.contractTerm = contractTerm;
    }

    public String getBidNumber() {
        return bidNumber;
    }

    public void setBidNumber(String bidNumber) {
        this.bidNumber = bidNumber;
    }

    public String getSiebelId() {
        return siebelId;
    }

    public void setSiebelId(String siebelId) {
        this.siebelId = siebelId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getExpedioReferenceNumber() {
        return expedioReferenceNumber;
    }

    public void setExpedioReferenceNumber(String expedioReferenceNumber) {
        this.expedioReferenceNumber = expedioReferenceNumber;
    }

    public String getQuoteVersionStatus() {
        return quoteVersionStatus;
    }

    public void setQuoteVersionStatus(String quoteVersionStatus) {
        this.quoteVersionStatus = quoteVersionStatus;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getSubOrderType() {
        return subOrderType;
    }

    public void setSubOrderType(String subOrderType) {
        this.subOrderType = subOrderType;
    }

    public String getSalesUserId() {
        return salesUserId;
    }

    public void setSalesUserId(String salesUserId) {
        this.salesUserId = salesUserId;
    }

    public String getTradeLevel() {
        return tradeLevel;
    }

    public void setTradeLevel(String tradeLevel) {
        this.tradeLevel = tradeLevel;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getContractReference() {
        return contractReference;
    }

    public void setContractReference(String contractReference) {
        this.contractReference = contractReference;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getQuoteIndicativeFlag() {
        return quoteIndicativeFlag;
    }

    public void setQuoteIndicativeFlag(String quoteIndicativeFlag) {
        this.quoteIndicativeFlag = quoteIndicativeFlag;
    }

    public String getQuoteOptionName() {
        return quoteOptionName;
    }

    public void setQuoteOptionName(String quoteOptionName) {
        this.quoteOptionName = quoteOptionName;
    }

    public String getQuoteExpiryDate() {
        return quoteExpiryDate;
    }

    public void setQuoteExpiryDate(String quoteExpiryDate) {
        this.quoteExpiryDate = quoteExpiryDate;
    }

    public String getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(String subGroup) {
        this.subGroup = subGroup;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }
}
