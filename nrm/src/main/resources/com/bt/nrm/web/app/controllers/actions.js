angular.module('app')
        .controller('ActionsController', ['$scope' , '$rootScope', 'EvaluatorService', '$stateParams', function ($scope, $rootScope, EvaluatorService, $stateParams) {

    $scope.personalActionsGrid = [];
    $scope.allActionsGrid = [];

    EvaluatorService.getListOfEvaluatorActionsUri($scope.nrmUser.EIN,$scope.nrmUser.groups,function (data, status) {
        if (status == '200') {
            if(!_.isUndefined(data)){
                $scope.personalActions = data.PersonalActions;
                $scope.allActions = data.AllActions;

                // Personal Action
                if(!_.isUndefined($scope.personalActions)){
                    for(i=0;i<$scope.personalActions.length ;i++) {
                        $scope.personalActionsGrid.push({requestId: $scope.personalActions[i].requestId,
                                                            requestName: $scope.personalActions[i].requestName,
                                                            quoteId: $scope.personalActions[i].quoteId,
                                                            quoteName: $scope.personalActions[i].quoteName,
                                                            productCategoryName: $scope.personalActions[i].productCategoryName,
                                                            templateName: $scope.personalActions[i].templateName,
                                                            customerName: $scope.personalActions[i].customerName,
                                                            requestState: $scope.personalActions[i].requestState,
                                                            requestEvaluatorId: $scope.personalActions[i].requestEvaluatorId
                                                        });
                    };

                    $scope.gridOptionPersonalActions.rowData = $scope.personalActionsGrid;
                    $scope.gridOptionPersonalActions.api.setRowData();
                }

                // All Actions
                if(!_.isUndefined($scope.allActions)){
                    for(j=0;j<$scope.allActions.length ;j++) {
                        $scope.allActionsGrid.push({requestId: $scope.allActions[j].requestId,
                                                        requestName: $scope.allActions[j].requestName,
                                                        quoteId: $scope.allActions[j].quoteId,
                                                        quoteName: $scope.allActions[j].quoteName,
                                                        productCategoryName: $scope.allActions[j].productCategoryName,
                                                        templateName: $scope.allActions[j].templateName,
                                                        customerName: $scope.allActions[j].customerName,
                                                        requestState: $scope.allActions[j].requestState,
                                                        salesChannel: $scope.allActions[j].salesChannelName,
                                                        createdDate: $scope.allActions[j].createdDate,
                                                        acceptedDate: $scope.allActions[j].acceptedDate,
                                                        acceptedBy: $scope.allActions[j].acceptedBy,
                                                        groupName: $scope.allActions[j].groupName,
                                                        requestEvaluatorId: $scope.allActions[j].requestEvaluatorId
                                                        });
                    };

                    $scope.gridOptionAllActions.rowData = $scope.allActionsGrid;
                    $scope.gridOptionAllActions.api.setRowData();
                }

            }
        }
    });

    var columnDefs = [
        {headerName: 'Request ID', field: 'requestId'},
        {headerName: "Request Name", field: "requestName", template:'<div class="ui-grid-cell-contents"><a style="padding-left: 5px;" ui-sref="app.actionDetails({requestEvaluatorId:data.requestEvaluatorId,requestId:data.requestId})">{{data.requestName}}</a></div>'},
        {headerName: "Quote/QuoteOption ID", field: "quoteId"},
        {headerName: "Quote/QuoteOption Name", field: "quoteName"},
        {headerName: "Product", field: "productCategoryName"},
        {headerName: "Feature", field: "templateName"},
        {headerName: "Customer Name", field: "customerName"},
        {headerName: "State", field: "requestState"},
        {headerName: "Id", field: "requestEvaluatorId"}
    ];

    $scope.gridOptionPersonalActions = {
        columnDefs: columnDefs,
        rowData: $scope.personalActionsGrid,
        rowHeight: 35,
        enableFilter: true,
        angularCompileRows : true
    };


    var columnDefAllActions = [
        {headerName: 'Request ID', field: 'requestId'},
        {headerName: "Request Name", field: "requestName", template:'<div class="ui-grid-cell-contents"><a style="padding-left: 5px;" ui-sref="app.actionDetails({requestEvaluatorId:data.requestEvaluatorId,requestId:data.requestId})">{{data.requestName}}</a></div>'},
        {headerName: "Quote/QuoteOption ID", field: "quoteId"},
        {headerName: "Quote/QuoteOption Name", field: "quoteName"},
        {headerName: "Product", field: "productCategoryName"},
        {headerName: "Feature", field: "templateName"},
        {headerName: "Customer Name", field: "customerName"},
        {headerName: "State", field: "requestState"},
        {headerName: "Sales Channel", field: "salesChannel"},
        {headerName: "Created Date", field: "createdDate"} ,
        {headerName: "Accepted Date", field: "acceptedDate"},
        {headerName: "Accepted By", field: "acceptedBy"},
        {headerName: "Group", field: "groupName"},
        {headerName: "Id", field: "requestEvaluatorId"}
    ];

    $scope.gridOptionAllActions = {
        columnDefs: columnDefAllActions,
        rowData: $scope.allActionsGrid,
        rowHeight: 35,
        enableFilter: true,
        angularCompileRows : true,
        groupUseEntireRow: true,
        groupKeys: ['groupName','productCategoryName'],
        groupSuppressAutoColumn: true
    };

}]);