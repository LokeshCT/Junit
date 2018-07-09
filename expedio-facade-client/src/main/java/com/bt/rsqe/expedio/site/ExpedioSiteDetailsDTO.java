package com.bt.rsqe.expedio.site;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExpedioSiteDetailsDTO {

    @XmlElement
    public int bfgSiteID;
    @XmlElement
    public int bfgAddressID;
    @XmlElement
    public int bfgSublocationID;
    @XmlElement
    public String childExpRef;
    @XmlElement
    public String crPropID;
    @XmlElement
    public int propSiteID;
    @XmlElement
    public int propAddressID;
    @XmlElement
    public int propSublocationID;

    // for jaxb
    public ExpedioSiteDetailsDTO() {
    }

    public ExpedioSiteDetailsDTO(int bfgSiteID,
                                 int bfgAddressID,
                                 int bfgSublocationID,
                                 String childExpRef,
                                 String crPropID,
                                 int propSiteID,
                                 int propAddressID,
                                 int propSublocationID) {

        this.bfgSiteID = bfgSiteID;
        this.bfgAddressID = bfgAddressID;
        this.bfgSublocationID = bfgSublocationID;
        this.childExpRef = childExpRef;
        this.crPropID = crPropID;
        this.propSiteID = propSiteID;
        this.propAddressID = propAddressID;
        this.propSublocationID = propSublocationID;
    }
}
