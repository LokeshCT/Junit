package com.bt.nrm.repository.entity;

import com.bt.nrm.dto.RequestWhoHasSeenDTO;
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
@Table(name = "REQUEST_WHO_HAS_SEEN")
public class RequestWhoHasSeenEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "REQUEST_WHO_HAS_SEEN_ID")
    private String requestWhoHasSeenId;

    @Column(name = "STATE")
    private String state;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "CREATED_USER_NAME")
    private String createdUserName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID")
    private RequestEntity requestEntity;

    public RequestWhoHasSeenEntity() {
    }

    public RequestWhoHasSeenEntity(String requestWhoHasSeenId, String state, Timestamp createdDate, String createdUser, String createdUserName, RequestEntity requestEntity) {
        this.requestWhoHasSeenId = requestWhoHasSeenId;
        this.state = state;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.createdUserName = createdUserName;
        this.requestEntity = requestEntity;
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

    public RequestEntity getRequestEntity() {
        return requestEntity;
    }

    public void setRequestEntity(RequestEntity requestEntity) {
        this.requestEntity = requestEntity;
    }

    public RequestWhoHasSeenDTO toDTO(RequestWhoHasSeenDTO dto){
        if(dto!=null){
            dto.setRequestWhoHasSeenId(this.getRequestWhoHasSeenId());
            dto.setState(this.getState());
            dto.setCreatedUser(this.getCreatedUser());
            dto.setCreatedUserName(this.getCreatedUserName());
            dto.setCreatedDate(this.getCreatedDate());
            dto.setRequestId(this.getRequestEntity().getRequestId());
        }
        return dto;
    }

    public RequestWhoHasSeenDTO toNewDTO(){
        return toDTO(new RequestWhoHasSeenDTO());
    }
}
