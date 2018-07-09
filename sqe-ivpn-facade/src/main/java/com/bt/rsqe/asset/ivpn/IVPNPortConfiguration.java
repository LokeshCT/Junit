package com.bt.rsqe.asset.ivpn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class IVPNPortConfiguration {
    @XmlElement
    private String portSpeed;
    @XmlElement
    private String aggregatedEFBandwidth;
    @XmlElement
    private String aggregatedAF1Bandwidth;
    @XmlElement
    private String aggregatedAF2Bandwidth;
    @XmlElement
    private String aggregatedAF3Bandwidth;
    @XmlElement
    private String aggregatedAF4Bandwidth;
    @XmlElement
    private String totalNoOfVPNConnections;
    @XmlElement
    private String dslCommercialSpeed;

    public IVPNPortConfiguration() {
    }

    public String getPortSpeed() {
        return portSpeed;
    }
}
