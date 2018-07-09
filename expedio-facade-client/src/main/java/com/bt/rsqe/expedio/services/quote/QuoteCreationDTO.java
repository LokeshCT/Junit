package com.bt.rsqe.expedio.services.quote;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class QuoteCreationDTO {
    @XmlElement
    private String salesChannel;

    @XmlElement
    private String roleType;

    @XmlElement
    private String EIN;

    @XmlElement
    private String bfgCustomerID;

    @XmlElement
    private Long bfgContractID;

    @XmlElement
    private String salesRepName;

    @XmlElement
    private String orderType;

    @XmlElement
    private String quoteName;

    @XmlElement
    private String contractTerm;

    @XmlElement
    private String bidNumber;

    @XmlElement (name = "opprtunityReferenceNumber")
    private String opportunityReferenceNumber;

    @XmlElement
    private String currency;

    @XmlElement
    private String subOrderType;

    @XmlElement
    private String quoteIndicativeFlag;

    @XmlElement
    private String boatID;

    @XmlElement
    private String userRole;

    @XmlElement
    private String tradeLevel;

    @XmlElement
    private String userEmailId;

    @XmlElement
    private String subGroup;
 
 
    @XmlElement
    private String quoteId = null;


    @XmlElement
    private List<QuotePriceBookDTO> priceBookDetails;

    /**
     * Default constructor needed by JAXB
     */
    public QuoteCreationDTO(){

    }

    public QuoteCreationDTO(String quoteName, String contractTerm, String currency ){
        this.quoteName = quoteName;
        this.contractTerm = contractTerm;
        this.currency = currency;
    }


    public String getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(String salesChannel) {
        this.salesChannel = salesChannel;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getEIN() {
        return EIN;
    }

    public void setEIN(String EIN) {
        this.EIN = EIN;
    }

    public String getBfgCustomerID() {
        return bfgCustomerID;
    }

    public void setBfgCustomerID(String bfgCustomerID) {
        this.bfgCustomerID = bfgCustomerID;
    }

    public Long getBfgContractID() {
        return bfgContractID;
    }

    public void setBfgContractID(Long bfgContractID) {
        this.bfgContractID = bfgContractID;
    }

    public String getSalesRepName() {
        return salesRepName;
    }

    public void setSalesRepName(String salesRepName) {
        this.salesRepName = salesRepName;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
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

    public String getOpportunityReferenceNumber() {
        return opportunityReferenceNumber;
    }

    public void setOpportunityReferenceNumber(String opportunityReferenceNumber) {
        this.opportunityReferenceNumber = opportunityReferenceNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSubOrderType() {
        return subOrderType;
    }

    public void setSubOrderType(String subOrderType) {
        this.subOrderType = subOrderType;
    }

    public String getQuoteIndicativeFlag() {
        return quoteIndicativeFlag;
    }

    public void setQuoteIndicativeFlag(String quoteIndicativeFlag) {
        this.quoteIndicativeFlag = quoteIndicativeFlag;
    }

    public String getBoatID() {
        return boatID;
    }

    public void setBoatID(String boatID) {
        this.boatID = boatID;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getTradeLevel() {
        return tradeLevel;
    }

    public void setTradeLevel(String tradeLevel) {
        this.tradeLevel = tradeLevel;
    }

    public String getUserEmailId() {
        return userEmailId;
    }

    public void setUserEmailId(String userEmailId) {
        this.userEmailId = userEmailId;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public List<QuotePriceBookDTO> getPriceBookDetails() {
        return priceBookDetails;
    }

    public void setPriceBookDetails(List<QuotePriceBookDTO> priceBookDetails) {
        this.priceBookDetails = priceBookDetails;
    }

    public String getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(String subGroup) {
        this.subGroup = subGroup;
    }

    @Override
    public String toString() {
        return "QuoteCreationDTO{" +
                " quoteId='"+quoteId+'\''+
               ", salesChannel='" + salesChannel + '\'' +
               ", roleType='" + roleType + '\'' +
               ", EIN='" + EIN + '\'' +
               ", bfgCustomerID='" + bfgCustomerID + '\'' +
               ", bfgContractID=" + bfgContractID +
               ", salesRepName='" + salesRepName + '\'' +
               ", orderType='" + orderType + '\'' +
               ", quoteName='" + quoteName + '\'' +
               ", contractTerm='" + contractTerm + '\'' +
               ", bidNumber='" + bidNumber + '\'' +
               ", opportunityReferenceNumber='" + opportunityReferenceNumber + '\'' +
               ", currency='" + currency + '\'' +
               ", subOrderType='" + subOrderType + '\'' +
               ", quoteIndicativeFlag='" + quoteIndicativeFlag + '\'' +
               ", boatID='" + boatID + '\'' +
               ", userRole='" + userRole + '\'' +
               ", tradeLevel='" + tradeLevel + '\'' +
               ", userEmailId='" + userEmailId + '\'' +
               ", priceBookDetails='" + priceBookDetails + '\'' +
               ", subGroup='" + subGroup + '\'' +
               '}';
    }

}
