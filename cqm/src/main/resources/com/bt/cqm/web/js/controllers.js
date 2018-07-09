//'use strict';

//Defining dummy console.log function to fix IE problem
if (!window.console) {
    console = { log:function () {
    } };
}

/* CQM Controllers */

var cqmAppControllers = angular.module('cqm.controllers', ['cqm.services', 'ngGrid','ui.grid', 'ui.select2', 'cqm.directives', 'cqm.dashboard']);

cqmAppControllers.controller('headerController', ['$scope', 'UIService', 'salesUserService', function ($scope, UIService, salesUserService) {
    console.log('Inside headerController');
    $scope.salesUserLoggedIn = {'cusFullName':"Guest", 'errorMessage':"Welcome! Guest"};
    $scope.$on('salesUserLoaded', function (event, salesUser) {
        $scope.salesUserLoggedIn = salesUser;
        _.forEach(salesUser.salesChannels, function (sChannel) {
            if (sChannel.defaultSalesChannel == 'Y') {
                salesUserService.broadcastSalesChannelSelected(sChannel);
            }
        });


    });

}]);