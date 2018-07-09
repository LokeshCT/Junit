package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.bt.rsqe.projectengine.DeliveryAddressDTO;

public class DeliveryAddressExtractor extends AbstractAdditionalDetailExtractor<DeliveryAddressDTO> {
    @ECRFSheetMapping(name = "First Name", allowNull = false)
    public String firstName;
    @ECRFSheetMapping(name = "Last Name", allowNull = false)
    public String lastName;
    @ECRFSheetMapping(name = "Job Title", allowNull = false)
    protected String jobTitle;
    @ECRFSheetMapping(name = "Sub Building")
    protected String subBuilding;
    @ECRFSheetMapping(name = "Building")
    protected String building;
    @ECRFSheetMapping(name = "Building Number")
    protected String buildingNumber;
    @ECRFSheetMapping(name = "Sub Street")
    protected String subStreet;
    @ECRFSheetMapping(name = "Street")
    protected String street;
    @ECRFSheetMapping(name = "Sub Locality")
    protected String subLocality;
    @ECRFSheetMapping(name = "Locality")
    protected String locality;
    @ECRFSheetMapping(name = "Country", allowNull = false)
    protected String country;
    @ECRFSheetMapping(name = "City", allowNull = false)
    protected String city;
    @ECRFSheetMapping(name = "Sub State")
    protected String subState;
    @ECRFSheetMapping(name = "State/Province", allowNull = false)
    protected String state;
    @ECRFSheetMapping(name = "Sub Zip Code")
    protected String subZipCode;
    @ECRFSheetMapping(name = "Zip Code")
    protected String zipCode;
    @ECRFSheetMapping(name = "PO Box")
    protected String poBox;
    @ECRFSheetMapping(name = "Phone Number", allowNull = false)
    protected String phoneNumber;
    @ECRFSheetMapping(name = "Mobile Number")
    protected String mobileNumber;
    @ECRFSheetMapping(name = "Fax")
    protected String fax;
    @ECRFSheetMapping(name = "Email", allowNull = false)
    protected String email;
    private String mappingAttribute;


    public DeliveryAddressExtractor(String input, ECRFWorkBook workBook, String mappingAttribute) {
        this.mappingAttribute = mappingAttribute;
        this.workBook = workBook;
        this.input = input;
    }

    public DeliveryAddressExtractor() {
        //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public String getSheetName() {
        return "Delivery Address";
    }

    @Override
    public DeliveryAddressDTO execute() {
        DeliveryAddressExtractor extractedValues = null;
        ECRFSheet sheet = getSheetByName(workBook);
        if (null == sheet) {
            throw new ECRFImportException(String.format(ECRFImportException.sheetNotFoundInWorkBook, getSheetName()));
        }
        if (null != sheet) {
            ECRFSheetModelRow row = pickRowToEvaluate(sheet, mappingAttribute);
            if (null == row) {
                throw new ECRFImportException(String.format(ECRFImportException.deliveryAddressDetailsNotAvailable, input));
            }
            extractedValues = extractData(row, sheet.getSheetName());
        }
        return extractedValues.mapToEntity();
    }

    @Override
    public DeliveryAddressDTO mapToEntity() {
        DeliveryAddressDTO deliveryAddress = new DeliveryAddressDTO();
        deliveryAddress.setSubLocality(this.subLocality);
        deliveryAddress.setSubStreet(this.subStreet);
        deliveryAddress.setBuilding(this.building);
        deliveryAddress.setBuildingNumber(this.buildingNumber);
        deliveryAddress.setCity(this.city);
        deliveryAddress.setCountry(this.country);
        deliveryAddress.setEmail(this.email);
        deliveryAddress.setFax(this.fax);
        deliveryAddress.setFirstName(this.firstName);
        deliveryAddress.setJobTitle(this.jobTitle);
        deliveryAddress.setLastName(this.lastName);
        deliveryAddress.setLocality(this.locality);
        deliveryAddress.setMobileNumber(this.mobileNumber);
        deliveryAddress.setPhoneNumber(this.phoneNumber);
        deliveryAddress.setPoBox(this.poBox);
        deliveryAddress.setState(this.state);
        deliveryAddress.setStreet(this.street);
        deliveryAddress.setSubBuilding(this.subBuilding);
        deliveryAddress.setSubState(this.subState);
        deliveryAddress.setSubZipCode(this.subZipCode);
        deliveryAddress.setZipCode(this.zipCode);
        return deliveryAddress;
    }

    @Override
    public ECRFSheetModelRow pickRowToEvaluate(ECRFSheet sheet, String mappingAttribute) {
        for (ECRFSheetModelRow row : sheet.getRows()) {
            if (row.getAttributeByName(mappingAttribute).getValue().equals(input)) {
                return row;
            }
        }
        return null;
    }

    private ECRFSheet getSheetByName(ECRFWorkBook workBook) {
        for (ECRFSheet ecrfSheet : workBook.getNonProductSheets()) {
            if (ecrfSheet.getSheetName().equals(getSheetName())) {
                return ecrfSheet;
            }
        }
        return null;
    }
}
