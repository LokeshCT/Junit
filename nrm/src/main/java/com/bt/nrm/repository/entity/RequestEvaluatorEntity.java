package com.bt.nrm.repository.entity;

import com.bt.nrm.dto.RequestEvaluatorDTO;
import com.bt.nrm.dto.RequestEvaluatorPriceGroupDTO;
import com.bt.nrm.dto.RequestEvaluatorSiteDTO;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.isNull;

@Entity
@Table(name="REQUEST_EVALUATOR")
public class RequestEvaluatorEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "REQUEST_EVALUATOR_ID")
    private String requestEvaluatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID")
    private RequestEntity requestEntity;

    @Column(name = "EVALUATOR_GROUP_ID")
    private String evaluatorGroupId;

    @Column(name = "EVALUATOR_GROUP_NAME")
    private String evaluatorGroupName;

    @OneToMany(mappedBy = "requestEvaluatorEntity",cascade = CascadeType.ALL)
    private List<RequestEvaluatorSiteEntity> requestEvaluatorSites;

    @Column(name = "STATE")
    private String state;

    @Column(name = "RESPONSE")
    private String response;

    @Column(name = "ACCEPTED_BY")
    private String acceptedBy;

    @Column(name = "ACCEPTED_DATE")
    private Timestamp acceptedDate;

    @Column(name = "CLOSED_DATE")
    private Timestamp closedDate;

    @Column(name = "CLOSED_BY")
    private String closedBy;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "MODIFIED_DATE")
    private Timestamp modifiedDate;

    @Column(name = "MODIFIED_USER")
    private String modifiedUser;

    @Column(name = "COMMENTS")
    @Lob
    private String comments;

    @Column(name = "DECISION")
    @Lob
    private String decision;

    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "ACCEPTED_BY_NAME")
    private String acceptedByName;

    @Column(name = "CLOSED_BY_NAME")
    private String closedByName;

    @Column(name = "CREATED_USER_NAME")
    private String createdUserName;

    @Column(name = "MODIFIED_USER_NAME")
    private String modifiedUserName;


    public RequestEvaluatorEntity() {
    }

    public RequestEvaluatorEntity(String requestEvaluatorId, RequestEntity requestEntity, String evaluatorGroupId, String evaluatorGroupName, List<RequestEvaluatorSiteEntity> requestEvaluatorSites, String state, String response, String acceptedBy, String acceptedByName, Timestamp acceptedDate, Timestamp closedDate, String closedBy, String closedByName, Timestamp createdDate, String createdUser, String createdUserName, Timestamp modifiedDate, String modifiedUser, String modifiedUserName, String comments, String decision, String currency) {
        this.requestEvaluatorId = requestEvaluatorId;
        this.requestEntity = requestEntity;
        this.evaluatorGroupId = evaluatorGroupId;
        this.evaluatorGroupName = evaluatorGroupName;
        this.requestEvaluatorSites = requestEvaluatorSites;
        this.state = state;
        this.response = response;
        this.acceptedBy = acceptedBy;
        this.acceptedByName = acceptedByName;
        this.acceptedDate = acceptedDate;
        this.closedDate = closedDate;
        this.closedBy = closedBy;
        this.closedByName = closedByName;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.createdUserName = createdUserName;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
        this.modifiedUserName = modifiedUserName;
        this.comments = comments;
        this.decision = decision;
        this.currency = currency;
    }


    public String getRequestEvaluatorId() {
        return requestEvaluatorId;
    }

    public void setRequestEvaluatorId(String requestEvaluatorId) {
        this.requestEvaluatorId = requestEvaluatorId;
    }

    public RequestEntity getRequestEntity() {
        return requestEntity;
    }

    public void setRequestEntity(RequestEntity requestEntity) {
        this.requestEntity = requestEntity;
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

    public List<RequestEvaluatorSiteEntity> getRequestEvaluatorSites() {
        return requestEvaluatorSites;
    }

    public void setRequestEvaluatorSites(List<RequestEvaluatorSiteEntity> requestEvaluatorSites) {
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

    public Timestamp getAcceptedDate() {
        return acceptedDate;
    }

    public void setAcceptedDate(Timestamp acceptedDate) {
        this.acceptedDate = acceptedDate;
    }

    public Timestamp getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Timestamp closedDate) {
        this.closedDate = closedDate;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
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

    public Timestamp getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Timestamp modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedUser() {
        return modifiedUser;
    }

    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
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

    public RequestEvaluatorDTO toDTO(RequestEvaluatorDTO dto){
        if(dto!=null){
            dto.setRequestEvaluatorId(this.getRequestEvaluatorId());
            dto.setEvaluatorGroupId(this.getEvaluatorGroupId());
            dto.setEvaluatorGroupName(this.getEvaluatorGroupName());
            if((isNull(this.getRequestEvaluatorSites())) || (this.getRequestEvaluatorSites().size() == 0)){
                dto.setRequestEvaluatorSites(new ArrayList<RequestEvaluatorSiteDTO>());
            }else{
                if(isNull(dto.getRequestEvaluatorSites())){
                    dto.setRequestEvaluatorSites(new ArrayList<RequestEvaluatorSiteDTO>());
                }
                for(RequestEvaluatorSiteEntity requestEvaluatorSiteEntity : this.getRequestEvaluatorSites()){
                    dto.getRequestEvaluatorSites().add(requestEvaluatorSiteEntity.toDTO(new RequestEvaluatorSiteDTO()));
                }
            }
            dto.setResponse(this.getResponse());
            dto.setState(this.getState());
            dto.setAcceptedBy(this.getAcceptedBy());
            dto.setAcceptedDate(this.getAcceptedDate());
            dto.setClosedBy(this.getClosedBy());
            dto.setClosedDate(this.getClosedDate());
            dto.setComments(this.getComments());
            dto.setDecision(this.getDecision());
            dto.setCurrency(this.getCurrency());
            dto.setRequestId(this.getRequestEntity().getRequestId());
            dto.setCreatedDate(this.getCreatedDate());
            dto.setModifiedDate(this.getModifiedDate());
            dto.setModifiedBy(this.getModifiedUser());
            dto.setAcceptedByName(this.getAcceptedByName());
            dto.setClosedByName(this.getClosedByName());
            dto.setCreatedUserName(this.getCreatedUserName());
            dto.setModifiedUserName(this.getModifiedUserName());
        }
        return dto;
    }

    public RequestEvaluatorDTO toNewDTO(){
        return toDTO(new RequestEvaluatorDTO());
    }


}
