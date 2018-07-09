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

    describe("for special bid site specific products", function () {
        beforeEach(function () {
            $("#productAction").val('Modify');
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

    describe('For any site based product', function() {

        beforeEach(function () {
            var json = JSON.parse('{"sites":[],"sEcho":"1","iTotalDisplayRecords":"0","iTotalRecords":"0"}');
            mockGetSiteResponse(json);
            mockGetServiceAttributesResponse('{"names":[]}');
            $("#productAction").val('Modify');
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

    describe('For Modify Service Products', function() {

        var selectOldVal;


        beforeEach(function() {
            var serviceJson = JSON.parse('{"iTotalDisplayRecords":0,"iTotalRecords":0,"sEcho":1,"services":[]}');
            var siteJson = JSON.parse('{"sites":[{"id":"1","isValidForProduct":true,"isValidForSpecialBidProduct":false,"isSpecialBidProduct":false,"site":"SITE 1","fullAddress":"Floor 1, Building 1, Addy 1 Line 1, MARTLESHAM, Zimbabwae, IP1","country":"Zimbabwae","summary":"summary1"},' +
                                              '{"id":"2","isValidForProduct":true,"isValidForSpecialBidProduct":false,"isSpecialBidProduct":false,"site":"SITE 2","fullAddress":"Floor 1, Building 2, Addy 2 Line 1, MARTLESHAM, France, IP2","country":"France","summary":"summary2"},' +
                                              '{"id":"3","isValidForProduct":false,"isValidForSpecialBidProduct":true,"isSpecialBidProduct":false,"site":"SITE 3","fullAddress":"Floor 1, Building 3, Addy 3 Line 1, MARTLESHAM, United Kingdom, IP3","country":"United Kingdom","summary":"summary3"}],' +
                                              '"sEcho":"1","iTotalDisplayRecords":"4","iTotalRecords":"4"}');

            mockGetSiteResponse(siteJson);
            mockGetServicesForProductResponse(serviceJson);
            mockGetLaunchStatusResponse("Yes");
            mockCardinalityCheckResponse(('{"errors":[], "hasError":false}'));
            mockSiteSelectedForProductCheckResponse(('{"errors":[], "hasError":false}'));
            mockGetServiceAttributesResponse('{"names":[]}');
            $("#productAction").val('Modify');
            new rsqe.AddModifyProducts().initialise();
            $('#numberOfProducts').hide();
        });

        afterEach(function() {
            $('.siteHeader').remove();
            $('.serviceHeader').remove();
            $('thead:eq(0) tr').append('<th class="siteHeader">Site Name</th>');
            $('thead:eq(0) tr').append('<th class="siteHeader">Site Address</th>');
            $('thead:eq(0) tr').append('<th class="siteHeader">Summary</th>');
        });

        it('should display table when in-service products found for service', function() {
            var json = JSON.parse('{"iTotalDisplayRecords":2,"iTotalRecords":2,"sEcho":1,"' +
                                  'services":[{"attributes":{},"id":"4c7cb3cd-8edf-49df-afe0-c65a3ed5e3e2","name":"Cascade Pilot"},' +
                                  '{"attributes":{},"id":"2a0d1ef9-3196-43bb-b65a-993ec015174e","name":"Cascade Pilot"}]}');
            mockGetServicesForProductResponse(json);
            changeProductDropDown($("select.product option:contains('RSQE Y')").val());
            expect($(".choice").length).toEqual(2);
            expect($(".choice:eq(0)").val()).toEqual('4c7cb3cd-8edf-49df-afe0-c65a3ed5e3e2');
            expect($(".choice:eq(1)").val()).toEqual('2a0d1ef9-3196-43bb-b65a-993ec015174e');
            expect($('.submit').is(":disabled")).toBeTruthy();
        });

        it('should display table with item selected when one in-service products found for service', function() {
            var json = JSON.parse('{"iTotalDisplayRecords":2,"iTotalRecords":2,"sEcho":1,"' +
                                  'services":[{"attributes":{},"id":"4c7cb3cd-8edf-49df-afe0-c65a3ed5e3e2","name":"Cascade Pilot"}]}');
            mockGetServicesForProductResponse(json);
            changeProductDropDown($("select.product option:contains('RSQE Y')").val());
            expect($(".choice").length).toEqual(1);
            expect($(".choice:eq(0)").val()).toEqual('4c7cb3cd-8edf-49df-afe0-c65a3ed5e3e2');
            expect($('input[name="Ids"]').attr('checked')).toEqual('checked');
            expect($('.submit').is(":disabled")).toBeFalsy();
        });

        it('should send correct json when adding modifying service products', function() {
            var json = JSON.parse('{"iTotalDisplayRecords":2,"iTotalRecords":2,"sEcho":1,"' +
                                  'services":[{"attributes":{},"id":"4c7cb3cd-8edf-49df-afe0-c65a3ed5e3e2","name":"Cascade Pilot"},' +
                                  '{"attributes":{},"id":"2a0d1ef9-3196-43bb-b65a-993ec015174e","name":"Cascade Pilot"}]}');
            mockGetServicesForProductResponse(json);
            changeProductDropDown($("select.product option:contains('RSQE Y')").val());
            expect($(".choice").length).toEqual(2);
            expect($(".choice:eq(0)").val()).toEqual('4c7cb3cd-8edf-49df-afe0-c65a3ed5e3e2');
            expect($(".choice:eq(1)").val()).toEqual('2a0d1ef9-3196-43bb-b65a-993ec015174e');
            $(".choice:eq(0)").click();
            $('.submit').click();
            json = $('.quoteOptionContext').val();
            expect(json).toContain('"lineItems":[');
            expect(json).toContain('{"lineItemId":"4c7cb3cd-8edf-49df-afe0-c65a3ed5e3e2","action":"Modify"}');
        });

        function changeProductDropDown(value) {
            selectOldVal = $("select.product").val();
            $("select.product").val(value);
            $("select.product").change();
        }

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