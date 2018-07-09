package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.bt.rsqe.projectengine.DeliveryAddressDTO;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.google.common.collect.Lists.newArrayList;
import static junit.framework.Assert.*;

public class DeliveryAddressExtractorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldReturnDeliveryAddressDTO() {
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode("ProductCode")
                                                   .withSheetName("Delivery Address")
                                                   .withRow(newDeliveryAddressRow("aRowId", "aFirstName"))
                                                   .withRow(newDeliveryAddressRow("aRowId2", "aFirstName"))
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withNonProductECRFSheets(newArrayList(ecrfSheetModel)).build();

        DeliveryAddressExtractor deliveryAddressExtractor = new DeliveryAddressExtractor("aRowId", ecrfWorkBook, "Address ID");
        DeliveryAddressDTO dto = deliveryAddressExtractor.execute();
        assertNotNull(dto);
        assertTrue(dto.building.equals("aBuilding"));
    }

    @Test
    public void shouldThrowExceptionWhenDeliveryAddressSheetNotPresentInWorkBook() {
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode("ProductCode")
                                                   .withSheetName("some other sheet")
                                                   .withRow(newDeliveryAddressRow("aRowId", "aFirstName"))
                                                   .withRow(newDeliveryAddressRow("aRowId2", "aFirstName"))
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).build();
        exception.expect(ECRFImportException.class);
        exception.expectMessage(String.format(ECRFImportException.sheetNotFoundInWorkBook, "Delivery Address"));
        DeliveryAddressExtractor deliveryAddressExtractor = new DeliveryAddressExtractor("aRowId", ecrfWorkBook, "Address ID");
        DeliveryAddressDTO dto = deliveryAddressExtractor.execute();
    }

    @Test
    public void shouldThrowExceptionWhenMatchingRowNotFoundForID() {
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode("ProductCode")
                                                   .withSheetName("Delivery Address")
                                                   .withRow(newDeliveryAddressRow("aRowId", "aFirstName"))
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withNonProductECRFSheets(newArrayList(ecrfSheetModel)).build();

        exception.expect(ECRFImportException.class);
        exception.expectMessage(String.format(ECRFImportException.deliveryAddressDetailsNotAvailable, "otherRowId"));
        DeliveryAddressExtractor deliveryAddressExtractor = new DeliveryAddressExtractor("otherRowId", ecrfWorkBook, "Address ID");
        DeliveryAddressDTO dto = deliveryAddressExtractor.execute();
    }


    @Test
    public void shouldThrowExceptionWhenMandatoryRowIsNull() {
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode("ProductCode")
                                                   .withSheetName("Delivery Address")
                                                   .withRow(newDeliveryAddressRow("aRowId", ""))
                                                   .withRow(newDeliveryAddressRow("aRowId2", ""))
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withNonProductECRFSheets(newArrayList(ecrfSheetModel)).build();

        exception.expect(ECRFImportException.class);
        exception.expectMessage(String.format(ECRFImportException.additionalDetailAttributeIsNull, "First Name", "Delivery Address"));
        DeliveryAddressExtractor deliveryAddressExtractor = new DeliveryAddressExtractor("aRowId", ecrfWorkBook, "Address ID");
        DeliveryAddressDTO dto = deliveryAddressExtractor.execute();
    }

    private ECRFSheetModelRow newDeliveryAddressRow(String rowId, String firstName) {
        return ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                       .withRowId(rowId)
                                       .withAttributes(newArrayList(
                                           new ECRFSheetModelAttribute("Address ID", rowId),
                                           new ECRFSheetModelAttribute("First Name", firstName),
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
