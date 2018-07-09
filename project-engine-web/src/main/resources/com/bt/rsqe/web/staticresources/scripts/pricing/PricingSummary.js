/**
 * This namespace defines the Pricing Summary Table displayed on the {@link standardTab} and CostTab.
 *
 * @namespace pricingSummary
 * @pricingSummary The object to append all functionality to.
 * @undefined This parameter prevents undefined from being redefined by other JavaScript code.
 */
(function(pricingSummary, undefined) {

    // Short names for the namespaces used here.
    var utility = rsqe.utility;

    // The toggle that sits alongside each of the Totals.
    var PRICING_SUMMARY_TOTAL_TOGGLE           = '.totalPrice';

    /**
     * Initialize the One Time, Recurring, and Usage Total toggles on the Pricing Summary (right pane).
     * @public
     */
    pricingSummary.initialize = function() {
        initPricingSummaryToggle();
    };

    /**
     * Retrieves the Pricing Summary data for display in the Pricing Summary from the PRODUCT_PRICE_SUMMARY end point, any params given
     * will be appended to the end of the URL.
     * @public
     * @param oneTimeGross The page element to populate with the One Time Gross value.
     * @param oneTimeDiscount The page element to populate with the One Time Discount value.
     * @param oneTimeNet The page element to populate with the One Time Net value.
     * @param recurringGross The page element to populate with the Recurring Gross value.
     * @param recurringDiscount The page element to populate with the Recurring Discount value.
     * @param recurringNet The page element to populate with the Recurring Net value.
     * @param usageOffNet The page element to populate with the Usage Off Net value.
     * @param usageOnNet The page element to populate with the Usage On Net value.
     * @param usageTotal The page element to populate with the Usage Total value.
     * @param params Parameters to append to the PRODUCT_PRICE_SUMMARY URL. Can be undefined.
     */
    pricingSummary.populatePricingSummary = function(oneTimeGross,
                                                     oneTimeDiscount,
                                                     oneTimeNet,
                                                     recurringGross,
                                                     recurringDiscount,
                                                     recurringNet,
                                                     usageOffNet,
                                                     usageOnNet,
                                                     usageTotal,
                                                     params) {

        var url = rsqe.urlBuilder.PRODUCT_PRICE_SUMMARY;

        if(!utility.isBlank(params)) {
            url += params;
        }

        /**
         * Populate the given page elements with the values returned by the PRODUCT_PRICE_SUMMARY end point.
         */
        $.get(url, {}, function(data) {
            oneTimeGross        .text(data.totalOneTimeGross);
            oneTimeDiscount     .text(data.totalOneTimeDiscount + "%");
            oneTimeNet          .text(data.totalOneTimeNet);
            recurringGross      .text(data.totalRecurringGross);
            recurringDiscount   .text(data.totalRecurringDiscount + "%");
            recurringNet        .text(data.totalRecurringNet);
            usageOffNet         .text(data.totalOffNetUsage);
            usageOnNet          .text(data.totalOnNetUsage);
            usageTotal          .text(data.totalUsage);
        });
    };

    /**
     * Toggle to hide or display the One Time, Recurring, and Usage Totals.
     * @private
     */
    function initPricingSummaryToggle() {

        // Toggle onClick event.
        $(PRICING_SUMMARY_TOTAL_TOGGLE).click(function() {
            $(this).next(".discounts").slideToggle("slow,");
        });

        $(PRICING_SUMMARY_TOTAL_TOGGLE).toggle(

            // Toggle on.
            function() {
                $(this).addClass("active");
            },

            // Toggle off.
            function() {
                $(this).removeClass("active");
            }
        );

    }

}(
    // Immediately invoke this namespace.
    rsqe.pricingTab.pricingSummary = rsqe.pricingTab.pricingSummary || {}
));