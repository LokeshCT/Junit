    package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

    public enum SiteProductSheetStaticColumn {
        //Column index defined here will be overwritten later, hence setting as 0
        SITE_STATUS("Site Status",0,"siteStatus",1,true, true, "PriceColumn"),
        SITE_ID("Site ID",0,"siteID",2,true, true, "PriceColumn"),
        SITE_NAME("Site",0,"siteName",1,true, true, "PriceColumn"),
        COUNTRY("Country",0,"country",1,true, true, "PriceColumn"),
        CITY("City",0,"city",1,true, true, "PriceColumn"),
        ORDER_TYPE("Order Type",0,"orderType",1,true, true, "PriceColumn"),
        //price line
        CPE_PRODUCT_INSTANCE("CPE Product Instance", 0, "cpeProductInstance",1,true, true, "PriceColumn"),
        CPE_PRODUCT_INSTANCE_VERSION("CPE Product Version", 0, "cpeProductInstanceVersion",0,true, true, "PriceColumn"),
        CPE_PRICE_STATUS("CPE Price Status",0,"cpePriceStatus",1,true, true, "PriceColumn"),
        CPE_INSTALL_EUP("CPE Install EUP",0,"cpeInstallEUP",0,true, true, "PriceColumn"),
        CPE_INSTALL_PRICE_LINE("CPE Install Price Line", 0, "cpeInstallPriceLine",1,true, true, "PriceColumn"),
        CPE_INSTALL_PTP("CPE Install PTP",0,"cpeInstallPTP",0,true, true, "PriceColumn"),
        CPE_INSTALL_DISCOUNT("CPE Install Discount %",0,"cpeInstallDiscount",0,false, true, "PriceColumn"),
        CPE_MONTHLY_EUP("CPE Monthly EUP",0,"cpeMonthlyEUP",0,true, true, "PriceColumn"),
        CPE_MONTHLY_PRICE_LINE("CPE Monthly Price Line", 0, "cpeMonthlyPriceLine",1,true, true, "PriceColumn"),
        CPE_MONTHLY_PTP("CPE Monthly PTP",0,"cpeMonthlyPTP",0,true, true, "PriceColumn"),
        CPE_MONTHLY_DISCOUNT("CPE Monthly Discount %",0,"cpeMonthlyDiscount",0,false, true, "PriceColumn"),
        CPE_NRC("CPE NRC",0,"cpeNRC",0,true, true, "PriceColumn"),
        CPE_MRC("CPE MRC",0,"cpeMRC",0,true, true, "PriceColumn"),
        HARDWARE_VENDOR_MTCE_NRC("Hardware Vendor Mtce NRC", 0, "hardwareVendorMtceNRC", 0, true, true, "PriceColumn"),
        HARDWARE_VENDOR_MTCE_MRC("Hardware Vendor Mtce MRC", 0, "hardwareVendorMtceMRC", 0, true, true, "PriceColumn"),

        CPE_DISCOUNTED_MRC("CPE Discounted MRC", 0, "cpeDiscountedMRC", 0, true, true, "CostColumn"),
        CPE_VENDOR_DISCOUNT_REF("CPE Vendor Discount Ref", 0, "cpeVendorDiscountRef", 1, true, true, "CostColumn"),
        CAPEX_NRC("Capex NRC", 0, "capexNRC", 0, true, true, "CostColumn"),
        CAPEX_DISCOUNTED_NRC("Capex Discounted NRC", 0, "Capex Discounted NRC", 0, true, true, "CostColumn"),
        KIT_MRC("Kit MRC", 0, "kitMRC", 0, true, true, "CostColumn"),
        KIT_DISCOUNTED_MRC("Kit Discounted MRC", 0, "kitDiscountedMRC", 0, true, true, "CostColumn"),
        CPE_SUPPLIER_MTCE_MRC("CPE Supplier Mtce MRC", 0, "cpeSupplierMtceMRC", 0, true, true, "CostColumn"),
        VENDOR_MTCE_DISCOUNTED_MRC("Vendor Mtce Discounted MRC", 0, "vendorMtceDiscountedMRC", 0, true, true, "CostColumn"),
        VENDOR_MTCE_DISCOUNT_REF("Vendor Mtce Discount Ref", 0, "vendorMtceDiscountRef", 1, true, true, "CostColumn");

        public final String columnName;
        public final int columnIndex;
        public final String retrieveValueFrom;
        public final int dataType;
        public boolean isReadOnly;
        public boolean visibility;
        public String columnGroup;

        private SiteProductSheetStaticColumn(String columnName, int columnIndex, String retrieveValueFrom, int dataType, boolean isReadOnly, boolean visibility, String columnGroup) {
            this.columnName = columnName;
            this.columnIndex = columnIndex;
            this.retrieveValueFrom = retrieveValueFrom;
            this.dataType = dataType;
            this.isReadOnly = isReadOnly;
            this.visibility = visibility;
            this.columnGroup = columnGroup;
        }

    }
