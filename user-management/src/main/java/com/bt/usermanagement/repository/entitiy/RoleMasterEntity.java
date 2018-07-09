package com.bt.usermanagement.repository.entitiy;

import com.bt.usermanagement.dto.RoleMasterDTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Date;

@Entity
@Table(name = "ROLE_MASTER")
public class RoleMasterEntity {

    @Id
    @Column(name = "ROLE_ID")
    private Long roleId;

    @Column(name = "ROLE_NAME")
    private String roleName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ROLE_TYPE_ID", referencedColumnName = "ROLE_TYPE_ID" )
    private RoleTypeMasterEntity roleType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ROLE_GROUP_ID", referencedColumnName = "ROLE_GROUP_ID" )
    private RoleGroupMasterEntity roleGroup;

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

    public RoleMasterEntity() {
    }

    public RoleMasterEntity(Long roleId, String roleName, RoleTypeMasterEntity roleType, RoleGroupMasterEntity roleGroup, String active, Date createdDate, String createdUser, Date modifiedDate, String modifiedUser) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.roleType = roleType;
        this.roleGroup = roleGroup;
        isActive = active;
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

    public RoleTypeMasterEntity getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleTypeMasterEntity roleType) {
        this.roleType = roleType;
    }

    public RoleGroupMasterEntity getTargetSystem() {
        return roleGroup;
    }

    public void setTargetSystem(RoleGroupMasterEntity roleGroup) {
        this.roleGroup = roleGroup;
    }

    public String getActive() {
        return isActive;
    }

    public void setActive(String active) {
        isActive = active;
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

    public RoleMasterDTO toDTO(RoleMasterDTO dto){
        if(dto != null){
            dto.setRoleId(this.getRoleId());
            dto.setRoleName(this.roleName);
            dto.setRoleType(this.roleType.toNewDTO());
            dto.setRoleGroup(this.roleGroup.toNewDTO());
            dto.setIsActive(this.getActive());
            dto.setCreatedDate(this.getCreatedDate());
            dto.setCreatedUser(this.getCreatedUser());
            dto.setModifiedDate(this.getModifiedDate());
            dto.setModifiedUser(this.getModifiedUser());
        }
        return dto;
    }

    public RoleMasterDTO toNewDTO(){
        return toDTO(new RoleMasterDTO());
    }
}
