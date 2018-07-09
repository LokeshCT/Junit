/**
 * This namespace defines all functionality required to build the Usage DataTable displayed on the {@link usageTab}.
 *
 * @namespace usageTable
 * @usageTable The object to append all logic onto.
 * @undefined This parameter prevents undefined from being redefined by other JavaScript code.
 */
(function(usageTable, undefined) {

    // Short names for the namespaces used here.
    var pricing     = rsqe.pricingTab;
    var usageTab    = rsqe.pricingTab.usageTab;
    var url         = rsqe.urlBuilder;
    var columnNames = rsqe.pricingTab.PRICE_LINE_TABLE_COLUMN_NAME;

    /**
     * Initialize the Usage DataTable and it's supporting elements.
     * @public
     */
    usageTable.initialize = function() {
        buildUsageTable();
    };

    // TODO: Functions in this namespace rely on variables existing on the global namespace. Handle this more elegantly.

    function buildUsageTable() {

        // TODO: Can we just use USAGE_PRICING_TABLE here instead of this extra var?
        usageTable.usageChargeDataTable = new PricingDataTable(
            $(pricing.USAGE_PRICING_TABLE),
            url.PRODUCT_USAGE_CHARGE,
            function(data) {
                return convertUsageJsonToRows(data);
            }
        );

        var onHeaderDrawCallback = function(trNode, usageChargeRow, colSpan) {

            if(usageChargeRow.isProduct()) {

                // Add product name row.
                createProductRow(
                    trNode,
                    [
                        {colSpan:1, value: usageChargeRow.product},
                        {colSpan:1, value: usageChargeRow.summary},
                        {colSpan:(colSpan-2), value:''}
                    ]
                );

            } else {

                // Add inner Price Line name row.
                createProductRow(
                    trNode,
                    [
                        {colSpan:2, value:''},
                        {colSpan:(colSpan-2), value:usageChargeRow.description}
                    ]
                );
            }

        };

        // TODO: Constants for the table column names used here.
        usageTable.usageChargeDataTable
            .metaFor(columnNames.PRODUCT, PRICING_CELL_SELECTOR.HIDDEN)
            .metaFor(columnNames.SUMMARY, PRICING_CELL_SELECTOR.HIDDEN)
            .metaFor(columnNames.DESCRIPTION, PRICING_CELL_SELECTOR.HIDDEN)
            .metaFor("tier", PRICING_CELL_SELECTOR.PREPEND_CHECKBOX)
            .metaFor("pricingModel")
            .numMetaFor("minCharge.value")
            .numMetaFor("minCharge.discount", [PRICING_CELL_SELECTOR.READ_ONLY, "minChargeDiscount", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("minCharge.netTotal", [PRICING_CELL_SELECTOR.READ_ONLY, "minChargeNet", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("fixedCharge.value")
            .numMetaFor("fixedCharge.discount", [PRICING_CELL_SELECTOR.READ_ONLY, "fixedChargeDiscount", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("fixedCharge.netTotal", [PRICING_CELL_SELECTOR.READ_ONLY, "fixedChargeNet", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("chargeRate.value")
            .numMetaFor("chargeRate.discount", [PRICING_CELL_SELECTOR.READ_ONLY, "chargeRateDiscount", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("chargeRate.netTotal", [PRICING_CELL_SELECTOR.READ_ONLY, "chargeRateNet", PRICING_CELL_SELECTOR.DISCOUNT])
            .initDataTable(
                onHeaderDrawCallback,
                function() {
                    usageTab.showWarnMessage();
                },
                ["minCharge", "fixedCharge", "chargeRate"], []
            );
    }

    usageTable.persistUsageCharges = function() {

        var usageChargesDeltas = [];
        _.each(
            usageTable.usageChargeDataTable.getUpdatedRows(),
            function(currentCharge) {
                usageChargesDeltas.push(
                    {
                        lineItemId:          currentCharge._json.lineItemId,
                        priceLineId:         currentCharge._json.priceLineId,
                        classifier:          currentCharge._json.tier,
                        minChargeDiscount:   currentCharge.minCharge.discount,
                        fixedChargeDiscount: currentCharge.fixedCharge.discount,
                        chargeRateDiscount:  currentCharge.chargeRate.discount
                    }
                );
            }
        );

        if(usageChargesDeltas.length > 0) {
            var delta = {quoteOptionPricingDeltas : usageChargesDeltas};
            var usageDiscountPostUri = $('#usageDiscountPostUri').html();

            $.post(
                usageDiscountPostUri,
                JSON.stringify(delta),
                function () {

                    refreshUsageCharges();
                    usageTab.showSuccessMessage();

                    setInterval(
                        function () {
                            usageTab.hideSuccessMessage();
                        },
                        2000
                    );
                }
            );
        }

        return usageChargesDeltas;
    };

    function refreshUsageCharges() {
        usageTab.hideWarnMessage();
        $(pricing.USAGE_PRICING_TABLE).dataTable().fnDraw();
    }

    function convertUsageJsonToRows(data) {
        var usageProductsJson = data.products;
        var charges = [];

        var ensureArray = function(array) {
            if (array.length) {
                return array;
            }

            return [array];
        };

        if(usageProductsJson) {
            usageProductsJson = ensureArray(usageProductsJson);

            for(var a = 0; a < usageProductsJson.length; a++) {
                var product = usageProductsJson[a];
                charges.push(new UsageChargeRow({product:product.productName,
                    summary:product.summary}));

                if(product.priceLines) {
                    var priceLines = ensureArray(product.priceLines);
                    for(var b = 0; b < priceLines.length; b++) {
                        var priceLine = priceLines[b];
                        charges.push(new UsageChargeRow({description:priceLine.description}));

                        if(priceLine.tiers) {
                            var tiers = ensureArray(priceLine.tiers);
                            for(var c = 0; c < tiers.length; c ++) {
                                charges.push(new UsageChargeRow(tiers[c]));
                            }
                        }
                    }
                }
            }
        }

        return charges;
    }

    // TODO: This function saves everything onto the global namespace.
    // TODO: Provide a function to return these value as an object instead. At minimum should point to this namespace rather than global.
    function UsageChargeRow(json) {
        this._json = json;
        this.tier = nullToEmpty(json.tierDescription);

        this.isTier = function() {
            return "" != this.tier;
        };

        this.isHeader = function() {
            return !this.isTier();
        };

        this.product      = this.isTier() ? "" : nullToEmpty(json.product);
        this.miniAddress  = this.isTier() ? "" : nullToEmpty(json.product);
        this.description  = this.isTier() ? "" : nullToEmpty(json.description);
        this.pricingModel = nullToEmpty(json.pricingModel);
        this.summary      = this.isTier() ? "" : nullToEmpty(json.summary);

        this.isProduct = function() {
            return !this.isTier() && "" != this.product;
        };

        var noCharge = function() {
            return {
                value:    "",
                discount: "",
                netTotal: ""
            };
        };

        this.minCharge   = json.minCharge   ? json.minCharge   : noCharge();
        this.fixedCharge = json.fixedCharge ? json.fixedCharge : noCharge();
        this.chargeRate  = json.chargeRate  ? json.chargeRate  : noCharge();

        mixinChargeMethods(this.minCharge);
        mixinChargeMethods(this.fixedCharge);
        mixinChargeMethods(this.chargeRate);
		mixinManualPriceMethods(this.minCharge);
        mixinManualPriceMethods(this.fixedCharge);
        mixinManualPriceMethods(this.chargeRate);
        mixinGrossValueMethods(this.minCharge);
        mixinGrossValueMethods(this.fixedCharge);
        mixinGrossValueMethods(this.chargeRate);

        this.isSelected = function() {

            var toInt = function(val) {
                return "" === val || isNaN(val) ? 0 : parseInt(val);
            };

            return (toInt(this.minCharge.discount) +
                    toInt(this.fixedCharge.discount) +
                    toInt(this.chargeRate.discount)) > 0;
        };

        this.hasChanged = false;
    }

    function nullToEmpty(obj) {
        return undefined == obj || null == obj ? "" : obj;
    }

// Immediately invoke this namespace.
}( rsqe.pricingTab.usageTab.usageTable = rsqe.pricingTab.usageTab.usageTable || {} ));