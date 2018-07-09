package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetStencilDetail;
import com.bt.rsqe.domain.StencilCode;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.StencilVersion;
import com.bt.rsqe.domain.bom.parameters.ProductName;

public class CIFAssetStencilDetailConverter {
    public static StencilId toStencilId(CIFAssetStencilDetail stencilDetail) {
        return StencilId.versioned(StencilCode.newInstance(stencilDetail.getStencilCode()),
                             StencilVersion.newInstance(stencilDetail.getStencilVersion()),
                             ProductName.newInstance(stencilDetail.getProductName()));
    }
}
