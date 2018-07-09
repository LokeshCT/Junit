var module = angular.module('cqm.controllers');

module.controller('DslUploadXlsController', ['$scope' , 'customerService', '$http', 'UIService', 'UrlConfiguration', 'PageContext', 'SessionContext', 'CreateCustomerResponseCode', 'SACService', '$rootScope', '$upload', function ($scope, customerService, $http, UIService, UrlConfiguration, PageContext, SessionContext, CreateCustomerResponseCode, SACService, $rootScope, $upload) {

    console.log('Inside DslUploadXlsController');
    $scope.description = '';
    $scope.hasAttachment = false;
    $scope.hasDescription = false;
    $scope.fileSize = '';
    $scope.reportData = [];
    $scope.anyReportGenInProcessing = false;
    $scope.reportGenInProcessingMsg = 'A file availability check is currently in progress, please try again later once outstanding file availability check is completed';
    $scope.disableBrowse = false;

    $scope.onPageLoad = function(){
        $scope.getUserUploadedFilesDetails();
    }

    $scope.myUploadGrid = {data:'reportData', enableFiltering:true, resizable:true, rowHeight:40, columnDefs:[
        {field:'fileDesc', displayName:'File Description'},
        {field:'timeStamp', displayName:'Time Stamp (GMT)'},
        {field:'userName', displayName:'User Name'},
        {field:'validationStatus', displayName:'Validation Status'},
        {field:'action', displayName:'Actions', headerCellTemplate:'<div>Actions<div>', width:280,
            cellTemplate:'<div class="ngSelectionCell">' +
                         '<div ng-show="{{row.entity.sharePointOrgDocId !=undefined && row.entity.sharePointOrgDocId.length>1}}" style="display: inline-block;">' +
                         '<a target="_self" class="dsl-btn" href="/cqm/dslchecker/downloadFile?docId={{row.entity.sharePointOrgDocId}}&fileName={{row.entity.fileName}}&salesChannel={{row.entity.salesChannel}}&docType=Import" ><i class="fa fa-download fa-fw"></i>File</a>' +
                         '</div>' +
                         '<div ng-show="{{row.entity.sharePointFailDocId !=undefined && row.entity.sharePointFailDocId.length>1}}" style="display: inline-block;">' +
                         '<a target="_self" href="/cqm/dslchecker/downloadFile?docId={{row.entity.sharePointFailDocId}}&fileName={{row.entity.fileName}}&salesChannel={{row.entity.salesChannel}}&docType=Failure"  class="dsl-btn "><i class="fa fa-download fa-fw"></i>Report</a>' +
                         '</div>' +
                         '<button ng-click="grid.appScope.deleteFile(row.entity)" class="dsl-btn dsl-btn-red "><i class="fa fa-trash fa-fw"></i>Delete</button>' +
                         '</div>' }

    ]};

    $scope.onBrowse = function ($files) {

        $scope.$file = $files[0];

        if (_.isUndefined($scope.$file)) {
            $scope.hasAttachment = false;
        } else {
            $scope.hasAttachment = true;
            $scope.fileSize = ($scope.$file.size / 1024).toFixed(2) + ' KB';
        }
    }

    $scope.getUserUploadedFilesDetails = function () {
        console.log("Inside getUserUploadedFilesDetails  !!");
        UIService.block();
        SACService.getUserUploadedFileList(function (data, status) {
            if (status == 200) {
                if(!_.isUndefined(data)){
                    $scope.reportData = data.sacBulkInputDTOs;
                    var isValidationProgress =_.find($scope.reportData,function(bulkUploadDto){
                        if("InProgress" == bulkUploadDto.validationStatus){
                          return true;
                        }
                    })

                    $scope.anyReportGenInProcessing = data.anyReportInProcessing;
                    if($scope.anyReportGenInProcessing || !_.isUndefined(isValidationProgress)){
                        $scope.brwsFileToolTipMsg =$scope.reportGenInProcessingMsg;
                        $scope.disableBrowse = true;
                    }else{
                        $scope.brwsFileToolTipMsg ='';
                        $scope.disableBrowse = false;
                    }
                }
            }else if(status ==404){
                $scope.reportData =[];
                $scope.anyReportGenInProcessing = false;
            } else {
                UIService.handleException('User File uploads', data, status);
            }

            if (!$rootScope.$$phase) {
                $rootScope.$apply();
            }

            UIService.unblock();
        });

    }

    $scope.onRefresh = function(){
        $scope.getUserUploadedFilesDetails();
    }

    $scope.uploadAttachment = function () {
        console.log("Inside Upload File !!");
        if($scope.hasAttachment == false || $scope.hasDescription == false){
            return;
        }
        if (!_.isUndefined($scope.$file)) {
            $scope.progress = -1;
            $scope.uploadResult = [];
            $scope.dataUrl = {};
            $scope.uploadStatusMsg = "";
            $scope.uploadStatusMsgStyle = "";
            UIService.block();

            SACService.uploadFile($scope.$file, $scope.description, $upload, function (data) {
                var title = 'File upload status.';

                if (data != undefined && data.status == 200) {
                    if(!_.isUndefined(data.data.description)){
                        var message = "File successfully uploaded. Generated File Name - "+data.data.description;
                        $scope.description = "";
                        UIService.openDialogBox(title, message, true, false);
                    }
                    $scope.getUserUploadedFilesDetails();

                }else if(data.status ==400){
                    UIService.openDialogBox('Invalid Data', data.data.description, true, false);
                } else {
                    $scope.getUserUploadedFilesDetails();
                    UIService.handleException('Failed to upload', data.data, data.status);

                }
                $scope.clearFile();
                UIService.unblock();
            })
        }
    };


    $scope.deleteFile = function (bulkInputDTO) {
        console.log("Inside deleteFile.");
        UIService.openDialogBox('Confirm deletion','Are you sure you want to delete this file?',true,true).result.then(function(){
            UIService.block();
            SACService.deleteFile(bulkInputDTO, function (data, status) {
                var title = 'File delete status.';
                var message = '';
                if (status == 200) {
                    UIService.openDialogBox(title, 'Successfully deleted.', true, false);
                    $scope.getUserUploadedFilesDetails();
                }else{
                    UIService.handleException('Delete Failed',data, status);
                }
                UIService.unblock();
            });
        },function(){
           console.log('Dont delete..');
        })


    }

    $scope.clearFile = function () {
        $scope.$file = undefined;
        $scope.hasAttachment = false;
        $scope.fileSize = '';
        if(!_.isEmpty(document.getElementById('upload-filename-ie9') && !_.isNull(document.getElementById('upload-filename-ie9')))){
            document.getElementById('upload-filename-ie9').value = "";
        }

        if(!_.isEmpty(document.getElementById("selectedFile")) && !_.isNull(document.getElementById('selectedFile'))){
            document.getElementById("selectedFile").value = '';
        }
    }

    $scope.$watch('description', function () {
        if (_.isUndefined($scope.description) || $scope.description.trim().length < 1) {
            $scope.hasDescription = false;
        } else {
            $scope.hasDescription = true;
        }

        $scope.description = $scope.description.replace(/\n/g, "");
    })

    $scope.ie9FileUpload = function () {
        var files = document.getElementById("selectedFile").files;

        if (!_.isEmpty(files)) {
            $scope.$file = files[0];
            $scope.uploadAttachment();
        } else {
            UIService.openDialogBox("No File Selected","Please select a file to upload",true,false);
        }
    }
}])
;
