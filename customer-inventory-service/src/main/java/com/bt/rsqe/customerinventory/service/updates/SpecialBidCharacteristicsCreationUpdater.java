package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesCreationRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesCreationResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UserDetails;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UserDetailsManager;
import com.bt.rsqe.customerinventory.service.extenders.SpecialBidMandatoryAttributeProvider;
import com.bt.rsqe.customerinventory.service.extenders.SpecialBidWellKnownAttributeProvider;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.SpecialBidReservedAttributesHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.SpecialBidMandatoryAttribute;
import com.bt.rsqe.domain.SpecialBidWellKnownAttribute;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import com.bt.rsqe.tpe.client.TemplateTpeClient;
import com.bt.rsqe.tpe.multisite.Common_Mandatory;
import com.bt.rsqe.tpe.multisite.Row_Mandatory;
import com.bt.rsqe.tpe.multisite.TPE_TemplateDetails_Request;
import com.bt.rsqe.tpe.multisite.TPE_TemplateDetails_Response;
import com.bt.rsqe.tpe.multisite.Template_Mandatory_Attributes;
import com.bt.rsqe.utils.AssertObject;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.projectengine.TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.*;
import static org.apache.commons.lang.StringUtils.isEmpty;

public class SpecialBidCharacteristicsCreationUpdater implements CIFAssetUpdater<SpecialBidAttributesCreationRequest, SpecialBidAttributesCreationResponse> {

    private CIFAssetOrchestrator cifAssetOrchestrator;
    private TemplateTpeClient tpeClient;
    private ExternalAttributesHelper externalAttributesHelper;
    private SpecialBidReservedAttributesHelper attributesHelper;
    private SpecialBidTemplateAttributeMapper bidTemplateAttributeMapper;
    private final SpecialBidWellKnownAttributeMapper wellKnownAttributeMapper;
    private final SpecialBidWellKnownAttributeProvider wellKnownAttributeProvider;
    private final SpecialBidMandatoryAttributeProvider mandatoryAttributeProvider;

    public SpecialBidCharacteristicsCreationUpdater(CIFAssetOrchestrator cifAssetOrchestrator,
                                                    TemplateTpeClient tpeClient,
                                                    ExternalAttributesHelper externalAttributesHelper,
                                                    SpecialBidReservedAttributesHelper attributesHelper,
                                                    SpecialBidTemplateAttributeMapper templateAttributeMapper,
                                                    SpecialBidWellKnownAttributeMapper wellKnownAttributeMapper,
                                                    SpecialBidWellKnownAttributeProvider wellKnownAttributeProvider,
                                                    SpecialBidMandatoryAttributeProvider mandatoryAttributeProvider) {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.tpeClient = tpeClient;
        this.externalAttributesHelper = externalAttributesHelper;
        this.attributesHelper = attributesHelper;
        this.bidTemplateAttributeMapper = templateAttributeMapper;
        this.wellKnownAttributeMapper = wellKnownAttributeMapper;
        this.wellKnownAttributeProvider = wellKnownAttributeProvider;
        this.mandatoryAttributeProvider = mandatoryAttributeProvider;
    }

    @Override
    public SpecialBidAttributesCreationResponse performUpdate(SpecialBidAttributesCreationRequest request) {

        CIFAsset cifAsset = cifAssetOrchestrator.getAsset(new CIFAssetKey(request.getAssetKey(), newArrayList(ProductOfferingDetail, QuoteOptionItemDetail, SiteDetail, AttributeDetails)));
        if (isSpecialBid(cifAsset)) {
            CIFAssetCharacteristic tpeTemplateName = attributesHelper.getTPETemplateName(cifAsset);
            if (isNotNull(tpeTemplateName) && isNotEmpty(tpeTemplateName.getValue())) {
                TpeRequestDTO tpeRequest = externalAttributesHelper.getAttributes(cifAsset);
                TPE_TemplateDetails_Response templateDetails = getTemplateDetails(cifAsset);
                mapWellKnownAttributeValues(cifAsset, tpeRequest);
                mapCommonAttributes(tpeRequest, cifAsset, templateDetails);
                mapPrimaryAttributes(tpeRequest, cifAsset, templateDetails);
                mapUserSpecificDetails(tpeRequest);
                externalAttributesHelper.saveAttributes(cifAsset, tpeRequest);
            }
        }

        return new SpecialBidAttributesCreationResponse(request);
    }

    private void mapUserSpecificDetails(TpeRequestDTO tpeRequest) {
        UserDetails userDetails = UserDetailsManager.get();
        if(userDetails != null && userDetails.isIndirectUser()) {
            tpeRequest.tier = "Non-Standard";
        }
    }

