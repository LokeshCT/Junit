var module = angular.module('cqm.controllers');

module.controller('billingAccountController', ['$scope', '$routeParams', '$http', 'countryService', 'httpService', '$modal', 'addressService', 'siteService', 'UIService', 'customerService', 'salesChannelService', 'billingAccountService', 'legalEntityService', 'PageContext', 'UserContext', 'WebMetrics', '$rootScope', function ($scope, $routeParams, $http, countryService, httpService, $modal, addressService, siteService, UIService, customerService, salesChannelService, billingAccountService, legalEntityService, PageContext, UserContext, WebMetrics, $rootScope) {

    if (PageContext.exist()) {
        $scope.customer = PageContext.getCustomer();
        $scope.contract = PageContext.getContract();
        $scope.selectedSalesChannel = PageContext.getSalesChannel();
    }

    if (UserContext.exist()) {
        $scope.salesUser = UserContext.getUser();
    }

    $scope.isDirectUser = false;
    $scope.isCpUser = true;
    $scope.customerList = salesChannelService.getCachedCustomerList();

    $scope.showMapLe = false;
    $scope.showMap = false;
    $scope.displayAccountGrid = true;
    $scope.displayAssLePanel = true;
    $scope.displayAccountDetPanel = true;
    $scope.numOfMatchingAddresses = 0;

    $scope.numOfMatchingAccounts = 0;

    $scope.showSearchResults = false;

    $scope.selectedNADAddress = [];

    $scope.selectedBillingAccountField = [];

    $scope.billingAccountUI = {'showSearchResultsDiv':false};

    $scope.billingAccountFormData = {};
    $scope.clarityProjectDetailsData = [];
    $scope.clarityProjectAssociationForm = {};
    $scope.clarityProjectAssociationForm.projectCode = null;
    $scope.clarityProjectAssociationForm.projectName = null;
    $scope.clarityProjectAssociationForm.sacId = null;

    $scope.legalEntityFormData = {};

    $scope.mapLegalEntityData = {};

    $scope.showInternalFields = false;

    $scope.showInternalFieldsReadOnly = false;

    // $scope.billingAccountUI.geoCodeSearch = false;

    $scope.bfgSiteIdForContacts = "";
    $scope.selectedLegalEntity = "";

    $scope.oldLeId = "";
    $scope.mappedLeId = "";

    $scope.disableUpdateBtnUI = true;
    $scope.isLoadLegalEntity = true;
    /* Re-usable Address Fields - STart*/
    $scope.cachedAddressDTO = undefined;
    $scope.disabled = true;
    $scope.disableSearch = true;
    $scope.disableSubmit = true;
    $scope.disableReset = true;
    $scope.hasResult = false;
    $scope.condReqNullFldCount = 0;
    $scope.emptyMandatoryFieldsArr = [];
    $scope.allCountries = countryService.getAllCountries();
    $scope.cachedAddressDTOStr = "";
    $scope.showAddressGrid = false;
    $scope.selectedAccount = false;
    $scope.defaultBillPeriod = '1';
    $scope.billPeriodPlaceholder = $scope.defaultBillPeriod;
    $scope.defaultBillPaymentDay = 30;
    $scope.billPaymentDayPlaceholder = $scope.defaultBillPaymentDay;
    $scope.billingAccountSearchData = {};
    $scope.selectedClarityProjectCode = null;
    var billingAccountMsgTitle = "Billing Account";
    var manageLEMsgTitle = "Manage LE";
    /*Re-usable Address Fields end*/

    $scope.allCountries = countryService.getAllCountries();

    $scope.currencyCodes = billingAccountService.getCurrencyCodes();

    $scope.getPeriodList = function (defaultValue) {
        $scope.periodList = ['1', '2', '3', '6', '12'];
        if (_.isUndefined(defaultValue) || _.isNull(defaultValue)) {
            $scope.billPeriodPlaceholder = $scope.defaultBillPeriod;
        } else {
            $scope.billPeriodPlaceholder = defaultValue;
        }

        var index = $scope.periodList.indexOf($scope.billPeriodPlaceholder);
        if (index == -1) {
            $scope.periodList.push($scope.billPeriodPlaceholder);
        }

        $scope.billingAccountFormData.billPeriod = $scope.billPeriodPlaceholder;
    }

    $scope.getPaymentDays = function (defaultValue) {

        $scope.paymentDaysList = [15, 30, 45, 60, 90, 120];

        if (_.isUndefined(defaultValue) || _.isNull(defaultValue)) {
            $scope.billPaymentDayPlaceholder = $scope.defaultBillPaymentDay;
        } else {
            $scope.billPaymentDayPlaceholder = defaultValue;
        }

        var index = $scope.paymentDaysList.indexOf($scope.billPaymentDayPlaceholder);
        if (index == -1) {
            $scope.paymentDaysList.push($scope.billPaymentDayPlaceholder);
        }

        $scope.billingAccountFormData.paymentDays = $scope.billPaymentDayPlaceholder;
    }


    $scope.accountClassificationList = ['External', 'Internal'];
    $scope.billingModeList = ['Contract', 'Product'];
    $scope.isAdrSearchSuccess = false;
    $scope.provRfo = '';
    $scope.zipRfo = '';
    $scope.zipRfoReq = false;
    $scope.provRfoReq = false;
    $scope.enableAssociateBillButton = false;
    $scope.billingCountryDisplayName = '';

    $scope.usScenario = [
        {name:'Billed in US, invoice is sent to customer', value:'1'},
        {name:'Billed in US, invoice is sent to incountry team', value:'2'},
        {name:'Billed in US, and charges manually inserted into local country bill', value:'3'},
        {name:'Tax calculated in US, and electronically included in the bill', value:'4'}
    ];

    $scope.invoiceLanguage = ['Dutch (Holland)', 'English (US)', 'English (UK)', 'French (Franch)', 'German (Germany)', 'Italian (Italy)', 'Spanish (Spain)', 'Catalan (Spain)', 'Swedish (Sweden)', 'Internal', ''];

    $scope.loadPage = function () {
        $scope.gridSelected = false;
        $scope.isAdrSearchSuccess = false;
        $scope.enableAssociateBillButton = false;
        $scope.selectedAccount = false;
        $scope.isDirectUser = UserContext.isDirectUser();
        countryService.broadcastSelectedCountryChangedEvent('');
        $scope.isCpUser = UserContext.getRole().roleName == 'CP User' ? true : false;
        $scope.isLoadLegalEntity = true;
        $scope.getPaymentDays();
        $scope.getPeriodList();
        if ($scope.customer != undefined) {
            $scope.getBillingAccountList($scope.customer.cusId, $scope.contract.id);
            $scope.getCustomerAdditionalDetail()
        }
    };


    $scope.loadPageForAdvancedBilling = function () {
        UIService.block();
        $scope.gridSelected = true;
        $scope.isDirectUser = UserContext.isDirectUser();
        $scope.isLoadLegalEntity = false;
        if ($scope.customer != undefined) {
            $scope.getBillingAccountList($scope.customer.cusId, $scope.contract.id);
        }
        UIService.unblock();
    };
    $scope.$on('selectedCountryChanged', function (event, countryName) {
        console.log('inside the selectedCountryChanged listener.');
        $scope.processCountryChange(countryName);
    });


    $scope.$watch('billingAccountFormData.buildingName', function (buildingName) {
        $scope.checkAddressValidation();
    });

    $scope.$watch('billingAccountFormData.buildingNumber', function (buildingName) {
        $scope.checkAddressValidation();
    });

    $scope.$watch('billingAccountFormData.street', function (buildingName) {
        $scope.checkAddressValidation();
    });

    $scope.$watch('billingAccountFormData.locality', function (buildingName) {
        $scope.checkAddressValidation();
    });

    $scope.$watch('billingAccountFormData.POBox', function (buildingName) {
        $scope.checkAddressValidation();
    });

    $scope.$watch('billingAccountFormData.country', function (country) {
        if (!_.isUndefined(country)) {
            $scope.processCountryChange(country.name);
        }
        $scope.validateAddrSearch();
    });

    $scope.$watch('billingAccountFormData.city', function (city) {
        $scope.validateAddrSearch();
    });

    $scope.validateAddrSearch = function () {

        if ((!_.isUndefined($scope.billingAccountFormData.country) && _.isEmpty($scope.billingAccountFormData.country.name)) || _.isEmpty($scope.billingAccountFormData.city)) {
            $scope.disableSearch = true;
        } else {
            $scope.disableSearch = false;
        }

    };

    $scope.checkAddressValidation = function () {

        if (_.isEmpty($scope.billingAccountFormData.POBox)) {
            var hasBuilding = false;
            var hasStreet = false;

            if (!_.isEmpty($scope.billingAccountFormData.buildingName) || !_.isEmpty($scope.billingAccountFormData.buildingNumber)) {
                $scope.isBuildingNumReq = false;
                $scope.isBuildingNameReq = false;
                hasBuilding = true;
            } else {
                $scope.isBuildingNumReq = true;
                $scope.isBuildingNameReq = true;
            }

            if (!_.isEmpty($scope.billingAccountFormData.street) || !_.isEmpty($scope.billingAccountFormData.locality)) {
                $scope.isLocalityReq = false;
                $scope.isStreetReq = false;
                hasStreet = true;
            } else {
                $scope.isLocalityReq = true;
                $scope.isStreetReq = true;
            }

            if (hasStreet && hasBuilding) {
                $scope.isPoBoxReq = false;
            } else {
                $scope.isPoBoxReq = true;
            }

        } else {
            $scope.isLocalityReq = false;
            $scope.isStreetReq = false;
            $scope.isBuildingNumReq = false;
            $scope.isBuildingNameReq = false;

        }
    }

    $scope.processCountryChange = function (countryName) {
        if (!_.isUndefined(countryName)) {
            _.forEach($scope.allCountries, function (country) {
                if (country.name.toUpperCase() == countryName.toUpperCase()) {
                    $scope.billingAccountFormData.country = country;

                    var hasMatchingZip = countryService.isZipRequired(country.name.toUpperCase());

                    if (hasMatchingZip) {
                        $scope.zipRfo = '(RFO)';
                        $scope.zipRfoReq = true;
                    } else {
                        $scope.zipRfo = '';
                        $scope.zipRfoReq = false;
                    }

                    var hasMatchingProv = countryService.isProvinceRequired(country.name.toUpperCase());
                    if (hasMatchingProv) {
                        $scope.provRfo = '(RFO)';
                        $scope.provRfoReq = true;
                    } else {
                        $scope.provRfo = '';
                        $scope.provRfoReq = false;
                    }

                    return;
                }
            });
        }
    };

    $scope.onClearClick = function () {
        if ($scope.clearLatLong) {
            $scope.cachedLat = $scope.billingAccountFormData.latitude;
            $scope.cachedLng = $scope.billingAccountFormData.longitude;
            $scope.billingAccountFormData.latitude = '';
            $scope.billingAccountFormData.longitude = '';
        } else {
            $scope.billingAccountFormData.latitude = $scope.cachedLat;
            $scope.billingAccountFormData.longitude = $scope.cachedLng;
        }
    }

    $scope.addressGrid = { data:'addressSearchResultData', selectedItems:$scope.selectedNADAddress,
        multiSelect:false,
        enableColumnResize:true,
        showGroupPanel:true,
        showColumnMenu:true,
        showFilter:true,
        rowTemplate:'<div style="height: 100%" ng-class="{green: row.getProperty(\'colorCode\') == \'Green\'}">' +
                    '<div style="height: 100%" ng-class="{red: row.getProperty(\'colorCode\') == \'Red\'}">' +
                    '<div style="height: 100%" ng-class="{amber: row.getProperty(\'colorCode\') == \'Amber\'}">' +
                    '<div ng-style="{ \'cursor\': row.cursor }" ng-repeat="col in renderedColumns" ng-class="col.colIndex()" class="ngCell ">' +
                    '<div class="ngVerticalBar" ng-style="{height: rowHeight}" ng-class="{ ngVerticalBarVisible: !$last }"> </div>' +
                    '<div ng-cell></div>' +
                    '</div></div>',
        afterSelectionChange:function (item, event) {
            if (item.selected == false) {
                console.log('got de-select event');
                return;
            }
            $scope.clearLatLong = false;
            $scope.updateAddressToForm(item.entity);
        },
        columnDefs:[
            {field:'buildingName', displayName:'Building Name', width:120},
            {field:'subBuilding', displayName:'Sub Building', width:120},
            {field:'buildingNumber', displayName:'Building Number', width:140},
            {field:'street', displayName:'Street', width:120},
            {field:'subStreet', displayName:'Sub Street', width:120},
            {field:'locality', displayName:'Locality', width:120},
            {field:'subLocality', displayName:'Sub Locality', width:120},
            {field:'city', displayName:'City', width:120},
            {field:'state', displayName:'State/Province', width:120},
            {field:'subState', displayName:'Sub State/County/Province', width:120},
            {field:'country', displayName:'Country', width:120},
            {field:'zipCode', displayName:'Zip/Post Code', width:120},
            {field:'subZipCode', displayName:'Sub Post Code', width:120},
            {field:'poBox', displayName:'PO Box', width:120},
            {field:'postalOrganisation', displayName:'Postal Organisation', width:120},
            {field:'latitude', displayName:'Latitude', width:120},
            {field:'longitude', displayName:'Longitude', width:120},
            {field:'managePlaceResult', displayName:'Manage Place Result', width:120},
            {field:'accuracyLevel', displayName:'Accuracy Level', width:240},
            {field:'failLevel', displayName:'Fail Level', width:120},
            {field:'validationLevel', displayName:'Validation Level', width:240},
            {field:'componentStatus', displayName:'Component Status', width:240},
            {field:'countryCode', displayName:'Country Code', width:240}
        ]
    };
    $scope.clarityProjectDetailsGrid = {data:'clarityProjectDetailsData', multiSelect:false, enableColumnResize:true,
        showGroupPanel:true,
        showColumnMenu:true,
        showFilter:true,
        afterSelectionChange:function (item, event) {
            $scope.selectedClarityProjectCode = item.entity.projectId;
        },
        columnDefs:[
            {field:'projectId', displayName:'Project Code', width:200},
            {field:'projectTitle', displayName:'Project Name', width:200},
            {field:'projectDescription', displayName:'Description', width:250},
            {field:'projectStartDateAsString', displayName:'Project Start Date', width:200},
            {field:'projectEndDateAsString', displayName:'Project End Date', width:200}
        ]};
    $scope.searchAddress = function () {
        console.log('Search address invoked');
        UIService.block();
        //$scope.branchSiteUI.geoCodeSearch = false;

        var nadAddressRequestDTO = new Object();
        var queryParamObject = new Object();
        queryParamObject.city = $scope.billingAccountFormData.city;
        queryParamObject.country = $scope.billingAccountFormData.country.name;
        queryParamObject.postCode = $scope.billingAccountFormData.postCode;
        if (queryParamObject.postCode == "" || queryParamObject.postCode == undefined) {
            queryParamObject.postCode = "0";
        }

        queryParamObject.locality = $scope.billingAccountFormData.locality;
        queryParamObject.countryCode = $scope.billingAccountFormData.country.codeAlpha2;
        queryParamObject.building = $scope.billingAccountFormData.buildingName;
        queryParamObject.street = $scope.billingAccountFormData.street;
        queryParamObject.buildingNumber = $scope.billingAccountFormData.buildingNumber;
        queryParamObject.subStreet = $scope.billingAccountFormData.subStreet;
        queryParamObject.subLocality = $scope.billingAccountFormData.subLocality;
        queryParamObject.state = $scope.billingAccountFormData.state;
        queryParamObject.poBox = $scope.billingAccountFormData.POBox;
        queryParamObject.subState = $scope.billingAccountFormData.subState;
        queryParamObject.subPostCode = $scope.billingAccountFormData.subPostCode;
        queryParamObject.subBuilding = $scope.billingAccountFormData.subBuilding;
        queryParamObject.latitude = $scope.billingAccountFormData.latitude;
        queryParamObject.longitude = $scope.billingAccountFormData.longitude;

        nadAddressRequestDTO.q = JSON.stringify(queryParamObject);

        var processResponseFunc = function (data, status) {
            if ('200' == status) {
                if (data["addressDTOList"] == undefined) {
                    $scope.numOfMatchingAddresses = 0;
                    return;
                }
                if (data["addressDTOList"].length == undefined) {
                    $scope.numOfMatchingAddresses = 1;
                    $scope.addressSearchResultData = [ data["addressDTOList"] ];
                } else {
                    $scope.addressSearchResultData = data["addressDTOList"];
                    $scope.numOfMatchingAddresses = $scope.addressSearchResultData.length;
                }
                $scope.operationResult = "Found: " + $scope.addressSearchResultData.length;
                $scope.isAdrSearchSuccess = true;
                window.setTimeout(function () {
                    $(window).resize();
                }, 1);

            } else if ('404' == status) {
                $scope.isAdrSearchSuccess = false;
                UIService.openDialogBox('Search Address', data.description, true, false);
            } else {
                $scope.isAdrSearchSuccess = false;
                UIService.handleException('Search Address', data, status);
            }
            UIService.unblock();
            console.log("Fetched Addresses: ", $scope.addressSearchResultData);
        };

        addressService.searchAddress(nadAddressRequestDTO, processResponseFunc);
    };


    $scope.getGeoCode = function () {
        UIService.block();

        var nadAddressRequestDTO = new Object();
        var queryParamObject = new Object();
        queryParamObject.city = $scope.billingAccountFormData.city;
        queryParamObject.country = $scope.billingAccountFormData.country.name;
        queryParamObject.postCode = $scope.billingAccountFormData.postCode;
        if (queryParamObject.postCode == "" || queryParamObject.postCode == undefined) {
            queryParamObject.postCode = "0";
        }
        queryParamObject.locality = $scope.billingAccountFormData.locality;
        queryParamObject.countryCode = $scope.billingAccountFormData.country.codeAlpha2;
        queryParamObject.building = $scope.billingAccountFormData.buildingName;
        queryParamObject.street = $scope.billingAccountFormData.street;
        queryParamObject.buildingNumber = $scope.billingAccountFormData.buildingNumber;
        queryParamObject.subStreet = $scope.billingAccountFormData.subStreet;
        queryParamObject.subLocality = $scope.billingAccountFormData.subLocality;
        queryParamObject.state = $scope.billingAccountFormData.state;
        queryParamObject.poBox = $scope.billingAccountFormData.poBox;
        queryParamObject.company = $scope.billingAccountFormData.company;
        queryParamObject.subState = $scope.billingAccountFormData.subState;
        queryParamObject.subPostCode = $scope.billingAccountFormData.subPostCode;
        queryParamObject.subBuilding = $scope.billingAccountFormData.subBuilding;
        queryParamObject.latitude = $scope.billingAccountFormData.latitude;
        queryParamObject.longitude = $scope.billingAccountFormData.longitude;
        nadAddressRequestDTO.q = JSON.stringify(queryParamObject);

        var processResponseFunc = function (data, status) {
            if ('200' == status) {
                if (data.errorDesc == 'SUCCESS') {
                    if (data["addressDTOList"] == undefined) {
                        //$scope.numOfMatchingAddresses = 0;
                        return;
                    }
                    if (data["addressDTOList"].length == undefined) {
                        //$scope.numOfMatchingAddresses = 1;
                        $scope.addressWithLongAndLat = [ data["addressDTOList"] ];
                    } else {
                        $scope.addressWithLongAndLat = data["addressDTOList"];
                        //$scope.numOfMatchingAddresses = $scope.addressSearchResultData.length;
                    }
                    $scope.billingAccountFormData.longitude = $scope.addressWithLongAndLat[0].longitude;
                    $scope.billingAccountFormData.latitude = $scope.addressWithLongAndLat[0].latitude;
                    window.setTimeout(function () {
                        $(window).resize();
                    }, 1);

                } else {
                    $scope.billingAccountFormData.longitude = '';
                    $scope.billingAccountFormData.latitude = '';
                    UIService.handleException('Get Geo-Code', data, status);
                }
            } else {
                $scope.billingAccountFormData.longitude = '';
                $scope.billingAccountFormData.latitude = '';
                UIService.handleException('Get Geo-Code', data, status);
            }
            UIService.unblock();
        };
        addressService.searchAddressWithGeoCode(nadAddressRequestDTO, processResponseFunc);
    };

    $scope.updateAddressToForm = function (selectedAddress) {
        if (!_.isUndefined(selectedAddress)) {
            var msg = 'Do you want to update current address details with the one you Selected from Search Results?';
            var dialogInstance = UIService.openDialogBox('Update Address Details', msg, true, true);
            dialogInstance.result.then(function () {
                if (!_.isUndefined(selectedAddress.buildingNumber) && !_.isEmpty(selectedAddress.buildingNumber)) {
                    $scope.billingAccountFormData.buildingNumber = selectedAddress.buildingNumber;
                }

                if (!_.isUndefined(selectedAddress.buildingName) && !_.isEmpty(selectedAddress.buildingName)) {
                    $scope.billingAccountFormData.buildingName = selectedAddress.buildingName;
                }

                if (!_.isUndefined(selectedAddress.locality) && !_.isEmpty(selectedAddress.locality)) {
                    $scope.billingAccountFormData.locality = selectedAddress.locality;
                }

                if (!_.isUndefined(selectedAddress.street) && !_.isEmpty(selectedAddress.street)) {
                    $scope.billingAccountFormData.street = selectedAddress.street;
                }

                if (!_.isUndefined(selectedAddress.subLocality) && !_.isEmpty(selectedAddress.subLocality)) {
                    $scope.billingAccountFormData.subLocality = selectedAddress.subLocality;
                }

                if (!_.isUndefined(selectedAddress.poBox) && !_.isEmpty(selectedAddress.poBox)) {
                    $scope.billingAccountFormData.POBox = selectedAddress.poBox;
                }

                if (!_.isUndefined(selectedAddress.subStreet) && !_.isEmpty(selectedAddress.subStreet)) {
                    $scope.billingAccountFormData.subStreet = selectedAddress.subStreet;
                }

                if (!_.isUndefined(selectedAddress.subZipCode) && !_.isEmpty(selectedAddress.subZipCode)) {
                    $scope.billingAccountFormData.subPostCode = selectedAddress.subZipCode;
                }
                if (!_.isUndefined(selectedAddress.subState) && !_.isEmpty(selectedAddress.subState)) {
                    $scope.billingAccountFormData.subState = selectedAddress.subState;
                }
                if (!_.isUndefined(selectedAddress.subBuilding) && !_.isEmpty(selectedAddress.subBuilding)) {
                    $scope.billingAccountFormData.subBuilding = selectedAddress.subBuilding;
                }
                if (!_.isUndefined(selectedAddress.poBox) && !_.isEmpty(selectedAddress.poBox)) {
                    $scope.billingAccountFormData.POBox = selectedAddress.poBox;
                }
                $scope.billingAccountFormData.postCode = selectedAddress.zipCode;
                $scope.billingAccountFormData.state = selectedAddress.state;
                $scope.billingAccountFormData.longitude = selectedAddress.longitude;
                $scope.billingAccountFormData.latitude = selectedAddress.latitude;
                $scope.getGeoCode();

            }, function () {})
        }
    }


    $scope.billingAccountGrid = { data:'billingAccountSearchData', selectedItems:$scope.selectedBillingAccountField,
        multiSelect:false,
        enableColumnResize:true,
        showGroupPanel:true,
        showColumnMenu:true,
        showFilter:true,

        afterSelectionChange:function (item, event) {
            if (item.selected == false) {
                console.log('Selected a billing account');
                $scope.billingAccountFormData.siteId = undefined;
                $scope.billingAccountFormData.siteName = undefined;
                $scope.billingAccountFormData.billingAccountId = undefined;
                $scope.disableUpdateBtnUI = true;
                $scope.selectedAccount = false;
                return;
            }
            $scope.clearLatLong = false;
            $scope.legalEntityFormData = {};
            $scope.gridSelected = true;
            $scope.fillBillingAccountFields(item.entity);
            $scope.fillLegalEntityFields(item.entity);
            $scope.isAdrSearchSuccess = false;
            $scope.selectedAccount = true;
            if ((_.isNumber($scope.billingAccountFormData.billingAccountId) && !_.isEmpty($scope.billingAccountFormData.billingAccountId.toString())) || (_.isString($scope.billingAccountFormData.billingAccountId) && !_.isEmpty($scope.billingAccountFormData.billingAccountId))) {
                $scope.disableUpdateBtnUI = false;
            }
        },
        columnDefs:[
            {field:'accountFriendlyName', displayName:'Account Name', width:120},
            {field:'accountReference', displayName:'Account Reference', width:160},
            //{field:'customerBillingReference', displayName:'Customer Billing Reference', width:120},
            {field:'billPeriod', displayName:'Bill Period', width:120},
            {field:'language', displayName:'Invoice Language', width:140},
            {field:'currencyName', displayName:'Billing Currency', width:140},
            {field:'paymentOption', displayName:'Payment Method', width:140},
            {field:'creditClassdays', displayName:'Payment Days', width:120},
            {field:'infoCurrencyName', displayName:'Info Currency', width:140},
            {field:'address.latitude', displayName:'Latitude', width:160},
            {field:'address.longitude', displayName:'Longitude', width:160},
            {field:'address.country.codeAlpha2', displayName:'Country Code', width:160},
            {field:'address.accuracyLevel', displayName:'Accuracy Level', width:160},
            {field:'address.validationLevel', displayName:'Fail Level', width:160},
            //{field:'ediAddress', displayName:'EDI Address', width:120},
            {field:'touchBillingOption', displayName:'US Scenario', width:120},
            {field:'originatorGfrCode', displayName:'Originating GFR', width:120},
            {field:'originatorOuc', displayName:'Originating OUC', width:120},
            {field:'receiverOuc', displayName:'Receiving OUC', width:120},
            //{field:'touchBillingOption', displayName:'TCA Number', width:120},
            {field:'activationDateInString', displayName:'Activation Date', width:140},
            {field:'clarityProjectCode', displayName:'Project Code', width:140}
        ]
    };

    $scope.fillBillingAccountFields = function (selectedBillingAccount) {
        $scope.billingAccountFormData.billingAccountId = selectedBillingAccount.billingAccountId;
        $scope.billingAccountFormData.infoCurrId = selectedBillingAccount.infoCurrId;
        $scope.billingAccountFormData.billingCurrencyId = selectedBillingAccount.billingCurrencyId;
        $scope.billingAccountFormData.accountFriendlyName = selectedBillingAccount.accountFriendlyName;
        $scope.billingAccountFormData.accountReference = selectedBillingAccount.accountReference;

        var billPeriod = selectedBillingAccount.billPeriod;
        if (!(_.isUndefined(billPeriod) || _.isNull(billPeriod))) {
            var periodNUnit = billPeriod.trim().split(' ');
            var period = periodNUnit[0];
            var unit = periodNUnit[1];
            if (!(_.isUndefined(unit) || _.isNull(unit)) && _.isEqual(unit, 'M')) {
                $scope.billingAccountFormData.billPeriod = period;
            } else {
                $scope.billingAccountFormData.billPeriod = billPeriod;
            }
        }

        $scope.billingAccountFormData.billingCurrency = selectedBillingAccount.currencyName;
        $scope.billingAccountFormData.paymentMethod = selectedBillingAccount.paymentOption;
        $scope.billingAccountFormData.paymentDays = parseInt(selectedBillingAccount.creditClassdays);
        $scope.billingAccountFormData.vatNumber = selectedBillingAccount.overrideVatNumber;
        $scope.billingAccountFormData.taxExemptionCode = selectedBillingAccount.overrideTaxExemptRef;
        $scope.billingAccountFormData.usScenario = selectedBillingAccount.touchBillingOption;
        $scope.billingAccountFormData.activationDate = selectedBillingAccount.activationDate;
        $scope.billingAccountFormData.invoiceLanguage = selectedBillingAccount.language;
        $scope.billingAccountFormData.customerBillingReference = selectedBillingAccount.clientBillRef;
        $scope.billingAccountFormData.contractId = $scope.contract.id;
        $scope.billingAccountFormData.siteId = selectedBillingAccount.siteId;
        $scope.billingAccountFormData.siteName = selectedBillingAccount.siteName;
        $scope.billingAccountFormData.sitReference = selectedBillingAccount.sitReference;

        var activationDate = selectedBillingAccount.activationDateInString;
        if (!(_.isUndefined(activationDate) || _.isUndefined(activationDate))) {
            $scope.billingAccountFormData.activationDate = activationDate;

        }

        var legalEntity = selectedBillingAccount.legalEntity;
        if (!(_.isUndefined(legalEntity) || _.isNull(legalEntity))) {
            $scope.billingAccountFormData.leID = legalEntity.leId;
        }

        var contactRole = selectedBillingAccount.contactRole;

        if (!(_.isUndefined(contactRole) || _.isNull(contactRole))) {
            $scope.billingAccountFormData.contactRoleId = contactRole.id;
            $scope.billingAccountFormData.contactRoleType = contactRole.ctpType;

            var contact = contactRole.contact;
            if (!_.isUndefined(contact)) {
                $scope.billingAccountFormData.contactId = contact.contactId;
                $scope.billingAccountFormData.firstName = contact.firstName;
                $scope.billingAccountFormData.lastName = contact.lastName;
                $scope.billingAccountFormData.middleName = contact.middleName;
                $scope.billingAccountFormData.jobTitle = contact.jobTitle;
                $scope.billingAccountFormData.phoneNumber = contact.phoneNumber;
                $scope.billingAccountFormData.mobileNo = contact.mobileNumber;
                $scope.billingAccountFormData.fax = contact.fax;
                $scope.billingAccountFormData.email = contact.email;
            }
        }

        var address = selectedBillingAccount.address;

        if (!_.isUndefined(address)) {
            $scope.billingAccountFormData.adrId = address.adrId;
            $scope.billingAccountFormData.buildingName = address.sitePremise;
            $scope.billingAccountFormData.buildingNumber = address.streetNo;
            $scope.billingAccountFormData.street = address.streetName;
            $scope.billingAccountFormData.subStreet = address.subStreet;
            $scope.billingAccountFormData.locality = address.locality;
            $scope.billingAccountFormData.city = address.town;
            $scope.billingAccountFormData.state = address.county;
            $scope.billingAccountFormData.postCode = address.postZipCode;
            $scope.billingAccountFormData.subPostCode = address.subPostCode;
            $scope.billingAccountFormData.latitude = address.latitude;
            $scope.billingAccountFormData.longitude = address.longitude;
            $scope.billingAccountFormData.POBox = address.poBoxNo;
            $scope.billingAccountFormData.subLocality = address.subLocality;
            $scope.billingAccountFormData.subBuilding = address.subBuilding;
            $scope.billingAccountFormData.subPostCode = address.subPostCode;
            $scope.billingAccountFormData.subState = address.subCountyStateProvince;

            var country = address.country;
            if (!_.isUndefined(country)) {
                countryService.broadcastSelectedCountryChangedEvent(country.name);
            }


        }

        $scope.getPeriodList($scope.billingAccountFormData.billPeriod);
        $scope.getPaymentDays($scope.billingAccountFormData.paymentDays);
        UIService.block()
        billingAccountService.getClarityProjectCodeDetails(selectedBillingAccount.clarityProjectCode, null, null, function (responsedata, status) {
            if (status == 200) {
                $scope.clarityProjectDetailsData = responsedata;
            } else {
                console.log("Failed to get clarity project details.");
            }
            UIService.unblock();
        })
    }

    $scope.fillLegalEntityFields = function (selectedBillingAccount) {
        if (!_.isUndefined(selectedBillingAccount) && !_.isUndefined(selectedBillingAccount.legalEntity) && !_.isNull(selectedBillingAccount.legalEntity)) {
            $scope.oldLeId = selectedBillingAccount.legalEntity.leId;
            $scope.legalEntityFormData.leCompanyName = selectedBillingAccount.legalEntity.leName;
            $scope.legalEntityFormData.registeredCountry = selectedBillingAccount.legalEntity.address.countryName;
            $scope.legalEntityFormData.billingCountry = selectedBillingAccount.originatorGfrCode;

            $scope.mapLegalEntityData.leName = selectedBillingAccount.legalEntity;

            $scope.mapLegalEntityData.leName = _.find($scope.legalEntityList, function (obj) {

                if (obj.leName == selectedBillingAccount.legalEntity.leName) {
                    return obj;
                }
            });

            $scope.mapLegalEntityData.accountClassification = selectedBillingAccount.accType;
            $scope.mapLegalEntityData.billingCountry = selectedBillingAccount.originatorGfrCode;
            $scope.legalEntityFormData.registeredCountry = selectedBillingAccount.legalEntity.address.countryName;
            $scope.legalEntityFormData.billingCountry = selectedBillingAccount.originatorGfrCode;
            if (_.isEmpty(selectedBillingAccount.billingMode)) {
                if (PageContext.getIsMNC() || $scope.selectedSalesChannel.name == 'MANAGED SERVICES FROM BT') {
                    $scope.mapLegalEntityData.billingMode = "Contract";
                    $scope.legalEntityFormData.billingMode = "Contract";
                }
                else {
                    $scope.mapLegalEntityData.billingMode = "Product";
                    $scope.legalEntityFormData.billingMode = "Product";
                }
            }
            else {
                if (selectedBillingAccount.billingMode == "MANUAL") {
                    $scope.mapLegalEntityData.billingMode = "Contract";
                    $scope.legalEntityFormData.billingMode = "Contract";
                }
                else {
                    $scope.mapLegalEntityData.billingMode = "Product";
                    $scope.legalEntityFormData.billingMode = "Product";

                }
            }

            if (!isNaN($scope.legalEntityFormData.billingCountry)) {
                $scope.billingCountryDisplayName = $scope.selectedSalesChannel.name;
            } else {
                $scope.billingCountryDisplayName = $scope.legalEntityFormData.billingCountry;
            }


            $scope.legalEntityFormData.leAccountClassification = selectedBillingAccount.accType;
            if (selectedBillingAccount.accType == "Internal") {
                $scope.showInternalFields = true;
                $scope.mapLegalEntityData.receivinggfr = selectedBillingAccount.receiverGrfCode;
                $scope.mapLegalEntityData.originatingouc = selectedBillingAccount.originatorOuc;
                $scope.mapLegalEntityData.receivingouc = selectedBillingAccount.receiverOuc;
                $scope.mapLegalEntityData.tcaNumber = selectedBillingAccount.transfrChgAgrmntCode;

            }
            else {
                $scope.showInternalFields = false;
                $scope.mapLegalEntityData.receivinggfr = "";
                $scope.mapLegalEntityData.originatingouc = "";
                $scope.mapLegalEntityData.receivingouc = "";
                $scope.mapLegalEntityData.tcaNumber = "";
            }
        }

    };

    $scope.getBillingAccountList = function (customerId, contractId) {
        var message = "";

        UIService.block();
        $scope.isAddressSelected = false;
        $scope.billingAccountSearchData = {};
        billingAccountService.getBillingAccounts(customerId, contractId, function (data, status) {
            if (status == '200') {
                WebMetrics.captureWebMetrics(WebMetrics.UserActions.BillingAccounts);
                $scope.numOfMatchingAccounts = data.length;
                $scope.billingAccountSearchData = data;
            } else if (status = '404') {
                $scope.numOfMatchingAccounts = 0;
                /*message = "No Billing Account found for the Customer ID " + customerId;
                 UIService.openDialogBox(billingAccountMsgTitle, message, true, false);*/
            } else {
                UIService.handleException(billingAccountMsgTitle, data, status);
            }
            if ($scope.isLoadLegalEntity) {
                $scope.loadLegalEntity();
            }
            UIService.unblock();


        });
    };

    $scope.checkBillPeriod = function () {

        var billPeriodList = ['1', '2', '3', '6', '12'];
        $scope.isBillPeriodAndPaymentValid = true;
        var title = 'Create/update billing Details';

        if (billPeriodList.indexOf($scope.billingAccountFormData.billPeriod) == -1) {

            var message = "Please enter correct Billing Period. Billing period should be among of 1, 2, 3, 6, 12 ";
            UIService.openDialogBox(title, message, true, false);
            return;
        }


        if ($scope.billingAccountFormData.billPeriod != $scope.defaultBillPeriod && $scope.billingAccountFormData.billPeriod != $scope.billPeriodPlaceholder) {

            $scope.displayNonStandredMessage("billPeriod");

        }


    }

    $scope.checkBillPaymentDays = function () {
        var billPaymentDays = [15, 30, 45, 60, 90, 120];
        var title = 'Create/update billing Details';
        if (billPaymentDays.indexOf($scope.billingAccountFormData.paymentDays) == -1) {

            var message = "Please enter correct Payment Day. Payment Day should be among of 15, 30, 45, 60, 90, 120 ";
            UIService.openDialogBox(title, message, true, false);
            return;
        }

        if ($scope.billingAccountFormData.paymentDays != $scope.defaultBillPaymentDay && $scope.billingAccountFormData.paymentDays != $scope.billPaymentDayPlaceholder) {

            $scope.displayNonStandredMessage("billPaymentDayas");

        }

    }

    $scope.displayNonStandredMessage = function (billPeriodOrbillPaymentDayas) {
        var title = 'Create/update billing Details';

        var message = "You have selected a non-standard value.  " + "Deviations from the standard can only be offered" + " to a customer if the non-standard terms have been" +
                      " authorised by someone with appropriate commercial" + " delegated authority.  Please see the Win Business process.  " +
                      "By clicking OK, you are confirming that such authorisation has been obtained.";

        var dialogInstance = UIService.openDialogBox(title, message, true, true);
        dialogInstance.result.then(function () {
            return;
        }, function () {
            if (billPeriodOrbillPaymentDayas == "billPaymentDayas") {
                $scope.getPaymentDays($scope.billPaymentDayPlaceholder);
            } else {
                $scope.getPeriodList($scope.billPeriodPlaceholder);
            }
            return;
        });
    }


    $scope.createBillingAccount = function () {

        $scope.isBillPeriodAndPaymentValid = true;
        var title = 'Create billing Details';

        if (_.isUndefined($scope.legalEntityFormData.leCompanyName) || _.isNull($scope.legalEntityFormData.leCompanyName)) {

            var message = "Please Create Legal Entity,Legal Entity is Mandatory to Create Billing Account.";
            UIService.openDialogBox(title, message, true, false);
            return;
        }

        $scope.executeCreateBillingAccount();
    };

    $scope.executeCreateBillingAccount = function () {

        var salesChannel = $scope.selectedSalesChannel.name;

        var cusName = $scope.customer.cusName;
        var cusId = $scope.customer.cusId;
        if (cusId == null || cusId == undefined || cusId == "") {
            cusId = " ";
        }
        var userId = $scope.salesUser.ein;
        var userName = $scope.salesUser.name;
        UIService.block();
        $scope.billingAccountFormData.leCompanyName = $scope.legalEntityFormData.leCompanyName;
        //$scope.billingAccountFormData.registeredCountry =$scope.legalEntityFormData.registeredCountry;
        //Need to modified
        $scope.billingAccountFormData.originatorGfrCode = $scope.legalEntityFormData.billingCountry;
        $scope.billingAccountFormData.receiverGrfCode = $scope.legalEntityFormData.receivergfr;
        $scope.billingAccountFormData.originatorOuc = $scope.legalEntityFormData.originatorouc;
        $scope.billingAccountFormData.receiverOuc = $scope.legalEntityFormData.receiverouc;
        $scope.billingAccountFormData.transfrChgAgrmntCode = $scope.legalEntityFormData.tcaCode;
        $scope.billingAccountFormData.clarityProjectCode = $scope.selectedClarityProjectCode;


        if ($scope.legalEntityFormData.billingMode == "Contract") {
            $scope.billingAccountFormData.billingMode = "MANUAL";
        }
        else {   //Product
            $scope.billingAccountFormData.billingMode = "AUTOMATED";
        }


        if ($scope.mappedLeId != undefined && $scope.mappedLeId != "")
            $scope.billingAccountFormData.leID = $scope.mappedLeId;
        var leAccountClassification = $scope.legalEntityFormData.leAccountClassification;
        $scope.billingAccountFormData.accType = $scope.legalEntityFormData.leAccountClassification;

        billingAccountService.createBillingAccount(userId, $scope.contract.id, cusId, $scope.billingAccountFormData, function (data, status) {
            var title = 'Create billing Details';
            var message = "";
            if (status == '200') {
                message = 'Successfully created Billing Account.' + '\n' + data;
                $scope.billingAccountFormData = {};
                $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.VALID);
                $scope.loadPage();
                $scope.clearAll();
            } else {
                UIService.handleException(billingAccountMsgTitle, data, status);
            }

            UIService.unblock();
            UIService.openDialogBox(title, message, true, false);

        });
    }

    $scope.updateBillingAccount = function () {

        var title = 'Update billing Details';

        if (_.isUndefined($scope.legalEntityFormData.leCompanyName) || _.isNull($scope.legalEntityFormData.leCompanyName)) {
            var message = "Please Map Legal Entity. Legal Entity is Mandatory to Create Billing Account.";
            UIService.openDialogBox(title, message, true, true);
            return;
        }

        $scope.executeBillingAccountUpdate();
    }

    $scope.executeBillingAccountUpdate = function () {
        $scope.billingAccountFormData.leCompanyName = $scope.legalEntityFormData.leCompanyName;
        //$scope.billingAccountFormData.registeredCountry =$scope.legalEntityFormData.registeredCountry;
        //Need to modified
        $scope.billingAccountFormData.originatorGfrCode = $scope.legalEntityFormData.billingCountry;
        $scope.billingAccountFormData.receiverGrfCode = $scope.legalEntityFormData.receivergfr;
        $scope.billingAccountFormData.originatorOuc = $scope.legalEntityFormData.originatorouc;
        $scope.billingAccountFormData.receiverOuc = $scope.legalEntityFormData.receiverouc;
        $scope.billingAccountFormData.transfrChgAgrmntCode = $scope.legalEntityFormData.tcaCode;
        var leAccountClassification = $scope.legalEntityFormData.leAccountClassification;
        $scope.billingAccountFormData.accType = $scope.legalEntityFormData.leAccountClassification;

        $scope.billingAccountFormData.billingMode = $scope.legalEntityFormData.billingMode;
        $scope.billingAccountFormData.clarityProjectCode = $scope.selectedClarityProjectCode;

        var salesChannel = $scope.selectedSalesChannel;
        var cusName = $scope.customer.cusName;
        var cusId = $scope.customer.cusId;
        if (cusId == null || cusId == undefined || cusId == "") {
            cusId = " ";
        }
        var userId = $scope.salesUser.ein;
        UIService.block();
        if ($scope.mappedLeId != undefined && $scope.mappedLeId != "")
            $scope.billingAccountFormData.leID = $scope.mappedLeId;

        billingAccountService.updateBillingAccount(userId, cusId, $scope.billingAccountFormData, function (data, status) {
            var message = "";

            if (status == '200') {
                message = "Successfully Updated Billing Account for " + cusName + ".";

                var associateLeBillingAccountName = '';
                if (!(_.isUndefined($scope.mapLegalEntityData.accountClassification) || _.isNull($scope.mapLegalEntityData.accountClassification))) {
                    associateLeBillingAccountName = $scope.mapLegalEntityData.accountClassification.name;
                }

                billingAccountService.associateLeBillingAccount(cusId, $scope.billingAccountFormData.leID, $scope.oldLeId, leAccountClassification, $scope.billingAccountFormData, function (data, status) {

                });
                $scope.loadPage();
                $scope.clearAll();

            } else {
                UIService.handleException(billingAccountMsgTitle, data, status);
                return;
            }

            UIService.unblock();
            UIService.openDialogBox(billingAccountMsgTitle, message, true, false);
        });

    }


    $scope.executeBillingAccountUpdateForAdvanceBilling = function () {
        UIService.block();
        $scope.billingAccountFormData.leCompanyName = $scope.legalEntityFormData.leCompanyName;
        //$scope.billingAccountFormData.registeredCountry =$scope.legalEntityFormData.registeredCountry;
        //Need to modified
        $scope.billingAccountFormData.originatorGfrCode = $scope.legalEntityFormData.billingCountry;
        $scope.billingAccountFormData.receiverGrfCode = $scope.legalEntityFormData.receivergfr;
        $scope.billingAccountFormData.originatorOuc = $scope.legalEntityFormData.originatorouc;
        $scope.billingAccountFormData.receiverOuc = $scope.legalEntityFormData.receiverouc;
        $scope.billingAccountFormData.transfrChgAgrmntCode = $scope.legalEntityFormData.tcaCode;
        var leAccountClassification = $scope.legalEntityFormData.leAccountClassification;
        $scope.billingAccountFormData.accType = $scope.legalEntityFormData.leAccountClassification;

        $scope.billingAccountFormData.billingMode = $scope.legalEntityFormData.billingMode;

        var salesChannel = $scope.selectedSalesChannel;
        var cusName = $scope.customer.cusName;
        var cusId = $scope.customer.cusId;
        if (cusId == null || cusId == undefined || cusId == "") {
            cusId = " ";
        }
        var userId = $scope.salesUser.ein;

        if ($scope.mappedLeId != undefined && $scope.mappedLeId != "")
            $scope.billingAccountFormData.leID = $scope.mappedLeId;

        billingAccountService.updateBillingAccount(userId, cusId, $scope.billingAccountFormData, function (data, status) {
            var message = "";

            if (status == '200') {
                message = "Successfully Updated Billing Account for " + cusName + ".";

                var associateLeBillingAccountName = '';
                if (!(_.isUndefined($scope.mapLegalEntityData.accountClassification) || _.isNull($scope.mapLegalEntityData.accountClassification))) {
                    associateLeBillingAccountName = $scope.mapLegalEntityData.accountClassification.name;
                }

                billingAccountService.associateLeBillingAccount(cusId, $scope.billingAccountFormData.leID, $scope.oldLeId, leAccountClassification, $scope.billingAccountFormData, function (data, status) {


                    if (status == '200') {
                        message = "Successfully Updated Legal Entity Details for Customer  " + cusName + ".";
                    }


                    UIService.openDialogBox(billingAccountMsgTitle, message, true, false);
                    $scope.loadPageForAdvancedBilling();
                    UIService.unblock();
                });


            } else {
                UIService.handleException(billingAccountMsgTitle, data, status);
                UIService.unblock();
                return;
            }

            // UIService.unblock();

        });

    }

    $scope.viewMapLE = function () {
        $scope.showMapLe = true;
        $scope.loadLegalEntity();

    };
    $scope.mapLeIdBillingAccount = function () {
        //  UIService.block();
        $scope.legalEntityFormData.leCompanyName = $scope.mapLegalEntityData.leName.leName;
        $scope.legalEntityFormData.registeredCountry = $scope.mapLegalEntityData.leName.address.countryName;
        $scope.legalEntityFormData.billingCountry = $scope.mapLegalEntityData.billingCountry;
        $scope.legalEntityFormData.leAccountClassification = $scope.mapLegalEntityData.accountClassification;
        if ($scope.mapLegalEntityData.accountClassification == "Internal") {
            $scope.showInternalFieldsReadOnly = true;
            $scope.legalEntityFormData.receivergfr = $scope.mapLegalEntityData.receivinggfr;
            $scope.legalEntityFormData.originatorouc = $scope.mapLegalEntityData.originatingouc;
            $scope.legalEntityFormData.receiverouc = $scope.mapLegalEntityData.receivingouc;
            $scope.legalEntityFormData.tcaCode = $scope.mapLegalEntityData.tcaNumber;
        }
        else {
            $scope.showInternalFieldsReadOnly = false;
            $scope.legalEntityFormData.receivergfr = "";
            $scope.legalEntityFormData.originatorouc = "";
            $scope.legalEntityFormData.receiverouc = "";
            $scope.legalEntityFormData.tcaCode = "";
        }
        if ($scope.mapLegalEntityData.billingMode == "Contract") {
            $scope.legalEntityFormData.billingMode = "MANUAL";
        }
        else {   //Product
            $scope.legalEntityFormData.billingMode = "AUTOMATED";
        }
        $scope.executeBillingAccountUpdateForAdvanceBilling();
        //   UIService.unblock();
    }
    $scope.updateLeId = function () {
        $scope.mappedLeId = $scope.mapLegalEntityData.leName.leId;

    };
    $scope.showInternalFieldsFunction = function () {
        if ($scope.mapLegalEntityData.accountClassification == "Internal")
            $scope.showInternalFields = true;
        else {
            $scope.showInternalFields = false;
            $scope.mapLegalEntityData.receivinggfr = "";
            $scope.mapLegalEntityData.originatingouc = "";
            $scope.mapLegalEntityData.receivingouc = "";
            $scope.mapLegalEntityData.tcaNumber = "";
        }
    };

    $scope.changeBillingMode = function () {
        if ($scope.mapLegalEntityData.billingMode == "Contract")
            $scope.legalEntityFormData.billingMode = "MANUAL";
        else
            $scope.legalEntityFormData.billingMode = "AUTOMATED";
    };

    $scope.loadLegalEntity = function () {
        console.log('Inside loadLegalEntity');
        $scope.showLegalEntityEdit = true;
        if ($scope.selectedSalesChannel != undefined && $scope.salesUser != undefined && $scope.customer != undefined) {
            $scope.getLegalEntity();
        }

    };


    $scope.getLegalEntity = function () {
        UIService.block();
        $scope.legalEntityList = [];
        legalEntityService.getLegalEntity($scope.salesUser.ein, $scope.selectedSalesChannel.name, $scope.customer.cusId, function (data, status) {
            if (status == '200') {
                $scope.legalEntity = data;
                var legalEntityListTemp = [];
                if (data.length == undefined) {
                    legalEntityListTemp = [data];
                    $scope.numOfMatchingLegalEntities = 1;
                } else {
                    legalEntityListTemp = data;
                    $scope.numOfMatchingLegalEntities = legalEntityListTemp.length;
                }

                var legalEntityAddress = data;
                $scope.legalEntityList = [];
                for (var i = 0; i < $scope.numOfMatchingLegalEntities; i++) {
                    $scope.legalEntityList[i] = legalEntityListTemp[i].legalEntity;
                }
                if ($scope.numOfMatchingLegalEntities != 0) {
                    $scope.mappedLeId = data[0].legalEntity.leId;
                    $scope.legalEntityFormData.leCompanyName = data[0].legalEntity.leName;
                    $scope.legalEntityFormData.registeredCountry = data[0].legalEntity.address.countryName;
                    $scope.legalEntityFormData.billingCountry = $scope.selectedSalesChannel.name
                    $scope.legalEntityFormData.leAccountClassification = $scope.accountClassificationList[0];
                    if (PageContext.getIsMNC() || $scope.selectedSalesChannel.name == 'MANAGED SERVICES FROM BT') {
                        $scope.legalEntityFormData.billingMode = "Contract";
                    }
                    else {
                        $scope.legalEntityFormData.billingMode = "Product";
                    }


                }
                UIService.unblock();
            } else if (status = '404') {
                console.log('No LE found for Cus ID:' + $scope.customer.cusId + ", Sales channel :" + $scope.selectedSalesChannel.name);
                $scope.numOfMatchingLegalEntities = 0;
                UIService.unblock();
            } else {
                UIService.handleException(manageLEMsgTitle, data, status);
            }
            UIService.unblock();
        });
    }

    $scope.searchProjectByCode = function () {
        var title = 'Link Project Code.';
        if (_.isNull($scope.clarityProjectAssociationForm.projectCode) || _.isEmpty($scope.clarityProjectAssociationForm.projectCode)) {
            UIService.openDialogBox(title, 'Please enter a Project Code to search with.', true, false);
        } else {

            if ($scope.projectCodeFormatValidation($scope.clarityProjectAssociationForm.projectCode) == false) {
                UIService.openDialogBox(title, 'The format of the Project Code entered is invalid.\n' +
                                               'Please enter a valid Project Code to be searched. E.g. "JA123456" ', true, false);
                return;
            } else {
                UIService.block();
                billingAccountService.getClarityProjectCodeDetails($scope.clarityProjectAssociationForm.projectCode, null, null, function (responsedata, status) {
                    if (status == 200) {
                        $scope.clarityProjectDetailsData = responsedata;
                    } else {
                        console.log("Failed to get clarity project details.");
                    }
                    UIService.unblock();
                })
            }
        }
    }

    $scope.advanceProjectSearch = function () {
        var title = 'Link Project Code.';

        if ((_.isNull($scope.clarityProjectAssociationForm.projectName) || _.isEmpty($scope.clarityProjectAssociationForm.projectName)) &&
            ( _.isNull($scope.clarityProjectAssociationForm.sacId) || _.isEmpty($scope.clarityProjectAssociationForm.sacId))
                && (_.isNull($scope.clarityProjectAssociationForm.projectCode) || _.isEmpty($scope.clarityProjectAssociationForm.projectCode))) {
            UIService.openDialogBox(title, 'Please enter at least one criterion to search with - Project Code/Project Name/SAC Code.', true, false);
        } else {
            if (!_.isNull($scope.clarityProjectAssociationForm.projectCode)&& !_.isUndefined($scope.clarityProjectAssociationForm.projectCode) && $scope.projectCodeFormatValidation($scope.clarityProjectAssociationForm.projectCode) == false) {
                UIService.openDialogBox(title, 'The format of the Project Code entered is invalid.\n' +
                                               'Please enter a valid Project Code to be searched. E.g. "JA123456" ', true, false);
                return;
            } else{
            UIService.block();
            billingAccountService.getClarityProjectCodeDetails($scope.clarityProjectAssociationForm.projectCode, $scope.clarityProjectAssociationForm.projectName,
                                                               $scope.clarityProjectAssociationForm.sacId, function (responsedata, status) {
                        if (status == 200) {
                            $scope.clarityProjectDetailsData = responsedata;
                        } else {
                            console.log("Failed to get clarity project details.");
                        }

                        UIService.unblock();
                    })
            }
        }
    }

    $scope.projectCodeFormatValidation = function (clarityProjectCode) {

        var part1 = clarityProjectCode.substr(0, 2);
        var part2 = clarityProjectCode.substr(2, 6);
        if (part1 == 'JA') {
            if (part2.length == 6) {
                if (parseInt(part2, 10)>=0)
                    return true;
                else
                    return false;
            } else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    $scope.clearAll = function () {
        $scope.billingAccountFormData = {};
        $scope.disableUpdateBtnUI = true;
        $scope.getPaymentDays();
        $scope.getPeriodList();
        $scope.clarityProjectAssociationForm = {};
        /*$scope.clarityProjectAssociationForm.sacId = null;
         $scope.clarityProjectAssociationForm.sacId = null;*/
        $scope.selectedClarityProjectCode = null;
        $scope.clarityProjectDetailsData = [];

    }


    $scope.collapseAccountGrid = function () {
        $scope.displayAccountGrid = !$scope.displayAccountGrid;
    }

    $scope.collapseAssLEPanel = function () {
        $scope.displayAssLePanel = !$scope.displayAssLePanel;
    }

    $scope.collapseAccountDetPanel = function () {
        $scope.displayAccountDetPanel = !$scope.displayAccountDetPanel;
    }

    $scope.associateBillingAccount = function () {
        $scope.customerAddDetDto.defaultCacId = $scope.billingAccountFormData.billingAccountId
        UIService.block();
        customerService.updateCustomerAdditionalDetail($scope.customerAddDetDto, function (data, status) {
            if ('200' == status) {
                UIService.openDialogBox('Billing Association', 'Account successfully associated to customer.', true, false);
            } else {
                UIService.handleException('Billing Association', data, status);
            }
            UIService.unblock();
        })
    }

    $scope.getCustomerAdditionalDetail = function () {
        $scope.enableAssociateBillButton = false;
        if (!(_.isUndefined($scope.billingAssociateModel) || _.isUndefined($scope.billingAssociateModel.assocCust))) {
            customerService.getCustomerAdditionalDetail($scope.billingAssociateModel.assocCust.cusId, function (data, status) {

                if ('200' == status) {
                    $scope.customerAddDetDto = data;
                    $scope.enableAssociateBillButton = true;
                } else {
                    $scope.customerAddDetDto = undefined;
                    $scope.enableAssociateBillButton = false;
                }
            })
        }
    }
    /////////////////////////    Google Map /////////////////////////////////////
    $scope.clickShowMap = function () {
        $scope.showMap = !$scope.showMap;
        $scope.isAdrSearchSuccess = false;
        $scope.displayGrid = !$scope.showMap;
        //$scope.flipFlopMap();
        if ($scope.showMap) {
            var latLng;
            var geocoder;
            geocoder = new google.maps.Geocoder();
            var infowindow = new google.maps.InfoWindow();
            var london = new google.maps.LatLng(51.555100, -0.264030);
            if (!(_.isUndefined($scope.billingAccountFormData) || _.isUndefined($scope.billingAccountFormData.latitude) || ((_.isNumber($scope.billingAccountFormData.latitude) && _.isNull($scope.billingAccountFormData.latitude)) || (_.isString($scope.billingAccountFormData.latitude) && _.isEmpty($scope.billingAccountFormData.latitude))))) {

                latLng = new google.maps.LatLng($scope.billingAccountFormData.latitude, $scope.billingAccountFormData.longitude);
            }

            if (_.isUndefined(latLng)) {
                if (!(_.isUndefined($scope.billingAccountFormData))) {
                    var address = $scope.billingAccountFormData.city + "," + $scope.billingAccountFormData.country.name + "," + $scope.billingAccountFormData.street + "," + $scope.billingAccountFormData.locality;
                    geocoder.geocode({ 'address':address}, function (results, status) {
                        if (status == google.maps.GeocoderStatus.OK) {
                            latLng = new google.maps.LatLng(results[0].geometry.location.lat(), results[0].geometry.location.lng());
                            $scope.mapOptions = {
                                zoom:16,
                                center:latLng,
                                navigationControl:true,
                                keyboardShortcuts:true,
                                disableDoubleClickZoom:false,
                                draggable:true,
                                //mapTypeId:google.maps.MapTypeId.ROADMAP,
                                mapTypeControlOptions:{
                                    style:google.maps.MapTypeControlStyle.DEFAULT,
                                    position:google.maps.ControlPosition.BOTTOM_LEFT
                                },
                                zoomControl:true,
                                zoomControlOptions:{
                                    style:google.maps.ZoomControlStyle.SMALL,
                                    position:google.maps.ControlPosition.RIGHT_BOTTOM
                                }
                            };

                            $scope.map = new google.maps.Map(document.getElementById('map-canvas'), $scope.mapOptions);
                            var closeAnchor = document.createElement('a');

                            var idAttr = document.createAttribute('id');
                            idAttr.value = 'close-icon';

                            var onclickAttr = document.createAttribute('onclick');
                            onclickAttr.value = 'clickShowMap';

                            var innerText = document.createTextNode('[X] Close');

                            closeAnchor.appendChild(innerText);
                            closeAnchor.setAttributeNode(idAttr);
                            closeAnchor.setAttributeNode(onclickAttr);

                            if (!_.isNull(closeAnchor)) {
                                $scope.map.controls[google.maps.ControlPosition.TOP_RIGHT].push(closeAnchor);
                            }

                            google.maps.event.addDomListener(closeAnchor, 'click', function () {
                                $scope.clickShowMap();
                            });


                            var marker = new google.maps.Marker({ map:$scope.map,
                                                                    animation:google.maps.Animation.DROP,
                                                                    position:latLng,
                                                                    title:'Click to Select and Drag',
                                                                    draggable:true
                                                                });

                            google.maps.event.addListener(marker, 'click', function () {
                                $scope.map.setZoom(16);
                                $scope.map.setCenter(marker.getPosition());
                            });
                            google.maps.event.addListener(marker, 'dragend', function (event) {
                                var lat = this.getPosition().lat();
                                var lng = this.getPosition().lng();
                                //  $scope.map.setCenter(marker.getPosition());
                                $scope.billingAccountFormData.latitude = lat;
                                $scope.billingAccountFormData.longitude = lng;

                                marker.setMap($scope.map);
                                $scope.nullifyAddress();
                                geocoder.geocode({'latLng':this.getPosition()}, function (results, status) {
                                    if (status == google.maps.GeocoderStatus.OK) {
                                        if (results[0]) {


                                            for (i = 0; i < results[0].address_components.length; i++) {
                                                for (j = 0; j < results[0].address_components[i].types.length; j++) {
                                                    var address_component = results[0].address_components[i];
                                                    var address_attib_name = address_component.types[j];
                                                    var address_component_val = address_component.long_name;

                                                    if ('country' == address_attib_name) {
                                                        countryService.broadcastSelectedCountryChangedEvent(address_component_val);
                                                    } else if ('street_number' == address_attib_name) {
                                                        $scope.billingAccountFormData.buildingNumber = address_component_val;
                                                    } else if ('route' == address_attib_name) {
                                                        $scope.billingAccountFormData.street = address_component_val;
                                                    } else if ('locality' == address_attib_name) {
                                                        $scope.billingAccountFormData.locality = address_component_val;
                                                    } else if ('sublocality' == address_attib_name) {
                                                        $scope.billingAccountFormData.subLocality = address_component_val;
                                                    } else if ('administrative_area_level_1' == address_attib_name) {
                                                        $scope.billingAccountFormData.state = address_component_val;
                                                    } else if ('administrative_area_level_2' == address_attib_name) {
                                                        $scope.billingAccountFormData.city = address_component_val;
                                                    } else if ('postal_code' == address_attib_name) {
                                                        $scope.billingAccountFormData.postCode = address_component_val;
                                                    } else if ('postal_town' == address_attib_name) {
                                                        $scope.billingAccountFormData.state = address_component_val;
                                                    }


                                                }
                                            }
                                            $scope.$apply();
                                            infowindow.setContent(results[1].formatted_address);
                                            infowindow.open($scope.map, marker);
                                        }
                                    } else if (status == google.maps.GeocoderStatus.ZERO_RESULTS) {
                                        console.log('Site located in sea ..');
                                    } else {
                                        console.log('Google geo code fetch failed for unknown reasons .. STATUS -' + status);
                                    }
                                });


                                $scope.$apply();
                                console.log('Dragend')
                                //infoWindow.close();
                            });
                        }
                        var listener = google.maps.event.addListener($scope.map, 'idle',
                                                                     function () {
                                                                         //marker.setPosition(latLng);
                                                                         $scope.map.setCenter(marker.getPosition());
                                                                         google.maps.event.removeListener(listener);
                                                                     }
                        );
                        $scope.map.setCenter(latLng);
                    });
                }
            }
            else {
                $scope.mapOptions = {
                    zoom:16,
                    center:latLng,
                    navigationControl:true,
                    keyboardShortcuts:true,
                    disableDoubleClickZoom:false,
                    draggable:true,
                    //mapTypeId:google.maps.MapTypeId.ROADMAP,
                    mapTypeControlOptions:{
                        style:google.maps.MapTypeControlStyle.DEFAULT,
                        position:google.maps.ControlPosition.BOTTOM_LEFT
                    },
                    zoomControl:true,
                    zoomControlOptions:{
                        style:google.maps.ZoomControlStyle.SMALL,
                        position:google.maps.ControlPosition.RIGHT_BOTTOM
                    }
                };

                $scope.map = new google.maps.Map(document.getElementById('map-canvas'), $scope.mapOptions);

                var closeAnchor = document.createElement('a');

                var idAttr = document.createAttribute('id');
                idAttr.value = 'close-icon';

                var onclickAttr = document.createAttribute('onclick');
                onclickAttr.value = 'clickShowMap';

                var innerText = document.createTextNode('[X] Close');

                closeAnchor.appendChild(innerText);
                closeAnchor.setAttributeNode(idAttr);
                closeAnchor.setAttributeNode(onclickAttr);

                if (!_.isNull(closeAnchor)) {
                    $scope.map.controls[google.maps.ControlPosition.TOP_RIGHT].push(closeAnchor);
                }

                google.maps.event.addDomListener(closeAnchor, 'click', function () {
                    $scope.clickShowMap();
                });


                var marker = new google.maps.Marker({ map:$scope.map,
                                                        animation:google.maps.Animation.DROP,
                                                        position:latLng,
                                                        title:'Click to Select and Drag',
                                                        draggable:true
                                                    });

                google.maps.event.addListener(marker, 'click', function () {
                    $scope.map.setZoom(16);
                    $scope.map.setCenter(marker.getPosition());
                });

                google.maps.event.addListener(marker, 'dragend', function (event) {
                    var lat = this.getPosition().lat();
                    var lng = this.getPosition().lng();
                    //  $scope.map.setCenter(marker.getPosition());
                    $scope.billingAccountFormData.latitude = lat;
                    $scope.billingAccountFormData.longitude = lng;

                    marker.setMap($scope.map);
                    $scope.nullifyAddress();
                    geocoder.geocode({'latLng':this.getPosition()}, function (results, status) {
                        if (status == google.maps.GeocoderStatus.OK) {
                            if (results[0]) {


                                for (i = 0; i < results[0].address_components.length; i++) {
                                    for (j = 0; j < results[0].address_components[i].types.length; j++) {
                                        var address_component = results[0].address_components[i];
                                        var address_attib_name = address_component.types[j];
                                        var address_component_val = address_component.long_name;

                                        if ('country' == address_attib_name) {
                                            countryService.broadcastSelectedCountryChangedEvent(address_component_val);
                                        } else if ('street_number' == address_attib_name) {
                                            $scope.billingAccountFormData.buildingNumber = address_component_val;
                                        } else if ('route' == address_attib_name) {
                                            $scope.billingAccountFormData.street = address_component_val;
                                        } else if ('locality' == address_attib_name) {
                                            $scope.billingAccountFormData.locality = address_component_val;
                                        } else if ('sublocality' == address_attib_name) {
                                            $scope.billingAccountFormData.subLocality = address_component_val;
                                        } else if ('administrative_area_level_1' == address_attib_name) {
                                            $scope.billingAccountFormData.state = address_component_val;
                                        } else if ('administrative_area_level_2' == address_attib_name) {
                                            $scope.billingAccountFormData.city = address_component_val;
                                        } else if ('postal_code' == address_attib_name) {
                                            $scope.billingAccountFormData.postCode = address_component_val;
                                        } else if ('postal_town' == address_attib_name) {
                                            $scope.billingAccountFormData.state = address_component_val;
                                        }


                                    }
                                }
                                $scope.$apply();
                                infowindow.setContent(results[1].formatted_address);
                                infowindow.open($scope.map, marker);
                            }
                        } else if (status == google.maps.GeocoderStatus.ZERO_RESULTS) {
                            console.log('Site located in sea ..');
                        } else {
                            console.log('Google geo code fetch failed for unknown reasons .. STATUS -' + status);
                        }
                    });


                    $scope.$apply();
                    console.log('Dragend')
                    //infoWindow.close();
                });

                var listener = google.maps.event.addListener($scope.map, 'idle',
                                                             function () {
                                                                 //marker.setPosition(latLng);
                                                                 $scope.map.setCenter(marker.getPosition());
                                                                 google.maps.event.removeListener(listener);
                                                             }
                );
                $scope.map.setCenter(latLng);
            }
        } else {
            $scope.$apply();
        }
        $(window).resize(function () {
            // (the 'map' here is the result of the created 'var map = ...' above)
            google.maps.event.trigger($scope.map, "resize");
        });

        window.setTimeout(function () {
            if (!_.isUndefined($scope.map)) {
                google.maps.event.trigger($scope.map, 'resize');
            }
        }, 10);

    }

    $scope.collapseGrid = function () {
        $scope.displayGrid = !$scope.displayGrid;
    }

    /////////////////////////    Google Map /////////////////////////////////////
    $scope.nullifyAddress = function () {

        $scope.billingAccountFormData.buildingNumber = '';
        $scope.billingAccountFormData.buildingName = '';
        $scope.billingAccountFormData.subBuilding = '';
        $scope.billingAccountFormData.street = '';
        $scope.billingAccountFormData.subStreet = '';
        $scope.billingAccountFormData.locality = '';
        $scope.billingAccountFormData.subLocality = '';
        $scope.billingAccountFormData.city = '';
        $scope.billingAccountFormData.state = '';
        $scope.billingAccountFormData.subState = '';
        $scope.billingAccountFormData.postCode = '';
        $scope.billingAccountFormData.subPostCode = '';
        $scope.billingAccountFormData.POBox = '';
        countryService.broadcastSelectedCountryChangedEvent('');
    }

/////////////////////////

}
])
