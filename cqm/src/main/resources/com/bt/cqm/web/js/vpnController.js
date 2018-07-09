var module = angular.module('cqm.controllers');

//Start of VPN Controller
module .controller('vpnController', ['$scope', '$routeParams', '$http', 'vpnService', '$location', '$modal', 'UIService','PageContext', 'UserContext', function ($scope, $routeParams, $http, vpnService, $location, $modal, UIService, PageContext, UserContext) {

    if( PageContext.exist() ) {
        $scope.customer = PageContext.getCustomer();
        $scope.customerList = PageContext.getSalesChannel().customerList;
        $scope.contract = PageContext.getContract();
        $scope.selectedSalesChannel = PageContext.getSalesChannel();
    }

    if( UserContext.exist() ) {
        $scope.salesUser = UserContext.getUser();
    }

    var selectedVPN;


    $scope.isReadonly = false;
    $scope.isVPNSelected =false;
    $scope.isCustomerSelected = false;
    var updateCustomerSelection = function (customer) {
        if ($scope.customer != undefined && $scope.customer != null) {
            $scope.isCustomerSelected = true;
            return;
        } else {
            $scope.isCustomerSelected = false;
            return;
        }
    };

    $scope.$on('customerChanged', function (event, salesChannel, salesUser, customer) {
        console.log('CustomerChanged event received will update the flags and VPN tab');
        updateCustomerSelection(customer);
        $scope.vpnDetailsSharedCustomer = [];
        $scope.selectedSharedCustomer =null;
        $scope.populateVPNDetails(customer.id);
    });


    $scope.viewSharedCustomerDetails = function () {
        $scope.isCustomerSelected = true;
        var customerId = $scope.selectedSharedCustomer.cusId;
        $scope.populateSharedVPNDetails(customerId);

    };


    $scope.loadVPNDetails = function () {
        console.log('inside loadVPNDetails');
        if ($scope.customer != undefined && $scope.customer != null) {
            $scope.populateVPNDetails($scope.customer.cusId);
        }
    };


    $scope.populateSharedVPNDetails = function (customerId) {
        UIService.block();
        $scope.vpnDetailsSharedCustomer = [];
        vpnService.getSharedVPNDetails(customerId, function (data, status) {
            UIService.unblock();
            if (status != '200') {
                $scope.noSharedVPNFound = true;
                $scope.vpnDetailsSharedCustomer = null;
                return;
            }
            else {
                $scope.isCustomerSelected = true;
                $scope.numOfVPNShared = 0;
                $scope.noSharedVPNFound = false;
                var vpnList = data;
                if(vpnList.length == undefined)
                  $scope.vpnDetailsSharedCustomer = [vpnList];
                else
                    $scope.vpnDetailsSharedCustomer = vpnList;
              //  $scope.numOfVPNShared = ($scope.vpnDetailsSharedCustomer).length;
                UIService.unblock();
            }
            UIService.unblock();

        });
    };

    $scope.populateVPNDetails = function (customerId) {

        UIService.block();
        vpnService.getVPNDetails(customerId, function (data, status) {

            if (status != '200') {
           /*     var msg = "No VPN details Found for the Customer"+customerId ;
                var title = 'VPN';

                var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                dialogInstance.result.then(function () {
                }, function () {
                });*/
                $scope.numOfVPN = 0;
                $scope.noVPNFound = true;
                $scope.vpnDetailsOwned = null;
                $scope.vpnDetailsShared = null;
                UIService.unblock();
                return;
            }
            else {

                $scope.isCustomerSelected = true;
                $scope.numOfVPN = 0;
                $scope.noVPNFound = false;
                var vpnList = data;
                $scope.vpnDetailsOwned = [];
                $scope.vpnDetailsShared = [];
                if (vpnList == undefined) {
                    $scope.numOfVPN = 0;
                    $scope.noVPNFound = true;
                    $scope.vpnDetailsOwned = null;
                    $scope.vpnDetailsShared = null;
                    UIService.unblock();
                    return;
                }

                if (vpnList.length == undefined) {
                    $scope.vpnDetails = [vpnList];
                } else {
                    $scope.vpnDetails = vpnList;
                }

                $scope.numOfVPN = ($scope.vpnDetails).length;
                var j = 0, k = 0;
                for (var i = 0; i < $scope.vpnDetails.length; i++) {
                    if ($scope.vpnDetails[i] != null && $scope.vpnDetails[i] != undefined && $scope.vpnDetails[i].vpnAccess == "OWNED") {
                        $scope.vpnDetailsOwned[j] = $scope.vpnDetails[i];
                        j++;

                    }
                    else {
                        $scope.vpnDetailsShared[k] = $scope.vpnDetails[i];
                        k++;
                    }
                }

                UIService.unblock();
            }

        });
    };
    /*
     $scope.$on('customerChanged', function (event, salesChannel, salesUser, customer) {
     console.log("quotes controller listened customerChanged event");
     var userId = salesUser.userId;
     var salesChannel = salesChannel;
     var customerId = customer.id;
     var customerName = customer.name;
     $scope.getQuotes();

     });*/

    $scope.vpnDetailsGrid = { data:'vpnDetailsOwned',
        multiSelect:false, enableColumnResize:true,
        afterSelectionChange:function (item, event) {
            if (item.selected == false) {
                console.log('got de-select event');
                return;
            }
            selectedVPN = item.entity;
            $scope.isVPNSelected =true;
        },
        columnDefs:[
            {field:'vpnServiceID', displayName:'VPN ID', width:120},
            {field:'vpnType', displayName:'VPN Type', width:120},
            {field:'vpnAccessType', displayName:'VPN Connectivity', width:180},
            {field:'vpnFriendlyName', displayName:'VPN Friendly Name', width:180},
            {field:'vpnSecondFriendlyName', displayName:'Extranet Name', width:120},
            {field:'vpnAccess', displayName:'VPN Access', width:120},
            {field:'customerID', displayName:'Customer ID', width:120},
            {field:'vpnID', displayName:' VNW_ID', width:120}
        ]
    };

    $scope.createSharedVPNDetail = function () {

        if (selectedVPN == undefined || selectedVPN == null) {
            console.log('VPN Not Selected');
            var dialogInstance = UIService.openDialogBox('VPN', 'Please Select VPN Owned', true, false);
            dialogInstance.result.then(function () {
            }, function () {
            });
            return;
        }

        if ($scope.selectedSharedCustomer == undefined || $scope.selectedSharedCustomer == null) {
            console.log('VPN Not Selected');
            var dialogInstance = UIService.openDialogBox('VPN', 'Please Select Shared Customer', true, false);
            dialogInstance.result.then(function () {
            }, function () {
            });
            return;
        }
        UIService.block();
        vpnService.createSharedVPNDetails(selectedVPN.vpnID, $scope.selectedSharedCustomer.cusId, $scope.selectedSharedCustomer, function (data, status) {
            var btns = [
                {result:'OK', label:'OK'}
            ];
            if (status == '200') {
                var title = 'VPN';


                var sqeUrl = data;

                var msg = "Shared VPN  with VPN Id: " + selectedVPN.vpnID + " for Customer " + $scope.selectedSharedCustomer.cusName + " Created Successfully!";
                var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                dialogInstance.result.then(function () {
                    $scope.populateSharedVPNDetails($scope.selectedSharedCustomer.cusId);
                }, function () {
                });

            } else {
                var msg = "Shared VPN Creation Failed! Reason:";
                var title = 'VPN';

                var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                dialogInstance.result.then(function () {
                }, function () {
                });
            }
            UIService.unblock();
        });
    };

    $scope.deleteSharedVPNDetail = function () {
        vpnService.deleteSharedVPNDetails(selectedSharedVPN.vpnID, $scope.selectedSharedCustomer.cusId);
    };
    $scope.vpnDetailsGridShared = { data:'vpnDetailsShared',
        multiSelect:false, enableColumnResize:true,
        columnDefs:[
            {field:'vpnServiceID', displayName:'VPN ID', width:120},
            {field:'vpnType', displayName:'VPN Type', width:120},
            {field:'vpnAccessType', displayName:'VPN Connectivity', width:180},
            {field:'vpnFriendlyName', displayName:'VPN Friendly Name', width:180},
            {field:'vpnSecondFriendlyName', displayName:'Extranet Name', width:120},
            {field:'vpnAccess', displayName:'VPN Access', width:120},
            {field:'customerID', displayName:'Customer ID', width:120},
            {field:'vpnID', displayName:' VNW_ID', width:120}
        ]
    };

    $scope.vpnDetailsGridSharedCustomers = { data:'vpnDetailsSharedCustomer',
        multiSelect:false, enableColumnResize:true,
        afterSelectionChange:function (item, event) {
            if (item.selected == false) {
                console.log('got de-select event');
                return;
            }
            selectedSharedVPN = item.entity;

        },
        columnDefs:[
            {field:'vpnServiceID', displayName:'VPN ID', width:120},
            {field:'vpnType', displayName:'VPN Type', width:120},
            {field:'vpnAccessType', displayName:'VPN Connectivity', width:180},
            {field:'vpnFriendlyName', displayName:'VPN Friendly Name', width:180},
            {field:'vpnSecondFriendlyName', displayName:'Extranet Name', width:120},
            {field:'vpnAccess', displayName:'VPN Access', width:120},
            {field:'customerID', displayName:'Customer ID', width:120},
            {field:'vpnID', displayName:' VNW_ID', width:120}
        ]
    };


}]);
    // added by Krishna K V for VPN Tab - END
    //End of VPN Controller.
