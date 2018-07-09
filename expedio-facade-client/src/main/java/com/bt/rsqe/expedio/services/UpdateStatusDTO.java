package com.bt.rsqe.expedio.services;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UpdateStatusDTO {

    @XmlElement(name = "activityID")
    private String activityID;

    @XmlElement(name = "state")
    private String state;

    @XmlElement(name = "substate")
    private String substate;

    @XmlElement(name = "bidManagerComments")
    private String bidManagerComment;

    @XmlElement(name = "closedDate")
    private String closedDate;


    @XmlElement(name = "salesChannel")
    private String salesChannel;

    @XmlElement(name = "customerName")
    private String customerName;


    @XmlElement(name = "assignedToEmailId")
    private String assignedToEmailId;


    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSubstate() {
        return substate;
    }

    public void setSubstate(String substate) {
        this.substate = substate;
    }

    public String getBidManagerComment() {
        return bidManagerComment;
    }

    public void setBidManagerComment(String bidManagerComment) {
        this.bidManagerComment = bidManagerComment;
    }

    public String getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(String closedDate) {
        this.closedDate = closedDate;
    }
    public String getSalesChannel() {
        return salesChannel;
}

    public String getCustomerName() {
        return customerName;
    }

    public String getAssignedToEmailId() {
        return assignedToEmailId;
    }
}
