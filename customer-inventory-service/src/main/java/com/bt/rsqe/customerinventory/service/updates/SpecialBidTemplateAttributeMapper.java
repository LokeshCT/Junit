package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.domain.SpecialBidWellKnownAttribute;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import com.bt.rsqe.projectengine.TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier;

import java.util.List;

public class SpecialBidTemplateAttributeMapper {
    public void map(List<CharacteristicChange> characteristicChanges, TpeRequestDTO tpeRequestDTO, AttributeClassifier attributeClassifier) {
        for (CharacteristicChange characteristicChange : characteristicChanges) {
            if(!SpecialBidWellKnownAttribute.contains(characteristicChange.getName())) {
                upsert(tpeRequestDTO, attributeClassifier, characteristicChange.getName(), characteristicChange.getNewValue());
            }

        }

    }

    public void upsert(TpeRequestDTO tpeRequestDTO, AttributeClassifier attributeClassifier, String name, String value) {
        for (TpeRequestDTO.TpeMandatoryAttributesDTO attr : tpeRequestDTO.tpeMandatoryAttributesDTOCollection) {
            if (attr.getAttributeName().equals(name)) {
                attr.setAttributeValue(value);
                if (null == attr.getAttributeClassifier()) {
                    attr.setAttributeClassifier(attributeClassifier);
                }
                return;
            }
        }
        tpeRequestDTO.tpeMandatoryAttributesDTOCollection.add(new TpeRequestDTO.TpeMandatoryAttributesDTO(tpeRequestDTO.id,
                                                                                                            name,
                                                                                                            null,
                                                                                                            value,
                                                                                                            0,
                                                                                                            attributeClassifier));
    }
}
