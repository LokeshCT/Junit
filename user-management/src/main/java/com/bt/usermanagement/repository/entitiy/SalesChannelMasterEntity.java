package com.bt.usermanagement.repository.entitiy;

import com.bt.usermanagement.dto.SalesChannelDTO;
import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "SALES_CHANNEL_MASTER")
public class SalesChannelMasterEntity {
    @Id
    @Column(name = "SALES_CHANNEL_ID")
    private String salesChannelID;
    @Column(name = "SALES_CHANNEL_NAME")
    private String salesChannelName;
    @ManyToOne
    @JoinColumn(name = "ROLE_TYPE_ID", referencedColumnName = "ROLE_TYPE_ID")
    private RoleTypeMasterEntity salesChannelType;
    @Column(name = "CREATED_DATE")
    private Date createdDate;
    @Column(name = "CREATED_USER")
    private String createdUser;
    @Column(name = "MODIFIED_DATE")
    private Date modifiedDate;
    @Column(name = "MODIFIED_USER")
    private String modifiedUser;

    public SalesChannelMasterEntity() {
    }

    public String getSalesChannelID() {
        return salesChannelID;
    }

    public void setSalesChannelID(String salesChannelID) {
        this.salesChannelID = salesChannelID;
    }

    public String getSalesChannelName() {
        return salesChannelName;
    }

    public void setSalesChannelName(String salesChannelName) {
        this.salesChannelName = salesChannelName;
    }

    public RoleTypeMasterEntity getSalesChannelType() {
        return salesChannelType;
    }

    public void setSalesChannelType(RoleTypeMasterEntity salesChannelType) {
        this.salesChannelType = salesChannelType;
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

    public SalesChannelDTO toDto(SalesChannelDTO dto){
        dto.setSalesChannelID(this.salesChannelID);
        dto.setSalesChannelName(this.salesChannelName);
        dto.setSalesChannelType(this.salesChannelType.toNewDTO());
        dto.setCreatedDate(this.getCreatedDate());
        dto.setCreatedUser(this.getCreatedUser());
        dto.setModifiedDate(this.getModifiedDate());
        dto.setModifiedUser(this.getModifiedUser());
        return dto;
    }

    public  SalesChannelDTO toNewDTO(){
        return toDto(new SalesChannelDTO());
    }

///CLOVER:ON
}
