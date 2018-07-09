DECLARE tbl_exist INTEGER; BEGIN SELECT count(*) INTO tbl_exist FROM USER_TABLES WHERE table_name = 'REQUEST_RESPONSE_STORE'; IF tbl_exist = 1 THEN EXECUTE IMMEDIATE 'DROP TABLE REQUEST_RESPONSE_STORE CASCADE CONSTRAINTS'; END IF; END;//

CREATE TABLE REQUEST_RESPONSE_STORE
(
    STORE_ID VARCHAR2(50 CHAR) NOT NULL,
    VALUE_TYPE VARCHAR2(8 CHAR) NOT NULL,
    ORIGIN VARCHAR2(100 CHAR) NOT NULL,
    OPERATION_NAME VARCHAR2(100 CHAR) NOT NULL,
    IDENTIFIER VARCHAR2(100 CHAR) NOT NULL,
    STORE_VALUE CLOB NOT NULL,
    CREATED_DATE TIMESTAMP DEFAULT SYSDATE,
    CONSTRAINT REQUEST_RESPONSE_STORE_PK PRIMARY KEY (STORE_ID) ENABLE
)
//