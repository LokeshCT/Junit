package com.bt.rsqe.expedio.services.quote;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class QuoteUpdateDTO {
    @XmlElement
    private String quoteRefID;

    @XmlElement
    private String customerName;

    @XmlElement
    private String quoteVersion;

    @XmlElement
    private String saleschannel;

    @XmlElement
    private String EIN;

    @XmlElement
    private String quoteName;

    @XmlElement
    private String quoteIndicativeFlag;

    @XmlElement
    private String opportunityReferenceNumber;



    /**
     * Default constructor needed by JAXB
     */
    public QuoteUpdateDTO(){

    }

    public String getQuoteRefID() {
        return quoteRefID;
    }

    public void setQuoteRefID(String quoteRefID) {
        this.quoteRefID = quoteRefID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getQuoteVersion() {
        return quoteVersion;
    }

    public void setQuoteVersion(String quoteVersion) {
        this.quoteVersion = quoteVersion;
    }

    public String getSaleschannel() {
        return saleschannel;
    }

    public void setSaleschannel(String saleschannel) {
        this.saleschannel = saleschannel;
    }

    public String getEIN() {
        return EIN;
    }

    public void setEIN(String EIN) {
        this.EIN = EIN;
    }

    public String getQuoteName() {
        return quoteName;
    }

    public void setQuoteName(String quoteName) {
        this.quoteName = quoteName;
    }

    public String getQuoteIndicativeFlag() {
        return quoteIndicativeFlag;
    }

    public void setQuoteIndicativeFlag(String quoteIndicativeFlag) {
        this.quoteIndicativeFlag = quoteIndicativeFlag;
    }

    public String getOpportunityReferenceNumber() {
        return opportunityReferenceNumber;
    }

    public void setOpportunityReferenceNumber(String opportunityReferenceNumber) {
        this.opportunityReferenceNumber = opportunityReferenceNumber;
    }
}
