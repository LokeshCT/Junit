var module = angular.module('cqm.controllers');

module.controller('CreateCustomerController', function ($scope, customerService, $http, UIService, UrlConfiguration, PageContext, SessionContext, CreateCustomerResponseCode,UserContext,$timeout,$rootScope) {

    console.log('Inside CreateCustomerController');

    $scope.showMatchingDialog = false;
    $scope.isCustomerExistByName = false;

    $scope.matchingCustomersList = [];
    $scope.showCustCustomersLabel = false;
    $scope.selectedCustomerId = '';

    $scope.mncAssociatedSalesChannel = undefined;
    $scope.salesChannelsWithGfr = [];
    $scope.isMncChannel = false;
    $scope.cctToolTipMsg = 'Please enter value within the range of 0-240 ';

    $scope.validationStatus = [
        { name:'Unknown', value:'U'},
        { name:'Invalid', value:'N'},
        { name:'Valid', value:'V'}
    ];
    $scope.status = $scope.validationStatus[0].value;

    $scope.salesChannelChange = function (salesChannel) {
        var salesChannelObj =$scope.toSalesChannelObj(salesChannel);

        if ((!_.isUndefined(salesChannelObj)) && (!_.isUndefined(salesChannelObj.name)) && ("MULTI NATIONAL CONTRACT" === salesChannelObj.name.toUpperCase())) {
            $scope.isMncChannel = true;
            if (_.isEmpty($scope.salesChannelsWithGfr)) {
                UIService.block();
                customerService.getAllSalesChannelsWithGfrCode(function (data, status) {
                    if ('200' == status) {
                        $scope.salesChannelsWithGfr = data;
                    }
                    UIService.unblock();
                })
            }
        } else {
            $scope.isMncChannel = false;
        }

        PageContext.setContext(salesChannelObj);

    };

    function processFindCustomerResponse(response) {
        var responseData = response.data;
        var status = response.status;

        var customersList;

        if (responseData == undefined) {
            console.log('data is undefined: ');
            $scope.showSimilarCustomers = false;
            $scope.disableUseSelection = false;
            $scope.showCustomerContacts = false;
            $scope.showCustCustomersLabel = false;
            return;
        }

        var title = 'Customer Creation';
        var message = '';
        $scope.isCustomerExistByName = false;

        if (CreateCustomerResponseCode.CUSTOMER_ALREADY_EXISTS == responseData.responseCode) {
            $scope.totalServerItems = 1;
            if (responseData.data.customerAccountType == 'Direct') {
                $scope.matchingCustomersList = [responseData.data];
                $scope.showMatchingDialog = true;
                $scope.isCustomerExistByName = true;
            } else {
                message = "Customer with name: " + responseData.data.cusName + " already exists.";
                UIService.openDialogBox(title, message, true, false);
            }
        } else if (CreateCustomerResponseCode.SIMILAR_CUSTOMERS_FOUND == responseData.responseCode) {
            $scope.matchingCustomersList = _.filter(responseData.data, function (customer) {return customer.customerAccountType == 'Direct'});
            $scope.totalServerItems = responseData.totalDataCount;
            console.log('matching cust: ', $scope.matchingCustomersList);

            $scope.showMatchingDialog = true;
        } else if (CreateCustomerResponseCode.NO_MATCHING_OR_SIMILAR_CUSTOMERS == responseData.responseCode) {
            message = "No relevant customers found. Please modify the name and perform search or create customer.";
            message = "No customers with matching name: " + responseData.customerName + "! Do you want to create customer with name: " + responseData.customerName + "?";
            var dialogInstance = UIService.openDialogBox(title, message, true, true);
            dialogInstance.result.then(function () {
                $scope.createCustomer($scope.customerName, $scope.status, $scope.contractFriendlyName,$scope.contractCeaseTerm,$scope.linkedContractualCeaseTerm);
            }, function () {
            });

        }
        UIService.unblock();
    }

    $scope.findCustomer = function (selectedSalesChannel, customerName, callback) {
        var salesChannel = JSON.parse(selectedSalesChannel);
        var customerName = customerName;
        if (_.isUndefined(callback)) {
            callback = processFindCustomerResponse;
        }
        UIService.block();

        var qParams = {
            customerName:customerName,
            pageIndex:1,
            pageSize:10
        };
        $http.get(UrlConfiguration.findCustomerUri, {params:qParams}).then(callback, callback);
    };

    $scope.$on('proceedToCreateCustomer', function () {

        var dialogInstance = UIService.openDialogBox('Create Customer', 'Do you want to create customer with name: ' + $scope.customerName, true, true);
        dialogInstance.result.then(function () {
            $scope.createCustomer($scope.customerName, $scope.status,$scope.contractFriendlyName,$scope.contractCeaseTerm,$scope.linkedContractualCeaseTerm);
        }, function () {
        });
    });

    $scope.createCustomer = function (customerName, status, contractFriendlyName,contractCeaseTerm,linkedContractualCeaseTerm) {
        UIService.block();
        var salesChannelName;

        if($scope.isMncChannel){
            UserContext.setReqHeader("IS_MNC", "Y");
            salesChannelName = $scope.toSalesChannelObj($scope.mncAssociatedSalesChannel).salesChannelName.toUpperCase();
        }else{
            UserContext.setReqHeader("IS_MNC", "N");
            salesChannelName =$scope.toSalesChannelObj($scope.selectedSalesChannel).name;
        }

        customerService.createCustomer(salesChannelName, customerName, status, contractFriendlyName,contractCeaseTerm,linkedContractualCeaseTerm, function (data, status) {
            var title = 'Customer Creation';

            if (status == '200') {
                var customer = {};
                customer.cusName = customerName;
                customer.contractDTO = data.contractDTO;
                customer.cusId = data.cusId;
                console.log('Customer created with customer Id: ' + customer.cusId);
                if($scope.isMncChannel){
                    PageContext.setContext($scope.toSalesChannelObj($scope.selectedSalesChannel), customer, customer.contractDTO, undefined, salesChannelName);
                }else{
                    PageContext.setContext($scope.toSalesChannelObj($scope.selectedSalesChannel), customer, customer.contractDTO, SessionContext.get(SESSION.SelectedRole));

                }
                $scope.display = false;

                UIService.unblock();
                var msg = "Customer " + customerName + " is successfully created with customer ID:" + customer.cusId + ".";
                var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                dialogInstance.result.then(function () {
                    customerService.broadcastCustomerSelectionChanged(salesChannelName, customer.cusId);
                    $timeout(function(){$rootScope.$broadcast(EVENT.CustomerLoaded)});
                    SessionContext.navigateToTab('Customer');
                }, function () {
                });
            } else {
                UIService.handleException(title, data, status);
                UIService.unblock();
            }

        });

    };

    $scope.toSalesChannelObj = function(salesChannel){
        if(_.isString(salesChannel)){
           return JSON.parse(salesChannel);
        }else{
            return salesChannel
        }
    }

});
