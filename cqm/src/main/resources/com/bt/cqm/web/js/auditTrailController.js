var module = angular.module('cqm.controllers');

module.controller('auditTrailController', ['$scope', 'UIService', 'AuditTrailService', 'PageContext', 'UIService', function ($scope, UIService, AuditTrailService, PageContext, UIService) {
    $scope.summaryData = {};
    $scope.detailData = {};
    $scope.hasDetailRecord = false;
    $scope.hasSummaryRecord =false;
    $scope.summaryRecordCount =0;
    $scope.detailRecordCount=0;

    $scope.pageType = 'Order';
    $scope.msg="";

    if (PageContext.exist()) {
        $scope.selectedSalesChannel = PageContext.getSalesChannel();
        $scope.salesChannel = $scope.selectedSalesChannel.name;
        $scope.customer = PageContext.getCustomer();
        $scope.contract = PageContext.getContract();
        $scope.customerList = PageContext.getSalesChannel().customerList;
    }
    $scope.loadPage = function (type) {
        $scope.hasDetailRecord = false;
        $scope.hasSummaryRecord =false;
        $scope.summaryRecordCount =0;
        $scope.detailRecordCount=0;
        $scope.msg="";
        if ('Quote' == type) {
            $scope.pageType = 'Quote'
            $scope.getQuoteAuditSummary();
        } else {
            $scope.pageType = 'Order'
            $scope.getOrderAuditSummary();
        }
    };

    $scope.auditSummaryGrid = { data:'summaryData',
        multiSelect:false,
        enableColumnResize:true,
        showGroupPanel:true,
        showColumnMenu:true,
        showFilter:true,

        afterSelectionChange:function (item, event) {
            if (item.selected == false) {
                return;
            }
            $scope.hasDetailRecord = true;
            $scope.fillDetailGrid(item.entity);
        },
        columnDefs:[
            {field:'quoteRefID', displayName:'Quote Reference Id', width:160},
            {field:'quoteName', displayName:'Quote Name', width:120},
            {field:'lastUpdatedDateTime', displayName:'Last Updated Date', width:160},
            {field:'lastUpdatedSummary', displayName:'Last Updated Summary', width:140},
            {field:'lastUpdatedValue', displayName:'Last Updated Value', width:120},
            {field:'quoteStatus', displayName:'Quote Status', width:120},
            {field:'userName', displayName:'User Name', width:120},
            {field:'expedioReference', displayName:'Expedio Reference', width:120},
            {field:'expedioOrderid', displayName:'Expedio Order ID', width:120},
            {field:'orderStatus', displayName:'Order Status', width:120},
            {field:'orderSubStatus', displayName:'Order Sub Status', width:120},
            {field:'quoteVersion', displayName:'Quote Version', width:120}
        ]

    };

    $scope.auditDetailGrid = { data:'detailData',
        multiSelect:false,
        enableColumnResize:true,
        showGroupPanel:true,
        showColumnMenu:true,
        showFilter:true,

        afterSelectionChange:function (item, event) {
            if (item.selected == false) {
                return;
            }
        },
        columnDefs:[
            {field:'quoteRefID', displayName:'Quote Reference Id', width:150},
            {field:'quoteName', displayName:'Quote Name', width:120},
            {field:'task', displayName:'Task', width:140},
            {field:'auditEvent', displayName:'Audit Event', width:120},
            {field:'oldValue', displayName:'Old Value', width:120},
            {field:'newValue', displayName:'New Value', width:120},
            {field:'quoteStatus', displayName:'Order Status', width:120},
            {field:'dateTime', displayName:'Date Time', width:160},
            {field:'userName', displayName:'User Name', width:160},
            {field:'expedioReference', displayName:'Expedio Reference', width:120},
            {field:'expedioOrderid', displayName:'Expedio Order ID', width:120},
            {field:'orderStatus', displayName:'Order Status', width:120}
        ]

    };

    $scope.fillDetailGrid = function(summaryEntity){
        var quoteId = summaryEntity.quoteRefID;
        var orderId = summaryEntity.expedioOrderid;

        if($scope.pageType =='Quote'){
            $scope.getQuoteAuditDetail(quoteId);
        }else{
            $scope.getOrderAuditDetail(orderId);
        }
    };

    $scope.getQuoteAuditSummary = function () {
        $scope.hasSummaryRecord =false;
        $scope.summaryRecordCount =0;
        UIService.block();
        AuditTrailService.getAuditQuoteSummary(PageContext.getCustomer().cusId, function (data, status) {
            if ('200' == status) {
                $scope.summaryData = data;
                $scope.hasSummaryRecord =true;
                $scope.summaryRecordCount =data.length;
            } else if ('404' == status) {
                $scope.summaryData = {};
                $scope.msg="No Audit Trail Record Exist.";
            } else {
                UIService.handleException('Quote Audit Summary', data, status);
            }

            UIService.unblock();
        });
    };

    $scope.getQuoteAuditDetail = function (id) {
        $scope.hasDetailRecord = false;
        $scope.detailRecordCount=0;
        UIService.block();
        AuditTrailService.getAuditQuoteDetail(PageContext.getCustomer().cusId, id, function (data, status) {
            if ('200' == status) {
                $scope.detailData = data;
                $scope.hasDetailRecord = true;
                $scope.detailRecordCount=data.length;
            } else if ('404' == status) {
                $scope.detailData = {};
            } else {
                UIService.handleException('Quote Audit Detail', data, status);
            }
            UIService.unblock();
        });
    };

    $scope.getOrderAuditSummary = function () {
        $scope.hasSummaryRecord =false;
        $scope.summaryRecordCount =0;
        UIService.block();
        AuditTrailService.getAuditOrderSummary(PageContext.getCustomer().cusId, function (data, status) {
            if ('200' == status) {
                $scope.summaryData = data;
                $scope.hasSummaryRecord =true;
                $scope.summaryRecordCount =data.length;
            } else if ('404' == status) {
                $scope.summaryData = {};
                $scope.msg="No Audit Trail Record Exist.";
            } else {
                UIService.handleException('Quote Audit Summary', data, status);
            }
            UIService.unblock();
        });
    };

    $scope.getOrderAuditDetail = function (id) {
        $scope.hasDetailRecord = false;
        $scope.detailRecordCount=0;
        UIService.block();
        AuditTrailService.getAuditOrderDetail(PageContext.getCustomer().cusId, id, function (data, status) {
            if ('200' == status) {
                $scope.detailData = data;
                $scope.hasDetailRecord = true;
                $scope.detailRecordCount=data.length;
            } else if ('404' == status) {
                $scope.detailData = {};
            } else {
                UIService.handleException('Quote Audit Detail', data, status);
            }
            UIService.unblock();
        });
    };

    /*Launch the Configurator.  */
    $scope.generateReport = function () {
        var userDetails = $scope.salesUser;

        AuditTrailService.getSQEReportsURL($scope.selectedSalesChannel.name,function (data, status) {
            if (status == '200') {
                var tokens = data.split("guid=");
                var guid = tokens[1];

                var domain = 'bt.com';
                /*var expires = (function(){
                    var date = new Date();
                    date.setTime(date.getTime() + (60 * 60 * 1000));
                    return date.toUTCString();
                })();*/
                var name = 'SQE_GUID';
                var path = '/';
                var value = guid;

                document.cookie = name + '=' + encodeURIComponent(value) +'; path=' + path + '; domain=' + domain + ';';

                var sqeURL = data;
                window.open(sqeURL);
            } else {
                UIService.handleException('Launch Configurator', data, status);
            }
        });
    };



}])
