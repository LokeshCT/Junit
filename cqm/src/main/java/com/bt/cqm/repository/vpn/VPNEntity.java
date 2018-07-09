package com.bt.cqm.repository.vpn;

import com.bt.cqm.dto.VPNDTO;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(VPNCustomerID.class)
@Table(schema = "BFG_OWNER", name = "VPN_DETAILS_V")
/*@NamedQuery(name = "BFG_VPN_DETAILS_WITH_ROWID", QUERY = "SELECT VNW_ID AS VNW_ID," +
        "VNW_SERVICE_ID            AS VNW_SERVICE_ID," +
        "VNW_DIAL_ACCESS_TYPE        AS VNW_DIAL_ACCESS_TYPE," +
        "VNW_TYPE         AS VNW_TYPE," +
        "VNW_SECOND_FRIENDLY_NAME         AS VNW_SECOND_FRIENDLY_NAME," +
        "VNW_FRIENDLY_NAME             AS VNW_FRIENDLY_NAME," +
        "VPN_ACCESS      AS VPN_ACCESS," +
        "CUSTOMER_ID     AS CUSTOMER_ID " +
        "FROM BFG_OWNER.VPN_DETAILS_V  BFG_VPN  ")*/
public class VPNEntity {


    //@Id
    //@Column(name = "UNIQUE_ID")
    //private Long id;
    @Id
    @Column(name = "CUSTOMER_ID")
    private String customerID;
    @Id
    @Column(name = "VNW_SERVICE_ID")
    private String vpnServiceID;
    @Column(name = "VNW_ID")
    private Long vpnID;
    @Column(name = "VNW_DIAL_ACCESS_TYPE")
    private String vpnAccessType;
    @Column(name = "VNW_TYPE")
    private String vpnType;
    @Column(name = "VNW_SECOND_FRIENDLY_NAME")
    private String vpnSecondFriendlyName;
    @Column(name = "VNW_FRIENDLY_NAME")
    private String vpnFriendlyName;
    @Id
    @Column(name = "VPN_ACCESS")
    private String vpnAccess;


    public VPNEntity() {

    }

    public VPNEntity(String customerID, String vpnServiceID,
                     Long vpnID, String vpnAccessType,
                     String vpnType, String vpnSecondFriendlyName, String vpnFriendlyName,
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

    public VPNDTO dto(Long customerID) {
        return new VPNDTO(String.valueOf(customerID), vpnServiceID, String.valueOf(vpnID),
                          vpnAccessType, vpnType, vpnSecondFriendlyName, vpnFriendlyName, vpnAccess);
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

    public Long getVpnID() {
        return vpnID;
    }

    public void setVpnID(Long vpnID) {
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

class VPNCustomerID implements Serializable {
    private String customerID;
    private String vpnServiceID;
    private String vpnAccess;
}
