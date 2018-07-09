DECLARE col_exist INTEGER; BEGIN select count(*) into col_exist from ALL_TAB_COLUMNS where table_name='SAC_BULK_UPLOAD' and column_name='USER_NAME';IF col_exist > 0 THEN EXECUTE IMMEDIATE 'alter table SAC_BULK_UPLOAD drop column USER_NAME'; END IF; END;//

DECLARE col_exist INTEGER; BEGIN select count(*) into col_exist from ALL_TAB_COLUMNS where table_name='SAC_BULK_UPLOAD' and column_name='HOST_NAME';IF col_exist > 0 THEN EXECUTE IMMEDIATE 'alter table SAC_BULK_UPLOAD drop column HOST_NAME'; END IF; END;//

alter table SAC_BULK_UPLOAD add (
   USER_NAME VARCHAR2(100),
   HOST_NAME     VARCHAR2(50)
)
//
