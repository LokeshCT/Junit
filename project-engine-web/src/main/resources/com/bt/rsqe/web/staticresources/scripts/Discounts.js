rsqe.Discounts = function (options) {
    this.unsavedChanges = options.unsavedChanges;
    this.saveMessage = options.saveMessage;
    this.commonError = options.commonError;

    this.oneTimeNetTotalFormInputId = '.oneTime_netTotal form input';
    this.recurringNetTotalFormInputId = '.recurring_netTotal form input';
    this.oneTimeDiscountFormInputId = '.oneTime_discount form input';
    this.recurringDiscountFormInputId = '.recurring_discount form input';
    this.oneTimeDiscountFieldId = '.oneTime_discount';
    this.recurringDiscountFieldId = '.recurring_discount';
    this.oneTimeNetTotalFieldId = '.oneTime_netTotal';
    this.recurringNetTotalFieldId = '.recurring_netTotal';
    this.oneTimeValueFieldSelector = ".oneTime_value";
    this.recurringValueFieldSelector = ".recurring_value";
    this.discountPostUri = $('#discountPostUri').html();

    this.discounts = { };
};

rsqe.Discounts.prototype = {
    addDiscount:function (row, discountOn, discount, lineItemId, priceLineId) {
        this.commonError.hide();

        var originalValue = $(this[discountOn + "ValueFieldSelector"], row).html();

        if (isNaN(discount)) {
            var originalNetTotal = $(this[discountOn + "NetTotalFieldId"], row).html();
            var originalDiscount = ((originalValue - originalNetTotal) / originalValue) * 100;
            return originalDiscount.asPercent();
        }
            if(discount>100)
            {
                this.commonError.show("Discount value cannot be greater than 100%");

                return false;

            }

        discount = discount.asPercent();
        var netTotal = originalValue - (discount / 100) * originalValue;
        if ($(this[discountOn + "NetTotalFormInputId"], row).length !== 0) {
            $(this[discountOn + "NetTotalFormInputId"], row).val(netTotal.asCurrency());
        } else {
            $(this[discountOn + "NetTotalFieldId"], row).html(netTotal.asCurrency());
        }
        this.discounts[row.attr(discountOn + "_id")] = {netTotal:netTotal, discount:discount, discountOn:discountOn,
            lineItemId:lineItemId,
            gross:originalValue,
            productPriceLineDescription:getProductPriceLineDescription(row),
            priceLineId:priceLineId
        };
        return discount;
    },

    addDiscountFromNetTotal:function (row, discountOn, netTotal, lineItemId, priceLineId) {
        var originalValue = $(this[discountOn + "ValueFieldSelector"], row).html();
        var discount = 0;
        var returnVal = originalValue.asCurrency();

        if (isNaN(netTotal)) {
            var originalDiscount = $(this[discountOn + "DiscountFieldId"], row).html();
            var originalNetValue = originalValue - (originalDiscount / 100) * originalValue;
            return originalNetValue.asCurrency();
        }

        returnVal = netTotal.asCurrency();
        discount = 100 * (1 - returnVal / originalValue);

        if ($(this[discountOn + "DiscountFormInputId"], row).length !== 0) {
            $(this[discountOn + "DiscountFormInputId"], row).val(discount.asPercent());
        } else {
            $(this[discountOn + "DiscountFieldId"], row).html(discount.asPercent());
        }

        this.discounts[row.attr(discountOn + "_id")] = {netTotal:returnVal, discount:discount.asPercent(), discountOn:discountOn,
            lineItemId:lineItemId,
            gross:originalValue.asCurrency(),
            productPriceLineDescription:getProductPriceLineDescription(row),
            priceLineId:priceLineId};

        return returnVal;
    },

    addNetBasedOnExistingGross:function (row, discountOn, grossTotal, lineItemId, priceLineId) {
        var returnVal = grossTotal;
        if (!isNaN(grossTotal) && grossTotal >= 0) {
            returnVal = grossTotal.asCurrency();
            var originalDiscount = $(this[discountOn + "DiscountFieldId"], row).html();
            var originalNetValue = grossTotal - (originalDiscount / 100) * grossTotal;
            $(this[discountOn + "NetTotalFieldId"], row).html(originalNetValue.asCurrency());

            this.discounts[row.attr(discountOn + "_id")] = {netTotal:originalNetValue.asCurrency(), discount:originalDiscount.asPercent(), discountOn:discountOn,
                lineItemId:lineItemId,
                gross:grossTotal,
                productPriceLineDescription:getProductPriceLineDescription(row),
                priceLineId:priceLineId};
        }

        return returnVal;
    },

    refreshDiscounts:function () {
        for (var discount in this.discounts) {
            var current = this.discounts[discount];

            var rowSelector = 'tr[' + current.discountOn + '_id="' + discount + '"]';
            if ($(rowSelector).length === 0)
                continue;

            this.unsavedChanges.removeClass("hidden");
            $(rowSelector).addClass(current.discountOn + 'ChangeDiscount');

            $(rowSelector + ' ' + this[current.discountOn + "NetTotalFieldId"]).html(current.netTotal.asCurrency());
            $(rowSelector + ' ' + this[current.discountOn + "DiscountFieldId"]).html(current.discount.asCurrency());
        }
    },

    persist:function (resetTable, retrievePricingSummary) {
        var self = this;
        var uri = this.discountPostUri;
        var dataDiscount = {};
        var dataGrossPrice = [];
        var data = {};
        for (var discount in self.discounts) {
            if (!dataDiscount[self.discounts[discount].lineItemId])
                dataDiscount[self.discounts[discount].lineItemId] = {};
            if (!dataGrossPrice[self.discounts[discount].lineItemId])
                dataGrossPrice[self.discounts[discount].lineItemId] = {};
            dataDiscount[self.discounts[discount].lineItemId][discount] = self.discounts[discount].discount;
            //Build Json for gross price

            var lineItemId = self.discounts[discount].lineItemId;
            var gross = self.discounts[discount].gross;
            var productPriceLineDescription = self.discounts[discount].productPriceLineDescription;
            var type = self.discounts[discount].discountOn;
            var priceLineId = self.discounts[discount].priceLineId;

            dataGrossPrice.push({
                                    "lineItemId":lineItemId,
                                    "gross":gross,
                                    "type":type,
                                    "productDescription":productPriceLineDescription ,
                                    "id":priceLineId
                                });
        }
        data["discount"] = dataDiscount;
        data["gross"] = dataGrossPrice;
        $.post(uri, JSON.stringify(data),
               function () {
                   resetTable();
                   $("tr").removeClass("oneTimeChangeDiscount");
                   $("tr").removeClass("recurringChangeDiscount");
                   self.displaySaveMessage();
                   self.resetToNoUnsavedDiscounts(self);
                   retrievePricingSummary();
               });
    },

    displaySaveMessage:function () {
        var self = this;
        this.saveMessage.removeClass("hidden");
        setTimeout(function () {
            self.saveMessage.addClass("hidden");
            $("#manualPriceWarnMessage").addClass("hidden");
        }, 5000);
    },

    discard:function (resetTable) {
        $("tr").removeClass("oneTimeChangeDiscount");
        $("tr").removeClass("recurringChangeDiscount");
        this.resetToNoUnsavedDiscounts(this);
        resetTable();
    },

    resetToNoUnsavedDiscounts:function (self) {
        self.unsavedChanges.addClass("hidden");
        self.discounts = { };
        self.commonError.hide();
    },

    hasUnsavedChanges:function () {
        for (var discount in this.discounts) {
            return true;
        }
        return false;
    }
};

function getProductPriceLineDescription(row) {
    var priceLineProductDescription = ""
    var checkBoxElement = row.children(".description").html().toString();
    var index = checkBoxElement.indexOf('">')
    priceLineProductDescription = checkBoxElement.substring(index + 2);
    return priceLineProductDescription
}
