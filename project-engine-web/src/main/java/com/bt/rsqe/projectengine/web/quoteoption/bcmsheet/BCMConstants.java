package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

public class BCMConstants {
    public static final Short HEADER_ROW_HEIGHT = 1540;
    public static final Short DATA_ROW_HEIGHT = 920;

    //Added by Arabinda, Release 31.0 BCM TASK
    //This will fetch the BCM Version number used in the BCM EXPORT & IMPORT SHEET
    public static final String BCM_SHEET_VERSION="31.0";
    public static final String BCM_BID_INFO_SHEET="Bid Info";
    public static final String BCM_PRODUCT_PER_SITE_SHEET="Product Per Site";
    public static final String BCM_SPECIAL_BID_INFO_SHEET="SBR Non Std";
    public static final String BCM_SITE_MANAGEMENT_SHEET="Site Management";
    public static final String BCM_SITE_SHEET="Site";
    //Added for HSSF Cell Types
    public static final int CELL_TYPE_INTEGER=2;
    public static final int VERSION_POSITION_AFTER_PRODUCT_ID = 1;
}
