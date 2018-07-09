package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.client.domain.updates.InvalidatePriceRequest;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.ConfigurationPhase;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.project.PricingStatus;
import com.google.common.base.Optional;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by 802998369 on 07/07/2015.
 */
public class InvalidatePriceRequestBuilderTest {
    @Test
    public void shouldReturnInvalidatePriceRequestForRelationshipChange() {
        // Setup
        CIFAssetOrchestrator cifOrchestrator = mock(CIFAssetOrchestrator.class);
        PmrHelper pmrHelper = mock(PmrHelper.class);
        InvalidatePriceRequestBuilder builder = new InvalidatePriceRequestBuilder(pmrHelper);
        CIFAssetQuoteOptionItemDetail cifAssetQuoteOptionItemDetail = new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, true, ProductCategoryCode.NIL, null, false);
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().withID("assetId").withVersion(2).withLineItemId("lineItemId").withPricingStatus(PricingStatus.FIRM).with(cifAssetQuoteOptionItemDetail).build();

        // Execute
        Optional<InvalidatePriceRequest> requestOptional = builder.invalidatePriceForRelationshipChange(cifAsset);

        // Assert
        assertThat(requestOptional.isPresent(), is(true));
        InvalidatePriceRequest request = requestOptional.get();
        assertThat(request.isChangeTypeOf(InvalidatePriceRequest.ChangeType.RelationshipChange), is(true));
        assertThat(request.isChangeTypeOf(InvalidatePriceRequest.ChangeType.PriceAffectingChange), is(false));
        assertThat(request.getAssetKey(), is(new AssetKey("assetId", 2)));
        assertThat(request.getLineItemId(), is("lineItemId"));
        assertThat(request.getLockVersion(), is(1));
    }

    @Test
    public void shouldReturnInvalidatePriceRequestForCharacteristicChangeRFQ() {
        // Setup
        CIFAssetQuoteOptionItemDetail cifAssetQuoteOptionItemDetail = new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, true, ProductCategoryCode.NIL, null, false);
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().withID("assetId").withVersion(2).withLineItemId("lineItemId").withPricingStatus(PricingStatus.ICB).with(cifAssetQuoteOptionItemDetail).build();

        PmrHelper pmrHelper = mock(PmrHelper.class);
        ProductOffering productOffering = mock(ProductOffering.class);
        when(pmrHelper.getProductOffering(cifAsset)).thenReturn(productOffering);
        Attribute attribute = mock(Attribute.class);
        when(productOffering.getAttributes()).thenReturn(newArrayList(attribute));
        when(attribute.getName()).thenReturn(new AttributeName("characteristicName"));
        when(attribute.isAttributeRequiredForPhase(ConfigurationPhase.PRE_CREDIT_VET)).thenReturn(true);

        InvalidatePriceRequestBuilder builder = new InvalidatePriceRequestBuilder(pmrHelper);

        // Execute
        Optional<InvalidatePriceRequest> requestOptional = builder.invalidatePriceForCharacteristicChanges(cifAsset, newArrayList("characteristicName"));

        // Assert
        assertThat(requestOptional.isPresent(), is(true));
        InvalidatePriceRequest request = requestOptional.get();
        assertThat(request.isChangeTypeOf(InvalidatePriceRequest.ChangeType.RelationshipChange), is(false));
        assertThat(request.isChangeTypeOf(InvalidatePriceRequest.ChangeType.PriceAffectingChange), is(true));
        assertThat(request.getAssetKey(), is(new AssetKey("assetId", 2)));
        assertThat(request.getLineItemId(), is("lineItemId"));
        assertThat(request.getLockVersion(), is(1));
    }

    @Test
    public void shouldNotReturnInvalidatePriceRequestForCharacteristicChangeNotRFQ() {
        // Setup
        CIFAssetQuoteOptionItemDetail cifAssetQuoteOptionItemDetail = new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, true, ProductCategoryCode.NIL, null, false);
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().withID("assetId").withVersion(2).withLineItemId("lineItemId").with(cifAssetQuoteOptionItemDetail).build();

        PmrHelper pmrHelper = mock(PmrHelper.class);
        ProductOffering productOffering = mock(ProductOffering.class);
        when(pmrHelper.getProductOffering(cifAsset)).thenReturn(productOffering);
        Attribute attribute = mock(Attribute.class);
        when(productOffering.getAttribute(new AttributeName("characteristicName"))).thenReturn(attribute);
        when(attribute.isAttributeRequiredForPhase(ConfigurationPhase.PRE_CREDIT_VET)).thenReturn(false);

        InvalidatePriceRequestBuilder builder = new InvalidatePriceRequestBuilder(pmrHelper);

        // Execute
        Optional<InvalidatePriceRequest> requestOptional = builder.invalidatePriceForCharacteristicChanges(cifAsset, newArrayList("characteristicName"));

        // Assert
        assertThat(requestOptional.isPresent(), is(false));
    }

    @Test
    public void shouldBuildInvalidPriceRequestOnlyWhenPricingStatusIsOtherThanNotApplicable() {
        //Setup
        CIFAssetQuoteOptionItemDetail cifAssetQuoteOptionItemDetail = new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, true, ProductCategoryCode.NIL, null, false);
        CIFAsset pricingNonApplicableAsset = CIFAssetFixture.aCIFAsset().withID("assetIdOne").withVersion(1).withLineItemId("lineItemIdOne").withPricingStatus(PricingStatus.NOT_APPLICABLE).with(cifAssetQuoteOptionItemDetail).build();
        CIFAsset notPricedAsset = CIFAssetFixture.aCIFAsset().withID("assetIdTwo").withVersion(1).withLineItemId("lineItemIdTwo").withPricingStatus(PricingStatus.NOT_PRICED).with(cifAssetQuoteOptionItemDetail).build();
        PmrHelper pmrHelper = mock(PmrHelper.class);

        // Execute
        InvalidatePriceRequestBuilder builder = new InvalidatePriceRequestBuilder(pmrHelper);

        // Assert
        assertThat(builder.invalidatePriceForCharacteristicChanges(pricingNonApplicableAsset, newArrayList("characteristicName")).isPresent(), is(false));
        assertThat(builder.invalidatePriceForRelationshipChange(pricingNonApplicableAsset).isPresent(), is(false));

        assertThat(builder.invalidatePriceForCharacteristicChanges(notPricedAsset, newArrayList("characteristicName")).isPresent(), is(false));
        assertThat(builder.invalidatePriceForRelationshipChange(notPricedAsset).isPresent(), is(false));
    }

    @Test
    public void shouldReturnInvalidatePriceRequestForStencilChange() {
        // Setup
        PmrHelper pmrHelper = mock(PmrHelper.class);
        InvalidatePriceRequestBuilder builder = new InvalidatePriceRequestBuilder(pmrHelper);
        CIFAssetQuoteOptionItemDetail cifAssetQuoteOptionItemDetail = new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, true, ProductCategoryCode.NIL, null, false);
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().withID("assetId").withVersion(2).withLineItemId("lineItemId").withPricingStatus(PricingStatus.NOT_APPLICABLE).with(cifAssetQuoteOptionItemDetail).build();

        // Execute
        Optional<InvalidatePriceRequest> requestOptional = builder.invalidatePriceForStencilChange(cifAsset);

        // Assert
        assertThat(requestOptional.isPresent(), is(true));
        InvalidatePriceRequest request = requestOptional.get();
        assertThat(request.isChangeTypeOf(InvalidatePriceRequest.ChangeType.StencilChange), is(true));
        assertThat(request.getAssetKey(), is(new AssetKey("assetId", 2)));
        assertThat(request.getLineItemId(), is("lineItemId"));
        assertThat(request.getLockVersion(), is(1));
    }
}