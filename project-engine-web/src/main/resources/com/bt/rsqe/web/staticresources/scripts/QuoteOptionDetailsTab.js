var rsqe = rsqe || {};

rsqe.QuoteOptionDetailsTab = function () {
    var that = this;

    this.validationAjaxRequests = [];
    this.serviceLevelAgreementDialog = $("#serviceLevelAgreementDialog");
    this.maintainerAgreementDialog = $("#maintainerAgreementDialog");
    this.contractDialog = $("#contractDialog");
    this.attachemtDialog = $("#attachmentDialog");
    this.applyFilterButton = $("#applyFilterButton");
    this.hideFailedLineItemsCheckbox = $("#hideFailedLineItemsCheckbox");
    this.importProductButtonSelector = "#importProduct";
    this.importProductButton = $("#importProduct");
    this.bulkConfigurationButton = $("#bulkConfigurationButton");
    this.updateProductConfig = $("#updateProductConfig");
    this.submitForCopyButton = $("#submitQuoteOptionsCopy");
    this.createOfferDialog = $("#createOfferDialog");
    this.addProductButton = $("#newLineItem");
    this.confirmationDialog = $("#confirmationDialog");
    this.confirmationDialogOkButton = $("#dialogOkButton");
    this.confirmationDialogYesOption = $("#confirmationDialogYesOption");
    this.updateProductConfigDialog = $("#updateProductConfigDialog");
    this.copyOptionsDialog = $("#copyOptionsDialog");
    this.bulkTemplateButton = $("#downloadBulkTemplate");
    this.importProductDialog = $("#importProductDialog");
    this.createOfferForm = $("#createOfferForm");
    this.lineItemsTable = $("#lineItems");
    this.offerNameText = $("#offerNameText", this.createOfferDialog);
    this.submitOfferButton = $("#submitOffer", this.createOfferDialog);
    this.importProductTargetIFrame = $("#importProductTarget");
    this.importProductForm = $("#importProductForm");
    this.progressDialogInstance = $('#progressDialog');
    this.progressDialogContainer = $("#progressDialog");
    this.errorMessages = $("#errorMessages", this.progressDialogContainer);
    this.quoteOptionId = $('#quoteOptionId').val();
    this.projectId = $('#projectId').val();
    this.maxLineItems = parseInt($('#maxLineItems').val());
    this.removeLineItemEnabled = "true" == $('#removeLineItemEnabled').val();
    this.revenueOwner = $('#revenueOwner').val();
    this.customerId = $('#customerId').val();
    this.contractId = $('#contractId').val();
    this.quoteOptionContext = $('#importProductDialog #quoteOptionContext');
    this.productConfigForm = $('#productConfigForm');
    this.selectAll = $('#lineItems #selectAll');
    this.raiseIfcs = $('#raiseIfcs');
    this.importProductFormFileInput = '#eCRFSheet';
    this.importProductFormFileName = '#eCRFSheetWorkBookName';
    this.validateImportProductUrl = $('#validateImportProductUrl');
    this.fetchPricesId = "#fetchPrices";
    this.staticHeight = 291;
    this.locateOnGoogleMapsButton = $('#locateOnGoogleMaps');
    this.locateOnGoogleMapsWindowObjectReference = null;
    this.locateOnGoogleMapsButton.click(function() {
        openLocateOnGoogleMapsPopUp(this);
    });

    this.noteLinks = $("a[rel='note']");
    this.noteDialog = $("#notesDialog");
    this.noteDialogUri = $("#notesDialogUri");
    that.importProducts = new rsqe.ImportProduct();

    this.successMessage = new rsqe.StatusMessage("#successMessage");
    this.commonError = new rsqe.StatusMessage("#commonError");
    //this.viewConfigurationLink = $("#viewConfigurationLink");
    this.userInfo = new rsqe.StatusMessage("#userInfo");


    this.bulkTemplateDialog = new rsqe.BulkTemplateDialog({container:$("#bulkTemplateDialog"), uri:$('#bulkTemplateUri').text()});
    this.linkItemCheckBoxes = new rsqe.CheckboxGroup("input:not([disabled])[name='listOfQuoteOptionItems']", {
        actionButtons:$("#createOffer , #copyOptions , #raiseIfcs, #fetchPrices"),
        select_all:"#selectAll",
        someChecked:function () {
            that.commonError.hide();
            $('#selectAll').attr('title', 'Select None');
            var selectedLineItem = that.commaSeparatedListOfSelectedLineItemIds(that);
            that.importProducts.enableImportProductButton(selectedLineItem.split(','));
        },
        allUnchecked:function () {
            that.commonError.hide();
            $('#selectAll').attr('title', 'Select All');
            $('#importProduct').addClass("hidden");
            $('#importProduct').hide();
        }
    });

    this.progressDialog = new rsqe.ProgressDialog("#progressDialog", {
        progressText:"#progressText",
        progressButton:"#close"
    });


    this.columnMetaData = [
        { "mDataProp":"id", "bSortable": false},
        { "mDataProp":"siteName"},
        { "mDataProp":"miniAddress"},
        { "mDataProp":"subLocationName"},
        { "mDataProp":"room"},
        { "mDataProp":"floor"},
        { "mDataProp":"name"},
        { "mDataProp":"summary"},
        { "mDataProp":"action"},
        { "mDataProp":"contractTerm"},
        { "mDataProp":"offerName"},
        { "mDataProp":"status"},
        { "mDataProp":"discountStatus"},
        { "mDataProp":"pricingStatus"},
        { "mDataProp":"orderStatus"},
        { "mDataProp":"validity", "bSortable": false},
        { "mDataProp":"serviceLevelAgreement","bSortable": false},
        { "mDataProp":"maintainerAgreement","bSortable": false},
        { "mDataProp":"remainingContractTerm"},
        { "mDataProp":"configurable", "bSortable": false}
    ];

    this.itemCount = 0;
    this.totalItemCount = 0;
    this.isValidProxyAsset = false;
    this.aData = [];

    function openLocateOnGoogleMapsPopUp(element) {
        if ($(element).attr('class').indexOf('disabled') != -1) {
            return;
        }
        if (this.locateOnGoogleMapsWindowObjectReference == null || this.locateOnGoogleMapsWindowObjectReference.closed) {
            //IE Issue with popup means that a blank string must always be passed as the popup name
            this.locateOnGoogleMapsWindowObjectReference = window.open($('#locateOnGoogleMapsUrl').html(),
                                                "", "toolbar=no,scrollbars=no,resizable=no,height=700,location=no");
        } else {
            self.locateOnGoogleMapsWindowObjectReference.focus();
        }
    }

    that.productPricingService = new rsqe.ProductPricing();
    that.productPricingService.setErrorCallBack(function(response) {
        var errorMessage = "An error occurred during pricing - " + response.responseText;
         that.commonError.show(errorMessage);
    })
};

