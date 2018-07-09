package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

public class ECRFImportException extends RuntimeException {

    public ECRFImportException(String message) {
        super(message);
    }

    public static final String workSheetNotFound = "Unable to find sheet \"%s\" within the import file";

    public static final String controlSheetDoesNotContainSCode = "Could not find a worksheet in the import file for the selected product.";

    public static final String siteIdValidation ="Site Id in the import file does not match the site id of the selected quote item";

    public static final String ownerInstanceNotFound = "Possible error while creating owner product \"%s\", so relationship not created";
    public static final String relatedInstanceNotFound = "Possible error while creating related product \"%s\", so relationship not created";
    public static final String ownerIdIsEmpty = "Owner Id should not be null for relationship creation";
    public static final String relationshipNotExist="Relationship with name \"%s\" does not exist.";
    public static final String relationshipNameIsEmpty = "Relationship name should not be null for relationship creation";
    public static final String relatedToIdIsEmpty = "Owner Id should not be null for relationship creation";
    public static final String workSheetNotFoundForIndex = "Unable to find sheet with index \"%s\" within the import file";
    public static final String controlSheetNotFound = "Excel sheet is not in correct format for importing. Please ensure the sheet selected " +
                                                      "has been generated specifically for importing into SQE.";

    public static final String duplicateParentIdsFoundInSheet = "Duplicate identifier of \"%s\" found on \"%s\" sheet.";

    public static final String attributeNotFoundInWorkSheet = "\"%s\" column is not present on \"%s\" sheet.";

    public static final String siteIdNotFoundInWorkSheet = "Not all rows on the \"%s\" sheet contain Site Ids.";

    public static final String siteIdNotFoundForCustomer = "Site Id : \"%s\" for this Customer cannot be found";
    public static final String attributeDataTypeMisMatch = "Cannot store \"%s\" for \"%s\" from \"%s\" sheet from row \"%s\" as a \"%s\".";
    public static final String valueNotAllowedInAllowedValues = "\"%s\" for \"%s\" from \"%s\" sheet in row \"%s\" is not an acceptable value. Allowed values: [%s]";
    public static final String parentIdNotFound = "Unable to find Parent Id \"%s\" from \"%s\" sheet, within Ids found in work book.";
    public static final String stencilMissingForStencilProduct = "No stencil specified for \"%s\" in row id: \"%s\".";
    public static final String stencilValueNotInProductOffering = "Stencil \"%s\" not applicable for \"%s\" in row \"%s\".";
    public static final String successfulImportAlreadyComplete = "Import already complete for the selected product.";
    public static final String maximumLengthExceeded = "Maximum length allowed for attribute \"%s\" is \"%s\", but attempting to set \"%s\", from sheet \"%s\" , from row \"%s\"";
    public static final String minimumLengthExceeded = "Minimum length allowed for attribute \"%s\" is \"%s\", but attempting to set \"%s\", from sheet \"%s\" , from row \"%s\"";
    public static final String sheetNotFoundInWorkBook = "Sheet \"%s\" not present in the Workbook";
    public static final String deliveryAddressDetailsNotAvailable = "Delivery Address not available in sheet for ID \"%s\"";
    public static final String additionalDetailAttributeIsNull = "Value not expected to be null for attribute \"%s\" in \"%s\" sheet";
    public static final String notAValidNumber="\"%s\" is not a valid Number for attribute \"%s\"";
    public static final String notAValidDate="\"%s\" is not a valid Date for attribute \"%s\"";
    public static final String additionalSheetValidationError = "Error while validating additional detail sheet : \"%s\"";
}
