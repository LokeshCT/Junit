DECLARE
  vcount NUMBER;
BEGIN
  SELECT COUNT(*) INTO vcount FROM dba_users WHERE username = UPPER('&1');
  IF vcount != 1
  THEN
    EXECUTE IMMEDIATE 'GRANT dba,connect,resource to &1 identified by &2 with ADMIN OPTION';
  END IF;

END;
/
