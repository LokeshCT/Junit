/**
 * This namespace defines all functionality required to build the Cost DataTable displayed on the {@link costTab}.
 *
 * @namespace costTable
 * @costTable The object to append all logic onto.
 * @undefined This parameter prevents undefined from being redefined by other JavaScript code.
 */
(function(costTable, undefined) {

    // Short names for the namespaces used here.
    var pricing     = rsqe.pricingTab;
    var url         = rsqe.urlBuilder;
    var columnNames = rsqe.pricingTab.PRICE_LINE_TABLE_COLUMN_NAME;
    var costTab     = rsqe.pricingTab.costTab;

    /**
     * Initialize the Cost DataTable and it's supporting elements.
     * @public
     */
    costTable.initialize = function() {
        buildCostTable();
    };

    // TODO: CONSTANTS!
    function buildCostTable() {

        costTable.costChargeDataTable =
            new PricingDataTable(
                $(pricing.COST_PRICING_TABLE),
                url.PRODUCT_COST_CHARGES,

                function(data) {
                    return convertCostJsonToRows(data);
                }
            );

        var onHeaderDrawCallback = function(trNode, costRow, colSpan) {
            // add product name row
            createProductRow(trNode, [
                {colSpan:1, value:costRow.product},
                {colSpan:1, value:costRow.summary},
                {colSpan:1, value:costRow.site},
                {colSpan:1, value:costRow.miniAddress},
                {colSpan:(colSpan-4), value:''}
            ]);
        };

        var onCellValueChanged = function() {
            $('#costWarn').show();
        };

        var onPricingSummaryEnabled = function() {
            $('#costDiscountWarnMessage').show();
            enableElement($(costTab.VENDOR_DISCOUNT_REFERENCE));
            enableButton($('#updateVendorDiscountReference'));
        };

        var onPricingSummaryDisabled = function() {
            $('#costDiscountWarnMessage').hide();
            $(costTab.VENDOR_DISCOUNT_REFERENCE).val('');
            disableElement($(costTab.VENDOR_DISCOUNT_REFERENCE));
            disableButton($('#updateVendorDiscountReference'));
        };

        var updateVendorDiscounts = function() {
            var vendorDiscounts = {};
            _.each(costTable.costChargeDataTable.pricingRows, function(pricingRow) {
                if(undefined != pricingRow.vendorDiscountRef && "" != pricingRow.vendorDiscountRef) {
                    vendorDiscounts[pricingRow.vendorDiscountRef] = pricingRow.vendorDiscountRef;
                }
            });

            var vendorDiscountRefSelect = $('#vendorDiscountRefFilter');
            vendorDiscountRefSelect.find('option').remove().end();
            vendorDiscountRefSelect.append($("<option>", { value: "", html: "--Please Select--" }));
            for(var k in vendorDiscounts) {
                var isSelected = k == costTab.selectedVendorDiscount;
                vendorDiscountRefSelect.append($("<option>", { value: k, html: k, selected: isSelected }));
            }
        };

        var onDrawComplete = function() {
            $('#costWarn').hide();
            $('#costDiscountWarnMessage').hide();
            costTab.populateCostPricingSummary();
            updateVendorDiscounts();
        };

        // TODO: Constants for the table column names used here.
        costTable.costChargeDataTable
            .metaFor(columnNames.SITE, PRICING_CELL_SELECTOR.HIDDEN)
            .metaFor(columnNames.MINI_ADDRESS, PRICING_CELL_SELECTOR.HIDDEN)
            .metaFor(columnNames.PRODUCT, PRICING_CELL_SELECTOR.HIDDEN)
            .metaFor(columnNames.SUMMARY, PRICING_CELL_SELECTOR.HIDDEN)
            .metaFor(columnNames.DESCRIPTION, PRICING_CELL_SELECTOR.PREPEND_CHECKBOX)
            .metaFor(columnNames.PRICE_STATUS)
            .numMetaFor("oneTimeCost.value" , [PRICING_CELL_SELECTOR.READ_ONLY ,"oneTimeCostGross"])
            .numMetaFor("oneTimeCost.discount", [PRICING_CELL_SELECTOR.BULK_ONE_TIME_DISCOUNT, PRICING_CELL_SELECTOR.READ_ONLY, "oneTimeCostDiscount", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("oneTimeCost.netTotal", [PRICING_CELL_SELECTOR.BULK_ONE_TIME_NET, PRICING_CELL_SELECTOR.READ_ONLY, "oneTimeCostNet", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("recurringCost.value" ,[PRICING_CELL_SELECTOR.READ_ONLY,"recurringCostGross"])
            .numMetaFor("recurringCost.discount", [PRICING_CELL_SELECTOR.BULK_RECURRING_DISCOUNT, PRICING_CELL_SELECTOR.READ_ONLY, "recurringCostDiscount", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("recurringCost.netTotal", [PRICING_CELL_SELECTOR.BULK_RECURRING_NET, PRICING_CELL_SELECTOR.READ_ONLY, "recurringCostNet", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("vendorDiscountRef", ["vendorDiscountRef", PRICING_CELL_SELECTOR.EDIT_TEXT, PRICING_CELL_SELECTOR.READ_ONLY])
            .withPricingSummary("#oneTimeCostBulkPricingActions", "#recurringCostBulkPricingActions", "#applyBulkDiscountCost", onPricingSummaryEnabled, onPricingSummaryDisabled)
            .initDataTable(
                onHeaderDrawCallback,
                onCellValueChanged,
                ["oneTimeCost", "recurringCost"],
                [
                    {
                        "selector": "vendorDiscountRef",
                        "maxLength": 15,
                        "isMandatoryFnName": "isVendorDiscountMandatory"}
                ],
                onDrawComplete
            );

    }

    function convertCostJsonToRows(data) {
        var rows = [];
        var headers = {};

        var createCostRow = function(item, isHeader) {
            var getVendorDiscount = function(item) {
                if("" != item.oneTime.value) {
                    return item.oneTime.vendorDiscountRef;
                } else {
                    return item.recurring.vendorDiscountRef;
                }
            };

            return {
                _json : item,
                product: item.product,
                summary: item.summary,
                site: item.site,
                miniAddress: item.miniAddress,
                description: item.description,
                status : item.status,
                oneTimeCost : {
                    value: item.oneTime.value,
                    discount: item.oneTime.discount,
                    netTotal: item.oneTime.netTotal,
                    isSet : function() {
                        return item.oneTime.discountEnabled;
                    },
                    isManualPricing: function(){
                        return item.isManualPricing;
                    },
                    isGrossValueNotPresent: function(){
                        return "" == item.oneTime.value || "0.00" == item.oneTime.value;
                    }
                },
                recurringCost : {
                    value: item.recurring.value,
                    discount: item.recurring.discount,
                    netTotal: item.recurring.netTotal,
                    isSet : function() {
                        return item.recurring.discountEnabled;
                    },
                    isManualPricing: function(){
                        return item.isManualPricing;
                    },
                    isGrossValueNotPresent: function(){
                        return "" == item.recurring.value || "0.00" == item.recurring.value;
                    }

                },
                vendorDiscountRef: getVendorDiscount(item),
                isVendorDiscountMandatory : function() {
                    var mandatory = false;
                    if($.isNumeric(item.oneTime.discount)) {
                        if("0.00000" != item.oneTime.discount) {
                            mandatory = true;
                        }
                    }
                    if($.isNumeric(item.recurring.discount)) {
                        if("0.00000" != item.recurring.discount) {
                            mandatory = true;
                        }
                    }
                    return mandatory;
                },
                isHeader: function() {
                    return undefined != isHeader && isHeader;
                },
                isReadOnly: function() {
                    return !item.oneTime.discountEnabled && !item.recurring.discountEnabled;
                }
            };
        };

        var buildHeaderKey = function(item) {
            return item.product + item.summary  + item.site + item.miniAddress ;
        };

        var pushItemToHeader = function(item) {
            var key = buildHeaderKey(item);
            if(undefined == headers[key]) {
                headers[key] = [];
            }
            headers[key].push(item);
        };

        _.each(data.itemDTOs, function(item) {
            pushItemToHeader(item);
        });

        _.each(headers, function(header) {
            rows.push(createCostRow(header[0], true));

            _.each(header, function(item) {
                rows.push(createCostRow(item));
            });
        });

        return rows;
    }

// Immediately invoke this namespace.
}( rsqe.pricingTab.costTab.costTable = rsqe.pricingTab.costTab.costTable || {} ));