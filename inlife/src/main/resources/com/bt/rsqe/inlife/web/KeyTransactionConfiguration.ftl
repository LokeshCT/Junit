<!DOCTYPE html>
<html ng-app="InlifeApp">
<head>
    <title>${view.title}</title>

    <meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
    <link type="text/css" href="/rsqe/inlife/static/lib/bootstrap/css/bootstrap.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/lib/bootstrap/css/ng-grid.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/css/inlife.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/css/font-awesome.min.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/css/normalize.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/css/prism.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/css/app.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/css/barChart.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/css/webMetrics.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/css/jquery-ui-1.8.17.custom.css" rel="stylesheet">

    <script type="text/javascript" src="/rsqe/inlife/static/lib/underscore/underscore.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/lib/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/lib/jquery/jquery.blockUI.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/lib/jquery/jquery-ui-1.9.1.custom.min.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/lib/angular/angular.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/lib/angular/ui/ng-grid.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/lib/tcCharts/Chart.min.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/lib/tcCharts/tc-angular-chartjs.js"></script>

    <script type="text/javascript" src="/rsqe/inlife/static/js/grid.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/js/charts.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/js/services.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/js/controllers.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/js/routeProvider.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/js/webMetricsPercentileAdapter.js"></script>

</head>
<body>

<div class="" ng-controller='KeyTransactionController' style="margin-left: 30px;">
    <div id="tabs" class="tabs">
        <div class="tab"><a href="/rsqe/inlife/web-metrics/home">Web Analytics</a></div>
        <div class="tab"><a href="/rsqe/inlife/web-metrics/percentile">Percentile</a></div>
    </div>

    <div class="webMetrics">
        <div>

            <label style="margin-top: 10px;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>
            <table width="30%">
                <tr>
                    <th>
                        <lable class="headerLabel"><b>Update Key Transaction Targets</b></lable>
                    </th>
                </tr>
                <tr ng-repeat="record in transactionTargets">
                    <td><label style="margin-top: 10px;">{{record.transactionName}} : </label></td>
                    <td><input type="text" ng-model="record.target" style="width:50px;"></td>
                </tr>
                <tr>
                    <th><input class="button" type="button" value="Save" ng-click="saveTargets()"/></th>
                </tr>
            </table>


        </div>


    </div>
</div>

<div class="hidden" id="pageContext">${pageContext}</div>

</body>
</html>
