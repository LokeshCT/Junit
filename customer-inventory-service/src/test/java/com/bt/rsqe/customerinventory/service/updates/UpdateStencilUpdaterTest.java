package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetStencilDetail;
import com.bt.rsqe.customerinventory.service.client.domain.updates.AutoDefaultRelationshipsRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateStencilRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateStencilResponse;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.util.TestWithRules;
import org.junit.Test;

import static com.bt.rsqe.customerinventory.service.updates.CIFAssetMockHelper.mockCharacteristic;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.*;

public class UpdateStencilUpdaterTest extends TestWithRules {
    @Test
    public void shouldGenerateResponseFromRequest()
    {
        // Setup
        final AssetKey assetId = new AssetKey("assetId", 1);
        final String lineItemId = "lineItemId";
        final String productCode = "aProductCode";
        final int lockVersion = 5;

        CIFAssetOrchestrator assetOrchestrator = mock(CIFAssetOrchestrator.class) ;
        CIFAssetStencilDetail stencilDetail = mock(CIFAssetStencilDetail.class) ;
        CIFAsset cifAsset = mock (CIFAsset.class);

        when(stencilDetail.getStencilCode()).thenReturn("oldId") ;
        when(cifAsset.getStencilDetail()).thenReturn(stencilDetail) ;
        when(cifAsset.getAssetKey()).thenReturn(assetId);
        when(cifAsset.getLineItemId()).thenReturn(lineItemId);
        when(cifAsset.getProductCode()).thenReturn(productCode);
        when(cifAsset.getQuoteOptionItemDetail()).thenReturn(new CIFAssetQuoteOptionItemDetail(null, lockVersion, false, false, null, null,
                                                                                               false, null, null, null, "name", true, ProductCategoryCode.NIL, null, false));
        mockCharacteristic(cifAsset, new CIFAssetCharacteristic(ProductOffering.STENCIL_RESERVED_NAME,"oldId",false)) ;
        when(assetOrchestrator.getAsset(new CIFAssetKey(assetId, anyListOf(CIFAssetExtension.class)))).thenReturn(cifAsset);

        CharacteristicChangeRequestBuilder characteristicChangeRequestBuilder = mock(CharacteristicChangeRequestBuilder.class);
        CancelRelationshipRequestBuilder cancelRelationshipRequestBuilder = mock(CancelRelationshipRequestBuilder.class);
        InvalidatePriceRequestBuilder invalidatePriceRequestBuilder = mock(InvalidatePriceRequestBuilder.class) ;
        DependentUpdateBuilderFactory dependentUpdateBuilderFactory = new DependentUpdateBuilderFactoryBuilder().with(characteristicChangeRequestBuilder).with(cancelRelationshipRequestBuilder).with(invalidatePriceRequestBuilder).build();
        UpdateStencilUpdater updater = new UpdateStencilUpdater(assetOrchestrator, dependentUpdateBuilderFactory) ;
        final UpdateStencilRequest request = new UpdateStencilRequest(assetId, "stencilId", "name", "uri", lineItemId,
                                                                      lockVersion) ;

        // Execute
        UpdateStencilResponse response = updater.performUpdate(request);

        // Assertions
        assertThat (response.getRequest().getAssetKey(), is(assetId)) ;
        assertThat (response.getRequest().getStencilId(), is("stencilId")) ;
        assertThat (response.getRequest().getName(), is("name")) ;
        assertThat (response.getRequest().getUri(), is("uri")) ;
        assertThat (response.getRequest().getLineItemId(), is(lineItemId)) ;
        assertThat (response.getRequest().getLockVersion(), is(lockVersion)) ;
        assertThat (response.getOldId(), is("oldId")) ;
        verify(characteristicChangeRequestBuilder, times(1)).defaultForAllCharacteristics(cifAsset, "stencilId", null);
        verify(cancelRelationshipRequestBuilder, times(1)).removeInvalidRelationships(cifAsset);
        assertThat(response.getDependantUpdates(),
                   hasItem((CIFAssetUpdateRequest)new AutoDefaultRelationshipsRequest(assetId, lineItemId, lockVersion, productCode, true)));
    }
}
