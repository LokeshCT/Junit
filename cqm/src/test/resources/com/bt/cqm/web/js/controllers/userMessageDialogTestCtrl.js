'use strict';

var module = angular.module('cqm.test.controllers');

module.controller('UserMessageDialogTestController', ['$scope', function($scope) {

    $scope.showUserMessage = false;

    $scope.userMessage = {};

    $scope.show = function() {
        $scope.userMessage.title = 'Test title';
        $scope.userMessage.content = 'user message content';
        $scope.userMessage.stack = 'stack content';
        $scope.showUserMessage = true;
    };
}]);

