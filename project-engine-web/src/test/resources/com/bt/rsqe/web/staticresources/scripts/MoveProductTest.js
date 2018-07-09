describe("Add product", function () {
    var $ajax = $.ajax;
    var getSiteResponse;
    var launchStatusResponse;
    var cardinalityCheckResponse;
    var siteSelectedForProductCheckResponse;
    var endOfLifeCheckResponse;
    var selectNewSiteForm;
    var json;
    var selectNewSiteDialog;
    var tab;
    var endOfLifeError = "End of Life Error";
    var endOfLifeWarning = "End of Life Warning";

    beforeEach(function () {
        $("form").submit(function (e) {
            e.preventDefault();
            return false;
        });

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
        $("#warningMessages").addClass("hidden");
        $('#siteTable').dataTable().fnDestroy();
    });

    describe("for move product", function () {

        it('should click Select New Site button on Move Product tab and populate site table', function () {
            spyAjaxCalls();
            json = JSON.parse('{"sites":[{"id":"1","isValidForProduct":true,"site":"SITE 1","fullAddress":"Floor 1, Building 1, Addy 1 Line 1, MARTLESHAM, France, IP1","newSiteId":"newSiteId","newSite":"","newFullAddress":""}],' +
                              '"sEcho":"1","iTotalDisplayRecords":"4","iTotalRecords":"4"}');

            mockGetSiteResponse(json);
            new rsqe.AddModifyProducts().initialise();

            mockGetLaunchStatusResponse("Yes");
            mockCardinalityCheckResponse(('{"errors":[], "hasError":false}'));
            mockSiteSelectedForProductCheckResponse(('{"errors":[], "hasError":false}'));
            mockSelectNewSiteForm(selectNewSiteForm);

            $("input[type='checkbox']")[2].click();
            expect($("#selectNewSiteButton")[0].disabled).toBeFalsy();
            $("#selectNewSiteButton").click();
            expect($("#selectNewSiteDialog")[0]).toBeDefined();
        });


        it('should load new site dialog and select new site', function () {
            spyAjaxCalls();
            json = JSON.parse('{"sites":[{"id":"1","isValidForProduct":true,"site":"SITE 1","fullAddress":"Floor 1, Building 1, Addy 1 Line 1, MARTLESHAM, France, IP1","newSiteId":"newSiteId","newSite":"","newFullAddress":""}],' +
                              '"sEcho":"1","iTotalDisplayRecords":"4","iTotalRecords":"4"}');

            mockGetSiteResponse(json);
            new rsqe.AddModifyProducts().initialise();
            mockGetLaunchStatusResponse("Yes");
            mockCardinalityCheckResponse(('{"errors":[], "hasError":false}'));
            mockSiteSelectedForProductCheckResponse(('{"errors":[], "hasError":false}'));
            mockSelectNewSiteForm(selectNewSiteForm);

            $("input[type='checkbox']")[2].click();
            expect($("#selectNewSiteButton")[0].disabled).toBeFalsy();
            $("#selectNewSiteButton").click();
            expect($("#selectNewSiteDialog")[0]).toBeDefined();
        });

        it('should disable select new site button when new site is selected', function () {
            spyAjaxCalls();
            json = JSON.parse('{"sites":[{"id":"1","isValidForProduct":true,"site":"SITE 1","fullAddress":"Floor 1, Building 1, Addy 1 Line 1, MARTLESHAM, France, IP1","newSiteId":"newSiteId","newSite":"newSite","newFullAddress":"newFullAddress"}],' +
                              '"sEcho":"1","iTotalDisplayRecords":"4","iTotalRecords":"4"}');

            mockGetSiteResponse(json);
            new rsqe.AddModifyProducts().initialise();

            mockGetLaunchStatusResponse("Yes");
            mockCardinalityCheckResponse(('{"errors":[], "hasError":false}'));
            mockSiteSelectedForProductCheckResponse(('{"errors":[], "hasError":false}'));
            mockSelectNewSiteForm(selectNewSiteForm);

            $("input[type='checkbox']")[2].click();
            expect($("#selectNewSiteButton")[0].disabled).toBeTruthy();
            expect($("#submit")[0].disabled).toBeFalsy();
            expect($("#selectNewSiteDialog")[0]).toBeDefined();
        });

        it('should submit the new site id when continuing to quote level configuration', function () {
            spyAjaxCalls();
            json = JSON.parse('{"sites":[{"id":"1","isValidForProduct":true,"site":"SITE 1","fullAddress":"Floor 1, Building 1, Addy 1 Line 1, MARTLESHAM, France, IP1","newSiteId":"newSiteId","newSite":"newSite","newFullAddress":"newFullAddress"}],' +
                              '"sEcho":"1","iTotalDisplayRecords":"4","iTotalRecords":"4"}');

            mockGetSiteResponse(json);
            new rsqe.AddModifyProducts().initialise();

            mockGetLaunchStatusResponse("Yes");
            mockCardinalityCheckResponse(JSON.stringify({"errors":[], "hasError":false}));
            mockEndOfLifeCheckResponse(JSON.stringify({"errors":[], "hasError":false, "warnings":[], "hasWarning":false}));
            mockSiteSelectedForProductCheckResponse(JSON.stringify({"errors":[], "hasError":false}));
            mockSelectNewSiteForm(selectNewSiteForm);

            $("input[type='checkbox']")[2].click();
            expect($("#selectNewSiteButton")[0].disabled).toBeTruthy();
            expect($("#submit")[0].disabled).toBeFalsy();
            expect($("#selectNewSiteDialog")[0]).toBeDefined();

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
            expect(json).toContain(',"newSiteId":"newSiteId"');
        });

        it('should select all sites using select all checkbox', function () {
            spyAjaxCalls();
            json = JSON.parse('{"sites":[{"id":"1","isValidForProduct":true,"site":"SITE 1","fullAddress":"Floor 1, Building 1, Addy 1 Line 1, MARTLESHAM, France, IP1","newSiteId":"newSiteId","newSite":"","newFullAddress":""}],' +
                              '"sEcho":"1","iTotalDisplayRecords":"4","iTotalRecords":"4"}');

            mockGetSiteResponse(json);
            new rsqe.AddModifyProducts().initialise();

            mockGetLaunchStatusResponse("Yes");
            mockCardinalityCheckResponse(('{"errors":[], "hasError":false}'));
            mockSiteSelectedForProductCheckResponse(('{"errors":[], "hasError":false}'));
            mockSelectNewSiteForm(selectNewSiteForm);

            expect($("#selectNewSiteButton")[0].disabled).toBeTruthy();
            $("input[type='checkbox']")[1].click();
            expect($("#selectNewSiteButton")[0].disabled).toBeFalsy();
            $("#selectNewSiteButton").click();

            // ensure select all check box is still binded to single row check boxes once new site dialog is closed
            this.selectNewSiteDialog = $("#selectNewSiteDialog");
            this.selectNewSiteDialogInstance = new rsqe.Dialog(this.selectNewSiteDialog, {width:1000, height:"auto"});
            this.selectNewSiteDialogInstance.close();
            expect($("#selectNewSiteButton")[0].disabled).toBeFalsy();
            $("input[type='checkbox']")[1].click();
            expect($("#selectNewSiteButton")[0].disabled).toBeTruthy();
            expect($("input[type='checkbox']")[2].checked).toBeFalsy();
        });

        it('should receive end of life error when attempting to move product', function () {
            spyAjaxCalls();
            json = JSON.parse('{"sites":[{"id":"1","isValidForProduct":true,"site":"SITE 1","fullAddress":"Floor 1, Building 1, Addy 1 Line 1, MARTLESHAM, France, IP1","newSiteId":"newSiteId","newSite":"newSite","newFullAddress":"newFullAddress"}],' +
                              '"sEcho":"1","iTotalDisplayRecords":"4","iTotalRecords":"4"}');

            mockGetSiteResponse(json);
            new rsqe.AddModifyProducts().initialise();

            mockGetLaunchStatusResponse("Yes");
            mockCardinalityCheckResponse(JSON.stringify({"errors":[], "hasError":false}));
            mockSiteSelectedForProductCheckResponse(JSON.stringify({"errors":[], "hasError":false}));
            mockEndOfLifeCheckResponse(JSON.stringify({"errors":['End of Life Error'], "hasError":true, "warnings":[], "hasWarning":false}));
            mockSelectNewSiteForm(selectNewSiteForm);

            $("input[type='checkbox']")[2].click();
            expect($("#selectNewSiteButton")[0].disabled).toBeTruthy();
            expect($("#submit")[0].disabled).toBeFalsy();
            expect($("#selectNewSiteDialog")[0]).toBeDefined();

            $('.submit').click();
            var json = $('.quoteOptionContext').val();

            var errorMessage = $('#commonError')[0];
            expect(errorMessage.innerHTML).toContain(endOfLifeError);
        });

        it('should receive end of life warning when attempting to move product and then continue with move', function () {
            spyAjaxCalls();
            json = JSON.parse('{"sites":[{"id":"1","isValidForProduct":true,"site":"SITE 1","fullAddress":"Floor 1, Building 1, Addy 1 Line 1, MARTLESHAM, France, IP1","newSiteId":"newSiteId","newSite":"newSite","newFullAddress":"newFullAddress"}],' +
                              '"sEcho":"1","iTotalDisplayRecords":"4","iTotalRecords":"4"}');

            mockGetSiteResponse(json);
            new rsqe.AddModifyProducts().initialise();

            mockGetLaunchStatusResponse("Yes");
            mockCardinalityCheckResponse(JSON.stringify({"errors":[], "hasError":false}));
            mockSiteSelectedForProductCheckResponse(JSON.stringify({"errors":[], "hasError":false}));
            mockEndOfLifeCheckResponse(JSON.stringify({"errors":[], "hasError":false, "warnings":['End of Life Warning'], "hasWarning":true}));
            mockSelectNewSiteForm(selectNewSiteForm);

            $("input[type='checkbox']")[2].click();
            expect($("#selectNewSiteButton")[0].disabled).toBeTruthy();
            expect($("#submit")[0].disabled).toBeFalsy();
            expect($("#selectNewSiteDialog")[0]).toBeDefined();

            expect($("#warningMessages").is(".hidden")).toEqual(true);
            var progressSpinner = $('#creating-product-spinner');
            $('.submit').click();
            var json = $('.quoteOptionContext').val();

            var progressDialog = $('.progressDialog');
            expect(progressDialog.is(":visible")).toEqual(true);

            var warningMessage = $('#warningMessages')[0];
            expect(warningMessage.innerHTML).toContain(endOfLifeWarning);
            expect($("#warningMessages").is(".hidden")).toEqual(false);

            $('#continueMove').click();
            expect(progressSpinner.is(":visible")).toEqual(true);
            expect(progressDialog.is(":visible")).toEqual(false);
        });

        it('should receive end of life warning when attempting to move product and then cancel the move', function () {
            spyAjaxCalls();
            json = JSON.parse('{"sites":[{"id":"1","isValidForProduct":true,"site":"SITE 1","fullAddress":"Floor 1, Building 1, Addy 1 Line 1, MARTLESHAM, France, IP1","newSiteId":"newSiteId","newSite":"newSite","newFullAddress":"newFullAddress"}],' +
                              '"sEcho":"1","iTotalDisplayRecords":"4","iTotalRecords":"4"}');

            mockGetSiteResponse(json);
            new rsqe.AddModifyProducts().initialise();

            mockGetLaunchStatusResponse("Yes");
            mockCardinalityCheckResponse(JSON.stringify({"errors":[], "hasError":false}));
            mockSiteSelectedForProductCheckResponse(JSON.stringify({"errors":[], "hasError":false}));
            mockEndOfLifeCheckResponse(JSON.stringify({"errors":[], "hasError":false, "warnings":['End of Life Warning'], "hasWarning":true}));
            mockSelectNewSiteForm(selectNewSiteForm);

            $("input[type='checkbox']")[2].click();
            expect($("#selectNewSiteButton")[0].disabled).toBeTruthy();
            expect($("#submit")[0].disabled).toBeFalsy();
            expect($("#selectNewSiteDialog")[0]).toBeDefined();

            expect($("#warningMessages").is(".hidden")).toEqual(true);
            $('.submit').click();
            var json = $('.quoteOptionContext').val();

            var progressDialog = $('.progressDialog');
            var progressSpinner = $('#creating-product-spinner');
            expect(progressDialog.is(":visible")).toEqual(true);
            expect(progressSpinner.is(":visible")).toEqual(false);

            var warningMessage = $('#warningMessages')[0];
            expect(warningMessage.innerHTML).toContain(endOfLifeWarning);
            expect($("#warningMessages").is(".hidden")).toEqual(false);

            $('#cancelMove').click();
            expect(progressDialog.is(":visible")).toEqual(false);
            expect(progressSpinner.is(":visible")).toEqual(false);
        });
    });

    function spyAjaxCalls() {

        var GET_SITE_REQUEST = 1;
        var GET_LAUNCH_STATUS = 2;
        var CARDINALITY_CHECK = 3;
        var SITE_SELECTED_FOR_PRODUCT_CHECK = 4;
        var SELECT_NEW_SITE_FORM = 5;
        var END_OF_LIFE_CHECK = 6;

        var attachSpyBehaviour = function(options) {
            options.requestType = function() {

                if( options.url.indexOf("sites") >= 0 ) {
                    return GET_SITE_REQUEST;
                }
                if( options.url.indexOf("getLaunched") >= 0 ) {
                    return GET_LAUNCH_STATUS;
                }
                if( options.url.indexOf("cardinalityCheck") >= 0 ) {
                    return CARDINALITY_CHECK;
                }
                if( options.url.indexOf("siteSelectedForProductCheck") >= 0 ) {
                    return SITE_SELECTED_FOR_PRODUCT_CHECK;
                }
                if( options.url.indexOf("selectNewSiteForm") >= 0 ) {
                    return SELECT_NEW_SITE_FORM;
                }
                if( options.url.indexOf("endOfLifeValidation") >= 0 ) {
                    return END_OF_LIFE_CHECK;
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
                case SELECT_NEW_SITE_FORM:
                    response =  selectNewSiteForm;
                    break;
                case END_OF_LIFE_CHECK:
                    response =  endOfLifeCheckResponse;
                    break;
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

    function mockEndOfLifeCheckResponse(data) {
        endOfLifeCheckResponse = data;
    }

    function mockSiteSelectedForProductCheckResponse(data) {
        siteSelectedForProductCheckResponse = data;
    }

    function mockSelectNewSiteForm(data) {
        selectNewSiteForm = data;
    }
});
