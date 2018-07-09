DECLARE
  vcount NUMBER;
BEGIN
  SELECT COUNT(*) INTO vcount FROM user_tables WHERE table_name = UPPER('CHANGELOG');
  IF vcount != 1
  THEN
    EXECUTE IMMEDIATE
    'CREATE TABLE changelog (
    change_number NUMBER(22,0) NOT NULL,
    complete_dt TIMESTAMP NOT NULL,
    applied_by VARCHAR2(100) NOT NULL,
    description VARCHAR2(500) NOT NULL)';
    EXECUTE IMMEDIATE 'ALTER TABLE changelog ADD CONSTRAINT PKchangelog PRIMARY KEY (change_number)';
  END IF;

END;
/