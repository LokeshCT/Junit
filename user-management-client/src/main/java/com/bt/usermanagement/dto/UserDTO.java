package com.bt.usermanagement.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserDTO {

    private String EIN;
    private String boatId;
    private String fullName;
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String phoneNumber;
    private String emailId;
    private RoleTypeMasterDTO userType; //Direct or Indirect
    private List<RoleMasterDTO> roles;  //rSQE roles
    private List<SalesChannelDTO> salesChannels;
    private String isActive;
    private Date lastLogIn;
    private String location;
    private String mobile;
    private String createUser;
    private Date createdDate;
    private String modifiedUser;
    private Date modifiedDate;

    public UserDTO() {
    }

    public UserDTO(String EIN, String boatId, String fullName, String firstName, String lastName, String jobTitle, String phoneNumber, String emailId, RoleTypeMasterDTO userType,
                   List<RoleMasterDTO> roles, List<SalesChannelDTO> salesChannels, String active, Date lastLogIn, String location, String mobile, String createUser, Date createdDate, String modifiedUser, Date modifiedDate) {
        this.EIN = EIN;
        this.boatId = boatId;
        this.fullName = fullName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.jobTitle = jobTitle;
        this.phoneNumber = phoneNumber;
        this.emailId = emailId;
        this.userType = userType;
        this.roles = roles;
        this.salesChannels = salesChannels;
        isActive = active;
        this.lastLogIn = lastLogIn;
        this.location = location;
        this.mobile = mobile;
        this.createUser = createUser;
        this.createdDate = createdDate;
        this.modifiedUser = modifiedUser;
        this.modifiedDate = modifiedDate;
    }

     public String getEIN() {
        return EIN;
    }

    public void setEIN(String EIN) {
        this.EIN = EIN;
    }

    public String getBoatId() {
        return boatId;
    }

    public void setBoatId(String boatId) {
        this.boatId = boatId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public RoleTypeMasterDTO getUserType() {
        return userType;
    }

    public void setUserType(RoleTypeMasterDTO userType) {
        this.userType = userType;
    }

    public List<RoleMasterDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleMasterDTO> roles) {
        this.roles = roles;
    }

    public List<SalesChannelDTO> getSalesChannels() {
        return salesChannels;
    }

    public void setSalesChannels(List<SalesChannelDTO> salesChannels) {
        this.salesChannels = salesChannels;
    }

    public String getActive() {
        return isActive;
    }

    public void setActive(String active) {
        isActive = active;
    }

    public Date getLastLogIn() {
        return lastLogIn;
    }

    public void setLastLogIn(Date lastLogIn) {
        this.lastLogIn = lastLogIn;
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

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedUser() {
        return modifiedUser;
    }

    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
