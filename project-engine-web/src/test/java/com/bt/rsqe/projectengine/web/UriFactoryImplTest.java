package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.enums.ProductAction;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.format;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

@SuppressWarnings("PMD.TooManyMethods")
public class UriFactoryImplTest
{
    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static final String CONTRACT_ID = "CONTRACT_ID";
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String QUOTE_OPTION_ID = "QUOTE_OPTION_ID";
    private static final String LINE_ITEM_ID = "LINE_ITEM_ID";
    private static final String OFFER_ID = "offerId";
    private static final String IS_COST_ATTACHMENT_DIALOG = "isCostAttachmentDialog";
    private static final String  PROJECT_URL = format("/rsqe/customers/%s/contracts/%s/projects/%s", CUSTOMER_ID, CONTRACT_ID, PROJECT_ID);
    private static final String  QUOTE_OPTIONS_URL = format("%s/quote-options", PROJECT_URL);
    private static final String  QUOTE_OPTION_URL = format("%s/%s", QUOTE_OPTIONS_URL, QUOTE_OPTION_ID);
    private static final String  LINE_ITEM_URL = format("%s/line-items/%s", QUOTE_OPTION_URL, LINE_ITEM_ID);
    private static final String  OFFER_URL = format("%s/offers/%s", QUOTE_OPTION_URL, OFFER_ID);
    private static final String SITE_SELECTED_FOR_PRODUCT_URL = format("%s/add-product", QUOTE_OPTION_URL);
    private static final String PRICING_SHEET_URL = format("%s/pricing-sheet?%s", QUOTE_OPTION_URL, OFFER_ID);
    private static final String ATTACHMENT_DIALOG_URL = format("%s/attachments/form?%s", QUOTE_OPTION_URL, IS_COST_ATTACHMENT_DIALOG);
    private static final String TRUE = "true";
    private static final String URL_SEPARATOR = "/";

