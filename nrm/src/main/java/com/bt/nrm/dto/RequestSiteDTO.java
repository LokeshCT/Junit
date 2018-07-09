package com.bt.nrm.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestSiteDTO {

    private String requestSiteId;
    private String siteId;
    private String siteName;
    private String countryName;
    private String countryISOAlpha2Code;
    private List<RequestAttributeDTO> siteAttributes;
    private List<RequestPriceGroupDTO> priceGroups;
    private Timestamp createdDate;
    private String createdUser;
    private Timestamp modifiedDate;
    private String modifiedUser;

    public RequestSiteDTO() {
    }

    public RequestSiteDTO(String requestSiteId, String siteId, String siteName, String countryName, String countryISOAlpha2Code, List<RequestAttributeDTO> siteAttributes, List<RequestPriceGroupDTO> priceGroups, Timestamp createdDate, String createdUser, Timestamp modifiedDate, String modifiedUser) {
        this.requestSiteId = requestSiteId;
        this.siteId = siteId;
        this.siteName = siteName;
        this.countryName = countryName;
        this.countryISOAlpha2Code = countryISOAlpha2Code;
        this.siteAttributes = siteAttributes;
        this.priceGroups = priceGroups;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
    }

    public String getRequestSiteId() {
        return requestSiteId;
    }

    public void setRequestSiteId(String requestSiteId) {
        this.requestSiteId = requestSiteId;
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

    public String getCountryISOAlpha2Code() {
        return countryISOAlpha2Code;
    }

    public void setCountryISOAlpha2Code(String countryISOAlpha2Code) {
        this.countryISOAlpha2Code = countryISOAlpha2Code;
    }

    public List<RequestAttributeDTO> getSiteAttributes() {
        return siteAttributes;
    }

    public void setSiteAttributes(List<RequestAttributeDTO> siteAttributes) {
        this.siteAttributes = siteAttributes;
    }

    public List<RequestPriceGroupDTO> getPriceGroups() {
        return priceGroups;
    }

    public void setPriceGroups(List<RequestPriceGroupDTO> priceGroups) {
        this.priceGroups = priceGroups;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Timestamp getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Timestamp modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedUser() {
        return modifiedUser;
    }

    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }
}
