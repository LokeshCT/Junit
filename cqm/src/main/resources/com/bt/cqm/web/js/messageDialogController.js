'use strict';

var controllers = angular.module('cqm.controllers');

controllers.controller('MessageDialogController', ['$scope', 'dateFilter', 'UIService', function($scope, dateFilter, UIService) {

    $scope.stackVisible = false;

    $scope.toggle = function() {
        $scope.stackVisible = !$scope.stackVisible;
    };

    $scope.show = false;

    $scope.message = {
        title:'',
        content:'',
        stack:''
    };

    $scope.date = function() {
       return dateFilter(new Date(), 'dd-MMM-yyyy H:mm:ss');
    };

    $scope.ok = function() {};

    $scope.cancel = function() {};

    $scope.error = function (content, stack) {
        $scope.show = true;
        $scope.message.content = content;
        $scope.message.stack = stack;
        $scope.message.title = 'Error!';
        $scope.message.date = dateFilter(new Date(), 'dd-MMM-yyyy H:mm:ss');
        $scope.hideDetailsButton = false;
    };

    $scope.warning = function(content, okFunction) {
        $scope.show = true;
        $scope.message.content = content;
        $scope.message.stack = '';
        $scope.message.title = 'Warning!';
        $scope.message.ok = okFunction;
        $scope.hideDetailsButton = true;
    };

    UIService.initializeDialog($scope);
}]);