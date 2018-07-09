rsqe = rsqe || {};
var discounts;
rsqe.QuoteOptionPricingTab = function() {
    var that = this;
    this.customerId = $('#customerId').html();
    this.contractId = $('#contractId').html();
    this.projectId = $('#projectId').html();
    this.quoteOptionId = $('#quoteOptionId').html();
    this.hasEupAccess = $('#hasEupAccess').html();
    this.hasBcmAccess = $('#hasBcmAccess').html();
    this.hasIndirectAccess = $('#hasIndirectAccess').html();
    this.costDiscountApplicable = $('#costDiscountApplicable').html();
    this.hasCostPricingTabAccess = $('#hasCostPricingTabAccess').html() == "true";
    this.pricingTableId = '#priceLines';
    this.usagePricingTableId = '#usagePriceLines';
    this.costPricingTableId = '#costPriceLines';
    this.revenueTableId = '#revenueTable';
    this.unsavedChanges = $("#unsavedDiscounts");
    this.saveMessage = $("#saveMessage");
    this.persistDiscounts = $("#persistDiscounts");
    this.discardDiscounts = $("#discardDiscounts");
    this.requestDiscountDialog = $("#requestDiscountDialog");
    this.requestDiscountForm = $("#requestDiscountDialogForm");
    this.okDiscountApproval = $("#okDiscountApprovalSuccess");
    this.applyFilterButton = $("#applyFilterButton");
    this.clearFilterButton = $("#clearFilterButton");
    this.requestDiscountPopupButton = $("#requestDiscountPopupButton");
    this.importBCMButton = $('#importBCMButton');
    this.unlockPriceLinesButton = $("#unlockPriceLinesButton");
    this.bidManagerList = $("#bidManagerList");
    this.productFilter = $("#productFilter");
    this.countryFilter = $("#countryFilter");
    this.applyBulkDiscountButton = $("#applyBulkDiscount");
    this.oneTimePercentRadioSelector = "#bulkDiscount input:radio[name='oneTime'][value='percent']";
    this.oneTimeNettRadioSelector = "#bulkDiscount input:radio[name='oneTime'][value='nett']";
    this.recurringPercentRadioSelector = "#bulkDiscount input:radio[name='recurring'][value='percent']";
    this.recurringNettRadioSelector = "#bulkDiscount input:radio[name='recurring'][value='nett']";
    this.oneTimePercentField = "#bulkDiscountOneTimePercent";
    this.oneTimeNettField = "#bulkDiscountOneTimeNett";
    this.recurringPercentField = "#bulkDiscountRecurringPercent";
    this.recurringNettField = "#bulkDiscountRecurringNett";
    this.editableFieldsSelector = '.oneTime_value:not(".readOnly"),.recurring_value:not(".readOnly"),.oneTime_discount:not(:empty, ".readOnly"), .oneTime_netTotal:not(:empty, ".readOnly"), .recurring_discount:not(:empty, ".readOnly"), .recurring_netTotal:not(:empty, ".readOnly")';
    this.editableRevenueFieldsSelector = '.proposedRevenue, .triggerMonths';
    this.selectAll = $('#priceLines #selectAll');
    this.oneTimeGrossTotalField = $("#oneTimeGrossTotal");
    this.oneTimeDiscountField = $("#oneTimeDiscountTotal");
    this.oneTimeNetTotalField = $("#oneTimeNetTotal");
    this.recurringGrossTotalField = $("#recurringGrossTotal");
    this.recurringDiscountField = $("#recurringDiscountTotal");
    this.recurringNetTotalField = $("#recurringNetTotal");
    this.usageOffNetTotalField = $("#usageOffNetTotal");
    this.usageOnNetTotalField = $("#usageOnNetTotal");
    this.usageTotalField = $("#usageTotal");
    this.commercialNonStandardRequest = $("#commercialNonStandardRequest");
    this.commercialNonStandardRequestPanel = $("#commercialNonStandardRequestPanel");
    this.proposedGroupField = $("#proposedText");
    this.triggerMonthsGroupField = $("#triggerMonths");
    this.progressDialog = new rsqe.ProgressDialog("#progressDialog", {
                progressText: "#progressText",
                progressButton: "#progressButton",
                errorClass: "error",
                successClass: "success"
    });
    this.costAttachmentDialog = $("#costAttachmentDialog");
    this.uploadCostAttachmentsButton = $("#uploadCostAttachmentsButton");
    this.costAttachmentDialogInstance = new rsqe.Dialog(this.costAttachmentDialog, {width:800});
    this.costAttachmentForm = new rsqe.AttachmentForm({cancelHandler:function () { self.costAttachmentDialogInstance.close(); }});

    this.bcmImportWarningDialog = $("#bcmImportWarningDialog");
    this.warningDialogYesButton = $("#warningDialogYesButton");

    this.bcmImportLinks = $("a[rel='bcmImport']");

    this.bcmExportLinks = $("a[rel='bcmExport']");

    this.bcmExportType = $("#bcmExportTypeFilter");
    this.bcmRejectDiscountLinks = $("a[rel='bcmRejectDiscounts']");
    this.bcmApproveDiscountLinks = $("a[rel='bcmApproveDiscounts']");

    this.pricingCommonError = new rsqe.StatusMessage("#pricingDiv #commonError");
    this.revenueData = {};
    var checkCallback = function () {
        that.pricingCommonError.hide();
        $('#selectAll').attr('title', 'Select None');
    };

    var unCheckCallback = function () {
        that.pricingCommonError.hide();
        $('#selectAll').attr('title', 'Select All');
    };
    this.linkPricingCheckBoxes = new rsqe.CheckboxGroup("input:not([disabled])[name='listOfPriceLines']", {
        actionButtons:$("#applyBulkDiscount, #bulkDiscount input.amount, #bulkDiscount input:radio"),
        select_all:"#priceLines_wrapper #selectAll",
        someChecked: checkCallback,
        allUnchecked: unCheckCallback
    });
    this.linkRevenueCheckBoxes = new rsqe.CheckboxGroup("input:not([disabled])[name='productName']", {
        select_all:"#revenueTable_wrapper #revenueSelectAll",
        someChecked: function () {
            $('#revenueSelectAll').attr('title', 'Select None');
            $('#proposedText').disableable('enable', true);
            $('#triggerMonths').disableable('enable', true);
        },
        allUnchecked: function () {
            $('#revenueSelectAll').attr('title', 'Select All');
            $("#proposedText").disableable('enable', false);
            $("#proposedText").removeClass();
            $("#triggerMonths").disableable('enable', false);
        }
    });

    this.itemCount = 0;

    //change title of right panel
    var pricingRightPaneTitle = $("#rightPane h2 span");
    pricingRightPaneTitle.attr('title', 'Summary');

    var pricingRightPaneText = $("#rightPane h2");
    var children = pricingRightPaneText.children();
    pricingRightPaneText.text("Summary");
    pricingRightPaneText.append(children);

    // make pricing breakdown toggle to show more detail
    $("h4").toggle(function() {
        $(this).addClass("active");
    }, function () {
        $(this).removeClass("active");
    });
    $("h4").click(function() {
        $(this).next(".discounts").slideToggle("slow,");
    });

    $("#exportPricingSheet").click(function(){
        $("#exportPricingSheet").addClass("disabled");
        $("#export-pricing-sheet-msg").dialog({
            modal:true,
            buttons: {
                OK: function() {
                $(this).dialog("close");
                }
            }
        });
    });

    $(".bcm-export-button").click(function(){
            $("#export-bcm-sheet-msg").dialog({
                modal:true,
                buttons: {
                    OK: function() {
                    $(this).dialog("close");
                    }
                }
            });
        });

    window.onblur= function(){
        $("#exportPricingSheet").removeClass("disabled");
    };


    //http://stackoverflow.com/questions/1106377/detect-when-browser-receives-file-download

    discounts = new rsqe.Discounts({
                                       unsavedChanges:this.unsavedChanges,
                                       saveMessage:this.saveMessage,
                                       commonError:this.pricingCommonError
                                   });
    this.discounts = discounts;
    var i = 0;
    this.columnMetaData = [];
    this.columnMetaData[i++] = { "mDataProp": "site", sWidth:"5%"};
    this.columnMetaData[i++] = { "mDataProp": "miniAddress", sWidth:"10%"};
    this.columnMetaData[i++] = { "mDataProp": "product", sWidth:"12%"};
    this.columnMetaData[i++] = { "mDataProp": "summary", sWidth:"10%"};
    this.columnMetaData[i++] = { "mDataProp": "discountStatus", sWidth:"10%"};
    this.columnMetaData[i++] = { "mDataProp": "offerName", sWidth:"10%"};
    this.columnMetaData[i++] = { "mDataProp": "description", sWidth:"15%"};
    this.columnMetaData[i++] = { "mDataProp": "status", sWidth:"10%"};
    if (this.hasEupAccess == "true") {
        this.columnMetaData[i++] = { "mDataProp": "oneTime.rrp", sClass: "numeric nowrap", sWidth:"5%" };
    }
    this.columnMetaData[i++] = { "mDataProp": "oneTime.value", sClass: "numeric nowrap", sWidth:"5%" };
    this.columnMetaData[i++] = { "mDataProp": "oneTime.discount", sClass: "numeric nowrap", sWidth:"5%"};
    this.columnMetaData[i++] = { "mDataProp": "oneTime.netTotal", sClass: "numeric nowrap", sWidth:"5%"};
    if (this.hasEupAccess == "true") {
        this.columnMetaData[i++] = { "mDataProp": "recurring.rrp", sClass: "numeric nowrap", sWidth:"5%" };
    }
    this.columnMetaData[i++] = { "mDataProp": "recurring.value", sClass: "numeric nowrap", sWidth:"5%" };
    this.columnMetaData[i++] = { "mDataProp": "recurring.discount", sClass: "numeric nowrap", sWidth:"5%"};
    this.columnMetaData[i++] = { "mDataProp": "recurring.netTotal", sClass: "numeric nowrap", sWidth:"5%"};

    var col = 0;
    this.revenueMetadata = [];
    this.revenueMetadata[col++] = {"mDataProp": "productCategoryName", sWidth:"25%"};
    this.revenueMetadata[col++] = {"mDataProp": "existingRevenue", sClass: "numeric nowrap", sWidth:"5%"};
    this.revenueMetadata[col++] = {"mDataProp": "proposedRevenue",  sClass: "numeric nowrap", sWidth:"5%"};
    this.revenueMetadata[col++] = {"mDataProp": "triggerMonths", sClass: "numeric nowrap", sWidth:"5%"};
    this.quoteOptionName = $("#quoteName").val();
};

