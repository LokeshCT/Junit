package com.bt.rsqe.expedio.audit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/26/15
 * Time: 8:14 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AuditDetailDTO {

    private String quoteRefID;
    private String quoteName;
    private String task;
    private String auditEvent;
    private String oldValue;
    private String newValue;
    private String quoteStatus;
    private String dateTime;
    private String userName;
    private String expedioReference;
    private String expedioOrderid;
    private String orderStatus;

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

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getAuditEvent() {
        return auditEvent;
    }

    public void setAuditEvent(String auditEvent) {
        this.auditEvent = auditEvent;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getQuoteStatus() {
        return quoteStatus;
    }

    public void setQuoteStatus(String quoteStatus) {
        this.quoteStatus = quoteStatus;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
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
}
