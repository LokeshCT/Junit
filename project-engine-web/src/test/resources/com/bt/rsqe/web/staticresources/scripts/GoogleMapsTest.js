/**
 * Created by IntelliJ IDEA.
 * User: 605908589
 * Date: 09/04/14
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
describe("Line item validation", function() {


    it("should not enable google maps button when no sites are added to quote", function() {
        expect($('input[name="listOfQuoteOptionItems"]').length).toEqual(0);
        expect($('#locateOnGoogleMap').is(':enabled')).toBeFalsy();
    });

    it('should open popup when clicking on Locate On Google Maps', function() {
        new rsqe.QuoteOptionDetailsTab();
        var popup;
        spyOn(window, 'open').andCallFake(function () {
            popup = {
                focus: jasmine.createSpy()
            };
            return popup;
        });
        addLineItemsAndEnableButton();
        expect($('input[name="listOfQuoteOptionItems"]').length).toEqual(1);
        $('#locateOnGoogleMaps').click();
        expect(window.open).toHaveBeenCalledWith($('#locateOnGoogleMapsUrl').html(), '', "toolbar=no,scrollbars=no,resizable=no,height=700,location=no");
    });


    function addLineItemsAndEnableButton() {
        var html = '<table>'+
                       '< tbody >'+
                   '<tr class="odd lineItem" id="id_1a1c16bc-a4de-4fc2-b551-ed74c6f7a03f">'+
                       '<td class="checkbox">'+
                           '<input type="checkbox" value="1a1c16bc-a4de-4fc2-b551-ed74c6f7a03f" name="listOfQuoteOptionItems"/>'+
                       '</td>'+
                       '<td class="validity">PENDING</td>'+
                   '</tr>'+
            '< /tbody>'+
        '</table>';
        $('.leftPaneContainer').append(html);
        $('#locateOnGoogleMaps').disableable('enable');
    }

    });