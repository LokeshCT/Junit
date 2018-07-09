var module = angular.module('cqm.controllers');

//Start of activity Controller
module.controller('activityController', ['$scope', '$http', 'UIService', 'quoteService', 'UserContext', 'PageContext', 'UrlConfiguration','WebMetrics','activityService',
    function ($scope, $http, UIService, quoteService, UserContext, PageContext, UrlConfiguration,WebMetrics,activityService) {

        $scope.activitySearchResultData = {};
        $scope.selectedActivity = [];
        $scope.disableConfigLauncher = true;
        $scope.disableApproveButton = true;
        $scope.disableRejectButton = true;
        $scope.disableSalesUserCommentsTextarea = true;
        $scope.selectedQuote =[];
        $scope.showSearchResultSection = false;
        $scope.showCommentSection = false;
        $scope.showButtonSection = false;
        $scope.filterText = '';
        $scope.filterActivityId = '';
        $scope.filterQuoteId = '';
        $scope.filterCustName = '';
        $scope.filterQuoteName ='';
        $scope.quoteNameSelected=false;
        $scope.quoteIdSelected=false;
        $scope.salesUser = UserContext.getUser();
        $scope.listOfQuotes=[];
        $scope.enableReassignEmail =false;
        $scope.enableReassignComments =false;
        $scope.isBidManager =false;
        $scope.isSQEActivity =false;
        $scope.enableWithdrawApprovalRole =false;
        $scope.enableWithdrawApprovalStatus =false;
        $scope.enableAcceptDelegation=false;
        if (PageContext.exist()) {
            $scope.selectedSalesChannel = PageContext.getSalesChannel();
            $scope.salesChannel = $scope.selectedSalesChannel.name;
            $scope.customer = PageContext.getCustomer();
            $scope.contract = PageContext.getContract();
            $scope.customerList = PageContext.getSalesChannel().customerList;
        }
        $scope.updateFilterText = function () {
            $scope.activityGrid.filterOptions.filterText = $scope.filterActivityId + ";" + $scope.filterCustName + ";" + $scope.filterQuoteId + ";" + $scope.filterQuoteName +";" + $scope.filterText;
        }

        $scope.$watch('activityId', function () {

            if (!_.isUndefined($scope.activityId)) {
                $scope.filterActivityId = 'ActivityID:' + $scope.activityId;
            } else {
                $scope.filterActivityId = '';
            }
            $scope.updateFilterText();
        });

        $scope.$watch('filterCustomerName', function () {

            if (!_.isUndefined($scope.activityId)) {
                $scope.filterCustName = 'CustomerName:' + $scope.filterCustomerName;
            } else {
                $scope.filterCustName = '';
            }
            $scope.updateFilterText();
        });

        $scope.$watch('quoteName', function () {

            if (!_.isUndefined($scope.activityId)) {
                $scope.filterQuoteName = 'QuoteName:' + $scope.quoteName;
            } else {
                $scope.filterQuoteName = '';
            }
            $scope.updateFilterText();
        });

        $scope.$watch('quoteId', function () {

            if (!_.isUndefined($scope.activityId)) {
                $scope.filterQuoteId = 'QuoteRefID:' + $scope.quoteId;
            } else {
                $scope.filterQuoteId = '';
            }
            $scope.updateFilterText();
        });

        $scope.$watch('quoteId', function () {

            if (!_.isUndefined($scope.activityId)) {
                $scope.filterQuoteId = 'QuoteRefID:' + $scope.quoteId;
            } else {
                $scope.filterQuoteId = '';
            }
            $scope.updateFilterText();
        });

        $scope.$watch('filterText', function () {
            $scope.updateFilterText();
        });

        $scope.onPageLoad = function(){
            if ($scope.salesUser.roles[0].roleName == 'Bid Manager') {
                $scope.isBidManager =true;
            }else{
                $scope.isBidManager =false;
            }
        }

        $scope.activityGrid = { data:'activitySearchResultData', selectedItems:$scope.selectedActivity,
            multiSelect:false,
            enableColumnResize:true,
            showGroupPanel:true,
            showColumnMenu:true,
            showFilter:false,
            filterOptions:{
                filterText:''
            },
            afterSelectionChange:function (item, event) {
                if (item.selected == false) {
                    console.log('got de-select event');
                    $scope.disableApproveButton = true;
                    $scope.disableRejectButton = true;
                    $scope.disableConfigLauncher = true;
                    $scope.showCommentSection = false;
                    $scope.showButtonSection = false;
                    return;
                }
                /*Things to be done after activity selection*/
                $scope.onSelectActivity(item.entity);
            },
            columnDefs:[
                /*Update fields*/
                {field:'ActivityID', displayName:'Activity ID', width:"150"},
                {field:'ActivityType', displayName:'Activity Type', width:"120"},
                {field:'ActivityDescription', displayName:'Activity Description', width:"200"},
                {field:'Status', displayName:'Status', width:"120"},
                {field:'SubStatus', displayName:'Sub Status', width:"120"},
                {field:'CustomerName', displayName:'Customer Name', width:"140"},
                {field:'SalesChannel', displayName:'Sales Channel', width:"120", visible:"false"},
                {field:'AssignedTo', displayName:'Assigned To', width:"120"},
                {field:'AssignedtoEmailid', displayName:'Assigned To Email-ID', width:"120"},
                {field:'Creator', displayName:'Created By', width:"220"},
                {field:'CreatedbyEmailid', displayName:'Created by Email-ID', width:"220"},
                {field:'QuoteName', displayName:'Quote Name', width:"120"},
                {field:'QuoteRefID', displayName:'Quote Ref ID', width:"150"},
                {field:'QuoteVersion', displayName:'Quote Version', width:"120"},
                {field:'ExpedioReference', displayName:'Expedio Reference', width:"120"},
                {field:'SourceSystem', displayName:'Source System', width:"120"},
                {field:'OrderType', displayName:'Order Type', width:"120"},
                {field:'BFGcustomerid', displayName:'BFG Customer ID', width:"120"},
                {field:'ActivityCreatedDate', displayName:'Activity Creation Date', width:"170"},
                {field:'ActivityClosedDate', displayName:'Activity Close Date', width:"170"},
                {field:'ChannelType', displayName:'Channel Type', width:"120"},
                {field:'ProductName', displayName:'Product Name', width:"120"}
            ]
        };

        $scope.getQuotes = function () {
            console.log('Inside get quotes');
            if ($scope.customer == undefined || $scope.customer == null) {
                console.log('customer not selected');
                $scope.isCustomerSelected = false;
                return;
            }
            UIService.block();
            var startTime = new Date().getTime();
            quoteService.getQuotes($scope.salesChannel, $scope.customer.cusId).then(function (data, status) {
                console.log("data: ", data);
                UIService.unblock();
                $scope.isCustomerSelected = true;
                $scope.numOfQuotes = 0;
                $scope.noQuotesFound = false;
                var quoteList = [];
                var quotesList = data;

                if (angular.isArray(data)) {
                    if (quotesList.length == undefined) {
                        $scope.numOfQuotes = 0;
                        $scope.noQuotesFound = true;
                        return;
                    } else {
                        $scope.listOfQuotes = quotesList;
                        $scope.numOfQuotes = ($scope.listOfQuotes).length;
                    }
                }
                else {
                    $scope.numOfQuotes = 0;
                    $scope.noQuotesFound = true;
                    return;
                }
            });

        };

        $scope.filterList = [];
        $scope.initializeFilters = function () {
            if ($scope.isBidManager) {
                $scope.filterList = [
                    "My Pending Activities", "My Team's Pending Activities", "My Completed Activities"
                ];
                $scope.enableWithdrawApprovalRole=true;
            } else {
                $scope.filterList = [
                    "My Open Activities", "My Team's Open Activities",
                    "My Team's Approved Activities", "My Team's Rejected Activities", "My Completed Activities"
                ];
                $scope.enableWithdrawApprovalRole=false;
            }
        };
        $scope.enableReassign = function () {
            if(!_.isEmpty($scope.viewUpdateActivityFormData.newComments))
            {
                $scope.enableReassignComments =true;
            }
            else
            {
                $scope.enableReassignComments =false;
            }
        }
        $scope.onSelectActivity = function (entity) {
            if (!(_.isNull(entity) && _.isUndefined(entity))) {
                $scope.showCommentSection = true;
                $scope.showButtonSection = true;
                $scope.enableReassignEmail =false;
                $scope.enableReassignComments =false;
                $scope.viewUpdateActivityFormData = entity;
                if ($scope.viewUpdateActivityFormData.SourceSystem == 'SQE') {
                    $scope.isSQEActivity = true;
                }
                else {
                    $scope.isSQEActivity = false;
                }
                if (!_.isUndefined($scope.viewUpdateActivityFormData.QuoteRefID) && !_.isEmpty($scope.viewUpdateActivityFormData.QuoteRefID) && $scope.viewUpdateActivityFormData.QuoteRefID != 'Nil' && !_.isUndefined($scope.viewUpdateActivityFormData.QuoteVersion) && $scope.viewUpdateActivityFormData.QuoteVersion != 'Nil' && !_.isEmpty($scope.viewUpdateActivityFormData.QuoteVersion)) {
                    $scope.disableConfigLauncher = false;
                }

                if($scope.viewUpdateActivityFormData.AssignedtoEmailid != $scope.salesUser.emailId && 'Open' == $scope.viewUpdateActivityFormData.Status){
                    var title = 'Activity Ownership';
                    var message = 'Do you want to assign activity ' + $scope.viewUpdateActivityFormData.ActivityID + ' to yourself?';
                    var dialogInstance = UIService.openDialogBox(title, message, true, true);
                    dialogInstance.result.then(function () {
                        function callback(response) {
                            var data = response.data;
                            var status = response.status;

                            var title = 'Activity Ownership';
                            var message = '';

                            if('200' == status){
                                if('SUCCESS' == data.responseStatus){
                                    if ($scope.viewUpdateActivityFormData.Status == 'Open') {
                                        $scope.disableApproveButton = false;
                                        $scope.disableRejectButton = false;
                                    }

                                    $scope.viewUpdateActivityFormData.AssignedTo = data.AssignedTo;
                                    $scope.viewUpdateActivityFormData.AssignedtoEmailid = data.AssignedtoEmailid;

                                    message = 'Activity with id: ' + data.ActivityID + ' assigned to you.';
                                } else {
                                    message = 'Activity with id: ' + data.ActivityID + ' can not be assigned to you.';
                                }
                            } else if('404' == status){
                                $scope.disableApproveButton = false;
                                $scope.disableRejectButton = false;
                                message = 'Activity with id: ' + $scope.viewUpdateActivityFormData.ActivityID + ' can not be assigned to you.';
                            } else {
                                $scope.disableApproveButton = false;
                                $scope.disableRejectButton = false;
                                message = 'Unable to assign activity with id: ' + $scope.viewUpdateActivityFormData.ActivityID + ' to you. Please try after some time.';
                            }
                            UIService.openDialogBox(title, message, true, false);
                        }
                        $scope.enableAcceptDelegation=false;
                        var changeOwnershipDTO = new Object();
                        changeOwnershipDTO.activityID =  $scope.viewUpdateActivityFormData.ActivityID;
                        changeOwnershipDTO.assignedTo =  $scope.salesUser.name;
                        changeOwnershipDTO.userLogin =  $scope.salesUser.boatId;
                        changeOwnershipDTO.assigneeSalesUsercomments =  '';
                        changeOwnershipDTO.bidManagerComments =  '';

                        $http.put(UrlConfiguration.assignActiviyToUri, changeOwnershipDTO).then(callback, callback);
                    }, function () {
                        console.log('Activity assignment operation cancelled.');
                    });
                    $scope.disableApproveButton = false;
                    $scope.disableRejectButton = false;
                    $scope.enableAcceptDelegation = false;
                } else {
                    if ($scope.viewUpdateActivityFormData.Status == 'Open') {
                        $scope.disableApproveButton = false;
                        $scope.disableRejectButton = false;
                    }
                    if ($scope.viewUpdateActivityFormData.AssignedtoEmailid == $scope.salesUser.emailId)
                        $scope.enableAcceptDelegation=true;
                    else
                        $scope.enableAcceptDelegation = false;
                }

                if ($scope.viewUpdateActivityFormData.Status == 'Closed') {
                    $scope.enableWithdrawApprovalStatus = true;
                }
                else {
                    $scope.enableWithdrawApprovalStatus = false;
                }
            }
        };


        $scope.initializeActivityDescListS = function () {
            $scope.activityDescListS = [];
            $scope.activityDescListS = [
                "Update Monthly Commitment",
                "Pricebook Renewal",
                "Upload Site Addresses",
                "Verify Uploaded Addresses",
                "Complete RFQ Data",
                "Complete RFO Data",
                "Complete Access Pricing",
                "Configure CPE",
                "Complete CPE Pricing",
                "Input ICB Router Costs",
                "Configure Non-Standard request",
                "Complete Discounts Approval",
                "Generate BCM",
                "Generate Order Forms",
                "Request Inflight Amend",
                "Others"
            ];
        };

        $scope.statusList = [];
        $scope.initializeStatusList = function () {
            $scope.statusList = ["Open","Closed"];
        };

        $scope.salesChannelTypeList = [];
        $scope.initializeSalesChannelTypeList = function () {
            $scope.salesChannelTypeList = [
                "Direct",
                "Indirect"
            ];
        };

        $scope.productList = [];

        $scope.initializeProductList = function () {
            $scope.productList = [
                "IP Connect global",
                "Internet Connect Reach",
                "Proactive Monitoring",
                "IVPN Access",
                "NTE",
                "BT MobileXpress",
                "Other"
            ];
        };

        $scope.numOfMatchingActivities = 0;


        $scope.initializeSearchActivity = function () {
            $scope.searchActivityFormData = {bidManagerName:$scope.salesUser.name, salesChannel:$scope.selectedSalesChannel.name};

        };

        $scope.initializeSearchActivity();

        $scope.searchActivity = function () {
            console.log('Search Activity invoked');
            UIService.block();
            $scope.activitySearchResultData = [];
            $scope.numOfMatchingActivities = 0;
            $scope.clearFilter();
            $scope.showSearchResultSection = false;
            $scope.showCommentSection = false;
            $scope.showButtonSection = false;

            function callback(response) {
                var data = response.data;
                var status = response.status;

                var title = 'Activity Search!';
                if (status == '200') {
                    if (data == undefined) {
                        $scope.numOfMatchingActivities = 0;
                        return;
                    }
                    if (data.length == undefined) {
                        $scope.numOfMatchingActivities = 0;
                        $scope.activitySearchResultData = [ data ];
                    } else {
                        $scope.activitySearchResultData = data;
                        $scope.numOfMatchingActivities = data.length;
                    }

                    $scope.showSearchResultSection = true;
                    WebMetrics.captureWebMetrics(WebMetrics.UserActions.LoadActivity);
                } else if (status == '404') {
                    var msg = "No matching activity found!";
                    var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                } else {
                    var msg = "Search Operation Failed!";
                    var dialogInstance = UIService.openDialogBox(title, msg, true, false, data);
                }

                $scope.operationResult = "Found: " + $scope.numOfMatchingActivities;

                window.setTimeout(function () {
                    $(window).resize();
                }, 1);

                UIService.unblock();

                console.log("Activities Found: ", $scope.activitySearchResultData);
            }

            ;

            $http.post(UrlConfiguration.searchActivityUri, $scope.searchActivityFormData).then(callback, callback);

            $scope.hasSelectedActivity = false;
        };

        $scope.reset = function () {
            $scope.resetSearchFields();
            $scope.resetSearchResults();
            $scope.selectedActivity = undefined;
            $scope.showSearchResultSection = false;

            $scope.clearFilter();
        }

        $scope.clearFilter = function () {
            $scope.filterText = '';
            $scope.filterActivityId = '';
            $scope.filterQuoteId = '';
            $scope.filterQuoteName ='';
            $scope.filterCustName = '';

            $scope.activityId = '';
            $scope.filterCustomerName = '';
            $scope.quoteId = '';
            $scope.quoteName = '';
            $scope.filterText = '';
        }

        $scope.resetSearchFields = function () {
            $scope.initializeSearchActivity();
        }

        $scope.resetSearchResults = function () {
            $scope.activitySearchResultData = {};
            $scope.hasSelectedActivity = false;
        }

        $scope.viewUpdateActivityFormData = {};

        $scope.approveActivity = function () {
            console.log("Approving activity...");
            $scope.viewUpdateActivityFormData.ApproverReason = $scope.viewUpdateActivityFormData.BidMangersComments;
            UIService.block();

            function callback(response) {
                var data = response.data;
                var status = response.status;

                var title = 'Approve Activity';
                var msg = '';
                var dialogInstance;
                if (status == '200') {
                    $scope.disableApproveButton = false;
                    $scope.disableRejectButton = false;
                    msg = 'Successfully approved activity Id:' + $scope.selectedActivity[0].ActivityID;
                    $scope.viewUpdateActivityFormData.Status = data.Status;
                    $scope.viewUpdateActivityFormData.SubStatus = data.SubStatus;
                    dialogInstance = UIService.openDialogBox(title, msg, true, false);
                } else {
                    msg = 'Activity approval failed for activity Id: ' + $scope.selectedActivity[0].ActivityID + '. ' + data;
                    dialogInstance = UIService.openDialogBox(title, msg, true, false);
                }
                UIService.unblock();
            }

            ;

            var updateStatusDTO = new Object();
            updateStatusDTO.activityID =  $scope.viewUpdateActivityFormData.ActivityID;
            updateStatusDTO.state =  $scope.viewUpdateActivityFormData.Status;
            updateStatusDTO.substate =  $scope.viewUpdateActivityFormData.SubStatus;
            updateStatusDTO.assigneeSalesUsercomments =  $scope.viewUpdateActivityFormData.SalesUsersComments;
            updateStatusDTO.bidManagerComments =  $scope.viewUpdateActivityFormData.BidMangersComments;

            $http.post(UrlConfiguration.approveActivityUri, updateStatusDTO).then(callback, callback);
            console.log("Approved activity.");
        };

        $scope.rejectActivity = function () {
            console.log("Rejecting activity...");
            $scope.viewUpdateActivityFormData.ApproverReason = $scope.viewUpdateActivityFormData.BidMangersComments;
            UIService.block();

            function callback(response) {
                var data = response.data;
                var status = response.status;

                var title = 'Reject Activity';
                var msg = '';
                var dialogInstance;
                if (status == '200') {
                    $scope.disableApproveButton = false;
                    $scope.disableRejectButton = false;
                    msg = 'Successfully rejected activity Id:' + $scope.selectedActivity[0].ActivityID;
                    $scope.viewUpdateActivityFormData.Status = data.Status;
                    $scope.viewUpdateActivityFormData.SubStatus = data.SubStatus;
                    dialogInstance = UIService.openDialogBox(title, msg, true, false);
                } else {
                    msg = 'Activity approval failed for activity Id: ' + $scope.selectedActivity[0].ActivityID + '. ' + data;
                    dialogInstance = UIService.openDialogBox(title, msg, true, false);
                }
                UIService.unblock();
            }

            ;
            $http.post(UrlConfiguration.rejectActivityUri, $scope.viewUpdateActivityFormData).then(callback, callback);
            console.log("Rejected activity.");
        };

        $scope.updateActivity = function (statusReceived) {
            console.log("Update activity...");
            $scope.viewUpdateActivityFormData.ApproverReason = $scope.viewUpdateActivityFormData.BidMangersComments;
            UIService.block();

            function callback(response) {
                var data = response.data;
                var status = response.status;

                var title = 'Update Activity';
                var msg = '';
                var dialogInstance;
                if (status == '200') {
                    if (statusReceived == "Open")
                        $scope.enableWithdrawApprovalStatus = false;
                    else
                        $scope.enableWithdrawApprovalStatus = true;
                    $scope.disableApproveButton = false;
                    $scope.disableRejectButton = false;
                    msg = 'Successfully Updated Activity Id:' + $scope.selectedActivity[0].ActivityID;
                    $scope.viewUpdateActivityFormData.Status = data.Status;
                    $scope.viewUpdateActivityFormData.SubStatus = data.SubStatus;
                    dialogInstance = UIService.openDialogBox(title, msg, true, false);
                } else {
                    msg = 'Activity Updated failed for activity Id: ' + $scope.selectedActivity[0].ActivityID;
                    dialogInstance = UIService.openDialogBox(title, msg, true, false);
                }
                UIService.unblock();
            }

            ;

            var updateStatusDTO = new Object();
            updateStatusDTO.activityID = $scope.viewUpdateActivityFormData.ActivityID;
            if (statusReceived == "Open") {
                updateStatusDTO.state = "Open";
                updateStatusDTO.substate = "Open";
            } else {
                updateStatusDTO.state = "Closed";
                updateStatusDTO.substate = "Closed";
            }
            updateStatusDTO.assigneeSalesUsercomments = $scope.viewUpdateActivityFormData.SalesUsersComments;
            updateStatusDTO.bidManagerComments = $scope.viewUpdateActivityFormData.BidMangersComments;
            updateStatusDTO.salesChannel = $scope.salesChannel;
            updateStatusDTO.customerName= $scope.customer.cusName;
            updateStatusDTO.assignedToEmailId= $scope.viewUpdateActivityFormData.AssignedtoEmailid;
            $http.post(UrlConfiguration.updateActivityStatusUri, updateStatusDTO).then(callback, callback);
            console.log("Updated Activity Status.");
        };
        $scope.launchConfigurator = function () {
            var title = 'Launch Configurator!';
            var msg = "Do you want to launch Configurator?";
            var dialogInstance = UIService.openDialogBox(title, msg, true, false);
            dialogInstance.result.then(function () {
                console.log('Launching product configurator');
                UIService.block();
                quoteService.getBundlingAppURL($scope.viewUpdateActivityFormData.QuoteRefID, $scope.viewUpdateActivityFormData.QuoteVersion, $scope.salesUser.ein, function (data, status) {
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
                    UIService.unblock();
                });
                console.log('Successfully launched product configurator.');

            }, function () {
                console.log('Launch product configurator operation cancelled.');
            });

        };

        // Create Activity page related data and controllers
        $scope.initializeCreateActivityFormData = function () {
            $scope.createActivityFormData = {activityType:"Action Required",
                createdByName:$scope.salesUser.name,
                createdByEmailId:$scope.salesUser.emailId,
                bfgCustomerId:$scope.customer.cusId,
                customerName:$scope.customer.cusName,
                status:"Open",
                subStatus:"Open",
                productName:"",
                role:$scope.salesUser.roles[0].roleName,
                quoteRefID:"Nil",
                quoteVersion:"Nil", orderType:"Nil", expedioReference:"Nil"};
            $scope.createActivityFormData.searchCustomer=$scope.cus
            $scope.quoteNameSelected=false;
            $scope.quoteIdSelected=false;
            $scope.listOfQuotes=[];
            $scope.selectedQuote=[];
            $scope.getQuotes();
        };

        $scope.initializeCreateActivityFormData();

        $scope.activityDescList = [];

        $scope.getActivityDescription = function () {
            //TODO DB
            $scope.activityDescList = [
                { name:'Update Monthly Commitment', value:'Update Monthly Commitment'},
                { name:'Pricebook Renewal', value:'Pricebook Renewal'},
                { name:'Upload Site Addresses', value:'Upload Site Addresses'},
                { name:'Verify Uploaded Addresses', value:'Verify Uploaded Addresses'},
                { name:'Complete RFQ Data', value:'Complete RFQ Data'},
                { name:'Complete RFO Data', value:'Complete RFO Data'},
                { name:'Complete Access Pricing', value:'Complete Access Pricing'},
                { name:'Configure CPE', value:'Configure CPE'},
                { name:'Complete CPE Pricing', value:'Complete CPE Pricing'},
                { name:'Input ICB Router Costs', value:'Input ICB Router Costs'},
                { name:'Configure Non-Standard request', value:'Configure Non-Standard request'},
                { name:'Complete Discounts Approval', value:'Complete Discounts Approval'},
                { name:'Generate BCM', value:'Generate BCM'},
                { name:'Generate Order Forms', value:'Generate Order Forms'},
                { name:'Request Inflight Amend', value:'Request Inflight Amend'},
                { name:'Others', value:'Others'}
            ];
        };
        $scope.setSelectedQuoteIndex = function(selected) {
            $scope.selectedQuoteIndex = selected;
            alert("selected"+$scope.selectedQuoteIndex);
        }
        $scope.populateQuoteDetailsBasedOnName = function (quote) {
            if (!_.isUndefined(quote) && ! _.isEmpty(quote) && !$scope.quoteIdSelected) {
                $scope.quoteIdSelected = false;
                $scope.quoteNameSelected = true;
                var quoteObject = JSON.parse(quote);
                $scope.selectedQuote = [quoteObject];
                $scope.numOfQuotes = $scope.selectedQuote.length;
            }
        };

        $scope.populateQuoteDetailsBasedOnId = function (quote) {
            if (!_.isUndefined(quote) && ! _.isEmpty(quote) && !$scope.quoteNameSelected) {
                $scope.quoteIdSelected = true;
                $scope.quoteNameSelected = false;
                var quoteObject = JSON.parse(quote);
                $scope.selectedQuote = [quoteObject];
                $scope.numOfQuotes = $scope.selectedQuote.length;
            }
        };
        $scope.assignedToList = [];

        $scope.getAssignedToList = function () {
            $scope.assignedToList = [];

            var salesChannel = $scope.selectedSalesChannel.name;
            var bfgId = $scope.customer.cusId;
            var userRole = $scope.salesUser.roles[0].roleName;
            // var userRole = 'Bid Manager';

            var queryParams = {
                salesChannel:salesChannel,
                userRole:userRole,
                bfgId:bfgId
            };

            UIService.block();

            function callback(response) {
                var data = response.data;
                var status = response.status;

                if (status == '200') {
                    $scope.assignedToList = data;
                } else {
                    var title = 'AssignedTo list!';
                    var msg = "Unable to get AssignedTo list."
                    var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                    dialogInstance.result.then(function () {
                    }, function () {
                    });
                }
                UIService.unblock();
            }

            ;

            $http.get(UrlConfiguration.getAssignedToListUri, {params:queryParams}).then(callback, callback);
        };

        $scope.assignedToChanged = function () {
            _.forEach($scope.assignedToList, function (assignedTo) {
                if (assignedTo.fullName == $scope.createActivityFormData.assignedTo.fullName) {
                    $scope.createActivityFormData.bidManagerName = assignedTo.fullName;
                    $scope.createActivityFormData.assignedToEmailID = assignedTo.emailAddress;
                    $scope.createActivityFormData.groupToEmailID = assignedTo.groupEmailID;
                }
            });
        };
        $scope.assignedToChangedUpdatePage = function () {

            _.forEach($scope.assignedToList, function (assignedTo) {
                if (assignedTo.fullName == $scope.viewUpdateActivityFormData.assignedTo.fullName) {
                    $scope.viewUpdateActivityFormData.fullName = assignedTo.fullName;
                    $scope.viewUpdateActivityFormData.assignedToEmailID = assignedTo.emailAddress;
                    $scope.viewUpdateActivityFormData.groupToEmailID = assignedTo.groupEmailID;
                    $scope.viewUpdateActivityFormData.firstName = assignedTo.forename;
                    $scope.enableReassignEmail =true;
                }
            });
        };


        $scope.createActivity = function () {
            UIService.block();

            function callback(response) {
                var data = response.data;
                var status = response.status;

                var title = 'Activity Creation!';
                var msg = '';
                if (status == '200') {
                    UIService.unblock();
                    WebMetrics.captureWebMetrics(WebMetrics.UserActions.CreateActivity);
                    msg = 'Activity created successfully. Request ID: ' + data;
                    $scope.initializeCreateActivityFormData();


                } else {
                    msg = "Activity creation failed! Reason:" + data + ". Please retry after sometime.";
                }
                var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                dialogInstance.result.then(function () {
                }, function () {
                });
                UIService.unblock();
            };

            $scope.createActivityFormData.ActivityDescription=$scope.createActivityFormData.activityDesc.value;
            $scope.createActivityFormData.AssignedTo=$scope.createActivityFormData.assignedTo.fullName;
            $scope.createActivityFormData.ActivityType =$scope.createActivityFormData.activityType;
            $scope.createActivityFormData.SalesChannel = $scope.createActivityFormData.salesChannel.name;
            $scope.createActivityFormData.AssignedtoEmailid = $scope.createActivityFormData.assignedToEmailID;
            $scope.createActivityFormData.GroupEmailID = $scope.createActivityFormData.groupToEmailID;
            $scope.createActivityFormData.CustomerName =$scope.createActivityFormData.searchCustomer.cusName;
            $scope.createActivityFormData.SalesUsersComments =$scope.createActivityFormData.salesUserComments;
            $scope.createActivityFormData.CreatorReason =$scope.createActivityFormData.salesUserComments;
            $scope.createActivityFormData.Status=$scope.createActivityFormData.status;
            $scope.createActivityFormData.SubStatus =$scope.createActivityFormData.subStatus;
            $scope.createActivityFormData.Creator  = $scope.createActivityFormData.createdByName;
            $scope.createActivityFormData.CreatedbyEmailid  = $scope.createActivityFormData.createdByEmailId;
            $scope.createActivityFormData.BFGcustomerid = $scope.createActivityFormData.bfgCustomerId;
            $scope.createActivityFormData.QuoteRefID = $scope.createActivityFormData.quoteRefID;
            $scope.createActivityFormData.QuoteVersion = $scope.createActivityFormData.quoteVersion;
            $scope.createActivityFormData.ExpedioReference = $scope.createActivityFormData.expedioReference;
            $scope.createActivityFormData.Role=  $scope.createActivityFormData.role;
            $scope.createActivityFormData.OrderType=  $scope.createActivityFormData.orderType;
            $scope.createActivityFormData.SourceSystem = "RSQE";
            if (!_.isUndefined($scope.createActivityFormData.quoteId) && !_.isEmpty($scope.createActivityFormData.quoteId)) {
                $scope.createActivityFormData.QuoteName = JSON.parse($scope.createActivityFormData.quoteId).quoteName;
                $scope.createActivityFormData.QuoteRefID = JSON.parse($scope.createActivityFormData.quoteId).quoteRefID;
                $scope.createActivityFormData.QuoteVersion = JSON.parse($scope.createActivityFormData.quoteId).quoteVersion;
            }

            $http.post(UrlConfiguration.createActivityUri, $scope.createActivityFormData).then(callback, callback);
        };


        $scope.acceptDelegation  = function () {
            console.log("Accepted Delegation...");
            $scope.viewUpdateActivityFormData.ApproverReason = $scope.viewUpdateActivityFormData.BidMangersComments;
            UIService.block();
            var assignorFirstName= $scope.salesUser.name;
            if(!_.isEmpty($scope.salesUser.name))
            {
                assignorFirstName= $scope.salesUser.name;
                var n = assignorFirstName.indexOf(" ");
                assignorFirstName = assignorFirstName.substring(0,n);
            }
            var assigneeFirstName= $scope.viewUpdateActivityFormData.AssignedTo;
            if(!_.isEmpty($scope.viewUpdateActivityFormData.AssignedTo))
            {
                assigneeFirstName= $scope.viewUpdateActivityFormData.AssignedTo;
                var m = assigneeFirstName.indexOf(" ");
                assigneeFirstName = assigneeFirstName.substring(0,m);
            }


            activityService.acceptDelegation($scope.selectedActivity[0].ActivityID,
                                             $scope.salesUser.emailId,
                                             $scope.salesUser.name,
                                             assignorFirstName,
                                             $scope.viewUpdateActivityFormData.assignedToEmailID,
                                             $scope.viewUpdateActivityFormData.AssignedTo,
                                             assigneeFirstName,
                                             $scope.viewUpdateActivityFormData.newComments ,
                                             function (data, status) {
                                                 var title = 'Accept Delegation';
                                                 var msg ="";
                                                 var btns = [
                                                     {result:'OK', label:'OK'}
                                                 ];

                                                 if (status == '200') {
                                                     msg = 'Accepted Delegation Activity Id: ' + $scope.selectedActivity[0].ActivityID;
                                                     dialogInstance = UIService.openDialogBox(title, msg, true, false);
                                                 } else {
                                                     msg = 'Accept Delegation Failed for activity Id: ' + $scope.selectedActivity[0].ActivityID + '. ' + data;
                                                     dialogInstance = UIService.openDialogBox(title, msg, true, false);
                                                 }
                                                 UIService.unblock();
                                                 var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                                                 dialogInstance.result.then(function () {
                                                 }, function () {
                                                     UIService.unblock();
                                                 });
                                             });
        }

        $scope.rejectDelegation  = function () {
            console.log("Accepted Delegation...");
            $scope.viewUpdateActivityFormData.ApproverReason = $scope.viewUpdateActivityFormData.BidMangersComments;
            UIService.block();

            activityService.rejectDelegation($scope.selectedActivity[0].ActivityID,
                                             $scope.salesUser.emailId,
                                             $scope.salesUser.name,
                                             $scope.viewUpdateActivityFormData.CreatedbyEmailid,
                                             $scope.viewUpdateActivityFormData.Creator,
                                             $scope.viewUpdateActivityFormData.newComments,
                                             function (data, status) {
                                                 var title = 'Reject Delegation';
                                                 var msg ="";
                                                 var btns = [
                                                     {result:'OK', label:'OK'}
                                                 ];

                                                 if (status == '200') {
                                                     msg = 'Successfully Rejected Delegation Activity Id: ' + $scope.selectedActivity[0].ActivityID;
                                                     dialogInstance = UIService.openDialogBox(title, msg, true, false);
                                                 } else {
                                                     msg = 'Reject Delegation Failed for activity Id: ' + $scope.selectedActivity[0].ActivityID + '. ' + data;
                                                     dialogInstance = UIService.openDialogBox(title, msg, true, false);
                                                 }
                                                 UIService.unblock();
                                                 var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                                                 dialogInstance.result.then(function () {
                                                 }, function () {
                                                     UIService.unblock();
                                                 });
                                             });
        }
        $scope.reassignActivity = function () {
            console.log("ReAssign Activity...");
            $scope.viewUpdateActivityFormData.ApproverReason = $scope.viewUpdateActivityFormData.BidMangersComments;
            UIService.block();
            var assignorFirstName= $scope.salesUser.name;
            if(!_.isEmpty($scope.salesUser.name))
            {
                var assignorFirstName= $scope.salesUser.name;
                var n = assignorFirstName.indexOf(" ");
                assignorFirstName = assignorFirstName.substring(0,n);
            }
            activityService.reassignActivity($scope.selectedActivity[0].ActivityID, $scope.salesUser.emailId, $scope.viewUpdateActivityFormData.fullName,
                                             $scope.viewUpdateActivityFormData.firstName,$scope.viewUpdateActivityFormData.assignedToEmailID,$scope.viewUpdateActivityFormData.newComments ,$scope.salesUser.name,assignorFirstName,function (data, status) {
                        var title = 'Reassign Activity';
                        var msg ="";
                        var btns = [
                            {result:'OK', label:'OK'}
                        ];

                        if (status == '200') {
                            msg = 'Successfully Reassigned Activity Id:' + $scope.selectedActivity[0].ActivityID;
                            $scope.viewUpdateActivityFormData.AssignedTo = $scope.viewUpdateActivityFormData.fullName;
                            $scope.viewUpdateActivityFormData.AssignedtoEmailid   =  $scope.viewUpdateActivityFormData.assignedToEmailID;
                            dialogInstance = UIService.openDialogBox(title, msg, true, false);
                        } else {
                            msg = 'Activity Reassgn Failed for activity Id: ' + $scope.selectedActivity[0].ActivityID + '. ' + data;
                            dialogInstance = UIService.openDialogBox(title, msg, true, false);
                        }
                        UIService.unblock();
                        var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                        dialogInstance.result.then(function () {
                        }, function () {
                            UIService.unblock();
                        });
                    });
        }
        $scope.withdrawApproval  = function () {
            console.log("Withdraw Approval...");
            $scope.viewUpdateActivityFormData.ApproverReason = $scope.viewUpdateActivityFormData.BidMangersComments;
            UIService.block();
            var assignorFirstName= $scope.salesUser.name;
            if(!_.isEmpty($scope.salesUser.name))
            {
                assignorFirstName= $scope.salesUser.name;
                var n = assignorFirstName.indexOf(" ");
                assignorFirstName = assignorFirstName.substring(0,n);
            }

            var creatorFirstName = $scope.viewUpdateActivityFormData.Creator;

            if(!_.isEmpty(creatorFirstName))
            {
                creatorFirstName= $scope.viewUpdateActivityFormData.Creator;
                var m = creatorFirstName.indexOf(" ");
                creatorFirstName = creatorFirstName.substring(0,m);
            }


            activityService.withdrawApproval($scope.selectedActivity[0].ActivityID,
                                             $scope.salesUser.name,
                                             assignorFirstName,
                                             $scope.salesUser.emailId,
                                             $scope.viewUpdateActivityFormData.Creator,
                                             creatorFirstName,
                                             $scope.viewUpdateActivityFormData.CreatedbyEmailid,
                                             $scope.viewUpdateActivityFormData.newComments,
                                             function (data, status) {
                                                 var title = 'Withdraw Approval';
                                                 var msg ="";
                                                 var btns = [
                                                     {result:'OK', label:'OK'}
                                                 ];

                                                 if (status == '200') {
                                                     msg = 'Successful Withdraw Approval with Activity Id: ' + $scope.selectedActivity[0].ActivityID;
                                                     dialogInstance = UIService.openDialogBox(title, msg, true, false);
                                                 } else {
                                                     msg = 'Failed Withdraw Approval with Activity Id: ' + $scope.selectedActivity[0].ActivityID + '. ' + data;
                                                     dialogInstance = UIService.openDialogBox(title, msg, true, false);
                                                 }
                                                 UIService.unblock();
                                                 var dialogInstance = UIService.openDialogBox(title, msg, true, false);
                                                 dialogInstance.result.then(function () {
                                                 }, function () {
                                                     UIService.unblock();
                                                 });
                                             });
        }
    }]);
