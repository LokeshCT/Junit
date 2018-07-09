package com.bt.cqm.repository.user;


import com.bt.cqm.dto.user.UserRoleMasterDTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.sql.Date;
import java.util.List;

@Entity
@Table(name = "USER_ROLE_MASTER")
public class UserRoleMasterEntity {

    @Id
    @Column(name = "ROLE_ID")
    private Long roleId;

    @Column(name = "ROLE_NAME")
    private String roleName;

    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "MODIFIED_DATE")
    private Date modifiedDate;

    @Column(name = "MODIFIED_USER")
    private String modifiedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ROLE_TYPE", referencedColumnName = "ROLE_TYPE_ID" )
    private RoleTypeEntity roleType;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "id.role")
    private List<UserRoleConfigEntity> userRoleConfig;


    /*
    Default Constructor
     */
    public UserRoleMasterEntity() {
    }

    public UserRoleMasterEntity(Long roleId) {
        this.roleId=roleId;
    }

    /**
     * Overloaded Constructor
     * @param roleId
     * @param roleName
     * @param createdDate
     * @param createdUser
     * @param modifiedDate
     * @param modifiedUser
     */
    public UserRoleMasterEntity(Long roleId, String roleName, Date createdDate, String createdUser, Date modifiedDate, String modifiedUser) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
    }

    ///CLOVER:OFF


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

    public RoleTypeEntity getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleTypeEntity roleType) {
        this.roleType = roleType;
    }

    public List<UserRoleConfigEntity> getUserRoleConfig() {
        return userRoleConfig;
    }

    public void setUserRoleConfig(List<UserRoleConfigEntity> userRoleConfig) {
        this.userRoleConfig = userRoleConfig;
    }

    public UserRoleMasterDTO toDTO(UserRoleMasterDTO dto){
        if(dto!=null){
        dto.setRoleId(this.getRoleId());
        dto.setRoleName(this.roleName);
        }
        return dto;
    }

    public UserRoleMasterDTO toNewDTO(){
        return toDTO(new UserRoleMasterDTO());
    }

    ///CLOVER:ON
}
