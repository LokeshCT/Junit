package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


import com.bt.rsqe.domain.product.ProductOffering;

import java.util.Map;

import static com.google.common.collect.Maps.*;

public enum AttributesMapper {

    SITE_CI_ATTRIBUTES("CI Site"){
        @Override
        public Map<String, String> attributesMap() {
            Map<String,String> map = newLinkedHashMap();
            map.put(CPE_TYPE_COLUMN, ProductOffering.NAME);
            map.put(LAN_TYPE_COLUMN, ProductOffering.LAN_TYPE);
            map.put(PROD_CODE_COLUMN,ProductOffering.PRODUCT_IDENTIFIER_RESERVED_NAME);
            return map;
        }
    },
    SITE_CA_ATTRIBUTES("CA Site"){
        @Override
        public Map<String, String> attributesMap() {
            Map<String,String> map = newLinkedHashMap();
            map.put(CPE_TYPE_COLUMN, ProductOffering.NAME);
            map.put(LAN_TYPE_COLUMN, ProductOffering.LAN_TYPE);
            map.put(PROD_CODE_COLUMN,ProductOffering.PRODUCT_IDENTIFIER_RESERVED_NAME);
            return map;
        }
    },
    SITE_CO_ATTRIBUTES("CO Site"){
        @Override
        public Map<String, String> attributesMap() {
            Map<String,String> map = newLinkedHashMap();
            map.put(CPE_OWNERSHIP_COLUMN, ProductOffering.CPE_OPTION);
            map.put(CPE_TYPE_COLUMN, ProductOffering.NAME);
            map.put(CPE_STATUS_COLUMN, ProductOffering.CPE_STATUS);
            return map;
        }
    },
    SITE_IPC_ATTRIBUTES("IPC Site"){
        @Override
        public Map<String, String> attributesMap() {
            Map<String,String> map = newLinkedHashMap();
            map.put(CPE_TYPE_COLUMN, ProductOffering.NAME);
            map.put(LAN_TYPE_COLUMN, ProductOffering.LAN_TYPE);
            map.put(PROD_CODE_COLUMN,ProductOffering.PRODUCT_IDENTIFIER_RESERVED_NAME);
            return map;
        }
    },
    SITE_CAS_ATTRIBUTES("CAS Site"){
        @Override
        public Map<String, String> attributesMap() {
            Map<String,String> map = newLinkedHashMap();
            map.put(CPE_TYPE_COLUMN, ProductOffering.NAME);
            map.put(LAN_TYPE_COLUMN, ProductOffering.LAN_TYPE);
            map.put(PROD_CODE_COLUMN,ProductOffering.PRODUCT_IDENTIFIER_RESERVED_NAME);
            return map;
        }
    },
    SITE_ICR_ATTRIBUTES("ICR Site"){
        @Override
        public Map<String, String> attributesMap() {
            Map<String,String> map = newLinkedHashMap();
            map.put(CPE_TYPE_COLUMN, ProductOffering.NAME);
            map.put(LAN_TYPE_COLUMN, ProductOffering.LAN_TYPE);
            map.put(PROD_CODE_COLUMN,ProductOffering.PRODUCT_IDENTIFIER_RESERVED_NAME);
            return map;
        }
    },
    SITE_ICG_ATTRIBUTES("ICG Site"){
        @Override
        public Map<String, String> attributesMap() {
            Map<String,String> map = newLinkedHashMap();
            map.put(CPE_TYPE_COLUMN, ProductOffering.NAME);
            map.put(LAN_TYPE_COLUMN, ProductOffering.LAN_TYPE);
            map.put(PROD_CODE_COLUMN,ProductOffering.PRODUCT_IDENTIFIER_RESERVED_NAME);
            return map;
        }
    },
    SERVICE_APMO_ATTRIBUTES(ServiceProductScode.ConnectIntelligenceAPMoService.getShortServiceName()){
        @Override
        public Map<String, String> attributesMap() {
            Map<String,String> map = newLinkedHashMap();
            map.put(BUNDLE_NAME_COLUMN, ProductOffering.NAME);
            map.put(CONNECT_APP_MGMT_OPTION_COLUMN, ProductOffering.CONNECT_APPLICATION_MANAGEMENT_OPTION);
            return map;
        }
    },
    SERVICE_WPMO_ATTRIBUTES(ServiceProductScode.ConnectIntelligenceWPMoService.getShortServiceName()){
        @Override
        public Map<String, String> attributesMap() {
            Map<String,String> map = newLinkedHashMap();
            map.put(METRICS_PROVIDED_COLUMN, ProductOffering.METRICS_PROVIDED);
            return map;
        }
    },
    SERVICE_CO_CENTRAL_ATTRIBUTES(ServiceProductScode.ConnectOptimisationCentralServices.getShortServiceName()){
        @Override
        public Map<String, String> attributesMap() {
            Map<String,String> map = newLinkedHashMap();
            map.put(MANAGEMENT_OPTION_COLUMN, ProductOffering.CONNECT_APPLICATION_MANAGEMENT_OPTION);
            map.put(CENTRAL_ANALYST_COLUMN, ProductOffering.CENTRAL_ANALYSTS_REQUIRED);
            map.put(CENTRAL_CONSULTANT_COLUMN, ProductOffering.CENTRAL_CONSULTANTS_REQUIRED);
            return map;
        }
    },
    SITE_CI_MANAGEMENT_ATTRIBUTES("CI Site Management"){
        @Override
        public Map<String, String> attributesMap() {
            Map<String,String> map = newLinkedHashMap();
            map.put(NO_OF_COLD_SPARE, "NO OF COLD SPARE");
            map.put(NO_OF_ACTIVE_DEVICES, "NO OF ACTIVE DEVICES");
            return map;
        }
    },
    SITE_CA_MANAGEMENT_ATTRIBUTES("CA Site Management"){
        @Override
        public Map<String, String> attributesMap() {
            Map<String,String> map = newLinkedHashMap();
            map.put(NO_OF_COLD_SPARE, "NO OF COLD SPARE");
            map.put(NO_OF_ACTIVE_DEVICES, "NO OF ACTIVE DEVICES");
            map.put(NO_OF_VIRTUAL_DEVICES, "NO OF VIRTUAL DEVICES");
            return map;
        }
    },
    SITE_CO_MANAGEMENT_ATTRIBUTES("CO Site Management"){
        @Override
        public Map<String, String> attributesMap() {
            Map<String,String> map = newLinkedHashMap();
            map.put(NO_OF_COLD_SPARE, "NO OF COLD SPARE");
            map.put(NO_OF_ACTIVE_DEVICES, "NO OF ACTIVE DEVICES");
            return map;
        }
    };

