<!DOCTYPE html>
<html ng-app="app" ng-controller="UserManagementBaseController">
    <head>
        <title>RSQE User Management</title>
        <!-- Head -->
        <meta charset="utf-8" />
        <title page-title>RSQE User Management</title>

        <meta name="description" content="blank page" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

        <!--Basic Styles-->
        <link href="/user-management/static/assets/css/bootstrap.css" rel="stylesheet" />
        <link ng-if="settings.rtl" ng-href="/user-management/static/assets/css/bootstrap-rtl.css" rel="stylesheet" />

        <!--user-management styles-->
        <link ng-href="/user-management/static/assets/css/user-management.css" rel="stylesheet" type="text/css" />
        <link ng-href="/user-management/static/assets/css/purple.css" rel="stylesheet" type="text/css" />

        <!-- Scripts -->
        <script src="/user-management/static/lib/jquery/jquery.min.js"></script>
        <script src="/user-management/static/lib/jquery/bootstrap.js"></script>
        <script src="/user-management/static/lib/angular/angular.js"></script>
        <script src="/user-management/static/lib/utilities.js"></script>
        <script src="/user-management/static/lib/angular/angular-ui-router/angular-ui-router.js"></script>
        <script src="/user-management/static/lib/angular/angular-ocLazyLoad/ocLazyLoad.js"></script>
        <script src="/user-management/static/lib/angular/angular-ui-bootstrap/ui-bootstrap.js"></script>

        <!-- App Config and Routing Scripts -->
        <script src="/user-management/static/app/app.js"></script>
        <script src="/user-management/static/app/config.js"></script>
        <script src="/user-management/static/app/config.lazyload.js"></script>
        <script src="/user-management/static/app/config.router.js"></script>
        <script src="/user-management/static/app/services/services.js"></script>

        <!-- Layout Related Directives -->
        <script src="/user-management/static/app/directives/header.js"></script>
        <script src="/user-management/static/app/directives/navbar.js"></script>
        <script src="/user-management/static/app/directives/widget.js"></script>
        <script src="/user-management/static/app/controllers/userManagementBasePage.js"></script>
    </head>

    <body>
        <div ui-view></div>
        <div class="hidden" id="urlConfig">${urlConfig}</div>
        <div class="hidden" id="userId">${userId}</div>
        <div class="hidden" id="cqmUrl">${cqmUrl}</div>
        <div class="hidden" id="nrmUrl">${nrmUrl}</div>
        <div class="hidden" id="roleGroupConstants">${roleGroupConstants}</div>
    </body>
</html>
