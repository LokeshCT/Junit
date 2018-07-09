package com.bt.rsqe.customerinventory.service.extenders.reservedattributes;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import static com.bt.rsqe.domain.product.ProductOffering.*;

public class SpecialBidReservedAttributesHelper extends ReservedAttributesHelper {
    public CIFAssetCharacteristic getSpecialBidCharacteristic(CIFAsset cifAsset) {
        return getFirstCharacteristicByNames(cifAsset, SPECIAL_BID_ATTRIBUTE_INDICATOR);
    }

    public CIFAssetCharacteristic getTPETemplateName(CIFAsset cifAsset) {
        return getFirstCharacteristicByNames(cifAsset, SPECIAL_BID_TEMPLATE_RESERVED_NAME);
    }

    public CIFAssetCharacteristic getConfigType(CIFAsset cifAsset) {
        return getFirstCharacteristicByNames(cifAsset, CONFIGURATION_TYPE_RESERVED_NAME);
    }
}
