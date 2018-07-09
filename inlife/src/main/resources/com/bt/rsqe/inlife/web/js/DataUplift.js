var upliftBy, assetId, assetVersion, lineItemId, upliftBtn, upliftResultDiv, upliftResultTableDiv, quoteOptionId, projectId, sCode, attName;

var UPLIFT_BY = {
    AssetId : 'assetId',
    LineItemId : 'lineItemId',
    QuoteOptionId : 'quoteOptionId',
    AttributeName : 'productAttributeName'
};

$(document).ready(function() {
    upliftBy = $("#upliftBy");
    assetId = $("#assetId");
    assetVersion = $("#assetVersion");
    lineItemId = $("#lineItemId");
    quoteOptionId = $("#quoteOptionId");
    projectId = $("#projectId");
    sCode = $("#sCode");
    attName = $("#attributeName");
    upliftBtn = $("#uplift");
    upliftResultDiv = $(".uplift-response");
    upliftResultTableDiv = $(".uplift-response-table");
    upliftResultDiv.css('visibility', 'hidden');

    function isEmpty(txt) {
        return txt == '';
    }

    function isNotEmpty(txt) {
        return !isEmpty(txt);
    }

    function clearAll() {
         assetId.val('');
         lineItemId.val('');
         quoteOptionId.val('');
         quoteOptionId.val('');
         projectId.val('');
         sCode.val('');
         attName.val('');
        $('.assetIdRow').hide();
        $('.lineItemIdRow').hide();
        $('.quoteOptionIdRow').hide();
        $('.productAttributeRow').hide();
        $('.upliftBtnRow').hide();
        upliftResultDiv.css('visibility', 'hidden');
    }

    clearAll();

    upliftBy.change(function() {
        clearAll();
        var upliftByVal = upliftBy.val();
        $('.upliftBtnRow').css('display', '');
        if (upliftByVal == UPLIFT_BY.AssetId) {
            $('.assetIdRow').css('display', '');
        } else if (upliftByVal == UPLIFT_BY.LineItemId) {
            $('.lineItemIdRow').css('display', '');
        } else if (upliftByVal == UPLIFT_BY.QuoteOptionId) {
            $('.quoteOptionIdRow').css('display', '');
        } else if (upliftByVal == UPLIFT_BY.AttributeName) {
            $('.productAttributeRow').css('display', '');
        }
    });

    assetId.change(function() {
        if (isNotEmpty(assetId.val())) {
            lineItemId.val('');
            projectId.val('');
            quoteOptionId.val('');
        }
        upliftResultDiv.css('visibility', 'hidden');
    });

    lineItemId.change(function() {
        if (isNotEmpty(lineItemId.val())) {
            assetId.val('');
            projectId.val('');
            quoteOptionId.val('');
        }
        upliftResultDiv.css('visibility', 'hidden');
    });

    quoteOptionId.change(function() {
        if (isNotEmpty(quoteOptionId.val())) {
            assetId.val('');
            lineItemId.val('');
        }
        upliftResultDiv.css('visibility', 'hidden');
    });

    function getUrl() {
        var version = isEmpty(assetVersion.val()) ? '1' : assetVersion.val();
        if (isNotEmpty(assetId.val())) {
            return  $("#assetUpliftUrl").val().replace('{assetId}', assetId.val()).replace('{assetVersion}', version);
        }
        if (isNotEmpty(lineItemId.val())) {
            return  $("#lineItemUpliftUrl").val().replace('{lineItemId}', lineItemId.val());
        }
        if (isNotEmpty(quoteOptionId.val()) && isNotEmpty(projectId.val())) {
            return  $("#quoteOptionUpliftUrl").val().replace('{projectId}', projectId.val()).replace('{quoteOptionId}', quoteOptionId.val());
        }
        if (isNotEmpty(sCode.val()) && isNotEmpty(attName.val())) {
            return  $("#productAttributeUpliftUrl").val().replace('{sCode}', sCode.val()).replace('{attName}', attName.val());
        }
        throw new Error();
    }

    function attributeName(attName, depth) {
        var result = '';
        for (var i = 0; i < depth; i++) {
            result += '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
        }
        return result + attName;
    }

    var renderUpliftResponse = function(data) {
        upliftResultDiv.css('visibility', 'visible');
        var headerRow = '<tr><td>Attribute Name</td><td>Old Value</td><td>New Value</td><td>Asset Id</td></tr>';
        var rowTemplate = '<tr><td>attName</td><td>oldValue</td><td>newValue</td><td>assetId</td></tr>';

        function transformToRow(attChange) {
            return rowTemplate
                .replace('attName', attributeName(attChange.attributeName, attChange.depth))
                .replace('oldValue', attChange.oldValue)
                .replace('newValue', attChange.newValue)
                .replace('assetId', attChange.assetKey);
        }

        var rows = '';
        _.each(data.upliftResult, function(upliftResult) {
            _.each(upliftResult.attributes, function(attChange) {
                rows += transformToRow(attChange);
                _.each(upliftResult.contributesTo, function(contributesToChange) {
                    rows += transformToRow(contributesToChange);
                });
            });
        });
        var html = '<table><thead>headerRow</thead><tbody>rows</tbody></table>'.replace('headerRow', headerRow).replace('rows', rows);
        upliftResultTableDiv.html(html);
    };

    var blockUI = function() {
        $.blockUI({
                      message:'<div style="height: 60px;text-align: center;"><b style="font: 13px">Please wait..</b></div>',
                      css:{
                          background:'url("/rsqe/inlife/static/img/spinner.gif") no-repeat scroll 50% 20px #595E62'
                      }
                  });
    };

    upliftBtn.click(function() {
        var uri = getUrl();
        blockUI();
        $.ajax({
                   type: "PUT",
                   url: uri
               })
            .done(function(data) {
                      $.unblockUI();
                      renderUpliftResponse(data);
                  })
            .fail(function(data) {
                      $.unblockUI();
                      upliftResultDiv.css('visibility', 'visible');
                      upliftResultTableDiv.html('<h style="color: red;">Error : ' + data.responseText + '</h>');
                  });
    });

});

