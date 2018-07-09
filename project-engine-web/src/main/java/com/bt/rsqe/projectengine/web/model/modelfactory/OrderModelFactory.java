package com.bt.rsqe.projectengine.web.model.modelfactory;

import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.web.model.OrderModel;

public class OrderModelFactory {
    private LineItemModelFactory lineItemModelFactory;

    public OrderModelFactory(LineItemModelFactory lineItemModelFactory) {
        this.lineItemModelFactory = lineItemModelFactory;
    }

    public OrderModel create(String customerId, String contractId, String projectId, String quoteOptionId, OrderDTO orderDTO) {
        return new OrderModel(projectId, quoteOptionId, customerId, contractId, orderDTO, lineItemModelFactory);
    }
}
