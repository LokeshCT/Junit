angular.module('app').controller('UserManagementBaseController', ['$scope', 'UrlConfiguration', function($scope, UrlConfiguration){

    $scope.headerImageUrl = UrlConfiguration.headerImageUrl;

}]) ;
