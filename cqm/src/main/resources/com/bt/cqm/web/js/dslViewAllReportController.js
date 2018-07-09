var module = angular.module('cqm.controllers');

module.controller('DslViewAllReportController', ['$scope' , 'customerService', '$http', 'UIService', 'UrlConfiguration', 'PageContext', 'SessionContext', 'CreateCustomerResponseCode','SACService', function ($scope, customerService, $http, UIService, UrlConfiguration, PageContext, SessionContext, CreateCustomerResponseCode, SACService) {

    console.log('Inside DslViewAllReportController');

    $scope.onPageLoad = function(){
        $scope.getAllUserUploadedFileList();
    }
    $scope.reportData = [];

    $scope.reportGrid =  {data:'reportData',enableFiltering: true,rowHeight:40,columnDefs:[
        {field : 'fileDesc',displayName:'File Description'},
        {field : 'timeStamp',displayName:'Time Stamp (GMT)'},
        {field : 'userName',displayName:'User Name'},
        {field : 'availabilityStatus',displayName:'Availability Status'},
        {field:'action', displayName:'Actions', headerCellTemplate:'<div>Actions<div>',width:240,
            cellTemplate:'<div class="ngSelectionCell">' +
                         '<div ng-show="{{row.entity.sharePointOrgDocId.length>1}}" style="display: inline-block;">'+
                         '<a target="_self" class="dsl-btn" href="/cqm/dslchecker/downloadFile?docId={{row.entity.sharePointOrgDocId}}&fileName={{row.entity.fileName}}&salesChannel={{row.entity.salesChannel}}&docType=Import" ><i class="fa fa-download fa-fw"></i>File</a>' +
                         '</div>'+
                         '<div ng-if="row.entity.sharePointResultDocId != null" style="display: inline-block;">'+
                            '<a target="_self" class="dsl-btn" href="/cqm/dslchecker/downloadFile?docId={{row.entity.sharePointResultDocId}}&fileName={{row.entity.fileName}}&salesChannel={{row.entity.salesChannel}}&docType=Result"><i class="fa fa-download fa-fw"></i>Result</a>' +
                         '</div>'+
                         '</div>' }
    ]};

    $scope.onRefresh = function(){
        $scope.getAllUserUploadedFileList();
    }

    $scope.getAllUserUploadedFileList = function () {
        console.log("Inside getAllUserUploadedFileList  !!");
        UIService.block();
        SACService.getAllUserUploadedFileList(function (data, status) {
            if (status == 200) {
                $scope.reportData = data;
                console.log("Reports retrieved successfully.");
            } else if (status == 404) {
                $scope.reportData = [];
                console.log("No Reports to retrieve.");
            }else {
                console.log("Failed to retrieve User reports!!");
                UIService.handleException('Get User Reports', data, status);
            }
            UIService.unblock();
        });

    };


}]);
