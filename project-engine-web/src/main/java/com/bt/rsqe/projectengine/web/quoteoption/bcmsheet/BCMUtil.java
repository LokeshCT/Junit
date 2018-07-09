package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmExportChannelInformationSheetFactory.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.base.Strings.*;

public class BCMUtil {

    public static BigDecimal changeDiscountToDecimalAndRound(String value) {
        if (isNullOrEmpty(value)) {
            return null;
        }
        final BigDecimal decimalValue = new BigDecimal(value).movePointLeft(2);
        return round(decimalValue, 7);
    }

    public static String getPriceInStr(BigDecimal decPrice){
        if(isNotNull(decPrice)){
            return String.valueOf(decPrice.setScale(2, RoundingMode.CEILING));
        }else{
            return "";
        }
    }
}
