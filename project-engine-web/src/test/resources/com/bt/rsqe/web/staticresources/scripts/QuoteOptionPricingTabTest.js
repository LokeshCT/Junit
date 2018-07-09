describe('Quote Option Pricing Tab', function() {
    var oneTimeDiscountFormInputId = '.oneTime_discount form input';
    var oneTimeNetTotalFormInputId = '.oneTime_netTotal form input';
    var oneTimeDiscountFieldId = '.oneTime_discount';
    var oneTimeNetTotalFieldId = '.oneTime_netTotal';
    var recurringDiscountFormInputId = '.recurring_discount form input';
    var recurringNetTotalFormInputId = '.recurring_netTotal form input';
    var recurringDiscountFieldId = '.recurring_discount';
    var recurringNetTotalFieldId = '.recurring_netTotal';
    var systemUnderTest;

    describe('Given one itemDto with no oneTime or recurring priceLine', function () {
        var json = JSON.parse('{"itemDTOs":[' +
                              '{"aggregateRow":"false",' +
                              ' "groupingLevel":"0",' +
                              ' "site":"SITE_ID_0", ' +
                              ' "summary":"10Mbps", ' +
                              ' "product":"PRODUCT_SCODE", ' +
                              ' "description":"DESCRIPTION", ' +
                              ' "oneTime" : { "id":"", "value":"", "discount":"", "netTotal":"" },' +
                              ' "recurring": { "id":"", "value":"", "discount":"", "netTotal":"" },' +
                              ' "lineItemId": "lineItemId",' +
                              ' "status": "BUDGETARY" }' +
                              '],"sEcho":"1","iTotalDisplayRecords":"1","iTotalRecords":"1"}');
        beforeEach(function() {
            spyOn($, "ajax").andCallFake(function(options) {
                options.success(json);
            });
            systemUnderTest = new rsqe.QuoteOptionPricingTab();
            systemUnderTest.initialise();
        });
        afterEach(function() {
            systemUnderTest.destroy();
        });
        describe('When clicking the oneTime discount field', function() {
            it('should NOT make it editable', function() {
                $(oneTimeDiscountFieldId).click();
                expect($(oneTimeDiscountFormInputId).length).toEqual(0);
            });
        });

        describe('When clicking the recurring discount field', function() {
            it('should NOT make it editable', function() {
                $(recurringDiscountFieldId).click();
                expect($(recurringDiscountFormInputId).length).toEqual(0);
            });
        });

        describe('When clicking the oneTime netTotal field', function() {
            it('should NOT make it editable', function() {
                $(oneTimeNetTotalFieldId).click();
                expect($(oneTimeNetTotalFormInputId).length).toEqual(0);
            });
        });

        describe('When clicking the recurring discount field', function() {
            it('should NOT make it editable', function() {
                $(recurringNetTotalFieldId).click();
                expect($(recurringNetTotalFormInputId).length).toEqual(0);
            });
        });

    });

    describe('Given one itemDto', function() {
        var json = JSON.parse('{"itemDTOs":[' +
                              '{"aggregateRow":"false",' +
                              ' "groupingLevel":"0",' +
                              ' "site":"SITE_ID_0", ' +
                              ' "summary":"summary", ' +
                              ' "product":"PRODUCT_SCODE", ' +
                              ' "description":"DESCRIPTION", ' +
                              ' "oneTime" : { "id":"oneTime", "value":"10.00", "discount":"0.00", "netTotal":"10.00" },' +
                              ' "recurring": { "id":"recurring", "value":"10.00", "discount":"0.00", "netTotal":"10.00" },' +
                              ' "lineItemId": "lineItemId",' +
                              ' "status": "BUDGETARY" }' +
                              '],"sEcho":"1","iTotalDisplayRecords":"1","iTotalRecords":"1"}');
        beforeEach(function() {
            spyOn($, "ajax").andCallFake(function(options) {
                options.success(json);
            });
            systemUnderTest = new rsqe.QuoteOptionPricingTab();
            systemUnderTest.initialise();
        });
        afterEach(function() {
            systemUnderTest.destroy();
        });

        describe('When clicking a discount field', function() {
            beforeEach(function() {
                $(oneTimeDiscountFieldId).click();
                $(recurringDiscountFieldId).click();
            });

            it('should make it editable', function() {
                expect($(oneTimeDiscountFormInputId).length).not.toEqual(0);
                expect($(recurringDiscountFormInputId).length).not.toEqual(0);
            });
        });

        describe('When changing the discount field value', function() {
            beforeEach(function() {
                $(oneTimeDiscountFieldId).click();
                $(recurringDiscountFieldId).click();
            });

            it('should update the net total field for 10%', function() {
                runTest("10.00", "9.00");
            });

            it('should update the net total field for 0%', function() {
                runTest("0", "10.00");
            });

            it('should update the net total field for -10%', function() {
                runTest("-10", "11.00");
            });

            it('should update the net total field for 10.33%', function() {
                runTest("10.33", "8.97");
            });

            // dodgy test
            xit('should update the net total field for 10.394%', function() {
                runTest("10.335", "8.97");
            });

            it('should update the net total field for 10.395%', function() {
                runTest("10.395", "8.96");
            });

            function runTest(value, expected) {
                runs(function() {
                    $(oneTimeDiscountFormInputId).val(value);
                    $(recurringDiscountFormInputId).val(value);
                    $(oneTimeDiscountFormInputId).blur();
                    $(recurringDiscountFormInputId).blur();
                });
                waitsFor(function() {
                    return $(oneTimeDiscountFormInputId).length === 0;// && $(recurringDiscountFormInputId).length === 0;
                }, "Blur didn't happen", 500);
                runs(function() {
                    expect($(oneTimeNetTotalFieldId).html()).toEqual(expected);
                    expect($(recurringNetTotalFieldId).html()).toEqual(expected);
                });
            }

            describe("and I enter an invalid number", function() {
                it("should set the discount to 0.00", function() {
                    runs(function() {
                        $(oneTimeDiscountFormInputId).val("Hello Bob");
                        $(oneTimeDiscountFormInputId).blur();
                    });
                    waitsFor(function() {
                        return $(oneTimeDiscountFormInputId).length === 0;
                    }, "Blur didn't happen", 500);
                    runs(function() {
                        expect($(oneTimeNetTotalFieldId).html()).toEqual("10.00");
                        expect($(oneTimeDiscountFieldId).html()).toEqual("0.00");
                        expect($('tr').hasClass('changeDiscount')).toBeFalsy();
                    });
                });
            });
        });

        describe('When clicking the net Total  field', function() {
            beforeEach(function() {
                $(oneTimeNetTotalFieldId).click();
                $(recurringNetTotalFieldId).click();
            });

            it('should make it editable', function() {
                expect($(oneTimeNetTotalFormInputId).length).not.toEqual(0);
                expect($(recurringNetTotalFormInputId).length).not.toEqual(0);
            });
        });

        describe('When changing the net total field value', function() {
            beforeEach(function() {
                $(oneTimeNetTotalFieldId).click();
                $(recurringNetTotalFieldId).click();
            });

            it('should update the discount field for 9', function() {
                runTest("9.00", "10.00000");
            });

            it('should update the discount  field for -10', function() {
                runTest("-10.00", "200.00000");
            });

            it('should update the discount  field for 0', function() {
                runTest("0", "100.00000");
            });

            it('should update the discount field for 10.33', function() {
                runTest("10.33", "-3.30000");
            });

            function runTest(value, expected) {
                runs(function() {
                    $(oneTimeNetTotalFormInputId).val(value);
                    $(recurringNetTotalFormInputId).val(value);
                    $(oneTimeNetTotalFormInputId).blur();
                    $(recurringNetTotalFormInputId).blur();
                });
                waitsFor(function() {
                    return $(oneTimeNetTotalFormInputId).length === 0 && $(recurringNetTotalFormInputId).length === 0;
                }, "Blur didn't happen", 500);
                runs(function() {
                    expect($(oneTimeDiscountFieldId).html()).toEqual(expected);
                    expect($(recurringDiscountFieldId).html()).toEqual(expected);
                    expect($('tr').hasClass('recurringChangeDiscount')).toBeTruthy();
                    expect($('tr').hasClass('oneTimeChangeDiscount')).toBeTruthy();
                });
            }
        });

        describe('When changing the filter on datatables', function() {
            it('should maintain previously set discount', function() {
                runs(function() {
                    $(oneTimeDiscountFieldId).click();
                    $(oneTimeDiscountFormInputId).val("10");
                    $(oneTimeDiscountFormInputId).blur();
                    $(recurringDiscountFieldId).click();
                    $(recurringDiscountFormInputId).val("10");
                    $(recurringDiscountFormInputId).blur();
                });
                waitsFor(function() {
                    return $(oneTimeDiscountFormInputId).length === 0 && $(recurringDiscountFormInputId).length === 0;
                }, "Blur didn't happen", 500);
                runs(function() {
                    var select = $('select[name="priceLines_length"]');
                    json.sEcho = 2;
                    select.change();
                    json.sEcho = 1;
                });
                waitsFor(function() {
                    return $('#priceLines_processing').css('visibility') == 'hidden';
                }, "Processing to go away", 1000);
                runs(function() {
                    expect($(oneTimeNetTotalFieldId).html()).toEqual("9.00");
                    expect($(oneTimeDiscountFieldId).html()).toEqual("10.00");
                    expect($(recurringNetTotalFieldId).html()).toEqual("9.00");
                    expect($(recurringDiscountFieldId).html()).toEqual("10.00");
                    expect($('tr').hasClass('oneTimeChangeDiscount')).toBeTruthy();
                    expect($('tr').hasClass('recurringChangeDiscount')).toBeTruthy();
                });
            });
        });

        describe('When selecting an editable field and switching to the other', function() {
            it('Should work allow you to edit net total', function() {
                runs(function() {
                    $(oneTimeDiscountFieldId).click();
                    $(oneTimeDiscountFormInputId).blur();
                    $(oneTimeNetTotalFieldId).click();
                });
                waitsFor(function() {
                    return $(oneTimeDiscountFormInputId).length === 0;
                }, "Blur didn't happen", 500);
                runs(function() {
                    expect($(oneTimeNetTotalFormInputId).length).toEqual(1);
                    $(oneTimeNetTotalFormInputId).blur();
                });
                waitsFor(function() {
                    return $(oneTimeDiscountFormInputId).length === 0;
                }, "Blur didn't happen", 500);

            });
            it('Should work allow you to edit discount', function() {
                runs(function() {
                    $(oneTimeNetTotalFieldId).click();
                    $(oneTimeNetTotalFormInputId).blur();
                    $(oneTimeDiscountFieldId).click();
                });
                waitsFor(function() {
                    return $(oneTimeNetTotalFormInputId).length === 0;
                }, "Blur didn't happen", 500);
                runs(function() {
                    expect($(oneTimeDiscountFormInputId).length).toEqual(1);
                    $(oneTimeDiscountFormInputId).blur();
                });
                waitsFor(function() {
                    return $(oneTimeDiscountFormInputId).length === 0;
                }, "Blur didn't happen", 500);

            });

        });

        describe('When changing the discount values', function() {
            it('User should be prompted to save the discount changes', function() {
                runs(function() {
                    $(oneTimeNetTotalFieldId).click();
                    $(oneTimeNetTotalFormInputId).val("15.00");
                    $(oneTimeNetTotalFormInputId).blur();
                });
                waitsFor(function() {
                    return $(oneTimeNetTotalFormInputId).length === 0;
                }, "Blur didn't happen", 500);
                runs(function() {
                    expect($('#unsavedDiscounts').hasClass("hidden")).toBeFalsy();
                });
            });
        });

        describe('When user saves the changes', function() {
            beforeEach(function() {
                runs(function() {
                    $(oneTimeNetTotalFieldId).click();
                    $(oneTimeNetTotalFormInputId).val("15.00");
                    $(oneTimeNetTotalFormInputId).blur();
                    $(recurringNetTotalFieldId).click();
                    $(recurringNetTotalFormInputId).val("15.00");
                    $(recurringNetTotalFormInputId).blur();
                });
                waitsFor(function() {
                    return $(oneTimeNetTotalFormInputId).length === 0 && $(recurringNetTotalFormInputId).length === 0;
                }, "Blur didn't happen", 500);
                runs(function() {
                    spyOn($, "post").andCallFake(function(url, data, callback) {
                        expect(url).toEqual("/rsqe/customers/customerId/projects/projectId/quote-options/quoteOptionId/discounts");
                        expect(data).toEqual(JSON.stringify({ lineItemId: { id_oneTime : "-50.00000", id_recurring : "-50.00000" } }));
                        callback();
                        return { error: function () {} };
                    });
                    $("#persistDiscounts").click();
                });
            });

            it('should save the changes', function() {
                runs(function() {
                    expect($('#unsavedDiscounts').hasClass("hidden")).toBeTruthy();
                    expect($('tr').hasClass('oneTimeChangeDiscount')).toBeFalsy();
                });
            });
        });

        describe('When user discards the changes', function () {
            beforeEach(function() {
                runs(function() {
                    $(oneTimeNetTotalFormInputId).click();
                    $(oneTimeNetTotalFormInputId).val("15.00");
                    $(oneTimeNetTotalFormInputId).blur();
                });
                waitsFor(function() {
                    return $(oneTimeNetTotalFormInputId).length === 0;
                }, "Blur didn't happen", 500);
                runs(function() {
                    $("#discardDiscounts").click();
                });
            });

            it('should hide the unsavedChanges div', function() {
                runs(function() {
                    expect($('#unsavedDiscounts').hasClass("hidden")).toBeTruthy();
                    expect($('tr').hasClass('oneTimeChangeDiscount')).toBeFalsy();
                    expect($(oneTimeNetTotalFieldId).html()).toEqual("10.00");
                });
            });

        });

        describe('When user changes netTotal or discount fields', function() {
            beforeEach(function() {
                runs(function() {
                    $(oneTimeNetTotalFieldId).click();
                    $(oneTimeNetTotalFormInputId).val("16.00");
                    $(oneTimeNetTotalFormInputId).blur();
                    $(recurringNetTotalFieldId).click();
                    $(recurringNetTotalFormInputId).val("16.00");
                    $(recurringNetTotalFormInputId).blur();
                });
                waitsFor(function() {
                    return $(oneTimeNetTotalFormInputId).length === 0 && $(recurringNetTotalFormInputId).length === 0;
                }, "Blur didn't happen", 500);

            });
            it('The netTotal and discount fields should be  highlighted', function() {
                runs(function() {
                    expect($('tr').hasClass('oneTimeChangeDiscount')).toBeTruthy();
                    expect($('tr').hasClass('recurringChangeDiscount')).toBeTruthy();
                });
            });
        });
    });

    describe('Given one itemDto', function() {
        var json = JSON.parse('{"itemDTOs":[' +
                              '{"aggregateRow":"false",' +
                              ' "groupingLevel":"0",' +
                              ' "site":"SITE_ID_0", ' +
                              ' "summary":"summary", ' +
                              ' "product":"PRODUCT_SCODE", ' +
                              ' "description":"DESCRIPTION", ' +
                              ' "oneTime" : { "id":"oneTime", "value":"100.00", "discount":"20.00", "netTotal":"80.00" },' +
                              ' "recurring": { "id":"recurring", "value":"15.00", "discount":"10.00", "netTotal":"13.50" },' +
                              ' "lineItemId": "lineItemId",' +
                              ' "status": "BUDGETARY" }' +
                              '],"sEcho":"1","iTotalDisplayRecords":"1","iTotalRecords":"1"}');
        beforeEach(function() {
            spyOn($, "ajax").andCallFake(function(options) {
                options.success(json);
            });
            systemUnderTest = new rsqe.QuoteOptionPricingTab();
            systemUnderTest.initialise();
        });
        afterEach(function() {
            systemUnderTest.destroy();
        });

        describe('When trying to change discounts fields values', function() {

            describe("and I enter an invalid number for one time discount", function() {
                it("should ignore the change and retain old value", function() {
                    runs(function() {
                        $(oneTimeDiscountFieldId).click();
                        $(oneTimeDiscountFormInputId).val("Hello Bob");
                        $(oneTimeDiscountFormInputId).blur();
                    });
                    waitsFor(function() {
                        return $(oneTimeDiscountFormInputId).length === 0;
                    }, "Blur didn't happen", 500);
                    runs(function() {
                        expect($(oneTimeNetTotalFieldId).html()).toEqual("80.00");
                        expect($(oneTimeDiscountFieldId).html()).toEqual("20.00000");
                        expect($('tr').hasClass('changeDiscount')).toBeFalsy();
                    });
                });
            });

            describe("and I enter an invalid number for one time net", function() {
                it("should ignore the change and retain old value", function() {
                    runs(function() {
                        $(oneTimeNetTotalFieldId).click();
                        $(oneTimeNetTotalFormInputId).val("Goodbye Uncle");
                        $(oneTimeNetTotalFormInputId).blur();
                    });
                    waitsFor(function() {
                        return $(oneTimeNetTotalFormInputId).length === 0;
                    }, "Blur didn't happen", 500);
                    runs(function() {
                        expect($(oneTimeNetTotalFieldId).html()).toEqual("80.00");
                        expect($(oneTimeDiscountFieldId).html()).toEqual("20.00");
                        expect($('tr').hasClass('changeDiscount')).toBeFalsy();
                    });
                });
            });

            describe("and I enter an invalid number for recurring discount", function() {
                it("should ignore the change and retain old value", function() {
                    runs(function() {
                        $(recurringDiscountFieldId).click();
                        $(recurringDiscountFormInputId).val("Invalid");
                        $(recurringDiscountFormInputId).blur();
                    });
                    waitsFor(function() {
                        return $(recurringDiscountFormInputId).length === 0;
                    }, "Blur didn't happen", 500);
                    runs(function() {
                        expect($(recurringNetTotalFieldId).html()).toEqual("13.50");
                        expect($(recurringDiscountFieldId).html()).toEqual("10.00000");
                        expect($('tr').hasClass('changeDiscount')).toBeFalsy();
                    });
                });
            });

            describe("and I enter an invalid number for recurring net", function() {
                it("should ignore the change and retain old value", function() {
                    runs(function() {
                        $(recurringNetTotalFieldId).click();
                        $(recurringNetTotalFormInputId).val("Invalid");
                        $(recurringNetTotalFormInputId).blur();
                    });
                    waitsFor(function() {
                        return $(recurringNetTotalFormInputId).length === 0;
                    }, "Blur didn't happen", 500);
                    runs(function() {
                        expect($(recurringNetTotalFieldId).html()).toEqual("13.50");
                        expect($(recurringDiscountFieldId).html()).toEqual("10.00");
                        expect($('tr').hasClass('changeDiscount')).toBeFalsy();
                    });
                });
            });
        });
    });
});
