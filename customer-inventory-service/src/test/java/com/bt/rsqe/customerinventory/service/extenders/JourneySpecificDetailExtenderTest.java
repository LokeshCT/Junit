package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.project.LineItemAction;
import com.bt.rsqe.pmr.dto.JourneyBehaviourDTO;
import com.bt.rsqe.projectengine.OrderLineItemDTO;
import com.bt.rsqe.projectengine.OrderLineItemResource;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import org.junit.Test;

import java.util.ArrayList;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.JourneySpecificDetail;
import static com.bt.rsqe.domain.QuoteOptionItemStatus.DRAFT;
import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;

public class JourneySpecificDetailExtenderTest {
    private PmrHelper pmrHelper = mock(PmrHelper.class);
    private OrderLineItemResource orderLineItemResource = mock(OrderLineItemResource.class);

    @Test
    public void shouldNotExtendWithJourneySpecificDetailsWhenNotRequested() {
        final CIFAsset cifAsset = mock(CIFAsset.class);

        final JourneySpecificDetailExtender journeySpecificDetailExtender = new JourneySpecificDetailExtender(pmrHelper, orderLineItemResource);
        journeySpecificDetailExtender.extend(new ArrayList<CIFAssetExtension>(), cifAsset);

        verify(cifAsset, times(0)).loadJourneySpecificDetail(any(JourneyBehaviourDTO.class));
    }

    @Test
    public void shouldLoadJourneySpecificDetailFromPmrWhenRequested() {
        final CIFAsset cifAsset = mock(CIFAsset.class);
        when(cifAsset.getAssetKey()).thenReturn(new AssetKey("AssetId", 5));
        when(cifAsset.getLineItemId()).thenReturn("LineItemId");
        when(cifAsset.getQuoteOptionItemDetail()).thenReturn(new CIFAssetQuoteOptionItemDetail(DRAFT, 1, false, true, "USD", "12", false,
                                                                                               JaxbDateTime.NIL, new ArrayList<PriceBookDTO>(),
                                                                                               LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false));

        final JourneyBehaviourDTO expectedDetail = new JourneyBehaviourDTO(true, true, false, false, true, false, false, true);

        final OrderLineItemDTO orderLineItemDTO = new OrderLineItemDTO(true, true, cifAsset.getAssetKey().getAssetId(), cifAsset.getLineItemId());
        when(orderLineItemResource.getOrderLineItemDTO(cifAsset.getLineItemId(), cifAsset.getAssetKey().getAssetId())).thenReturn(orderLineItemDTO);
        when(pmrHelper.getJourneyBehaviour(cifAsset.getAction(), true, true)).thenReturn(expectedDetail);

        final JourneySpecificDetailExtender journeySpecificDetailExtender = new JourneySpecificDetailExtender(pmrHelper, orderLineItemResource);
        journeySpecificDetailExtender.extend(newArrayList(JourneySpecificDetail), cifAsset);

        verify(cifAsset, times(1)).loadJourneySpecificDetail(expectedDetail);
    }

    @Test
    public void shouldReturnDefaultJourneySpecificDetailWhenNotIFC() {
        final CIFAsset cifAsset = mock(CIFAsset.class);
        when(cifAsset.getAssetKey()).thenReturn(new AssetKey("AssetId", 5));
        when(cifAsset.getLineItemId()).thenReturn("LineItemId");
        when(cifAsset.getQuoteOptionItemDetail()).thenReturn(new CIFAssetQuoteOptionItemDetail(DRAFT, 1, false, false, "USD", "12", false,
                                                                                               JaxbDateTime.NIL, new ArrayList<PriceBookDTO>(),
                                                                                               LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false));

        final JourneyBehaviourDTO expectedDetail = new JourneyBehaviourDTO(true, true, true, true, true, true, true, true);

        final JourneySpecificDetailExtender journeySpecificDetailExtender = new JourneySpecificDetailExtender(pmrHelper, orderLineItemResource);
        journeySpecificDetailExtender.extend(newArrayList(JourneySpecificDetail), cifAsset);

        verify(cifAsset, times(1)).loadJourneySpecificDetail(expectedDetail);
    }

    @Test
    public void shouldReturnJourneySpecificDetailWhenNoOrderLineItem() {
        final CIFAsset cifAsset = mock(CIFAsset.class);
        when(cifAsset.getAssetKey()).thenReturn(new AssetKey("AssetId", 5));
        when(cifAsset.getLineItemId()).thenReturn("LineItemId");
        when(cifAsset.getQuoteOptionItemDetail()).thenReturn(new CIFAssetQuoteOptionItemDetail(DRAFT, 1, false, true, "USD", "12", false,
                                                                                               JaxbDateTime.NIL, new ArrayList<PriceBookDTO>(),
                                                                                               LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false));

        final JourneyBehaviourDTO expectedDetail = new JourneyBehaviourDTO(true, true, false, false, true, false, false, true);

        when(orderLineItemResource.getOrderLineItemDTO(cifAsset.getLineItemId(), cifAsset.getAssetKey().getAssetId())).thenThrow(new ResourceNotFoundException());
        when(pmrHelper.getJourneyBehaviour(cifAsset.getAction(), false, false)).thenReturn(expectedDetail);

        final JourneySpecificDetailExtender journeySpecificDetailExtender = new JourneySpecificDetailExtender(pmrHelper, orderLineItemResource);
        journeySpecificDetailExtender.extend(newArrayList(JourneySpecificDetail), cifAsset);

        verify(cifAsset, times(1)).loadJourneySpecificDetail(expectedDetail);
    }
}
