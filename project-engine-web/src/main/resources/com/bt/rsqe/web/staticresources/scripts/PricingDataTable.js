var PRICING_CELL_SELECTOR = {
    HIDDEN : "cellHidden",
    PREPEND_CHECKBOX : "prependCheckBox",
    READ_ONLY : "readOnlyCell",
    DISCOUNT : "pricingDiscountCell",
    BULK_ONE_TIME_DISCOUNT : "bulkOneTimeDiscountCell",
    BULK_ONE_TIME_NET : "bulkOneTimeNetCell",
    BULK_RECURRING_DISCOUNT : "bulkRecurringDiscountCell",
    BULK_RECURRING_NET : "bulkRecurringNetCell",
    EDIT_TEXT : "pricingTextCell",
    MANDATORY_PRICING_CELL : "mandatoryPricingCell"
};

function PricingDataTable(pricingDataTableSelector, sourceUri, adaptRowDataFn) {
    this._pricingDataTableSelector = pricingDataTableSelector;
    this._sourceUri = sourceUri;
    this._columnMetaData = [];
    this._adaptRowDataFn = adaptRowDataFn;
    this.pricingRows = [];
}

PricingDataTable.prototype.metaFor = function(propertyName, selectors) {
    selectors = ensureArray(selectors);
    createBeanToRowMeta(this._columnMetaData, propertyName, selectors);
    return this;
};

PricingDataTable.prototype.numMetaFor = function(propertyName, selectors) {
    selectors = ensureArray(selectors);
    selectors.unshift("numeric nowrap");
    createBeanToRowMeta(this._columnMetaData, propertyName, selectors, "5%");
    return this;
};

PricingDataTable.prototype.withPricingSummary = function(oneTimePricingActionsSelector,
                                                         recurringPricingActionsSelector,
                                                         applyDiscountsButtonSelector,
                                                         summaryEnabledCallbackFn,
                                                         summaryDisabledCallbackFn) {
    this._pricingSummary = {
        "oneTimePricingActionsSelector" : oneTimePricingActionsSelector,
        "recurringPricingActionsSelector" : recurringPricingActionsSelector,
        "applyDiscountsButtonSelector" : applyDiscountsButtonSelector,
        "summaryEnabledCallback" : summaryEnabledCallbackFn,
        "summaryDisabledCallback" : summaryDisabledCallbackFn
    };

    return this;
};

PricingDataTable.prototype.initDataTable = function(onHeaderDrawFn, onChangesFn, discountCells, textCells, onDrawCompleteFn) {
    this.onChangesFn = onChangesFn;
    this.tableMeta = pricingTableMetaFor(this._sourceUri, this._columnMetaData, this._adaptRowDataFn);

    var that = this;

    var addCheckBoxOnClick = function(pricingRowCheckBox, trRow, pricingRow, status) {
        pricingRowCheckBox.click(function() {
            var isChecked = pricingRowCheckBox.prop('checked');
            var discountFields = trRow.find("." + PRICING_CELL_SELECTOR.DISCOUNT);
            var textFields = trRow.find("." + PRICING_CELL_SELECTOR.EDIT_TEXT);
            var grossFields = trRow.find("." + PRICING_CELL_SELECTOR.PRICING_FIELD);
            if(isChecked) {
                pricingRow._isSelected = true;
                pricingRow._trNode = trRow;
                textFields.removeClass("readOnly");
                _.each(discountCells, function(discountCell) {
                    that.setupDiscountingForPricingRow(trRow, pricingRow, pricingRow[discountCell], "." + discountCell ,discountFields);
                });
                _.each(textCells, function(textCell) {
                    that.setupEditTextsForPricingRow(trRow, pricingRow, textCell, "." + textCell.selector);
                });
            } else {
                pricingRow._isSelected = false;
                discountFields.addClass("readOnly");
                discountFields.editable('disable');
                textFields.addClass("readOnly");
                textFields.editable('disable');
            }

            if(that._pricingSummary) {
                that._pricingSummary.onRowClicked();
            }
        });
    };

    this.tableMeta.fnDrawCallback = function(settings) {
        if(that._pricingSummary) {
            that._initPricingSummary();
        }

        that.pricingRows = [];

        // post-processing! Iterate through each pricing row
        // and replace the product/priceLine placeholders
        // with proper product group rows!
        var colSpan = that._columnMetaData.length;

        for(var a = 0; a < settings.aoData.length; a++) {
            var pricingRow = settings.aoData[a]._aData;
            var trNode = $(settings.aoData[a].nTr);

            if(pricingRow.isHeader && pricingRow.isHeader()) {
                if(onHeaderDrawFn) {
                    onHeaderDrawFn(trNode, pricingRow, colSpan);
                }
            } else {
                that.pricingRows.push(pricingRow);
                trNode.find("." + PRICING_CELL_SELECTOR.HIDDEN).addClass('hideCell');

                var pricingRowCheckBox = document.createElement('input');
                pricingRowCheckBox.type = 'checkbox';
                pricingRowCheckBox.name = 'pricingDTCheckBox';
                trNode.find("." + PRICING_CELL_SELECTOR.PREPEND_CHECKBOX).prepend(pricingRowCheckBox);
                trNode.find("." + PRICING_CELL_SELECTOR.READ_ONLY).addClass('readOnly');

                // highlight text cells as mandatory if applicable
                _.each(textCells, function(textCell) {
                    if(pricingRow[textCell.selector] == '' && textCell.isMandatoryFnName && pricingRow[textCell.isMandatoryFnName]()) {
                        var pricingTextFields = trNode.find("." + textCell.selector);
                        pricingTextFields.addClass(PRICING_CELL_SELECTOR.MANDATORY_PRICING_CELL);
                    }
                });

                var rowReadOnly = pricingRow.isReadOnly && pricingRow.isReadOnly();
                var status = pricingRow.status;

                if(!rowReadOnly) {
                    addCheckBoxOnClick($(pricingRowCheckBox), trNode, pricingRow , status);

                    // if pricing row has already been discounted then select it by default
                    if(pricingRow.isSelected && pricingRow.isSelected()) {
                        pricingRowCheckBox.click();
                    }
                } else {
                    $(pricingRowCheckBox).attr('disabled', 'disabled');
                }
            }
        }

        if(onDrawCompleteFn) {
            onDrawCompleteFn();
        }
    };

    $(this._pricingDataTableSelector).dataTable(this.tableMeta);
};

