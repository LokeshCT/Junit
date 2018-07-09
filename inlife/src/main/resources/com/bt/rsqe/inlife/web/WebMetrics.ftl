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
    <link type="text/css" href="/rsqe/inlife/static/css/lineChart.css" rel="stylesheet">
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
    <script type="text/javascript" src="/rsqe/inlife/static/js/webMetricsAdapter.js"></script>

</head>
<body>

<div class="" ng-controller='WebMetricsController' style="margin-left: 30px;">
    <div id="tabs" class="tabs">
        <div class="tab"><a href="/rsqe/inlife/web-metrics/home">Web Analytics</a></div>
    <#--<div class="tab"><a href="/rsqe/inlife/dashboard">Dashboard</a></div>-->
    </div>
    <div class="webMetrics">
        <div class="userNavigationListDiv">
            <div>
                <div class="heading">
                    <lable class="heading"><b>User Actions</b></lable>
                </div>
                <ul ng-repeat="navigation in navigations">
                    <span ng-click="navigationSelected(navigation.name)">{{navigation.name}}</span>
                </ul>
            </div>
        </div>

        <div class="navigationDetailsDiv" ng-hide="!userActionSelected">
            <div>
                <div class="dateDiv">
                    <label>From date : </label>
                    <input class="date" type="text" id="fromDate">
                    <label>To date : </label>
                    <input class="date" type="text" id="toDate">
                    <input class="button" type="button" value="Go" ng-click="navigationSelected(navigationName)">
                    <input class="button" type="button" value="Export Raw Data" ng-click="exportData(navigationName)" id="exportBtn">
                </div>
                <div class="timeDistributionChartDiv">
                    <label class="headerLabel" style="margin-top: 10px;">{{navigationName}} - Percentage response time distribution by
                        country:</label>
                    <rsqe-line-chart data="webMetricsByLocation" options="lineChartOptions"></rsqe-line-chart>
                </div>
            </div>
            <div class="ragTableDiv">
                <label class="headerLabel">Response time RAG distribution by country:</label>
                <table class="ragTable">
                        <thead>
                        <tr>
                            <th>Country</th>
                            <th>Data Points</th>
                            <th><=5sec</th>
                            <th>5 to 10sec</th>
                            <th>Above 10sec</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr class="ragRow" ng-repeat="rag in ragByLocations">
                            <td class="ragLocation">{{rag.location}}</td>
                            <td class="ragDataPoints">{{rag.dataPoints}}</td>
                            <td class="ragGreen">{{rag.green}}%</td>
                            <td class="ragAmber">{{rag.amber}}%</td>
                            <td class="ragRed">{{rag.red}}%</td>
                        </tr>
                        </tbody>
                    </table>
            </div>
        </div>
    </div>
</div>

<div class="hidden" id="pageContext">${pageContext}</div>

</body>
</html>
