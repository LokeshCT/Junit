angular.module('app')
        .controller('ActionsDetailsController', ['$scope' , '$rootScope', 'EvaluatorService', 'RequestsService', 'RequestEvaluatorResponseConstantsService', '$stateParams', '$modal', function ($scope, $rootScope, EvaluatorService, RequestsService, RequestEvaluatorResponseConstantsService, $stateParams, $modal) {

    $scope.requestEvaluatorId = $stateParams.requestEvaluatorId;
    $scope.comments = "[]";
    $scope.messageModel = "";
    $scope.commentsChanged = false;
    $scope.otherEvaluators = [];
    $scope.sitePriceGroupGrid = [];
    $scope.requestId = $stateParams.requestId;
    $scope.requestEvaluatorResponseConstantsService = RequestEvaluatorResponseConstantsService;
    $scope.evaluatorResponseModel = {};

    $scope.priceGroupMap = {};

    $scope.decisionArr = ["Go","NoGo"];

     RequestsService.getRequestByRequestId($stateParams.requestId,function (data, status) {
        if (status == '200') {
            if(!_.isUndefined(data)){
                $scope.request = data;
                for(j=0;j<$scope.request.requestEvaluators.length;j++) {
                    if($scope.request.requestEvaluators[j].requestEvaluatorId==$scope.requestEvaluatorId){
                        $scope.evaluator = $scope.request.requestEvaluators[j];
                        $scope.formatDates();

                    }else{
                        $scope.otherEvaluators.push($scope.request.requestEvaluators[j]);
                    }
                }
                if(!$scope.evaluator){
                    $scope.openGenericPageLoadErrorModal("Bad Request! Could not load Evaluator Details.", 'app.actions');
                }
                $scope.comments = !_.isUndefined($scope.evaluator.comments) ? JSON.parse($scope.evaluator.comments) : JSON.parse("[]");

                // Price Groups
                if(!_.isUndefined($scope.evaluator.requestEvaluatorSites)){
                    for(i=0;i<$scope.evaluator.requestEvaluatorSites.length ;i++){
                        for(j=0;j<$scope.evaluator.requestEvaluatorSites[i].requestEvaluatorPriceGroups.length ;j++) {
                            $scope.sitePriceGroupGrid.push({siteId:$scope.evaluator.requestEvaluatorSites[i].siteId,
                                                               siteName: $scope.evaluator.requestEvaluatorSites[i].siteName,
                                                               priceGroupType: $scope.evaluator.requestEvaluatorSites[i].requestEvaluatorPriceGroups[j].priceGroupType,
                                                               priceGroupDescription: $scope.evaluator.requestEvaluatorSites[i].requestEvaluatorPriceGroups[j].priceGroupDescription,
                                                               oneOffRecommendedRetail: $scope.evaluator.requestEvaluatorSites[i].requestEvaluatorPriceGroups[j].oneOffRecommendedRetail,
                                                               recurringRecommendedRetail: $scope.evaluator.requestEvaluatorSites[i].requestEvaluatorPriceGroups[j].recurringRecommendedRetail,
                                                               nrcPriceToPartner: $scope.evaluator.requestEvaluatorSites[i].requestEvaluatorPriceGroups[j].nrcPriceToPartner,
                                                               rcPriceToPartner: $scope.evaluator.requestEvaluatorSites[i].requestEvaluatorPriceGroups[j].rcPriceToPartner,
                                                               oneOffCost: $scope.evaluator.requestEvaluatorSites[i].requestEvaluatorPriceGroups[j].oneOffCost,
                                                               recurringCost: $scope.evaluator.requestEvaluatorSites[i].requestEvaluatorPriceGroups[j].recurringCost,
                                                               requestEvaluatorPriceGroupId: $scope.evaluator.requestEvaluatorSites[i].requestEvaluatorPriceGroups[j].requestEvaluatorPriceGroupId}
                            );
                        }
                    }



                    $scope.gridOptionsSitePriceGroupList.rowData = $scope.sitePriceGroupGrid;
                    $scope.gridOptionsSitePriceGroupList.api.setRowData();

                };
            }
        }else{
            $scope.openGenericPageLoadErrorModal("Bad Request! Could not load Evaluator Details.", 'app.actions');
        }
    });


    var columnDefs = [
        {headerName: "Site Id", field: "siteId"},
       /* {headerName: "Site Name", field: "siteName", headerGroup: "Site Details"},
        {headerName: "Type", field: "priceGroupType", headerGroup: "Price Group Details"},*/
        {headerName: "Description", field: "priceGroupDescription"},
        {headerName: "One Time", field: "oneOffRecommendedRetail", headerGroup: "Recommended Retail Price", editable: true, newValueHandler: valueChangedHandler},
        {headerName: "Recurring", field: "recurringRecommendedRetail" , headerGroup: "Recommended Retail Price", editable: true, newValueHandler: valueChangedHandler},
        {headerName: "One Time", field: "nrcPriceToPartner", headerGroup: "Price To Partner", editable: true, newValueHandler: valueChangedHandler},
        {headerName: "Recurring", field: "rcPriceToPartner", headerGroup: "Price To Partner", editable: true, newValueHandler: valueChangedHandler},
        {headerName: "One Time", field: "oneOffCost", headerGroup: "Cost", editable: true, newValueHandler: valueChangedHandler},
        {headerName: "Recurring", field: "recurringCost", headerGroup: "Cost", editable: true, newValueHandler: valueChangedHandler}
        //,{headerName: "Update", field: "", cellRenderer: recurringCostCellRendererFunc}
    ];

    $scope.gridOptionsSitePriceGroupList = {
        columnDefs: columnDefs,
        angularCompileRows: true,
        rowData: $scope.sitePriceGroupGrid,
        rowHeight: 35,
        // enableSorting: true,
        enableFilter: true,
        groupHeaders: true,
        enableSorting: true,
        angularCompileRows : true,
        groupUseEntireRow: true,
        groupKeys: ['siteName','priceGroupType']
    };

    function recurringCostCellRendererFunc(params) {
        console.log("params in recurringCostCellRendererFunc : "+params);
        return '<span>' +
                   '<a class="btn btn-sm btn-primary" ng-click="saveGridData1()">' +
                        '<span class="glyphicon glyphicon-save"></span>' +
                   '</a>' +
               '</span>';
    }

    $scope.startWorkingOnAction  = function(){
        if(!_.isUndefined($scope.evaluator)){

            $scope.evaluator.acceptedBy = $scope.nrmUser.EIN;
            $scope.evaluator.acceptedByName = $scope.nrmUser.firstName+ " " +$scope.nrmUser.lastName;
            $scope.evaluator.modifiedBy = $scope.nrmUser.EIN;
            $scope.evaluator.modifiedUserName = $scope.nrmUser.firstName+ " " +$scope.nrmUser.lastName;

            EvaluatorService.startWorkingOnAction($scope.evaluator, function (data, status) {
                if (status == 200){
                    console.log("Agent accepted the action !");
                    $scope.evaluator = data;
                    $scope.formatDates();
                    $scope.openGenericSuccessModal("Agent action accepted Successfully!");
                    $scope.evaluator.state = "issued";
                } else{
                    $scope.openGenericPageLoadErrorModal("Please try again.", 'app.actionDetails');
                }
            });
        }
    };

    $scope.saveRequestEvaluatorComments  = function(){
        if(!_.isUndefined(this.messageModel) && !_.isEmpty(this.messageModel)){
            var tempComment = {};
            tempComment['name'] = $scope.nrmUser.fullName;
            tempComment['message'] = JSON.parse(JSON.stringify(this.messageModel));
            tempComment['date'] = $scope.getCurrentDateNTime();
            var commentsTobeSaved = (!_.isUndefined($scope.comments) && !_.isNull($scope.comments)) ? JSON.parse(JSON.stringify($scope.comments)) : JSON.parse("[]");//JSON.parse(JSON.stringify(comments));
            commentsTobeSaved.push(tempComment);
            var controller = this;
            RequestsService.saveRequestGroupComments($scope.evaluator.requestEvaluatorId, JSON.stringify(commentsTobeSaved), $scope.nrmUser.EIN, "", function (data, status) {
                if (status == 200){
                    $scope.comments = commentsTobeSaved;
                    controller.messageModel = "";
                } else{

                }
            });
        }
    };

    $scope.saveGridData = function() {
        var newData = $scope.gridOptionsSitePriceGroupList.rowData;
        console.log("Grid Data 1 : "+newData);
        console.log("Changed Price Group Data : "+$scope.priceGroupMap);

        if(!_.isUndefined($scope.priceGroupMap)){
            EvaluatorService.updateEvaluatorPriceGroupUri($scope.requestId, $scope.priceGroupMap, $scope.nrmUser.EIN, function (data, status) {
                if (status == 200){
                    console.log("Price Group successfully updated !");
                    $scope.openGenericSuccessModal("Price Group successfully updated!");
                } else{
                    $scope.openGenericPageLoadErrorModal("Please try again.", 'app.actionDetails');
                }
            });
        }
    };

    function valueChangedHandler(params) {
        params.data[params.colDef.field] = params.newValue.toUpperCase();
        $scope.priceGroupMap[params.data.requestEvaluatorPriceGroupId] = params.data;
        console.log("Changed Price Group Map : "+$scope.priceGroupMap);
    }

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

    $scope.formatDates = function(){

        if(!_.isUndefined($scope.evaluator)){
            if(!_.isUndefined($scope.evaluator.createdDate)){
                $scope.evaluator.createdDate = $scope.getReadableDate($scope.evaluator.createdDate);
            }
            if(!_.isUndefined($scope.evaluator.acceptedDate)){
                $scope.evaluator.acceptedDate = $scope.getReadableDate($scope.evaluator.acceptedDate);
            }
            if(!_.isUndefined($scope.evaluator.closedDate)){
                $scope.evaluator.closedDate = $scope.getReadableDate($scope.evaluator.closedDate);
            }
            if(!_.isUndefined($scope.evaluator.modifiedDate)){
                $scope.evaluator.modifiedDate = $scope.getReadableDate($scope.evaluator.modifiedDate);
            }
        }
    }



    $scope.updateFinalDecision = function(){
      console.log("$scope.data.decisionArrVal---"+$scope.selectedDecision);
      console.log("$scope.goNoGoDecisionModel---"+$scope.goNoGoDecisionModel);
    }



    /*EvaluatorService.getEvaluatorActionDetailsUri($stateParams.requestEvaluatorId,$stateParams.requestId,function (data, status) {
        if (status == '200') {
            if(!_.isUndefined(data)){

            }
        }
    });*/

}]);
