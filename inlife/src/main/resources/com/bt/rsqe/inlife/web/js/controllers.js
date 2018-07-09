'use strict';

var controllers = angular.module('rsqe.inlife.controllers', ['rsqe.inlife.services']);

controllers.controller('QuoteStatsController', ['$scope', 'httpService', 'QuoteStatsSummaryAdapter', 'QuoteItemStatsAdapter', function ($scope, httpService, QuoteStatsSummaryAdapter, QuoteItemStatsAdapter) {

    httpService.httpGet("/rsqe/inlife/stats/product/quote-stats-summary").then(function (response) {
        $scope.quoteSummaryData = QuoteStatsSummaryAdapter.adapt(response);
        $scope.pieData = QuoteStatsSummaryAdapter.adaptToPieChart(response);
    });

    var cellTamplate = '<div ng-click="onCellSelect(row,col)">{{row.getProperty(col.field)}}</div>';

    $scope.quoteSummaryColumns = [
        {field:'entity', displayName:' '},
        {field:'Today', displayName:'Today', cellTemplate:cellTamplate},
        {field:'Yesterday', displayName:'Yesterday', cellTemplate:cellTamplate},
        {field:'Last7Days', displayName:'Last 7 days', cellTemplate:cellTamplate},
        {field:'Last30Days', displayName:'This Month', cellTemplate:cellTamplate},
        {field:'Last90Days', displayName:'Last 3 Months', cellTemplate:cellTamplate},
        {field:'Total', displayName:'Total', cellTemplate:cellTamplate}
    ];

    $scope.footerTemplate = '<div class="gridFooter" style="margin-top: 2px"><strong>Note : </strong> 1(7) means 1 Quote and 7 Quote Items. Click on the row to see status of each Quote Item.</div>';

    $scope.pieOption = {};

    $scope.columnDefs = [
        {field:'salesChannel', displayName:'Sales Channel'},
        {field:'customerName', displayName:'Customer Name'},
        {field:'quoteOptionName', displayName:'Quote Option Name'},
        {field:'siteName', displayName:'Site Name'},
        {field:'productVariant', displayName:'Product Group'},
        {field:'productName', displayName:'Product Name'},
        {field:'expedioQuoteId', displayName:'Expedio Quote Id'},
        {field:'createdBy', displayName:'Created By'},
        {field:'createdOn', displayName:'Created Date'},
        {field:'status', displayName:'Status'},
        {field:'journey', displayName:'Journey'}
    ];

    $scope.loadQuoteItemStats = function (row, column) {
        var queryParams = {
            product:row.entity.entity,
            dateRange:column.field
        };
        httpService.httpQParamGet('/rsqe/inlife/stats/quote-item-stats', queryParams).then(function (response) {
            $scope.quoteItemStats = QuoteItemStatsAdapter.adapt(response);
        });
    };

}]);

