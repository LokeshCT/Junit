package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.domain.AccessUserCommentsDTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;
import java.text.SimpleDateFormat;

@Table(name = "ACCESS_USER_COMMENTS")
@Entity
public class AccessUserCommentsEntity {
    @Id
    @Column(name = "USER_QREF_ID")
    private String accessUserQrefId;

    @Column(name = "QREF_ID")
    private String qrefId;

    @Column(name = "USER_NAME")
    private String username;

    @Column(name = "USER_COMMENTS")
    private String comment;

    @Column(name = "CREATED_DATE")
    private Date createdDate;

    public AccessUserCommentsEntity() {

    }

    public AccessUserCommentsEntity(String accessUserQrefId, String qrefId, String username, String comment, Date createdDate) {
        this.accessUserQrefId = accessUserQrefId;
        this.qrefId = qrefId;
        this.username = username;
        this.comment = comment;
        this.createdDate = createdDate;
    }

    public String getAccessUserQrefId() {
        return accessUserQrefId;
    }

    public String getQrefId() {
        return qrefId;
    }

    public String getComment() {
        return comment;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getUsername() {
        return username;
    }

    public AccessUserCommentsDTO toDto() {
        return new AccessUserCommentsDTO(accessUserQrefId, qrefId, username, comment, toDayMonthYear(createdDate));
    }

    private String toDayMonthYear(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

}
