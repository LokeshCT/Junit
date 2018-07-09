function loadViewConfiguration() {
    self.setUpEvents();
    self.setUpActions();

    self.configurationTree.hide();
    self.spinner.hide();
    self.cancel.hide();

    self.form.submit(function() {
        return false;
    });

};

function setUpActions(){
    self.goButton.click(function() {
       self.productsBySiteRadio.click();
    });
    self.sitesByProductRadio.click(function(){
        self.spinner.show();
        self.errorMessage.addClass("hidden");
        self.configurationTree.getSitesByProductTree('destroy');
        self.sitesByProductRadio.attr('checked',true);
        self.productsBySiteRadio.attr('checked',false);
        var promise = self.loadSitesByProductTree();
        promise.then(function () {
            self.configurationTree.show();
            self.spinner.hide();
        });
    });

    self.productsBySiteRadio.click(function(){
        self.spinner.show();
        self.errorMessage.addClass("hidden");
        self.configurationTree.getProductsBySiteTree('destroy');
        self.sitesByProductRadio.attr('checked',false);
        self.productsBySiteRadio.attr('checked',true);

        var promise = self.loadProductsBySiteTree();
        promise.then(function () {
            self.configurationTree.show();
            self.spinner.hide();
        });
    });
};

function loadProductsBySiteTree() {
    var item = {};
    item ["quoteOptionId"] = self.quoteOptionFilter.val();
    item ["offerId"] = self.offerFilter.val();
    item ["orderId"] = self.orderFilter.val();
    item ["treeViewType"] = "productsBySiteTree";
    var jsonObj = JSON.stringify(item);

    var promise;
    promise = $.when(promise,
                  $.ajax({
                      type : 'POST',
                      data: jsonObj,
                      url: self.url.val(),
                      success: function(data) {
                          self.configurationTree.getProductsBySiteTree({
                              source: data,
                              rootLabel: 'Sites'
                          });
                      },
                      error: function (data) {
                         self.errorMessage.removeClass("hidden");
                         self.spinner.hide();
                         return;
                      }
                  }));
    return promise;
};

function loadSitesByProductTree() {
        var item = {};
        item ["quoteOptionId"] = self.quoteOptionFilter.val();
        item ["offerId"] = self.offerFilter.val();
        item ["orderId"] = self.orderFilter.val();
        item ["treeViewType"] = "sitesByProductTree";
        var jsonObj = JSON.stringify(item);

        var promise;
        promise = $.when(promise,
                      $.ajax({
                          type : 'POST',
                          data: jsonObj,
                          url: self.url.val(),
                          success: function(data) {
                              self.configurationTree.getSitesByProductTree({
                                  source: data,
                                  rootLabel: 'Products'
                              });
                          },
                          error: function (data) {
                             self.errorMessage.removeClass("hidden");
                             self.spinner.hide();
                             return;
                          }
                      }));
        return promise;
    };

function setUpEvents() {
    var that = this;

    self.quoteOptionFilter = $("#quoteOptionFilter");
    self.offerFilterPanel = $("#offerFilterPanel");
    self.offerFilter = $("#offerFilter");
    self.orderFilter = $("#orderFilter");
    self.orderFilterPanel = $("#orderFilterPanel");
    self.spinner = $('#loading-data-spinner');
    self.cancel = $('#cancelOptionButtonId')
    self.url= $("#viewConfigurationURI");
    self.configurationTree = $("#configurationTree");
    self.errorMessage = $("#errorMessage");
    self.productsBySiteRadio = $("#productsBySiteRadio");
    self.sitesByProductRadio = $("#sitesByProductRadio");
    self.goButton = $("#goButton");
    self.form = $('#viewConfigurationForm');

    var optionsToJson = function(selectElement) {
        var options = [];

        selectElement.find('option').each(function() {
            options.push({value: $(this).val(), text: $(this).text(), parentId:$(this)[0].getAttribute('parentId')});
        });

        return options;
    };

    var resetDropDown = function(selectElement) {
        selectElement.find('option').first().attr('selected', 'selected');
        selectElement.change();
    };

    var offerAllowed = function(quoteOptionId, offer) {
        return quoteOptionId == offer.parentId;
    };

    var orderAllowed = function(offerId, order) {
        return offerId == order.parentId;
    };

    var configureDropDownAndPanel = function(panelIdentifier, selectedValue, allowedOptions, selectElement, optionAllowedFn) {
        self.configurationTree.hide();
        self.errorMessage.addClass("hidden");
        if(selectedValue && null != selectedValue && "" != selectedValue) {
            selectElement.empty().scrollTop(0);

            _.each(allowedOptions, function(allowedOption) {
               if("" == allowedOption.value || optionAllowedFn(selectedValue, allowedOption)) {
                    selectElement.append($('<option>').text(allowedOption.text).val(allowedOption.value));
               }
            });

            $(panelIdentifier).show();
        } else {
            $(panelIdentifier).hide();
        }
    };

    var offersOptions = optionsToJson(self.offerFilter);
    var ordersOptions = optionsToJson(self.orderFilter);

    self.quoteOptionFilter.change(function() {
        // reset
        resetDropDown(self.offerFilter);

        // filter by category group
        var quoteOptionId = self.quoteOptionFilter.val();

        configureDropDownAndPanel("#offerFilterPanel",
                                  quoteOptionId,
                                  offersOptions,
                                  self.offerFilter,
                                  offerAllowed);
    });

    self.offerFilter.change(function () {
        // reset
        resetDropDown(self.orderFilter);

        // filter by category
        var offerId = self.offerFilter.val();

        configureDropDownAndPanel("#orderFilterPanel",
                                  offerId,
                                  ordersOptions,
                                  self.orderFilter,
                                  orderAllowed);
    });

    var disableQuoteOption = function(){
        if(self.quoteOptionFilter.val() != null && self.quoteOptionFilter.val() !=''){
           self.quoteOptionFilter.attr("disabled", true);
           configureDropDownAndPanel("#offerFilterPanel",
                                                 self.quoteOptionFilter.val(),
                                                 optionsToJson(self.offerFilter),
                                                 self.offerFilter,
                                                 offerAllowed);
           self.offerFilterPanel.show();

       }
       else{
           self.offerFilterPanel.hide();
       }
       self.orderFilterPanel.hide();
    };

    disableQuoteOption();
};