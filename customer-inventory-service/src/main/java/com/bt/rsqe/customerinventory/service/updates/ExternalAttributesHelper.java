package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.TpeRequestDTO;

public class ExternalAttributesHelper {

    private ProjectResource projectResource;

    public ExternalAttributesHelper(ProjectResource projectResource) {
        this.projectResource = projectResource;
    }

    public TpeRequestDTO getAttributes(CIFAsset cifAsset) {
        return projectResource.quoteOptionResource(cifAsset.getProjectId())
                .quoteOptionItemResource(cifAsset.getQuoteOptionId())
                .getTpeRequest(cifAsset.getAssetKey().getAssetId(), cifAsset.getAssetKey().getAssetVersion());
    }

    public void saveAttributes(CIFAsset cifAsset, TpeRequestDTO tpeRequest) {
        projectResource.quoteOptionResource(cifAsset.getProjectId())
                .quoteOptionItemResource(cifAsset.getQuoteOptionId())
                .putTpeRequest(tpeRequest);
    }
}
