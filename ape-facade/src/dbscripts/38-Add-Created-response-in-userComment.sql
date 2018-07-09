
DECLARE col_exist INTEGER; BEGIN select count(*) into col_exist from ALL_TAB_COLUMNS where table_name='ACCESS_USER_COMMENTS' and column_name='USER_NAME';IF col_exist > 0 THEN EXECUTE IMMEDIATE 'alter table ACCESS_USER_COMMENTS drop column USER_NAME'; END IF; END;//

ALTER TABLE ACCESS_USER_COMMENTS ADD (USER_NAME VARCHAR2(300 CHAR))
//

DECLARE col_exist INTEGER; BEGIN select count(*) into col_exist from ALL_TAB_COLUMNS where table_name='ACCESS_USER_COMMENTS' and column_name='CREATED_DATE';IF col_exist > 0 THEN EXECUTE IMMEDIATE 'alter table ACCESS_USER_COMMENTS drop column CREATED_DATE'; END IF; END;//

ALTER TABLE ACCESS_USER_COMMENTS ADD (CREATED_DATE TIMESTAMP DEFAULT SYSDATE)
//

DECLARE col_exist INTEGER; BEGIN select count(*) into col_exist from ALL_TAB_COLUMNS where table_name='ACCESS_USER_COMMENTS' and column_name='USER_QREF_ID';IF col_exist > 0 THEN EXECUTE IMMEDIATE 'alter table ACCESS_USER_COMMENTS drop column USER_QREF_ID'; END IF; END;//

ALTER TABLE ACCESS_USER_COMMENTS ADD (USER_QREF_ID VARCHAR2(100 CHAR))
//

update ACCESS_USER_COMMENTS set USER_QREF_ID =(select regexp_replace(rawtohex(sys_guid()), '([A-F0-9]{8})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{4})([A-F0-9]{12})', '\1-\2-\3-\4-\5')  from dual) , CREATED_DATE = sysdate
//

DECLARE is_nullable varchar2(1); BEGIN select nullable into is_nullable from ALL_TAB_COLUMNS where table_name='ACCESS_USER_COMMENTS' and column_name='USER_QREF_ID';IF is_nullable = 'Y' THEN EXECUTE IMMEDIATE 'ALTER TABLE ACCESS_USER_COMMENTS MODIFY (USER_QREF_ID  NOT NULL)'; END IF; END;//

