package com.bt.pms.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by 609274802 on 14/12/2015.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CustomerOneTimeChargeDTO {
    @XmlAttribute
    private String oneOffRecommendedRetail;
    @XmlAttribute
    private String nrcPriceToPartner;
    @XmlAttribute
    private String oneOffCost;

    public CustomerOneTimeChargeDTO() {
    }

    public CustomerOneTimeChargeDTO(String oneOffRecommendedRetail, String nrcPriceToPartner, String oneOffCost) {
        this.oneOffRecommendedRetail = oneOffRecommendedRetail;
        this.nrcPriceToPartner = nrcPriceToPartner;
        this.oneOffCost = oneOffCost;
    }

    public String getOneOffRecommendedRetail() {
        return oneOffRecommendedRetail;
    }

    public void setOneOffRecommendedRetail(String oneOffRecommendedRetail) {
        this.oneOffRecommendedRetail = oneOffRecommendedRetail;
    }

    public String getNrcPriceToPartner() {
        return nrcPriceToPartner;
    }

    public void setNrcPriceToPartner(String nrcPriceToPartner) {
        this.nrcPriceToPartner = nrcPriceToPartner;
    }

    public String getOneOffCost() {
        return oneOffCost;
    }

    public void setOneOffCost(String oneOffCost) {
        this.oneOffCost = oneOffCost;
    }
}
