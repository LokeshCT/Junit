CREATE TABLE LE_COUNTRY_REGION_VAT
  (
    "COUNTRY"    VARCHAR2(80 BYTE),
    "REGION"     VARCHAR2(8 BYTE),
    "VAT_PREFIX" VARCHAR2(8 BYTE),
    PRIMARY KEY ("COUNTRY") USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 TABLESPACE "USERS" ENABLE
  )
  //
 insert into LE_COUNTRY_REGION_VAT values('UNITED KINGDOM','UK','GB')
//
insert into LE_COUNTRY_REGION_VAT values('AUSTRIA','EU','AT')
//
insert into LE_COUNTRY_REGION_VAT values('BELGIUM','EU','BE')
//
insert into LE_COUNTRY_REGION_VAT values('BULGARIA','EU','BG')
//
insert into LE_COUNTRY_REGION_VAT values('CROATIA','EU','HR')
//
insert into LE_COUNTRY_REGION_VAT values('CYPRUS','EU','CY')
//
insert into LE_COUNTRY_REGION_VAT values('CZECH REPUBLIC','EU','CZ')
//
insert into LE_COUNTRY_REGION_VAT values('DENMARK','EU','DK')
//
insert into LE_COUNTRY_REGION_VAT values('ESTONIA','EU','EE')
//
insert into LE_COUNTRY_REGION_VAT values('FINLAND','EU','FI')
//
insert into LE_COUNTRY_REGION_VAT values('FRANCE','EU','FR')
//
insert into LE_COUNTRY_REGION_VAT values('GERMANY','EU','DE')
//
insert into LE_COUNTRY_REGION_VAT values('GREECE','EU','EL')
//
insert into LE_COUNTRY_REGION_VAT values('HUNGARY','EU','HU')
//
insert into LE_COUNTRY_REGION_VAT values('IRELAND','EU','IE')
//
insert into LE_COUNTRY_REGION_VAT values('ITALY','EU','IT')
//
insert into LE_COUNTRY_REGION_VAT values('LATVIA','EU','LV')
//
insert into LE_COUNTRY_REGION_VAT values('LITHUANIA','EU','LT')
//
insert into LE_COUNTRY_REGION_VAT values('LUXEMBOURG','EU','LU')
//
insert into LE_COUNTRY_REGION_VAT values('MALTA','EU','MT')
//
insert into LE_COUNTRY_REGION_VAT values('POLAND','EU','PL')
//
insert into LE_COUNTRY_REGION_VAT values('PORTUGAL','EU','PT')
//
insert into LE_COUNTRY_REGION_VAT values('ROMANIA','EU','RO')
//
insert into LE_COUNTRY_REGION_VAT values('SLOVAKIA','EU','SK')
//
insert into LE_COUNTRY_REGION_VAT values('SLOVENIA','EU','SI')
//
insert into LE_COUNTRY_REGION_VAT values('SPAIN','EU','ES')
//
insert into LE_COUNTRY_REGION_VAT values('SWEDEN','EU','SE')
//
insert into LE_COUNTRY_REGION_VAT values('NETHERLANDS','EU','NL')
//


