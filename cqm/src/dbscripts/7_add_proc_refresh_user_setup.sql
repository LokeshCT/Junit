CREATE OR REPLACE PROCEDURE REFRESH_USER_SETUP IS

CURSOR IPSDK_ROLES IS SELECT DISTINCT USER_ROLES FROM  EXP__IPSDK__USER_ROLES where user_name is not null ORDER BY USER_ROLES;
cursor sales_channels is select distinct sales_channel, int_sales_channel_id from  EXP__IPSDK__USERCONFIGURATION where int_sales_channel_id is not null and delete_flag is not null;
cursor ipsdk_users is select distinct user_id from EXP__IPSDK__USERCONFIGURATION where user_id is not null and delete_flag is not null;
CURSOR ipsdk_user_roles(userId varchar2) IS SELECT USER_ROLES FROM  EXP__IPSDK__USER_ROLES where user_name = userId;
cursor ipsdk_user_channels(userId varchar2) is select sales_channel from  EXP__IPSDK__USERCONFIGURATION where user_id = userId and sales_channel is not null;

L_USER_ROLE VARCHAR2(100);
l_count number;
c_user varchar2(15);
l_expedio_user_id varchar2(50);
l_ein varchar2(15);
l_user_name varchar2(70);
l_role_type number;
l_sales_channel varchar2(150);
l_sales_channel_id number;
l_role_id number;
BEGIN

c_user :=  'Refresh_script';

OPEN IPSDK_ROLES;
LOOP
FETCH IPSDK_ROLES INTO L_USER_ROLE;
EXIT WHEN ipsdk_roles%NOTFOUND;
SELECT COUNT(ROLE_ID) INTO L_COUNT FROM USER_ROLE_MASTER WHERE ROLE_NAME = L_USER_ROLE;

IF L_COUNT = 0 THEN 
  INSERT INTO USER_ROLE_MASTER (ROLE_ID, ROLE_NAME, CREATED_DATE, CREATED_USER, MODIFIED_DATE, MODIFIED_USER)
  VALUES (ROLE_MASTER_SEQ.NEXTVAL, L_USER_ROLE, SYSDATE, c_user, SYSDATE, c_user);
END IF;

end loop;
close IPSDK_ROLES;

/*
open sales_channels;
loop
fetch sales_channels into l_sales_channel, l_sales_channel_id;
EXIT WHEN sales_channels%NOTFOUND;
	select count(1) into l_count from sales_channel where sales_channel_name = l_sales_channel;
	dbms_output.put_line('Sales channel ' || l_sales_channel || ' exist ? ' || l_count);
	if l_count = 0 then
		insert into sales_channel (sales_channel_id, sales_channel_name, created_date, created_user, modified_date, modified_user)
		values(l_sales_channel_id, l_sales_channel, sysdate, c_user, sysdate, c_user);
	end if;
end loop;
close sales_channels;
*/


open ipsdk_users;
loop
fetch ipsdk_users into l_expedio_user_id;
EXIT WHEN ipsdk_users%NOTFOUND;
begin
	select ein, full_name, decode(role_type, 0, 1, 1, 2, 1) into l_ein, l_user_name, l_role_type from POM__CFG__USER_DETAILS where login_name = l_expedio_user_id;
	if l_ein is not null then
		select count(1) into l_count from user_authorization where user_id = l_ein;
		
		if l_count = 0 then 
			dbms_output.put_line('User to insert : ' || l_expedio_user_id);		
			insert into user_authorization (USER_ID,USER_NAME,ROLE_TYPE_ID,CREATED_DATE,CREATED_USER,MODIFIED_DATE,MODIFIED_USER, ACTIVE) 
			values(l_ein, l_user_name, l_role_type, sysdate, c_user, sysdate, c_user, 'Y');
		else
			update user_authorization set user_name = l_user_name, role_type_id = l_role_type, modified_date = sysdate, modified_user = c_user
			where user_id = l_ein;
		end if;
		
		open ipsdk_user_roles(l_expedio_user_id);
		loop
		fetch ipsdk_user_roles into l_user_role;
			EXIT WHEN ipsdk_user_roles%NOTFOUND;
			select count(1) into l_count from user_role_config where user_id = l_ein;
			select role_id into l_role_id from user_role_master where role_name = l_user_role;
			if l_count = 0 then
				insert into user_role_config (user_id, role_id, created_date, created_user, modified_date, modified_user)
				values(l_ein, l_role_id, sysdate, c_user, sysdate, c_user);
			end if;
		end loop;
		close ipsdk_user_roles;
		
		open ipsdk_user_channels(l_expedio_user_id);
		loop
		fetch ipsdk_user_channels into l_sales_channel;
		EXIT WHEN ipsdk_user_channels%NOTFOUND;
		select count(1) into l_count from user_sales_channel where user_id = l_ein and sales_channel = l_sales_channel;
		if l_count = 0 then
			insert into user_sales_channel (user_id, sales_channel, created_date, created_user, modified_date, modified_user)
			values (l_ein, l_sales_channel, sysdate, c_user, sysdate, c_user);
		end if;
		end loop;
		close ipsdk_user_channels;
	end if;
	
exception 
when no_data_found then
	dbms_output.put_line('User not found : ' || l_expedio_user_id);
	if ipsdk_user_roles%ISOPEN then 
		close ipsdk_user_roles;
	end if;
	if ipsdk_user_channels%ISOPEN then 
		close ipsdk_user_channels;
	end if;
when others then
	dbms_output.put_line('Error for : ' || l_expedio_user_id || '. Details:' || SQLERRM);	
	if ipsdk_user_roles%ISOPEN then 
		close ipsdk_user_roles;
	end if;
	if ipsdk_user_channels%ISOPEN then 
		close ipsdk_user_channels;
	end if;
end;
end loop;
close ipsdk_users;

commit;

END;
//
