describe("disable-able", function() {
    var called;
    beforeEach(function() {
        $("#enabled-link, #disabled-link").disableable('click', function() {
            called = true;
        });
        called = false;
    });

    it("should chain with empty call", function() {
        expect($("#enabled-link").disableable().text()).toBe($("#enabled-link").text());
    });

    it("should call enable with parameter", function() {
        var called = false;
        $("#enabled-link").disableable({click:function() {
                                           called = true;
                                       }});
        expect($("#enabled-link").disableable('enable', false).text()).toBe($("#enabled-link").text());
        expect($("#enabled-link").is(".disabled")).toBeTruthy();
        expect($("#enabled-link[disabled]").length).toBe(1);
        $('#enabled-link').click();
        expect(called).toBeFalsy();

        expect($("#enabled-link").disableable('enable').text()).toBe($("#enabled-link").text());
        $('#enabled-link').click();
        expect($("#enabled-link").is(".disabled")).toBeFalsy();
        expect($("#enabled-link[disabled]").length).toBe(0);
        expect(called).toBeTruthy();
    });


    it("should be able to accept click handlers as initializer", function() {
        var anotherCalled = false;
        $("#enabled-link, #disabled-link").disableable({click:function() {
                                                           anotherCalled = true;
                                                       }});
        $('#disabled-link').click();
        expect(anotherCalled).toBeFalsy();
        $('#enabled-link').click();
        expect(anotherCalled).toBeTruthy();
    });

    it("should be able to accept enabled as option", function() {
        $("#enabled-link").disableable({enabled: false});
        $('#enabled-link').click();
        expect(called).toBeFalsy();
        $("#enabled-link").disableable({enabled: true});
        $('#enabled-link').click();
        expect(called).toBeTruthy();
    });

    it("should call handler for enabled link", function() {
        $("#enabled-link").click();
        expect(called).toBeTruthy();
    });

    it("should not call handler for disabled link", function() {
        $("#disabled-link").disableable('click');
        expect(called).toBeFalsy();
    });

    it("should enable/disable", function() {
        $("#enabled-link").disableable('disable');
        expect($("#enabled-link").is(".disabled")).toBeTruthy();
        expect($("#enabled-link[disabled]").length).toBe(1);

        $("#enabled-link").disableable('enable');
        expect($("#enabled-link").is(".disabled")).toBeFalsy();
        expect($("#enabled-link[disabled]").length).toBe(0);
    });
});