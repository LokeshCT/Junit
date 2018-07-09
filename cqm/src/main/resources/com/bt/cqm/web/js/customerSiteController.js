var module = angular.module('cqm.controllers');

module.controller('centralSiteController', ['$scope', 'httpService', 'siteService', 'UIService', 'customerService', 'addressService', 'countryService', 'PageContext', 'UserContext', 'WebMetrics', '$rootScope', function ($scope, httpService, siteService, UIService, customerService, addressService, countryService, PageContext, UserContext, WebMetrics, $rootScope) {
    console.log('Inside central site controller');

    /* Re-usable Address Fields - STart*/
    $scope.cachedAddressDTO = undefined;
    $scope.cachedCustStatus = undefined;
    $scope.disabled = true;
    $scope.disableSearch = true;
    //$scope.disableSubmit = true;
    $scope.disableReset = true;
    $scope.hasResult = false;
    $scope.condReqNullFldCount = 0;
    $scope.emptyMandatoryFieldsArr = [];
    $scope.allCountries = countryService.getAllCountries();
    $scope.cachedAddressDTOStr = "";
    $scope.isAddressSelected = false;
    $scope.provReq = false;
    $scope.zipReq = false;
    $scope.clearLatLong = false;
    $scope.allowCentralSiteCreation = true;
    $scope.hasMultipleCentralSite =false;
    $scope.centralSites = {};
    $scope.siteReference = undefined;
    /*Re-usable Address Fields end*/

    if (PageContext.exist()) {
        $scope.selectedSalesChannel = PageContext.getSalesChannel();
        $scope.customer = PageContext.getCustomer();
        $scope.contract = PageContext.getContract();
    }

    if (UserContext.exist()) {
        $scope.salesUser = UserContext.getUser();
    }

    $scope.select2Options = {
        allowClear:true
    };

    $scope.statuses =
    [
        { name:'Unknown', value:'U'},
        { name:'Invalid', value:'N'},
        { name:'Valid', value:'V'}
    ];

    $scope.centralSiteFormData = {};

    $scope.cachedSiteDTO = '';
    $scope.isDefaultCentralSite = true;
    $scope.createOrUpdateLabel = "Create";

    $scope.loadCentralSite = function () {
        console.log('Inside loadCentralSite');
        $scope.centralSiteFormData.custValidStatus = $scope.statuses[0];
        $scope.hasMultipleCentralSite =false;
        $scope.reset();
        if ($scope.customer != undefined) {
            $scope.getCentralSite($scope.customer);
        }
    };

    $scope.$on('LoadCustomerEvent', function () {
        $scope.customer = PageContext.getCustomer();
        $scope.selectValidationStatus($scope.customer);
    });

    $scope.$on('LoadCentralSiteEvent', function () {
        $scope.loadCentralSite();
    });

    $scope.getCentralSite = function (customer) {
        console.log('inside the controller. received the change message');
        UIService.block();

        var customerId = customer.cusId;

        $scope.selectValidationStatus(customer);
        //Modified from Customer Id to Contract ID .. LE Changes.
        $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.INVALID);
        customerService.getCentralSite($scope.contract.id, customerId, function (data, status) {


            if (status == '200') {
                $scope.allowCentralSiteCreation = true;
                WebMetrics.captureWebMetrics(WebMetrics.UserActions.CentralSite);
                $scope.hasMultipleCentralSite =false;
                if (!_.isUndefined(data) && !_.isNull(data)) {
                    if (_.isEmpty(data.country)) {
                        $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.INVALID);
                    } else {
                        $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.VALID);
                    }
                    $scope.loadSiteDetailOnToForm(data);
                    if (!_.isUndefined(data) && !_.isEmpty(data.city)) {
                        $scope.isDefaultCentralSite = false;
                        $scope.createOrUpdateLabel = "Update";
                    } else {
                        $scope.isDefaultCentralSite = true;
                        $scope.createOrUpdateLabel = "Create";
                    }
                    //Cache Central Site Id
                    if (_.isUndefined(PageContext.getCentralSiteId()) && !_.isUndefined(data.siteId)) {
                        PageContext.setCentralSiteId(data.siteId);
                    }
                }

            } else if (status == '404') {
                $scope.allowCentralSiteCreation = true;
                $scope.hasMultipleCentralSite =false;
                UIService.openDialogBox("Central Site", 'Please create a Central site before proceeding !!', true, false);
            } else if (status == '409') {
                $scope.allowCentralSiteCreation = false;
                $scope.hasMultipleCentralSite = true;


                $scope.centralSites = data;

                if (!$rootScope.$$phase) $rootScope.$digest();

                UIService.unblock();
            } else {
                var title = "Central Site";
                $scope.allowCentralSiteCreation = false;
                $scope.hasMultipleCentralSite =false;
                UIService.handleException(title, data, status);
                return;
            }

            UIService.unblock();
        });
    };

    $scope.$watch('centralSiteFormData.custValidStatus', function (status) {
        console.log('Cust Status Change ...');
        //Don't remove :: Without this watch for some reason the html change to model is getting reflected in JS
    });

    $scope.$watch('addressDTO.buildingName', function (buildingName) {
        $scope.checkAddressValidation();
    });

    $scope.$watch('addressDTO.buildingNumber', function (buildingName) {
        $scope.checkAddressValidation();
    });

    $scope.$watch('addressDTO.street', function (buildingName) {
        $scope.checkAddressValidation();
    });

    $scope.$watch('addressDTO.locality', function (buildingName) {
        $scope.checkAddressValidation();
    });

    $scope.$watch('addressDTO.POBox', function (buildingName) {
        $scope.checkAddressValidation();
    });

    $scope.$watch('addressDTO.country', function (country) {
        if (!_.isUndefined(country)) {
            $scope.processCountryChange(country.name);
        }
        $scope.validateAddrSearch();
    });

    $scope.$watch('addressDTO.city', function (city) {
        $scope.validateAddrSearch();
    });

    $scope.validateAddrSearch = function () {

        if ((!_.isUndefined($scope.addressDTO.country) && _.isEmpty($scope.addressDTO.country.name)) || _.isEmpty($scope.addressDTO.city)) {
            $scope.disableSearch = true;
        } else {
            $scope.disableSearch = false;
        }

    };

    $scope.onClearClick = function () {
        if ($scope.clearLatLong) {
            $scope.cachedLat = $scope.addressDTO.latitude;
            $scope.cachedLng = $scope.addressDTO.longitude;
            $scope.addressDTO.latitude = '';
            $scope.addressDTO.longitude = '';
        } else {
            $scope.addressDTO.latitude = $scope.cachedLat;
            $scope.addressDTO.longitude = $scope.cachedLng;
        }
    }

    $scope.checkAddressValidation = function () {

        if (_.isEmpty($scope.addressDTO.POBox)) {
            var hasBuilding = false;
            var hasStreet = false;

            if (!_.isEmpty($scope.addressDTO.buildingName) || !_.isEmpty($scope.addressDTO.buildingNumber)) {
                $scope.isBuildingNumReq = false;
                $scope.isBuildingNameReq = false;
                hasBuilding = true;
            } else {
                $scope.isBuildingNumReq = true;
                $scope.isBuildingNameReq = true;
            }

            if (!_.isEmpty($scope.addressDTO.street) || !_.isEmpty($scope.addressDTO.locality)) {
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

    $scope.loadSiteDetailOnToForm = function (siteDTO) {

        if (!_.isUndefined(siteDTO)) {
            $scope.centralSiteFormData.localCompanyName = siteDTO.name;
            $scope.addressDTO = {};

            $scope.addressDTO.buildingName = siteDTO.building;
            $scope.addressDTO.subBuilding = siteDTO.subBuilding;
            $scope.addressDTO.buildingNumber = siteDTO.buildingNumber;
            $scope.addressDTO.street = siteDTO.street;
            $scope.addressDTO.subStreet = siteDTO.subStreet;
            $scope.addressDTO.locality = siteDTO.locality;
            $scope.addressDTO.subLocality = siteDTO.subLocality;
            $scope.addressDTO.city = siteDTO.city;
            $scope.addressDTO.state = siteDTO.state;
            $scope.addressDTO.subState = siteDTO.subCountyStateProvince;
            $scope.addressDTO.postCode = siteDTO.postCode;
            $scope.addressDTO.subPostCode = siteDTO.subPostCode;
            $scope.addressDTO.POBox = siteDTO.poBoxNumber;
            $scope.addressDTO.phoneNumber = siteDTO.phoneNum;
            $scope.addressDTO.latitude = siteDTO.latitude;
            $scope.addressDTO.longitude = siteDTO.longitude;

            $scope.siteReference = siteDTO.siteReference;

            countryService.broadcastSelectedCountryChangedEvent(siteDTO.country);

            $scope.cachedAddressDTOStr = JSON.stringify($scope.addressDTO);
        }else{
            $scope.siteReference=undefined;
        }
        //$scope.fillAddressFields(addressDto);
    };


    $scope.createCentralSite = function () {
        UIService.block();
        var startTime = new Date().getTime();
        var salesChannel = $scope.selectedSalesChannel;
        var cusName = $scope.customer.cusName;
        var cusId = $scope.customer.cusId;

        if (cusId == null || cusId == undefined || cusId == "") {
            cusId = " ";
        }
        var userId = $scope.salesUser.ein;

        if (!$scope.allowCentralSiteCreation) {
            UIService.openDialogBox('Create Site', 'Site Creation is not allowed as multiple central Sites are already available in BFG. Please contact System Administrator to map appropriate site to contract !! Contract ID :' + $scope.contract.id + ', Customer Id :' + cusId, true, false);
            return;
        }

        siteService.createSite(userId, cusId, cusName, $scope.contract.id,$scope.siteReference, $scope.centralSiteFormData, $scope.addressDTO, function (data, status) {
            var title = 'Customer Site';
            var message = "";
            var btns = [
                {result:'OK', label:'OK'}
            ];
            if (status == '200') {
                title = $scope.createOrUpdateLabel + ' Customer Site';
                if ('Create' == $scope.createOrUpdateLabel) {
                    message = 'Central site successfully created for customer - ' + cusName;
                } else {
                    message = 'Central site successfully updated for customer - ' + cusName;
                }
                $scope.getCentralSite($scope.customer);
                $scope.isAdrSearchSuccess = false;
                $scope.showMap = false;
                UIService.unblock();
                customerService.updateCustomer(undefined, $scope.centralSiteFormData.custValidStatus.value);
                WebMetrics.captureWebMetrics('CQM Customer Tab - Create Central Site', startTime);
                UIService.openDialogBox(title, message, true, false);
            } else {
                UIService.handleException(title, data, status);
            }
            UIService.unblock();
        });
    };

    /* Helper Functions */

    $scope.selectValidationStatus = function (customer) {
        _.forEach($scope.statuses, function (status) {
            if (status.value == customer.status) {
                $scope.centralSiteFormData.custValidStatus = status;
                $scope.cachedCustStatus = status;
                return;
            }
        });
    };


    /*  Reusable Address Filed Capture and Validation - Start*/

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
            $scope.clearLatLong = false;
            $scope.fillAddressFieldsFromGrid(item.entity);
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
        var startTime = new Date().getTime();
        var nadAddressRequestDTO = new Object();
        var queryParamObject = new Object();
        queryParamObject.city = $scope.addressDTO.city;
        queryParamObject.country = $scope.addressDTO.country.name;
        queryParamObject.postCode = $scope.addressDTO.postCode;
        if (queryParamObject.postCode == "" || queryParamObject.postCode == undefined) {
            queryParamObject.postCode = "0";
        }
        queryParamObject.locality = $scope.addressDTO.locality;
        queryParamObject.countryCode = $scope.addressDTO.country.codeAlpha2;
        queryParamObject.building = $scope.addressDTO.buildingName;
        queryParamObject.street = $scope.addressDTO.street;
        queryParamObject.buildingNumber = $scope.addressDTO.buildingNumber;
        queryParamObject.subStreet = $scope.addressDTO.subStreet;
        queryParamObject.subLocality = $scope.addressDTO.subLocality;
        queryParamObject.state = $scope.addressDTO.state;
        queryParamObject.poBox = $scope.addressDTO.poBox;
        queryParamObject.company = $scope.addressDTO.company;
        queryParamObject.subState = $scope.addressDTO.subState;
        queryParamObject.subPostCode = $scope.addressDTO.subPostCode;
        queryParamObject.subBuilding = $scope.addressDTO.subBuilding;
        queryParamObject.latitude = $scope.addressDTO.latitude;
        queryParamObject.longitude = $scope.addressDTO.longitude;

        queryParamObject.locality = $scope.addressDTO.locality;
        nadAddressRequestDTO.q = JSON.stringify(queryParamObject);

        var processAddressCallbackFunc = function (data, status) {
            if ('200' == status) {
                WebMetrics.captureWebMetrics('CQM Customer Tab - Search Address', startTime);

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
        };

        addressService.searchAddress(nadAddressRequestDTO, processAddressCallbackFunc);

    };

    $scope.getGeoCode = function () {
        UIService.block();
        var startTime = new Date().getTime();
        var nadAddressRequestDTO = new Object();
        var queryParamObject = new Object();
        queryParamObject.city = $scope.addressDTO.city;
        queryParamObject.country = $scope.addressDTO.country.name;
        queryParamObject.postCode = $scope.addressDTO.postCode;
        if (queryParamObject.postCode == "" || queryParamObject.postCode == undefined) {
            queryParamObject.postCode = "0";
        }
        nadAddressRequestDTO.q = JSON.stringify(queryParamObject);

        var processResponseFunc = function (data, status) {
            if ('200' == status) {
                WebMetrics.captureWebMetrics('CQM Customer Tab - Search Address -Get Geo Code', startTime);
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
                    $scope.addressDTO.longitude = $scope.addressWithLongAndLat[0].longitude;
                    $scope.addressDTO.latitude = $scope.addressWithLongAndLat[0].latitude;
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

    $scope.fillAddressFieldsFromGrid = function (selectedItem) {

        if (!_.isUndefined(selectedItem)) {
            var msg = 'Do you want to update current address details with the one you selected from Search Results?';
            var dialogInstance = UIService.openDialogBox('Update Address Details', msg, true, true);
            dialogInstance.result.then(function () {
                $scope.addressDTO = {};
                $scope.addressDTO.buildingName = selectedItem.buildingName;
                $scope.addressDTO.subBuilding = selectedItem.subBuilding;
                $scope.addressDTO.buildingNumber = selectedItem.buildingNumber;
                $scope.addressDTO.street = selectedItem.street;
                $scope.addressDTO.subStreet = selectedItem.subStreet;
                $scope.addressDTO.locality = selectedItem.locality;
                $scope.addressDTO.subLocality = selectedItem.subLocality;
                $scope.addressDTO.city = selectedItem.city;
                $scope.addressDTO.state = selectedItem.state;
                $scope.addressDTO.subState = selectedItem.subState;

                countryService.broadcastSelectedCountryChangedEvent(selectedItem.country);

                $scope.addressDTO.postCode = selectedItem.zipCode;
                $scope.addressDTO.subPostCode = selectedItem.subPostCode;
                $scope.addressDTO.POBox = selectedItem.poBox;
                $scope.addressDTO.phoneNumber = selectedItem.phoneNumber;
                $scope.addressDTO.longitude = selectedItem.longitude;
                $scope.addressDTO.latitude = selectedItem.latitude;
                $scope.getGeoCode();

            }, function () {

            })
        }

    };

    $scope.reset = function () {
        $scope.isAdrSearchSuccess = false;
        $scope.clearLatLong = false;
        var cachedAddressDTO = {};
        if (!_.isUndefined($scope.cachedAddressDTOStr) && !$scope.cachedAddressDTOStr.length < 1) {
            $scope.addressDTO = JSON.parse($scope.cachedAddressDTOStr);
            if (!_.isUndefined($scope.addressDTO) && !_.isUndefined($scope.addressDTO.country) && !_.isUndefined($scope.addressDTO.country.name)) {
                countryService.broadcastSelectedCountryChangedEvent($scope.addressDTO.country.name);
            }
        } else {
            $scope.addressDTO = {};
        }

        if (!_.isUndefined($scope.centralSiteFormData)) {
            $scope.centralSiteFormData.custValidStatus = $scope.cachedCustStatus;
        }
    };

    $scope.$on('selectedCountryChanged', function (event, countryName) {
        console.log('inside the selectedCountryChanged listener.');
        $scope.processCountryChange(countryName);
    });


    $scope.processCountryChange = function (countryName) {
        if (!_.isUndefined(countryName)) {
            _.forEach($scope.allCountries, function (country) {
                if (country.name.toUpperCase() == countryName.toUpperCase()) {
                    $scope.addressDTO.country = country;

                    var hasMatchingZip = countryService.isZipRequired(country.name.toUpperCase());

                    if (hasMatchingZip) {
                        $scope.zipRfo = '(RFO)';
                        $scope.zipReq = true;
                    } else {
                        $scope.zipRfo = '';
                        $scope.zipReq = false;
                    }

                    var hasMatchingProv = countryService.isProvinceRequired(country.name.toUpperCase());
                    if (hasMatchingProv) {
                        $scope.provRfo = '(RFO)';
                        $scope.provReq = true;
                    } else {
                        $scope.provRfo = '';
                        $scope.provReq = false;
                    }

                    return;

                }
            });
        }
    }

    /*  Reusable Address Filed Capture and Validation - End*/


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
            if (!(_.isUndefined($scope.addressDTO) || _.isUndefined($scope.addressDTO.latitude) || ((_.isNumber($scope.addressDTO.latitude) && _.isNull($scope.addressDTO.latitude)) || (_.isString($scope.addressDTO.latitude) && _.isEmpty($scope.addressDTO.latitude))))) {

                latLng = new google.maps.LatLng($scope.addressDTO.latitude, $scope.addressDTO.longitude);
            }

            if (_.isUndefined(latLng)) {
                if (!(_.isUndefined($scope.addressDTO))) {
                    var address = $scope.addressDTO.city + "," + $scope.addressDTO.country.name + "," + $scope.addressDTO.street + "," + $scope.addressDTO.locality;
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
                                $scope.addressDTO.latitude = lat;
                                $scope.addressDTO.longitude = lng;

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
                                                        $scope.addressDTO.buildingNumber = address_component_val;
                                                    } else if ('route' == address_attib_name) {
                                                        $scope.addressDTO.street = address_component_val;
                                                    } else if ('locality' == address_attib_name) {
                                                        $scope.addressDTO.locality = address_component_val;
                                                    } else if ('sublocality' == address_attib_name) {
                                                        $scope.addressDTO.subLocality = address_component_val;
                                                    } else if ('administrative_area_level_1' == address_attib_name) {
                                                        $scope.addressDTO.state = address_component_val;
                                                    } else if ('administrative_area_level_2' == address_attib_name) {
                                                        $scope.addressDTO.city = address_component_val;
                                                    } else if ('postal_code' == address_attib_name) {
                                                        $scope.addressDTO.postCode = address_component_val;
                                                    } else if ('postal_town' == address_attib_name) {
                                                        $scope.addressDTO.state = address_component_val;
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
                    $scope.addressDTO.latitude = lat;
                    $scope.addressDTO.longitude = lng;

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
                                            $scope.addressDTO.buildingNumber = address_component_val;
                                        } else if ('route' == address_attib_name) {
                                            $scope.addressDTO.street = address_component_val;
                                        } else if ('locality' == address_attib_name) {
                                            $scope.addressDTO.locality = address_component_val;
                                        } else if ('sublocality' == address_attib_name) {
                                            $scope.addressDTO.subLocality = address_component_val;
                                        } else if ('administrative_area_level_1' == address_attib_name) {
                                            $scope.addressDTO.state = address_component_val;
                                        } else if ('administrative_area_level_2' == address_attib_name) {
                                            $scope.addressDTO.city = address_component_val;
                                        } else if ('postal_code' == address_attib_name) {
                                            $scope.addressDTO.postCode = address_component_val;
                                        } else if ('postal_town' == address_attib_name) {
                                            $scope.addressDTO.state = address_component_val;
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


    $scope.nullifyAddress = function () {

        $scope.addressDTO.buildingNumber = '';
        $scope.addressDTO.buildingName = '';
        $scope.addressDTO.subBuilding = '';
        $scope.addressDTO.street = '';
        $scope.addressDTO.subStreet = '';
        $scope.addressDTO.locality = '';
        $scope.addressDTO.subLocality = '';
        $scope.addressDTO.city = '';
        $scope.addressDTO.state = '';
        $scope.addressDTO.subState = '';
        $scope.addressDTO.postCode = '';
        $scope.addressDTO.subPostCode = '';
        $scope.addressDTO.POBox = '';
        $scope.addressDTO.floor = '';
        $scope.addressDTO.room = '';
        countryService.broadcastSelectedCountryChangedEvent('');
    }
/////////////////////////

}])
