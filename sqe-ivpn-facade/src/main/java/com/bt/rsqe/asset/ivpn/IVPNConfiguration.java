package com.bt.rsqe.asset.ivpn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class IVPNConfiguration {
    @XmlElement
    public String siteId;
    @XmlElement
    public String resiliency;
    @XmlElement
    public boolean reachOutInterconnect;
    @XmlElement
    private List<IVPNLegConfiguration> legConfigurations;

    public IVPNConfiguration() {
    }

    public String getSiteId() {
        return siteId;
    }

    public String getResiliency() {
        return resiliency;
    }

    public boolean isReachOutInterconnect() {
        return reachOutInterconnect;
    }

    public List<IVPNLegConfiguration> getLegConfigurations() {
        return legConfigurations;
    }


}
