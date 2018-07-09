var cqmTest = angular.module('cqmTestModule', ['cqm.test.controllers', 'ngRoute']);


cqmTest.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/select-customer', {templateUrl: "select-customer-test.html", controller: 'SelectCustomerTestCtrl'});
    $routeProvider.when('/dialog', {templateUrl: "dialog-test.html", controller: 'DialogTestController'});
    $routeProvider.when('/userMessageDialog', {templateUrl: "user-message-dialog-test.html", controller: 'UserMessageDialogTestController'});
    $routeProvider.when('/createCustomer', {templateUrl: "create-customer-test.html", controller: 'CreateCustomerTestController'});
}]);