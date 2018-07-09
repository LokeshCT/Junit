package com.bt.rsqe.customerinventory.service.filters;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ValidationImpactChangeResponse;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class ValidationImpactChangeResponseFilter implements UpdateResponseFilter {

    @Override
    public List<CIFAssetUpdateResponse> filter(List<CIFAssetUpdateResponse> cifAssetUpdateResponses) {
        List<CIFAssetUpdateResponse> filteredResponse = newArrayList();
        for (CIFAssetUpdateResponse response : cifAssetUpdateResponses) {
            if (!(response instanceof ValidationImpactChangeResponse && validationOfCancellingRequest(cifAssetUpdateResponses, (ValidationImpactChangeResponse) response))) {
                filteredResponse.add(response);

            }
        }
        return filteredResponse;
    }

    private boolean validationOfCancellingRequest(List<CIFAssetUpdateResponse> cifAssetUpdateResponses, final ValidationImpactChangeResponse impactChangeResponse) {
        return Iterables.tryFind(cifAssetUpdateResponses, new Predicate<CIFAssetUpdateResponse>() {
            @Override
            public boolean apply(CIFAssetUpdateResponse input) {
                if (input instanceof CancelRelationshipResponse) {
                    CancelRelationshipResponse response = (CancelRelationshipResponse) input;
                    return response.getRequest().getCancellingAssetId().equals(impactChangeResponse.getRequest().getAssetKey());
                }
                return false;
            }
        }).isPresent();
    }
}
