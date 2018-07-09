describe('Quote Option Pricing Tab', function() {

    var oneTimePercentField = "#bulkDiscountOneTimePercent";
    var oneTimeNettField = "#bulkDiscountOneTimeNett";
    var recurringPercentField = "#bulkDiscountRecurringPercent";
    var recurringNettField = "#bulkDiscountRecurringNett";
    var applyBulkDiscountButton = "#applyBulkDiscount";
    var oneTimeRadioSelector = "#bulkDiscount input:radio:checked[name='oneTime']";
    var recurringRadioSelector = "#bulkDiscount input:radio:checked[name='recurring']";
    var oneTimePercentRadioSelector = "#bulkDiscount input:radio[name='oneTime'][value='percent']";
    var oneTimeNettRadioSelector = "#bulkDiscount input:radio[name='oneTime'][value='nett']";
    var recurringPercentRadioSelector = "#bulkDiscount input:radio[name='recurring'][value='percent']";
    var recurringNettRadioSelector = "#bulkDiscount input:radio[name='recurring'][value='nett']";
    var percentRadioValue = "percent";
    var nettRadioValue = "nett";
    var oneTimeDiscountField = 'oneTime_discount';
    var oneTimeNettTotalField = 'oneTime_netTotal';
    var recurringDiscountField = 'recurring_discount';
    var recurringNettTotalField = 'recurring_netTotal';
    var oneTimeValue = 'oneTime_value';
    var recurringValue = 'recurring_value';
    var selectAll = "#selectAll";
    var selectNone = "#selectAll";

    describe('Given three itemDto with oneTime and recurring priceLines', function () {

        beforeEach(function() {
            spyOn($, "ajax").andCallFake(function(options) {
                options.success(tableJson);
            });
            systemUnderTest = new rsqe.QuoteOptionPricingTab();
            systemUnderTest.initialise();
            waitsFor(function() {
                return $("tr.priceLine").length == 7;
            }, "Rows did not appear", 1000);
        });

        afterEach(function() {
            systemUnderTest.destroy();
        });

        it("should have empty bulk discount boxes with percent radio selected and a checkbox per row", function() {
            expect($(oneTimePercentField).val()).toEqual("");
            expect($(oneTimeNettField).val()).toEqual("");
            expect($(recurringPercentField).val()).toEqual("");
            expect($(recurringNettField).val()).toEqual("");

            expect($(oneTimeRadioSelector).val()).toEqual(percentRadioValue);
            expect($(recurringRadioSelector).val()).toEqual(percentRadioValue);
            expect(getCheckboxes().length).toEqual(7);
        });

        describe('Check price lines 1 and 2', function() {

            beforeEach(function() {
                getCheckboxes("id_o1").attr('checked', 'checked');
                getCheckboxes("id_o2").attr('checked', 'checked');
            });

            afterEach(function() {
                getCheckboxes("id_o1").removeAttr('checked');
                getCheckboxes("id_o2").removeAttr('checked');
            });

            it('settings one-time percentage discount should apply changes to table', function() {
                $(oneTimePercentField).focus().val("10");
                $(applyBulkDiscountButton).click();
                assertOneTimePercentDiscount("id_o1", 10.0);
                assertOneTimePercentDiscount("id_o2", 10.0);
                assertOneTimePercentDiscount("id_o3", 0.0);
            });

            function assertOneTimePercentDiscount(oneTimeId, discount) {
                var value = getCellValue(oneTimeId, oneTimeValue);
                var percent = getCellValue(oneTimeId, oneTimeDiscountField);
                var nett = getCellValue(oneTimeId, oneTimeNettTotalField);
                expect(percent).toEqual(discount);
                expect(nett).toEqual(value - value * discount / 100);
            }

            it('settings one-time nett value should apply changes to table', function() {
                $(oneTimeNettField).focus().val("5");
                $(applyBulkDiscountButton).click();
                assertOneTimeNettDiscount("id_o1", 5.0);
                assertOneTimeNettDiscount("id_o2", 5.0);
                assertOneTimeNettDiscount("id_o3");
            });

            function assertOneTimeNettDiscount(oneTimeId, nettValue) {
                var value = getCellValue(oneTimeId, oneTimeValue);
                if (!nettValue) {
                    nettValue = value;
                }
                var percent = getCellValue(oneTimeId, oneTimeDiscountField);
                var nett = getCellValue(oneTimeId, oneTimeNettTotalField);
                expect(nett).toEqual(nettValue);
                expect(percent).toEqual((1 - nettValue / value) * 100);
            }

            it('settings recurring percentage discount should apply changes to table', function() {
                $(recurringPercentField).focus().val("20");
                $(applyBulkDiscountButton).click();
                assertRecurringPercentDiscount("id_o1", 20.0);
                assertRecurringPercentDiscount("id_o2", 20.0);
                assertRecurringPercentDiscount("id_o3", 0.0);
            });


            it('setting zero recurring percentage discount should apply changes to table', function() {
                $(recurringPercentField).focus().val("0");
                $(applyBulkDiscountButton).click();
                assertRecurringPercentDiscount("id_o1", 0.0);
                assertRecurringPercentDiscount("id_o2", 0.0);
            });


            function assertRecurringPercentDiscount(recurringId, discount) {
                var value = getCellValue(recurringId, recurringValue);
                var percent = getCellValue(recurringId, recurringDiscountField);
                var nett = getCellValue(recurringId, recurringNettTotalField);
                expect(percent).toEqual(discount);
                expect(nett).toEqual(value - value * discount / 100);
            }

            it('settings recurring nett value should apply changes to table', function() {
                $(recurringNettField).focus().val("5");
                $(applyBulkDiscountButton).click();
                assertRecurringNettDiscount("id_o1", 5.0);
                assertRecurringNettDiscount("id_o2", 5.0);
                assertRecurringNettDiscount("id_o3");
            });

            it('setting zero recurring nett value should apply changes to table', function() {
                $(recurringNettField).focus().val("0");
                $(applyBulkDiscountButton).click();
                assertRecurringNettDiscount("id_o1", 0.0);
                assertRecurringNettDiscount("id_o2", 0.0);
            });

            function assertRecurringNettDiscount(recurringId, nettValue) {
                var value = getCellValue(recurringId, recurringValue);
                if (!nettValue && nettValue !== 0) {
                    nettValue = value;
                }
                var percent = getCellValue(recurringId, recurringDiscountField);
                var nett = getCellValue(recurringId, recurringNettTotalField);
                expect(nett).toEqual(nettValue);
                expect(percent).toEqual((1 - nettValue / value) * 100);
            }
        });

        describe("Clicking select all", function() {

            it("should select all price line checkboxes", function() {
                $(selectAll).click();
                expect($("#priceLines tbody .description input:checked").length).toEqual(7);
            });
        });

        describe("Clicking deselect all", function() {

            beforeEach(function() {
                $(selectAll).click();
            });

            it("should deselect all price line checkboxes", function() {
                $(selectNone).click();
                expect($("#priceLines tbody .description input:checked").length).toEqual(0);
            });
        });

        describe("Exclusive discount type entry", function() {

            describe("Focus into one-time percentage box", function() {
                beforeEach(function() {
                    $(oneTimeNettField).val("10");
                });
                it("should clear nett box and select percentage radio", function() {
                    $(oneTimePercentField).focus();
                    expect($(oneTimeNettField).val()).toEqual("");
                    expect($(oneTimeRadioSelector).val()).toEqual(percentRadioValue)
                });
            });

            describe("Focus into one-time nett box", function() {
                beforeEach(function() {
                    $(oneTimePercentField).val("10");
                });
                it("should clear percentage box and select nett radio", function() {
                    $(oneTimeNettField).focus();
                    expect($(oneTimePercentField).val()).toEqual("");
                    expect($(oneTimeRadioSelector).val()).toEqual(nettRadioValue)
                });
            });

            describe("Focus into recurring percentage box", function() {
                beforeEach(function() {
                    $(recurringNettField).val("20");
                });
                it("should clear nett box and select percentage radio", function() {
                    $(recurringPercentField).focus();
                    expect($(recurringNettField).val()).toEqual("");
                    expect($(recurringRadioSelector).val()).toEqual(percentRadioValue)
                });
            });

            describe("Focus into recurring nett box", function() {
                beforeEach(function() {
                    $(recurringPercentField).val("10");
                });
                it("should clear percentage box and select nett radio", function() {
                    $(recurringNettField).focus();
                    expect($(recurringPercentField).val()).toEqual("");
                    expect($(recurringRadioSelector).val()).toEqual(nettRadioValue)
                });
            });

            describe("Click one-time percentage radio", function() {
                beforeEach(function() {
                    $(oneTimeNettField).val("10");
                });
                it("should clear nett box and select percentage radio", function() {
                    $(oneTimePercentRadioSelector).click();
                    expect($(oneTimeNettField).val()).toEqual("");
                });
            });

            describe("Click one-time nett radio", function() {
                beforeEach(function() {
                    $(oneTimePercentField).val("10");
                });
                it("should clear percentage box and select nett radio", function() {
                    $(oneTimeNettRadioSelector).click();
                    expect($(oneTimePercentField).val()).toEqual("");
                });
            });

            describe("Click recurring percentage radio", function() {
                beforeEach(function() {
                    $(recurringNettField).val("10");
                });
                it("should clear nett box and select percentage radio", function() {
                    $(recurringPercentRadioSelector).click();
                    expect($(recurringNettField).val()).toEqual("");
                });
            });

            describe("Click recurring nett radio", function() {
                beforeEach(function() {
                    $(recurringPercentField).val("10");
                });
                it("should clear percentage box and select nett radio", function() {
                    $(recurringNettRadioSelector).click();
                    expect($(recurringPercentField).val()).toEqual("");
                });
            });

            describe("Clicking apply discounts", function() {
                beforeEach(function() {
                    getCheckboxes("id_o1").attr('checked', 'checked');
                    getCheckboxes("id_o2").attr('checked', 'checked');
                    $(oneTimePercentField).val("20");
                    $(recurringNettField).val("10");
                });

                it("should reset bulk form", function() {
                    $(applyBulkDiscountButton).click();
                    expect(getCheckboxes("id_o1").attr("checked") === undefined).toEqual(true);
                    expect(getCheckboxes("id_o2").attr("checked") === undefined).toEqual(true);
                    expect($(oneTimePercentField).val()).toEqual("");
                    expect($(oneTimeNettField).val()).toEqual("");
                    expect($(recurringNettField).val()).toEqual("");
                    expect($(recurringPercentField).val()).toEqual("");
                });
            });
        });

        describe("Applying discounts using spaces as entries", function() {

            it("should not apply percentage discount", function() {
                $(selectAll).click();
                $(oneTimePercentField).focus().val(" ");
                $(recurringPercentField).focus().val(" ");
                $(applyBulkDiscountButton).click();

                expect(getCellValue("id_o6", "oneTime_discount")).toEqual(10.0);
                expect(getCellValue("id_o6", "recurring_discount")).toEqual(10.0);
            });

            it("should not apply nett discount", function() {
                $(selectAll).click();
                $(oneTimeNettField).focus().val(" ");
                $(recurringNettField).focus().val(" ");
                $(applyBulkDiscountButton).click();

                expect(getCellValue("id_o6", "oneTime_discount")).toEqual(10.0);
                expect(getCellValue("id_o6", "recurring_discount")).toEqual(10.0);
            });
        });

        describe("Applying discounts using other characters as entries", function() {

            it("should not apply percentage discount", function() {
                $(selectAll).click();
                $(oneTimePercentField).focus().val("aString");
                $(recurringPercentField).focus().val("aString");
                $(applyBulkDiscountButton).click();

                expect(getCellValue("id_o6", "oneTime_discount")).toEqual(10.0);
                expect(getCellValue("id_o6", "recurring_discount")).toEqual(10.0);
            });

            it("should not apply nett discount", function() {
                $(selectAll).click();
                $(oneTimeNettField).focus().val("aString");
                $(recurringNettField).focus().val("aString");
                $(applyBulkDiscountButton).click();

                expect(getCellValue("id_o6", "oneTime_discount")).toEqual(10.0);
                expect(getCellValue("id_o6", "recurring_discount")).toEqual(10.0);
            });
        });

        describe("Should not apply discount", function() {

            beforeEach(function() {
                $(selectAll).click();
                $(oneTimePercentField).focus().val("5");
                $(recurringPercentField).focus().val("5");
                $(applyBulkDiscountButton).click();
            });

            it("to non existent one time priceline", function() {
                var selectorPrefix = "tr[onetime_id='id_'] td.";
                expect($(selectorPrefix + "oneTime_value").text()).toEqual("");
                expect($(selectorPrefix + "oneTime_discount").text()).toEqual("");
                expect($(selectorPrefix + "oneTime_netTotal").text()).toEqual("");
            });

            it("to non existent recurring priceline", function() {
                var selectorPrefix = "tr[recurring_id='id_'] td.";
                expect($(selectorPrefix + "recurring_value").text()).toEqual("");
                expect($(selectorPrefix + "recurring_discount").text()).toEqual("");
                expect($(selectorPrefix + "recurring_netTotal").text()).toEqual("");
            });
        });

        describe("When gross is 0 should not apply discount", function() {
            beforeEach(function() {
                $(selectAll).click();
            });

            it("percentage to recurring", function() {
                $(recurringPercentField).focus().val("0");
                $(applyBulkDiscountButton).click();
                assertRecurringIsZero("id_o7");
            });

            it("nett to recurring", function() {
                $(recurringNettField).focus().val("0");
                $(applyBulkDiscountButton).click();
                assertRecurringIsZero("id_o7");
            });

            function assertRecurringIsZero(recurringId) {
                var percent = getCellValue(recurringId, recurringDiscountField);
                var nett = getCellValue(recurringId, recurringNettTotalField);
                expect(nett).toEqual(0);
                expect(percent).toEqual(0);
            };

            it("percentage to onetime", function() {
                $(oneTimePercentField).focus().val("0");
                $(applyBulkDiscountButton).click();
                assertRecurringIsZero("id_o7");
            });

            it("nett to onetime", function() {
                $(oneTimeNettField).focus().val("0");
                $(applyBulkDiscountButton).click();
                assertRecurringIsZero("id_o7");
            });

            function assertOneTimeIsZero(recurringId) {
                var percent = getCellValue(recurringId, oneTimeDiscountField);
                var nett = getCellValue(recurringId, oneTimeNettTotalField);
                expect(nett).toEqual(0);
                expect(percent).toEqual(0);
            };
        })
    });

    function getCheckboxes(oneTimeId) {
        var selector = "tr.priceLine";
        if (oneTimeId) {
            selector += "[onetime_id='" + oneTimeId + "']";
        }
        selector += " input:checkbox";
        return $(selector);
    }

    function getCellValue(oneTimeId, cellClass) {
        return parseFloat($("tr.priceLine[onetime_id='" + oneTimeId + "'] td." + cellClass).text());
    }
});
