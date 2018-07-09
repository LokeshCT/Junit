/**
 * This namespace defines all functionality required to build the Pricing Details DataTable.
 * This is the default view on the {@link standardTab}.
 *
 * @namespace detailsTable
 * @detailsTable The object to append all logic onto.
 * @undefined This parameter prevents undefined from being redefined by other JavaScript code.
 */
(function(detailsTable, undefined) {

    // Short names for the namespaces used here.
    var pricing                   = rsqe.pricingTab;
    var url                       = rsqe.urlBuilder;
    var utility                   = rsqe.utility;
    var columnNames               = rsqe.pricingTab.PRICE_LINE_TABLE_COLUMN_NAME;
    var standardTab               = rsqe.pricingTab.standardTab;
    var bulkDiscount              = rsqe.pricingTab.bulkDiscount;

    // One Time Class Names:
    var ONE_TIME_VALUE_CLASS      = 'oneTime_value';
    var ONE_TIME_DISCOUNT_CLASS   = 'oneTime_discount';
    var ONE_TIME_NET_CLASS        = 'oneTime_netTotal';

    // Recurring Names:
    var RECURRING_VALUE_CLASS     = 'recurring_value';
    var RECURRING_DISCOUNT_CLASS  = 'recurring_discount';
    var RECURRING_NET_CLASS       = 'recurring_netTotal';

    // The DataTable library displays a dialog with this ID when it is processing the table.
    var PRICE_LINES_PROCESSING    = '#priceLinesDetails_processing';

    var MANUAL_PRICE_WARN_MESSAGE = '#manualPriceWarnMessage';

    /**
     * Initialize the Details DataTable and it's supporting elements.
     * @public
     */
    detailsTable.initialize = function() {
        buildDetailsTable();
    };

    /**
     * Initialise and configure the Pricing Details DataTable.
     * This table is displayed on the Details View of the {@link standardTab}. This is the default view of the Priceline data.
     * The table displays each products price lines along with a total, and a product summary line.
     *
     * Configuration:
     *  bProcessing     :: Display a loading screen during initial page load and whilst performing searches/filters.
     *  bServerSide     :: The databases contain large data-sets that would be inefficient to process on the client side so enable server mode
     *                     and have the database return paginated results.
     *  sAjaxSource     :: Retrieve product prices from the /productPrices end point. As the JSON object returned by the end point does not
     *                     contain either the 'data' or 'aadata' fields required by the DataTable library the default value has been over-
     *                     written to itemDTOs.
     *  sPaginationType :: Set paging to be displayed as: 'First Previous <Current Page> Next Last'.
     *  bSort           :: Table sorting/ordering is disabled.
     *  bAutoWidth      :: Prevent DataTables from restricting the size of the table. When true the table drawn is about half the size of
     *                     it's containing div.
     *  aoColumns       :: Column configuration options.
     *                     - sWidth      - Specify the size of the column. Required to prevent the jeditable library from resizing table cells.
     *                     - sClass      - Specify classes to apply to the cells of this table.
     *                     - numeric     - Display value as a number.
     *                     - nowrap      - Do not wrap the numeric columns.
     *                     - readOnly    - Do not allow any edits. This needs to be explicitly removed to make the cell editable.
     *  aoColumnDefs    :: Hide the RRP columns if the current user does not have EUP access.
     *  oLanguage       :: Adds two information displays to the table.
     *                     - sInfo       - Shows the pagination for the data currently on screen: E.g. 'Showing 1 to 5 of 5 Root Products'
     *                     - aLengthMenu - Shows a menu that allows the user to select how many results should be shown on the page.
     *
     * @private
     * @returns {DataTable} The Details DataTable after it has been initialized.
     */
    function buildDetailsTable() {

        // RRP table index positions.
        var ONE_TIME_RRP_COLUMN_INDEX  = 8;
        var RECURRING_RRP_COLUMN_INDEX = 12;

        $(pricing.PRICING_DETAILS_TABLE).dataTable(
            {
                bProcessing: true,
                bServerSide: true,
                sAjaxSource: url.PRODUCT_PRICES,
                sPaginationType: "full_numbers",
                bSort: false,
                bAutoWidth: false,
                aoColumns: [
                    { mData: columnNames.SITE,                sWidth: "3%" },
                    { mData: columnNames.MINI_ADDRESS,        sWidth: "5%" },
                    { mData: columnNames.PRODUCT,             sWidth: "10%" },
                    { mData: columnNames.SUMMARY,             sWidth: "10%" },
                    { mData: columnNames.DESCRIPTION,         sWidth: "15%" },
                    { mData: columnNames.OFFER_NAME,          sWidth: "5%" },
                    { mData: columnNames.DISCOUNT_STATUS,     sWidth: "5%" },
                    { mData: columnNames.PRICE_STATUS,        sWidth: "4%" },
                    { mData: columnNames.ONE_TIME_RRP,        sWidth: "4%", sClass: "numeric nowrap readOnly" },
                    { mData: columnNames.ONE_TIME_GROSS,      sWidth: "4%", sClass: "numeric nowrap readOnly" },
                    { mData: columnNames.ONE_TIME_DISCOUNT,   sWidth: "4%", sClass: "numeric nowrap readOnly" },
                    { mData: columnNames.ONE_TIME_NET_TOTAL,  sWidth: "4%", sClass: "numeric nowrap readOnly" },
                    { mData: columnNames.RECURRING_RRP,       sWidth: "4%", sClass: "numeric nowrap readOnly" },
                    { mData: columnNames.RECURRING_GROSS,     sWidth: "4%", sClass: "numeric nowrap readOnly" },
                    { mData: columnNames.RECURRING_DISCOUNT,  sWidth: "4%", sClass: "numeric nowrap readOnly" },
                    { mData: columnNames.RECURRING_NET_TOTAL, sWidth: "4%", sClass: "numeric nowrap readOnly" }
                ],
                aoColumnDefs: [
                    {
                        aTargets: [
                            ONE_TIME_RRP_COLUMN_INDEX,
                            RECURRING_RRP_COLUMN_INDEX
                        ],
                        bVisible: pricing.hasEupAccess()
                    }
                ],
                oLanguage: {
                    sInfo: "Showing _START_ to _END_ of _TOTAL_ Root Products",
                    aLengthMenu: "Display _MENU_ Root Products"
                },

                /**
                 * Displays an error to the user if there was an error loading data with sAjaxSource: url.PRODUCT_PRICES.
                 * Hides the DataTables processing window which would otherwise remain on screen permanently.
                 * @param sSource The URL set by sAjaxSource configuration.
                 * @param aoData This is an object sent by the DataTables library to the server. Includes things like user entered searches.
                 * @param fnCallback The fnCallBack function definition.
                 * @param oSettings The settings object for this DataTable.
                 */
                fnServerData: function( sSource, aoData, fnCallback, oSettings ) {
                    handleDataTableError(
                        sSource,
                        aoData,
                        fnCallback,
                        oSettings,
                        function(xhr, error) {
                            var message = "Error loading standard prices. " + error + " " + xhr.responseText;
                            pricing.PRICING_COMMON_ERROR.show(message);
                            $(PRICE_LINES_PROCESSING).hide();
                        }
                    );
                },

                /**
                 * By default DataTables expects the JSON object returned by sAjaxSource to contain either a 'aadata' or 'data' field.
                 * The /product-prices end point contains neither. This configuration overwrites the default to point at the itemDTOs field.
                 * @param data The JSON object returned by the URL set by sAjaxSource.
                 * @returns {Array} The itemDTOs array returned as part of the JSON response returned by a call to the /product-prices end point.
                 */
                sAjaxDataProp: function(data) {
                    return data.itemDTOs;
                },

                /**
                 * Process each row of the DataTable after it is generated but before it is rendered on the page.
                 * See {@link self.fnRowCallback} for details on the specific modifications made to each row.
                 * Reference: https://datatables.net/reference/option/rowCallback
                 *
                 * @param row The element to be inserted into the document.
                 * @param data The datasource for the object.
                 * @returns The row after being processed.
                 */
                fnRowCallback: function(row, data) {
                    var columns = getVisibleDataTableColumnNames(this.fnSettings().aoColumns);
                    return fnRowCallback(row, data, columns);
                },

                /**
                 * Called every time the table is drawn.
                 * The functions here are executed AFTER the table has been drawn.
                 * - Make the table editable using the JQuery Jeditable plugin.
                 * - Refresh discounts.
                 * - Prepend the Summary row for each price line.
                 * - Populate the Pricing Summary.
                 * - Links the description checkboxes in the table header to the checkboxes within the table.
                 * - Populates the Pricing Summary in the Right Pane.
                 * @param settings The settings object for this table.
                 */
                fnDrawCallback: function(settings) {
                    pricing.discounts.refreshDiscounts();
                    setEditableCells();
                    linkSelectAllToDescriptionCheckBoxes();
                    standardTab.renderGroups(pricing.PRICING_DETAILS_TABLE, settings);
                    standardTab.configureTableForNoEupAccess();
                    standardTab.populatePricingSummary();
                }
            }
        );
    }

    /**
     * Builds a list of all the Visible column names in the PRICING_DETAILS_TABLE.
     * @param aoColumns The auColumns object from the DataTables fnSettings function.
     * @returns {Array} An array of containing all visible columns in the DataTable.
     */
    function getVisibleDataTableColumnNames(aoColumns) {
        var columns = [];
        aoColumns.forEach(function(column) {

            if (column.bVisible) {
                columns.push(column.mData);
            }

        });
        return columns;
    }

    /**
     * Process each row of the DataTable after it is generated but before it is rendered on the page.
     * At this stage we alter the data or append classes prior to the row being rendered by the browser.
     * The majority of the changes made relate to determining if cells should be editable or read only.
     *
     * All cells are defaulted to ReadOnly and are only made editable on the following conditions;
     *  - The gross value is non-blank (zero/empty/null/undefined) String.
     *  - Discounting is enabled (for discounting cells).
     *
     * For Gross cells there are two extra conditions:
     *  - The Product has a manual pricing strategy.
     *  - The user is a BID Manager.
     *
     * A checkbox is appended to each cell of the Description column. This checkbox allows a user to edit the discounts of that row using
     * the right hand panel.
     *
     * Reference: https://datatables.net/reference/option/rowCallback
     *
     * @private
     * @param row A single HTML row of the DataTable prior to being rendered on the page.
     * @param aData The datasource for the row.
     * @param columns An array containing the columns names of the table.
     * @returns {*} The row after being processed.
     */
    function fnRowCallback(row, aData, columns) {

        // Row values:
        var oneTimeId                = aData.oneTime.id;
        var oneTimeGross             = aData.oneTime.value;
        var oneTimeDiscountEnabled   = aData.oneTime.discountEnabled;
        var recurringId              = aData.recurring.id;
        var recurringGross           = aData.recurring.value;
        var recurringDiscountEnabled = aData.recurring.discountEnabled;
        var readOnlyRow              = aData.readOnly;
        var isManualPricing          = aData.isManualPricing;

        // Iterate over each cell in the row.
        $.each(getTableRowsAndCells(), function(i, td) {

            var column = columns[i];

            // Add the column name as a class on the cell.
            $(td).addClass(column.replace('.', '_'));

            // Do not apply any special formatting to read only rows.
            if (!readOnlyRow) {

                // Switch on the column the cell belongs to. Remove the readOnly class from any cell that should be user editable.
                switch(column) {

                    case columnNames.DESCRIPTION :
                        $(td).prepend(buildDescriptionCheckbox(readOnlyRow));
                        break;

                    case columnNames.ONE_TIME_GROSS :
                        if (isGrossCellEditable(isManualPricing)) {
                            $(td).removeClass('readOnly');
                        }
                        break;

                    case columnNames.RECURRING_GROSS :
                        if (isGrossCellEditable(isManualPricing)) {
                            $(td).removeClass("readOnly");
                        }
                        break;

                    case columnNames.ONE_TIME_DISCOUNT :
                        if (isDiscountCellEditable(oneTimeGross, oneTimeDiscountEnabled)) {
                            $(td).removeClass("readOnly");
                        }
                        break;

                    case columnNames.RECURRING_DISCOUNT :
                        if (isDiscountCellEditable(recurringGross, recurringDiscountEnabled)) {
                            $(td).removeClass("readOnly");
                        }
                        break;

                    case columnNames.ONE_TIME_NET_TOTAL :
                        if (isNetCellEditable(oneTimeGross)) {
                            $(td).removeClass("readOnly");
                        }

                        break;

                    case columnNames.RECURRING_NET_TOTAL :
                        if (isNetCellEditable(recurringGross)) {
                            $(td).removeClass("readOnly");
                        }
                        break;
                }
            }
        });

        // Appends a readOnly class to the row that is used to style the row.
        if (readOnlyRow) {
            $(row).addClass("readOnly");
        }

        $(row).addClass("priceLine");
        $(row).attr("oneTime_id", "id_" + oneTimeId);
        $(row).attr("recurring_id", "id_" + recurringId);

        return row;

        /**
         * Returns the table rows and the cells they contain to iterate over.
         * @returns {*|jQuery|HTMLElement} The table rows to iterate over.
         */
        function getTableRowsAndCells() {
            return $("td", row);
        }

        /**
         * Builds an HTML checkbox with an onclick event that enables editing for the associated price line.
         * The isDisabled parameter determines if the checkbox is disabled.
         * @param isDisabled {Boolean} Determines if the checkbox built by this function should be disabled.
         * @returns {Element} The built checkbox.
         */
        function buildDescriptionCheckbox(isDisabled) {
            var checkbox      = document.createElement('input');
            checkbox.type     = "checkbox";
            checkbox.name     = "listOfPriceLines";
            checkbox.disabled = isDisabled;

            // Add the onclick event.
            checkbox.onclick = function() {
                descriptionCheckboxOnClick(this);
            };
            return checkbox;
        }

        /**
         * Functionality executed when one of the Description column checkboxes is clicked.
         * @private
         * @param checkbox The checkbox that triggered the on click event.
         */
        function descriptionCheckboxOnClick(checkbox) {

            if(checkbox.checked) {
                descriptionCheckboxIsChecked();
            }
            else {
                descriptionCheckboxUnChecked();
            }
        }

        /**
         * This function is called every time one of the description column checkboxes is checked.
         * It is NOT called when the '#selectAll' description checkbox is ticked.
         * @private
         */
        function descriptionCheckboxIsChecked() {
            detailsTable.showManualPriceWarnMessage();
        }

        /**
         * This function is called every time one of the description column checkboxes is unchecked.
         * It is NOT called when the '#selectAll' description checkbox is ticked.
         * @private
         */
        function descriptionCheckboxUnChecked() {
            detailsTable.hideManualPriceWarnMessage();
        }

    }

    /**
     * Show the on screen message telling the user to Enter a valid price or untick checkbox.
     * @public
     */
    detailsTable.showManualPriceWarnMessage = function() {
        $(MANUAL_PRICE_WARN_MESSAGE).removeClass("hidden");
    };

    /**
     * Hide the on screen message telling the user to Enter a valid price or untick checkbox.
     * The message is only hidden if all price line checkboxes are unchecked.
     * @public
     */
    detailsTable.hideManualPriceWarnMessage = function() {
        if (!detailsTable.isAnyDescriptionCheckboxChecked()) {
            $(MANUAL_PRICE_WARN_MESSAGE).addClass("hidden");
        }
    };

    /**
     * Determines if any of the Price Line checkboxes is checked.
     * @public
     * @returns {boolean} True if any Price Line checkbox is checked. False otherwise.
     */
    detailsTable.isAnyDescriptionCheckboxChecked = function() {

        var result = false;
        $(pricing.PRICING_DETAILS_TABLE).find("tr.priceLine:not('.readOnly') input:checkbox")
            .each(

                /**
                 * Sets the return value to true if any of the checkboxes is checked.
                 */
                function() {
                    if (this.checked) {
                        result = true;
                    }
                }

            );
        return result;
    };

    /**
     * Display the Unsaved Discount Message to users.
     * This message should be shown anytime unsaved discounts exist on the page.
     * @public
     */
    detailsTable.showUnsavedDiscountsMessage = function() {
        $(pricing.UNSAVED_DISCOUNT_MESSAGE).removeClass("hidden");
    };

    /**
     * Hide the Unsaved Discount Message from users.
     * This message should not be vivisble unless there are unsaved discounts on the page.
     * @public
     */
    detailsTable.hideUnsavedDiscountsMessage = function() {
        $(pricing.UNSAVED_DISCOUNT_MESSAGE).addClass("hidden");
    };

    /**
     * Determines if the given cell is either the One Time or Recurring Gross cell by checking the class name.
     * @public
     * @param cell The cell to check.
     * @returns {Boolean} True if the cell is either the One Time or Recurring Gross cell.
     */
    detailsTable.isGrossCell = function(cell) {
        return $(cell).hasClass(ONE_TIME_VALUE_CLASS) ||
               $(cell).hasClass(RECURRING_VALUE_CLASS);
    };

    /**
     * Determines if the given cell is either the One Time or Recurring Discount cell by checking the class name.
     * @public
     * @param cell The cell to check.
     * @returns {Boolean} True if the cell is either the One Time or Recurring Discount Cell.
     */
    detailsTable.isDiscountCell = function(cell) {
        return $(cell).hasClass(ONE_TIME_DISCOUNT_CLASS) ||
               $(cell).hasClass(RECURRING_DISCOUNT_CLASS);
    };

    /**
     * Determines if the given cell is either the One Time or Recurring Net cell by checking the class name.
     * @public
     * @param cell The cell to check.
     * @returns {Boolean} True if the cell is either the One Time or Recurring Net Cell.
     */
    detailsTable.isNetCell = function(cell) {
        return $(cell).hasClass(ONE_TIME_NET_CLASS) ||
               $(cell).hasClass(RECURRING_NET_CLASS);
    };

    /**
     * Determines if the given cell is a 'oneTime' or 'recurring' cell.
     * @public
     * @param cell The cell to check.
     * @returns {String} 'oneTime' if the the cell any One Time cell. 'recurring' if the cell is any Recurring cell.
     */
    detailsTable.isOneTimeOrRecurring = function(cell) {

        if (detailsTable.isOneTimeCell(cell)) {
            return "oneTime";
        }

        if (detailsTable.isRecurringCell(cell)) {
            return "recurring";
        }

    };

    /**
     * Determines if the given cell is any One Time cell.
     * @public
     * @param cell The cell to check.
     * @returns {Boolean} True if the cell is any One Time cell. False otherwise.
     */
    detailsTable.isOneTimeCell = function(cell) {
        return $(cell).hasClass(ONE_TIME_VALUE_CLASS)    ||
               $(cell).hasClass(ONE_TIME_DISCOUNT_CLASS) ||
               $(cell).hasClass(ONE_TIME_NET_CLASS);
    };

    /**
     * Determines if the given cell is any Recurring cell.
     * @public
     * @param cell The cell to check.
     * @returns {Boolean} True if the cell is any Recurring cell. False otherwise.
     */
    detailsTable.isRecurringCell = function(cell) {
        return $(cell).hasClass(RECURRING_VALUE_CLASS)    ||
               $(cell).hasClass(RECURRING_DISCOUNT_CLASS) ||
               $(cell).hasClass(RECURRING_NET_CLASS);
    };

    // TODO: Should gross cells only be editable when we have manual pricing strategy? Why do Bid Managers have special privilege here?
    /**
     * Determines if a Gross Cell (One Time or Recurring) should be editable.
     * @public
     * @param {boolean} isManualPricing Is this line Item set for manual pricing.
     * @returns {boolean} True if the Gross Cell should be editable. False otherwise.
     */
    function isGrossCellEditable(isManualPricing) {
        return isManualPricing || pricing.isCurrentUserABidManager();
    }

    /**
     * Determines if a Discount Cell (One Time or Recurring) should be editable.
     * @public
     * @param gross {String} The value in the Gross cell.
     * @param discountingEnabled {boolean} Have discounts been enabled.
     * @returns {boolean} True if the Discount Cell should be editable. False otherwise.
     */
    function isDiscountCellEditable(gross, discountingEnabled) {
        return !utility.isValueZeroOrBlank(gross) && discountingEnabled;
    }

    /**
     * Determine if a Net Cell (One Time or Recurring) should be editable.
     * @public
     * @param gross {String} The value in the Gross cell.
     * @returns {boolean} True if the Net Cell should be editable. False otherwise.
     */
    function isNetCellEditable(gross) {
        return !utility.isValueZeroOrBlank(gross);
    }

    /**
     * Retrieves the Line Item ID for the given row.
     * @public
     * @param row The row to return the Line Item ID of.
     * @returns {String} The Line Item ID for the given row.
     */
    detailsTable.getLineItemId = function(row) {
        return pricing.htmlRowToDataTableRow(pricing.PRICING_DETAILS_TABLE, row).lineItemId;
    };

    /**
     * Retrieves the Price Line ID for the given cell.
     * @public
     * @param row The row to retrieve the Price Line ID of.
     * @param cell A cell in the target row. Used to determine if we are retrieving the Recurring or One Time Price Line ID.
     * @returns {String} Either the Recurring or One Time Price Line ID for the given row.
     */
    detailsTable.getPriceLineID = function(row, cell) {

        // Handle One Time Cells.
        if (detailsTable.isOneTimeCell(cell)) {
            return pricing.htmlRowToDataTableRow(pricing.PRICING_DETAILS_TABLE, row).oneTime.id;
        }

        // Handle Recurring Cells.
        if (detailsTable.isRecurringCell(cell)) {
            return pricing.htmlRowToDataTableRow(pricing.PRICING_DETAILS_TABLE, row).recurring.id;
        }

    };

    // TODO: This should be used when building the table above!
    /**
     * Determines if the value given is a valid discount value for the given cell.
     * A discount is valid value for the cell if;
     * - The value is a number.
     * - The value is not the same as the value in the target cell.
     *
     * @public
     * @param cell The cell the value is a discount value for.
     * @param value The value to discount by.
     * @returns {boolean} True if the discount is valid. False otherwise.
     */
    detailsTable.isValidDiscount = function(cell, value) {
        return utility.isNumeric(value) &&
               !detailsTable.isValueEqualToTargetCell(cell, value);
    };

    /**
     * Checks if the given value is equal to the value in the given DataTable Cell.
     * @param cell The cell containing the old value.
     * @param value The new value to populate into the cell.
     * @returns {boolean} True if the old and new values match. False otherwise.
     */
    detailsTable.isValueEqualToTargetCell = function(cell, value) {
        return cell.revert.asCurrency() === value.asCurrency();
    };

    /**
     * Reverts the discount applied to the given discount.
     * @private
     * @param cell The cell to revert.
     */
    function revertDiscount(cell) {

        if (detailsTable.isDiscountCell(cell)) {
            return cell.revert.asPercent();
        }
        else {
            return cell.revert.asCurrency();
        }

    }

    /**
     * Link the Description checkbox (Select All) on the header bar of the pricing details table with the checkboxes in the cells of the
     * Description column.
     * @private
     */
    function linkSelectAllToDescriptionCheckBoxes() {

        var DESCRIPTION_SELECT_ALL                  = '#selectAll';
        var PRICE_LINES_DESCRIPTION_SELECT_ALL      = '#priceLinesDetails_wrapper #selectAll';
        var DESCRIPTION_CHECKBOXES_NOT_DISABLED     = 'input:not([disabled])[name="listOfPriceLines"]';
        var BULK_DISCOUNT_BUTTON_AND_DISCOUNT_VALUE = '#applyBulkDiscount, #bulkDiscount input.amount, #bulkDiscount input:radio';

        // Action to take when checkbox is checked. Set title to 'Select None' and enable the Bulk Discount Form.
        var checkCallback = function() {
            $(pricing.PRICING_COMMON_ERROR).hide();
            $(DESCRIPTION_SELECT_ALL).attr('title', 'Select None');
            bulkDiscount.enableBulkDiscountElements();
        };

        // Action to take when checkbox is un-checked. Set title to 'Select All' and disable the Bulk Discount Form.
        var unCheckCallback = function () {
            $(pricing.PRICING_COMMON_ERROR).hide();
            $(DESCRIPTION_SELECT_ALL).attr('title', 'Select All');
            bulkDiscount.disableBulkDiscountElements();
        };

        new rsqe.CheckboxGroup(DESCRIPTION_CHECKBOXES_NOT_DISABLED,
            {
                actionButtons : $(BULK_DISCOUNT_BUTTON_AND_DISCOUNT_VALUE),
                select_all    : PRICE_LINES_DESCRIPTION_SELECT_ALL,
                someChecked   : checkCallback,
                allUnchecked  : unCheckCallback
            }
        ).initialize();
    }

    /**
     * Options to provide the EditTable Library.
     * - placeholder - Overwrite the default placeholder for an empty editable cell.
     * - onblur      - When this event is triggered submit the EditTable form. I.e. when the editable cell loses focus.
     * - cssclass    - The css class to mark page elements as being editable. In this case the cells of the PRICING_DETAILS_TABLE.
     * - width       - Fix the width so that when the editable element is selected the containing element does not expand.
     * - height      - Fix the height so that when the editable element is selected the containing element does not expand.
     * @private
     */
    var EDITABLE_OPTIONS = {
        placeholder : "",
        onblur      : 'submit',
        cssclass    : 'editable',
        width       : "100%",
        height      : "100%"
    };

    /**
     * Returns the value to be populated into the editable cell based on the value input by the user.
     * If the value input was invalid the current value in the cell will remain. See {@link isValidDiscount} for a description of a
     * valid discount value.
     *
     * @private
     * @param value The value input by the user.
     * @param context The context that the value comes from. I.e. The this parameter for the cell that was edited.
     * @returns {Number} Returns the value to be set in the cell. Which will be the value entered by the user if it was a valid discount
     *                   value. If the value entered was invalid this function will return no value.
     */
    function applyDiscount(value, context) {

        // The cell that has been changed.
        var cell   = context;

        // TODO: Message when reverting. With reason why.
        // TODO: Add - Discount should be invalid if net makes the Discount equal more than 100%.
        // Revert the cells value if the value given is not a number or the value given is the same as the cells current value.
        if(!detailsTable.isValidDiscount(cell, value)){
            return revertDiscount(cell);
        }

        // The row this checkbox is within. HTML and and as an object.
        var row    = context.parentNode;
        var rowObj = $(context).parent();

        // Discount is either on a 'oneTime' or 'recurring' cell.
        var discountOn  = detailsTable.isOneTimeOrRecurring(cell);
        var lineItemId  = detailsTable.getLineItemId(row);
        var priceLineId = detailsTable.getPriceLineID(row, cell);

        $(row).addClass(discountOn + "ChangeDiscount");

        detailsTable.showUnsavedDiscountsMessage();

        // Apply discount to Gross cell (One Time or Recurring).
        if (detailsTable.isGrossCell(cell)) {
            return pricing.discounts.addNetBasedOnExistingGross(rowObj, discountOn, value, lineItemId, priceLineId);
        }

        // Apply discount to Discount cell (One Time or Recurring).
        if (detailsTable.isDiscountCell(cell)) {
            return pricing.discounts.addDiscount(rowObj, discountOn, value, lineItemId, priceLineId);
        }

        // Apply discount to Net cell (One Time or Recurring).
        if (detailsTable.isNetCell(cell)) {
            return pricing.discounts.addDiscountFromNetTotal(rowObj, discountOn, value, lineItemId, priceLineId);
        }

    }

    /**
     * Sets which cells in the PRICING_DETAILS_TABLE should be editable.
     * This sets the default edibility of the table.
     * This function is called every time the table is drawn.
     * @private
     */
    function setEditableCells() {

        // All of the One Time and Recurring cells can be edited as long as they do not have the readOnly class.
        var EDITABLE_DETAILS_FIELDS_SELECTOR = '.oneTime_value:not(".readOnly"),'              +
                                               '.recurring_value:not(".readOnly"),'            +
                                               '.oneTime_discount:not(:empty, ".readOnly"),'   +
                                               '.oneTime_netTotal:not(:empty, ".readOnly"),'   +
                                               '.recurring_discount:not(:empty, ".readOnly"),' +
                                               '.recurring_netTotal:not(:empty, ".readOnly")';

        /**
         * This function is called whenever the user clicks into an editable cell.
         */
        $(EDITABLE_DETAILS_FIELDS_SELECTOR).editable(

            /**
             * Returns the value to be populated into the editable cell based on the value input by the user.
             * If the value input was invalid the current value in the cell will remain. See {@link isValidDiscount} for a description of a
             * valid discount value.
             * @param value The value input by the user.
             * @returns {Number} Returns the value to be set in the cell. Which will be the value entered by the user if it was a valid discount
             *                   value. If the value entered was invalid this function will return no value.
             */
            function(value) {
                return applyDiscount(value, this);
            },

            EDITABLE_OPTIONS

        );
    }

// Immediately invoke this namespace.
}( rsqe.pricingTab.standardTab.detailsTable = rsqe.pricingTab.standardTab.detailsTable || {} ));