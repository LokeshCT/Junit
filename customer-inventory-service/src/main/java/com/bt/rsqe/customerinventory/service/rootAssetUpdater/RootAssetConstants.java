package com.bt.rsqe.customerinventory.service.rootAssetUpdater;

public class RootAssetConstants {

    public static final String InstanceType = "Product";
    public static final String SourceSystem = "CIF-rSQE";
    public static final String Status = "LIVE";
    public static final String  BASE_PATH = "/rsqe/rootAssetService";
    public static final String  BFG_ASSET_ID = "bfgAssetId";
    public static final String  PRODUCT_INSTANCE_ID = "productInstanceId";

    public enum OfferingType{
        FOI("FOI"),
        NetworkService ("NetworkService"),
        NetworkNode ("NetworkNode"),
        Software ("Software"),
        Package ("Package"),
        Bearer ("Bearer"),
        VPN ("VPN"),
        VAS("VAS"),
        INVALID_OfferingType("invalid");

        String value;

        private OfferingType(String value){
            this.value = value;
        }

        public static OfferingType getByName(String offeringType) {
            if (offeringType != null) {
                for (OfferingType ot : OfferingType.values()) {
                    if (offeringType.equalsIgnoreCase(ot.value)) {
                        return ot;
                    }
                }
            }
            return INVALID_OfferingType;
        }

        @Override
        public String toString(){
            return value;
        }
    }

    public enum OfferingShortName{
        FOI("FOI"),
        NWS ("NetworkService"),
        NTN ("NetworkNode"),
        SW ("Software"),
        PI ("Package"),
        BIN ("Bearer"),
        VPN ("VPN"),
        VAS("VAS"),
        INVALID_OfferingShortName("invalid");

        String value;

        private OfferingShortName(String value){
            this.value = value;
        }

        public static String getByName(String offeringType) {
            if (offeringType != null) {
                for (OfferingShortName ot : OfferingShortName.values()) {
                    if (offeringType.equalsIgnoreCase(ot.value)) {
                        return ot.name();
                    }
                }
            }
            return "invalid";
        }

        @Override
        public String toString(){
            return value;
        }
    }

    public static final String Customer_OnBoard_LookUp_Query = "select * from WS_USER.WS_NGSD_CUSTOMERS_V where CUS_ID =:id";


}
