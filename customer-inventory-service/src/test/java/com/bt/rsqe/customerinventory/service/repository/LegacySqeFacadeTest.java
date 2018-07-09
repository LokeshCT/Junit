package com.bt.rsqe.customerinventory.service.repository;

import com.bt.rsqe.ConnectionRole;
import com.bt.rsqe.asset.ivpn.IVPNQuote;
import com.bt.rsqe.asset.ivpn.dto.IVPNConfigurationDTO;
import com.bt.rsqe.asset.ivpn.dto.IVPNLegConfigurationDTO;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.domain.ClassPathResource;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.sqefacade.InProgressAssetResource;
import com.bt.rsqe.sqefacade.domain.IvpnAssetId;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.utils.RsqeCharset;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.JsonParser;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static com.bt.rsqe.IVPNAttributeName.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LegacySqeFacadeTest {

    private static IVPNConfigurationDTO ivpnConfigurationDTO;

    @BeforeClass
    public static void setup() throws IOException {
        String sampleResponse = new ClassPathResource("com/bt/rsqe/customerinventory/service/repository/get-inprogress-asset-by-siteId-response-template.json").textContent(RsqeCharset.defaultCharset());
        String jsonString = new JsonParser().parse(sampleResponse).toString();
        ivpnConfigurationDTO = JSONSerializer.getInstance().deSerialize(jsonString, IVPNQuote.class).toDTO().get(0);
    }

    @Test
    public void shouldGetExternalAssetWithPrimaryLeg() {
        // Set the response for primary leg with reach In
        ivpnConfigurationDTO.setReachOutInterconnect(true);
        IVPNLegConfigurationDTO ivpnLegConfigurationDTO = ivpnConfigurationDTO.getIVPNLegConfigurations().get(0);
        ivpnLegConfigurationDTO.setLegType(ConnectionRole.PRIMARY.toString());

        InProgressAssetResource inProgressAssetResource = mock(InProgressAssetResource.class);
        when(inProgressAssetResource.get(com.bt.rsqe.domain.project.SiteId.newInstance(1L), IvpnAssetId.newInstance("anExternalId"))).thenReturn(ivpnConfigurationDTO);

        Optional<CIFAsset> cifAssetOptional = new LegacySqeFacade(inProgressAssetResource).getAsset(new SiteId("1"), "anExternalId", new LengthConstrainingProductInstanceId("anId"), 1, ProductInstanceState.LIVE, false);

        CIFAsset cifAsset = cifAssetOptional.get();

        assertThat(cifAsset.getAssetKey().getAssetId(), is("anId"));
        assertThat(cifAsset.getAssetVersionStatus(), is(AssetVersionStatus.IN_SERVICE));

        assertThat(Iterables.find(cifAsset.getCharacteristics(), byName(REACH_OUT_INTERCONNECT.getDpv2Name())).getValue(), is("Yes"));
        assertThat(Iterables.find(cifAsset.getCharacteristics(), byName(CONNECTION_ROLE.getDpv2Name())).getValue(), is(ConnectionRole.PRIMARY.toString()));
        assertThat(Iterables.find(cifAsset.getCharacteristics(), byName(ACCESS_TECHNOLOGY.getDpv2Name())).getValue(), is("Ethernet"));
        assertThat(Iterables.find(cifAsset.getCharacteristics(), byName(PORT_SPEED.getDpv2Name())).getValue(), is("1025MBPS"));

    }

    @Test
    public void shouldGetExternalAssetWithSecondaryLeg() {

        // Set the response for primary leg with reach In
        ivpnConfigurationDTO.setReachOutInterconnect(false);
        IVPNLegConfigurationDTO ivpnLegConfigurationDTO = ivpnConfigurationDTO.getIVPNLegConfigurations().get(0);
        ivpnLegConfigurationDTO.setLegType(ConnectionRole.SECONDARY.toString());

        InProgressAssetResource inProgressAssetResource = mock(InProgressAssetResource.class);
        when(inProgressAssetResource.get(com.bt.rsqe.domain.project.SiteId.newInstance(1L), IvpnAssetId.newInstance("anExternalId"))).thenReturn(ivpnConfigurationDTO);

        Optional<CIFAsset> cifAssetOptional = new LegacySqeFacade(inProgressAssetResource).getAsset(new SiteId("1"), "anExternalId", new LengthConstrainingProductInstanceId("anId"), 1, ProductInstanceState.LIVE, true);

        CIFAsset cifAsset = cifAssetOptional.get();

        assertThat(cifAsset.getAssetKey().getAssetId(), is("anId"));
        assertThat(cifAsset.getAssetVersionStatus(), is(AssetVersionStatus.IN_SERVICE));

        assertThat(Iterables.find(cifAsset.getCharacteristics(), byName(REACH_OUT_INTERCONNECT.getDpv2Name())).getValue(), is("No"));
        assertThat(Iterables.find(cifAsset.getCharacteristics(), byName(CONNECTION_ROLE.getDpv2Name())).getValue(), is(ConnectionRole.SECONDARY.toString()));
        assertThat(Iterables.find(cifAsset.getCharacteristics(), byName(ACCESS_TECHNOLOGY.getDpv2Name())).getValue(), is("Ethernet"));
        assertThat(Iterables.find(cifAsset.getCharacteristics(), byName(PORT_SPEED.getDpv2Name())).getValue(), is("1025MBPS"));

    }

    @Test
    public void shouldNotReturnAnyAssetUponAnyExceptionDuringFacadeCall() {
        InProgressAssetResource inProgressAssetResource = mock(InProgressAssetResource.class);
        when(inProgressAssetResource.get(com.bt.rsqe.domain.project.SiteId.newInstance(1L), IvpnAssetId.newInstance("anExternalId"))).thenThrow(Exception.class);
        Optional<CIFAsset> cifAssetOptional = new LegacySqeFacade(inProgressAssetResource).getAsset(new SiteId("1"), "anExternalId", new LengthConstrainingProductInstanceId("anId"), 1, ProductInstanceState.LIVE, true);
        assertThat(cifAssetOptional.isPresent(), is(false));
    }

    private Predicate<? super CIFAssetCharacteristic> byName(final String name) {
        return new Predicate<CIFAssetCharacteristic>() {
            @Override
            public boolean apply(CIFAssetCharacteristic input) {
                return input.getName().equals(name);
            }
        };
    }
}
