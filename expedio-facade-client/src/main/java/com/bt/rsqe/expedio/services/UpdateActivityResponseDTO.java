package com.bt.rsqe.expedio.services;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UpdateActivityResponseDTO {

    @XmlElement(name = "responseStatus")
    private String responseStatus;

    @XmlElement(name = "ActivityID")
    private String activityID;

    @XmlElement(name = "Status")
    private String status;

    @XmlElement(name = "SubStatus")
    private String subStatus;

    @XmlElement(name = "AssignedTo")
    private String assignedTo;

    @XmlElement(name = "AssignedtoEmailid")
    private String assignedToEmailID;

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(String subStatus) {
        this.subStatus = subStatus;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignedToEmailID() {
        return assignedToEmailID;
    }

    public void setAssignedToEmailID(String assignedToEmailID) {
        this.assignedToEmailID = assignedToEmailID;
    }
}
