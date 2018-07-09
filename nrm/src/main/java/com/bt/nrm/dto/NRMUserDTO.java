package com.bt.nrm.dto;

import com.bt.pms.dto.ProductCategoryDTO;
import com.bt.usermanagement.dto.RoleMasterDTO;
import com.bt.usermanagement.dto.RoleTypeMasterDTO;
import com.bt.usermanagement.dto.SalesChannelDTO;
import com.bt.usermanagement.dto.UserDTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NRMUserDTO {

    private String EIN;  //UserId
    private String boatId;
    private String fullName; //This includes combination of first name and last name of the user
    private String firstName;
    private String lastName;
    private String jobTitle; // JobTitle as per BT directory
    private String phoneNumber;
    private String emailId;
    private RoleTypeMasterDTO userType; //User type : Direct or Indirect
    private List<RoleMasterDTO> roles;  //roles assigned to user for NRM
    private List<SalesChannelDTO> salesChannels; //Sales Channels assigned to user
    private List<UserProductDTO> products; //Products assigned to user
    private List<UserGroupDTO> groups;  //Groups assigned to user
    private String isActive;
    private Date lastLogIn;
    private String location;
    private String mobile;
    private String createUser;
    private Date createdDate;
    private String modifiedUser;
    private Date modifiedDate;

    public NRMUserDTO() {
    }

    public NRMUserDTO(String EIN, String boatId, String fullName, String firstName, String lastName, String jobTitle, String phoneNumber, String emailId,
                      RoleTypeMasterDTO userType, List<RoleMasterDTO> roles, List<SalesChannelDTO> salesChannels, List<UserProductDTO> products, List<UserGroupDTO> groups, String active, Date lastLogIn, String location, String mobile, String createUser, Date createdDate, String modifiedUser, Date modifiedDate) {
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
        this.products = products;
        this.groups = groups;
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

    public List<UserProductDTO> getProducts() {
        return products;
    }

    public void setProducts(List<UserProductDTO> products) {
        this.products = products;
    }

    public List<UserGroupDTO> getGroups() {
        return groups;
    }

    public void setGroups(List<UserGroupDTO> groups) {
        this.groups = groups;
    }

    public String getActive() {
        return isActive;
    }

    public void setActive(String active) {
        isActive = active;
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

    public static NRMUserDTO getNRMDTOFromUserDTO(UserDTO userDTO){
        NRMUserDTO newNRMUserDTO = new NRMUserDTO();
        newNRMUserDTO.setEIN(userDTO.getEIN());
        newNRMUserDTO.setBoatId(userDTO.getBoatId());
        newNRMUserDTO.setFullName(userDTO.getFullName());
        newNRMUserDTO.setFirstName(userDTO.getFirstName());
        newNRMUserDTO.setLastName(userDTO.getLastName());
        newNRMUserDTO.setJobTitle(userDTO.getJobTitle());
        newNRMUserDTO.setJobTitle(userDTO.getJobTitle());
        newNRMUserDTO.setPhoneNumber(userDTO.getPhoneNumber());
        newNRMUserDTO.setEmailId(userDTO.getEmailId());
        newNRMUserDTO.setUserType(userDTO.getUserType());
        newNRMUserDTO.setRoles(userDTO.getRoles());
        newNRMUserDTO.setSalesChannels(userDTO.getSalesChannels());
        newNRMUserDTO.setActive(userDTO.getActive());
        newNRMUserDTO.setLastLogIn(userDTO.getLastLogIn());
        newNRMUserDTO.setLocation(userDTO.getLocation());
        newNRMUserDTO.setMobile(userDTO.getMobile());
        newNRMUserDTO.setCreateUser(userDTO.getCreateUser());
        newNRMUserDTO.setCreatedDate(userDTO.getCreatedDate());
        newNRMUserDTO.setModifiedUser(userDTO.getModifiedUser());
        newNRMUserDTO.setModifiedDate(userDTO.getModifiedDate());
        return newNRMUserDTO;
    }
}
