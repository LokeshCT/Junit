'use strict';

var rsqeGridModule = angular.module('rsqeGridModule', ['ngGrid']);

rsqeGridModule.directive('rsqeGrid', ['$compile', function($compile) {
    return {
        restrict:'E',
        replace: true,
        controller:'RsqeGridController',
        scope: {
            data:'=',
            columnDefs:'=',
            footerTemplate:'=',
            onRowSelect:'=',
            onCellSelect:'='
        },
        link: function(scope, element, attrs) {

            scope.gridData = {data: 'data', multiSelect:false, enableColumnResize:true, showColumnMenu:true, showFilter:true, showFooter:true,
                afterSelectionChange: function(data) {
                    if (!_.isUndefined(scope.onRowSelect) && data.selected) {
                        scope.onRowSelect(data.entity);
                    }
                },
                enableCellSelection: !_.isUndefined(attrs.enableCellSelection),
                enableRowSelection: _.isUndefined(attrs.enableCellSelection)
            };
            if (!_.isUndefined(scope.columnDefs)) {
                scope.gridData.columnDefs = scope.columnDefs;
            }

            if (!_.isUndefined(attrs.enableGrouping)) {
                scope.gridData.showGroupPanel = true;
            }

            if (!_.isUndefined(attrs.hideFooter)) {
                scope.gridData.showFooter = false;
            }

            if (!_.isUndefined(attrs.enablePinning)) {
                scope.gridData.enablePinning = true;
            }

            if (!_.isUndefined(attrs.footerTemplate)) {
                scope.gridData.showFooter = true;
                scope.gridData.footerTemplate = scope.$eval(attrs.footerTemplate);
            }

            element.append('<div class="' + attrs.class + '" ng-grid="gridData"></div>');
            var childScope = scope.$new();
            element.bind('destroyed', function () {});
            $compile(element.contents())(childScope);
        }
    };
}]);

rsqeGridModule.controller('RsqeGridController', ['$scope', 'httpService', function($scope) {

}]);
