package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAttributeDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.SpecialBidReservedAttributesHelper;
import com.bt.rsqe.domain.product.AttributeDataType;
import com.bt.rsqe.domain.product.AttributeOwner;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import com.bt.rsqe.tpe.client.TemplateTpeClient;
import com.bt.rsqe.tpe.multisite.Common_Mandatory;
import com.bt.rsqe.tpe.multisite.Row_Mandatory;
import com.bt.rsqe.tpe.multisite.TPE_TemplateDetails_Request;
import com.bt.rsqe.tpe.multisite.TPE_TemplateDetails_Response;
import com.bt.rsqe.tpe.multisite.Template_Mandatory_Attributes;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.projectengine.TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static org.apache.commons.lang.StringUtils.*;

public class SpecialBidTemplateAttributeProvider {
    private final TemplateTpeClient templateTpeClient;
    private final SpecialBidReservedAttributesHelper attributesHelper;

    public SpecialBidTemplateAttributeProvider(TemplateTpeClient templateTpeClient, SpecialBidReservedAttributesHelper attributesHelper) {
        this.templateTpeClient = templateTpeClient;
        this.attributesHelper = attributesHelper;
    }

    public List<CIFAssetCharacteristic> getSpecialBidCharacteristics(CIFAsset cifAsset, TpeRequestDTO tpeRequest) {
        if (isNull(attributesHelper.getTPETemplateName(cifAsset))) {
            return new ArrayList<CIFAssetCharacteristic>();
        }
        if(StringUtils.isEmpty(attributesHelper.getTPETemplateName(cifAsset).getValue())) {
            return new ArrayList<CIFAssetCharacteristic>();
        }
        final TPE_TemplateDetails_Response templateDetails = getTemplateDetails(cifAsset);

        List<CIFAssetCharacteristic> characteristics = new ArrayList<CIFAssetCharacteristic>();
        if(templateDetails.getCommon_Info()!=null) {
            for (Common_Mandatory commonMandatory : templateDetails.getCommon_Info()) {
                for (Row_Mandatory row_mandatory : commonMandatory.getRow()) {
                    for (Template_Mandatory_Attributes mandatoryAttribute : row_mandatory.getMandate_Info()) {
                        characteristics.add(toCIFAssetCharacteristic(mandatoryAttribute, tpeRequest, COMMON));
                    }
                }
            }
        }

        if (templateDetails.getPrimary() != null) {
            for (Template_Mandatory_Attributes mandatoryAttributes : templateDetails.getPrimary()) {
                characteristics.add(toCIFAssetCharacteristic(mandatoryAttributes, tpeRequest, PRIMARY));
            }
        }

        return characteristics;
    }

    private CIFAssetCharacteristic toCIFAssetCharacteristic(Template_Mandatory_Attributes mandatoryAttribute, TpeRequestDTO tpeRequest,
                                                            TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier attributeClassifier) {
        final CIFAssetCharacteristic cifAssetCharacteristic = new CIFAssetCharacteristic(mandatoryAttribute.getAttribute_Name(),
                                                                                         getValue(mandatoryAttribute, tpeRequest,
                                                                                                  attributeClassifier), true);
        cifAssetCharacteristic.loadAllowedValues(null);
        cifAssetCharacteristic.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering,
                                                                               AttributeDataType.STRING, false,
                                                                               mandatoryAttribute.getDisplay_name(),
                                                                               false, ""));
        return cifAssetCharacteristic;
    }

    private String getValue(Template_Mandatory_Attributes mandatoryAttribute, TpeRequestDTO tpeRequest,
                            TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier attributeClassifier) {
        final String valueFromRequest = getMandatoryAttributeValueFromRequest(tpeRequest, mandatoryAttribute, attributeClassifier);
        if(!StringUtils.isBlank(valueFromRequest)) {
            return valueFromRequest;
        }
        return mandatoryAttribute.getDefault_value();
    }

    private String getMandatoryAttributeValueFromRequest(TpeRequestDTO tpeRequest, Template_Mandatory_Attributes mandatoryAttribute,
                                                         TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier attributeClassifier) {
        for (TpeRequestDTO.TpeMandatoryAttributesDTO tpeMandatoryAttributesDTO : tpeRequest.tpeMandatoryAttributesDTOCollection) {
            if(attributeClassifier.equals(tpeMandatoryAttributesDTO.getAttributeClassifier())){
                if(mandatoryAttribute.getAttribute_Name().equals(tpeMandatoryAttributesDTO.getAttributeName())){
                    return tpeMandatoryAttributesDTO.getAttributeValue();
                }
            }
        }
        return EMPTY;
    }

    private TPE_TemplateDetails_Response getTemplateDetails(CIFAsset cifAsset) {
        TPE_TemplateDetails_Request templateRequest = new TPE_TemplateDetails_Request(cifAsset.getOfferingDetail().getProductGroupName(),
                                                                                      attributesHelper.getConfigType(cifAsset).getValue(),
                                                                                      attributesHelper.getTPETemplateName(cifAsset).getValue());
        return templateTpeClient.SQE_TPE_TemplateDetails(templateRequest);
    }
}
