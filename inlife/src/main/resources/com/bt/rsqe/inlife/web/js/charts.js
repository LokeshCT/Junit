'use strict';

var rsqeCharts = angular.module('rsqeCharts',['tc.chartjs']);

rsqeCharts.directive('rsqePieChart', ['$compile', function($compile){
    return {
        restrict:'E',
        replace: true,
        scope: {
           pieData:'=',
           options:'='
        },
        link:function(scope, element, attrs) {

            var buildLegendTemplate = function() {
                var legend = '<ul class="tc-chart-js-legend">';
                _.each(scope.pieData, function(data){
                    legend += '<li><span style="background-color: ' + data.color +'"></span> ' + data.label + '</li>';
                });
                legend += '</ul>'
                return legend;
            };
            scope.options = {
                animateRotate : true,
                legendTemplate : '<ul class="tc-chart-js-legend"><% for (var i=0; i<segments.length; i++){%><li><span style="background-color:<%=segments[i].fillColor%>"></span><%if(segments[i].label){%><%=segments[i].label%><%}%></li><%}%></ul>'
            };
            element.append('<canvas tc-chartjs-pie chart-data="pieData" chart-options="options" auto-legend></canvas>');
            var childScope = scope.$new();
            element.bind('destroyed', function () {});
            $compile(element.contents())(childScope);
        }
    };
}]);

rsqeCharts.directive('rsqeLineChart', ['$compile',function($compile){
     return {
        restrict:'E',
        replace: true,
        scope: {
           data:'=',
           options:'='
        },
        link:function(scope, element, attrs) {
            element.append('<div class="lineChartCanvas"><canvas tc-chartjs-line chart-data="data" chart-options="options" auto-legend width=700 height=250></canvas></div>');
            var childScope = scope.$new();
            element.bind('destroyed', function () {});
            $compile(element.contents())(childScope);
        }
    };
}]);

rsqeCharts.directive('rsqeBarChart', ['$compile', function($compile) {
    return {
        restrict: 'E',
        replace:true,
        scope: {
            data:'=',
            options : '='
        },
        link:function(scope, element, attrs) {
            element.append('<div class="barChartCanvas"> <canvas tc-chartjs-bar width="500" height="300" chart-options="options" chart-data="data" auto-legend></canvas></div>');
            var childScope = scope.$new();
            element.bind('destroyed', function(){});
            $compile(element.contents())(childScope);
        }
    };
}]);
