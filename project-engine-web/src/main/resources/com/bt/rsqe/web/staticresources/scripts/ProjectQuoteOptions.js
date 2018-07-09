var rsqe = rsqe || {};

function getQuoteOptionName(scope) {
    return $(scope).parents('tr').find('.name').text()
}



rsqe.ProjectQuoteOptions = function() {
    this.quoteOptionDialog = $("#newQuoteOptionDialog");
    this.quoteOptionDialogUri = $("#quoteOptionDialogUri");
    this.newQuoteOptionLink = $("#newQuoteOptionLink");
    this.viewConfigurationLink = $("#viewConfigurationLink");
    this.editQuoteOptionLinks = $("a[rel='edit']");
    this.bidCommentsDialog = $("#commentsDialog");

    this.noteLinks = $("a[rel='note']");
    this.noteDialog = $("#notesDialog");
    this.noteDialogUri = $("#notesDialogUri");

    this.bcmImportLinks = $("a[rel='bcmImport']");
    this.bcmRejectDiscountLinks = $("a[rel='bcmRejectDiscounts']");
    this.bcmApproveDiscountLinks = $("a[rel='bcmApproveDiscounts']");

    this.progressDialog = new rsqe.ProgressDialog("#progressDialog", {
                progressText: "#progressText",
                progressButton: "#progressButton",
                errorClass: "error",
                successClass: "success"
    });

    this.deleteQuoteOption = new rsqe.DeleteQuoteOption();
    this.quoteOptionCount = 0;
};

