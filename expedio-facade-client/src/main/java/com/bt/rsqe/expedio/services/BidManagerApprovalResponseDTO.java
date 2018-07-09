package com.bt.rsqe.expedio.services;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BidManagerApprovalResponseDTO {

    @XmlElement
    public String activityId;

    public BidManagerApprovalResponseDTO() {
        /* for jaxb*/
    }

    public BidManagerApprovalResponseDTO(String activityId) {
        this.activityId = activityId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DiscountApprovalResponseDTO");
        sb.append("{activityId='").append(activityId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
