package com.bt.rsqe.projectengine.web;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class RequestDiscountResponseDTO {
    @XmlElement
    public String status;
    @XmlElement
    public String message;

    public RequestDiscountResponseDTO() {
        //for jaxb
    }

    public RequestDiscountResponseDTO(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
