var module = angular.module('cqm.controllers');

module.controller('branchSiteMapsController', function ($scope, $rootScope, UrlConfiguration, UIService, branchSiteService, PageContext, UserContext, WebMetrics, filterFilter) {


    console.log('Initializing: branchSiteMapsController CQM');
    $scope.branchSiteMarkers;
    $scope.aPopMarkers;
    $scope.gPopMarkers;
    $scope.showProductSelectionPopUp = false;
    if (PageContext.exist()) {
        $scope.customer = PageContext.getCustomer();
        $scope.contract = PageContext.getContract();
        $scope.selectedSalesChannel = PageContext.getSalesChannel();
    }

    if (UserContext.exist()) {
        $scope.salesUser = UserContext.getUser();
    }

    $scope.branchSitesCache = [];
    $scope.aPopCache = [];
    $scope.pinImgUri = UrlConfiguration.redMarkerImgUri;


    $scope.mapOptions = {
        zoom:2,
        minZoom:2,
        center:new google.maps.LatLng(40.0000, -98.0000),
        //mapTypeId: google.maps.MapTypeId.ROADMAP,
        //mapTypeId: google.maps.MapTypeId.TERRAIN,
        //mapTypeId: google.maps.MapTypeId.SATELLITE,
        //mapTypeId:google.maps.MapTypeId.ROADMAP,
        mapTypeControlOptions:{
            style:google.maps.MapTypeControlStyle.DROPDOWN_MENU,
            position:google.maps.ControlPosition.TOP_CENTER
            //position: google.maps.ControlPosition.BOTTOM_RIGHT
        },
        zoomControl:true,
        zoomControlOptions:{
            style:google.maps.ZoomControlStyle.LARGE,
            position:google.maps.ControlPosition.LEFT_CENTER
        }
    };

    $scope.map = new google.maps.Map(document.getElementById('map'), $scope.mapOptions);
    $scope.markers = [];

    /* G-POP Click : Start*/
    var gPopAnchor = document.createElement('a');
    var gPopIdAttr = document.createAttribute('id');
    var gPopClass = document.createAttribute('class');
    gPopIdAttr.value = 'gpop-icon';
    gPopClass.value = 'fa fa-map-marker';

    var innerText = document.createTextNode('Show GPoPs');

    gPopAnchor.appendChild(innerText);
    gPopAnchor.setAttributeNode(gPopIdAttr);
    gPopAnchor.setAttributeNode(gPopClass);

    if (!_.isNull(gPopAnchor)) {
        $scope.map.controls[google.maps.ControlPosition.TOP_LEFT].push(gPopAnchor);
    }

    google.maps.event.addDomListener(gPopAnchor, 'click', function () {

        if (($('#gpop-icon').text()).indexOf('Show') > -1) {
            $scope.onClickGPOP();
        } else {
            $scope.onHideGPOP();
            $('#gpop-icon').text('Show GPoPs')
        }

    });

    $scope.$on('FILTER_GPOPS', function (event, prodFilter) {
        if (!_.isUndefined(prodFilter) && (!_.isEmpty(prodFilter.productNames) || prodFilter.includeDomesticGPOPs)) {
            UIService.block();
            branchSiteService.getGPOPs(prodFilter, function (data, status) {
                if ('200' == status && !_.isEmpty(data)) {
                    $scope.createGPOPMarkers(data);
                    $('#gpop-icon').text('Hide GPoPs');
                }
                UIService.unblock();
            });
        }
    });
    /* G-POP Click : End*/

    /* A-POP Click : Start*/
    var aPopAnchor = document.createElement('a');
    var aPopIdAttr = document.createAttribute('id');
    var aPopClass = document.createAttribute('class');
    aPopIdAttr.value = 'apop-icon';
    aPopClass.value = 'fa fa-map-marker';

    var innerText = document.createTextNode('Show APoPs');

    aPopAnchor.appendChild(innerText);
    aPopAnchor.setAttributeNode(aPopIdAttr);
    aPopAnchor.setAttributeNode(aPopClass);

    if (!_.isNull(aPopAnchor)) {
        $scope.map.controls[google.maps.ControlPosition.TOP_LEFT].push(aPopAnchor);
    }

    google.maps.event.addDomListener(aPopAnchor, 'click', function () {

        if (($('#apop-icon').text()).indexOf('Show') > -1) {
            $scope.onClickAPOP();
            $('#apop-icon').text('Hide APoPs')
        } else {
            $scope.onHideAPOP();
            $('#apop-icon').text('Show APoPs')
        }


    });
    /* A-POP Click : End*/

    /* Show Sites Click: Start*/
    var sitesAnchor = document.createElement('a');
    var sitesIdAttr = document.createAttribute('id');
    var sitesClass = document.createAttribute('class');
    sitesIdAttr.value = 'site-icon';
    sitesClass.value = 'fa fa-map-marker';

    var sitesInnerText = document.createTextNode('Hide All Sites');

    sitesAnchor.appendChild(sitesInnerText);
    sitesAnchor.setAttributeNode(sitesIdAttr);
    sitesAnchor.setAttributeNode(sitesClass);

    if (!_.isNull(sitesAnchor)) {
        $scope.map.controls[google.maps.ControlPosition.TOP_LEFT].push(sitesAnchor);
    }

    google.maps.event.addDomListener(sitesAnchor, 'click', function () {

        if (($('#site-icon').text()).indexOf('Show') > -1) {
            $scope.onClickAllSites();
            $('#site-icon').text('Hide All Sites')
        } else {
            $scope.onHideAllSites();
            $('#site-icon').text('Show All Sites')
        }


    });
    /* Cancel Click : End*/

    var infoWindow = new google.maps.InfoWindow();

    $scope.createMarker = function (type, info) {
        var marker = undefined;
        switch (type) {
            case 'APOP':
                marker = new google.maps.Marker({
                                                    map:$scope.map,
                                                    position:new google.maps.LatLng(info.latitude, info.longitude),
                                                    title:'APOP: ' + info.popName,
                                                    icon:UrlConfiguration.blueMarkerImgUri
                                                });
                marker.content = '<div class="infoWindowContent"> Platform  :' + info.platformName + '</div>';
                marker.type = 'APOP';
                break;
            case 'GPOP':
                marker = new google.maps.Marker({
                                                    map:$scope.map,
                                                    position:new google.maps.LatLng(info.latitude, info.longitude),
                                                    title:'GPOP: ' + info.popName,
                                                    icon:UrlConfiguration.greenMarkerImgUri
                                                });
                marker.content = '<div class="infoWindowContent"> Platform  :' + info.platformName + '</div>';
                marker.type = 'GPOP';
                break;

            case 'SITES':
                marker = new google.maps.Marker({
                                                    map:$scope.map,
                                                    position:new google.maps.LatLng(info.lat, info.long),
                                                    title:'Site: ' + info.Site,
                                                    icon:$scope.pinImgUri
                                                });
                marker.content = '<div class="infoWindowContent"> City  :' + info.City + '</div>';
                marker.type = 'SITES';
                break;
        }

        google.maps.event.addListener(marker, 'mouseover', function () {
            infoWindow.setContent('<h2>' + marker.title + '</h2>' + marker.content);
            infoWindow.open($scope.map, marker);
        });

        google.maps.event.addListener(marker, 'mouseout', function () {
            infoWindow.close();
        });

        google.maps.event.addListener(marker, 'click', function () {
            $scope.map.setZoom(8);
            $scope.map.setCenter(marker.getPosition());
        });

        $scope.markers.push(marker);

    }

    $scope.onClickGPOP = function () {
        $scope.showProductSelectionPopUp = true;

        if (!$rootScope.$$phase) {
            $rootScope.$digest();
        }
    }

    $scope.onHideGPOP = function () {
        var gPopMarkers = function () {
            return filterFilter($scope.markers, { type:'GPOP' });
        }();

        _.forEach(gPopMarkers, function (marker) {
            marker.setVisible(false);
        });
    }

    $scope.onClickAPOP = function () {
        if (_.isEmpty($scope.aPopCache)) {
            UIService.block();
            branchSiteService.getAPOPs(function (data, status) {
                if ('200' == status) {
                    $scope.aPopCache = data;
                    $scope.createAPOPMarkers();
                } else {
                    $scope.aPopCache = [];
                }
                UIService.unblock();
            })
        } else {
            $scope.createAPOPMarkers();
        }
    }

    $scope.onHideAPOP = function () {
        var gPopMarkers = function () {
            return filterFilter($scope.markers, { type:'APOP' });
        }();

        _.forEach(gPopMarkers, function (marker) {
            marker.setVisible(false);
        });
    }

    $scope.onClickAllSites = function () {
        $scope.createSitesMarkers();
    }

    $scope.onHideAllSites = function () {
        var gPopMarkers = function () {
            return filterFilter($scope.markers, { type:'SITES' });
        }();

        _.forEach(gPopMarkers, function (marker) {
            marker.setVisible(false);
        });
    }

    $scope.openInfoWindow = function (e, selectedMarker) {
        e.preventDefault();
        google.maps.event.trigger(selectedMarker, 'click');
    }


    $scope.getLatLong = function () {
        console.log('inside the googleMapSiteController, loadLatLong method');
        if (_.isEmpty($scope.branchSitesCache)) {
            UIService.block();
            branchSiteService.getBranchSite($scope.salesUser.ein, $scope.selectedSalesChannel.name, $scope.customer.cusId, function (data, status) {
                if (status == '200') {

                    //alert('siteList--->'+data);
                    for (var i = 0; i < data.length; i++) {
                        $scope.branchSitesCache.push({lat:data[i].latitude, long:data[i].longitude, Site:data[i].name, City:data[i].city});
                    }
                    $scope.createSitesMarkers();
                    WebMetrics.captureWebMetrics(WebMetrics.UserActions.BranchSiteMaps);
                }
                else {
                    // This should never happen.
                    var title = "Branch Site"
                    var message = "No Branch Site found for user: " + $scope.customer.cusName + "\n";
                    UIService.openDialogBox(title, message, true, false);

                }
                UIService.unblock();
            });
        } else {
            $scope.createSitesMarkers();
        }
    };

    $scope.createSitesMarkers = function () {

        for (var i = 0; i < $scope.branchSitesCache.length; i++) {
            $scope.createMarker('SITES', $scope.branchSitesCache[i]);
        }

        if (!_.isUndefined($scope.branchSitesCache) && $scope.branchSitesCache.length > 0) {
            $scope.map.setCenter(new google.maps.LatLng($scope.branchSitesCache[0].lat, $scope.branchSitesCache[0].long));
        }
    };

    $scope.createAPOPMarkers = function () {

        for (var i = 0; i < $scope.aPopCache.length; i++) {
            $scope.createMarker('APOP', $scope.aPopCache[i]);
        }

        if (!_.isUndefined($scope.aPopCache) && $scope.aPopCache.length > 0) {
            $scope.map.setCenter(new google.maps.LatLng($scope.aPopCache[0].latitude, $scope.aPopCache[0].longitude));
        }
    };

    $scope.createGPOPMarkers = function (data) {

        for (var i = 0; i < data.length; i++) {
            $scope.createMarker('GPOP', data[i]);
        }

        if (!_.isUndefined(data) && data.length > 0) {
            $scope.map.setCenter(new google.maps.LatLng(data[0].latitude, data[0].longitude));
        }
    };

});
