
create table ROLE_TYPE_CONFIG
(
  ROLE_TYPE_ID   INTEGER not null,
  ROLE_TYPE_NAME VARCHAR2(50) not null,
  ACTIVE         CHAR(1) not null,
  CREATED_DATE   TIMESTAMP(6),
  CREATED_USER   VARCHAR2(25),
  MODIFIED_DATE  TIMESTAMP(6),
  MODIFIED_USER  VARCHAR2(25)
)
//

alter table ROLE_TYPE_CONFIG add constraint PK_GROUP_CONFIG primary key (ROLE_TYPE_ID)
  using index
  //


create table SALES_CHANNEL
(
  SALES_CHANNEL_ID   INTEGER not null,
  SALES_CHANNEL_NAME VARCHAR2(100) not null,
  CREATED_DATE       TIMESTAMP(6),
  CREATED_USER       VARCHAR2(25) not null,
  MODIFIED_DATE      TIMESTAMP(6),
  MODIFIED_USER      VARCHAR2(25)
)
//

alter table SALES_CHANNEL
  add constraint PK_SALES_CHANNEL primary key (SALES_CHANNEL_ID)
  using index
  //


create table USER_AUTHORIZATION
(
  USER_ID       VARCHAR2(25) not null,
  USER_NAME     VARCHAR2(100) not null,
  ROLE_TYPE_ID  INTEGER not null,
  ACTIVE        CHAR(1) not null,
  CREATED_DATE  TIMESTAMP(6),
  CREATED_USER  VARCHAR2(25) not null,
  MODIFIED_DATE TIMESTAMP(6),
  MODIFIED_USER VARCHAR2(25)
)
//

alter table USER_AUTHORIZATION
  add constraint PK_USER_CONFIG primary key (USER_ID)
  using index
  //

alter table USER_AUTHORIZATION
  add constraint FK1_USER_CONFIG foreign key (ROLE_TYPE_ID)
  references ROLE_TYPE_CONFIG (ROLE_TYPE_ID)
  //



create table USER_ROLE_MASTER
(
  ROLE_ID       INTEGER not null,
  ROLE_NAME     VARCHAR2(50) not null,
  CREATED_DATE  TIMESTAMP(6),
  CREATED_USER  VARCHAR2(25) not null,
  MODIFIED_DATE TIMESTAMP(6),
  MODIFIED_USER VARCHAR2(25)
)
//

alter table USER_ROLE_MASTER
  add constraint PK_ROLE_CONFIG primary key (ROLE_ID)
  using index
  //


create table USER_ROLE_CONFIG
(
  USER_ID       VARCHAR2(25) not null,
  ROLE_ID       INTEGER not null,
  CREATED_DATE  TIMESTAMP(6),
  CREATED_USER  VARCHAR2(25) not null,
  MODIFIED_DATE TIMESTAMP(6),
  MODIFIED_USER VARCHAR2(25),
  DEFAULT_ROLE  CHAR(1) default 'F' not null
)
//


alter table USER_ROLE_CONFIG
  add constraint PK_USER_ROLE_CONFIG primary key (USER_ID, ROLE_ID)
  using index
  //

alter table USER_ROLE_CONFIG
  add constraint FK1_USER_ROLE_CONFIG foreign key (USER_ID)
  references USER_AUTHORIZATION (USER_ID)
  //

alter table USER_ROLE_CONFIG
  add constraint FK2_USER_ROLE_CONFIG foreign key (ROLE_ID)
  references USER_ROLE_MASTER (ROLE_ID)
  //



create table USER_SALES_CHANNEL
(
  USER_ID               VARCHAR2(25) not null,
  SALES_CHANNEL         VARCHAR2(100) not null,
  CREATED_DATE          TIMESTAMP(6),
  CREATED_USER          VARCHAR2(25) not null,
  MODIFIED_DATE         TIMESTAMP(6),
  MODIFIED_USER         VARCHAR2(25),
  DEFAULT_SALES_CHANNEL CHAR(1) default 'N' not null
)
//