rsqe.QuoteOptionPricingTab.prototype = {

    initialise: function() {
        var self = this;
        self.initPricingTypeRadio();
        self.initUsageChargesView();
        self.initCostChargesView();
        $("#commercialNonStandardRequestPanel").hide();
        $(this.pricingTableId).dataTable({
                                             sPaginationType: "full_numbers",
                                             sDom: 'lrt<"table_footer"ip>',
                                             sAjaxSource: "/rsqe/customers/" + this.customerId + "/contracts/" + this.contractId + "/projects/" + this.projectId + "/quote-options/" + this.quoteOptionId + "/product-prices",
                                             bAutoWidth: true,
                                             bProcessing: true,
                                             bServerSide: true,
                                             bDeferRender: true,
                                             bSort: false,
                                             bRetrieve: true,
                                             bLengthChange : true,
                                             bScrollCollapse: false,
                                             sScrollY: ($(window).height() - 340) + "px",
                                             bStateSave: false,
                                             bFilter: true,
                                             iDisplayLength: 10,
                                             aLengthMenu: [1, 5, 10, 20, 100],
                                             aoColumns: this.columnMetaData,
                                             sAjaxDataProp: function(data) {
                                                 var items = $.makeArray(data.itemDTOs);
                                                 self.itemCount = items.length;
                                                 return items;
                                             },
                                             fnRowCallback: function(row, aData) {
                                                 return self.fnRowCallback(row, aData);
                                             },
                                             fnDrawCallback: function(settings) {
                                                 self.setEditable();
                                                 self.discounts.refreshDiscounts();
                                                 self.renderGroups(settings);

                                                 $(window).unbind('resize').bind('resize', function () {
                                                     var sScrollY = ($(window).height() - 330) + "px";
                                                     $(".dataTables_scrollBody").css("height", sScrollY);
                                                     self.calculateTableWidth();
                                                 });
                                                 self.linkPricingCheckBoxes.initialize();
                                             },
                                             fnInitComplete : function() {
                                                 self.retrievePricingSummary();
                                             },
                                             "fnServerData": function ( sSource, aoData, fnCallback, oSettings ) {
                                                 handleDataTableError(sSource, aoData, fnCallback, oSettings, function(xhr, textStatus, error) {
                                                    self.pricingCommonError.show("Error loading standard prices. " + error + " " + xhr.responseText);
                                                    $("#priceLines_processing").hide();
                                                 });
                                             },
                                             oLanguage : {
                                                 sInfo: "Showing _START_ to _END_ of _TOTAL_ Root Products",
                                                 sLengthMenu: "Display _MENU_ Root Products"
                                             }
                                         });

        this.attachEvents();
        this.setupAttachmentDialog();
        this.setupBcmImportWarningDialog();
        self.setupProgressDialog();
        this.bcmImportDialog = new rsqe.BcmImportDialog(this.progressDialog, this.progressDialogInstance);
        this.bcmImportDialog.setupDialog();
        this.bidCommentsDialog = $("#commentsDialog");
        this.bcmImportLinks.each(function() {
            var uri = $(this).attr("href");
            $(this).click(function() {
                self.bcmImportWarningDialogInstance.open();
                return false;
            });
        });

        this.bcmExportLinks.each(function() {
            var uri = $(this).attr("href");
            $(this).click(function() {
                return $(this).attr("href", uri+ '&offerName='+self.bcmExportType.val());
            });
        });

        this.commentsDialogInstance = new rsqe.Dialog(self.bidCommentsDialog, {width:500, position:'top'});
        this.commentsForm = new rsqe.BidCommentsAndCaveatsDialog({cancelHandler:function () { self.commentsDialogInstance.close(); }});

        this.bcmRejectDiscountLinks.each(function() {
                                             var commentsUri = this.getAttribute('action');
                                             $(this).click(function () {
                                                 self.commentsDialogInstance.setOptions({"title":"Rejection Confirmation"});
                                                 self.loadCommentsAndCaveatsDialog(commentsUri, "Reject", self.quoteOptionName, "Pricing");
                                                 self.commentsDialogInstance.open();
                                                 return false;
                                             });
                                         }
        );


        this.bcmApproveDiscountLinks.click(function() {
            var commentsUri = this.getAttribute('action');
            self.commentsDialogInstance.setOptions({"title":"Approval Confirmation"});
            self.loadCommentsAndCaveatsDialog(commentsUri, "Approve", self.quoteOptionName, "Pricing");
            self.commentsDialogInstance.open();
            return false;
        });
        return this;
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

    setupProgressDialog:function () {
        var that = this;
        that.progressDialogInstance = new rsqe.Dialog($("#progressDialog"), {
            title:"Progress",
            width:"350px",
            closers: $("#progressDialog").find(".ok")
        });
    },

    initPricingTypeRadio : function() {
        var that = this;
        var innerTabView = new ButtonGroupTabView($("#pricingTypeRadio"));

        var resizeTable = function(selector) {
            $(selector).dataTable().fnAdjustColumnSizing(false);
        };

        innerTabView.withView("#standardChargesView", "label[for='standardChargesRadio']", undefined, true)
                    .withView("#usageChargesView", "label[for='usageChargesRadio']", function() {
                        resizeTable(that.usagePricingTableId);
                    });

        if(that.hasCostPricingTabAccess) {
            innerTabView.withView("#costPricingView", "label[for='costPricingRadio']", function() {
                resizeTable(that.costPricingTableId);
            });
        }

        innerTabView.initView();
    },

    initCostChargesView : function() {
        if(!this.hasCostPricingTabAccess) {
            return;
        }

        $('#costWarn').hide();
        $('#costSuccess').hide();
        $('#costDiscountWarnMessage').hide();

        // TODO change the URI to be the cost one!
        var costChargesUri = "/rsqe/customers/" + this.customerId + "/contracts/" + this.contractId + "/projects/" + this.projectId + "/quote-options/" + this.quoteOptionId + "/product-cost-charges";
        this.costChargeDataTable = new PricingDataTable(this.costPricingTableId, costChargesUri, function(data) {return convertCostJsonToRows(data);});

        var onHeaderDrawCallback = function(trNode, costRow, colSpan) {
            // add product name row
            createProductRow(trNode,
                             [{colSpan:1, value:costRow.product},
                              {colSpan:1, value:costRow.summary},
                              {colSpan:1, value:costRow.site},
                              {colSpan:1, value:costRow.miniAddress},
                              {colSpan:(colSpan-4), value:''}]);
        };

        var that = this;
        var onCellValueChanged = function() {
            $('#costWarn').show();
        };

        var onPricingSummaryEnabled = function() {
            $('#costDiscountWarnMessage').show();
            enableElement($('#vendorDiscountReference'));
            enableButton($('#updateVendorDiscountReference'));
        };

        var onPricingSummaryDisabled = function() {
            $('#costDiscountWarnMessage').hide();
            $('#vendorDiscountReference').val('');
            disableElement($('#vendorDiscountReference'));
            disableButton($('#updateVendorDiscountReference'));
        };

        $('#importBCMButton').click(function() {
            if (!that.validVendorDiscountRef()) {
                return;
            }
            that.bcmImportWarningDialogInstance.open();
            return false;
        });

        $('#exportBCMButton').click(function() {
           var pricingRowsWithNoVendorDiscountRef = _.filter(that.costChargeDataTable.pricingRows, function(pricingRow) {
               return "" != pricingRow.recurringCost.discount && 0 != parseInt(pricingRow.recurringCost.discount) && pricingRow.vendorDiscountRef == "";
           });

            if(pricingRowsWithNoVendorDiscountRef.length > 0) {
                alert('A Vendor Discount Reference must be filled in for the highlighted product(s)');
                return false;
            }
            var bcmUri = $('#importBCMActionUrl').html() + '?newBcmExportVersion=no';
            return $.get(bcmUri);
        });

        $('#updateVendorDiscountReference').click(function() {
            var value = $('#vendorDiscountReference').val();
            if(undefined != value) {
                if(value.length > 15) {
                    alert('Vendor Discount Reference can not be more than 15 characters.');
                } else {
                    that.costChargeDataTable.bulkUpdate("vendorDiscountRef", value);
                }
            }
        });

        var updateVendorDiscounts = function() {
            var vendorDiscounts = {};
            _.each(that.costChargeDataTable.pricingRows, function(pricingRow) {
                if(undefined != pricingRow.vendorDiscountRef && "" != pricingRow.vendorDiscountRef) {
                    vendorDiscounts[pricingRow.vendorDiscountRef] = pricingRow.vendorDiscountRef;
                }
            });

            var vendorDiscountRefSelect = $('#vendorDiscountRefFilter');
            vendorDiscountRefSelect.find('option').remove().end();
            vendorDiscountRefSelect.append($("<option>", { value: "", html: "--Please Select--" }));
            for(var k in vendorDiscounts) {
                var isSelected = k == that.selectedVendorDiscount;
                vendorDiscountRefSelect.append($("<option>", { value: k, html: k, selected: isSelected }));
            }
        };

        var onDrawComplete = function() {
            $('#costWarn').hide();
            $('#costDiscountWarnMessage').hide();
            that.retrieveCostSummary();
            updateVendorDiscounts();
        };

        $('#costDiscard').click(function() {
            that.costChargeDataTable.refresh();
        });

        $('#costSave').click(function() {
            $('#costWarn').hide();
            $('#costDiscountWarnMessage').hide();
            $('#loadingMessage').text('Saving...');
            setTimeout(function(){ $('#loadingMessage').show(); }, 100);
            that.persistCostDiscounts();
        });

        $('#applyCostFilterButton').click(function() {
            var selectedProduct = $('#costProductFilter').val();
            var selectedCountry = $('#costCountryFilter').val();
            var selectedVendorDiscount = $('#vendorDiscountRefFilter').val();
            that.selectedVendorDiscount = selectedVendorDiscount;
            that.costChargeDataTable.filter("product=" + selectedProduct + "|country=" + selectedCountry + "|vendorDiscount=" + selectedVendorDiscount);
        });

        $('#clearCostFilterButton').click(function() {
            $('#costProductFilter').val("");
            $('#costCountryFilter').val("");
            $('#vendorDiscountRefFilter').val("");
            that.costChargeDataTable.filter("");
        });

        this.costChargeDataTable
            .metaFor("site", PRICING_CELL_SELECTOR.HIDDEN)
            .metaFor("miniAddress", PRICING_CELL_SELECTOR.HIDDEN)
            .metaFor("product", PRICING_CELL_SELECTOR.HIDDEN)
            .metaFor("summary", PRICING_CELL_SELECTOR.HIDDEN)
            .metaFor("description", PRICING_CELL_SELECTOR.PREPEND_CHECKBOX)
            .metaFor("status")
            .numMetaFor("oneTimeCost.value" , [PRICING_CELL_SELECTOR.READ_ONLY,"oneTimeCostGross"])
            .numMetaFor("oneTimeCost.discount", [PRICING_CELL_SELECTOR.BULK_ONE_TIME_DISCOUNT, PRICING_CELL_SELECTOR.READ_ONLY, "oneTimeCostDiscount", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("oneTimeCost.netTotal", [PRICING_CELL_SELECTOR.BULK_ONE_TIME_NET, PRICING_CELL_SELECTOR.READ_ONLY, "oneTimeCostNet", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("recurringCost.value" ,[PRICING_CELL_SELECTOR.READ_ONLY,"recurringCostGross"])
            .numMetaFor("recurringCost.discount", [PRICING_CELL_SELECTOR.BULK_RECURRING_DISCOUNT, PRICING_CELL_SELECTOR.READ_ONLY, "recurringCostDiscount", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("recurringCost.netTotal", [PRICING_CELL_SELECTOR.BULK_RECURRING_NET, PRICING_CELL_SELECTOR.READ_ONLY, "recurringCostNet", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("vendorDiscountRef", ["vendorDiscountRef", PRICING_CELL_SELECTOR.EDIT_TEXT, PRICING_CELL_SELECTOR.READ_ONLY])
            .withPricingSummary("#oneTimeCostBulkPricingActions", "#recurringCostBulkPricingActions", "#applyBulkDiscountCost", onPricingSummaryEnabled, onPricingSummaryDisabled)
            .initDataTable(onHeaderDrawCallback, onCellValueChanged, ["oneTimeCost", "recurringCost"], [{"selector":"vendorDiscountRef", "maxLength":15, "isMandatoryFnName":"isVendorDiscountMandatory"}], onDrawComplete);
    },

    initUsageChargesView : function() {
        var that = this;

        $('#usageWarn').hide();
        $('#usageSuccess').hide();

        $('#usageSave').click(function() {
            that.persistUsageCharges();
        });

        $('#usageDiscard').click(function() {
            that.refreshUsageCharges();
        });

        var usageChargesUri = "/rsqe/customers/" + this.customerId + "/contracts/" + this.contractId + "/projects/" + this.projectId + "/quote-options/" + this.quoteOptionId + "/product-usage-charges";
        this.usageChargeDataTable = new PricingDataTable(this.usagePricingTableId, usageChargesUri, function(data) {return convertUsageJsonToRows(data);});

        var onHeaderDrawCallback = function(trNode, usageChargeRow, colSpan) {
            if(usageChargeRow.isProduct()) {
                // add product name row
                createProductRow(trNode,
                                 [{colSpan:1, value:usageChargeRow.product},
                                  {colSpan:1, value:usageChargeRow.summary},
                                  {colSpan:(colSpan-2), value:''}]);
            } else {
                // add inner priceline name row
                createProductRow(trNode,
                                 [{colSpan:2, value:''},
                                  {colSpan:(colSpan-2), value:usageChargeRow.description}]);
            }
        };

        this.usageChargeDataTable
            .metaFor("product", PRICING_CELL_SELECTOR.HIDDEN)
            .metaFor("summary", PRICING_CELL_SELECTOR.HIDDEN)
            .metaFor("description", PRICING_CELL_SELECTOR.HIDDEN)
            .metaFor("tier", PRICING_CELL_SELECTOR.PREPEND_CHECKBOX)
            .metaFor("pricingModel")
            .numMetaFor("minCharge.value")
            .numMetaFor("minCharge.discount", [PRICING_CELL_SELECTOR.READ_ONLY, "minChargeDiscount", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("minCharge.netTotal", [PRICING_CELL_SELECTOR.READ_ONLY, "minChargeNet", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("fixedCharge.value")
            .numMetaFor("fixedCharge.discount", [PRICING_CELL_SELECTOR.READ_ONLY, "fixedChargeDiscount", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("fixedCharge.netTotal", [PRICING_CELL_SELECTOR.READ_ONLY, "fixedChargeNet", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("chargeRate.value")
            .numMetaFor("chargeRate.discount", [PRICING_CELL_SELECTOR.READ_ONLY, "chargeRateDiscount", PRICING_CELL_SELECTOR.DISCOUNT])
            .numMetaFor("chargeRate.netTotal", [PRICING_CELL_SELECTOR.READ_ONLY, "chargeRateNet", PRICING_CELL_SELECTOR.DISCOUNT])
            .initDataTable(onHeaderDrawCallback, function() {
                               $('#usageWarn').show();
                           },
                           ["minCharge", "fixedCharge", "chargeRate"], []);
    },

    validVendorDiscountRef : function() {
        var that = this;
        var pricingRowsWithNoVendorDiscountRef = _.filter(that.costChargeDataTable.pricingRows, function(pricingRow) {
            return "" != pricingRow.recurringCost.discount && 0 != parseInt(pricingRow.recurringCost.discount) && pricingRow.vendorDiscountRef == "";
        });

        if (pricingRowsWithNoVendorDiscountRef.length > 0) {
            alert('A Vendor Discount Reference must be filled in for the highlighted product(s)');
            return false;
        }
        return true;
    },

    persistCostDiscounts : function() {
        var that = this;

        var costDiscountDeltas = [];

        _.each(that.costChargeDataTable.getUpdatedRows(), function(currentCharge) {
            costDiscountDeltas.push({
                lineItemId: currentCharge._json.lineItemId,
                description: currentCharge._json.description,
                vendorDiscountRef: currentCharge.vendorDiscountRef,
                oneTimeDiscount: {
                    priceLineId : currentCharge._json.oneTime.id,
                    discount : currentCharge.oneTimeCost.discount,
                    discountUpdated : currentCharge.oneTimeCost.discount != currentCharge._json.oneTime.discount,
                    grossValue : currentCharge.oneTimeCost.value
                },
                recurringDiscount: {
                    priceLineId : currentCharge._json.recurring.id,
                    discount : currentCharge.recurringCost.discount,
                    discountUpdated : currentCharge.recurringCost.discount != currentCharge._json.recurring.discount,
                    grossValue : currentCharge.recurringCost.value
                },
                isManualPricing : currentCharge.recurringCost.isManualPricing(),
                isGrossAdded : currentCharge._json.oneTime.value != currentCharge.oneTimeCost.value || currentCharge._json.recurring.value != currentCharge.recurringCost.value
            });
        });

        if(costDiscountDeltas.length > 0) {
            var delta = {quoteOptionCostDeltas : costDiscountDeltas};
            var costDiscountPostUri = $('#costDiscountPostUri').html();

            $.post(costDiscountPostUri,
                   JSON.stringify(delta),
                   function () {
                       $('#loadingMessage').hide();
                        that.costChargeDataTable.refresh();
                        $("#costSuccess").show();
                        setInterval(function () {
                            $("#costSuccess").fadeOut();
                        }, 3000);
                   }).fail(function(data) {
                        $('#loadingMessage').hide();
                        that.costChargeDataTable.refresh();
                        new rsqe.StatusMessage("#costError").show("Error: " + data.responseText);
                        that.pricingCommonError.show("Error: " + data.responseText);
                   });
        }
    },

    persistUsageCharges : function() {
        var that = this;

        var usageChargesDeltas = [];
        _.each(that.usageChargeDataTable.getUpdatedRows(), function(currentCharge) {
            usageChargesDeltas.push({
                lineItemId: currentCharge._json.lineItemId,
                priceLineId: currentCharge._json.priceLineId,
                classifier: currentCharge._json.tier,
                minChargeDiscount: currentCharge.minCharge.discount,
                fixedChargeDiscount: currentCharge.fixedCharge.discount,
                chargeRateDiscount: currentCharge.chargeRate.discount
            });
        });

        if(usageChargesDeltas.length > 0) {
            var delta = {quoteOptionPricingDeltas : usageChargesDeltas};
            var usageDiscountPostUri = $('#usageDiscountPostUri').html();

            $.post(usageDiscountPostUri,
                   JSON.stringify(delta),
                   function () {
                        that.refreshUsageCharges();
                        $("#usageSuccess").show();
                        setInterval(function () {
                            $("#usageSuccess").fadeOut();
                        }, 2000);
                   });
        }

        return usageChargesDeltas;
    },

    refreshUsageCharges : function() {
        $('#usageWarn').hide();
        $(this.usagePricingTableId).dataTable().fnDraw();
    },

    retrieveCostSummary : function() {
        this._retrievePricingSummary($("#totalOneTimeCost"),
                                     $("#totalOneTimeNet"),
                                     $("#totalOneTimeDiscount"),
                                     $("#totalRecurringCost"),
                                     $("#totalRecurringNet"),
                                     $("#totalRecurringDiscount"),
                                     $("#totalOffNetUsageCost"),
                                     $("#totalOnNetUsageCost"),
                                     $("#totalUsageCost"),
                                     "?suppressStrategy=UI_COSTS");
    },

    retrievePricingSummary : function() {
        this._retrievePricingSummary(this.oneTimeGrossTotalField,
                                     this.oneTimeDiscountField,
                                     this.oneTimeNetTotalField,
                                     this.recurringGrossTotalField,
                                     this.recurringDiscountField,
                                     this.recurringNetTotalField,
                                     this.usageOffNetTotalField,
                                     this.usageOnNetTotalField,
                                     this.usageTotalField,
                                     undefined);
    },

    _retrievePricingSummary : function(oneTimeGross,
                                       oneTimeDiscount,
                                       oneTimeNet,
                                       recurringGross,
                                       recurringDiscount,
                                       recurringNet,
                                       usageOffNet,
                                       usageOnNet,
                                       usageTotal,
                                       params) {
        var url = "/rsqe/customers/" + this.customerId + "/contracts/" + this.contractId + "/projects/" + this.projectId + "/quote-options/" + this.quoteOptionId + "/product-price-summary";
        if(undefined != params) {
            url += params;
        }
        $.get(url, {}, function(data) {
            oneTimeGross.text(data.totalOneTimeGross);
            oneTimeDiscount.text(data.totalOneTimeDiscount + "%");
            oneTimeNet.text(data.totalOneTimeNet);
            recurringGross.text(data.totalRecurringGross);
            recurringDiscount.text(data.totalRecurringDiscount + "%");
            recurringNet.text(data.totalRecurringNet);
            usageOffNet.text(data.totalOffNetUsage);
            usageOnNet.text(data.totalOnNetUsage);
            usageTotal.text(data.totalUsage);
        });
    },

    attachEvents: function() {

        var self = this;

        $(".editable input").live("focus", function() {
            this.select();
        });

        this.persistDiscounts.click(function() {
            $("#pricing-change-save").removeClass("hidden");
            $("#persistDiscounts").addClass("disabled");
            self.discounts.persist(function () { self.resetTable() }, function() { self.retrievePricingSummary()});
        });

        this.discardDiscounts.click(function() {
            self.discounts.discard(function () { self.resetTable() });
        });

        this.applyFilterButton.click(function() {
            var selectedProduct = self.productFilter.val();
            var selectedCountry = self.countryFilter.val();
            $(self.pricingTableId).dataTable().fnFilter("product=" + selectedProduct + "|country=" + selectedCountry);
        });

        this.clearFilterButton.click(function() {
            self.productFilter.val("");
            self.countryFilter.val("");
            $(self.pricingTableId).dataTable().fnFilter("");
        });

        this.applyBulkDiscountButton.click(function() {
            self.applyBulkDiscounts();
            self.clearBulkDiscountForm();
        });

        $(this.oneTimePercentRadioSelector).click(function() {
            $(self.oneTimeNettField).val("");
            $(self.oneTimePercentField).focus();
        });

        $(this.oneTimeNettRadioSelector).click(function() {
            $(self.oneTimePercentField).val("");
            $(self.oneTimeNettField).focus();
        });
        $(this.recurringPercentRadioSelector).click(function() {
            $(self.recurringNettField).val("");
            $(self.recurringPercentField).focus();
        });
        $(this.recurringNettRadioSelector).click(function() {
            $(self.recurringPercentField).val("");
            $(self.recurringNettField).focus();
        });

        $(this.oneTimePercentField).focus(function() {
            $(self.oneTimeNettField).val("");
            $(self.oneTimePercentRadioSelector).attr("checked", "checked");
        });

        $(this.oneTimeNettField).focus(function() {
            $(self.oneTimePercentField).val("");
            $(self.oneTimeNettRadioSelector).attr("checked", "checked");
        });

        $(this.recurringPercentField).focus(function() {
            $(self.recurringNettField).val("");
            $(self.recurringPercentRadioSelector).attr("checked", "checked");
        });

        $(this.recurringNettField).focus(function() {
            $(self.recurringPercentField).val("");
            $(self.recurringNettRadioSelector).attr("checked", "checked");
        });

        $(this.commercialNonStandardRequest).click(function() {
            if (self.commercialNonStandardRequest.is(":checked")) {
                $(self.commercialNonStandardRequestPanel).show();

                $(self.revenueTableId)
                    .dataTable({
                                                   sPaginationType: "full_numbers",
                                                   sDom: 'lrt<"table_footer"ip>',
                                                   sAjaxSource: "/rsqe/customers/" + self.customerId + "/contracts/" + self.contractId + "/projects/" + self.projectId + "/quote-options/" + self.quoteOptionId + "/revenue",
                                                   bAutoWidth: false,
                                                   bProcessing: true,
                                                   bServerSide: true,
                                                   bDeferRender: true,
                                                   bSort: false,
                                                   bRetrieve: true,
                                                   bLengthChange : true,
                                                   bScrollCollapse: false,
                                                   sScrollY: ($(window).height() - 500) + "px",
                                                   bStateSave: false,
                                                   bFilter: true,
                                                   iDisplayLength: 10,
                                                   aLengthMenu: [1, 5, 10, 20, 100],
                                                   aoColumns: self.revenueMetadata,
                                                   sAjaxDataProp: function(data) {
                                                       return $.makeArray(data.itemDTOs);
                                                   },
                                                   fnRowCallback: function(row, aData) {
                                                       return self.fnRevenueRowCallback(row, aData)
                                                   },
                                                   fnDrawCallback: function() {
                                                       self.setRevenueTableEditable();
                                                       $(window).unbind('resize').bind('resize', function () {
                                                           var sScrollY = ($(window).height() - 500) + "px";
                                                           $(".dataTables_scrollBody").css("height", sScrollY);
                                                           $(self.revenueTableId).dataTable().fnAdjustColumnSizing(false);
                                                       });
                                                       self.linkRevenueCheckBoxes.initialize();
                                                   },
                                                   oLanguage : {
                                                       sInfo: "Showing _START_ to _END_ of _TOTAL_ Root Products",
                                                       sLengthMenu: "Display _MENU_ Root Products"
                                                   }
                                               });

                self.attachRevenueFunctions(self);
                $("#proposedText").disableable('enable', false);
                $("#triggerMonths").disableable('enable', false);
            } else {
                self.commercialNonStandardRequestPanel.hide();
                $("#sendDiscountApprovalMessage").removeClass().text("");
                $(self.revenueTableId).dataTable().fnDestroy();
                self.proposedGroupField.unbind("change");
                self.triggerMonthsGroupField.unbind("change");
                self.revenueData = [];
            }
        });

        this.setupRequestDiscountDialog();
    },
    attachRevenueFunctions : function() {
        var self = this;
                    $(".editable input").live("focus", function() {
                        this.select();
                    });
                    self.proposedGroupField.change(function() {
                        var textValue = this.value;
                        self.linkRevenueCheckBoxes.selected_elements().each(function () {
                            var data = $(self.revenueTableId).dataTable().fnGetData(this.parentNode.parentNode);
                            var revenue = self.revenueData[data.id];
                            var triggerVal = revenue == undefined ? 0 : revenue.triggerMonths;
                            self.revenueData[data.id] = {"proposedRevenue":textValue,
                                "existingRevenue":data.existingRevenue,
                                "triggerMonths":triggerVal,
                                "productCategoryName":data.productCategoryName
                            };
                            var row = this.parentNode.parentNode;
                            row.cells[2].textContent = textValue;
                        });
                    });
                    self.triggerMonthsGroupField.change(function() {
                        var textValue = this.value;
                        self.linkRevenueCheckBoxes.selected_elements().each(function () {
                            var data = $(self.revenueTableId).dataTable().fnGetData(this.parentNode.parentNode);
                            var revenue = self.revenueData[data.id];
                            var proposedVal = revenue == undefined ? 0 : revenue.proposedRevenue;
                            self.revenueData[data.id] = {"proposedRevenue":proposedVal,
                                "existingRevenue":data.existingRevenue,
                                "triggerMonths":textValue,
                                "productCategoryName":data.productCategoryName
                            };
                            var row = this.parentNode.parentNode;
                            row.cells[3].textContent = textValue;
                        });
                    });
                },
    destroy: function() {
        $(this.persistDiscounts).unbind("click");
        $(this.discardDiscounts).unbind("click");
        $(this.applyFilterButton).unbind("click");
        $(this.clearFilterButton).unbind("click");
        this.applyBulkDiscountButton.unbind("click");
        $(this.oneTimePercentRadioSelector).unbind("click");
        $(this.oneTimeNettRadioSelector).unbind("click");
        $(this.recurringPercentRadioSelector).unbind("click");
        $(this.recurringNettField).unbind("focus");
        $(this.recurringPercentField).unbind("focus");
        $(this.oneTimeNettField).unbind("focus");
        $(this.oneTimePercentField).unbind("focus");
        $(this.requestDiscountPopupButton).unbind("click");
        $(this.unlockPriceLinesButton).unbind("click");
        $(this.commercialNonStandardRequest).unbind("click");
        $(".editable input").unbind("focus");
        $(window).unbind('resize');
        $(this.pricingTableId).dataTable().fnDestroy();
    },

    resetTable: function() {
        $(this.pricingTableId).dataTable().fnDraw();
        $("#pricing-change-save").addClass("hidden");
        $("#persistDiscounts").removeClass("disabled");
    },

    validateRevenueTable: function() {
        var that = this;
        var message = "";

        function isEmpty(proposedRevenue) {
            return proposedRevenue == undefined || proposedRevenue === "";
        }

        $("#revenueTable tbody tr").each(function (i, row) {
            if ($("input:checkbox", row).attr("checked")) {
                var data = $(that.revenueTableId).dataTable().fnGetData(row);
                var revenueObj = that.revenueData[data.id];
                if (isEmpty(revenueObj.proposedRevenue)) {
                    message = "Please enter proposing revenue commitment.";
                }
                if (parseInt(revenueObj.proposedRevenue) < parseInt(data.existingRevenue)) {
                    message = "Proposed revenue should be greater than existing revenue commitment.";
                }
                if(!isEmpty(revenueObj.triggerMonths) && 12 <= parseInt(revenueObj.triggerMonths) ){
                    message = message + "Trigger Months should be between 0 to 12.";
                }
            }
        });
        return message;
    },

    getData: function(row) {
        return $(this.pricingTableId).dataTable().fnGetData(row);
    },
    fnRevenueRowCallback: function(row, aData) {
        var that = this;
        $.each($("td", row), function(i, td) {
            var mDataProp = that.revenueMetadata[i].mDataProp;
            $(td).addClass(mDataProp);
            if (mDataProp === "productCategoryName") {
                $(td).prepend("<input type='checkbox' name='productName'/>");
            }
           $(td).attr("id",mDataProp);
        });
        $(row).attr("id", "id_" + aData.id);
        $(row).addClass("revenueTable");
        return row;
    },
    setRevenueTableEditable :function() {
        var self = this;
        $(self.editableRevenueFieldsSelector)
            .editable(function(value) {
                          var data = $(self.revenueTableId).dataTable().fnGetData(this.parentNode);
                                  var revenue= self.revenueData[data.id];
                                  var proposedVal = revenue == undefined? "" : revenue.proposedRevenue;
                                  var triggerVal = revenue == undefined? "" : revenue.triggerMonths;
                                  if(this.id === "proposedRevenue"){
                                      proposedVal = value;
                                  }
                                  if(this.id === "triggerMonths"){
                                      triggerVal = value;
                                  }
                                  self.revenueData[data.id] = {"proposedRevenue":proposedVal,
                                      "existingRevenue":data.existingRevenue,
                                      "triggerMonths":triggerVal,
                                      "productCategoryName":data.productCategoryName
                                  };
                          return value;
                      },
                      {
                          onblur: 'submit',
                          cssclass: 'editable',
                          placeholder: ''
                      });
    },
    fnRowCallback: function(row, aData) {
        var that = this;
        $.each($("td", row), function(i, td) {
            $(td).addClass(that.columnMetaData[i].mDataProp.replace('.', '_'));
            if (that.isDescriptionCellOfPriceLine(row, i)) {
                if ($("input[name='listOfPriceLines']", td).length == 0) {
                    if (aData.readOnly) {
                        $(td).prepend("<input type='checkbox' disabled='disabled' name='listOfPriceLines'/>");
                    }
                    else {
                        var checkBox = "<input type='checkbox' name='listOfPriceLines' onclick='checkboxTicked(this,\"" + aData.userEntered + "\"," + that.hasBcmAccess + ",\"" + aData.oneTime.value + "\",\"" + aData.recurring.value + "\"," + aData.isManualPricing + ");' />";
                        $(td).prepend(checkBox);
                    }

                }
            }
            if (that.columnMetaData[i].mDataProp === "oneTime.value") {
                if($('#isManualModifyMap').text()!=""){
                    var isModifyJson = JSON.parse($('#isManualModifyMap').text());
                    var user = getCookie("AUTHN_TOKEN");
                    if (user != "BID_MGR" && (isModifyJson[aData.lineItemId] == "Modify" || isModifyJson[aData.lineItemId] == "Cease")) {
                        $(td).addClass('grossEdit readOnly');
                    }

                    if (that.hasBcmAccess == "true" && aData.oneTime.id != "" && (aData.oneTime.value == "0.00" || aData.oneTime.value == "" || aData.userEntered == "Y")) {
                        $(td).removeClass("readOnly");
                        if (!aData.readOnly && (aData.oneTime.value == "0.00" || aData.oneTime.value == "")) {
                            $(td).css('background-color', 'yellow');
                        }
                    } else {
                        $(td).addClass("grossEdit readOnly");
                    }
                }
            }

            if (that.columnMetaData[i].mDataProp === "recurring.value") {
                if (that.hasBcmAccess == "true" && aData.recurring.id != "" && (aData.recurring.value == "0.00" || aData.recurring.value == "" || aData.userEntered == "Y")) {
                    $(td).removeClass("readOnly");
                    if (!aData.readOnly && (aData.recurring.value == "0.00" || aData.recurring.value == "")) {
                        $(td).css('background-color', 'yellow');
                    }
                } else {
                    $(td).addClass("grossEdit readOnly");
                }
            }


            if (that.columnMetaData[i].mDataProp === "oneTime.discount") {
                if (!aData.oneTime.discountEnabled || aData.oneTime.value == "0.00" || aData.oneTime.value == "" || (that.hasBcmAccess != "true" && aData.userEntered == "Y")) {
                    $(td).addClass("readOnly");
                }
            }

            if (that.columnMetaData[i].mDataProp === "oneTime.netTotal") {
                if (aData.oneTime.value == "0.00" || aData.oneTime.value == "" || (that.hasBcmAccess != "true" && aData.userEntered == "Y")) {
                    $(td).addClass("readOnly");
                }
            }

            if (that.columnMetaData[i].mDataProp === "recurring.discount") {
                if (!aData.recurring.discountEnabled || aData.recurring.value == "0.00" || aData.recurring.value == "" || (that.hasBcmAccess != "true" && aData.userEntered == "Y")) {
                    $(td).addClass("readOnly");
                }
            }

            if (that.columnMetaData[i].mDataProp === "recurring.netTotal") {
                if (aData.recurring.value == "0.00" || aData.recurring.value == "" || (that.hasBcmAccess != "true" && aData.userEntered == "Y")) {
                    $(td).addClass("readOnly");
                }
            }

            if (aData.readOnly) {
                $(td).addClass("readOnly");
            }
        });
        $(row).attr("oneTime_id", "id_" + aData.oneTime.id);
        $(row).attr("recurring_id", "id_" + aData.recurring.id);
        $(row).addClass("priceLine");
        if (aData.readOnly) {
            $(row).addClass("readOnly");
        }
        return row;
    },

    isDescriptionCellOfPriceLine: function(row, cellIndex) {
        return cellIndex == 6 /*&& $(row).hasClass("priceLine")*/;
    },

    setEditable : function() {
        var self = this;

        $(self.editableFieldsSelector)
            .editable(function(value) {

                          var parent = $(this).parent();

                          var discountOn;
                          if ($(this).attr("class").indexOf("oneTime") > -1) {
                              discountOn = "oneTime";
                          } else {
                              discountOn = "recurring";
                          }

                          var isDiscountField = false;
                          var isGrossField = false;
                          if ($(this).attr("class").indexOf("discount") > -1) {
                              isDiscountField = true;
                          } else if ($(this).attr("class").indexOf("value") > -1) {
                              isGrossField = true;
                          }

                          if (isNaN(value)) {
                              if (isDiscountField) {
                                  return this.revert.asPercent();
                              } else {
                                  return this.revert.asCurrency();
                              }
                          }

                          if (!(this.revert === "0.00" && value === "0") && (this.revert.asCurrency() === value.asCurrency())) {
                              if (isDiscountField) {
                                  return this.revert.asPercent();
                              } else {
                                  return this.revert.asCurrency();
                              }
                          }

                                  var data = self.getData(this.parentNode);

                                  parent.addClass(discountOn + "ChangeDiscount");
                                  self.unsavedChanges.removeClass("hidden");

                                  var priceLineId = '';
                                  if (discountOn == "oneTime") {
                                      priceLineId = data.oneTime.id;
                                  } else if (discountOn == "recurring") {
                                      priceLineId = data.recurring.id;
                                  }


                                  if (isDiscountField) {
                                      var check= self.discounts.addDiscount(parent, discountOn, value, data.lineItemId, priceLineId);
                                      if(check == false)
                                      {
                                          $("#unsavedDiscounts").addClass("hidden");
                                      }
                                      return check;
                                  } else if (isGrossField) {
                                      return self.discounts.addNetBasedOnExistingGross(parent, discountOn, value, data.lineItemId, priceLineId);
                                  }
                                  else {
                                      return self.discounts.addDiscountFromNetTotal(parent, discountOn, value, data.lineItemId, priceLineId);
                                  }
                      },
                      {
                          onblur: 'submit',
                          cssclass: 'editable'
                      });
    },

    calculateTableWidth: function() {
        // ie hack because collapsing tab doesn't fire window.resize (FF doesn't need it)
        $(this.pricingTableId).dataTable().fnAdjustColumnSizing(false);
    },

    renderGroups: function(settings) {
        var tableRows = $('#priceLines tbody tr');
        var colspan = tableRows[0].getElementsByTagName('td').length;
        var product = "";
        if (!$(tableRows[0].getElementsByTagName('td')[0]).hasClass("dataTables_empty")) {


            for (var i = 0; i < tableRows.length; i++) {

                var currentRow = settings.aoData[ settings.aiDisplay[i] ]._aData;
                var newProduct = currentRow.product;
                var newMiniAddress=currentRow.miniAddress;
                var newSite = currentRow.site;
                var newDiscountStatus = currentRow.discountStatus;
                var newOfferName = currentRow.offerName;
                var groupLevel = currentRow.groupingLevel;
                var aggregateRow = currentRow.aggregateRow;
                var newSummary = currentRow.summary;
                var newProductSiteSummary = newProduct + " " + newSite + " " + newSummary + " " +newMiniAddress;
                if (newProductSiteSummary != product) {
                    var nGroup = document.createElement('tr');
                    if (currentRow.forIfc == "true") {
                        nGroup.className = " ifc-item product_group group_" + groupLevel;
                    }
                    else {
                        nGroup.className = "product_group group_" + groupLevel;
                    }

                    var productCell = document.createElement('td');
                    productCell.colSpan = 1;
                    productCell.className = "product";
                    productCell.innerHTML = newProduct;
                    var miniAddressCell = document.createElement('td');
                    miniAddressCell.colSpan = 1;
                    miniAddressCell.className = "miniAddress";
                    //miniAddressCell.innerHTML = newMiniAddress;
                    miniAddressCell.innerHTML = groupLevel == "0" ? newMiniAddress : "";
                    var summaryCell = document.createElement('td');
                    summaryCell.colSpan = 1;
                    summaryCell.className = "summary";
                    summaryCell.innerHTML = groupLevel == "0" ? newSummary : "";
                    var siteCell = document.createElement('td');
                    siteCell.colSpan = 1;
                    siteCell.className = "site";
                    siteCell.innerHTML = groupLevel == "0" ? newSite : "";
                    var discountStatusCell = document.createElement('td');
                    discountStatusCell.colSpan = 1;
                    discountStatusCell.className = "site";
                    discountStatusCell.innerHTML = groupLevel == "0" ? newDiscountStatus : "";
                    var offerNameCell = document.createElement('td');
                    offerNameCell.colSpan = colspan - 1;
                    offerNameCell.className = "site";
                    offerNameCell.innerHTML = groupLevel == "0" ? newOfferName : "";
                    nGroup.appendChild(siteCell);
                    nGroup.appendChild(miniAddressCell);
                    nGroup.appendChild(productCell);
                    nGroup.appendChild(summaryCell);
                    nGroup.appendChild(discountStatusCell);
                    nGroup.appendChild(offerNameCell);
                    tableRows[i].parentNode.insertBefore(nGroup, tableRows[i]);
                    product = newProductSiteSummary;
                }
                if (aggregateRow === 'true') {
                    tableRows[i].parentNode.removeChild(tableRows[i]);
                }
            }
        }
    },

    applyBulkDiscounts: function() {
        var isOneTimePercent = $("#bulkDiscount input[name='oneTime']:checked").val() == "percent";
        var isRecurringPercent = $("#bulkDiscount input[name='recurring']:checked").val() == "percent";
        var oneTimeAmount;
        var recurringAmount;

        if (isOneTimePercent) {
            oneTimeAmount = parseFloat($.trim($("#bulkDiscountOneTimePercent").val()));
        } else {
            oneTimeAmount = parseFloat($.trim($("#bulkDiscountOneTimeNett").val()));
        }
        if (isRecurringPercent) {
            recurringAmount = parseFloat($.trim($("#bulkDiscountRecurringPercent").val()));
        } else {
            recurringAmount = parseFloat($.trim($("#bulkDiscountRecurringNett").val()));
        }

        var that = this;

        $("tr.priceLine:not('.readOnly')").each(function(index, row) {
            if ($("input:checkbox", row).attr("checked")) {
                var lineItemId = that.getData(row).lineItemId;

                var rowObj = $(row);
                if (!isNaN(oneTimeAmount) && that.doesRowHaveId(rowObj, "onetime_id") &&
                    that.getData(row).oneTime.value !== "0.00") {
                    if (isOneTimePercent) {
                        that.discounts.addDiscount(rowObj, "oneTime", oneTimeAmount, lineItemId, that.getData(row).oneTime.id);
                    } else {
                        that.discounts.addDiscountFromNetTotal(rowObj, "oneTime", oneTimeAmount, lineItemId, that.getData(row).oneTime.id);
                    }
                }

                if (!isNaN(recurringAmount) && that.doesRowHaveId(rowObj, "recurring_id") &&
                    that.getData(row).recurring.value !== "0.00") {
                    if (isRecurringPercent) {
                        that.discounts.addDiscount(rowObj, "recurring", recurringAmount, lineItemId, that.getData(row).recurring.id);
                    } else {
                        that.discounts.addDiscountFromNetTotal(rowObj, "recurring", recurringAmount, lineItemId, that.getData(row).recurring.id);
                    }
                }
            }
        });

        this.discounts.refreshDiscounts();

    },

    doesRowHaveId: function(row, idType) {
        return row.attr(idType) != "id_";
    },

    clearBulkDiscountForm: function() {
        this.linkPricingCheckBoxes.uncheck();
        $(this.oneTimePercentRadioSelector).attr("checked", "checked");
        $(this.recurringPercentRadioSelector).attr("checked", "checked");
        $(this.oneTimePercentField).val("");
        $(this.oneTimeNettField).val("");
        $(this.recurringPercentField).val("");
        $(this.recurringNettField).val("");
    },

    setupRequestDiscountDialog: function() {
        var that = this;
        var pricingActionsUri = $("#requestDiscountActionUrl").html();
        var dialog = new rsqe.Dialog(that.requestDiscountDialog, {title:"Request Discount Approval",
            width: "600px",
            closers: that.okDiscountApproval});

        that.requestDiscountvalidator = $(this.requestDiscountForm).validate(
                {
                    rules:{
                        bidManagerList:"required",
                        customerGroupEmailId:{
                            required:true,
                            email:true
                        }
                    },
                    messages:
                    {
                        bidManagerList:"Please select a Bid Manager",
                        customerGroupEmailId:"Please enter a valid email"
                    } ,

                    errorPlacement:function (error, element) {
                        if (element.attr("name") == "customerGroupEmailId"){
                            error.insertAfter("#copyToEmail");
                        } else {
                            error.insertAfter(element);
                        }

                    }
                }

        );

        this.requestDiscountPopupButton.click(function() {

            if (that.hasIndirectAccess == "true") {
                $("#commercialNonStandardRequest").disableable('enable', true);
            } else {
                $("#commercialNonStandardRequest").disableable('enable', false);
            }
            if (!that.discounts.hasUnsavedChanges()) {
                $("#commentsDiv").scrollTop(1000);
                that.pricingCommonError.hide();
                that.okDiscountApproval.hide();
                $(".bidManagerList")
                    .empty();
                $("#sendDiscountApprovalMessage").removeClass().text("");

                $("#requestDiscountDialogForm, input#sendDiscountApprovalButton, a.cancel").show()

                $.getJSON(pricingActionsUri + '/bid-managers',
                          function(result) {
                              $(".bidManagerList").append($("<option></option>")
                                                              .attr("value", "")
                                                              .text("--Please Select--"));
                              var userList = $.makeArray(result.users);
                              $.each(userList, function(i, user) {
                                  $(".bidManagerList")
                                      .append($("<option></option>")
                                                  .attr("value", user.email)
                                                  .text(user.forename + ' ' + user.surname + ' (' + user.email + ')'));
                              });
                          });
                dialog.open();
            } else {
                that.pricingCommonError.show("Cannot request discounts due to unsaved changes");
            }
        });

        this.unlockPriceLinesButton.click(function() {
            $.post(pricingActionsUri + '/unlock-price-lines')
                .success(function () {
                             $(that.unlockPriceLinesButton).attr("disabled", true);
                             $(that.unlockPriceLinesButton).addClass("disabled")
                             $(that.requestDiscountPopupButton).attr("disabled", false)
                             $(that.requestDiscountPopupButton).removeClass("disabled")
                             $(that.pricingTableId).dataTable().fnDraw();
                         })
        });


        var sendDiscountApprovalButton = this.requestDiscountDialog.find("#sendDiscountApprovalButton");
        sendDiscountApprovalButton.click(function() {
           $('#requestDiscountLoadingMessage').hide();
            $("#sendDiscountApprovalMessage").removeClass();
            $("#sendDiscountApprovalMessage").text("");

            if (that.requestDiscountvalidator.form()) {
                if (!$.trim($("#bidManagerList").val()) && !$.trim($("#customerGroupEmailId").val())) {
                    $("#sendDiscountApprovalMessage").addClass("error");
                    $("#sendDiscountApprovalMessage").text("Please select a Bid Manager");
                } else {
                    var validateRevenueTable = that.validateRevenueTable();
                    if (validateRevenueTable != ""){
                        $("#sendDiscountApprovalMessage").addClass("error");
                        $("#sendDiscountApprovalMessage").text(validateRevenueTable);
                    } else {
                        $('#requestDiscountLoadingMessage').show(); //validations passed; show loading message
                        var items = [];
                        var i = 0;
                        for (var key in that.revenueData) {
                            items[i] = that.revenueData[key];
                            i++;
                        }
                        var revenueDTO = i>0? {"itemDTOs":items}:{};
                        $.post(pricingActionsUri + '/request-discount-approval'
                                   + '?bidManagerEmail=' + $("#bidManagerList").val()
                                   + '&groupEmailId=' + $("#customerGroupEmailId").val() +'&comment=' +$("#salesUserNewComment").val(), JSON.stringify(revenueDTO))
                            .success(function(result) {
                                         if(result.status == 'success') {
                                             var email = $("#bidManagerList").val();
                                             $("#sendDiscountApprovalMessage").addClass("success");
                                             $("#requestDiscountDialogForm, input#sendDiscountApprovalButton, a.cancel").hide();
                                             $('#requestDiscountLoadingMessage').hide();
                                             that.okDiscountApproval.show();
                                             $("#sendDiscountApprovalMessage").text(result.message + ' to ' + email);
                                             $(that.pricingTableId).dataTable().fnDraw();
                                             $(that.requestDiscountPopupButton).attr("disabled", true);
                                             $(that.requestDiscountPopupButton).addClass("disabled")
                                         } else if(result.status == 'fail') {
                                             $("#sendDiscountApprovalMessage").addClass("error");
                                             $('#requestDiscountLoadingMessage').hide();
                                             $("#sendDiscountApprovalMessage").text("Request Discount has encountered the following error: " + result.message);
                                         }  else {
                                            $('#requestDiscountLoadingMessage').hide();
                                         }
                                     });
                    }
                }
                }
        });
    },
    getItemCount:function() {
        return this.itemCount;
    },

    setupAttachmentDialog : function() {
        var self = this;
        $('#uploadCostAttachmentsButton').click(function () {
            var costAttachmentUrl = $("#costAttachmentUrl").html();

            self.costAttachmentDialogInstance.setOptions({"title": "Add/Edit Attachments","position": ["center",150], "resizable": false,
                                                     close: function() {
                                                        self.costAttachmentForm.resetForm();
                                                     }});

            self.costAttachmentDialog.load(costAttachmentUrl, function() {
                self.costAttachmentForm.setUpAttachmentDialog();
                $("#tierFilter").attr("disabled", true);
            });

            self.costAttachmentDialogInstance.open();
        });
    },

    setupBcmImportWarningDialog: function() {
        var self = this;
        self.bcmImportWarningDialogInstance = new rsqe.Dialog(self.bcmImportWarningDialog, {
            title: "Warning!",
            width: 350
        });

        self.warningDialogYesButton.click(function() {
            self.bcmImportWarningDialogInstance.close();
            self.openBcmImportDialog();
            return false;
        });
    } ,
    openBcmImportDialogOnCondition: function() {
        if (this.costDiscountApplicable == "true") {
            this.bcmImportWarningDialogInstance.open();
        } else {
            this.openBcmImportDialog();
        }
    },
    openBcmImportDialog: function() {
        var bcmUri = $('#importBCMActionUrl').html();
        this.bcmImportDialog.openDialog(bcmUri, this.importBCMButton);
    }
};

function getCookie(c_name) {
    var c_value = document.cookie;
    var c_start = c_value.indexOf(" " + c_name + "=");
    if (c_start == -1) {
        c_start = c_value.indexOf(c_name + "=");
    }
    if (c_start == -1) {
        c_value = null;
    }
    else {
        c_start = c_value.indexOf("=", c_start) + 1;
        var c_end = c_value.indexOf(";", c_start);
        if (c_end == -1) {
            c_end = c_value.length;
        }
        c_value = unescape(c_value.substring(c_start, c_end));
    }
    return c_value;
}


function checkboxTicked(checkboxElement, userEntered, bidManager, oneTimeValue, recurringValue, isManualPrice) {
    if(checkboxElement.checked) {
          var row = $(checkboxElement.parentElement.parentElement);
          var grossOneTimeEdit = row.find(".oneTime_value");
          var grossRecurringEdit = row.find(".recurring_value");

        grossOneTimeEdit.removeClass("readOnly");
        grossRecurringEdit.removeClass("readOnly");


        $("#manualPriceWarnMessage").removeClass("hidden");

          var target = function(value) {

                         var parent = $(this).parent();

                         var discountOn;
                         if ($(this).attr("class").indexOf("oneTime") > -1) {
                             discountOn = "oneTime";
                         } else {
                             discountOn = "recurring";
                         }

                          var isDiscountField = false;
                          var isGrossField = false;
                          if ($(this).attr("class").indexOf("discount") > -1) {
                              isDiscountField = true;
                          } else if ($(this).attr("class").indexOf("value") > -1) {
                              isGrossField = true;
                          }

                         if(isNaN(value)){
                             if (isDiscountField) {
                                 return this.revert.asPercent();
                             } else {
                                 return this.revert.asCurrency();
                             }
                         }

                         if (!(this.revert === "0.00" && value === "0") && (this.revert.asCurrency() === value.asCurrency())) {
                                                       if (isDiscountField) {
                                                           return this.revert.asPercent();
                                                       } else {
                                                           return this.revert.asCurrency();
                                                       }
                                                   }

                         var lineItemId = "";
                         if (parent.attr('onetime_id') != null) {
                             lineItemId = parent.attr('onetime_id').substring(3);
                         }
                         lineItemId = getTableData(this.parentNode).lineItemId;

                         var priceLineId = '';
                         if(discountOn == "oneTime") {
                             priceLineId = getTableData(this.parentNode).oneTime.id;
                         } else if (discountOn == "recurring") {
                             priceLineId = getTableData(this.parentNode).recurring.id;
                         }

                         parent.addClass(discountOn + "ChangeDiscount");
                         $("#unsavedDiscounts").removeClass("hidden");
                         if (isDiscountField) {
                             return discounts.addDiscount(parent, discountOn, value, lineItemId, priceLineId);
                             $("#manualPriceWarnMessage").addClass("hidden");
                         } else if (isGrossField) {
                             return discounts.addNetBasedOnExistingGross(parent, discountOn, value, lineItemId, priceLineId);
                             $("#manualPriceWarnMessage").addClass("hidden");
                         } else {
                             return discounts.addDiscountFromNetTotal(parent, discountOn, value, lineItemId, priceLineId);
                             $("#manualPriceWarnMessage").addClass("hidden");
                         }
                     };

          var options = {
                         onblur: 'submit',
                         cssclass: 'editable'
                     };

                    grossRecurringEdit.editable(target, options);
                    grossOneTimeEdit.editable(target, options);
                    if (!bidManager && !isManualPrice && (userEntered == "Y" || oneTimeValue == "0.00" || recurringValue == "0.00")) {
                        grossOneTimeEdit.addClass("readOnly");
                        grossOneTimeEdit.editable('disable');
                        grossRecurringEdit.addClass("readOnly");
                        grossRecurringEdit.editable('disable');
                    }

          $(".editable input").live("focus", function() {
                        this.select();
                });
      }else {
          $("#manualPriceWarnMessage").addClass("hidden");
          var grossOneTimeEdit = $(checkboxElement.parentElement.parentElement).find(".oneTime_value");
          grossOneTimeEdit.addClass("readOnly");
          grossOneTimeEdit.editable('disable');

          var grossRecurringEdit = $(checkboxElement.parentElement.parentElement).find(".recurring_value");
          grossRecurringEdit.addClass("readOnly");
          grossRecurringEdit.editable('disable');

        if (bidManager && (userEntered == "Y" || oneTimeValue == "0.00" || oneTimeValue == "" || recurringValue == "0.00" || recurringValue == "")) {
            grossOneTimeEdit.removeClass("readOnly");
            grossOneTimeEdit.editable('enable');
            grossRecurringEdit.removeClass("readOnly");
            grossRecurringEdit.editable('enable');
        }
      }
  }

function getTableData(row) {
    var data = $("#priceLines").dataTable().fnGetData(row);
    return data;
}

function convertCostJsonToRows(data) {
    var rows = [];
    var headers = {};

    var createCostRow = function(item, isHeader) {
        var getVendorDiscount = function(item) {
            if("" != item.oneTime.value) {
                return item.oneTime.vendorDiscountRef;
            } else {
                return item.recurring.vendorDiscountRef;
            }
        };

        var aRow = {
            _json : item,
            product: item.product,
            summary: item.summary,
            site: item.site,
            miniAddress: item.miniAddress,
            description: item.description,
            status : item.status,
            oneTimeCost : {
                value: item.oneTime.value,
                discount: item.oneTime.discount,
                netTotal: item.oneTime.netTotal,
                isSet : function() {
                    return item.oneTime.discountEnabled;
                },
                isManualPricing: function(){
                    return item.isManualPricing;
                },
                isGrossValueNotPresent: function(){
                    return "" == item.oneTime.value || "0.00" == item.oneTime.value;
                }
            },
            recurringCost : {
                value: item.recurring.value,
                discount: item.recurring.discount,
                netTotal: item.recurring.netTotal,
                isSet : function() {
                    return item.recurring.discountEnabled;
                },
                isManualPricing: function(){
                    return item.isManualPricing;
                },
                isGrossValueNotPresent: function(){
                    return "" == item.recurring.value || "0.00" == item.recurring.value;
                }
            },
            vendorDiscountRef: getVendorDiscount(item),
            isVendorDiscountMandatory : function() {
                var mandatory = false;
                if($.isNumeric(item.oneTime.discount)) {
                    if("0.00000" != item.oneTime.discount) {
                        mandatory = true;
                    }
                }
                if($.isNumeric(item.recurring.discount)) {
                    if("0.00000" != item.recurring.discount) {
                        mandatory = true;
                    }
                }
                return mandatory;
            },
            isHeader: function() {
                return undefined != isHeader && isHeader;
            },
            isReadOnly: function() {
                return !item.oneTime.discountEnabled && !item.recurring.discountEnabled;
            }
        };
        return aRow;
    };

    var buildHeaderKey = function(item) {
        return item.product + item.summary  + item.site + item.miniAddress ;
    };

    var pushItemToHeader = function(item) {
        var key = buildHeaderKey(item);
        if(undefined == headers[key]) {
            headers[key] = [];
        }
        headers[key].push(item);
    };

    _.each(data.itemDTOs, function(item) {
        pushItemToHeader(item);
    });

    _.each(headers, function(header) {
        rows.push(createCostRow(header[0], true));

        _.each(header, function(item) {
            rows.push(createCostRow(item));
        });
    });

    return rows;
}

function convertUsageJsonToRows(data) {
    var usageProductsJson = data.products;
    var charges = [];

    var ensureArray = function(array) {
        if (array.length) {
            return array;
        }

        return [array];
    };

    if(usageProductsJson) {
        usageProductsJson = ensureArray(usageProductsJson);

        for(var a = 0; a < usageProductsJson.length; a++) {
            var product = usageProductsJson[a];
            charges.push(new UsageChargeRow({product:product.productName,
                                             summary:product.summary}));

            if(product.priceLines) {
                var priceLines = ensureArray(product.priceLines);
                for(var b = 0; b < priceLines.length; b++) {
                    var priceLine = priceLines[b];
                    charges.push(new UsageChargeRow({description:priceLine.description}));

                    if(priceLine.tiers) {
                        var tiers = ensureArray(priceLine.tiers);
                        for(var c = 0; c < tiers.length; c ++) {
                            charges.push(new UsageChargeRow(tiers[c]));
                        }
                    }
                }
            }
        }
    }

    return charges;
}

function UsageChargeRow(json) {
    this._json = json;
    this.tier = nullToEmpty(json.tierDescription);
    this.isTier = function() {
        return "" != this.tier;
    };
    this.isHeader = function() {
        return !this.isTier();
    };
    this.product = this.isTier() ? "" : nullToEmpty(json.product);
    this.miniAddress = this.isTier() ? "" : nullToEmpty(json.product);
    this.description = this.isTier() ? "" : nullToEmpty(json.description);
    this.pricingModel = nullToEmpty(json.pricingModel);
    this.summary = this.isTier() ? "" : nullToEmpty(json.summary);
    this.isProduct = function() {
        return !this.isTier() && "" != this.product;
    };

    var noCharge = function() {
        return {
            value: "",
            discount: "",
            netTotal: ""
        };
    };

    this.minCharge = json.minCharge ? json.minCharge : noCharge();
    this.fixedCharge = json.fixedCharge ? json.fixedCharge : noCharge();
    this.chargeRate = json.chargeRate ? json.chargeRate : noCharge();

    mixinChargeMethods(this.minCharge);
    mixinChargeMethods(this.fixedCharge);
    mixinChargeMethods(this.chargeRate);
    mixinManualPriceMethods(this.minCharge);
    mixinManualPriceMethods(this.fixedCharge);
    mixinManualPriceMethods(this.chargeRate);
    mixinGrossValueMethods(this.minCharge);
    mixinGrossValueMethods(this.fixedCharge);
    mixinGrossValueMethods(this.chargeRate);

    this.isSelected = function() {
        var toInt = function(val) {
            return "" === val || isNaN(val) ? 0 : parseInt(val);
        };

        return (toInt(this.minCharge.discount)
                   + toInt(this.fixedCharge.discount)
                   + toInt(this.chargeRate.discount)) > 0;
    };

    this.hasChanged = false;
}

function nullToEmpty(obj) {
    return undefined == obj || null == obj ? "" : obj;
}
