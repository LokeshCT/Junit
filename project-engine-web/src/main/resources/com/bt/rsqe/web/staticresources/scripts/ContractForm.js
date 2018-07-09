var rsqe = rsqe || {};

rsqe.ContractForm = function(opts) {
    if (opts == null || opts.cancelHandler == null) throw "cancelHandler should be passed in";
    this.cancelHandler = opts.cancelHandler;
    this.cancelButtonId = ".dialog .cancel";
    this.contractFormId = "#contractForm";
};

rsqe.ContractForm.prototype = {
    load: function() {
        var self = this;
        self.validator = $(this.contractFormId).validate(
                {
                    rules: {
                        eupPriceBook: "required",
                        ptpPriceBook: "required"
                    },

                    messages: {
                        eupPriceBook: "Please select a EUP Price book",
                        ptpPriceBook: "Please select a PTP Price book"
                    }
                }
        );
        $('#submitContractButton').click(function() {
            var form = $(self.contractFormId);
            if (self.validator.form()) {
                $.post(form.attr('action'), form.serialize()).success(
                    function() {
                        $.navigate();//refresh
                    }).error(function(response, second) {
                                 $("#commonError").text(response.responseText);
                                 $("#commonError").removeClass("hidden");
                             });

            }
        });
        $('#contractForm').submit(function() {
            return false;
        });
        $(self.cancelButtonId).click(function() {
            self.cancelHandler();
        });
    },

    resetForm: function() {
        $(this.contractFormId).each(function() {
            this.reset();
        });
        this.validator.resetForm();
    }
};