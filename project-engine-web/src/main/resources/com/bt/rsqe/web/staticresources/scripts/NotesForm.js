var rsqe = rsqe || {};

rsqe.NotesForm = function(opts) {
    if (opts == null || opts.cancelHandler == null) throw "cancelHandler should be passed in";
    this.cancelHandler = opts.cancelHandler;
    this.cancelNoteButtonId = ".cancel";
    this.addNoteButtonId = ".submit";
    this.notesFormId = "#notesForm";
    this.newTextId = "#newText";
    this.maxLength = opts.maxLength ? opts.maxLength : 4000;
};

rsqe.NotesForm.prototype = {
    load: function() {
        var self = this;
        self.validator = $(this.notesFormId).validate(
            {
                rules: {
                    newText: {
                        required: true,
                        maxlength: self.maxLength
                    }
                },

                messages: {
                    newText: {
                        required: "Please enter a note to save.",
                        maxlength: "You have reached the maximum ("+self.maxLength+") number of characters allowed for this field."
                    }
                }

            }
        );
        $(self.addNoteButtonId).click(function() {
            var form = $(self.notesFormId);
            if (self.validator.form()) {
                $.post(form.attr('action'), form.serialize()).success(
                    function() {
                        self.cancelHandler();
                    }).error(function(response) {
                                 $("#commonError").text($.parseJSON(response.responseText).description);
                             });
            }
        });
        $(self.notesFormId).submit(function() {
            return false;
        });
        $(self.cancelNoteButtonId).click(function() {
            self.cancelHandler();
        });

        $(this.newTextId).focus();
    },

    resetForm: function() {
        $(this.notesFormId).each(function() {
            this.reset();
        });
        this.validator.resetForm();
    }
};