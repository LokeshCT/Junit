var module = angular.module('cqm.controllers');

module.controller('MultipleCentralSiteController', ['$scope', 'UIService', 'customerService', '$location', '$rootScope', function ($scope, UIService, customerService, $location, $rootScope) {
    console.log('Inside MultipleCentralSiteController ..');

    $scope.selectedCentralSiteId = undefined;
    $scope.onPageLoad = function () {
        console.log('On MultipleCentralSiteController Page Load ...');
        $scope.title = 'Associate a Central Sites';
        $scope.isSiteSelected = false;
        window.setTimeout(function () {
            $(window).resize();
        }, 10);
    };

    $scope.onSubmit = function () {

        if (!_.isUndefined($scope.selectedCentralSiteId)) {
            UIService.block();
            customerService.associateCentralSite($scope.selectedCentralSiteId, function (data, status) {
                if ('200' == status) {
                    $scope.display = false;
                    $rootScope.$broadcast('LoadCentralSiteEvent');
                    UIService.unblock();
                } else {
                    UIService.handleException("Associate Central Site", data, status);
                }
            })
        }
    };

    $scope.onSelect = function (site) {

        if (!$scope.isSiteSelected) {
            if (!_.isUndefined(site)) {
                $scope.selectedCentralSiteId = site.entity.siteId;
            }
        } else {
            $scope.selectedCentralSiteId = undefined;
        }


    };


    $scope.centralSitesGrid = { data:'sites', multiSelect:false, keepLastSelected:false,
        showFooter:true,
        width:'*',
        afterSelectionChange:function (item, event) {
            if (item.selected == false) {
                console.log('got de-select event');
                //$scope.isCustomerSelected = false;
                return;
            }
        },
        columnDefs:[
            {field:'\u2714',
                width:25,
                sortable:false,
                enableColumnResize:true,
                groupable:false,
                headerCellTemplate:'<div><div>',
                cellTemplate:'<div class="ngSelectionCell"><input tabindex="-1"  type="radio" name="chosenCustomer" id="chosenCustomer" ng-model="isSiteSelected" ng-click="onSelect(row)" value="custRadioBtnSelected"/></div>' },
            {field:'siteId', displayName:'Site ID'},
            {field:'name', displayName:'Site Name'},
            {field:'country', displayName:'Country'},
            {field:'city', displayName:'City'}
        ] };

}])
    