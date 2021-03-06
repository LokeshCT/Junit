DECLARE tbl_exist INTEGER; BEGIN SELECT COUNT (*) INTO tbl_exist FROM USER_TABLES WHERE table_name = 'SUPPLIER_CHECK_CLIENT_REQUEST'; IF tbl_exist > 0 THEN EXECUTE IMMEDIATE 'DROP TABLE SUPPLIER_CHECK_CLIENT_REQUEST CASCADE CONSTRAINTS'; EXECUTE IMMEDIATE 'DROP SEQUENCE SCCR_ID'; END IF; END;//

CREATE TABLE SUPPLIER_CHECK_CLIENT_REQUEST
(
   SCCR_ID                          VARCHAR2 (255),
   CALLBACK_URI                     VARCHAR2 (255),
   TRIGGER_TYPE                     VARCHAR2 (20),
   AUTO_TRIGGER                     VARCHAR2 (20),
   SOURCE_SYSTEM_NAME               VARCHAR2 (100),
   REQUESTED_BY                     VARCHAR2 (100),
   CUSTOMER_ID                      NUMBER (30) NOT NULL,
   STATUS                           VARCHAR2 (20),
   CREATED_ON                       TIMESTAMP(6),
   UPDATED_ON                       TIMESTAMP(6)
)
//

ALTER TABLE SUPPLIER_CHECK_CLIENT_REQUEST ADD CONSTRAINT SCCR_PK PRIMARY KEY(SCCR_ID)
//

CREATE SEQUENCE SCCR_ID
   START WITH 1
   MAXVALUE 999999999999999999999999999
   MINVALUE 1
   NOCYCLE
   CACHE 100
   NOORDER
//

DECLARE tbl_exist INTEGER; BEGIN SELECT COUNT (*) INTO tbl_exist FROM USER_TABLES WHERE table_name = 'SUPPLIER_CHECK_APE_REQUEST'; IF tbl_exist > 0 THEN EXECUTE IMMEDIATE 'DROP TABLE SUPPLIER_CHECK_APE_REQUEST CASCADE CONSTRAINTS'; EXECUTE IMMEDIATE 'DROP SEQUENCE SCAR_ID'; END IF; END;//

CREATE TABLE SUPPLIER_CHECK_APE_REQUEST
(
   SCAR_ID                          VARCHAR2 (255),
   SCCR_ID                          VARCHAR2 (255) NOT NULL,
   OPERATION_NAME                   VARCHAR2 (30),
   AVAIL_CHECK_TYPE                 VARCHAR2 (20),
   STATUS                           VARCHAR2 (20),
   CREATED_ON                       TIMESTAMP(6),
   UPDATED_ON                       TIMESTAMP(6)
)
//

ALTER TABLE SUPPLIER_CHECK_APE_REQUEST ADD CONSTRAINT SCAR_PK PRIMARY KEY(SCAR_ID)
//

ALTER TABLE SUPPLIER_CHECK_APE_REQUEST ADD CONSTRAINT SCAR_FK FOREIGN KEY(SCCR_ID) REFERENCES SUPPLIER_CHECK_CLIENT_REQUEST(SCCR_ID)
//

CREATE SEQUENCE SCAR_ID
   START WITH 1
   MAXVALUE 999999999999999999999999999
   MINVALUE 1
   NOCYCLE
   CACHE 100
   NOORDER
//

DECLARE tbl_exist INTEGER; BEGIN SELECT COUNT (*) INTO tbl_exist FROM USER_TABLES WHERE table_name = 'SUPPLIER_REQUEST_SITE'; IF tbl_exist > 0 THEN EXECUTE IMMEDIATE 'DROP TABLE SUPPLIER_REQUEST_SITE CASCADE CONSTRAINTS'; EXECUTE IMMEDIATE 'DROP SEQUENCE SRS_ID'; END IF; END;//

CREATE TABLE SUPPLIER_REQUEST_SITE
(
   SRS_ID                           NUMBER,
   SCCR_ID                          VARCHAR2 (255),
   SITE_ID                          VARCHAR2 (255),
   STATUS                           VARCHAR2 (20),
   SUB_STATUS                       VARCHAR2 (20),
   CREATED_ON                       TIMESTAMP(6),
   UPDATED_ON                       TIMESTAMP(6)
)
//

ALTER TABLE SUPPLIER_REQUEST_SITE ADD CONSTRAINT SRS_PK PRIMARY KEY(SRS_ID)
//

ALTER TABLE SUPPLIER_REQUEST_SITE ADD CONSTRAINT SRS_FK FOREIGN KEY(SCCR_ID) REFERENCES SUPPLIER_CHECK_CLIENT_REQUEST(SCCR_ID)
//

CREATE SEQUENCE SRS_ID
   START WITH 1
   MAXVALUE 999999999999999999999999999
   MINVALUE 1
   NOCYCLE
   CACHE 100
   NOORDER
//

DECLARE tbl_exist INTEGER; BEGIN SELECT COUNT (*) INTO tbl_exist FROM USER_TABLES WHERE table_name = 'SUPPLIER_REQUEST_SITE_SPAC'; IF tbl_exist > 0 THEN EXECUTE IMMEDIATE 'DROP TABLE SUPPLIER_REQUEST_SITE_SPAC CASCADE CONSTRAINTS'; EXECUTE IMMEDIATE 'DROP SEQUENCE SRSS_ID'; END IF; END;//

CREATE TABLE SUPPLIER_REQUEST_SITE_SPAC
(
   SRSS_ID                          NUMBER,
   SRS_ID                           NUMBER,
   SPAC_ID                          VARCHAR2 (255),
   STATUS                           VARCHAR2 (20),
   SUB_STATUS                       VARCHAR2 (20),
   CREATED_ON                       TIMESTAMP(6),
   UPDATED_ON                       TIMESTAMP(6)
)
//

ALTER TABLE SUPPLIER_REQUEST_SITE_SPAC ADD CONSTRAINT SRSS_PK PRIMARY KEY(SRSS_ID)
//

ALTER TABLE SUPPLIER_REQUEST_SITE_SPAC ADD CONSTRAINT SRSS_FK FOREIGN KEY(SRS_ID) REFERENCES SUPPLIER_REQUEST_SITE(SRS_ID)
//

CREATE SEQUENCE SRSS_ID
   START WITH 1
   MAXVALUE 999999999999999999999999999
   MINVALUE 1
   NOCYCLE
   CACHE 100
   NOORDER
//
