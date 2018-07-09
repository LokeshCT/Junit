package com.bt.cqm.dto;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 19/02/14
 * Time: 17:44
 * To change this template use File | Settings | File Templates.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class VPNDTO {

    @XmlElement
    private String customerID;
    @XmlElement
    private String vpnServiceID;
    @XmlElement
    private String vpnID;
    @XmlElement
    private String vpnAccessType;
    @XmlElement
    private String vpnType;
    @XmlElement
    private String vpnSecondFriendlyName;
    @XmlElement
    private String vpnFriendlyName;
    @XmlElement
    private String vpnAccess;

    public VPNDTO(String customerID, String vpnServiceID, String vpnID,
                              String vpnAccessType, String vpnType, String vpnSecondFriendlyName, String vpnFriendlyName,
                              String vpnAccess) {
        this.customerID = customerID;
        this.vpnServiceID = vpnServiceID;
        this.vpnID = vpnID;
        this.vpnAccessType = vpnAccessType;
        this.vpnType = vpnType;
        this.vpnSecondFriendlyName = vpnSecondFriendlyName;
        this.vpnFriendlyName = vpnFriendlyName;
        this.vpnAccess = vpnAccess;

    }

    public VPNDTO() {
        // required by jaxb
    }

    ///CLOVER:OFF

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getVpnServiceID() {
        return vpnServiceID;
    }

    public void setVpnServiceID(String vpnServiceID) {
        this.vpnServiceID = vpnServiceID;
    }

    public String getVpnID() {
        return vpnID;
    }

    public void setVpnID(String vpnID) {
        this.vpnID = vpnID;
    }

    public String getVpnAccessType() {
        return vpnAccessType;
    }

    public void setVpnAccessType(String vpnAccessType) {
        this.vpnAccessType = vpnAccessType;
    }

    public String getVpnType() {
        return vpnType;
    }

    public void setVpnType(String vpnType) {
        this.vpnType = vpnType;
    }

    public String getVpnSecondFriendlyName() {
        return vpnSecondFriendlyName;
    }

    public void setVpnSecondFriendlyName(String vpnSecondFriendlyName) {
        this.vpnSecondFriendlyName = vpnSecondFriendlyName;
    }

    public String getVpnFriendlyName() {
        return vpnFriendlyName;
    }

    public void setVpnFriendlyName(String vpnFriendlyName) {
        this.vpnFriendlyName = vpnFriendlyName;
    }

    public String getVpnAccess() {
        return vpnAccess;
    }

    public void setVpnAccess(String vpnAccess) {
        this.vpnAccess = vpnAccess;
    }


///CLOVER:ON

}
