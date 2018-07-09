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

<div class="" ng-controller='WebMetricsPercentileController' style="margin-left: 30px;">
    <div id="tabs" class="tabs">
        <div class="tab"><a href="/rsqe/inlife/web-metrics/home">Web Analytics</a></div>
        <div class="tab"><a href="/rsqe/inlife/web-metrics/configure">Configure</a></div>
    </div>
    <div class="percentileDiv">
                <div>
                    Country :
                    <select ng-model="selectedLocation">
                        <option value="">-- All --</option>
                        <option ng-repeat="location in locations" value="{{location.name}}">{{location.name}}</option>
                    </select>

                    Navigation :
                    <select ng-model="selectedNavigation">
                        <option value="">-- All --</option>
                        <option ng-repeat="navigation in navigations" value="{{navigation.name}}">{{navigation.name}}</option>
                    </select>

                    From date : <input class="date" type="text" id="fromDate" placeholder="{{fromDate}}" size="7" style="width:70px;">
                    To date : <input class="date" type="text" id="toDate" placeholder="{{toDate}}" size="7" style="width:70px;">
                    <input class="button" type="button" value="Go" ng-click="actionSelected()">

                </div>
                <div class="timeDistributionChartDiv">
                    <label class="headerLabel" style="margin-top: 10px;">Percentile of location :<b> {{selectedLocation}} </b>for action :<b> {{selectedNavigation}}</b></label>
                    <rsqe-bar-chart data="percentileByCountry" options="barChartOptions"></rsqe-bar-chart>

                </div>

                <div class="percentageTableDiv">
                    <table class="percentageTable">
                        <thead>
                        <tr class="ragRow">
                            <th ng-repeat="percentageLabel in percentageLabels">{{percentageLabel}}</th>
                        </tr>
                        </thead>
                        <tbody>
                            <tr class="ragRow" ng-repeat="record in percentageByCountry">
                                <td class="ragDataPoints">{{record.navigationName}}</td>
                                <td class="ragDataPoints">{{record.target}} </td>
                                <td class="ragDataPoints" ng-repeat="percentage in record.percentages">{{percentage}}</td>

                            </tr>
                            </tbody>
                        </table>
                </div>
    </div>
</div>

<div class="hidden" id="pageContext">${pageContext}</div>

</body>
</html>
