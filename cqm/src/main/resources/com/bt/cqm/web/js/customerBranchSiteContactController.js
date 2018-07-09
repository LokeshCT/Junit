var module = angular.module('cqm.controllers');

// Branch site contact controller BEGIN
module.controller('customerBranchSiteContactController', ['$scope', '$routeParams', 'UIService', 'customerContactService', 'branchSiteService', '$modal', 'PageContext', 'UserContext', 'WebMetrics', '$rootScope', function ($scope, $routeParams, UIService, customerContactService, branchSiteService, $modal, PageContext, UserContext, WebMetrics, $rootScope) {
    console.log('Inside customerBranchSiteContactController');
    var branchSiteContactMsgTitle = 'Branch Site Contact';
    if (PageContext.exist()) {
        $scope.customer = PageContext.getCustomer();
        $scope.contract = PageContext.getContract();
        $scope.salesChannel = PageContext.getSalesChannel();
    }

    if (UserContext.exist()) {
        $scope.salesUser = UserContext.getUser();
    }

    $scope.availableContactRoles = [];
    $scope.hasContacts = false;
    var index = -1;
    $scope.siteId = "";
    $scope.siteName = "";
    $scope.fiterText = '';
    $scope.isEmailMandatory='true';
    $scope.countryName="";

    $scope.loadBranchSite = function () {
        $scope.showBranchSiteContacts = false;
        $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.NOT_APPLICABLE);
        console.log('Inside Contact LoadBranchSite');
        $scope.onStandardFilterChange();
        $scope.numOfCustomerContacts = 0;
        $scope.fiterText='';
        $scope.getBranchSite();
    }

    $scope.onStandardFilterChange = function(){

        switch($scope.siteType){
            case 0:
                $scope.filterTextStdFilter ='';
                break;
            case 1:
                $scope.filterTextStdFilter ='sitQuoteOnlyFlag:N';
                break;
            case 2:
                $scope.filterTextStdFilter ='sitQuoteOnlyFlag:Y';
                break;

        }

        $scope.updateFilterText();
    }

    $scope.updateFilterText = function(){
        $scope.branchSiteGrid.filterOptions.filterText = $scope.filterTextStdFilter +";"+$scope.fiterText;
    }

    $scope.$watch('fiterText',function(){
        if(!_.isUndefined($scope.fiterText)){
            $scope.updateFilterText();
        }
    })

    $scope.getBranchSite = function () {
        UIService.block();
        var startTime = new Date().getTime();
        branchSiteService.getBranchSite($scope.salesUser.ein, $scope.selectedSalesChannel.name, $scope.customer.cusId, function (data, status) {
            if (status == '200') {
                var branchSiteListTemp;
                if (data.length == undefined) {
                    branchSiteListTemp = [data];
                }
                else {
                    branchSiteListTemp = data;
                }
                $scope.branchSiteList = branchSiteListTemp;
                window.setTimeout(function () {
                    $(window).resize();
                }, 1);
            }
            WebMetrics.captureWebMetrics(WebMetrics.UserActions.BranchSites, startTime);
            UIService.unblock();
        });
    }

    $scope.getBranchSiteContacts = function (selectedBranchSite) {
        $scope.branchSiteContactData = [];
        $scope.contactToBeUpdated = {};
        console.log('inside getBranchSiteContacts');
        $scope.populateBranchContacts($scope.customer.cusId, selectedBranchSite.siteId);
    }

    $scope.populateBranchContacts = function (customerId, siteId) {
        console.log('Inside CQMController.populateBranchContacts');
        if (!_.isUndefined(customerId) && !_.isNull(customerId) && !_.isUndefined(siteId) && !_.isNull(siteId)) {
            UIService.block();
            var startTime = new Date().getTime();
            customerContactService.getSiteContacts(customerId, siteId, function (data, status) {

                if (status == '200' || status == '404') {
                    if (_.isUndefined(data) || _.isUndefined(data.length) || data.length < 1) {
                        $scope.numOfCustomerContacts = 0;
                        $scope.branchSiteContactData = [];
                        $scope.hasContacts = false;
                        $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.INVALID);

                    } else if (data.length >= 1) {
                        for (var i = 0; i < data.length; i++) {
                            if (data[i].ctpType == "KCI Contact") {
                                data[i].ctpType = "Service Assurance";
                            }
                        }
                        $scope.branchSiteContactData = data;
                        $scope.numOfCustomerContacts = $scope.branchSiteContactData.length;
                        $scope.hasContacts = true;

                        var isSitePrimaryExist = false;


                        _.each($scope.branchSiteContactData, function (contact) {
                            if (contact.ctpType == $scope.roles[0].value) {
                               isSitePrimaryExist = true;
                            }
                        })

                        /*isSitePrimaryExist = _.find($scope.branchSiteContactData, function (contact) {
                            if (contact.ctpType == $scope.roles[0].value) {
                                return true;
                            }
                        })*/

                        if (isSitePrimaryExist) {
                            $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.VALID);
                        } else {
                            $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.INVALID);
                        }

                        window.setTimeout(function () {
                            $(window).resize();
                        }, 1);
                    }
                    WebMetrics.captureWebMetrics(WebMetrics.UserActions.BranchSiteContacts, startTime);
                } else {
                    UIService.handleException(branchSiteContactMsgTitle, data, status);
                    $scope.hasContacts = false;
                }

                $scope.cacheAvailableContactRoles();
                UIService.unblock();

            });
        }
    };

    $scope.branchSiteContactUI = {'enableCreateContactButton':false, 'enableUpdateContactButton':false};
    //$scope.numOfCustomerContacts = 0;
    $scope.selectedBranchSiteContactAddress = [];
    $scope.contactToBeUpdated = {};

    $scope.branchSiteContactGrid = { data:'branchSiteContactData', selectedItems:$scope.selectedBranchSiteContactAddress,
        multiSelect:false, enableColumnResize:true,
        afterSelectionChange:function (item, event) {
            if (item.selected == false) {
                console.log('got de-select event');
                return;
            }
            $scope.branchSiteContactUI.enableUpdateContactButton = true;
            $scope.index = item.rowIndex;
            $scope.fillBranchSiteContactFields(item.entity);
            if (item.entity.ctpType == "KCI Contact") {
                item.entity.ctpType = "Service Assurance";
            }
        },
        columnDefs:[
            {field:'ctpType', displayName:'Contact Role', width:240},
            {field:'contact.firstName', displayName:'First Name', width:120},
            {field:'contact.lastName', displayName:'Last Name', width:120},
            {field:'contact.jobTitle', displayName:'Job Title', width:120},
            {field:'contact.email', displayName:'e-mail', width:240},
            {field:'contact.phoneNumber', displayName:'Phone Number', width:120},
            {field:'contact.mobileNumber', displayName:'Mobile Number', width:120},
            {field:'contact.fax', displayName:'Fax', width:120}

        ]
    };

    $scope.fillBranchSiteContactFields = function (selectedBranchSiteContact) {
        $scope.branchSiteContactUI.enableCreateContactButton = false;
        $scope.branchSiteContactUI.enableUpdateContactButton = true;
        if (selectedBranchSiteContact.ctpType == "Service Assurance") {
            selectedBranchSiteContact.ctpType = "KCI Contact";
        }
        var roleFound = _.find($scope.roles, function (role) { if (role.value == selectedBranchSiteContact.ctpType) { return role}}); //selectedBranchSiteContact.ctpType;

        if (!_.isUndefined(roleFound)) {
            $scope.contactToBeUpdated.role = roleFound.value;
        } else {
            $scope.contactToBeUpdated.role = undefined;
        }

        $scope.cachedFormRole = $scope.contactToBeUpdated.role;

        $scope.contactToBeUpdated.fax = selectedBranchSiteContact.contact.fax;
        $scope.contactToBeUpdated.mobileNumber = selectedBranchSiteContact.contact.mobileNumber;
        $scope.contactToBeUpdated.phoneNumber = selectedBranchSiteContact.contact.phoneNumber;
        $scope.contactToBeUpdated.email = selectedBranchSiteContact.contact.email;
        $scope.contactToBeUpdated.jobTitle = selectedBranchSiteContact.contact.jobTitle;
        $scope.contactToBeUpdated.lastName = selectedBranchSiteContact.contact.lastName;
        $scope.contactToBeUpdated.firstName = selectedBranchSiteContact.contact.firstName;
        $scope.contactToBeUpdated.contactID = selectedBranchSiteContact.contact.contactId;
        $scope.contactToBeUpdated.contactRoleId = selectedBranchSiteContact.id;
        $scope.contactToBeUpdated.bfgSiteId = $scope.siteId;
    }

    $scope.roles = [
        {name:"Site Primary Contact", value:"Site Primary Contact"},
        {name:"Site Secondary Contact", value:"Site Secondary Contact"},
        {name:"Main Contact", value:"Main Contact"},
        {name:"Secondary Contact", value:"Secondary Contact"},
        {name:"Service Assurance", value:"KCI Contact"},
        {name:"Service Delivery", value:"Service Delivery"},
        {name:"Site Technical", value:"Site Technical"}
    ];


    $scope.cacheAvailableContactRoles = function () {
        var role = $scope.contactToBeUpdated.role;
        if (!_.isEmpty($scope.branchSiteContactData)) {
            var exists = false;
            if ($scope.branchSiteContactData.length == undefined) {
                if (!_.contains($scope.availableContactRoles, $scope.branchSiteContactData.ctpType)) {
                    $scope.availableContactRoles.push($scope.branchSiteContactData.ctpType);
                }
                //$scope.roles.reduce()
            } else {
                for (var i = 0; i < $scope.branchSiteContactData.length; i++) {
                    if (!_.isEmpty($scope.branchSiteContactData[i]) && !_.isEmpty($scope.branchSiteContactData[i].ctpType)) {
                        if (!_.contains($scope.availableContactRoles, $scope.branchSiteContactData[i].ctpType)) {
                            $scope.availableContactRoles.push($scope.branchSiteContactData[i].ctpType);
                        }
                    }

                }
            }
        } else {
            $scope.availableContactRoles = [];
        }
    }

    $scope.createBranchSiteContact = function () {

        var ein = $scope.salesUser.ein;
        var salesChannel = $scope.salesChannel;
        var customerId = $scope.customer.cusId;
        var customerName = $scope.customer.cusName;
        $scope.contactToBeUpdated.bfgSiteId = $scope.siteId;
        UIService.block();
        // STORY GSCE-159036
        var phoneNumber =   $scope.contactToBeUpdated.phoneNumber;
        var mobileNumber =   $scope.contactToBeUpdated.mobileNumber;
        var contactRole =    $scope.contactToBeUpdated.role;
        var validPhoneNumber=true;
        var validMobileNumber=true;

        if (!_.isEmpty($scope.countryName) && $scope.countryName == "FRANCE") {
            if (contactRole == "Site Primary Contact" || contactRole == "Site Secondary Contact") {
                if (!_.isEmpty(phoneNumber) && phoneNumber.length == 12 && (phoneNumber.indexOf("+33") == 0)) {
                    validPhoneNumber = true;
                }
                else {
                    validPhoneNumber = false;
                }
                if (!_.isEmpty(mobileNumber)) {
                    if (mobileNumber.length == 12 && (mobileNumber.indexOf("+33") == 0)) {
                        validMobileNumber = true;
                    }
                    else {
                        validMobileNumber = false;
                    }
                }
            }
        }
        if(!validPhoneNumber && !validMobileNumber)
        {
           var message = "The entered Phone Number and Mobile Number is not valid. Please note that the  Number needs to start with +33 followed by 9 digits " ;
            UIService.openDialogBox(branchSiteContactMsgTitle, message, true, false);
            UIService.unblock();
            return;
        }

        else if(!validPhoneNumber)
        {
            var message = "The entered Phone Number is not valid. Please note that the Phone Number needs to start with +33 followed by 9 digits " ;
            UIService.openDialogBox(branchSiteContactMsgTitle, message, true, false);
            UIService.unblock();
            return;
        }
        else if(!validMobileNumber)
        {
            var message = "The entered Mobile Number is not valid. Please note that the Mobile Number needs to start with +33 followed by 9 digits " ;
            UIService.openDialogBox(branchSiteContactMsgTitle, message, true, false);
            UIService.unblock();
            return;
        }
        // STORY GSCE-159036
        branchSiteService.createBranchSiteContact(ein, customerId, $scope.siteId, $scope.contactToBeUpdated, function (responseData, status) {
            //var title = 'Create Customer Contact';
            var message = "";
            UIService.unblock();

            if (status == '200') {
                message = "Successfully created contact for Site : " + $scope.siteName + ".";
                $scope.populateBranchContacts($scope.customer.cusId, $scope.siteId);
                $scope.contactToBeUpdated = {};
                $scope.availableContactRoles = [];
            } else {
                UIService.handleException(branchSiteContactMsgTitle, responseData, status);
                return;
            }


            UIService.openDialogBox(branchSiteContactMsgTitle, message, true, false);

        });
    };

    $scope.updateBranchSiteContact = function () {
        var ein = $scope.salesUser.ein;
        var salesChannel = $scope.salesChannel.name;
        var customerId = $scope.customer.cusId;
        var customerName = $scope.customer.cusName;
        //var title = 'Update Customer Branch Site Contact';
        var message = "";

        UIService.block();
        var phoneNumber =   $scope.contactToBeUpdated.phoneNumber;
        var mobileNumber =   $scope.contactToBeUpdated.mobileNumber;
        var contactRole =    $scope.contactToBeUpdated.role;
        var validPhoneNumber=true;
        var validMobileNumber=true;

        if (!_.isEmpty($scope.countryName) && $scope.countryName == "FRANCE") {
            if (contactRole == "Site Primary Contact" || contactRole == "Site Secondary Contact") {
                if (!_.isEmpty(phoneNumber) && phoneNumber.length == 12 && (phoneNumber.indexOf("+33") == 0)) {
                    validPhoneNumber = true;
                }
                else {
                    validPhoneNumber = false;
                }

                if (!_.isEmpty(mobileNumber)) {
                    if (mobileNumber.length == 12 && (mobileNumber.indexOf("+33") == 0)) {
                        validMobileNumber = true;
                    }
                    else {
                        validMobileNumber = false;
                    }
                }
            }
        }
        if(!validPhoneNumber && !validMobileNumber)
        {
            var message = "The entered Phone Number and Mobile Number is not valid. Please note that the  Number needs to start with +33 followed by 9 digits " ;
            UIService.openDialogBox(branchSiteContactMsgTitle, message, true, false);
            UIService.unblock();
            return;
        }

        else if(!validPhoneNumber)
        {
            var message = "The entered Phone Number is not valid. Please note that the Phone Number needs to start with +33 followed by 9 digits " ;
            UIService.openDialogBox(branchSiteContactMsgTitle, message, true, false);
            UIService.unblock();
            return;
        }
        else if(!validMobileNumber)
        {
            var message = "The entered Mobile Number is not valid. Please note that the Mobile Number needs to start with +33 followed by 9 digits " ;
            UIService.openDialogBox(branchSiteContactMsgTitle, message, true, false);
            UIService.unblock();
            return;
        }
        // STORY GSCE-159036

        branchSiteService.updateBranchSiteContact(ein, customerId, $scope.siteId, $scope.contactToBeUpdated, function (responseData, status) {
            UIService.unblock();
            if (status == '200') {
                message = "Successfully updated contact for Site : " + $scope.siteName + ".";
                $scope.populateBranchContacts($scope.customer.cusId, $scope.siteId);
                $scope.contactToBeUpdated = {};
                $scope.availableContactRoles = [];
            } else {
                UIService.handleException(branchSiteContactMsgTitle, responseData, status);
                return;
            }


            UIService.openDialogBox(branchSiteContactMsgTitle, message, true, false);

        });
    };

    $scope.updateBranchSiteContactUI = function () {

        /*Enable/Disable Create Button*/
        if (_.contains($scope.availableContactRoles, $scope.contactToBeUpdated.role)) {
            $scope.branchSiteContactUI.enableCreateContactButton = false;
        } else {
            $scope.branchSiteContactUI.enableCreateContactButton = true;
        }

        /*Enable/Disable Update Button*/
        if (_.contains($scope.availableContactRoles, $scope.contactToBeUpdated.role) && ($scope.cachedFormRole != $scope.contactToBeUpdated.role)) {
            $scope.branchSiteContactUI.enableUpdateContactButton = false;
        } else if (_.isUndefined($scope.contactToBeUpdated.bfgSiteId) || _.isEmpty($scope.contactToBeUpdated.bfgSiteId.toString())) {
            $scope.branchSiteContactUI.enableUpdateContactButton = false;
        } else {
            $scope.branchSiteContactUI.enableUpdateContactButton = true;
        }
         //chek the role type and make Email field as mandatory.
        if ($scope.contactToBeUpdated.role == "Site Primary Contact" ) {
            $scope.isEmailMandatory = true;
        } else {
            $scope.isEmailMandatory = false;
        }



    };

    $scope.branchSiteGrid = { data:'branchSiteList', multiSelect:false, enableColumnResize:true,
        showGroupPanel:true,
        showColumnMenu:true,
        showFilter:false,
        filterOptions:{
            filterText:''
        },
        afterSelectionChange:function (item, event) {
            if (item.selected == false) {
                console.log('got de-select event');
                $scope.siteId = undefined;
                $scope.siteName = undefined;
                $scope.countryName   = undefined;
                $scope.availableContactRoles = [];
                return;
            }
            $scope.showBranchSiteContacts = true;
            if (!_.isUndefined(item.entity)) {
                $scope.siteId = item.entity.siteId;
                $scope.siteName = item.entity.name;
                $scope.countryName = item.entity.country;
            }
            $scope.getBranchSiteContacts(item.entity);
            $scope.contactToBeUpdated = {};
        },
        columnDefs:[
            {field:'name', displayName:'Site Name', width:"*"},
            {field:'siteId', displayName:'Site Id', width:"*" },
            {field:'localCompanyName', displayName:'Company Name', width:"*" },
            {field:'building', displayName:'Building Name', width:"*", visible:false},
            {field:'subBuilding', displayName:'Sub Building', width:"*", visible:false},
            {field:'buildingNumber', displayName:'Building Number', width:"*", visible:false},
            {field:'floor', displayName:'Floor', width:"*"},
            {field:'room', displayName:'Room', width:"*"},
            {field:'subPremises', displayName:'Sub Premises', width:"*"},
            {field:'street', displayName:'Street', width:"*", visible:false},
            {field:'subStreet', displayName:'Sub Street', width:"*", visible:false},
            {field:'locality', displayName:'Locality', width:"*"},
            {field:'subLocality', displayName:'Sub Locality', width:"*", visible:false},
            {field:'city', displayName:'City', width:"*"},
            {field:'state', displayName:'State/Province', width:"*"},
            {field:'subState', displayName:'Sub State/County/Province', width:"*", visible:false},
            {field:'country', displayName:'Country', width:"*"},
            {field:'postCode', displayName:'Zip/Post Code', width:"*", visible:false},
            {field:'subZipCode', displayName:'Sub Post Code', width:"*", visible:false},
            {field:'poBox', displayName:'PO Box', width:"*", visible:false},
            {field:'postalOrganisation', displayName:'Postal Organisation', width:"*", visible:false},
            {field:'latitude', displayName:'Latitude', width:"*", visible:false},
            {field:'longitude', displayName:'Longitude', width:"*", visible:false},
            {field:'managePlaceResult', displayName:'Manage Place Result', width:"*", visible:false},
            {field:'accuracyLevel', displayName:'Accuracy Level', width:"*", visible:false},
            {field:'failLevel', displayName:'Fail Level', width:"*", visible:false},
            {field:'validationLevel', displayName:'Validation Level', width:"*", visible:false},
            {field:'componentStatus', displayName:'Component Status', width:"*", visible:false},
            {field:'countryCode', displayName:'Country Code', width:"*", visible:false}
        ]

    };


}])
// Branch site contact controller END
