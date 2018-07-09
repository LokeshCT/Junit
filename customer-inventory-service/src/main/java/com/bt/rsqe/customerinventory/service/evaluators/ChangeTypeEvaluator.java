package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.productinstancemerge.ChangeType;

import static com.bt.rsqe.customerinventory.parameter.ProductInstanceState.CEASED;
import static com.bt.rsqe.productinstancemerge.ChangeType.*;

public class ChangeTypeEvaluator {
    public static ChangeType fromSourceAndState(boolean hasSourceAsset, ProductInstanceState state) {
        if(!hasSourceAsset) {
            return ADD;
        }
        if(CEASED==state) {
            return DELETE;
        }
        return UPDATE;
    }
}
