var rsqe = rsqe || {};

rsqe.QuoteOptionForm = function(opts) {
    if (opts == null || opts.cancelHandler == null) throw "cancelHandler should be passed in";
    this.cancelHandler = opts.cancelHandler;
    this.cancelOptionButtonId = ".dialog .cancel";
    this.newQuoteOptionFormId = "#quoteOptionForm";
};

rsqe.QuoteOptionForm.prototype = {
    load: function() {
        var self = this;

        self.validator = $(this.newQuoteOptionFormId).validate(
            {
                rules: {
                    quoteOptionName: {required: true, maxlength: 50},
                    contractTerm: "required",
                    currency: "required"


                },

                messages: {
                    quoteOptionName:"Please provide a Quote Name.",
                    contractTerm:"Please select a Contract Term.",
                    currency:"Please select a Currency."
                      }
            }
        );
        $('#submitOptionButton').click(function() {
            var form = $(self.newQuoteOptionFormId);
            if (self.validator.form()) {
                $.post(form.attr('action'), form.serialize()).success(
                    function() {
                        window.location = "?quoteCreated=true"; // refresh page with query string param
                    }).error(function(response, second) {
                                 $("#createQuoteOptionError").text(response.responseText);
                                 $("#createQuoteOptionError").removeClass("hidden");
                             });

            }
        });
        $('#quoteOptionForm').submit(function() {
            return false;
        });

        $('#contractTerm').change(function () {

            var hasFirmAssetsOnQuote = {
                type:'GET',
                url: $("#contractTermChangeValidationUri").val(),
                dataType:'json'
            };

            $("#blockUI").css('display', 'block');

            $.ajax(hasFirmAssetsOnQuote).then(function(data) {
                if (data) {
                    $("#saveContractTermWarning").removeClass("hidden");
                }
                $("#blockUI").css('display', 'none');
            }, function() {
               $("#blockUI").css('display', 'none');
            });

        });

        $(self.cancelOptionButtonId).click(function() {
            self.cancelHandler();
        });
    },

    resetForm: function() {
        $(this.newQuoteOptionFormId).each(function() {
            this.reset();
        });
        this.validator.resetForm();
    }
};
