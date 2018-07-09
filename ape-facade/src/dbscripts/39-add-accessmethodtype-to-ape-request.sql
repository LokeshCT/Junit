DECLARE col_exist INTEGER; BEGIN select count(*) into col_exist from ALL_TAB_COLUMNS where table_name='APE_REQUEST' and column_name='ACCESS_METHOD_TYPE';IF col_exist > 0 THEN EXECUTE IMMEDIATE 'alter table APE_REQUEST drop column ACCESS_METHOD_TYPE'; END IF; END;//

ALTER TABLE APE_REQUEST ADD ACCESS_METHOD_TYPE VARCHAR2 (20)
//