package com.bt.rsqe.expedio.contact;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ContactDetailDTO {
    @XmlElement
    public Long bfgSiteId;
    @XmlElement
    public Long bfgCustomerId;
    @XmlElement
    public Long correlationId;
    @XmlElement
    public String userNameEin;
    @XmlElement
    public String contactType;
    @XmlElement
    public Long bfgAddressId;
    @XmlElement
    public Long bfgContactId;
    @XmlElement
    public Long bfgContactRoleId;
    @XmlElement
    public String firstName;
    @XmlElement
    public String lastName;
    @XmlElement
    public String jobTitle;
    @XmlElement
    public String emailAddress;
    @XmlElement
    public String phoneNumber;
    @XmlElement
    public String zipCode;
    @XmlElement
    public String country;
    @XmlElement
    public String city;
    @XmlElement
    public String state;
    @XmlElement
    public String productName;
    @XmlElement
    public String errorMessage;
    @XmlElement
    public String errorCode;

    // for jaxb
    public ContactDetailDTO() {
    }

    public ContactDetailDTO(Long bfgContactId, Long bfgContactRoleId, Long correlationId, String productName,
                            String errorMessage, String errorCode) {
        this.bfgContactId = bfgContactId;
        this.bfgContactRoleId = bfgContactRoleId;
        this.correlationId = correlationId;
        this.productName = productName;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public ContactDetailDTO(Long bfgSiteId, Long bfgCustomerId, Long correlationId, String userNameEin,
                            String contactType, Long bfgAddressId, Long bfgContactId, Long bfgContactRoleId,
                            String firstName, String lastName, String jobTitle, String emailAddress,
                            String phoneNumber,
                            String zipCode, String country, String city, String state, String productName) {
        this.bfgSiteId = bfgSiteId;
        this.bfgCustomerId = bfgCustomerId;
        this.correlationId = correlationId;
        this.userNameEin = userNameEin;
        this.contactType = contactType;
        this.bfgAddressId = bfgAddressId;
        this.bfgContactId = bfgContactId;
        this.bfgContactRoleId = bfgContactRoleId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.jobTitle = jobTitle;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.zipCode = zipCode;
        this.country = country;
        this.city = city;
        this.state = state;
        this.productName = productName;
    }

    //TODO: Need to understand how this works so far as the mandatory fields are not mapped.
    public ContactDetailDTO(Long bfgSiteId, Long bfgCustomerId, Long correlationId, String userNameEin, String contactType, Long bfgAddressId, String firstName, String lastName, String jobTitle, String emailAddress, String phoneNumber, String productName) {
        this.bfgSiteId = bfgSiteId;
        this.bfgCustomerId = bfgCustomerId;
        this.correlationId = correlationId;
        this.contactType = contactType;
        this.bfgAddressId = bfgAddressId;
        this.productName = productName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.jobTitle = jobTitle;
        this.phoneNumber = phoneNumber;
        this.userNameEin = userNameEin;
        this.emailAddress = emailAddress;
    }

    @Override
    public boolean equals(final Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return "ContactDetailDTO{" +
               "errorMessage='" + errorMessage + '\'' +
               ", errorCode='" + errorCode + '\'' +
               ", correlationId=" + correlationId +
               ", bfgContactId=" + bfgContactId +
               ", bfgContactRoleId=" + bfgContactRoleId +
               ", productName='" + productName + '\'' +
               '}';
    }
}
