'use strict';
var rsqeCharts = angular.module('rsqeCharts');

rsqeCharts.factory('WebMetricsPercentileAdapter', [function () {

    var buildData = function (locationPercentile) {
        var data = [];
        if (locationPercentile != null) {
            data.push(locationPercentile.percentile.percentile25th);
            data.push(locationPercentile.percentile.percentile40th);
            data.push(locationPercentile.percentile.percentile50th);
            data.push(locationPercentile.percentile.percentile60th);
            data.push(locationPercentile.percentile.percentile75th);
            data.push(locationPercentile.percentile.percentile90th);
            data.push(locationPercentile.percentile.percentile95th);
            data.push(locationPercentile.percentile.percentile99th);
        }
        return data;
    };

    var buildPercentageData = function (percentageByLocation) {
        var data = [];
        _.each(percentageByLocation, function (percentageObj) {
            data.push(percentageObj.percentage +'% [' +percentageObj.dataPoint+']');
        });

        return data;
    };

    return {
        adapt:function (locationPercentile) {
            var labels = ["25th Percentile","40th Percentile", "50th Percentile",  "60th Percentile", "75th Percentile", "90th Percentile", "95th Percentile", "99th Percentile"];

            return {
                labels:labels,
                datasets:[
                    {
                        label:locationPercentile.location,
                        fillColor:'rgba(145, 78, 255,0.5)',
                        strokeColor:'rgba(145, 78, 255,0.8)',
                        highlightFill:'rgba(145, 78, 255,0.75)',
                        highlightStroke:'rgba(145, 78, 255,1)',
                        data:buildData(locationPercentile)
                    }
                ]
            };
        },

        adaptForPercentage:function (countryWisePercentages) {

            var data = [];

            _.each(countryWisePercentages, function (countryWisePercentage) {
                data.push({
                              "navigationName":countryWisePercentage.navigationName,
                              "target":countryWisePercentage.target,
                              "percentages":buildPercentageData(countryWisePercentage.percentageByLocation)
                          });
            });

            return data;
        },

        adaptForLabels:function (countryWisePercentages) {

            var data = [];
            data.push("Key Transaction");
            data.push("Target");
            if (countryWisePercentages != null && countryWisePercentages.length >= 1) {
                _.each(countryWisePercentages, function (countryWisePercentage) {
                    _.each(countryWisePercentage.percentageByLocation, function (percentageObj) {
                        if(!_.contains(data, percentageObj.location)){
                            data.push(percentageObj.location);
                        }
                    });
                });
            }
            return data;
        }
    };
}]);
