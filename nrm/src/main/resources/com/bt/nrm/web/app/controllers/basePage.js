angular.module('app')
        .controller('NRMBaseController', ['$scope', '$state', 'UrlConfiguration', 'nrmUserService', '$modal', function ($scope, $state, UrlConfiguration, nrmUserService, $modal) {

    $scope.headerImageUri = UrlConfiguration.headerImageUri;
    $scope.nrmUser =  {};

    nrmUserService.getNrmUserByUserId(function (data, status) {
        if (status == '200') {
            if(!_.isUndefined(data)){
                $scope.nrmUser = data;
            }
        }else {
            $state.go('error404');
        }
    });

    $scope.ifAuthorized = function (userRoles, menuRoles) {
        var isVisible = false;
        _.each(userRoles,function(role){
            if(_.contains(menuRoles, role.roleId)){
                isVisible = true;
            }
        });
        return isVisible;
    }

    $scope.getCurrentDateNTime = function(){
        var monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
        var currentDate = new Date();
        return currentDate.getDate() + " " + monthNames[currentDate.getMonth()] + " " + currentDate.getFullYear() + " at " + currentDate.getHours() + ":" + currentDate.getMinutes();
    }

    $scope.getDateAndTime = function(milliseconds) {   //This method takes milliseconds and returns the formatted string with date and time
        if(!_.isUndefined(milliseconds)){
            return $scope.getDate(milliseconds) + "  " + $scope.getTime(milliseconds);
        }
        return "";
    };

    $scope.getDate = function(milliseconds) {   //This method takes milliseconds and returns the formatted string with just date
        if(!_.isUndefined(milliseconds)){
            var date = new Date(milliseconds);
            return date.getDate() + "/" + (date.getMonth()+1) + "/" + date.getFullYear();
        }
        return "";
    };

    $scope.getTime = function(milliseconds) {   //This method takes milliseconds and returns the formatted string with just time
        if(!_.isUndefined(milliseconds)){
            var date = new Date(milliseconds);
            var hours = date.getHours();
            var minutes = date.getMinutes();
            var ampm = hours >= 12 ? 'PM' : 'AM';
            hours = hours ? (hours % 12) : 12; // the hour '0' should be '12'
            minutes = minutes < 10 ? '0'+ minutes : minutes;
            var time = hours + ':' + minutes + ' ' + ampm;
            return  time;
        }
        return "";
    };

    $scope.goToRequestDetails = function () {
        $state.go('app.requestDetails({requestId:requestId})');
    }

    $scope.openGenericModal = function (windowClass, templateUrl, size, modalData) {
        $scope.modalInstance = $modal.open({
                                               windowClass: windowClass,
                                               templateUrl: templateUrl,
                                               controller: 'GenericModalController',
                                               size: size,
                                               resolve: {
                                                   modalData: function () {
                                                       return modalData;
                                                   }
                                               }
                                           });
        $scope.modalInstance.result.then(function () {
        });
    };

    $scope.openGenericWarningModal = function(message, callback){
        var modalData = { "message" : message, "callback" : callback};
        $scope.openGenericModal('modal-message modal-purple','/nrm/static/views/modal/generic/genericWarningModal.html','',modalData);
    };

    $scope.openGenericSuccessModal = function(message){
        var modalData = { "message" : message};
        $scope.openGenericModal('modal-message modal-success','/nrm/static/views/modal/generic/genericSuccessModal.html','',modalData);
    };

    $scope.openGenericErrorModal = function(message){
        var modalData = { "message" : message};
        $scope.openGenericModal('modal-message modal-danger', '/nrm/static/views/modal/generic/genericErrorModal.html', '', modalData);
    };

    $scope.openGenericPageLoadErrorModal = function(message, newState){
        var modalData = { "message" : message, "newState" : newState};
        $scope.openGenericModal('modal-message modal-danger', '/nrm/static/views/modal/generic/genericPageLoadErrorModal.html', '', modalData);
    };

}]);


angular.module('app')
        .controller('GenericModalController', ['$scope', '$rootScope', '$modalInstance', '$state', 'modalData', function ($scope, $rootScope, $modalInstance, $state, modalData) {

    $scope.modalData = modalData;

    $scope.ok = function () {
        $modalInstance.close();
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

    $scope.pageLoadErrorOk  = function () {
        $state.go($scope.modalData.newState);
        $modalInstance.dismiss();
    }

    $scope.warningOK = function (){
        if(!_.isUndefined($scope.modalData.callback)){
            $scope.modalData.callback();
        }
        $modalInstance.close();
    }

}]);
