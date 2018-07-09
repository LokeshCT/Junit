'use strict';

describe('Select Customer Page Scenarios', function() {

    beforeEach(function() {
        //load page
        browser().navigateTo('/test/resources/web/pages/test-page.html#/select-customer');
    });

    it('should display sales channel select box with sales channels', function() {
        expect(element("#salesChannel").css("display")).not().toEqual("none");
    });

    it('should display empty customer select box', function() {
        expect(element("#customer").css("display")).not().toEqual("none");
    });

    it('should hide contract select box unless customer is selected', function() {
        expect(element("#contractRow").css("display")).toEqual("none");
    });

    it('should display buttons to launch customer configuration page', function() {
        expect(element(".selectCustomersBtns").count()).toBe(1);
        expect(element(".selectCustomersBtns a").count()).toBe(4);
        expect(element(".selectCustomersBtns a:eq(0)").text()).toEqual("Manage Customer");
        expect(element(".selectCustomersBtns a:eq(0)").css("display")).toEqual("inline");

        expect(element(".selectCustomersBtns a:eq(1)").text()).toEqual("Manage Site");
        expect(element(".selectCustomersBtns a:eq(1)").css("display")).toEqual("inline");

        expect(element(".selectCustomersBtns a:eq(2)").text()).toEqual("Manage Quote");
        expect(element(".selectCustomersBtns a:eq(2)").css("display")).toEqual("inline");

        expect(element(".selectCustomersBtns a:eq(3)").text()).toEqual("Track Order");
        expect(element(".selectCustomersBtns a:eq(3)").css("display")).toEqual("inline");
    });

    it('should load sales channels', function() {
        expect(element("#salesChannel option").count()).toBe(5);
    });

    it('should load customers upon sales channel selection', function() {
        expect(element("#customer option").count()).toBe(1);

        select("salesChannel").option("2");
        expect(element("#customer option").count()).toBeGreaterThan(1);
    });

    it('should auto default customer if there exist only one', function() {

    });

    it('should load contracts upon customer selection', function() {
        expect(element("#contractRow").css("display")).toEqual("none");
        expect(element("#customer option").count()).toBe(1);

        select("salesChannel").option("2");
        select("customer").option("2");
        expect(element("#contractRow").css("display")).not().toEqual("none");
        expect(element("#customer option").count()).toBeGreaterThan(1);
    });

    it('should auto default contract if there exist only one', function() {
        select("salesChannel").option("2");
        select("customer").option("2");
        expect( element("#contract option[selected = 'selected']").count()).toBe(1);
    });


    describe('Validations', function() {
        it('should display error message if mandatory field not selected', function() {

        });
    });
});