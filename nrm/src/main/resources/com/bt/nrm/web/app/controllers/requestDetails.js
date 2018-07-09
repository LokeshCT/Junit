angular.module('app')
        .controller('RequestDetailsController', ['$scope' ,'$stateParams', 'RequestsService', 'RequestEvaluatorResponseConstantsService', '$modal', function ($scope,  $stateParams, RequestsService, RequestEvaluatorResponseConstantsService, $modal) {

    $scope.requestEvaluatorResponseConstantsService = RequestEvaluatorResponseConstantsService;
    $scope.request = !_.isUndefined($stateParams.request) ? $stateParams.request : null;
    $scope.requestId = $stateParams.requestId!= null ? $stateParams.requestId : null;
    this.requestCommentsModel = "";
    //$scope.requestComments = [];
    $scope.requestComments = (!_.isUndefined($scope.request.comments) && !_.isUndefined($scope.request)) ? JSON.parse($scope.request.comments) : JSON.parse("[]");

    $scope.openCommentsModal = function (windowClass, templateUrl, size, requestGroup, nrmUser) {
        $scope.modalInstance = $modal.open({
                                               windowClass: windowClass,
                                               templateUrl: templateUrl,
                                               controller: 'ControllerEvaluatorCommentsController',
                                               size: size,
                                               resolve: {
                                                   requestGroup: function () {
                                                       return requestGroup;
                                                   },
                                                   nrmUser: function () {
                                                       return nrmUser;
                                                   }
                                               }
                                           });
        $scope.modalInstance.result.then(function (requestGroup) {
            if(requestGroup){
                $scope.currentRequestGroup = _.find($scope.request.requestEvaluators,{requestEvaluatorId:requestGroup.requestEvaluatorId});
                $scope.currentRequestGroup.comments = JSON.stringify(requestGroup.comments);
            }
        });
    };

    if(!$scope.request && !$scope.requestId){
        $scope.openGenericPageLoadErrorModal("Bad Request! Request Id is missing. Please select it again.", 'app.requests');
    }

    if(!$scope.request && $scope.requestId){
        RequestsService.getRequestByRequestId($scope.requestId, function (data, status) {
            if (!_.isUndefined(data)) {
                $scope.request = data;
                $scope.requestComments = (!_.isUndefined($scope.request.comments) && !_.isUndefined($scope.request)) ? JSON.parse($scope.request.comments) : JSON.parse("[]");
            }
        });
    }

    this.saveRequestComment = function () {
        if(!_.isEmpty(this.requestCommentsModel)){
            var formData = "";
            var tempComment = {};
            tempComment['name'] = $scope.nrmUser.fullName;
            tempComment['message'] = JSON.parse(JSON.stringify(this.requestCommentsModel));
            tempComment['date'] = $scope.getCurrentDateNTime();
            //var commentsTobeSaved = JSON.parse(JSON.stringify($scope.requestComments));
            //commentsTobeSaved.push(tempComment);
            var commentsTobeSaved = [];
            commentsTobeSaved = (!_.isUndefined($scope.requestComments) && !_.isNull($scope.requestComments)) ? JSON.parse(JSON.stringify($scope.requestComments)) : JSON.parse("[]");
            commentsTobeSaved.push(tempComment);
            var controller = this;
            RequestsService.saveRequestComments($scope.request.requestId, JSON.stringify(commentsTobeSaved), $scope.nrmUser.EIN, formData, function (data, status) {
                if (status == 200){
                    $scope.requestComments.push(tempComment);
                    controller.requestCommentsModel = "";
                }
            });
        }
    };

    $scope.getCurrentDateNTime = function(){
        var monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
        var currentDate = new Date();
        return currentDate.getDate() + " " + monthNames[currentDate.getMonth()] + " " + currentDate.getFullYear() + " at " + currentDate.getHours() + ":" + currentDate.getMinutes();
    }

}]);

angular.module('app')
        .controller('ControllerEvaluatorCommentsController', ['$scope', '$rootScope', '$modalInstance', 'requestGroup', 'nrmUser', 'RequestsService', function ($scope, $rootScope, $modalInstance, requestGroup, nrmUser, RequestsService) {
    $scope.requestGroup = requestGroup;
    $scope.comments = (!_.isUndefined($scope.requestGroup.comments) && !_.isNull($scope.requestGroup.comments)) ? JSON.parse($scope.requestGroup.comments) : JSON.parse("[]");
    $scope.messageModel = "";
    $scope.commentsChanged = false;
    $scope.saveComment = function () {
        if(!_.isEmpty($scope.messageModel)){
            $scope.formData = "";
            $scope.tempComment = {};
            $scope.tempComment['name'] = nrmUser.fullName;
            $scope.tempComment['message'] = JSON.parse(JSON.stringify($scope.messageModel));
            $scope.tempComment['date'] = $scope.getCurrentDateNTime();
            //$scope.commentsTobeSaved = JSON.parse(JSON.stringify($scope.comments));
            //$scope.commentsTobeSaved.push($scope.tempComment);
            var commentsTobeSaved = (!_.isUndefined($scope.comments) && !_.isNull($scope.comments)) ? JSON.parse(JSON.stringify($scope.comments)) : JSON.parse("[]");
            commentsTobeSaved.push($scope.tempComment);
            RequestsService.saveRequestGroupComments($scope.requestGroup.requestEvaluatorId, JSON.stringify(commentsTobeSaved), nrmUser.EIN, $scope.formData, function (data, status) {
                if (status == 200){
                    $scope.comments.push($scope.tempComment);
                    $scope.requestGroup.comments = $scope.comments;
                    $scope.messageModel = "";
                    $scope.commentsChanged = true;
                }
            });
        }
    };
    $scope.close = function () {
        if($scope.commentsChanged){
            $modalInstance.close($scope.requestGroup);
        }else{
            $modalInstance.close();
        }
    };

    $scope.getCurrentDateNTime = function(){
        var monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
        var currentDate = new Date();
        return currentDate.getDate() + " " + monthNames[currentDate.getMonth()] + " " + currentDate.getFullYear() + " at " + currentDate.getHours() + ":" + currentDate.getMinutes();
    }

}]);