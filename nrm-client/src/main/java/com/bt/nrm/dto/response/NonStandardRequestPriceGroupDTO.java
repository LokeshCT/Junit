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
public class NonStandardRequestPriceGroupDTO {

    private String requestPriceGroupId;
    private String requestId;
    private String siteId;
    private String sqeUniqueId;
    private String priceGroupType;
    private String priceGroupDescription;
    private String oneOffRecommendedRetail; //Only for SalesChannelType = Indirect. For Direct type, it will be 0.
    private String recurringRecommendedRetail; //Only for SalesChannelType = Indirect. For Direct type, it will be 0.
    private String nrcPriceToPartner;
    private String rcPriceToPartner;
    private String oneOffCost;
    private String recurringCost;

    public NonStandardRequestPriceGroupDTO() {
    }

    public NonStandardRequestPriceGroupDTO(String requestPriceGroupId, String priceGroupType, String priceGroupDescription, String sqeUniqueId, String oneOffRecommendedRetail, String recurringRecommendedRetail, String nrcPriceToPartner, String rcPriceToPartner, String oneOffCost, String recurringCost) {
        this.requestPriceGroupId = requestPriceGroupId;
        this.priceGroupType = priceGroupType;
        this.priceGroupDescription = priceGroupDescription;
        this.sqeUniqueId = sqeUniqueId;
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

    public String getSqeUniqueId() {
        return sqeUniqueId;
    }

    public void setSqeUniqueId(String sqeUniqueId) {
        this.sqeUniqueId = sqeUniqueId;
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
