var rsqe = rsqe || {};
rsqe.OrdersTab = function () {
    var rfoImportDialogBox;
    var migrateOkDialogBox;
    var migrationCancelledInfoDialog;
    var progressDialog;
    var orderSubmissionDialog;
    var inProgressStatus = 'In progress';
    var pollDelay = 10000;
    var spinnerHTML = '<img src="/rsqe/project-engine/static/images/cell_validate.gif">';
    var numberOfOrders = 0;
    var confirmationDialogInstance;
    var updateSiteDialogInstance;

    function initialize() {

        updateSiteDialogInstance = new rsqe.Dialog("#updateSiteDialog", {
            title:"Edit Site Details",
            width:"auto",
            height:"auto"
        });

        rfoImportDialogBox = new rsqe.Dialog("#rfoImportDialog", {
            title:"Import RFO Sheet",
            width:400,
            openers:"a.importRFO"
        });

        migrateOkDialogBox = new rsqe.Dialog("#migrationConfirmationDialog", {
            title:"Ceased Existing Inventory?",
            width:400
        });

        migrationCancelledInfoDialog = new rsqe.Dialog("#migrationCancelledInfoDialog", {
            title:"Migration Order Not Submitted",
            width:400,
            closers:$("#migrationCancelledInfoDialog").find(".close")
        });

        progressDialog = new rsqe.Dialog("#progressDialog", {
            title:"Progress",
            width:"350px",
            buttons: {
              "Done": function() {
                location.reload();
              },
            },
        });
        confirmationDialogInstance = new rsqe.Dialog("#confirmationDialog", {
            title:"Delete Order",
            width:360
        });

        orderSubmissionDialog = new rsqe.Dialog("#submitOrderProgressDialog", {
            title:"Initiating Submit Order",
            width:"auto",
            height:"auto"
        });

        $("a.exportRFOSheet").click(function() {
            $("#commonError").addClass("hidden");
            var validationUri = $(this).attr("validateRFOExportUri");
            var href = $(this).attr("href");

            $.get(validationUri)
                .success(function () {
                    window.location.href = href;
                })
                .error(function (data) {
                    $("#commonError").html("RFO Sheet could not be exported. " + (undefined != data.responseText ? data.responseText : '')).removeClass("hidden");
                });

            return false;
        });

        $("a.importRFO").each(function () {
            var uri = $(this).attr("href");
            var currentOrder = $(this).closest('tr.order');
            loadRFOImportDialog(uri, currentOrder);
            $(this).click(function () {
                rfoImportDialogBox.open();
                return false;
            });
        });
        setupEvents();
        numberOfOrders = $.makeArray($("#orders tbody tr")).length;
    }

    function loadRFOImportDialog(uri, currentOrder) {
        $("#rfoImportDialog .submit").click(function () {
            var onLoadEvent = $("#rfoImportDialog").load(onLoad);
            rfoImportDialogBox.close();
            $("#progressText").text("Upload In Progress")
            $("#spinning").show();
            progressDialog.open();

            var success = function (data) {
                var response = $.parseJSON(data);
                if (response.successful == true) {
                    $("#progressText").text("Upload Successful");
                    $("#commonError").addClass("hidden");
                    var submitButton = $(".submitOrder", currentOrder);
                    $(submitButton).removeClass("disabled");
                    $(submitButton).removeAttr("disabled", "disabled");
                }
                else {
                    $("#progressText").text("Upload Unsuccessful");
                    $("#spinning").hide();
                    $("#commonError").html(response.errors).removeClass("hidden");
                    $("#successMessage").addClass("hidden");
                    progressDialog.close();
                }
                $("#spinning").hide();
            };

            var error = function (data) {
                var response = $.parseJSON(data);
                $("#progressText").text("Upload Unsuccessful");
                $("#spinning").empty();
                $("#commonError").html(response.errors).removeClass("hidden");
                $("#successMessage").addClass("hidden");
            };

            $("#rfoImportForm").ajaxSubmit({
                                               url:uri,
                                               success:success,
                                               error:error,
                                               statusCode:{
                                                   202:success,
                                                   500:error
                                               }
                                           });

            var onLoad = function () {
                $("#progressText").text("Uploading ...");
                var response = $("#rfoTarget").text();
                $("#rfoTarget").unbind("load", onLoad);
            };
        });
    }

    function setupEvents() {
        $("tr.order").each(function () {
            var order = $(this);
            var submitButton = $(".submitOrder", order);
            var cancelButton = $(".cancelOrder", order);
            var submitUrl = $(".submitUrl", order).text();
            var statusUrl = $(".statusUrl", order).text();
            var cancelUrl = $(".cancelUrl", order).text();
            var migrationConfirmationDialogOkButton = $("#migrationYesButton");
            var confirmationDialogOkButton = $("#confirmationDialogYesButton");

            var migrationQuote =  $(".migrationQuote", order).text() === 'Yes';

            $("#rfoExport").click(function(){
                        $("#export-rfo-sheet-msg").dialog({
                            modal:true,
                            buttons: {
                                OK: function() {
                                $(this).dialog("close");
                                }
                            }
                        });
                    }),

            migrationConfirmationDialogOkButton.click(function () {
                submitOrder(submitButton, submitUrl, statusUrl, order);
                migrateOkDialogBox.close();
            });

            var migrationConfirmationDialogNoButton = $("#migrationNoButton");
            migrationConfirmationDialogNoButton.click(function () {
                migrationCancelledInfoDialog.open();
                migrateOkDialogBox.close();
            });

            if(migrationQuote) {
                submitButton.disableable('click', function () {
                    migrateOkDialogBox.open();
                });
            } else {
                submitButton.disableable('click', function () {
                    submitOrder(submitButton, submitUrl, statusUrl, order);
                });
            }
             cancelButton.disableable('click', function () {
                    confirmationDialogInstance.open();
                });

            confirmationDialogOkButton.click(function () {
/*               $.post(cancelUrl)
                        .success(function () {
                                     var successMessage = "Order has been successfully deleted";
                                     $("#commonError").addClass("hidden");
                                     $("#successMessage").html(successMessage).removeClass("hidden");
                                 })
                        .error(function (data) {
                                   var failureMessage = "Order Cancel Failure: " + data.responseText;
                                   $("#commonError").html(failureMessage).removeClass("hidden");
                                   $("#successMessage").addClass("hidden");
                               });*/
                $.ajax({
                           type:'POST',
                           url:cancelUrl,
                           dataType:'text'
                       }).then(function () {
                                   window.location.reload(true);
                               });
                $("#orders").dataTable().fnDraw();
                confirmationDialogInstance.close();

            });
            hideRFOAndSubmitButtonBasedOnStatus(order, $(".status", order).text().trim());
            refreshStatus(order);
        });

        $("#orderDetailTab").click(function () {
            $("#sitesDetailList").removeClass("hidden");
            $("#sitesWithError").addClass('hidden');
        });

        $("#sitesErrorDetail").click(function () {
            $("#sitesDetailList").addClass("hidden");
            $("#sitesWithError").removeClass('hidden');
        });

        $("tr.site").each(function () {
            var site = $(this);
            var orderUrl = $("#orderUrl").val();
            var siteForEdit = $("#siteForEdit",site);
            var siteIdVal = $(".siteIdVal",site).text();
            var siteData = $("#siteData");
            var siteCustomerId = $("#siteCustomerId");
            var asciiCharFlag = $("#asciiCharFlag",site).val();
            var siteType = $(".siteType",site).text();
            var uri = orderUrl + "/siteId/" + siteIdVal + "?customerId=" + siteCustomerId.val() + "&siteType=" + siteType;

            if(asciiCharFlag == 'false'){
                $(".submitOrder", site).addClass("disabled");
                $(".submitOrder", site).attr("disabled", "disabled");
                $("#commonSiteError").html('Order(s) below cannot be submitted until the invalid characters corrected in Site Address').addClass("commonError").removeClass("hidden");
                siteForEdit.addClass("table_data_bg_color");
            }
            siteForEdit.click(function () {
             if(asciiCharFlag == 'false'){
                $.ajax({
                          type:"GET",
                          url:uri,
                          dataType:'html',
                          success:function (data) {
                              siteData.html(data);
                              updateSiteDialogInstance.open();
                          },
                          error:function (data) {
                              $("#commonError").html(data.errors).removeClass("hidden");
                          }
                      });
                      }
            });
            $("#siteUpdateButton").click(function () {
                $.ajax({
                           type: "POST",
                           url:orderUrl + "/siteDetail",
                           data:$("#siteUpdateForm").serialize(),
                           success:function (data) {

                               $("#siteDetailUpdateSuccess").html("Site Details Updated.").removeClass("hidden");
                           },
                           error:function (data) {

                               $("#siteDetailUpdateError").html("Site Details could not be Updated. " + (undefined != data.responseText ? data.responseText : '')).removeClass("hidden");
                           }
                       });
            });
        });
    }

    function submitOrder(submitButton, submitUrl, statusUrl, order) {
        $("#successMessage").addClass("hidden");
        $("#commonError").addClass("hidden");
        $(submitButton).addClass("disabled");
        $(submitButton).attr("disabled", "disabled");
        orderSubmissionDialog.open();

        $.ajax({ type:"POST", url:submitUrl })
        $("#successMessage").html('Order ' + $(".name", order).text() + ' submission initiated successfully.').removeClass("hidden");
        $(".status", order).text(inProgressStatus);

        hideRFOAndSubmitButtonBasedOnStatus(order, inProgressStatus);
        //During Submit Order, the order status will be set to In progress,
        // but the immediate polling call will happen based on In progress status, to avoid this race condition, delaying the refresh by 4 Secs.
        setTimeout(function () {refreshStatus(order); }, 4000)
        orderSubmissionDialog.close();
    }

    function hideRFOAndSubmitButtonBasedOnStatus(order, status) {
        if ("Created" !== status) {
            $(".importRFO", order).each(function () {
                $(this).addClass("hidden");
            });
        }
        if (inProgressStatus == status) {
            $(".submitOrder", order).addClass("disabled");
            $(".submitOrder", order).attr("disabled", "disabled");
        }
    }

    function refreshStatus(order) {
        var statusUrl = $(".statusUrl", order).text();
        $.get(statusUrl)
                .success(function (data) {
                             if (inProgressStatus == data) {
                                 $(".status", order).html(spinnerHTML);
                                 setTimeout(function () { refreshStatus(order); }, pollDelay);
                             } else {
                                 $("#successMessage").html('').addClass("hidden");
                                 $(".status", order).text(data);
                             }
                         })
                .error(function () {
                           if ($(".status", order) == spinnerHTML) {
                               $(".status", order).html('ERROR');
                           }
                       });
    }

    return {
        initialise:initialize,
        getItemCount : function() {
            return numberOfOrders;
        }
    };
};
