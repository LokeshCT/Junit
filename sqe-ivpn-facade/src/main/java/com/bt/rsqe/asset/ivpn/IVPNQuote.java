package com.bt.rsqe.asset.ivpn;

import com.bt.rsqe.asset.ivpn.dto.IVPNConfigurationDTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class IVPNQuote {
    @XmlElement
    private String quoteHeaderName;
    @XmlElement
    private String customerId;
    @XmlElement
    private String contractId;
    @XmlElement
    private List<IVPNConfiguration> configuration;

    public IVPNQuote() {
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getContractId() {
        return contractId;
    }

    public List<IVPNConfiguration> getConfiguration() {
        return configuration;
    }

    public List<IVPNConfigurationDTO> toDTO() {
        return new ConfigurationDTOBuilder().with(this).build();
    }
}
