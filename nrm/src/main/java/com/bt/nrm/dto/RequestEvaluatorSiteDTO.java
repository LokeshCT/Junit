package com.bt.nrm.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by 608143048 on 02/02/2016.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestEvaluatorSiteDTO {

    private String requestEvaluatorSiteId;
    private String siteId;
    private String siteName;
    private String countryName;
    private String countryISOAlpha2Code;
    private List<RequestEvaluatorPriceGroupDTO> requestEvaluatorPriceGroups;
    private Timestamp createdDate;
    private String createdUser;
    private Timestamp modifiedDate;
    private String modifiedUser;

    public RequestEvaluatorSiteDTO() {
    }

    public RequestEvaluatorSiteDTO(String requestEvaluatorSiteId, String siteId, String siteName, String countryName, String countryISOAlpha2Code, List<RequestEvaluatorPriceGroupDTO> requestEvaluatorPriceGroups, Timestamp createdDate, String createdUser, Timestamp modifiedDate, String modifiedUser) {
        this.requestEvaluatorSiteId = requestEvaluatorSiteId;
        this.siteId = siteId;
        this.siteName = siteName;
        this.countryName = countryName;
        this.countryISOAlpha2Code = countryISOAlpha2Code;
        this.requestEvaluatorPriceGroups = requestEvaluatorPriceGroups;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
    }

    public String getRequestEvaluatorSiteId() {
        return requestEvaluatorSiteId;
    }

    public void setRequestEvaluatorSiteId(String requestEvaluatorSiteId) {
        this.requestEvaluatorSiteId = requestEvaluatorSiteId;
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

    public List<RequestEvaluatorPriceGroupDTO> getRequestEvaluatorPriceGroups() {
        return requestEvaluatorPriceGroups;
    }

    public void setRequestEvaluatorPriceGroups(List<RequestEvaluatorPriceGroupDTO> requestEvaluatorPriceGroups) {
        this.requestEvaluatorPriceGroups = requestEvaluatorPriceGroups;
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
