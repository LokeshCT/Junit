'use strict';

var module = angular.module('cqm.test.controllers');

module.controller('CreateCustomerTestController', ['$scope', function($scope) {
    $scope.display = false;
    $scope.launchCreateCustomerDialog = function() {
        $scope.display = true;
    };


    $scope.title = 'Create Customer';

    $scope.salesUser = {"ein":"608308027","name":"Leela Sankar Pinjala","role":"CP User","salesChannelList":[
        {
            "id":"BT NETHERLANDS",
            "name":"BT NETHERLANDS",
            "defaultSalesChannel":"N"
        },
        {
            "id":"BT FRANCE",
            "name":"BT FRANCE",
            "defaultSalesChannel":"N"
        },
        {
            "id":"BT AMERICAS",
            "name":"BT AMERICAS",
            "defaultSalesChannel":"Y"
        },
        {
            "id":"BT LUXEMBOURG",
            "name":"BT LUXEMBOURG",
            "defaultSalesChannel":"N"
        }
    ]};
}]);