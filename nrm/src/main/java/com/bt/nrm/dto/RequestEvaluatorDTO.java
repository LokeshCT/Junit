package com.bt.nrm.dto;

import com.bt.pms.dto.EvaluatorGroupDTO;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestEvaluatorDTO {

    private String requestEvaluatorId;
    private String evaluatorGroupId;
    private String evaluatorGroupName;
    private List<RequestEvaluatorSiteDTO> requestEvaluatorSites;
    private String state;
    private String response;
    private String acceptedBy;
    private Date acceptedDate;
    private Date closedDate;
    private String closedBy;
    private String comments;
    private String decision;
    private String currency;
    private String requestId;

    private Date createdDate;
    private Date modifiedDate;
    private String modifiedBy;

    private String acceptedByName;
    private String closedByName;
    private String createdUserName;
    private String modifiedUserName;


    public RequestEvaluatorDTO() {
    }

    public RequestEvaluatorDTO(String requestEvaluatorId, String evaluatorGroupId, String evaluatorGroupName, List<RequestEvaluatorSiteDTO> requestEvaluatorSites, String state, String response, String acceptedBy, Date acceptedDate, Date closedDate, String closedBy, String comments, String decision, String currency, String requestId, Date createdDate, Date modifiedDate, String modifiedBy, String acceptedByName, String closedByName, String createdUserName, String modifiedUserName) {
        this.requestEvaluatorId = requestEvaluatorId;
        this.evaluatorGroupId = evaluatorGroupId;
        this.evaluatorGroupName = evaluatorGroupName;
        this.requestEvaluatorSites = requestEvaluatorSites;
        this.state = state;
        this.response = response;
        this.acceptedBy = acceptedBy;
        this.acceptedDate = acceptedDate;
        this.closedDate = closedDate;
        this.closedBy = closedBy;
        this.comments = comments;
        this.decision = decision;
        this.currency = currency;
        this.requestId = requestId;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.modifiedBy = modifiedBy;
        this.acceptedByName = acceptedByName;
        this.closedByName = closedByName;
        this.createdUserName = createdUserName;
        this.modifiedUserName = modifiedUserName;
    }

    public String getRequestEvaluatorId() {
        return requestEvaluatorId;
    }

    public void setRequestEvaluatorId(String requestEvaluatorId) {
        this.requestEvaluatorId = requestEvaluatorId;
    }

    public String getEvaluatorGroupId() {
        return evaluatorGroupId;
    }

    public void setEvaluatorGroupId(String evaluatorGroupId) {
        this.evaluatorGroupId = evaluatorGroupId;
    }

    public String getEvaluatorGroupName() {
        return evaluatorGroupName;
    }

    public void setEvaluatorGroupName(String evaluatorGroupName) {
        this.evaluatorGroupName = evaluatorGroupName;
    }

    public List<RequestEvaluatorSiteDTO> getRequestEvaluatorSites() {
        return requestEvaluatorSites;
    }

    public void setRequestEvaluatorSites(List<RequestEvaluatorSiteDTO> requestEvaluatorSites) {
        this.requestEvaluatorSites = requestEvaluatorSites;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(String acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public Date getAcceptedDate() {
        return acceptedDate;
    }

    public void setAcceptedDate(Date acceptedDate) {
        this.acceptedDate = acceptedDate;
    }

    public Date getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Date closedDate) {
        this.closedDate = closedDate;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getAcceptedByName() {
        return acceptedByName;
    }

    public void setAcceptedByName(String acceptedByName) {
        this.acceptedByName = acceptedByName;
    }

    public String getClosedByName() {
        return closedByName;
    }

    public void setClosedByName(String closedByName) {
        this.closedByName = closedByName;
    }

    public String getCreatedUserName() {
        return createdUserName;
    }

    public void setCreatedUserName(String createdUserName) {
        this.createdUserName = createdUserName;
    }

    public String getModifiedUserName() {
        return modifiedUserName;
    }

    public void setModifiedUserName(String modifiedUserName) {
        this.modifiedUserName = modifiedUserName;
    }
}
