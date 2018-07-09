package com.bt.usermanagement.repository.entitiy;

import com.bt.usermanagement.dto.RoleMasterDTO;
import com.bt.usermanagement.dto.SalesChannelDTO;
import com.bt.usermanagement.dto.UserDTO;
import com.bt.usermanagement.util.GeneralUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "USER_MASTER")
public class UserMasterEntity {

    @Id
    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "JOB_TITLE")
    private String jobTitle;

    @Column(name = "EMAIL_ID")
    private String emailId;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "ACTIVE")
    private String active;

    @Column(name = "LAST_LOGIN")
    private Timestamp lastLogin;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "MOBILE")
    private String mobile;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "MODIFIED_DATE")
    private Timestamp modifiedDate;

    @Column(name = "MODIFIED_USER")
    private String modifiedUser;

    @OneToOne
    @JoinColumn(name = "ROLE_TYPE_ID")
    private RoleTypeMasterEntity userType;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "USER_ROLE",
        joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")},
        inverseJoinColumns = {@JoinColumn(name = "ROLE_ID", referencedColumnName = "ROLE_ID")}
    )
    private List<RoleMasterEntity> userRoles;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "USER_SALES_CHANNEL",
        joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")},
        inverseJoinColumns = {@JoinColumn(name = "SALES_CHANNEL_ID", referencedColumnName = "SALES_CHANNEL_ID")}
    )
    private List<SalesChannelMasterEntity> salesChannels;


    public UserMasterEntity() {
    }

    public UserMasterEntity(String userId, String userName,String active, Timestamp lastLogin, Timestamp createdDate, String createdUser, Timestamp modifiedDate, String modifiedUser) {
        this.userId = userId;
        this.userName = userName;
        this.active = active;
        this.lastLogin = lastLogin;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserMasterEntity that = (UserMasterEntity) o;

        if (active != null ? !active.equals(that.active) : that.active != null) return false;
        if (createdDate != null ? !createdDate.equals(that.createdDate) : that.createdDate != null) return false;
        if (createdUser != null ? !createdUser.equals(that.createdUser) : that.createdUser != null) return false;
        if (modifiedDate != null ? !modifiedDate.equals(that.modifiedDate) : that.modifiedDate != null) return false;
        if (modifiedUser != null ? !modifiedUser.equals(that.modifiedUser) : that.modifiedUser != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        result = 31 * result + (createdDate != null ? createdDate.hashCode() : 0);
        result = 31 * result + (createdUser != null ? createdUser.hashCode() : 0);
        result = 31 * result + (modifiedDate != null ? modifiedDate.hashCode() : 0);
        result = 31 * result + (modifiedUser != null ? modifiedUser.hashCode() : 0);
        result = 31 * result + (userType != null ? userType.hashCode() : 0);
        return result;
    }

    ///CLOVER:OFF

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getActive() {
        return active;
    }

    public boolean isActive() {
        return "Y".equalsIgnoreCase(active);
    }

    public void setActive(String active) {
        this.active = active;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Timestamp getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Timestamp modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedUser() {
        return modifiedUser;
    }

    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }

    public RoleTypeMasterEntity getUserType() {
        return userType;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setUserType(RoleTypeMasterEntity userType) {
        this.userType = userType;
    }

    public List<RoleMasterEntity> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<RoleMasterEntity> userRoles) {
        this.userRoles = userRoles;
    }

    public List<SalesChannelMasterEntity> getSalesChannels() {
        return salesChannels;
    }

    public void setSalesChannels(List<SalesChannelMasterEntity> salesChannels) {
        this.salesChannels = salesChannels;
    }

    public UserDTO toDto(UserDTO dto){
        dto.setEIN(this.userId);
        dto.setFullName(this.userName);
        dto.setFirstName(this.firstName);
        dto.setLastName(this.lastName);
        dto.setJobTitle(this.jobTitle);
        dto.setEmailId(this.emailId);
        dto.setPhoneNumber(this.phoneNumber);
        dto.setRoles(getUserRoleDtos(this.userRoles));
        dto.setSalesChannels(getSalesChannelDtos(this.salesChannels));
        dto.setUserType(this.userType.toNewDTO());
        dto.setActive(this.active);
        dto.setLastLogIn(this.lastLogin);
        dto.setLocation(this.location);
        dto.setMobile(this.mobile);
        dto.setCreatedDate(this.createdDate);
        dto.setCreateUser(this.createdUser);
        dto.setModifiedDate(this.modifiedDate);
        dto.setModifiedUser(this.modifiedUser);
        return dto;
    }


    public UserDTO toNewDto(){
        return toDto(new UserDTO());
    }

    public List<RoleMasterDTO> getUserRoleDtos(List<RoleMasterEntity> userRoleEntities){
        List<RoleMasterDTO> userRoles = new ArrayList<RoleMasterDTO>();
        for(RoleMasterEntity userRoleEntity : userRoleEntities){
            userRoles.add(userRoleEntity.toNewDTO());
        }
        return userRoles;
    }

    public List<SalesChannelDTO> getSalesChannelDtos(List<SalesChannelMasterEntity> userSalesChannelEntities){
        List<SalesChannelDTO> userSalesChannels = new ArrayList<SalesChannelDTO>();
        for(SalesChannelMasterEntity userSalesChannelEntity : userSalesChannelEntities){
            userSalesChannels.add(userSalesChannelEntity.toNewDTO());
        }
        return userSalesChannels;
    }

}
