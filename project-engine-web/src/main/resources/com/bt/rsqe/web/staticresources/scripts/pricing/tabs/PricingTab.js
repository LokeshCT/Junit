// TODO: Simplify the pricing namespace so that all elements of pricing exist under one namespace whilst keeping each in separate files.
/**
 * This namespace defines the shared functionality for the Pricing tab.
 *
 * This is an eventual replacement for {@link rsqe.QuoteOptionPricingTab}.
 *
 * The PricingTab created by the {@link rsqe.BasePage} file's tabLoaded function if the 'useNewPricingTab' row in the
 * APPLICATION_PROPERTY_STORE table of the Inlife database is set true. This switch determines whether the new or old pricing implementation
 * is used. If the value is set to false the {@link QuoteOptionPricingTab} class is created (I.e. The old pricing JavaScript class).
 *
 * Pricing JavaScript files:
 *
 * Tabs:
 * - {@link pricingTab}
 * - {@link standardTab}
 * - {@link costTab}
 * - {@link usageTab}
 *
 * Tables:
 * - {@link detailsTable}
 * - {@link summaryTable}
 * - {@link revenueTable}
 * - {@link costTable}
 * - {@link usageTable}
 *
 * Other:
 * - {@link bulkDiscount}
 * - {@link pricingSummary}
 * - {@link BCM}
 *
 * Utility Classes:
 * - {@link urlBuilder}
 * - {@link utility}
 *
 * @namespace pricingTab
 * @pricingTab The object to append all functionality to.
 * @undefined This parameter prevents undefined from being redefined by other JavaScript code.
 */
