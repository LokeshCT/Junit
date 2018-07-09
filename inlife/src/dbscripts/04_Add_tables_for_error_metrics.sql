CREATE TABLE EXCEPTION_POINT_DIMENSION (
    ID NUMBER NOT NULL,
    EXCEPTION_POINT VARCHAR2(30) NOT NULL,
    CONSTRAINT EXCEPTION_POINT_DIMENSION_PK PRIMARY KEY (ID) USING INDEX
)
//

CREATE TABLE USER_DIMENSION (
    ID NUMBER NOT NULL,
    USER_IDENTIFIER VARCHAR2(30) NOT NULL,
    TYPE VARCHAR2(20),
    SALES_CHANEL VARCHAR2(30),
    CONSTRAINT USER_DIMENSION_PK PRIMARY KEY (ID) USING INDEX
)
//

CREATE TABLE ERROR_FACT (
    ID NUMBER NOT NULL,
    TIME_STAMP TIMESTAMP NOT NULL,
    USER_ID NUMBER NOT NULL,
    EXCEPTION_POINT_ID NUMBER NOT NULL,
    QUOTE_OPTION_ID VARCHAR2(100) NOT NULL,
    QUOTE_LINE_ITEM_ID VARCHAR2(100) NOT NULL,
    ERROR_MESSAGE VARCHAR2(500),
    URL VARCHAR2(300),
    CONSTRAINT ERROR_FACT_PK PRIMARY KEY (ID) USING INDEX
)
//

CREATE SEQUENCE EXCEPTION_POINT_DIMENSION_SEQ START WITH 1
//

CREATE SEQUENCE USER_DIMENSION_SEQ START WITH 1
//

CREATE SEQUENCE ERROR_FACT_SEQ START WITH 1
//

ALTER TABLE ERROR_FACT ADD CONSTRAINT FK1_ERROR_FACT FOREIGN KEY (USER_ID) REFERENCES USER_DIMENSION (ID)
//

ALTER TABLE ERROR_FACT ADD CONSTRAINT FK2_ERROR_FACT FOREIGN KEY (EXCEPTION_POINT_ID) REFERENCES EXCEPTION_POINT_DIMENSION (ID)
//

ALTER TABLE USER_DIMENSION ADD CONSTRAINT USER_IDENTIFIER_CNST UNIQUE("USER_IDENTIFIER")
//

ALTER TABLE EXCEPTION_POINT_DIMENSION ADD CONSTRAINT EXCEPTION_POINT_CNST UNIQUE("EXCEPTION_POINT")
//