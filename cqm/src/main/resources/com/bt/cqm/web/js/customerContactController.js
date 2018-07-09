// Customer Contacts controller BEGIN
module.controller('centralSiteContactController', ['$scope', 'UIService', 'customerContactService', 'customerService', '$modal', 'PageContext', 'UserContext', 'WebMetrics', '$rootScope', function ($scope, UIService, customerContactService, customerService, $modal, PageContext, UserContext, WebMetrics, $rootScope) {
    console.log('Inside centralSiteContactController');

    if (PageContext.exist()) {
        $scope.selectedSalesChannel = PageContext.getSalesChannel();
        $scope.customer = PageContext.getCustomer();
        $scope.contract = PageContext.getContract();
    }

    if (UserContext.exist()) {
        $scope.salesUser = UserContext.getUser();
    }

    $scope.availableContactRoles = [];
    var contactMsgTitle = 'Central Site Contact';
    var centralSiteMsgTitle = 'Central Site';

    $scope.loadContacts = function () {
        UIService.block();

        if (_.isUndefined(PageContext.getCentralSiteId())) {
            customerService.getCentralSite( $scope.contract.id, $scope.customer.cusId, function (data, status) {
                if (status == '404') {
                    var message = "No Central Site found for user: " + $scope.customer.cusName + "\n" + data;
                    UIService.unblock();
                    UIService.openDialogBox(centralSiteMsgTitle, message, true, false);
                    return;
                } else if (status == '500') {
                    console.log('Failed to load Central Site.');
                    UIService.unblock();
                    UIService.handleException(centralSiteMsgTitle, data, status);
                    return;
                }
                $scope.centralSite = data;
                if (!_.isUndefined(data) && !_.isUndefined(data.siteId)) {
                    PageContext.setCentralSiteId(data.siteId);
                }
                $scope.populateContacts($scope.customer.cusId, PageContext.getCentralSiteId());
                $scope.contactToBeUpdated = {};
                $scope.selectedCentralSiteContactAddress = [];
                UIService.unblock();
            });
        } else {
            $scope.populateContacts($scope.customer.cusId, PageContext.getCentralSiteId());
        }

    };

    $scope.centralSiteContactUI = {'enableCreateContactButton':false, 'enableUpdateContactButton':false};
    $scope.numOfCustomerContacts = 0;
    $scope.selectedCentralSiteContactAddress = [];
    $scope.contactToBeUpdated = {};

    $scope.centralSiteContactGrid = { data:'centralSiteContactData', selectedItems:$scope.selectedCentralSiteContactAddress,
        multiSelect:false, enableColumnResize:true,
        afterSelectionChange:function (item, event) {
            if (!item.selected) {
                console.log('got de-select event');
                return;
            }
            $scope.centralSiteContactUI.enableUpdateContactButton = true;
            $scope.index = item.rowIndex;
            $scope.fillCentralSiteContactFields(item.entity);
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

    $scope.fillCentralSiteContactFields = function (selectedCentralSiteContact) {
        $scope.centralSiteContactUI.enableCreateContactButton = false;
        $scope.centralSiteContactUI.enableUpdateContactButton = true;
        $scope.contactToBeUpdated.role = selectedCentralSiteContact.ctpType;
        $scope.cachedFormRole = selectedCentralSiteContact.ctpType;
        $scope.contactToBeUpdated.fax = selectedCentralSiteContact.contact.fax;
        $scope.contactToBeUpdated.mobileNumber = selectedCentralSiteContact.contact.mobileNumber;
        $scope.contactToBeUpdated.phoneNumber = selectedCentralSiteContact.contact.phoneNumber;
        $scope.contactToBeUpdated.email = selectedCentralSiteContact.contact.email;
        $scope.contactToBeUpdated.jobTitle = selectedCentralSiteContact.contact.jobTitle;
        $scope.contactToBeUpdated.lastName = selectedCentralSiteContact.contact.lastName;
        $scope.contactToBeUpdated.firstName = selectedCentralSiteContact.contact.firstName;
        $scope.contactToBeUpdated.contactID = selectedCentralSiteContact.contact.contactId;
        $scope.contactToBeUpdated.contactRoleId = selectedCentralSiteContact.id;
        $scope.contactToBeUpdated.bfgSiteId = selectedCentralSiteContact.siteId;
    };

    $scope.roles = ["PRIMARY CUSTOMER CONTACT",
        "SECONDARY CUSTOMER CONTACT",
        "Main Contact",
        "Secondary Contact",
        "Site Primary Contact",
        "Site Secondary Contact"
    ];

    $scope.populateContacts = function (customerId, siteId) {
        UIService.block();
        if (!_.isNull(siteId) && !_.isUndefined(siteId)) {
            customerContactService.getSiteContacts(customerId, siteId, function (data, status) {

                if (status == '200' || status == '404') {
                    if (_.isUndefined(data) || _.isUndefined(data.length) || data.length < 1) {
                        $scope.numOfCustomerContacts = 0;
                        $scope.centralSiteContactData = [];
                    } else if (data.length >= 1) {
                        $scope.centralSiteContactData = data;
                        $scope.numOfCustomerContacts = $scope.centralSiteContactData.length;
                        var isPrimaryCustomerContactExist = false;
                        var isMainContactExist = false;
                        var isSitePrimaryExist = false;

                        _.each($scope.centralSiteContactData, function (contact) {
                            if (contact.ctpType == $scope.roles[0]) {
                                isPrimaryCustomerContactExist = true;
                            } else if (contact.ctpType == $scope.roles[2]) {
                                isMainContactExist = true;
                            }else if (contact.ctpType == $scope.roles[4]) {
                                isSitePrimaryExist = true;
                            }
                        })

                        if ((isMainContactExist || isSitePrimaryExist) && isPrimaryCustomerContactExist) {
                            $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.VALID);
                        } else {
                            $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.INVALID);
                        }
                    }
                    UIService.unblock();
                    WebMetrics.captureWebMetrics(WebMetrics.UserActions.CentralSiteContacts);
                } else if (status == '500') {
                    UIService.unblock();
                    console.log('Failed to load Central Site Contact!!');
                    UIService.handleException(contactMsgTitle, data, status);
                }

                $scope.cacheAvailableContactRoles();
                UIService.unblock();
            });
        }

    };

    $scope.cacheAvailableContactRoles = function () {
        var role = $scope.contactToBeUpdated.role;
        if (!_.isEmpty($scope.centralSiteContactData)) {
            var exists = false;
            if ($scope.centralSiteContactData.length == undefined) {
                if (!_.contains($scope.availableContactRoles, $scope.centralSiteContactData.ctpType)) {
                    $scope.availableContactRoles.push($scope.centralSiteContactData.ctpType);
                }
                //$scope.roles.reduce()
            } else {
                for (var i = 0; i < $scope.centralSiteContactData.length; i++) {
                    if (!_.isEmpty($scope.centralSiteContactData[i]) && !_.isEmpty($scope.centralSiteContactData[i].ctpType)) {
                        if (!_.contains($scope.availableContactRoles, $scope.centralSiteContactData[i].ctpType)) {
                            $scope.availableContactRoles.push($scope.centralSiteContactData[i].ctpType);
                        }
                    }

                }
            }
        } else {
            $scope.availableContactRoles = [];
        }
    };


    $scope.createCentralSiteContact = function () {

        var userId = $scope.salesUser.ein;
        var salesChannel = $scope.selectedSalesChannel;
        var customerId = $scope.customer.cusId;
        var customerName = $scope.customer.cusName;
        $scope.siteId = "";
        UIService.block();

        // STORY GSCE-159036
        var phoneNumber =   $scope.contactToBeUpdated.phoneNumber;
        var mobileNumber =   $scope.contactToBeUpdated.mobileNumber;
        var contactRole =    $scope.contactToBeUpdated.role;
        var validPhoneNumber=true;
        var validMobileNumber=true;

       /* if (salesChannel.name == "BT FRANCE") {
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
        } */
        if(!validPhoneNumber && !validMobileNumber)
        {
            var message = "The entered Phone Number and Mobile Number is not valid. Please note that the  Number needs to start with +33 followed by 9 digits " ;
            UIService.openDialogBox(centralSiteMsgTitle, message, true, false);
            UIService.unblock();
            return;
        }

        else if(!validPhoneNumber)
        {
            var message = "The entered Phone Number is not valid. Please note that the Phone Number needs to start with +33 followed by 9 digits " ;
            UIService.openDialogBox(centralSiteMsgTitle, message, true, false);
            UIService.unblock();
            return;
        }
        else if(!validMobileNumber)
        {
            var message = "The entered Mobile Number is not valid. Please note that the Mobile Number needs to start with +33 followed by 9 digits " ;
            UIService.openDialogBox(centralSiteMsgTitle, message, true, false);
            UIService.unblock();
            return;
        }
        // STORY GSCE-159036
        var startTime = new Date().getTime();
        customerContactService.createCentralSiteContact(userId, customerId, PageContext.getCentralSiteId(), $scope.contactToBeUpdated, function (responseData, status) {
            var message = "";

            if (status == '200') {
                message = "Successfully created contact for " + customerName + ".";
                $scope.populateContacts($scope.customer.cusId, PageContext.getCentralSiteId());
                $scope.contactToBeUpdated = {};
                $scope.availableContactRoles = [];
                WebMetrics.captureWebMetrics('CQM Customer Tab - Create Central Site Contact', startTime);
            } else {
                console.log('Failed to Create Central site !!');
                UIService.handleException(centralSiteMsgTitle, responseData, status);
                return;
            }

            UIService.unblock();
            UIService.openDialogBox(centralSiteMsgTitle, message, true, false);
        });
    };

    $scope.updateCentralSiteContact = function () {
        var userId = $scope.salesUser.ein;
        var salesChannel = $scope.selectedSalesChannel;
        var customerId = $scope.customer.cusId;
        var customerName = $scope.customer.cusName;
        $scope.siteId = "";
        var title = 'Update Customer Contact';
        var message = "";

        UIService.block();
        // STORY GSCE-159036
        var phoneNumber =   $scope.contactToBeUpdated.phoneNumber;
        var mobileNumber =   $scope.contactToBeUpdated.mobileNumber;
        var contactRole =    $scope.contactToBeUpdated.role;
        var validPhoneNumber=true;
        var validMobileNumber=true;

        if (salesChannel.name == "BT FRANCE") {
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
            UIService.openDialogBox(centralSiteMsgTitle, message, true, false);
            UIService.unblock();
            return;
        }

        else if(!validPhoneNumber)
        {
            var message = "The entered Phone Number is not valid. Please note that the Phone Number needs to start with +33 followed by 9 digits " ;
            UIService.openDialogBox(centralSiteMsgTitle, message, true, false);
            UIService.unblock();
            return;
        }
        else if(!validMobileNumber)
        {
            var message = "The entered Mobile Number is not valid. Please note that the Mobile Number needs to start with +33 followed by 9 digits " ;
            UIService.openDialogBox(centralSiteMsgTitle, message, true, false);
            UIService.unblock();
            return;
        }
        // STORY GSCE-159036
        customerContactService.updateCentralSiteContact(userId, customerId, PageContext.getCentralSiteId(), $scope.contactToBeUpdated, function (responseData, status) {

            if (status == '200') {
                message = "Successfully updated contact for " + customerName + ".";
                $scope.populateContacts($scope.customer.cusId, PageContext.getCentralSiteId());
                $scope.contactToBeUpdated = {};
                $scope.availableContactRoles = [];
            } else {
                console.log('Failed to update central site contact !!');
                UIService.handleException(contactMsgTitle, responseData, status);
            }

            UIService.unblock();
            UIService.openDialogBox(title, message, true, false);
        });
    };

    $scope.updateCentralSiteContactUI = function () {
        /*Enable/Disable Create Button*/
        if (_.contains($scope.availableContactRoles, $scope.contactToBeUpdated.role)) {
            $scope.centralSiteContactUI.enableCreateContactButton = false;
        } else {
            $scope.centralSiteContactUI.enableCreateContactButton = true;
        }

        /*Enable/Disable Update Button*/
        if (_.contains($scope.availableContactRoles, $scope.contactToBeUpdated.role) && ($scope.cachedFormRole != $scope.contactToBeUpdated.role)) {
            $scope.centralSiteContactUI.enableUpdateContactButton = false;
        } else if (_.isUndefined($scope.contactToBeUpdated.bfgSiteId) || _.isEmpty($scope.contactToBeUpdated.bfgSiteId.toString())) {
            $scope.centralSiteContactUI.enableUpdateContactButton = false;
        } else {
            $scope.centralSiteContactUI.enableUpdateContactButton = true;
        }


    };
}]);
// Central site contact controller END
