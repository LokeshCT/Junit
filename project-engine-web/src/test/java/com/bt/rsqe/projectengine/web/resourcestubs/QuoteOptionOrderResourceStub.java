package com.bt.rsqe.projectengine.web.resourcestubs;

import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.OrderResource;
import com.bt.rsqe.projectengine.RfoValidDTO;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QuoteOptionOrderResourceStub extends OrderResource {

    private Map<String, OrderDTO> orders = new HashMap<String, OrderDTO>();
    private Map<String, RfoValidDTO> rfoValidDTOMap = new HashMap<String, RfoValidDTO>();

    protected QuoteOptionOrderResourceStub() {
        super(URI.create(""));
    }

    public QuoteOptionOrderResourceStub with(OrderDTO order) {
        orders.put(order.id, order);
        return this;
    }

     public QuoteOptionOrderResourceStub with(String orderId, RfoValidDTO rfoValidDTO) {
        rfoValidDTOMap.put(orderId, rfoValidDTO);
        return this;
    }

    @Override
    public List<OrderDTO> getAll() {
        return new ArrayList<OrderDTO>(orders.values());
    }

    @Override
    public OrderDTO post(OrderDTO order) {
        order.id = UUID.randomUUID().toString();
        orders.put(order.id, order);
        return order;
    }

    public RfoValidDTO getIsRfoValid(String orderId) {
        return rfoValidDTOMap.get(orderId);
    }

    public List<OrderDTO> storedOrderList() {
        return new ArrayList<OrderDTO>(orders.values());
    }
}
