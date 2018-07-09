var module = angular.module('cqm.controllers');

module.controller('locationController', ['$scope', 'UIService', 'branchSiteService','$rootScope', function ($scope, UIService, branchSiteService,$rootScope) {
    console.log('Inside LocationController ..');

    $scope.onPageLoad = function () {
        console.log('On LocationController Page Load ...');
        $scope.title = 'Add Location';

    };

    $scope.onSubmit = function () {
        $scope.createLocation();
    };

    $scope.$watch('subLocNameDlg',function(){
       console.log('Changed subLocNameDlg');
    })

    $scope.createLocation = function () {
        UIService.block();
        branchSiteService.createLocation($scope.siteId, $scope.$$childTail.room, $scope.$$childTail.floor,$scope.$$childTail.subLocationName, function (data, status) {
            var title = 'Add Location';

            if (status == '200') {
                UIService.openDialogBox(title, 'Successfully added location.', true, false).result.then(function () {
                    $scope.getBranchSite();
                }, function () {
                    $scope.getBranchSite();
                })
                ;
                UIService.unblock();
                $scope.$parent.loadBranchSite();
            } else if (status == '400'){
                UIService.openDialogBox(title, 'Provide a valid Sub location Name, Floor or Room !!', true, false);
                UIService.unblock();
            } else if (status == '409'){
                UIService.openDialogBox(title, 'A Site already exist with same Sub Location Name Or Floor/Room !!', true, false);
                UIService.unblock();
            }else
            {
                UIService.handleException(title, data, status);
                UIService.unblock();
            }
        });
    }


}])
    