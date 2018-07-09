package com.bt.usermanagement.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 *  Created by 608143048 on 12/01/2016.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RoleGroupDTO {

    private String roleGroupId;
    private String roleGroupName;
    private String roleGroupDesc;
    private String isActive;
    private Date createdDate;
    private String createdUser;
    private Date modifiedDate;
    private String modifiedUser;

    public RoleGroupDTO() {
    }

    public RoleGroupDTO(String roleGroupId, String roleGroupName, String roleGroupDesc, String isActive, Date createdDate, String createdUser, Date modifiedDate, String modifiedUser) {
        this.roleGroupId = roleGroupId;
        this.roleGroupName = roleGroupName;
        this.roleGroupDesc = roleGroupDesc;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
    }

    public String getRoleGroupId() {
        return roleGroupId;
    }

    public void setRoleGroupId(String roleGroupId) {
        this.roleGroupId = roleGroupId;
    }

    public String getRoleGroupName() {
        return roleGroupName;
    }

    public void setRoleGroupName(String roleGroupName) {
        this.roleGroupName = roleGroupName;
    }

    public String getRoleGroupDesc() {
        return roleGroupDesc;
    }

    public void setRoleGroupDesc(String roleGroupDesc) {
        this.roleGroupDesc = roleGroupDesc;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
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
