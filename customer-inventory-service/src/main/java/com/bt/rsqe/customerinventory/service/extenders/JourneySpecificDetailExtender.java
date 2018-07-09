package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.pmr.dto.JourneyBehaviourDTO;
import com.bt.rsqe.projectengine.OrderLineItemDTO;
import com.bt.rsqe.projectengine.OrderLineItemResource;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.JourneySpecificDetail;

public class JourneySpecificDetailExtender {
    private PmrHelper pmrHelper;
    private OrderLineItemResource orderLineItemResource;

    public JourneySpecificDetailExtender(PmrHelper pmrHelper, OrderLineItemResource orderLineItemResource) {
        this.pmrHelper = pmrHelper;
        this.orderLineItemResource = orderLineItemResource;
    }

    public void extend(List<CIFAssetExtension> extensionsList, CIFAsset cifAsset) {
        if(JourneySpecificDetail.isInList(extensionsList)){
            JourneyBehaviourDTO journeyBehaviour;
            if(cifAsset.getQuoteOptionItemDetail().isIfc()) {
                OrderLineItemDTO orderLineItemDTO;
                try {
                    orderLineItemDTO = orderLineItemResource.getOrderLineItemDTO(cifAsset.getLineItemId(), cifAsset.getAssetKey().getAssetId());
                }catch(ResourceNotFoundException notFoundException){
                    orderLineItemDTO = OrderLineItemDTO.NIL;
                }
                journeyBehaviour = pmrHelper.getJourneyBehaviour(cifAsset.getAction(), orderLineItemDTO.isPona(), orderLineItemDTO.isPonc());
            }else{
                journeyBehaviour = JourneyBehaviourDTO.DEFAULT;
            }
            cifAsset.loadJourneySpecificDetail(journeyBehaviour);
        }
    }
}
