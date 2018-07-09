var module = angular.module('cqm.controllers');

module.controller('quotesController', ['$scope', '$routeParams', '$http', 'quoteService','branchSiteService','channelHierarchyService', '$location', '$modal', 'UIService', 'PageContext', 'UserContext', 'channelContactService', 'WebMetrics', 'SessionContext', '$rootScope','customerService', function ($scope, $routeParams, $http, quoteService,branchSiteService,channelHierarchyService, $location, $modal, UIService, PageContext, UserContext, channelContactService, WebMetrics, SessionContext, $rootScope,customerService) {
    console.log('Inside Quotes controller');
    var waitingForSecondClick = false;
    $scope.isContractResign=false;
    $scope.billingAccountAssociated = false;
    $scope.quotePriceBooks = [];
    $scope.distributorRoles =
    [
        { name:'Account Manager', label:'Account Manager*'},
        { name:'Sales Support Contact', label:'Sales Support Contact*'},
        { name:'System Eng Contact', label:'System Eng Contact*'},
        { name:'Delivery Manager', label:'Delivery Manager*'},
        { name:'Project Manager', label:'Project Manager'}
    ];

    $scope.subGroups = [];
    $scope.quoteActivityStatusList = [
        {name:'', value:0},
        {name:'All Quotes', value:1},
        {name:'Expired Quotes', value:2},
        {name:'Active Quotes', value:3}
    ];

    $scope.quoteActivityFilterList = [
        {name:'', value:0, status:''},
        {name:'My Quotes', value:1, status:''},
        {name:'My Draft Quotes', value:2, status:"DRAFT"},
        {name:'My Approved Quotes', value:3, status:"Tech Validation Approved"},
        {name:'My Customer Approved Quotes', value:4, status:"Customer Approved"},
        {name:'My Converted to Order Quotes', value:5, status:"Converted To Order"},
        {name:'My Pending Approval Quotes', value:6, status:"Pending Customer Approval"},
        {name:'My Rejected Quotes', value:7, status:"Customer Rejected"}

    ]

    $scope.currencies =
    [
        { name:'EUR', value:'11'},
        { name:'GBP', value:'21'},
        { name:'USD', value:'31'}

    ];

    $scope.orderTypes =
    [
        { name:'Provide', value:'1'},
        { name:'Modify', value:'2'},
        { name:'Cease', value:'3'}
    ];

    $scope.contractTerms = ['12','24','36','48','60'];
    /*[
        { name:'12', value:'11'},
        { name:'24', value:'21'},
        { name:'36', value:'31'},
        { name:'48', value:'41'},
        { name:'60', value:'51'}
    ];*/
    $scope.subOrderTypes = [
        { name:'', value:'0'},
        { name:'Contract Resign', value:'1'}
    ];

    $scope.quoteIndicativeFlagList = [
        { name:'Indicative', value:'0'},
        { name:'Firm', value:'1'}
    ];

    $scope.siteFilterList =
    [
        { name:'', value:''},
        { name:'Site Name', value:'siteName'},
        { name:'Site Id', value:'siteId'}

    ];

    $scope.filterTextActStatus = '';
    $scope.filterTextStdFilter = '';
    $scope.userName = SessionContext.getUser().name;

    $scope.numOfQuotes = 0;
    $scope.listOfQuotes = [];
    $scope.contractTermReq = true;
    $scope.oppReferenceReq = true;
    // channel contact Form initialization.
    var distributorRoleTemp = {"name":"", "value":""};
    $scope.channelContactFormData = {};
    $scope.channelContactFormData.channelContactId = null;
    $scope.channelContactFormData.ein = null;
    $scope.channelContactFormData.firstName = null;
    $scope.channelContactFormData.lastName = null;
    $scope.channelContactFormData.jobTitle = null;
    $scope.channelContactFormData.phone = null;
    $scope.channelContactFormData.mobile = null;
    $scope.channelContactFormData.fax = null;
    $scope.channelContactFormData.email = null;
    $scope.channelContactFormData.distributorRole = distributorRoleTemp;

    $scope.quoteId = null;
    $scope.quoteDetail = null;
    $scope.isQuoteSelected = false;
    $scope.isContactSelected = false;
    $scope.hasContacts = false;
    $scope.showContactsForm = false;
    $scope.showBTDirectorygrid = false;
    $scope.isDraftQuote = false;
    $scope.isBtDirectorySelected = false;
    $scope.channelContactsGridData = [];
    $scope.channelContactsData = [];
    $scope.siteId='';
    $scope.quoteFormData = {};
    $scope.quoteFormData.contractTerm=null;
    $scope.fiterText = '';
    $scope.siteNameSelected =false;
    $scope.siteIdSelected =false;
    $scope.showSubGroupUser =false;
    $scope.userSubGroupList=[];
    $scope.userSubGroupListDisplay=[];
    $scope.quotesCached =[];

    if (PageContext.exist()) {
        $scope.customer = PageContext.getCustomer();
        $scope.contract = PageContext.getContract();
        $scope.salesChannel = PageContext.getSalesChannel();

    }

    if (UserContext.exist()) {
        $scope.salesUser = UserContext.getUser();
        $scope.showSubGroupUser=UserContext.isSubGroupUser();
        $scope.userSubGroupList=UserContext.getUserSubGroups();
    }

    $scope.loadCreateQuotePage = function () {
        UIService.block();
        if ($scope.showSubGroupUser) {
            for (var i = 0; i < $scope.userSubGroupList.length; i++) {
                if ($scope.userSubGroupList[i]!="ALL") {
                    $scope.userSubGroupListDisplay.push($scope.userSubGroupList[i]);
                }
            }
            $scope.createQuoteFormData.subGroup =$scope.userSubGroupListDisplay[0];
        }
        UIService.unblock();

    }

    $scope.onPageLoad = function () {
        $scope.quoteActivityStatus = $scope.quoteActivityStatusList[0];
        $scope.quoteActivityFilter = $scope.quoteActivityFilterList[0];
      //  $scope.getBranchSite();
        $scope.siteId='';
        $scope.getQuotes();

    };

    $scope.onQuoteActivityStatusChange = function () {

        if (!_.isUndefined($scope.quoteActivityStatus)) {

            switch ($scope.quoteActivityStatus.value) {
                case 0:
                    $scope.filterTextActStatus = '';
                    break;
                case 1:
                    $scope.filterTextActStatus = '';
                    break;
                case 2:
                    //$scope.filterTextActStatus = 'quoteExpiryDate:' + "/" + ";";
                    $scope.filterTextActStatus = 'expired:' + "true" + ";";
                    break;
                case 3:
                    //$scope.filterTextActStatus = 'quoteExpiryDate:' + "NIL" + ";";
                    $scope.filterTextActStatus = 'expired:' + "false" + ";";
                    break;
            }

            $scope.updateFilterText();
        }
    };

    $scope.onQuoteActivityFilterChange = function () {
        if (!_.isUndefined($scope.quoteActivityFilter)) {
            switch ($scope.quoteActivityFilter.value) {
                case 0:
                    $scope.filterTextStdFilter = '';
                    break;
                case 1:
                    $scope.filterTextStdFilter = 'salesRepName:' + $scope.userName;
                    break;
                case 2:
                    $scope.filterTextStdFilter = 'salesRepName:' + $scope.userName + ";" + $scope.getQuoteStatus(2);
                    break;
                case 3:
                    $scope.filterTextStdFilter = 'salesRepName:' + $scope.userName + ";" + $scope.getQuoteStatus(3);
                    break;
                case 4:
                    $scope.filterTextStdFilter = 'salesRepName:' + $scope.userName + ";" + $scope.getQuoteStatus(4);
                    break;
                case 5:
                    $scope.filterTextStdFilter = 'salesRepName:' + $scope.userName + ";" + $scope.getQuoteStatus(5);
                    break;
                case 6:
                    $scope.filterTextStdFilter = 'salesRepName:' + $scope.userName + ";" + $scope.getQuoteStatus(6);
                    break;
                case 7:
                    $scope.filterTextStdFilter = 'salesRepName:' + $scope.userName + ";" + $scope.getQuoteStatus(7);
                    break;
            }

            $scope.updateFilterText();
        }
    };

    $scope.getQuoteStatus = function (seqNo) {
        var quoteSate = _.find($scope.quoteActivityFilterList, function (quoteState) {
            if (quoteState.value == seqNo) {
                return quoteState;
            }
        });

        if (!_.isUndefined(quoteSate)) {
            return 'quoteStatus:' + quoteSate.status;
        } else {
            return '';
        }
    };

    $scope.updateFilterText = function () {
        $scope.quotesGrid.filterOptions.filterText = $scope.filterTextActStatus + ";" + $scope.filterTextStdFilter + ";" + $scope.fiterText;
        $scope.quoteFormData = {};
        $scope.resetChannelContact();
    }

    $scope.$watch('fiterText', function () {
        if (!_.isUndefined($scope.fiterText)) {
            $scope.updateFilterText();
        }
    })

    var initializeQuoteFormData = function () {
        if (UserContext.getUser().userType == 'Direct') {
            $scope.oppReferenceReq = true;
        } else {
            $scope.oppReferenceReq = false;

        }
        if ($scope.customer != undefined && $scope.customer != null) {
            $scope.createQuoteFormData = {'salesOrgName':$scope.salesChannel.name, 'salesRepName':$scope.salesUser.name, 'customerName':$scope.customer.cusName, 'customerId':$scope.customer.cusId, 'quoteStatus':"DRAFT", 'quoteVersion':"1.0", 'contractId':$scope.contract.id, 'subOrderType':''};
            $scope.isCustomerSelected = true;
        } else {
            $scope.isCustomerSelected = false;
            return;
        }

        $scope.createQuoteFormData.subOrderType = $scope.subOrderTypes[0];
        $scope.createQuoteFormData.quoteIndicativeFlag = $scope.quoteIndicativeFlagList[1];
    };

    initializeQuoteFormData();

    $scope.setModifyCeaseFields = function () {
        if (!_.isEmpty($scope.createQuoteFormData.orderType)) {
            if ($scope.createQuoteFormData.orderType.name == "Modify" && $scope.createQuoteFormData.subOrderType.name != "Contract Resign") {
                $scope.contractTermReq = false;
            } else {
                $scope.contractTermReq = true;
            }

            if ($scope.createQuoteFormData.orderType.name == "Cease") {
                $scope.oppReferenceReq = false;
                $scope.contractTermReq = false;
            } else if (UserContext.getUser().userType == 'Direct') {
                $scope.oppReferenceReq = true;
            }
        }
        if($scope.createQuoteFormData.subOrderType.name == "Contract Resign" && UserContext.getUser().userType != 'Direct' )
        {
            $scope.isContractResign=true;
        }
        else
        {
            $scope.isContractResign=false;
        }
    }

    $scope.setModifyCeaseFieldsForViewQuotes = function () {
        if ($scope.quoteFormData.orderType == "Cease") {
            $scope.oppReferenceReq = false;
        } else if (UserContext.getUser().userType == 'Direct') {
            $scope.oppReferenceReq = true;
        }
    }
    //SEARCH BT directory........................

    var cqmDialogBoxController = function ($scope, $modalInstance, hTitle, messageBody, showOkButton, showCancelButton, griddata, parentscope) {

        $scope.hTitle = hTitle;
        $scope.messageBody = messageBody;
        $scope.showOk = showOkButton;
        $scope.showCancel = showCancelButton;
        $scope.channelContactsGridData = griddata;
        $scope.parentscope = parentscope;
        $scope.dialogItem = dialogItem;

        $scope.ok = function () {
            console.log('Clicked Ok');
            $modalInstance.close($scope.dialogItem);
        };

        $scope.cancel = function () {
            console.log('Clicked Cancel');
            $modalInstance.dismiss('cancel');
        };
    };

    $scope.openCustomDialogBox = function (hTitle, messageBody, templateUrl, showOkButton, showCancelButton, gridDefinition, parentscope) {

        var modalInstance = $modal.open({
                                            templateUrl:templateUrl,
                                            controller:cqmDialogBoxController,
                                            resolve:{
                                                hTitle:function () {
                                                    return hTitle;
                                                },
                                                messageBody:function () {
                                                    return messageBody;
                                                },
                                                showOkButton:function () {
                                                    return showOkButton;
                                                },
                                                showCancelButton:function () {
                                                    return showCancelButton;
                                                },
                                                griddata:function () {
                                                    return gridDefinition;
                                                },
                                                parentscope:function () {
                                                    return parentscope;
                                                }

                                            }
                                        });
        return modalInstance;
    };


    $scope.searchBtDirectoryGrid = { data:'channelContactsGridData', enableRowSelection:true, multiSelect:false, enableColumnResize:true, selectWithCheckboxOnly:true,
        afterSelectionChange:function (item, event) {
            if (item.selected == false) {
                console.log('got de-select event in channelContactsData');
                $scope.isBtDirectorySelected = false;
                return;
            }

            $scope.populatedChannelContactForm(item.entity);
            $scope.isBtDirectorySelected = true;
            $scope.showBTDirectorygrid = false;
        },
        columnDefs:[
            {field:'ein', displayName:'EIN', width:110},
            {field:'firstName', displayName:'First Name', width:110},
            {field:'lastName', displayName:'Last Name', width:120},
            {field:'jobTitle', displayName:'Job Title', width:140},
            {field:'phoneNum', displayName:'Phone', width:110},
            {field:'mobile', displayName:'Mobile', width:110},
            {field:'fax', displayName:'Fax', width:110},
            {field:'mailId', displayName:'E-Mail', width:180},
            {field:'thirdPartyEmailId', displayName:'Third party E-Mail', width:180},
            {field:'distributorRole', displayName:'Distributor Role', width:130}

        ] };


    $scope.populatedChannelContactForm = function (data) {
        $scope.channelContactSelection = false;
        $scope.channelContactFormData.ein = data.ein;
        $scope.channelContactFormData.firstName = data.firstName;
        $scope.channelContactFormData.lastName = data.lastName;
        $scope.channelContactFormData.jobTitle = data.jobTitle;
        $scope.channelContactFormData.phone = data.phoneNum;
        $scope.channelContactFormData.mobile = data.mobileNum;
        $scope.channelContactFormData.fax = data.fax;
        if (!_.isEmpty(data.mailId))
            $scope.channelContactFormData.email = data.mailId;
        else
            $scope.channelContactFormData.email = data.thirdPartyEmailId;
        $scope.channelContactFormData.distributorRole = data.distributorRole;


    }


    $scope.createChannelContact = function () {
        $scope.showContactsForm = true;
    }


    $scope.channelContactDataComparator = function (gridData, formData) {
        if (formData.ein != gridData.ein || formData.firstName != gridData.firstName ||
            formData.lastName != gridData.lastName || formData.jobTitle != gridData.jobTitle ||
            formData.phone != gridData.phone || formData.mobile != gridData.mobile ||
            formData.fax != gridData.fax || formData.email != gridData.email ||
            formData.distributorRole != gridData.distributorRole) {
            return true;
        } else {
            return false;
        }

    }

    $scope.showChannelContactsData = function (quoteId) {
        quoteService.getChannelContacts(quoteId, function (data, status) {
            if (status == '200') {
                $scope.hasContacts = true;

                if (data != undefined && data.length == undefined) {
                    $scope.channelContactsData = [data];
                } else if (data != undefined) {
                    $scope.channelContactsData = data;
                }
                window.setTimeout(function () {
                    $(window).resize();
                }, 1);
            } else if (status == '404') {
                $scope.hasContacts = false;
            } else {
                UIService.handleException('Quote Channel Contact', data, status);
                $scope.hasContacts = false;
            }


        });
    }

    $scope.channelContactsGrid = { data:'channelContactsData', enableRowSelection:true, multiSelect:false, enableColumnResize:true,
        afterSelectionChange:function (item, event) {
            if (item.selected == false) {
                console.log('got de-select event in channelContactsData');
                //$scope.showChannelActions(item.entity);
                $scope.resetChannelContact();
                $scope.isContactSelected = false;
                $scope.showBTDirectorygrid = false;
                return;
            }
            $scope.isContactSelected = true;
            $scope.selectedChannelContactGridData = item.entity;
            $scope.showChannelActions(item.entity);
        },
        columnDefs:[
            {field:'channelContactID', displayName:'Channel Contact ID', width:130},
            {field:'quoteID', displayName:'Quote Ref ID', width:140},
            {field:'ein', displayName:'EIN', width:110},
            {field:'firstName', displayName:'First Name', width:110},
            {field:'lastName', displayName:'Last Name', width:120},
            {field:'jobTitle', displayName:'Job Title', width:140},
            {field:'phoneNumber', displayName:'Phone', width:110},
            {field:'mobileNumber', displayName:'Mobile', width:110},
            {field:'fax', displayName:'Fax', width:110},
            {field:'email', displayName:'E-Mail', width:180},
            {field:'role', displayName:'Distributor Role', width:130}

        ] };

    $scope.quotesGrid = { data:'listOfQuotes', enableRowSelection:true, multiSelect:false, enableColumnResize:true, showFilter:false, showColumnMenu:true, showGroupPanel:true,
        filterOptions:{
            filterText:''
        },
        sortInfo:{
            fields:['quoteRefID'],
            directions:['dsc']
        },
        onRegisterApi:function (gridApi) {
            $scope.gridApi = gridApi;
        },

        rowTemplate:'<div style="height: 100%" ng-click="clickEventHandler(row)" ><div ng-repeat="col in renderedColumns" ng-class="col.colIndex()" class="ngCell">' +
                    '<div class="ngVerticalBar" ng-style="{height: rowHeight}" ng-class="{ ngVerticalBarVisible: !$last }"> </div>' +
                    '<div ng-cell></div>' +
                    '</div></div>',

        columnDefs:[
            {field:'quoteRefID', displayName:'Quote Ref ID', width:140},
            {field:'quoteName', displayName:'Quote Name', width:140},
            {field:'quoteVersion', displayName:'Quote Version', width:120},
            {field:'quoteStatus', displayName:'Quote Status', width:110},
            {field:'quoteOptionName', displayName:'QuoteOption Name', width:170},
            {field:'subGroup', displayName:'Sub Group', width:100,visible:$scope.showSubGroupUser},
            {field:'quoteIndicativeFlag', displayName:'Quote Indicative flag', width:100},
            {field:'orderType', displayName:'Order Type', width:100},
            {field:'orderStaus', displayName:'Order Status', width:100},
            {field:'subOrderType', displayName:'Sub Order Type', width:140},
            {field:'contractTerm', displayName:'Contract Term', width:110},
            {field:'currency', displayName:'Currency', width:100},
            {field:'siebelID', displayName:'Opportunity Reference Number', width:230},
            {field:'bidNumber', displayName:'BID Number', width:100},
            {field:'salesRepName', displayName:'Sales Rep Name', width:130},
            {field:'customerID', displayName:'Customer Id', width:130, visible:false},
            {field:'customerName', displayName:'Customer Name', width:130},
            {field:'salesOrganization', displayName:'Sales Organization', width:130},
            {field:'createdDate', displayName:'Created date', width:130, visible:false},
            {field:'modifiedDate', displayName:'Modified Date', width:130, visible:false},
            {field:'quoteExpiryDate', displayName:'Quote Expiry Date', width:140},
            {field:'expired', displayName:'Expired',visible:false}
        ] };

    $scope.clickEventHandler = function (row) {
        executingDoubleClick = false;
        if (waitingForSecondClick) {
            waitingForSecondClick = false;
            executingDoubleClick = true;
            return $scope.launchConfigurator(row.entity.quoteRefID, row.entity.quoteVersion);
        }
        waitingForSecondClick = true;

        setTimeout(function () {
            waitingForSecondClick = false;
            return $scope.singleClickOnQuoteGrid(row);
        }, 250); // waiting for 250 ns

    }

    $scope.singleClickOnQuoteGrid = function (item) {
        console.log('got Single Click Event in custQuotesData');
        if (item.selected == false) {
            $scope.isQuoteSelected = false;
            $scope.isContactSelected = false;
            $scope.quoteId = undefined;
            $scope.resetChannelContact();
            $scope.channelContactsData = [];
            $scope.showBTDirectorygrid = false;
            UIService.unblock();
            return;
        }
        UIService.block();
        $scope.isQuoteSelected = true;
        $scope.showQuoteDetail(item.entity);
        $scope.showChannelContacts(item.entity);
        $scope.setModifyCeaseFieldsForViewQuotes();
        UIService.unblock();

    }

    $scope.showQuoteDetail = function (quote) {
        $scope.quoteFormData.quoteRefId = quote.quoteRefID;
        $scope.quoteFormData.quoteVer = quote.quoteVersion;
        $scope.quoteFormData.custName = quote.customerName;
        $scope.quoteFormData.custId = quote.customerID;
        $scope.quoteFormData.salesOrgName = quote.salesOrganization;
        $scope.quoteFormData.quoteName = quote.quoteName;
        $scope.quoteFormData.orderType = quote.orderType;
        $scope.quoteFormData.bidNum = quote.bidNumber;
        $scope.quoteFormData.salesRepName = quote.salesRepName;
        $scope.quoteFormData.quoteStatus = quote.quoteStatus;
        $scope.quoteFormData.currency = quote.currency;
        $scope.quoteFormData.opportunityRef = quote.siebelID;
        $scope.quoteFormData.subOrderType = quote.subOrderType;
        $scope.quoteFormData.orderStatus = quote.orderStatus;
        $scope.quoteFormData.contractTerm = quote.contractTerm;
        $scope.quoteFormData.quoteIndFlag = _.find($scope.quoteIndicativeFlagList, function (obj) {

            if (obj.name == quote.quoteIndicativeFlag) {
                return obj;
            }
        });

        if (quote.quoteStatus == 'DRAFT' || quote.quoteStatus == 'Draft') {
            $scope.isDraftQuote = true;
        } else {
            $scope.isDraftQuote = false;
        }
        $scope.quoteFormData.quoteExpiryDate = quote.quoteExpiryDate;
        $scope.quoteFormData.quoteOptionName = quote.quoteOptionName;

    };

    $scope.showChannelContacts = function (obj) {
        console.log('Inside showChannelContacts() ', obj);
        $scope.quoteId = obj.quoteRefID;
        $scope.quoteDetail = obj;
        $scope.showChannelContactsData(obj.quoteRefID);

    };


    /* $scope.selectedChannelContactGridData = null;
     $scope.channelContactsData = [];

     $scope.channelContacts = { data:'channelContactsData', enableRowSelection:true, multiSelect:false, enableColumnResize:true,
     afterSelectionChange:function (item, event) {
     $scope.selectedChannelContactGridData = item.entity;
     $scope.createChannelContact();
     if (item.selected == false) {
     console.log('got de-select event in channelContactsData');
     $scope.showChannelActions(item.entity);
     return;
     }
     $scope.selectedChannelContactGridData = item.entity;
     $scope.showChannelActions(item.entity);
     },
     columnDefs:[
     {field:'channelContactID', displayName:'Channel Contact ID', width:130},
     {field:'quoteID', displayName:'Quote Ref ID', width:140},
     {field:'ein', displayName:'EIN', width:110},
     {field:'firstName', displayName:'First Name', width:110},
     {field:'lastName', displayName:'Last Name', width:120},
     {field:'jobTitle', displayName:'Job Title', width:140},
     {field:'phoneNumber', displayName:'Phone', width:110},
     {field:'mobileNumber', displayName:'Mobile', width:110},
     {field:'fax', displayName:'Fax', width:110},
     {field:'email', displayName:'E-Mail', width:180},
     {field:'role', displayName:'Distributor Role', width:130}

     ] };
     */
    $scope.showChannelActions = function (obj) {
        console.log('Inside showChannelActions() ', obj);
        $scope.channelContactSelection = false;
        $scope.channelContactFormData.channelContactId = obj.channelContactID;
        $scope.channelContactFormData.ein = obj.ein;
        $scope.channelContactFormData.firstName = obj.firstName;
        $scope.channelContactFormData.lastName = obj.lastName;
        $scope.channelContactFormData.jobTitle = obj.jobTitle;
        $scope.channelContactFormData.phone = obj.phoneNumber;
        $scope.channelContactFormData.mobile = obj.mobileNumber;
        $scope.channelContactFormData.fax = obj.fax;
        $scope.channelContactFormData.email = obj.email;
        $scope.channelContactFormData.distributorRole = _.find($scope.distributorRoles, function (role) {
            if (role.name == obj.role) {
                return role;
            }
        });
    };


    $scope.resetChannelContact = function () {
        console.log('Inside resetChannelContact. ');
        $scope.channelContactSelection = false;
        $scope.channelContactFormData.channelContactId = null;
        $scope.channelContactFormData.ein = null;
        $scope.channelContactFormData.firstName = null;
        $scope.channelContactFormData.lastName = null;
        $scope.channelContactFormData.jobTitle = null;
        $scope.channelContactFormData.phone = null;
        $scope.channelContactFormData.mobile = null;
        $scope.channelContactFormData.fax = null;
        $scope.channelContactFormData.email = null;
        $scope.channelContactFormData.distributorRole = null;

        $scope.isBtDirectorySelected = false;
        $scope.showBTDirectorygrid = false;
    };


    /* ---------------------------------------------------Server Calls ----------------------------------*/

    $scope.getQuotes = function () {
        console.log('Inside get quotes');
      UIService.block();
        var startTime = new Date().getTime();
        quoteService.getQuotes($scope.salesChannel.name, $scope.customer.cusId,$scope.siteId).then(function (data, status) {
            $scope.isCustomerSelected = true;
            $scope.numOfQuotes = 0;
            $scope.noQuotesFound = false;
            var quoteList = [];
            var quotesList = data;
            UIService.unblock();
            $scope.getBranchSite();
           // UIService.unblock();
            if (angular.isArray(data)) {
                if (quotesList.length == undefined) {
                    $scope.numOfQuotes = 0;
                    $scope.noQuotesFound = true;
                } else {

                    if ($scope.showSubGroupUser) {
                        $scope.listOfQuotes = [];
                        if (_.contains($scope.userSubGroupList, "ALL")) { //Need to show all Quotes.
                            $scope.listOfQuotes = quotesList;
                        }
                        else {
                            for (var i = 0; i < quotesList.length; i++) {
                                if (!_.isEmpty(quotesList[i].subGroup)) {
                                    if (_.contains($scope.userSubGroupList, quotesList[i].subGroup)) {
                                        $scope.listOfQuotes.push(quotesList[i])
                                    }
                                }
                            }
                        }
                    }
                    else {
                        if (_.contains($scope.userSubGroupList, "ALL")) { //Need to show all Quotes.
                            $scope.listOfQuotes = quotesList;
                        }
                        else {
                            $scope.listOfQuotes = [];
                            for (var i = 0; i < quotesList.length; i++) {
                                if (_.isEmpty(quotesList[i].subGroup)) {
                                    $scope.listOfQuotes.push(quotesList[i]);
                                }
                            }
                        }
                    }
                    /*_.each($scope.listOfQuotes, function (aQuote) {
                        if (aQuote.quoteExpiryDate == '') {
                            aQuote.quoteExpiryDate = 'NIL';
                        }
                    })*/
                    $scope.numOfQuotes = ($scope.listOfQuotes).length;
                    $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.VALID);
                }


            }
            else {
                $scope.numOfQuotes = 0;
                $scope.noQuotesFound = true;
                $scope.listOfQuotes=[];
            }

            $scope.quotesCached = $scope.listOfQuotes;
        });

    };


    /*Launch the Configurator.  */
    $scope.launchConfigurator = function (quoteId, quoteVersion) {
        var userDetails = $scope.salesUser;
        if (quoteId == undefined || quoteVersion == undefined) {
            quoteId = $scope.quoteDetail.quoteRefID;
            quoteVersion = $scope.quoteDetail.quoteVersion;
        }

        quoteService.getBundlingAppURL(quoteId, quoteVersion, userDetails.ein, function (data, status) {
            if (status == '200') {
                var tokens = data.split("guid=");
                var guid = tokens[1];

                if (!_.isEmpty(guid)) {
                    var domain = 'bt.com';
                    /*var expires = (function () {
                        var date = new Date();
                        date.setTime(date.getTime() + (60 * 60 * 1000));
                        return date.toUTCString();
                    })();*/
                    var name = 'SQE_GUID';
                    var path = '/';
                    var value = guid;

                    document.cookie = name + '=' + encodeURIComponent(value) + '; path=' + path + '; domain=' + domain + ';';

                    var sqeURL = data;
                    window.open(sqeURL);
                } else {
                    console.log('Failed to retrieve GUID from token :' + data);
                }
            }else if('409'){
                UIService.openDialogBox('Launch Configurator', data,true, false);
            }else {
                UIService.handleException('Launch Configurator', data, status);
            }
        });
    };
    $scope.createQuoteBillingAssoCheck = function () {
        if (UserContext.getUser().userType != 'Direct')
        {
            if ($scope.customer != undefined && $scope.customer != null) {
                UIService.block();
                channelHierarchyService.loadChannelPartnerDetails($scope.customer.cusId, function (responseData, status) {
                    var channelPartnerDetails = responseData;
                    if (status == '200') {
                        if (responseData != undefined) {
                            $scope.createQuote();
                        }

                    } else {
                        UIService.openDialogBox('Quote Creation', "Please link  customer into the hierarchy on the Channel Hierarchy section before attempting to create a quote",true, false);
                        UIService.unblock();
                        return;
                    }
                    UIService.unblock();

                });
            }
        }
        else
        {
            $scope.createQuote();
        }
    }




        $scope.createQuote = function () {
        UIService.block();
        var startTime = new Date().getTime();

            quoteService.createQuoteService($scope.salesUser.ein, $scope.createQuoteFormData,$scope.quotePriceBooks, function (data, status) {
            if (status == '200') {
                var tokens = data.split("guid=");
                var guid = tokens[1];
                var quoteRefStrEnd = guid.length;
                var quoteRefStrStart = quoteRefStrEnd - 15;
                var quoteRefId = guid.substring(quoteRefStrStart, quoteRefStrEnd);
                var sqeUrl = data;
                WebMetrics.captureWebMetrics('CQM Quotes Tab - Create Quote', startTime);
                $scope.createQuoteFormData.quoteReference = quoteRefId;

                var quoteDetails = {};
                quoteDetails.quoteName = $scope.createQuoteFormData.quoteName;
                quoteDetails.quoteRefID = quoteRefId;
                quoteDetails.quoteVersion = $scope.createQuoteFormData.quoteVersion;

                var dialogInstance = UIService.openDialogBox('Create Quote', 'Quote successfully created. Quote Ref ID - ' + quoteRefId + ' .', true, false);
                dialogInstance.result.then(function () {
                    $scope.launchConfigurator(quoteRefId, quoteDetails.quoteVersion);
                }, function () {
                });
                initializeQuoteFormData();
            }
            else if('409'){
                UIService.openDialogBox('Quote Creation', data.description,true, false);
            }else {
                UIService.handleException('Quote Creation', data, status);
            }
            UIService.unblock();
        });
    };

    $scope.updateQuote = function () {
        UIService.block();
        quoteService.updateQuote($scope.salesUser.ein, $scope.quoteFormData, function (data, status) {
            var msg = 'Failed to update !!';
            var title = 'Update Quote';

            if (status == '200') {
                msg = "Successfully updated quote !!";
                UIService.openDialogBox(title, msg, true, false);
                $scope.getQuotes();

            } else {

                if (status == '404') {
                    msg = 'Expedio Service Unavailable. Please try after some time !!';
                }

                dialogInstance = UIService.openDialogBox(title, msg, true, false);
            }
            UIService.unblock();
        });
    };

    $scope.createChannel = function (channelContactForm) {
        var opType = 'save';
        if ($scope.quoteId == null) {
            window.alert("Please select a quote.");
        } else {
            var title = 'Quote Channel Contact';
            var msg = 'Please make some changes in then save.';

            if (channelContactForm.channelContactId != null && channelContactForm.channelContactId.trim() != "") {
                opType = 'update';
                var hasValueChanged = $scope.channelContactDataComparator($scope.selectedChannelContactGridData, channelContactForm);
                if (hasValueChanged == false) {
                    UIService.openDialogBox(title, msg, true, false);
                }
            }
            UIService.block();
            channelContactService.createUpdateDeleteChannelContact($scope.quoteId, $scope.customer.cusId, opType, channelContactForm, function (data, status) {

                if (status == 200) {

                    if (opType == 'save') {
                        msg = "Successfully created channel contact.";
                    } else {
                        msg = "Successfully updated channel contact.";
                    }
                    UIService.openDialogBox(title, msg, true, false);

                    $scope.showChannelContactsData($scope.quoteId);
                    $scope.resetChannelContact();
                    $scope.showContactsForm = false;
                    UIService.unblock();
                } else if (status == 409) {
                    msg = "Already a contact exist on this distributor role !!. Choose a different role type.";
                    UIService.openDialogBox(title, msg, true, false);
                } else {
                    UIService.handleException('Channel Contact', data, status);
                }
                UIService.unblock();
            });
        }
    };

    $scope.deleteChannelContact = function (channelContactForm) {
        if (!_.isEmpty($scope.quoteId)) {

            channelContactService.createUpdateDeleteChannelContact($scope.quoteId, $scope.customer.cusId, 'delete', channelContactForm, function (data, status) {

                if (status == 200) {
                    //window.alert("Success to delete the Channel Contact.");
                    UIService.openDialogBox('Delete Quote Contact', 'Successfully deleted channel contact.', true, false);
                    $scope.showChannelContactsData($scope.quoteId)

                } else {
                    //window.alert("Success to delete Channel Contact.");
                    UIService.openDialogBox('Delete Quote Contact', 'Failed to deleted channel contact.', true, false);
                }
            });
        }
    };

    $scope.searchBtDirectory = function (channelContactForm) {
        console.log("launch BT directory. searcBTDirectory.html" + channelContactForm);
        if (_.isEmpty(channelContactForm.ein) && _.isEmpty(channelContactForm.firstName) && _.isEmpty(channelContactForm.lastName) && _.isEmpty(channelContactForm.email)) {
            return;
        }
        UIService.block();
        quoteService.getBTdirectoryService(channelContactForm.ein, channelContactForm.firstName, channelContactForm.lastName, channelContactForm.email).then(function (data, status) {
            console.log("data of BT directory : ", data);
            /*
             var title = "BT directory search result.";
             var msg = "";
             var templateURL = '/cqm/static/partials/templates/searcBTDirectory.html';
             */
            var sbtdData = [];
            if (data.customers != null && data.customers.length == undefined) {
                $scope.channelContactsGridData = [data.customers];
                $scope.showBTDirectorygrid = true;

            } else if (data.customers != null) {
                $scope.channelContactsGridData = data.customers;
                $scope.showBTDirectorygrid = true;

            } else {
                $scope.showBTDirectorygrid = false;
            }

            window.setTimeout(function () {
                $(window).resize();
            }, 10);
            UIService.unblock();
        });


    };

    $scope.getBranchSite = function () {

        console.log('Inside Get Branch Site - Quote Controller');
        $scope.addressSearchResultData = {};
        $scope.branchSiteList = [];
        UIService.block();
        branchSiteService.getBranchSiteNamesIds($scope.salesUser.ein, $scope.salesChannel.name, $scope.customer.cusId, function (data, status) {
            if (status == '200') {
                $scope.branchSite = data;
                var branchSiteListTemp = [];
                if (data.length == undefined) {
                    branchSiteListTemp = [data];
                    $scope.numOfMatchingBranchSites = 1;
                }
                else {
                    branchSiteListTemp = data;
                    $scope.numOfMatchingBranchSites = branchSiteListTemp.length;
                }
                $scope.branchSiteList = branchSiteListTemp;
                window.setTimeout(function () {
                    $(window).resize();
                }, 1);
                UIService.unblock();
            } else if (status == '404') {
                //Do Nothing
                UIService.unblock();
            } else {
                UIService.unblock();
                UIService.handleException('Branch Search', data, status);
            }
        });
    };
    $scope.updateBranchSiteId = function () {
        if (!_.isEmpty($scope.quoteSearchFieldsForm.siteName)) {
            $scope.siteId = JSON.parse($scope.quoteSearchFieldsForm.siteName).siteId;
            $scope.siteName = JSON.parse($scope.quoteSearchFieldsForm.siteName).name;
            $scope.getQuotesWithSiteDetails();
        }
    };
    $scope.updateBranchSiteName = function () {
        if (!_.isEmpty($scope.quoteSearchFieldsForm.siteId)) {
        $scope.siteName  = JSON.parse($scope.quoteSearchFieldsForm.siteId).name;
        $scope.siteId  = JSON.parse($scope.quoteSearchFieldsForm.siteId).siteId;
        $scope.getQuotesWithSiteDetails();
        }
    };

    $scope.showSiteFilter=function(){
        if($scope.quoteSearchFieldsForm.siteFilter.value=='siteName')
        {
            $scope.siteNameSelected =true;
            $scope.siteIdSelected =false;

        }else if(_.isEmpty($scope.quoteSearchFieldsForm.siteFilter.value)){
            $scope.siteNameSelected =false;
            $scope.siteIdSelected =false;
            $scope.quoteSearchFieldsForm.siteName='';
            $scope.quoteSearchFieldsForm.siteId='';
        }else
        {
            $scope.siteNameSelected =false;
            $scope.siteIdSelected =true;
        }

    };


    $scope.getQuotesWithSiteDetails = function () {
        console.log('Inside get quotes');
        UIService.block();
        var startTime = new Date().getTime();
        quoteService.getQuotes($scope.salesChannel.name, $scope.customer.cusId,$scope.siteId).then(function (data, status) {
            $scope.isCustomerSelected = true;
            $scope.numOfQuotes = 0;
            $scope.noQuotesFound = false;
            $scope.isContactSelected = false;
            $scope.hasContacts = false;
            $scope.isQuoteSelected   =false;
            var quoteList = [];
            var quotesList = data;
            //$scope.getBranchSite();
             UIService.unblock();
            if (angular.isArray(data)) {
                if (quotesList.length == undefined) {
                    $scope.numOfQuotes = 0;
                    $scope.noQuotesFound = true;
                    $scope.listOfQuotes=[];
                    $scope.quoteFormData = {};
                    $scope.channelContactsGridData = [];
                    $scope.channelContactsData = [];
                    $scope.isContactSelected = false;
                    $scope.hasContacts = false;
                    $scope.isQuoteSelected   =false;
                } else {
                   // $scope.listOfQuotes = quotesList;
                    $scope.isQuoteSelected   =false;
                    $scope.isContactSelected = false;
                    $scope.hasContacts = false;

                    if ($scope.showSubGroupUser) {
                        $scope.listOfQuotes = [];
                        if (_.contains($scope.userSubGroupList, "ALL")) { //Need to show all Quotes.
                            $scope.listOfQuotes = quotesList;
                        }
                        else {
                            for (var i = 0; i < quotesList.length; i++) {
                                if (!_.isEmpty(quotesList[i].subGroup)) {
                                    if (_.contains($scope.userSubGroupList, quotesList[i].subGroup)) {
                                        $scope.listOfQuotes.push(quotesList[i]);
                                    }
                                }
                            }
                        }
                    }
                    else {
                        if (_.contains($scope.userSubGroupList, "ALL")) { //Need to show all Quotes.
                            $scope.listOfQuotes = quotesList;
                        }
                        else {
                        $scope.listOfQuotes = [];
                        for (var i = 0; i < quotesList.length; i++) {
                            if (_.isEmpty(quotesList[i].subGroup)) {
                                $scope.listOfQuotes.push(quotesList[i]);
                            }
                        }
                        }
                    }

                    /*_.each($scope.listOfQuotes, function (aQuote) {
                        if (aQuote.quoteExpiryDate == '') {
                            aQuote.quoteExpiryDate = 'NIL';
                        }
                    })*/
                    $scope.numOfQuotes = ($scope.listOfQuotes).length;
                     if($scope.numOfQuotes==0)
                     {
                         $scope.noQuotesFound = true;
                         $scope.listOfQuotes=[];
                         $scope.quoteFormData = {};
                         $scope.channelContactsGridData = [];
                         $scope.channelContactsData = [];
                         $scope.isQuoteSelected   =false;
                         $scope.isContactSelected = false;
                         $scope.hasContacts = false;
                     }
                    $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.VALID);
                }


            }
            else {
                $scope.numOfQuotes = 0;
                $scope.noQuotesFound = true;
                $scope.listOfQuotes=[];
                $scope.quoteFormData = {};
                $scope.channelContactsGridData = [];
                $scope.channelContactsData = [];
                $scope.isQuoteSelected   =false;
                $scope.isContactSelected = false;
                $scope.hasContacts = false;

            }
        });

    };

    $scope.reset = function() {
        $scope.quoteFormData = {};
        $scope.channelContactsGridData = [];
        $scope.channelContactsData = [];
        $scope.isQuoteSelected   =false;
        $scope.isContactSelected = false;
        $scope.hasContacts = false;
        $scope.quoteActivityStatus = $scope.quoteActivityStatusList[0];
        $scope.quoteActivityFilter = $scope.quoteActivityFilterList[0];
        $scope.listOfQuotes=$scope.quotesCached;
        if(!_.isUndefined($scope.listOfQuotes) && !_.isEmpty($scope.listOfQuotes)){
            $scope.numOfQuotes = ($scope.listOfQuotes).length;
        }
        $scope.fiterText ='';
        $scope.quotesGrid.filterOptions.filterText ='';
        $scope.quoteSearchFieldsForm.siteName="";
        $scope.quoteSearchFieldsForm.siteId="";
        $scope.quoteSearchFieldsForm.siteFilter =  $scope.siteFilterList[0];
        $scope.showSiteFilter();

        if(!$rootScope.$$phase){
            $rootScope.$digest();
        }

    };

    $scope.loadChannelPartnerDetails = function () {

    }

}]);
//End of quotesController.
