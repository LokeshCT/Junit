package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.domain.DateFormats;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.bt.rsqe.domain.SpecialBidWellKnownAttribute.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SpecialBidWellKnownAttributeMapperTest {

    @Test
    public void shouldUpdateWellKnownAttributeValuesInTpeRequest() throws ParseException {

        Date date = new SimpleDateFormat(DateFormats.TPE_DATE_FORMAT).parse("2011-11-11");
        String cusReqDate = new SimpleDateFormat(DateFormats.DATE_FORMAT).format(date);

        CharacteristicChange customerValue = new CharacteristicChange(CustomerValue.getAttributeName(), "1");
        CharacteristicChange customerValueCurrency = new CharacteristicChange(CustomerValueCurrency.getAttributeName(), "EUP");
        CharacteristicChange additionalInfo = new CharacteristicChange(AdditionalInformation.getAttributeName(), "aAddInfo");
        CharacteristicChange bidManager = new CharacteristicChange(BidManagerName.getAttributeName(), "John");
        CharacteristicChange requestName = new CharacteristicChange(RequestName.getAttributeName(), "aReqName");
        CharacteristicChange tier = new CharacteristicChange(Tier.getAttributeName(), "aTier");
        CharacteristicChange winChance = new CharacteristicChange(WinChance.getAttributeName(), "2");
        CharacteristicChange contractLength = new CharacteristicChange(ContractLength.getAttributeName(), "36");
        CharacteristicChange customerRequestedDate = new CharacteristicChange(CustomerRequestedDate.getAttributeName(), cusReqDate);
        CharacteristicChange detailedResponse = new CharacteristicChange(DetailedResponse.getAttributeName(), "aDetailedResponse");
        CharacteristicChange bidState = new CharacteristicChange(BidState.getAttributeName(), "aBidState");
        CharacteristicChange templateWiki = new CharacteristicChange(TemplateWiki.getAttributeName(), "aTemplateWiki");
        CharacteristicChange volumeForFeature = new CharacteristicChange(VolumeForFeature.getAttributeName(), "100");
        CharacteristicChange nonSpecialBid = new CharacteristicChange("nonWellKnownAttribute", "aNonWellKnownAttributeValue");

        //When
        SpecialBidWellKnownAttributeMapper attributeMapper = new SpecialBidWellKnownAttributeMapper();
        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        attributeMapper.map(newArrayList(customerValue, additionalInfo, bidManager, customerValueCurrency, requestName, tier, winChance, contractLength, customerRequestedDate,
                                         detailedResponse, bidState, templateWiki, volumeForFeature, nonSpecialBid), tpeRequestDTO);

        //Then

        assertThat(tpeRequestDTO.tier, is("aTier"));
        assertThat(tpeRequestDTO.requestName, is("aReqName"));
        assertThat(tpeRequestDTO.bidManagerName, is("John"));
        assertThat(tpeRequestDTO.additionalInformation, is("aAddInfo"));
        assertThat(tpeRequestDTO.customerValueCurrency, is("EUP"));
        assertThat(tpeRequestDTO.contractLength, is(36L));
        assertThat(tpeRequestDTO.customerValue, is(1L));
        assertThat(tpeRequestDTO.winChance, is(2L));
        assertThat(tpeRequestDTO.customerRequestedDate, is(date));
        assertThat(tpeRequestDTO.detailedResponse, is("aDetailedResponse"));
        assertThat(tpeRequestDTO.bidState, is("aBidState"));
        assertThat(tpeRequestDTO.templateWiki, is("aTemplateWiki"));
        assertThat(tpeRequestDTO.volumeForFeature, is(100L));
    }

    @Test
    public void shouldSyncUpTpeTemplateName() {

        CharacteristicChange requestName = new CharacteristicChange(RequestName.getAttributeName(), "aReqName");

        SpecialBidWellKnownAttributeMapper attributeMapper = new SpecialBidWellKnownAttributeMapper();
        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        attributeMapper.map(newArrayList(requestName), tpeRequestDTO);
        assertThat(tpeRequestDTO.requestName, is("aReqName"));

        //When

        attributeMapper.syncUpTemplateName(tpeRequestDTO, RequestName.getAttributeName(), "aNewTemplateName");

        //Then
        assertThat(tpeRequestDTO.requestName, is("aNewTemplateName"));
    }

    @Test
    public void shouldMapNullValueToWellKnownAttributesWhenNoValueGiven() throws ParseException {

        CharacteristicChange customerValue = new CharacteristicChange(CustomerValue.getAttributeName(), null);
        CharacteristicChange customerValueCurrency = new CharacteristicChange(CustomerValueCurrency.getAttributeName(), null);
        CharacteristicChange additionalInfo = new CharacteristicChange(AdditionalInformation.getAttributeName(), null);
        CharacteristicChange bidManager = new CharacteristicChange(BidManagerName.getAttributeName(), null);
        CharacteristicChange requestName = new CharacteristicChange(RequestName.getAttributeName(), null);
        CharacteristicChange tier = new CharacteristicChange(Tier.getAttributeName(), null);
        CharacteristicChange winChance = new CharacteristicChange(WinChance.getAttributeName(), null);
        CharacteristicChange contractLength = new CharacteristicChange(ContractLength.getAttributeName(), null);
        CharacteristicChange customerRequestedDate = new CharacteristicChange(CustomerRequestedDate.getAttributeName(), null);
        CharacteristicChange detailedResponse = new CharacteristicChange(DetailedResponse.getAttributeName(), null);
        CharacteristicChange bidState = new CharacteristicChange(BidState.getAttributeName(), null);
        CharacteristicChange templateWiki = new CharacteristicChange(TemplateWiki.getAttributeName(), null);
        CharacteristicChange volumeForFeature = new CharacteristicChange(VolumeForFeature.getAttributeName(), null);

        //When
        SpecialBidWellKnownAttributeMapper attributeMapper = new SpecialBidWellKnownAttributeMapper();
        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        attributeMapper.map(newArrayList(customerValue, additionalInfo, bidManager, customerValueCurrency, requestName, tier, winChance, contractLength, customerRequestedDate,
                                         detailedResponse, bidState, templateWiki, volumeForFeature), tpeRequestDTO);

        //Then

        assertThat(tpeRequestDTO.tier, nullValue());
        assertThat(tpeRequestDTO.requestName, nullValue());
        assertThat(tpeRequestDTO.bidManagerName, nullValue());
        assertThat(tpeRequestDTO.additionalInformation, nullValue());
        assertThat(tpeRequestDTO.customerValueCurrency, nullValue());
        assertThat(tpeRequestDTO.contractLength, nullValue());
        assertThat(tpeRequestDTO.customerValue, nullValue());
        assertThat(tpeRequestDTO.winChance, nullValue());
        assertThat(tpeRequestDTO.customerRequestedDate, nullValue());
        assertThat(tpeRequestDTO.detailedResponse, nullValue());
        assertThat(tpeRequestDTO.bidState, nullValue());
        assertThat(tpeRequestDTO.templateWiki, nullValue());
        assertThat(tpeRequestDTO.volumeForFeature, nullValue());
    }

    @Test
    public void shouldNotSetCustomerRequiredDateWhenNotGivenInExpectedFormat() throws ParseException {

        Date date = new SimpleDateFormat(DateFormats.TPE_DATE_FORMAT).parse("2011-11-11");
        String cusReqDate = new SimpleDateFormat(DateFormats.DATE_FORMAT).format(date);

        CharacteristicChange characteristicChange = new CharacteristicChange(CustomerRequestedDate.getAttributeName(), cusReqDate);

        SpecialBidWellKnownAttributeMapper attributeMapper = new SpecialBidWellKnownAttributeMapper();
        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        attributeMapper.map(newArrayList(characteristicChange), tpeRequestDTO);
        assertThat(tpeRequestDTO.customerRequestedDate, is(date));

        //When

        Date newDate = new SimpleDateFormat(DateFormats.TPE_DATE_FORMAT).parse("2011-11-11");
        String newCusReqDate = new SimpleDateFormat("yyyyy-MMM-dd").format(newDate);
        CharacteristicChange newCusReqDateChange = new CharacteristicChange(CustomerRequestedDate.getAttributeName(), newCusReqDate);


        attributeMapper.map(newArrayList(newCusReqDateChange), tpeRequestDTO);

        //Then
        assertThat(tpeRequestDTO.customerRequestedDate, is(date));

    }


}