(function(pricingTab, undefined) {

    // Short names for the namespaces used here.
    var utility                                = rsqe.utility;

    // FreeMarker Template Values:
    var QUOTE_OPTION_NAME                      = '#quoteName';
    var QUOTE_CURRENCY                         = '#quoteCurrency';
    var HAS_INDIRECT_ACCESS                    = '#hasIndirectAccess';
    var HAS_EUP_ACCESS                         = '#hasEupAccess';
    var HAS_BCM_ACCESS                         = '#hasBcmAccess';
    var COST_DISCOUNT_APPLICABLE               = '#costDiscountApplicable';
    var HAS_COST_PRICING_TAB_ACCESS            = '#hasCostPricingTabAccess';

    // Right Pane:
    var RIGHT_PANE_SELECTOR                    = '#rightPane';

    // Pricing Tab Selection
    var PRICING_TYPE_RADIO                     = '#pricingTypeRadio';

    // Tables
    pricingTab.PRICING_DETAILS_TABLE           = '#priceLinesDetails';
    pricingTab.PRICING_SUMMARY_TABLE           = '#priceLinesSummary';
    pricingTab.PRICING_REVENUE_TABLE           = '#revenueTable';
    pricingTab.USAGE_PRICING_TABLE             = '#usagePriceLines';
    pricingTab.COST_PRICING_TABLE              = '#costPriceLines';

    // User Message Elements:
    pricingTab.UNSAVED_DISCOUNT_MESSAGE        = '#unsavedDiscounts';
    var SAVE_MESSAGE                           = '#saveMessage';

    // TODO: EXTRACT INTO OWN CLASS.
    // Request Discount Dialog:
    pricingTab.REQUEST_DISCOUNT_POPUP_BUTTON   = '#requestDiscountPopupButton';
    pricingTab.SEND_DISCOUNT_APPROVAL_MESSAGE  = '#sendDiscountApprovalMessage';
    pricingTab.COMMERCIAL_NON_STANDARD_REQUEST = '#commercialNonStandardRequest';

    var REQUEST_DISCOUNT_LOADING_MESSAGE       = '#requestDiscountLoadingMessage';
    var CUSTOMER_GROUP_EMAIL_ID                = '#customerGroupEmailId';
    var SALES_USER_NEW_COMMENT                 = '#salesUserNewComment';
    var COMMENTS_DIV                           = '#commentsDiv';
    var BID_MANAGER_LIST                       = '#bidManagerList';
    var OKAY_DISCOUNT_APPROVAL_SUCCESS         = '#okDiscountApprovalSuccess';
    var REQUEST_DISCOUNT_DIALOG                = '#requestDiscountDialog';
    var REQUEST_DISCOUNT_DIALOG_FORM           = '#requestDiscountDialogForm';

    /**
     * Initialise all functionality required by the Pricing tab.
     * @public
     */
    pricingTab.initialise = function() {

        // Initialize the tab selection.
        initPricingTypeRadio();

        // Construct the URLs to be used across pricing.
        rsqe.urlBuilder.initialize();

        // Initialize the Pricing Summary in the Right Pane.
        rsqe.pricingTab.pricingSummary.initialize();

        // Initialize BCM
        pricingTab.BCM.initialize();

        // Initialize the Pricing Tabs.
        pricingTab.standardTab.initialize();
        pricingTab.costTab.initialize();
        pricingTab.usageTab.initialize();

        // Setup the summary on the right pane.
        setupRightPaneSummary();

        // Initialize User Messages:
        initCommonError();

        // Setup Discounting.
        initDiscounts();
        setupRequestDiscountDialog();

        // Attach Pricing wide HTML events.
        attachEvents();

    };

    /**
     * Define constants for each of the priceLines DataTable columns.
     * @public
     */
    pricingTab.PRICE_LINE_TABLE_COLUMN_NAME = {

        SITE                  : "site",
        MINI_ADDRESS          : "miniAddress",
        PRODUCT               : "product",
        SUMMARY               : "summary",
        DISCOUNT_STATUS       : "discountStatus",
        OFFER_NAME            : "offerName",
        DESCRIPTION           : "description",
        PRICE_STATUS          : "status",
        ONE_TIME_RRP          : "oneTime.rrp",
        ONE_TIME_GROSS        : "oneTime.value",
        ONE_TIME_DISCOUNT     : "oneTime.discount",
        ONE_TIME_NET_TOTAL    : "oneTime.netTotal",
        RECURRING_RRP         : "recurring.rrp",
        RECURRING_GROSS       : "recurring.value",
        RECURRING_DISCOUNT    : "recurring.discount",
        RECURRING_NET_TOTAL   : "recurring.netTotal"

    };

    /**
     * Returns the name of the current quote option.
     * @returns {String} The current quotes name.
     */
    pricingTab.getQuoteOptionName = function() {
        return $(QUOTE_OPTION_NAME).val();
    };

    /**
     * Returns the currency assigned to the current quote.
     * @returns {String} The currency assigned to the current quote.
     */
    pricingTab.getCurrency = function() {
        return $(QUOTE_CURRENCY).html();
    };

    // TODO: What is Indirect Access?
    /**
     * Determines if the current user has Indirect Access.
     * This value is pulled from the Free Marker template which is populated by the backend.
     * @public
     * @returns {boolean} True if the user has indirect access. False otherwise.
     */
    pricingTab.hasIndirectAccess = function() {
        return $(HAS_INDIRECT_ACCESS).html();
    };

    // TODO: What is EUP Access?
    /**
     * Determines if the current user has EUP Access.
     * This value is pulled from the Free Marker template which is populated by the backend.
     * @public
     * @returns {boolean}
     */
    pricingTab.hasEupAccess = function() {
        return $(HAS_EUP_ACCESS).html() == true;
    };

    /**
     * Determine if the current user has access to the BCM Actions.
     * @public
     * @returns {Boolean} True if the current user has BCM Access. False otherwise.
     */
    pricingTab.hasBcmAccess = function() {
        return $(HAS_BCM_ACCESS).html();
    };

    // TODO: What is Cost Discount applicable.
    /**
     * Determines if cost discount is applicable.
     * @returns {Boolean} True if the Cost Discount is applicable.
     */
    pricingTab.costDiscountApplicable = function() {
        return $(COST_DISCOUNT_APPLICABLE).html();
    };

    /**
     * Returns whether or not the current user has access to the Cost Tab.
     * @returns {boolean} True if the current user has Cost Tab Access. False otherwise.
     */
    pricingTab.hasCostTabAccess = function() {
        return $(HAS_COST_PRICING_TAB_ACCESS).html() == "true";
    };

    /**
     * Tests if the current user is a Bid Manager.
     * @public
     * @returns {boolean} True if the user is a Bid Manager. false otherwise.
     */
    pricingTab.isCurrentUserABidManager = function() {
        var BID_MANAGER_COOKIE_NAME = "BID_MGR";
        return utility.contains(utility.getCookie(), BID_MANAGER_COOKIE_NAME);
    };

    /**
     * Determines if the given the Line Item with the given ID employs a Manual Pricing strategy.
     * @param lineItemId The ID of the Line Item to check.
     * @returns {boolean} True if the Line Item is Manual Modify. False otherwise.
     */
    pricingTab.isManualModify = function(lineItemId) {
        var IS_MANUAL_MODIFY  = '#isManualModifyMap';
        var isModifyJson      = JSON.parse($(IS_MANUAL_MODIFY).text());
        var lineItemAction    = isModifyJson[lineItemId];
        return lineItemAction === "Modify" || lineItemAction === "Cease";
    };

    /**
     * Converts the given HTML row into a DataTable row.
     * @param table The table the row belongs to. Should be in the form an HTML ID E.g. '#priceLinesDetails',
     * @param row An HTML row.
     * @returns {array|Object|string|*|jQuery} The DataTables object representing the given HTML row.
     */
    pricingTab.htmlRowToDataTableRow = function(table, row) {
        return $(table).dataTable().fnGetData(row);
    };

    /**
     * Initialize the radio button that allows selection of the sub tabs on the Pricing Tab.
     */
    function initPricingTypeRadio() {

        var innerTabView = new ButtonGroupTabView($(PRICING_TYPE_RADIO));

        var resizeTable = function(selector) {
            $(selector).dataTable().fnAdjustColumnSizing(false);
        };

        innerTabView
            .withView("#standardChargesView", "label[for='standardChargesRadio']", undefined, true)
            .withView("#usageChargesView", "label[for='usageChargesRadio']", function() {
                resizeTable($(pricingTab.USAGE_PRICING_TABLE));
            });

        // Only add the Cost Tab if the current user has Access.
        if(pricingTab.hasCostTabAccess()) {
            innerTabView.withView("#costPricingView", "label[for='costPricingRadio']", function() {
                resizeTable($(pricingTab.COST_PRICING_TABLE));
            });
        }

        innerTabView.initView();
    }

    /**
     * Renames the Right Pane.
     */
    function setupRightPaneSummary() {
        var pricingRightPaneTitle = $(RIGHT_PANE_SELECTOR).find("h2 span");
        pricingRightPaneTitle.attr('title', 'Summary');

        var pricingRightPaneText = $(RIGHT_PANE_SELECTOR).find("h2");
        var children = pricingRightPaneText.children();
        pricingRightPaneText.text("Summary");
        pricingRightPaneText.append(children);
    }

    // TODO: Discount Dialog.

    /**
     * TODO: JSDOC
     */
    function initCommonError() {
        pricingTab.PRICING_COMMON_ERROR = new rsqe.StatusMessage("#pricingDiv #commonError");
    }

    /**
     * // TODO: JSDOC FOR THIS.
     * @returns {rsqe.Discounts}
     */
    function initDiscounts() {
        pricingTab.discounts =  new rsqe.Discounts(
            {
                unsavedChanges  : $(pricingTab.UNSAVED_DISCOUNT_MESSAGE),
                saveMessage     : $(SAVE_MESSAGE),
                commonError     : pricingTab.PRICING_COMMON_ERROR
            }
        )
    }

    // TODO: Extract these large dialogs into their own classes.
    function setupRequestDiscountDialog() {

        var dialog = new rsqe.Dialog($(REQUEST_DISCOUNT_DIALOG),
            {
                title:"Request Discount Approval",
                width: "600px",
                closers: $(OKAY_DISCOUNT_APPROVAL_SUCCESS)
            }
        );

        pricingTab.requestDiscountvalidator =
            $(REQUEST_DISCOUNT_DIALOG_FORM).validate({

                rules: {
                    bidManagerList:"required",
                    customerGroupEmailId: {
                        required:true,
                        email:true
                    }
                },

                messages:
                {
                    bidManagerList:"Please select a Bid Manager",
                    customerGroupEmailId:"Please enter a valid email"
                },

                errorPlacement:function (error, element) {
                    if (element.attr("name") == "customerGroupEmailId"){
                        error.insertAfter("#copyToEmail");
                    } else {
                        error.insertAfter(element);
                    }
                }

            }
        );

        $(pricingTab.REQUEST_DISCOUNT_POPUP_BUTTON).click(function() {

            if (pricingTab.hasIndirectAccess) {
                $(pricingTab.COMMERCIAL_NON_STANDARD_REQUEST).disableable('enable', true);
            } else {
                $(pricingTab.COMMERCIAL_NON_STANDARD_REQUEST).disableable('enable', false);
            }
            if (!pricingTab.discounts.hasUnsavedChanges()) {
                $(COMMENTS_DIV).scrollTop(1000);
                pricingTab.PRICING_COMMON_ERROR.hide();
                $(OKAY_DISCOUNT_APPROVAL_SUCCESS).hide();
                $(BID_MANAGER_LIST).empty();
                $(pricingTab.SEND_DISCOUNT_APPROVAL_MESSAGE).removeClass().text("");
                $(REQUEST_DISCOUNT_DIALOG_FORM).find('input#sendDiscountApprovalButton, a.cancel').show();

                $.getJSON(rsqe.urlBuilder.BID_MANAGERS,
                          function(result) {
                              $(BID_MANAGER_LIST)
                                  .append($("<option></option>")
                                              .attr("value", "")
                                              .text("--Please Select--"));
                              var userList = $.makeArray(result.users);
                              $.each(userList, function(i, user) {
                                  $(BID_MANAGER_LIST)
                                      .append($("<option></option>")
                                                  .attr("value", user.email)
                                                  .text(user.forename + ' ' + user.surname + ' (' + user.email + ')'));
                              });
                          });
                dialog.open();
            } else {
                pricingTab.PRICING_COMMON_ERROR.show("Cannot request discounts due to unsaved changes");
            }
        });

        var sendDiscountApprovalButton = $(REQUEST_DISCOUNT_DIALOG).find("#sendDiscountApprovalButton");
        sendDiscountApprovalButton.click(function() {
            $(REQUEST_DISCOUNT_LOADING_MESSAGE).hide();
            $(pricingTab.SEND_DISCOUNT_APPROVAL_MESSAGE).removeClass();
            $(pricingTab.SEND_DISCOUNT_APPROVAL_MESSAGE).text("");

            if (pricingTab.requestDiscountvalidator.form()) {
                if (!$.trim($(BID_MANAGER_LIST).val()) && !$.trim($(CUSTOMER_GROUP_EMAIL_ID).val())) {
                    $(pricingTab.SEND_DISCOUNT_APPROVAL_MESSAGE).addClass("error");
                    $(pricingTab.SEND_DISCOUNT_APPROVAL_MESSAGE).text("Please select a Bid Manager");
                } else {
                    var validateRevenueTable = validateRevenueTableFunc();
                    if (validateRevenueTable != ""){
                        $(pricingTab.SEND_DISCOUNT_APPROVAL_MESSAGE).addClass("error");
                        $(pricingTab.SEND_DISCOUNT_APPROVAL_MESSAGE).text(validateRevenueTable);
                    } else {
                        $(REQUEST_DISCOUNT_LOADING_MESSAGE).show(); //validations passed; show loading message
                        var items = [];
                        var i = 0;
                        for (var key in pricingTab.standardTab.revenueTable.revenueData) {
                            items[i] = pricingTab.standardTab.revenueTable.revenueData[key];
                            i++;
                        }

                        var revenueDTO = i > 0 ? {"itemDTOs":items} : {};
                        $.post(
                            rsqe.urlBuilder.REQUEST_DISCOUNT_APPROVAL +
                            '?bidManagerEmail=' + $(BID_MANAGER_LIST).val()
                            + '&groupEmailId='  + $(CUSTOMER_GROUP_EMAIL_ID).val() +
                            '&comment='         + $(SALES_USER_NEW_COMMENT).val(),
                            JSON.stringify(revenueDTO))

                        .success(function(result) {

                            if(result.status == 'success') {
                                var email = $(BID_MANAGER_LIST).val();
                                $(pricingTab.SEND_DISCOUNT_APPROVAL_MESSAGE).addClass("success");
                                $(REQUEST_DISCOUNT_DIALOG_FORM).find('input#sendDiscountApprovalButton, a.cancel').hide();
                                $(REQUEST_DISCOUNT_LOADING_MESSAGE).hide();
                                $(OKAY_DISCOUNT_APPROVAL_SUCCESS).show();
                                $(pricingTab.SEND_DISCOUNT_APPROVAL_MESSAGE).text(result.message + ' to ' + email);
                                $(pricingTab.PRICING_DETAILS_TABLE).dataTable().fnDraw();
                                $(pricingTab.REQUEST_DISCOUNT_POPUP_BUTTON).attr("disabled", true);
                                $(pricingTab.REQUEST_DISCOUNT_POPUP_BUTTON).addClass("disabled")
                            }

                            else if(result.status == 'fail') {
                                $(pricingTab.SEND_DISCOUNT_APPROVAL_MESSAGE).addClass("error");
                                $(REQUEST_DISCOUNT_LOADING_MESSAGE).hide();
                                $(pricingTab.SEND_DISCOUNT_APPROVAL_MESSAGE).text("Request Discount has encountered the following error: " + result.message);
                            }

                            else {
                                $(REQUEST_DISCOUNT_LOADING_MESSAGE).hide();
                            }
                        });
                    }
                }
            }
        });

        function validateRevenueTableFunc() {

            var message = "";

            function isEmpty(proposedRevenue) {
                return proposedRevenue == undefined || proposedRevenue === "";
            }

            $(pricingTab.PRICING_REVENUE_TABLE).find("tbody tr").each(function (i, row) {
                if ($("input:checkbox", row).attr("checked")) {
                    var data = $(pricingTab.PRICING_REVENUE_TABLE).dataTable().fnGetData(row);
                    var revenueObj = pricingTab.standardTab.revenueTable.revenueData[data.id];
                    if (isEmpty(revenueObj.proposedRevenue)) {
                        message = "Please enter proposing revenue commitment.";
                    }
                    if (parseInt(revenueObj.proposedRevenue) < parseInt(data.existingRevenue)) {
                        message = "Proposed revenue should be greater than existing revenue commitment.";
                    }
                    if(!isEmpty(revenueObj.triggerMonths) && 12 <= parseInt(revenueObj.triggerMonths) ){
                        message = message + "Trigger Months should be between 0 to 12.";
                    }
                }
            });
            return message;
        }
    }

    // TODO: END DISCOUNT DIALOG.

    /**
     * Attach HTML events for the elements of the Standard Tab.
     * @private
     */
    function attachEvents() {

        // Any input element assigned the '.editable' class will be made editable via the jeditable library.
        var EDITABLE_CLASS = '.editable input';

        /**
         * Called whenever a page element with the EDITABLE_CLASS comes into focus (I.e. it is clicked).
         * Selects the value in the edited cell. So that the user can type immediately, which will overwrite the existing value.
         */
        $(EDITABLE_CLASS).live("focus", function() {
            this.select();
        });

    }

}(
    // Immediately invoke this namespace.
    rsqe.pricingTab = rsqe.pricingTab || {}
));