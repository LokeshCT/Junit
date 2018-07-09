var module = angular.module('cqm-test', ['cqm']);

module.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/chooseCustomer', {templateUrl: '/cqm/static/ChooseCustomerTest.html', controller: 'CreateCustomerTestController'});
}]);