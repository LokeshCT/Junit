'use strict';

/* Filters */

var filters = angular.module('cqm.filters', ['cqm-constants']);


filters.filter('interpolate', ['version', function(version) {
    return function(text) {
        return String(text).replace(/\%VERSION\%/mg, version);
    }
}]);

filters.filter('visibleCssClasses', [ function () {
    return function (node) {
        return 'visible';
    }
}]);


filters.filter('validCssClasses', [function() {
    return function(node, depth) {
        var validity = node.valid == 'true' ? "valid" : "invalid";
        return 'level' + depth + ' ' + validity;
    };
}]);


filters.filter('treeNodeValidClasses', [function() {
    return function (node) {
        switch (node.status) {
            case NODE_STATUS.NOT_APPLICABLE:
                return 'BT_TOUCH-NOT_APPLICABLE';
            case NODE_STATUS.INVALID:
                return 'BT_TOUCH-NOT_CONFIGURED';
            case NODE_STATUS.VALID:
                return 'BT_TOUCH-CONFIGURED';
            case NODE_STATUS.ERROR:
                return 'BT_TOUCH-CONFIGURED_AND_ERROR';
        }
    }
}]);


filters.filter('expandedCssClasses', [function () {
    return function (node) {
        return (node.expanded == undefined || !node.expanded) ?
            'icon-chevron-right' :
            'icon-chevron-down';
    }
}]);


filters.filter('isExpandableCssClasses', [function() {
    return function(node) {
        return node.children.length > 0 ? "isExpandable" : "isNotExpandable";
    };
}]);


filters.filter('selectedCssClasses', [ function () {
    return function (node) {
        return node.selected == true ? 'selected' : 'unselected';
    }
}]);

filters.filter('tabSelectedCssClasses', [ function () {
    return function (tab) {
        return tab.selected == true ? 'active' : 'inactive';
    }
}]);
