package com.bt.nrm.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestWhoHasSeenDTO {

    private String requestWhoHasSeenId;
    private String state;
    private Timestamp createdDate;
    private String createdUser;
    private String createdUserName;
    private String requestId;

    public RequestWhoHasSeenDTO() {
    }

    public RequestWhoHasSeenDTO(String requestWhoHasSeenId, String state, Timestamp createdDate, String createdUser, String createdUserName, String requestId) {
        this.requestWhoHasSeenId = requestWhoHasSeenId;
        this.state = state;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.createdUserName = createdUserName;
        this.requestId = requestId;
    }

    public String getRequestWhoHasSeenId() {
        return requestWhoHasSeenId;
    }

    public void setRequestWhoHasSeenId(String requestWhoHasSeenId) {
        this.requestWhoHasSeenId = requestWhoHasSeenId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public String getCreatedUserName() {
        return createdUserName;
    }

    public void setCreatedUserName(String createdUserName) {
        this.createdUserName = createdUserName;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
