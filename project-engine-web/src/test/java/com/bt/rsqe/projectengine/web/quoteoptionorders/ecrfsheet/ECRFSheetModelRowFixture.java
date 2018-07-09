package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;

public class ECRFSheetModelRowFixture {

    private ECRFSheetModelRow ecrfSheetModelRow;

    public ECRFSheetModelRowFixture() {
        this.ecrfSheetModelRow = new ECRFSheetModelRow();
    }

    public static ECRFSheetModelRowFixture aECRFSheetModelRow() {
        return new ECRFSheetModelRowFixture();
    }


    public ECRFSheetModelRowFixture withAttributes(ArrayList<ECRFSheetModelAttribute> ecrfSheetModelAttributes) {
        this.ecrfSheetModelRow.setAttributes(ecrfSheetModelAttributes);
        return this;
    }

    public ECRFSheetModelRow build() {
        return this.ecrfSheetModelRow;
    }

    public ECRFSheetModelRowFixture withRowId(String sheetId) {
        this.ecrfSheetModelRow.setRowId(sheetId);
        return this;
    }

    public ECRFSheetModelRowFixture withParentId(String parentId) {
        this.ecrfSheetModelRow.setParentId(parentId);
        return this;
    }
	
	public ECRFSheetModelRowFixture withSheetName(String sheetName) {
        this.ecrfSheetModelRow.setSheetName(sheetName);
        return this;
    }

    public ECRFSheetModelRowFixture withOwnerProductId(String ownerProductId) {
        this.ecrfSheetModelRow.setOwnerId(ownerProductId);
        return this;
    }

    public ECRFSheetModelRowFixture withRelatedToId(String relatedProductRowId) {
        this.ecrfSheetModelRow.setRelatedToId(relatedProductRowId);
        return this;
    }

    public ECRFSheetModelRowFixture withRelationShipName(String relationShipName) {
        this.ecrfSheetModelRow.setRelationshipName(relationShipName);
        return this;
    }

    public static ECRFSheetModelRow aDeliveryAddressModelRow(String productRowId) {
        return ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                       .withAttributes(newArrayList(
                                           new ECRFSheetModelAttribute("Address ID", productRowId),
                                           new ECRFSheetModelAttribute("First Name", "aFirstName"),
                                           new ECRFSheetModelAttribute("Last Name", "aLastName"),
                                           new ECRFSheetModelAttribute("Job Title", "aJobTile"),
                                           new ECRFSheetModelAttribute("Sub Building", "aSubBuilding"),
                                           new ECRFSheetModelAttribute("Building", "aBuilding"),
                                           new ECRFSheetModelAttribute("Building Number", "aBuildingNumber"),
                                           new ECRFSheetModelAttribute("Sub Street", "aSubStreet"),
                                           new ECRFSheetModelAttribute("Street", "aStreet"),
                                           new ECRFSheetModelAttribute("Sub Locality", "aSubLocality"),
                                           new ECRFSheetModelAttribute("Locality", "aLocality"),
                                           new ECRFSheetModelAttribute("Country", "aCountry"),
                                           new ECRFSheetModelAttribute("City", "aCity"),
                                           new ECRFSheetModelAttribute("Sub State", "aSubState"),
                                           new ECRFSheetModelAttribute("State/Province", "aState"),
                                           new ECRFSheetModelAttribute("Sub Zip Code", "aSubZipCode"),
                                           new ECRFSheetModelAttribute("Zip Code", "aZipCode"),
                                           new ECRFSheetModelAttribute("PO Box", "aPoBox"),
                                           new ECRFSheetModelAttribute("Phone Number", "aPhoneNumber"),
                                           new ECRFSheetModelAttribute("Mobile Number", "aMobileNumber"),
                                           new ECRFSheetModelAttribute("Fax", "aFax"),
                                           new ECRFSheetModelAttribute("Email", "aEmail"))).build();
    }
}