PricingDataTable.prototype._initPricingSummary = function() {
    var bindDisableOnToggleBehaviour = function(radio, inputToEnable, inputToDisable) {
        radio.click(function() {
            disableElement(inputToDisable).val('');
            enableElement(inputToEnable);
        });
    };

    var getSummaryElements = function(radioSection) {
        radioSection = $(radioSection);

        return {
            percentRadio : radioSection.find('li.percent input[type=radio]'),
            percentInput : radioSection.find('li.percent input[type=text]'),
            netRadio : radioSection.find('li.net input[type=radio]'),
            netInput : radioSection.find('li.net input[type=text]')
        }
    };

    var setupBulkRadioBehaviour = function(radioSection) {
        var summaryElements = getSummaryElements(radioSection);
        bindDisableOnToggleBehaviour(summaryElements.percentRadio, summaryElements.percentInput, summaryElements.netInput);
        bindDisableOnToggleBehaviour(summaryElements.netRadio, summaryElements.netInput, summaryElements.percentInput);
    };

    // set up summary section
    setupBulkRadioBehaviour(this._pricingSummary.oneTimePricingActionsSelector);
    setupBulkRadioBehaviour(this._pricingSummary.recurringPricingActionsSelector);

    var that = this;

    this._pricingSummary.clearSummary = function() {
        var _clearSummary = function(summaryElements) {
            summaryElements.percentInput.val('');
            summaryElements.netInput.val('');
        };

        _clearSummary(getSummaryElements(that._pricingSummary.oneTimePricingActionsSelector));
        _clearSummary(getSummaryElements(that._pricingSummary.recurringPricingActionsSelector));
    };

    this._pricingSummary.enableSummary = function() {
        that._pricingSummary.enabled = true;

        var _enableSummary = function(summaryElements) {
            enableElement(summaryElements.percentRadio);
            enableElement(summaryElements.netRadio);
            summaryElements.percentRadio.click();
        };

        _enableSummary(getSummaryElements(that._pricingSummary.oneTimePricingActionsSelector));
        _enableSummary(getSummaryElements(that._pricingSummary.recurringPricingActionsSelector));
        enableButton($(that._pricingSummary.applyDiscountsButtonSelector));

        if(that._pricingSummary.summaryEnabledCallback) {
            that._pricingSummary.summaryEnabledCallback();
        }
    };

    this._pricingSummary.disableSummary = function() {
        that._pricingSummary.enabled = false;

        var _disableSummary = function(summaryElements) {
            disableElement(summaryElements.percentRadio);
            disableElement(summaryElements.netRadio);
            disableElement(summaryElements.percentInput);
            disableElement(summaryElements.netInput);
        };

        that._pricingSummary.clearSummary();
        _disableSummary(getSummaryElements(that._pricingSummary.oneTimePricingActionsSelector));
        _disableSummary(getSummaryElements(that._pricingSummary.recurringPricingActionsSelector));
        disableButton($(that._pricingSummary.applyDiscountsButtonSelector));

        if(that._pricingSummary.summaryDisabledCallback) {
            that._pricingSummary.summaryDisabledCallback();
        }
    };

    this._pricingSummary.onRowClicked = function() {
        var selectedRows = that.getSelectedRows();
        if(selectedRows.length > 0) {
            if(!that._pricingSummary.enabled) {
                that._pricingSummary.enableSummary();
            }
        } else {
            that._pricingSummary.disableSummary();
        }
    };

    $(this._pricingSummary.applyDiscountsButtonSelector).click(function() {
        var oneTimeSummaryElements = getSummaryElements(that._pricingSummary.oneTimePricingActionsSelector);
        var recurringSummaryElements = getSummaryElements(that._pricingSummary.recurringPricingActionsSelector);

        var ensureEmpty = function(value, allowNegativeDiscounting) {
            if(undefined == value || "" == value) {
                return "";
            } else if(isNaN(value)) {
                throw "Please enter a valid number!";
            } else if(undefined != allowNegativeDiscounting && !allowNegativeDiscounting) {
                if(parseFloat(value) > 100 || parseFloat(value) < 0) {
                    throw "Negative Discounting is not allowed.  Please enter a value between 0 and 100";
                }
            }

            return value;
        };

        try {
            var oneTimePercent = ensureEmpty(oneTimeSummaryElements.percentInput.val());
            var oneTimeNet = ensureEmpty(oneTimeSummaryElements.netInput.val());
            var recurringPercent = ensureEmpty(recurringSummaryElements.percentInput.val(), false);
            var recurringNet = ensureEmpty(recurringSummaryElements.netInput.val());

            var updateCellValue = function(trNode, selector, value) {
                var pricingCells = trNode.find("." + selector);

                pricingCells.each(function() {
                    var pricingCell = $(this);

                    if(pricingCell.hasClass(PRICING_CELL_SELECTOR.DISCOUNT)) {
                        pricingCell.html(value);
                        pricingCell.click();
                        pricingCell.find("input").blur();
                    }
                });
            };

            _.each(that.getSelectedRows(), function(selectedRow) {
                if("" != oneTimePercent) updateCellValue(selectedRow._trNode, PRICING_CELL_SELECTOR.BULK_ONE_TIME_DISCOUNT, oneTimePercent.asPercent());
                if("" != oneTimeNet) updateCellValue(selectedRow._trNode, PRICING_CELL_SELECTOR.BULK_ONE_TIME_NET, oneTimeNet.asCurrency());
                if("" != recurringPercent) updateCellValue(selectedRow._trNode, PRICING_CELL_SELECTOR.BULK_RECURRING_DISCOUNT, recurringPercent.asPercent());
                if("" != recurringNet) updateCellValue(selectedRow._trNode, PRICING_CELL_SELECTOR.BULK_RECURRING_NET, recurringNet.asCurrency());
            });
        } catch(err) {
            alert(err);
        }
    });

    this._pricingSummary.disableSummary();
};

