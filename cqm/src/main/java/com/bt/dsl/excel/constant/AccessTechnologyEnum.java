package com.bt.dsl.excel.constant;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 01/09/15
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public enum AccessTechnologyEnum {
    DSLSTANDARD,
    DSLPLUS,
    DSLPREMIUM,
    EFM,
    ADSL,
    SDSL;
    @Override
    public String toString(){
        switch(this){
            case DSLSTANDARD:
                return "DSL Standrd";
            case DSLPLUS:
                return "DSL Plus";
            case DSLPREMIUM:
                return "DSL Premium";
        }
        return this.name();
    }

    public static  AccessTechnologyEnum getEnum(String accessType){
        if (accessType != null){

            if (accessType.equalsIgnoreCase("ADSL")){
               return AccessTechnologyEnum.ADSL;
            }

            if (accessType.equalsIgnoreCase("SDSL")){
                return AccessTechnologyEnum.SDSL;
            }
            if (accessType.equalsIgnoreCase("Ethernet")){

                return AccessTechnologyEnum.EFM;
            }
            return null;

        } else{
            return null;
        }
    }
}
