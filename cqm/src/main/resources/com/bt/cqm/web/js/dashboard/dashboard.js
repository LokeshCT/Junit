'use strict';

var dashboard = angular.module('cqm.dashboard', ['ui.grid', 'ngRoute', 'ngAnimate', 'cqm.controllers']);

dashboard.config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/dashboard', { templateUrl : '/cqm/static/cqm/web/partials/dashboard/dashboard.html', controller: 'dashboardController' });
    $routeProvider.when('/allUserQuoteStatus', { templateUrl : '/cqm/static/cqm/web/partials/dashboard/allUserQuotesStatus.html', controller: 'dashboardController' });
}]);


dashboard.directive('sqeDashboardMetric', [function () {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            value: '=',
            name: '@'
        },
        template: '<div class="sqe-dashboard-metric" style="position: relative;"> <p>{{name}} <br>{{value}}</p></div>',
        link: function (scope, ele) {
            ele.removeAttr('style');
        }
    }
}]);



dashboard.directive('sqeStatusImg', [function () {
    return {
        restrict: 'E',
        replace: true,
        link: function (scope, element, attr) {
            element.html('<div class="sqe-status-image ' + attr.class + '"><img src="/cqm/static/cqm/web/img/rgb.png" class="' + attr.type + '"></div>');
        }
    };
}]);

dashboard.factory('DashboardService', [function () {

    return {
        adapt: function (quoteStatusArray) {

            return _.map(quoteStatusArray, function (quoteStatus) {
                var cloneCopy = _.clone(quoteStatus);
                cloneCopy.quoteName = quoteStatus.quoteName + ' (' + quoteStatus.quoteVersion + ')';
                return cloneCopy;
            });
        }
    };
}]);


dashboard.controller('dashboardController', ['$scope', '$location', '$q', '$http', 'UIService', 'UrlConfiguration', 'DashboardService', 'quoteService',
    function ($scope, $location, $q, $http, UIService, UrlConfiguration, DashboardService, quoteService) {

        $scope.userQuoteMetrics = {
            "numberOfCustomers": "",
            "numberOfQuotes": "",
            "numberOfSites": "",
            "numberOfOrders": ""
        };

        $scope.launchQuote = function (row) {
            UIService.block();
            quoteService.getSqeAppURL(row.entity.expedioQuoteId, row.entity.expedioQuoteVersion, row.entity.quoteHeaderId, $scope.salesUser.ein, function (sqeUrl, status) {
                UIService.unblock();
                if (status == '200') {
                    window.open(sqeUrl);
                } else {
                    UIService.handleException('Launch Configurator', data, status);
                }
            });
        };

        var rgbStatusCellTemplate = '<sqe-status-img type="{{COL_FIELD}}"></sqe-status-img>';
        var quoteNameCellTemplate = '<a href="" ng-click="grid.appScope.launchQuote(row)">{{COL_FIELD}}</a>';

        $scope.quoteStatusSummaryGridOptions = $scope.recentQuoteStatusSummaryGridOptions = {
            flatEntityAccess: true,
            fastWatch: true,
            enableHorizontalScrollbar: 0,
            enableVerticalScrollbar: 0,
            columnDefs: [
            {name : 'Quote Name', field : 'quoteName', cellClass : 'grid-cell-left', cellTemplate : quoteNameCellTemplate, width: 250, enableHiding: false},
                {name: 'Last Access On', field: 'lastAccessedOn', cellClass: 'grid-cell-centre', width: 160, enableHiding: false},
                {name: 'Site Count', field: 'siteCount', width: 100, cellClass: 'grid-cell-centre', enableHiding: false},
                {name: 'Config ', field: 'configStatus', cellTemplate: rgbStatusCellTemplate, enableHiding: false},
                {name: 'Pricing', field: 'pricingStatus', cellTemplate: rgbStatusCellTemplate, enableHiding: false},
                {name: 'Offer', field: 'offerStatus', cellTemplate: rgbStatusCellTemplate, enableHiding: false},
                {name: 'Order', field: 'orderStatus', cellTemplate: rgbStatusCellTemplate, enableHiding: false}
            ],
            data: []
        };

        var getFromServer = function (url) {
            UIService.block();
            var deferred = $q.defer();
            $http.get(url).then(function (response) {
                deferred.resolve(response.data);
                UIService.unblock();
            });
            return deferred.promise;
        };

        if ($location.path() == '/dashboard') {
            getFromServer(UrlConfiguration.recentQuoteStatusUrl).then(function (data) {
                $scope.recentQuoteStatusSummaryGridOptions.data = DashboardService.adapt(data);
            });

            getFromServer(UrlConfiguration.getUserQuoteMetricsUrl).then(function (data) {
                $scope.userQuoteMetrics = data;
            });

        } else if ($location.path() == '/allUserQuoteStatus') {
            getFromServer(UrlConfiguration.allUserQuoteStatusUrl).then(function (data) {
                $scope.quoteStatusSummaryGridOptions.data = DashboardService.adapt(data);
            });
        }

        $scope.backToHomePage = function () {
            $scope.context.state = STATE.CustomerSelection;
        }
    }]);
