package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAttributeDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.customerinventory.service.client.domain.CIFPermission;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.SpecialBidReservedAttributesHelper;
import com.bt.rsqe.domain.DateFormats;
import com.bt.rsqe.domain.SpecialBidWellKnownAttribute;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.domain.product.AttributeOwner.Offering;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.*;

public class SpecialBidWellKnownAttributeProvider {
    private static final int CUSTOMER_REQUESTED_DATE_FUTURE_DEFAULT_DAYS = 15;
    private final String templateSelectionGuideUrl;
    private SpecialBidReservedAttributesHelper attributeHelper;

    public SpecialBidWellKnownAttributeProvider(String templateSelectionGuideUrl, SpecialBidReservedAttributesHelper attributeHelper) {
        this.templateSelectionGuideUrl = templateSelectionGuideUrl;
        this.attributeHelper = attributeHelper;
    }

    public List<CIFAssetCharacteristic> getSpecialBidCharacteristics(TpeRequestDTO tpeRequest, CIFAsset cifAsset) {
        List<CIFAssetCharacteristic> characteristics = new ArrayList<CIFAssetCharacteristic>();
        addWellKnownAttributes(characteristics, tpeRequest, cifAsset);
        return characteristics;
    }

    private void addWellKnownAttributes(List<CIFAssetCharacteristic> characteristics, TpeRequestDTO tpeRequest, CIFAsset cifAsset) {
        for (SpecialBidWellKnownAttribute attribute : SpecialBidWellKnownAttribute.values()) {
            final String value = getValue(attribute, tpeRequest, cifAsset);
            final CIFAssetCharacteristic characteristic = new CIFAssetCharacteristic(attribute.getAttributeName(),
                    value,
                    true);
            CIFAssetAttributeDetail attributeDetail = new CIFAssetAttributeDetail(false,
                    attribute.isReadOnly(),
                    Offering, attribute.getDataType(),
                    false, attribute.getAttributeName(),
                    !attribute.isOptional(), "");
            characteristic.loadAttributeDetail(attributeDetail);
            characteristic.loadAllowedValues(getAllowedValues(attribute));
            characteristics.add(characteristic);
        }
    }

    private List<CIFAssetCharacteristicValue> getAllowedValues(SpecialBidWellKnownAttribute attribute) {
        switch (attribute) {
            case CustomerValueCurrency:
                return newArrayList(new CIFAssetCharacteristicValue("EUR", "EUR", ""),
                        new CIFAssetCharacteristicValue("GBP", "GBP", ""),
                        new CIFAssetCharacteristicValue("USD", "USD", ""));
            case Tier:
                return newArrayList(new CIFAssetCharacteristicValue("Standard", "Standard", ""),
                        new CIFAssetCharacteristicValue("Non-Standard", "Non-Standard", ""),
                        new CIFAssetCharacteristicValue("Complex", "Complex", "", CIFPermission.DirectOnly));
            case ContractLength:
                return newArrayList(new CIFAssetCharacteristicValue("12", "12", ""),
                        new CIFAssetCharacteristicValue("24", "24", ""),
                        new CIFAssetCharacteristicValue("36", "36", ""),
                        new CIFAssetCharacteristicValue("60", "60", ""));
        }
        return null;
    }

    public String getValue(SpecialBidWellKnownAttribute attribute, TpeRequestDTO tpeRequest, CIFAsset cifAsset) {
        String value = null;
        switch (attribute) {
            case TemplateSelectionGuide:
                if(!isEmpty(templateSelectionGuideUrl)) {
                    value = templateSelectionGuideUrl;
                }
                break;
            case TemplateWiki:
                if(!isEmpty(tpeRequest.templateWiki)) {
                    value = tpeRequest.templateWiki;
                }
                break;
            case RequestName:
                final CIFAssetCharacteristic tpeTemplateName = attributeHelper.getTPETemplateName(cifAsset);
                if(tpeTemplateName!=null && !isEmpty(tpeTemplateName.getValue())) {
                    value = tpeTemplateName.getValue();
                }
                break;
            case WinChance:
                if(tpeRequest.winChance!=null) {
                    value = String.valueOf(tpeRequest.winChance);
                }
                break;
            case VolumeForFeature:
                if(tpeRequest.volumeForFeature!=null) {
                    value = String.valueOf(tpeRequest.volumeForFeature);
                }
                break;
            case BidManagerName:
                if(!isEmpty(tpeRequest.bidManagerName)) {
                    value = tpeRequest.bidManagerName;
                }
                break;
            case CustomerValue:
                if(tpeRequest.customerValue!=null) {
                    value = String.valueOf(tpeRequest.customerValue);
                }
                break;
            case CustomerValueCurrency:
                if(!isEmpty(tpeRequest.customerValueCurrency)) {
                    value = tpeRequest.customerValueCurrency;
                }
                break;
            case CustomerRequestedDate:
                if(tpeRequest.customerRequestedDate != null) {
                    value = new SimpleDateFormat(DateFormats.DATE_FORMAT).format(tpeRequest.customerRequestedDate);
                }
                break;
            case Tier:
                if(!isEmpty(tpeRequest.tier)) {
                    value = tpeRequest.tier;
                }
                break;
            case ContractLength:
                if(tpeRequest.contractLength!=null) {
                    value = String.valueOf(tpeRequest.contractLength);
                }
                break;
            case AdditionalInformation:
                if(!isEmpty(tpeRequest.additionalInformation)) {
                    value = tpeRequest.additionalInformation;
                }
                break;
            case DetailedResponse:
                if(!isEmpty(tpeRequest.detailedResponse)) {
                    value = tpeRequest.detailedResponse;
                }
                break;
            case BidState:
                if(!isEmpty(tpeRequest.bidState)) {
                    value = tpeRequest.bidState;
                }
                break;
        }
        if(value == null){
            return getDefaultValue(attribute, cifAsset);
        }
        return value;
    }

    private String getDefaultValue(SpecialBidWellKnownAttribute attribute, CIFAsset cifAsset) {
        switch (attribute){
            case CustomerValueCurrency:
                return cifAsset.getQuoteOptionItemDetail().getCurrency();
            case ContractLength:
                return cifAsset.getContractTerm();
            case WinChance:
                return SpecialBidWellKnownAttribute.WinChance.getDefaultValue();
            case VolumeForFeature:
                return SpecialBidWellKnownAttribute.VolumeForFeature.getDefaultValue();
            case BidManagerName:
                return SpecialBidWellKnownAttribute.BidManagerName.getDefaultValue();
            case CustomerRequestedDate:
                return new DateTime().plusDays(CUSTOMER_REQUESTED_DATE_FUTURE_DEFAULT_DAYS).toString(DateFormats.DATE_FORMAT);
            case Tier:
                return SpecialBidWellKnownAttribute.Tier.getDefaultValue();
            default:
                return EMPTY;
        }
    }
}