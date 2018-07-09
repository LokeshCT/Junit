var rsqe = rsqe || {};

// Enable multiple selection for the countryFilter drop down element.
rsqe.setupMultiSelect = function () {
    $(document).ready(function () {
        $("#countryFilter").select2({
            placeholder: "--Please Select--"
        });
    });
};

rsqe.AddProducts = function () {
    $('#Modify-product').empty();
    $('#Move-product').empty();
    $('#Migrate-product').empty();
    tabObject = eval("new rsqe.AddModifyProducts");
    tabObject.initialise();
};

rsqe.ModifyProducts = function () {
    $('#Add-product').empty();
    $('#Move-product').empty();
    $('#Migrate-product').empty();
    tabObject = eval("new rsqe.AddModifyProducts");
    tabObject.initialise();
};

rsqe.MoveProducts = function () {
    $('#Add-product').empty();
    $('#Modify-product').empty();
    $('#Migrate-product').empty();
    tabObject = eval("new rsqe.AddModifyProducts");
    tabObject.initialise();
};

rsqe.MigrateProducts = function () {
    $('#Add-product').empty();
    $('#Modify-product').empty();
    $('#Move-product').empty();
    tabObject = eval("new rsqe.AddModifyProducts");
    tabObject.initialise();
};

rsqe.AddModifyProducts = function () {
    this.productAction = $("#productAction");
    this.globalSearch = $("#globalSearch");
    this.cancelButton = $('.cancel');
    this.itemCreationForm = $('#itemCreationForm');
    this.submitButton = $('.submit');
    this.productCode = $('.product');
    this.rsqeQuoteOptionId = $('.rsqeQuoteOptionId');
    this.productScode = $('.productSCode');
    this.productVersion = $('.productVersion');
    this.isProductImportable = $('.isProductImportable');
    this.redirectUri = $('.redirectUri');
    this.getSitesUri = $('.getSitesUri');
    this.getLaunchStatusUri = $('.getLaunchStatusUri');
    this.getCreateProductUri = $('.getCreateProductUri');
    this.cardinalityCheckUri = $('.cardinalityCheckUri');
    this.siteSelectedForProductCheckUri = $('.siteSelectedForProductCheckUri');
    this.endOfLifeCheckUri = $('.endOfLifeCheckUri');
    this.expedioQuoteId = $('.expedioQuoteId');
    this.expedioCustomerId = $('.expedioCustomerId');
    this.expedioContractId = $('.expedioContractId');
    this.authenticationToken = $('.authenticationToken');
    this.revenueOwner = $('.revenueOwner');
    this.checkedSites = $('input:checked');
    this.quoteOptionContext = $('.quoteOptionContext');
    this.siteCheckBoxes = $('.choice');
    this.sitesTable = '#siteTable';
    this.customerId = $('#expedioCustomerId').attr('value');
    this.projectId = $('#expedioQuoteId').attr('value');
    this.quoteOptionId = $('#rsqeQuoteOptionId').attr('value');
    this.quoteOptionCurrency = $('#rsqeQuoteOptionCurrency');
    this.quoteOptionName = $('#rsqeQuoteOptionName');
    this.staticHeight = 330;
    this.applyFilterButton = $("#applyFilterButton");
    this.clearFilterButton = $("#clearFilterButton");
    this.categoryGroupFilter = $('#categoryGroupFilter');
    this.categoryFilter = $("#categoryFilter");
    this.categoryGroupSearch = $("#categoryGroupSearch");
    this.categorySearch = $("#categorySearch");
    this.helpLink = $('#helpLink');
    this.countryFilter = $("#countryFilter");
    this.complianceCheckBox = $("#complianceCheckBox");
    this.selectResignCheckBox = $("#selectResignCheckBox");
    this.orderType = $("#orderType");
    this.subOrderType = $("#subOrderType");
    this.complianceCheckPanel = $("#complianceCheckPanel");
    this.prerequisiteUrl = $("#prerequisiteUrl");
    this.channelLaunchStatus = null;
    this.selectNewSiteDialogUri = $('#selectNewSiteDialogUri');
    this.spinner = $('#creating-product-spinner');
    this.unsupportedCountriesArray = new Array();
    this.specialBidCountriesArray = new Array();
    this.servicesTable = 'serviceTable';
    this.getServicesUrl = $('.getServicesUri');
    this.bulkTemplateExportButton = $('#bulkTemplateExport');
    this.bulkTemplateExportUrl = $('.bulkTemplateExportUri');
    this.productMoveConfigurationType = $('.productMoveConfigurationType');
    this.contractTermForMove = $('.contractTermForMove');
    this.warningDialogContainer = $("#warningDialog");
    this.warningMessages = $("#warningMessages", this.warningDialogContainer);
    this.continueMoveButton = $("#continueMove");
    this.cancelMoveButton = $("#cancelMove");
    this.addImportProductDialog = $("#addImportProductDialog");
    this.addImportProductForm = $("#addImportProductForm");
    this.addImportProductFormFileInput = '#eCRFSheet';
    this.addImportProductFormFileName = '#eCRFSheetWorkBookName';
    this.validateImportProductURL = $('.validateImportProductURL');
    this.userImportStatusURL = $('.userImportStatusURL');
    this.validateUserImportProductURL = $('.validateUserImportProductURL');
    this.userImportProductURL = $('.userImportUri');
    this.addImportProductTargetIFrame = $("#importProductTarget");
    this.importProgressDialogInstance = $("#importProgressDialog");
    this.importProgressDialogContainer = $("#importProgressDialog");

    this.bulkImportProductDialog = $("#bulkImportProductDialog");
    this.bulkImportProductForm = $("#bulkImportProductForm");
    this.bulkImportProductFormFileInput = '#bulkSheet';
    this.bulkImportProductFormFileName = '#bulkWorkBookName';

    this.errorMessages = $("#errorMessages", this.importProgressDialogContainer);
    this.bulkTemplateUserExportButton = $('#bulkTemplateUserExport');
    this.bulkTemplateUserExportUrl = $('.userExportUri');
    this.productAvailabilityCheckUrl = $('.productAvailabilityUri');
    this.hideButtons=$('#hideButtons');
    this.importProgressDialog = new rsqe.ProgressDialog("#importProgressDialog", {
        progressText:"#progressText",
        progressButton:"#close"
    });

    this.warningDialog = new rsqe.ProgressDialog("#warningDialog", {
        warningText:"#warningText",
        yesButton:"#continueMove",
        noButton:"#cancelMove"
    });
    this.setupWarningDialog();
    this.language = {
        sInfo:"Showing _START_ to _END_ of _TOTAL_ sites",
        sInfoEmpty:"Showing 0 to 0 of 0 sites",
        sInfoFiltered:"- filtered from _MAX_ sites",
        sZeroRecords:"Please select a product family, variant and offering",
        sLengthMenu:"Show _MENU_ sites"
    };

    this.columnMetaData = [
         { "mDataProp":"id"},
         { "mDataProp":"site"},
         { "mDataProp":"fullAddress"}
    ];

    if (this.productAction[0].defaultValue == "Move") {
        this.columnMetaData.push({ "mDataProp":"newSite"});
        this.columnMetaData.push({ "mDataProp":"newFullAddress"});
    } else if (this.productAction[0].defaultValue == "Modify") {
        this.columnMetaData.push({ "mDataProp":"summary"});
    }

    var checkCallback = function () {
        $('#selectAll').attr('title', 'Select None');
    };

    var unCheckCallback = function () {
        $('#selectAll').attr('title', 'Select All');
    };

    this.addProductCheckBoxes = new rsqe.CheckboxGroup("input[name='Ids']", {
        select_all:"#siteTable_wrapper #selectAll",
        //actionButtons:$("#submit"),
        someChecked:checkCallback,
        allUnchecked:unCheckCallback
    });

    this.dataTableObj = null;
    this.siteHeaders = '.siteHeader';
    this.serviceHeaders = '.serviceHeader';
    this.wrapper = '#siteTable_wrapper';
    this.numberOfProducts = '#numberOfProductsPanel';
    this.getProductAttributesUri = $('.getProductAttributesUri');
    this.expedioQuoteVersion = $('.expedioQuoteVersion');
};


