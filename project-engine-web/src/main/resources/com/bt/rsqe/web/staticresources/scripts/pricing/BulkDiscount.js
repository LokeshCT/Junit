/**
 * This namespace defines the Bulk Discount functionality.
 * Currently it is used by the {@link standardTab}.
 *
 * @namespace bulkDiscount
 * @bulkDiscount The object to append all functionality to.
 * @undefined This parameter prevents undefined from being redefined by other JavaScript code.
 */
(function(bulkDiscount, undefined) {

    // Short names for Pricing namespaces.
    var pricing = rsqe.pricingTab;
    var utility = rsqe.utility;

    // Text input fields:
    var BULK_ONE_TIME_PERCENT_FIELD            = '#bulkDiscountOneTimePercent';
    var BULK_ONE_TIME_NET_FIELD                = '#bulkDiscountOneTimeNett';
    var BULK_RECURRING_PERCENT_FIELD           = '#bulkDiscountRecurringPercent';
    var BULK_RECURRING_NET_FIELD               = '#bulkDiscountRecurringNett';

    // The radio buttons next to the text input fields.
    var BULK_ONE_TIME_PERCENT_RADIO_SELECTOR   = "#pricingBulkDiscount input:radio[name='oneTime'][value='percent']";
    var BULK_ONE_TIME_NET_RADIO_SELECTOR       = "#pricingBulkDiscount input:radio[name='oneTime'][value='nett']";
    var BULK_RECURRING_PERCENT_RADIO_SELECTOR  = "#pricingBulkDiscount input:radio[name='recurring'][value='percent']";
    var BULK_RECURRING_NET_RADIO_SELECTOR      = "#pricingBulkDiscount input:radio[name='recurring'][value='nett']";

    // Button executes the bulk discount.
    var APPLY_BULK_DISCOUNT_BUTTON             = '#applyBulkDiscount';

    /**
     * Initialize the Bulk Discount element. Attaching all of it's events.
     * @public
     */
    bulkDiscount.initialize = function() {
        attachEvents();
    };

    /**
     * Attach HTML events for the elements of the Standard Tab.
     * @private
     */
    function attachEvents() {

        /**
         * This function is called when the user applies changes made to the discounts on the PRICING_DETAILS_TABLE using the
         * APPLY_BULK_DISCOUNT_BUTTON (#applyBulkDiscount) button.
         */
        $(APPLY_BULK_DISCOUNT_BUTTON).click(function() {
            bulkDiscount.applyBulkDiscounts(pricing.PRICING_DETAILS_TABLE);
            bulkDiscount.resetBulkDiscountForm();
        });

        /**
         * User clicked on the One Time percent radio selector.
         */
        $(BULK_ONE_TIME_PERCENT_RADIO_SELECTOR).click(function() {
            $(BULK_ONE_TIME_NET_FIELD).val("");
            $(BULK_ONE_TIME_PERCENT_FIELD).focus();
        });

        /**
         * User clicked on the One Time net radio selector.
         */
        $(BULK_ONE_TIME_NET_RADIO_SELECTOR).click(function() {
            $(BULK_ONE_TIME_PERCENT_FIELD).val("");
            $(BULK_ONE_TIME_NET_FIELD).focus();
        });

        /**
         * User clicked on the Recurring percent radio selector.
         */
        $(BULK_RECURRING_PERCENT_RADIO_SELECTOR).click(function() {
            $(BULK_RECURRING_NET_FIELD).val("");
            $(BULK_RECURRING_PERCENT_FIELD).focus();
        });

        /**
         * User clicked on the Recurring net radio selector.
         */
        $(BULK_RECURRING_NET_RADIO_SELECTOR).click(function() {
            $(BULK_RECURRING_PERCENT_FIELD).val("");
            $(BULK_RECURRING_NET_FIELD).focus();
        });

        /**
         * User clicked in the One Time percent text field.
         */
        $(BULK_ONE_TIME_PERCENT_FIELD).focus(function() {
            $(BULK_ONE_TIME_NET_FIELD).val("");
            $(BULK_ONE_TIME_PERCENT_RADIO_SELECTOR).attr("checked", "checked");
        });

        /**
         * User clicked in the One Time net text field.
         */
        $(BULK_ONE_TIME_NET_FIELD).focus(function() {
            $(BULK_ONE_TIME_PERCENT_FIELD).val("");
            $(BULK_ONE_TIME_NET_RADIO_SELECTOR).attr("checked", "checked");
        });

        /**
         * User clicked in the Recurring percent text field.
         */
        $(BULK_RECURRING_PERCENT_FIELD).focus(function() {
            $(BULK_RECURRING_NET_FIELD).val("");
            $(BULK_RECURRING_PERCENT_RADIO_SELECTOR).attr("checked", "checked");
        });

        /**
         * User clicked in the Recurring net text field.
         */
        $(BULK_RECURRING_NET_FIELD).focus(function() {
            $(BULK_RECURRING_PERCENT_FIELD).val("");
            $(BULK_RECURRING_NET_RADIO_SELECTOR).attr("checked", "checked");
        });

    }

    /**
     * Resets the Bulk Discount Forms radio buttons to their default position, and empties the values of the input fields.
     * @public
     */
    bulkDiscount.resetBulkDiscountForm = function() {
        $(BULK_ONE_TIME_PERCENT_RADIO_SELECTOR) .prop('checked', true);
        $(BULK_RECURRING_PERCENT_RADIO_SELECTOR).prop('checked', true);
        $(BULK_ONE_TIME_PERCENT_FIELD)          .val("");
        $(BULK_ONE_TIME_NET_FIELD)              .val("");
        $(BULK_RECURRING_PERCENT_FIELD)         .val("");
        $(BULK_RECURRING_NET_FIELD)             .val("");
    };

    /**
     * Disables the bulk discounts radio buttons, input fields, and submit button.
     * @public
     */
    bulkDiscount.disableBulkDiscountElements = function() {
        $(BULK_ONE_TIME_PERCENT_FIELD)          .prop("disabled", true);
        $(BULK_ONE_TIME_NET_FIELD)              .prop("disabled", true);
        $(BULK_RECURRING_PERCENT_FIELD)         .prop("disabled", true);
        $(BULK_RECURRING_NET_FIELD)             .prop("disabled", true);
        $(BULK_ONE_TIME_PERCENT_RADIO_SELECTOR) .prop("disabled", true);
        $(BULK_ONE_TIME_NET_RADIO_SELECTOR)     .prop("disabled", true);
        $(BULK_RECURRING_PERCENT_RADIO_SELECTOR).prop("disabled", true);
        $(BULK_RECURRING_NET_RADIO_SELECTOR)    .prop("disabled", true);
        $(APPLY_BULK_DISCOUNT_BUTTON)           .prop("disabled", true);
    };

    /**
     * Enables the Bulk Discount radio buttons, input fields, and submit button.
     * @public
     */
    bulkDiscount.enableBulkDiscountElements = function() {
        $(BULK_ONE_TIME_PERCENT_FIELD)          .prop("disabled", false);
        $(BULK_ONE_TIME_NET_FIELD)              .prop("disabled", false);
        $(BULK_RECURRING_PERCENT_FIELD)         .prop("disabled", false);
        $(BULK_RECURRING_NET_FIELD)             .prop("disabled", false);
        $(BULK_ONE_TIME_PERCENT_RADIO_SELECTOR) .prop("disabled", false);
        $(BULK_ONE_TIME_NET_RADIO_SELECTOR)     .prop("disabled", false);
        $(BULK_RECURRING_PERCENT_RADIO_SELECTOR).prop("disabled", false);
        $(BULK_RECURRING_NET_RADIO_SELECTOR)    .prop("disabled", false);
        $(APPLY_BULK_DISCOUNT_BUTTON)           .prop("disabled", false);
    };

    /**
     * This function is called when the Apply Discount button in the right panel is clicked.
     *
     * Applies the given One Time or Recurring percent or net discount to all price lines of the PRICING_DETAILS_TABLE who's description
     * checkbox has been checked.
     * @public
     * @param table The table to apply the bulk discounts to.
     */
    bulkDiscount.applyBulkDiscounts = function(table) {

        // Parameters required by the Discounts class to determine whether to discount on One Time or Recurring value.
        var discountOnOneTime   = "oneTime";
        var discountOnRecurring = "recurring";

        // The user is only able to enter a single value per Bulk Discount application. Retrieve this value.
        var discount = getBulkDiscountValue();

        getEditablePriceLines().each(function(index, row) {

            // Only apply discounts to rows that have their description checkbox checked.
            if (isCheckboxChecked(row)) {

                var lineItemID  = pricing.htmlRowToDataTableRow(table, row).lineItemId;
                var oneTimeID   = pricing.htmlRowToDataTableRow(table, row).oneTime.id;
                var recurringID = pricing.htmlRowToDataTableRow(table, row).recurring.id;
                var rowObj      = $(row);

                // Handle one time discount values.
                if (isOneTimeDiscountValueValid(discount, table, row)) {

                    if (isOneTimeBulkDiscountPercentValueSet()) {
                        pricing.discounts.addDiscount(rowObj, discountOnOneTime, discount, lineItemID, oneTimeID);
                    }

                    if (isOneTimeBulkDiscountNetValueSet()) {
                        pricing.discounts.addDiscountFromNetTotal(rowObj, discountOnOneTime, discount, lineItemID, oneTimeID);
                    }

                }

                // Handle Recurring discount values.
                if (isRecurringDiscountValueValid(discount, table, row)) {

                    if (isRecurringBulkDiscountPercentValueSet()) {
                        pricing.discounts.addDiscount(rowObj, discountOnRecurring, discount, lineItemID, recurringID);
                    }

                    if (isRecurringBulkDiscountNetValueSet()) {
                        pricing.discounts.addDiscountFromNetTotal(rowObj, discountOnRecurring, discount, lineItemID, recurringID);
                    }

                }
            }
        });

        // TODO: What does this do?
        pricing.discounts.refreshDiscounts();

    };

    /**
     * Returns all editable rows of the PRICING_DETAILS_TABLE.
     * @private
     * @returns {*|jQuery|HTMLElement} A list of HTML rows of the PRICING_DETAILS_TABLE that can be edited (i.e. not read only).
     */
    function getEditablePriceLines() {
        return $("tr.priceLine:not('.readOnly')");
    }

    /**
     * Checks that the checkbox on the given row has been checked.
     * @private
     * @param row The row to check.
     * @returns {Boolean} True if the checkbox has been checked. False otherwise.
     */
    function isCheckboxChecked(row) {
        return $("input:checkbox", row).is(':checked');
    }

    /**
     * The user is only able to enter a single value per Bulk Discount application, one of the following;
     * - One Time Percent
     * - One Time Net
     * - Recurring Percent
     * - Recurring Net
     *
     * Retrieve the discount value set by the user in the Bulk Discount Form.
     *
     * @private
     * @returns {Number} The single value that was present in teh Bulk Discount Form.
     */
    function getBulkDiscountValue() {

        if (isOneTimeBulkDiscountPercentValueSet()) {
            return $(BULK_ONE_TIME_PERCENT_FIELD).val();
        }

        if (isOneTimeBulkDiscountNetValueSet()) {
            return $(BULK_ONE_TIME_NET_FIELD).val();
        }

        if (isRecurringBulkDiscountPercentValueSet()) {
            return $(BULK_RECURRING_PERCENT_FIELD).val();
        }

        if (isRecurringBulkDiscountNetValueSet()) {
            return $(BULK_RECURRING_NET_FIELD).val();
        }

    }

    /**
     * Determines if the One Time Percent value has been entered into the Bulk Discount Form.
     * @private
     * @returns {boolean} True if the One Time Percent value has been entered. False otherwise.
     */
    function isOneTimeBulkDiscountPercentValueSet() {
        var oneTimePercent = $(BULK_ONE_TIME_PERCENT_FIELD).val();
        return !utility.isBlank(oneTimePercent);
    }

    /**
     * Determines if the One Time Net value has been entered into the Bulk Discount Form.
     * @private
     * @returns {boolean} True if the One Time Net value has been entered. False otherwise.
     */
    function isOneTimeBulkDiscountNetValueSet() {
        var oneTimeNet = $(BULK_ONE_TIME_NET_FIELD).val();
        return !utility.isBlank(oneTimeNet);
    }

    /**
     * Determines if the Recurring Percent value has been entered into the Bulk Discount Form.
     * @private
     * @returns {boolean} True if the Recurring Percent value has been entered. False otherwise.
     */
    function isRecurringBulkDiscountPercentValueSet() {
        var recurringPercent = $(BULK_RECURRING_PERCENT_FIELD).val();
        return !utility.isBlank(recurringPercent);
    }

    /**
     * Determines if a Recurring Net value has been entered into the Bulk Discount Form.
     * @private
     * @returns {boolean} True if the Recurring Net value has been entered. False otherwise.
     */
    function isRecurringBulkDiscountNetValueSet() {
        var recurringNet = $(BULK_RECURRING_NET_FIELD).val();
        return !utility.isBlank(recurringNet);
    }

    /**
     * Checks that the given table row is valid for the given One Time discount value to be applied.
     * @private
     * @param discount The one time value we are attempting to discount by.
     * @param table The table the row belongs to.
     * @param row The table row the discount is to applied to.
     * @returns {boolean} True if the discount is valid. False otherwise.
     */
    function isOneTimeDiscountValueValid(discount, table, row) {
        var recurringGrossValue = pricing.htmlRowToDataTableRow(table, row).oneTime.value;
        return isDiscountValueValid(discount, row, "onetime_id", recurringGrossValue);
    }

    /**
     * Checks that the given table row is valid for the given Recurring discount value to be applied.
     * @private
     * @param discount The recurring value we are attempting to discount by.
     * @param table The table the row belongs to.
     * @param row The table row the discount is to applied to.
     * @returns {boolean} True if the discount is valid. False otherwise.
     */
    function isRecurringDiscountValueValid(discount, table, row) {
        var recurringGrossValue = pricing.htmlRowToDataTableRow(table, row).recurring.value;
        return isDiscountValueValid(discount, row, "recurring_id", recurringGrossValue);
    }

    /**
     * Checks that the given table row is valid for the given discount value to be applied.
     * The row must have an ID, the discount value must be numeric, and the relevant gross field in the table must be non blank.
     * @private
     * @param discount The value wea re attempting to discount by.
     * @param row The table row the discount is to applied to.
     * @param idType The id type of the row. Should be either; 'onetime_id' or 'recurring_id'.
     * @param gross Either the recurring or one time gross value of the given row.
     * @returns {boolean} True if the discount is valid. False otherwise.
     */
    function isDiscountValueValid(discount, row, idType, gross) {
        return utility.isNumeric(discount) && rowHasID(row, idType) && !utility.isBlank(gross);
    }

    /**
     * Checks if the given row has an id of the given type.
     * @private
     * @param row The row to check.
     * @param id The type of id to search for.
     * @returns {boolean} True if the row has the id of the given type. False otherwise.
     */
    function rowHasID(row, id) {
        return $(row).attr(id) != "id_";
    }

}(
    // Immediately invoke this namespace.
    rsqe.pricingTab.bulkDiscount = rsqe.pricingTab.bulkDiscount || {}
));