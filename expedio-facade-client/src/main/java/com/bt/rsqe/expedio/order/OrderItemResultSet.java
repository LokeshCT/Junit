package com.bt.rsqe.expedio.order;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 8/6/14
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class OrderItemResultSet {

    @XmlElement
    List<OrderLineItemDTO> orderItems;

    public List<OrderLineItemDTO> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderLineItemDTO> orderItems) {
        this.orderItems = orderItems;
    }
}
