describe("Orders tab",function(){
    $(function(){
        new rsqe.OrdersTab().initialise();
    });

    it("should open rfo dialog on import button click",function(){
        expect($("#rfoImportDialog").is(":visible")).toBeFalsy();
        $("a.importRFO").click();
        expect($("#rfoImportDialog").is(":visible")).toBeTruthy();
        $("input.cancel.button").click();
    });

    it("should not be marked as a Migration Order",function(){
        expect($(".migrationQuote").text()).toBe("No");
    });

    it("should disable submit order button after upload rfo not successfully",function(){
        expect($("#rfoImportDialog").is(":visible")).toBeFalsy();
        expect($(".submitOrder").is("disabled"));
        $("a.importRFO").click();
        $("#uploadButton").click();
        expect($(".submitOrder").is("disabled"));
    });

});