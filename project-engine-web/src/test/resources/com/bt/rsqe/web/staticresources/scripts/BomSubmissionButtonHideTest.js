describe("Orders tab",function(){
    $(function(){
        new rsqe.OrdersTab().initialise();
    });

    it("should disable submit order button and hide rfo buttons when order status is In progress", function () {
        expect($("#rfoImportDialog").is(":visible")).toBeFalsy();
        expect($(".submitOrder").is("disabled"));
    });
});