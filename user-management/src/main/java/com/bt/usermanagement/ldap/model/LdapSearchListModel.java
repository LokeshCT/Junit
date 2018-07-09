package com.bt.usermanagement.ldap.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class LdapSearchListModel {
    @XmlElement
    private boolean displayErrors;
    @XmlElement
    private String errorMessage;
    @XmlElement
    private List<LdapSearchModel> customers;

    public LdapSearchListModel() {
    }

    public boolean isDisplayErrors() {
        return displayErrors;
    }

    public void setDisplayErrors(boolean displayErrors) {
        this.displayErrors = displayErrors;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }


    public LdapSearchListModel(List<LdapSearchModel> customers, String errorMessage, boolean displayErrors) {
        this.customers = customers;
        this.errorMessage = errorMessage;
        this.displayErrors = displayErrors;
    }

    public List<LdapSearchModel> getCustomers() {
        return customers;
    }

    public void setCustomers(List<LdapSearchModel> customers) {
        this.customers = customers;
    }
}
