package com.bt.usermanagement.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Sales Channel DTO class.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SalesChannelDTO {

    private String salesChannelID;
    private String salesChannelName;
    private RoleTypeMasterDTO salesChannelType;
    private boolean isDefault;
    private Date createdDate;
    private String createdUser;
    private Date modifiedDate;
    private String modifiedUser;

    public SalesChannelDTO() {
    }

    public SalesChannelDTO(String salesChannelID, String salesChannelName, RoleTypeMasterDTO salesChannelType, boolean isDefault, Date createdDate, String createdUser, Date modifiedDate, String modifiedUser) {
        this.salesChannelID = salesChannelID;
        this.salesChannelName = salesChannelName;
        this.salesChannelType = salesChannelType;
        this.isDefault = isDefault;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
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

    public RoleTypeMasterDTO getSalesChannelType() {
        return salesChannelType;
    }

    public void setSalesChannelType(RoleTypeMasterDTO salesChannelType) {
        this.salesChannelType = salesChannelType;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
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
}

