DECLARE
  vcount NUMBER;
  SELECT_STMT VARCHAR2(500);
  CURSOR sql_stmt IS SELECT 'CREATE SYNONYM ' || OBJECT_NAME || ' FOR ' || OWNER ||'.' ||  OBJECT_NAME || ';' FROM ALL_OBJECTS WHERE OWNER IN
        (SELECT USERNAME FROM DBA_USERS) AND OBJECT_TYPE='TABLE';
BEGIN
  SELECT COUNT(*) INTO vcount FROM dba_users WHERE username = UPPER('&1');
  IF vcount != 1
  THEN
    IF '&1' !=  'sqe_mis_reporting'
    THEN
       EXECUTE IMMEDIATE 'GRANT connect, resource, CREATE SYNONYM, CREATE VIEW to &1 identified by &2';
    else
        EXECUTE IMMEDIATE 'GRANT dba,connect,resource,CREATE DATABASE LINK,CREATE SYNONYM to &1 identified by &2 with ADMIN OPTION';
        open sql_stmt;
        LOOP
            FETCH sql_stmt INTO SELECT_STMT;
             EXECUTE IMMEDIATE SELECT_STMT;
            EXIT WHEN sql_stmt%NOTFOUND;
        END LOOP;
       close sql_stmt;
    end if;
  END IF;
END;
/