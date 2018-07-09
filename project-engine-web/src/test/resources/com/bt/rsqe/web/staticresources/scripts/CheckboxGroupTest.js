describe("Checkbox group", function() {
    var called;
    var checkboxGroup = new rsqe.CheckboxGroup("input[name='foo-group']", {
        someChecked: function () {called = true;},
        actionButtons: "#button1 , #button2, #link1, #link2",
        select_all: "#select-all"
    });

    var noDisabledCheckBoxesMatchGroup = new rsqe.CheckboxGroup("input:not([disabled])[name = 'disabled-group']", {
        someChecked: function () {called = true;},
        actionButtons: "#button1 , #button2, #link1, #link2",
        select_all: "#select-all"
    });


    beforeEach(function() {
        checkboxGroup.uncheck();
        checkboxGroup.initialize();
        $("#select-all").removeAttr("checked");
        called = false;
    });

    it("initialize should disable links if no items selected", function() {
        $('#link1').removeClass("disabled");
        checkboxGroup.initialize();
        expect($('#link1').is(".disabled")).toBeTruthy();
    });

    it("should enable links when atleast one checkbox is selected", function() {
        $("#check1").click();//check
        assertActionButtonsEnabled();
        $("#check1").click();//uncheck
        assertActionButtonsDisabled();
    });


    function assertActionButtonsDisabled() {
        $("#button1 , #button2 , #link1 , #link2").each(function() {
            expect($(this).is(".disabled")).toBeTruthy();
        });
    }

    function assertActionButtonsEnabled() {
        $("#button1 , #button2 , #link1 , #link2").each(function() {
            expect($(this).is(".disabled")).toBeFalsy();
        });
    }

    it("should disable action buttons by default(when nothing is checked)", function() {
        $("#button1 , #button2").each(function() {
            expect($(this).is(":disabled")).toBeTruthy();
        });
    });

    it("should enable buttons when atleast one checkbox is checked", function() {
        $("#check1").click();//check
        expect($('button').is(":disabled")).toBeFalsy();
        $("#check1").click();//uncheck
        expect($('button').is(":disabled")).toBeTruthy();
    });

    it("should check all", function() {
        checkboxGroup.check();
        $("input[name='foo-group']").each(function() {
            expect($(this).is(":checked")).toBeTruthy();
        })
    });

    it("should call some checked", function() {
        $("#check1").click();//check
        expect(called).toBeTruthy();
    });

    it("select-all check should check all items in the group", function() {
        $("#select-all").click();
        expect(checkboxGroup.all_selected()).toBeTruthy();
        assertActionButtonsEnabled();
        $("#select-all").click();
        expect(checkboxGroup.all_unselected()).toBeTruthy();
        assertActionButtonsDisabled();
    });

    it("select-all should not check any disabled checkboxes", function() {
        expect(noDisabledCheckBoxesMatchGroup.all_selected()).toBeFalsy();
        $("#select-all").click();
        expect(noDisabledCheckBoxesMatchGroup.all_selected()).toBeFalsy();
    });

    it("select-all check should be checked when all checks are checked", function() {
        $("input[name='foo-group']").click();
        expect($("#select-all").is(":checked")).toBeTruthy();
    });

    it("select-all check should be unchecked if some checkboxes are not selected", function() {
        $("#select-all").click();
        $("#check1").click();
        expect($("#select-all").is(":checked")).toBeFalsy();
        expect(checkboxGroup.all_selected()).toBeFalsy();
    });

    it("should return selected items", function() {
        $("#check1").click();
        $("#check3").click();
        var selected_elements = checkboxGroup.selected_elements();
        expect(selected_elements.length).toBe(2);
        expect(selected_elements.filter("#check1").length).toBe(1);
        expect(selected_elements.filter("#check3").length).toBe(1);
    });

    it("select-all check should be unchecked if the uncheck method is called", function() {
        $("#select-all").click();
        checkboxGroup.uncheck();
        expect($("#select-all").is(":checked")).toBeFalsy();
        expect(checkboxGroup.all_selected()).toBeFalsy();
    })
});