
describe('Unit tests for the DetailsTable file.', function() {

    // Constants for the namespaces used in this file.
    var utility = rsqe.utility;

    it('Details DataTable should not exist before a call to the DataTables initialize function.', function() {
        var expected = utility.isDataTableInitialized('#priceLinesDetails');
        expect(expected).toBeFalsy();
    });

});