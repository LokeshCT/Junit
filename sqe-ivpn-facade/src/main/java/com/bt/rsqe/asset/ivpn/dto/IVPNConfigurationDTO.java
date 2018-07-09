package com.bt.rsqe.asset.ivpn.dto;

import com.google.common.base.Predicate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.bt.rsqe.asset.ivpn.dto.IVPNLegConfigurationDTO.LegType.*;
import static com.google.common.collect.Iterables.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class IVPNConfigurationDTO {
    @XmlElement
    private String customerId;
    @XmlElement
    private String contractId;
    @XmlElement
    private String siteId;
    @XmlElement
    private String resiliency;
    @XmlElement
    private boolean reachOutInterconnect;
    @XmlElement
    private List<IVPNLegConfigurationDTO> iVPNLegConfigurations;

    public IVPNConfigurationDTO() {}

    public String getCustomerId() {
        return customerId;
    }

    public String getContractId() {
        return contractId;
    }

    public String getSiteId() {
        return siteId;
    }

    public String getResiliency() {
        return resiliency;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public void setResiliency(String resiliency) {
        this.resiliency = resiliency;
    }

    public boolean isReachOutInterconnect() {
        return reachOutInterconnect;
    }

    public void setReachOutInterconnect(boolean reachOutInterconnect) {
        this.reachOutInterconnect = reachOutInterconnect;
    }

    public List<IVPNLegConfigurationDTO> getIVPNLegConfigurations() {
        return iVPNLegConfigurations;
    }

    public void setiVPNLegConfigurations(List<IVPNLegConfigurationDTO> iVPNLegConfigurations) {
        this.iVPNLegConfigurations = iVPNLegConfigurations;
    }


    public IVPNLegConfigurationDTO primaryLeg() {
        return find(iVPNLegConfigurations, new Predicate<IVPNLegConfigurationDTO>() {
            @Override
            public boolean apply(IVPNLegConfigurationDTO input) {
                return Primary.equals(input.legType());
            }
        });
    }

    public IVPNLegConfigurationDTO secondaryLeg() {
        return find(iVPNLegConfigurations, new Predicate<IVPNLegConfigurationDTO>() {
            @Override
            public boolean apply(IVPNLegConfigurationDTO input) {
                return Secondary.equals(input.legType());
            }
        });
    }


}
