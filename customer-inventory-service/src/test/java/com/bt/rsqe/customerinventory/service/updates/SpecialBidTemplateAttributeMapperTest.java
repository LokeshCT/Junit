package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.domain.SpecialBidWellKnownAttribute;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import org.junit.Test;

import static com.bt.rsqe.projectengine.TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class SpecialBidTemplateAttributeMapperTest {

    @Test
    public void shouldUpdateValueForAnAttribute() {
        //Given
        CharacteristicChange characteristicChange = new CharacteristicChange("A", "aNewValue");
        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        tpeRequestDTO.tpeMandatoryAttributesDTOCollection = newArrayList(new TpeRequestDTO.TpeMandatoryAttributesDTO("id",
                "A",
                "type",
                "aOldValue",
                0,
                null));

        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeName(), is("A"));
        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeValue(), is("aOldValue"));
        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeClassifier(), nullValue());


        //When
        SpecialBidTemplateAttributeMapper attributeMapper = new SpecialBidTemplateAttributeMapper();
        attributeMapper.map(newArrayList(characteristicChange), tpeRequestDTO, PRIMARY);

        //Then

        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeName(), is("A"));
        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeValue(), is("aNewValue"));
        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeClassifier(), is(PRIMARY));

    }


    @Test
    public void shouldAddAttributeWhenNotAvailable() {
        //Given
        CharacteristicChange characteristicChange = new CharacteristicChange("A", "aNewValue");
        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.isEmpty(), is(true));


        //When
        SpecialBidTemplateAttributeMapper attributeMapper = new SpecialBidTemplateAttributeMapper();
        attributeMapper.map(newArrayList(characteristicChange), tpeRequestDTO, PRIMARY);

        //Then

        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeName(), is("A"));
        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeValue(), is("aNewValue"));
        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeClassifier(), is(PRIMARY));

    }

    @Test
    public void shouldUpdateWithAttributeClassifierWhenTPETemplateAttributeDoesNotHave() {

        CharacteristicChange characteristicChange = new CharacteristicChange("A", "aNewValue");
        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        tpeRequestDTO.tpeMandatoryAttributesDTOCollection = newArrayList(new TpeRequestDTO.TpeMandatoryAttributesDTO("id",
                "A",
                "type",
                "aOldValue",
                0,
                null));

        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeClassifier(), nullValue());


        //When
        SpecialBidTemplateAttributeMapper attributeMapper = new SpecialBidTemplateAttributeMapper();
        attributeMapper.map(newArrayList(characteristicChange), tpeRequestDTO, COMMON);

        //Then

        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeName(), is("A"));
        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeValue(), is("aNewValue"));
        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeClassifier(), is(COMMON));
    }

    @Test
    public void shouldNotUpdateWithAttributeClassifierWhenTPETemplateAttributeHasAttributeClassifier() {

        CharacteristicChange characteristicChange = new CharacteristicChange("A", "aNewValue");
        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        tpeRequestDTO.tpeMandatoryAttributesDTOCollection = newArrayList(new TpeRequestDTO.TpeMandatoryAttributesDTO("id",
                "A",
                "type",
                "aOldValue",
                0,
                PRIMARY));

        //When
        SpecialBidTemplateAttributeMapper attributeMapper = new SpecialBidTemplateAttributeMapper();
        attributeMapper.map(newArrayList(characteristicChange), tpeRequestDTO, COMMON);

        //Then

        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeName(), is("A"));
        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeValue(), is("aNewValue"));
        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeClassifier(), is(PRIMARY));
    }

     @Test
    public void shouldNotUpdateMandatoryAtrributeValueforAdditionalInformation() {

        String name = SpecialBidWellKnownAttribute.AdditionalInformation.name();
        CharacteristicChange characteristicChange = new CharacteristicChange(name, "aNewValue");
        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        tpeRequestDTO.tpeMandatoryAttributesDTOCollection = newArrayList(new TpeRequestDTO.TpeMandatoryAttributesDTO("id",
                                                                                                                     name,
                                                                                                                     "type",
                                                                                                                     "aOldValue",
                                                                                                                     0,
                                                                                                                     WELL_KNOWN));

        //When
        SpecialBidTemplateAttributeMapper attributeMapper = new SpecialBidTemplateAttributeMapper();
        attributeMapper.map(newArrayList(characteristicChange), tpeRequestDTO, WELL_KNOWN);

        //Then
        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeName(), is(name));
        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeValue(), is("aNewValue"));
        assertThat(tpeRequestDTO.tpeMandatoryAttributesDTOCollection.get(0).getAttributeClassifier(), is(WELL_KNOWN));
    }

}