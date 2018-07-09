/**
 * This namespace defines all functionality required to build the {@link pricingTab}s Cost Tab.
 *
 * @namespace costTab
 * @costTab The object to append all logic onto.
 * @undefined This parameter prevents undefined from being redefined by other JavaScript code.
 */
(function(costTab, undefined) {

    var pricing        = rsqe.pricingTab;
    var pricingSummary = rsqe.pricingTab.pricingSummary;
    var url            = rsqe.urlBuilder;
    var BCM            = rsqe.pricingTab.BCM;

    // Cost Attachment Dialog.
    var COST_ATTACHMENT_DIALOG                = '#costAttachmentDialog';

    // Right Pane - Vendor Discount Reference
    costTab.VENDOR_DISCOUNT_REFERENCE         = '#vendorDiscountReference';
    var UPLOAD_COST_ATTACHMENT_BUTTON         = '#uploadCostAttachmentsButton';

    // Pricing Summary Values:

        // One Time Cost Totals:
        var ONE_TIME_GROSS_TOTAL        = '#totalOneTimeCost';
        var ONE_TIME_NET_TOTAL          = '#totalOneTimeNet';
        var ONE_TIME_DISCOUNT_TOTAL     = '#totalOneTimeDiscount';

        // Recurring Cost Totals:
        var RECURRING_GROSS_TOTAL       = '#totalRecurringCost';
        var RECURRING_NET_TOTAL         = '#totalRecurringNet';
        var RECURRING_DISCOUNT_TOTAL    = '#totalRecurringDiscount';

        // Usage Cost Totals:
        var USAGE_OFF_NET_TOTAL         = '#totalOffNetUsageCost';
        var USAGE_ON_NET_TOTAL          = '#totalOnNetUsageCost';
        var USAGE_TOTAL                 = '#totalUsageCost';

    /**
     * Initialise all functionality required by the Cost Tab.
     * @public
     */
    costTab.initialize = function() {

        // Only initialize the Cost Tab if the has Cost Tab Access.
        if (pricing.hasCostTabAccess()) {
            costTab.costTable.initialize();
            attachEvents();

            // Setup dialog boxes.
            setupCostAttachmentDialog();

            // TODO: Functions for these. Used by CostTable.
            $('#costWarn').hide();
            $('#costSuccess').hide();
            $('#costDiscountWarnMessage').hide();
        }

    };

    costTab.populateCostPricingSummary = function() {
        pricingSummary.populatePricingSummary(
            $(ONE_TIME_GROSS_TOTAL),
            $(ONE_TIME_NET_TOTAL),
            $(ONE_TIME_DISCOUNT_TOTAL),
            $(RECURRING_GROSS_TOTAL),
            $(RECURRING_NET_TOTAL),
            $(RECURRING_DISCOUNT_TOTAL),
            $(USAGE_OFF_NET_TOTAL),
            $(USAGE_ON_NET_TOTAL),
            $(USAGE_TOTAL),
            "?suppressStrategy=UI_COSTS"
        );
    };

    // TODO: ADD CONSTANTS FOR THE HTML SELECTORS!

    function attachEvents() {

        /**
         * Called by the Import BCM Button on the Cost Tab.
         */
        $(BCM.IMPORT_BCM_BUTTON).click(function() {

            if (!validVendorDiscountRef()) {
                return;
            }
            BCM.bcmImportWarningDialogInstance.open();
            return false;
        });

        /**
         * Called by the Export BCM Button on the Cost Tab.
         */
        $(BCM.BCM_EXPORT_BUTTON).click(function() {

            var pricingRowsWithNoVendorDiscountRef = _.filter(costTab.costTable.costChargeDataTable.pricingRows, function(pricingRow) {
                return "" != pricingRow.recurringCost.discount &&
                       0 != parseInt(pricingRow.recurringCost.discount) &&
                       pricingRow.vendorDiscountRef == "";
            });

            if(pricingRowsWithNoVendorDiscountRef.length > 0) {
                alert('A Vendor Discount Reference must be filled in for the highlighted product(s)');
                return false;
            }
            var bcmUri = url.BCM_NOT_NEW_EXPORT_URL;
            return $.get(bcmUri);
        });

        $('#updateVendorDiscountReference').click(function() {
            var value = $(costTab.VENDOR_DISCOUNT_REFERENCE).val();
            if(undefined != value) {
                if(value.length > 15) {
                    alert('Vendor Discount Reference can not be more than 15 characters.');
                } else {
                    costTab.costTable.costChargeDataTable.bulkUpdate("vendorDiscountRef", value);
                }
            }
        });

        $('#costDiscard').click(function() {
            costTab.costTable.costChargeDataTable.refresh();
        });

        $('#costSave').click(function() {
            $('#costWarn').hide();
            $('#costDiscountWarnMessage').hide();
            $('#loadingMessage').text('Saving...');
            setTimeout(function(){ $('#loadingMessage').show(); }, 100);
            persistCostDiscounts();
        });

        // TODO: Make sure this is still functional once new multi select filters are implemented.
        $('#applyCostFilterButton').click(function() {
            var selectedProduct = $('#costProductFilter').val();
            var selectedCountry = $('#costCountryFilter').val();
            costTab.selectedVendorDiscount = $('#vendorDiscountRefFilter').val();
            costTab.costTable.costChargeDataTable.filter("product=" + selectedProduct + "|country=" + selectedCountry + "|vendorDiscount=" + costTab.selectedVendorDiscount);
        });

        $('#clearCostFilterButton').click(function() {
            $('#costProductFilter').val("");
            $('#costCountryFilter').val("");
            $('#vendorDiscountRefFilter').val("");
            costTab.costTable.costChargeDataTable.filter("");
        });

    }

    function validVendorDiscountRef() {

        var pricingRowsWithNoVendorDiscountRef = _.filter(costTab.costTable.costChargeDataTable.pricingRows, function(pricingRow) {
            return "" != pricingRow.recurringCost.discount &&
                   0 != parseInt(pricingRow.recurringCost.discount) &&
                   pricingRow.vendorDiscountRef == "";
        });

        if (pricingRowsWithNoVendorDiscountRef.length > 0) {
            alert('A Vendor Discount Reference must be filled in for the highlighted product(s)');
            return false;
        }
        return true;
    }

    function setupCostAttachmentDialog() {

        costTab.COST_ATTACHMENT_DIALOG_INSTANCE = new rsqe.Dialog(
            $(COST_ATTACHMENT_DIALOG),
            {
                width:800
            }
        );

        costTab.COST_ATTACHMENT_FORM = new rsqe.AttachmentForm(
            {
                cancelHandler : function() {
                    costTab.COST_ATTACHMENT_DIALOG_INSTANCE.close();
                }
            }
        );

        $(UPLOAD_COST_ATTACHMENT_BUTTON).click(function () {
            var costAttachmentUrl = $("#costAttachmentUrl").html();

            costTab.COST_ATTACHMENT_DIALOG_INSTANCE.setOptions(
                {
                    "title": "Add/Edit Attachments",
                    "position":
                        [
                            "center",
                            150
                        ],
                    "resizable": false,
                    close: function() {
                        costTab.COST_ATTACHMENT_FORM.resetForm();
                    }
                }
            );

            $(COST_ATTACHMENT_DIALOG).load(costAttachmentUrl, function() {
                costTab.COST_ATTACHMENT_FORM.setUpAttachmentDialog();
                $("#tierFilter").attr("disabled", true);
            });

            costTab.COST_ATTACHMENT_DIALOG_INSTANCE.open();
        });
    }

    function persistCostDiscounts() {

        var costDiscountDeltas = [];

        _.each(costTab.costTable.costChargeDataTable.getUpdatedRows(), function(currentCharge) {
            costDiscountDeltas.push(
                {
                    lineItemId: currentCharge._json.lineItemId,
                    description: currentCharge._json.description,
                    vendorDiscountRef: currentCharge.vendorDiscountRef,
                    oneTimeDiscount: {
                        priceLineId : currentCharge._json.oneTime.id,
                        discount : currentCharge.oneTimeCost.discount,
                        discountUpdated : currentCharge.oneTimeCost.discount != currentCharge._json.oneTime.discount,
                        grossValue : currentCharge.oneTimeCost.value
                    },
                    recurringDiscount: {
                        priceLineId : currentCharge._json.recurring.id,
                        discount : currentCharge.recurringCost.discount,
                        discountUpdated : currentCharge.recurringCost.discount != currentCharge._json.recurring.discount,
                        grossValue : currentCharge.recurringCost.value
                    },
                    isManualPricing : currentCharge.oneTimeCost.isManualPricing(),
                    isGrossAdded : currentCharge._json.oneTime.value   != currentCharge.oneTimeCost.value ||
                                   currentCharge._json.recurring.value != currentCharge.recurringCost.value
                }
            );
        });

        if(costDiscountDeltas.length > 0) {
            var delta = {quoteOptionCostDeltas : costDiscountDeltas};
            var costDiscountPostUri = $('#costDiscountPostUri').html();

            $.post(costDiscountPostUri,
                   JSON.stringify(delta),
                   function () {
                       $('#loadingMessage').hide();
                       costTab.costTable.costChargeDataTable.refresh();
                       $("#costSuccess").show();
                       setInterval(function () {
                           $("#costSuccess").fadeOut();
                       }, 3000);
                   }).fail(function(data) {
                $('#loadingMessage').hide();
                costTab.costTable.costChargeDataTable.refresh();
                new rsqe.StatusMessage("#costError").show("Error: " + data.responseText);
                pricing.PRICING_COMMON_ERROR.show("Error: " + data.responseText);
            });
        }
    }

// Immediately invoke this namespace.
}( rsqe.pricingTab.costTab = rsqe.pricingTab.costTab || {} ));