    @Test
    public void shouldGenerateQuoteOptionPricingTabUri() throws Exception {
        final String expectedUriString = format("%s/pricing-tab",QUOTE_OPTION_URL);
        final URI actualUri = UriFactoryImpl.quoteOptionPricingTab(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    /**
     * Test that the quoteOptionPricingTabNew() method returns the correct URI for the new pricing tab implementation.
     */
    @Test
    public void generatePricingTabUriNew() throws URISyntaxException
    {
        URI expected = new URI(QUOTE_OPTION_URL + URL_SEPARATOR + "pricing-tab-new");
        URI actual   = UriFactoryImpl.quoteOptionPricingTabNew(CUSTOMER_ID,
                                                               CONTRACT_ID,
                                                               PROJECT_ID,
                                                               QUOTE_OPTION_ID);
        assertEquals("Actual URI should match expected URI.", actual, expected);
    }

    @Test
    public void shouldGenerateQuoteOptionDetailsTabUri() throws Exception {
        final String expectedUriString = format("%s/details-tab",QUOTE_OPTION_URL);
        final URI actualUri = UriFactoryImpl.quoteOptionDetailsTab(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateQuoteOptionsTabUri() throws Exception {
        final String expectedUriString = format("%s/quote-options-tab",
                                                PROJECT_URL,
                                                QUOTE_OPTION_ID);
        final URI actualUri = UriFactoryImpl.quoteOptionsTab(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateOrdersTabUri() throws Exception {
        final String expectedUriString = format("%s#OrdersTab",
                                                QUOTE_OPTION_URL);
        final URI actualUri = UriFactoryImpl.ordersTab(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateOffersTabUri() throws Exception {
        final String expectedUriString = format("%s#QuoteOptionOffersTab",
                                                QUOTE_OPTION_URL);
        final URI actualUri = UriFactoryImpl.quoteOptionOffersTab(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateOfferDetailsTabUri() throws Exception {
        final String expectedUriString = format("%s/offer-details-tab",
                                                OFFER_URL);
        final URI actualUri = UriFactoryImpl.offerDetailsTab(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateBulkUploadTargetUri() throws Exception {
        final String expectedUriString = format("%s/quote-options/bulk-upload",
                                                PROJECT_URL);
        final URI actualUri = UriFactoryImpl.bulkUploadTargetUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateQuoteOptionDialogUri() throws Exception {
        final String expectedUriString = format("%s/quote-options/form",
                                                PROJECT_URL);
        final URI actualUri = UriFactoryImpl.quoteOptionDialog(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateQuoteOptionNotesDialogUri() throws Exception {
        final String expectedUriString = format("%s/notes",
                                                QUOTE_OPTIONS_URL);
        final URI actualUri = UriFactoryImpl.quoteOptionNotesDialog(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateQuoteOptionNotesUri() throws Exception {
        final String expectedUriString = format("%s/notes",
                                                QUOTE_OPTION_URL);
        final URI actualUri = UriFactoryImpl.quoteOptionNotes(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateQuoteOptionBcmUri() throws Exception {
        final String expectedUriString = format("%s/bcm",
                                                QUOTE_OPTION_URL);
        final URI actualUri = UriFactoryImpl.quoteOptionBcm(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateOrdersUri() throws Exception {
        final String expectedUriString = format("%s/orders",
                                                QUOTE_OPTION_URL);
        final URI actualUri = UriFactoryImpl.orders(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateQuoteOptionOfferApproveUri() throws Exception {
        final String expectedUriString = format("%s/approve",
                                                OFFER_URL);
        final URI actualUri = UriFactoryImpl.offerApprove(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateQuoteOptionOfferRejectUri() throws Exception {
        final String expectedUriString = OFFER_URL + "/reject";
        final URI actualUri = UriFactoryImpl.offerReject(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID, CONTRACT_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateBulkTemplateDialogUri() throws Exception {
        final String expectedUriString = format("%s/dialogs/bulk-template",QUOTE_OPTION_URL);
        final URI actualUri = UriFactoryImpl.bulkTemplateUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(expectedUriString));

    }

    @Test
    public void shouldGeneratePricingActionsUri() throws Exception {
        final String expectedUriString = format("%s/pricing-actions",QUOTE_OPTION_URL);
        final URI actualUri = UriFactoryImpl.pricingActionsUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldReturnLineItemValidationUri(){
        assertThat(new UriFactoryImpl(null).lineItemValidationUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID).toString(),
                   is(LINE_ITEM_URL+"/validate"));
    }

    @Test
    public void shouldReturnSiteSelectedForProductCheckUri(){
        assertThat(new UriFactoryImpl(null).siteSelectedForProductCheckUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID).toString(),
                   is(SITE_SELECTED_FOR_PRODUCT_URL+"/siteSelectedForProductCheck"));
    }

    @Test
    public void shouldReturnCreateProductUri(){
        assertThat(new UriFactoryImpl(null).createProductURI(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID).toString(),
                   is(SITE_SELECTED_FOR_PRODUCT_URL+"/createProduct"));
    }

    @Test
    public void shouldGenerateProductTabUriBasedOnProductAction() throws Exception {
        assertThat(UriFactoryImpl.productTabURI(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ProductAction.Provide.description()).toString(),
                   is(format("%s/add-product/product-tab/Add", QUOTE_OPTION_URL)));

        assertThat(UriFactoryImpl.productTabURI(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ProductAction.Migrate.description()).toString(),
                   is(format("%s/add-product/product-tab/Migrate", QUOTE_OPTION_URL)));
    }

    @Test
    public void shouldReturnSelectNewSiteUri(){
        assertThat(new UriFactoryImpl(null).selectNewSite(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID).toString(),
                   is(SITE_SELECTED_FOR_PRODUCT_URL+"/selectNewSiteForm"));
    }

    @Test
    public void shouldGenerateImportProductTargetUri() throws Exception {
        final String expectedUriString = format("%s/quote-options/%s/line-items/import-product-configuration",
                                                PROJECT_URL, QUOTE_OPTION_ID, LINE_ITEM_ID);
        final URI actualUri = UriFactoryImpl.importProductTargetUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateAddImportProductTargetUri() throws Exception {
        final String expectedUriString = format("%s/quote-options/%s/line-items/add-import-product-configuration",
                                                PROJECT_URL, QUOTE_OPTION_ID, LINE_ITEM_ID);
        final URI actualUri = UriFactoryImpl.addImportProductTargetUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateQuoteOptionDeleteUri() throws Exception {
        final String expectedUriString = format("%s/quote-options/delete",
                                                PROJECT_URL);
        final URI actualUri = UriFactoryImpl.deleteQuoteOptionUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateFilterLineItemsUri() throws Exception {
        final String expectedUriString = QUOTE_OPTIONS_URL + "/filter-line-items";
        final URI actualUri = UriFactoryImpl.filterLineItemsUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldReturnExportConfigurationUri(){
        String sCode = "S0123456";
        assertThat(new UriFactoryImpl(null).getBulkTemplateExportUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID,sCode).toString(),
                   is(QUOTE_OPTION_URL+"/bulk-template-export/sCode/"+sCode));
    }

    @Test
    public void shouldReturnUserExportUri(){
        String sCode = "S0123456";
        assertThat(new UriFactoryImpl(null).getUserExportUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID,sCode).toString(),
                   is(QUOTE_OPTION_URL+"/sCode/"+sCode+"/user-export"));
    }

    @Test
    public void shouldReturnUserImportValidationUri(){
        String sCode = "S0123456";
        assertThat(new UriFactoryImpl(null).getUserImportValidateUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID,sCode).toString(),
                   is(QUOTE_OPTION_URL+"/sCode/"+sCode+"/user-import-validate"));
    }

    @Test
    public void shouldReturnUserImportStatusUri(){
        String sCode = "S0123456";
        assertThat(new UriFactoryImpl(null).getUserImportStatusUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID,sCode).toString(),
                   is(QUOTE_OPTION_URL+"/sCode/"+sCode+"/user-import-status"));
    }

    @Test
    public void shouldReturnUserImportUri(){
        String sCode = "S0123456";
        assertThat(new UriFactoryImpl(null).getUserImportUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID,sCode).toString(),
                   is(QUOTE_OPTION_URL+"/sCode/"+sCode+"/user-import"));
    }

    @Test
    public void shouldGenerateEndOfLifeUri() throws Exception {
        final String expectedUriString = format("%s/quote-options/%s/add-product/endOfLifeValidation",
                                                PROJECT_URL, QUOTE_OPTION_ID, LINE_ITEM_ID);
        final URI actualUri = UriFactoryImpl.endOfLifeCheckUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateValidationUri(){
        final URI actualUri = UriFactoryImpl.validation(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(QUOTE_OPTION_URL+"/validation"));
    }

    @Test
    public void shouldGenerateProductImportValidationUri(){
        final URI actualUri = UriFactoryImpl.validateImportProductUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(QUOTE_OPTION_URL+"/validation/validate-import-with-line-item"));
    }

    @Test
    public void shouldGenerateAddProductImportValidationUri(){
        final URI actualUri = UriFactoryImpl.validateAddProductImportUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(QUOTE_OPTION_URL+"/validation/validate-import-with-product-code"));
    }

    @Test
    public void shouldGenerateOfferTabPricingSheetUri() throws Exception {
        final String expectedUriString = format("%s=%s",PRICING_SHEET_URL, OFFER_ID);
        final URI actualUri = UriFactoryImpl.exportPricingSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateAttachmentDialogFormUri() throws Exception {
        final String expectedUriString = format("%s=%s",ATTACHMENT_DIALOG_URL, TRUE);
        final URI actualUri = UriFactoryImpl.attachmentDialogForm(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, true);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateBidManagerCommentsUri() throws Exception {
        final String expectedUriString = format("%s/bcm/bidmanagercomments", QUOTE_OPTION_URL);
        final URI actualUri = UriFactoryImpl.bidManagerCommentsAndCaveats(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }

    @Test
    public void shouldGenerateCancelOfferApprovalUri() throws Exception {
        final String expectedUriString = format("%s/cancel-approval",
                                                OFFER_URL);
        final URI actualUri = UriFactoryImpl.cancelOfferApproval(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID);
        assertThat(actualUri.toString(), is(expectedUriString));
    }
}