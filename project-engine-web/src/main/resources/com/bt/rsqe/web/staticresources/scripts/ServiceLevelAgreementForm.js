var rsqe = rsqe || {};

rsqe.ServiceLevelAgreementForm = function (opts) {
    if (opts == null || opts.cancelHandler == null) throw "cancelHandler should be passed in";
    this.cancelHandler = opts.cancelHandler;
    this.serviceLevelAgreementDialog = $("#serviceLevelAgreementDialog");
    this.serviceLevelAgreementFormId = $("#serviceLevelAgreementForm");
    this.successMessage = new rsqe.StatusMessage("#successMessage");
    this.commonError = new rsqe.StatusMessage("#commonError");
    this.cancelButton = ".cancel";

    this.columnMetaData = [
        { "mDataProp":"id"},
        { "mDataProp":"slaId"},
        { "mDataProp":"slaLabel"},
        { "mDataProp":"sltLabel"},
        { "mDataProp":"hoursOfBusiness"},
        { "mDataProp":"resiliencyRepairTarget"},
        { "mDataProp":"resiliencyResponseTarget"},
        { "mDataProp":"severityRepairTarget"},
        { "mDataProp":"severityResponseTarget"},
        { "mDataProp":"siteAvailabilityTarget"},
        { "mDataProp":"slaReference"}
    ];
};

function enableAssociateSLAButton(radioButton) {
    if (radioButton.checked) {
        $("#associateSLAButton").removeClass("disableBtn");
        $("#associateSLAButton").disableable('enable', true);
    }
}

rsqe.ServiceLevelAgreementForm.prototype = {

    load:function () {

        var that = this;
        this.associateSLAButton = $("#associateSLAButton");
        this.typeFilter = $("#typeFilter");
        this.countryFilter = $("#countryFilter");
        this.serviceLevelAgreementTable = $("#serviceLevelAgreementTable");
        this.staticHeight = 300;

        // this.globalSearch = $("#globalSearch");


        this.setupTable();

        that.associateSLAButton.addClass("disableBtn");
        that.associateSLAButton.disableable('enable', false);

        if ($("#complexContract").text() == "No") {
            that.typeFilter.addClass("disableBtn");
            that.typeFilter.disableable('enable', false);

            that.countryFilter.addClass("disableBtn");
            that.countryFilter.disableable('enable', false);
        }


        $(that.cancelButton).click(function () {
            that.cancelHandler();
        });


        this.associateSLAButton.click(function () {
            that.successMessage.hide();
            that.commonError.hide();

            var selectedSlaId = $('[name="selectedSlaId"]:radio:checked').val();
            if (selectedSlaId !== undefined) {
                $.post($("#productAgreementsUri").text() + "/persistServiceLevelAgreementId"
                               + "?selectedSlaId=" + selectedSlaId)
                        .success(function () {
                                     that.successMessage.show("SLA ID Associated Successfully.", 5000);

                                 })
                        .error(function () {
                                   var errorMessage = "Error While Associating SLA Details";
                                   that.commonError.show(errorMessage);
                               });
                that.cancelHandler();
                window.location.reload();
            } else {

                alert(" please select valid SLA id");
            }
        });

        this.typeFilter.change(function () {

            var typeName = that.typeFilter.val();
            var cityId = that.countryFilter.val();
            if (typeName!==""){
                var typeFilterUrl = $("#productAgreementsUri").text() + "/slaListForFilters"
                                            + "?typeName=" + typeName
                                            + "&cityId=" + cityId;
                that.configureDataTable(typeFilterUrl);
                that.associateSLAButton.addClass("disableBtn");
                that.associateSLAButton.disableable('enable', false);
                //$("#countryFilter option:first").attr("selected", true);

            }

        });

        this.countryFilter.change(function () {

            var typeName = that.typeFilter.val();
            var cityId = that.countryFilter.val();

            if(typeName !=="") {
                var countryFilterUrl =  $("#productAgreementsUri").text() + "/slaListForFilters"
                                                + "?typeName=" + typeName
                                                + "&cityId=" + cityId;
                that.configureDataTable(countryFilterUrl);
                that.configureDataTable(countryFilterUrl);
                that.associateSLAButton.addClass("disableBtn");
                that.associateSLAButton.disableable('enable', false);
            }

        });
    },

    setupTable:function () {
        var getSlaDetailsFromBfgUrl = $("#productAgreementsUri").text() + '/serviceLevelAgreements';
        this.configureDataTable(getSlaDetailsFromBfgUrl);
    },
    configureDataTable:function (source) {


        var that = this;

        $(this.serviceLevelAgreementTable).dataTable({
                                                         bPaginate:false,
                                                         sAjaxSource:source,
                                                         bProcessing:true,
                                                         bServerSide:true,
                                                         bSort:true,
                                                         "bLengthChange":true,
                                                         "bInfo":false,
                                                         bAutoWidth:true,
                                                         bDeferRender:true,
                                                         bStateSave:false,
                                                         aoColumns:that.columnMetaData,
                                                         bDestroy:true,
                                                         "bFilter":false,
                                                         sAjaxDataProp:function (data) {
                                                             return $.makeArray(data.serviceLevelAgreementDTOList);
                                                         },
                                                         sScrollX:1250 + "px",
                                                         "sScrollY":300 + "px",
                                                         "bScrollCollapse": true,
                                                         fnRowCallback:function (row, aData) {
                                                             return that.fnRowCallback(row, aData);

                                                         },
                                                         fnDrawCallback:function (settings) {
                                                             $(window).unbind('resize').bind('resize', function () {
                                                                 that.applyTableHeight();
                                                         });

                                                         }
                                                     });
    },

    applyTableHeight:function () {
        var sScrollY = ($(window).height() - this.staticHeight) + "px";
        $(".dataTables_scrollBody").css("height", sScrollY);
    },


    fnRowCallback:function (row, aData) {
        var that = this;

        $.each($("td", row), function (i, td) {
            var columnName = that.columnMetaData[i].mDataProp;

            if (columnName === "id") {
                $(td).html("<input type='radio' name='selectedSlaId' onclick = 'enableAssociateSLAButton(this)' value='" + aData.id + "' />");
                $(td).addClass("radio");

            }
            else {
                $(td).addClass(columnName);
            }
        });
        $(row).addClass("selectedSlaId");
        $(row).attr("id", "id_" + aData.id);

        return row;
    },
    resetForm:function () {
        $(this.serviceLevelAgreementFormId).each(function () {
            this.reset();
        });

    }



};