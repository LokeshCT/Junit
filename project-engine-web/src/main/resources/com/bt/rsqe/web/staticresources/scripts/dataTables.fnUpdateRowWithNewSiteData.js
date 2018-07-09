/*
 * Function: fnUpdateRowWithNewSiteData
 * Purpose:  Redraw the Move Product Table with new site details
 * Returns:
 * Inputs:   object:oSettings - DataTables settings object
 */
$.fn.dataTableExt.oApi.fnUpdateRowWithNewSiteData = function (oSettings, selectedCheckBoxes) {
    this.productCode = $('.product');
    var value = this.productCode.val();
    var forProduct = $.parseJSON(value == "" ? "{}" : value);
    var oAPI = this.oApi;
    var aoData = oAPI._fnAjaxParameters(oSettings);

    var existingLineItems = [];
    $(selectedCheckBoxes).each(function () {
        existingLineItems.push({"siteId":this.defaultValue, "action":"Move"});
    });

    var newSiteId = $('input.choice:radio:checked')[0].defaultValue;
    $(existingLineItems).each(function (existingLineItem) {
        aoData.push({"name":"existingSiteId", "value":$(this)[0].siteId});
    });

    aoData.push({"name":"newSiteId", "value":newSiteId});
    aoData.push({"name":"productAction", "value":"Move"});
    aoData.push({"name":"forProduct", "value":forProduct.sCode});
    oSettings.fnServerData.call(oSettings.oInstance, oSettings.sAjaxSource, aoData,
                                function(json) {
                                    oAPI._fnAjaxUpdateDraw(oSettings, json);
                                }, oSettings);
}