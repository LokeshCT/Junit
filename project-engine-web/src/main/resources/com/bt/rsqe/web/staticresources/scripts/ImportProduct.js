var rsqe = rsqe || {};

rsqe.ImportProduct = function() {
    var that = this;

    that.importProductButtonSelector = '#importProduct';

    return {
        enableImportProductButton: function(lineItems) {
            if (lineItems.length == 1) {
                var itemId = lineItems[0];
                $.each($('input[name="importable"]'), function(index, item) {
                    if ($(item).attr('id') == itemId && $('#id_'+itemId+' .status').html() != 'Customer Approved') {
                        $(that.importProductButtonSelector).removeClass("hidden");
                        $(that.importProductButtonSelector).show();
                        return;
                    }
                });
            } else {
                $(that.importProductButtonSelector).addClass("hidden");
                $(that.importProductButtonSelector).hide();
            }
        }
    }
};
