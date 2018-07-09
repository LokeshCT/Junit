var module = angular.module('cqm.controllers');

// Controller for ChannelHierarchy
module.controller('quotePriceBookController', ['$scope', '$routeParams', 'UIService', '$modal', 'channelHierarchyService', 'PageContext', 'UserContext', '$rootScope', 'billingAccountService' , 'contractService',
    function ($scope, $routeParams, UIService, $modal, channelHierarchyService, PageContext, UserContext, $rootScope, billingAccountService, contractService) {
        console.log('Inside indirectCustomerController');

        if (PageContext.exist()) {
            $scope.selectedSalesChannel = PageContext.getSalesChannel();
            $scope.customer = PageContext.getCustomer();
            $scope.contract = PageContext.getContract();
        }
        $scope.quotePriceBookFormData = {};
        $scope.$parent.quotePriceBooks = [];
        $scope.selectedQuoteTrackId = -1;
        $scope.disableAddButton =true;
        $scope.disableDeleteButton =true;
        //  $scope.isContractResign=false;
        $scope.priceBookId = "";
        $scope.priceBookName = "";
        $scope.orderSubmitFlag = "";
        $scope.ptpVersionGrid = "";
        $scope.rrpVersionGrid = "";
        if (UserContext.exist()) {
            $scope.salesUser = UserContext.getUser();
            if (UserContext.getUser().userType != 'Direct') {
                //  $scope.isContractResign=false;
            }
        }

        $scope.tradeLevelEntities =
        [
            { name:'Customer', value:'Customer'},
            { name:'Channel Partner', value:'Channel Partner'}
        ];


        $scope.productNameList = [];
        $scope.populateProductNames = function () {
            $scope.$parent.quotePriceBooks = [];
            $scope.disableDeleteButton = true;
            $scope.disableAddButton = true;
            $scope.quotePriceBookFormData.ptpVersion = "";
            $scope.quotePriceBookFormData.rrpVersion = "";
            if ($scope.quotePriceBookFormData.tradeLevelEntity.name == 'Customer') {
                UIService.block();
                channelHierarchyService.getPriceBookDetails($scope.customer.cusId, function (responseData, status) {
                    if (status == '200') {
                        $scope.productNameList = [];
                        var priceBookList = responseData;
                        if (priceBookList == undefined) {
                            return;
                        }
                        if (priceBookList.length == undefined) {
                            $scope.priceBookDetails = [priceBookList];
                        } else {
                            $scope.priceBookDetails = priceBookList;
                        }
                        //  for(i=0;i< $scope.priceBookDetails.length;i++)
                        //    $scope.productNameList.push($scope.priceBookDetails[i].productName)
                        $scope.productNameList = $scope.priceBookDetails;
                        $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.VALID);

                    } else if (status == '404') {
                    }
                    else {
                        UIService.handleException('Price Book', responseData, status);
                    }
                    UIService.unblock();

                });
            }
            else {
                UIService.block();
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

        //PRICE BOOK -START

        $scope.loadPriceBookDetails = function () {
            if ($scope.customer != undefined && $scope.customer != null) {
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
                $scope.loadChannelPartnerDetails();
            }
        }

        $scope.priceBookGrid = {data:'$parent.quotePriceBooks', selectedItems:$scope.selectedPriceBook,
            multiSelect:false,
            enableColumnResize:true,
            showGroupPanel:true,
            showColumnMenu:true,
            showFilter:true,

            afterSelectionChange:function (item, event) {
                if (item.selected == false) {
                    console.log('got de-select event');
                    $scope.disableDeleteButton =true;
                    return;
                }
                else {
                    $scope.selectedQuoteTrackId = item.entity.scode;
                    $scope.priceBookName = item.entity.productName;
                    $scope.orderSubmitFlag = item.entity.orderSubmitFlag;
                    $scope.ptpVersionGrid = item.entity.ptpVersion;
                    $scope.rrpVersionGrid = item.entity.rrpVersion;
                    $scope.disableDeleteButton =false;
                }
            },
            columnDefs:[
                {field:'tradeLevel', displayName:'Trade Level', width:"*"},
                {field:'productName', displayName:'Product Name', width:"*"},
                {field:'rrpPriceBook', displayName:'RRP Price Book', width:"*"},
                {field:'ptpPriceBook', displayName:'PTP Price Book', width:"*"}

            ]
        };

        $scope.getProductVersions = function () {
            UIService.block();
            channelHierarchyService.getProductVersions($scope.selectedSalesChannel.name, $scope.customer.cusId, $scope.quotePriceBookFormData.productName.productName, function (responseData, status) {
                if (status == '200') {
                    //var result = responseData.split("#")
                    $scope.quotePriceBookFormData.rrpVersion = responseData.EUPVersion;
                    if (responseData.PTPversion != undefined && responseData.PTPversion != null && responseData.PTPversion != 'null') {
                        $scope.quotePriceBookFormData.ptpVersion = responseData.PTPversion;
                    }
                    if (responseData.PTPversion == null || responseData.PTPversion == 'null') {
                        $scope.quotePriceBookFormData.ptpVersion = "";
                    }
                    if(_.isEmpty($scope.quotePriceBookFormData.rrpVersion )||_.isEmpty($scope.quotePriceBookFormData.ptpVersion))
                    {
                         $scope.disableAddButton=true;
                    }
                    else
                    {
                    $scope.disableAddButtonCheck();
                    }

                } else {
                    $scope.quotePriceBookFormData.rrpVersion = "";
                    $scope.quotePriceBookFormData.ptpVersion = "";
                    UIService.openDialogBox("Price Book ", "No Price Book Found ", true, false);
                }

                UIService.unblock();
            });
        }

        $scope.loadChannelPartnerDetails = function () {
            if ($scope.customer != undefined && $scope.customer != null) {
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

                                }
                                if (responseData.tradeLevel != 'undefined') {
                                    $scope.quotePriceBookFormData.tradeLevel = responseData.tradeLevel;
                                }

                                $scope.yCRRO = true;
                                $scope.tLevelRO = true;
                                $scope.enableCreateChannel = false;
                            }
                        }
                        UIService.unblock();
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


        $scope.saveQuotePriceBook = function () {

            console.log(" Saving Quote Price Book .......");
            UIService.block();
            var customerName, productName , rrpVersion, ptpVersion;
            $scope.productName = $scope.quotePriceBookFormData.productName.productName;
            $scope.rrpVersion = $scope.quotePriceBookFormData.rrpVersion;
            $scope.ptpVersion = $scope.quotePriceBookFormData.ptpVersion;

            var salesChannelId = $scope.selectedSalesChannel.name;
            channelHierarchyService.getPriceBookCodes(salesChannelId, $scope.customer.cusId, $scope.customer.cusName, $scope.productName, $scope.rrpVersion, $scope.ptpVersion,

                                                      function (data, status) {
                                                          var title = 'Price Book';
                                                          if (status == 200) {
                                                              var priceBook = {
                                                                  tradeLevelEntity:$scope.quotePriceBookFormData.tradeLevelEntity.name,
                                                                  tradeLevel:$scope.quotePriceBookFormData.tradeLevel,
                                                                  productName:$scope.productName,
                                                                  rrpPriceBook:$scope.rrpVersion,
                                                                  ptpPriceBook:$scope.ptpVersion,
                                                                  scode:data.productScode,
                                                                  hcode:data.pmfcategoryID
                                                                  //  quoteTrackId:$scope.quoteTrackId
                                                              }
                                                              $scope.$parent.quotePriceBooks.push(priceBook);
                                                              $scope.disableAddButton = true;
                                                              UIService.unblock();
                                                          } else if (status == 409) {
                                                              //     UIService.openDialogBox(title, 'Product you are trying to add is already available.', true, false);
                                                              UIService.unblock();
                                                          } else {
                                                              UIService.handleException(title, data, status);
                                                          }

                                                      });


        };

        $scope.deleteQuotePriceBook = function () {
            var priceBookCount = $scope.$parent.quotePriceBooks.length;
            for (var i = 0; i < priceBookCount; i++) {
                var priceBook = $scope.$parent.quotePriceBooks[i];
                if ($scope.selectedQuoteTrackId == priceBook.scode) {

                    $scope.$parent.quotePriceBooks.splice(i, 1);
                    $scope.disableDeleteButton = true;
                   if($scope.quotePriceBookFormData.productName.productName == priceBook.productName)
                       $scope.disableAddButton = false;
                    break;
                }

            }
        };

        $scope.disableAddButtonCheck = function () {
            var priceBookCount = $scope.$parent.quotePriceBooks.length;
            $scope.disableAddButton = false;
            for (var i = 0; i < priceBookCount; i++) {
                var priceBook = $scope.$parent.quotePriceBooks[i];
                if ($scope.quotePriceBookFormData.productName.productName == priceBook.productName) {
                    $scope.disableAddButton = true;
                }

            }
        };

    }]);
