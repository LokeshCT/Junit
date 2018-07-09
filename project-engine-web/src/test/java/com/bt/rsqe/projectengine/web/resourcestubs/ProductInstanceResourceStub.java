package com.bt.rsqe.projectengine.web.resourcestubs;

import com.bt.rsqe.domain.project.ProductInstance;

import java.util.Map;

import static com.google.common.collect.Maps.*;

public class ProductInstanceResourceStub  {

    private Map<String, ProductInstance> byId = newHashMap();
    private Map<String, ProductInstance> byLineItemId = newHashMap();

    public ProductInstanceResourceStub() {
    }

    public void with(ProductInstance productInstance) {
        byId.put(productInstance.getProductInstanceId().getValue(), productInstance);
        byLineItemId.put(productInstance.getLineItemId(), productInstance);
    }

    public Map<String, ProductInstance> byLineItemIdMap() {
        return byLineItemId;
    }



}
