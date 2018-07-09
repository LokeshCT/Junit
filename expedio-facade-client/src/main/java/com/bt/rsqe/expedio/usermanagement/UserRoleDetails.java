package com.bt.rsqe.expedio.usermanagement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 25/03/15
 * Time: 15:47
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserRoleDetails {

    @XmlElement
    private String userName;
    @XmlElement
    private String userRole;
    @XmlElement
    private String orderType;
    @XmlElement
    private String operation;


    public UserRoleDetails() {
    }
    public UserRoleDetails(String userRole, String orderType, String operation, String userName) {
        this.userRole = userRole;
        this.orderType = orderType;
        this.operation = operation;
        this.userName = userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
