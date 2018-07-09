package com.bt.rsqe.expedio.order;

public class OrderChangeRequestDTO {
    private String orderId;
    private String orderLineId;
    private String suComments;
    private String suReasonForReject;

    public OrderChangeRequestDTO(String orderId, String orderLineId, String suComments, String suReasonForReject) {
        this.orderId = orderId;
        this.orderLineId = orderLineId;
        this.suComments = suComments;
        this.suReasonForReject = suReasonForReject;
    }

    ///CLOVER:OFF
    public String getOrderId() {
        return orderId;
    }

    public String getOrderLineId() {
        return orderLineId;
    }

    public String getSuComments() {
        return suComments;
    }

    public String getSuReasonForReject() {
        return suReasonForReject;
    }
    ///CLOVER:ON
}
