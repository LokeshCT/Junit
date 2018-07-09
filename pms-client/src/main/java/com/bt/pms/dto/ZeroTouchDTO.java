package com.bt.pms.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ZeroTouchDTO {
    @XmlAttribute
    private String predefinedOutputDescription;
    @XmlAttribute
    private String responseStatus;

    public ZeroTouchDTO() {
    }

    public ZeroTouchDTO(String predefinedOutputDescription, String responseStatus) {
        this.predefinedOutputDescription = predefinedOutputDescription;
        this.responseStatus = responseStatus;
    }

    public String getPredefinedOutputDescription() {
        return predefinedOutputDescription;
    }

    public void setPredefinedOutputDescription(String predefinedOutputDescription) {
        this.predefinedOutputDescription = predefinedOutputDescription;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }
}
