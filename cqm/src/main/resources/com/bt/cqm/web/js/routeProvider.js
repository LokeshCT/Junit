'use strict';


// Declare app level module which depends on filters, and services
angular.module('cqm', ['cqm.filters', 'cqm.services', 'cqm.directives', 'cqm.controllers', 'ngRoute', 'cqm.dashboard']).
    config(['$routeProvider','$httpProvider',function ($routeProvider,$httpProvider) {
    $httpProvider.interceptors.push('cqmInterceptor');

    $routeProvider.when('/createCustomer', {templateUrl: '/cqm/static/cqm/web/partials/grid/createCustomer.html', controller: 'CreateCustomerController',tabId: 'Customer', description:'CQM - Customer Tab - Load Create Customer'});
    $routeProvider.when('/centralSite', {templateUrl: '/cqm/static/cqm/web/partials/grid/centralSite.html', controller: 'centralSiteController',tabId: 'Customer', description:'CQM Customer Tab -  Load Central Site'});
    $routeProvider.when('/customerContract', {templateUrl: '/cqm/static/cqm/web/partials/grid/updateContract.html', controller: 'ContractController',tabId: 'Customer', description:'CQM Customer Tab -  Contract Edit Screen'});
    $routeProvider.when('/centralSiteContact', {templateUrl: '/cqm/static/cqm/web/partials/grid/centralSiteContact.html', controller: 'centralSiteContactController',tabId: 'Customer', description:'CQM Customer Tab - Load Central Site Contacts'});

    $routeProvider.when('/createQuote', {templateUrl: '/cqm/static/cqm/web/partials/grid/createQuote.html', controller: 'quotesController',tabId: 'Quote', description:'CQM Quotes Tab - Create Quote'});
    $routeProvider.when('/searchQuotes', {templateUrl: '/cqm/static/cqm/web/partials/grid/searchQuotes.html', controller: 'quotesController',tabId: 'Quote', description:'CQM Quotes Tab - Load Quotes'});

    // Route Providers for Channel Hierarchy Tab.
    $routeProvider.when('/createChannelPartner', {templateUrl: '/cqm/static/cqm/web/partials/grid/createChannelPartner.html', controller: 'indirectCustomerController',tabId: 'Customer', description:'CQM - Customer Tab - Create Channel Partner'});
    $routeProvider.when('/priceBookDetails', {templateUrl: '/cqm/static/cqm/web/partials/grid/priceBookDetails.html', controller: 'indirectCustomerController',tabId: 'Customer', description:'CQM - Customer Tab - Load Price book'});
    $routeProvider.when('/channelMonthlyCommittment', {templateUrl: '/cqm/static/cqm/web/partials/grid/ChannelMonthlyCommittment.html', controller: 'indirectCustomerController',tabId: 'Customer', description:'CQM - Customer Tab - Channel Monthly Commitment'});

    // Route Providers for Activities Tab.
    $routeProvider.when('/createActivity', {templateUrl: '/cqm/static/cqm/web/partials/grid/createActivity.html', controller: 'activityController',tabId: 'Activity', description:'CQM Activity Tab - Create Activities'});
    $routeProvider.when('/viewUpdateActivity', {templateUrl: '/cqm/static/cqm/web/partials/grid/viewUpdateActivity.html', controller: 'activityController',tabId: 'Activity', description:'CQM Activity Tab - Load Activities'});

    // Route providers for Billing Account Tab
    $routeProvider.when('/billingAccountDetails', {templateUrl: '/cqm/static/cqm/web/partials/grid/billingAccountDetail.html', controller: 'billingAccountController',tabId: 'Customer', description:'CQM Customer Tab - Load Billing Accounts'});

    // Route providers for Billing Account Tab
    $routeProvider.when('/legalEntity', {templateUrl: '/cqm/static/cqm/web/partials/grid/legalEntity.html', controller: 'LegalEntityController',tabId: 'Customer', description:'CQM Customer Tab - Load Legal Entities'});

    // Route providers for Billing Account Tab
    $routeProvider.when('/advanceBillingAccountDetails', {templateUrl: '/cqm/static/cqm/web/partials/grid/mapLegalEntity.html', controller: 'billingAccountController',tabId: 'Customer', description:'CQM Customer Tab - Map Legal Entities'});

    //Route Providers for User Configuration Tab.
    $routeProvider.when('/userConfiguration', {templateUrl: '/cqm/static/cqm/web/partials/grid/userManagement/userConfiguration.html', controller: 'userManagementController',tabId: 'userManagementTabID', description:'CQM - User Tab - Load User Configuration'});
    $routeProvider.when('/userSubGroupMgmt', {templateUrl: '/cqm/static/cqm/web/partials/grid/userManagement/userSubGroup.html', controller: 'userManagementController',tabId: 'userSubGroupTabID', description:'CQM - User Tab - Sub Group Management'});
    $routeProvider.when('/createSubGroup', {templateUrl: '/cqm/static/cqm/web/partials/grid/userManagement/createSubGroup.html', controller: 'userManagementController',tabId: 'createSubGroupTabID', description:'CQM - User Tab - Create Sub Group'});

    // Route Providers for Order Details Tab.
    $routeProvider.when('/searchOrders', {templateUrl: '/cqm/static/cqm/web/partials/grid/searchOrders.html', controller: 'orderDetailsController',tabId: 'Order', description:'CQM Orders Tab - Load Order'});

    // Route Providers for Order Details Tab.
    $routeProvider.when('/vpn', {templateUrl: '/cqm/static/cqm/web/partials/grid/vpn.html', controller: 'vpnController',tabId: 'Customer', description:'CQM - Customer Tab - Load Vpn'});

    // Route Providers for branchSite Tab.      // Added by Krishna K V for the branchSite Tab
    $routeProvider.when('/branchSite', {templateUrl: '/cqm/static/cqm/web/partials/grid/branchSite.html', controller: 'customerBranchSiteController',tabId: 'Site', description:'CQM Manage Sites Tab - Load Branch Sites'});
    $routeProvider.when('/branchSiteMaps', {templateUrl: '/cqm/static/cqm/web/partials/grid/branchSiteMaps.html', controller: 'branchSiteMapsController',tabId: 'Site', description:'CQM Manage Sites Tab - Load Google Maps'});
    $routeProvider.when('/branchSiteContact', {templateUrl: '/cqm/static/cqm/web/partials/grid/branchSiteContact.html', controller: 'customerBranchSiteController',tabId: 'Site', description:'CQM Manage Sites Tab - Load Branch Site Contacts'});
    $routeProvider.when('/branchSiteCreate', {templateUrl: '/cqm/static/cqm/web/partials/grid/createBranchSite.html', controller: 'customerBranchSiteController',tabId: 'Site', description:'CQM Manage Sites Tab - Create Branch Site'});
    $routeProvider.when('/branchSiteAssociatedQuote', {templateUrl: '/cqm/static/cqm/web/partials/grid/BranchSiteAssociatedQuote.html', controller: 'customerBranchSiteController',tabId: 'Site', description:'CQM Manage Sites Tab - Display Associated Quotes'});

    // Added by Krishna K V for the branchSite Tab

    // Route Providers for Audit Trail Tab.
    $routeProvider.when('/viewQuoteAudit', {templateUrl: '/cqm/static/cqm/web/partials/grid/viewQuoteAudit.html', controller: 'auditTrailController',tabId: 'audit', description:'CQM Audit Trail Tab - View Quote Audit'});
    $routeProvider.when('/viewOrderAudit', {templateUrl: '/cqm/static/cqm/web/partials/grid/viewOrderAudit.html', controller: 'auditTrailController',tabId: 'audit', description:'CQM Audit Trail Tab - View Order Audit'});
    $routeProvider.when('/reports', {templateUrl: '/cqm/static/cqm/web/partials/grid/reports.html', controller: 'auditTrailController',tabId: 'audit', description:'CQM Audit Trail Tab - QRef Reports'});

    // DSL Checker
    $routeProvider.when('/dslUpload', {templateUrl: '/cqm/static/cqm/web/partials/tabs/dslchecker/UploadXls.html', controller: 'DslUploadXlsController',tabId: 'dslUpload', description:'DSL Checker Tab - Upload Doc'});
    $routeProvider.when('/allAvailableDslDocs', {templateUrl: '/cqm/static/cqm/web/partials/tabs/dslchecker/ViewAllReports.html', controller: 'DslViewAllReportController',tabId: 'allDslDocs', description:'DSL Checker Tab - All Available Docs'});

    // Route Providers for Logout.
    $routeProvider.when('/logout', {templateUrl: '/cqm/static/cqm/web/partials/grid/logout.html', controller: 'logoutController',tabId: 'logoutTabID', description:'CQM - Logout'});

}]);
