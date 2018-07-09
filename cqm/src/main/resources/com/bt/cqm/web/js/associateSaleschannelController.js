var module = angular.module('cqm.controllers');

module.controller('AssociateSalesChannelController', ['$scope', 'UIService', 'customerService', '$location', '$rootScope', function ($scope, UIService, customerService, $location, $rootScope) {
    console.log('Inside AssociateSalesChannelController ..');

    $scope.salesChannelList = undefined;
    $scope.associatedSalesChannel = undefined;

    $scope.onPageLoad = function () {
        console.log('On AssociateSalesChannelController Page Load ...');
        $scope.title = 'Associate Sales channel';
        console.log('Getting all saleschannels ..');
        $scope.salesChannelList = customerService.getAllAvailableSalesChannel();
        window.setTimeout(function () {
            $(window).resize();
        }, 10);
    };


    $scope.onClosure = function(){
        $scope.reset();
    }

    $scope.reset = function(){
        $scope.associatedSalesChannel = $scope.salesChannelList[0];
        $('#associatedChannel').prop('value', '0');
        $('#roleId').prop('value', '');
    }

    $scope.onChangeSalesChannel = function (channel) {
        $scope.associatedSalesChannel = channel;
    }

    $scope.onSubmit = function () {
        if (!_.isUndefined($scope.associatedSalesChannel) && !_.isEmpty($scope.associatedSalesChannel.orgName)) {
            UIService.block();
            customerService.associateSalesChannelToContract($scope.associatedSalesChannel.orgName, $scope.contract.id, function (data, status) {
                if ('200' == status) {
                    $scope.display = false;
                    console.log('Sales Channel Associated to contract.');
                    $rootScope.$broadcast('associatedSalesChannelEvent');

                } else if ('404' == status) {

                    UIService.openDialogBox($scope.title, data.description, true, false, false);
                }
                else {
                    UIService.handleException("Associate Sales channel", data, status);
                }
                UIService.unblock();
            })
            $scope.reset();
        }
    };


}])
    