package com.bt.rsqe.asset.ivpn.dto;

import com.google.common.base.Predicate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;

import static com.google.common.collect.Iterables.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class IVPNLegConfigurationDTO {
    @XmlElement
    private String uuid;
    @XmlElement
    private String legType;
    @XmlElement
    private String portSpeed;
    @XmlElement
    private String accessTechnology;

    public IVPNLegConfigurationDTO() {}

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setLegType(String legType) {
        this.legType = legType;
    }

    public String getPortSpeed() {
        return portSpeed;
    }

    public void setPortSpeed(String portSpeed) {
        this.portSpeed = portSpeed;
    }

    public String getAccessTechnology() {
        return accessTechnology;
    }

    public void setAccessTechnology(String accessTechnology) {
        this.accessTechnology = accessTechnology;
    }

    public LegType legType() {
        return LegType.getLegTypeFrom(legType);
    }

    public boolean isPrimaryLeg() {
        return LegType.Primary.equals(legType());
    }

    enum LegType {
        Primary,
        Secondary;

        static LegType getLegTypeFrom(final String legType) {
            return find(Arrays.asList(LegType.values()), new Predicate<LegType>() {
                @Override
                public boolean apply(LegType input) {
                    return input.name().equalsIgnoreCase(legType);
                }
            });
        }
    }
}
