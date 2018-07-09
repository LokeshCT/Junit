var rsqe = rsqe || {};

rsqe.OfferDetailsTab = function() {

    this.customerId = $('#customerId').html();
    this.contractId = $('#contractId').html();
    this.projectId = $('#projectId').html();
    this.quoteOptionId = $('#quoteOptionId').html();
    this.offerId = $('#offerId').html();
    this.offerDetailsTable = '#offerDetails';
    this.showApproveOffer = $('#showApproveOffer').html();
    this.showRejectOffer = $('#showRejectOffer').html();
    this.showCreateOrder = $('#showCreateOrder').html();
    this.approveOffer = $("#customerApproved");
    this.rejectOffer = $("#customerRejected");
    this.rejectOfferForm = $("#rejectOfferForm");
    this.createOrder = $("#createOrder");
    this.createOrderForm = $("#createOrderForm");
    this.createOrderDialog = $("#createOrderDialog");
    this.submitCreateOrderButton = $("#submitCreateOrder", this.createOrderDialog);
    this.orderName = $("#orderName", this.createOrderDialog);
    this.selectAll = $('.dataTable #selectAll');
    this.dialogError = $("#dialogError", this.createOrderDialog);
    this.staticHeight = 330;
    this.cancelOfferApproval = $("#cancelOfferApproval");
    this.cancelOfferApprovalDialog = $("#cancelOfferApprovalDialog");
    this.cancelOfferApprovalDialogOkButton = $("#cancelDialogOkButton");
    this.showCancelOfferApproval = $('#showCancelOfferApproval').html();


    var checkCallback = function () {
           $("#commonError").text("");
           $("#commonError").addClass("hidden");
           $('#selectAll').attr('title', 'Select None');
           $(':checkbox').each(function(){

                    if(this.checked && this.getAttribute('id')!='selectAll'){
                        var orderStatus = $("#id_" + this.value + " td.status")[0].innerHTML;
                        if (orderStatus === "Order Created" || orderStatus === "Order Submitted") {
                                        submitCreateOrderForm = false;
                                        $('#createOrder').addClass('disabled');

                                    }
                                    }

                        if (this.getAttribute('id')!='selectAll'){
                               var orderStatus = $("#id_" + this.value + " td.status")[0].innerHTML;
                               if (orderStatus === "Order Created" || orderStatus === "Order Submitted") {
                                      $('#cancelOfferApproval').addClass('disabled');
                                      }

                        }

                    });
       };

    var unCheckCallback = function () {
        $("#commonError").text("");
        $("#commonError").addClass("hidden");
        $('#selectAll').attr('title', 'Select All');
        $(':checkbox').each(function(){

         if (this.getAttribute('id')!='selectAll'){
                   var orderStatus = $("#id_" + this.value + " td.status")[0].innerHTML;
                   if (orderStatus === "Order Created" || orderStatus === "Order Submitted") {
                           $('#cancelOfferApproval').addClass('disabled');
                                      }
                    }
         });
    };

    this.offerDetailsItemCheckBoxes = new rsqe.CheckboxGroup("input[name='listOfOfferItems']", {
        actionButtons: $("#createOrder,#cancelOfferApproval"),
        select_all:"#offerDetails_wrapper #selectAll",
        someChecked: checkCallback,
        allUnchecked: unCheckCallback
    });

    this.columnMetaData = [
        { "mDataProp": "id"},
        { "mDataProp": "site"},
        { "mDataProp": "miniAddress"},
        { "mDataProp": "product"},
        { "mDataProp": "summary"},
        { "mDataProp": "status"},
        { "mDataProp": "discountStatus"},
        { "mDataProp": "pricingStatus" },
        { "mDataProp": "validity"}
    ];
};

