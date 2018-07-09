package com.bt.usermanagement.util;

import java.sql.Timestamp;

import static com.bt.rsqe.utils.AssertObject.isNotNull;

public class GeneralUtil {

    public static java.sql.Timestamp getCurrentTimeStamp() {
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new Timestamp(utilDate.getTime());
        return  sqlDate;
    }

    public static String getLocationFromPostalAddress(String postalAddress){
        String location = "";
        if(isNotNull(postalAddress)){
            String[] postalAddressArr = postalAddress.split(java.util.regex.Pattern.quote("$"));
            if(isNotNull(postalAddressArr) && postalAddressArr.length > 0){
                for(int i=postalAddressArr.length-1;i<postalAddressArr.length;i++){
                    location = postalAddressArr[i];
                }
            }
        }
     return location;
    }

}
