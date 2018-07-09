describe('Import Product', function() {

    var ImportProduct;

    beforeEach(function() {
        ImportProduct = new rsqe.ImportProduct();
    });

    it('should not enable import product when offer is customer approved', function() {
        $('#id_94e78f3d-9df6-4de0-89f8-ffa7abad1d92 .status').html('Customer Approved');
        expect($('#importProduct').attr('class')).toContain('hidden');
        ImportProduct.enableImportProductButton(['94e78f3d-9df6-4de0-89f8-ffa7abad1d92']);
        expect($('#importProduct').attr('class')).toContain('hidden');
    });

    afterEach(function() {

    });
});

