package com.bt.rsqe.expedio.audit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/26/15
 * Time: 8:08 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AuditSummaryDTO {
   private String quoteRefID;
    private String quoteName;
    private String  lastUpdatedDateTime;
    private String  lastUpdatedSummary;
    private String  lastUpdatedValue;
    private String  quoteStatus;
    private String  userName;
    private String  expedioReference;
    private String  expedioOrderid;
    private String  orderStatus;
    private String  orderSubStatus;
    private String  quoteVersion;

    public String getQuoteRefID() {
        return quoteRefID;
    }

    public void setQuoteRefID(String quoteRefID) {
        this.quoteRefID = quoteRefID;
    }

    public String getQuoteName() {
        return quoteName;
    }

    public void setQuoteName(String quoteName) {
        this.quoteName = quoteName;
    }

    public String getLastUpdatedDateTime() {
        return lastUpdatedDateTime;
    }

    public void setLastUpdatedDateTime(String lastUpdatedDateTime) {
        this.lastUpdatedDateTime = lastUpdatedDateTime;
    }

    public String getLastUpdatedSummary() {
        return lastUpdatedSummary;
    }

    public void setLastUpdatedSummary(String lastUpdatedSummary) {
        this.lastUpdatedSummary = lastUpdatedSummary;
    }

    public String getLastUpdatedValue() {
        return lastUpdatedValue;
    }

    public void setLastUpdatedValue(String lastUpdatedValue) {
        this.lastUpdatedValue = lastUpdatedValue;
    }

    public String getQuoteStatus() {
        return quoteStatus;
    }

    public void setQuoteStatus(String quoteStatus) {
        this.quoteStatus = quoteStatus;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getExpedioReference() {
        return expedioReference;
    }

    public void setExpedioReference(String expedioReference) {
        this.expedioReference = expedioReference;
    }

    public String getExpedioOrderid() {
        return expedioOrderid;
    }

    public void setExpedioOrderid(String expedioOrderid) {
        this.expedioOrderid = expedioOrderid;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderSubStatus() {
        return orderSubStatus;
    }

    public void setOrderSubStatus(String orderSubStatus) {
        this.orderSubStatus = orderSubStatus;
    }

    public String getQuoteVersion() {
        return quoteVersion;
    }

    public void setQuoteVersion(String quoteVersion) {
        this.quoteVersion = quoteVersion;
    }
}