PricingDataTable.prototype.refresh = function() {
    $(this._pricingDataTableSelector).dataTable().fnDraw();
};

PricingDataTable.prototype.filter = function(filterStr) {
    $(this._pricingDataTableSelector).dataTable().dataTable().fnFilter(filterStr);
};

PricingDataTable.prototype.bulkUpdate = function(selector, value) {
    _.each(this.getSelectedRows(), function(selectedRow) {
        var pricingCell = selectedRow._trNode.find("." + selector);
        pricingCell.html(value);
        pricingCell.click();
        pricingCell.find("input").blur();
    });
};

PricingDataTable.prototype.getSelectedRows = function() {
    return _.filter(this.pricingRows, function(pricingRow) {
        return pricingRow._isSelected;
    });
};

PricingDataTable.prototype.getUpdatedRows = function() {
    return _.filter(this.pricingRows, function(pricingRow) {
        return pricingRow.hasChanged;
    });
};

PricingDataTable.prototype.setupEditTextsForPricingRow = function(trNode, pricingRow, pricingRowEditTextMember, textSelector) {
    var that = this;

    var textModelUpdated = function(cell, pricingRow, pricingRowEditTextMember, selector, updatedValue) {
        if(pricingRowEditTextMember.maxLength && updatedValue.length > pricingRowEditTextMember.maxLength) {
            alert('Please enter a value no longer than ' + pricingRowEditTextMember.maxLength + ' characters.');
            return '';
        }

        if(undefined != updatedValue && updatedValue != '') {
            pricingTextFields.removeClass(PRICING_CELL_SELECTOR.MANDATORY_PRICING_CELL);
        } else if(pricingRowEditTextMember.isMandatoryFnName && pricingRow[pricingRowEditTextMember.isMandatoryFnName]()) {
            pricingTextFields.addClass(PRICING_CELL_SELECTOR.MANDATORY_PRICING_CELL);
        }

        var parent = cell.parent();
        parent.addClass("pricingRowChargeDiscount"); // mark cell as updated on UI

        pricingRow.hasChanged = true;
        pricingRow[pricingRowEditTextMember.selector] = updatedValue;

        parent.find(selector).html(updatedValue);

        if(that.onChangesFn) {
            that.onChangesFn();
        }

        return updatedValue;
    };

    var pricingTextFields = trNode.find(textSelector);

    pricingTextFields.editable('enable');
    pricingTextFields.editable(function(value) {
        return textModelUpdated($(this), pricingRow, pricingRowEditTextMember, textSelector, value);
    },
    {
        onblur: 'submit',
        cssclass: 'editable'
    });
};

