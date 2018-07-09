package com.bt.rsqe.expedio.services;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ActivityAssignedToContactDTO {

    String emailAddress;
    String entityID;
    String entityName;
    String forename;
    String surname;
    String fullName;
    String groupEmailIDAt;
    String groupEmailID;
    String loginName;
    String requestID;
    String requestID2;
    String salesChannel;
    String userID;
    String userName;
    String userRoles;
    String userRole;

    ///CLOVER:OFF
    public ActivityAssignedToContactDTO() {
    }

    public ActivityAssignedToContactDTO(String emailAddress, String entityID, String entityName, String forename, String surname,
                                        String fullName, String groupEmailIDAt, String groupEmailID, String loginName, String requestID,
                                        String requestID2, String salesChannel, String userID, String userName, String userRoles, String userRole) {
        this.emailAddress = emailAddress;
        this.entityID = entityID;
        this.entityName = entityName;
        this.forename = forename;
        this.surname = surname;
        this.fullName = fullName;
        this.groupEmailIDAt = groupEmailIDAt;
        this.groupEmailID = groupEmailID;
        this.loginName = loginName;
        this.requestID = requestID;
        this.requestID2 = requestID2;
        this.salesChannel = salesChannel;
        this.userID = userID;
        this.userName = userName;
        this.userRoles = userRoles;
        this.userRole = userRole;
    }

    public ActivityAssignedToContactDTO(String requestID, String fullName) {
        this.requestID = requestID;
        this.fullName = fullName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getEntityID() {
        return entityID;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getForename() {
        return forename;
    }

    public String getSurname() {
        return surname;
    }

    public String getFullName() {
        return fullName;
    }

    public String getGroupEmailIDAt() {
        return groupEmailIDAt;
    }

    public String getGroupEmailID() {
        return groupEmailID;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getRequestID() {
        return requestID;
    }

    public String getRequestID2() {
        return requestID2;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserRoles() {
        return userRoles;
    }

    public String getUserRole() {
        return userRole;
    }

    ///CLOVER:ON

}
