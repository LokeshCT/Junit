package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.web.model.modelfactory.LineItemModelFactory;

import java.util.List;

public class OrderModel {
    private String projectId;
    private String quoteOptionId;
    private String customerId;
    private String contractId;

    public OrderDTO getOrderDTO() {
        return orderDTO;
    }

    public String getOrderName()
    {
        return orderDTO.name;
    }

    private OrderDTO orderDTO;
    private LineItemModelFactory lineItemModelFactory;

    public OrderModel(String projectId, String quoteOptionId, String customerId, String contractId, OrderDTO orderDTO, LineItemModelFactory lineItemModelFactory) {
        this.projectId = projectId;
        this.quoteOptionId = quoteOptionId;
        this.customerId = customerId;
        this.contractId = contractId;

        this.orderDTO = orderDTO;
        this.lineItemModelFactory = lineItemModelFactory;
    }

    public List<LineItemModel> getLineItems() {
        return lineItemModelFactory.create(projectId, quoteOptionId, customerId, contractId, orderDTO.getVisibleOrderItems());
    }
}
