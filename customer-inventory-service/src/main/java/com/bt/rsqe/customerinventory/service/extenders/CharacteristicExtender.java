package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAttributeDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.ProductOffering;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.google.common.collect.Lists.*;

public class CharacteristicExtender {
    public static final List<String> STENCIL_CHARACTERISTIC_NAMES = newArrayList(ProductOffering.STENCIL_RESERVED_NAME, ProductOffering.PRODUCT_IDENTIFIER_RESERVED_NAME);
    private final PmrHelper pmrHelper;

    public CharacteristicExtender(PmrHelper pmrHelper) {
        this.pmrHelper = pmrHelper;
    }

    public void extend(List<CIFAssetExtension> cifAssetExtensions, CIFAsset cifAsset, ProductOffering productOffering, Iterable<CIFAssetCharacteristic> characteristics) {
        for (CIFAssetCharacteristic characteristic : characteristics) {
            extendCharacteristic(cifAssetExtensions, cifAsset, productOffering, characteristic);
        }

        if (Description.isInList(cifAssetExtensions)) {
            loadDescriptions(cifAsset);
        }
    }

    private void loadDescriptions(CIFAsset cifAsset) {
        StringBuilder description = new StringBuilder();
        StringBuilder shortDescription = new StringBuilder();
        List<String> relationShipSummary = newArrayList();
        for (CIFAssetCharacteristic cifAssetCharacteristic : cifAsset.getCharacteristics()) {
            final CIFAssetAttributeDetail attributeDetail = cifAssetCharacteristic.getAttributeDetail();
            if (attributeDetail.isVisibleInSummary() && cifAssetCharacteristic.getValue() != null) {
                description.append(attributeDetail.getDisplayName());
                description.append(":");
                description.append(cifAssetCharacteristic.getValue());
                description.append(" ");

                shortDescription.append(cifAssetCharacteristic.getValue());
                shortDescription.append(" ");

                relationShipSummary.add(cifAssetCharacteristic.getValue());
            }
        }
        cifAsset.loadDescription(description.toString(), shortDescription.toString(), Joiner.on(",").join(relationShipSummary));
    }

    private void extendCharacteristic(List<CIFAssetExtension> cifAssetExtensions, CIFAsset cifAsset, ProductOffering productOffering, CIFAssetCharacteristic characteristic) {
        if (needsAllowedValues(cifAssetExtensions, characteristic)) {
            extendAllowedValues(cifAsset, productOffering, characteristic);
        }

        if (AttributeDetails.isInList(cifAssetExtensions)) {
            extendAttributeDetails(productOffering, characteristic, cifAsset);
        }

        if(CharacteristicValue.isInList(cifAssetExtensions))  {
            if(characteristic.getAllowedValues() != null && characteristic.getAllowedValues().size() == 1 )  {
                characteristic.setValue(characteristic.getAllowedValues().get(0).getValue());
            }
        }
    }

    private void extendAttributeDetails(ProductOffering productOffering, CIFAssetCharacteristic characteristic, CIFAsset cifAsset) {
        final Attribute attribute = productOffering.getAttribute(new AttributeName(characteristic.getName()));
        final boolean isReadOnly = attribute.isReadOnly() || cifAsset.getQuoteOptionItemDetail().getStatus().isLocked();
        final Object defaultValue = attribute.getDefaultValue().getValue()==null ? "" : attribute.getDefaultValue().getValue();
        characteristic.loadAttributeDetail(new CIFAssetAttributeDetail(attribute.isStencil(),
                                                                       isReadOnly,
                                                                       attribute.getAttributeOwner(),
                                                                       attribute.dataType(),
                                                                       attribute.isVisibleInSummary(),
                                                                       attribute.getDisplayName(),
                                                                       !attribute.isOptional(),
                                                                       defaultValue));
    }

    private void extendAllowedValues(CIFAsset cifAsset, ProductOffering productOffering, CIFAssetCharacteristic characteristic) {
        if (!characteristic.hasExtension(CharacteristicAllowedValues)) {
            characteristic.loadAllowedValues(null);
            Attribute attribute = productOffering.getAttribute(new AttributeName(characteristic.getName()));
            Optional<List<CIFAssetCharacteristicValue>> allowedValues = pmrHelper.getAllowedValues(cifAsset, attribute);
            if (allowedValues.isPresent()) {
                characteristic.loadAllowedValues(allowedValues.get());
            }
        }
    }

    private boolean needsAllowedValues(List<CIFAssetExtension> cifAssetExtensions,
                                       CIFAssetCharacteristic characteristic) {
        return CharacteristicAllowedValues.isInList(cifAssetExtensions) ||
               (CIFAssetExtension.StencilDetails.isInList(cifAssetExtensions) &&
                STENCIL_CHARACTERISTIC_NAMES.contains(characteristic.getName()));
    }
}
