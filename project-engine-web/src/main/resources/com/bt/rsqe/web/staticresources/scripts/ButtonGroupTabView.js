function ButtonGroupTabView(radioGroup) {
    this._radioGroup = radioGroup;
    this._radioGroup.buttonset();
    this._tabSet = [];
}

ButtonGroupTabView.prototype.withView = function(viewSelector, radioSelector, onShowCallbackFn, showByDefault) {
    this._tabSet.push({
        radio: this._radioGroup.find(radioSelector),
        view: $(viewSelector),
        showByDefault: showByDefault ? showByDefault : false,
        onShowCallbackFn : onShowCallbackFn
    });
    return this;
};

ButtonGroupTabView.prototype.initView = function() {
    var that = this;
    var toggleVisibility = function(visibleView) {
        _.each(that._tabSet, function(tab) {
            if (tab.view.selector == visibleView.selector) {
                tab.view.show();
                if(tab.onShowCallbackFn) {
                    tab.onShowCallbackFn();
                }
            }else {
                tab.view.hide();
            }
        });
    };

    // remove rounded corners!
    this._tabSet[0].radio.removeClass("ui-corner-left");
    this._tabSet[this._tabSet.length-1].radio.removeClass("ui-corner-right");

    _.each(this._tabSet, function(tab) {
        tab.radio.click(function() {
            toggleVisibility(tab.view);
        });

        if(tab.showByDefault) {
            tab.radio.click();
        } else {
            tab.view.hide();
        }
    });
};

