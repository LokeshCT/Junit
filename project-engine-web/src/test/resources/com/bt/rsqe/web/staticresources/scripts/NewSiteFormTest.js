var selectNewSiteForm;
describe('New Site Dialog', function() {
    //var errorFn,successFn;
    //var $ajax = $.ajax;
    var getSiteResponse;
    var siteTable;
    var selectNewSiteForm;
    var selectNewSiteOkButton;
    var oSettings;

    beforeEach(function() {
        var json = JSON.parse('{"sites":[' +
                              '{"id":"1","isValidForProduct":true,"siteName":"SITE 1","addressLine1":"Floor 1" , "addressLine2":"Building 1", "addressLine3":"Addy 1 Line 1", "townCity":"MARTLESHAM", "country":"France", "postCode":"IP1"},' +
                              '{"id":"2","isValidForProduct":true,"siteName":"SITE 2","addressLine1":"Floor 1" , "addressLine2":"Building 2", "addressLine3":"Addy 2 Line 1", "townCity":"MARTLESHAM", "country":"France", "postCode":"IP2"},' +
                              '{"id":"3","isValidForProduct":true,"siteName":"SITE 3","addressLine1":"Floor 1" , "addressLine2":"Building 3", "addressLine3":"Addy 3 Line 1", "townCity":"MARTLESHAM", "country":"France", "postCode":"IP3"}],' +
                              '"sEcho":"1","iTotalDisplayRecords":"4","iTotalRecords":"4"}');

        mockGetSiteResponse(json);
        spyAjaxCalls();

        selectNewSiteForm = new rsqe.NewSiteForm({cancelHandler: function() {}});
        selectNewSiteOkButton = $("#selectNewSiteOkButton");

        siteTable = $("#siteTable");

        spyOn(siteTable, "dataTable").andCallFake(function(test) {
            return "";
        });

        spyOn(siteTable.dataTableExt.oApi, "fnUpdateRowWithNewSiteData").andCallThrough();
        oSettings = $("#siteTable").dataTable().fnSettings();

        var fakeObject = jasmine.createSpyObj("fakeObject", ['call']);
        spyOn(oSettings, "fnServerData").andReturn(fakeObject);

        selectNewSiteForm.load();
    });

    describe('with new sites', function() {

        it('should select a new site', function() {
            var siteRadioButtons = $("input[type='radio']");
            siteRadioButtons[0].click();
            selectNewSiteOkButton.click();
            expect(siteTable.dataTableExt.oApi.fnUpdateRowWithNewSiteData).toHaveBeenCalledWith(jasmine.any(Object), jasmine.any(Object));
        });
    });

    function spyAjaxCalls() {
        $.ajax = function (options) {
            var response = getSiteResponse;
            if (options.success) {
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
});