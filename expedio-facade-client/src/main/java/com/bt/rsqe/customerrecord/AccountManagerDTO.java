package com.bt.rsqe.customerrecord;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AccountManagerDTO {

    @XmlElement
    public String customerId;
    @XmlElement
    public String firstName;
    @XmlElement
    public String lastName;
    @XmlElement
    public String phoneNumber;
    @XmlElement
    public String faxNumber;
    @XmlElement
    public String email;
    @XmlElement
    public String role;
    @XmlElement
    public String contactId;

    public AccountManagerDTO(String customerId, String firstName, String lastName, String phoneNumber, String faxNumber, String email, String role, String contactId) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.faxNumber = faxNumber;
        this.email = email;
        this.role = role;
        this.contactId = contactId;
    }

    public AccountManagerDTO() {
        //required by jaxb
    }

    @Override
    public boolean equals(final Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String getFullName() {
        String fullName = "";
        if (firstName != null && !firstName.equals("")) {
            fullName += firstName + " ";
        }
        if (lastName != null && !lastName.equals("")) {
            fullName += lastName;
        }
        return fullName;
    }
}

