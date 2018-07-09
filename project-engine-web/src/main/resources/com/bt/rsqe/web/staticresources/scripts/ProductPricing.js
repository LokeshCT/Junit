var rsqe = rsqe || {};

/**
 * This class is responsible for pricing the Line Items on the Quote Option Details Tab.
 *
 * It extracts the Line Item ID's from the LineItems Table and calls the LINE_ITEMS_PRICES_URL:
 *  /rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/line-item-prices
 *
 * This end point returns the pricing status of each of the line items, these values replace the existing pricing status for the given
 * line item.
 *
 * The call to LINE_ITEMS_PRICES_URL is asynchronous. The poll function in this class will be called every POLL_DELAY milliseconds until
 * the end points done value returns as true.
 *
 * @returns ProductPricing object with the following functions exposed:
 *
 *  {
 *      {
 *          priceLineItems   : pollForPrices,
 *          setErrorCallBack : setErrorCallBack
 *      }
 *  }
 *
 * @constructor
 */
rsqe.ProductPricing = function() {

    // This end point called to calculate prices for the given line item ids, returning the status for each.
    var LINE_ITEMS_PRICES_URL = $("#pricingUrl").text();

    // How long to wait between polls of the LINE_ITEMS_PRICES_URL, in milliseconds.
    var POLL_DELAY = 10000;

    // Value to display in Status cells that where still awaiting a real status from the backend when the AJAX call failed.
    var STATUS_ERROR_VALUE = 'ERROR';

    // Image to display in the Pricing Status cells whilst awaiting the completion of the AJAX call that returns the status.
    var SPINNER_HTML = '<img src="/rsqe/project-engine/static/images/cell_validate.gif">';

    var errorCallBack;

    /**
     * Expose the PollForPrices and error callback to calling classes.
     */
    return {

        priceLineItems   : pollForPrices,
        setErrorCallBack :
            function(callBack) {
                errorCallBack = callBack;
            }

    };

    /**
     * Price the given Line Items.
     * @public
     * @param lineItems The Line Items to Price.
     */
    function pollForPrices(lineItems) {

        // Filter out any instance of SPINNER_HTML.
        var processedLineItems = filterLineItems(lineItems);

        // Only proceed if there are Line Items to price.
        if (processedLineItems != '') {
            poll(processedLineItems);
        }

    }

    // FIXME: We should disable the Calculate Price button on the Quote Option Details tab rather than just greying it out.
    // Once done, this function can be removed. Functionality to achieve this is implemented in Utility.js.
    /**
     * Filters out SPINNER_HTML from the Pricing Status cells.
     * The SPINNER_HTML value should only be present if the user clicks Calculate Price whilst price calculation is ongoing.
     * @private
     * @param lineItemIds The comma separated list of Line Item IDs to be filtered.
     * @returns {string} The input list with any instance of SPINNER_HTML filtered out.
     */
    function filterLineItems(lineItemIds) {

        return _.filter(

            // Filter out any instance of SPINNER_HTML
            lineItemIds.split(','),
            function(lineItemId) {
                return $(getStatusCell(lineItemId)).html() != SPINNER_HTML;
            }

        // Rebuild the list.
        ).join(',');

    }

    /**
     * Polls the LINE_ITEMS_PRICES_URL end point every POLL_DELAY milliseconds until the end points done value is true.
     * @private
     * @param lineItems The Line Items being priced.
     */
    function poll(lineItems) {

        // Display loading image whilst the price is calculated.
        setStatusToLoadingImage(lineItems);

        $.get(buildLineItemPricesUrl(lineItems))

            .error(function(response) {
                setStatusToError();
                activateQuoteOptionDetailsTabButtons();
                errorCallBack(response);
            })

            .success(function(data) {

                data = JSON.parse(data);

                // If the asynchronous call is not complete call poll() again after POLL_DELAY milliseconds. Repeat until done.
                if (!data.done) {
                    setTimeout(
                        function () {
                            poll(lineItems);
                        },
                        POLL_DELAY
                    );
                }

                // If the asynchronous call is complete update the pricing status to the values returned by the LINE_ITEMS_PRICES_URL.
                else {
                    updatePriceStatus(data.response);
                    activateQuoteOptionDetailsTabButtons();
                }

            })
    }

    /**
     * Update the Pricing Status cell for each of the Price Lines in the response object.
     * @private
     * @param response An AJAX response from calling the LINE_ITEMS_PRICES_URL.
     */
    function updatePriceStatus(response) {

        _.each(response, function(lineItem) {

            var id     = lineItem.lineItemId;
            var status = lineItem.status;
            var errors = lineItem.errors;

            // Update the status of the cell on the row with the given ID.
            $(getStatusCell(id)).html(status);

            // The HTML row with the id.
            var row = $(buildRowId(id));

            // Set the Validity of the line item based on whether it has any errors.
            row.find(".validity").text(hasErrors(errors) ? 'INVALID' : 'VALID');

            // Display any error messages for the line items.
            displayPricingErrors(row, errors);

        });

    }

    /**
     * Sets the overall error message to display at the top of the page if there were any issues with Price Calculation.
     * Updates the Line Items Table to display any Line Item specific errors in the table.
     * @private
     * @param row The row the errors belong to.
     * @param errors The array of errors for the given the Line Items priced by the user.
     */
    function displayPricingErrors(row, errors) {

        if (hasErrors(errors) && errorCallBack) {

            errorCallBack(
                {
                    responseText : 'Some items failed to Price. See below for further information.'
                }
            );

        }

        // Update the Option Details Table to display any errors.
        new rsqe.optiondetails.DataTable().updatePricingErrorRow(row, errors);

    }

    /**
     * Set the status cells to loading image whilst prices are calculated.
     * Replaces the status of each of the given Line Items status cell with a loading image.
     * If the Line Item's status is set as 'N/A' do nothing.
     * @private
     * @param lineItemIds The Line Item's whose status should be updated.
     */
    function setStatusToLoadingImage(lineItemIds) {

        var array = lineItemIds.split(',');

        array.forEach(function(lineItemId) {

            var row = $(getStatusCell(lineItemId));

            // TODO: Why do we check for 'N/A'? Products that cannot be priced?
            // Change the the Line Items with the given IDs to a loading image until their status is returned.
            if (row.html() != 'N/A') {
                row.html(SPINNER_HTML);
            }

        });

    }

    /**
     * Sets the status cells to STATUS_ERROR_VALUE.
     * Check each of the Price Lines Status cells. If they still display the SPINNER_HTML replace the spinner with STATUS_ERROR_VALUE.
     * @private
     */
    function setStatusToError() {
        $('.pricingStatus').each(function() {
            if ($(this).html() == SPINNER_HTML) {
                $(this).html(STATUS_ERROR_VALUE);
            }
        })
    }

    /**
     * Checks if any of the Line Items on the Quote Option Details Tab have been checked.
     * @returns {boolean} True if there are any Checked Line Items. False otherwise.
     */
    function isLineItemChecked() {
        return $('#lineItems').find('input[type=checkbox]:checked').length > 0;
    }

    /**
     * Determines if there are any errors in the error input.
     * @param error An array of error messages.
     * @returns {boolean} True if there is more than one error. False otherwise.
     */
    function hasErrors(error) {
        return error && error.length > 0;
    }

    /**
     * Builds the URL used to call the price calculation.
     * Each Line Item ID is appended onto the Base URL separated by commas.
     * @private
     * @param lineItemIds A comma separated list og Line Item IDs to append onto the LINE_ITEMS_PRICES_URL.
     * @returns {string} The complete URL.
     */
    function buildLineItemPricesUrl(lineItemIds) {
        return LINE_ITEMS_PRICES_URL + '/' + lineItemIds
    }

    /**
     * Returns a Jquery selector for the row with the given Line Item ID.
     * @private
     * @param lineItemId The Line Item ID for the row.
     * @returns {string} A Jquery selector for the row with the given Line Item ID.
     */
    function buildRowId(lineItemId) {
        return "#id_" + lineItemId
    }

    /**
     * Returns a Jquery selector for the Status cell whose row has the given Line Item ID.
     * @private
     * @param lineItemId The Line Item ID for the cell's row.
     * @returns {string} A Jquery selector for the status cell of the row with the given Line Item ID.
     */
    function getStatusCell(lineItemId) {
        return buildRowId(lineItemId) + ' .pricingStatus'
    }

    /**
     * Reactivate the buttons on the Quote Option Details Tab.
     */
    function activateQuoteOptionDetailsTabButtons() {

        // Only enable the action buttons if there are checked Line Items.
        if (isLineItemChecked()) {
            enableActionButtons();
        }
        enableButtons();

    }

    /**
     * Enables all elements with the '.btnDisable' class.
     *
     * Enables the following buttons:
     * - Add Product
     * - Configure Product
     * - Add/Edit Attachments
     * - Locate On Google Map
     *
     * @private
     */
    function enableButtons() {
        $('.btnDisable')
            .addClass('enabled')
            .removeClass('disabled')
            .disableable('enable');
    }

    /**
     * Enables all elements with the '.actionBtnDisable' class.
     *
     * Enables the following buttons:
     * - Calculate Price
     * - Create Offer
     * - Raise IFC
     *
     * @private
     */
    function enableActionButtons() {
        $('.actionBtnDisable')
            .addClass('enabled')
            .removeClass('disabled')
            .disableable('enable');
    }

};