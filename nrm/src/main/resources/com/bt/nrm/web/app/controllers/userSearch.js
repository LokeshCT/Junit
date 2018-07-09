angular.module('app')
        .controller('UserSearchController', ['$scope' , '$rootScope', 'nrmUserService', '$stateParams', function ($scope, $rootScope, nrmUserService, $stateParams) {

    $scope.userSearchModel = "";
    $scope.users = [];

    // Search user data based on search parameters
    $scope.getNrmUserByEINOrName = function() {
        if ($scope.userSearchModel == ""){
            $scope.openGenericErrorModal("Search parameter can not be empty. Please enter EIN/First Name or Last Name to search user.");
        } else {
            nrmUserService.getUserByEINOrName($scope.userSearchModel, function (data, status) {
                if (status == '200') {
                    if (!_.isUndefined(data) && data.length > 0) {
                        $scope.users = data;
                    } else {
                        $scope.openGenericErrorModal("No users found. Please try again!");
                    }
                }
            });
        }
    };

}]);