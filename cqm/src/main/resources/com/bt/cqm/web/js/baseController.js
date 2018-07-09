var module = angular.module('cqm.controllers');

module.controller('CQMBaseController', ['$scope' , '$rootScope', 'salesUserService', 'UIService', 'UserContext','countryService','billingAccountService','SessionContext','WebMetrics', function ($scope, $rootScope, salesUserService, UIService, UserContext,countryService,billingAccountService, SessionContext,WebMetrics) {

    console.log('Inside CQMController');
    $scope.salesUser = {'name':"Guest", 'roles[0].roleName':"Guest"};
    $scope.salesUser.salesChannelList = [];

    $scope.initSalesUser = function () {
        UIService.block();
        console.log('About to load Sales User ...');
        salesUserService.getSalesUser(function (data, status) {
            if (status == '200') {
                console.log('Loaded Sales User ...');
                var salesUser = data;
                UserContext.initialize(salesUser);
                SessionContext.setUser(salesUser);
                SessionContext.put(SESSION.SelectedRole, salesUser.roles[0]);
                UserContext.setReqHeader();
                WebMetrics.captureWebMetrics('CQM- Main Page - Page Load');
            }else if(status == '404'){
                console.log('Sales User Not found...');
                UIService.openDialogBox('CQM', data, true, false);
            }else {
                console.log('Failed to fetch Sales User...');
                UIService.handleException('CQM',data,status);
            }
            UIService.unblock();
        });
    };

    $scope.initSalesUser();

    countryService.getAllCountries(); // Load All Countries and cache it at service

    billingAccountService.getCurrencyCodes(); // Load All Currency Codes on Client

    $scope.context = {
        state:STATE.CustomerSelection
    };

    $scope.$on(EVENT.StateChange, function(event, state) {
        $scope.context.state = state;
    });

}]);
