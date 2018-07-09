describe("ProgressDialog", function() {
    var toBeStartedDialog, toBeFinishedDialog, toBeStartedDialogWithNoProgressText;

     $(
        function() {
            toBeStartedDialog = new rsqe.ProgressDialog("#toBeStartedProgressDialog", {
                progressText: "#toBeStartedProgressText",
                progressButton: "#toBeStartedButton",
                errorClass: "error",
                successClass: "success"
            });
            toBeFinishedDialog = new rsqe.ProgressDialog("#toBeFinishedProgressDialog", {
                progressText: "#toBeFinishedProgressText",
                progressButton: "#toBeFinishedButton",
                successClass: "success",
                errorClass: "error"
            });
            toBeStartedDialogWithNoProgressText = new rsqe.ProgressDialog("#toBeStartedWithNoProgressText", {
                progressButton: "#toBeStartedWithNoTextButton"
            });

        }
    );

    it("should show progress started when show called", function() {
        toBeStartedDialog.taskStarted("Task started");
        expect($("#toBeStartedButton").is(":visible")).toBeFalsy();
        expect($("#toBeStartedProgressDialog").hasClass("done")).toBeFalsy();
        expect($('#toBeStartedProgressText').text()).toBe("Task started");
        expect($("#toBeStartedProgressText").hasClass("error")).toBeFalsy();
        expect($("#toBeStartedProgressText").hasClass("success")).toBeFalsy();
    });

    it("should show progress started when show called for dialog with no progress text", function() {
        toBeStartedDialogWithNoProgressText.taskStarted("");
        expect($("#toBeStartedWithNoTextButton").is(":visible")).toBeFalsy();
        expect($("#toBeStartedWithNoProgressText").hasClass("done")).toBeFalsy();
    });

    it("should show yes & no buttons when delete is called for dialog", function() {
        toBeStartedDialogWithNoProgressText.taskDelete("Task started");
        expect($("#spinning").is(":visible")).toBeFalsy();
        expect($("#toBeStartedWithNoProgressText").hasClass("done")).toBeFalsy();
        expect($("#close").is(":visible")).toBeFalsy();
    });

    describe("Finish task", function() {

        beforeEach(function() {
            $("#toBeFinishedButton").addClass("hidden");
            $("#toBeFinishedProgressDialog").removeClass("done");
            $('#toBeFinishedProgressText').text("");
            $('#toBeFinishedProgressText').removeClass("error");
            $('#toBeFinishedProgressText').removeClass("success");
        });

        it("should show progress complete with errors when finished with errors called", function() {
            toBeFinishedDialog.taskFinishedWithErrors("Task finished");
            expect($("#toBeFinishedButton").is(":visible")).toBeTruthy();
            expect($("#toBeFinishedProgressDialog").hasClass("done")).toBeTruthy();
            expect($('#toBeFinishedProgressText').text()).toBe("Task finished");
            expect($('#toBeFinishedProgressText').hasClass("error")).toBeTruthy();
            expect($('#toBeFinishedProgressText').hasClass("success")).toBeFalsy();
        });

        it("should show progress completed successfully when finished called", function() {
            toBeFinishedDialog.taskFinished("Task finished");
            expect($("#toBeFinishedButton").is(":visible")).toBeTruthy();
            expect($("#toBeFinishedProgressDialog").hasClass("done")).toBeTruthy();
            expect($('#toBeFinishedProgressText').text()).toBe("Task finished");
            expect($('#toBeFinishedProgressText').hasClass("success")).toBeTruthy();
            expect($('#toBeFinishedProgressText').hasClass("error")).toBeFalsy();
        });
    });

});