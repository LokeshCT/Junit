package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;

public enum FixedAction {
    Delete, Add;

    public static FixedAction fromAssetStatus(ProductInstanceState cifAssetStatus) {
        if(ProductInstanceState.CEASED.equals(cifAssetStatus)){
            return Delete;
        }
        return Add;
    }
}