PricingDataTable.prototype.setupDiscountingForPricingRow = function(trNode, pricingRow, pricingRowCharge, chargeSelector, discountFields) {
    var that = this;

    var getGrossCell = function(row, selector) {
        return row.find(selector + "Gross");
    };

    var getNetCell = function(row, selector) {
        return row.find(selector + "Net");
    };

    var getDiscountCell = function(row, selector) {
        return row.find(selector + "Discount." + PRICING_CELL_SELECTOR.DISCOUNT);
    };

    var getDiscountAndNetCells = function(row, selector) {
        return row.find(selector + "Discount."+ PRICING_CELL_SELECTOR.DISCOUNT +", " + selector + "Net." + PRICING_CELL_SELECTOR.DISCOUNT);
    };

    var getGrossAndDiscountAndNetCells = function(row, selector) {
        return row.find(selector + "Gross" +", " +selector + "Discount."+ PRICING_CELL_SELECTOR.DISCOUNT +", " + selector + "Net." + PRICING_CELL_SELECTOR.DISCOUNT);
    };

    var isNetCharge = function(element) {
        return element.attr("class").indexOf("Net") > -1
    };

    var isGrossCharge = function(element) {
            return element.attr("class").indexOf("Gross") > -1
    };

    var chargeModelUpdated = function(cell, chargeRow, charge, selector, net, discount) {
        var parent = cell.parent();
        parent.addClass("pricingRowChargeDiscount"); // mark discount as updated on UI

        chargeRow.hasChanged = true;
        charge.netTotal = net;
        charge.discount = discount;

        getDiscountCell(parent, selector).html(discount);
        getNetCell(parent, selector).html(net);

        if(that.onChangesFn) {
            that.onChangesFn();
        }
    };

    var discountUpdated = function(cell, value, selector, charge, chargeRow) {
        if(isNaN(value) || charge.discount == value.asPercent() || parseFloat(value) < 0 || parseFloat(value) > 100) {
            // return current value if user doesn't enter a valid number
            // or they enter the same number that's already stored.
            return charge.discount;
        }

        var discountedNetValue = applyDiscount(charge.value, value.asPercent()).asCurrency();
        chargeModelUpdated(cell, chargeRow, charge, selector, discountedNetValue, value.asPercent());

        return charge.discount;
    };

    var grossUpdated = function(cell, value, selector, charge, chargeRow) {
         if(isNaN(value) || charge.value == value.asCurrency()) {
           // return current value if user doesn't enter a valid number
           // or they enter the same number that's already stored.
           return charge.value;
         }

         var parent = cell.parent();
         chargeRow.hasChanged = true;
         charge.value = value;
         charge.netTotal = applyDiscount(charge.value, charge.discount.asPercent()).asCurrency();

         getNetCell(parent, selector).html(charge.netTotal);
         getGrossCell(parent, selector).html(value);

         if(that.onChangesFn) {
            that.onChangesFn();
         }

        return charge.value;
    };

    var netUpdated = function(cell, value, selector, charge, chargeRow) {
        if(isNaN(value) || charge.netTotal == value.asCurrency()) {
            // return current value if user doesn't enter a valid number
            // or they enter the same number that's already stored.
            return charge.netTotal;
        }

        var newDiscountValue = getDiscountFromNet(charge.value, value.asCurrency()).asPercent();
        chargeModelUpdated(cell, chargeRow, charge, selector, value.asCurrency(), newDiscountValue);

        return charge.netTotal;
    };


    var addGrossValue = function(selector, charge, chargeRow) {
            grossFields = getGrossCell(trNode, selector);
            grossFields.editable('enable');
            grossFields.removeClass("readOnly");
            grossFields.css('background-color', 'yellow');

            grossFields.editable(function(value) {
                 return grossUpdated($(this), value, selector, charge, chargeRow);;
            },
            { onblur: 'submit',
              cssclass: 'editable'
            });
        };


    var addDiscount = function(selector, charge, chargeRow) {
        var pricingDiscountFields = getDiscountAndNetCells(trNode, selector);

        if(charge.isManualPricing){
          pricingDiscountFields = getGrossAndDiscountAndNetCells(trNode, selector);
        }

        pricingDiscountFields.editable('enable');
        pricingDiscountFields.editable(function(value) {
            if(isGrossCharge($(this))) {
                 return grossUpdated($(this), value, selector, charge, chargeRow);
            }
            else if(isNetCharge($(this))) {
                discountFields.removeClass("readOnly");
                return netUpdated($(this), value, selector, charge, chargeRow);
            } else {
                return discountUpdated($(this), value, selector, charge, chargeRow);
            }
        },
        {
            onblur: 'submit',
            cssclass: 'editable'
        });
    };

    var removeDiscount = function(selector) {
        getDiscountAndNetCells(trNode, selector).removeClass(PRICING_CELL_SELECTOR.DISCOUNT);
    };

    var setupDiscount = function(chargeRow, charge, selector) {
        if(charge.isManualPricing() && charge.isGrossValueNotPresent()){
           addGrossValue(selector, charge, chargeRow);
        }
        else if(charge.isSet()) {
            addDiscount(selector, charge, chargeRow);
        } else {
            removeDiscount(selector);
        }
    };

    setupDiscount(pricingRow, pricingRowCharge, chargeSelector);
};

