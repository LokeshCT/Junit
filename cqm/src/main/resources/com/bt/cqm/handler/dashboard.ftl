<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>CQM User Dashboard</title>

    <script src="/cqm/static/cqm/web/lib/angular-1.3.15/angular.min.js"></script>
    <script src="/cqm/static/cqm/web/lib/angular-1.3.15/angular-route.min.js"></script>
    <script src="/cqm/static/cqm/web/lib/angular-1.3.15/angular-animate.min.js"></script>
    <script src="/cqm/static/cqm/web/lib/angular-ui/ui-grid/3.0.5/ui-grid.min.js"></script>
    <script src="/cqm/static/cqm/web/lib/jquery/jquery-1.9.1.min.js"></script>
    <script src="/cqm/static/cqm/web/lib/jquery/jquery-ui-1.9.1.custom.min.js"></script>
    <script src="/cqm/static/cqm/web/lib/jquery/jquery.blockUI.js"></script>
    <script src="/cqm/static/cqm/web/lib/underscore/underscore.js"></script>
    <script src="/cqm/static/cqm/web/lib/json/json3.min.js"></script>

    <link rel="stylesheet" href="/cqm/static/cqm/web/lib/angular-ui/ui-grid/3.0.5/ui-grid.css" media='all'>
    <link rel="stylesheet" href="/cqm/static/cqm/web/lib/bootstrap/css/bootstrap.css" media='all'>
    <link rel="stylesheet" type="text/css" href="/cqm/static/cqm/web/css/font-awesome/4.4.0/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="/cqm/static/cqm/web/lib/animate.css">

    <script src="/cqm/static/cqm/web/lib/jquery/jquery.leanModal.min.js"></script>
    <script src="/cqm/static/cqm/web/lib/bootstrap/js/ui-bootstrap-tpls.js"></script>
    <script src="/cqm/static/cqm/web/lib/bootstrap/js/bootstrap.min.js"></script>
    <script src="/cqm/static/cqm/web/lib/select2/select2.js"></script>
    <script src="/cqm/static/cqm/web/lib/angular/angular-file-upload.js"></script>
    <script src="/cqm/static/cqm/web/lib/angular/ui/select2/select2.js"></script>
    <script src="/cqm/static/cqm/web/lib/angular-ui/ng-grid.js"></script>


    <link type="text/css" href="/cqm/static/cqm/web/css/core.css" rel="stylesheet">
    <link rel="stylesheet" href="/cqm/static/cqm/web/css/dashboard.css" type='text/css' media='all'>

    <script src="/cqm/static/cqm/web/js/constants.js"></script>
    <script src="/cqm/static/cqm/web/js/directives.js"></script>
    <script src="/cqm/static/cqm/web/js/services.js"></script>
    <script src="/cqm/static/cqm/web/js/headerController.js"></script>
    <script src="/cqm/static/cqm/web/js/baseController.js"></script>
    <script src="/cqm/static/cqm/web/js/dashboard/dashboard.js"></script>

</head>

<body ng-app="cqm.dashboard" ng-controller="CQMBaseController">

    <cqm-page-header sales-user='salesUser' context='context'></cqm-page-header>
    <div ng-view class="view-animate"></div>

    <div id="urlConfig" style="display: none;">${urlConfig}</div>
</body>

</html>
