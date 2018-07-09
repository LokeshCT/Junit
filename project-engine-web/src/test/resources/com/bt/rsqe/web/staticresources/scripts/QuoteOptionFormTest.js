var form;
describe('Quote Option Dialog', function() {
    var errorFn,successFn;
    beforeEach(function() {
        form = new rsqe.QuoteOptionForm({cancelHandler: function() {}});
        form.load();
        spyOn($, "post").andCallFake(function(){
            return {
                error: function(){
                    errorFn = arguments[0];
                    return this;
                },
                success: function(){
                    successFn = arguments[0];
                    return this;
                }
            };
        });
        $("#quoteOptionForm").submit(function(e) {
            e.preventDefault();
            return false;
        });
    });

    afterEach(function() {
        form.resetForm();
    });


    describe('with empty form', function() {
        it('should perform required field validation', function() {
            $("#submitOptionButton").click();
            expect($(".error[for='quoteOptionName']").is(":visible")).toBeTruthy();
            expect($(".error[for='contractTerm']").is(":visible")).toBeTruthy();
            expect($(".error[for='currency']").is(":visible")).toBeTruthy();
        });
    });

    describe('with form filled in ', function() {
        beforeEach(function() {
            $('#quoteOptionName').val('blahName');
            $('#contractTerm').val($('#contractTerm option:last').val());
            $('#currency').val($('#currency option:last').val());
        });


        it('Should validate that the name is less than 50 characters', function() {
            $('#quoteOptionName').val(new Array(52).join('x'));
            $("#submitOptionButton").click();
            expect($(".error[for='quoteOptionName']").is(":visible")).toBeTruthy();
        });

        describe('with valid values', function() {
            describe('on "create" click', function() {
                beforeEach(function() {
                    $("#submitOptionButton").click();
                });

                it('Should have no errors', function() {
                    expect($(".error[for='quoteOptionName']").is(":visible")).toBeFalsy();
                    expect($(".error[for='contractTerm']").is(":visible")).toBeFalsy();
                    expect($(".error[for='currency']").is(":visible")).toBeFalsy();
                });

                it('should try to post the request', function() {
                    expect($.post).toHaveBeenCalledWith($("#quoteOptionForm").attr('action'),$("#quoteOptionForm").serialize());
                });

                it('should reload page on success',function(){
                    spyOn($,'navigate');
                    expect(successFn).not.toBeUndefined();
                    expect(successFn).not.toBeNull();
                    successFn();
                    expect($.navigate).toHaveBeenCalledWith();//should be called with no params
                });

                it('should show error on failure', function(){
                    expect(errorFn).not.toBeUndefined();
                    expect(errorFn).not.toBeNull();
                    errorFn({responseText:'foo bar'});
                    expect($("#createQuoteOptionError").text()).toBe("foo bar");
                });
            });
        });


    });
});