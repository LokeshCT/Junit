var rsqe = rsqe || {};

rsqe.NewSiteForm = function(opts) {
    if (opts == null || opts.cancelHandler == null) throw "cancelHandler should be passed in";
    this.cancelHandler = opts.cancelHandler;
    this.selectNewSiteDialog = $("#newSelectSiteDialog");
    this.productAction = $("#productAction");
    this.getSitesUri = $('.getSitesUri');
    this.newSiteTable = '#newSiteTable';
    this.productCode = $('.product');
    this.productScode = $('.productSCode');
    this.newSiteForm = $("#newSiteForm");
    this.cancelButton = ".cancel";

    this.columnMetaData = [
        { "mDataProp":"id"},
        { "mDataProp":"siteName"},
        { "mDataProp":"addressLine1"},
        { "mDataProp":"addressLine2"},
        { "mDataProp":"addressLine3"},
        { "mDataProp":"townCity"},
        { "mDataProp":"country"},
        { "mDataProp":"postCode"}
    ];

};

rsqe.NewSiteForm.prototype = {
    load: function() {
        var that = this;
        this.setupTable();

        $(that.cancelButton).click(function() {
            that.cancelHandler();
        });

        var selectedCheckBoxes = $('input:checkbox:checked:not(#selectAll):not(#complianceCheckBox)');
        this.selectNewSiteOkButton = $("#selectNewSiteOkButton");
        this.showHiddenSites = $('#showHiddenSites');
        this.selectNewSiteOkButton.click(function () {
            var siteTable = $("#siteTable");
            siteTable.dataTable();
            siteTable.fnUpdateRowWithNewSiteData(selectedCheckBoxes);
            that.cancelHandler();
        });
    },

    setupTable:function () {
        var that = this;
        var selectedCountries = [];
        $('input:checkbox:checked:not(#selectAll):not(#complianceCheckBox):not(#showHiddenSites)').each(function () {
            selectedCountries.push($(this).attr('country'));
        });
        var filter = 'country='+selectedCountries.join(',');
        $(this.newSiteTable).dataTable({
                                           sPaginationType:"full_numbers",
                                           sAjaxSource:that.getSitesUri.val(),
                                           sDom:'lrt<"table_footer"ip>',
                                           bProcessing:true,
                                           bServerSide:true,
                                           bSort:false,
                                           bLengthChange:true,
                                           bDeferRender:true,
                                           bStateSave:false,
                                           iDisplayStart:0,
                                           iDisplayLength:10,
                                           iScrollLoadGap:50,
                                           aoColumns:this.columnMetaData,
                                           bDestroy:true,
                                           sAjaxDataProp:function (data) {
                                               return $.makeArray(data.sites);
                                           },
                                           sScrollY:200 + "px",
                                           fnRowCallback:function (row, aData) {
                                               return that.fnRowCallback(row, aData);
                                           },
                                           fnDrawCallback:function (settings) {
                                               $(window).unbind('resize').bind('resize', function () {
                                                   that.applyTableHeight();
                                               });
                                               $("input[type='radio']")[1].checked = true;
                                           },
                                           oLanguage:{
                                               sInfo:"Showing _START_ to _END_ of _TOTAL_ sites",
                                               sInfoEmpty:"Showing 0 to 0 of 0 sites",
                                               sInfoFiltered:"- filtered from _MAX_ sites",
                                               sZeroRecords:"Please select a product to proceed",
                                               sLengthMenu:"Show _MENU_ sites"
                                           },
                                           fnServerParams:function (data) {
                                               data.push({"name":"productAction", "value":"SelectNewSite"});
                                               var sCode = that.productScode.val();
                                               if (sCode) {
                                                   data.push({"name":"forProduct", "value":sCode});
                                               }

                                           }
                                       }).fnFilter(filter);
    },

    applyTableHeight:function () {
        var sScrollY = ($(window).height() - this.staticHeight) + "px";
        $(".dataTables_scrollBody").css("height", sScrollY);
    },

    fnRowCallback:function (row, aData) {
        var that = this;
        var selectedSiteIds = [];
        $('input:checkbox:checked:not(#selectAll):not(#complianceCheckBox):not(#showHiddenSites)').each(function () {
            selectedSiteIds.push($(this).val());
        });
        $.each($("td", row), function (i, td) {
            var columnName = that.columnMetaData[i].mDataProp;
            if (columnName == "id") {
                var cssClass = "choice";
                if (!aData.isValidForProduct || aData.isValidForProduct == "false") {
                    cssClass += " invalid-country-for-product";
                }
                var input = $("<input type='radio' class='" + cssClass + "' name='siteIds' value='" + aData.id + "' />");
                $(td).html(input);
                $(td).addClass("radio");
            } else {
                $(td).addClass(columnName);
            }
        });
        $(row).addClass("siteLine");
        $(row).attr("id", "id_" + aData.id);
        if (_.find(selectedSiteIds, function(id) {return id == aData.id})) {
            $(row).addClass("hidden");
        }
        return row;
    },

    productJson:function () {
        var value = this.productCode.val();
        return $.parseJSON(value == "" ? "{}" : value);
    },

    resetTable: function() {
        $('#newSiteTable').remove();
    }
};
