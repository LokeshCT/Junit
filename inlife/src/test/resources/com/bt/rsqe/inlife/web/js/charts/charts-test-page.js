'use strict';

var rsqeChartsTest = angular.module('rsqeChartsTest', ['rsqeCharts']);

rsqeChartsTest.controller('PieChartTestController', ['$scope', function($scope) {

    $scope.quoteStatsSummary = {"yesterday":{"stats":[],"dateRange":"13-10-2014"},"last7Days":{"stats":[
        {"groupBy":"Connect Acceleration","quoteOptionCount":2,"lineItemCount":2}
    ],"dateRange":"07-10-2014 to 13-10-2014"},"last30Days":{"stats":[
        {"groupBy":"Connect Acceleration","quoteOptionCount":9,"lineItemCount":9},
        {"groupBy":"Internet Connect Reach","quoteOptionCount":3,"lineItemCount":3},
        {"groupBy":"Internet Connect Global","quoteOptionCount":14,"lineItemCount":14},
        {"groupBy":"One Cloud Cisco-Create Contract","quoteOptionCount":9,"lineItemCount":9},
        {"groupBy":"One Cloud Cisco-Build Order","quoteOptionCount":9,"lineItemCount":9},
        {"groupBy":"TEM","quoteOptionCount":5,"lineItemCount":7},
        {"groupBy":"Ethernet Connect Global","quoteOptionCount":5,"lineItemCount":5}
    ],"dateRange":"14-09-2014 to 13-10-2014"},"last90Days":{"stats":[
        {"groupBy":"Connect Acceleration","quoteOptionCount":29,"lineItemCount":29},
        {"groupBy":"Connect Intelligence","quoteOptionCount":6,"lineItemCount":6},
        {"groupBy":"Internet Connect Reach","quoteOptionCount":14,"lineItemCount":17},
        {"groupBy":"One Cloud Cisco-Build Order","quoteOptionCount":148,"lineItemCount":170},
        {"groupBy":"Internet Connect Global","quoteOptionCount":30,"lineItemCount":31},
        {"groupBy":"Connect Optimisation","quoteOptionCount":5,"lineItemCount":5},
        {"groupBy":"One Cloud Cisco-Create Contract","quoteOptionCount":88,"lineItemCount":91},
        {"groupBy":"TEM","quoteOptionCount":7,"lineItemCount":9},
        {"groupBy":"Ethernet Connect Global","quoteOptionCount":8,"lineItemCount":8}
    ],"dateRange":"16-07-2014 to 13-10-2014"},"total":{"stats":[
        {"groupBy":"Connect Acceleration","quoteOptionCount":66,"lineItemCount":68},
        {"groupBy":"Connect Intelligence","quoteOptionCount":12,"lineItemCount":12},
        {"groupBy":"Internet Connect Reach","quoteOptionCount":24,"lineItemCount":32},
        {"groupBy":"One Cloud Cisco-Build Order","quoteOptionCount":209,"lineItemCount":235},
        {"groupBy":"Internet Connect Global","quoteOptionCount":39,"lineItemCount":42},
        {"groupBy":"Connect Optimisation","quoteOptionCount":5,"lineItemCount":5},
        {"groupBy":"One Cloud Cisco-Create Contract","quoteOptionCount":207,"lineItemCount":217},
        {"groupBy":"TEM","quoteOptionCount":7,"lineItemCount":9},
        {"groupBy":"Ethernet Connect Global","quoteOptionCount":8,"lineItemCount":8}
    ],"dateRange":"Total"}};


    $scope.pieData = _.map($scope.quoteStatsSummary.total.stats, function(stats) {
        return {
            value: stats.lineItemCount,
            color: randomColor(),
            label: stats.groupBy
        };
    });
//// Chart.js Data
//    $scope.pieData = [
//      {
//        value: 300,
//        color: randomColor(),
//        label: 'Red'
//      },
//      {
//        value: 50,
//        color: randomColor(),
//        label: 'Green'
//      },
//      {
//        value: 100,
//        color: randomColor(),
//        label: 'Yellow'
//      }
//    ];

    // Chart.js Options


    function randomColor() {
        return '#' + (Math.random() * 0xFFFFFF << 0).toString(16);
    }


}]);


rsqeChartsTest.controller('LineChartTestController', ['$scope', 'WebMetricsAdaptor', function($scope, WebMetricsAdaptor) {

    var dataFromServer = {"navigationName":"navName","metricsByLocation":[
        {
            "location":"IND",
            "metrics":[
                {
                    "txCount":29,
                    "txPercentage":14,
                    "timeInSeconds":34
                },
                {
                    "txCount":343,
                    "txPercentage":26,
                    "timeInSeconds":3
                },
                {
                    "txCount":222,
                    "txPercentage":20,
                    "timeInSeconds":1
                },
                {
                    "txCount":33,
                    "txPercentage":14,
                    "timeInSeconds":1
                },
                {
                    "txCount":55,
                    "txPercentage":17,
                    "timeInSeconds":3
                },
                {
                    "txCount":34,
                    "txPercentage":5,
                    "timeInSeconds":123
                },
                {
                    "txCount":22,
                    "txPercentage":3,
                    "timeInSeconds":1
                }
            ]
        },
        {
            "location":"UK",
            "metrics":[
                {
                    "txCount":22,
                    "txPercentage":11,
                    "timeInSeconds":4
                },
                {
                    "txCount":44,
                    "txPercentage":22,
                    "timeInSeconds":16
                },
                {
                    "txCount":55,
                    "txPercentage":28,
                    "timeInSeconds":1
                },
                {
                    "txCount":22,
                    "txPercentage":11,
                    "timeInSeconds":454
                },
                {
                    "txCount":55,
                    "txPercentage":28,
                    "timeInSeconds":7878
                }
            ]
        }
    ]};


    $scope.data =  WebMetricsAdaptor.adapt(dataFromServer);

    function buildLegend() {
        var legend = '<div class="lineChartLegend"><ul class="tc-chart-js-legend">';

        _.each($scope.data.datasets, function(dataset) {
            legend +='<li><span style="background-color: ' + dataset.strokeColor + '"></span>' + dataset.label + ' </li>';
        });
        legend += '</ul></div>';

        return legend;
    }

        // Chart.js Options
    $scope.options =  {
          responsive: false,
          scaleShowGridLines : true,
          scaleGridLineColor : "rgba(0,0,0,.05)",
          scaleGridLineWidth : 1,
          bezierCurve : true,
          bezierCurveTension : 0.4,
          pointDot : true,
          pointDotRadius : 1,
          pointDotStrokeWidth : 1,
          pointHitDetectionRadius : 20,
          datasetStroke : true,
          datasetStrokeWidth : 2,
          datasetFill : true,
        legendTemplate : buildLegend()
        };


}]);
