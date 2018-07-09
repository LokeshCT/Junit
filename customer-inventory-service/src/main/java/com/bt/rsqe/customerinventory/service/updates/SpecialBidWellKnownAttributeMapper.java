package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.domain.DateFormats;
import com.bt.rsqe.domain.SpecialBidWellKnownAttribute;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.apache.commons.lang.StringUtils.*;

public class SpecialBidWellKnownAttributeMapper {

    public void map(List<CharacteristicChange> characteristicChanges, TpeRequestDTO tpeRequest) {
        for (CharacteristicChange characteristicChange : characteristicChanges) {
            map(tpeRequest, characteristicChange.getName(), characteristicChange.getNewValue());
        }

    }

    public void syncUpTemplateName(TpeRequestDTO tpeRequest, String name, String value) {
        map(tpeRequest, name, value);
    }

    public TpeRequestDTO map(TpeRequestDTO tpeRequestDTO, String name, String value) {
        if (SpecialBidWellKnownAttribute.contains(name)) {
            switch (SpecialBidWellKnownAttribute.get(name)) { // After checking if given name is available in well known attributes, the following switch is happening.
                case AdditionalInformation:
                    tpeRequestDTO.additionalInformation = value;
                    break;
                case WinChance:
                    tpeRequestDTO.winChance = asLong(value);
                    break;
                case RequestName:
                    tpeRequestDTO.requestName = value;
                    break;
                case VolumeForFeature:
                    tpeRequestDTO.volumeForFeature = asLong(value);
                    break;
                case BidManagerName:
                    tpeRequestDTO.bidManagerName = value;
                    break;
                case CustomerValue:
                    tpeRequestDTO.customerValue = asLong(value);
                    break;
                case CustomerValueCurrency:
                    tpeRequestDTO.customerValueCurrency = value;
                    break;
                case CustomerRequestedDate:
                    try {
                        if (isNotEmpty(value)) {
                            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DateFormats.DATE_FORMAT);
                            DateTime dateTime = dateTimeFormatter.parseDateTime(value);
                            String tpeFormattedDated = String.format("%s-%s-%s", dateTime.toString("yyyy"), dateTime.toString("MM"), dateTime.toString("dd"));
                            tpeRequestDTO.customerRequestedDate = new SimpleDateFormat(DateFormats.TPE_DATE_FORMAT).parse(tpeFormattedDated);
                        }
                    } catch (ParseException e) {
                        // Leave the value as-is if date is not readable
                    }
                    break;
                case Tier:
                    tpeRequestDTO.tier = value;
                    break;
                case ContractLength:
                    tpeRequestDTO.contractLength = asLong(value);
                    break;
                case DetailedResponse:
                    tpeRequestDTO.detailedResponse = value;
                    break;
                case BidState:
                    tpeRequestDTO.bidState = value;
                    break;
                case TemplateWiki:
                    tpeRequestDTO.templateWiki = value;
                    break;
            }
        }
        return tpeRequestDTO;
    }

    private Long asLong(String value) {
        return isEmpty(value) ? null : Long.parseLong(value);
    }
}