    public final String attributeType;
    //The Map contains Key as the Column Name and Value as the Attribute Name
    public abstract Map<String,String>  attributesMap();
    //This are the COLUMN Names
    public static final String CPE_TYPE_COLUMN="CPE Type";
    public static final String LAN_TYPE_COLUMN="LAN Type";
    public static final String PROD_CODE_COLUMN="Prod Code";
    public static final String METRICS_PROVIDED_COLUMN="Metrics Provided";
    public static final String BUNDLE_NAME_COLUMN="Bundle Name";
    public static final String CONNECT_APP_MGMT_OPTION_COLUMN="Connect Applications Management Option";
    public static final String CPE_STATUS_COLUMN="CPE Status";
    public static final String CENTRAL_CONSULTANT_COLUMN= "Central Consultant";
    public static final String MANAGEMENT_OPTION_COLUMN= "Management Option";
    public static final String CENTRAL_ANALYST_COLUMN="Central Analyst";
    public static final String CPE_OWNERSHIP_COLUMN="CPE Ownership";
    public static final String NO_OF_COLD_SPARE="No of Cold Spare";
    public static final String NO_OF_ACTIVE_DEVICES="No of Active devices";
    public static final String NO_OF_VIRTUAL_DEVICES="No of Virtual devices";


    private AttributesMapper(String attributeType) {
        this.attributeType = attributeType;
    }

    public static AttributesMapper getAttributeMapper(String attributeType){
        for(AttributesMapper attributesMapper : AttributesMapper.values()){
            if(attributesMapper.attributeType.equalsIgnoreCase(attributeType)){
                return attributesMapper;
            }
        }
        return null;
    }
}
