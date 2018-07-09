describe("Orders tab",function(){
    $(function(){
        new rsqe.OrdersTab().initialise();
    });

    it("should Be Marked As A Migration Order",function(){
        spyOn($, 'post').andCallThrough();
        expect($(".migrationQuote").text()).toBe("Yes");
    });

    it("should show Migration Confirmation Dialog on clicking Submit Order",function(){
        spyOn($, 'post').andCallThrough();
        expect($("#migrationConfirmationDialog").is(":visible")).toBeFalsy();
        $(".submitOrder").click();
        expect($("#migrationConfirmationDialog").is(":visible")).toBeTruthy();
        expect($.post.callCount).toEqual(0);
    });

    it("should close Migration Confirmation Dialog and show Migration Cancelled Info Dialog when No is Clicked",function(){
        spyOn($, 'post').andCallThrough();
        expect($("#migrationConfirmationDialog").is(":visible")).toBeTruthy();
        expect($("#migrationCancelledInfoDialog").is(":visible")).toBeFalsy();
        $("#migrationNoButton").click();
        expect($("#migrationConfirmationDialog").is(":visible")).toBeFalsy();
        expect($("#migrationCancelledInfoDialog").is(":visible")).toBeTruthy();
        expect($.post.callCount).toEqual(0);
    });

    it("should close Migration Cancelled Info Dialog when Close is Clicked",function(){
        spyOn($, 'post').andCallThrough();
        expect($("#migrationCancelledInfoDialog").is(":visible")).toBeTruthy();
        $("#migrationCancellationInfoDialogCloseButton").click();
        expect($("#migrationCancelledInfoDialog").is(":visible")).toBeFalsy();
        expect($.post.callCount).toEqual(0);
    });

    it("should submit order on clicking Yes on the Migration Confirmation Dialog ",function(){
        spyOn($, 'ajax').andCallThrough();
        $(".submitOrder").click();
        $("#migrationYesButton").click();
        var orderPostUri = '/rsqe/customers/customerId1/contracts/contractId1/projects/projectId1/quote-options/quoteOptionId1/orders/orderId1/submit';
        expect($.ajax).toHaveBeenCalledWith({ type:"POST", url:orderPostUri});
        expect($("#rfoImportDialog").is(":visible")).toBeFalsy();
        expect($(".submitOrder").is("disabled"));
    });
});