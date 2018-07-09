package com.bt.cqm.ldap.model;


import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class LdapSearchModel {
    @XmlElement
    private String boatId;
    @XmlElement
    private String mailId;
    @XmlElement
    private String phoneNum;
    @XmlElement
    private String firstName;
    @XmlElement
    private String ein;
    @XmlElement
    private String jobTitle;
    @XmlElement
    private String fullName;
    @XmlElement
    private String lastName;
    @XmlElement
    private String thirdPartyEmailId;

    public LdapSearchModel() {
    }

    private LdapSearchModel(String boatId, String lastName, String mailId, String phoneNum, String firstName,
                            String ein, String jobTitle, String fullName,String thirdPartyEmailId) {
        this.boatId = boatId;
        this.lastName = lastName;
        this.mailId = mailId;
        this.phoneNum = phoneNum;
        this.firstName = firstName;
        this.ein = ein;
        this.jobTitle = jobTitle;
        this.fullName = fullName;
        this.thirdPartyEmailId=thirdPartyEmailId;
    }

    @Override
    public String toString() {
        return "LdapSearchDTO{" +
                "boatId='" + boatId + '\'' +
                ", mailId='" + mailId + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", firstName='" + firstName + '\'' +
                ", ein='" + ein + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", fullName='" + fullName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", thirdPartyEmailId='" + thirdPartyEmailId + '\'' +
                '}';
    }

    public void setValues(Attributes attributes) throws NamingException {
        this.ein = getAttributeValue(attributes.get("cn"));
        this.boatId = getAttributeValue(attributes.get("btoriginalmailid"));
        this.firstName = getAttributeValue(attributes.get("givenname"));
        this.fullName = getAttributeValue(attributes.get("fullname"));
        this.jobTitle = getAttributeValue(attributes.get("title"));
        this.lastName = getAttributeValue(attributes.get("sn"));
        this.mailId = getAttributeValue(attributes.get("mail"));
        this.phoneNum = getAttributeValue(attributes.get("telephonenumber"));
        this.thirdPartyEmailId=getAttributeValue(attributes.get("btrfc822mailbox"));

    }

    private String getAttributeValue(Attribute attr) throws NamingException {
        String cn = "";
        if (attr != null && attr.get() != null) {
            cn = (String) attr.get();
        }
        return cn;
    }

    public String getBoatId() {
        return boatId;
    }

    public void setBoatId(String boatId) {
        this.boatId = boatId;
    }

    public String getMailId() {
        return mailId;
    }

    public void setMailId(String mailId) {
        this.mailId = mailId;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