rsqe.QuoteOptionDetailsTab.prototype = {

    initialise:function () {
        var self = this;
        this.setupTable();
        this.setupLineItemDialog();
        this.setupCopyOptionsDialog();
        this.setupImportProductDialog();
        this.setupProgressDialog();
        this.setupEvents();
        this.setupQuoteOptionContext();
        this.setUpUpdateProductConfigurationDialog();
        this.setupRaiseIfcDialog();
        new rsqe.LineItemValidation(this.linkItemCheckBoxes).initialize();
        this.contractDialogInstance = new rsqe.Dialog(this.contractDialog, {width:400});
        this.contractForm = new rsqe.ContractForm({cancelHandler:function () { self.contractDialogInstance.close(); }});
        this.attachmentDialogInstance = new rsqe.Dialog(this.attachemtDialog, {width:800});
        this.attachmentForm = new rsqe.AttachmentForm({cancelHandler:function () { self.attachmentDialogInstance.close(); }});
        this.setupAttachmentDialog();

        this.noteDialogInstance = new rsqe.Dialog(self.noteDialog, {width:400,position: 'top'});
        this.noteForm = new rsqe.NotesForm({cancelHandler: function() { self.noteDialogInstance.close(); }, maxLength:1023});

        this.serviceLevelAgreementDialogInstance = new rsqe.Dialog(this.serviceLevelAgreementDialog, {width:800, position: [130,70]});
        this.serviceLevelAgreementForm = new rsqe.ServiceLevelAgreementForm({cancelHandler:function () { self.serviceLevelAgreementDialogInstance.close(); }});


        this.maintainerAgreementDialogInstance = new rsqe.Dialog(this.maintainerAgreementDialog, {width:800,position: [130,70]});
        this.maintainerAgreementForm = new rsqe.MaintainerAgreementForm({cancelHandler:function () { self.maintainerAgreementDialogInstance.close(); }});

        var uri = $("#viewConfigurationDialogUri")[0];
        var quoteOptionFormUri = "";

        if (typeof uri != "undefined") {
            quoteOptionFormUri =  uri.innerText;
        }
        if (this.quoteOptionId !== null) {
            quoteOptionFormUri += "?quoteOptionId=" + this.quoteOptionId;
        }
        $("#viewConfigurationLink").click(function() {
            window.open( quoteOptionFormUri ,"View Configuration","scrollbar=1,resizable=1,width=1000,height=500");
        });

        return this;
    },

    destroy:function () {
        // Not sure why this works, tried dialog.dialog("destroy").remove() and it's not deleting out of DOM
        // Toy + Sumit
        var removeDialog = function (dialog) {
            $("div#" + dialog.attr("id") + ".dialog").remove();
        };

        var removeProgressDialog = function (dialog) {
            $("div#" + dialog.attr("id") + ".progressDialog").remove();
        };

        removeDialog(this.createOfferDialog);
        removeDialog(this.copyOptionsDialog);
        removeDialog(this.importProductDialog);
        removeProgressDialog(this.progressDialogContainer);
        removeDialog(this.updateProductConfigDialog);
        this.bulkTemplateDialog.destroy(removeDialog);

        $("a.main-action[href='#']").unbind();//remove all event handlers for all action links
        $(this.applyFilterButton).unbind("click");
    },

    setupRaiseIfcDialog:function () {
        var that = this;
        this.confirmationDialogInstance = new rsqe.Dialog(that.confirmationDialog, {
            title:"Confirmation",
            width:360,
            openers:that.raiseIfcs
        });
    },

    setupTable:function () {
        var that = this;
        $(this.lineItemsTable).dataTable({
                                             sPaginationType:"full_numbers",
                                             "sDom":'lrt<"table_footer"ip>',
                                             bAutoWidth:true,
                                             bProcessing:true,
                                             bServerSide:true,
                                             bSort:true,
                                             "bRetrieve":true,
                                             bSortClasses:false,
                                             bLengthChange:true,
                                             aLengthMenu: [
                                                [10, 25, 50, 100, -1],
                                                [10, 25, 50, 100, "All"]
                                             ],
                                             iDisplayLength:10,
                                             sAjaxSource:$("#lineItemsUrl").text(),
                                             aoColumns:this.columnMetaData,
                                             sAjaxDataProp:function (data) {
                                                 that.fnCheckActions(data.isDiscountRequested);
                                                 var lineItems = $.makeArray(data.lineItems);
                                                 that.itemCount = lineItems.length;
                                                 that.totalItemCount = data.iTotalRecords;
                                                 if(that.totalItemCount == 0) {
                                                     $("#bulkConfigurationButton").addClass("disabled");
                                                     $("#bulkConfigurationButton").attr("disabled", true);
                                                 }

                                                 if(data.userInfo != '') {
                                                   that.userInfo.show(data.userInfo);
                                                 }

                                                 return lineItems;
                                             },
                                             "sScrollY":($(window).height() - that.staticHeight) + "px",
                                             bStateSave:false,
                                             bFilter:true,
                                             fnRowCallback:function (row, aData) {
                                                 return that.fnRowCallback(row, aData);
                                             },
                                             fnInitComplete:function () {
                                                 that.fnInitComplete();
                                             },
                                             fnPreDrawCallback:function() {
                                                 that.abortPendingValidationRequests();
                                             },
                                             fnDrawCallback:function (settings) {
                                                 that.renderMessageRows(settings);//todo: Do we need to add these upfront? can this be added onclick()
                                                 that.linkItemCheckBoxes.initialize();
                                                 $(window).unbind('resize').bind('resize', function () {
                                                     that.applyTableHeight();
                                                 });
                                                 that.validateLineItems();
                                             },
                                             "fnServerParams":function (aoData) {
                                                 /*
                                                  This next line is a tactical solution to the issue where requests of exactly 1536 bytes fail.
                                                  It will break filtering so if this becomes a requirement we'll have to revisit this.
                                                  TO-DO: This needs a better fix, as if the request parameter needed is one that is chopped off
                                                  then the value below will need to be increased.
                                                  */
                                                 //aoData.splice(20, aoData.length - 20);
                                                 // (martin) we need to send all params for sorting to the back-end and splice send only few 21 params. Not sure what error previous devs been getting
                                                 aoData.push({ "name": "globalSearch", "value": $('#globalSearch').val() } );
                                             },
                                             "fnServerData": function ( sSource, aoData, fnCallback, oSettings ) {
                                                 handleDataTableError(sSource, aoData, fnCallback, oSettings, function(xhr, textStatus, error) {
                                                    that.commonError.show("Error loading line items. " + error + " " + xhr.responseText);
                                                    $("#lineItems_processing").hide();
                                                 });
                                             }
                                         });

        $('#globalSearchBtn').click(function() {
            $(that.lineItemsTable).DataTable().fnDraw();
        });
    },

    validateLineItems:function (element) {
        var that = this;
        var elementList = element !== undefined ? element : this.linkItemCheckBoxes.all_elements();
        that.validationAjaxRequests = [];
        elementList.each(function () {

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
                if(text=='VALID'){
                    that.isValidProxyAsset = true;
                    if(that.aData.isProxyProduct){
                        that.fnRowCallback(row, that.aData);
                    }
                }

                new rsqe.optiondetails.DataTable().updateErrorRow(row, errors);
            }

            that.validationAjaxRequests.push($.ajax({
                type : 'POST',
                url : linkUrl.replace("(id)", lineItemId),
                dataType:'text'
            }).then(function (response) {
                        var responseJson = $.parseJSON(response);
                        updateValidity(lineItemId, responseJson.status, $.makeArray(responseJson.descriptions));
                        statusSelector.removeClass("validating");
  /*                      that.loadServiceLevelAgreement(row,lineItemId);
                        that.loadMaintainerAgreement(row,lineItemId);
*/                    },
                    function () {
                         updateValidity(lineItemId, "Could not validate", ["Try again?"]);
                         statusSelector.removeClass("validating");
                    }));
        });
    },

    loadServiceLevelAgreement: function (lineItemId) {
        var that = this;

        var lineItemIds = that.commaSeparatedListOfSelectedLineItemIds(that) === "" ? null : that.commaSeparatedListOfSelectedLineItemIds(that);

        var validationUri = $("#lineItemsUrl").text() + "/" + lineItemId + "/"
                            + "lineItem-array" + "/" + lineItemIds + "/productAgreements/serviceLevelAgreementValidation";

        var serviceLevelAgreementUrl = $("#lineItemsUrl").text() + "/" + lineItemId + "/"
                                       + "lineItem-array" + "/" + lineItemIds + "/productAgreements/serviceLevelAgreementForm";

        $.ajax({
                   type: "GET",
                   url: validationUri,
                   success: function (data) {
                       $('#loadingMessage').hide();
                       if (data == "") {
                           that.serviceLevelAgreement(serviceLevelAgreementUrl)
                       } else {
                           jAlert(data, "Service Level Agreement");
                       }
                   },
                   error: function (data) {
                       $('#loadingMessage').hide();
                   }
               });



    },serviceLevelAgreement:function(serviceLevelAgreementUrl){
        var that=this;

        that.serviceLevelAgreementDialog.load(serviceLevelAgreementUrl,
                                              function () {
                                                  that.serviceLevelAgreementForm.load();
                                              });
        that.serviceLevelAgreementDialogInstance.setOptions({
                                                                "title": "Service Level Agreements(SLA)",
                                                                close: function () {
                                                                    that.serviceLevelAgreementForm.resetForm();
                                                                }
                                                            });


        that.serviceLevelAgreementDialogInstance.open();

    },

    maintainerAgreement: function (lineItemId) {
        var that = this;

        var lineItemIds = that.commaSeparatedListOfSelectedLineItemIds(that) === "" ? null : that.commaSeparatedListOfSelectedLineItemIds(that);
        var maintainerAgreementUrl = $("#lineItemsUrl").text() + "/" + lineItemId + "/"
                                     + "lineItem-array" + "/" + lineItemIds + "/productAgreements/maintainerAgreementForm";

        that.maintainerAgreementDialog.load(maintainerAgreementUrl,
                                            function () {
                                                that.maintainerAgreementForm.load();
                                            });
        that.maintainerAgreementDialogInstance.setOptions({
                                                              "title": "Maintainer Agreements(MAG)",
                                                              close: function () {
                                                                  that.maintainerAgreementForm.resetForm();
                                                              }
                                                          });


        that.maintainerAgreementDialogInstance.open();


    },
    applyTableHeight:function () {
        var sScrollY = ($(window).height() - this.staticHeight) + "px";
        $(".dataTables_scrollBody").css("height", sScrollY);
        this.calculateTableWidth();
    },

    fnCheckActions:function (isDiscountRequested) {
        var that = this;
        if (isDiscountRequested == "true") {
            that.addProductButton.attr("disabled", "disabled");
            that.addProductButton.attr("href", "#");
            that.addProductButton.addClass("disabled");
        }
    },

    fnRowCallback:function (row, aData) {
        var that = this;
        that.aData = aData;
        $.each($("td", row), function (i, td) {
            var columnName = that.columnMetaData[i].mDataProp;
            if (columnName === "id") {
                var disabled = "";
                if (aData.status === "Failed") {
                    disabled = " disabled='disabled'";
                }
                $(td).html("<input type='checkbox' name='listOfQuoteOptionItems' value='" + aData.id + "'" + disabled + " />");
                $(td).addClass("checkbox");
            } else if (columnName === "name") {
                $(td).addClass("name");
                $(td).addClass("sc_" + aData.productSCode);
                if(aData.isProxyProduct && that.isValidProxyAsset){
                $(td).html("<a target='_blank' class='name' href='" + aData.productDetailsUrl + "'>" + aData.name + "</a>");
                }
            } else if (columnName === "offerName") {
                $(td).html("<a class='offerName' href='" + aData.offerDetailsUrl + "'>" + aData.offerName + "</a>");
            }  else if (columnName === "serviceLevelAgreement") {
                if (aData.isInFrontCatalogue === true) {
                    $(td).addClass("service_Level_Agreements");
                    $(td).text(aData.serviceLevelAgreement);
                    var lineItemId=aData.id;
                    var serviceLevelAgreementText=aData.serviceLevelAgreement;
                    $(td).html("<a href='#' class='service_Level_Agreements' data-id='${quoteOption.id}'>" +
                                                                 "<img src='/rsqe/project-engine/static/images/success.png'>" +
                                                                 serviceLevelAgreementText + "</a>");
                    $(td).find('.service_Level_Agreements').click(function () {
                    that.loadServiceLevelAgreement(lineItemId);
                    });

                }
            } else if (columnName === "maintainerAgreement") {
                if (aData.isInFrontCatalogue === true) {
                    $(td).addClass("maintainer_Agreements");
                    $(td).text(aData.maintainerAgreement);
                    var lineItemId=aData.id;
                    var maintainerText=aData.maintainerAgreement;
                    if (maintainerText === "Advanced Agreements") {
                        $(td).html("<a href='#' class='maintainer_Agreements' data-id='${quoteOption.id}'>" +
                                                                  "<img src='/rsqe/project-engine/static/images/success.png'>" +
                                                                  maintainerText + "</a>");
                    } else if (maintainerText === "No Agreements Selected") {
                        $(td).html("<a href='#' class='maintainer_Agreements' data-id='${quoteOption.id}'>" +
                                                                  "<img src='/rsqe/project-engine/static/images/error.png'>" +
                                                                  maintainerText + "</a>");


                    }

                    $(td).find('.maintainer_Agreements').click(function () {
                        that.maintainerAgreement(lineItemId);
                    });
                }
            }  else if (columnName === "configurable") {
                var tagName = "a";
                var attributes = "";
                var classes = "configure action ";
                if (aData.configurable) {
                    attributes = "href='" + aData.configureUrl + "' target='" + aData.productSCode + "'";
                }
                else {
                    tagName = "span";
                    classes += " disabled ";
                }

                var editContractLinkText = "<a class='contract' href='#'><img src='/rsqe/project-engine/static/images/cog.png' title='Edit Contract Term and Price Book' alt='Edit Contract' /></a>&nbsp;";
                var addNoteLinkTxt = '';
                if(aData.hasLineItemNotes){
                addNoteLinkTxt = "<a href='#' data-id='${quoteOption.id}' rel='note' class='add_note action' title='View Notes' alt='View Notes'>VIEW</a>";
                }
                else{
                addNoteLinkTxt = "<a href='#' data-id='${quoteOption.id}' rel='note' class='add_note action'><img src='/rsqe/project-engine/static/images/note_add.png' title='Note' alt='Note'/>";
                }
                var removeLineItemLinkTxt = that.removeLineItemEnabled ? "<a href='#' data-id='${quoteOption.id}' rel='remove_line_item' class='remove_line_item action'><img src='/rsqe/project-engine/static/images/delete.png' title='Remove Line Item from Quote Option' alt='Remove Line Item'/>" : "";
                var actionsHtml = $("<div style='min-width:70px'>"+editContractLinkText + addNoteLinkTxt + removeLineItemLinkTxt+"</div>");

                $(td).html(actionsHtml);

                $(td).find('.add_note').click(function() {
                    var noteFormUri = $("#lineItemsUrl").text() + "/" + aData.id + "/notes";
                    that.noteDialogInstance.setOptions({"title": "Notes: for line item",
                                                           close: function() {
                                                               that.noteForm.resetForm();
                                                               location.reload();

                                                           }});
                    that.noteDialog.load(noteFormUri,
                                         function () {
                                             that.noteForm.load()
                                         });
                    that.noteDialogInstance.open();

                    return false;

                });

                $(td).find('.remove_line_item').click(function() {
                    var removeLineItemUri = $("#lineItemsUrl").text() + "/" + aData.id + "/remove";
                    $('#loadingMessage').text('Removing Line Item from Quote Option...');
                    $('#loadingMessage').show();

                    $.ajax({
                       type:"GET",
                       url: removeLineItemUri,
                       success: function(data) {
                            $('#loadingMessage').hide();
                            $(that.lineItemsTable).DataTable().fnDraw();
                       },
                       error: function(data) {
                           $('#loadingMessage').hide();
                           that.commonError.show('Error removing line item. ' + data.responseText);
                       }
                   });
                });

                $(td).addClass("actions");
            } else {
                $(td).addClass(columnName);
            }
        });
        $(row).addClass("lineItem");
        $(row).attr("id", "id_" + aData.id);
        if (aData.isImportable) {
            $(row).append('<input type="hidden" id="'+ aData.id + '" name="importable" value="yes">');
        }
        return row;
    },

    fnInitComplete:function () {
        var tableRows = $('#lineItems tbody tr');
        if ($(tableRows[0].getElementsByTagName('td')[0]).hasClass("dataTables_empty")) {
            this.updateProductConfigurationDisable();
            this.productConfigurationButtonDisable();
            this.locateOnGoogleMapsButtonDisable()
        }
        else {
            this.updateProductConfigurationEnable();
            this.productConfigurationButtonEnable();
            this.locateOnGoogleMapsButtonEnable()
        }
    },


    setupLineItemDialog:function () {
        var that = this;
        that.createOfferDialogInstance = new rsqe.Dialog(that.createOfferDialog, {
            title:"Create Offer",
            width:360
        });
    },

    setUpUpdateProductConfigurationDialog:function () {
        var that = this;
        that.updateProductConfigDialogInstance =
        new rsqe.Dialog(that.updateProductConfigDialog, {
            openers:that.updateProductConfig,
            title:"Update Product Configuration",
            width:"350px"
        });

        this.updateProductConfigValidator = $(this.productConfigForm).validate(
                {
                    rules:{
                        product:"required"
                    },
                    messages:{
                        product:"Please select a product"
                    }
                }
        );

        $("#updateProductConfigDialog .prod-config").click(function () {
            if (that.updateProductConfigValidator.form()) {
                var jsonObject = {
                    "rsqeQuoteOptionId":that.quoteOptionId,
                    "expedioQuoteId":that.projectId,
                    "expedioCustomerId":that.customerId
                };

                $('#updateProductConfigDialog .quoteOptionContext').val($.toJSON(jsonObject));
                that.productConfigForm.attr("action", $('#updateProductConfigDialog .product').val());
                that.productConfigForm.submit();
            }
        });
    },

    setupCopyOptionsDialog:function () {
        var that = this;

        that.copyOptionsDialogInstance = new rsqe.Dialog(that.copyOptionsDialog, {
            title:"Select Quote Option",
            width:"360px"
        });

        this.submitForCopyButton.click(function () {
            $("#copyOptionsMessage").addClass("hidden");
            var baseQuoteOptionsUri = $(".baseQuoteOptionsUri").val();
            var copyToQuoteOptionId = $.trim($("#targetQuoteOption").val());
            if (copyToQuoteOptionId.length == 0) {
                $("#copyOptionsMessage").removeClass("hidden");
                return;
            }
            that.progressDialog.taskStarted("Copying In Progress");
            that.progressDialogInstance.open();

            var form = that.copyOptionsDialog.find("form");

            var success = function () {
                $(location).attr('href', baseQuoteOptionsUri + "/" + copyToQuoteOptionId);
            };

            var error = function (response, second) {
                that.progressDialog.taskFinishedWithErrors("Copy was unsuccessful. " + response.responseText);
            };

            $.post(baseQuoteOptionsUri + "/" + copyToQuoteOptionId + '/clones', form.serialize()).success(success).error(error);


        });
    },

    setupQuoteOptionContext:function () {
        var jsonObject = {
            "rsqeQuoteOptionId":this.quoteOptionId,
            "expedioQuoteId":this.projectId,
            "expedioCustomerId":this.customerId,
            "currency":$("#currency").text(),
            "revenueOwner":this.revenueOwner,
            "lineItems":[]
        };
        this.quoteOptionContext.val($.toJSON(jsonObject));
    },

    setupImportProductDialog:function () {
        var that = this;
        that.importProductDialogInstance = new rsqe.Dialog(that.importProductDialog, {
            openers:"#importProduct",
            title:"Import Product",
            height:150,
            width:360,
            close:function () {
                that.importProductTargetIFrame.attr("src", "about:blank");
                that.errorMessages.addClass("hidden");
            }
        });

        this.importProductvalidator = $(this.importProductForm).validate(
                {
                    rules:{
                        eCRFSheet:{
                            required:true,
                            accept:"xls|xlsx"
                        }
                    },
                    messages:{
                        eCRFSheet:"An extension of .xls or .xlsx is required"
                    }

                }
        );

        var submitButton = this.importProductDialog.find(".submit");
        submitButton.click(function () {
            if (that.importProductvalidator.form()) {

                that.importProductTargetIFrame.load(onLoad);
                that.importProductDialogInstance.close();
                that.progressDialog.taskStarted("Validating Import ...");
                that.progressDialogInstance.open();

                var form = that.importProductDialog.find("form");
                var oldActionUri = form.attr('action');
                validateProductImportSubmission();
                form.attr('action', form.attr('action')+'/'+that.commaSeparatedListOfSelectedLineItemIds(that));

                var beforeSend = function() {
                    $(that.importProductFormFileName).val($(that.importProductFormFileInput).val().split('\\').pop());
                };

                function validateProductImportSubmission(){
                    var validationUrl = that.validateImportProductUrl.val();
                    validationUrl = validationUrl +'/'+that.commaSeparatedListOfSelectedLineItemIds(that);
                    $.ajax({
                               type:"GET",
                               url: validationUrl,
                               success: function(data) {
                                   verifyProductImportValidationResponse(data);
                               },
                               error: function() {
                               }
                           });
                }

                function verifyProductImportValidationResponse(data) {
                    if(data.successful && true == data.successful){
                        importProduct();
                    } else {
                        printValidationResult("Import Request Rejected..",data.errors);
                        $('#spinning').addClass("hidden");
                    }
                }

                function printValidationResult(header, errorMessages) {
                    if (errorMessages.length === 0) {
                        $('#spinning').addClass("hidden");
                        that.progressDialog.taskFinished(header);
                    } else {
                        that.progressDialog.taskFinishedWithErrors(header);
                        that.errorMessages.text(errorMessages).removeClass("hidden");
                    }
                }

                function completed(header, errorMessages) {
                    resetFormAction(oldActionUri);
                    that.progressDialog.taskFinished(header);
                }

                function resetFormAction(uri) {
                    form.attr('action', uri);
                }

                function performPostSubmissionActivities(){
                    resetFormAction(oldActionUri);
                    completed("Upload Initialized... Please await status update email...", "");
                    $('#spinning').addClass("hidden");
                }

                function importProduct(){
                $(form).ajaxSubmit(function(){
                    beforeSend();
                    $.ajax({
                               type:"POST",
                               url: form.attr('action'),
                               data: $(form).serialize()
                           });
                });
                performPostSubmissionActivities()
                }
            }
        });


        var onLoad = function () {
            var response = that.importProductTargetIFrame.text();
            that.importProductTargetIFrame.unbind("load", onLoad);
        };
    },

    setupProgressDialog:function () {
        var that = this;
        that.progressDialogInstance = new rsqe.Dialog(that.progressDialogContainer, {
            title:"Progress",
            width:"350px",
            closers:that.progressDialogContainer.find(".close")
        });
    },

    setupEvents:function () {
        var that = this;
        $("#createOffer").disableable('click', function () {
            if (that.createAndValidateLineItemIds(that)) {
                that.createOfferDialogInstance.open();
            }
            return false;
        });

        this.createOffervalidator = $(this.createOfferForm).validate(
                {
                    rules:{
                        offerName:"required"
                    },

                    messages:{
                        offerName:"Please provide a Offer name"
                    }
                }
        );

        $("#bulkConfigurationButton").click(function() {
			if($(this).hasClass("disabled")) {
                return false;
            }

            var maxLineItems = that.maxLineItems;
            that.commonError.hide();

            var createCookie = function(name, value) {
                var expires = "";
                document.cookie = encodeURIComponent(name) + "=" + encodeURIComponent(value) + expires + "; path=/";
            };

            var bulkConfigureAllowed = function(lineItems) {
                if(lineItems.length == 0) {
                    if(that.totalItemCount > maxLineItems) {
                        return false;
                    }
                } else if(lineItems.length > maxLineItems) {
                    return false;
                }

                return true;
            };

            var lineItems = that.lineItemsArray();
            if(!bulkConfigureAllowed(lineItems)) {
                that.commonError.show('You can\'t Bulk Configure more than '+maxLineItems+' line items at a time.  Please choose a smaller subset of items and try again.');
                return false;
            }

            $("#bulkConfigurationButton").addClass("disabled");
            $("#bulkConfigurationButton").attr("disabled", true);
            $('#loadingMessage').text('Navigating to Bulk Configuration...');
            $('#loadingMessage').show();

            createCookie('RSQE-LINEITEM-FILTER', JSON.stringify({filters:[{quoteOptionId:that.quoteOptionId, lineItemIds:lineItems}]}));

            that.abortPendingValidationRequests();

            return true;
        });

        $("#copyOptions").disableable('click', function () {
            var cloneAllowed = true;

            $('input[type=checkbox]', that.lineItemsTable).each(function () {

                if (this.checked) {
                    var orderStatus = $("#id_" + this.value + " td.status")[0].innerHTML;

                    if (orderStatus == "Order Submitted" || orderStatus == "Order Created" || orderStatus == "Customer Approved" || $("#id_" + this.value).hasClass("ifc-item")) {
                        that.commonError.show("One or more of the items selected have progressed to offer. These cannot be copied over to another quote option.");
                        cloneAllowed = false;
                    }
                }
            });

            if (cloneAllowed) {
                var commaSeparatedListOfSelectedLineItemIds = that.commaSeparatedListOfSelectedLineItemIds(that);
                if (commaSeparatedListOfSelectedLineItemIds !== "") {
                    $("#copyOptionsMessage").addClass("hidden");
                    $("#quoteOptionItemIdsToCopy").val(commaSeparatedListOfSelectedLineItemIds);
                    var targetQuoteOptionsUri = $(".targetQuoteOptionsUri").val();
                    $.get(targetQuoteOptionsUri,
                          function (result) {
                              $("#copyTargetOptions").html(result);
                          });
                    that.copyOptionsDialogInstance.open();
                }
            }
        });

        this.submitOfferButton.click(function () {
            if (that.createOffervalidator.form()) {
                var form = that.createOfferForm;
                disableButtons($('#createOfferDialog'));

                $('#offerLoadingMessage').show();

                $.ajax({
                    type : 'POST',
                    url : form.attr('action'),
                    dataType:'text',
                    data: form.serialize()
                }).then(function(location) {
                            window.location.replace(location);
                            document.location.reload();
                        },
                        function(response) {
                            $("#createOfferError").text(response.responseText);
                            $("#createOfferError").removeClass("hidden");
                            enableButtons($('#createOfferDialog'));
                            $('#offerLoadingMessage').hide();
                         });
            }
        });

        this.bulkTemplateButton.click(function () {
            that.bulkTemplateDialog.show();
        });

        this.createOfferForm.submit(function() {
            return false;
        });

        this.updateProductConfig.click(function () {
            that.updateProductConfigDialogInstance.open();
        });


        this.confirmationDialogOkButton.click(function () {
            if (that.confirmationDialogYesOption.attr("checked") != "undefined" && that.confirmationDialogYesOption.attr("checked") == "checked") {
                var quoteOptionItemIdsToCopy = that.commaSeparatedListOfSelectedLineItemIds(that);
                that.successMessage.hide();
                that.commonError.hide();

                if (quoteOptionItemIdsToCopy !== "") {
                    $.post($("#raiseIfcUrl").text(), {"quoteOptionItemIdsToCopy":quoteOptionItemIdsToCopy})
                            .success(function () {
                                         that.successMessage.show("In Flight Change successfully created.", 5000);
                                         $(that.lineItemsTable).dataTable().fnDraw();

                                     })
                            .error(function (response) {
                                       var errorMessage = "In Flight Change failed: " + response.responseText;
                                       that.commonError.show(errorMessage);
                                   });
                }
            }
            that.confirmationDialogInstance.close();
        });

        this.applyFilterButton.click(function () {
            if (that.hideFailedLineItemsCheckbox.is(':checked')) {
                $(that.lineItemsTable).dataTable().fnFilter("excludeFailed=yes");
            } else {
                $(that.lineItemsTable).dataTable().fnFilter("");
            }
        });

        $(this.fetchPricesId).click(function () {
            $('.btnDisable').addClass('disabled').removeClass('enabled');
            $('.btnDisable').disableable('disable');
            $('.actionBtnDisable').addClass('disabled').removeClass('enabled');
            $('.actionBtnDisable').disableable('disable');

            that.commonError.hide();
            that.productPricingService.priceLineItems(that.commaSeparatedListOfSelectedLineItemIds(that));
        });

        $('#customerOrderRefText').keyup(function() {
            if ($(this).val().length == 20) {
                $('#customerOrderRefTextError').removeClass('hidden');
            } else {
                $('#customerOrderRefTextError').addClass('hidden');
            }
        })

    },

    updateProductConfigurationEnable:function () {
        $("#updateProductConfig").disableable('enable');
    },

    updateProductConfigurationDisable:function () {
        $("#updateProductConfig").disableable('disable');
    },

    productConfigurationButtonEnable:function () {
        this.bulkConfigurationButton.disableable('enable');
    },

    productConfigurationButtonDisable:function () {
        this.bulkConfigurationButton.disableable('disable');
    },

    locateOnGoogleMapsButtonEnable:function() {
        this.locateOnGoogleMapsButton.disableable('enable');
    },

    locateOnGoogleMapsButtonDisable:function() {
        this.locateOnGoogleMapsButton.disableable('disable');
    },

    commaSeparatedListOfSelectedLineItemIds:function (that) {
        var lineItemIds = "";

        $('input[type=checkbox]', that.lineItemsTable).each(function () {
            if (this.checked) {
                lineItemIds += this.value + ",";
            }
        });
        if (lineItemIds.length > 0) {
            lineItemIds = lineItemIds.substring(0, lineItemIds.length - 1);
        }

        return lineItemIds;
    },

    lineItemsArray:function() {
        var lineItems = [];

        $('input:checked[type=checkbox]', this.lineItemsTable).each(function () {
            lineItems.push(this.value);
        });

        return lineItems;
    },

    abortPendingValidationRequests:function() {
        // Cancel all pending line item validation requests as they block the UI on IE...
        $.each(this.validationAjaxRequests, function(i, request) {
            request.abort();
        });
    },

    updatePriceStatus:function(data, that) {
        $('tr', that.lineItemsTable).each(function() {
            var result = that.getResultForLineItemId(data, this.id.replace("id_", ""));
            if (this.id && result) {
                that.updatePriceStatusOnTable(result.priceStatus, this.children);
            }
        })
    },

    updatePriceStatusOnTable:function(status, children) {
        _.each(children, function(child) {
            if (child.className == "pricingStatus") {
                child.innerHTML = status;
            }
        })
    },

    getResultForLineItemId:function(data, lineItemId) {
        var result = null;
        _.each(data, function(priceResult) {
            if (priceResult.lineItemId == lineItemId){
                result = priceResult;
            }
        });
        return result;
    },

    createAndValidateLineItemIds:function (that) {
        var lineItemIds = "";
        var openCreateOfferDialog = true;
        $('input[type=checkbox]', that.lineItemsTable).each(function () {
            if (this.checked) {
                var pricingStatus = $(this).closest("tr").children("td.pricingStatus").text();
                var offerName = $(this).closest("tr").children("td").children("a.offerName").text();
                var status = $(this).closest("tr").children("td.status").text();
                if (offerName !== "") {
                    that.commonError.show("Some of the selected items have already been offered.");
                    openCreateOfferDialog = false;
                } else if (status === "Initializing") {
                    that.commonError.show("Some of the selected items are not ready to be moved to an offer.");
                    openCreateOfferDialog = false;
                } else if (status === "Obsolete") {
                    that.commonError.show("Some of the selected items are obsolete and cannot have an offer.");
                    openCreateOfferDialog = false;
                }
                lineItemIds += this.value + ",";
            }
        });

        if (lineItemIds.length > 0) {
            lineItemIds = lineItemIds.substring(0, lineItemIds.length - 1);
        }

        $("#quoteOptionItemIds").val(lineItemIds);

        return openCreateOfferDialog;
    },

    calculateTableWidth:function () {
        $(this.lineItemsTable).dataTable().fnAdjustColumnSizing(false);
    },

    renderMessageRows:function (settings) {
        var tableRows = $('#lineItems tbody tr');
        if (!$(tableRows[0].getElementsByTagName('td')[0]).hasClass("dataTables_empty")) {
            for (var i = 0; i < tableRows.length; i++) {
                var currentRow = settings.aoData[ settings.aiDisplay[i] ]._aData;
                if (currentRow.status !== "Failed") {
                    new rsqe.optiondetails.DataTable().updateErrorRow($(tableRows[i]), currentRow.errorMessage);
                }

                if (currentRow.forIfc == true) {
                    $(tableRows[i]).addClass("ifc-item");
                    this.addIfcActionRow($(tableRows[i]), currentRow);
            }
        }
        }
        this.bindContractClick();
    },

    bindContractClick:function () {
        var self = this;
        $(".contract").click(function () {
            var lineItemId = $(this).parents("tr:first").attr("id").split("_")[1];
            self.loadContractDialog(lineItemId);
        });
    },

    loadContractDialog:function (lineItemId) {
        var self = this;
        var contractUri = "/rsqe/customers/" + self.customerId +
                          "/contracts/" + self.contractId +
                          "/projects/" + self.projectId +
                          "/quote-options/" + self.quoteOptionId +
                          "/line-items/" + lineItemId +
                          "/contract/form";
        self.contractDialogInstance.setOptions({"title":"Edit Contract",
                                                   close:function () {
                                                       self.contractForm.resetForm();
                                                   }});

        this.contractDialog.load(contractUri,
                                 function () {
                                     self.contractForm.load();
                                 });

        self.contractDialogInstance.open();
    },

    setupAttachmentDialog : function() {
        var self = this;
        $('#attachments').click(function () {
            var attachmentUrl = $("#attachmentUrl").html();

            self.attachmentDialogInstance.setOptions({"title": "Add/Edit Attachments","position": ["center",150], "resizable": false,
                                                     close: function() {
                                                        self.attachmentForm.resetForm();
                                                     }});

            self.attachemtDialog.load(attachmentUrl, function() {
                self.attachmentForm.setUpAttachmentDialog();
            });

            self.attachmentDialogInstance.open();
        });
    },

    addIfcActionRow:function (toRow, currentRow) {
        var actionRowId = "id_action_" + currentRow.id;
        var cssClass;
        if ($(toRow).hasClass("odd")) {
            cssClass = "odd";
        } else {
            cssClass = "even";
        }
        $(toRow).after("<tr class='" + cssClass + " lineItemAction' id='" + actionRowId + "'><td></td><td class='action' colspan='" + (toRow.find("td").length - 1) + "'>" + currentRow.ifcAction + "</td></tr>");
    },
    getItemCount:function() {
        return this.itemCount;
    }
};