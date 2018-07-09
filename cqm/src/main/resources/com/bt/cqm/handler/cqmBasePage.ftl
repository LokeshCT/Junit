<!DOCTYPE html>
<html ng-app="cqm">
<head>
    <title>Customer Quote Management</title>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge">
    <style type="text/css">@charset "UTF-8";
    [ng\:cloak], [ng-cloak], [data-ng-cloak], [x-ng-cloak], .ng-cloak, .x-ng-cloak {
        display: none;
    }

    ng\:form {
        display: block;
    }</style>
    <link rel="stylesheet" type="text/css" href="/cqm/static/cqm/web/css/jquery-ui-1.8.17.custom.css">
    <link type="text/css" href="/cqm/static/cqm/web/lib/select2/select2.css" rel="stylesheet">
    <link type="text/css" href="/cqm/static/cqm/web/lib/bootstrap/css/bootstrap.css" rel="stylesheet">
    <link type="text/css" href="/cqm/static/cqm/web/css/font-awesome/4.4.0/css/font-awesome.css" rel="stylesheet">
   <#-- <link type="text/css" href="http://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.3.0/css/font-awesome.css" rel="stylesheet">-->
    <link type="text/css" href="/cqm/static/cqm/web/css/ng-grid.css" rel="stylesheet">
    <link type="text/css" href="/cqm/static/cqm/web/lib/angular-ui/ui-grid/3.0.5/ui-grid.css" rel="stylesheet">
    <link type="text/css" href="/cqm/static/cqm/web/css/core.css" rel="stylesheet">
    <link rel="stylesheet" href="/cqm/static/cqm/web/css/dashboard.css" type='text/css' media='all'>
    <link rel="stylesheet" type="text/css" href="/cqm/static/cqm/web/lib/animate.css">


    <!--[if IE]>
    <link type="text/css" href="/script/css/ie.css" rel="stylesheet">
    <![endif]-->

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="/script/lib/htmlshiv/html5shiv.js"></script>
    <![endif]-->

    <script src="/cqm/static/cqm/web/lib/jquery/jquery-1.9.1.min.js"></script>
    <script src="/cqm/static/cqm/web/lib/jquery/jquery.blockUI.js"></script>
    <script src="/cqm/static/cqm/web/lib/jquery/jquery-ui-1.9.1.custom.min.js"></script>
    <script src="/cqm/static/cqm/web/lib/select2/select2.js"></script>
    <script src="/cqm/static/cqm/web/lib/angular-1.3.15/angular.js"></script>
    <script src="/cqm/static/cqm/web/lib/angular-1.3.15/angular-route.js"></script>
    <script src="/cqm/static/cqm/web/lib/angular-1.3.15/angular-animate.min.js"></script>
    <script src="/cqm/static/cqm/web/lib/angular/angular-file-upload.js"></script>
    <script src="/cqm/static/cqm/web/lib/angular/ui/select2/select2.js"></script>
    <script src="/cqm/static/cqm/web/lib/angular-ui/ng-grid.js"></script>
    <script src="/cqm/static/cqm/web/lib/angular-ui/ui-grid/3.0.5/ui-grid.js"></script>
    <script src="/cqm/static/cqm/web/lib/underscore/underscore.js"></script>
    <script src="/cqm/static/cqm/web/lib/json/json3.js"></script>
    <script src="/cqm/static/cqm/web/js/routeProvider.js"></script>
    <script src="/cqm/static/cqm/web/js/directives.js"></script>
    <script src="/cqm/static/cqm/web/js/services.js"></script>
    <script src="/cqm/static/cqm/web/js/headerController.js"></script>
    <script src="/cqm/static/cqm/web/js/baseController.js"></script>
    <script src="/cqm/static/cqm/web/js/tabController.js"></script>
    <script src="/cqm/static/cqm/web/js/actionController.js"></script>
    <script src="/cqm/static/cqm/web/js/customerSiteController.js"></script>
    <script src="/cqm/static/cqm/web/js/customerContactController.js"></script>
    <script src="/cqm/static/cqm/web/js/createCustomerController.js"></script>
    <script src="/cqm/static/cqm/web/js/matchingCustomerController.js"></script>
    <script src="/cqm/static/cqm/web/js/quoteController.js"></script>
    <script src="/cqm/static/cqm/web/js/quotePriceBookController.js"></script>
    <script src="/cqm/static/cqm/web/js/channelHierarchyController.js"></script>
    <script src="/cqm/static/cqm/web/js/vpnController.js"></script>
    <script src="/cqm/static/cqm/web/js/activityController.js"></script>
    <script src="/cqm/static/cqm/web/js/orderDetailsController.js"></script>
    <script src="/cqm/static/cqm/web/js/userManagementController.js"></script>
    <script src="/cqm/static/cqm/web/js/billingAccountController.js"></script>
    <script src="/cqm/static/cqm/web/js/treeNodeController.js"></script>
    <script src="/cqm/static/cqm/web/js/customerQuoteConfigController.js"></script>
    <script src="/cqm/static/cqm/web/js/dslCheckerController.js"></script>
    <script src="/cqm/static/cqm/web/js/customerController.js"></script>
    <script src="/cqm/static/cqm/web/js/customerBranchSiteContactController.js"></script>
    <script src="/cqm/static/cqm/web/js/branchSiteMapsController.js"></script>
    <script src="/cqm/static/cqm/web/js/customerBranchSiteController.js"></script>
    <script src="/cqm/static/cqm/web/js/LegalEntityController.js"></script>
    <script src="/cqm/static/cqm/web/js/messageDialogController.js"></script>
   <#-- <script src="/cqm/static/cqm/web/js/addressController.js"></script>-->
    <script src="/cqm/static/cqm/web/js/gPopProductSelectionController.js"></script>
    <script src="/cqm/static/cqm/web/js/cqmInterceptor.js"></script>
    <script src="/cqm/static/cqm/web/js/auditTrailController.js"></script>
    <script src="/cqm/static/cqm/web/js/filters.js"></script>
    <script src="/cqm/static/cqm/web/js/constants.js"></script>
    <script src="/cqm/static/cqm/web/js/multipleCentralSiteController.js"></script>
    <script src="/cqm/static/cqm/web/js/associateSaleschannelController.js"></script>
    <script src="/cqm/static/cqm/web/js/dslUploadXlsController.js"></script>
    <script src="/cqm/static/cqm/web/js/dslViewAllReportController.js"></script>
    <script src="/cqm/static/cqm/web/js/contractController.js"></script>
    <script src="/cqm/static/cqm/web/js/locationController.js"></script>
    <script src="/cqm/static/cqm/web/js/dashboard/dashboard.js"></script>
    <script src="/cqm/static/rsqe/web/staticresources/scripts/WebMetrics.js"></script>
    <script src="/cqm/static/cqm/web/lib/jquery/jquery.leanModal.min.js"></script>
    <script src="/cqm/static/cqm/web/lib/bootstrap/js/ui-bootstrap-tpls.js"></script>
    <script src="/cqm/static/cqm/web/lib/bootstrap/js/bootstrap.min.js"></script>
    <script type="text/javascript" charset="utf-8" src="https://maps.googleapis.com/maps/api/js?v=3.exp&libraries=places&sensor=false"></script>
</head>
<body>


<div ng-controller="CQMBaseController">

    <cqm-page-header sales-user='salesUser' context='context'></cqm-page-header>

    <div ng-switch on="context.state">
        <div ng-switch-when="CustomerSelection">
            <cqm-select-customer sales-user='salesUser' context='context'></cqm-select-customer>
        </div>
        <div ng-switch-when="CustomerConfiguration">
            <cqm-customer-quote-config tabId="context.subState" context='context'/>
        </div>
        <div ng-switch-when="DslChecker">
            <dsl-checker-config tabId="context.subState" context='context'/>
        </div>
        <div ng-switch-when="UserDashboard">
            <cqm-user-dashboard sales-user='salesUser' context='context'></cqm-user-dashboard>
        </div>
        <div ng-switch-when="Logout">
            <cqm-logout/>
        </div>
    </div>

    <cqm-message-dialog></cqm-message-dialog>

</div>

<div class="hidden" id="urlConfig">${urlConfig}</div>
<div class="hidden" id="customerCreationResponse">${customerCreationResponse}</div>

</body>
</html>
