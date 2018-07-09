describe("Add product", function () {
    var $ajax = $.ajax;
    var getSiteResponse;
    var launchStatusResponse;
    var cardinalityCheckResponse;
    var siteSelectedForProductCheckResponse;
    var serviceForProductResponse;
    var serviceAttributesResponse;

    beforeEach(function () {
        $("form").submit(function (e) {
            e.preventDefault();
            return false;
        });

        spyAjaxCalls();
        $("#commonError").html('');
        $("#commonError").addClass("hidden");

        $("select.product").val($("select.product option:contains('RSQE X')").val());
    });

    afterEach(function () {
        var sites = $("input[type='checkbox']");
        sites.each(function () {
            this.checked = false;
        });
        $.ajax = $ajax;
        this.selectNewSiteDialog = $("#selectNewSiteDialog");
        this.selectNewSiteDialogInstance = new rsqe.Dialog(this.selectNewSiteDialog, {width:1000, height:"auto"});
        this.selectNewSiteDialogInstance.close();
        $('#siteTable').dataTable().fnDestroy();
    });

    describe("for standard site specific products", function () {
        beforeEach(function () {
            var json = JSON.parse('{"sites":[{"id":"1","isValidForProduct":true,"isValidForSpecialBidProduct":false,"isSpecialBidProduct":false,"site":"SITE 1","fullAddress":"Floor 1, Building 1, Addy 1 Line 1, MARTLESHAM, Zimbabwae, IP1","country":"Zimbabwae"},' +
                                              '{"id":"2","isValidForProduct":true,"isValidForSpecialBidProduct":false,"isSpecialBidProduct":false,"site":"SITE 2","fullAddress":"Floor 1, Building 2, Addy 2 Line 1, MARTLESHAM, France, IP2","country":"France"},' +
                                              '{"id":"3","isValidForProduct":false,"isValidForSpecialBidProduct":true,"isSpecialBidProduct":false,"site":"SITE 3","fullAddress":"Floor 1, Building 3, Addy 3 Line 1, MARTLESHAM, United Kingdom, IP3","country":"United Kingdom"}],' +
                                              '"sEcho":"1","iTotalDisplayRecords":"4","iTotalRecords":"4"}');

            mockGetSiteResponse(json);
            mockGetServiceAttributesResponse('{"names":[]}');
            new rsqe.AddModifyProducts().initialise();
        });

        it('should have correct json string with sites', function () {

            mockGetLaunchStatusResponse('Yes');
            mockCardinalityCheckResponse('{"errors":[], "hasError":false}');
            mockSiteSelectedForProductCheckResponse('{"errors":[], "hasError":false}');

            var action = $("input[type='radio']");
            $(action[0]).click();
            var sites = $("input[type='checkbox']");
            $(sites[1]).click();
            $(sites[3]).click();

            $('.submit').click();
            var json = $('.quoteOptionContext').val();

            expect(json).toContain('"quoteOptionId":"quoteOptionId",');
            expect(json).toContain('"expedioQuoteId":"projectId",');
            expect(json).toContain('"customerId":"54",');
            expect(json).toContain('"contractId":"123",');
            expect(json).toContain('"rsqeQuoteOptionName":"NAME",');
            expect(json).toContain('"currency":"GBP",');
            expect(json).toContain('"revenueOwner":"revenue owner",');
            expect(json).toContain('"lineItems":[');
            expect(json).toContain('{"siteId":"1","action":"Add"},');
            expect(json).toContain('{"siteId":"3","action":"Add"}');
            expect(json).toContain('"isImportable"');
            expect(json).toContain('"moveConfigurationType"');
            expect(json).toContain('"rollOnContractTermForMove"');
        });

        it('should display error message for standard product available in country but special bid pricing type', function(){
            mockGetLaunchStatusResponse('Yes');
            mockCardinalityCheckResponse('{"errors":[], "hasError":false}');
            mockSiteSelectedForProductCheckResponse('{"errors":[], "hasError":false}');

            var action = $("input[type='radio']");
            $(action[0]).click();
            var sites = $("input[type='checkbox']");
            $(sites[1]).click();
            expect($("#commonError").hasClass("hidden")).toBeFalsy();
            expect($("#commonError").html()).toEqual("Unable to support product as standard in United Kingdom, therefore the product has not been added to sites in those"
                                                                                         + " countries.  However the service may be available via special bid.  To progress further in the quote journey"
                                                                                         + " please add a General Special Bid product with the required service configuration for the selected site/s.");
            $('.submit').click();
            var json = $('.quoteOptionContext').val();
        });

        it("should have a correct json string with correct lineItems", function() {
            mockGetLaunchStatusResponse('Yes');
            mockCardinalityCheckResponse('{"errors":[], "hasError":false}');
            mockSiteSelectedForProductCheckResponse('{"errors":[], "hasError":false}');

            var action = $("input[type='radio']");
            $(action[0]).click();
            var sites = $("input[type='checkbox']");
            $(sites[1]).click();
            $(sites[3]).click();

            $('.submit').click();
            var json = $('.quoteOptionContext').val();
            expect(json).toContain('"lineItems":[');
            expect(json).toContain('{"siteId":"1","action":"Add"},');
            expect(json).toContain('{"siteId":"3","action":"Add"}');
            expect(json).not.toContain('{"action":"Add"}');

            var json_nonSitesSpecific = JSON.parse('{"sites":[],"sEcho":"1","iTotalDisplayRecords":"0","iTotalRecords":"0"}');
            mockGetSiteResponse(json_nonSitesSpecific);
            new rsqe.AddModifyProducts().initialise();
            mockGetLaunchStatusResponse("Yes");
            mockCardinalityCheckResponse('{"errors":[], "hasError":false}');
            mockSiteSelectedForProductCheckResponse('{"errors":[], "hasError":false}');

            $("select.product").val($("select.product option:contains('RSQE Y')").val());
            $("select.product").change();
            expect($(".filterPanel").is(":visible")).toBeFalsy();
            expect($("#siteTable_wrapper").is(":visible")).toBeFalsy();

            $($("input[type='radio']")[0]).click();
            $('.submit').click();

            var jsonData = $('.quoteOptionContext').val();
            expect(jsonData).not.toContain('{"siteId":"1","action":"Add"},');
            expect(jsonData).not.toContain('{"siteId":"3","action":"Add"}');
            expect(jsonData).toContain('"lineItems":[{"action":"Add"}]');
        });

        it('should not be able to proceed when no sites are selected for site dependent products', function () {
            expect($('.submit').is(":disabled")).toBeTruthy();

            var action = $("input[type='radio']");
            $(action[0]).click();
            var sites = $("input[type='checkbox']");
            $(sites[1]).click();
            $(sites[3]).click();

            expect($('.submit').is(":disabled")).toBeFalsy();
        });
    });

        describe('when selecting a Product Category Group', function() {
            it('should hide category drop down before category group has been selected', function() {
                expect($("#categoryFilterPanel").is(":visible")).toBeFalsy();
                expect(isEnabled($('#bulkTemplateExport'))).toBeFalsy();
            });

            it('should populate the Categories drop down with the categories contained in the selected category group', function() {
                $("#categoryGroupFilter").val($("#categoryGroupFilter option:contains('A Product Category Group')").val());
                $("#categoryGroupFilter").change();
                expect($("#categoryFilter").is(":visible")).toBeTruthy();
                expect($("#numberOfProductsPanel").is(":visible")).toBeFalsy();
                expect(isEnabled($('#bulkTemplateExport'))).toBeFalsy();

                var options = $("#categoryFilter").find('option');
                expect(options.size()).toBe(3); // including the -- please select --
                expect($(options[1]).text().trim()).toBe('Product Category 1');
                expect($(options[2]).text().trim()).toBe('Product Category 2');
                expect($("#numberOfProductsPanel").is(":visible")).toBeFalsy();
            });
        });

        describe("when selecting a Product Category", function () {
            afterEach(function() {
                resetProductCategoryDropDown();
            });

            var chooseCategoryGroup = function() {
                $("#categoryGroupFilter").val($("#categoryGroupFilter option:contains('A Product Category Group')").val());
                $("#categoryGroupFilter").change();
            };

            it('should hide products drop down before a category has been selected', function () {
                expect($("#productFilterPanel").is(":visible")).toBeFalsy();
                expect($("#numberOfProductsPanel").is(":visible")).toBeFalsy();
                expect(isEnabled($('#bulkTemplateExport'))).toBeFalsy();
            });

            it('should populate the Products drop down with products contained in the selected category', function () {
                chooseCategoryGroup();

                $("#categoryFilter").val($("#categoryFilter option:contains('Product Category 2')").val());
                $("#categoryFilter").change();
                expect($("#productFilterPanel").is(":visible")).toBeTruthy();
                expect(isEnabled($('#bulkTemplateExport'))).toBeFalsy();

                var options = $("select.product").find('option');
                expect(options.size()).toBe(2);
                expect(options.last().text().trim()).toBe("RSQE A");
                expect(isEnabled($('#bulkTemplateExport'))).toBeFalsy();
            });

            it('should reload sites in case selected countries are changed', function() {
                chooseCategoryGroup();
                $("#countryFilter").append(new Option("France", "France"));
                $("#countryFilter").append(new Option("India", "India"));
                $("#countryFilter").append(new Option("United Kingdom", "United Kingdom"));
                $("#countryFilter").append(new Option("all", "All Countries"));
                expect($('#countryFilter').val()).toEqual(null);

                $("#categoryFilter").val($("#categoryFilter option:contains('Product Category 2')").val());
                $("#categoryFilter").change();

                $("select.product").val($("select.product option:contains('RSQE Y')").val());
                $("select.product").change();

                $('#countryFilter').val($("#countryFilter option:contains('France')").val());
                $('#countryFilter').change();
                expect($('#countryFilter').val()).toEqual(['France']);

                $('#countryFilter').val($("#countryFilter option:contains('all')").val());
                $('#countryFilter').change();
                expect($('#countryFilter').val()).toEqual(['All Countries']);
                expect($('#countryFilter').val()).not.toContain(['France']);


                var sites = $('input:checkbox:not(#selectAll):not(#complianceCheckBox):not(#showHiddenSites):not(#hidden)');
                expect(sites.length).toEqual(2);

                $('#countryFilter').find('option[value="France"]').remove();
                $('#countryFilter').find('option[value="India"]').remove();
                $('#countryFilter').find('option[value="United Kingdom"]').remove();
                $('#countryFilter').find('option[value="all"]').remove();
            });

            it('should reset the country filter if product category is changed', function() {
                chooseCategoryGroup();

                $("#countryFilter").append(new Option("First", "First"));
                $("#countryFilter").append(new Option("Second", "Second"));
                expect($('#countryFilter').val()).toEqual(null);

                $("#categoryFilter").val($("#categoryFilter option:contains('Product Category 2')").val());
                $("#categoryFilter").change();

                $("select.product").val($("select.product option:contains('RSQE Y')").val());
                $("select.product").change();

                $('#countryFilter').val($("#countryFilter option:contains('First')").val());
                $('#countryFilter').change();
                expect($('#countryFilter').val()).toEqual(['First']);
                $("#categoryFilter").val($("#categoryFilter option:contains('Product Category 1')").val());
                $("#categoryFilter").change();

                expect($('#countryFilter').val()).toEqual(null);

                $('#countryFilter').find('option[value="First"]').remove();
                $('#countryFilter').find('option[value="Second"]').remove();
                $("#siteTable_wrapper").hide();
            });

            it('should display help link for category with orderPreRequisiteUrl', function () {
                chooseCategoryGroup();

                $("#categoryFilter").val($("#categoryFilter option:contains('Product Category 1')").val());
                $("#categoryFilter").change();
                expect($("#productFilterPanel").is(":visible")).toBeTruthy();

                expect($('#helpLink').is(":visible")).toBeTruthy();
                expect($('#helpLink')[0].getAttribute('href')).toEqual("orderPreRequisiteUrl1");
            });

            it('should hide help link for category without orderPreRequisiteUrl', function () {
                chooseCategoryGroup();

                $("#categoryFilter").val($("#categoryFilter option:contains('Product Category 2')").val());
                $("#categoryFilter").change();
                expect($("#productFilterPanel").is(":visible")).toBeTruthy();

                expect($('#helpLink').is(":visible")).toBeFalsy();
            });

            function resetProductCategoryDropDown() {
                $("#categoryFilter").val($("#categoryFilter option:contains('Product Category 1')").val());
                $("#categoryFilter").change();
            }
        });

    describe("for special bid site specific products", function () {
        beforeEach(function () {

        });
        it('should not throw special bid error when product is general special bid', function(){
            var json_specialBid = JSON.parse('{"sites":[{"id":"4","isValidForProduct":false,"isValidForSpecialBidProduct":true,"isSpecialBidProduct":true,"site":"SITE 4","fullAddress":"Floor 1, Building 3, Addy 3 Line 1, MARTLESHAM, United Kingdom, IP3"}],' +
                                              '"sEcho":"1","iTotalDisplayRecords":"5","iTotalRecords":"5"}');

            mockGetSiteResponse(json_specialBid);
            new rsqe.AddModifyProducts().initialise();
            mockGetLaunchStatusResponse('Yes');
            mockCardinalityCheckResponse('{"errors":[], "hasError":false}');
            mockSiteSelectedForProductCheckResponse('{"errors":[], "hasError":false}');
            mockGetServiceAttributesResponse('{"names":[]}');

            var action = $("input[type='radio']");
            $(action[0]).click();
            var sites = $("input[type='checkbox']");
            $(sites[1]).click();
            expect($("#commonError").hasClass("hidden")).toBeTruthy();

        });

        it('should list unavailable error countries in correct format and each country only once', function(){
            var json = JSON.parse('{"sites":[{"id":"5","isValidForProduct":false,"isValidForSpecialBidProduct":false,"isSpecialBidProduct":false,"site":"SITE 5","fullAddress":"Floor 1, Building 2, Addy 2 Line 1, MARTLESHAM, Zimbabwae, IP2","country":"Zimbabwae"},' +
                                            '{"id":"8","isValidForProduct":false,"isValidForSpecialBidProduct":false,"isSpecialBidProduct":false,"site":"SITE 8","fullAddress":"Floor 1, Building 3, Addy 3 Line 1, MARTLESHAM, United Kingdom, IP3","country":"France"},' +
                                            '{"id":"9","isValidForProduct":false,"isValidForSpecialBidProduct":false,"isSpecialBidProduct":false,"site":"SITE 9","fullAddress":"Floor 1, Building 3, Addy 3 Line 1, MARTLESHAM, United Kingdom, IP3","country":"Germany"},' +
                                            '{"id":"6","isValidForProduct":false,"isValidForSpecialBidProduct":false,"isSpecialBidProduct":false,"site":"SITE 6","fullAddress":"Floor 1, Building 2, Addy 2 Line 1, MARTLESHAM, Zimbabwae, IP2","country":"Zimbabwae"}],' +
                                            '"sEcho":"1","iTotalDisplayRecords":"5","iTotalRecords":"5"}');

            mockGetSiteResponse(json);
            new rsqe.AddModifyProducts().initialise();
            mockGetLaunchStatusResponse('Yes');
            mockCardinalityCheckResponse('{"errors":[], "hasError":false}');
            mockSiteSelectedForProductCheckResponse('{"errors":[], "hasError":false}');
            mockGetServiceAttributesResponse('{"names":[]}');

            var action = $("input[type='radio']");
            $(action[0]).click();
            var sites = $("input[type='checkbox']");
            $(sites[1]).click();
            expect($("#commonError").hasClass("hidden")).toBeFalsy();
            expect($("#commonError").html()).toEqual("Unable to supply product to Zimbabwae, France and Germany");
        });

        it('should list special bid type countries in correct format and each country only once', function(){
            var json = JSON.parse('{"sites":[{"id":"7","isValidForProduct":false,"isValidForSpecialBidProduct":true,"isSpecialBidProduct":false,"site":"SITE 7","fullAddress":"Floor 1, Building 3, Addy 3 Line 1, MARTLESHAM, United Kingdom, IP3","country":"United Kingdom"},' +
                                            '{"id":"8","isValidForProduct":false,"isValidForSpecialBidProduct":true,"isSpecialBidProduct":false,"site":"SITE 8","fullAddress":"Floor 1, Building 3, Addy 3 Line 1, MARTLESHAM, United Kingdom, IP3","country":"France"},' +
                                            '{"id":"9","isValidForProduct":false,"isValidForSpecialBidProduct":true,"isSpecialBidProduct":false,"site":"SITE 9","fullAddress":"Floor 1, Building 3, Addy 3 Line 1, MARTLESHAM, United Kingdom, IP3","country":"Germany"},' +
                                            '{"id":"10","isValidForProduct":false,"isValidForSpecialBidProduct":true,"isSpecialBidProduct":false,"site":"SITE 10","fullAddress":"Floor 1, Building 3, Addy 3 Line 1, MARTLESHAM, United Kingdom, IP3","country":"France"}],' +
                                            '"sEcho":"1","iTotalDisplayRecords":"5","iTotalRecords":"5"}');

            mockGetSiteResponse(json);
            new rsqe.AddModifyProducts().initialise();
            mockGetLaunchStatusResponse('Yes');
            mockCardinalityCheckResponse('{"errors":[], "hasError":false}');
            mockSiteSelectedForProductCheckResponse('{"errors":[], "hasError":false}');
            mockGetServiceAttributesResponse('{"names":[]}');

            var action = $("input[type='radio']");
            $(action[0]).click();
            var sites = $("input[type='checkbox']");
            $(sites[1]).click();
            expect($("#commonError").hasClass("hidden")).toBeFalsy();
            expect($("#commonError").html()).toEqual("Unable to support product as standard in United Kingdom, France and Germany, therefore the product has not been added to sites in those"
                                                           + " countries.  However the service may be available via special bid.  To progress further in the quote journey"
                                                           + " please add a General Special Bid product with the required service configuration for the selected site/s.");
        })
    });

    describe("for site agnostic products", function () {

        beforeEach(function () {
            var json = JSON.parse('{"sites":[],"sEcho":"1","iTotalDisplayRecords":"0","iTotalRecords":"0"}');
            mockGetSiteResponse(json);
            mockGetServiceAttributesResponse('{"names":[]}');
            new rsqe.AddModifyProducts().initialise();
        });

        it('should have correct json string with no siteIds but only actions', function () {
            mockGetLaunchStatusResponse("Yes");
            mockCardinalityCheckResponse(('{"errors":[], "hasError":false}'));
            mockSiteSelectedForProductCheckResponse(('{"errors":[], "hasError":false}'));

            $("select.product").val($("select.product option:contains('RSQE Y')").val());
            $("select.product").change();
            expect($(".filterPanel").is(":visible")).toBeFalsy();
            expect($("#siteTable_wrapper").is(":visible")).toBeFalsy();

            $($("input[type='radio']")[0]).click();
            $('.submit').click();

            var json = $('.quoteOptionContext').val();
            expect(json).toContain('"quoteOptionId":"quoteOptionId",');
            expect(json).toContain('"expedioQuoteId":"projectId",');
            expect(json).toContain('"customerId":"54",');
            expect(json).toContain('"contractId":"123",');
            expect(json).toContain('"currency":"GBP",');
            expect(json).toContain('"rsqeQuoteOptionName":"NAME",');
            expect(json).toContain('"lineItems":[{"action":"Add"}]');

            expect($("#commonError").hasClass("hidden")).toBeTruthy();
        });

        it('should display error if sales channel not launched', function() {
            mockGetLaunchStatusResponse('No');
            mockCardinalityCheckResponse(('{"errors":[], "hasError":false}'));
            mockSiteSelectedForProductCheckResponse(('{"errors":[], "hasError":false}'));

            $("select.product").val($("select.product option:contains('RSQE Y')").val());
            $("select.product").change();

            $($("input[type='radio']")[0]).click();
            $('.submit').click();

            expect($("#commonError").hasClass("hidden")).toBeFalsy();
            expect($("#commonError").html()).toEqual("Service is not launched for the channel revenue owner.");
        });

        it('should display error if cardinality check failed', function() {
            mockGetLaunchStatusResponse('Yes');
            mockCardinalityCheckResponse(('{"errors":["Contract Cardinality Error"],"hasError":true}'));
            mockSiteSelectedForProductCheckResponse(('{"errors":[], "hasError":false}'));

            $("select.product").val($("select.product option:contains('RSQE Y')").val());
            $("select.product").change();

            $($("input[type='radio']")[0]).click();
            $('.submit').click();

            expect($("#commonError").hasClass("hidden")).toBeFalsy();
            expect($("#commonError").html()).toEqual("Contract Cardinality Error.<br>");
        });

        it('should display error dialog if no pricebook is available', function() {
            mockGetLaunchStatusResponse('Yes');
            mockCardinalityCheckResponse(('{"errors":["No PriceBooks found for Connect Acceleration"], "hasError":true}'));
            $("select.product").val($("select.product option:contains('RSQE Y')").val());

            $("select.product").change();
            $($("input[type='radio']")[0]).click();

            $('.submit').click();

            expect($("#commonError").hasClass("hidden")).toBeFalsy();
            expect($("#commonError").html()).toEqual("No PriceBooks found for Connect Acceleration.<br>");

            mockCardinalityCheckResponse(('{"errors":[], "hasError":false}'));

            $('.submit').click();

            expect($("#commonError").hasClass("hidden")).toBeTruthy();
        });

        it('should have correct json string with no siteIds but only actions and multiple services', function () {
            mockGetLaunchStatusResponse("Yes");
            mockCardinalityCheckResponse(('{"errors":[], "hasError":false}'));
            mockSiteSelectedForProductCheckResponse(('{"errors":[], "hasError":false}'));

            $("select.product").val($("select.product option:contains('RSQE Y')").val());
            $("select.product").change();
            expect($(".filterPanel").is(":visible")).toBeFalsy();
            expect($("#siteTable_wrapper").is(":visible")).toBeFalsy();
            $('#numberOfProducts').val('2');
            $($("input[type='radio']")[0]).click();
            $('.submit').click();

            var json = $('.quoteOptionContext').val();
            expect(json).toContain('"quoteOptionId":"quoteOptionId",');
            expect(json).toContain('"expedioQuoteId":"projectId",');
            expect(json).toContain('"customerId":"54",');
            expect(json).toContain('"contractId":"123",');
            expect(json).toContain('"currency":"GBP",');
            expect(json).toContain('"rsqeQuoteOptionName":"NAME",');
            expect(json).toContain('"lineItems":[{"action":"Add"},{"action":"Add"}]');

            expect($("#commonError").hasClass("hidden")).toBeTruthy();
        });
    });

    describe('For any site based product', function() {

        beforeEach(function () {
            var json = JSON.parse('{"sites":[],"sEcho":"1","iTotalDisplayRecords":"0","iTotalRecords":"0"}');
            mockGetSiteResponse(json);
            mockGetServiceAttributesResponse('{"names":[]}');
            new rsqe.AddModifyProducts().initialise();
        });

        it('should enable \"Configure Product\" button based on products on the quote', function() {
            expect(isEnabled($('#bulkConfigurationButton'))).toBeTruthy();
            rsqe.AddModifyProducts.prototype.createProductSuccessCallback("0");
            expect(isEnabled($('#bulkConfigurationButton'))).toBeFalsy();
            rsqe.AddModifyProducts.prototype.createProductSuccessCallback("1");
            expect(isEnabled($('#bulkConfigurationButton'))).toBeTruthy();
        });

    });

    function isEnabled(element) {
        return element.attr('class').indexOf('disabled') === -1;
    }

    function spyAjaxCalls() {

        var GET_SITE_REQUEST = 1;
        var GET_LAUNCH_STATUS = 2;
        var CARDINALITY_CHECK = 3;
        var SITE_SELECTED_FOR_PRODUCT_CHECK = 4;
        var SERVICES_FOR_PRODUCT = 5;
        var SERVICE_ATTRIBUTES = 6;

        var attachSpyBehaviour = function(options) {
            options.requestType = function() {

                if(options.url.indexOf("sites") >= 0) {
                    return GET_SITE_REQUEST;
                }
                if(options.url.indexOf("getLaunched") >= 0) {
                    return GET_LAUNCH_STATUS;
                }
                if(options.url.indexOf("cardinalityCheck") >= 0) {
                    return CARDINALITY_CHECK;
                }
                if(options.url.indexOf("siteSelectedForProductCheck") >= 0) {
                    return SITE_SELECTED_FOR_PRODUCT_CHECK;
                }
                if(options.url.indexOf('services') >= 0) {
                    return SERVICES_FOR_PRODUCT;
                }
                if(options.url.indexOf('service-attributes') >= 0) {
                    return SERVICE_ATTRIBUTES;
                }
            };
        };

        $.ajax = function (options) {
            var response;
            attachSpyBehaviour(options);


            switch( options.requestType() ) {
                case GET_SITE_REQUEST:
                    response = getSiteResponse;
                    break;
                case GET_LAUNCH_STATUS:
                    response =  launchStatusResponse;
                    break;
                case CARDINALITY_CHECK:
                    response =  cardinalityCheckResponse;
                    break;
                case SITE_SELECTED_FOR_PRODUCT_CHECK:
                    response =  siteSelectedForProductCheckResponse;
                    break;
                case SERVICES_FOR_PRODUCT:
                    response = serviceForProductResponse;
                    break;
                case SERVICE_ATTRIBUTES:
                    response = serviceAttributesResponse;
            }

            if(options.success) {
                options.success(response);
            }
            return response;
        };

        $.when = function(resp1, resp2, resp3) {
            return {
                then : function(successCallback, failureCallback) {
                    successCallback([resp1], [resp2], [resp3]);
                }
            };
        };
    }

    function mockGetSiteResponse(data) {
        getSiteResponse = data;
    }

    function mockGetLaunchStatusResponse(data) {
        launchStatusResponse = data;
    }

    function mockCardinalityCheckResponse(data) {
        cardinalityCheckResponse = data;
    }

    function mockSiteSelectedForProductCheckResponse(data) {
        siteSelectedForProductCheckResponse = data;
    }

    function mockGetServicesForProductResponse(data) {
        serviceForProductResponse = data;
    }

    function mockGetServiceAttributesResponse(data) {
        serviceAttributesResponse = data;
    }
});
