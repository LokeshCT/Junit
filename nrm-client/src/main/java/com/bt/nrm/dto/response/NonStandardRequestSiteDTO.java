package com.bt.nrm.dto.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by 608143048 on 21/01/2016.
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NonStandardRequestSiteDTO {

    private String siteId;
    private String siteStatus;
    private NonStandardRequestPriceGroupDTO primaryPriceGroups;
    private NonStandardRequestPriceGroupDTO secondaryPriceGroups;

    public NonStandardRequestSiteDTO() {
    }

    public NonStandardRequestSiteDTO(String siteId, String siteStatus, NonStandardRequestPriceGroupDTO primaryPriceGroups, NonStandardRequestPriceGroupDTO secondaryPriceGroups) {
        this.siteId = siteId;
        this.siteStatus = siteStatus;
        this.primaryPriceGroups = primaryPriceGroups;
        this.secondaryPriceGroups = secondaryPriceGroups;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteStatus() {
        return siteStatus;
    }

    public void setSiteStatus(String siteStatus) {
        this.siteStatus = siteStatus;
    }

    public NonStandardRequestPriceGroupDTO getPrimaryPriceGroups() {
        return primaryPriceGroups;
    }

    public void setPrimaryPriceGroups(NonStandardRequestPriceGroupDTO primaryPriceGroups) {
        this.primaryPriceGroups = primaryPriceGroups;
    }

    public NonStandardRequestPriceGroupDTO getSecondaryPriceGroups() {
        return secondaryPriceGroups;
    }

    public void setSecondaryPriceGroups(NonStandardRequestPriceGroupDTO secondaryPriceGroups) {
        this.secondaryPriceGroups = secondaryPriceGroups;
    }
}
