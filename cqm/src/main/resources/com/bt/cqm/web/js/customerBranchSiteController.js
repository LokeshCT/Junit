var module = angular.module('cqm.controllers');

// Branch site controller BEGIN
module.controller('customerBranchSiteController', ['$scope', '$routeParams', '$http', 'countryService', 'httpService', '$modal', 'addressService', 'siteService', 'UIService', 'branchSiteService', 'salesChannelService', 'PageContext', 'UserContext', 'WebMetrics', '$rootScope', function ($scope, $routeParams, $http, countryService, httpService, $modal, addressService, siteService, UIService, branchSiteService, salesChannelService, PageContext, UserContext, WebMetrics, $rootScope) {


    console.log('Inside branch site controller');
    $scope.cachedBranchSite = undefined;
    $scope.selectedBranchSite = undefined;
    $scope.clearLatLong = false;
    $scope.showAddLocDialog = false;
    //$scope.cachedAddrDtoStr = undefined;

    if (PageContext.exist()) {
        $scope.customer = PageContext.getCustomer();
        $scope.contract = PageContext.getContract();
        $scope.selectedSalesChannel = PageContext.getSalesChannel();
    }

    if (UserContext.exist()) {
        $scope.salesUser = UserContext.getUser();
    }

    /* Re-usable Address Fields - STart*/
    $scope.disableSearch = true;
    $scope.disableSubmit = true;
    $scope.disableReset = true;
    $scope.disablePriceFields = false;
    $scope.emptyMandatoryFieldsArr = [];
    $scope.allCountries = countryService.getAllCountries();
    /*Re-usable Address Fields end*/
    $scope.disableUpdate = true;
    $scope.associatedQuotes = [];
    $scope.numOfMatchingAddresses = 0;
    $scope.input = null;
    $scope.siteRegionList = [];

    $scope.numOfMatchingBranchSites = 0;
    $scope.addressDTO = {};
    $scope.siteDTO = {};
    $scope.showWarningMessageRegion = true;
    $scope.provRfq = '';
    $scope.zipRfq = '';
    $scope.zipRfqReq = false;
    $scope.provRfqReq = false;
    $scope.siteDTO.notifySiteUpdate = false;
    $scope.showQuoteDialog = false;
    $scope.branchSiteUI = {'enableCreateButton':false, "enableUpdateBranchSiteButton":false, 'showSearchResultsDiv':false};

    $scope.showSearchResults = false;

    $scope.selectedNADAddress = [];

    $scope.branchSiteUI.geoCodeSearch = false;
    // $scope.associatedQuotes= [];
    $scope.select2Options = {
        allowClear:true
    };

    $scope.displayGrid = true;
    $scope.allowNewRegion = false;
    $scope.hasPreLoadedRegions = false;

    $scope.statuses =
    [
        { name:'Unknown', value:'U'},
        { name:'Invalid', value:'N'},
        { name:'Valid', value:'V'}
    ];

    $scope.siteTypeList = [
        {name:'', value:0},
        {name:'Ordered Sites', value:1},
        {name:'Quote Only Sites', value:2}
    ]

    $scope.branchSiteFormData = {};
    $scope.showMap = false;
    $scope.map;

    $scope.isReadonly = false;
    $scope.fiterText = '';
    $scope.siteType = 0;

    $scope.clickedAddLoc = false;
    $scope.updateSiteOrCreateLocLabel = "Update Branch Site"

    $scope.loadCreateSitePage = function () {
        $scope.siteType = 0;
        $scope.fiterText = '';
        // countryService.broadcastSelectedCountryChangedEvent('');
        $scope.addressGridSelected = false;
        $scope.showBranchSiteEdit = true;
        $scope.clearLatLong = false;
        //$scope.isMncOrCs = $scope.isMncOrCSChannel();
        $scope.clear();
    }
    $scope.loadBranchSite = function () {
        console.log('Inside LoadBranchSite');
        $scope.siteType = 0;
        $scope.fiterText = '';
        $scope.onStandardFilterChange();
        $scope.addressGridSelected = false;
        $scope.showBranchSiteEdit = true;
        $scope.clearLatLong = false;
        $scope.allowNewRegion = false;
        $scope.hasPreLoadedRegions = false;
        $scope.clear();
        //   $scope.getSiteRegions();
        $scope.getBranchSite();
        //$scope.isMncOrCs = $scope.isMncOrCSChannel();
    }

    /*$scope.isMncOrCSChannel = function(){
     return (PageContext.isMnc || (PageContext.getSalesChannelName() == 'MANAGED SERVICES FROM BT'));
     }*/
    /*    $scope.getBranchSiteCreateUpdate = function (isCreate, responseData) {
     UIService.block();
     console.log('inside the controller. received the change message');
     $scope.branchSiteUI.showSearchResultsDiv = false;
     $scope.addressSearchResultData = {};
     $scope.branchSiteUI.enableCreateButton = false;
     $scope.branchSiteUI.enableUpdateBranchSiteButton = false;

     $scope.branchSiteList = [];
     var startTime = new Date().getTime();
     $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.INVALID);
     branchSiteService.getBranchSite($scope.salesUser.ein, $scope.selectedSalesChannel.name, $scope.customer.cusId, function (data, status) {
     if (status == '200') {
     $scope.branchSite = data;
     var branchSiteListTemp = [];
     if (data.length == undefined) {
     branchSiteListTemp = [data];
     $scope.numOfMatchingBranchSites = 1;
     }
     else {
     branchSiteListTemp = data;
     $scope.numOfMatchingBranchSites = branchSiteListTemp.length;
     }
     $scope.branchSiteList = branchSiteListTemp;
     window.setTimeout(function () {
     $(window).resize();
     }, 1);
     $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.VALID);
     if (isCreate) {
     var title = 'Create Customer Branch Site';
     var message = "Successfully Created Branch Site for " + $scope.customer.cusName + ".";
     $scope.clear();
     var dialogInstance = UIService.openDialogBox(title, message, true, false);
     dialogInstance.result.then(function () {
     }, function () {
     UIService.unblock();
     });
     } else {
     var message = "Successfully Updated Branch Site for " + $scope.customer.cusName + ".";
     if ($scope.siteDTO.notifySiteUpdate) {
     if (responseData == 'true') {
     message = "Successfully Updated Branch Site for " + $scope.customer.cusName + " and SQE has been Notified Regarding the changes";
     }
     else {
     message = "Successfully Updated Branch Site for " + $scope.customer.cusName + "  and Notifying SQE has failed ,Please update with same details";
     }
     }
     var title = 'Update Customer Branch Site';
     $scope.clear();
     var dialogInstance = UIService.openDialogBox(title, message, true, false);
     dialogInstance.result.then(function () {
     }, function () {
     UIService.unblock();
     });
     }

     } else if (status == '404') {
     //Do Nothing
     } else {
     UIService.handleException('Search Branch', data, status);
     }
     // WebMetrics.captureWebMetrics(WebMetrics.UserActions.BranchSites, startTime);
     UIService.unblock();
     });
     }*/

    $scope.onStandardFilterChange = function () {

        switch ($scope.siteType) {
            case 0:
                $scope.filterTextStdFilter = '';
                break;
            case 1:
                $scope.filterTextStdFilter = 'sitQuoteOnlyFlag:N';
                break;
            case 2:
                $scope.filterTextStdFilter = 'sitQuoteOnlyFlag:Y';
                break;

        }

        $scope.updateFilterText();
    }


    $scope.updateFilterText = function () {
        $scope.branchSiteGrid.filterOptions.filterText = $scope.filterTextStdFilter + ";" + $scope.fiterText;
    }

    $scope.$watch('fiterText', function () {
        if (!_.isUndefined($scope.fiterText)) {
            $scope.updateFilterText();
        }
    })

    $scope.getBranchSite = function () {
        UIService.block();
        console.log('inside the controller. received the change message');
        $scope.branchSiteUI.showSearchResultsDiv = false;
        $scope.addressSearchResultData = {};
        $scope.branchSiteUI.enableCreateButton = false;
        $scope.branchSiteUI.enableUpdateBranchSiteButton = false;

        $scope.branchSiteList = [];
        var startTime = new Date().getTime();
        $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.INVALID);
        branchSiteService.getBranchSite($scope.salesUser.ein, $scope.selectedSalesChannel.name, $scope.customer.cusId, function (data, status) {
            if (status == '200') {
                $scope.branchSite = data;
                var branchSiteListTemp = [];
                if (data.length == undefined) {
                    branchSiteListTemp = [data];
                    $scope.numOfMatchingBranchSites = 1;
                }
                else {
                    branchSiteListTemp = data;
                    $scope.numOfMatchingBranchSites = branchSiteListTemp.length;
                }
                $scope.branchSiteList = branchSiteListTemp;
                window.setTimeout(function () {
                    $(window).resize();
                }, 1);
                $rootScope.$broadcast(EVENT.NodeStatusChange, NODE_STATUS.VALID);
            } else if (status == '404') {
                //Do Nothing
            } else {
                UIService.handleException('Branch Search', data, status);
            }
            WebMetrics.captureWebMetrics(WebMetrics.UserActions.BranchSites, startTime);
            UIService.unblock();
        });
    }

    $scope.addLocation = function () {
        /*$scope.clickedAddLoc = !$scope.clickedAddLoc;

         if ($scope.clickedAddLoc) {
         $scope.reset();
         $scope.addLocLabel = "Back";
         $scope.updateSiteOrCreateLocLabel = "Create Location";
         } else {
         $scope.addLocLabel = "Add Location";
         $scope.updateSiteOrCreateLocLabel = "Update Branch Site";
         }*/

        $scope.showAddLocDialog = true;
    }


    $scope.isExistingBranchSite = function (localCompanyName) {
        if (localCompanyName != undefined) {
            return true;
        }
        return false;
    };

    $scope.$on('selectedCountryChanged', function (event, countryName) {
        console.log('inside the selectedCountryChanged listener.');

        $scope.processCountryChange(countryName);
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

    $scope.processCountryChange = function (countryName) {
        if (!_.isUndefined(countryName)) {
            _.forEach($scope.allCountries, function (country) {
                if (country.name.toUpperCase() == countryName.toUpperCase()) {
                    $scope.addressDTO.country = country;

                    var hasMatchingZip = countryService.isZipRequired(country.name.toUpperCase());

                    if (hasMatchingZip) {
                        $scope.zipRfq = '(RFQ)';
                        $scope.zipRfqReq = true;
                    } else {
                        $scope.zipRfq = '';
                        $scope.zipRfqReq = false;
                    }

                    var hasMatchingProv = countryService.isProvinceRequired(country.name.toUpperCase());
                    if (hasMatchingProv) {
                        $scope.provRfq = '(RFQ)';
                        $scope.provRfqReq = true;
                    } else {
                        $scope.provRfq = '';
                        $scope.provRfqReq = false;
                    }
                    $scope.getSiteRegions();
                    return;
                }
            });
        }
    }


    $scope.searchAddress = function () {
        console.log('Search address invoked');
        UIService.block();
        $scope.branchSiteUI.geoCodeSearch = false;
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

        nadAddressRequestDTO.q = JSON.stringify(queryParamObject);
        $scope.showMap = false;

        var processResponseFunc = function (data, status) {
            if ('200' == status) {
                WebMetrics.captureWebMetrics('CQM Manage Sites Tab - Load Search Address', startTime);
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
                    $scope.addressDTO.longitude = $scope.addressWithLongAndLat[0].longitude;
                    $scope.addressDTO.latitude = $scope.addressWithLongAndLat[0].latitude;
                    $scope.addressDTO.adrAccuracyLevel = $scope.addressWithLongAndLat[0].accuracyLevel;
                    $scope.addressDTO.adrValidationLevel = $scope.addressWithLongAndLat[0].failLevel;
                    $scope.isAdrSearchSuccess = true;
                    window.setTimeout(function () {
                        $(window).resize();
                    }, 1);
                    WebMetrics.captureWebMetrics('CQM Manage Sites Tab - Load Geo Code', startTime);
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


    $scope.reset = function () {
        $scope.clearLatLong = false;
        if (!_.isEmpty($scope.siteDTO)) {
            if (!_.isUndefined($scope.cachedBranchSite) && !_.isEmpty($scope.cachedBranchSite)) {
                var branchSiteObj = JSON.parse($scope.cachedBranchSite)
                $scope.fillBranchSiteForm(branchSiteObj);
            }
        }

        $scope.isAdrSearchSuccess = false;

    };

    $scope.clear = function () {
        $scope.clearLatLong = false;
        $scope.addressDTO = {};
        $scope.siteDTO = {};
        if (!_.isUndefined($scope.branchSiteFormData)) {
            $scope.branchSiteFormData.localCompanyName = "";
        }

        if (!_.isUndefined($scope.branchSiteGrid) && !_.isUndefined($scope.branchSiteGrid.$gridScope)) {
            $scope.branchSiteGrid.$gridScope.toggleSelectAll(false, false);
            $scope.branchSiteGrid.$gridScope.selectedItems.length = 0;
        }

        $scope.isAdrSearchSuccess = false;
        countryService.broadcastSelectedCountryChangedEvent('');

    }

    $scope.clickSubmit = function () {

        $scope.updateBranchSite();

    }


    $scope.createBranchSite = function () {
        UIService.block();
        var cusName = $scope.customer.cusName;
        var cusId = $scope.customer.cusId;
        if (cusId == null || cusId == undefined || cusId == "") {
            cusId = " ";
        }
        var ein = $scope.salesUser.ein;

        if (_.isEmpty($scope.siteDTO.sitCusRegion)) {
            $scope.siteDTO.isNewRegion = false;
            dialog = $scope.getWarningDialogForBlankRegion('Create');
            dialog.result.then(function () {
                branchSiteService.createBranchSite(ein, cusId, cusName, $scope.addressDTO, $scope.siteDTO, $scope.createBranchSiteCallback);
            }, function () {

            });
        } else if ($scope.hasPreLoadedRegions && $scope.allowNewRegion) {
            var dialog = $scope.getWarningDialogForNewRegion('Create', cusName, $scope.siteDTO.sitCusRegion, $scope.addressDTO.country.name);
            dialog.result.then(function () {
                $scope.siteDTO.isNewRegion = true;
                branchSiteService.createBranchSite(ein, cusId, cusName, $scope.addressDTO, $scope.siteDTO, $scope.createBranchSiteCallback);
            }, function () {
                /* Don't update site*/
                $scope.allowNewRegion = false;
                UIService.unblock();
                return;
            });

        } else {
            $scope.siteDTO.isNewRegion = false;
            branchSiteService.createBranchSite(ein, cusId, cusName, $scope.addressDTO, $scope.siteDTO, $scope.createBranchSiteCallback);
        }

        /*if ($scope.showWarningMessageRegion) {
         UIService.unblock();
         var title = 'Create Customer Branch Site';
         var message = "Region is not configured for the selected customer and country in BFG, Please raise service request to UKGSIITSCRSUP to get it configured";
         var dialogInstance = UIService.openDialogBox(title, message, true, false);
         dialogInstance.result.then(function () {
         UIService.block();
         branchSiteService.createBranchSite(ein, cusId, cusName, $scope.addressDTO, $scope.siteDTO, $scope.createBranchSiteCallback);
         }, function () {
         UIService.unblock();
         });
         }*/

    };

    $scope.createBranchSiteCallback = function (data, status) {
        var title = 'Create Customer Branch Site';
        var message = "";

        if (status == '200') {
            /*$scope.getBranchSiteCreateUpdate(true, data);*/
            message = "Successfully Created Branch Site for " + $scope.customer.cusName + ".";
            $scope.clear();
            var dialogInstance = UIService.openDialogBox(title, message, true, false);
            dialogInstance.result.then(function () {
            }, function () {
                UIService.unblock();
            });
        } else if (status == '409') {
            //message = "Already a site exist with name : " + $scope.siteDTO.siteName + " !!  Create site with a different Site Name.";
            var dialogInstance = UIService.openDialogBox(title, data, true, false);
            dialogInstance.result.then(function () {
            }, function () {
                UIService.unblock();
            });
        }
        else {
            message = "Request failed for customer " + $scope.customer.cusName + "." + "\n" + data;
            var dialogInstance = UIService.openDialogBox(title, message, true, false);
            dialogInstance.result.then(function () {
            }, function () {
                UIService.unblock();
            });
        }
        UIService.unblock();

    };

    $scope.getBranchSiteStatus = function () {
        UIService.block();
        branchSiteService.getSiteStatus($scope.siteDTO.sitId, function (data, status) {
            var title = 'Customer Branch Site';
            var message = "";
            var btns = [
                {result:'OK', label:'OK'}
            ];

            if (status == '200') {
                if (data != undefined && data.status == 'SITE_IS_ACTIVE') {

                    var message = $scope.siteDTO.siteName + " Site is Associated to Quotes/Assets, On Updating SQE will be Notified regarding the changes. ";
                    $scope.siteDTO.notifySiteUpdate = true;
                    $scope.disablePriceFields = false;
                    $scope.associatedQuotes = data.data;
                    if (data.data != undefined && data.data.length > 0) {
                        $scope.showQuoteDialog = true;
                        $scope.siteDTO.notifySiteUpdate = true;
                    } else {
                        $scope.siteDTO.notifySiteUpdate = false;
                        $scope.showQuoteDialog = false;
                    }

                    console.log("associatedQuotes associatedQuotes: ", $scope.associatedQuotes);

                }
                else {
                    $scope.siteDTO.notifySiteUpdate = false;
                    $scope.disablePriceFields = false;
                    $scope.showQuoteDialog = false;
                }
            }

            UIService.unblock();


        });

    }
    $scope.updateBranchSite = function () {
        UIService.block();
        var salesChannel = $scope.selectedSalesChannel.name;
        var cusName = $scope.customer.cusName;
        var cusId = $scope.customer.cusId;
        if (cusId == null || cusId == undefined || cusId == "") {
            cusId = " ";
        }
        var ein = $scope.salesUser.ein;
        if (!_.isUndefined($scope.selectedBranchSite) && $scope.siteDTO.siteName == $scope.selectedBranchSite.name &&
            $scope.siteDTO.sitId == $scope.selectedBranchSite.siteId &&
            $scope.siteDTO.locSubLocationId == $scope.selectedBranchSite.locationId &&
            $scope.siteDTO.siteType == $scope.selectedBranchSite.siteType &&
            $scope.siteDTO.locSubPremise == $scope.selectedBranchSite.subPremises &&
            $scope.siteDTO.localCompanyName == $scope.selectedBranchSite.localCompanyName &&
            $scope.addressDTO.buildingNumber == $scope.selectedBranchSite.buildingNumber &&
            $scope.addressDTO.buildingName == $scope.selectedBranchSite.building &&
            $scope.addressDTO.subBuilding == $scope.selectedBranchSite.subBuilding &&
            $scope.addressDTO.street == $scope.selectedBranchSite.street &&
            $scope.addressDTO.subStreet == $scope.selectedBranchSite.subStreet &&
            $scope.addressDTO.locality == $scope.selectedBranchSite.locality &&
            $scope.addressDTO.subLocality == $scope.selectedBranchSite.subLocality &&
            $scope.addressDTO.city == $scope.selectedBranchSite.city &&
            $scope.addressDTO.state == $scope.selectedBranchSite.state &&
            $scope.addressDTO.subState == $scope.selectedBranchSite.subCountyStateProvince &&
            $scope.addressDTO.postCode == $scope.selectedBranchSite.postCode &&
            $scope.addressDTO.subPostCode == $scope.selectedBranchSite.subPostCode &&
            $scope.addressDTO.POBox == $scope.selectedBranchSite.poBoxNumber &&
            $scope.addressDTO.latitude == $scope.selectedBranchSite.latitude &&
            $scope.addressDTO.longitude == $scope.selectedBranchSite.longitude) {
            $scope.siteDTO.notifySiteUpdate = false;
        }

        if (_.isEmpty($scope.siteDTO.sitCusRegion)) {
            $scope.siteDTO.isNewRegion = false;
            dialog = $scope.getWarningDialogForBlankRegion('Update');
            dialog.result.then(function () {
                branchSiteService.updateBranchSite(ein, cusId, cusName, $scope.addressDTO, $scope.siteDTO, $scope.updateBranchSiteCallback);
            }, function () {

            });
        } else if ($scope.hasPreLoadedRegions && $scope.allowNewRegion) {
            var dialog = $scope.getWarningDialogForNewRegion('Update', cusName, $scope.siteDTO.sitCusRegion, $scope.addressDTO.country.name);
            dialog.result.then(function () {
                $scope.siteDTO.isNewRegion = true;
                branchSiteService.updateBranchSite(ein, cusId, cusName, $scope.addressDTO, $scope.siteDTO, $scope.updateBranchSiteCallback);
            }, function () {
                /* Don't update site*/
                $scope.allowNewRegion = false;
                UIService.unblock();
                return;
            });

        } else {
            $scope.siteDTO.isNewRegion = false;
            branchSiteService.updateBranchSite(ein, cusId, cusName, $scope.addressDTO, $scope.siteDTO, $scope.updateBranchSiteCallback);
        }
    }


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
    };

    $scope.updateBranchSiteCallback = function (data, status) {
        var title = 'Update Customer Branch Site';
        if (status == '200') {

            var message = "Successfully Updated Branch Site for " + $scope.customer.cusName + ".";
            if ($scope.siteDTO.notifySiteUpdate) {
                if (data == 'true') {
                    message = "Successfully Updated Branch Site for " + $scope.customer.cusName + " and SQE has been Notified Regarding the changes";
                }
                else {
                    message = "Successfully Updated Branch Site for " + $scope.customer.cusName + "  and Notifying SQE has failed ,Please update with same details";
                }
            }

            $scope.clear();
            var dialogInstance = UIService.openDialogBox(title, message, true, false);
            dialogInstance.result.then(function () {
            }, function () {
                UIService.unblock();
            });

            $scope.getBranchSite();

            $scope.branchSiteUI.enableCreateButton = false;
            $scope.branchSiteUI.enableUpdateBranchSiteButton = false;
            UIService.unblock();
        }
        else if (status == '409') {
            var message = data.description;
            var dialogInstance = UIService.openDialogBox(title, message, true, false);
            dialogInstance.result.then(function () {
            }, function () {
                UIService.unblock();
            });
            UIService.unblock();
        }
        else {
            UIService.handleException(title, data, status);

            UIService.unblock();

        }

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
            if (_.isUndefined(item.selected) || item.selected == false) {
                console.log('got de-select event');
                return;
            }
            $scope.clearLatLong = false;

            $scope.addressGridSelected = true;
            if (!_.isUndefined($scope.branchSiteGrid.$gridScope)) {
                $scope.branchSiteGrid.$gridScope.toggleSelectAll(false, false);
                $scope.branchSiteGrid.$gridScope.selectedItems.length = 0;
            }

            $scope.branchSiteFormData.localCompanyName = "";
            $scope.fillFormFromAddressGrid(item.entity);

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

    $scope.fillFormFromAddressGrid = function (selectedAddress) {

        var msg = 'Do you want to update current Branch Site details with the one you selected from Search Results?';
        var dialogInstance = UIService.openDialogBox('Update Address Details', msg, true, true);
        dialogInstance.result.then(function () {
            if (!_.isUndefined(selectedAddress)) {
                if (_.isUndefined($scope.addressDTO)) {
                    $scope.addressDTO = {};
                }

                if (_.isUndefined($scope.siteDTO)) {
                    $scope.siteDTO = {};
                }

                if (!_.isUndefined(selectedAddress.buildingNumber) && !_.isEmpty(selectedAddress.buildingNumber)) {
                    $scope.addressDTO.buildingNumber = selectedAddress.buildingNumber;
                }

                if (!_.isUndefined(selectedAddress.buildingName) && !_.isEmpty(selectedAddress.buildingName)) {
                    $scope.addressDTO.buildingName = selectedAddress.buildingName;
                }

                if (!_.isUndefined(selectedAddress.subBuilding) && !_.isEmpty(selectedAddress.subBuilding)) {
                    $scope.addressDTO.subBuilding = selectedAddress.subBuilding;
                }

                if (!_.isUndefined(selectedAddress.locality) && !_.isEmpty(selectedAddress.locality)) {
                    $scope.addressDTO.locality = selectedAddress.locality;
                }

                if (!_.isUndefined(selectedAddress.subLocality) && !_.isEmpty(selectedAddress.subLocality)) {
                    $scope.addressDTO.subLocality = selectedAddress.subLocality;
                }

                if (!_.isUndefined(selectedAddress.subZipCode) && !_.isEmpty(selectedAddress.subZipCode)) {
                    $scope.addressDTO.subPostCode = selectedAddress.subZipCode;
                }

                if (!_.isUndefined(selectedAddress.street) && !_.isEmpty(selectedAddress.street)) {
                    $scope.addressDTO.street = selectedAddress.street;
                }

                if (!_.isUndefined(selectedAddress.poBox) && !_.isEmpty(selectedAddress.poBox)) {
                    $scope.addressDTO.POBox = selectedAddress.poBox;
                }

                if (!_.isUndefined(selectedAddress.subState) && !_.isEmpty(selectedAddress.subState)) {
                    $scope.addressDTO.subState = selectedAddress.subState;
                }
                if (!_.isUndefined(selectedAddress.city) && !_.isEmpty(selectedAddress.city)) {
                    $scope.addressDTO.city = selectedAddress.city;
                }

                $scope.addressDTO.state = selectedAddress.state;
                // $scope.addressDTO.city = selectedAddress.city;
                $scope.addressDTO.postCode = selectedAddress.zipCode;
                $scope.addressDTO.longitude = selectedAddress.longitude;
                $scope.addressDTO.latitude = selectedAddress.latitude;
                $scope.addressDTO.adrAccuracyLevel = selectedAddress.accuracyLevel;
                $scope.addressDTO.adrValidationLevel = selectedAddress.failLevel;
                countryService.broadcastSelectedCountryChangedEvent(selectedAddress.country);
                $scope.getGeoCode();
            }
        }, function () {
        });

    };

    $scope.branchSiteGrid = { data:'branchSiteList', multiSelect:false, enableColumnResize:true,
        showGroupPanel:true,
        showColumnMenu:true,
        showFilter:false,
        filterOptions:{
            filterText:''
        },

        afterSelectionChange:function (item, event) {
            if (_.isUndefined(item.selected) || item.selected == false) {
                console.log('got de-select event');
                $scope.showMap = false;
                return;
            }
            $scope.clearLatLong = false;
            $scope.addressGrid.$gridScope.toggleSelectAll(false, false);
            $scope.addressGrid.$gridScope.selectedItems.length = 0;
            $scope.branchGridSelected = true;
            $scope.fillBranchSiteForm(item.entity);
            $scope.isAdrSearchSuccess = false;
            $scope.showAddLocDialog = false;
            $scope.allowNewRegion = false;
            $scope.hasPreLoadedRegions = false;
        },
        columnDefs:[
            {field:'name', displayName:'Site Name', width:"*" },
            {field:'siteId', displayName:'Site Id', width:"*" },
            {field:'siteType', displayName:'Site Type', width:"*", visible:false},
            {field:'sitQuoteOnlyFlag', displayName:'Quote Only Flag', width:"*" },
            {field:'localCompanyName', displayName:'Local Company Name', width:"*" },
            {field:'building', displayName:'Building Name', width:"*"},
            {field:'subBuilding', displayName:'Sub Building', width:"*", visible:false},
            {field:'buildingNumber', displayName:'Building Number', width:"*", visible:false},
            {field:'floor', displayName:'Floor', width:"*"},
            {field:'room', displayName:'Room', width:"*"},
            {field:'subPremises', displayName:'Sub Premises', width:"*", visible:false},
            {field:'street', displayName:'Street', width:"*", visible:false},
            {field:'subStreet', displayName:'Sub Street', width:"*", visible:false},
            {field:'locality', displayName:'Locality', width:"*", visible:false},
            {field:'subLocality', displayName:'Sub Locality', width:"*", visible:false},
            {field:'city', displayName:'City', width:"*"},
            {field:'state', displayName:'State/Province', width:"*", visible:false},
            {field:'subState', displayName:'Sub State/County/Province', width:"*", visible:false},
            {field:'country', displayName:'Country', width:"*"},
            {field:'postCode', displayName:'Zip/Post Code', width:"*"},
            {field:'subZipCode', displayName:'Sub Post Code', width:"*", visible:false},
            {field:'phoneNum', displayName:'Phone Number', width:"*", visible:false},
            {field:'postalOrganisation', displayName:'Postal Organisation', width:"*", visible:false},
            {field:'latitude', displayName:'Latitude', width:"*"},
            {field:'longitude', displayName:'Longitude', width:"*"},
            {field:'managePlaceResult', displayName:'Manage Place Result', width:"*", visible:false},
            {field:'accuracyLevel', displayName:'Accuracy Level', width:"*", visible:false},
            //Mapping is incorrect in the BFG ---
            //{field:'failLevel', displayName:'Validation Level', width:"*", visible:false},
            {field:'validationLevel', displayName:'Fail Level', width:"*", visible:false},
            {field:'componentStatus', displayName:'Component Status', width:"*", visible:false},
            {field:'countryCode', displayName:'Country Code', width:"*", visible:false},
            {field:'sitFriendlyName', displayName:'Customer Friendly Site Name', width:"*", visible:false}
        ]

    };

    $scope.fillBranchSiteForm = function (selectedBranch) {
        if (!_.isUndefined(selectedBranch)) {
            $scope.addressDTO = {};
            $scope.siteDTO = {};
            $scope.siteDTO.siteName = selectedBranch.name;
            $scope.siteDTO.sitId = selectedBranch.siteId;
            $scope.siteDTO.locSubLocationId = selectedBranch.locationId;
            $scope.siteDTO.siteType = selectedBranch.siteType;
            $scope.siteDTO.locSubPremise = selectedBranch.subPremises;
            $scope.siteDTO.localCompanyName = selectedBranch.localCompanyName;
            $scope.siteDTO.sitNetworkReportingRef = selectedBranch.sitNetworkReportingRef;
            $scope.siteDTO.sitReference = selectedBranch.siteReference;
            $scope.siteDTO.sitCusReference = selectedBranch.sitCusRef;
            $scope.siteDTO.sitCusRegion = selectedBranch.sitCusRegion;
            $scope.siteDTO.sitFriendlyName = selectedBranch.sitFriendlyName;
            $scope.siteDTO.comment = selectedBranch.comments;
            $scope.siteDTO.sitCusRegion = selectedBranch.sitCusRegion;
            $scope.addressDTO.buildingNumber = selectedBranch.buildingNumber;
            $scope.addressDTO.buildingName = selectedBranch.building;
            $scope.addressDTO.subBuilding = selectedBranch.subBuilding;
            $scope.addressDTO.street = selectedBranch.street;
            $scope.addressDTO.subStreet = selectedBranch.subStreet;
            $scope.addressDTO.locality = selectedBranch.locality;
            $scope.addressDTO.subLocality = selectedBranch.subLocality;
            $scope.addressDTO.city = selectedBranch.city;
            $scope.addressDTO.state = selectedBranch.state;
            $scope.addressDTO.subState = selectedBranch.subCountyStateProvince;
            $scope.addressDTO.postCode = selectedBranch.postCode;
            $scope.addressDTO.subPostCode = selectedBranch.subPostCode;
            $scope.addressDTO.POBox = selectedBranch.poBoxNumber;
            $scope.addressDTO.phoneNumber = selectedBranch.phoneNum;
            $scope.addressDTO.floor = selectedBranch.floor;
            $scope.addressDTO.room = selectedBranch.room;
            $scope.addressDTO.latitude = selectedBranch.latitude;
            $scope.addressDTO.longitude = selectedBranch.longitude;
            $scope.addressDTO.adrAccuracyLevel = selectedBranch.accuracyLevel;
            $scope.addressDTO.adrValidationLevel = selectedBranch.failLevel;
            countryService.broadcastSelectedCountryChangedEvent(selectedBranch.country);
            $scope.cachedBranchSite = JSON.stringify(selectedBranch);
            $scope.selectedBranchSite = selectedBranch;
            $scope.getBranchSiteStatus();
        }
    }

    $scope.foldGrid = function () {
        $scope.displayGrid = !$scope.displayGrid;

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

    $scope.getSiteRegions = function () {
        UIService.block();
        console.log('Get Site Regions');
        $scope.siteRegionList = [];
        //  var startTime = new Date().getTime();
        if (_.isEmpty($scope.addressDTO.country.codeAlpha2)) {
            UIService.unblock();
            return;
        }
        branchSiteService.getSiteRegions($scope.customer.cusId, $scope.addressDTO.country.codeAlpha2, function (data, status) {
            if (status == '200') {
                var length = 0;

                if (!_.isUndefined(data.length)) {
                    length = data.length;
                }

                $scope.siteRegionList = data;
                $scope.siteRegionList.splice(0, 0, {region:""})
                $scope.siteRegionList.splice(length + 1, 0, {region:"NOT APPLICABLE"})

                var region = _.find($scope.siteRegionList, function (obj) {
                    if (obj.region == $scope.siteDTO.sitCusRegion) {
                        return true;
                    }
                });

                if (!_.isUndefined(region)) {
                    $scope.siteDTO.sitCusRegion = region.region;
                } else {
                    $scope.siteDTO.sitCusRegion = $scope.siteRegionList[0].region;
                }

                $scope.hasPreLoadedRegions = true;
                $scope.allowNewRegion = false;
            } else if (status == '404') {
                $scope.siteRegionList = [];
                //$scope.showWarningMessageRegion = true;
                $scope.hasPreLoadedRegions = false;
                $scope.allowNewRegion = true;
            }
            UIService.unblock();
        });
    }

    /////////////////////////    Google Map /////////////////////////////////////
    $scope.getLatLongGoogleMap = function () {
        var geocoder;
        geocoder = new google.maps.Geocoder();
        var latLng = new google.maps.LatLng(51.555100, -0.264030);
        if (!(_.isUndefined($scope.addressDTO))) {
            var address = $scope.addressDTO.city + "," + $scope.addressDTO.country.name;
            geocoder.geocode({ 'address':address}, function (results, status) {
                if (status == google.maps.GeocoderStatus.OK) {
                    latLng = new google.maps.LatLng(results[0].geometry.location.lat(), results[0].geometry.location.lng());
                    return latLng;
                }
            });
        }
        return latLng;
    }

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
            var address = "London" + "," + "UNITED KINGDOM";
            if (_.isUndefined(latLng)) {
                if (!(_.isUndefined($scope.addressDTO.city)) && !(_.isUndefined($scope.addressDTO.country.name))) {
                    address = $scope.addressDTO.city + "," + $scope.addressDTO.country.name + "," + $scope.addressDTO.street + "," + $scope.addressDTO.locality;
                    //latLng=london;
                }
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


                        var markers = [];

                        var marker = new google.maps.Marker({ map:$scope.map,
                                                                animation:google.maps.Animation.DROP,
                                                                position:latLng,
                                                                title:'Click to Select and Drag',
                                                                draggable:true
                                                            });


                        markers.push(marker);
                        //Search Box
                        // Create the search box and link it to the UI element.

                        if (!_.isNull(document.getElementById('pac-input'))) {
                            $scope.input = document.getElementById('pac-input');
                        }
                        google.maps.event.trigger($scope.input, 'focus')
                        google.maps.event.trigger($scope.input, 'keydown', {keyCode:13})

                        var searchBox = new google.maps.places.SearchBox($scope.input);
                        $scope.map.controls[google.maps.ControlPosition.TOP_LEFT].push($scope.input);

                        // Bias the SearchBox results towards current map's viewport.
                        $scope.map.addListener('bounds_changed', function () {
                            searchBox.setBounds($scope.map.getBounds());
                        });


                        // Listen for the event fired when the user selects a prediction and retrieve
                        // more details for that place.
                        searchBox.addListener('places_changed', function () {
                            var places = searchBox.getPlaces();

                            if (places.length == 0) {
                                return;
                            }

                            // Clear out the old markers.
                            markers.forEach(function (marker) {
                                marker.setMap(null);
                            });
                            markers = [];

                            // For each place, get the icon, name and location.
                            var bounds = new google.maps.LatLngBounds();
                            var place = places[0];
                            // places.forEach(function(place) {
                            var icon = {
                                url:place.icon,
                                size:new google.maps.Size(71, 71),
                                origin:new google.maps.Point(0, 0),
                                anchor:new google.maps.Point(17, 34),
                                scaledSize:new google.maps.Size(25, 25)
                            };

                            // Create a marker for each place.
                            marker = new google.maps.Marker({
                                                                map:$scope.map,
                                                                animation:google.maps.Animation.DROP,
                                                                title:'Click to Select and Drag',
                                                                position:place.geometry.location,
                                                                draggable:true
                                                            });


                            markers.push(marker);
                            if (place.geometry.viewport) {
                                // Only geocodes have viewport.
                                bounds.union(place.geometry.viewport);
                            } else {
                                bounds.extend(place.geometry.location);
                            }

                            // });
                            $scope.map.fitBounds(bounds);

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
                        });
                        //Search Box-end

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

                var markers = [];

                var marker = new google.maps.Marker({map:$scope.map,
                                                        animation:google.maps.Animation.DROP,
                                                        position:latLng,
                                                        title:'Click to Select and Drag',
                                                        draggable:true
                                                    });

                markers.push(marker);
                //Search Box
                // Create the search box and link it to the UI element.
                if (!_.isNull(document.getElementById('pac-input'))) {
                    $scope.input = document.getElementById('pac-input');
                }
                google.maps.event.trigger($scope.input, 'focus')
                google.maps.event.trigger($scope.input, 'keydown', {keyCode:13})
                var searchBox = new google.maps.places.SearchBox($scope.input);
                $scope.map.controls[google.maps.ControlPosition.TOP_LEFT].push($scope.input);


                // Bias the SearchBox results towards current map's viewport.
                $scope.map.addListener('bounds_changed', function () {
                    searchBox.setBounds($scope.map.getBounds());
                });


                // Listen for the event fired when the user selects a prediction and retrieve
                // more details for that place.
                searchBox.addListener('places_changed', function () {
                    var places = searchBox.getPlaces();

                    if (places.length == 0) {
                        return;
                    }

                    // Clear out the old markers.
                    markers.forEach(function (marker) {
                        marker.setMap(null);
                    });
                    markers = [];

                    // For each place, get the icon, name and location.
                    var bounds = new google.maps.LatLngBounds();
                    var place = places[0]
                    /// places.forEach(function(place) {
                    var icon = {
                        url:place.icon,
                        size:new google.maps.Size(71, 71),
                        origin:new google.maps.Point(0, 0),
                        anchor:new google.maps.Point(17, 34),
                        scaledSize:new google.maps.Size(25, 25)
                    };

                    // Create a marker for each place.
                    marker = new google.maps.Marker({
                                                        map:$scope.map,
                                                        animation:google.maps.Animation.DROP,
                                                        title:'Click to Select and Drag',
                                                        position:place.geometry.location,
                                                        draggable:true
                                                    });


                    markers.push(marker);
                    if (place.geometry.viewport) {
                        // Only geocodes have viewport.
                        bounds.union(place.geometry.viewport);
                    } else {
                        bounds.extend(place.geometry.location);
                    }
                    //  });
                    $scope.map.fitBounds(bounds);

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
                });
                //Search Box-end
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

    $scope.collapseGrid = function () {
        $scope.displayGrid = !$scope.displayGrid;
    }


    $scope.initQuoteDisplayWindow = function () {
        $scope.title = 'Associated Quotes with Site';
        window.setTimeout(function () {
            $(window).resize();
        }, 10);
    };

    $scope.associatedQuotesGrid = { data:'associatedQuotes', multiSelect:false, keepLastSelected:false, enableRowSelection:false,
        enableColumnResize:true,
        width:'*',
        afterSelectionChange:function (item, event) {
        },
        columnDefs:[
            {field:'quoteId', displayName:'Quote ID', width:160} ,
            {field:'quoteName', displayName:'Quote Name', width:120},
            {field:'quoteLineItemStatus', displayName:'Quote Status', width:160}
        ] };

    $scope.addRegion = function () {
        $scope.allowNewRegion = !$scope.allowNewRegion;

    }

    $scope.getWarningDialogForBlankRegion = function (operation) {
        var title = operation + ' Customer Branch Site';
        var message = "Region is not configured for the selected customer and country in BFG, Please raise service request to UKGSIITSCRSUP to get it configured";
        return UIService.openDialogBox(title, message, true, false);
    }

    $scope.getWarningDialogForNewRegion = function (operation, custName, newRegion, country) {
        var title = operation + ' Customer Branch Site';
        var message = "Customer [" + custName + "] sites are currently restricted to region code. Do you want to have [" + newRegion + "] as a region code in [" + country + "] from now on?";
        return UIService.openDialogBox(title, message, true, true);
    }

}])
// Branch site controller END
