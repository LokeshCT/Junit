/*
 'use strict';


 // Declare app level module which depends on filters, and services
 angular.module('cqm', ['cqm.filters', 'cqm.services', 'cqm.directives', 'cqm.controllers']).
 config(['$routeProvider', function ($routeProvider) {

 $routeProvider.when('/createCustomer', {templateUrl: '/cqm/static/cqm/web/partials/grid/createCustomer.html', controller: 'CreateCustomerController',tabId: 'customer'});
 $routeProvider.when('/centralSite', {templateUrl: '/cqm/static/cqm/web/partials/grid/centralSite.html', controller: 'centralSiteController',tabId: 'customer'});
 $routeProvider.when('/centralSiteContact', {templateUrl: '/cqm/static/cqm/web/partials/grid/centralSiteContact.html', controller: 'centralSiteContactController',tabId: 'customer'});

 $routeProvider.when('/createQuote', {templateUrl: '/cqm/static/cqm/web/partials/grid/createQuote.html', controller: 'quotesController',tabId: 'quotes'});
 $routeProvider.when('/searchQuotes', {templateUrl: '/cqm/static/cqm/web/partials/grid/searchQuotes.html', controller: 'quotesController',tabId: 'quotes'});

 // Route Providers for Channel Hierarchy Tab.
 $routeProvider.when('/createChannelPartner', {templateUrl: '/cqm/static/cqm/web/partials/grid/createChannelPartner.html', controller: 'indirectCustomerController',tabId: 'indirectCustomerID'});
 $routeProvider.when('/priceBookDetails', {templateUrl: '/cqm/static/cqm/web/partials/grid/priceBookDetails.html', controller: 'indirectCustomerController',tabId: 'indirectCustomerID'});
 $routeProvider.when('/channelMonthlyCommittment', {templateUrl: '/cqm/static/cqm/web/partials/grid/ChannelMonthlyCommittment.html', controller: 'indirectCustomerController',tabId: 'indirectCustomerID'});


 // Route Providers for Activities Tab.
 $routeProvider.when('/createActivity', {templateUrl: '/cqm/static/cqm/web/partials/grid/createActivity.html', controller: 'activityController',tabId: 'activitiesTabID'});
 $routeProvider.when('/viewUpdateActivity', {templateUrl: '/cqm/static/cqm/web/partials/grid/viewUpdateActivity.html', controller: 'activityController',tabId: 'activitiesTabID'});



 // Route Providers for Reports Tab.      // Added by Krishna K V for the Report Tab
 $routeProvider.when('/vpn', {templateUrl: '/cqm/static/cqm/web/partials/grid/vpn.html', controller: 'vpnController',tabId: 'vpnTabID'});
 // Added by Krishna K V for the Report Tab

 // Added by Krishna K V for the Site Tab
 $routeProvider.when('/branchSite', {templateUrl: '/cqm/static/cqm/web/partials/grid/branchSite.html', controller: 'customerBranchSiteController',tabId: 'branchSiteTabId'})
 $routeProvider.when('/branchSiteContact', {templateUrl: '/cqm/static/cqm/web/partials/grid/branchSiteContact.html', controller: 'customerBranchSiteController',tabId: 'branchSiteTabId'})
 // Added by Krishna K V for the Site Tab

 $routeProvider.when('/logout', {controller: 'logoutController'});

 }]);
 */
