package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAttributeDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;

import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;

public class CharacteristicValueCalculator {

    /**
     * This method is being used to calculate value to be set in asset characteristic using its allowed values.
     * @param characteristic CIFAssetCharacteristic
     * @return calculatedCharacteristicValue String
     */
    public static String calculate(CIFAssetCharacteristic characteristic) {
        final List<CIFAssetCharacteristicValue> allowedValues = characteristic.getAllowedValues();
        final CIFAssetAttributeDetail attributeDetail = characteristic.getAttributeDetail();
        final String defaultValue = isNotNull(attributeDetail.getDefaultValue()) ? attributeDetail.getDefaultValue().toString() : null;

        if (allowedValues == null) {
            return defaultValue;
        } else {
            if (allowedValues.size() == 1) {
                return allowedValues.get(0).getValue();
            } else {
                return containsValue(allowedValues, defaultValue) ? defaultValue : "";
            }
        }
    }

    private static boolean containsValue(List<CIFAssetCharacteristicValue> allowedValues, String value) {
        for (CIFAssetCharacteristicValue allowedValue : allowedValues) {
            if (value.equals(allowedValue.getValue())) {
                return true;
            }
        }
        return false;
    }
}
