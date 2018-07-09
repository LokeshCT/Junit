package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.domain.product.constraints.AttributeValue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.*;

public class CIFAssetCharacteristicValueConverter {
    public static List<CIFAssetCharacteristicValue> fromAttributeValues(List<AttributeValue> attributeValues) {
        List<CIFAssetCharacteristicValue> allowedValues = new ArrayList<CIFAssetCharacteristicValue>();
        for (AttributeValue attributeValue : attributeValues) {
            allowedValues.add(new CIFAssetCharacteristicValue(attributeValue.getAsStringValue(),
                                                              attributeValue.getCaption(),
                                                              attributeValue.getGroup()));
        }
        return allowedValues;
    }

    public static List<CIFAssetCharacteristicValue> fromStrings(List<String> allowedValueStrings) {
        Set<CIFAssetCharacteristicValue> allowedValues = new LinkedHashSet<CIFAssetCharacteristicValue>();
        for (String allowedValue : allowedValueStrings) {
            allowedValues.add(new CIFAssetCharacteristicValue(allowedValue));
        }
        return newArrayList(allowedValues);
    }

    public static List<String> toStrings(List<CIFAssetCharacteristicValue> allowedValues) {
        List<String> stringAllowedValues = new ArrayList<String>();
        if(allowedValues!=null) {
            for (CIFAssetCharacteristicValue allowedValue : allowedValues) {
                stringAllowedValues.add(allowedValue.getValue());
            }
        }
        return stringAllowedValues;
    }
}
