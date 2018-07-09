'use strict';

var module = angular.module('cqm.test.controllers');

module.controller('DialogTestController', ['$scope', function($scope) {

    $scope.dialogType = '';

    $scope.showDialog = function(option) {
        $scope.show = true;
        $scope.dialogType = _.isUndefined(option) ? '' : option;
    };

    $scope.show = false;
    $scope.title = "test title";

    $scope.okAction = function() {
        $scope.actionResult = 'OkActionPerformed';
    };

    $scope.cancelAction = function() {
        $scope.actionResult = 'CancelActionPerformed';
    };

    $scope.closeAction = function() {
        $scope.actionResult = 'CloseActionPerformed';
    };
}]);