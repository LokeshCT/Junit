package com.bt.rsqe.projectengine.web.projects;

import com.bt.rsqe.projectengine.web.PageTestBase;
import com.bt.rsqe.projectengine.web.quoteoption.CustomerQuoteOptionDialogPage;
import com.bt.rsqe.security.PermissionsDTO;
import com.bt.rsqe.session.client.SessionServiceClientResources;
import org.junit.Ignore;
import org.junit.Test;

import static com.bt.rsqe.projectengine.web.fixtures.SessionServiceClientResourcesFixture.*;
import static com.bt.rsqe.projectengine.web.projects.CustomerProjectPageTestFixture.*;

@SuppressWarnings("PMD.TooManyMethods")
@Ignore
public class CustomerProjectPageTest extends PageTestBase {
    private PermissionsDTO permissionsDTO = new PermissionsDTO(true, true, false, false, true, false);

    @Test
    /*Given the project has not been created in RSQE*/
    public void shouldCreateAndDisplayDefaultQuoteOption() throws Exception {
        CustomerProjectPage page = new CustomerProjectPageTestFixture(this).withPopulatedProjectResource().build();
        page.assertIsExpectedPage();
        page.detailsTab().assertQuoteOptionPresent(DEFAULT_QUOTE_OPTION_NAME, DEFAULT_QUOTE_CURRENCY);
    }

    @Test
    public void shouldDisplayANewQuoteOptionForm() throws Exception {
        CustomerProjectPage page = new CustomerProjectPageTestFixture(this).withPopulatedProjectResource().build();
        page.clickNewQuoteOptionLink();
        page.quoteOptionForm().assertHasExpectedFields();
    }

    @Test
    public void shouldValidateRequiredFields() throws Exception {
        CustomerProjectPage page = new CustomerProjectPageTestFixture(this).withPopulatedProjectResource().build();
        page.clickNewQuoteOptionLink();
        page.quoteOptionForm().submit();
        page.quoteOptionForm().assertIsDisplayed();
    }

    @Test
    public void shouldCreateNewQuoteOption() throws Exception {
        CustomerProjectPage page = new CustomerProjectPageTestFixture(this).withPopulatedProjectResource().build();
        openDialogAndEnterNewQuoteOptionDetails(page);
        page.quoteOptionForm().submit();
        page.assertIsExpectedPage();
        page.detailsTab().assertQuoteOptionPresent(DEFAULT_QUOTE_OPTION_NAME, DEFAULT_QUOTE_CURRENCY);
    }

    @Test
    public void shouldCloseDialogUponClickingCancel() throws Exception {
        CustomerProjectPage page = new CustomerProjectPageTestFixture(this).withPopulatedProjectResource().build();
        CustomerQuoteOptionDialogPage dialog = page.quoteOptionForm();
        openDialogAndEnterNewQuoteOptionDetails(page);
        dialog.assertHasExpectedFields();
        dialog.cancel();
        dialog.assertNotVisible();
    }

    @Test
    public void shouldClearDialogFormFieldsUponClose() throws Exception {
        CustomerProjectPage page = new CustomerProjectPageTestFixture(this).withPopulatedProjectResource().build();
        openDialogAndEnterNewQuoteOptionDetails(page);
        page.quoteOptionForm().close();
        page.quoteOptionForm().assertFieldsEmpty();
    }

    @Test
    public void shouldClearDialogFormFieldsUponCancel() throws Exception {
        CustomerProjectPage page = new CustomerProjectPageTestFixture(this).withPopulatedProjectResource().build();
        openDialogAndEnterNewQuoteOptionDetails(page);
        page.quoteOptionForm().cancel();
        page.quoteOptionForm().assertFieldsEmpty();
    }

    @Test
    public void shouldDisplayEditQuoteOptionForm() throws Exception {
        CustomerProjectPage page = new CustomerProjectPageTestFixture(this).withPopulatedProjectResource().build();
        page.assertHasQuoteOptionWithEditButton();
        page.clickFirstEditQuoteOptionLink();
        page.quoteOptionForm().assertPopulatedFields(DEFAULT_QUOTE_OPTION_NAME, DEFAULT_QUOTE_CURRENCY, DEFAULT_QUOTE_TERM);
        page.quoteOptionForm().enterQuoteOptionDetails(
            NEW_QUOTE_OPTION_NAME,
            NEW_QUOTE_OPTION_CURRENCY_VALUE,
            NEW_QUOTE_OPTION_TERM);
        page.quoteOptionForm().submit();
        page.detailsTab().assertQuoteOptionPresent(DEFAULT_QUOTE_OPTION_NAME, DEFAULT_QUOTE_CURRENCY);
    }

    @Test
    public void shouldNotShowEditButtonWhenHasLineItems() throws Exception {
        CustomerProjectPage page = new CustomerProjectPageTestFixture(this).withPopulatedLineItemResource().build();
        page.assertDoesNotHaveQuoteOptionWithEditButton();
    }

    @Test
    public void shouldShowBCMImportExportOptionsForBidManager() throws Exception {
        asBidManager();
        CustomerProjectPage page =
            new CustomerProjectPageTestFixture(this)
                .withPopulatedLineItemResource()
                .build();
        page.assertHasQuoteOptionWithBCMImportExportButtons();
    }

    @Test
    public void shouldNotShowBCMImportExportOptionsForSalesUser() throws Exception {
        asSalesUser();
        CustomerProjectPage page =
            new CustomerProjectPageTestFixture(this)
                .withPopulatedLineItemResource()
                .build();
        page.assertDoesNotHaveQuoteOptionWithBCMImportExportButtons();
        page.assertDoesNotHaveQuoteOptionWithBCMImportExportButtons();
    }

    private void openDialogAndEnterNewQuoteOptionDetails(CustomerProjectPage page) {
        page.clickNewQuoteOptionLink();
        page.quoteOptionForm().enterQuoteOptionDetails(
            NEW_QUOTE_OPTION_NAME,
            NEW_QUOTE_OPTION_CURRENCY,
            NEW_QUOTE_OPTION_TERM);
    }

    private void asBidManager() {
        permissionsDTO = new PermissionsDTO(true, true, false, false, true, false);
    }

    private void asSalesUser() {
        permissionsDTO = new PermissionsDTO(true, false, false, false, false, false);
    }

    @Override
    protected SessionServiceClientResources getSessionServiceClientResources() {
        return aFakeSessionService().withPermissions(permissionsDTO).build();
    }
}
