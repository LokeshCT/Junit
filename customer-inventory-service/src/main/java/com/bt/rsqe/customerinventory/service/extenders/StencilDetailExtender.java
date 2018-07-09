package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.StencilReservedAttributesHelper;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.StencilDetails;

public class StencilDetailExtender {
    private final StencilReservedAttributesHelper stencilReservedAttributesHelper;

    public StencilDetailExtender(StencilReservedAttributesHelper stencilReservedAttributesHelper) {
        this.stencilReservedAttributesHelper = stencilReservedAttributesHelper;
    }

    public void extend(List<CIFAssetExtension> cifAssetExtensions, CIFAsset cifAsset) {
        if(StencilDetails.isInList(cifAssetExtensions)) {
            cifAsset.loadStencilDetail(stencilReservedAttributesHelper.getStencilDetail(cifAsset));
        }
    }
}
