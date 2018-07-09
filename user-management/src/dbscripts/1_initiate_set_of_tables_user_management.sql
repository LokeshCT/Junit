create table ROLE_TYPE_MASTER
(
  ROLE_TYPE_ID   NUMBER not null,
  ROLE_TYPE_NAME VARCHAR2(50) not null,
  CREATED_USER     VARCHAR2(25),
  CREATED_DATE   TIMESTAMP(6),
  MODIFIED_USER    VARCHAR2(25),
  MODIFIED_DATE  TIMESTAMP(6)
)//

-- Create/Recreate primary, unique and foreign key constraints 
alter table ROLE_TYPE_MASTER
  add constraint PK_ROLE_TYPE_ID primary key (ROLE_TYPE_ID)//

-- Create table
create table ROLE_GROUP_MASTER
(
  ROLE_GROUP_ID     NUMBER not null,
  ROLE_GROUP_NAME   VARCHAR2(50) not null,
  ROLE_GROUP_DESC   VARCHAR2(200) not null,
  ACTIVE            CHAR(1) not null,
  CREATED_USER		VARCHAR2(25),
  CREATED_DATE		TIMESTAMP(6),
  MODIFIED_USER		VARCHAR2(25),
  MODIFIED_DATE		TIMESTAMP(6)
)//

-- Create/Recreate primary, unique and foreign key constraints 
alter table ROLE_GROUP_MASTER
  add constraint PK_ROLE_GROUP_ID primary key (ROLE_GROUP_ID)//


-- Create table
create table USER_MASTER
(
  USER_ID           VARCHAR2(25) not null,
  USER_NAME         VARCHAR2(100) not null,
  FIRST_NAME        VARCHAR2(50),
  LAST_NAME         VARCHAR2(50),
  JOB_TITLE         VARCHAR2(100),
  EMAIL_ID	        VARCHAR2(50),
  PHONE_NUMBER	    VARCHAR2(100),
  ROLE_TYPE_ID      NUMBER  default (1) not null,
  ACTIVE            CHAR(1) not null,
  LAST_LOGIN        TIMESTAMP(6),
  LOCATION	        VARCHAR2(150),
  MOBILE	        VARCHAR2(50),
  CREATED_USER      VARCHAR2(25),
  CREATED_DATE      TIMESTAMP(6),
  MODIFIED_USER     VARCHAR2(25),
  MODIFIED_DATE     TIMESTAMP(6)

)//

-- Create/Recreate primary, unique and foreign key constraints
alter table USER_MASTER
  add constraint PK1_USER_ID primary key (USER_ID)//

alter table USER_MASTER
  add constraint FK_ROLE_TYPE_ID foreign key (ROLE_TYPE_ID)
  references ROLE_TYPE_MASTER (ROLE_TYPE_ID)//

-- Create table
create table ROLE_MASTER
(
  ROLE_ID		    NUMBER not null,
  ROLE_NAME		    VARCHAR2(50) not null,
  ROLE_GROUP_ID     NUMBER not null,
  ROLE_TYPE_ID		NUMBER default (1) not null,
  ACTIVE            CHAR(1) not null,
  CREATED_USER		VARCHAR2(25),
  CREATED_DATE		TIMESTAMP(6),
  MODIFIED_USER		VARCHAR2(25),
  MODIFIED_DATE		TIMESTAMP(6)
)//

-- Create/Recreate primary, unique and foreign key constraints
alter table ROLE_MASTER
  add constraint PK_ROLE_ID primary key (ROLE_ID)//

alter table ROLE_MASTER
  add constraint FK_ROLE_GROUP_ID foreign key (ROLE_GROUP_ID)
  references ROLE_GROUP_MASTER (ROLE_GROUP_ID)//

alter table ROLE_MASTER
  add constraint FK_ROLE_TYPE_ID_1 foreign key (ROLE_TYPE_ID)
  references ROLE_TYPE_MASTER (ROLE_TYPE_ID)//

-- Create table
create table USER_ROLE
(
  USER_ID       VARCHAR2(25) not null,
  ROLE_ID       NUMBER not null,
  CREATED_USER    VARCHAR2(25),
  CREATED_DATE  TIMESTAMP(6),
  DEFAULT_ROLE  CHAR(1) default 'F'
)//

-- Create/Recreate primary, unique and foreign key constraints 
alter table USER_ROLE
  add constraint PK_USER_ROLE_ID primary key (USER_ID, ROLE_ID)//

alter table USER_ROLE
  add constraint FK1_USER_ID foreign key (USER_ID)
  references USER_MASTER (USER_ID)//

alter table USER_ROLE
  add constraint FK_ROLE_ID foreign key (ROLE_ID)
  references ROLE_MASTER (ROLE_ID)//


-- Create table
create table SALES_CHANNEL_MASTER
(
  SALES_CHANNEL_ID	NUMBER(10) not null,
  SALES_CHANNEL_NAME	VARCHAR2(100) not null,
  ROLE_TYPE_ID		NUMBER(10) default 1, 
  CREATED_USER		VARCHAR2(25),
  CREATED_DATE		TIMESTAMP(6),
  MODIFIED_USER		VARCHAR2(25),
  MODIFIED_DATE		TIMESTAMP(6),
  GFR_CODE		NUMBER(38)
)//

-- Create/Recreate primary, unique and foreign key constraints 
alter table SALES_CHANNEL_MASTER
  add constraint PK_SALES_CHANNEL_ID primary key (SALES_CHANNEL_ID)//

alter table SALES_CHANNEL_MASTER
  add constraint FK_ROLE_TYPE_ID_2 foreign key (ROLE_TYPE_ID)
  references ROLE_TYPE_MASTER (ROLE_TYPE_ID)//

-- Create table
create table USER_SALES_CHANNEL
(
  USER_ID               VARCHAR2(25) not null,
  SALES_CHANNEL_ID      VARCHAR2(100) not null,
  CREATED_DATE          TIMESTAMP(6),
  CREATED_USER          VARCHAR2(25) not null,
  MODIFIED_DATE         TIMESTAMP(6),
  MODIFIED_USER         VARCHAR2(25),
  DEFAULT_SALES_CHANNEL CHAR(1) default 'N' not null
)//
