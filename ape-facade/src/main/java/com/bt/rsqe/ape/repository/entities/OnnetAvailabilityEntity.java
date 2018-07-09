package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.SupplierSite;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ONNET_AVAILABILITY_STATUS")
public class OnnetAvailabilityEntity {

    @Id
    @Column(name = "SITE_ID")
    private Long siteId;

    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Column(name = "SITE_NAME")
    private String siteName;

    @Column(name = "COUNTRY_ISO_CODE")
    private String countryISOCode;

    @Column(name = "COUNTRY_NAME")
    private String countryName;

    @Column(name = "CITY")
    private String city;

    @Column(name = "EXPIRY_DATE")
    private Date expiryDate;

    @Column(name = "REQUEST_TIMEOUT")
    private Date timeout;

    @Column(name = "ONNET_AVAILABILITY")
    private String onNetAvailability;

    @Column(name = "ONNET_AVAILABILITY_TYPE_ID")
    private Integer onNetAvailabilityTypeId;

    @Column(name = "FAILURE_REASON")
    private String failureReason;

    public OnnetAvailabilityEntity() {
    }

    public OnnetAvailabilityEntity(Long siteId, Long customerId, String siteName, String countryISOCode, String countryName, String city, Date expiryDate, Date timeout, String onNetAvailability, Integer onNetAvailabilityTypeId, String failureReason) {
        this.siteId = siteId;
        this.customerId = customerId;
        this.siteName = siteName;
        this.countryISOCode = countryISOCode;
        this.countryName = countryName;
        this.city = city;
        this.expiryDate = expiryDate;
        this.onNetAvailability=onNetAvailability;
        this.timeout = timeout;
        this.onNetAvailabilityTypeId = onNetAvailabilityTypeId;
        this.failureReason = failureReason;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getCountryISOCode() {
        return countryISOCode;
    }

    public void setCountryISOCode(String countryISOCode) {
        this.countryISOCode = countryISOCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Date getTimeout() {
        return timeout;
    }

    public void setTimeout(Date timeout) {
        this.timeout = timeout;
    }

    public Integer getOnNetAvailabilityTypeId() {
        return onNetAvailabilityTypeId;
    }

    public void setOnNetAvailabilityTypeId(Integer onNetAvailabilityTypeId) {
        this.onNetAvailabilityTypeId = onNetAvailabilityTypeId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getOnNetAvailability() {
        return onNetAvailability;
    }

    public void setOnNetAvailability(String onNetAvailability) {
        this.onNetAvailability = onNetAvailability;
    }
}
