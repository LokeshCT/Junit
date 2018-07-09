package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CardinalityImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CardinalityImpactChangeResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RuleFilterImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RuleFilterImpactChangeResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ValidationImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ValidationImpactChangeResponse;

public class UpdationIgnoreRequestUpdater implements CIFAssetUpdater<CIFAssetUpdateRequest, CIFAssetUpdateResponse> {
    @Override
    public CIFAssetUpdateResponse performUpdate(CIFAssetUpdateRequest update) {
        if (update instanceof ValidationImpactChangeRequest) {
            return new ValidationImpactChangeResponse((ValidationImpactChangeRequest) update);
        } else if (update instanceof CardinalityImpactChangeRequest) {
            return new CardinalityImpactChangeResponse((CardinalityImpactChangeRequest) update);
        } else if (update instanceof RuleFilterImpactChangeRequest) {
            return new RuleFilterImpactChangeResponse((RuleFilterImpactChangeRequest) update);
        }
        throw new RuntimeException(String.format("Cannot build for request %s, as its not currently supported",update.getClass().getCanonicalName()));
    }
}
