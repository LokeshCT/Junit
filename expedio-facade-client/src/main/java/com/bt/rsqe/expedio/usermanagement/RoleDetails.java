package com.bt.rsqe.expedio.usermanagement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 23/03/15
 * Time: 15:35
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class RoleDetails {

    @XmlElement
    private String userRoleType;
    @XmlElement
    private String userName;
    @XmlElement
    private String userRole;



    public RoleDetails() {
    }

    public RoleDetails(String userRoleType, String userRole) {
        this.userRoleType = userRoleType;
        this.userRole = userRole;
    }

    public RoleDetails(String userRoleType, String userRole, String userName) {
        this.userRoleType = userRoleType;
        this.userRole = userRole;
        this.userName=userName;
    }


    public String getUserRoleType() {
        return userRoleType;
    }

    public void setUserRoleType(String userRoleType) {
        this.userRoleType = userRoleType;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
