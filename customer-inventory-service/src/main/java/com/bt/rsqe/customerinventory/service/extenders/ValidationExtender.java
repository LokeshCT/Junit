package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.ValidationOrchestrator;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.RuleValidation;

public class ValidationExtender {
    private final ValidationOrchestrator validationOrchestrator;

    public ValidationExtender(ValidationOrchestrator validationOrchestrator) {
        this.validationOrchestrator = validationOrchestrator;
    }

    public void extend(List<CIFAssetExtension> cifAssetExtensions, CIFAsset baseAsset) {
        if(RuleValidation.isInList(cifAssetExtensions)){
            baseAsset.loadValidationNotifications(validationOrchestrator.validate(baseAsset));
        }
    }
}
