package com.bt.cqm.repository.user;

import org.hibernate.annotations.Type;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Date;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 7/24/14
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name="USER_ROLE_CONFIG")
@AssociationOverrides({
                          @AssociationOverride(name = "id.user", joinColumns = @JoinColumn(name = "USER_ID")),
                          @AssociationOverride(name = "id.role", joinColumns = @JoinColumn(name = "ROLE_ID"))})
public class UserRoleConfigEntity {

    @EmbeddedId
    private UserRoleConfigID id;

    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "MODIFIED_DATE")
    private Date modifiedDate;

    @Column(name = "MODIFIED_USER")
    private String modifiedUser;

    @Type(type = "true_false")
    @Column(name = "DEFAULT_ROLE")
    private boolean defaultRole;

    public UserRoleConfigEntity() { /* for JPA */
    }

    public UserRoleConfigEntity(UserRoleConfigID id) {
        this.id = id;
    }

    public UserRoleConfigID getId() {
        return id;
    }

    public void setId(UserRoleConfigID id) {
        this.id = id;
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

    public boolean isDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(boolean defaultRole) {
        this.defaultRole = defaultRole;
    }

    @Transient
    public UserEntity getUser(){
        if(getId()!=null)
        {
            return getId().getUser();
        }else{
            return null;
        }
    }

    @Transient
    public UserRoleMasterEntity getRole(){
        if(getId()!=null){
            return getId().getRole();
        } else{
            return null;
        }
    }
}
