var rsqe = rsqe || {};

var submitWebMetricsUri;
var navStartTime;
var pageLoadStartTime;
var webMetricsSent = false;
var selectedTabId;
var currentTabId;
var userAction;

function handleDataTableError(sSource, aoData, fnCallback, oSettings, errorFn) {
    oSettings.jqXHR = $.ajax({
    "dataType": 'json',
    "type": "GET",
    "url": sSource,
    "data": aoData,
    "success": fnCallback,
    "error" : function(xhr, textStatus, error) {
        errorFn(xhr, textStatus, error);
    }
  });
}

function showLoadingMsg() {
    $('#loadingMessage').text('Loading...');
    $("#loadingMessage").show();
}

function hideLoadingMsg() {
    $("#loadingMessage").hide();
}

rsqe.BasePage = function() {
     submitWebMetricsUri = $("#submitWebMetricsUri").val();
};

// This function changes the active tabs content. I.e. When navigating between tabs it will replace the previous tabs content with the tab
// selected by the user.
rsqe.BasePage.prototype = {
    initialise : function() {
        showLoadingMsg();

        var that = this;
        $("#content").tabs({
                               load:that.tabLoaded,
                               select:that.tabSelected,
                               active:false,
                               collapsible:true,
                               ajaxOptions: { error: that.tabFailedToLoad }
                           });
        var hash = location.hash;
        if (hash) {
            var index = $("#tabs a").index($(hash));
            $("#content").tabs("select", index);

            pageLoadStartTime = window.performance ? window.performance.timing.navigationStart : null;
            userAction = "Page Reload";
        }
    },

    tabFailedToLoad : function(data, ui) {
        if(0 == data.status) {return;} // ignore abort status as it is a valid transition.
        new rsqe.StatusMessage("#overallError").show('Failed to load Tab. ' + data.statusText + " " +  (undefined != data.responseText ? data.responseText : ''));
    },

    tabSelected : function(event, ui) {
        if(ui.tab.id == currentTabId) {
            return false;
        }

        new rsqe.StatusMessage("#overallError").hide();
        new rsqe.StatusMessage("#overallMessage").hide();
        navStartTime = new Date().getTime();
        webMetricsSent = false;
        userAction = userAction ? userAction : 'Tab Click';

        if (this.currentTab) {
            try {
                this.currentTab.destroy();
            } catch (err) {
                console.log(err);
            }
            $("div.ui-tabs-panel").empty();
        }
        if (location.hash.replace("#", "") != ui.tab.id) {
            location.hash = ui.tab.id;
        }

        selectedTabId = ui.tab.id;
        showLoadingMsg();
    },

    tabLoaded : function(event, ui) {
        if(ui.tab.id == currentTabId) {
            return false;
        }

        hideLoadingMsg();
        currentTabId = ui.tab.id;

        var tabObject;
        try {

            // The new Pricing Tab cannot be loaded in the same manner as the other tabs. Handle this case.
            if (ui.tab.id === "PricingTab") {
                tabObject = rsqe.pricingTab;
            }

            // This call here means that a JavaScript object with a name matching the tab name (as set in Java) will be appended to the
            // rsqe JavaScript object.
            else {
                tabObject = eval("new rsqe." + ui.tab.id);
            }

        } catch(e) {   // not all tabs require javascript
            tabObject = null;
        }

        if (tabObject && tabObject.initialise)
            this.currentTab = tabObject.initialise();
        else
            this.currentTab = null;

        new rsqe.RightPane().initialize(this.currentTab);

        if( !selectedTabId || selectedTabId == ui.tab.id) {
            selectedTabId = ui.tab.id;
            $(document).ajaxStop(function() {
                var itemCount = tabObject.getItemCount ? tabObject.getItemCount() : -1;
                if ( !webMetricsSent ) {
                    postWebMetrics(submitWebMetricsUri,
                                   selectedTabId,
                                   pageLoadStartTime ? pageLoadStartTime : navStartTime,
                                   userAction,
                                   itemCount);
                    webMetricsSent = true;
                    pageLoadStartTime = null;
                    userAction = null;
                    selectedTabId = null;

                    $(this).unbind("ajaxStop");
                }
            });
        }

    },

    table : function(cssSelector) {
        var self = this;
        var rows = $(cssSelector + " tbody tr");
        rows.mouseover(
            function() {
                $(this).addClass("selectedRow");

            });
        rows.mouseout(
            function() {
                $(this).removeClass("selectedRow");
            });
        rows.click(function(event) {
            if (!self.containsClickableElement(event)) {
                window.location.href = $(".uri", this).text();
            }
        });
    },

    containsClickableElement: function(event) {
        var sourceIsInput = event.target.tagName.toLowerCase() === "input"
                                || $(event.target).hasClass("action")
                                || $(event.target).parent().hasClass("action")
            || event.target.tagName.toLowerCase() === "a";
        var sourceHasInput = $("input", event.target).length > 0;
        return sourceIsInput || sourceHasInput;
    }

};

//move this class out into its own file if it becomes any bigger
rsqe.RightPane = function () {
    this.initialize = function (currentTab) {
        var sliderButtons = $(".rightPaneContainer .slideRightPane");
        sliderButtons.unbind("click");
        sliderButtons.click(function() {
            $('#content').toggleClass('noRightPane');
            if (currentTab != null && currentTab.calculateTableWidth) {
                currentTab.calculateTableWidth()
            }
        });
        $(".slideFilter").toggleClass('noFilterPane');
        $(".slideFilter").parent().find('.filterContent').hide();
        var slideFilter = $(".slideFilter");
        slideFilter.unbind("click");
        slideFilter.click(function() {
            $(this).toggleClass('noFilterPane');
            $(this).parent().find('.filterContent').toggle();
        });
    };
};

function enableButtons(parent) {
    parent.find(':button').each(function(index, element) {
        enableButton($(element));
    });
}

function disableButtons(parent) {
    parent.find(':button').each(function(index, element) {
        disableButton($(element));
    });
}

function disableButton(btn) {
    btn.attr("disabled", true);
    btn.addClass("disableBtn");
}

function enableButton(btn) {
    btn.attr("disabled", false);
    btn.removeClass("disableBtn");
}

function disableElement(el) {
    el.attr("disabled", "disabled");
    return el;
}

function enableElement(el) {
    el.removeAttr("disabled");
    return el;
}

