'use strict';
var rsqeCharts = angular.module('rsqeCharts');

rsqeCharts.factory('WebMetricsAdaptor', [function() {

    var attachBehaviour = function(locationMetrics) {
        locationMetrics.getPercentageHitsForResponseTime = function(respTime) {
            var matchingMetrics = _.find(locationMetrics.metrics, function(metricsData) {
                return metricsData.timeInSeconds == respTime;
            });
            return _.isUndefined(matchingMetrics) ? 0 : matchingMetrics.txPercentage;
        };

        locationMetrics.getPercentageHitsForAboveResponseTime = function(respTime) {
            var percentage = 0;
            _.each(locationMetrics.metrics, function(metricsData) {
                if (metricsData.timeInSeconds >= respTime) {
                    percentage += metricsData.txPercentage;
                }
            });
            return percentage;
        };

        locationMetrics.getPercentageHitsBetween = function(from, to) {
            var percentage = 0;
            _.each(locationMetrics.metrics, function(metricsData) {
                if (metricsData.timeInSeconds >= from && metricsData.timeInSeconds < to) {
                    percentage += metricsData.txPercentage;
                }
            });
            return percentage;
        };

        locationMetrics.getHitCountBetween = function(from, to) {
            var hitCont = 0;
            _.each(locationMetrics.metrics, function(metricsData) {
                if (metricsData.timeInSeconds >= from && metricsData.timeInSeconds < to) {
                    hitCont += metricsData.txCount;
                }
            });
            return hitCont;
        };

        locationMetrics.getHitCountForAboveResponseTime = function(respTime) {
            var hitCont = 0;
            _.each(locationMetrics.metrics, function(metricsData) {
                if (metricsData.timeInSeconds > respTime) {
                    hitCont += metricsData.txCount;
                }
            });
            return hitCont;
        };

        locationMetrics.dataPoints = function() {
            var count = 0;
            _.each(locationMetrics.metrics, function(metricsData) {
                count += metricsData.txCount;
            });
            return count;
        };
    };

    function randomColor() {
        var color = '#' + (Math.random() * 0xFFFFFF << 0).toString(16);
        return  color == '#ffffff' ? randomColor() : color;
    }


    var buildData = function(locationMetrics) {
        var data = [];
        for (var i = 0; i <= 30; ++i) {
            data.push(locationMetrics.getPercentageHitsForResponseTime(i));
        }
        data.push(locationMetrics.getPercentageHitsForAboveResponseTime(31));
        return data;
    };


    return {
        adapt : function(metricsForNavigation) {
            var labels = ["0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15",
                "16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","30+ sec"];

            var dataSets = _.map(metricsForNavigation.metricsByLocation, function(locationMetrics) {
                attachBehaviour(locationMetrics);
                var color = randomColor();
                return {
                    label : locationMetrics.location,
                    fillColor: color,
                    strokeColor: color,
                    pointColor: color,
                    pointStrokeColor: '#fff',
                    pointHighlightFill: '#fff',
                    pointHighlightStroke: color,
                    data: buildData(locationMetrics)
                };
            });

            return {
                labels : labels,
                datasets: dataSets
            };
        },
        adaptForRAG : function(metricsForNavigation) {
            return _.map(metricsForNavigation.metricsByLocation, function(locationMetrics) {
                attachBehaviour(locationMetrics);
                return {
                    location : locationMetrics.location,
                    dataPoints : locationMetrics.dataPoints(),
                    green : locationMetrics.getPercentageHitsBetween(0, 5),
                    amber : locationMetrics.getPercentageHitsBetween(5, 10),
                    red : locationMetrics.getPercentageHitsForAboveResponseTime(10)
                };
            });
        },
        groupByRag : function(metricsGroupByLocation) {

            var labels = [];

            var green = {label : 'Green', fillColor: 'Green', strokeColor: 'green', data : []};
            var amber = {label : 'Amber', fillColor: '#ffcc00', strokeColor: '#ffcc00', data : []};
            var red = {label : 'Red', fillColor: 'Red', strokeColor: 'Red', data : []};

            _.each(metricsGroupByLocation.metricsByLocation, function(locationMetrics) {
                attachBehaviour(locationMetrics);
                labels.push(locationMetrics.location);
                green.data.push(locationMetrics.getHitCountBetween(0, 5));
                amber.data.push(locationMetrics.getHitCountBetween(5, 10));
                red.data.push(locationMetrics.getHitCountForAboveResponseTime(10));
            });

            return {
                labels : labels,
                datasets : [green, amber, red]
            };
        }

    };
}]);
