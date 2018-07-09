package com.bt.rsqe.expedio.services.quote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by 607937181 on 11/08/2014.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ChannelContactCreateDTO {

    @XmlElement
    private String quoteID;
    @XmlElement
    private String ein;
    @XmlElement
    private String firstName;
    @XmlElement
    private String lastName;
    @XmlElement
    private String jobTitle;
    @XmlElement
    private String phoneNumber;
    @XmlElement
    private String mobileNumber;
    @XmlElement
    private String fax;
    @XmlElement
    private String email;
    @XmlElement
    private String role;
    @XmlElement
    private String customerID;
/*
    @XmlElement
    private String channelContactID;
*/


    public String getQuoteID() {
        return quoteID;
    }

    public void setQuoteID(String quoteID) {
        this.quoteID = quoteID;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
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

    public void setJobTitle(String jobTitl) {
        this.jobTitle = jobTitl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNum) {
        this.phoneNumber = phoneNum;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobile) {
        this.mobileNumber = mobile;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String mailId) {
        this.email = mailId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    @Override
    public String toString() {
        return "ChannelContactCreateDTO{" +
               "quoteID='" + quoteID + '\'' +
               ", ein='" + ein + '\'' +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", jobTitle='" + jobTitle + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", mobileNumber='" + mobileNumber + '\'' +
               ", fax='" + fax + '\'' +
               ", email='" + email + '\'' +
               ", role='" + role + '\'' +
               ", customerID='" + customerID + '\'' +
               '}';
    }

    /*
    public String getChannelContactID() {
        return channelContactID;
    }

    public void setChannelContactID(String channelContactID) {
        this.channelContactID = channelContactID;
    }
*/
}

