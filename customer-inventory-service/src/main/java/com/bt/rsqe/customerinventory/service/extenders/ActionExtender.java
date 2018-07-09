package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.comparisons.ActionCalculator;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.Action;

public class ActionExtender {
    private ActionCalculator actionCalculator;

    public ActionExtender(ActionCalculator actionCalculator) {
        this.actionCalculator = actionCalculator;
    }

    public void extend(List<CIFAssetExtension> cifAssetExtensions, CIFAsset cifAsset) {
        if(Action.isInList(cifAssetExtensions)) {
            final CIFAsset asIsAsset = cifAsset.getAsIsAsset();
            cifAsset.loadAction(actionCalculator.getAction(cifAsset, asIsAsset));
        }
    }
}
