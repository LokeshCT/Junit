describe("Status Message", function() {
    var statusMessage;

    beforeEach(function() {
        statusMessage = new rsqe.StatusMessage("#successMessage");
    });

    it("should show message and then hide after half a second", function() {
        expect($("#successMessage").hasClass("hidden")).toBeTruthy();
        statusMessage.show("A message.", 500);
        expect($("#successMessage").hasClass("hidden")).toBeFalsy();
        expect($("#successMessage").text()).toEqual("A message.");
        waitsFor(function() {
            return $("#successMessage").hasClass("hidden");
        }, "Timed out", 1000);
    });

    it("should show message permanently", function() {
        expect($("#successMessage").hasClass("hidden")).toBeTruthy();
        statusMessage.show("A message.");
        expect($("#successMessage").hasClass("hidden")).toBeFalsy();
        expect($("#successMessage").text()).toEqual("A message.");
    });

    it("should hide the message", function() {
        statusMessage.show("A message.");
        expect($("#successMessage").hasClass("hidden")).toBeFalsy();
        statusMessage.hide();
        expect($("#successMessage").hasClass("hidden")).toBeTruthy();
        expect($("#successMessage").text()).toEqual("");
    });
});
