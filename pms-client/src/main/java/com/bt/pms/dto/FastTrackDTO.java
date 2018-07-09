package com.bt.pms.dto;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FastTrackDTO {
    @XmlAttribute
    private String defaultOutputDescription;

    public FastTrackDTO() {
    }

    public FastTrackDTO(String defaultOutputDescription) {
        this.defaultOutputDescription = defaultOutputDescription;
    }

    public String getDefaultOutputDescription() {
        return defaultOutputDescription;
    }

    public void setDefaultOutputDescription(String defaultOutputDescription) {
        this.defaultOutputDescription = defaultOutputDescription;
    }
}
