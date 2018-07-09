package com.bt.nrm.dto.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by 608143048 on 10/12/2015.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NonStandardRequestSiteDTO {

    private String siteId;
    private String siteName;
    private String countryName;
    private String countryISOCode;
    private NonStandardRequestSiteDetailsDTO primaryDetails;
    private NonStandardRequestSiteDetailsDTO secondaryDetails;

    public NonStandardRequestSiteDTO() {
    }

    public NonStandardRequestSiteDTO(String siteId, String siteName, String countryName, String countryISOCode, NonStandardRequestSiteDetailsDTO primaryDetails, NonStandardRequestSiteDetailsDTO secondaryDetails) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.countryName = countryName;
        this.countryISOCode = countryISOCode;
        this.primaryDetails = primaryDetails;
        this.secondaryDetails = secondaryDetails;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryISOCode() {
        return countryISOCode;
    }

    public void setCountryISOCode(String countryISOCode) {
        this.countryISOCode = countryISOCode;
    }

    public NonStandardRequestSiteDetailsDTO getPrimaryDetails() {
        return primaryDetails;
    }

    public void setPrimaryDetails(NonStandardRequestSiteDetailsDTO primaryDetails) {
        this.primaryDetails = primaryDetails;
    }

    public NonStandardRequestSiteDetailsDTO getSecondaryDetails() {
        return secondaryDetails;
    }

    public void setSecondaryDetails(NonStandardRequestSiteDetailsDTO secondaryDetails) {
        this.secondaryDetails = secondaryDetails;
    }
}
