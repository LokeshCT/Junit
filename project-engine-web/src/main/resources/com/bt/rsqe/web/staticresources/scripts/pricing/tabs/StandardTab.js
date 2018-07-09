/**
 * This namespace defines all functionality required to build the {@link pricingTab} tab's Standard Tab.
 *
 * @namespace standardTab
 * @standardTab The object to append all logic onto.
 * @undefined This parameter prevents undefined from being redefined by other JavaScript code.
 */
(function(standardTab, undefined) {

    // Short names for the namespaces used here.
    var pricing                         = rsqe.pricingTab;
    var url                             = rsqe.urlBuilder;
    var utility                         = rsqe.utility;
    var BCM                             = rsqe.pricingTab.BCM;
    var bulkDiscount                    = rsqe.pricingTab.bulkDiscount;
    var pricingSummary                  = rsqe.pricingTab.pricingSummary;
    var columnNames                     = rsqe.pricingTab.PRICE_LINE_TABLE_COLUMN_NAME;

    // View selection (summary/details).
    var TABLE_VIEW_SELECTOR             = '#tableViewSelector';
    var PRICING_DETAILS_TABLE_WRAPPER   = '#priceLinesDetailsWrapper';
    var PRICING_SUMMARY_TABLE_WRAPPER   = '#priceLinesSummaryWrapper';

    // Filter elements:
    var GLOBAL_FILTER_SEARCH_BOX        = '#globalSearch';
    var PRODUCT_DROP_DOWN_MENU          = '#productFilter';
    var COUNTRY_DROP_DOWN_MENU          = '#countryFilter';
    var APPLY_FILTER_BUTTON             = '#applyFilterButton';
    var CLEAR_FILTER_BUTTON             = '#clearFilterButton';
    var SEARCH_BUTTON                   = '#searchButton';
    var CLEAR_SEARCH_BUTTON             = '#clearSearchButton';

    // User Message Elements:
    var EXPORT_PRICING_SHEET_MESSAGE    = '#export-pricing-sheet-msg';

    // Right Pane - Actions:
    var EXPORT_PRICING_SHEET            = '#exportPricingSheet';
    var UNLOCK_PRICE_LINES_BUTTON       = '#unlockPriceLinesButton';

    // TODO: Move to DetailsTable.js?
    // Pricing discount change panel:
    var PRICING_CHANGE_SAVE             = '#pricing-change-save';
    var PERSIST_DISCOUNTS               = '#persistDiscounts';
    var DISCARD_DISCOUNTS               = '#discardDiscounts';

    // Pricing Summary Values:

        // One Time Totals:
        var ONE_TIME_GROSS_TOTAL        = '#oneTimeGrossTotal';
        var ONE_TIME_NET_TOTAL          = '#oneTimeNetTotal';
        var ONE_TIME_DISCOUNT_TOTAL     = '#oneTimeDiscountTotal';

        // Recurring Totals:
        var RECURRING_GROSS_TOTAL       = '#recurringGrossTotal';
        var RECURRING_NET_TOTAL         = '#recurringNetTotal';
        var RECURRING_DISCOUNT_TOTAL    = '#recurringDiscountTotal';

        // Usage Totals:
        var USAGE_OFF_NET_TOTAL         = '#usageOffNetTotal';
        var USAGE_ON_NET_TOTAL          = '#usageOnNetTotal';
        var USAGE_TOTAL                 = '#usageTotal';

    /**
     * Initialise all functionality required by the Standard Tab.
     * @public
     */
    standardTab.initialize = function() {

        // Initialise the Standard tab tables.
        standardTab.detailsTable.initialize();
        standardTab.summaryTable.initialize();
        standardTab.revenueTable.initialize();

        // Transform the Product and Country select filters into select2 elements.
        setupSelectMenus();

        // Attach HTML event handlers.
        attachEvents();

        // Initialize Bulk Discount.
        bulkDiscount.initialize();

        // Hide the Summary Table on initial page load.
        $(PRICING_SUMMARY_TABLE_WRAPPER).hide();

    };

    /**
     * Transforms the PRODUCT_DROP_DOWN_MENU and COUNTRY_DROP_DOWN_MENU selects into select2, and configures select2.
     *  placeholder :: Set the value the select box should display whilst empty.
     *  width       :: Set the width of the select box to preserve it's width through changes.
     * @private
     */
     function setupSelectMenus() {

        $(PRODUCT_DROP_DOWN_MENU).select2(
            {
                placeholder : "--Please Select--",
                width       : 'resolve'
            }
        );

        $(COUNTRY_DROP_DOWN_MENU).select2(
            {
                placeholder : "--Please Select--",
                width       : 'resolve'
            }
        );
    }

    /**
     * Attach HTML events for the elements of the Standard Tab.
     * @private
     */
    function attachEvents() {

        /**
         * This function is called when the BCM_EXPORT_BUTTON button is clicked.
         * Display a dialog box informing the user that the BCM export has been downloaded.
         * Disable the button so that users cannot click it multiple times accidentally.
         */
        $(BCM.BCM_EXPORT_BUTTON).click(function() {
            utility.timeoutOnClickEvent(BCM.BCM_EXPORT_BUTTON, 5000);
            $(BCM.EXPORT_BCM_SHEET_MESSAGE).dialog(
                {
                    modal: true,
                    title: "BCM Sheet Download"
                }
            ).removeClass('hidden');
        });

        /**
         * This function is called when the EXPORT_PRICING_SHEET link is clicked.
         * Display a dialog box informing the user that the Pricing export has been downloaded.
         * Disable the button so that users cannot click it multiple times accidentally.
         */
        $(EXPORT_PRICING_SHEET).click(function() {
            utility.timeoutOnClickEvent(EXPORT_PRICING_SHEET, 5000);
            $(EXPORT_PRICING_SHEET_MESSAGE).dialog(
                {
                    modal: true,
                    title: "Pricing Sheet Download"
                }
            ).removeClass('hidden');
        });

        /**
         * Handle users changing the View select box. Switching displays between the Summary and Details tables.
         */
        $(TABLE_VIEW_SELECTOR).on('change', function() {

            var DETAILS_VIEW = "Pricing Details View";
            var SUMMARY_VIEW = "Pricing Summary View";

            var selection = this.value;

            if (selection === SUMMARY_VIEW) {
                showPriceLinesSummaryView();
            }

            if (selection === DETAILS_VIEW) {
                showPriceLinesDetailsView();
            }

        });

        /**
         * Functionality for the APPLY_FILTER_BUTTON.
         * When clicked this button will filter the line items displayed on the page, by the search term entered in the search box, and
         * the search term(s) entered in the product and country drop down menu.
         */
        $(APPLY_FILTER_BUTTON).click(function() {
            applySearchAndFilters();
        });

        /**
         * Functionality for the SEARCH_BUTTON.
         * When clicked this button will filter the line items displayed on the page, by the search term entered in the search box, and
         * the search term(s) entered in the product and country drop down menu.
         */
        $(SEARCH_BUTTON).click(function() {
            applySearchAndFilters();
        });

        /**
         * Functionality for the Pricing Tab's CLEAR_FILTER_BUTTON.
         * Resets the DataTable filtering by clearing the contents of the Product and Country filters, and the global search.
         */
        $(CLEAR_FILTER_BUTTON).click(function() {
            clearSearchAndFilter()
        });

        /**
         * Functionality for the Pricing Tab's CLEAR_SEARCH_BUTTON.
         * Resets the DataTable filtering by clearing the contents of the Product and Country filters, and the global search.
         */
        $(CLEAR_SEARCH_BUTTON).click(function() {
            clearSearchAndFilter()
        });

        /**
         * This function is called when the user saves changes made to the discounts on the PRICING_DETAILS_TABLE using the
         * PERSIST_DISCOUNTS (#persistDiscounts) button.
         */
        $(PERSIST_DISCOUNTS).click(function() {

            $(PRICING_CHANGE_SAVE).removeClass("hidden");
            $(PERSIST_DISCOUNTS).addClass("disabled");
            $(pricing.REQUEST_DISCOUNT_POPUP_BUTTON)
                .prop("disabled", false)
                .removeClass("disabled");

            pricing.discounts.persist(

                function() {
                    resetTable();
                },

                function() {
                    standardTab.populatePricingSummary();
                });

        });

        /**
         * This function is called when the user discards changes made to the discounts on the PRICING_DETAILS_TABLE using the
         * DISCARD_DISCOUNTS (#discardDiscounts) button.
         */
        $(DISCARD_DISCOUNTS).click(function() {
            pricing.discounts.discard(
                function () {
                    resetTable();
                }
            );
        });

        $(UNLOCK_PRICE_LINES_BUTTON).click(function() {
            $.post(url.UNLOCK_PRICE_LINES)
                .success(function () {
                    $(UNLOCK_PRICE_LINES_BUTTON)
                        .attr("disabled", true)
                        .addClass("disabled");
                    $(pricing.REQUEST_DISCOUNT_POPUP_BUTTON)
                        .attr("disabled", false)
                        .removeClass("disabled");
                    $(pricing.PRICING_DETAILS_TABLE).dataTable().fnDraw();
                })
        });

    }

    /**
     * Show the Pricing Summary tab. Hide the Pricing Details table.
     */
    function showPriceLinesSummaryView() {
        $(PRICING_DETAILS_TABLE_WRAPPER).hide();
        $(PRICING_SUMMARY_TABLE_WRAPPER).show();
    }

    /**
     * Show the Pricing Details tab. Hide the Pricing Summary table.
     * @private
     */
    function showPriceLinesDetailsView() {
        $(PRICING_SUMMARY_TABLE_WRAPPER).hide();
        $(PRICING_DETAILS_TABLE_WRAPPER).show();
    }

    /**
     * This function retrieves the values input by users in the front end filters and search boxes, and uses them to filter the contents
     * of the Details and Summary tables.
     * @private
     */
    function applySearchAndFilters() {
        var globalSearch    = $(GLOBAL_FILTER_SEARCH_BOX).val();
        var selectedProduct = $(PRODUCT_DROP_DOWN_MENU)  .val();
        var selectedCountry = $(COUNTRY_DROP_DOWN_MENU)  .val();

        // This character will be used by the filters in the Java code to separate the three search categories.
        var SEPARATOR = '|';
        var search = "globalSearch=" + globalSearch    + SEPARATOR +
                     "product="      + selectedProduct + SEPARATOR +
                     "country="      + selectedCountry;

        $(pricing.PRICING_DETAILS_TABLE).dataTable().fnFilter(search);
        $(pricing.PRICING_SUMMARY_TABLE).dataTable().fnFilter(search);
    }

    /**
     * This functions resets the values of the filters and search boxes for both the Summary and Details tables.
     * @private
     */
    function clearSearchAndFilter() {
        $(GLOBAL_FILTER_SEARCH_BOX)     .val('').change();
        $(PRODUCT_DROP_DOWN_MENU)       .val('').change();
        $(COUNTRY_DROP_DOWN_MENU)       .val('').change();
        $(pricing.PRICING_DETAILS_TABLE).dataTable().fnFilter("");
        $(pricing.PRICING_SUMMARY_TABLE).dataTable().fnFilter("");
    }

    /**
     * Reset the price line tables.
     * @private
     */
    function resetTable() {
        $(pricing.PRICING_DETAILS_TABLE).dataTable().fnDraw();
        $(pricing.PRICING_SUMMARY_TABLE).dataTable().fnDraw();
        $(PRICING_CHANGE_SAVE)          .addClass("hidden");
        $(PERSIST_DISCOUNTS)            .removeClass("disabled");
    }

    /**
     * Retrieves the information to populate into the Right Pane's Pricing Summary.
     * @public
     */
    standardTab.populatePricingSummary = function() {
        pricingSummary.populatePricingSummary(
            $(ONE_TIME_GROSS_TOTAL),
            $(ONE_TIME_DISCOUNT_TOTAL),
            $(ONE_TIME_NET_TOTAL),
            $(RECURRING_GROSS_TOTAL),
            $(RECURRING_DISCOUNT_TOTAL),
            $(RECURRING_NET_TOTAL),
            $(USAGE_OFF_NET_TOTAL),
            $(USAGE_ON_NET_TOTAL),
            $(USAGE_TOTAL),
            undefined
        );
    };

    // TODO: This functionality should be moved into the backend. We will calculate the total values there.
    /**
     * This function handles the generation of table rows for the two group levels.
     *  0 - Indicates a Summary row.
     *  1 - Indicates a Header row.
     *
     * This function is used by the PRICING_DETAILS_TABLE and the PRICING_SUMMARY_TABLE.
     *
     * @public
     * @param table The table to add the row to.
     * @param settings The settings object of the DataTable we are altering.
     */
    standardTab.renderGroups = function(table, settings) {

        // If the table is empty there will be no line items to generate summaries for. So return early.
        if (utility.isDataTableEmpty(table)) {
            return;
        }

        var tableRows = $(table).find('tbody tr');
        var rowLength = tableRows[0].getElementsByTagName('td').length;
        var product = "";

        for (var i = 0; i < tableRows.length; i++) {

            // The existing row.
            var row               = settings.aoData[settings.aiDisplay[i]]._aData;

            // The grouping level is used to determine if the row we are building is a Summary Row or a Header row.
            var groupLevel        = row.groupingLevel;

            // TODO: What is an aggregate row?
            var aggregateRow      = row.aggregateRow;

            // Cell values for new row.
            var newSite           = row.site;
            var newMiniAddress    = row.miniAddress;
            var newProduct        = row.product;
            var newSummary        = row.summary;
            var newDescription    = "Total Price";
            var newOfferName      = row.offerName;
            var newDiscountStatus = row.discountStatus;

            var newProductSiteSummary = newProduct + " " +
                                        newSite    + " " +
                                        newSummary + " " +
                                        newMiniAddress;

            // This check ensures we do not add the Summary line after each row of the line item.
            if (newProductSiteSummary != product) {

                // Build either a Summary or Header row based on the group level.
                var newRow = buildSummaryOrHeaderRow(newSite,
                                                     newMiniAddress,
                                                     newProduct,
                                                     newSummary,
                                                     newDescription,
                                                     newOfferName,
                                                     newDiscountStatus,
                                                     rowLength,
                                                     groupLevel
                );

                // Append HTML class to distinguish this summary row from the other rows.
                newRow.className = "product_group group_" + groupLevel;

                // Append an additional class if this row is IFC.
                if (isIFC(row)) {
                    newRow.className = "ifc-item";
                }

                // Insert the new row prior to the other rows of this price line.
                tableRows[i].parentNode.insertBefore(newRow, tableRows[i]);
                product = newProductSiteSummary;
            }

            // Remove the row if it is an aggregate row.
            if (aggregateRow === 'true') {
                tableRows[i].parentNode.removeChild(tableRows[i]);
            }
        }
    };

    /**
     * Builds either a Summary of a Header row with the given cell values based on the given groupLevel.
     * @param site           {String} The value to populate the site cell with.
     * @param miniAddress    {String} The value to populate the miniAddress cell with.
     * @param product        {String} The value to populate the product cell with.
     * @param summary        {String} The value to populate the summary cell with.
     * @param description    {String} The value to populate the description cell with.
     * @param offerName      {String} The value to populate the offerName cell with.
     * @param discountStatus {String} The value to populate the discountStatus cell with.
     * @param rowLength      {Number} The number of cells in the row.
     * @param groupLevel     {String} 0 - Indicates that a Summary row should be built.
     *                                1 - Indicates that a Header row should be built.
     * @returns              {Element} The built Summary or header row.
     */
    function buildSummaryOrHeaderRow(site,
                                     miniAddress,
                                     product,
                                     summary,
                                     description,
                                     offerName,
                                     discountStatus,
                                     rowLength,
                                     groupLevel) {

        var SUMMARY_ROW_GROUP_LEVEL = 0;
        var HEADER_ROW_GROUP_LEVEL  = 1;

        switch(groupLevel) {

            // Display all given column values on a summary row.
            case SUMMARY_ROW_GROUP_LEVEL :
                return buildRow(site,
                                miniAddress,
                                product,
                                summary,
                                description,
                                offerName,
                                discountStatus,
                                rowLength);
                break;

            // We only wish to see the product name on a header row.
            case HEADER_ROW_GROUP_LEVEL :
                return buildRow("",
                                "",
                                product,
                                "",
                                "",
                                "",
                                "",
                                rowLength);
                break;
        }

    }

    /**
     * Build a row with the given values.
     * @param site           {String} The value to populate the site cell with.
     * @param miniAddress    {String} The value to populate the miniAddress cell with.
     * @param product        {String} The value to populate the product cell with.
     * @param summary        {String} The value to populate the summary cell with.
     * @param description    {String} The value to populate the description cell with.
     * @param offerName      {String} The value to populate the offerName cell with.
     * @param discountStatus {String} The value to populate the discountStatus cell with.
     * @param rowLength      {Number} The number of cells in the row.
     * @returns              {Element} The built row.
     */
    function buildRow(site,
                      miniAddress,
                      product,
                      summary,
                      description,
                      offerName,
                      discountStatus,
                      rowLength) {

        var newRow = document.createElement('tr');

        // Build the cells for the new row.
        var siteCell           = buildTableCell(columnNames.SITE,            1,             site);
        var miniAddressCell    = buildTableCell(columnNames.MINI_ADDRESS,    1,             miniAddress);
        var productCell        = buildTableCell(columnNames.PRODUCT,         1,             product);
        var summaryCell        = buildTableCell(columnNames.SUMMARY,         1,             summary);
        var descriptionCell    = buildTableCell(columnNames.DESCRIPTION,     1,             description);
        var offerNameCell      = buildTableCell(columnNames.OFFER_NAME,      1,             offerName);
        var discountStatusCell = buildTableCell(columnNames.DISCOUNT_STATUS, rowLength - 1, discountStatus);

        // Build the new row.
        newRow.appendChild(siteCell);
        newRow.appendChild(miniAddressCell);
        newRow.appendChild(productCell);
        newRow.appendChild(summaryCell);
        newRow.appendChild(descriptionCell);
        newRow.appendChild(offerNameCell);
        newRow.appendChild(discountStatusCell);

        // TODO: Append Total values here. Likely best to do this in the backend.

        return newRow;

    }

    /**
     * Builds a table cell.
     * @private
     * @param className The HTML className to apply to this cell.
     * @param colspan How many column cells should the new cell encompass.
     * @param value The value to populate the cell with.
     * @returns {Element} The completed cell.
     */
    function buildTableCell(className, colspan, value) {
        var cell       = document.createElement('td');
        cell.colSpan   = colspan;
        cell.className = className;
        cell.innerHTML = value;
        return cell;
    }

    /**
     * Checks if the given row is an IFC row.
     * @private
     * @param currentRow The row to check.
     * @returns {boolean} True if the current row is IFC. False otherwise.
     */
    function isIFC(currentRow) {
        return currentRow.forIfc === "true";
    }

    /**
     * The Details and Summary tables by default show all of the EUP Access elements.
     * This function configures the tables for users that do not have EUP Access.
     * If the user has EUP Access this function does nothing.
     * @public
     */
    standardTab.configureTableForNoEupAccess = function() {

        if (!pricing.hasEupAccess()) {

            // Selectors for the One Time and Recurring headings.
            var TABLE_ONE_TIME_HEADING  = '.tableOneTimeHeading';
            var TABLE_RECURRING_HEADING = '.tableRecurringHeading';

            // The currency assigned to the current quote:
            var currency = pricing.getCurrency();
            var newOneTimeHeading   = "One Time Price - RRP ("  + currency + ")";
            var newRecurringHeading = "Recurring Price - RRP (" + currency + ")";

            // Change the heading name from PTP (Price To Partner) to RRP (Regular Retail Price).
            $(TABLE_ONE_TIME_HEADING) .text(newOneTimeHeading);
            $(TABLE_RECURRING_HEADING).text(newRecurringHeading);
        }
    };

// Immediately invoke this namespace.
}( rsqe.pricingTab.standardTab = rsqe.pricingTab.standardTab || {} ));