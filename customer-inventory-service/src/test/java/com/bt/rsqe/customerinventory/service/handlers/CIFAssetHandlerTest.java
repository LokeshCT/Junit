package com.bt.rsqe.customerinventory.service.handlers;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetLineItemKey;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import org.junit.Test;

import javax.persistence.NoResultException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CIFAssetHandlerTest{
    private CIFAsset cifAsset = aCIFAsset().build();

    @Test
    public void shouldCallOrchestratorToLoadRootAsset(){
        CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
        final CIFAssetLineItemKey CIFAssetLineItemKey = new CIFAssetLineItemKey(cifAsset.getLineItemId(), new ArrayList<CIFAssetExtension>(), "", "");
        when(cifAssetOrchestrator.getAsset(CIFAssetLineItemKey)).thenReturn(cifAsset);

        CIFAssetHandler cifAssetHandler = new CIFAssetHandler(cifAssetOrchestrator);
        Response response = cifAssetHandler.loadCIFAsset(CIFAssetLineItemKey);

        verify(cifAssetOrchestrator).getAsset(CIFAssetLineItemKey);
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat((CIFAsset)response.getEntity(), is(cifAsset));
    }

    @Test
    public void shouldReturn404WhenRootAssetDoesNotExist(){
        CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
        final CIFAssetLineItemKey CIFAssetLineItemKey = new CIFAssetLineItemKey(cifAsset.getLineItemId(), new ArrayList<CIFAssetExtension>(), "", "");
        CIFAssetHandler cifAssetHandler = new CIFAssetHandler(cifAssetOrchestrator);
        when(cifAssetOrchestrator.getAsset(CIFAssetLineItemKey))
            .thenThrow(new NoResultException());

        Response response = cifAssetHandler.loadCIFAsset(CIFAssetLineItemKey);

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void shouldCallOrchestratorToLoadAsset(){
        CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
        final CIFAssetKey cifAssetKey = new CIFAssetKey(cifAsset.getAssetKey());
        when(cifAssetOrchestrator.getAsset(cifAssetKey)).thenReturn(cifAsset);

        CIFAssetHandler cifAssetHandler = new CIFAssetHandler(cifAssetOrchestrator);
        Response response = cifAssetHandler.loadCIFAsset(cifAssetKey);

        verify(cifAssetOrchestrator).getAsset(cifAssetKey);
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat((CIFAsset)response.getEntity(), is(cifAsset));
    }

    @Test
    public void shouldReturn404WhenAssetDoesNotExist(){
        CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
        CIFAssetHandler cifAssetHandler = new CIFAssetHandler(cifAssetOrchestrator);
        final CIFAssetKey cifAssetKey = new CIFAssetKey(cifAsset.getAssetKey());
        when(cifAssetOrchestrator.getAsset(cifAssetKey))
            .thenThrow(new NoResultException());

        Response response = cifAssetHandler.loadCIFAsset(cifAssetKey);

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }
}