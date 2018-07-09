angular.module('app')
        .controller('TemplateController', ['$scope' , '$rootScope', 'ProductTemplateService', '$stateParams', function ($scope, $rootScope, ProductTemplateService, $stateParams) {

    ProductTemplateService.getTemplateByTemplateCode($stateParams.templateCode,$stateParams.templateVersion,function (data, status) {
        if (status == '200') {
            if(!_.isUndefined(data)){
                $scope.templateData = data;
                if (!_.isUndefined($scope.templateData) && !_.isUndefined($scope.templateData.versionCreatedDate)) {
                    $scope.templateData.versionCreatedDate = $scope.getReadableDate($scope.templateData.versionCreatedDate);
                }
            }
        }
    });

    $scope.columnBreak = 4;//max number of cols

    $scope.startNewRow = function (index, count) {
        return ((index) % count) === 0;
    };

    $scope.m_names = new Array("January", "February", "March",
                               "April", "May", "June", "July", "August", "September",
                               "October", "November", "December");

    $scope.getReadableDate = function(dateString){
        var createdDate = new Date(dateString);
        var createdDay = createdDate.getDate();
        var createdMonth = $scope.m_names[createdDate.getMonth()];
        var createdYear = createdDate.getFullYear();

        return createdDay+"-"+createdMonth+"-"+createdYear;
    }

}]);