angular.module('app')
        .controller('DataBuildController', ['$scope', 'RequestsService', function ($scope, RequestsService) {

    $scope.dataBuildRequestsForGrid = [];

    $scope.dataBuildGridOptions = {
        rowData: $scope.dataBuildRequestsForGrid,
        rowSelection: 'multiple',
        enableColResize: true,
        enableSorting: true,
        enableFilter: true,
        rowHeight: 35,
        angularCompileRows : true,
        columnDefs:[
             {field:'requestId', headerName:'Request Id',width:120,template:'<div class="ui-grid-cell-contents"><a style="padding-left: 5px;" ui-sref="app.requestDetails({request:{{data.request}}})">{{data.requestId}}</a></div>'},
             {field:'requestName', headerName:'Request Name'  },
             {field:'templateName', headerName:'Template Name'  },
             {field:'productCategoryName',  headerName:'Product'},
             {field:'customerName',  headerName:'Customer'},
             {field:'createdUser',  headerName:'Requestor'},
             {field:'responseType',  headerName:'Response'},
             {field:'createdDate',  headerName:'Created Date' , cellStyle:function(params) {
                 if($scope.getAge(params.value)<=5) {
                     return {color: 'red'};
                 }else{
                     return {color: 'blue'};
                 }
             } },
             {field:'expectedResponseTime',  headerName:'Expected Response Date'},
             {field:'ifDataBuildCompleted',  headerName:'Data Build', suppressSorting: true,suppressMenu:true,cellRenderer: dataBuildCompletionCellRenderer}
        ]
    };

    RequestsService.getDataBuildRequests($scope.nrmUser.EIN, function (data, status) {

        if (!_.isUndefined(data)) {
            for(i=0;i<data.length ;i++) {
                $scope.dataBuildRequestsForGrid.push({
                                           requestId: data[i].requestId,
                                           requestName: data[i].requestName,
                                           templateName: data[i].templateName,
                                           productCategoryName:  data[i].productCategoryName,
                                           customerName:  data[i].quote.customerName,
                                           createdUser:  data[i].createdUser,
                                           responseType: data[i].responseType,
                                           createdDate:  $scope.getReadableDate(data[i].createdDate),
                                           expectedResponseTime:  data[i].expectedResponseTime,
                                           ifDataBuildRequired: data[i].dataBuildRequired,
                                           ifDataBuildCompleted: data[i].dataBuildCompleted,
                                           request: data[i]
                                       });
            };

            $scope.dataBuildGridOptions.rowData = $scope.dataBuildRequestsForGrid;
            $scope.dataBuildGridOptions.api.setRowData();
            $scope.columnStates = $scope.dataBuildGridOptions.columnApi.getState();
        }
    });



    $scope.getAge = function(dateString) {
        var oneDay = 24*60*60*1000;
        var today = new Date();
        var theDate = new Date(dateString);
        return Math.round(Math.abs((today.getTime() - theDate.getTime())/(oneDay)));
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

    $scope.setDataBuildCompleted = function(request){
        $scope.openGenericWarningModal("This will permanently mark Data Build as 'completed' for request id : '" + request.requestId + "'. Are you sure you want to proceed?", function (){
            RequestsService.updateDataBuildStatus(request.requestId,'Y',$scope.nrmUser.EIN, function (data, status) {
                if (status == '200') {
                    $scope.openGenericSuccessModal("Data Build Completion Saved Successfully!");
                    request.ifDataBuildCompleted = 'Y';
                    $scope.dataBuildGridOptions.rowData = $scope.dataBuildRequestsForGrid;
                    $scope.dataBuildGridOptions.api.setRowData();
                }else if (status == '404'){
                    $scope.openGenericErrorModal("Save/Update was unsuccessful. Please try again.");
                    request.ifDataBuildCompleted = 'N';
                    $scope.dataBuildGridOptions.api.setRowData();
                }else {
                    $scope.openGenericErrorModal("Bad Request! Please try again.");
                    request.ifDataBuildCompleted = 'N';
                    $scope.dataBuildGridOptions.api.setRowData();
                }
            });
        });
    }

    function dataBuildCompletionCellRenderer(params) {
        var dataBuildMarker = [];
        var dataBuildPendingTemplate =  '<div class="ui-grid-cell-contents" style="white-space: normal"><span style="cursor:pointer; "  ng-click="setDataBuildCompleted(data)" >'+
                                         '<span class="badge badge-pending" tooltips tooltip-content="Data Build Pending" tooltip-size="large" tooltip-side="right">'+
                                         '<i class="fa fa-times"></i></span></span></div>';

        var dataBuildCompletedTemplate = '<div class="ui-grid-cell-contents" style="white-space: normal"><span>'+
                                       '<span class="badge badge-success" tooltips tooltip-content="Data Build Successful" tooltip-class="tooltip-successful" tooltip-size="large" tooltip-side="right">'+
                                       '<i class="fa fa-check"></i></span></span></div>';

        if(params.data.ifDataBuildRequired ==='Y' && params.data.ifDataBuildCompleted ==='N'){
            dataBuildMarker.push(dataBuildPendingTemplate);
        } else{
            dataBuildMarker.push(dataBuildCompletedTemplate);
        }
        return dataBuildMarker.join(' ');
    }

    $scope.hideSelectedColumn = function(columnState, columnStateList){
         $scope.dataBuildGridOptions.columnApi.setState(columnStateList);
    };

    $scope.columnBreak = 4;//max number of cols

    $scope.getColumnName = function(columnId){
        var columnMapForDataBuildGrid = new Map();
        columnMapForDataBuildGrid.set("requestId","Request Id");
        columnMapForDataBuildGrid.set("requestName","Request Name");
        columnMapForDataBuildGrid.set("templateName","Template Name");
        columnMapForDataBuildGrid.set("productCategoryName","Product Category Name");
        columnMapForDataBuildGrid.set("customerName","Customer Name");
        columnMapForDataBuildGrid.set("createdUser","Requestor");
        columnMapForDataBuildGrid.set("responseType","Response");
        columnMapForDataBuildGrid.set("createdDate","Created Date");
        columnMapForDataBuildGrid.set("expectedResponseTime","Expected Response Date");
        columnMapForDataBuildGrid.set("ifDataBuildRequired","Data Build");
        columnMapForDataBuildGrid.set("ifDataBuildCompleted","Data Build");

        return  columnMapForDataBuildGrid.get(columnId);
    }

    $scope.showPendingDataBuildRequests = function() {
        var filterApi = $scope.dataBuildGridOptions.api.getFilterApi('ifDataBuildCompleted');
        filterApi.selectNothing();
        filterApi.selectValue('N');
        filterApi.selectValue('n');
        $scope.dataBuildGridOptions.api.onFilterChanged();
    };

    $scope.showSuccesfulDataBuildRequests = function() {
        var filterApi = $scope.dataBuildGridOptions.api.getFilterApi('ifDataBuildCompleted');
        filterApi.selectNothing();
        filterApi.selectValue('Y');
        filterApi.selectValue('y');
        $scope.dataBuildGridOptions.api.onFilterChanged();
    };
}]);