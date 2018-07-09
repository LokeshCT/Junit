var module = angular.module('cqm.controllers');

module.controller('tabsController', ['$scope', '$routeParams', '$location', '$rootScope', 'TabService','SessionContext', '$location', function ($scope, $routeParams, $location, $rootScope, TabService,SessionContext,$location) {

    $scope.tabClick = function(tab) {
        _.each($scope.tabs, function(tab) {
            tab.selected = false;
        });
        tab.selected = true;

        if (!_.isEmpty(tab.uri)) {
            $location.path(tab.uri);
        }
        $rootScope.$broadcast(EVENT.TabChange, tab);
    };

    function loadTabs() {
        TabService.getTabs().then(function(data) {
            $scope.tabs = data["tabs"];
            $scope.tabClick($scope.$parent.tabToSelect);
        });
    }

    $scope.$on(EVENT.TabDeSelect, function (event) {
        _.each($scope.tabs, function(tab) {
            tab.selected = false;
        });
    });

    $scope.$on(EVENT.GoToCustomerTab, function (event) {
        $location.path('/centralSite');
        SessionContext.navigateToTab('Customer');

        _.each($scope.tabs, function(tab) {
            if(tab.label =='Customer'){
                tab.selected = true;
                _.each(tab.treeNode.children, function(child){
                    if(child.id=='customerSite'){
                        child.selected =true;
                    }else{
                        child.selected =false;
                    }
                })
            }else{
                tab.selected = false;
            }

        });
    });

    loadTabs();
}]);
