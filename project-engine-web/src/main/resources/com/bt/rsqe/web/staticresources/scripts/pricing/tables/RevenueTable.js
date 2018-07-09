/**
 * This namespace defines all functionality required to build the Revenue DataTable displayed on a dialog launched by the {@link standardTab}.
 *
 * @namespace revenueTable
 * @revenueTable The object to append all logic onto.
 * @undefined This parameter prevents undefined from being redefined by other JavaScript code.
 */
(function(revenueTable, undefined) {

    // Short names for the namespaces used here.
    var pricing = rsqe.pricingTab;
    var url     = rsqe.urlBuilder;

    var PROPOSED_TEXT                         = '#proposedText';
    var TRIGGER_MONTHS                        = '#triggerMonths';

    // Table
    var EDITABLE_REVENUE_FIELDS_SELECTOR      = '.proposedRevenue, .triggerMonths';

    // Data Table element used to set table height.
    var DATA_TABLE_SCROLL_BODY_SELECTOR       = '.dataTables_scrollBody';

    // Commercial Non Standard Request Panel.
    var COMMERCIAL_NON_STANDARD_REQUEST_PANEL = '#commercialNonStandardRequestPanel';
    var REVENUE_SELECT_ALL                    = '#revenueSelectAll';

    /**
     * Initialize the Revenue DataTable and it's supporting elements.
     * @public
     */
    revenueTable.initialize = function() {

        revenueTable.revenueData = {};

        revenueTable.revenueMetadata = [
            { "mDataProp": "productCategoryName", sWidth:"25%" },
            { "mDataProp": "existingRevenue",     sWidth:"5%", sClass: "numeric nowrap" },
            { "mDataProp": "proposedRevenue",     sWidth:"5%", sClass: "numeric nowrap" },
            { "mDataProp": "triggerMonths",       sWidth:"5%", sClass: "numeric nowrap" }
        ];

        // Link the Description checkbox in the Standard tab's table headers to the checkboxes in the table.
        linkPricingRevenueCheckBoxes();

        // Attach HTML event handlers.
        attachEvents();

        $(COMMERCIAL_NON_STANDARD_REQUEST_PANEL).hide();

    };

    function attachEvents() {

        $(pricing.COMMERCIAL_NON_STANDARD_REQUEST).click(function() {

            if ($(pricing.COMMERCIAL_NON_STANDARD_REQUEST).is(":checked")) {

                $(COMMERCIAL_NON_STANDARD_REQUEST_PANEL).show();

                buildRevenueTable();

                attachRevenueFunctions();
                $(PROPOSED_TEXT).disableable('enable', false);
                $(TRIGGER_MONTHS).disableable('enable', false);
            }

            else {
                $(COMMERCIAL_NON_STANDARD_REQUEST_PANEL).hide();
                $(pricing.SEND_DISCOUNT_APPROVAL_MESSAGE).removeClass().text("");
                $(pricing.PRICING_REVENUE_TABLE).dataTable().fnDestroy();
                $(PROPOSED_TEXT).unbind("change");
                $(TRIGGER_MONTHS).unbind("change");
                revenueTable.revenueData = [];
            }

        });
    }

    function buildRevenueTable() {

        $(pricing.PRICING_REVENUE_TABLE).dataTable(
            {
                sPaginationType: "full_numbers",
                sDom: 'lrt<"table_footer"ip>',
                sAjaxSource: url.PRODUCT_REVENUE,
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
                aoColumns: revenueTable.revenueMetadata,

                sAjaxDataProp: function(data) {
                    return $.makeArray(data.itemDTOs);
                },

                fnRowCallback: function(row, aData) {
                    return fnRevenueRowCallback(row, aData)
                },

                fnDrawCallback: function() {
                    setRevenueTableEditable();
                    $(window).unbind('resize').bind('resize', function () {
                        var sScrollY = ($(window).height() - 500) + "px";
                        $(DATA_TABLE_SCROLL_BODY_SELECTOR).css("height", sScrollY);
                        $(pricing.PRICING_REVENUE_TABLE).dataTable().fnAdjustColumnSizing(false);
                    });
                    revenueTable.linkRevenueCheckBoxes.initialize();
                },

                oLanguage : {
                    sInfo: "Showing _START_ to _END_ of _TOTAL_ Root Products",
                    sLengthMenu: "Display _MENU_ Root Products"
                }
            }
        );
    }

    /**
     * Link the Description checkbox on the header bar of the Revenue Table with the checkboxes in the cells of the Description column.
     */
     function linkPricingRevenueCheckBoxes() {

        revenueTable.linkRevenueCheckBoxes = new rsqe.CheckboxGroup(

            "input:not([disabled])[name='productName']",

            {
                select_all    : "#revenueTable_wrapper #revenueSelectAll",

                // Action to take when checkbox is checked.
                someChecked: function () {
                    $(REVENUE_SELECT_ALL).attr('title', 'Select None');
                    $(PROPOSED_TEXT).disableable('enable', true);
                    $(TRIGGER_MONTHS).disableable('enable', true);
                },

                // Action to take when checkbox is un-checked.
                allUnchecked: function () {
                    $(REVENUE_SELECT_ALL).attr('title', 'Select All');
                    $(PROPOSED_TEXT).disableable('enable', false);
                    $(PROPOSED_TEXT).removeClass();
                    $(TRIGGER_MONTHS).disableable('enable', false);
                }
            }
        );

    }

    // TODO: Edit allows Non-Numeric values. Should reuse the same conditions for determining a valid discount value as elsewhere in pricing.
    function attachRevenueFunctions() {

        $(PROPOSED_TEXT).change(function() {
            var textValue = this.value;
            revenueTable.linkRevenueCheckBoxes.selected_elements().each(function () {
                var data = $(pricing.PRICING_REVENUE_TABLE).dataTable().fnGetData(this.parentNode.parentNode);
                var revenue = revenueTable.revenueData[data.id];
                var triggerVal = revenue == undefined ? 0 : revenue.triggerMonths;
                revenueTable.revenueData[data.id] = {
                    "proposedRevenue"     : textValue,
                    "existingRevenue"     : data.existingRevenue,
                    "triggerMonths"       : triggerVal,
                    "productCategoryName" : data.productCategoryName
                };
                var row = this.parentNode.parentNode;
                row.cells[2].textContent = textValue;
            });
        });

        $(TRIGGER_MONTHS).change(function() {

            var textValue = this.value;
            revenueTable.linkRevenueCheckBoxes.selected_elements().each(function () {
                var data = $(pricing.PRICING_REVENUE_TABLE).dataTable().fnGetData(this.parentNode.parentNode);
                var revenue = revenueTable.revenueData[data.id];
                var proposedVal = revenue == undefined ? 0 : revenue.proposedRevenue;
                revenueTable.revenueData[data.id] =
                {
                    "proposedRevenue"     : proposedVal,
                    "existingRevenue"     : data.existingRevenue,
                    "triggerMonths"       : textValue,
                    "productCategoryName" : data.productCategoryName
                };
                var row = this.parentNode.parentNode;
                row.cells[3].textContent = textValue;
            });
        });

    }

    function fnRevenueRowCallback(row, aData) {

        $.each($("td", row), function(i, td) {

            var mDataProp = revenueTable.revenueMetadata[i].mData;
            $(td).addClass(mDataProp);
            if (mDataProp === "productCategoryName") {
                $(td).prepend("<input type='checkbox' name='productName'/>");
            }
            $(td).attr("id",mDataProp);

        });

        $(row).attr("id", "id_" + aData.id);
        $(row).addClass("revenueTable");
        return row;
    }

     function setRevenueTableEditable() {

        $(EDITABLE_REVENUE_FIELDS_SELECTOR).editable(

            function(value) {

                var data        = $(pricing.PRICING_REVENUE_TABLE).dataTable().fnGetData(this.parentNode);
                var revenue     = revenueTable.revenueData[data.id];
                var proposedVal = revenue == undefined ? "" : revenue.proposedRevenue;
                var triggerVal  = revenue == undefined ? "" : revenue.triggerMonths;

                if(this.id === "proposedRevenue") {
                    proposedVal = value;
                }

                if(this.id === "triggerMonths") {
                    triggerVal = value;
                }

                revenueTable.revenueData[data.id] =
                {
                    "proposedRevenue"     : proposedVal,
                    "existingRevenue"     : data.existingRevenue,
                    "triggerMonths"       : triggerVal,
                    "productCategoryName" : data.productCategoryName
                };

                return value;
            },

            {
                onblur      : 'submit',
                cssclass    : 'editable'
            }
        );
    }

// Immediately invoke this namespace.
}( rsqe.pricingTab.standardTab.revenueTable = rsqe.pricingTab.standardTab.revenueTable || {} ));