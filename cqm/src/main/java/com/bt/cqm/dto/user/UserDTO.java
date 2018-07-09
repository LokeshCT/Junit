package com.bt.cqm.dto.user;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserDTO {

    private String userId;

    private String userName;

    private String isActive;

    private String createUser;

    private Date createdDate;

    private String userRoleTypeId;

    private Date modifiedDate;

    private RoleTypeDTO userType;

    private String modifiedUser;

    private String loginId;

    private List<UserRoleMasterDTO> roles;

    private List<SalesChannelDTO> userSalesChannelList;

    public UserDTO() {
    }

    public UserDTO(String userId, String userName, String isActive, String createUser, Date createdDate, String modifiedUser, Date modifiedDate, RoleTypeDTO userType, /*UserRoleMasterDTO defaultRole,*/ List<UserRoleMasterDTO> roles, List<SalesChannelDTO> userSalesChannelList,String userRoleTypeId,String loginId) {
        this.userId = userId;
        this.userName = userName;
        this.isActive = isActive;
        this.createUser = createUser;
        this.createdDate = createdDate;
        this.modifiedUser = modifiedUser;
        this.modifiedDate = modifiedDate;
        this.userType = userType;
        this.roles = roles;
        this.userSalesChannelList = userSalesChannelList;
        this.userRoleTypeId=userRoleTypeId;
        this.loginId=loginId;
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
        return isActive;
    }

    public void setActive(String active) {
        this.isActive = active;
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

    public RoleTypeDTO getUserType() {
        return userType;
    }

    public void setUserType(RoleTypeDTO userType) {
        this.userType = userType;
    }

    public List<UserRoleMasterDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<UserRoleMasterDTO> roles) {
        this.roles = roles;
    }

    public List<SalesChannelDTO> getUserSalesChannelList() {
        return userSalesChannelList;
    }

    public void setUserSalesChannelList(List<SalesChannelDTO> userSalesChannelList) {
        this.userSalesChannelList = userSalesChannelList;
    }

    public String getUserRoleTypeId() {
        return userRoleTypeId;
    }

    public void setUserRoleTypeId(String userRoleTypeId) {
        this.userRoleTypeId = userRoleTypeId;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

///CLOVER:ON

}
