package com.bt.nrm.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestHistoryDTO {

    private String requestHistoryId;
    private String stateFrom;
    private String stateTo;
    private Timestamp createdDate;
    private String createdUser;
    private String createdUserName;
    private String requestId;

    public RequestHistoryDTO() {
    }

    public RequestHistoryDTO(String requestHistoryId, String stateFrom, String stateTo, Timestamp createdDate, String createdUser, String createdUserName, String requestId) {
        this.requestHistoryId = requestHistoryId;
        this.stateFrom = stateFrom;
        this.stateTo = stateTo;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.createdUserName = createdUserName;
        this.requestId = requestId;
    }

    public String getRequestHistoryId() {
        return requestHistoryId;
    }

    public void setRequestHistoryId(String requestHistoryId) {
        this.requestHistoryId = requestHistoryId;
    }

    public String getStateFrom() {
        return stateFrom;
    }

    public void setStateFrom(String stateFrom) {
        this.stateFrom = stateFrom;
    }

    public String getStateTo() {
        return stateTo;
    }

    public void setStateTo(String stateTo) {
        this.stateTo = stateTo;
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
