package com.bt.usermanagement.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RoleMasterDTO {

    private Long roleId;
    private String roleName;
    private RoleTypeMasterDTO roleType;
    private RoleGroupDTO roleGroup;
    private String isActive;
    private Date createdDate;
    private String createdUser;
    private Date modifiedDate;
    private String modifiedUser;

    public RoleMasterDTO() {
    }

    public RoleMasterDTO(Long roleId, String roleName, RoleTypeMasterDTO roleType, RoleGroupDTO roleGroup, String isActive, Date createdDate, String createdUser, Date modifiedDate, String modifiedUser) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.roleType = roleType;
        this.roleGroup = roleGroup;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public RoleTypeMasterDTO getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleTypeMasterDTO roleType) {
        this.roleType = roleType;
    }

    public RoleGroupDTO getRoleGroup() {
        return roleGroup;
    }

    public void setRoleGroup(RoleGroupDTO roleGroup) {
        this.roleGroup = roleGroup;
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
