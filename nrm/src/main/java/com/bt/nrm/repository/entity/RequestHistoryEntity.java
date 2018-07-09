package com.bt.nrm.repository.entity;

import com.bt.nrm.dto.RequestHistoryDTO;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "REQUEST_HISTORY")
public class RequestHistoryEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "REQUEST_HISTORY_ID")
    private String requestHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID")
    private RequestEntity requestEntity;

    @Column(name = "STATE_TO")
    private String stateTo;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "CREATED_USER_NAME")
    private String createdUserName;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    public RequestHistoryEntity() {
    }

    public RequestHistoryEntity(String requestHistoryId, RequestEntity requestEntity, String stateTo, String createdUser, String createdUserName, Timestamp createdDate) {
        this.requestHistoryId = requestHistoryId;
        this.requestEntity = requestEntity;
        this.stateTo = stateTo;
        this.createdUser = createdUser;
        this.createdUserName = createdUserName;
        this.createdDate = createdDate;
    }

    public String getRequestHistoryId() {
        return requestHistoryId;
    }

    public void setRequestHistoryId(String requestHistoryId) {
        this.requestHistoryId = requestHistoryId;
    }

    public RequestEntity getRequestEntity() {
        return requestEntity;
    }

    public void setRequestEntity(RequestEntity requestEntity) {
        this.requestEntity = requestEntity;
    }

    public String getStateTo() {
        return stateTo;
    }

    public void setStateTo(String stateTo) {
        this.stateTo = stateTo;
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

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public RequestHistoryDTO toDTO(RequestHistoryDTO dto){
        if(dto!=null){
            dto.setRequestHistoryId(this.getRequestHistoryId());
            dto.setRequestId(this.getRequestEntity().getRequestId());
            dto.setStateTo(this.getStateTo());
            dto.setCreatedUser(this.getCreatedUser());
            dto.setCreatedUserName(this.getCreatedUserName());
            dto.setCreatedDate(this.getCreatedDate());
        }
        return dto;
    }

    public RequestHistoryDTO toNewDTO(){
        return toDTO(new RequestHistoryDTO());
    }

}
