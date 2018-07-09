var cqmTest = angular.module('cqm.test.controllers', ['cqm']);

cqmTest.controller('SelectCustomerTestCtrl', ['$scope', function($scope) {

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
