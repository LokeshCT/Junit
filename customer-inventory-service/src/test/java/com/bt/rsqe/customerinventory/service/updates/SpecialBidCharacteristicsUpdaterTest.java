package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidCharacteristicChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidCharacteristicChangeResponse;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;

import static com.bt.rsqe.domain.SpecialBidWellKnownAttribute.*;
import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SpecialBidCharacteristicsUpdaterTest {

    InvalidatePriceRequestBuilder invalidatePriceRequestBuilder ;
    DependentUpdateBuilderFactory dependentUpdateBuilderFactory ;

    @Before
    public void setUp () throws Exception
    {
        invalidatePriceRequestBuilder = mock(InvalidatePriceRequestBuilder.class) ;
        dependentUpdateBuilderFactory = new DependentUpdateBuilderFactoryBuilder().with(invalidatePriceRequestBuilder).build();
    }

    @Test
    public void shouldUpdateSpecialBidCharacteristics() {

        //Given
        CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
        ProjectResource projectResource = mock(ProjectResource.class);

        AssetKey assetKey = new AssetKey("anAsset", 1L);
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().withID("anAsset").withVersion(1L).build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, Collections.<CIFAssetExtension>emptyList()))).thenReturn(cifAsset);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        when(projectResource.quoteOptionResource(cifAsset.getProjectId())).thenReturn(quoteOptionResource);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(quoteOptionResource.quoteOptionItemResource(cifAsset.getQuoteOptionId())).thenReturn(quoteOptionItemResource);
        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        when(quoteOptionItemResource.getTpeRequest(assetKey.getAssetId(), assetKey.getAssetVersion())).thenReturn(tpeRequestDTO);
        SpecialBidCharacteristicsUpdater updater = new SpecialBidCharacteristicsUpdater(cifAssetOrchestrator,
                new SpecialBidWellKnownAttributeMapper(),
                                                                                        new SpecialBidTemplateAttributeMapper(), new ExternalAttributesHelper(projectResource), dependentUpdateBuilderFactory);

        //When
        CharacteristicChange characteristicChange = new CharacteristicChange("A", "200");
        SpecialBidCharacteristicChangeResponse changeResponse = updater.performUpdate(new SpecialBidCharacteristicChangeRequest(assetKey,
                                                                                                                                CIFAssetExtension.allExtensions(),
                                                                                                                                newArrayList(characteristicChange, new CharacteristicChange(CustomerValue.getAttributeName(), "1")), "lineItemId", 1));


        //verify
        ArgumentCaptor<TpeRequestDTO> argumentCaptor = ArgumentCaptor.forClass(TpeRequestDTO.class);
        verify(quoteOptionItemResource, times(1)).putTpeRequest(argumentCaptor.capture());

        TpeRequestDTO dto = argumentCaptor.getValue();
        assertThat(dto.customerValue, is(1L));
        assertThat(dto.requestName, is(""));

        List<TpeRequestDTO.TpeMandatoryAttributesDTO> dtoCollection = dto.tpeMandatoryAttributesDTOCollection;
        TpeRequestDTO.TpeMandatoryAttributesDTO tpeMandatoryAttributesDTO = dtoCollection.get(0);
        assertThat(tpeMandatoryAttributesDTO.getAttributeName(), is("A"));
        assertThat(tpeMandatoryAttributesDTO.getAttributeValue(), is("200"));
        assertThat(tpeMandatoryAttributesDTO.getAttributeClassifier(), is(TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier.PRIMARY));

        assertThat(changeResponse, notNullValue());
        assertThat(changeResponse.getCharacteristicChanges(), hasItem(characteristicChange));
    }

    @Test
    public void shouldSyncUpTpeTemplateNameDuringSpecialBidCharacteristicsUpdate() {

        //Given
        CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
        ProjectResource projectResource = mock(ProjectResource.class);

        AssetKey assetKey = new AssetKey("anAsset", 1L);
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().withID("anAsset").withVersion(1L).withCharacteristic(SPECIAL_BID_TEMPLATE_RESERVED_NAME, "someTpeTemplateName").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, Collections.<CIFAssetExtension>emptyList()))).thenReturn(cifAsset);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        when(projectResource.quoteOptionResource(cifAsset.getProjectId())).thenReturn(quoteOptionResource);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(quoteOptionResource.quoteOptionItemResource(cifAsset.getQuoteOptionId())).thenReturn(quoteOptionItemResource);
        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        when(quoteOptionItemResource.getTpeRequest(assetKey.getAssetId(), assetKey.getAssetVersion())).thenReturn(tpeRequestDTO);
        SpecialBidCharacteristicsUpdater updater = new SpecialBidCharacteristicsUpdater(cifAssetOrchestrator,
                new SpecialBidWellKnownAttributeMapper(),
                                                                                        new SpecialBidTemplateAttributeMapper(), new ExternalAttributesHelper(projectResource), dependentUpdateBuilderFactory);

        //When
        SpecialBidCharacteristicChangeResponse changeResponse = updater.performUpdate(new SpecialBidCharacteristicChangeRequest(assetKey,
                                                                                                                                CIFAssetExtension.allExtensions(),
                                                                                                                                Collections.<CharacteristicChange>emptyList(), "lineItemId", 1));


        //verify
        ArgumentCaptor<TpeRequestDTO> argumentCaptor = ArgumentCaptor.forClass(TpeRequestDTO.class);
        verify(quoteOptionItemResource, times(1)).putTpeRequest(argumentCaptor.capture());

        TpeRequestDTO dto = argumentCaptor.getValue();
        assertThat(dto.requestName, is("someTpeTemplateName"));
        assertThat(changeResponse, notNullValue());
        assertThat(changeResponse.getCharacteristicChanges().isEmpty(), is(true));
    }


}