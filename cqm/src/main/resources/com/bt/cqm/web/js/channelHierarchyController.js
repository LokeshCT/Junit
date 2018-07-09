var module = angular.module('cqm.controllers');

// Controller for ChannelHierarchy
module.controller('indirectCustomerController', ['$scope', '$routeParams', 'UIService', '$modal', 'channelHierarchyService', 'PageContext', 'UserContext', '$rootScope', 'billingAccountService' ,'contractService',
    function ($scope, $routeParams, UIService, $modal, channelHierarchyService, PageContext, UserContext, $rootScope,billingAccountService,contractService) {
        console.log('Inside indirectCustomerController');

        if (PageContext.exist()) {
            $scope.selectedSalesChannel = PageContext.getSalesChannel();
            $scope.customer = PageContext.getCustomer();
            $scope.contract = PageContext.getContract();
        }
        $scope.priceBookId="";
        $scope.priceBookName="";
        $scope.orderSubmitFlag ="" ;
        $scope.ptpVersionGrid ="" ;
        $scope.rrpVersionGrid ="" ;
        if (UserContext.exist()) {
            $scope.salesUser = UserContext.getUser();
        }


        var updateCustomerSelection = function (customer) {

            if ($scope.customer != undefined && $scope.customer != null) {
                $scope.isCustomerSelected = true;
                $scope.loadChannelForm();
            } else {
                $scope.isCustomerSelected = false;
            }
        };

        $scope.$on('customerChanged', function (event, salesChannel, salesUser, customer) {
            console.log('customerChanged event received will update the flags and Channel Hierarchy tab');
            updateCustomerSelection(customer);
            $scope.loadChannelPartnerDetails();
            $scope.loadPriceBookDetails();

        });

        $scope.loadChannelForm = function () {

            $scope.createCPFormData = {};
            $scope.priceBookFormData = {};
            $scope.parentAccounts = [];
            $scope.priceBookDetails = [];
            $scope.priceBookFormData.customerName = $scope.customer.cusName;

            var roles = [];
            var userRole = undefined;
            if (UserContext.exist()) {
                roles = UserContext.getUser().roles;
            }

            if (roles.length > 0) {
                userRole = roles[0];
            }

            if (userRole.roleName == 'CP User') {
                $scope.accountTypeList = [
                    { name:'CP-ROOT-CUSTOMER', value:'CP-ROOT-CUSTOMER'},
                    { name:'CP-SUB-CUSTOMER', value:'CP-SUB-CUSTOMER'}
                ];
            } else {
                $scope.accountTypeList = [
                    { name:'CP', value:'CP'},
                    { name:'SUB-CP', value:'SUB-CP'},
                    { name:'CP-ROOT-CUSTOMER', value:'CP-ROOT-CUSTOMER'},
                    { name:'CP-SUB-CUSTOMER', value:'CP-SUB-CUSTOMER'}
                ];
            }

            $scope.tradeLevels = [
                { name:'PLATINUM', value:'1'},
                { name:'GOLD', value:'2'},
                { name:'SILVER', value:'3'}
            ];

            $scope.pAccNameRO = false;
            $scope.pAccRequired = false;
            $scope.yCRRO = true;
            $scope.tLevelRO = true;
            $scope.enableCreateChannel = true;
            $scope.selectedPriceBook = [];
            $scope.accountTypeCP = "CP";
            $scope.showModifyPriceBook=false;
            $scope.enableUpdatePriceBook=false;
            $scope.selectedBillingAccountId="";
        }

        $scope.accountTypeSelected = function () {
            $scope.updateEditable();
            $scope.getParentAccountDetails();
        }

        $scope.updateEditable = function () {
            UIService.block();
            if ($scope.createCPFormData.accountType.name == $scope.accountTypeCP ) {
                $scope.createCPFormData.pAccName = "";
                $scope.createCPFormData.pACRef = "";
                $scope.createCPFormData.bAC = "";
                $scope.createCPFormData.tradeLevel = undefined;
                $scope.createCPFormData.tLevel = undefined;
                $scope.pAccNameRO = true;
                $scope.yCRRO = false;
                $scope.tLevelRO = false;
                $scope.createCPFormData.cType = "Indirect";
            } else {
                $scope.createCPFormData.yCR = undefined;
                $scope.pAccNameRO = false;
                $scope.pAccRequired = true;
                $scope.yCRRO = true;
                $scope.tLevelRO = true;
                $scope.createCPFormData.tLevel = "";
                $scope.createCPFormData.tradeLevel = "";
                $scope.createCPFormData.cType = "Indirect";
                $scope.createCPFormData.pACRef = "";
            }
            UIService.unblock();
        };

        $scope.productNameList = [];

        //PRICE BOOK -START

        $scope.loadPriceBookDetails = function () {
            $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.INVALID);

            if ($scope.customer != undefined && $scope.customer != null) {
                $scope.loadChannelForm();
                $scope.priceBookFormData.customerName = $scope.customer.cusName;
                UIService.block();
                channelHierarchyService.getPriceBookDetails($scope.customer.cusId, function (responseData, status) {
                    if (status == '200') {
                        var priceBookList = responseData;
                        if (priceBookList == undefined) {
                            return;
                        }
                        if (priceBookList.length == undefined) {
                            $scope.priceBookDetails = [priceBookList];
                        } else {
                            $scope.priceBookDetails = priceBookList;
                        }

                        $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.VALID);

                    } else if (status == '404') {
                    }
                    else {
                        UIService.handleException('Price Book', responseData, status);
                    }


                });
                // var salesChannelId = $scope.selectedSalesChannel.id;
                channelHierarchyService.getProductNames($scope.selectedSalesChannel.name, function (responseData, status) {
                    if (status == '200') {
                        $scope.productNameList = responseData;
                    } else {
                        UIService.handleException('Price Book', responseData, status);
                    }
                    UIService.unblock();
                });
            }
        }

        $scope.priceBookGrid = {data:'priceBookDetails', selectedItems:$scope.selectedPriceBook,
            multiSelect:false,
            enableColumnResize:true,
            showGroupPanel:true,
            showColumnMenu:true,
            showFilter:true,

            afterSelectionChange:function (item, event) {
                if (item.selected == false) {
                    console.log('got de-select event');
                    return;
                }
                else
                {


                    $scope.priceBookId =  item.entity.priceBookId;
                    $scope.priceBookName =  item.entity.productName;
                    $scope.orderSubmitFlag =  item.entity.orderSubmitFlag;
                    $scope.ptpVersionGrid =item.entity.PTPversion;
                    $scope.rrpVersionGrid =item.entity.EUPVersion;
                    if ($scope.orderSubmitFlag.toUpperCase() == "Y" || $scope.orderSubmitFlag.toUpperCase() == "YES") {
                        if ($scope.priceBookName == $scope.priceBookFormData.productName.productName) {
                            if ($scope.priceBookFormData.ptpVersion != "" && $scope.ptpVersionGrid != $scope.priceBookFormData.ptpVersion &&
                                $scope.priceBookFormData.rrpVersion != "" && $scope.rrpVersionGrid != $scope.priceBookFormData.rrpVersion)
                                $scope.enableUpdatePriceBook = true;
                }
                        else {
                            $scope.enableUpdatePriceBook = false;
                        }
                    }
                    else {
                        $scope.enableUpdatePriceBook = false;
                    }
                }
            },
            columnDefs:[
                {field:'productName', displayName:'Product Name', width:"*"},
                {field:'EUPVersion', displayName:'RRP Price Book', width:"*"},
                {field:'PTPversion', displayName:'PTP Price Book', width:"*"},
                {field:'orderSubmitFlag', displayName:'Order Submitted Flag', width:"*"}
            ]
        };

        //PRICE BOOK END
        $scope.getParentAccountDetails = function () {
            if ($scope.createCPFormData.accountType.name != $scope.accountTypeCP) {
                UIService.block();

                channelHierarchyService.getParentAccountNames($scope.createCPFormData.accountType.name, $scope.selectedSalesChannel.name).then(function (data) {
                    $scope.pAccNameList = data;
                    UIService.unblock();
                });
            }
        }

        $scope.getChannelCreationDetails = function () {
            UIService.block();
            var hierarchyData =[];
            channelHierarchyService.getChannelCreationDetails($scope.createCPFormData.pAccName.parentCustomerName, $scope.customer.cusId,function (responseData, status) {
                if(status== '200')
                {
                   hierarchyData = responseData;
                    contractService.getContracts( $scope.selectedSalesChannel.name, hierarchyData.parentAccountReference, function (data, statusContract) {
                        if (statusContract == '200' && !_.isUndefined(data)) {
                            $scope.contractListChannel = data;
                            if ($scope.contractListChannel.length == 1) {
                                $scope.contractChannel = $scope.contractListChannel[0];
                            }

                            billingAccountService.getBillingAccounts(hierarchyData.parentAccountReference, $scope.contractChannel.id, function (data, statusBilling) {
                                if (statusBilling == '200') {
                                    $scope.billingAccountList = data;
                                    $scope.createCPFormData.pACRef = hierarchyData.parentAccountReference;
                                     $scope.createCPFormData.bAC = undefined;
                                    if (hierarchyData.tradeLevel == 'PLATINUM')
                                        $scope.createCPFormData.tLevel = $scope.tradeLevels[0];
                                    else if (hierarchyData.tradeLevel == 'GOLD')
                                        $scope.createCPFormData.tLevel = $scope.tradeLevels[1];
                                    else if (hierarchyData.tradeLevel == 'SILVER')
                                        $scope.createCPFormData.tLevel = $scope.tradeLevels[2];
                                    UIService.unblock();
                                }
                                else
                                {
                                    $scope.billingAccountList = [];
                                    $scope.createCPFormData.bAC = undefined;
                                    $scope.createCPFormData.pACRef = hierarchyData.parentAccountReference;
                                    // $scope.createCPFormData.bAC = hierarchyData.billingAccount;
                                    if (hierarchyData.tradeLevel == 'PLATINUM')
                                        $scope.createCPFormData.tLevel = $scope.tradeLevels[0];
                                    else if (hierarchyData.tradeLevel == 'GOLD')
                                        $scope.createCPFormData.tLevel = $scope.tradeLevels[1];
                                    else if (hierarchyData.tradeLevel == 'SILVER')
                                        $scope.createCPFormData.tLevel = $scope.tradeLevels[2];
                                    UIService.unblock();
                                }
                            });
                        }
                        else
                        {
                            $scope.createCPFormData.pACRef = hierarchyData.parentAccountReference;
                            // $scope.createCPFormData.bAC = hierarchyData.billingAccount;
                            if (hierarchyData.tradeLevel == 'PLATINUM')
                                $scope.createCPFormData.tLevel = $scope.tradeLevels[0];
                            else if (hierarchyData.tradeLevel == 'GOLD')
                                $scope.createCPFormData.tLevel = $scope.tradeLevels[1];
                            else if (hierarchyData.tradeLevel == 'SILVER')
                                $scope.createCPFormData.tLevel = $scope.tradeLevels[2];
                            UIService.unblock();
                        }
                    });
                }
                else
                {
                    UIService.unblock();
                }

            });
        }


        $scope.getProductVersions = function () {
            UIService.block();
            channelHierarchyService.getProductVersions($scope.selectedSalesChannel.name, $scope.customer.cusId, $scope.priceBookFormData.productName.productName, function (responseData, status) {
                if (status == '200') {
                    //var result = responseData.split("#")
                    $scope.priceBookFormData.rrpVersion =responseData.EUPVersion;
                    if (responseData.PTPversion != undefined && responseData.PTPversion!= null && responseData.PTPversion != 'null') {
                        $scope.priceBookFormData.ptpVersion = responseData.PTPversion;
                    }
                    if (responseData.PTPversion == null || responseData.PTPversion== 'null') {
                        $scope.priceBookFormData.ptpVersion = "";
                    }
                    if ($scope.orderSubmitFlag.toUpperCase() == "Y" || $scope.orderSubmitFlag.toUpperCase() == "YES") {
                        if ($scope.priceBookName == $scope.priceBookFormData.productName.productName) {
                            if ($scope.priceBookFormData.ptpVersion != "" && $scope.ptpVersionGrid != $scope.priceBookFormData.ptpVersion &&
                                $scope.priceBookFormData.rrpVersion != "" && $scope.rrpVersionGrid != $scope.priceBookFormData.rrpVersion)
                                $scope.enableUpdatePriceBook = true;
                        }
                        else {
                            $scope.enableUpdatePriceBook = false;
                        }
                    }
                    else {
                        $scope.enableUpdatePriceBook = false;
                    }

                } else {
                    $scope.priceBookFormData.rrpVersion = "";
                    $scope.priceBookFormData.ptpVersion = "";
                    UIService.openDialogBox("Price Book ", "No Price Book Found ", true, false);
                }

                UIService.unblock();
            });
        }

        $scope.loadChannelPartnerDetails = function (customer) {
            $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.INVALID);
            if ($scope.customer != undefined && $scope.customer != null) {
                $scope.loadChannelForm();
                UIService.block();
                $scope.billingAccountList = [];
                channelHierarchyService.loadChannelPartnerDetails($scope.customer.cusId, function (responseData, status) {
                    var channelPartnerDetails = responseData;
                    if (status == '200') {
                        if (responseData != undefined) {
                            $scope.pAccNameRO = true;
                            $scope.pAccRequired = true;
                            if (responseData.accountType != undefined) {
                                if (responseData.accountType != "CP") {
                                    $scope.pAccNameList = [responseData["parentCustomerName"]];
                                    $scope.createCPFormData.pAccNameAvail = $scope.pAccNameList[0];
                                    $scope.createCPFormData.pACRef = responseData.parentAccountReference;
                                }
                                if (responseData.tradeLevel == 'PLATINUM')
                                    $scope.createCPFormData.tLevel = $scope.tradeLevels[0];
                                else if (responseData.tradeLevel == 'GOLD')
                                    $scope.createCPFormData.tLevel = $scope.tradeLevels[1];
                                else if (responseData.tradeLevel == 'SILVER')
                                    $scope.createCPFormData.tLevel = $scope.tradeLevels[2];
                                $scope.createCPFormData.bAC = [responseData.billingAccount];
                                $scope.createCPFormData.yCR = responseData.yearlyCommittedRevenue;
                                $scope.createCPFormData.cType = responseData.salesChannelType;
                                $scope.accountTypeList = [
                                    { name:responseData.accountType, value:responseData.accountType}
                                ];
                                $scope.createCPFormData.accountType = $scope.accountTypeList[0];
                                $scope.yCRRO = true;
                                $scope.tLevelRO = true;
                                $scope.enableCreateChannel = false;
                                $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.VALID);
                            }
                        }

                    } else {
                        $scope.enableCreateChannel = true;
                        $scope.yCRRO = true;
                        $scope.tLevelRO = true;
                        $scope.pAccNameRO = true;
                        $scope.pAccRequired = true;
                    }
                    UIService.unblock();

                });
            }
        }

        $scope.createChannelPartner = function () {
            console.log("Creating channel partner...");
            UIService.block();
            var pAccName, pACRef , cType, bAC  , yCR;
            if ($scope.createCPFormData.pAccName.parentCustomerName == undefined || $scope.createCPFormData.pAccName.parentCustomerName == "")
                $scope.pAccName = "null";
            else
                $scope.pAccName = $scope.createCPFormData.pAccName.parentCustomerName;
            if ($scope.createCPFormData.pACRef == undefined || $scope.createCPFormData.pACRef == "")
                $scope.pACRef = "null";
            else
                $scope.pACRef = $scope.createCPFormData.pACRef;
            if ($scope.createCPFormData.cType == undefined || $scope.createCPFormData.cType == "")
                $scope.cType = "null";
            else
                $scope.cType = $scope.createCPFormData.cType;
            if ($scope.selectedBillingAccountId == undefined || $scope.selectedBillingAccountId == "")
                $scope.bAC = "null";
            else
                $scope.bAC = $scope.selectedBillingAccountId;
            if ($scope.createCPFormData.yCR == undefined || $scope.createCPFormData.yCR == "")
                $scope.yCR = "null";
            else
                $scope.yCR = $scope.createCPFormData.yCR
            channelHierarchyService.createChannelPartner($scope.customer.cusId, $scope.createCPFormData.accountType.name,
                                                         $scope.pAccName,
                                                         $scope.pACRef,
                                                         $scope.bAC,
                                                         $scope.yCR,
                                                         $scope.cType,
                                                         $scope.createCPFormData.tLevel.name,
                                                         $scope.selectedSalesChannel.name,
                                                         $scope.customer.cusName,
                                                         function (data, status) {
                                                             var title = 'Channel Hierarchy';
                                                             var msg = 'Channel Partner Created Successfully!';
                                                             var btns = [
                                                                 {result:'OK', label:'OK'}
                                                             ];

                                                             if (status == 200) {
                                                                 // On Success
                                                                 UIService.unblock();
                                                                 $scope.enableCreateChannel = false;
                                                                 $scope.pAccNameRO = true;
                                                                 if ($scope.pAccName != null && $scope.pAccName != 'null' && $scope.pAccName != undefined) {
                                                                     $scope.createCPFormData.pAccNameAvail = $scope.pAccName;
                                                                     $scope.createCPFormData.pAccName = $scope.pAccName;
                                                                     $scope.createCPFormData.bAC=$scope.createCPFormData.bAC.accountFriendlyName;
                                                                 }
                                                                 else {
                                                                     $scope.createCPFormData.pAccNameAvail = "";
                                                                     $scope.createCPFormData.pAccName = "";
                                                                 }
                                                                 var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                                                                 dialogInstance.result.then(function () {
                                                                 }, function () {
                                                                 });

                                                                 //$scope.createCPFormData = {};

                                                             } else {
                                                                 // On failure

                                                                 UIService.unblock();
                                                                 msg = 'Channel Partner Creation Failed!';
                                                                 var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                                                                 dialogInstance.result.then(function () {
                                                                 }, function () {
                                                                 });
                                                             }

                                                         });

        };


        $scope.updateBillingAccount = function () {
            $scope.selectedBillingAccountId = $scope.createCPFormData.bAC.billingAccountId;
        };

        $scope.savePriceBook = function () {

            console.log(" Saving Price Book .......");
            UIService.block();
            var customerName, productName , rrpVersion, ptpVersion;
            $scope.productName = $scope.priceBookFormData.productName.productName;
            $scope.rrpVersion = $scope.priceBookFormData.rrpVersion;
            $scope.ptpVersion = $scope.priceBookFormData.ptpVersion;

            var salesChannelId = $scope.selectedSalesChannel.name;
            channelHierarchyService.savePriceBook(salesChannelId, $scope.customer.cusId, $scope.customer.cusName, $scope.productName, $scope.rrpVersion, $scope.ptpVersion,

                                                  function (data, status) {
                                                      var title = 'Price Book';
                                                      if (status == 200) {
                                                          // On Success
                                                          $scope.loadPriceBookDetails();

                                                          UIService.openDialogBox(title, 'Price Book Created Successfully!', true, false);
                                                          UIService.unblock();

                                                          $scope.priceBookFormData.productName = "";
                                                          $scope.priceBookFormData.rrpVersion = "";
                                                          $scope.priceBookFormData.ptpVersion = "";
                                                          $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.VALID);
                                                      } else if (status == 409) {
                                                          UIService.openDialogBox(title, 'Product you are trying to add is already available.', true, false);
                                                          UIService.unblock();
                                                      } else {
                                                          UIService.handleException(title, data, status);
                                                      }

                                                  });


        };


        $scope.monthlyCommittmentGrid = {data:'monthlyCommittmentDetails', selectedItems:$scope.selectedPriceBook,
            multiSelect:false,
            enableColumnResize:true,
            showGroupPanel:true,
            showColumnMenu:true,
            showFilter:true,

            afterSelectionChange:function (item, event) {
                if (item.selected == false) {
                    console.log('got de-select event');
                    return;
                }
            },
            columnDefs:[
                {field:'monCommRev', displayName:'Monthly Committed Revenue', width:"*"},
                {field:'commStartDate', displayName:'Commitment Start Date', width:"*"},
                {field:'commEndDate', displayName:'Commitment End Date', width:"*"},
                {field:'pbProductId', displayName:'PB Product ID', width:"*"},
                {field:'pbeCurrId', displayName:'PBE CURR ID', width:"*"},
                {field:'pbePbId', displayName:'PBE PB ID', width:"*"},
                {field:'pbeTriggerMonths', displayName:'PBE TRIGGER MONTHS', width:"*"},
                {field:'pbId', displayName:'PB_ID', width:"*"}
            ]
        };
        $scope.updatePriceBookDetails = function () {

            console.log(" Updating Price Book .......");
            UIService.block();
            var customerName, productName , rrpVersion, ptpVersion;
            $scope.productName = $scope.priceBookFormData.productName.productName;
            var salesChannelId = $scope.selectedSalesChannel.name;
            channelHierarchyService.updatePriceBook(salesChannelId,
                                                    $scope.customer.cusId,
                                                    $scope.customer.cusName,
                                                    $scope.productName,
                                                    $scope.priceBookFormData.rrpVersionSelected,
                                                    $scope.priceBookFormData.ptpVersionSelected,
                                                    $scope.priceBookFormData.triggerMonths,
                                                    $scope.priceBookFormData.committedRevenue,
                                                    $scope.priceBookFormData.priceBookId,
                                                    $scope.orderSubmitFlag,
                                                    $scope.priceBookFormData.productId,
                                                  function (data, status) {
                                                      var title = 'Price Book';
                                                      if (status == 200) {
                                                          // On Success
                                                          $scope.loadPriceBookDetails();

                                                          UIService.openDialogBox(title, 'Price Book Updated Successfully!', true, false);
                                                          UIService.unblock();
                                                          $scope.enableUpdatePriceBook=false;
                                                          $scope.priceBookFormData.productName = "";
                                                          $scope.priceBookFormData.rrpVersion = "";
                                                          $scope.priceBookFormData.ptpVersion = "";
                                                          $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.VALID);
                                                      } else if (status == 409) {
                                                          UIService.openDialogBox(title, 'Product you are trying to add is already available.', true, false);
                                                          UIService.unblock();
                                                      } else {
                                                          UIService.handleException(title, data, status);
                                                      }

                                                  });


        };

        $scope.getUpdatePriceBookDetails = function () {
            $scope.showModifyPriceBook=true;
            UIService.block();

            channelHierarchyService.getProductVersions($scope.selectedSalesChannel.name, $scope.customer.cusId, $scope.priceBookFormData.productName.productName, function (responseData, status) {
                if (status == '200') {
                    $scope.priceBookFormData.rrpVersionSelected =responseData.EUPVersion;
                    if (responseData.PTPversion != undefined && responseData.PTPversion  != null && responseData.PTPversion  != 'null') {
                        $scope.priceBookFormData.ptpVersionSelected =responseData.PTPversion;
                    }
                    else if (responseData.PTPversion  == null || responseData.PTPversion  == 'null') {
                        $scope.priceBookFormData.ptpVersionSelected = "";
                    }
                    $scope.priceBookFormData.productId =responseData.productID;
                } else {
                    $scope.priceBookFormData.rrpVersionSelected = "";
                    $scope.priceBookFormData.ptpVersionSelected = "";
                }
                UIService.unblock();
            });
            channelHierarchyService.getPriceBookExtension($scope.priceBookId, function (responseData, status) {
                UIService.block();
                var channelPartnerDetails = responseData;
                if (status == '200') {
                    if (responseData != undefined) {
                            $scope.priceBookFormData.priceBookId= $scope.priceBookId;
                            $scope.priceBookFormData.customerId  = $scope.customer.cusId;
                            $scope.priceBookFormData.triggerMonths   =  responseData.triggerMonths;
                            $scope.priceBookFormData.committedRevenue   =responseData.monthlyCommtRevenue;

                    }

                }
               UIService.unblock();

            });
        }
}]);
