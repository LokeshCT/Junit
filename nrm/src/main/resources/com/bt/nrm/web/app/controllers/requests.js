angular.module('app')
        .controller('RequestsController', ['$scope' , '$stateParams', 'RequestsService', 'ProductTemplateService', 'RequestStateConstantsService', 'RequestEvaluatorStateConstantsService', 'RequestEvaluatorResponseConstantsService', 'RequestResponseTypeService',  function ($scope, $stateParams , RequestsService, ProductTemplateService, RequestStateConstantsService, RequestEvaluatorStateConstantsService, RequestEvaluatorResponseConstantsService, RequestResponseTypeService) {

    $scope.requestStateConstantsService = RequestStateConstantsService;
    $scope.requestEvaluatorStateConstantsService = RequestEvaluatorStateConstantsService;
    $scope.requestEvaluatorResponseConstantsService = RequestEvaluatorResponseConstantsService;
    $scope.requestResponseTypeService  = RequestResponseTypeService;

    $scope.statusFilter = {
        "issued":{
            "name": $scope.requestStateConstantsService.issued,
            "value": false
        },
        "signedOut":{
            "name": $scope.requestStateConstantsService.signedOut,
            "value": false
        },
        "allAgentsFinishedWork":{
            "name": $scope.requestStateConstantsService.allAgentsHaveFinishedWork,
            "value": false
        },
        "noAgents":{
            "name": $scope.requestStateConstantsService.noAgents,
            "value": false
        },
        "won":{
            "name": $scope.requestStateConstantsService.won,
            "value": false
        },
        "all":{
            "name": $scope.requestStateConstantsService.all,
            "value": false
        }
    };
    $scope.statusFilterBackup = JSON.parse(JSON.stringify($scope.statusFilter));
    $scope.date = { startDate: '2015-02-23', endDate: '2015-03-01' };

    $scope.getRequests = function(){
        var states = [];
        _.each( $scope.statusFilter, function( val, key ) {
            if ( val.value && !_.isUndefined(val.name)) {
                states.push(val.name);
            }
        });
        RequestsService.getRequestsByUserIdAndStates($scope.nrmUser.EIN, states, function (data, status) {
            if (!_.isUndefined(data)) {
                $scope.requests = data;
                $scope.requestGridOptions.rowData = $scope.requests;
                $scope.requestGridOptions.api.setRowData();
            }
        });
    };

    $scope.filterRequestGrid = function(){
        $scope.getRequests();
    };

    $scope.resetRequestFilters = function(){
        $scope.statusFilter = JSON.parse(JSON.stringify($scope.statusFilterBackup));
    };

    $scope.allRequestFilterSelectionChanged = function(){
        if($scope.statusFilter["all"].value){
            $scope.statusFilter = JSON.parse(JSON.stringify($scope.statusFilterBackup));
            $scope.statusFilter["all"].value = true;
        }
        $scope.getRequests();
    };

    $scope.requestFilterSelectionChanged = function(){
        if($scope.statusFilter["all"].value){
            $scope.statusFilter["all"].value = false;
        }
        $scope.getRequests();
    };

    /* $scope.requestGridOptions = {
        data: 'requests',
        enableFiltering: true,
        enableColumnResizing: true,
        *//*enableGridMenu: true,*//*
        *//*gridMenuTitleFilter: fakeI18n,*//*
        *//*enableHorizontalScrollbar: 2,*//*
        rowHeight: 50,
        columnDefs:[
            {field:'requestId', displayName:'Request Id', *//*enableHiding: false,*//* cellTemplate: '<div class="ui-grid-cell-contents"></div><a style="padding-left: 5px;" ui-sref="app.requestDetails({request:{{row.entity}}})">{{row.entity.requestId}}</a></div>',  width: '10%'},
            {field:'createdDate', displayName:'Age',  cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{grid.appScope.getAge(row.entity.createdDate)}} Days</div>', width: '7%'},
            {field:'requestEvaluators', displayName:'Group Actions', cellTemplate: '/nrm/static/views/requestEvaluators.html',  width: '25%'},
            {field:'state', displayName:'State', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.state}}</div>', width: '10%'},
            {field:'state', displayName:'Change State', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.state}}</div>',  width: '15%'},
            {field:'quote.salesChannelName', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.quote.salesChannelName}}</div>', displayName:'Sales Channel',  width: '20%'},
            {field:'quote.customerName', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.quote.customerName}}</div>', displayName:'Customer',  width: '25%'},
            {field:'productCategoryName', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.productCategoryName}}</div>', displayName:'Product',  width: '25%'},
            {field:'templateName', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.templateName}}</div>', displayName:'Feature',  width: '25%'},
            {field:'createdUser', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.createdUser}}</div>', displayName:'Requester',  width: '20%'},
            {field:'comments', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.comments}}</div>', displayName:'Comments',  width: '15%'}
        ]
    };*/

    $scope.requestGridOptions = {
        rowData: $scope.requests,
        rowSelection: 'multiple',
        enableColResize: true,
        enableSorting: true,
        enableFilter: true,
        rowHeight: 35,
        angularCompileRows : true,
        columnDefs:[
             {field:'requestId', headerName:'Request Id', width:120, template:'<div class="ui-grid-cell-contents"></div><a style="padding-left: 5px;" ui-sref="app.requestDetails({request:{{row.entity}}})">{{row.entity.requestId}}</a></div>'},
             {field:'createdDate', headerName:'Age', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{getAge(row.entity.createdDate)}} Days</div>'},
             {field:'requestEvaluators', headerName:'Group Actions', cellTemplate: '/nrm/static/views/requestEvaluators.html',  width: '25%'},
             {field:'state', headerName:'State', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.state}}</div>', width: '10%'},
             {field:'quote.salesChannelName', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.quote.salesChannelName}}</div>', headerName:'Sales Channel',  width: '20%'},
             {field:'quote.customerName', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.quote.customerName}}</div>', headerName:'Customer',  width: '25%'},
             {field:'productCategoryName', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.productCategoryName}}</div>', headerName:'Product',  width: '25%'},
             {field:'templateName', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.templateName}}</div>', headerName:'Feature',  width: '25%'},
             {field:'createdUser', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.createdUser}}</div>', headerName:'Requester',  width: '20%'},
             {field:'comments', cellTemplate: '<div class="ui-grid-cell-contents" style="padding-left: 5px;">{{row.entity.comments}}</div>', headerName:'Comments',  width: '15%'}
        ]
    };

    $scope.getRequests();

}]);