rsqe.ProjectQuoteOptions.prototype = {
    initialise: function() {
        var self = this;

$(document).ready(function(){
    // Automatically enter in to the most recent quote created
    var quoteCreated = getParameterByName('quoteCreated');
    if (quoteCreated == "true") {
       var quoteUri = $("td.uri").first().text();
       window.location = quoteUri;
    }
});

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
};
        $('.caBCMButton').click(function() {
           $("#commonError").addClass("hidden");
           var validationUri = $(this).attr("validateBcmUri");
           var href = $(this).attr("href");

           $.get(validationUri)
                .success(function () {
                    window.location.href = href;
                })
                .error(function (data) {
                    $("#commonError").html("Error exporting BCM Sheet. " + (undefined != data.responseText ? data.responseText : '')).removeClass("hidden");
                });

           return false;
        });

        $('.launchQuote').click(function () {
            $("#commonError").addClass("hidden");
            var expedioQuoteId = $("#expedioQuoteId").val();
            if (expedioQuoteId == "") {
                $("#commonError").html("Please select a Expedio Quote Id. ").removeClass("hidden");
                return false;
            }
            var launchQuoteUri = self.buildLaunchQuoteUri(expedioQuoteId, $(this).attr("launchQuoteUri"));

            $.get(launchQuoteUri)
                    .success(function () {
                                 window.location.href = launchQuoteUri;
                             })
                    .error(function (data) {
                               $("#commonError").html("Error Loading Quote. " + (undefined != data.responseText ? data.responseText : '')).removeClass("hidden");
                           });

            return false;
        });

        self.optionDialogInstance = new rsqe.Dialog(this.quoteOptionDialog, {width:400});
        this.quoteOptionForm = new rsqe.QuoteOptionForm({cancelHandler: function() { self.optionDialogInstance.close(); }});
        this.newQuoteOptionLink.click(function() {
            self.loadQuoteOptionDialog(null, "Create Quote Option");
            self.optionDialogInstance.open();
        });


        var uri = $("#viewConfigurationDialogUri").val();
        this.viewConfigurationLink.click(function() {
            window.open( uri ,"View Configuration","scrollbar=1,resizable=1,width=1000,height=500");
        });

        this.editQuoteOptionLinks.each(function () {
            var quoteOptionId = $(this).attr("data-id");
            if ("" != quoteOptionId) {
                $(this).click(function () {
                    self.loadQuoteOptionDialog(quoteOptionId, "Edit Quote Option");
                    self.optionDialogInstance.open();
                });
            }
        });


        this.noteDialogInstance = new rsqe.Dialog(self.noteDialog, {width:400,position: 'top'
                                                  }

        );
        this.noteForm = new rsqe.NotesForm({cancelHandler: function() { self.noteDialogInstance.close(); }});
        this.noteLinks.each(function() {
            var quoteOptionId = $(this).attr("data-id");
            $(this).click(function() {
                var quoteOptionName = $(this).parents('tr').find('.name').text();
                self.loadNoteDialog(quoteOptionId, quoteOptionName);
                self.noteDialogInstance.open();
                return false;
            });
        });


        this.setupProgressDialog();

        this.bcmImportDialog = new rsqe.BcmImportDialog(this.progressDialog, this.progressDialogInstance);
        this.bcmImportDialog.setupDialog();

        this.bcmImportLinks.each(function() {
            var uri = $(this).attr("href");
            $(this).click(function() {
                self.bcmImportDialog.openDialog(uri, this);
                return false;
            });
        });

        this.bcmRejectDialog = new rsqe.BcmRejectDialog();
        this.bcmRejectDialog.setupDialog();

        this.bcmRejectDiscountLinks.each(function() {
                                             var uri = $(this).attr("href");
                                             $(this).click(function() {
                                                 var quoteOptionName = getQuoteOptionName(this);
                                                 return false;
                                             });
                                         }
        );

        this.bcmRejectDiscountLinks.click(function() {
                    var currentScope = this;
                    var commentsUri = $(this).attr('action');
                    $.get(this.getAttribute('action'))
                        .success(function () {
                            var quoteOptionName = getQuoteOptionName(currentScope);
                            var successMessage = "Discounts Rejected for quote option " + quoteOptionName;
                            $("#commonError").addClass("hidden");
                            self.commentsDialogInstance.setOptions({"title":"Rejection Confirmation"});
                                                                             self.loadCommentsAndCaveatsDialog(commentsUri, "Reject", quoteOptionName, "Quote Options");
                                                                             self.commentsDialogInstance.open();
                        })
                        .error(function (data) {
                            var failureMessage = "Discount rejection failure: " + data.responseText;
                            $("#commonError").html(failureMessage).removeClass("hidden");
                            $("#successMessage").addClass("hidden");

                        });
                });


    this.commentsDialogInstance = new rsqe.Dialog(self.bidCommentsDialog, {width:500, position:'top'});
    this.commentsForm = new rsqe.BidCommentsAndCaveatsDialog({cancelHandler:function () { self.commentsDialogInstance.close(); }});
        this.bcmApproveDiscountLinks.click(function() {
            var currentScope = this;
            var commentsUri = $(this).attr('action');
            $.get(this.getAttribute('action'))
                .success(function () {
                    var quoteOptionName = getQuoteOptionName(currentScope);
                    var successMessage = "Discounts Approved for quote option " + quoteOptionName;
                    $("#commonError").addClass("hidden");
                    self.commentsDialogInstance.setOptions({"title":"Approval Confirmation"});
                                                                     self.loadCommentsAndCaveatsDialog(commentsUri, "Approve", quoteOptionName, "Quote Options");
                                                                     self.commentsDialogInstance.open();
                })
                .error(function (data) {
                    var failureMessage = "Discount approval failure: " + data.responseText;
                    $("#commonError").html(failureMessage).removeClass("hidden");
                    $("#successMessage").addClass("hidden");

                });
        });

		this.deleteQuoteOption.initialise();

        global.basePage.table("#quoteOptionTable");
        self.quoteOptionCount = $.makeArray($("#quoteOptionTable tbody tr")).length;
    },

    buildLaunchQuoteUri:function (expedioQuoteId, Uri) {
        var launchQuoteUri = Uri;
        var index = launchQuoteUri.lastIndexOf('/');
        return launchQuoteUri.substr(0, index + 1) + expedioQuoteId;
    },

    loadCommentsAndCaveatsDialog:function (commentsCaveatsUri, discountRequestStatus, quoteOptionName, calledFromPage) {
            var that = this;
            this.commentsDialogInstance.setOptions({close:function () {
                that.commentsForm.resetForm();
            }});
            this.bidCommentsDialog.load(commentsCaveatsUri, function () {
                that.commentsForm.load(discountRequestStatus, quoteOptionName, calledFromPage);
            });
        },

    loadQuoteOptionDialog: function(quoteOptionId, dialogTitle) {
        var self = this;
        var quoteOptionFormUri = this.quoteOptionDialogUri.text();
        if (quoteOptionId !== null) {
            quoteOptionFormUri += "?quoteOptionId=" + quoteOptionId;
        }
        self.optionDialogInstance.setOptions({"title": dialogTitle,
                                                 close: function() {
                                                     self.quoteOptionForm.resetForm();
                                                 }});
        this.quoteOptionDialog.load(quoteOptionFormUri,
                                    function () {
                                        self.quoteOptionForm.load();
                                        $("#quoteOptionName").focus();
                                    });
    },

    loadNoteDialog: function(quoteOptionId, quoteOptionName) {
        var self = this;
        var noteFormUri = this.noteDialogUri.text() + "?quoteOptionId=" + quoteOptionId;
        this.noteDialogInstance.setOptions({"title": "Notes: " + quoteOptionName,
                                               close: function() {
                                                   self.noteForm.resetForm();
                                               }});
        this.noteDialog.load(noteFormUri,
                             function () {
                                 self.noteForm.load()
                             });
    }
    ,

    setupProgressDialog:function () {
        var that = this;
        that.progressDialogInstance = new rsqe.Dialog($("#progressDialog"), {
            title:"Progress",
            width:"350px",
            closers: $("#progressDialog").find(".ok")
        });
    },
    getItemCount:function() {
        return this.quoteOptionCount;
    }
};

rsqe.BcmRejectDialog = function() {
    this.confirmationDialog = $("#confirmationDialog");
    this.confirmationDialogOkButton = $("#dialogOkButton");
    this.confirmationDialogYesOption = $("#confirmationDialogYesOption");
};

