INSERT INTO APE_QREF_DETAIL (REQUEST_ID, QREF_ID, ATTRIBUTE_NAME, ATTRIBUTE_VALUE, SEQUENCE)
SELECT AQD.REQUEST_ID ,AQD.QREF_ID,'PLATFORM NAME',DECODE(AQD.ATTRIBUTE_VALUE,
                                                                  'Level 3 LATAM Argentina', 'IP Internet Access - harmonized-Level 3 LATAM',
                                                                  'Level 3 LATAM Chile','IP Internet Access - harmonized-Level 3 LATAM',
                                                                  'Level 3 LATAM Brazil','IP Internet Access - harmonized-Level 3 LATAM',
                                                                  'Level 3 LATAM Columbia','IP Internet Access - harmonized-Level 3 LATAM',
                                                                  'Level 3 LATAM','IP Internet Access - harmonized-Level 3 LATAM',
                                                                  'Level 3 LATAM Peru','IP Internet Access - harmonized-Level 3 LATAM',
                                                                  'Level 3 LATAM Venezuala','IP Internet Access - harmonized-Level 3 LATAM',
                                                                  'CenturyLink','IP Internet Access - harmonized-CenturyLink',
                                                                  'Telstra','IP Internet Access – harmonized-Telstra',
                                                                  'IP Internet Access - harmonized') AS ATTRIBUTE_VALUE ,AQD.SEQUENCE
FROM APE_QREF_DETAIL AQD
WHERE AQD.QREF_ID IN (SELECT DISTINCT QREF_ID FROM APE_QREF_DETAIL WHERE ATTRIBUTE_NAME = 'ACCESS SUPPLIER NAME (TELCO NAME)'
                                                                                                                MINUS SELECT DISTINCT QREF_ID FROM APE_QREF_DETAIL WHERE ATTRIBUTE_NAME ='PLATFORM NAME')
AND AQD.ATTRIBUTE_NAME = 'ACCESS SUPPLIER NAME (TELCO NAME)'//