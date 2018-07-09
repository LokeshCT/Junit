package com.bt.rsqe.customerinventory.service.extenders.reservedattributes;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetStencilDetail;
import com.bt.rsqe.domain.QrefIdFormat;
import com.bt.rsqe.domain.StencilCode;
import com.bt.rsqe.domain.StencilId;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.CharacteristicAllowedValues;
import static com.bt.rsqe.domain.product.ProductOffering.*;

public class StencilReservedAttributesHelper extends ReservedAttributesHelper{
    public CIFAssetStencilDetail getStencilDetail(CIFAsset cifAsset) {
        CIFAssetCharacteristic stencilCharacteristic = getStencilCharacteristic(cifAsset);
        if(stencilCharacteristic!=null){
            CIFAssetCharacteristic stencilVersionCharacteristic = getStencilVersionCharacteristic(cifAsset);

            final String stencilCode = stencilCharacteristic.getValue();
            final List<CIFAssetStencilDetail> allowedStencils = getAllowedStencils(stencilCharacteristic);
            final String stencilName = getStencilNameFromAllowedStencils(allowedStencils, stencilCode);
            final String stencilVersion = stencilVersionCharacteristic==null ? null : stencilVersionCharacteristic.getValue();

            return new CIFAssetStencilDetail(stencilCode, stencilVersion, stencilName, allowedStencils);
        }

        return null;
    }

    private String getStencilNameFromAllowedStencils(List<CIFAssetStencilDetail> allowedStencils, String stencilCode) {
        StencilId stencilId = StencilId.latestVersionFor(StencilCode.newInstance(stencilCode));
        if(stencilId.isAccessStencil()){
            return QrefIdFormat.convertFromStencil(stencilCode);
        }
        if(allowedStencils!=null) {
            for (CIFAssetStencilDetail allowedStencil : allowedStencils) {
                if (allowedStencil.getStencilCode().equals(stencilCode)) {
                    return allowedStencil.getProductName();
                }
            }
        }
        return "";
    }

    private List<CIFAssetStencilDetail> getAllowedStencils(CIFAssetCharacteristic stencilCharacteristic) {
        if(stencilCharacteristic.hasExtension(CharacteristicAllowedValues) && stencilCharacteristic.getAllowedValues()!=null) {
            List<CIFAssetStencilDetail> allowedStencils = new ArrayList<CIFAssetStencilDetail>();
            for (CIFAssetCharacteristicValue cifAssetCharacteristicValue : stencilCharacteristic.getAllowedValues()) {
                allowedStencils.add(new CIFAssetStencilDetail(cifAssetCharacteristicValue.getValue(), null,
                                                              cifAssetCharacteristicValue.getCaption(), null));
            }
            return allowedStencils;
        }
        return null;
    }

    public CIFAssetCharacteristic getStencilVersionCharacteristic(CIFAsset cifAsset) {
        return getFirstCharacteristicByNames(cifAsset, STENCIL_VERSION_RESERVED_NAME);
    }

    public CIFAssetCharacteristic getStencilCharacteristic(CIFAsset cifAsset) {
        return getFirstCharacteristicByNames(cifAsset, STENCIL_RESERVED_NAME, PRODUCT_IDENTIFIER_RESERVED_NAME);
    }
}
