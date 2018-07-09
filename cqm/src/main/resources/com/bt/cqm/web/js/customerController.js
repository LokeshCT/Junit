var module = angular.module('cqm.controllers');

module.controller('SelectCustomerController', ['$scope', '$rootScope', 'UIService', 'salesChannelService', 'contractService', 'PageContext', 'UserContext', 'SessionContext', 'WebMetrics', 'customerService', 'Utility','$timeout', '$location', function ($scope, $rootScope, UIService, salesChannelService, contractService, PageContext, UserContext, SessionContext, WebMetrics, customerService, Utility,$timeout, $location) {
    console.log('Inside SelectCustomerController');

    $scope.selectedSalesChannel = undefined;
    $scope.selectedDslSalesChannel = undefined;

    $scope.selectExistingCustomer = true;
    $scope.disableSelectCustomer = true;
    $scope.showAssociateSalesChannel = false;
    $scope.hasAssociateSalesChannel = false;
    $scope.associatedSalesChannel = '';
    $scope.toggleSelectCreateCustomer = function () {
        $scope.selectExistingCustomer = !$scope.selectExistingCustomer;
    }

    $scope.$on(EVENT.LoadedSalesUser, function () {
        $scope.salesUser = UserContext.getUser();

        $scope.selectedDslSalesChannel = _.find($scope.salesUser.salesChannelList, function (channel) {
            if (channel.name == 'BT FRANCE') {
                return channel;
            }
        });
    });

    function tab(tab) {
        document.getElementById('tab').style.display = 'none';
        document.getElementById('tab2').style.display = 'none';
        document.getElementById('li_tab1').setAttribute('class', '');
        document.getElementById('li_tab2').setAttribute('class', '');
        document.getElementById(tab).style.display = 'block';
        document.getElementById('li_' + tab).setAttribute('class', 'active');
    }

    if (PageContext.exist()) {
        $scope.salesUser = UserContext.getUser();
        $scope.selectedSalesChannel = PageContext.getActualSalesChannel();
        $scope.customerList = PageContext.getSalesChannel().customerList;
        $scope.disableSelectCustomer = false;
        $scope.customer = JSON.stringify(PageContext.getCustomer());
        $scope.contractList = PageContext.getCustomer().contractList;
        $scope.contract = PageContext.getContract();
        $scope.role = PageContext.getSelectedRole();

        $scope.selectedDslSalesChannel = _.find($scope.salesUser.salesChannelList, function (channel) {
            if (channel.name == 'BT FRANCE') {
                return channel;
            }
        });
    } else {
        $scope.salesUser = SessionContext.getUser();
        $scope.customerList = [];
        $scope.contractList = [];

        $scope.selectedDslSalesChannel = _.find($scope.salesUser.salesChannelList, function (channel) {
            if (channel.name == 'BT FRANCE') {
                return channel;
            }
        });
    }

    $scope.onPageLoad = function(){
        $scope.selectedDslSalesChannel = _.find($scope.salesUser.salesChannelList, function (channel) {
            if (channel.name == 'BT FRANCE') {
                return channel;
            }
        });
    }


    function resetCustomer() {
        $scope.customer = undefined;
        $scope.customerList = [];
    }

    function resetContract() {
        $scope.hasAssociateSalesChannel = false;
        $scope.showAssociateSalesChannel = false;
        $scope.contract = undefined;
        $scope.contractList = [];

        if (!$rootScope.$$phase) {
            $rootScope.$digest();
        }

    }

    function loadCustomers(salesChannel) {
        UIService.block();
        var startTime = new Date().getTime();
        salesChannelService.getCustomers(salesChannel.name).then(function (data) {
            if (!_.isUndefined(data)) {
                $scope.customerList = data;
                salesChannelService.cacheCustomers(data);
                salesChannel.customerList = data;

                if (!(_.isUndefined($scope.customerList) || _.isNull($scope.customerList)) && $scope.customerList.length == 1) {
                    $scope.customer = JSON.stringify($scope.customerList[0]);
                    $scope.customerChange($scope.customer);
                }
                UIService.unblock();
                WebMetrics.captureWebMetrics('CQM Main Page - Select Sales Channel', startTime);
            } else {
                UIService.unblock();
            }

        });
    }

    $scope.salesChannelChange = function (salesChannel) {
        resetCustomer();
        resetContract();
        if (!_.isUndefined(salesChannel)) {
            if (_.isString(salesChannel)) {
                loadCustomers(JSON.parse(salesChannel));
            } else {
                loadCustomers(salesChannel);
            }
        }
        $scope.disableSelectCustomer = false;
    };

    $scope.customerChange = function (customer) {
        resetContract();
        if (_.isUndefined(customer)) {
            return;
        }
        UIService.block();
        var startTime = new Date().getTime();
        if (_.isString(customer)) {
            customer = JSON.parse(customer);
        }

        var salesChannel = undefined;
        if (_.isString($scope.selectedSalesChannel)) {
            salesChannel = JSON.parse($scope.selectedSalesChannel);
        } else {
            salesChannel = $scope.selectedSalesChannel;
        }
        contractService.getContracts(salesChannel.name, customer.cusId, function (data, status) {
            if (status == '200' && !_.isUndefined(data)) {
                $scope.contractList = data;
                if ($scope.contractList.length == 1) {
                    $scope.contract = $scope.contractList[0];
                }
            }
            WebMetrics.captureWebMetrics('CQM Main Page - Select Customer', startTime);
            UIService.unblock();
        });
    };

    function valid() {
        return !_.isUndefined($scope.selectedSalesChannel) &&
               !_.isUndefined($scope.customer) &&
               ( $scope.contractList.length == 0 || !_.isUndefined($scope.contract)) && ($scope.hasConGfrCode() || $scope.hasAssociateSalesChannel );
    }

    $scope.hasConGfrCode = function () {
        var channel = _.isUndefined($scope.selectedSalesChannel) ? undefined : _.isString($scope.selectedSalesChannel) ? JSON.parse($scope.selectedSalesChannel) : $scope.selectedSalesChannel;

        if ("MULTI NATIONAL CONTRACT" === channel.name.toUpperCase()) {
            if (Utility.isBlank($scope.contract.conGfrCode) || Utility.isBlank($scope.contract.conRoleId)) {
                $scope.showAssociateSalesChannel = true;
            } else {
                $scope.showAssociateSalesChannel = false;
            }

            return !$scope.showAssociateSalesChannel;
        } else {
            return true;
        }
    }

    $rootScope.$on('associatedSalesChannelEvent', function () {
        $scope.customerChange($scope.customer);
    })


    $scope.$watch('showAssociateSalesChannel', function (value) {
        console.log('Customer Controller showAssociateSalesChannel :' + value);
        if (!$rootScope.$$phase) {
            $rootScope.$digest();
        }
    })

    $scope.launchConfiguration = function (tabId) {
        if (valid()) {

            var channel = _.isUndefined($scope.selectedSalesChannel) ? undefined : _.isString($scope.selectedSalesChannel) ? JSON.parse($scope.selectedSalesChannel) : $scope.selectedSalesChannel;
            var customer = _.isUndefined($scope.customer) ? undefined : JSON.parse($scope.customer);
            var contract = $scope.contract;

            if (!_.isUndefined(channel)) {
                channel.customerList = $scope.customerList;
            }

            if (!_.isUndefined(customer)) {
                customer.contractList = $scope.contractList;
            }
            var mncChannel = '';
            if ("MULTI NATIONAL CONTRACT" === channel.name.toUpperCase() && !_.isUndefined(contract.conRoleId)) {
                customerService.getPortDistributor(contract.conRoleId, function (data, status) {
                    if ('200' == status) {
                        PageContext.setContext(channel, customer, contract, undefined, data.orgName);
                        customerService.updateCustomer(customer.cusId);
                        /* $scope.context.state = STATE.CustomerConfiguration;
                         $scope.context.subState = tabId;*/
                    } else {
                        UIService.handleException(undefined, data, status);
                    }
                    $timeout(function(){$rootScope.$broadcast(EVENT.CustomerLoaded)});

                })
                UserContext.setReqHeader("IS_MNC", "Y");
            } else {
                PageContext.setContext(channel, customer, contract);
                customerService.updateCustomer(customer.cusId);
                /*$scope.context.state = STATE.CustomerConfiguration;
                 $scope.context.subState = tabId;*/
                UserContext.setReqHeader("IS_MNC", "N");
                $timeout(function(){$rootScope.$broadcast(EVENT.CustomerLoaded)});
            }

            $scope.context.state = STATE.CustomerConfiguration;
            $scope.context.subState = tabId;
            SessionContext.setState(STATE.CustomerConfiguration);
            SessionContext.navigateToTab(tabId);
            UserContext.setReqHeader("CONTRACT_ID", contract.id);
        }
    };

    $scope.launchDslChecker = function () {
        //PageContext.setContext(channel);
        UserContext.setReqHeader('SALES_CHANNEL',$scope.selectedDslSalesChannel.name);
        SessionContext.navigateToDsl('allReports');
    }

    $scope.$watch('selectedDslSalesChannel', function () {
        if (_.isUndefined($scope.selectedDslSalesChannel) || (_.isEmpty($scope.selectedDslSalesChannel.name))) {
            $scope.disableDslChkBtn = true;
        } else {
            $scope.disableDslChkBtn = false;
        }

    });

    if ($scope.salesUser.salesChannelList.length == 1) {

        $scope.selectedSalesChannel = _.isString($scope.salesUser.salesChannelList[0]) ? $scope.salesUser.salesChannelList[0] : JSON.stringify($scope.salesUser.salesChannelList[0]);
        $scope.salesChannelChange($scope.selectedSalesChannel);
    }

    $scope.openDashboard = function() {
        $location.path('/dashboard');
        $scope.context.state = STATE.UserDashboard;
    };
}]);
