package com.bt.rsqe.asset.ivpn;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class IVPNAccessConfiguration {
    @XmlElement
    private String accessSpeed;
    @XmlElement
    private String accessType;
    @XmlElement
    private String accessTechnology;
    @XmlElement
    private String valueOfN;

    public IVPNAccessConfiguration() {
    }

    public String getAccessTechnology() {
        return accessTechnology;
    }
}
