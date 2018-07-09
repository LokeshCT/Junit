describe("Dialog", function() {
    var dialog1, dialog2, validation_result, validated_dialog;
    $(
        function() {
            dialog1 = new rsqe.Dialog("#theDialog", {
                title: "The Dialog",
                openers: "#openTheDialog1,#openTheDialog2"//pass in css selectors
            });
            dialog2 = new rsqe.Dialog($("#anotherDialog"), {
                title: "Another dialog",
                openers: $("#openAnotherDialog"),
                closers: $("#closeAnotherDialog1 , #anotherDialog .another-close")//pass in elements
            });
            validated_dialog = new rsqe.Dialog($("#validatedDialog"), {
                title: "Another dialog",
                validator: function(){
                    return validation_result;
                }
            });
        }
    );
    afterEach(function() {
        dialog1.close();
        dialog2.close();
    });

    it("Should create dialog that is not auto opened", function() {
        expect($("#theDialog").is(":visible")).toBeFalsy();
        expect($("#anotherDialog").is(":visible")).toBeFalsy();
    });

    it("Clicking openers should show dialog", function() {
        $("#openTheDialog1").click();
        expect($("#theDialog").is(":visible")).toBeTruthy();
        dialog1.close();
        $("#openTheDialog2").click();
        expect($("#theDialog").is(":visible")).toBeTruthy();
        dialog1.close();
        $("#openAnotherDialog").click();
        expect($("#anotherDialog").is(":visible")).toBeTruthy();
    });

    it("should open/close the dialog", function() {
        dialog1.open();
        expect($("#theDialog").is(":visible")).toBeTruthy();
        dialog1.close();
        expect($("#theDialog").is(":visible")).toBeFalsy();
    });


    it("should close the dialog when closers are clicked", function() {
        dialog2.open();
        $('#closeAnotherDialog1').click();
        expect($("#anotherDialog").is(":visible")).toBeFalsy();
        dialog2.open();
        $('#anotherDialog .another-close').click();
        expect($("#anotherDialog").is(":visible")).toBeFalsy();
    });

    it("should auto wire cancel link", function() {
        dialog1.open();
        $('#theDialog .cancel').click();
        expect($("#theDialog").is(":visible")).toBeFalsy();
        dialog2.open();
        $('#anotherDialog .cancel').click();
        expect($("#anotherDialog").is(":visible")).toBeFalsy();
    });

    it("Should set options",function(){
        //ui-dialog-title
        dialog1.open();
        expect($('#ui-dialog-title-theDialog').text()).toBe("The Dialog");
        dialog1.setOptions({"title": "foo-bar"});
        expect($('#ui-dialog-title-theDialog').text()).toBe("foo-bar");
    });

    it("should validate before open",function(){
        validation_result = false;
        validated_dialog.open();
        expect($("#validatedDialog").is(":visible")).toBeFalsy();
        validation_result = true;
        validated_dialog.open();
        expect($("#validatedDialog").is(":visible")).toBeTruthy();
    });
});
