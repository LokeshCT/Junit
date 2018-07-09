package com.bt.usermanagement.repository.entitiy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import com.bt.usermanagement.dto.RoleTypeMasterDTO;
import java.sql.Date;

@Entity
@Table(name = "ROLE_TYPE_MASTER")
public class RoleTypeMasterEntity {

  @Id
  @Column(name = "ROLE_TYPE_ID")
  private Long roleTypeId;
  @Column(name = "ROLE_TYPE_NAME")
  private String roleTypeName;
  @Column(name = "CREATED_DATE")
  private Date createdDate;
  @Column(name = "CREATED_USER")
  private String createdUser;
  @Column(name = "MODIFIED_DATE")
  private Date modifiedDate;
  @Column(name = "MODIFIED_USER")
  private String modifiedUser;

  public RoleTypeMasterEntity() { /* Default */ }

  public RoleTypeMasterEntity(Long roleTypeId, String roleTypeName, Date createdDate, String createdUser, Date modifiedDate, String modifiedUser) {
    this.roleTypeId = roleTypeId;
    this.roleTypeName = roleTypeName;
    this.createdDate = createdDate;
    this.createdUser = createdUser;
    this.modifiedDate = modifiedDate;
    this.modifiedUser = modifiedUser;
  }

  public enum UserType {
      Direct, InDirect
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

  public RoleTypeMasterDTO toDto(RoleTypeMasterDTO dto){
    dto.setRoleTypeId(this.roleTypeId);
    dto.setRoleTypeName(this.roleTypeName);
    dto.setCreatedDate(this.getCreatedDate());
    dto.setCreatedUser(this.getCreatedUser());
    dto.setModifiedDate(this.getModifiedDate());
    dto.setModifiedUser(this.getModifiedUser());
    return dto;
  }

  public RoleTypeMasterDTO toNewDTO(){
    return toDto(new RoleTypeMasterDTO());
  }

}
