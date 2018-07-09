package com.bt.usermanagement.repository.entitiy;

import com.bt.usermanagement.dto.RoleGroupDTO;
import com.bt.usermanagement.dto.RoleMasterDTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;

@Entity
@Table(name = "ROLE_GROUP_MASTER")
public class RoleGroupMasterEntity {

    @Id
    @Column(name = "ROLE_GROUP_ID")
    private String roleGroupId;

    @Column(name = "ROLE_GROUP_NAME")
    private String roleGroupName;

    @Column(name = "ROLE_GROUP_DESC")
    private String roleGroupDesc;

    @Column(name = "ACTIVE")
    private String isActive;

    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "MODIFIED_DATE")
    private Date modifiedDate;

    @Column(name = "MODIFIED_USER")
    private String modifiedUser;

    public RoleGroupMasterEntity() {
    }

    public RoleGroupMasterEntity(String roleGroupId, String roleGroupName, String roleGroupDesc, String isActive, Date createdDate, String createdUser, Date modifiedDate, String modifiedUser) {
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

    public RoleGroupDTO toDTO(RoleGroupDTO dto){
        if(dto != null){
            dto.setRoleGroupId(this.getRoleGroupId());
            dto.setRoleGroupName(this.getRoleGroupName());
            dto.setRoleGroupDesc(this.getRoleGroupDesc());
            dto.setIsActive(this.getIsActive());
            dto.setCreatedDate(this.getCreatedDate());
            dto.setCreatedUser(this.getCreatedUser());
            dto.setModifiedDate(this.getModifiedDate());
            dto.setModifiedUser(this.getModifiedUser());
        }
        return dto;
    }

    public RoleGroupDTO toNewDTO(){
        return toDTO(new RoleGroupDTO());
    }
}