controllers.controller('WebMetricsController', ['$scope', 'Configuration', 'httpService', 'WebMetricsAdaptor', 'NoCacheHttpUrlConvertor', 'UIService', function ($scope, Configuration, httpService, WebMetricsAdaptor, NoCacheHttpUrlConvertor, UIService) {

    $scope.navigations = {};
    $scope.userActionSelected = false;
    $scope.countryFilter = [];

    $(function () {
        $("#fromDate").datepicker({ dateFormat:"dd-mm-yy", onSelect:function (date) {
            $scope.fromDate = date;
        }});
    });

    $(function () {
        $("#toDate").datepicker({ dateFormat:"dd-mm-yy", onSelect:function (date) {
            $scope.toDate = date;
        }});
    });


    httpService.httpGet(Configuration.navigationListUri).then(function (data) {
        $scope.navigations = data;
    });

    function validateAndSetDate() {
        var fromDate = $scope.fromDate;
        var toDate = $scope.toDate;

        if (_.isUndefined(fromDate) || _.isUndefined(toDate)) {
            var today = new Date();
            var yesterdayMs = today.getTime() - 1000 * 60 * 60 * 24; // Offset by one day;
            today.setTime(yesterdayMs);

            fromDate = today.getDate() + "-" + (today.getMonth() + 1) + "-" + today.getFullYear();
            toDate = today.getDate() + "-" + (today.getMonth() + 1) + "-" + today.getFullYear();

            $scope.fromDate = fromDate;
            $scope.toDate = toDate;

            $("#fromDate").datepicker("setDate", fromDate);
            $("#toDate").datepicker("setDate", toDate);
        }
    }

    $scope.navigationSelected = function (navigationName) {

        $scope.userActionSelected = true;
        $scope.navigationName = navigationName;

        validateAndSetDate();

        var uri = Configuration.navigationWebMetricsUri.replace("{navName}", navigationName).replace("{fromDate}", $scope.fromDate).replace("{toDate}", $scope.toDate);

        var adaptData = function (data) {
            $scope.webMetricsByLocation = WebMetricsAdaptor.adapt(data);
            buildLineChartOptions();
            $scope.ragByLocations = WebMetricsAdaptor.adaptForRAG(data);
        };

        httpService.httpGet(uri).then(function (data) {
            $scope.webMetrics = data;
            adaptData(data);
        }, function () {
            $scope.webMetricsByLocation = {};
            $scope.ragByLocations = {};
        });
    };

    $scope.exportData = function (navigationName) {
        $scope.userActionSelected = true;
        $scope.navigationName = navigationName;

        validateAndSetDate();
        var uri = Configuration.dataExportUri.replace("{fromDate}", $scope.fromDate).replace("{toDate}", $scope.toDate);
        var btn = $("#exportBtn");

        UIService.block();
        var ifr = ($('<iframe />').attr('src', NoCacheHttpUrlConvertor.convert(uri)).hide().appendTo(btn));
        UIService.unblock();
        setTimeout(function () {ifr.remove();}, 5000);
    };


    function buildLineChartOptions() {
        var buildLegendForLineChart = function () {
            var legend = '<div class="lineChartLegend"><ul class="tc-chart-js-legend">';

            _.each($scope.webMetricsByLocation.datasets, function (dataset) {
                legend += '<li><span style="background-color: ' + dataset.strokeColor + '"></span>' + dataset.label + ' </li>';
            });
            legend += '</ul></div>';

            return legend;
        };

        $scope.lineChartOptions = {
            responsive:false,
            scaleShowGridLines:true,
            scaleGridLineColor:"rgba(0,0,0,.05)",
            scaleGridLineWidth:1,
            bezierCurve:true,
            bezierCurveTension:0.4,
            pointDot:true,
            pointDotRadius:1,
            pointDotStrokeWidth:1,
            pointHitDetectionRadius:20,
            datasetStroke:true,
            datasetStrokeWidth:2,
            datasetFill:true,
            legendTemplate:buildLegendForLineChart()
        };
    }

    $scope.toggleCountrySelection = function (country) {
        $scope.countryFilter.push(country);
    };

    $scope.removeCountry = function (country) {
        $scope.countryFilter.remove(country);
    };
}]);

