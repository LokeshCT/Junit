package com.bt.nrm.repository.entity;


import com.bt.nrm.dto.request.QuoteDTO;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "QUOTE_MASTER")
public class QuoteEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "QUOTE_MASTER_ID")
    private String quoteMasterId;
    @Column(name = "QUOTE_ID")
    private String quoteId;
    @Column(name = "QUOTE_NAME")
    private String quoteName;
    @Column(name = "QUOTE_OPTION_ID")
    private String quoteOptionId;
    @Column(name = "QUOTE_OPTION_NAME")
    private String quoteOptionName;
    @Column(name = "CUSTOMER_ID")
    private String customerId;
    @Column(name = "CUSTOMER_NAME")
    private String customerName;
    @Column(name = "CUSTOMER_MORE_INFO")
    private String customerMoreInfo;
    @Column(name = "SOURCE_SYSTEM")
    private String sourceSystem;
    @Column(name = "SALES_CHANNEL_ID")
    private String salesChannelId;
    @Column(name = "SALES_CHANNEL_NAME")
    private String salesChannelName;
    @Column(name = "SALES_CHANNEL_TYPE")
    private String salesChannelType;
    @Column(name = "CURRENCY")
    private String currency;
    @Column(name = "TIER")
    private String tier;
    @Column(name = "CONTRACT_VALUE")
    private String contractValue;
    @Column(name = "CONTRACT_LENGTH")
    private String contractLegth;
    @Column(name = "ALTERNATE_CONTACT")
    private String alternateContact;
    @Column(name = "WIN_CHANCE")
    private String winChance;
    @Column(name = "CREATED_BY_EIN")
    private String createdByEIN;
    @Column(name = "CREATED_BY_EMAIL")
    private String createdByEmailId;
    @Column(name = "CREATED_USER")
    private String createdByUserName;
    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;
    @Column(name = "MODIFIED_DATE")
    private Timestamp modifiedDate;
    @Column(name = "MODIFIED_USER")
    private String modifiedUser;

    public QuoteEntity() {
    }

    public QuoteEntity(String quoteMasterId, String quoteId, String quoteName, String quoteOptionId, String quoteOptionName, String customerId, String customerName, String customerMoreInfo, String sourceSystem, String salesChannelId, String salesChannelName, String salesChannelType, String currency, String tier, String contractValue, String contractLegth, String alternateContact, String winChance, String createdByEIN, String createdByEmailId, String createdByUserName, Timestamp createdDate, Timestamp modifiedDate, String modifiedUser) {
        this.quoteMasterId = quoteMasterId;
        this.quoteId = quoteId;
        this.quoteName = quoteName;
        this.quoteOptionId = quoteOptionId;
        this.quoteOptionName = quoteOptionName;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerMoreInfo = customerMoreInfo;
        this.sourceSystem = sourceSystem;
        this.salesChannelId = salesChannelId;
        this.salesChannelName = salesChannelName;
        this.salesChannelType = salesChannelType;
        this.currency = currency;
        this.tier = tier;
        this.contractValue = contractValue;
        this.contractLegth = contractLegth;
        this.alternateContact = alternateContact;
        this.winChance = winChance;
        this.createdByEIN = createdByEIN;
        this.createdByEmailId = createdByEmailId;
        this.createdByUserName = createdByUserName;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
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

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getContractValue() {
        return contractValue;
    }

    public void setContractValue(String contractValue) {
        this.contractValue = contractValue;
    }

    public String getContractLegth() {
        return contractLegth;
    }

    public void setContractLegth(String contractLegth) {
        this.contractLegth = contractLegth;
    }

    public String getAlternateContact() {
        return alternateContact;
    }

    public void setAlternateContact(String alternateContact) {
        this.alternateContact = alternateContact;
    }

    public String getWinChance() {
        return winChance;
    }

    public void setWinChance(String winChance) {
        this.winChance = winChance;
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

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }

    public Timestamp getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Timestamp modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedUser() {
        return modifiedUser;
    }



    public QuoteDTO toDTO(QuoteDTO dto){
        if(dto!=null){
            dto.setQuoteMasterId(this.getQuoteMasterId());
            dto.setQuoteId(this.getQuoteId());
            dto.setQuoteName(this.getQuoteName());
            dto.setQuoteOptionId(this.getQuoteOptionId());
            dto.setQuoteOptionName(this.getQuoteOptionName());
            dto.setCustomerId(this.getCustomerId());
            dto.setCustomerName(this.getCustomerName());
            dto.setCustomerMoreInfo(this.getCustomerMoreInfo());
            dto.setQuoteCurrency(this.getCurrency());
            dto.setTier(this.getTier());
            dto.setSalesChannelId(this.getSalesChannelId());
            dto.setSalesChannelName(this.getSalesChannelName());
            dto.setSalesChannelType(this.getSalesChannelType());
            dto.setSourceSystem(this.getSourceSystem());
            dto.setContractValue(this.getContractValue());
            dto.setContractLength(this.getContractLegth());
            dto.setAlternateContact(this.getAlternateContact());
            dto.setWinChance(this.getWinChance());
            dto.setCreatedByEIN(this.getCreatedByEIN());
            dto.setCreatedByUserName(this.getCreatedByUserName());
            dto.setCreatedByEmailId(this.getCreatedByEmailId());
            dto.setCreatedDate(this.getCreatedDate());
        }
        return dto;
    }

    public QuoteDTO toNewDTO(){
        return toDTO(new QuoteDTO());
    }
}
