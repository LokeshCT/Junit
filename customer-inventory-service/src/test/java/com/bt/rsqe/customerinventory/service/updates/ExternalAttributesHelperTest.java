package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import org.aspectj.lang.annotation.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ExternalAttributesHelperTest {

    private ProjectResource projectResource = mock(ProjectResource.class);
    private QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
    private QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
    private CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);

    @Test
    public void shouldGetExternalAttributesForGivenAsset() {
        //Given
        AssetKey assetKey = new AssetKey("anAsset", 1L);
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().withID("anAsset").withVersion(1L).build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, Collections.<CIFAssetExtension>emptyList()))).thenReturn(cifAsset);
        when(projectResource.quoteOptionResource(cifAsset.getProjectId())).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource(cifAsset.getQuoteOptionId())).thenReturn(quoteOptionItemResource);
        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        when(quoteOptionItemResource.getTpeRequest(assetKey.getAssetId(), assetKey.getAssetVersion())).thenReturn(tpeRequestDTO);

        //When
        ExternalAttributesHelper externalAttributesHelper = new ExternalAttributesHelper(projectResource);
        TpeRequestDTO requestDTO = externalAttributesHelper.getAttributes(cifAsset);

        //Then
        assertThat(requestDTO, is(tpeRequestDTO));
        verify(quoteOptionItemResource, times(1)).getTpeRequest(assetKey.getAssetId(), assetKey.getAssetVersion());
    }

    @Test
    public void shouldSaveExternalAttributesForGivenAsset() {
        //Given
        AssetKey assetKey = new AssetKey("anAsset", 1L);
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().withID("anAsset").withVersion(1L).build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, Collections.<CIFAssetExtension>emptyList()))).thenReturn(cifAsset);
        when(projectResource.quoteOptionResource(cifAsset.getProjectId())).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource(cifAsset.getQuoteOptionId())).thenReturn(quoteOptionItemResource);
        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();

        //When
        ExternalAttributesHelper externalAttributesHelper = new ExternalAttributesHelper(projectResource);
        externalAttributesHelper.saveAttributes(cifAsset, tpeRequestDTO);

        //Then
        verify(quoteOptionItemResource, times(1)).putTpeRequest(tpeRequestDTO);
    }

}