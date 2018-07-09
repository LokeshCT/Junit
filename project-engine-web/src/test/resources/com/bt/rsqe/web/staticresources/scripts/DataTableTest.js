describe("RSQE data table",function(){
    it("should add/append to error row",function(){
        var data = [{
            message: "blah message",
            errorType: "Error",
            category: "DP"
        }];
        var data2 = [{
            message: "new blah message",
            errorType: "Error",
            category: "DP"
        }];
        var row = $("tr").first();
        expect(row.find(".validity").is(".pending")).toBeFalsy();
        new rsqe.optiondetails.DataTable().updateErrorRow(row,data);
        expect($("#error_" + row.attr("id") + " div").text()).toContain("blah message");
        expect(row.find(".validity").is(".pending")).toBeTruthy();
        new rsqe.optiondetails.DataTable().updateErrorRow(row,data2);
        expect($("#error_" + row.attr("id")).length).toBe(1);
        expect($("#error_" + row.attr("id") + " div").text()).toContain("new blah message");
    });

    it("should hook uo click event to display error row",function(){
        var row = $("#id_94e78f3d-9df6-4de0-89f8-ffa7abad1d92");
        new rsqe.optiondetails.DataTable().updateErrorRow(row,"blah message");
        expect($("#error_" + row.attr("id")).is(".hidden")).toBeTruthy();
        row.find(".validity").click();
        expect($("#error_" + row.attr("id")).is(".hidden")).toBeFalsy();
        expect($("#error_" + row.attr("id") + " div").is(":visible")).toBeTruthy();
    });
    it("should remove error row", function() {
        var row = $("#id_oneWithError");

        expect($("#error_id_oneWithError").length).toBe(1);

        row.find(".validity").text("VALID");
        new rsqe.optiondetails.DataTable().updateErrorRow(row, "");

        expect(row.find(".validity").is(".pending")).toBeFalsy();
        expect(row.find(".validity").is(".valid")).toBeTruthy();
        expect($("#error_id_oneWithError").length).toBe(0);

    })

    it("should add/append to pending row",function(){
        var data = [{
            message: "blah pending message",
            errorType: "Pending",
            category: "DP"
        }];
        var row = $("tr").first();
        expect(row.find(".validity").is(".valid")).toBeFalsy();
        expect(row.find(".validity").is(".pending")).toBeTruthy();
        new rsqe.optiondetails.DataTable().updateErrorRow(row,data);
        expect($("#error_" + row.attr("id") + " div").text()).toContain("blah pending message");
    });

    it('should append pricing errors', function() {
        //Add validation messages first
        var data = [{
            message: "blah message",
            errorType: "Error",
            category: "DP"
        }];
        var row = $("tr").first();
        new rsqe.optiondetails.DataTable().updateErrorRow(row,data);
        //Add pricing errors
        var pricingErrors = [{
            error: 'error'
        }];
        new rsqe.optiondetails.DataTable().updatePricingErrorRow(row,pricingErrors);
        expect($('.pricing-message').first().find('li').text()).toEqual('error');
    });

    it('should create the error row for pricing errors', function() {
        var row = $("tr").first();
        $("#error_" + row.attr("id")).remove();
        var pricingErrors = [{
            error: 'error'
        }];
        new rsqe.optiondetails.DataTable().updatePricingErrorRow(row,pricingErrors);
        expect($("#error_" + row.attr("id") + " div").text()).toContain("error");
    })

});
