'use strict';

var module = angular.module('cqm.controllers');

module.controller('GenericAddressController', ['$scope', '$routeParams', '$http', 'countryService', 'httpService', '$modal', 'addressService', 'siteService', 'UIService', 'customerService', 'salesChannelService', 'PageContext', 'UserContext', '$compile', function ($scope, $routeParams, $http, countryService, httpService, $modal, addressService, siteService, UIService, customerService, salesChannelService, PageContext, UserContext, $compile) {


    $scope.cachedAddressDTO = undefined;
    $scope.addressDto = undefined;
    $scope.disabled = true;
    $scope.disableSearch = true;
    $scope.disableSubmit = true;
    $scope.disableReset = true;
    $scope.hasResult = false;
    $scope.condReqNullFldCount = 0;
    $scope.emptyMandatoryFieldsArr = [];
    $scope.allCountries = countryService.getAllCountries();
    $scope.cachedAddressDTOStr = "";
    $scope.isAddressSelected = false;

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
            $scope.isAddressSelected = true;
            $scope.fillAddressFields(item.entity);
            $scope.getGeoCode();
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
            {field:'countryCode', displayName:'Country Code', width:120}
        ]
    };

    $scope.searchAddress = function () {
        UIService.block();

        var nadAddressRequestDTO = new Object();
        var queryParamObject = new Object();
        queryParamObject.city = $scope.addressDto.city;
        queryParamObject.country = $scope.addressDto.country.name;
        queryParamObject.postCode = $scope.addressDto.postCode;
        if (queryParamObject.postCode == "" || queryParamObject.postCode == undefined) {
            queryParamObject.postCode = "0";
        }

        queryParamObject.locality = $scope.addressDto.locality;
        nadAddressRequestDTO.q = JSON.stringify(queryParamObject);

        var processAddressCallbackFunc = function (data, status) {
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

        addressService.searchAddress(nadAddressRequestDTO, processAddressCallbackFunc);

    };

    $scope.getGeoCode = function () {
        UIService.block();

        var nadAddressRequestDTO = new Object();
        var queryParamObject = new Object();
        queryParamObject.city = $scope.addressDto.city;
        queryParamObject.country = $scope.addressDto.country.name;
        queryParamObject.postCode = $scope.addressDto.postCode;
        if (queryParamObject.postCode == "" || queryParamObject.postCode == undefined) {
            queryParamObject.postCode = "0";
        }
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
                    $scope.addressDto.longitude = $scope.addressWithLongAndLat[0].longitude;
                    $scope.addressDto.latitude = $scope.addressWithLongAndLat[0].latitude;
                    $scope.isAdrSearchSuccess = true;
                    window.setTimeout(function () {
                        $(window).resize();
                    }, 1);

                } else {
                    $scope.isAdrSearchSuccess = false;
                    var title = 'Get Geo-Code';
                    var message = data.errorText;
                    var dialogInstance = UIService.openDialogBox(title, message,
                                                                 true, false);
                    dialogInstance.result.then(function () {
                    }, function () {
                    });
                }
            } else {
                $scope.isAdrSearchSuccess = false;
                var title = 'Get Geo-Code';
                var message = 'Get geo-code failed. Error Code: ' + status;
                var dialogInstance = UIService.openDialogBox(title, message, true, false);
                dialogInstance.result.then(function () {
                }, function () {
                });
            }
            UIService.unblock();
        };
        addressService.searchAddressWithGeoCode(nadAddressRequestDTO, processResponseFunc);
    };

    $scope.fillAddressFields = function (newAddress) {

        if (!_.isUndefined(newAddress)) {
            $scope.addressDto = {};
            $scope.addressDto.buildingName = newAddress.buildingName;
            $scope.addressDto.subBuilding = newAddress.subBuilding;
            $scope.addressDto.buildingNumber = newAddress.buildingNumber;
            $scope.addressDto.street = newAddress.street;
            $scope.addressDto.subStreet = newAddress.subStreet;
            $scope.addressDto.locality = newAddress.locality;
            $scope.addressDto.subLocality = newAddress.subLocality;
            $scope.addressDto.city = newAddress.city;
            $scope.addressDto.state = newAddress.state;
            $scope.addressDto.subState = newAddress.subState;

            countryService.broadcastSelectedCountryChangedEvent(newAddress.country);

            $scope.addressDto.postCode = newAddress.postCode;
            $scope.addressDto.subPostCode = newAddress.subPostCode;
            $scope.addressDto.POBox = newAddress.POBox;
            $scope.addressDto.phoneNumber = newAddress.phoneNumber;
            $scope.addressDto.longitude = newAddress.longitude;
            $scope.addressDto.latitude = newAddress.latitude;

            setTimeout(function () {
                $scope.$apply;
            }, 30);
        }

    };

    $scope.reset = function () {

        var cachedAddressDTO = {};
        if (!_.isUndefined($scope.cachedAddressDTOStr) && !$scope.cachedAddressDTOStr.length < 1) {
            $scope.cachedAddressDTOObj = JSON.parse($scope.cachedAddressDTOStr);
        }
        $scope.fillAddressFields($scope.cachedAddressDTOObj);
        addressService.broadcastResetAddress();
        $scope.disableSubmit = false;
    };

    /*    $scope.$on('selectedCountryChanged', function (event, countryName) {
     console.log('inside the selectedCountryChanged listener.');

     _.forEach($scope.allCountries, function (country) {
     if (country.name.toUpperCase() == countryName.toUpperCase()) {
     if ($scope.isAddressSelected) {
     $scope.addressDto.country = country;
     $scope.isAddressSelected=false;
     } else {
     setTimeout(function () {
     $scope.addressDto.country = country;
     $scope.$apply();
     }, 30);
     }
     return;

     }
     });
     });*/

    /*  $scope.$on('onLoadAddress', function (event, addressDto) {
     $scope.cachedAddressDTOStr = JSON.stringify(addressDto);
     $scope.fillAddressFields(addressDto);
     });
     */

    /*    $scope.$on('onSubmitAddress', function (event) {
     $scope.isAdrSearchSuccess = false;
     });*/

    $scope.updateCountry = function () {
        countryService.broadcastSelectedCountryChangedEvent($scope.addressDto.country.name);
        $scope.countryCode = $scope.addressDto.country.codeAlpha2;
    };


}])
