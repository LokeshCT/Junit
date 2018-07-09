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
@Table(name = "SUPPLIER_SITE")
public class SupplierSiteEntity {

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

    @Column(name = "AVAILABILITY_TYPE_ID")
    private Integer availabilityTypeId;

    @Column(name = "AVAILABILITY_TELEPHONE_NUMBER")
    private String availabilityTelephoneNumber;

    @Column(name = "ERROR_DESCRIPTION")
    private String errorDescription;

    @OneToMany(mappedBy = "supplierSiteEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<SupplierProductEntity> supplierProductEntityList;

    public SupplierSiteEntity() {
    }

    public SupplierSiteEntity(Long siteId) {
        this.siteId = siteId;
    }

    public SupplierSiteEntity(Integer availabilityTypeId) {
        this.availabilityTypeId = availabilityTypeId;
    }

    public SupplierSiteEntity(Long siteId, Long customerId, String siteName, String countryISOCode, String countryName, Date expiryDate, Integer availabilityTypeId, String availabilityTelephoneNumber, String errorDescription, List<SupplierProductEntity> supplierProductEntityList, Date timeout) {
        this.siteId = siteId;
        this.customerId = customerId;
        this.siteName = siteName;
        this.countryISOCode = countryISOCode;
        this.countryName = countryName;
        this.expiryDate = expiryDate;
        this.timeout = timeout;
        this.availabilityTypeId = availabilityTypeId;
        this.availabilityTelephoneNumber = availabilityTelephoneNumber;
        this.errorDescription = errorDescription;
        this.supplierProductEntityList = supplierProductEntityList;
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

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getAvailabilityTypeId() {
        return availabilityTypeId;
    }

    public void setAvailabilityTypeId(Integer availabilityTypeId) {
        this.availabilityTypeId = availabilityTypeId;
    }

    public String getAvailabilityTelephoneNumber() {
        return availabilityTelephoneNumber;
    }

    public void setAvailabilityTelephoneNumber(String availabilityTelephoneNumber) {
        this.availabilityTelephoneNumber = availabilityTelephoneNumber;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public List<SupplierProductEntity> getSupplierProductEntityList() {
        return supplierProductEntityList;
    }

    public void setSupplierProductEntityList(List<SupplierProductEntity> supplierProductEntityList) {
        this.supplierProductEntityList = supplierProductEntityList;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Date getTimeout() {
        return timeout;
    }

    public void setTimeout(Date timeout) {
        this.timeout = timeout;
    }

    public SupplierSite toDto() {
        return new SupplierSite(getSiteId(), getSiteName(), getCountryISOCode(), getCity(), getCountryName(), getExpiryDate(), getAvailabilityTypeId(),
                getAvailabilityTelephoneNumber(), getErrorDescription(), null, null);

    }
}