/*
Static Utility Methods
*/

function createBeanToRowMeta(metaArray, propertyName, classNames, width) {
    var meta = {"mDataProp": propertyName};
    if(width) {
        meta.sWidth = width;
    }

    if(classNames.length > 0) {
        meta.sClass = "";
    }

    _.each(classNames, function(className) {
        meta.sClass += className + " ";
    });

    metaArray[metaArray.length] = meta;
}

function pricingTableMetaFor(sourceUri, columnMeta, dataPropertyFn) {
    return {
        sPaginationType: "full_numbers",
        sDom: 'lrt<"table_footer"ip>',
        sAjaxSource: sourceUri,
        bAutoWidth: false,
        bProcessing: true,
        bServerSide: true,
        bDeferRender: true,
        bSort: false,
        bRetrieve: true,
        bLengthChange : true,
        bScrollCollapse: false,
        sScrollY: ($(window).height() - 340) + "px",
        bStateSave: false,
        bFilter: true,
        iDisplayLength: 10,
        aLengthMenu: [1, 5, 10, 20, 100],
        aoColumns: columnMeta,
        sAjaxDataProp: dataPropertyFn,
        oLanguage : {
            sInfo: "Showing _START_ to _END_ of _TOTAL_ Root Products",
            sLengthMenu: "Display _MENU_ Root Products"
        }
    }
}

function createProductRow(trNode, rows) {
    var rowHtml = "<tr class='product_group group_0'>";

    _.each(rows, function(row) {
        rowHtml += "<td colspan='"+row.colSpan+"'>"+row.value+"</td>";
    });

    rowHtml += "</tr>";

    trNode.replaceWith(rowHtml);
}

function ensureArray(array) {
    if(undefined == array || "" == array) {
        return [];
    }

    if (array.push) {
        return array;
    }

    return [array];
}

function applyDiscount(grossValue, discountValue) {
    return grossValue - (discountValue / 100) * grossValue;
}

function getDiscountFromNet(grossValue, netValue) {
    return 100 - ((netValue / grossValue) * 100);
}

function mixinChargeMethods(charge) {
    charge.isSet = function() {
        return "" != charge.value && "" != charge.discount && "" != charge.netTotal;
    };
}

function mixinManualPriceMethods(charge) {
    charge.isManualPricing = function() {
        return false;
    };
}

function mixinGrossValueMethods(charge) {
    charge.isGrossValueNotPresent = function() {
        return false;
    };
}
