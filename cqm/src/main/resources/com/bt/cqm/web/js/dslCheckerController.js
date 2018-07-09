'use strict';

var module = angular.module('cqm.controllers');

module.controller('DslCheckerController', ['$scope', 'TabService', 'PageContext', 'SessionContext', '$rootScope', 'UserContext', function ($scope, TabService, PageContext, SessionContext, $rootScope, UserContext) {
    function identifyTabToSelect() {
        TabService.getTabs('DSL_CHK').then(function (data) {
            var tabs = data["tabs"];
            var tabId = $scope.context.subState;
            $scope.tabToSelect = TabService.findTab(tabs, tabId);
            if (_.isUndefined($scope.tabToSelect)) {
                $scope.tabToSelect = _.first(tabs);
            }
        });
    }

    identifyTabToSelect();

    $scope.$on(EVENT.LoadDslCheckerApp, function (event) {
       identifyTabToSelect();
    });

    $scope.goHome = function () {
        SessionContext.setState(STATE.CustomerSelection);
    };

}]);
