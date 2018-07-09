package com.bt.rsqe.ape;

import com.bt.rsqe.ape.client.ApeOnNetBuildingClient;
import com.bt.rsqe.ape.config.SupplierCheckConfig;
import com.bt.rsqe.ape.dto.OnnetCheckRequestDTO;
import com.bt.rsqe.ape.dto.OnnetBuildingDTO;
import com.bt.rsqe.ape.onnet.OnnetBuildingsPerSite;
import com.bt.rsqe.ape.onnet.SiteDetails;
import com.bt.rsqe.ape.repository.entities.OnnetBuildingEntity;
import com.bt.rsqe.ape.source.OnnetDetailsOrchestrator;
import com.bt.rsqe.ape.source.processor.RequestBuilder;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static com.bt.rsqe.ape.OnNetBuildingPerSiteBuilder.OnNetBuildingBuilder.anOnNetBuilding;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class ApeOnnetBuildingResourceHandlerTest {
    private SupplierCheckConfig config;
    private CustomerResource customerResource;
    private RequestBuilder requestBuilder;
    private ApeOnnetBuildingResourceHandlerClient apeOnnetBuildingResourceHandlerClient;
    private ApeOnNetBuildingClient apeOnNetBuildingClient;
    private ApeOnnetBuildingResourceHandler apeOnnetBuildingResourceHandler;
    private OnnetDetailsOrchestrator orchestrator;
    List<OnnetBuildingEntity> onnetBuildingEntityList=newArrayList();

    @Before
    public void setUp() {
        config = mock(SupplierCheckConfig.class);
        customerResource = mock(CustomerResource.class);
        requestBuilder = mock(RequestBuilder.class);
        apeOnnetBuildingResourceHandlerClient =mock(ApeOnnetBuildingResourceHandlerClient.class);
        apeOnNetBuildingClient = mock(ApeOnNetBuildingClient.class);
        orchestrator=mock(OnnetDetailsOrchestrator.class);
        apeOnnetBuildingResourceHandler = new ApeOnnetBuildingResourceHandler(apeOnNetBuildingClient,config,customerResource,requestBuilder, apeOnnetBuildingResourceHandlerClient, orchestrator);

        OnnetBuildingEntity onnetBuildingEntity=new OnnetBuildingEntity("21","12","13","14","15");
        OnnetBuildingEntity onnetBuildingEntity1=new OnnetBuildingEntity("21","22","23","24","25");
        OnnetBuildingEntity onnetBuildingEntity2=new OnnetBuildingEntity("31","32","33","34","35");

        onnetBuildingEntityList.add(onnetBuildingEntity);
        onnetBuildingEntityList.add(onnetBuildingEntity1);
        onnetBuildingEntityList.add(onnetBuildingEntity2);
    }
    @Test
    public void shouldInvokeApeAndConvertResultIntoDTO() {

        when(apeOnNetBuildingClient.getOnNetBuildings(any(SiteDetails.class))).thenReturn(new OnnetBuildingsPerSite[]{
            new OnNetBuildingPerSiteBuilder().withSiteId("siteId").withBuildings(anOnNetBuilding().withStreetName("st1").withStreetNumber("1")).build()
        });

        OnnetCheckRequestDTO onnetCheckRequestDTO = new OnnetCheckRequestDTO("siteId", "10.10", "20.10", "GB", 0, "bfgSiteId","city", "street", "postCode", "telephoneNumber", null, null);
        List<OnnetBuildingDTO> actual = (List<OnnetBuildingDTO>) apeOnnetBuildingResourceHandler.getOnNetBuildings(onnetCheckRequestDTO).getEntity();

        OnnetBuildingDTO expected = new OnnetBuildingDTO(null, null, "1", "st1", null, null, null, null, null, 0, 0d, 0d, Lists.<String>newArrayList(), null, null, null, null);

        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is(expected));

        //Assert APE input.
        ArgumentCaptor<SiteDetails> apeInputArgumentCapture = ArgumentCaptor.forClass(SiteDetails.class);
        verify(apeOnNetBuildingClient).getOnNetBuildings(apeInputArgumentCapture.capture());
        SiteDetails actualApeInput = apeInputArgumentCapture.getValue();

        assertThat(actualApeInput, is(new SiteDetails("siteId", "bfgSiteId", -1, 0, 10.10d, 20.10d, "GB", "postCode", "city", "street", "telephoneNumber","","")));
    }


}
