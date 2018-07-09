angular.module('app').
        controller('QuoteOptionDetailsController',['$scope' ,'$stateParams', 'RequestsService','RequestEvaluatorResponseConstantsService',function($scope, $stateParams, RequestsService,RequestEvaluatorResponseConstantsService){

    $scope.requestEvaluatorResponseConstantsService = RequestEvaluatorResponseConstantsService;
    $scope.relatedRequestsForGrid = [];

    $scope.quote = !_.isUndefined($stateParams.quote) ? $stateParams.quote : null;

    RequestsService.getAllRequestsByQuoteId($scope.quote.quoteMasterId, function (data, status) {
        if (!_.isUndefined(data)) {
            for(i=0;i<data.length ;i++) {
                $scope.relatedRequestsForGrid.push({
                                              requestId: data[i].requestId,
                                              templateName: data[i].templateName,
                                              productCategoryName: data[i].productCategoryName,
                                              requestName:  data[i].requestName,
                                              requestGroups:  data[i].salesChannelName,
                                              state:  data[i].state,
                                              state:  data[i].state,
                                              createdDate:  $scope.getReadableDate(data[i].createdDate),
                                              createdUser:  data[i].createdUser,
                                              comments: data[i].comments,
                                              request: data[i]
                                          });
            };

            $scope.relatedRequestsGridOptions.rowData = $scope.relatedRequestsForGrid;
            $scope.relatedRequestsGridOptions.api.setRowData();
            $scope.columnStates = $scope.relatedRequestsGridOptions.columnApi.getState();
        }
    });

    $scope.relatedRequestsGridOptions = {
        rowData: $scope.relatedRequestsForGrid,
        rowSelection: 'multiple',
        enableColResize: true,
        enableSorting: true,
        enableFilter: true,
        rowHeight: 35,
        angularCompileRows : true,
        columnDefs:[
            {field:'requestId', headerName:'Request Id',template:'<div class="ui-grid-cell-contents"><a style="padding-left: 5px;" ui-sref="app.requestDetails({request:{{data.request}}})">{{data.requestId}}</a></div>'},
            {field:'templateName', headerName:'Feature'},
            {field:'productCategoryName', headerName:'Product'},
            {field:'requestName', headerName:'Request Name'},
            {field:'requestGroups', headerName:'Group Actions',cellRenderer:groupActionsCellRenderer},
            {field:'state', headerName:'State'},
            {field:'state', headerName:'Change State'},
            {field:'createdUser' , headerName:'Requester'},
            {field:'comments', headerName:'comments'}
        ]
    };

    $scope.m_names = new Array("January", "February", "March",
                               "April", "May", "June", "July", "August", "September",
                               "October", "November", "December");


    var createdDate = new Date($scope.quote.createdDate);
    var createdDay = createdDate.getDate();
    var createdMonth = $scope.m_names[createdDate.getMonth()];
    var createdYear = createdDate.getFullYear();

    $scope.readableDate = createdDay+"-"+createdMonth+"-"+createdYear;

    $scope.getReadableDate = function(dateString){
        var createdDate = new Date(dateString);
        var createdDay = createdDate.getDate();
        var createdMonth = $scope.m_names[createdDate.getMonth()];
        var createdYear = createdDate.getFullYear();

        return createdDay+"-"+createdMonth+"-"+createdYear;
    }

    function groupActionsCellRenderer(params) {
        var groupActionsMarker = [];
        var groupNotAvailableTemplate = '<div class="ui-grid-cell-contents " style="white-space: normal">'+
                                        '<span>No Evaluators</span></div>';

        var groupActionTemplate = '<div class="ui-grid-cell-contents " style="white-space: normal"><span>';

        if(params.data.request.requestGroups ===null ||params.data.request.requestGroups ===undefined){
            groupActionsMarker.push(groupNotAvailableTemplate);
        }else{
            for(j=0;j<params.data.request.requestGroups.length;j++){
                var evaluatorGroupNameForCurrentIteration =  params.data.request.requestGroups[j].evaluatorGroupName;
                var closedByForCurrentIteration = params.data.request.requestGroups[j].closedBy;
                $scope.popOverForCurrentIteration = 'Evaluator : '+closedByForCurrentIteration + ', Group : '+ evaluatorGroupNameForCurrentIteration;

                 if(params.data.request.requestGroups[j].response ===$scope.requestEvaluatorResponseConstantsService.requestEvaluatorResponse_go) {
                     groupActionTemplate +='<a class="badge badge-success" popover-append-to-body data-titleclass="bordered-purple" data-class="dark" popover-placement="bottom"  popover-title="Group Details"'+
                                           ' popover="{{popOverForCurrentIteration}}">'+evaluatorGroupNameForCurrentIteration +'</a>';
                 }
                 if(params.data.request.requestGroups[j].response ===$scope.requestEvaluatorResponseConstantsService.requestEvaluatorResponse_noGo){
                     groupActionTemplate +='<a class="badge badge-danger" popover-append-to-body data-titleclass="bordered-purple" data-class="dark" popover-placement="bottom"  popover-title="Group Details"'+
                                           ' popover="{{popOverForCurrentIteration}}">'+ evaluatorGroupNameForCurrentIteration +'</a>';
                 }
                 if(params.data.request.requestGroups[j].response ===$scope.requestEvaluatorResponseConstantsService.requestEvaluatorResponse_none){
                    groupActionTemplate +='<a class="badge badge-default" popover-append-to-body data-titleclass="bordered-purple" data-class="dark" popover-placement="bottom"  popover-title="Group Details"'+
                                          ' popover="{{popOverForCurrentIteration}}">'+ evaluatorGroupNameForCurrentIteration +'</a>';
                 }
            }
            groupActionTemplate+= '</span></div>';
            groupActionsMarker.push(groupActionTemplate) ;
        }
        return groupActionsMarker.join(' ');
    }
}])