controllers.controller('WebMetricsPercentileController', ['$scope', 'Configuration', 'httpService', 'WebMetricsPercentileAdapter', 'NoCacheHttpUrlConvertor', 'UIService', function ($scope, Configuration, httpService, WebMetricsPercentileAdapter, NoCacheHttpUrlConvertor, UIService) {
    $scope.locations = {};
    $scope.navigations = {};
    $scope.actionSelected = false;

    $(function () {
        $("#fromDate").datepicker({ dateFormat:"dd-mm-yy", onSelect:function (date) {
            $scope.fromDate = date;
        }});
    });

    $(function () {
        $("#toDate").datepicker({ dateFormat:"dd-mm-yy", onSelect:function (date) {
            $scope.toDate = date;
        }});
    });

    httpService.httpGet(Configuration.locationListUri).then(function (data) {
        $scope.locations = data;
    });

    httpService.httpGet(Configuration.navigationListUri).then(function (data) {
        $scope.navigations = data;
    });
    function validateAndSetDate() {
        var fromDate = $scope.fromDate;
        var toDate = $scope.toDate;

        if (_.isUndefined(fromDate) || _.isUndefined(toDate)) {
            var today = new Date();
            var todayMinus90Days = new Date();
            var todayMinus90DaysMs = todayMinus90Days.getTime() - 1000 * 60 * 60 * 24 * 90; // Offset by 90 days;
            todayMinus90Days.setTime(todayMinus90DaysMs);

            fromDate = todayMinus90Days.getDate() + "-" + (todayMinus90Days.getMonth() + 1) + "-" + todayMinus90Days.getFullYear();
            toDate = today.getDate() + "-" + (today.getMonth() + 1) + "-" + today.getFullYear();

            $scope.fromDate = fromDate;
            $scope.toDate = toDate;

            $("#fromDate").datepicker("setDate", fromDate);
            $("#toDate").datepicker("setDate", toDate);
        }
    }

    $scope.actionSelected = function () {

        validateAndSetDate();
        var locationSelected = '';
        if(!_.isUndefined($scope.selectedLocation)) {
            locationSelected = $scope.selectedLocation;
        }
        var navigationSelected = '';
        if(!_.isUndefined($scope.selectedNavigation)) {
            navigationSelected = $scope.selectedNavigation;
        }
        var uriCountryWisePercentage = Configuration.countryWisePercentageUri.replace("{location}", locationSelected).replace("{navigation}", navigationSelected).replace("{fromDate}", $scope.fromDate).replace("{toDate}", $scope.toDate);
        httpService.httpGet(uriCountryWisePercentage).then(function (data) {
            $scope.percentageByCountry = WebMetricsPercentileAdapter.adaptForPercentage(data);
            $scope.percentageLabels = WebMetricsPercentileAdapter.adaptForLabels(data);
        });

        var uri = Configuration.percentileWebMetricsUri.replace("{location}", locationSelected).replace("{navigation}", navigationSelected).replace("{fromDate}", $scope.fromDate).replace("{toDate}", $scope.toDate);
        var adaptData = function (data) {
            $scope.percentileByCountry = WebMetricsPercentileAdapter.adapt(data);

            buildBarChartOptions();
        };

        httpService.httpGet(uri).then(function (data) {
            $scope.webMetrics = data;
            adaptData(data);
        }, function () {
            $scope.percentileByCountry = {};
            $scope.percentageByCountry = {};
            $scope.percentageLabels = {};
        });

    };

    function buildBarChartOptions() {
        var buildLegendForBarChart = function () {
            var legend = '<div class="barChartLegend"><ul class="tc-chart-js-legend">';

            _.each($scope.percentileByCountry.datasets, function (dataset) {
                legend += '<li><span style="background-color: ' + dataset.strokeColor + '"></span>' + dataset.label + ' </li>';
            });
            legend += '</ul></div>';

            return legend;
        };

        $scope.barChartOptions = {
            // Sets the chart to be responsive
            responsive:false,

            //Boolean - Whether the scale should start at zero, or an order of magnitude down from the lowest value
            scaleBeginAtZero:true,

            //Boolean - Whether grid lines are shown across the chart
            scaleShowGridLines:true,

            //String - Colour of the grid lines
            scaleGridLineColor:"rgba(0,0,0,.05)",

            //Number - Width of the grid lines
            scaleGridLineWidth:1,

            //Boolean - If there is a stroke on each bar
            barShowStroke:true,

            //Number - Pixel width of the bar stroke
            barStrokeWidth:2,

            //Number - Spacing between each of the X value sets
            barValueSpacing:5,

            //Number - Spacing between data sets within X values
            barDatasetSpacing:1,
            legendTemplate:buildLegendForBarChart()
        };
    }

    $scope.actionSelected();
}]);

controllers.controller('KeyTransactionController', ['$scope', 'Configuration', 'httpService', 'WebMetricsPercentileAdapter', 'NoCacheHttpUrlConvertor', 'UIService', function ($scope, Configuration, httpService, WebMetricsPercentileAdapter, NoCacheHttpUrlConvertor, UIService) {
    $scope.transactionTargets = {};

    httpService.httpGet(Configuration.transactionTargetsUri).then(function (data) {

        var transactionTargetsData = [];
        if (data != null && data.length >= 1) {
            _.each(data, function (transactionTarget) {
                transactionTargetsData.push({"id":transactionTarget.id,"transactionName":transactionTarget.name,  "target":transactionTarget.target,"createdBy":transactionTarget.createdBy,"createdDate":transactionTarget.createdDate});
            });
            $scope.transactionTargets = transactionTargetsData;
        }

    });



    $scope.saveTargets = function(){
     var updateData = {data:JSON.stringify($scope.transactionTargets)};
       httpService.httpQParamGet(Configuration.updateKeyTransactionTargetUri,updateData).then(function (data){
           if(data != null) {
              alert("Configuration details successfully updated");
           }
       });
    }
}]);


