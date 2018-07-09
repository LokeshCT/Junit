create index SPS_AVAILABILITY_TYPE_ID_FK on SUPPLIER_SITE(AVAILABILITY_TYPE_ID)//
create index SP_SITE_ID_FK on SUPPLIER_PRODUCT (SITE_ID)//
create index AS_SUPP_PROD_ID_FK on AVAILABILITY_SET (SUPP_PROD_ID)//
create index AP_SUPP_PROD_ID_FK on AVAILABILITY_PARAM (SET_ID)//
create index SCAR_SCCR_ID_FK on SUPPLIER_CHECK_APE_REQUEST(SCCR_ID)//
create index SRS_SCCR_ID_FK on SUPPLIER_REQUEST_SITE(SCCR_ID)//
create index SRSS_SRS_ID_FK on SUPPLIER_REQUEST_SITE_SPAC(SRS_ID)//