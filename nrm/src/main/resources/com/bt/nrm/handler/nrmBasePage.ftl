<!DOCTYPE html>
<html ng-app="app" ng-controller="NRMBaseController">
    <head>
        <title>Non-standard Request Management</title>
        <!-- Head -->
        <meta charset="utf-8" />
        <title page-title></title>

        <meta name="description" content="blank page" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <link rel="shortcut icon" href="/nrm/static/assets/img/favicon.jpeg" type="image/x-icon">

        <!--Basic Styles-->
        <link href="/nrm/static/assets/css/bootstrap.css" rel="stylesheet" />
        <link ng-if="settings.rtl" ng-href="/nrm/static/assets/css/bootstrap-rtl.css" rel="stylesheet" />
        <link href="/nrm/static/assets/css/font-awesome.min.css" rel="stylesheet" />
        <link href="/nrm/static/assets/css/weather-icons.min.css" rel="stylesheet" />

        <!--Fonts-->
        <link href="http://fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300"
              rel="stylesheet" type="text/css">

        <!--NRM styles-->
        <link ng-if="!settings.rtl" href="/nrm/static/assets/css/nrm.css" rel="stylesheet" />
        <link href="/nrm/static/assets/css/nrm-extra.css" rel="stylesheet" />
        <link href="/nrm/static/assets/css/typicons.min.css" rel="stylesheet" />
        <link href="/nrm/static/assets/css/animate.css" rel="stylesheet" />
        <link ng-href="/nrm/static/assets/css/purple.css" rel="stylesheet" type="text/css" />

        <!-- Scripts -->
        <script src="/nrm/static/lib/jquery/jquery.min.js"></script>
        <script src="/nrm/static/lib/jquery/bootstrap.js"></script>
        <script src="/nrm/static/lib/angular/angular.js"></script>
        <script src="/nrm/static/lib/utilities.js"></script>
        <script src="/nrm/static/lib/angular/angular-animate/angular-animate.js"></script>
        <script src="/nrm/static/lib/angular/angular-cookies/angular-cookies.js"></script>
        <script src="/nrm/static/lib/angular/angular-resource/angular-resource.js"></script>
        <script src="/nrm/static/lib/angular/angular-sanitize/angular-sanitize.js"></script>
        <script src="/nrm/static/lib/angular/angular-touch/angular-touch.js"></script>
        <script src="/nrm/static/lib/angular/angular-ui-router/angular-ui-router.js"></script>
        <script src="/nrm/static/lib/angular/angular-ocLazyLoad/ocLazyLoad.js"></script>
        <script src="/nrm/static/lib/angular/angular-ngStorage/ngStorage.js"></script>
        <script src="/nrm/static/lib/angular/angular-ui-utils/angular-ui-utils.js"></script>
        <script src="/nrm/static/lib/angular/angular-breadcrumb/angular-breadcrumb.js"></script>
        <script src="/nrm/static/lib/angular/angular-ui-bootstrap/ui-bootstrap.js"></script>
        <script src="/nrm/static/lib/jquery/slimscroll/jquery.slimscroll.js"></script>
        <script src="/nrm/static/lib/underscore-min.js"></script>
        <script src="/nrm/static/lib/json3.min.js"></script>

        <!-- App Config and Routing Scripts -->
        <script src="/nrm/static/app/app.js"></script>
        <script src="/nrm/static/app/config.js"></script>
        <script src="/nrm/static/app/config.lazyload.js"></script>
        <script src="/nrm/static/app/config.router.js"></script>
        <script src="/nrm/static/app/services/services.js"></script>
        <script src="/nrm/static/app/constants.js"></script>

        <!-- Layout Related Directives -->
        <script src="/nrm/static/app/directives/loading.js"></script>
        <script src="/nrm/static/app/directives/sidebar.js"></script>
        <script src="/nrm/static/app/directives/header.js"></script>
        <script src="/nrm/static/app/directives/navbar.js"></script>
        <script src="/nrm/static/app/directives/widget.js"></script>
        <script src="/nrm/static/app/controllers/basePage.js"></script>

    </head>

    <body>
    <div ui-view></div>
    <div class="hidden" id="urlConfig">${urlConfig}</div>
    <div class="hidden" id="requestStateConstants">${requestStateConstants}</div>
    <div class="hidden" id="requestEvaluatorStateConstants">${requestEvaluatorStateConstants}</div>
    <div class="hidden" id="requestEvaluatorResponseConstants">${requestEvaluatorResponseConstants}</div>
    <div class="hidden" id="requestResponseType">${requestResponseType}</div>
    <div class="hidden" id="nrmUserRoles">${nrmUserRoles}</div>
    </body>
</html>
