package com.bt.rsqe.expedio.order;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ManageOrderResponseDTO {

    @XmlElement
    public String expedioReference;

    @XmlElement
    public String orderReference;

    @XmlElement
    public String lineItemId;

    public ManageOrderResponseDTO() {
        //for jaxb
    }

    public ManageOrderResponseDTO(String lineItemId, String expRef, String orderReference) {
        this.lineItemId = lineItemId;
        this.expedioReference = expRef;
        this.orderReference = orderReference;
    }

    @Override
    public String toString() {
        return "ManageOrderResponseDTO{" +
               "expedioReference='" + expedioReference + '\'' +
               ", orderReference='" + orderReference + '\'' +
               ", lineItemId='" + lineItemId + '\'' +
               '}';
    }

}
