package com.bt.cqm.repository.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.sql.Date;
import java.util.List;

@Entity
@Table(name = "ROLE_TYPE_MASTER")
public class RoleTypeEntity {

  @Id
  @Column(name = "ROLE_TYPE_ID")
  private Long roleTypeId;
  @Column(name = "ROLE_TYPE_NAME")
  private String roleTypeName;
  /*@Column(name = "ACTIVE")
  private String active;
  */
  @Column(name = "CREATED_DATE")
  private Date createdDate;
  @Column(name = "CREATED_USER")
  private String createdUser;
  @Column(name = "MODIFIED_DATE")
  private Date modifiedDate;
  @Column(name = "MODIFIED_USER")
  private String modifiedUser;

  @OneToMany (mappedBy = "roleType", fetch = FetchType.LAZY)
  private List<UserRoleMasterEntity>  roles;

  @OneToMany (mappedBy = "roleType", fetch = FetchType.LAZY)
  private List<SalesChannelEntity>  salesChannels;


  public RoleTypeEntity() { /* Default */ }

  public RoleTypeEntity(Long roleTypeId, String roleTypeName,/* String active,*/ Date createdDate, String createdUser, Date modifiedDate, String modifiedUser) {
    this.roleTypeId = roleTypeId;
    this.roleTypeName = roleTypeName;
   /* this.active = active;*/
    this.createdDate = createdDate;
    this.createdUser = createdUser;
    this.modifiedDate = modifiedDate;
    this.modifiedUser = modifiedUser;
  }

  public Long getRoleTypeId() {
    return roleTypeId;
  }

  public void setRoleTypeId(Long roleTypeId) {
    this.roleTypeId = roleTypeId;
  }

  public String getRoleTypeName() {
    return roleTypeName;
  }

  public void setRoleTypeName(String roleTypeName) {
    this.roleTypeName = roleTypeName;
  }

  /*
  public String getActive() {
    return active;
  }

  public void setActive(String active) {
    this.active = active;
  }
  */

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

    public List<UserRoleMasterEntity> getRoles() {
        return roles;
    }

    public void setRoles(List<UserRoleMasterEntity> roles) {
        this.roles = roles;
    }

    public List<SalesChannelEntity> getSalesChannels() {
        return salesChannels;
    }

    public void setSalesChannels(List<SalesChannelEntity> salesChannels) {
        this.salesChannels = salesChannels;
    }

    public enum UserType {
      Direct, InDirect
  }

}
