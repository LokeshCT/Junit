BEGIN
    EXECUTE IMMEDIATE 'DROP USER &1 CASCADE';
END;
/