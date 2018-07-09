var rsqe = rsqe || {};

rsqe.BidCommentsAndCaveatsDialog = function (opts) {
    if (opts == null || opts.cancelHandler == null) throw "cancelHandler should be passed in";
    this.cancelHandler = opts.cancelHandler;
    this.cancelCommentButtonId = ".cancel";
    this.saveCommentsaddCaveats = ".submit";
    this.commentsAndCaveatsFormId = "#commentsAndCaveatsForm";
    this.newCommentId = "#newComment";
};

rsqe.BidCommentsAndCaveatsDialog.prototype = {

    load:function (discountRequestStatus, quoteOptionName, calledFromPage) {
        var self = this;
        self.approveDiscountUri = $("#approveDiscountUri");
        self.rejectDiscountUri = $("#rejectDiscountUri");
        $("#commentsDiv").scrollTop(1000);

        if (discountRequestStatus === "Reject") {
            $("#bidCaveats").hide();
            self.commentsDialogInstance = new rsqe.Dialog(self.bidCommentsDialog, {width:300, position:'top'});
        }

        self.validator = $(this.commentsAndCaveatsFormId).validate(
                {
                    rules:{
                        newComment:{required:true, maxlength:900},
                        newCaveat:{required:true, maxlength:900}
                    },

                    messages:{
                        newComment:{
                            required:"Please enter a comment to save.",
                            maxlength:"You have reached the maximum (900) number of characters allowed for this field."
                        },
                        newCaveat:{
                            required:"Please enter a caveat to save.",
                            maxlength:"You have reached the maximum (900) number of characters allowed for this field."
                        }
                    }
                }
        );
        $(self.saveCommentsaddCaveats).click(function () {
            var form = $(self.commentsAndCaveatsFormId);
            if (self.validator.form()) {

                // DISCOUNT APPROVAL
                if (discountRequestStatus === "Approve") {
                    $.post(self.approveDiscountUri.val(), form.serialize()).success(function () {
                        var successMessage = "Discounts Approved for quote option " + quoteOptionName;
                        self.renderSuccessMessage(successMessage);
                            if (calledFromPage === "Pricing") {
                                self.refreshPricingTab();
                            }
                            else if (calledFromPage === "Quote Options") {
                                self.refreshQuoteOptionsTab();
                            }
                            else {
                                var failureMessage = "The discount has been successfully approved but the web page could not be refreshed automatically to reflect these changes.  Please refresh your browser to view changes.";
                                                                 self.renderFailureMessage(failureMessage);
                            }
                        self.cancelHandler();
                    }).error(function (data) {
                                 var failureMessage = "Discount approval failure: " + data.responseText;
                                 self.renderFailureMessage(failureMessage);
                             });
                }

                // DISCOUNT REJECTION
                else if (discountRequestStatus === "Reject") {
                    $.post(self.rejectDiscountUri.val(), form.serialize()).success(function () {
                        var successMessage = "Discounts requested for quote option " + quoteOptionName + " have been rejected.";
                        self.renderSuccessMessage(successMessage);
                         if (calledFromPage === "Pricing") {
                             self.refreshPricingTab();
                         }
                         else if (calledFromPage === "Quote Options") {
                             self.refreshQuoteOptionsTab();
                         }
                         else {
                             var failureMessage = "The discount has been successfully rejected but the web page could not be refreshed automatically to reflect these changes.  Please refresh your browser to view changes.";
                                                              self.renderFailureMessage(failureMessage);
                         }
                        self.cancelHandler();
                    }).error(function (data) {
                                 var failureMessage = "Discount rejection failure: " + data.responseText;
                                 self.renderFailureMessage(failureMessage);
                             });
                }

                // ERROR HANDLING FOR UNKNOWN STATE
                else {
                    var errorMessage = "Discount application failure: unable to determine whether this application was to accepted or rejected";
                    self.renderFailureMessage(errorMessage);
                    self.cancelHandler();
                }
            }
        });

        $(self.commentsAndCaveatsFormId).submit(function () {
            return false;
        });
        $(self.cancelCommentButtonId).click(function () {
            self.cancelHandler();
        });
        $(this.newCommentId).focus();

    },
    resetForm:function () {
        $(this.commentsAndCaveatsFormId).each(function () {
            this.reset();
        });
        this.validator.resetForm();
    },
    refreshPricingTab:function () {

        // Handle a redraw of the existing pricing tab.
        if ( rsqe.utility.isDataTableInitialized($('#priceLines'))) {
            $("#priceLines").dataTable().fnDraw();
        }

        // Handle a redraw of the new pricing tab.
        else {
            $("#priceLinesDetails").dataTable().fnDraw();
        }

        $("#discountApprove").hide();
        $("#discountReject").hide();
        $("#bcmImport").hide();

    },

    refreshQuoteOptionsTab:function () {
           location.reload();
        },
    renderSuccessMessage:function (successMessage) {
        $("#commonError").addClass("hidden");
        $("#successMessage").html(successMessage).removeClass("hidden");
    },
    renderFailureMessage:function (failureMessage) {
        $("#commonError").html(failureMessage).removeClass("hidden");
        $("#successMessage").addClass("hidden");
    }
};
