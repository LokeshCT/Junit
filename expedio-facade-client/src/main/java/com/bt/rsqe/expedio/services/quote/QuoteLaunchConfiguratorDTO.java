package com.bt.rsqe.expedio.services.quote;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181(Goutam Roy)
 * Date: 17/01/14
 * Time: 12:48
 * Following parameter are required to get the GUID from GUID
 * for existing quote.
 * To change this template use File | Settings | File Templates.
 */


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class QuoteLaunchConfiguratorDTO {

    @XmlElement
    private String quoteID;

    @XmlElement
    private String quoteVersion;

    @XmlElement
    private String EIN;

    @XmlElement
    private String boatID;

    @XmlElement
    private String salesRepName;

    @XmlElement
    private String userRole;

    @XmlElement
    private String roleType;

    @XmlElement
    private String userEmailId;

    @XmlElement
    private String managedCustomer;


    @XmlElement
    private String ceaseOptimizationFlag ;

    ///CLOVER:OFF

    public QuoteLaunchConfiguratorDTO() {
    }

    public String getQuoteID() {
        return quoteID;
    }

    public String getQuoteVersion() {
        return quoteVersion;
    }

    public String getEIN() {
        return EIN;
    }

    public String getBoatID() {
        return boatID;
    }

    public String getSalesRepName() {
        return salesRepName;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getRoleType() {
        return roleType;
    }

    public String getUserEmailId() {
        return userEmailId;
    }

    public String getManagedCustomer() {
        return managedCustomer;
    }

    public String getCeaseOptimizationFlag() {
        return ceaseOptimizationFlag;
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public String toString() {
        return "QuoteLaunchConfiguratorDTO{" +
               "quoteID='" + quoteID + '\'' +
               ", quoteVersion='" + quoteVersion + '\'' +
               ", EIN='" + EIN + '\'' +
               ", boatID='" + boatID + '\'' +
               ", salesRepName='" + salesRepName + '\'' +
               ", userRole='" + userRole + '\'' +
               ", roleType='" + roleType + '\'' +
               ", userEmailId='" + userEmailId + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    ///CLOVER:ON

    public static class Builder {
        private String quoteID;
        private String quoteVersion;
        private String EIN;
        private String boatID;
        private String salesRepName;
        private String userRole;
        private String roleType;
        private String userEmailId;
        private String managedCustomer;
        private String ceaseOptimizationFlag ;

        public Builder withQuoteID(String quoteID) {
            this.quoteID = quoteID;
            return this;
        }

        public Builder withQuoteVersion(String quoteVersion) {
            this.quoteVersion = quoteVersion;
            return this;
        }

        public Builder withEIN(String EIN) {
            this.EIN = EIN;
            return this;
        }

        public Builder withBoatID(String boatID) {
            this.boatID = boatID;
            return this;
        }

        public Builder withSalesRepName(String salesRepName) {
            this.salesRepName = salesRepName;
            return this;
        }

        public Builder withUserRole(String userRole) {
            this.userRole = userRole;
            return this;
        }

        public Builder withRoleType(String roleType) {
            this.roleType = roleType;
            return this;
        }

        public Builder withUserEmailId(String userEmailId) {
            this.userEmailId = userEmailId;
            return this;
        }

        public Builder withManagedCustomer(String managedCustomer) {
            this.managedCustomer = managedCustomer;
            return this;
        }

        public Builder withCeaseOptimizationFlag(String ceaseOptimizationFlag) {
            this.ceaseOptimizationFlag = ceaseOptimizationFlag;
            return this;
        }

        public QuoteLaunchConfiguratorDTO build() {
            QuoteLaunchConfiguratorDTO quoteLaunchConfiguratorDTO = new QuoteLaunchConfiguratorDTO();
            quoteLaunchConfiguratorDTO.quoteID = this.quoteID;
            quoteLaunchConfiguratorDTO.quoteVersion = this.quoteVersion;
            quoteLaunchConfiguratorDTO.EIN = this.EIN;
            quoteLaunchConfiguratorDTO.boatID = this.boatID;
            quoteLaunchConfiguratorDTO.salesRepName = this.salesRepName;
            quoteLaunchConfiguratorDTO.userRole = this.userRole;
            quoteLaunchConfiguratorDTO.roleType = this.roleType;
            quoteLaunchConfiguratorDTO.userEmailId = this.userEmailId;
            quoteLaunchConfiguratorDTO.managedCustomer = this.managedCustomer;
            quoteLaunchConfiguratorDTO.ceaseOptimizationFlag = this.ceaseOptimizationFlag;
            return quoteLaunchConfiguratorDTO;
        }
    }

}
