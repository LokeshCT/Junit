/**
 * Created by IntelliJ IDEA.
 * User: 605908589
 * Date: 08/05/14
 * Time: 15:33
 * To change this template use File | Settings | File Templates.
 */
describe("Offers tab",function(){
    $(function(){
        new rsqe.OfferDetailsTab().initialise();
        $('.leftPaneContainer').append('<div id="offerDetails"></div>');
    });

    var approveOfferPostUri = '/rsqe/customers/customer/contracts/contractId/projects/projectId/quote-options/quoteOptionId/offers/offerId/approve';

    beforeEach(function() {
        spyOn($, 'post').andCallThrough();
    });

    //Happy Path
    it("should approve offer when all items are valid and priced",function(){
        tableWithMultipleValidandPricedItem();
        $('#customerApproved').click();
        expect($.post).toHaveBeenCalledWith(approveOfferPostUri);
    });

    //Sad Path
    it('should not approve the offer if item is not priced', function() {
        tableWithOneItem('Not Priced', 'N/A', 'VALID');
        $('#customerApproved').click();
        expect($.post.callCount).toEqual(0);
    });

    it('should always have export pricing sheet option', function() {
        tableWithOneItem('Not Priced', 'N/A', 'VALID');
        $('#exportPricingSheet').click();
        expect($.post.callCount).toEqual(0);
    });

    it('should not approve the offer if item\'s discount status is not valid', function() {
        tableWithOneItem('Firm', 'No Valid', 'VALID');
        $('#customerApproved').click();
        expect($.post.callCount).toEqual(0);
    });

    it('should not approve the offer if item is not VALID', function() {
        tableWithOneItem('Not Priced', 'N/A', 'INVALID');
        $('#customerApproved').click();
        expect($.post.callCount).toEqual(0);
    });

    it('should not enable create order button for MBP product line items', function() {
        tableWithOneMBPItem('Firm', 'N/A', 'VALID');
        $('#createOrder').click();
        expect($.post.callCount).toEqual(0);
    });

    it("should load the offer details columns and summary as expected", function() {
        tableWithOneItem('Firm', 'No Valid', 'VALID');
        $('#customerApproved').click();
        expect($('#offerDetails tbody tr td').length).toEqual(8);
        expect($("#offerDetails").find(".summary").html()).toEqual("10Mbps");
    });

    function tableWithOneMBPItem(pricingStatus, discountStatus, validity) {
        var html = '<table id="offerDetails" class="dataTable" aria-describedby="offerDetails_info" style="margin-left: 0px; width: 100%;">'+
                   '<tbody role="alert" aria-live="polite" aria-relevant="all">'+
                   '<tr class="odd offerItem" id="id_e3634c2c-c79c-4f38-b345-3e3bcab45f50">'+
                   '<td class="checkbox"><input type="checkbox" name="listOfOfferItems" value="e3634c2c-c79c-4f38-b345-3e3bcab45f50" disabled></td>'+
                   '<td class="product">Market Based</td>'+
                   '<td class="site">SITE 1</td>'+
                   '<td class="summary">10Mbps</td>'+
                   '<td class="status">Customer Approved</td>'+
                   '<td class="discountStatus">'+discountStatus+'</td>'+
                   '<td class="pricingStatus">'+pricingStatus+'</td>'+
                   '<td class="validity valid">'+validity+'</td>'+
                   '</tr>'+
                   '</tbody>'+
                   '</table>'
        $('#offerDetails').replaceWith(html);
    }

    function tableWithOneItem(pricingStatus, discountStatus, validity) {
        var html = '<table id="offerDetails" class="dataTable" aria-describedby="offerDetails_info" style="margin-left: 0px; width: 100%;">'+
            '<tbody role="alert" aria-live="polite" aria-relevant="all">'+
            '<tr class="odd offerItem" id="id_e3634c2c-c79c-4f38-b345-3e3bcab45f50">'+
                '<td class="checkbox"><input type="checkbox" name="listOfOfferItems" value="e3634c2c-c79c-4f38-b345-3e3bcab45f50"></td>'+
                '<td class="product">Cascade Pilot</td>'+
                '<td class="site">SITE 2</td>'+
                '<td class="summary">10Mbps</td>'+
                '<td class="status">Customer Approved</td>'+
                '<td class="discountStatus">'+discountStatus+'</td>'+
                '<td class="pricingStatus">'+pricingStatus+'</td>'+
                '<td class="validity valid">'+validity+'</td>'+
            '</tr>'+
            '</tbody>'+
        '</table>'
        $('#offerDetails').replaceWith(html);
    }

    function tableWithMultipleValidandPricedItem() {
        var html = '<table id="offerDetails" class="dataTable" aria-describedby="offerDetails_info" style="margin-left: 0px; width: 100%;">'+
            '<tbody role="alert" aria-live="polite" aria-relevant="all">'+
            '<tr class="odd offerItem" id="id_e3634c2c-c79c-4f38-b345-3e3bcab45f50">'+
                '<td class="checkbox"><input type="checkbox" name="listOfOfferItems" value="e3634c2c-c79c-4f38-b345-3e3bcab45f50"></td>'+
                '<td class="product">Item 1</td>'+
                '<td class="site">SITE 2</td>'+
                '<td class="summary">10Mbps</td>'+
                '<td class="status">Customer Approved</td>'+
                '<td class="discountStatus">N/A</td>'+
                '<td class="pricingStatus">Firm</td>'+
                '<td class="validity valid">VALID</td>'+
            '</tr>'+
            '<tr class="odd offerItem" id="id_e3634c2c-c79c-4f38-b345-3e3bcab45f50gvsd">'+
                '<td class="checkbox"><input type="checkbox" name="listOfOfferItems" value="e3634c2c-c79c-4f38-b345-3e3bcab45f50"></td>'+
                '<td class="product">Item 2</td>'+
                '<td class="site">SITE 2</td>'+
                '<td class="summary">10Mbps</td>'+
                '<td class="status">Customer Approved</td>'+
                '<td class="discountStatus">Approved</td>'+
                '<td class="pricingStatus">Firm</td>'+
                '<td class="validity valid">VALID</td>'+
            '</tr>'+
            '<tr class="odd offerItem" id="id_e3634c2c-c79c-4f38-b345-3e3bcab45fsdfdsfs50">'+
                '<td class="checkbox"><input type="checkbox" name="listOfOfferItems" value="e3634c2c-c79c-4f38-b345-3e3bcab45f50"></td>'+
                '<td class="product">Item 3</td>'+
                '<td class="site">SITE 2</td>'+
                '<td class="summary">10Mbps</td>'+
                '<td class="status">Customer Approved</td>'+
                '<td class="discountStatus">N/A</td>'+
                '<td class="pricingStatus">Firm</td>'+
                '<td class="validity valid">WARNING</td>'+
            '</tr>'+
               '<tr class="odd offerItem" id="id_e3634c2c-c79c-4f38-b345-3e3bcab45fsdfdsfs50">' +
               '<td class="checkbox"><input type="checkbox" name="listOfOfferItems" value="e3634c2c-c79c-4f38-b345-3e3bcab45f50"></td>' +
               '<td class="product">Item 4</td>' +
               '<td class="site">SITE 2</td>' +
               '<td class="summary">10Mbps</td>'+
               '<td class="status">Customer Approved</td>' +
               '<td class="discountStatus">N/A</td>' +
               '<td class="pricingStatus">N/A</td>' +
               '<td class="validity valid">VALID</td>' +
               '</tr>' +
            '</tbody>'+
        '</table>'
        $('#offerDetails').replaceWith(html);
    }
});