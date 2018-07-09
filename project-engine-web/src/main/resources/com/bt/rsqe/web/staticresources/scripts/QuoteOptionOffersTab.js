var rsqe = rsqe || {};

rsqe.QuoteOptionOffersTab = function() {

    this.approveOfferButtons = $("#offers .approve");
    this.numberOfOffers = 0;
    this.cancelOfferApproval = $("#cancelApproval");
    this.cancelOfferApprovalDialog = $("#cancelOfferApprovalDialog");
    this.cancelOfferApprovalDialogOkButton = $("#cancelDialogOkButton");

};

rsqe.QuoteOptionOffersTab.prototype = {
    initialise: function() {
        var self = this;
        global.basePage.table("#offers");
        this.setupEvents();
        self.numberOfOffers = $.makeArray($("#offers tbody tr")).length;
        self.cancelOfferApprovalUri = $("#cancelOfferApprovalUri");
        self.cancelOfferApprovalDialogInstance = new rsqe.Dialog(self.cancelOfferApprovalDialog, {title:"Cancel Approval", width:360});
        this.cancelOfferApproval.click(function () {
            self.cancelOfferApprovalDialogInstance.open();
        })
        this.cancelOfferApprovalDialogOkButton.click(function () {
            $.ajax({
                       type:'GET',
                       url:self.cancelOfferApprovalUri.val(),
                       dataType:'text'
                   }).then(function () {
                               window.location.reload(true);
                           });
        })
        return this;
    },

    destroy: function() {
    },

    setupEvents: function() {
        this.approveOfferButtons.click(function() {
            if($(this).hasClass("disabled")) {
                return false;
            }

            $(this).addClass("disabled");
            $('#loadingMessage').text("Approving Offer...");
            $('#loadingMessage').show();

            $("#offersContainer #commonError");

            $("#offersContainer #commonError").addClass("hidden");
            $("#offersContainer #commonError").text("");
            var offerName = $(".name", this.parentElement.parentElement)[0].innerHTML;
            var currentLocation = window.location.href;
            var uri = this.getAttribute('action');

            $.ajax({
                type : 'POST',
                url : uri,
                dataType:'text'
            }).then(function() {
                        window.location = currentLocation;
                        window.location.reload(true);
                    },
                    function(response) {
                        $(this).removeClass("disabled");
                        $('#loadingMessage').hide();
                        $("#offersContainer #commonError").text("Offer " + offerName + " cannot be approved because " + response.responseText);
                        $("#offersContainer #commonError").removeClass("hidden");
                    });

            return false;
        })
    },
    getItemCount:function() {
        return this.numberOfOffers;
    }


};
