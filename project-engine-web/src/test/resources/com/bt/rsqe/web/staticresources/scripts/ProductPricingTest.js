describe('Product Pricing', function() {

    var keepPolling = true;
    var $get = $.get;
    var response;
    var ProductPriceService;
    var pricingUri = '/rsqe/customers/1/contracts/2/projects/3/quote-options/4/line-item-prices';
    var loadIcon_fff = '<img src="/rsqe/project-engine/static/images/cell_validate.gif">';
    var LINEITEMS = "1a1c16bc-a4de-4fc2-b551-ed74c6f7a03f,6f95029f-a033-4886-8cf7-0095db560810,94e78f3d-9df6-4de0-89f8-ffa7abad1d92";
    //Mock the data table class
    rsqe.optiondetails.DataTable = function() {
        return {
            updatePricingErrorRow: function() {

            }
        }
    };

    beforeEach(function() {
        $.get = function() {
            return {
                success: function(fn) {
                    if (keepPolling) {
                        fn(JSON.stringify({
                            done: false,
                            response: null
                        }))
                    } else {
                        response ? fn(response) : fn(JSON.stringify({done: true,
                                                         response:[{
                                                             "status": "Firm",
                                                             "lineItemId": "1a1c16bc-a4de-4fc2-b551-ed74c6f7a03f",
                                                             "errors": []
                                                         }
                                                         ,{
                                                             "status": "Firm",
                                                             "lineItemId": "6f95029f-a033-4886-8cf7-0095db560810",
                                                             "errors": []
                                                         }]}))
                    }
                    return this;
                },
                error: function(fn) {
                }
            };
        };
        ProductPriceService = new rsqe.ProductPricing();
        spyOn($, 'get').andCallThrough();
        keepPolling = true;
    });

    it('should start polling and return prices', function() {
        ProductPriceService.priceLineItems(LINEITEMS);
        expect($.get).toHaveBeenCalledWith(pricingUri+'/'+LINEITEMS);
    });

    it('should call get multiple times when polling', function() {
        runs(function() {
            ProductPriceService.priceLineItems(LINEITEMS);
            expect($.get).toHaveBeenCalledWith(pricingUri+'/'+LINEITEMS);
        });

        runs(function() {
            expect($.get).toHaveBeenCalledWith(pricingUri+'/'+LINEITEMS);
        });

        keepPolling = false;

        waits(6000);

        runs(function() {
            expect($.get).toHaveBeenCalledWith(pricingUri+'/'+LINEITEMS);
        });
    });

    it('should set pricing status to spinner when polling', function() {
        ProductPriceService.priceLineItems(LINEITEMS);
        expect($('#id_1a1c16bc-a4de-4fc2-b551-ed74c6f7a03f .pricingStatus').html()).toEqual(loadIcon_fff);
        expect($('#id_6f95029f-a033-4886-8cf7-0095db560810 .pricingStatus').html()).toEqual(loadIcon_fff);
        keepPolling = false;

        waits(6000);

        runs(function() {
            expect($('#id_1a1c16bc-a4de-4fc2-b551-ed74c6f7a03f .pricingStatus').html()).toEqual('Firm');
            expect($('#id_6f95029f-a033-4886-8cf7-0095db560810 .pricingStatus').html()).toEqual('Firm');
            expect($('#id_94e78f3d-9df6-4de0-89f8-ffa7abad1d92 .pricingStatus').html()).toEqual('N/A');
        })
    });

    it('should not send Pricing requests for items with spinners', function() {
        $('#id_6f95029f-a033-4886-8cf7-0095db560810 .pricingStatus').html(loadIcon_fff);
        ProductPriceService.priceLineItems(LINEITEMS);
        expect($.get).toHaveBeenCalledWith(pricingUri+'/'+'1a1c16bc-a4de-4fc2-b551-ed74c6f7a03f,94e78f3d-9df6-4de0-89f8-ffa7abad1d92');
    });

    it('should handle repeated requests for the same line item', function() {
        $('#id_1a1c16bc-a4de-4fc2-b551-ed74c6f7a03f .pricingStatus').html(loadIcon_fff);
        $('#id_6f95029f-a033-4886-8cf7-0095db560810 .pricingStatus').html(loadIcon_fff);
        $('#id_94e78f3d-9df6-4de0-89f8-ffa7abad1d92 .pricingStatus').html(loadIcon_fff);
        ProductPriceService.priceLineItems(LINEITEMS);
        expect($.get.callCount).toEqual(0);
    });

    afterEach(function() {
        $.get = $get;
        keepPolling = false;
        $('#id_1a1c16bc-a4de-4fc2-b551-ed74c6f7a03f .pricingStatus').html('Firm');
        $('#id_6f95029f-a033-4886-8cf7-0095db560810 .pricingStatus').html('Firm');
        $('#id_94e78f3d-9df6-4de0-89f8-ffa7abad1d92 .pricingStatus').html('N/A');
    });
});
