var rsqe = rsqe || {};

rsqe.MaintainerAgreementForm = function (opts) {
    if (opts == null || opts.cancelHandler == null) throw "cancelHandler should be passed in";
    this.cancelHandler = opts.cancelHandler;
    this.maintainerAgreementDialog = $("#maintainerAgreementDialog");
    this.maintainerAgreementFormId = $("#maintainerAgreementForm");
    this.successMessage = new rsqe.StatusMessage("#successMessage");
    this.commonError = new rsqe.StatusMessage("#commonError");
    this.cancelButton = ".cancel";

    this.columnMetaData = [
        { "mDataProp":"id"},
        { "mDataProp":"magId"},
        { "mDataProp":"magLabel"},
        { "mDataProp":"maintainerName"},
        { "mDataProp":"magContact"},
        { "mDataProp":"magResponseTime"},
        { "mDataProp":"magFixTime"},
        { "mDataProp":"magHelpDesk"},
        { "mDataProp":"magHelpDeskName"},
        { "mDataProp":"linkedCountry"},
        { "mDataProp":"linkedName"},
        { "mDataProp":"hoursOfBusiness"}
    ];
};

function enableAssociateMAGButton(radioButton) {
    if (radioButton.checked) {
        $("#associateMAGButton").removeClass("disableBtn");
        $("#associateMAGButton").disableable('enable', true);
    }
}

rsqe.MaintainerAgreementForm.prototype = {

    load:function () {

        var that = this;
        this.associateMAGButton = $("#associateMAGButton");
        this.typeFilter = $("#typeFilter");
        this.maintainerFilter=$("#maintainerFilter");
        this.countryFilter = $("#countryFilter");
        this.maintainerAgreementTable = $("#maintainerAgreementTable");
        this.staticHeight = 300;

        // this.globalSearch = $("#globalSearch");


        this.setupTable();

        that.associateMAGButton.addClass("disableBtn");
        that.associateMAGButton.disableable('enable', false);

        $(that.cancelButton).click(function () {
            that.cancelHandler();
        });


        this.associateMAGButton.click(function () {
            that.successMessage.hide();
            that.commonError.hide();

            var selectedMagId = $('[name="selectedMagId"]:radio:checked').val();
            if (selectedMagId !== undefined) {
                $.post($("#productAgreementsUri").text() + '/persistMaintainerAgreementId'
                               + "?selectedMagId=" + selectedMagId)
                        .success(function () {
                                     that.successMessage.show("MAG ID Associated Successfully.", 5000);

                                 })
                        .error(function () {
                                   var errorMessage = "Error While Associating MAG Details";
                                   that.commonError.show(errorMessage);
                               });
                that.cancelHandler();
                window.location.reload();
            } else {

                alert(" please select valid MAG id");
            }
        });

        this.typeFilter.change(function () {

          var typeName = that.typeFilter.val();
          var maintainerId = that.maintainerFilter.val();
          var cityId = that.countryFilter.val();
            if(typeName!=="") {
              var typeFilterUrl = $("#productAgreementsUri").text() +"/magListForFilters"
                                            + "?typeSelected=" + typeName
                                            + "&maintainerSelected=" + maintainerId
                                            + "&countrySelected=" + cityId;
                that.configureDataTable(typeFilterUrl);
                that.associateMAGButton.addClass("disableBtn");
                that.associateMAGButton.disableable('enable', false);
               // $("#maintainerFilter option:first").attr("selected", true);
               // $("#countryFilter option:first").attr("selected", true);
            }

        });

        this.maintainerFilter.change(function () {

            var typeName = that.typeFilter.val();
            var maintainerId = that.maintainerFilter.val();
            var cityId = that.countryFilter.val();
            if(typeName!=="") {
                var maintainerFilterUrl = $("#productAgreementsUri").text()+"/magListForFilters"
                                                  + "?typeSelected=" + typeName
                                                  + "&maintainerSelected=" + maintainerId
                                                  + "&countrySelected=" + cityId;
                that.configureDataTable(maintainerFilterUrl);
                that.associateMAGButton.addClass("disableBtn");
                that.associateMAGButton.disableable('enable', false);
               // $("#countryFilter option:first").attr("selected", true);
            }

        });
        this.countryFilter.change(function () {

            var typeName = that.typeFilter.val();
            var maintainerId = that.maintainerFilter.val();
            var cityId = that.countryFilter.val();
            if(typeName!=="") {
                var countryFilterUrl = $("#productAgreementsUri").text()+"/magListForFilters"
                                               + "?typeSelected=" + typeName
                                               + "&maintainerSelected=" + maintainerId
                                               + "&countrySelected=" + cityId;
                that.configureDataTable(countryFilterUrl);
                that.associateMAGButton.addClass("disableBtn");
                that.associateMAGButton.disableable('enable', false);
            }

        });
    },

    setupTable:function () {
        var getMagDetailsUri = $("#productAgreementsUri").text() + '/maintainerAgreements';
        this.configureDataTable(getMagDetailsUri);
    },
    configureDataTable:function (source) {


        var that = this;

        $(this.maintainerAgreementTable).dataTable({
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
                                                        iDisplayLength:10,
                                                         sAjaxDataProp:function (data) {
                                                             return $.makeArray(data.maintainerAgreementDTOList);
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
    },fnRowCallback:function (row, aData) {
        var that = this;
        $.each($("td", row), function (i, td) {
            var columnName = that.columnMetaData[i].mDataProp;

            if (columnName === "id") {
                $(td).html("<input type='radio' name='selectedMagId' onclick = 'enableAssociateMAGButton(this)' value='" + aData.id + "' />");
                $(td).addClass("radio");

            }
            else {
                $(td).addClass(columnName);
            }
        });
        $(row).addClass("selectedMagId");
        $(row).attr("id", "id_" + aData.id);

        return row;
    },
    resetForm:function () {
        $(this.maintainerAgreementFormId).each(function () {
            this.reset();
        });

    }



};