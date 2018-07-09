/**
 * Created by IntelliJ IDEA.
 * User: 605908589
 * Date: 13/06/14
 * Time: 11:49
 * To change this template use File | Settings | File Templates.
 */
describe('Offer Dialog Test', function() {

    it('should display a message when the customer order ref input has reached 20 characters', function() {
        var below20 = 'this is below 20';
        var just20Chars = 'this is just 20.....';
        var justUnder20 = 'this is under 20...';

        expect($('#customerOrderRefTextError').hasClass('hidden')).toBeTruthy();
        $('#customerOrderRefText').val(below20);
        $('#customerOrderRefText').trigger('keyup');
        expect($('#customerOrderRefTextError').hasClass('hidden')).toBeTruthy();

        $('#customerOrderRefText').val(just20Chars);
        $('#customerOrderRefText').trigger('keyup');
        expect($('#customerOrderRefTextError').hasClass('hidden')).toBeFalsy();

        $('#customerOrderRefText').val(justUnder20);
        $('#customerOrderRefText').trigger('keyup');
        expect($('#customerOrderRefTextError').hasClass('hidden')).toBeTruthy();
    })

});