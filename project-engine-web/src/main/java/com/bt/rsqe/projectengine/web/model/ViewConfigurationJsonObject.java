package com.bt.rsqe.projectengine.web.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by 608876182 on 20/02/2016.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ViewConfigurationJsonObject {

    public String quoteOptionId;
    public String offerId;
    public String orderId;
    public String treeViewType;

    public ViewConfigurationJsonObject(String quoteOptionId, String offerId, String orderId, String treeViewType) {
        this.quoteOptionId = quoteOptionId;
        this.offerId = offerId;
        this.orderId = orderId;
        this.treeViewType = treeViewType;
    }

    public ViewConfigurationJsonObject() {
    }

    public String getQuoteOptionId() {
        return quoteOptionId;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getTreeViewType() {
        return treeViewType;
    }
}
