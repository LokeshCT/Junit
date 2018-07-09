'use strict';

var rsqeHeader = angular.module('rsqeCharts',[]);

rsqeHeader.directive('rsqePageHeader', [function() {
    return {
        restrict: 'E',
        replace: true,
        controller:'RsqeHeaderController',
        templateUrl:'/rsqe/static/partials/templates/header.html',
        scope :  {
            user : '=user',
            context : '=',
            showLogout: '='
        }
    };
}]);

rsqeHeader.controller('RsqeHeaderController', ['$scope', function($scope){
        $scope.url = '/rsqe/static/img/mx_banner.jpg';
}]);

