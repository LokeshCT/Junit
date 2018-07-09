angular.module('app')
        .controller('MyProfileController', ['$scope' , 'nrmUserService', function ($scope, nrmUserService) {

    $scope.userStats = {};

    nrmUserService.getUserStats($scope.nrmUser, function (data, status) {
        if (status == '200') {
            if(!_.isUndefined(data)){
                $scope.userStats = data;
                $scope.userStats.requestsCreated = data.requestsCreated;
                $scope.userStats.signIns = data.signIns;
                $scope.userStats.signOffs = data.signOffs;
            }
        }else {
            $scope.openGenericErrorModal("Error loading data. Please try after some time.");
        }
    });
}]);