    private void mapWellKnownAttributeValues(CIFAsset cifAsset, TpeRequestDTO tpeRequest) {
        for (SpecialBidWellKnownAttribute wellKnownAttribute : SpecialBidWellKnownAttribute.values()) {
            String characteristicValue = getAssetCharacteristicValue(cifAsset, wellKnownAttribute.getAttributeName());
            String calculatedValue = isEmpty(characteristicValue) ? wellKnownAttributeProvider.getValue(wellKnownAttribute, tpeRequest, cifAsset) : characteristicValue;
            wellKnownAttributeMapper.map(tpeRequest, wellKnownAttribute.getAttributeName(), calculatedValue);
        }
    }

    private void mapPrimaryAttributes(TpeRequestDTO tpeRequest, CIFAsset cifAsset, TPE_TemplateDetails_Response templateDetails) {
        if (templateDetails.getPrimary() != null) {
            for (Template_Mandatory_Attributes mandatoryAttributes : templateDetails.getPrimary()) {
                String value = getAssetCharacteristicValueByDisplayName(cifAsset, mandatoryAttributes.getAttribute_Name());

                if (isEmpty(value) && SpecialBidMandatoryAttribute.contains(mandatoryAttributes.getAttribute_Name())) {
                    SpecialBidMandatoryAttribute specialBidMandatoryAttribute = SpecialBidMandatoryAttribute.get(mandatoryAttributes.getAttribute_Name());
                    value = mandatoryAttributeProvider.getValue(specialBidMandatoryAttribute, cifAsset);
                }

                if(AssertObject.isEmpty(value) && isNotEmpty(mandatoryAttributes.getDefault_value())) {
                    value = mandatoryAttributes.getDefault_value();
                }

                bidTemplateAttributeMapper.upsert(tpeRequest, PRIMARY, mandatoryAttributes.getAttribute_Name(), value);
            }
        }
    }

    private void mapCommonAttributes(TpeRequestDTO tpeRequest, CIFAsset cifAsset, TPE_TemplateDetails_Response templateDetails) {
        if (templateDetails.getCommon_Info() != null) {
            for (Common_Mandatory commonMandatory : templateDetails.getCommon_Info()) {
                for (Row_Mandatory row_mandatory : commonMandatory.getRow()) {
                    for (Template_Mandatory_Attributes mandatoryAttribute : row_mandatory.getMandate_Info()) {
                        String value = getAssetCharacteristicValueByDisplayName(cifAsset, mandatoryAttribute.getAttribute_Name());

                        if (isEmpty(value) && SpecialBidMandatoryAttribute.contains(mandatoryAttribute.getAttribute_Name())) {
                            SpecialBidMandatoryAttribute specialBidMandatoryAttribute = SpecialBidMandatoryAttribute.get(mandatoryAttribute.getAttribute_Name());
                            value = mandatoryAttributeProvider.getValue(specialBidMandatoryAttribute, cifAsset);
                        }

                        if(AssertObject.isEmpty(value) && isNotEmpty(mandatoryAttribute.getDefault_value())) {
                            value = mandatoryAttribute.getDefault_value();
                        }

                        bidTemplateAttributeMapper.upsert(tpeRequest, COMMON, mandatoryAttribute.getAttribute_Name(), value);
                    }
                }
            }
        }
    }

    private TPE_TemplateDetails_Response getTemplateDetails(CIFAsset cifAsset) {
        TPE_TemplateDetails_Request templateRequest = new TPE_TemplateDetails_Request(cifAsset.getOfferingDetail().getProductGroupName(),
                attributesHelper.getConfigType(cifAsset).getValue(),
                attributesHelper.getTPETemplateName(cifAsset).getValue());
        return tpeClient.SQE_TPE_TemplateDetails(templateRequest);
    }


    private boolean isSpecialBid(CIFAsset cifAsset) {
        CIFAssetCharacteristic specialBidCharacteristic = attributesHelper.getSpecialBidCharacteristic(cifAsset);
        return specialBidCharacteristic != null &&
                ("Y".equals(specialBidCharacteristic.getValue()) || "Yes".equals(specialBidCharacteristic.getValue()));
    }


    private String getAssetCharacteristicValue(CIFAsset cifAsset, String attributeName) {
        CIFAssetCharacteristic characteristic = cifAsset.getCharacteristic(attributeName);
        if (isNotNull(characteristic) && isNotEmpty(characteristic.getValue())) {
            return characteristic.getValue();
        }
        return EMPTY;

    }

    private String getAssetCharacteristicValueByDisplayName(CIFAsset cifAsset, String attributeName) {
        CIFAssetCharacteristic characteristic = cifAsset.getCharacteristicByDisplayName(attributeName);
        if (isNotNull(characteristic) && isNotEmpty(characteristic.getValue())) {
            return characteristic.getValue();
        }
        return EMPTY;

    }
}