rsqe.BcmRejectDialog.prototype = {

    setupDialog: function() {
        var self = this;

        this.confirmationDialogInstance = new rsqe.Dialog(self.confirmationDialog, {
            title:"Confirmation",
            width:360
        });


        this.confirmationDialogOkButton.click(function () {
            if (self.confirmationDialogYesOption.attr("checked") != "undefined" &&
                self.confirmationDialogYesOption.attr("checked") == "checked") {
                $.get(self.rejectUri)
                    .success(function () {
                        var successMessage = "Discounts requested for quote option " + self.quoteOptionName + " have been rejected.";
                        $("#commonError").addClass("hidden");
                        $("#successMessage").html(successMessage).removeClass("hidden");
                        hideDiscountButtons(self.rejectLink);
                        $(self.pricingTableId).dataTable().fnDraw();
                    })
                    .error(function (data) {
                        var failureMessage = "Discount rejection failure: " + data.responseText;
                        $("#commonError").html(failureMessage).removeClass("hidden");
                        $("#successMessage").addClass("hidden");
                    });

            }
            self.confirmationDialogInstance.close();
        });
    },

    openDialog: function(quoteOptionName, uri, rejectLink) {
        this.quoteOptionName = quoteOptionName;
        this.rejectUri = uri;
        this.rejectLink = rejectLink;
        this.confirmationDialogInstance.open();
    }

}

rsqe.BcmImportDialog = function(progressDialog, progressDialogInstance) {
    this.bcmImportDialog = $("#bcmImportDialog");
    this.bcmTargetIFrame = $("#bcmTarget");
    this.progressDialog = progressDialog;
    this.progressDialogInstance = progressDialogInstance;
}

rsqe.BcmImportDialog.prototype = {


    setupDialog: function () {
        var that = this;

        this.bcmImportDialogInstance = new rsqe.Dialog(this.bcmImportDialog, {width:400});

        this.bcmImportDialogInstance.setOptions({"title": "BCM Import"
                                                });

        var submitButton = this.bcmImportDialog.find(".submit");
        var form = this.bcmImportDialog.find("#bcmImportForm");

        submitButton.click(function () {

            var onLoadEvent = that.bcmTargetIFrame.load(onLoad);
            that.bcmImportDialogInstance.close();
            that.progressDialog.taskStarted("Upload In Progress");
            that.progressDialogInstance.open();

            var success = function (data) {
                var response = $.parseJSON(data);
                if (response.successful) {
                    that.progressDialog.taskFinished("Upload Successful");
                }
                else {

                    that.progressDialog.taskFinishedWithErrors("Upload Unsuccessful\n"+response.errors);
                }

                // Check if thw new or existing DataTable has been initialized. Redraw whichever exists.
                if (rsqe.utility.isDataTableInitialized($('#priceLines'))) {
                    $("#priceLines").dataTable().fnDraw();
                }
                else {
                    $("#priceLinesDetails").dataTable().fnDraw();
                }
            };

            var error = function (data) {
                var errorMessage = "Upload Unsuccessful";
                if (data.responseText !== null && data.responseText !== "") {
                    errorMessage = errorMessage + ":\n" + data.responseText;
                }

                that.progressDialog.taskFinishedWithErrors(errorMessage);
            };

            $(form).ajaxSubmit({
                headers:{'test':'value'},
                                   success:success,
                                   error:error//,
                                   //statusCode:{
                                   //    202:success,
                                   //    500:error
                                   //}
                               });

        });

        var onLoad = function () {
            var response = that.bcmTargetIFrame.text();
            that.bcmTargetIFrame.unbind("load", onLoad);
        };
    },

    openDialog:function (uri, importLink) {
        this.bcmImportDialog.find("#bcmImportForm").attr("action", uri);
        this.bcmImportDialogInstance.open();
        this.callingImportLink = importLink;
    }

}

rsqe.DeleteQuoteOption = function() {
    this.deleteQuoteOptionUri = $('#deleteQuoteOptionUri').text();
    this.deleteButtons = $('.delete');
    this.confirmationDialog = $("#deleteQuoteOptionDialog");
    this.confirmationDialogOkButton = $("#deleteDialogOkButton");
    this.quoteOptionIdToDelete = '';
};

rsqe.DeleteQuoteOption.prototype = {
    initialise: function() {
        var that = this;

        this.confirmationDialogInstance = new rsqe.Dialog(that.confirmationDialog, {
            title:"Delete Quote Option",
            width:360
        });
        this.confirmationDialogOkButton.click(function () {

            $.post(that.deleteQuoteOptionUri + '/' + that.quoteOptionIdToDelete).success(
                function() {
                    $("#commonError").addClass("hidden");
                    $('#id_' + that.quoteOptionIdToDelete).hide();
                    that.confirmationDialogInstance.close();
                }).error(function(data) {
                     var failureMessage = "Error While deleting quote: " + data.responseText;
                     $("#commonError").html(failureMessage).removeClass("hidden");
                });
        });

        that.deleteButtons.each(function() {
            $(this).click(function() {
                that.quoteOptionIdToDelete = $(this).attr('data-id');
                that.confirmationDialogInstance.open();
                return false;
            });
        });
    }
}