rsqe.AddModifyProducts.prototype = {

    initialise:function () {
        var that = this;
        this.setupLineItemCreation();
        this.setupTable();
        this.setResign();
        this.setUpEvents();
        new rsqe.RightPane().initialize();
        this.setFormAction();
        this.applyTableHeight();
        rsqe.setupMultiSelect();
        this.setupAddImportProductDialog();
        this.setupImportProgressDialog();
        this.setupAddUserImportProductDialog();
        $('#categoryFilterPanel').hide();
        $("#productFilterPanel").hide();
        $("#filterPanel").hide();
        $("#resignPanel").hide();
        $("#complianceCheckPanel").hide();
        $('#helpLink').hide();
        that.setIsProductUserImportable(false, false);

        this.selectNewSiteDialog = $("#selectNewSiteDialog");
        this.selectNewSiteDialogInstance = new rsqe.Dialog(this.selectNewSiteDialog, {width:1000, height:"auto"});
        this.selectNewSiteForm = new rsqe.NewSiteForm({cancelHandler: function() {
            that.selectNewSiteDialogInstance.close();
        }});

        $('#productCounter').text() != '0' ? $('#bulkConfigurationButton').removeClass('disabled') : $('#bulkConfigurationButton').addClass('disabled');
        $('#bulkTemplateExport').addClass('disabled');
        $('#bulkTemplateUserExport').addClass('disabled');
        $(this.serviceHeaders).hide();
    },

    configureDataTable: function(source) {
        var that = this;
        var productJson = that.productJson();
        var processServices = function(services) {
            _.each(services, function(service) {
                service = $.extend(service, service.attributes);
            });
            return services;
        };
        $(this.sitesTable).dataTable({
                 sPaginationType:"full_numbers",
                 sAjaxSource:source,
                 sDom:'lrt<"table_footer"ip>',
                 bProcessing:true,
                 bServerSide:true,
                 bSort:false,
                 bLengthChange:true,
                 bDeferRender:true,
                 bStateSave:false,
                 iDisplayStart:0,
                 aLengthMenu: [
                    [10, 25, 50, 100, -1],
                    [10, 25, 50, 100, "All"]
                 ],
                 iDisplayLength:10,
                 iScrollLoadGap:50,
                 aoColumns:that.columnMetaData,
                 bDestroy: true,
                 sAjaxDataProp:function (data) {
                     return data.sites ? $.makeArray(data.sites) : processServices($.makeArray(data.services));
                 },
                 sScrollY:($(window).height() - that.staticHeight) + "px",
                 bFilter:true,
                 fnRowCallback:function (row, aData) {
                     return that.fnRowCallback(row, aData);
                 },
                 fnDrawCallback:function (settings) {
                     $(window).unbind('resize').bind('resize', function () {
                         that.applyTableHeight();
                     });
                     $("#selectAll").unbind('change');
                     that.addProductCheckBoxes.initialize();
                     if (that.productAction.val() == 'Modify' && !productJson.isSiteSpecific && $('input[name="Ids"]').length ==1) {
                         $('input[name="Ids"]').click();
                     }
                     $("#selectAll").change(function () {
                         that.calculateSubmitButtonStatus();
                         that.calculateAddImportButtonStatus();
                         if ($("#productAction").val() == "Move") {
                             that.calculateNewSiteButtonStatus();
                         }
                     });
                     that.calculateSubmitButtonStatus();
                     that.calculateAddImportButtonStatus();
                     if ($("#productAction").val() == "Move") {
                         $('#selectNewSiteButton').remove();
                         $('.dataTables_scrollBody').append('<input type="button" class="newSite" id="selectNewSiteButton" style="margin-top:20px" value="Select New Site">')
                         that.calculateNewSiteButtonStatus();
                         this.selectNewSiteButton = $("#selectNewSiteButton");
                         this.selectNewSiteButton.click(function() {
                             that.loadNewSiteDialog("Select New Site");
                             that.selectNewSiteDialogInstance.open();
                         });
                     }
                     that.displayComplianceCheckPanel($('.dataTables_empty').length == 0 ? productJson.isComplianceCheckNeeded : false , productJson.prerequisiteUrl);
                 },
                 oLanguage:this.language,
                 fnServerParams:function (data) {
                     //discriminator for getSites.
                     data.push({"name":"productAction", "value":that.productAction.val()},
                               {"name":"globalSearch", "value":$("#globalSearch").val()});
                     if (!_.isEmpty(productJson)) {
                         if (productJson.sCode) {
                             data.push({"name":"forProduct", "value":productJson.sCode});
                         }
                         if (!productJson.isSiteSpecific || $("#productAction").val() == "Modify") {
                            data.push({name:"productVersion", value: productJson.productVersion});
                         }
                     }
                 }
             });

        $('#siteGlobalSearchBtn').click(function() {
            $(that.sitesTable).DataTable().fnDraw();
        });

    },
    setupTable:function () {
        this.configureDataTable(this.getSitesUri.val());
    },

    setResign:function () {
        if(this.orderType.val() == "Provide" || this.orderType.val() == "Cease") {
            $("#selectResignCheckBox").disableable('enable', false);
        }else if(this.orderType.val() == "Modify" && this.subOrderType.val() == "Contract Resign"){
            $("#selectResignCheckBox").prop("checked", true).disableable('enable', false);
        }else{
            $("#selectResignCheckBox").disableable('enable', true);
        }
    },
    applyTableHeight:function () {
        var sScrollY = ($(window).height() - this.staticHeight) + "px";
        $(".dataTables_scrollBody").css("height", sScrollY);
    },
    fnRowCallback:function (row, aData) {
        var that = this;
        this.unsupportedCountriesArray = [];
        this.specialBidCountriesArray = [];
        var productData = that.productJson();
        $.each($("td", row), function (i, td) {
            var columnName = that.columnMetaData[i].mDataProp;
            if (columnName == "id") {
                var cssClass = "choice";
                var unavailableCountries = "";
                var specialBidCountries = "";
                if (productData.isSiteSpecific) {
                    if (!aData.isValidForProduct || aData.isValidForProduct == "false") {
                        if (!aData.isValidForSpecialBidProduct || aData.isValidForSpecialBidProduct == "false") {
                            cssClass += " invalid-country-for-product";
                            unavailableCountries += aData.country.toString();
                        } else {
                            if (!aData.isSpecialBidProduct || aData.isSpecialBidProduct == "false") {
                                cssClass += " try-special-bid-product";
                                specialBidCountries += aData.country.toString();
                            }
                        }
                    }
                }
                var input = $("<input type='checkbox' sourceLineItemId='"+ aData.sourceLineItemId +"' sourceQuoteOptionId='" + aData.sourceQuoteOptionId + "'  country='"+ aData.country+"' specialBidCountries='" + specialBidCountries + "' unavailableCountries='" + unavailableCountries + "' class='" + cssClass + "' name='Ids' value='" + aData.id + "' />");
                $(input).bind('click', function () {
                    that.calculateSubmitButtonStatus();
                    that.calculateAddImportButtonStatus();
                    if ($("#productAction").val() == "Move") {
                        that.calculateNewSiteButtonStatus();
                    }
                });
                $(td).html(input);
                $(td).addClass("checkbox");
            } else {
                $(td).addClass(columnName);
            }
        });
        $(row).addClass("siteLine");
        $(row).attr("id", "id_" + aData.id);
        $(row).attr("newSiteId", aData.newSiteId);
        if( aData.isPartialSite) {
            $(row).addClass("invalid");
        }
        return row;
    },
    productJson:function (val) {
        var value = val ? val : this.productCode.val();
        return $.parseJSON(value == "" ? "{}" : value);
    },
    hideFilterPanel:function () {
        $("#filterPanel").hide();
        this.staticHeight = 330;
    },
    changeToSiteTable:function() {
        var that = this;
        $(that.numberOfProducts).hide();
        $("#countryFilter").val('').change();
        $("#filterPanel").show();
        $("#resignPanel").show();
        that.staticHeight = 370;
        $("#siteTable td.dataTables_empty").text("Please select a filter value");
        $(that.serviceHeaders).remove();
        if ($(that.siteHeaders).length == 0) {
            that.columnMetaData = [];
            that.columnMetaData.push({ "mDataProp":"id"});
            $('thead:eq(0) tr').append('<th class="siteHeader">Site Name</th>');
            that.columnMetaData.push({ "mDataProp":"site"});
            $('thead:eq(0) tr').append('<th class="siteHeader">Site Address</th>');
            that.columnMetaData.push({ "mDataProp":"fullAddress"});
            if (that.productAction.val() == 'Modify') {
               $('thead:eq(0) tr').append('<th class="siteHeader">Summary</th>');
               that.columnMetaData.push({ "mDataProp":"summary"});
            }
            if (that.productAction.val() == 'Move') {
                $('thead:eq(0) tr').append('<th class="siteHeader">New Site Name</th>');
                that.columnMetaData.push({ "mDataProp":"newSite"});
                $('thead:eq(0) tr').append('<th class="siteHeader">New Site Address</th>');
                that.columnMetaData.push({ "mDataProp":"newFullAddress"});
            }
        }
        that.configureDataTable(that.getSitesUri.val());
    },
    changeToServiceTable:function() {
        var that = this;
        $("#resignPanel").show();
        $(that.siteHeaders).remove();
        $.ajax(that.getProductAttributes(function(attributes) {
            $(that.serviceHeaders).remove();
            var names = JSON.parse(attributes).names;
            var columns = [
                { "mDataProp":"id"},
                { "mDataProp":"name"}
            ];
            $('thead:eq(0) tr').append('<th class="serviceHeader">Product</th>');
            for(var i = 0; i < names.length; i++) {
                $('thead:eq(0) tr').append('<th class="serviceHeader">'+names[i]+'</th>');
                columns.push({"mDataProp":names[i].toLowerCase()});
            }
            that.columnMetaData = columns;
            that.configureDataTable(that.getServicesUrl.val());
        }));
    },
    setupLineItemCreation:function () {
        var that = this;
        that.launchedCount = 0;
        this.productCode.change(function () {
            var productData = that.productJson();
            that.setFormAction(productData.creationUrl);
            that.setProductSCode(productData.sCode);
            that.setProductVersion(productData.productVersion);
            that.setIsProductImportable(productData.isImportable);
            that.setMoveConfigurationType(productData.moveConfigurationType);
            that.setRollOnContractTermForMove(productData.rollOnContractTermForMove);
            that.countryFilter.val('--Please Select--');
            that.complianceCheckPanel.hide();
            that.setIsProductUserImportable(productData.isImportable, productData.isUserImportable);
            if (productData.isSiteSpecific) {
                that.changeToSiteTable();
                $(that.wrapper).toggle(true);
            } else if('Modify' != that.productAction.val()) {
                if (productData.contractCardinality > 1) {
                    $(that.numberOfProducts).show();
                    $("#resignPanel").show();
                }
                $(that.wrapper).toggle(false);
                that.displayComplianceCheckPanel(productData.isComplianceCheckNeeded, productData.prerequisiteUrl);
            } else {
                if ($(this.selectedOptions).html() != '--Please Select--') {
                    that.changeToServiceTable();
                    $(that.wrapper).toggle(true);
                }
            }
            if (that.productCode.val() == "" || !productData.isSiteSpecific) {
                that.hideFilterPanel();
            }
            that.applyTableHeight();
            if(!_.isUndefined(productData.sCode)
                    && "true"==productData.isImportable){
                $('#bulkTemplateExport').removeClass('disabled');
            }else if(!_.isUndefined(productData.sCode) && "true"==productData.isUserImportable){
                $('#bulkTemplateUserExport').removeClass('disabled');
            }
            else{
                $('#bulkTemplateExport').addClass('disabled');
                $('#bulkTemplateUserExport').addClass('disabled');
            }
        });

        this.submitButton.click(function () {

            var productAction = $("#productAction").val();

            var newSiteId = "";
            if (productAction == "Move") {
                newSiteId = $("#siteTable").dataTable()[0].rows[1].attributes.getNamedItem("newsiteid").nodeValue;
            }
            var contractResignStatus = ($("#selectResignCheckBox").prop("checked")) ? "Yes" : "No";

            var jsonObject = {
                "quoteOptionId":that.rsqeQuoteOptionId.val(),
                "expedioQuoteId":that.expedioQuoteId.val(),
                "customerId":that.expedioCustomerId.val(),
                "contractId":that.expedioContractId.val(),
                "currency":that.quoteOptionCurrency.val(),
                "authenticationToken":that.authenticationToken.val(),
                "rsqeQuoteOptionName":that.quoteOptionName.val(),
                "revenueOwner":that.revenueOwner.val(),
                "productCode":that.productScode.val(),
                "productVersion":that.productVersion.val(),
                "lineItems":[],
                "redirectUri":that.redirectUri.val(),
                "action":that.productAction.val(),
                "channelLaunchStatus":"",
                "newSiteId":newSiteId,
                "isImportable":that.isProductImportable.val(),
                "moveConfigurationType":that.productMoveConfigurationType.val(),
                "rollOnContractTermForMove":that.contractTermForMove.val(),
                "isUserImportable": undefined == that.isUserImportable ? "false" : that.isUserImportable.val(),
                "productCategoryCode": that.categoryFilter.val(),
                "contractResignStatus": contractResignStatus
            };

            $('input:radio:checked').each(function () {
                productAction  = $("#productAction").val();
            });

            if (that.productJson().isSiteSpecific) {
                $('input:checkbox:checked:not(#selectAll):not(#complianceCheckBox):not(#showHiddenSites):not(#selectResignCheckBox)').each(function () {
                    var sourceLineItemId = $(this).attr('sourceLineItemId');
                    var sourceQuoteOptionId = $(this).attr('sourceQuoteOptionId');
                    var siteId = $(this).val();
                    jsonObject.lineItems.push({"siteId":siteId, "action":productAction, "lineItemId":sourceLineItemId, "quoteOptionId":sourceQuoteOptionId});
                });
            }
            else {
                if (productAction == 'Modify') {
                    $('input:checkbox:checked:not(#selectAll):not(#complianceCheckBox):not(#showHiddenSites):not(#selectResignCheckBox)').each(function () {
                       var sourceQuoteOptionId = $(this).attr('sourceQuoteOptionId');
                       jsonObject.lineItems.push({"lineItemId":$(this).val(), "action":productAction, "quoteOptionId":sourceQuoteOptionId});
                    });
                } else {
                    for (var i = 0; i < parseInt($(that.numberOfProducts+ ' input').val()); i++) {
                        jsonObject.lineItems.push({"action":productAction});
                    }
                }
            }

            that.quoteOptionContext.val($.toJSON(jsonObject));
            //that.selectedSiteIsLaunched(that.quoteOptionContext, that.launchedCount);

            $("#commonError").html('');
            $("#commonError").addClass("hidden");

            $("#commonWarning").html('');

            that.spinner.show();
            if( 'Add' == that.productAction.val() ) {
                $.when($.ajax(that.getLaunchStatus()), $.ajax(that.cardinalityCheck())).then(addASuccessCallback, that.failCallback);
                $('#hideScreen').append('<div id="screenHide" style="background-color:grey;position: absolute;top:0;left:0;width: 100%;height:100%;z-index:2;opacity:0.4;filter: alpha(opacity = 100)"></div>');

            } else if( 'Modify' == that.productAction.val() ){
                if("Yes"==contractResignStatus){
                    $.when($.ajax(that.getLaunchStatus()),$.ajax(that.endOfLifeCheck())).then(modifySuccessCallBack, that.failCallback);
                }else{
                    $.when($.ajax(that.createProduct())).then(that.createProductSuccessCallback, that.failCallback);
                }
               $('#hideScreen').append('<div id="screenHide" style="background-color:grey;position: absolute;top:0;left:0;width: 100%;height:100%;z-index:2;opacity:0.4;filter: alpha(opacity = 100)"></div>');

            } else if( 'Move' == that.productAction.val() ){
                $.when($.ajax(that.getLaunchStatus()), $.ajax(that.cardinalityCheck()), $.ajax(that.endOfLifeCheck())).then(moveSuccessCallback, that.failCallback);
                $('#hideScreen').append('<div id="screenHide" style="background-color:grey;position: absolute;top:0;left:0;width: 100%;height:100%;z-index:2;opacity:0.4;filter: alpha(opacity = 100)"></div>');

            } else {
                $.when($.ajax(that.getLaunchStatus()), $.ajax(that.cardinalityCheck())).then(addASuccessCallback, that.failCallback);
                $('#hideScreen').append('<div id="screenHide" style="background-color:grey;position: absolute;top:0;left:0;width: 100%;height:100%;z-index:2;opacity:0.4;filter: alpha(opacity = 100)"></div>');
            }

            function addASuccessCallback(data1, data2) {
                if( data1[0] == 'Yes' && !$.parseJSON(data2[0]).hasError) {
                    $.when($.ajax(that.createProduct())).then(that.createProductSuccessCallback, that.failCallback);
                } else {
                    that.spinner.hide();
                    $('#screenHide').removeClass();
                }
            }


            function modifySuccessCallBack(data1,data2) {
                if(data1[0] == 'Yes' && !$.parseJSON(data2[0]).hasError) {
                    if ($.parseJSON(data2[0]).hasWarning) {
                        that.warningMessages.text($.parseJSON(data2[0]).warnings[0]).removeClass("hidden");
                        that.warningDialogInstance.open();
                        that.spinner.hide();
                        $('#screenHide').removeClass();
                    } else {
                        $.when($.ajax(that.createProduct())).then(that.createProductSuccessCallback, that.failCallback);
                    }
                } else {
                    that.spinner.hide();
                    $('#screenHide').removeClass();
                }
            }

            function moveSuccessCallback(data1, data2, data3) {
                if (data1[0] == 'Yes' && !$.parseJSON(data2[0]).hasError && !$.parseJSON(data3[0]).hasError) {
                    if ($.parseJSON(data3[0]).hasWarning) {
                        that.warningMessages.text($.parseJSON(data3[0]).warnings[0]).removeClass("hidden");
                        that.warningDialogInstance.open();
                        that.spinner.hide();
                         $('#screenHide').removeClass();
                    } else {
                        $.when($.ajax(that.createProduct())).then(that.createProductSuccessCallback, that.failCallback);
                    }
                } else {
                    that.spinner.hide();
                     $('#screenHide').removeClass();
                }
            }
        });

        this.hideButtons.click(function(){

        if(document.getElementById('hideButtons').style.backgroundPosition==="0px -195px"){
         document.getElementById('hideButtons').style.backgroundPosition="0px -210px";
         $("#bulkTemplateUserExport").removeClass ('hidden');
                        $("#addUserImportButton").removeClass ('hidden');

                        }

               else{
               document.getElementById('hideButtons').style.backgroundPosition="0px -195px";
               $("#addUserImportButton").addClass ('hidden');
                       $("#bulkTemplateUserExport").addClass ('hidden');
                       document.getElementById('hideButtons').style.backgroundPosition="0px -195px";
                       }
            });

        this.bulkTemplateExportButton.click(function () {
            var productSCode = that.productScode.val();
            var exportUrl = that.bulkTemplateExportUrl.val();
            exportUrl=exportUrl.replace("(productSCode)", productSCode);

            if(!_.isNull(productSCode)){
                console.log(exportUrl);
                window.location = exportUrl;
            }else{
                console.log("Failed to Download" +exportUrl);
            }


        });

        this.bulkTemplateUserExportButton.click(function () {
            var productSCode = that.productScode.val();
            validateProductImportSubmission()
            function validateProductImportSubmission(){
                var availabilityCheckUrl = that.productAvailabilityCheckUrl.val();
                availabilityCheckUrl = availabilityCheckUrl.replace("(productSCode)",productSCode);
                $.ajax({
                           type:"POST",
                           url: availabilityCheckUrl,
                           success: function(data) {
                               processProductAvailabilityResult(data);
                           },
                           error: function(data) {
                               processProductAvailabilityResult(data)
                           }
                       });
            }
            function processProductAvailabilityResult(data) {
                if(data.hasError == true ){
                    var errors = $("#commonError");
                    var warnings = $("#commonWarning");
                    $("#commonError").html('');

                    $.each(data.errors, function (index) {
                        errors.html(errors.html() + data.errors[index] + ".<br/>");
                    });

                    if (data.hasError == true) {
                        errors.removeClass("hidden");
                    }

                    $.each(data.warnings, function (index) {
                        warnings.html(warnings.html() + data.warnings[index] + ".<br/>");
                    });
                } else {
                    exportProduct();
                }
            }
            function exportProduct() {
                var exportUrl = that.bulkTemplateUserExportUrl.val();
                exportUrl=exportUrl.replace("(productSCode)", productSCode);
                if(!_.isNull(productSCode)){
                    console.log(exportUrl);
                    window.location = exportUrl;
                }else{
                    console.log("Failed to Download" +exportUrl);
                };
            }
        });
    },
    getLaunchStatus: function() {
        var that = this;
        var productData = that.productJson();

        return {
            type:'GET',
            url:that.getLaunchStatusUri.val(),
            dataType:'text',
            data:{salesChannel:'' + that.revenueOwner.val() + '', productSCode:'' + productData.sCode + ''},
            success: function(data) {
                that.processLaunchStatusResult(data);
            },
             error: function(data) {
                    $("#screenHide").remove();
                                    }

        };
    },
    siteSelectedForProductCheck: function() {
        var that = this;
        return {
            type : 'POST',
            url : that.siteSelectedForProductCheckUri.val(),
            dataType:'text',
            data: that.quoteOptionContext.val(),
            success: function(data) {
                that.processSiteSelectedForProductResult(data);
            }
        };
    },
    cardinalityCheck: function() {
        var that = this;
        return {
            type : 'POST',
            url : that.cardinalityCheckUri.val(),
            dataType:'text',
            data: that.quoteOptionContext.val(),
            success: function(data) {
                that.processCardinalityResult(data);
            },
            error: function(data) {
                       $("#screenHide").remove();
                                  }
        };
    },
    endOfLifeCheck: function() {
        var that = this;
        return {
            type : 'POST',
            url : that.endOfLifeCheckUri.val(),
            dataType:'text',
            data: that.quoteOptionContext.val(),
            success: function(data) {
                that.processEndOfLifeResult(data);
            },
            error: function(data) {
                      $("#screenHide").remove();
                                  }
        };
    },
    createProduct: function() {
        var that = this;
        var productData = {quoteOptionContext: that.quoteOptionContext.val()};
        return {
            type : 'POST',
            url : that.getCreateProductUri.val(),
            dataType:'text',
            data: productData
        };
    },
    processLaunchStatusResult:function (data) {

        var errors = $("#commonError");
        if (data == 'No') {
            errors.removeClass("hidden");
            errors.html("Service is not launched for the channel " + this.revenueOwner.val() + ".");
            $("#screenHide").remove();
        }
    },
    processCardinalityResult:function (data) {
        var errors = $("#commonError");
        var result = $.parseJSON(data);

        $.each(result.errors, function (index) {
            errors.html(errors.html() + result.errors[index] + ".<br/>");
        });

        if (result.hasError == true) {
            errors.removeClass("hidden");
            $("#screenHide").remove();
        }
    },
    processEndOfLifeResult:function (data) {
        var errors = $("#commonError");
        var warnings = $("#commonWarning");
        var result = $.parseJSON(data);

        $.each(result.errors, function (index) {
            errors.html(errors.html() + result.errors[index] + ".<br/>");
        });

        if (result.hasError == true) {
            errors.removeClass("hidden");
            $("#screenHide").remove();
        }

        $.each(result.warnings, function (index) {
            warnings.html(warnings.html() + result.warnings[index] + ".<br/>");
        });
    },
    processSiteSelectedForProductResult:function (data) {
        var errors = $("#commonError");
        var result = $.parseJSON(data);

        $.each(result.errors, function (index) {
            errors.html(errors.html() + result.errors[index] + ".<br/>");
        });

        if (result.hasError == true) {
            errors.removeClass("hidden");
        }
    },
    createProductSuccessCallback: function(data) {
       $('#creating-product-spinner').hide();
       $("#screenHide").remove();
       $('.productCounterWrapper').effect('highlight', {}, 3000);
       $('#productCounter').text(data);
       $("#bulkConfigurationButton").addClass('enabled').removeClass('disabled');
       $("#bulkConfigurationButton").disableable('enable');

    },
    failCallback:function (jqXHR, textStatus, object) {
        $("#screenHide").remove();
        $('#creating-product-spinner').hide();
        $("#commonError").removeClass("hidden");
        $("#commonError").html(jqXHR.responseText);
    },
    setUpEvents:function () {
        var that = this;

        var optionsToJson = function(selectElement) {
            var options = [];

            selectElement.find('option').each(function() {
                options.push({value: $(this).val(), text: $(this).text(), orderPreRequisiteUrl: $(this)[0].getAttribute('orderPreRequisiteUrl')});
            });

            return options;
        };

        var searchOptionsToJson = function(selectElement) {
             var searchOptions = [];
               selectElement.find('option').each(function() {
                             searchOptions.push({value: $(this).val(), label: $(this).text(), orderPreRequisiteUrl: $(this)[0].getAttribute('orderPreRequisiteUrl')});
                         });

                        // alert("SearchOptionsToJson "+searchOptions.length);
             return searchOptions;
         };


        var resetDropDown = function(selectElement) {
            selectElement.find('option').first().attr('selected', 'selected');
            selectElement.change();
        };

        var resetSearchFields = function(selectElement) {
            selectElement.val('');
            selectElement.change();
        };

        var resetMultiselect = function(selectElement){
            selectElement.val('').change();
        };

        var categoryAllowed = function(categoryGroup, category) {
            var match = _.find(productOptions, function(productOption) {
                var optionJson = that.productJson(productOption.value);
                return categoryGroup == optionJson.categoryGroupCode && category == optionJson.categoryCode;
            });

            return undefined !== match;
        };

        var configureDropDownAndPanel = function(panelIdentifier, selectedValue, allowedOptions, selectElement, optionAllowedFn, onShowFn) {
            if(selectedValue && null != selectedValue && "" != selectedValue) {
                selectElement.empty().scrollTop(0);

                _.each(allowedOptions, function(allowedOption) {
                   if("" == allowedOption.value || optionAllowedFn(selectedValue, allowedOption.value)) {
                        selectElement.append($('<option>').text(allowedOption.text).val(allowedOption.value));
                   }
                });

                if(onShowFn) {
                    onShowFn();
                }

                $(panelIdentifier).show();
            } else {
                $(panelIdentifier).hide();
            }
        };

        // Store the initial set of Categories & Products so we can filter later
        var categoryGroupOptions = optionsToJson(this.categoryGroupFilter);
        var productOptions = optionsToJson(that.productCode);
        var categoryOptions = optionsToJson(this.categoryFilter);

        var categoryGroupSearchOptions = searchOptionsToJson(this.categoryGroupFilter);
        var categorySearchOptions = [];
        var productSearchOptions = [];

    $(function() {
         $( "#categoryGroupSearch" ).autocomplete({
            minLength: 0,
            source: categoryGroupSearchOptions,
             focus: function( event, ui ) {
               $( "#categoryGroupSearch" ).val( ui.item.label);
                  return false;
               },
            select: function( event, ui ) {
                //Set values to input fields
               $( "#categoryGroupSearch" ).val( ui.item.label );
               $( "#categoryGroup-id" ).val( ui.item.value );
               $( "#categoryGroupUrl" ).val( ui.item.orderPreRequisiteUrl);
               $( "#categoryGroupFilter" ).val( ui.item.value ); // select the product family i.e. categoryGroupDropdown
               adjustInputWidth($( "#categoryGroupSearch" )); // auto adjust input field width
                //enable or disable fields
                configureDropDownAndPanel("#categoryFilterPanel",
                                                      $( "#categoryGroup-id" ).val(),
                                                      categoryOptions,
                                                      that.categoryFilter,
                                                      categoryAllowed);
                 $(that.numberOfProducts).hide();
                categorySearchOptions = searchOptionsToJson(that.categoryFilter); //set to new json format expected
                categoryOptionsSelect(categorySearchOptions);

               return false;
            },
            change: function(event, ui){
            $(that.numberOfProducts).hide();
            resetSearchFields($( "#categorySearch" ));
            configureSearchFieldAndPanel("#categoryFilterPanel","#productFilterPanel","#filterPanel",$( "#categoryGroupSearch" ).val());
            return false;
            }
         }).focus(function(){ $(this).autocomplete('search'); return false;});

    });


        var categoryOptionsSelect = function() {
         $( "#categorySearch" ).autocomplete({
          minLength: 0,
            source: categorySearchOptions,
            focus: function( event, ui ) {
            $( "#categorySearch" ).val( ui.item.label);
            return false;
            },
            select: function( event, ui ) {
             //Set values to input fields
            $( "#categorySearch" ).val( ui.item.label );
            $( "#category-id" ).val( ui.item.value );
            $( "#categoryUrl" ).val( ui.item.orderPreRequisiteUrl);
            $( "#categoryFilter" ).val( ui.item.value ); // select the product variant i.e. categoryDropdown
            adjustInputWidth($( "#categorySearch" )); // auto adjust input field width
              //enable or disable fields
             var  categoryCode = $( "#category-id" ).val();
             configureDropDownAndPanel("#productFilterPanel",
                                           categoryCode,
                                           productOptions,
                                           that.productCode,
                                           function(categoryCode, optionValue) {
                                            return categoryCode == that.productJson(optionValue).categoryCode;
                                           });
             $(that.numberOfProducts).hide();
             resetSearchFields($( "#productSearch" ));
              resetMultiselect(that.countryFilter);
              configureSearchFieldAndPanel("#productFilterPanel","#filterPanel","#",$( "#categorySearch" ).val());
            /* that.helpLink.hide();
             _.each(categoryOptions, function(categoryOption) {
                if (that.categoryFilter.val() == categoryOption.value && !_.isEmpty(categoryOption.orderPreRequisiteUrl)) {
                     var orderPreRequisiteUrl = categoryOption.orderPreRequisiteUrl;
                         that.helpLink.show();
                         that.helpLink.attr('href', orderPreRequisiteUrl);
                     }
                 });*/
                 productSearchOptions = searchOptionsToJson(that.productCode);
                 productOptionsSelect(productSearchOptions);

                return false;
             },
             change: function(event, ui){
                    $(that.numberOfProducts).hide();
                     resetSearchFields($( "#productSearch" ));
                     resetMultiselect(that.countryFilter);
                 configureSearchFieldAndPanel("#productFilterPanel","#filterPanel","#",$( "#categorySearch" ).val());
                 return false;
            }
          }).focus(function(){ $(this).autocomplete('search'); return false; });

     };

        var productOptionsSelect = function() {
         $( "#productSearch" ).autocomplete({
            minLength: 0,
            source: productSearchOptions,
            focus: function( event, ui ) {
               $( "#productSearch" ).val( ui.item.label);
                  return false;
            },
            select: function( event, ui ) {
                //Set values to input fields
               $( "#productSearch" ).val( $.trim(ui.item.label) );
               $( "#productSearch-id" ).val( ui.item.value );
               $( "#productSearchUrl" ).val( ui.item.orderPreRequisiteUrl);
               adjustInputWidth( $( "#productSearch" ));
               that.productCode.val( ui.item.value ); // select the product offering dropdown
               that.productCode.change();

               return false;
            },
           /* change: function(event, ui){
                    var productVal = $( "#productSearch" ).val();
                    //alert(productVal);
                    that.productCode.val( productVal ); // select the product offering dropdown
                    //that.productCode.change();
                     //$(that.numberOfProducts).hide(); --
                     //resetMultiselect(that.countryFilter); --
                    configureSearchFieldAndPanel("#filterPanel","#numberOfProductsPanel","#complianceCheckPanel",productVal);
              return false;
            }*/
         }).focus(function(){ $(this).autocomplete('search'); return false;});

   };


        var adjustInputWidth = function(fieldName){
            var fieldVal = fieldName.val();
            if ( fieldVal != '' &&  fieldVal.length > '25'){
                        fieldName.animate({
                        'width':((fieldVal.length) * 8) + 'px'
                        },'slow');
            }else{
                  fieldName.animate({
                    'width':'150px'
                    },'slow');
            }
        }

        var configureSearchFieldAndPanel = function(searchPanelIdentifier1, searchPanelIdentifier2, searchPanelIdentifier3, selectedSearchValue) {

        if(selectedSearchValue && null != selectedSearchValue && "" != selectedSearchValue) {
              //$(searchPanelIdentifier1).show();
        } else {
             $(searchPanelIdentifier1).hide();
             $(searchPanelIdentifier2).hide();
             $(searchPanelIdentifier3).hide();
       }
    };


        this.categoryGroupFilter.change(function() {
            // reset
            resetDropDown(that.categoryFilter);
            $(that.wrapper).show();

            // filter by category group
            var categoryGroupCode = that.categoryGroupFilter.val();

            configureDropDownAndPanel("#categoryFilterPanel",
                                      categoryGroupCode,
                                      categoryOptions,
                                      that.categoryFilter,
                                      categoryAllowed);
            $(that.numberOfProducts).hide();
        });

        this.categoryFilter.change(function () {
            // reset
            resetDropDown(that.productCode);
            resetMultiselect(that.countryFilter);
            $(that.wrapper).show();

            // filter by category
            var categoryCode = that.categoryFilter.val();

            configureDropDownAndPanel("#productFilterPanel",
                                      categoryCode,
                                      productOptions,
                                      that.productCode,
                                      function(categoryCode, optionValue) {
                                          return categoryCode == that.productJson(optionValue).categoryCode;
                                      });
            $(that.numberOfProducts).hide();
            that.helpLink.hide();
                _.each(categoryOptions, function(categoryOption) {
                    if (that.categoryFilter.val() == categoryOption.value && !_.isEmpty(categoryOption.orderPreRequisiteUrl)) {
                    var orderPreRequisiteUrl = categoryOption.orderPreRequisiteUrl;
                        that.helpLink.show();
                        that.helpLink.attr('href', orderPreRequisiteUrl);
                    }
                });
        });

        this.countryFilter.change(function () {
            var selectedCountry = that.countryFilter.val();
            var filterPattern = "";
            if(_.isNull(selectedCountry)){
                selectedCountry="";
            };

            if(selectedCountry != undefined && selectedCountry.length > 1 && ($.inArray("all", selectedCountry) == 0)){
                $("#countryFilter").select2('val', 'all');
                selectedCountry = that.countryFilter.val();
            }

            for (var i = 0; i < selectedCountry.length; i++) {
                filterPattern += selectedCountry[i] + ";";
            }
            filterPattern = filterPattern.substring(0, filterPattern.length - 1);
            $(that.sitesTable).dataTable().fnFilter("country="+filterPattern, null, true);

        });
        $("#bulkTemplateExport, #bulkTemplateUserExport").click(function(){
                   // $("#exportPricingSheet").addClass("disabled");
                    $("#export-bulk-template-msg").dialog({
                        modal:true,
                        buttons: {
                            OK: function() {
                            $(this).dialog("close");
                            }
                        }
                    });
                });


        this.complianceCheckBox.click(function () {
            if ($("#submit").is(":enabled")) $("#submit").disableable('enable', false);
            else $("#submit").disableable('enable', true);
        });

        this.selectResignCheckBox.click(function () {
            if ($("#selectResignCheckBox").is(":enabled")) $("#selectResignCheckBox").disableable('enable', false);
            else $("#selectResignCheckBox").disableable('enable', true);
        });

        this.continueMoveButton.click(function () {
            $.when($.ajax(that.createProduct())).then(that.createProductSuccessCallback, that.failCallback);
            that.warningDialogInstance.close();
            that.spinner.show();
        });

        this.cancelMoveButton.click(function () {
            that.warningDialogInstance.close();
            that.spinner.hide();
        });
    },

    setFormAction:function (creationUrl) {
        this.itemCreationForm.attr("action", creationUrl);
        this.calculateSubmitButtonStatus();
        this.calculateAddImportButtonStatus();
    },

    setProductSCode:function (productCode) {
        this.productScode.val(productCode);
    },

    setProductVersion:function (productVersion) {
        this.productVersion.val(productVersion);
    },

    setIsProductImportable:function (isImportable) {
        this.isProductImportable.val(isImportable);
        var allowedActions = ['Add', 'Migrate'];
        if (isImportable == "true" && _.contains(allowedActions , this.productAction.val()) ) {
            $('#addImportContainer').removeClass('hidden');
        } else {
            $('#addImportContainer').addClass('hidden');
        }
    },

    setMoveConfigurationType:function (moveConfigurationType) {
        this.productMoveConfigurationType.val(moveConfigurationType);
    },

    setRollOnContractTermForMove:function (rollOnContractTermForMove) {
        this.contractTermForMove.val(rollOnContractTermForMove);
    },

    newSiteDetailsRetrieved:function () {
        if (this.productAction[0].defaultValue == "Move") {
            if ($('#siteTable').dataTable().fnGetData(0, 3) == "") {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    },

    sitesSelectedIfNeeded:function () {
        if (!this.productJson().isSiteSpecific && this.productAction.val() != 'Modify' && !this.productJson().isComplianceCheckNeeded) return true;
        return this.canEnableSubmit();
    },

    calculateSubmitButtonStatus:function () {
        $("#submit").disableable('enable', this.selectedSiteCountriesAreAvailableForProduct() && this.sitesSelectedIfNeeded() && $('.product').val() !== "" && this.complianceChecked() && this.newSiteDetailsRetrieved());
    },
    calculateAddImportButtonStatus:function () {
        if (this.canEnableSubmit() || this.isProductImportable.val() != "true" || this.productAction.val() == 'Modify' ||  this.productAction.val() == 'Move') {
            $('#addImportContainer').addClass("hidden");
        } else {
            $('#addImportContainer').removeClass("hidden");
        }
    },
    calculateNewSiteButtonStatus:function () {
        if (this.canEnableSubmit() && $('#siteTable').dataTable().fnGetData(0, 3) == "") {
            $("#selectNewSiteButton")[0].disabled = false;
        } else {
            $("#selectNewSiteButton")[0].disabled = true;
        }
    },

    canEnableSubmit:function () {
        var selectedCheckBoxes = $('input:not(#selectAll,#complianceCheckBox):checkbox:checked').size();
        if (this.productJson().isComplianceCheckNeeded) {
            selectedCheckBoxes += $('#complianceCheckBox:checkbox').size();
        }
        return selectedCheckBoxes > 0;

    },

    setIsProductUserImportable:function (isImportable, isUserImportable) {
        this.isProductImportable.val(isImportable);
        var allowedActions = ['Add', 'Modify'];
        if (isImportable != "true" && isUserImportable == "true" && _.contains(allowedActions , this.productAction.val()) ) {
            $('#addUserImportContainer').removeClass('hidden');
        } else {
            $('#addUserImportContainer').addClass('hidden');
        }
    },

    setupAddImportProductDialog:function () {
        var that = this;
        that.addImportProductDialogInstance = new rsqe.Dialog(that.addImportProductDialog, {
            openers:"#addImportButton",
            title:"Import Product",
            height:150,
            width:360,
            close:function () {
                that.addImportProductTargetIFrame.attr("src", "about:blank");
                that.errorMessages.addClass("hidden");
            }
        });

        this.addImportProductvalidator = $(this.addImportProductForm).validate(
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

        var submitButton = this.addImportProductDialog.find("#addImportProductUpload");
        submitButton.click(function () {
            if (that.addImportProductvalidator.form()) {

                that.addImportProductTargetIFrame.load(onLoad);
                that.addImportProductDialogInstance.close();
                that.importProgressDialog.taskStarted("Validating Import ...");
                that.importProgressDialogInstance.open();
                $('#spinning').removeClass("hidden");

                var form = that.addImportProductDialog.find("form");
                var oldActionUri = form.attr('action');
                var productScode = that.productScode.val();
                form.attr('action', form.attr('action') + '/' + productScode + '/action/' + that.productAction.val() + '?productCategoryCode=' + that.categoryFilter.val());
                validateProductImportSubmission();
                var beforeSend = function() {
                    $(that.addImportProductFormFileName).val($(that.addImportProductFormFileInput).val().split('\\').pop());
                };

                function completed(header, errorMessages) {
                    resetFormAction(oldActionUri);
                    that.importProgressDialog.taskFinished(header);
                }

                function printValidationResult(header, errorMessages) {
                    if (errorMessages.length === 0) {
                        $('#spinning').addClass("hidden");
                        that.importProgressDialog.taskFinished(header);
                    } else {
                        that.importProgressDialog.taskFinishedWithErrors(header);
                        that.errorMessages.text(errorMessages).removeClass("hidden");
                    }
                }

                function resetFormAction(uri) {
                    form.attr('action', uri);
                }

                function performPostSubmissionActivities(){
                    resetFormAction(oldActionUri);
                    completed("Upload Initialized..", "");
                    $('#spinning').addClass("hidden");
                }

                function validateProductImportSubmission(){
                    var validationUrl = that.validateImportProductURL.val();
                    validationUrl = validationUrl +'/'+productScode;
                    $.ajax({
                               type:"GET",
                               url: validationUrl,
                               success: function(data) {
                                   verifyAddProductImportValidationResponse(data);
                               },
                               error: function(data) {
                                   verifyAddProductImportValidationResponse(data)
                               }
                           });
                }

                function verifyAddProductImportValidationResponse(data) {
                    if(data.successful && true == data.successful){
                        importProduct();
                    } else {
                        printValidationResult("Import Request Rejected..",data.errors);
                        $('#spinning').addClass("hidden");
                    }
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
            var response = that.addImportProductTargetIFrame.text();
            that.addImportProductTargetIFrame.unbind("load", onLoad);
        };
    },

    setupAddUserImportProductDialog:function () {
        var that = this;
        that.bulkImportProductDialogInstance = new rsqe.Dialog(that.bulkImportProductDialog, {
            openers:"#addUserImportButton",
            title:"Import Product",
            height:150,
            width:360,
            close:function () {
                that.errorMessages.addClass("hidden");
            }
        });

        this.bulkImportProductvalidator = $(this.bulkImportProductForm).validate(
                {
                    rules:{
                        bulkSheet:{
                            required:true,
                            accept:"xls|xlsx"
                        }
                    },
                    messages:{
                        bulkSheet:"An extension of .xls or .xlsx is required"
                    }

                }
        );

        var submitButton = this.bulkImportProductDialog.find("#bulkImportProductUpload");
        submitButton.click(function () {
            if (that.bulkImportProductvalidator.form()) {
                var productSCode = that.productScode.val();
                that.importProgressDialog.taskStarted("Validating Import ...");
                validateProductImportSubmission();

                that.bulkImportProductDialogInstance.close();
                that.importProgressDialogInstance.open();
                $('#spinning').removeClass("hidden");

                var form = that.bulkImportProductDialog.find("form");
                var oldActionUri = form.attr('action');
                var productScode = that.productScode.val();

                var beforeSend = function () {
                    $(that.bulkImportProductFormFileName).val($(that.bulkImportProductFormFileInput).val().split('\\').pop());
                };


                function printValidationResult(header, errorMessages) {
                    if (errorMessages.isEmpty || errorMessages == "") {
                        $('#spinning').addClass("hidden");
                        that.importProgressDialog.taskFinished(header);
                    } else {
                        that.importProgressDialog.taskFinishedWithErrors(header);
                        that.errorMessages.text(errorMessages).removeClass("hidden");
                    }
                }

                function resetFormAction(uri) {
                    form.attr('action', uri);
                }

                function performPostValidationActivities(data) {
                    if (data.successful && true == data.successful) {
                        printValidationResult("Import Initialized..", data.errors);
                        $("#overallMessage").html('Processing is initiated for the uploaded spreadsheet.Email notification will be sent once processing is completed').removeClass("hidden");
                        importProduct();
                    } else {
                        printValidationResult("Import request failed..", data.errors);
                    }

                    $('#spinning').addClass("hidden");
                }

                function performPostSubmissionActivities() {
                    resetFormAction(oldActionUri);
                    $('#spinning').addClass("hidden");
                }

                function validateExcelSheet() {
                    var excelValidateUrl = that.validateUserImportProductURL.val();
                    form.attr('action', excelValidateUrl.replace("(productSCode)",productSCode));
                    $(form).ajaxSubmit(function (data) {
                        var jsonData = $.parseJSON(data);
                        performPostValidationActivities(jsonData);
                    });
                }

                function validateProductImportSubmission() {
                    var availabilityCheckUrl = that.productAvailabilityCheckUrl.val();
                    availabilityCheckUrl = availabilityCheckUrl.replace("(productSCode)", productSCode);
                    $.ajax({
                               type:"POST",
                               url:availabilityCheckUrl,
                               success:function (data) {
                                   processProductAvailabilityResult(data);

                               },
                               error:function (data) {
                                   processProductAvailabilityResult(data)
                               }
                           });
                }

                function processProductAvailabilityResult(data) {
                    if (data.hasError == true) {
                        that.importProgressDialog.taskFinishedWithErrors(data.errors[0]);
                        $('#spinning').addClass("hidden");
                    } else {
                        validateExcelSheet();
                    }
                }

                function importProduct() {
                    form.attr('action', that.userImportProductURL.val().replace("(productSCode)", productSCode));
                    $(form).ajaxSubmit(function () {
                        beforeSend();
                        $.ajax({
                                   type:"POST",
                                   url:form.attr('action'),
                                   data:$(form).serialize()
                               });
                    });
                    performPostSubmissionActivities();
                    setTimeout(function () {refreshStatus(productScode);}, 5000);
                }



            }
        });

        function refreshStatus(productScode) {
            var statusUrl = that.userImportStatusURL.val().replace("(productSCode)", productScode);
            $.get(statusUrl)
                    .success(function (data) {
                                 if (data.successful == false) {
                                     $("#overallMessage").html(data.errors).removeClass("hidden");
                                     setTimeout(function () { refreshStatus(); }, 10000);
                                 } else {
                                     $("#overallMessage").html('').addClass("hidden");
                                 }
                             })

                    .error(function () {
                               $("#overallMessage").html('').addClass("hidden");
                           });
        }

        var onLoad = function () {
            var response = that.addImportProductTargetIFrame.text();
            that.addImportProductTargetIFrame.unbind("load", onLoad);
        };
    },

    setupImportProgressDialog:function () {
        var that = this;
        that.importProgressDialogInstance = new rsqe.Dialog(that.importProgressDialogContainer, {
            title:"Progress",
            width:"350px",
            closers:that.importProgressDialogContainer.find(".close")
        });
    },

    selectedSiteCountriesAreAvailableForProduct:function () {
        var specialBidElement = $("input:checkbox:checked.try-special-bid-product");
        var unsupportedElement = $("input:checkbox:checked.invalid-country-for-product");
        var isUnsupportedCountries = (unsupportedElement.size() == 0 || $("input:checkbox").size() == 0);
        var isSpecialBidSupported = (specialBidElement.size() == 0 || $("input:checkbox").size() == 0);

        if (!isUnsupportedCountries) {
            for (var i=0; i<unsupportedElement.length; i++){
                var country = unsupportedElement[i].getAttribute('unavailablecountries');
                if ($.inArray(country, this.unsupportedCountriesArray) == -1 ) {
                    this.unsupportedCountriesArray.push(country);
                }
            }
            listOfUnsupportedCountries = this.formatListOfCountries(this.unsupportedCountriesArray);
            $("#commonError").removeClass("hidden");
            $("#commonError").html("Unable to supply product to " + listOfUnsupportedCountries);
        } else if (!isSpecialBidSupported) {
            for (var i=0; i<specialBidElement.length; i++){
                var country = specialBidElement[i].getAttribute('specialbidcountries');
                if ($.inArray(country, this.specialBidCountriesArray) == -1 ) {
                    this.specialBidCountriesArray.push(country);
                }
            }
            listOfSpecialBidCountries = this.formatListOfCountries(this.specialBidCountriesArray);
            $("#commonError").removeClass("hidden");
            $("#commonError").html("Unable to support product as standard in " + listOfSpecialBidCountries + ", therefore the product has not been added to sites in those"
                                    + " countries.  However the service may be available via special bid.  To progress further in the quote journey"
                                    + " please add a General Special Bid product with the required service configuration for the selected site/s.");
        } else {
            $("#commonError").addClass("hidden");
        }
        return isUnsupportedCountries;
    },

    formatListOfCountries: function (listOfCountries) {
        var listAsString = listOfCountries.join(', ');
        if (listOfCountries.length != 1){
            var index = listAsString.lastIndexOf(',');
            listAsString = listAsString.substr(0, index) + ' and ' + listAsString.substr(index+'and'.length-1);
        }
        return listAsString;
    },

    displayComplianceCheckPanel:function (complianceCheckNeeded, prerequisiteUrl) {
        if (complianceCheckNeeded) {
            this.prerequisiteUrl.attr('href', prerequisiteUrl);
            this.complianceCheckPanel.show();
        }
        else this.complianceCheckPanel.hide();

    },
    complianceChecked:function () {
        if (this.productJson().isComplianceCheckNeeded) {
            return this.complianceCheckBox.is(":checked");
        }
        return true;
    },

    loadNewSiteDialog: function(dialogTitle) {
        var that = this;
        var newSiteDialogUri = this.selectNewSiteDialogUri[0].defaultValue;
        that.selectNewSiteDialogInstance.productId = this.productScode.val();
        that.selectNewSiteDialogInstance.setOptions({"title": dialogTitle,
                                                        close: function() {
                                                            that.selectNewSiteForm.resetTable();
                                                            that.calculateSubmitButtonStatus();
                                                            that.calculateAddImportButtonStatus();
                                                        }});
        this.selectNewSiteDialog.load(newSiteDialogUri,
                                      function () {
                                          that.selectNewSiteForm.load();
                                      });
    },
    getProductAttributes: function(callback) {
        var that = this;
        var productData = that.productJson();

        return {
            type:'GET',
            url:that.getProductAttributesUri.val(),
            dataType:'text',
            data:{productCode:productData.sCode},
            success: function(data) {
                callback(data);
            }
        };
    },
    setupWarningDialog:function () {
        var that = this;
        that.warningDialogInstance = new rsqe.Dialog(that.warningDialogContainer, {
            title:"Warning!",
            width:"450px",
            closers:that.warningDialogContainer.find(".close")
        });
    }
};

rsqe.ModifyProducts.prototype = function () {
    rsqe.AddModifyProducts.prototype = prototype;
}
rsqe.AddProducts.prototype = function () {
    rsqe.AddModifyProducts.prototype = prototype;
}
rsqe.MoveProducts.prototype = function () {
    rsqe.AddModifyProducts.prototype = prototype;
}
rsqe.MigrateProducts.prototype = function () {
    rsqe.AddModifyProducts.prototype = prototype;
}


