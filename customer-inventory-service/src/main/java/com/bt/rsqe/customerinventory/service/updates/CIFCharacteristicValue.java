package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.domain.DateFormats;
import com.bt.rsqe.domain.product.AttributeDataType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.commons.lang.StringUtils.*;

public class CIFCharacteristicValue {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DateFormats.DATE_FORMAT);
    private static final SimpleDateFormat cifDateFormat = new SimpleDateFormat(DateFormats.CHARACTERISTIC_DATE_FORMAT);
    private static final String YYYY_MM_DD_HH_MM = "([0-9]{4})/([0-9]{2})/([0-9]{2}) ([0-9]{2}):([0-9]{2})";


    public String valueOf(AttributeDataType dataType, String value) {
        switch (dataType) {
            case DATE:
                try {
                    if (isNotEmpty(value)) {
                        if (value.matches(YYYY_MM_DD_HH_MM)) {
                            return value;
                        }
                        Date date = dateFormat.parse(value);
                        return cifDateFormat.format(date);
                    }
                    return null;

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            default:
                return value;
        }

    }
}
