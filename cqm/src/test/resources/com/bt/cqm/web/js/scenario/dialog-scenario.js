'use strict';

describe('DialogTest', function() {


    beforeEach(function() {
        browser().navigateTo('/test/resources/web/pages/test-page.html#/dialog');
        element('#openDialog').click();

    });

    it('should open dialog', function() {
        expect(element('.rsqe-dialog-container').css('display')).not().toBe("none");
    });


    it('should have title', function() {
        expect(element('.ui-dialog-title').text()).toBe("test title");
    });


    it('should perform ok action', function() {
        element('.action-ok').click();

        expect(element('.rsqe-dialog-container').css('display')).toBe("none");
        expect(element('.action-result').val()).toBe("OkActionPerformed");
    });


    it('should close dialog on close button click', function() {
        element('.action-cancel').click();

        expect(element('.rsqe-dialog-container').css('display')).toBe("none");
        expect(element('.action-result').text()).toBe("CancelActionPerformed");
    });


    it('should close dialog on "cross" icon click', function() {
        element('.ui-dialog-titlebar-close').click();

        expect(element('.rsqe-dialog-container').css('display')).toBe("none");
        expect(element('.action-result').text()).toBe("CloseActionPerformed");
    });


    describe('Button visibility', function() {

        beforeEach(function() {
            browser().navigateTo('/test/resources/web/pages/test-page.html#/dialog');
        });


        it('should display ok, cancel & detail buttons', function() {
            element('#openDialog').click();

            expect(element('.action-ok').css('display')).not().toBe('none');
            expect(element('.action-cancel').css('display')).not().toBe('none');
            expect(element('.action-details').css('display')).not().toBe('none');
        });


        it('should hide buttons', function() {
            element('#noOkCancelButtons').click();

            expect(element('.action-ok').css('display')).toBe('none');
            expect(element('.action-cancel').css('display')).toBe('none');
            expect(element('.action-details').css('display')).toBe('none');
        });


        it('should hide "Details" button', function() {
            element('#noDetailsButton').click();

            expect(element('.action-ok').css('display')).not().toBe('none');
            expect(element('.action-cancel').css('display')).not().toBe('none');
            expect(element('.action-details').css('display')).toBe('none');
        });


        it('should display only "Ok" button', function() {
            element('#okButNoCancelButton').click();

            expect(element('.action-ok').css('display')).not().toBe('none');
            expect(element('.action-cancel').css('display')).toBe('none');
            expect(element('.action-details').css('display')).toBe('none');
        });
    });
});