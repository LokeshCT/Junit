package com.bt.cqm.repository.user;


import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "USER_SALES_CHANNEL")
@IdClass(UserSalesChannelID.class)
public class UserSalesChannelEntity {

    @Id
    @Column(name = "USER_ID")
    private String userId;

    @Id
    @Column(name = "SALES_CHANNEL")
    private String salesChannel;

    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "MODIFIED_DATE")
    private Date modifiedDate;

    @Column(name = "MODIFIED_USER")
    private String modifiedUser;

    @Type(type = "true_false")
    @Column(name = "DEFAULT_SALES_CHANNEL")
    private boolean defaultSalesChannel;

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 4305346924244201428L;

    ///CLOVER:OFF

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(String salesChannel) {
        this.salesChannel = salesChannel;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedUser() {
        return modifiedUser;
    }

    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }

    public boolean isDefaultSalesChannel() {
        return defaultSalesChannel;
    }

    public void setDefaultSalesChannel(boolean defaultSalesChannel) {
        this.defaultSalesChannel = defaultSalesChannel;
    }

    ///CLOVER:ON
}


