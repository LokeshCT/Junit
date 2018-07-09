<!DOCTYPE html>
<html ng-app="InlifeApp">
<head>
    <title>PMD.SQE Dashboard</title>

    <meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
    <link type="text/css" href="/rsqe/inlife/static/lib/bootstrap/css/bootstrap.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/lib/bootstrap/css/ng-grid.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/css/inlife.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/css/font-awesome.min.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/css/normalize.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/css/prism.css" rel="stylesheet">
    <link type="text/css" href="/rsqe/inlife/static/css/app.css" rel="stylesheet">

    <script type="text/javascript" src="/rsqe/inlife/static/lib/underscore/underscore.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/lib/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/lib/jquery/jquery.blockUI.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/lib/angular/angular.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/lib/angular/ui/ng-grid.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/lib/tcCharts/Chart.min.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/lib/tcCharts/tc-angular-chartjs.js"></script>

    <script type="text/javascript" src="/rsqe/inlife/static/js/grid.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/js/charts.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/js/services.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/js/controllers.js"></script>
    <script type="text/javascript" src="/rsqe/inlife/static/js/routeProvider.js"></script>

</head>
<body>


<div ng-controller='QuoteStatsController'>
    <div id="header">
        <img src="/rsqe/inlife/static/img/mx_banner.jpg" class="logo"/>

        <div class="header-text">
            <div><h1 class="dashboard-msg">Dashboard</h1></div>
        </div>
    </div>

    <div class="quoteStatsDiv">
        <label style="margin-left: 40px; text-align:justify; font-size: 12px; margin-top: 10px; font-weight: bold;">Quotes (Quote Items) :</label>
        <div class="quotesSummaryByChannelDiv">
            <rsqe-grid data="quoteSummaryData" class="quotesSummaryByChannelTab" column-defs="quoteSummaryColumns" footer-template="footerTemplate"
                on-cell-select="loadQuoteItemStats" enable-cell-selection></rsqe-grid>
        </div>

        <div class="quoteItemDetailsDiv">
            <label style="margin-left: 20px; margin-top: 20px; position: absolute;">Quote Items :</label>
            <rsqe-grid class="quoteItemDetailsTab" data="quoteItemStats" class="gridStyle" column-defs="columnDefs" enable-grouping></rsqe-grid>
        </div>
    </div>

</div>

</body>
</html>
