package com.bt.rsqe.asset.ivpn;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class IVPNLegConfiguration {
    @XmlElement
    public String uuid;
    @XmlElement
    public String legType;
    @XmlElement
    private IVPNAccessConfiguration accessConfiguration;
    @XmlElement
    private IVPNPortConfiguration portConfiguration;

    public IVPNLegConfiguration() {
    }

    public String getUuid() {
        return uuid;
    }

    public String getLegType() {
        return legType;
    }

    public IVPNAccessConfiguration getAccessConfiguration() {
        return accessConfiguration;
    }

    public IVPNPortConfiguration getPortConfiguration() {
        return portConfiguration;
    }
}
