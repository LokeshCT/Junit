var rsqe = rsqe || {};

rsqe.LineItemValidation = function(checkboxGroup) {
    function updateValidity(lineItemId, text, errors) {
        var row = $("#id_" + lineItemId);
        row.find(".validity").text(text);

        new rsqe.optiondetails.DataTable().updateErrorRow(row, errors);
    }

    function initialize() {
        $("#validate").disableable('click',function() {
            var linkUrl = $(this).attr("href");
            checkboxGroup.selected_elements().each(function() {
                var lineItemId = $(this).val();

                var loadIcon_fff = $("<img>").attr("src", "/rsqe/project-engine/static/images/cell_validate.gif");
                var loadIcon_blue = $("<img>").attr("src", "/rsqe/project-engine/static/images/cell_validate_blue.gif");

                var row = $("#id_" + lineItemId);
                var statusSelector
                if(row.find("td.validity").hasClass("warning")) {
                    statusSelector = row.find("td.validity").addClass("validating").removeClass("warning");
                } else {
                    statusSelector = row.find("td.validity").addClass("validating").removeClass("invalid");
                }

                if(row.hasClass("odd")) {
                    statusSelector.html(loadIcon_fff);
                } else {
                    statusSelector.html(loadIcon_blue);
                }


                $.post(linkUrl.replace("(id)", lineItemId)).success(
                    function(response) {
                        updateValidity(lineItemId, response.status, $.makeArray(response.descriptions));
                        statusSelector.removeClass("validating");
                    }).error(function() {
                        updateValidity(lineItemId, "Could not validate", ["Try again?"]);
                        statusSelector.removeClass("validating");
                    });

            });
            return false;
        });
    }

    return {
        initialize: initialize
    };
};
