var module = angular.module('cqm.controllers');

module.controller('actionsController', ['$scope', '$routeParams', function ($scope, $routeParams) {
    console.log('Inside action controller');

    var customerActionsArray = [
        {
            "actName":"Customer Site",
            "actReference":"centralSite",
            "actId":"centralSite",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/centralSite.html",
            "isActive":"inactive"
        },
        {
            "actName":"Customer Contacts",
            "actReference":"centralSiteContact",
            "actId":"centralSiteContact",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/centralSiteContact.html",
            "isActive":"inactive"
        },
        {
            "actName":"Create Customer",
            "actReference":"createCustomer",
            "actId":"createCustomer",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/createCustomer.html",
            "isActive":"inactive"
        }
    ];

    var quoteActionsArray = [
        {
            "actName":"Quote Details",
            "actReference":"searchQuotes",
            "actId":"searchQuotes",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/searchQuotes.html",
            "isActive":"inactive"
        },

        {
            "actName":"Create Quote",
            "actReference":"createQuote",
            "actId":"createQuote",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/createQuote.html",
            "isActive":"inactive"
        }

    ];

    var indirectCustomerActionsArray = [
        {
            "actName":"Create Channel Partner",
            "actReference":"createChannelPartner",
            "actId":"createChannelPartnerId",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/createChannelPartner.html",
            "isActive":"inactive"
        },
        {
            "actName":"Price Book Details",
            "actReference":"priceBookDetails",
            "actId":"priceBookDetailsID",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/priceBookDetails.html",
            "isActive":"inactive"
        },
        {
            "actName":"Channel Monthly Committment",
            "actReference":"channelMonthlyCommittment",
            "actId":"channelMonthlyCommittmentID",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/ChannelMonthlyCommittment.html",
            "isActive":"inactive"
        }
    ];

    var activitiesActionsArray = [
        {
            "actName":"View/Update Activity",
            "actReference":"viewUpdateActivity",
            "actId":"viewUpdateActivityActID",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/viewUpdateActivity.html",
            "isActive":"inactive"
        },
        {
            "actName":"Create Activity",
            "actReference":"createActivity",
            "actId":"createActivityActId",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/createActivity.html",
            "isActive":"inactive"
        }
    ];
    var billingAccountActionsArray = [
        {
            "actName":"Billing Account Details",
            "actReference":"billingAccountDetails",
            "actId":"billingAccountTabID",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/billingAccountDetail.html",
            "isActive":"inactive"
        },
        {
            "actName":"Manage LE",
            "actReference":"legalEntity",
            "actId":"legalEntityID",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/legalEntity.html",
            "isActive":"inactive"
        },
        {
            "actName":"Map LE",
            "actReference":"mapLegalEntity",
            "actId":"mapLegalEntityID",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/mapLegalEntity.html",
            "isActive":"inactive"
        }
    ];


    var userManagementActionsArray = [
        {
            "actName":"User Configuration",
            "actReference":"userConfiguration",
            "actId":"userConfigurationActID",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/userManagement/userConfiguration.html",
            "isActive":"inactive"
        }
    ];

    var vpnActionsArray = [
        {
            "actName":"VPN",
            "actReference":"vpn",
            "actId":"vpnTabId",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/vpn.html",
            "isActive":"inactive"
        }
    ];
    var branchSiteActionsArray = [
        {
            "actName":"Branch Site",
            "actReference":"branchSite",
            "actId":"branchSiteTabId",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/branchSite.html",
            "isActive":"inactive"
        },
        {
            "actName":"Contacts",
            "actReference":"branchSiteContact",
            "actId":"branchSiteTabId",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/branchSiteContact.html",
            "isActive":"inactive"
        }
    ];
    var orderDetailsActionsArray = [
        {
            "actName":"Search Order",
            "actReference":"searchOrders",
            "actId":"searchOrderActId",
            "templateUrl":"/cqm/static/cqm/web/partials/grid/searchOrders.html",
            "isActive":"inactive"
        }
    ];

    $scope.actions = customerActionsArray;

    $scope.$on('$routeChangeSuccess', function (event, currentRoute, previousRoute) {
        console.log("Current tab: ", currentRoute.tabId);
        var actionTobeSetInScope = [];
        var actionsArray = [];
        if (currentRoute.tabId == "customer") {
            actionsArray = customerActionsArray;
        } else if (currentRoute.tabId == "quotes") {
            actionsArray = quoteActionsArray;
        } else if (currentRoute.tabId == "indirectCustomerID") {
            actionsArray = indirectCustomerActionsArray;
        } else if (currentRoute.tabId == "activitiesTabID") {
            actionsArray = activitiesActionsArray;
        } else if(currentRoute.tabId == "billingAccountTabID") {
            actionsArray = billingAccountActionsArray;
        } else if (currentRoute.tabId == "userManagementTabID"){
            actionsArray = userManagementActionsArray;    //Added by AB
        } else if (currentRoute.tabId == "ordersTabID") {
            actionsArray = orderDetailsActionsArray;
        } else if (currentRoute.tabId == "vpnTabID") {
            actionsArray = vpnActionsArray;
        }  else if (currentRoute.tabId == "branchSiteTabId") {
            actionsArray = branchSiteActionsArray;
        }

        _.forEach(actionsArray, function (act) {
            if (act.templateUrl == currentRoute.templateUrl) {
                act.isActive = "label selected active";
            } else {
                act.isActive = "label unselected inactive";
            }

            actionTobeSetInScope.push(act);
        });

        $scope.actions = actionTobeSetInScope;
    });
}]);