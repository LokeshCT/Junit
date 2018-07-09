var module = angular.module('cqm.controllers');

// Manage LE site controller BEGIN
module.controller('LegalEntityController', ['$scope', '$routeParams', '$http', 'countryService', 'httpService', '$modal', 'addressService', 'siteService', 'UIService', 'legalEntityService', 'salesChannelService', 'PageContext', 'UserContext', 'customerService', 'WebMetrics', '$rootScope', function ($scope, $routeParams, $http, countryService, httpService, $modal, addressService, siteService, UIService, legalEntityService, salesChannelService, PageContext, UserContext, customerService, WebMetrics, $rootScope) {


    console.log('Inside Manage LE Controller');

    if (PageContext.exist()) {
        $scope.customer = PageContext.getCustomer();
        $scope.contract = PageContext.getContract();
        $scope.selectedSalesChannel = PageContext.getSalesChannel();
    }

    if (UserContext.exist()) {
        $scope.salesUser = UserContext.getUser();
    }


    $scope.numOfMatchingAddresses = 0;

    $scope.numOfMatchingLegalEntities = 0;

    $scope.legalEntityUI = {'enableCreateButton':false, "enableUpdateButton":false, 'showSearchResultsDiv':false};

    $scope.showSearchResults = false;

    $scope.selectedNADAddress = [];

    $scope.disableCreate = false;

    $scope.isEditable = false;

    $scope.select2Options = {
        allowClear:true
    };

    $scope.emptyMandatoryFieldsArr = [];
    $scope.legalEntityFormData = {};

    $scope.provRfo = '';
    $scope.zipRfo = '';
    $scope.zipRfoReq = false;
    $scope.provRfoReq = false;
    $scope.vatPrefixOfSelectedCountry = null;
    $scope.showVatErrorMessage=false;

    $scope.reset = function () {
        $scope.legalEntityFormData = {};
        $scope.showVatErrorMessage=false;
        $scope.vatPrefixOfSelectedCountry=null;

        if (!_.isUndefined($scope.legalEntityUI)) {
            $scope.legalEntityUI.disableUpdate = true;
            $scope.legalEntityUI.disableCreate = true;
        }

        if (!_.isUndefined($scope.legalEntityGrid)) {
            $scope.legalEntityGrid.selectAll(false);
        }
        countryService.broadcastSelectedCountryChangedEvent('');
    }

    $scope.isReadonly = false;
    $scope.leAvailableNames = [];

    $scope.loadLegalEntity = function () {
        console.log('Inside loadLegalEntity');
        $scope.showLegalEntityEdit = true;
        countryService.broadcastSelectedCountryChangedEvent('');
        if ($scope.selectedSalesChannel != undefined && $scope.salesUser != undefined && $scope.customer != undefined) {
            $scope.getLegalEntity($scope.selectedSalesChannel, $scope.salesUser, $scope.customer);
        }
    }

    $scope.getLegalEntity = function (salesChannel, salesUser, customer) {
        UIService.block();
        var startTime = new Date().getTime();
        console.log('inside the controller. received the change message');
        $scope.legalEntityUI.showSearchResultsDiv = false;
        $scope.addressSearchResultData = {};
        $scope.legalEntityUI.enableCreateButton = false;
        $scope.legalEntityUI.enableUpdateButton = false;

        $scope.legalEntityList = [];
        legalEntityService.getLegalEntity($scope.salesUser.ein, $scope.selectedSalesChannel.name, $scope.customer.cusId, function (data, status) {
            if (status == '200') {
                $scope.legalEntity = data;
                var legalEntityListTemp = [];

                if (!_.isUndefined(data.length)) {
                    legalEntityListTemp = data;
                    $scope.numOfMatchingLegalEntities = legalEntityListTemp.length;
                    WebMetrics.captureWebMetrics('CQM Customer Tab - Load Legal Entities', startTime);
                }

                var legalEntityAddress = data;
                $scope.legalEntityList = legalEntityListTemp;
            } else if (status = '404') {
                $scope.numOfMatchingLegalEntities = 0;
            } else {
                UIService.handleException('Legal Entity', data, status);
            }
            UIService.unblock();
        });
    }


    $scope.isExistingLegalEntity = function (legalCompanyName) {
        if (legalCompanyName != undefined) {
            return true;
        }
        return false;
    };

    $scope.$on('selectedCountryChanged', function (event, countryName) {
        console.log('inside the selectedCountryChanged listener.');
        $scope.processCountryChange(countryName);
    });

    $scope.$watch('legalEntityFormData.country', function (country) {
        $scope.processCountryChange(country.name);
    });

    $scope.processCountryChange = function (countryName) {
        if (!_.isUndefined(countryName)) {
            _.forEach($scope.allCountries, function (country) {
                if (country.name.toUpperCase() == countryName.toUpperCase()) {
                    $scope.legalEntityFormData.country = country;

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
    }

    $scope.allCountries = countryService.getAllCountries();

    $scope.updateCountry = function () {
        countryService.broadcastSelectedCountryChangedEvent($scope.legalEntityFormData.country.name);
        $scope.countryCode = $scope.legalEntityFormData.country.codeAlpha2;
    }


    $scope.searchAddress = function () {
        console.log('Search address invoked');
        UIService.block();

        var nadAddressRequestDTO = new Object();
        var queryParamObject = new Object();
        queryParamObject.city = $scope.legalEntityFormData.city;
        queryParamObject.country = $scope.legalEntityFormData.country.name;
        queryParamObject.postCode = $scope.legalEntityFormData.postCode;
        if (queryParamObject.postCode == "" || queryParamObject.postCode == undefined) {
            queryParamObject.postCode = "0";
        }


        queryParamObject.locality = $scope.legalEntityFormData.locality;
        queryParamObject.countryCode = $scope.legalEntityFormData.country.codeAlpha2;
        queryParamObject.building = $scope.legalEntityFormData.buildingName;
        queryParamObject.street = $scope.legalEntityFormData.street;
        queryParamObject.buildingNumber = $scope.legalEntityFormData.buildingNumber;
        queryParamObject.subStreet = $scope.legalEntityFormData.subStreet;
        queryParamObject.subLocality = $scope.legalEntityFormData.subLocality;
        queryParamObject.state = $scope.legalEntityFormData.state;
        queryParamObject.poBox = $scope.legalEntityFormData.POBox;
        queryParamObject.subState = $scope.legalEntityFormData.subState;
        queryParamObject.subPostCode = $scope.legalEntityFormData.subPostCode;
        queryParamObject.subBuilding = $scope.legalEntityFormData.subBuilding;
        queryParamObject.latitude = $scope.legalEntityFormData.latitude;
        queryParamObject.longitude = $scope.legalEntityFormData.longitude;
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
                $scope.operationResult = "Found: " + $scope.addressSearchResultData.length;
                $scope.legalEntityUI.showSearchResultsDiv = true;
                window.setTimeout(function () {
                    $(window).resize();
                }, 1);

            } else if ('404' == status) {
                $scope.legalEntityUI.showSearchResultsDiv = false;
                UIService.openDialogBox('Search Address', data.description, true, false);
            } else {
                $scope.legalEntityUI.showSearchResultsDiv = false;
                UIService.handleException('Search Address', data, status);
            }
            UIService.unblock();
            console.log("Fetched Addresses: ", $scope.addressSearchResultData);
        };

        addressService.searchAddress(nadAddressRequestDTO, processResponseFunc);
    };


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
            //$scope.fillLegalEntityFields(item.entity);
            $scope.updateLegalEntityForm(item.entity);
            $scope.legalEntityUI.enableCreateButton = true;
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
            {field:'poBoxNo', displayName:'PO Box', width:120},
            {field:'postalOrganisation', displayName:'Postal Organisation', width:120},
            {field:'latitude', displayName:'Latitude', width:120},
            {field:'longitude', displayName:'Longitude', width:120},
            {field:'managePlaceResult', displayName:'Manage Place Result', width:120},
            {field:'accuracyLevel', displayName:'Accuracy Level', width:240},
            {field:'failLevel', displayName:'Fail Level', width:240},
            {field:'validationLevel', displayName:'Validation Level', width:240},
            {field:'componentStatus', displayName:'Component Status', width:240},
            {field:'countryCode', displayName:'Country Code', width:120}
        ]
    };


    $scope.updateLegalEntityForm = function (selectedAddress) {
        if (!_.isUndefined(selectedAddress)) {
            var msg = 'Do you want to update current address details with the one you selected from Search Results?';
            var dialogInstance = UIService.openDialogBox('Update Address Details', msg, true, true);
            dialogInstance.result.then(function () {
                if (!_.isUndefined(selectedAddress.buildingNumber) && !_.isEmpty(selectedAddress.buildingNumber)) {
                    $scope.legalEntityFormData.buildingNumber = selectedAddress.buildingNumber;
                }

                if (!_.isUndefined(selectedAddress.buildingName) && !_.isEmpty(selectedAddress.buildingName)) {
                    $scope.legalEntityFormData.buildingName = selectedAddress.buildingName;
                }

                if (!_.isUndefined(selectedAddress.subBuilding) && !_.isEmpty(selectedAddress.subBuilding)) {
                    $scope.legalEntityFormData.subBuilding = selectedAddress.subBuilding;
                }

                if (!_.isUndefined(selectedAddress.locality) && !_.isEmpty(selectedAddress.locality)) {
                    $scope.legalEntityFormData.locality = selectedAddress.locality;
                }

                if (!_.isUndefined(selectedAddress.street) && !_.isEmpty(selectedAddress.street)) {
                    $scope.legalEntityFormData.street = selectedAddress.street;
                }

                if (!_.isUndefined(selectedAddress.subLocality) && !_.isEmpty(selectedAddress.subLocality)) {
                    $scope.legalEntityFormData.subLocality = selectedAddress.subLocality;
                }

                if (!_.isUndefined(selectedAddress.poBox) && !_.isEmpty(selectedAddress.poBox)) {
                    $scope.legalEntityFormData.POBox = selectedAddress.poBox;
                }

                if (!_.isUndefined(selectedAddress.subStreet) && !_.isEmpty(selectedAddress.subStreet)) {
                    $scope.legalEntityFormData.subStreet = selectedAddress.subStreet;
                }

                if (!_.isUndefined(selectedAddress.subZipCode) && !_.isEmpty(selectedAddress.subZipCode)) {
                    $scope.legalEntityFormData.subPostCode = selectedAddress.subZipCode;
                }

                $scope.legalEntityFormData.state = selectedAddress.state;
                $scope.legalEntityFormData.subState = selectedAddress.subState;
                $scope.legalEntityFormData.postCode = selectedAddress.zipCode;
            }, function () {})
        }

    }

    $scope.createLegalEntity = function (create) {
        UIService.block();
        var startTime = new Date().getTime();
        var salesChannel = $scope.selectedSalesChannel.name;
        var cusName = $scope.customer.cusName;
        var cusId = $scope.customer.cusId;
        if (cusId == null || cusId == undefined || cusId == "") {
            cusId = " ";
        }
        var ein = $scope.salesUser.ein;

        if(!_.isNull($scope.legalEntityFormData.vatNo) && !_.isEmpty($scope.legalEntityFormData.vatNo) && ! $scope.isValidVATNo($scope.legalEntityFormData.vatNo)) {
            $scope.showVatErrorMessage=true;
            UIService.unblock();
            return;
        }

        if (create) {
            var existingLE = _.find($scope.legalEntityList, function (legalEntity) {
                if (legalEntity.legalEntity.leName == $scope.legalEntityFormData.legalCompanyName) {
                    return legalEntity;
                }
            });

            if (!_.isUndefined(existingLE)) {
                var dialogInstance = UIService.openDialogBox('Create Legal Entity', "Provide an Unique Legal Company Name !!", true, false);
                dialogInstance.result.then(function () {
                }, function () {
                });
                UIService.unblock();
                return;
            }

            if ($scope.legalEntityFormData.taxRef != undefined && $scope.legalEntityFormData.taxRef != null && $scope.legalEntityFormData.taxRef.trim() != "") {
                UIService.unblock();
                var title = "Create Legal Entity";
                var msg = "You are specifying a tax exemption code and hence this customer will be treated as tax exempt. Do you wish to continue?";
                var dialogInstance = UIService.openDialogBox(title, msg, true, true);
                dialogInstance.result.then(function () {
                    UIService.block();
                    legalEntityService.createLegalEntity(ein, salesChannel, cusId, $scope.legalEntityFormData, function (data, status) {
                        var title = 'Customer Manage LE';
                        var message = "";
                        var btns = [
                            {result:'OK', label:'OK'}
                        ];
                        if (status == '200') {
                            title = 'Create Legal Entity';
                            message = "Successfully Created Legal Entity for " + cusName + ".";
                            $scope.getLegalEntity($scope.selectedSalesChannel, $scope.salesUser, $scope.customer);
                            $scope.legalEntityFormData = {};
                            $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.VALID);
                            WebMetrics.captureWebMetrics('CQM Customer Tab - Create Legal Entity', startTime);
                            $scope.reset();
                        }
                        else {
                            message = "Request failed for customer " + cusName + "." + "\n" + data;
                        }

                        UIService.unblock();

                        var dialogInstance = UIService.openDialogBox(title, message, true, false);
                        dialogInstance.result.then(function () {
                        }, function () {
                        });
                    });
                }, function () {
                    console.log('Cancel the LE creations.....');
                    UIService.unblock();
                    return;
                });
            }
            else {
                legalEntityService.createLegalEntity(ein, salesChannel, cusId, $scope.legalEntityFormData, function (data, status) {
                    var title = 'Customer Manage LE';
                    var message = "";
                    var btns = [
                        {result:'OK', label:'OK'}
                    ];
                    if (status == '200') {
                        title = 'Create Legal Entity';
                        message = "Successfully Created Legal Entity for " + cusName + ".";
                        $scope.getLegalEntity($scope.selectedSalesChannel, $scope.salesUser, $scope.customer);
                        $scope.legalEntityFormData = {};
                        WebMetrics.captureWebMetrics('CQM Customer Tab - Create Legal Entity', startTime);
                        $scope.reset();
                    }
                    else {
                        message = "Request failed for customer " + cusName + "." + "\n" + data;
                    }

                    UIService.unblock();

                    var dialogInstance = UIService.openDialogBox(title, message, true, false);
                    dialogInstance.result.then(function () {
                    }, function () {
                    });
                });
            }


        }
        else {

            var existingLE = _.find($scope.legalEntityList, function (legalEntity) {
                if (legalEntity.legalEntity.leName == $scope.legalEntityFormData.legalCompanyName && legalEntity.legalEntity.leId != $scope.legalEntityFormData.leId) {
                    return legalEntity;
                }
            });
            if (!_.isUndefined(existingLE)) {
                var dialogInstance = UIService.openDialogBox('Update Legal Entity', "Provide an Unique Legal Company Name !!", true, false);
                dialogInstance.result.then(function () {
                }, function () {
                });
                UIService.unblock();
                return;
            }
            if ($scope.legalEntityFormData.taxRef != undefined && $scope.legalEntityFormData.taxRef != null && $scope.legalEntityFormData.taxRef.trim() != "") {
                UIService.unblock();
                var title = "Update Legal Entity.";
                var msg = "You are specifying a tax exemption code and hence this customer will be treated as tax exempt. Do you wish to continue?";

                var dialogInstance = UIService.openDialogBox(title, msg, true, true);
                dialogInstance.result.then(function () {
                    UIService.block();
                    legalEntityService.updateLegalEntity(ein, salesChannel, cusId, $scope.legalEntityFormData, function (data, status) {
                        var title = 'Customer Manage LE';
                        var message = "";
                        var btns = [
                            {result:'OK', label:'OK'}
                        ];

                        if (status == '200') {
                            title = 'Update Legal Entity';
                            message = "Successfully Updated Legal Entity for " + cusName + ".";
                            $scope.getLegalEntity($scope.selectedSalesChannel, $scope.salesUser, $scope.customer);
                            $scope.legalEntityUI.enableCreateButton = false;
                            $scope.legalEntityUI.enableUpdateButton = false;
                            WebMetrics.captureWebMetrics('CQM Customer Tab - Update Legal Entity', startTime);
                            $scope.reset();
                        }
                        else {
                            message = "Request failed for customer " + cusName + "." + "\n" + data;
                        }

                        UIService.unblock();

                        var dialogInstance = UIService.openDialogBox(title, message, true, false);
                        dialogInstance.result.then(function () {
                        }, function () {
                        });
                    });
                }, function () {
                    console.log('Cancel the LE Updatation.....');
                    UIService.unblock();
                    return;
                });
            }
            else {
                legalEntityService.updateLegalEntity(ein, salesChannel, cusId, $scope.legalEntityFormData, function (data, status) {
                    var title = 'Customer Manage LE';
                    var message = "";
                    var btns = [
                        {result:'OK', label:'OK'}
                    ];

                    if (status == '200') {
                        title = 'Update Legal Entity';
                        message = "Successfully Updated Legal Entity for " + cusName + ".";
                        $scope.getLegalEntity($scope.selectedSalesChannel, $scope.salesUser, $scope.customer);
                        $scope.legalEntityUI.enableCreateButton = false;
                        $scope.legalEntityUI.enableUpdateButton = false;
                        WebMetrics.captureWebMetrics('CQM Customer Tab - Update Legal Entity', startTime);
                        $scope.reset();
                    }
                    else {
                        message = "Request failed for customer " + cusName + "." + "\n" + data;
                    }

                    UIService.unblock();

                    var dialogInstance = UIService.openDialogBox(title, message, true, false);
                    dialogInstance.result.then(function () {
                    }, function () {
                    });
                });
            }
        }
    }

    $scope.legalEntityGrid = { data:'legalEntityList', multiSelect:false, enableColumnResize:true,
        showGroupPanel:true,
        showColumnMenu:true,
        showFilter:true,

        afterSelectionChange:function (item, event) {
            if (item.selected == false) {
                console.log('got de-select event');
                $scope.legalEntityFormData.leId = undefined;
                $scope.legalEntityUI.enableCreateButton = false;
                return;
            }
            $scope.showLegalEntityEdit = false;
            $scope.legalEntityUI.showSearchResultsDiv = false;
            $scope.fillLegalEntityForm(item.entity);

        },
        columnDefs:[
            {field:'legalEntity.leId', displayName:'Legal Company Id', width:"*", visible:false},
            {field:'legalEntity.leName', displayName:'Legal Company Name', width:"*" },
            {field:'legalEntity.address.country.name', displayName:'Country', width:"*"},
            {field:'legalEntity.address.county', displayName:'State', width:"*", visible:true},
            {field:'legalEntity.address.subCountyStateProvince', displayName:'Sub State', width:"*", visible:false},
            {field:'legalEntity.address.locality', displayName:'Locality', width:"*", visible:false},
            {field:'legalEntity.address.subLocality', displayName:'Sub Locality', width:"*", visible:false},
            {field:'legalEntity.address.postZipCode', displayName:'Zip/Post Code', width:"*", visible:true},
            {field:'legalEntity.address.streetNo', displayName:'Building Number', width:"*", visible:false},
            {field:'legalEntity.address.town', displayName:'City', width:"*"},
            {field:'legalEntity.address.subZipCode', displayName:'Sub Post Code', width:"*", visible:false},
            {field:'legalEntity.address.poBoxNo', displayName:'PO Box', width:"*", visible:false},
            {field:'legalEntity.address.sitePremise', displayName:'Building Name', width:"*"},
            {field:'legalEntity.address.streetName', displayName:'Street', width:"*", visible:false},
            {field:'legalEntity.address.subStreet', displayName:'Sub Street', width:"*", visible:false},
            {field:'legalEntity.address.subBuilding', displayName:'Sub Building', width:"*", visible:false},
            {field:'legalEntity.leTaxExemptRef', displayName:'Tax Exemption', width:"*", visible:true},
            {field:'legalEntity.leVatNo', displayName:'VAT Number', width:"*", visible:true},
            {field:'legalEntity.compRegNo', displayName:'Company Registration Number', width:"*", visible:false}
        ]

    };

    $scope.fillLegalEntityForm = function (selectedLE) {
        if (!_.isUndefined(selectedLE)) {
            $scope.disableCreate = true;

            $scope.legalEntityFormData.leId = selectedLE.legalEntity.leId;
            $scope.legalEntityFormData.legalCompanyName = selectedLE.legalEntity.leName;

            if (!_.isUndefined(selectedLE.legalEntity.address)) {
                $scope.legalEntityFormData.buildingName = selectedLE.legalEntity.address.sitePremise;
                $scope.legalEntityFormData.subBuilding = selectedLE.legalEntity.address.subBuilding;
                $scope.legalEntityFormData.buildingNumber = selectedLE.legalEntity.address.streetNo;
                $scope.legalEntityFormData.street = selectedLE.legalEntity.address.streetName;
                $scope.legalEntityFormData.subStreet = selectedLE.legalEntity.address.subStreet;
                $scope.legalEntityFormData.locality = selectedLE.legalEntity.address.locality;
                $scope.legalEntityFormData.subLocality = selectedLE.legalEntity.address.subLocality;
                $scope.legalEntityFormData.city = selectedLE.legalEntity.address.town;
                $scope.legalEntityFormData.state = selectedLE.legalEntity.address.county;
                $scope.legalEntityFormData.subStreet = selectedLE.legalEntity.address.subStreet;
                $scope.legalEntityFormData.subState = selectedLE.legalEntity.address.subCountyStateProvince;
                countryService.broadcastSelectedCountryChangedEvent(selectedLE.legalEntity.address.country.name);
                $scope.legalEntityFormData.postCode = selectedLE.legalEntity.address.postZipCode;
                $scope.legalEntityFormData.subPostCode = selectedLE.legalEntity.address.subPostCode;
                $scope.legalEntityFormData.POBox = selectedLE.legalEntity.address.poBoxNo;
                $scope.legalEntityFormData.compRegNo = selectedLE.legalEntity.compRegNo;
            }
            $scope.legalEntityFormData.vatNo = selectedLE.legalEntity.leVatNo;
            $scope.legalEntityFormData.taxRef = selectedLE.legalEntity.leTaxExemptRef;
            $scope.legalEntityFormData.adrId = selectedLE.legalEntity.address.adrId;
            $scope.legalEntityUI.enableUpdateButton = true;
            $scope.legalEntityUI.enableCreateButton = false;
            $scope.loadVatPrefix();
        }
    }

    $scope.loadCentralSite = function () {
        console.log('inside loadCentralSite of legal Entity');
        if ($scope.isEditable) {
            if ($scope.customer != undefined) {
                $scope.getCentralSite($scope.customer);
            }
        }
        else {
            $scope.legalEntityFormData = {};
        }
    };
    $scope.getCentralSite = function (customer) {
        console.log('inside the controller. received the change message');
        UIService.block();

        if (_.isUndefined($scope.allCountries) || !$scope.allCountries.length > 0) {
            $scope.allCountries = countryService.getAllCountries();
        }
        $scope.showErrorMsg = false;
        $scope.legalEntityUI.showSearchResultsDiv = false;
        $scope.addressSearchResultData = {};
        $scope.legalEntityUI.enableCreateButton = false;
        $scope.legalEntityUI.enableUpdateCentralSiteButton = false;

        var customerId = customer.cusId;
        var customerName = customer.cusName;
        customerService.getCentralSite($scope.contract.id, customerId, function (data, status) {

            if (status == '200') {
                $scope.legalEntityUI.enableCreateButton = true;
            }
            else {
                // This should never happen.
                var title = "Central Site"
                var message = "No Central Site found for user: " + customerName + "\n" + data;
                var btns = [
                    {result:'OK', label:'OK'}
                ];

                UIService.unblock();

                var dialogInstance = UIService.openDialogBox(title, message, true, false);
                dialogInstance.result.then(function () {
                }, function () {
                });
                return;
            }

            if (data == 'undefined' || data == null) {
                console.log('No central site found for customer: ' + customerName + data);
                return;
            }
            $scope.loadSiteDetailOnToForm(data);
            $scope.cachedSiteDTO = JSON.stringify(data);
            // $scope.modifyDataPersistButtonLabel();
            $scope.resetForm();
            UIService.unblock();
        });
        UIService.unblock();
    };
    $scope.loadSiteDetailOnToForm = function (siteDTO) {

        /*var callFrom = "sc";*/
        $scope.centralSite = siteDTO;
        /*        $scope.legalEntityFormData.legalCompanyName = cusLeDTO.name;*/
        $scope.legalEntityFormData.buildingName = siteDTO.building;
        $scope.legalEntityFormData.subBuilding = siteDTO.subBuilding;
        $scope.legalEntityFormData.buildingNumber = siteDTO.buildingNumber;
        $scope.legalEntityFormData.street = siteDTO.street;
        $scope.legalEntityFormData.subStreet = siteDTO.subStreet;
        $scope.legalEntityFormData.locality = siteDTO.locality;
        $scope.legalEntityFormData.subLocality = siteDTO.subLocality;
        $scope.legalEntityFormData.city = siteDTO.city;
        $scope.legalEntityFormData.state = siteDTO.state;
        $scope.legalEntityFormData.subState = siteDTO.subCountyStateProvince;
        countryService.broadcastSelectedCountryChangedEvent(siteDTO.country);
        $scope.legalEntityFormData.postCode = siteDTO.postCode;
        $scope.legalEntityFormData.subPostCode = siteDTO.subPostCode;
        $scope.legalEntityFormData.POBox = siteDTO.poBoxNumber;
        $scope.legalEntityFormData.phoneNumber = siteDTO.phoneNum;


    };

    $scope.$watch('legalEntityFormData.leId', function () {

        if (_.isUndefined($scope.legalEntityFormData.leId)) {
            $scope.disableUpdate = true;
        } else {
            $scope.disableUpdate = false;
        }

    });

    $scope.isValidVATNo = function (vatNo) {
        var vatPrefixTemp = vatNo.substr(0, 2);
        if(!_.isUndefined($scope.vatPrefixOfSelectedCountry) && !_.isNull($scope.vatPrefixOfSelectedCountry) && $scope.vatPrefixOfSelectedCountry != ""){
          if($scope.vatPrefixOfSelectedCountry == vatPrefixTemp){
              return true;
          } else{
              return false;
          }
        }else{
            return true;
        }
    };

    $scope.loadVatPrefix = function () {
        var countryName = $scope.legalEntityFormData.country.name;
        UIService.block();
        if (_.isUndefined(countryName) || _.isNull(countryName) || _.isEmpty(countryName)) {
            $scope.vatPrefixOfSelectedCountry = null;
        } else {
            legalEntityService.getVatPrefixForSelectedCountry(countryName, function (data, status) {
                if (status == '200') {
                    $scope.vatPrefixOfSelectedCountry = data;
                    UIService.unblock();
                } else {
                    $scope.vatPrefixOfSelectedCountry = null;

                    var title = "Legal Entity VAT Prefix.";
                    var message = "Failed to get the VAT details for the selected country " + countryName;
                    var dialogInstance = UIService.openDialogBox(title, message, true, false);
                    UIService.unblock();
                }
                UIService.unblock();
            });
        }
    }

    /*    $scope.resetForm = function () {

     setTimeout(function () {
     $scope.trackFieldGotModified = false;
     $scope.formGotModified = false;
     $scope.$apply;
     }, 30);
     };*/


}
])
// Manage LE controller END
