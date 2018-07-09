var module = angular.module('cqm.controllers');

module.controller('orderDetailsController', ['$scope', '$http', '$timeout', 'UIService', 'orderService', 'quoteService', '$modal', '$upload', 'PageContext', 'UserContext','WebMetrics',
    function ($scope, $http, $timeout, UIService, orderService, quoteService, $modal, $upload, PageContext, UserContext,WebMetrics) {
        console.log('Inside order details controller');

        if (PageContext.exist()) {
            $scope.customer = PageContext.getCustomer();
            $scope.contract = PageContext.getContract();
            $scope.selectedSalesChannel = PageContext.getSalesChannel();
        }

        if (UserContext.exist()) {
            $scope.salesUser = UserContext.getUser();
            $scope.showSubGroupUser=UserContext.isSubGroupUser();
            $scope.userSubGroupList=UserContext.getUserSubGroups();
        }

        $scope.noOrdersFound = true;
        $scope.msg ="";
        $scope.searchOrderFormData = {};
        $scope.orderDetailFormData = {};
        $scope.selectedOrder = [ ];
        $scope.numOfMatchingOrders = 0;
        $scope.showLineItem = false;
        $scope.showSearchField = false;
        $scope.countOrderLineItems = 0;
        $scope.attachList = [
            {fileName:"", documentId:""}
        ];

        $scope.searchOrderFormData.refText = '';

        $scope.selectedQoute = {};
        $scope.userDetails = {};
        $scope.ksuModel = {};

        $scope.disableCancelOrderBut = true;
        $scope.disableLanunchCofigBut = true;
        $scope.disableReqCancelBut = true;
        $scope.disableIFCBut = true;
        $scope.disableViewKsuBut = true;

        $scope.gridSelected = false;

        $scope.filterList = [];
        $scope.initializeFilters = function () {
            $scope.filterList = [
                {name:"Customer Name", value:"customerName"},
                {name:"Expedio Reference", value:"expedioRef"},
                {name:"Order Reference", value:"orderRef"},
                {name:"", value:''}
            ];
        };


        $scope.attachList = [
            {name:"Service Delivery", value:"Service Delivery"},
            {name:"Sales", value:"Sales"}
        ];


        $scope.orderSearchResultData = {};
        $scope.selectedOrder = [];

        $scope.getOrders = function () {
            console.log('Inside search Orders');
            $scope.msg ="";
            if ($scope.customer == undefined || $scope.customer == null) {
                console.log('customer not selected');
                $scope.isCustomerSelected = false;
                return;
            }

            UIService.block();
            var startTime = new Date().getTime();
            orderService.searchOrders($scope.selectedSalesChannel.name, $scope.customer.cusId, function (data, status) {
                console.log("data: ", data);
                WebMetrics.captureWebMetrics('CQM Orders Tab - Load Order',startTime);
                UIService.unblock();
                $scope.isCustomerSelected = true;
                var ordersList = data;
                $scope.noOrdersFound = false;
                $scope.msg ="No Orders found."
                if (ordersList.length == undefined) {
                    $scope.noOrdersFound = true;
                    //  $scope.listOfOrders = [ordersList];
                    $scope.numOfOrders = 0;
                    $scope.listOfOrders=[];
                    UIService.unblock();
                    return;

                }
                else {
                    $scope.listOfOrders=[];
                    if ($scope.showSubGroupUser) {
                        if (_.contains($scope.userSubGroupList, "ALL")) { //Need to show all Quotes.
                            ordersList = data;
                            $scope.listOfOrders =ordersList;
                        }
                        else {
                            for (var i = 0; i < ordersList.length; i++) {
                                if (!_.isEmpty(ordersList[i].subGroup)) {
                                    if (_.contains($scope.userSubGroupList, ordersList[i].subGroup)) {
                                        $scope.listOfOrders.push(ordersList[i])
                                    }
                                }
                            }
                        }
                    }
                    else {
                        if (_.contains($scope.userSubGroupList, "ALL")) { //Need to show all Quotes.
                            ordersList = data;
                            $scope.listOfOrders = ordersList;
                        }
                        else {
                            for (var i = 0; i < ordersList.length; i++) {
                                if (_.isEmpty(ordersList[i].subGroup)) {
                                    $scope.listOfOrders.push(ordersList[i]);
                                }
                            }
                        }
                    }
                    $scope.numOfOrders = ($scope.listOfOrders).length;
                    UIService.unblock();
                }

                window.setTimeout(function () {
                    $(window).resize();
                }, 1);
            });
        };


        $scope.orderGrid = { data:'listOfOrders', selectedItems:$scope.selectedOrder,
            multiSelect:false,
            enableColumnResize:true,
            showGroupPanel:true,
            showColumnMenu:true,
            showFilter:true,
            sortInfo: {
                fields: ['orderID'],
                directions: ['dsc']
            },

            afterSelectionChange:function (item, event) {
                if (item.selected == false) {
                    console.log('got de-select event');
                    return;
                }
                $scope.gridSelected = false;
                $scope.listOfOrderLineItems = {};
                $scope.showLineItem = true;
                $scope.saveOrderInfo(item.entity);
                $scope.populateOrderLineItem(item.entity.orderID);
                $scope.populateQoute(item.entity);
                $scope.resetPanelFields();
                //$scope.updateKSUModel(item.entity);
                $scope.initAttachPanel();
            },
            columnDefs:[
                {field:'orderID', displayName:'Order ID', width:"*"},
                {field:'orderVersion', displayName:'Order Version', width:"*"},
                {field:'expedioReference', displayName:'Expedio Reference', width:"*"},
                {field:'quoteID', displayName:'Quote ID', width:"*"},
                {field:'customerName', displayName:'Customer Name', width:"*"},
                {field:'quoteVersion', displayName:'Quote Version', width:"*"},
                {field:'subGroup', displayName:'Sub Group', width:"*", visible:$scope.showSubGroupUser}
            ]
            //filterOptions:$scope.filterOptions

        };

        $scope.orderLineItemGrid = { data:'listOfOrderLineItems', selectedItems:$scope.selectedOrder,
            multiSelect:false,
            enableColumnResize:true,
            showGroupPanel:true,
            showColumnMenu:true,
            showFilter:true,


            afterSelectionChange:function (item, event) {
                if (item.selected == false) {
                    console.log('got de-select event');
                    $scope.gridSelected = false;
                    return;
                }
                $scope.fillOrderLineItemFields(item.entity);
                $scope.populateOrderLineItem(item.orderID);
                //$scope.updateKSUModel(item.entity);
                $scope.validateButtons(item.entity);
                $scope.initAttachPanel();

                $scope.gridSelected = true;
            },
            columnDefs:[
                {field:'orderLineID', displayName:'Order Line ID', width:"150" },
                {field:'orderStatus', displayName:'Order Status', width:"150"},
                {field:'productName', displayName:'Product Name', width:"250"},
                {field:'productCode', displayName:'Product Code', width:"150"},
                {field:'productType', displayName:'Product Type', width:"150"},
                {field:'subGroup', displayName:'Sub Group', width:"150", visible:$scope.showSubGroupUser},
                {field:'productStatus', displayName:'Product Status', width:"150"},
                {field:'siteName', displayName:'Site Name', width:"150"},
                {field:'room', displayName:'Room', width:"150",visible:false},
                {field:'floor', displayName:'Floor', width:"100", visible:false},
                {field:'orderSubStatus', displayName:'Order Sub Status', width:"150"},
                {field:'suReasonForRejection', displayName:'SU Reason For Reject', width:"165"},
                {field:'parentID', displayName:'Parent ID', width:"250"},
                {field:'productSubStatus', displayName:'Product Sub Status', width:"150"},
                {field:'romReasonForRejection', displayName:'ROM Reason For Reject', width:"175"},
                {field:'romFirstName', displayName:'ROM First Name', width:"150"},
                {field:'romLastName', displayName:'ROM Last Name', width:"150",visible:false},
                {field:'romPhoneNumber', displayName:'ROM Phone Number', width:"200",visible:false},
                {field:'romEmailID', displayName:'ROM Email ID', width:"150",visible:false},
                {field:'submittedToAib', displayName:'Submitted to AIB', width:"150",cellTemplate: '<div>{{{true:\'No\', false:\'Yes\'}[row.entity[col.field]==\'0\']}}</div>'},
                {field:'supplierErrorCode', displayName:'Supplier Error Code', width:"150"}
            ]
            // filterOptions:$scope.filterOptions

        };
        /*
         $scope.orderGrid.columnDefs[23].visible = false;
         $scope.orderGrid.columnDefs[24].visible = false;
         $scope.orderGrid.columnDefs[25].visible = false;*/

        $scope.fillOrderLineItemFields = function (selectedOrder) {
            $scope.orderDetailFormData.productName = selectedOrder.productName;
            $scope.orderDetailFormData.orderLineId = selectedOrder.orderLineID;
            $scope.orderDetailFormData.parentId = selectedOrder.parentID;
            $scope.orderDetailFormData.orderStatus = selectedOrder.orderStatus;
            $scope.orderDetailFormData.orderSubStatus = selectedOrder.orderSubStatus;
            $scope.orderDetailFormData.productStatus = selectedOrder.productStatus;
            $scope.orderDetailFormData.productSubStatus = selectedOrder.productSubStatus;

            $scope.orderDetailFormData.orderId = $scope.orderId;
            $scope.orderDetailFormData.bfgCustId = $scope.customer.cusId;
            $scope.orderDetailFormData.quoteId = $scope.quoteId;
        };

        $scope.saveOrderInfo = function (selectedOrder) {
            $scope.orderId = selectedOrder.orderID;
            $scope.quoteId = selectedOrder.quoteID;
        };

        $scope.validateButtons = function (selectedOrder) {

            //Enable View KSU Button
            $scope.disableViewKsuBut = false;

            // Enable 'Cancel Order Button' Logic
            if ((selectedOrder.productSubStatus == 'On Hold') ||
                ((selectedOrder.orderStatus == 'Suspended') &&
                 (selectedOrder.reason != 'In Flight Amend - Operational Input Data') &&
                 (selectedOrder.productSubStatus != 'Not Possible To Be Fulfiled') )) {
                $scope.disableCancelOrderBut = false;
            } else {
                $scope.disableCancelOrderBut = true;
            }


            // Enable 'Launch Configurator Button' Logic
            if ((selectedOrder.orderStatus == 'Suspended') &&
                (selectedOrder.reason != 'In Flight Amend - Operational Input Data')) {
                $scope.disableLanunchCofigBut = true;
                $scope.disableReqCancelBut = true;
                $scope.disableCancelOrderBut = false;
            } else {
                $scope.disableLanunchCofigBut = false;
                $scope.disableReqCancelBut = false;
            }

            // Enable 'Request IFC Button' Logic
            if ((selectedOrder.productStatus == 'S0208941') ||
                (selectedOrder.productStatus == 'S0208942') ||
                (selectedOrder.productStatus == 'S0208943') ||
                (selectedOrder.productStatus == 'S0208944') ||
                (selectedOrder.productStatus == 'S0220894') ||
                (selectedOrder.productStatus == 'S0316711') ||
                (selectedOrder.productStatus == 'S0317411')) {
                $scope.disableIFCBut = true;
            } else {
                $scope.disableIFCBut = false;
            }

        };

        $scope.loadAttachment = function ($files) {

            $scope.$file = $files[0];

            if($scope.orderDetailFormData.attachType.length>0)
            {
                $scope.disableUploadBtn=false;
            }

        }

        $scope.uploadAttachment = function () {
            console.log("Inside Upload File !!");
            if (!_.isUndefined($scope.$file)) {
                var $file = $scope.$file;
                $scope.progress = -1;
                $scope.uploadResult = [];
                $scope.dataUrl = {};
                $scope.uploadStatusMsg = "";
                $scope.uploadStatusMsgStyle = ""

                orderService.uploadFile($scope.selectedSalesChannel.name, $scope.orderDetailFormData.bfgCustId, $scope.orderDetailFormData.attachType, $scope.orderDetailFormData.quoteId, $file, $upload, function (data, status) {
                    $scope.uploadStatusMsg = data.data;
                    $scope.uploadStatusMsgStyle = "color: red;float:left;font-size:10px;";
                    console.log('File Upload Failed !!');
                })
            }
        };


        $scope.clickDownload = function (doc) {
            console.log('Clicked Doc :' + doc);
            orderService.downloadAttachment($scope.selectedSalesChannel.name, $scope.orderDetailFormData.bfgCustId, $scope.orderDetailFormData.attachType, $scope.orderDetailFormData.quoteId, doc.documentId, doc.fileName, function (data, status) {

                if (status == '200') {
                    console.log('File Download Succeeded !!');
                } else {
                    console.log('File Download Failed !!');
                }
            });
        };

        $scope.attachTypeChange = function () {
            console.log("Attach Type" + $scope.orderDetailFormData.attachType);

            $scope.clearMsgs();
            orderService.listAttachments($scope.selectedSalesChannel.name, $scope.orderDetailFormData.bfgCustId, $scope.orderDetailFormData.attachType, $scope.orderDetailFormData.quoteId, function (data, status) {
                console.log('Status :' + status);
                if (status == '200') {
                    $scope.listAttachMsg = "";
                    console.log("data: ", data);
                    var docList = data;
                    if (docList == undefined) {
                        return;
                    }


                    $scope.attachList = docList;


                } else if (status == '404' || status == '400' || status == '500') {

                    $scope.listAttachMsg = "No attachments available !!";
                    $scope.listAttachMsgStyle = "color: green;";
                } else {
                    $scope.listAttachMsg = 'Error:' + data;
                    $scope.listAttachMsgStyle = "color: red;";

                }
            });

        };

        $scope.clearMsgs = function () {
            $scope.uploadStatusMsg = "";
            $scope.uploadStatusMsgStyle = "";
            $scope.listAttachMsg = "";
            $scope.listAttachMsgStyle = "";
            window.document.getElementById("orderDetailFormData.file").value = "";
            $scope.$file=undefined;
            $scope.disableUploadBtn=true;
        };

        $scope.resetPanelFields = function () {
            $scope.orderDetailFormData.productName = "";
            $scope.orderDetailFormData.orderId = "";
            $scope.orderDetailFormData.orderLineId = "";
            $scope.orderDetailFormData.parentId = "";
            $scope.orderDetailFormData.orderStatus = "";
            $scope.orderDetailFormData.orderSubStatus = "";
            $scope.orderDetailFormData.productStatus = "";
            $scope.orderDetailFormData.productSubStatus = "";

            $scope.disableLanunchCofigBut = true;
        };

        $scope.initAttachPanel = function () {
            $scope.clearMsgs();

            $scope.orderDetailFormData.attachType = "";

        };


        $scope.updateKSUModel = function (selectedItem) {
            $scope.ksuModel = [
                {name:'Order ID', value:selectedItem.orderId},
                {name:'Product Name', value:selectedItem.productName},
                {name:'Product Status', value:selectedItem.productStatus},
                {name:'Product Sub Status', value:selectedItem.productSubStatus},
                {name:'R.ExpedioReference', value:selectedItem.expedioRef}
            ];
        };


        var cqmDialogBoxController = function ($scope, $modalInstance, hTitle, messageBody, showOkButton, showCancelButton, items) {

            $scope.hTitle = hTitle;
            $scope.messageBody = messageBody;
            $scope.showOk = showOkButton;
            $scope.showCancel = showCancelButton;
            $scope.dialogItem = items;

            $scope.ok = function () {
                console.log('Clicked Ok');
                $modalInstance.close(items);
            };

            $scope.cancel = function () {
                console.log('Clicked Cancel');
                $modalInstance.dismiss('cancel');
            };
        };

        $scope.openCustomDialogBox = function (hTitle, messageBody, templateUrl, showOkButton, showCancelButton, items) {

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
                                                    items:function () {
                                                        return items;
                                                    }

                                                }
                                            });
            return modalInstance;
        };

        $scope.populateQoute = function (selectedOrder) {
            $scope.selectedQoute.QUOTEREFID = selectedOrder.quoteID;
            $scope.selectedQoute.QUOTEVERSION = selectedOrder.quoteVersion;
        };

        /*Populate Order Line Items*/
        $scope.populateOrderLineItem = function (orderId) {

            if (!_.isUndefined(orderId)) {
                $scope.showLineItems = true;

                orderService.getOrderLineItems(orderId, function (data, status) {

                    if (status != 200) {
                        $scope.showLineItems = false;
                        console.log("Order Line Item Request Failed !! STATUS:" + status);
                    } else {
                        if (!_.isUndefined(data)) {
                            if (data.length == undefined) {
                                $scope.listOfOrderLineItems = [data];
                                $scope.countOrderLineItems = 1;
                            } else {
                                $scope.listOfOrderLineItems = data;
                                $scope.countOrderLineItems = data.length;
                            }

                        } else {
                            $scope.showLineItems = false;
                        }

                        window.setTimeout(function () {
                            $(window).resize();
                        }, 1);
                    }

                });

            }

        };


        /*Launch the Configurator.  */

        $scope.launchConfigurator = function () {
            var title = "Launch Configurator.";
            var msg = "Do you want to launch Configurator?  Quote Ref Id: " + $scope.selectedQoute.QUOTEREFID + ", Version :" + $scope.selectedQoute.QUOTEVERSION + " .";
            var btns = [
                {result:'OK', label:'OK'},
                {result:'CANCEL', label:'Cancel'}
            ];

            var dialogInstance = UIService.openDialogBox(title, msg, true, true);
            dialogInstance.result.then(function () {
                quoteService.getBundlingAppURL($scope.selectedQoute.QUOTEREFID, $scope.selectedQoute.QUOTEVERSION, $scope.salesUser.ein, function (data, status) {
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
                    } else {
                        UIService.handleException('Launch Configurator', data, status);
                    }

                });
                console.log('Success to launch configuration.....');
            }, function () {
                console.log('Cancel to launch configuration.....');
            });
        }

        /*Cancel Order*/
        /* $scope.cancelOrder = function () {
         orderService.cancelOrder($scope.salesUser.ein, $scope.orderDetailFormData.orderId, $scope.orderDetailFormData.orderLineId);
         };*/

        /* Click View KSU Button*/

        /*$scope.viewKSU = function () {
         console.log('Inside View KSU Click');
         var title = "KSU";
         var msg = "";
         var templateURL = '/cqm/static/partials/templates/viewKSU.html';

         var dialogInstance = $scope.openCustomDialogBox(title, msg, templateURL, true, false, $scope.ksuModel);
         dialogInstance.result.then(function () {
         console.log("Accepted Ok");
         }, function () {
         console.log("Cancel Window")
         });
         };*/

        /* Click Request IFC*/

        /* $scope.requestIFC = function () {

         console.log('Inside Request IFC');
         var title = "Confirm In Flight Change";
         var msg = "";
         var templateURL = '/cqm/static/partials/templates/requestOrderIFC.html';

         var data = {
         reasonType:['In-flight change', 'Validation Failure'],
         cancelReason:'',
         comment:''
         };

         var dialogInstance = $scope.openCustomDialogBox(title, msg, templateURL, true, true, data);
         dialogInstance.result.then(function (modalReturnData) {
         console.log("Accepted OK");
         $scope.processIFCSubmit(modalReturnData);
         }, function () {
         console.log("Cancel Window")
         });


         };*/

        /* $scope.processIFCSubmit = function (modalDialogData) {

         var reasonCode = modalDialogData.reason;//'In-flight change';
         var comment = modalDialogData.comment;//'Comment from me for the IFC !!'

         $scope.orderDetailFormData.chkSuIfc = 1;
         $scope.orderDetailFormData.suComment = comment;
         $scope.orderDetailFormData.reasonForReject = reasonCode;

         var payload = $scope.orderDetailFormData;

         orderService.requestIFC($scope.salesUser.ein, payload, function (data, status) {
         var msg = '';
         var title = 'User Note';
         if (status == '200') {
         var sqeURL = data;
         msg = 'IFC Request has been sent to ROM.';
         $scope.openCustomDialogBox(title, msg, true, false);
         } else {
         msg = 'Failed to commit IFC';
         $scope.openCustomDialogBox(title, msg, true, false);
         }

         });
         };*/

        /*Click Request Cancellation*/
        /* $scope.requestCancel = function () {
         console.log('Inside Request Cancellation');
         var title = "Cancel Request";
         var msg = "";
         var templateURL = '/cqm/static/partials/templates/requestOrderCancel.html';
         var data = {
         comment:''
         };

         var dialogInstance = $scope.openCustomDialogBox(title, msg, templateURL, true, true, data);
         dialogInstance.result.then(function (modalData) {
         console.log("Accepted OK");
         $scope.requestCancelSubmit(modalData);
         }, function () {
         console.log("Cancel Window")
         });


         };*/

        /*$scope.requestCancelSubmit = function (modalData) {
         var comment = modalData.comment;

         $scope.orderDetailFormData.suComment = comment;

         var payload = $scope.orderDetailFormData;
         orderService.requestCancel($scope.salesUser.ein, payload);
         };*/

    }])

