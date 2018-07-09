package com.bt.rsqe.asset.ivpn;

import com.bt.rsqe.asset.ivpn.dto.IVPNConfigurationDTO;
import com.bt.rsqe.asset.ivpn.dto.IVPNLegConfigurationDTO;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ConfigurationDTOBuilder {

    private final List<IVPNConfigurationDTO> iVPNConfigurationDTOs = newArrayList();

    public ConfigurationDTOBuilder() {
    }

    public List<IVPNConfigurationDTO> build() {
        return iVPNConfigurationDTOs;
    }

    public ConfigurationDTOBuilder with(IVPNQuote ivpnQuote) {
        for (IVPNConfiguration ivpnConfiguration : ivpnQuote.getConfiguration()) {
            IVPNConfigurationDTO ivpnConfigurationDTO = new IVPNConfigurationDTO();
            ivpnConfigurationDTO.setCustomerId(ivpnQuote.getCustomerId());
            ivpnConfigurationDTO.setContractId(ivpnQuote.getContractId());
            ivpnConfigurationDTO.setResiliency(ivpnConfiguration.getResiliency());
            ivpnConfigurationDTO.setReachOutInterconnect(ivpnConfiguration.isReachOutInterconnect());
            ivpnConfigurationDTO.setSiteId(ivpnConfiguration.getSiteId());
            withLegs(ivpnConfigurationDTO, ivpnConfiguration.getLegConfigurations());

            iVPNConfigurationDTOs.add(ivpnConfigurationDTO);
        }
        return this;
    }

    private void withLegs(IVPNConfigurationDTO ivpnConfigurationDTO, List<IVPNLegConfiguration> legConfigurations) {
        IVPNLegConfigurationDTO iVPNLegConfigurationDTO;
        List<IVPNLegConfigurationDTO> iVPNLegConfigurationDTOList = newArrayList();

        for (IVPNLegConfiguration legConfiguration : legConfigurations) {
            iVPNLegConfigurationDTO = new IVPNLegConfigurationDTO();
            iVPNLegConfigurationDTO.setLegType(legConfiguration.getLegType());
            iVPNLegConfigurationDTO.setUuid(legConfiguration.getUuid());

            applyAccessConfiguration(iVPNLegConfigurationDTO, legConfiguration.getAccessConfiguration());
            applyPortConfiguration(iVPNLegConfigurationDTO, legConfiguration.getPortConfiguration());

            iVPNLegConfigurationDTOList.add(iVPNLegConfigurationDTO);
        }
        ivpnConfigurationDTO.setiVPNLegConfigurations(iVPNLegConfigurationDTOList);
    }

    private void applyAccessConfiguration(IVPNLegConfigurationDTO IVPNLegConfigurationDTO, IVPNAccessConfiguration accessConfiguration) {
        IVPNLegConfigurationDTO.setAccessTechnology(accessConfiguration.getAccessTechnology());
    }

    private void applyPortConfiguration(IVPNLegConfigurationDTO IVPNLegConfigurationDTO, IVPNPortConfiguration portConfiguration) {
        IVPNLegConfigurationDTO.setPortSpeed(portConfiguration.getPortSpeed());
    }

}