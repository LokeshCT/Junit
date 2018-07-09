DELETE FROM availability_type
      WHERE availability_type_id IN (1, 2, 3, 4, 5, 6)
//

INSERT INTO availability_type
     VALUES (
               1,
               'Not Known/Supported',
               'Grey',
               'DSL/EFM service availability is not known for this site, click on this icon to navigate to bundling page to confirm availability')
//

INSERT INTO availability_type
     VALUES (2,
             'Not Supported',
             'Light Grey with red cross',
             'DSL/EFM services are currently not available for  this site.')
//

INSERT INTO availability_type
     VALUES (3,
             'Available',
             'Blue',
             'Supplier Product information has been retrieved please click on this icon to check supplier availability.')
//

INSERT INTO availability_type
     VALUES (4,
             'SP Available',
             'Green',
             'One or more DSL/EFM suppliers has confirmed product availability at this site.')
//

INSERT INTO availability_type
     VALUES (
               5,
               'Failed',
               'Red',
               'DSL/EFM service availability check has Failed – Please contact the helpdesk.')
//

INSERT INTO availability_type
     VALUES (
               6,
               'Expired',
               'Orange',
               'DSL/EFM Supplier availability information has expired, click on this icon to navigate to bundling page and re-trigger the request for updated Supplier availability')
//