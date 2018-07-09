package com.bt.nrm.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class QuoteDTO {

    private String quoteMasterId;
    private String quoteId;
    private String quoteName;
    private String quoteOptionId;
    private String quoteOptionName;
    private String quoteCurrency;
    private String contractLength;
    private String tier;
    private String winChance;
    private String sourceSystem;
    private String customerId;
    private String customerName;
    private String customerMoreInfo;
    private String opportunityReferenceNo;
    private String alternateContact;
    private String salesChannelId;
    private String salesChannelName;
    private String salesChannelType;
    private String contractValue;
    private String contractValueCurrency;
    private String createdByEIN;
    private String createdByEmailId;
    private String createdByUserName;
    private Date createdDate;

    public QuoteDTO() {
    }

    public QuoteDTO(String quoteMasterId, String quoteId, String quoteName, String quoteOptionId, String quoteOptionName, String quoteCurrency, String contractLength, String tier, String winChance, String sourceSystem, String customerId, String customerName, String customerMoreInfo, String opportunityReferenceNo, String alternateContact, String salesChannelId, String salesChannelName, String salesChannelType, String contractValue, String contractValueCurrency, String createdByEIN, String createdByEmailId, String createdByUserName, Date createdDate) {
        this.quoteMasterId = quoteMasterId;
        this.quoteId = quoteId;
        this.quoteName = quoteName;
        this.quoteOptionId = quoteOptionId;
        this.quoteOptionName = quoteOptionName;
        this.quoteCurrency = quoteCurrency;
        this.contractLength = contractLength;
        this.tier = tier;
        this.winChance = winChance;
        this.sourceSystem = sourceSystem;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerMoreInfo = customerMoreInfo;
        this.opportunityReferenceNo = opportunityReferenceNo;
        this.alternateContact = alternateContact;
        this.salesChannelId = salesChannelId;
        this.salesChannelName = salesChannelName;
        this.salesChannelType = salesChannelType;
        this.contractValue = contractValue;
        this.contractValueCurrency = contractValueCurrency;
        this.createdByEIN = createdByEIN;
        this.createdByEmailId = createdByEmailId;
        this.createdByUserName = createdByUserName;
        this.createdDate = createdDate;
    }

    public String getQuoteMasterId() {
        return quoteMasterId;
    }

    public void setQuoteMasterId(String quoteMasterId) {
        this.quoteMasterId = quoteMasterId;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public String getQuoteName() {
        return quoteName;
    }

    public void setQuoteName(String quoteName) {
        this.quoteName = quoteName;
    }

    public String getQuoteOptionId() {
        return quoteOptionId;
    }

    public void setQuoteOptionId(String quoteOptionId) {
        this.quoteOptionId = quoteOptionId;
    }

    public String getQuoteOptionName() {
        return quoteOptionName;
    }

    public void setQuoteOptionName(String quoteOptionName) {
        this.quoteOptionName = quoteOptionName;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public void setQuoteCurrency(String quoteCurrency) {
        this.quoteCurrency = quoteCurrency;
    }

    public String getContractLength() {
        return contractLength;
    }

    public void setContractLength(String contractLength) {
        this.contractLength = contractLength;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getWinChance() {
        return winChance;
    }

    public void setWinChance(String winChance) {
        this.winChance = winChance;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerMoreInfo() {
        return customerMoreInfo;
    }

    public void setCustomerMoreInfo(String customerMoreInfo) {
        this.customerMoreInfo = customerMoreInfo;
    }

    public String getOpportunityReferenceNo() {
        return opportunityReferenceNo;
    }

    public void setOpportunityReferenceNo(String opportunityReferenceNo) {
        this.opportunityReferenceNo = opportunityReferenceNo;
    }

    public String getAlternateContact() {
        return alternateContact;
    }

    public void setAlternateContact(String alternateContact) {
        this.alternateContact = alternateContact;
    }

    public String getSalesChannelId() {
        return salesChannelId;
    }

    public void setSalesChannelId(String salesChannelId) {
        this.salesChannelId = salesChannelId;
    }

    public String getSalesChannelName() {
        return salesChannelName;
    }

    public void setSalesChannelName(String salesChannelName) {
        this.salesChannelName = salesChannelName;
    }

    public String getSalesChannelType() {
        return salesChannelType;
    }

    public void setSalesChannelType(String salesChannelType) {
        this.salesChannelType = salesChannelType;
    }

    public String getContractValue() {
        return contractValue;
    }

    public void setContractValue(String contractValue) {
        this.contractValue = contractValue;
    }

    public String getContractValueCurrency() {
        return contractValueCurrency;
    }

    public void setContractValueCurrency(String contractValueCurrency) {
        this.contractValueCurrency = contractValueCurrency;
    }

    public String getCreatedByEIN() {
        return createdByEIN;
    }

    public void setCreatedByEIN(String createdByEIN) {
        this.createdByEIN = createdByEIN;
    }

    public String getCreatedByEmailId() {
        return createdByEmailId;
    }

    public void setCreatedByEmailId(String createdByEmailId) {
        this.createdByEmailId = createdByEmailId;
    }

    public String getCreatedByUserName() {
        return createdByUserName;
    }

    public void setCreatedByUserName(String createdByUserName) {
        this.createdByUserName = createdByUserName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}

