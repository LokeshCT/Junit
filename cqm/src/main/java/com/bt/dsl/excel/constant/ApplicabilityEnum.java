package com.bt.dsl.excel.constant;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 01/09/15
 * Time: 12:45
 * To change this template use File | Settings | File Templates.
 */
public enum ApplicabilityEnum {
    YES,
    NO,
    NORESPONSE;


    @Override
    public String toString(){
        switch(this){
            case YES:
                return "Yes";
            case NO:
                return "No";
            case NORESPONSE:
                return "No Response";
        }
        return this.name();
    }

    public static ApplicabilityEnum getApplicabilityEnum(String value){
        if( value != null ){
            if("Yes".equalsIgnoreCase(value.trim())){
                return ApplicabilityEnum.YES;
            }

            if("No".equalsIgnoreCase(value.trim())){
                return ApplicabilityEnum.NO;
            }
        }
        return ApplicabilityEnum.NORESPONSE;
    }
}
