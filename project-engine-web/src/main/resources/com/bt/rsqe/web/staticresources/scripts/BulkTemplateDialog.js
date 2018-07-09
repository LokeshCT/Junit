rsqe = rsqe || {};

rsqe.BulkTemplateDialog = function (opts) {
    if (opts == null || opts.container == null || opts.uri == null) throw "container AND uri should be passed in";
    this.container = opts.container;
    this.bulkTemplateUri = opts.uri;
    this.downloadButtonSelector = ".downloadForProduct";
    this.productsSelectSelector = ".product";
};

rsqe.BulkTemplateDialog.prototype = {
    destroy:function (removeDialog) {
        removeDialog(this.container);
    },

    show:function () {
        var that = this;
        this.container.load(this.bulkTemplateUri, function () {
            var downloadButton = $(that.downloadButtonSelector, that.container);
            new rsqe.Dialog(that.container, {autoOpen:true,title:'Download Bulk Template',closers:downloadButton});

            var productsSelect = $(that.productsSelectSelector, that.container);

            downloadButton.attr('href', productsSelect.val());
            productsSelect.change(function () {
                downloadButton.attr('href', $(this).val());
            });
        });
    }

};