rsqe.OfferDetailsTab.prototype = {
    initialise: function() {
        this.setupTable();
        this.setupCreateOrderDialog();
        this.setupCancelOfferApprovalDialog();
        this.setupEvents();
        new rsqe.LineItemValidation(this.offerDetailsItemCheckBoxes).initialize();
        return this;
    },

    destroy: function() {
        $("div#" + this.createOfferDialog.attr("id") + ".dialog").remove();
    },

    setupTable : function() {
        var that = this;
        $(this.offerDetailsTable).dataTable({
                                                sPaginationType: "full_numbers",
                                                "sDom": 'lrt<"table_footer"ip>',
                                                bProcessing: true,
                                                bServerSide: true,
                                                bSort: false,
                                                bLengthChange : true,
                                                aLengthMenu: [
                                                    [10, 25, 50, 100, -1],
                                                    [10, 25, 50, 100, "All"]
                                                ],
                                                iDisplayLength: 100,
                                                sAjaxSource: "/rsqe/customers/" + this.customerId + "/contracts/" + this.contractId + "/projects/" + this.projectId + "/quote-options/" + this.quoteOptionId + "/offers/" + this.offerId + "/offer-details",
                                                aoColumns: this.columnMetaData,
                                                sAjaxDataProp: function(data) {return $.makeArray(data.itemDTOs);},
                                                "sScrollY": ($(window).height() - that.staticHeight) + "px",
                                                bStateSave: false,
                                                fnInitComplete:function () {
                                                    that.validateLineItems();
                                                },
                                                fnRowCallback: function(row, aData) {
                                                    return that.fnRowCallback(row, aData);
                                                },
                                                fnDrawCallback: function(settings) {
                                                    $(window).unbind('resize').bind('resize', function () {
                                                        that.applyTableHeight();
                                                    });
                                                    that.renderMessageRows(settings);
                                                    that.offerDetailsItemCheckBoxes.initialize();
                                                }
                                            });
    },

    validateLineItems:function () {
        this.offerDetailsItemCheckBoxes.all_elements().each(function () {

            var lineItemId = $(this).val();
            var loadIcon_fff = $("<img>").attr("src", "/rsqe/project-engine/static/images/cell_validate.gif");
            var loadIcon_blue = $("<img>").attr("src", "/rsqe/project-engine/static/images/cell_validate_blue.gif");

            var row = $("#id_" + lineItemId);
            var statusSelector;
            if(row.find("td.validity").hasClass("warning")) {
                statusSelector = row.find("td.validity").addClass("validating").removeClass("warning");
            } else {
                statusSelector = row.find("td.validity").addClass("validating").removeClass("invalid");
            }

            if (row.hasClass("odd")) {
                statusSelector.html(loadIcon_fff);
            } else {
                statusSelector.html(loadIcon_blue);
            }
            var linkUrl = $("#validate").text();

            function updateValidity(lineItemId, text, errors) {
                var row = $("#id_" + lineItemId);
                row.find(".validity").text(text);

                new rsqe.optiondetails.DataTable().updateErrorRow(row, errors);
            }

            $.post(linkUrl.replace("(id)", lineItemId)).success(
                function (response) {
                    updateValidity(lineItemId, response.status, $.makeArray(response.descriptions));
                    statusSelector.removeClass("validating");
                }).error(function () {
                             updateValidity(lineItemId, "Could not validate", ["Try again?"]);
                             statusSelector.removeClass("validating");
                         });

        });
    },

    renderMessageRows:function (settings) {
        var tableRows = $('#offerDetails tbody tr');
        if (!$(tableRows[0].getElementsByTagName('td')[0]).hasClass("dataTables_empty")) {
            for (var i = 0; i < tableRows.length; i++) {
                var currentRow = settings.aoData[ settings.aiDisplay[i] ]._aData;
                new rsqe.optiondetails.DataTable().updateErrorRow($(tableRows[i]), currentRow.errorMessage);
            }
        }
    },

    applyTableHeight:function() {
        var sScrollY = ($(window).height() - this.staticHeight) + "px";
        $(".dataTables_scrollBody").css("height", sScrollY);
        this.collapseRightTab();
    },

    calculateTableWidth: function () {
        $(this.offerDetailsTable).dataTable().fnAdjustColumnSizing(false);
    },


    setupCreateOrderDialog: function() {
        var that = this;
        new rsqe.Dialog(this.createOrderDialog,
                        {
                            title:'Create Order',
                            width:360,
                            openers: that.createOrder,
                            validator: function() {return that.createOrderItemCommaDelimitedList()}

                        });
    },

    setupCancelOfferApprovalDialog: function() {
        var that = this;
        new rsqe.Dialog(this.cancelOfferApprovalDialog,
                        {
                            title:'Cancel Approval',
                            width:360,
                            openers: that.cancelOfferApproval
                        });
    },

    fnRowCallback: function(row, aData) {
        var that = this;
        $.each($("td", row), function(i, td) {
            var columnName = that.columnMetaData[i].mDataProp;
            if (columnName === "id") {
                var enableDisableFlag="";
                if(aData.isQuoteOnlyLeadToCashPhase){
                    enableDisableFlag="disabled";
                }
                $(td).html("<input type='checkbox' name='listOfOfferItems' value='" + aData.id + "'"+enableDisableFlag+"/>");
                $(td).addClass("checkbox");
            } else {
                $(td).addClass(columnName);
            }
        });
        $(row).addClass("offerItem");
        if (rsqe.jsonSafeBooleanCheck(aData.forIfc)) $(row).addClass("ifc-item");
        $(row).attr("id", "id_" + aData.id);
        return row;
    },

    setupEvents : function() {
        var that = this;
        if (this.showCreateOrder) {
            this.createOrder.click(function() {
                that.dialogError.text("");
                that.orderName.val("");
            });
    $("#exportPricingSheet").click(function(){
           // $("#exportPricingSheet").addClass("disabled");
            $("#export-pricing-sheet-msg").dialog({
                modal:true,
                buttons: {
                    OK: function() {
                    $(this).dialog("close");
                    }
                }
            });
        }),


            this.submitCreateOrderButton.click(function() {
                if (that.validateOrderDialog(that)) {
                    $('#orderLoadingMessage').show();
                    disableButtons($('#createOrderDialog'));
                    var currentLocation = window.location.href;
                    var orderNameText = that.orderName.val();
                    var selectedOfferItemIds = $("#offerItemIds").val();

                    $.ajax({
                        type : 'POST',
                        url : that.createOrderForm.attr('action'),
                        dataType:'text',
                        data: {orderName: orderNameText,offerItemIds:selectedOfferItemIds}
                    }).then(function() {
                                var reloadUrl = "/rsqe/customers/" + $('#customerId').html() + "/contracts/" + $('#contractId').html() + "/projects/" + $('#projectId').html() + "/quote-options/" + $('#quoteOptionId').html() + "#OrdersTab";
                                window.location = reloadUrl;
                            },
                            function(response) {
                                $('#orderLoadingMessage').hide();
                                enableButtons($('#createOrderDialog'));
                                {
                                    $("#commonError").text(response.responseText);
                                    $("#commonError").removeClass("hidden");
                                }
                            });
                }
            });
        }

        if (this.showApproveOffer) {
            this.approveOffer.click(function() {
                                        if (that.validateApproveOffer()) {
                                            $("#commonError").addClass("hidden");
                                            $("#commonError").text("");
                                            var currentLocation = window.location.href;
                                            $.post(this.getAttribute('action')).success(
                                                function() {
                                                    window.location = currentLocation;
                                                    window.location.reload(true);
                                                }).error(function(response) {
                                                             $("#commonError").text(response.responseText);
                                                             $("#commonError").removeClass("hidden");
                                                         });
                                        }
                                    }
            );
        }

        if (this.showRejectOffer) {
            this.rejectOffer.click(function() {
                that.rejectOfferForm.submit();
            });
        }

        if (this.showCancelOfferApproval) {
            this.cancelOfferApprovalDialogOkButton.click(function () {
                $.ajax({
                           type:'GET',
                           url:$("#cancelOfferApprovalUri").text(),
                           dataType:'text'
                       }).then(function () {
                                   window.location.reload(true);
                               });
            });
        }
    },

    validateApproveOffer: function() {
        var that = this;
        $("#commonError").text("");
        var isValid = true;
        $(':checkbox', $(this.offerDetailsTable)).each(function() {
            var pricingStatus = $("#id_" + this.value + " td.pricingStatus")[0].innerHTML;
            var discountStatus = $("#id_" + this.value + " td.discountStatus")[0].innerHTML;
            var validity = $("#id_" + this.value + " td.validity")[0].innerHTML;

            function pricingStatusValid(pricingStatus) {
                return pricingStatus == 'Firm' || pricingStatus == 'No Price' || pricingStatus == 'N/A';
            }

            function discountStatusValid(discountStatus) {
                return discountStatus == 'N/A' || discountStatus == 'Approved';
            }

            function valid(validity) {
                return validity == 'VALID' || validity == 'WARNING';
            }

            if ((!pricingStatusValid(pricingStatus) || !discountStatusValid(discountStatus)) ||
                !valid(validity)) {
                $("#commonError").text("Some of the items selected may not be valid or have firm prices").removeClass('hidden');
                    isValid = false;
            }
        });
        return isValid;
    }
    ,

    createOrderItemCommaDelimitedList : function() {
        var that = this;
        var offerIds = "";
        var submitCreateOrderForm = true;
        $("#commonError").text("");
        $(':checkbox', $(this.offerDetailsTable)).each(function() {
            if (this.checked) {
                var orderStatus = $("#id_" + this.value + " td.status")[0].innerHTML;
                if (orderStatus === "Order Created" || orderStatus === "Order Submitted") {
                    $("#commonError").text("Some of the selected items have already been ordered").removeClass('hidden');
                    submitCreateOrderForm = false;
                }
                var priceStatus = $("#id_" + this.value + " td.pricingStatus")[0].innerHTML;
                if (priceStatus === "ICB Budgetary") {
                    $("#commonError").text("Order cannot be created as one of the configuration[s] pricing statuses is still Budgetary.Please proceed with firm prices to create order.").removeClass('hidden');
                    submitCreateOrderForm = false;
                }
                offerIds += this.value + ",";
            }
        });

        if (offerIds.length > 0) {
            offerIds = offerIds.substring(0, offerIds.length - 1);
        }

        $("#offerItemIds").attr("value", offerIds);
        return submitCreateOrderForm;
    }
    ,

    validateOrderDialog: function (that) {
        var result = true;
        var trimmedOrderName = $.trim(that.orderName.attr("value"));
        if (trimmedOrderName.length > 0) {
            if (trimmedOrderName.length > 50) {
                that.dialogError.text("Order name can not exceed 50 characters");
                result = false;
            }
        } else {
            that.dialogError.text("Order name is a mandatory field");
            result = false;
        }
        return result;
    }
};
