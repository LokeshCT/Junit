var module = angular.module('cqm.controllers');

module.controller('MatchingCustomerController',function ($scope, customerService, $http, UIService, UrlConfiguration, SessionContext, PageContext, contractService, $rootScope,$timeout) {

    $scope.title = 'Similar Customers';
    $scope.isCustomerSelected = false;
    $scope.initSelectedCustomerDialog = function () {

        if ($scope.customerExistByName) {
            $scope.title = 'Existing Customer';
            //$scope.custRadioBtnSelected = true;
        } else {
            $scope.title = 'Similar Customers';
            //$scope.custRadioBtnSelected = false;
        };

        $('#chosenCustomer').prop('checked', false);

        $scope.mySelections = [];
        $scope.selectedCustContacts = [];
        $scope.showCustomerContacts = false;
        $scope.showCustCustomersLabel = false;
        $scope.associateCustomerFlag = false;
        $scope.isCustomerSelected = false;
        window.setTimeout(function () {
            $(window).resize();
        }, 10);
    };

    $scope.disableCustSelection = false;
    $scope.gridPagingOptions = { pageSizes:[10, 50, 100, 500],
        pageSize:10,
        currentPage:1,
        totalServerItems:$scope.totalServerItems };


    $scope.matchingCustomersGrid = { data:'matchingCustomersList', multiSelect:false, keepLastSelected:false, selectedItems:$scope.mySelections,
        enablePaging:true,
        showFooter:true,
        width:'*',
        pagingOptions:$scope.gridPagingOptions,
        totalServerItems:'totalServerItems',
        footerTemplate:'cqm/static/cqm/web/partials/templates/ng-grid-footer.html',
        afterSelectionChange:function (item, event) {
            if (item.selected == false) {
                console.log('got de-select event');
                $scope.showCustomerContacts = false;
                $scope.showCustCustomersLabel = false;
                //$scope.isCustomerSelected = false;
                return;
            }
            //$scope.isCustomerSelected = true;
            //$scope.customerSelected(item.entity);
        },
        columnDefs:[
            {field:'\u2714',
                width:25,
                sortable:false,
                resizable:false,
                groupable:false,
                headerCellTemplate:'<div><div>',
                cellTemplate:'<div class="ngSelectionCell"><input tabindex="-1"  type="radio" name="chosenCustomer" id="chosenCustomer" ng-model="isCustomerSelected" ng-click="custSelectedFun(row)" value="custRadioBtnSelected"/></div>' },
            {field:'cusName', displayName:'Customer Name'}
        ] };

    $scope.$watch('gridPagingOptions', function (newValue, oldValue) {
        if (newValue !== oldValue || newValue.currentPage !== oldValue.currentPage) {
            $scope.getPageData($scope.gridPagingOptions.pageSize, $scope.gridPagingOptions.currentPage);
        }
    }, true);

    $scope.getPageData = function (pageSize, pageIndex) {
        var pageIndex = pageIndex;
        var pageSize = pageSize;
        setTimeout(function () {
            $scope.getSimilarCustomers($scope.customerName, pageIndex, pageSize, function (response) {
                var customerList = response.data;

                if (_.isUndefined(customerList)) {
                    return;
                } else {
                    $scope.matchingCustomersList = customerList;
                }

                if (!$scope.$$phase) {
                    $scope.$apply();
                }
                UIService.unblock();
            });

        }, 100);
    };

    $scope.getSimilarCustomers = function (customerName, pageIndex, pageSize, callback) {
        UIService.block();
        var qParams = {
            customerName:customerName,
            startIndex:pageIndex,
            pageSize:pageSize,
            sortColumn:null,
            sortOrder:null
        };
        $http.get(UrlConfiguration.similarCustomersListUri, {params:qParams}).then(callback, callback);
    };

    $scope.viewOrAssociateSelectedCustomer = function () {
        //$scope.display = false;

        if (!$scope.isCustomerSelected) {
            $scope.initSelectedCustomerDialog();
            if (!$scope.customerExistByName) {
                $rootScope.$broadcast('proceedToCreateCustomer');
            }
            return;
        }

        if ($scope.customerExistByName || $scope.associateCustomerFlag) {
            $scope.associateCustomer($scope.matchingSelectedCustomer, PageContext.getSalesChannel());
        } else {
            SessionContext.setCustomer($scope.matchingSelectedCustomer);
            PageContext.setContext(PageContext.getSalesChannel(), $scope.matchingSelectedCustomer, $scope.contract, SessionContext.get(SESSION.SelectedRole));
            SessionContext.navigateToTab('Customer');
        }
    }

    $scope.associateCustomer = function (customer, salesChannel) {
        var customerDTO = new Object();
        customerDTO.userId = SessionContext.getUser().ein;
        customerDTO.userName = SessionContext.getUser().name;
        customerDTO.cusName = customer.cusName;
        customerDTO.cusId = customer.cusId;
        customerDTO.cusReference = customer.cusReference;
        customerDTO.salesChannel = salesChannel.name;

        console.log('customerDTO :', customerDTO);

        UIService.block();

        function processCustomerAssociationResponse(response) {
            var status = response.status;
            var data = response.data;
            var title = 'Customer Association';
            var msg = '';
            if (status == 200) {
                var customer = {};
                customer.cusName = data.customerName;
                customer.contractDTO = data.contractDTO;
                customer.cusId = data.customerId;
                PageContext.setContext(salesChannel, customer, customer.contractDTO, SessionContext.get(SESSION.SelectedRole));
                $scope.display = false;

                msg = "Customer " + customerDTO.cusName + " is successfully associated with sales channel: " + customerDTO.salesChannel + ".";
                var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                dialogInstance.result.then(function () {
                    customerService.broadcastCustomerSelectionChanged(customerDTO.selectedSalesChannel, customer.cusId);
                    $timeout(function(){$rootScope.$broadcast(EVENT.CustomerLoaded)});
                    SessionContext.navigateToTab('Customer');
                }, function () {
                });
            } else {
                msg = " Failed to associate customer:  " + customerDTO.cusName + " with sales channel: " + customerDTO.salesChannel + ".";
                UIService.openDialogBox(title, msg, true, false);
            }

            UIService.unblock();
        }

        $http.post(UrlConfiguration.associateCustomerUri, customerDTO).then(processCustomerAssociationResponse, processCustomerAssociationResponse);
    };

    $scope.custSelectedFun = function (item) {
        $scope.isCustomerSelected = true;
        $scope.customerSelected(item.entity);
    };

    $scope.customerSelected = function (selectedCustomer) {
        $scope.matchingSelectedCustomer = selectedCustomer;
        var selectedCustomerId = selectedCustomer.cusId;
        UIService.block();

        function processChannelContactResponse(response) {
            var data = response.data;
            var status = response.status;

            if (status == 200) {
                $scope.numOfCustomerContacts = 0;
                $scope.showCustCustomersLabel = true;
                console.log('data : ', data);
                var customerContactList = data;
                console.log('Response from getSelectedCustomerContacts : ', customerContactList);
                UIService.unblock();
                if (customerContactList == undefined) {
                    $scope.showCustomerContacts = false;
                    $scope.disableUseSelection = false;
                    return;
                } else {
                    $scope.selectedCustContacts = customerContactList;
                    $scope.disableUseSelection = false;
                    $scope.showCustomerContacts = true;
                    $scope.numOfCustomerContacts = customerContactList.length;
                }
            }
            getContracts()
        }

        function getContracts() {
            contractService.getContracts(PageContext.getSalesChannel().name, $scope.matchingSelectedCustomer.cusId, function (data, status) {
                if (status == '200' && !_.isUndefined(data)) {
                    $scope.contractList = data;
                    if (!_.isUndefined($scope.contractList)) {
                        _.forEach($scope.contractList, function (contract) {
                            if (contract.portDistributor.orgName == PageContext.getSalesChannel().name) {
                                $scope.contract = contract;
                                $scope.associateCustomerFlag = false;
                                return;
                            }
                        });
                        _.forEach($scope.contractList, function (contract) {
                            if (contract.managedSolutionFlag == 'Y' || contract.managedSolutionFlag == 'y') {
                                var title = 'Managed Contract';
                                var msg = "Selected customer is associated to a managed contract. Do you want to associate the customer to the selected sales channel?";
                                var dialogInstance = UIService.openDialogBox(title, msg, true, true);
                                dialogInstance.result.then(function () {
                                    $scope.associateCustomerFlag = true;
                                }, function () {
                                    $scope.display = false;
                                });
                            }
                        });
                    }
                } else if (status == '404') {
                    $scope.associateCustomerFlag = true;
                } else {
                    var title = 'Contract';
                    var msg = "No contract found for customer " + $scope.matchingSelectedCustomer.cusName + ". Please contact support team.";
                    var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                    dialogInstance.result.then(function () {
                        $scope.display = false;
                    }, function () {
                    });
                    //$scope.associateCustomer($scope.matchingSelectedCustomer, PageContext.getSalesChannel());
                }
                UIService.unblock();
            });
        }

        var qParams = {
            customerId:selectedCustomerId
        };
        $http.get(UrlConfiguration.getCustomerContactsUri, {params:qParams}).then(processChannelContactResponse, processChannelContactResponse);
    };

    $scope.selectedCustomerContactGrid = { data:'selectedCustContacts', enableRowSelection:false,
        columnDefs:[
            {field:'contact.firstName', displayName:'First Name'},
            {field:'contact.lastName', displayName:'Last Name'},
            {field:'contact.jobTitle', displayName:'Job Title'},
            {field:'contact.email', displayName:'EMail'},
            {field:'contact.phoneNumber', displayName:'Telephone'},
            {field:'contact.mobileNumber', displayName:'Mobile'}
        ]
    };
});//end of create customer controller
