/**
 * This namespace defines all functionality required to build the {@link pricingTab}s Usage Tab.
 *
 * @namespace usageTab
 * @usageTab The object to append all logic onto.
 * @undefined This parameter prevents undefined from being redefined by other JavaScript code.
 */
(function(usageTab, undefined) {

    // Buttons to save or discard usage changes.
    var USAGE_SAVE_BUTTON     = '#usageSave';
    var USAGE_DISCARD_BUTTON  = '#usageDiscard';

    // Messages displayed to the user.
    var USAGE_WARN_MESSAGE    = '#usageWarn';
    var USAGE_SUCCESS_MESSAGE = '#usageSuccess';

    /**
     * Initialise all functionality required by the Cost Tab.
     * @public
     */
    usageTab.initialize = function() {

        // Initialise the Standard tab tables.
        usageTab.usageTable.initialize();

        // Attach HTML event handlers.
        attachEvents();

        // Hide messages on initial load of the tab.
        usageTab.hideWarnMessage();
        usageTab.hideSuccessMessage();

    };

    /**
     * Show the USAGE_WARN_MESSAGE.
     * @public
     */
    usageTab.showWarnMessage = function() {
        $(USAGE_WARN_MESSAGE).show();
    };

    /**
     * Hide the USAGE_WARN_MESSAGE.
     * @public
     */
    usageTab.hideWarnMessage = function() {
        $(USAGE_WARN_MESSAGE).hide();
    };

    /**
     * Show the USAGE_SUCCESS_MESSAGE.
     * @public
     */
    usageTab.showSuccessMessage = function() {
        $(USAGE_SUCCESS_MESSAGE).show();
    };

    /**
     * Hide the USAGE_SUCCESS_MESSAGE.
     * @public
     */
    usageTab.hideSuccessMessage = function() {
        $(USAGE_SUCCESS_MESSAGE).hide();
    };

    /**
     * Attach HTML events for the elements of the Standard Tab.
     * @private
     */
    function attachEvents() {

        $(USAGE_SAVE_BUTTON).click(function() {
            usageTab.usageTable.persistUsageCharges();
        });

        $(USAGE_DISCARD_BUTTON).click(function() {
            usageTab.usageTable.refreshUsageCharges();
        });

    }

// Immediately invoke this namespace.
}( rsqe.pricingTab.usageTab = rsqe.pricingTab.usageTab || {} ));