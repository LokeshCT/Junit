package com.bt.cqm.model;

import com.bt.cqm.dto.user.SalesChannelDTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SalesUserDTO {
    private String ein;
    private String boatId;
    private String name;
    private String emailId;
    private List<UserRoleDTO> roles;
    private List<SalesChannelDTO> salesChannelList;
    private String userType;
    private boolean isSubGroupUser=false;
   private String[] userSubGroups;

    public SalesUserDTO(String ein, String boatId, String name, String emailId, List<UserRoleDTO> roles, List<SalesChannelDTO> salesChannelList, String userType,boolean isSubGroupUser) {
        this.ein = ein;
        this.boatId = boatId;
        this.name = name;
        this.emailId = emailId;
        this.roles = roles;
        this.salesChannelList = salesChannelList;
        this.userType = userType;
        this.isSubGroupUser=isSubGroupUser;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    public String getBoatId() {
        return boatId;
    }

    public void setBoatId(String boatId) {
        this.boatId = boatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public List<UserRoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<UserRoleDTO> roles) {
        this.roles = roles;
    }

    public List<SalesChannelDTO> getSalesChannelList() {
        return salesChannelList;
    }

    public void setSalesChannelList(List<SalesChannelDTO> salesChannelList) {
        this.salesChannelList = salesChannelList;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public boolean isSubGroupUser() {
        return isSubGroupUser;
    }

    public void setSubGroupUser(boolean subGroupUser) {
        isSubGroupUser = subGroupUser;
    }
    public String[] getUserSubGroups() {
        return userSubGroups;
    }

    public void setUserSubGroups(String[] userSubGroups) {
        this.userSubGroups = userSubGroups;
    }
}
