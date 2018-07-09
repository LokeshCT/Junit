package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;

import java.util.List;

public class BillingId {
    public static String fromOrderDTOs(String lineItemId, List<OrderDTO> orders) {
        for(OrderDTO order : orders) {
            for (QuoteOptionItemDTO quoteOptionItem : order.getOrderItems()) {
                if (lineItemId.equals(quoteOptionItem.getId())) {
                    return quoteOptionItem.getBillingId();
                }
            }
        }

        return "";
    }
}
