angular.module('app').controller('RoleGroupSelectionController',['$scope', 'userService', '$window', function($scope, userService, $window){

    $scope.roleGroupConstants = userService.getRoleGroupConstants();
    $scope.cqmUrl = userService.getCQMUrl();
    $scope.nrmUrl = userService.getNRMUrl();

    userService.getRSQEUserRelatedRoles(function (data, status) {
        if (status == '200') {
            $scope.userRoles = data;

            for (var i =0; i < $scope.userRoles.length ; i++) {
                if($scope.userRoles[i].roleGroup.roleGroupName == $scope.roleGroupConstants.CQM_ROLE_GROUP_NAME){
                    $scope.hasCQMRole = true;
                }else if($scope.userRoles[i].roleGroup.roleGroupName == $scope.roleGroupConstants.NRM_ROLE_GROUP_NAME){
                    $scope.hasNRMRole = true;
                }
            }

        }else {
            $state.go('error404');
        }
    });

    $scope.goToCQM = function(){
         $window.location.href = $scope.cqmUrl;
    }

    $scope.goToNRM = function(){
        $window.location.href = $scope.nrmUrl;
    }


}]) ;
