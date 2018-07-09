package com.bt.rsqe.sqefacade.integration;

import com.bt.rsqe.SqeIvpnEnvironmentConfig;
import com.bt.rsqe.asset.ivpn.dto.IVPNConfigurationDTO;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.domain.project.SiteId;
import com.bt.rsqe.sqefacade.InProgressAssetResource;
import com.bt.rsqe.sqefacade.domain.IvpnAssetId;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class GetInProgressAssetIntegrationTest {

    private static InProgressAssetResource inProgressAssetResource;

    @Before
    public void setup() {
        SqeIvpnEnvironmentConfig envConfig = ConfigurationProvider.provide(SqeIvpnEnvironmentConfig.class, "apl10045t1app01");
        inProgressAssetResource = new InProgressAssetResource(envConfig.getCustomerInventoryConfig().getSqeIvpnFacadeConfig());
    }

    @Test
    public void shouldInvokeSqeIvpnToFetchInProgressAssetsBasedOnSiteIdAndQuoteName() {

        IVPNConfigurationDTO ivpnConfigurationDTO = inProgressAssetResource.get(SiteId.newInstance(938692L), "INOXTIC_1234").get(0);

        assertThat(ivpnConfigurationDTO.getCustomerId(), is("116932"));
        assertThat(ivpnConfigurationDTO.getContractId(), is("436256"));
        assertThat(ivpnConfigurationDTO.getSiteId(), is("938692"));
        assertThat(ivpnConfigurationDTO.getResiliency(), is("SECURE"));
        assertThat(ivpnConfigurationDTO.isReachOutInterconnect(), is(false));

        assertThat(ivpnConfigurationDTO.primaryLeg().getUuid(), is("d515a21006734e908118b412812a0d"));
        assertThat(ivpnConfigurationDTO.primaryLeg().getPortSpeed(), is("4Mbps"));
        assertThat(ivpnConfigurationDTO.primaryLeg().getAccessTechnology(), is("Leased Line"));

        assertThat(ivpnConfigurationDTO.secondaryLeg().getUuid(), is("363b9d3ace3b484d95946ea1d579b4"));
        assertThat(ivpnConfigurationDTO.secondaryLeg().getPortSpeed(), is("4Mbps"));
        assertThat(ivpnConfigurationDTO.secondaryLeg().getAccessTechnology(), is("Leased Line"));
    }

    @Test
    public void shouldInvokeSqeIvpnToFetchInProgressAssetsBaseOnQuoteName() {
        List<IVPNConfigurationDTO> configurationDTOs = inProgressAssetResource.get("INOXTIC_1234");
        assertThat(configurationDTOs.size(), is(5));
    }


    @Test
    public void shouldGetInProgressIVPNAssetsBasedOnSiteIdAndUuid() {

        IVPNConfigurationDTO ivpnConfigurationDTO = inProgressAssetResource.get(SiteId.newInstance(938692L), IvpnAssetId.newInstance("d515a21006734e908118b412812a0d"));

        assertThat(ivpnConfigurationDTO.getResiliency(), is("SECURE"));
        assertThat(ivpnConfigurationDTO.isReachOutInterconnect(), is(false));
        assertThat(ivpnConfigurationDTO.primaryLeg().getPortSpeed(), is("4Mbps"));
        assertThat(ivpnConfigurationDTO.primaryLeg().getAccessTechnology(), is("Leased Line"));
    }
}
