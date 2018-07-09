package com.bt.rsqe.sqefacade;

import com.bt.rsqe.asset.ivpn.IVPNQuote;
import com.bt.rsqe.asset.ivpn.dto.IVPNConfigurationDTO;
import com.bt.rsqe.domain.ClassPathResource;
import com.bt.rsqe.domain.project.SiteId;
import com.bt.rsqe.sqefacade.domain.IvpnAssetId;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.utils.RsqeCharset;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.RestResponse;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static junit.framework.Assert.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class InProgressAssetResourceTest {

    private InProgressAssetResource resource;
    private RestResponse mockClientResponse;

    @Before
    public void setup() {
        RestRequestBuilder mockRestRequestBuilder = mock(RestRequestBuilder.class);
        RestResource mockRestResource = mock(RestResource.class);
        mockClientResponse = mock(RestResponse.class);

        resource = new InProgressAssetResource(mockRestRequestBuilder);

        when(mockRestRequestBuilder.build(any(Map.class))).thenReturn(mockRestResource);
        when(mockRestRequestBuilder.build(any(String.class), any(String.class), any(String.class), any(String.class))).thenReturn(mockRestResource);
        when(mockRestResource.get()).thenReturn(mockClientResponse);
    }

    @Test
    public void shouldReturnValidResponse() throws IOException {
        String stubResponse = new ClassPathResource("com/bt/rsqe/sqefacade/get-inprogress-asset-by-siteId-response.json").textContent(RsqeCharset.defaultCharset());
        IVPNQuote IVPNQuoteDTO = JSONSerializer.getInstance().deSerialize(stubResponse, IVPNQuote.class);

        when(mockClientResponse.getEntity(IVPNQuote.class)).thenReturn(IVPNQuoteDTO);

        List<IVPNConfigurationDTO> configurationDTOs = resource.get(SiteId.newInstance(12345l), "Quote Name");

        IVPNConfigurationDTO IVPNConfigurationDTO = configurationDTOs.get(0);
        assertThat(IVPNConfigurationDTO.getIVPNLegConfigurations().size(), is(2));
        assertThat(IVPNConfigurationDTO.getResiliency(), is("Secure"));
        assertThat(IVPNConfigurationDTO.primaryLeg().getPortSpeed(), is("1025MBPS"));
        assertThat(IVPNConfigurationDTO.secondaryLeg().getPortSpeed(), is("1025MBPS"));
    }

    @Test
    public void shouldReturnAbsentForInvalidInput() {
        List<IVPNConfigurationDTO> configurationDTOs = resource.get(null, (String) null);
        assertTrue(configurationDTOs.size() == 0);
    }

    @Test
    public void shouldReturnObsentResponse() {
        doThrow(new RuntimeException()).when(mockClientResponse).getEntity(String.class);

        List<IVPNConfigurationDTO> configurationDTOs = resource.get(SiteId.newInstance(1l), "Quote Name");
        assertTrue(configurationDTOs.size() == 0);
    }

    @Test
    public void shouldReturnAbsentInCaseOfInvalidResponse() {
        when(mockClientResponse.getEntity(String.class)).thenReturn("{}");

        List<IVPNConfigurationDTO> configurationDTOs = resource.get(SiteId.newInstance(1l), "Quote Name");
        assertTrue(configurationDTOs.size() == 0);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowExceptionIfThereIsNoSecondaryLeg() throws IOException {
        String stubResponse = new ClassPathResource("com/bt/rsqe/sqefacade/get-inprogress-asset-by-uuid-response.json").textContent(RsqeCharset.defaultCharset());
        IVPNQuote ivpnQuote = JSONSerializer.getInstance().deSerialize(stubResponse, IVPNQuote.class);

        when(mockClientResponse.getEntity(IVPNQuote.class)).thenReturn(ivpnQuote);

        IVPNConfigurationDTO iVPNConfigurationDTO = resource.get(SiteId.newInstance(12345l), IvpnAssetId.newInstance("dfdfafdsfdfdfdfdfdfdfdd"));
        iVPNConfigurationDTO.secondaryLeg();
    }

    @Test
    public void shouldReturnValidIVPNAssetByUuid() throws IOException {
        String stubResponse = new ClassPathResource("com/bt/rsqe/sqefacade/get-inprogress-asset-by-uuid-response.json").textContent(RsqeCharset.defaultCharset());
        IVPNQuote ivpnQuote = JSONSerializer.getInstance().deSerialize(stubResponse, IVPNQuote.class);

        when(mockClientResponse.getEntity(IVPNQuote.class)).thenReturn(ivpnQuote);

        IVPNConfigurationDTO iVPNConfigurationDTO = resource.get(SiteId.newInstance(12345l), IvpnAssetId.newInstance("dfdfafdsfdfdfdfdfdfdfdd"));
        assertThat(iVPNConfigurationDTO.getIVPNLegConfigurations().size(), is(1));
        assertTrue(iVPNConfigurationDTO.getIVPNLegConfigurations().get(0).isPrimaryLeg());
        assertThat(iVPNConfigurationDTO.getResiliency(), is("Secure"));
        assertTrue(iVPNConfigurationDTO.isReachOutInterconnect());
        assertThat(iVPNConfigurationDTO.primaryLeg().getPortSpeed(), is("1025MBPS"));
        assertThat(iVPNConfigurationDTO.primaryLeg().getAccessTechnology(), is("Ethernet"));
        assertThat(iVPNConfigurationDTO.primaryLeg().getUuid(), is("dfdfafdsfdfdfdfdfdfdfdd"));
    }
}
