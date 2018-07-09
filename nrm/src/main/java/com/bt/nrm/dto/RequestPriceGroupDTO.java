package com.bt.nrm.dto;

import com.bt.nrm.repository.entity.RequestEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestPriceGroupDTO {

    private String requestPriceGroupId;
    private String requestId;
    private String siteId;
    private String sqeUniqueId;
    private String priceGroupType;
    private String priceGroupDescription;
    private String oneOffRecommendedRetail;
    private String recurringRecommendedRetail;
    private String nrcPriceToPartner; //Only for SalesChannelType = Indirect
    private String rcPriceToPartner; //Only for SalesChannelType = Indirect
    private String oneOffCost;
    private String recurringCost;

    public RequestPriceGroupDTO() {
    }

    public RequestPriceGroupDTO(String requestPriceGroupId, String requestId, String siteId, String sqeUniqueId, String priceGroupType, String priceGroupDescription, String oneOffRecommendedRetail, String recurringRecommendedRetail, String nrcPriceToPartner, String rcPriceToPartner, String oneOffCost, String recurringCost) {
        this.requestPriceGroupId = requestPriceGroupId;
        this.requestId = requestId;
        this.siteId = siteId;
        this.sqeUniqueId = sqeUniqueId;
        this.priceGroupType = priceGroupType;
        this.priceGroupDescription = priceGroupDescription;
        this.oneOffRecommendedRetail = oneOffRecommendedRetail;
        this.recurringRecommendedRetail = recurringRecommendedRetail;
        this.nrcPriceToPartner = nrcPriceToPartner;
        this.rcPriceToPartner = rcPriceToPartner;
        this.oneOffCost = oneOffCost;
        this.recurringCost = recurringCost;
    }

    public String getRequestPriceGroupId() {
        return requestPriceGroupId;
    }

    public void setRequestPriceGroupId(String requestPriceGroupId) {
        this.requestPriceGroupId = requestPriceGroupId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteUniqueId() {
        return sqeUniqueId;
    }

    public void setSiteUniqueId(String siteUniqueId) {
        this.sqeUniqueId = siteUniqueId;
    }

    public String getPriceGroupType() {
        return priceGroupType;
    }

    public void setPriceGroupType(String priceGroupType) {
        priceGroupType = priceGroupType;
    }

    public String getPriceGroupDescription() {
        return priceGroupDescription;
    }

    public void setPriceGroupDescription(String priceGroupDescription) {
        priceGroupDescription = priceGroupDescription;
    }

    public String getOneOffRecommendedRetail() {
        return oneOffRecommendedRetail;
    }

    public void setOneOffRecommendedRetail(String oneOffRecommendedRetail) {
        this.oneOffRecommendedRetail = oneOffRecommendedRetail;
    }

    public String getRecurringRecommendedRetail() {
        return recurringRecommendedRetail;
    }

    public void setRecurringRecommendedRetail(String recurringRecommendedRetail) {
        this.recurringRecommendedRetail = recurringRecommendedRetail;
    }

    public String getNrcPriceToPartner() {
        return nrcPriceToPartner;
    }

    public void setNrcPriceToPartner(String nrcPriceToPartner) {
        this.nrcPriceToPartner = nrcPriceToPartner;
    }

    public String getRcPriceToPartner() {
        return rcPriceToPartner;
    }

    public void setRcPriceToPartner(String rcPriceToPartner) {
        this.rcPriceToPartner = rcPriceToPartner;
    }

    public String getOneOffCost() {
        return oneOffCost;
    }

    public void setOneOffCost(String oneOffCost) {
        this.oneOffCost = oneOffCost;
    }

    public String getRecurringCost() {
        return recurringCost;
    }

    public void setRecurringCost(String recurringCost) {
        this.recurringCost = recurringCost;
    }
}

