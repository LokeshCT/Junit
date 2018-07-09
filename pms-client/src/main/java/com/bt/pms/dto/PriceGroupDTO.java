package com.bt.pms.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class PriceGroupDTO {
    @XmlAttribute
    private String priceGroupType;
    @XmlAttribute
    private String priceGroupDescription;
    @XmlAttribute
    private String oneOffRecommendedRetail;
    @XmlAttribute
    private String recurringRecommendedRetail;
    @XmlAttribute
    private String nrcPriceToPartner;
    @XmlAttribute
    private String rcPriceToPartner;
    @XmlAttribute
    private String oneOffCost;
    @XmlAttribute
    private String recurringCost;

    public PriceGroupDTO() {
    }

    public PriceGroupDTO(String priceGroupType, String priceGroupDescription, String oneOffRecommendedRetail, String recurringRecommendedRetail, String nrcPriceToPartner, String rcPriceToPartner, String oneOffCost, String recurringCost) {
        this.priceGroupType = priceGroupType;
        this.priceGroupDescription = priceGroupDescription;
        this.oneOffRecommendedRetail = oneOffRecommendedRetail;
        this.recurringRecommendedRetail = recurringRecommendedRetail;
        this.nrcPriceToPartner = nrcPriceToPartner;
        this.rcPriceToPartner = rcPriceToPartner;
        this.oneOffCost = oneOffCost;
        this.recurringCost = recurringCost;
    }

    public String getPriceGroupType() {
        return priceGroupType;
    }

    public void setPriceGroupType(String priceGroupType) {
        this.priceGroupType = priceGroupType;
    }

    public String getPriceGroupDescription() {
        return priceGroupDescription;
    }

    public void setPriceGroupDescription(String priceGroupDescription) {
        this.priceGroupDescription = priceGroupDescription;
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
