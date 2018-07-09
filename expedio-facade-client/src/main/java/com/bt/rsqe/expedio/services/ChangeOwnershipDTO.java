package com.bt.rsqe.expedio.services;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ChangeOwnershipDTO {

    @XmlElement(name = "activityID")
    private String activityID;

    @XmlElement(name = "assignedTo")
    private String assignedTo;

    @XmlElement(name = "userLogin")
    private String userLogin;

    @XmlElement(name = "assigneeSalesUsercomments")
    private String assigneeSalesUsercomments;

    @XmlElement(name = "bidManagerComments")
    private String bidManagerComments;

    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getAssigneeSalesUsercomments() {
        return assigneeSalesUsercomments;
    }

    public void setAssigneeSalesUsercomments(String assigneeSalesUsercomments) {
        this.assigneeSalesUsercomments = assigneeSalesUsercomments;
    }

    public String getBidManagerComments() {
        return bidManagerComments;
    }

    public void setBidManagerComments(String bidManagerComments) {
        this.bidManagerComments = bidManagerComments;
    }
}
