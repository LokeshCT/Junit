package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesReloadResponse;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import com.bt.rsqe.tpe.client.TemplateTpeClient;
import com.bt.rsqe.tpe.multisite.TPE_TemplateDetails_Request;
import com.bt.rsqe.tpe.multisite.TPE_TemplateDetails_Response;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;

public class SpecialBidCharacteristicsReloadUpdaterTest {
    @Test
    public void shouldReloadSpecialBidCharacteristicsWhenTpeTemplateNameChanges() {

        //Given
        final InvalidatePriceRequestBuilder invalidatePriceRequestBuilder = mock(InvalidatePriceRequestBuilder.class);
        final DependentUpdateBuilderFactory dependentUpdateBuilderFactory = new DependentUpdateBuilderFactoryBuilder().with(invalidatePriceRequestBuilder).build();
        CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
        TemplateTpeClient templateTpeClient = mock(TemplateTpeClient.class);
        ExternalAttributesHelper externalAttributesHelper = mock(ExternalAttributesHelper.class);
        TPE_TemplateDetails_Response detailsResponse = mock(TPE_TemplateDetails_Response.class);
        PmrHelper pmrHelper = mock(PmrHelper.class);
        AssetKey assetKey = new AssetKey("anAssetId", 1L);

        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().withProductIdentifier("S123", "A.1").withCharacteristic(SPECIAL_BID_TEMPLATE_RESERVED_NAME, "oldTemplateName")
                .withCharacteristic(CONFIGURATION_TYPE_RESERVED_NAME, "oldConfigType")
                .withCharacteristic(CONFIGURATION_CATEGORY_RESERVED_NAME, "oldConfigCat")
                .withCharacteristic(SPECIAL_BID_BILL_DESCRIPTION, "oldBillDesc")
                .build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships))))
                .thenReturn(cifAsset);

        when(pmrHelper.getProductOffering("S123")).thenReturn(new ProductOfferingFixture("S123")
                .withAttribute(CONFIGURATION_TYPE_RESERVED_NAME)
                .withAttribute(CONFIGURATION_CATEGORY_RESERVED_NAME)
                .withAttribute(SPECIAL_BID_BILL_DESCRIPTION).build());

        when(templateTpeClient.SQE_TPE_TemplateDetails(any(TPE_TemplateDetails_Request.class))).thenReturn(detailsResponse);
        when(detailsResponse.getConfiguration_Category()).thenReturn("newConfigCat");
        when(detailsResponse.getBill_Description()).thenReturn("newBillDesc");
        when(detailsResponse.getTemplate_Unique_id()).thenReturn("anUniqueId");
        when(detailsResponse.getTemplate_WIKI()).thenReturn("aWiki");

        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        when(externalAttributesHelper.getAttributes(cifAsset)).thenReturn(tpeRequestDTO);

        SpecialBidAttributesReloadRequest specialBidAttributesReloadRequest = new SpecialBidAttributesReloadRequest(new AssetKey("anAssetId", 1L), SPECIAL_BID_TEMPLATE_RESERVED_NAME, "newTemplateName");

        //When
        SpecialBidCharacteristicsReloadUpdater reloadUpdater = new SpecialBidCharacteristicsReloadUpdater(cifAssetOrchestrator, templateTpeClient, externalAttributesHelper, pmrHelper, dependentUpdateBuilderFactory);
        SpecialBidAttributesReloadResponse specialBidAttributesReloadResponse = reloadUpdater.performUpdate(specialBidAttributesReloadRequest);

        //Then
        assertThat(specialBidAttributesReloadResponse.getRequest(), is(specialBidAttributesReloadRequest));
        assertThat(specialBidAttributesReloadResponse.getCharacteristicChanges().size(), is(2));
        assertThat(specialBidAttributesReloadResponse.getCharacteristicChanges(), hasItems(new CharacteristicChange(CONFIGURATION_CATEGORY_RESERVED_NAME, "newConfigCat", "oldConfigCat"),
                new CharacteristicChange(SPECIAL_BID_BILL_DESCRIPTION, "newBillDesc", "oldBillDesc")));
        assertThat(specialBidAttributesReloadResponse.getDependantUpdates().isEmpty(), is(true));

        ArgumentCaptor<TpeRequestDTO> argumentCaptor = ArgumentCaptor.forClass(TpeRequestDTO.class);
        verify(externalAttributesHelper, times(1)).saveAttributes(eq(cifAsset), argumentCaptor.capture());

        TpeRequestDTO value = argumentCaptor.getValue();

        assertThat(value.tpeTemplateUniqueId, is("anUniqueId"));
        assertThat(value.templateWiki, is("aWiki"));

    }

}