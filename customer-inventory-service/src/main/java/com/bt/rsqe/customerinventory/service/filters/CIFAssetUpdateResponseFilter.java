package com.bt.rsqe.customerinventory.service.filters;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateResponse;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.*;

public class CIFAssetUpdateResponseFilter {

    List<UpdateResponseFilter> responseFilters;

    public CIFAssetUpdateResponseFilter() {
        responseFilters = new ArrayList<UpdateResponseFilter>() {{
            add(new ValidationImpactChangeResponseFilter());
        }};
    }

    public List<CIFAssetUpdateResponse> filter(List<CIFAssetUpdateResponse> cifAssetUpdateResponses) {
        List<CIFAssetUpdateResponse> filteredResponses = newArrayList();
        for (UpdateResponseFilter responseFilter : responseFilters) {
            filteredResponses.addAll(responseFilter.filter(cifAssetUpdateResponses));
        }
        return filteredResponses;
    }

}
