package com.bt.nrm.dto.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by 608143048 on 10/12/2015.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserDTO {

    private String userEIN;
    private String userEmail;
    private String userFirstName;
    private String userLastName;

    public UserDTO() {
    }

    public UserDTO(String userEIN, String userEmail, String userFirstName, String userLastName) {
        this.userEIN = userEIN;
        this.userEmail = userEmail;
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
    }

    public String getUserEIN() {
        return userEIN;
    }

    public void setUserEIN(String userEIN) {
        this.userEIN = userEIN;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }
}
