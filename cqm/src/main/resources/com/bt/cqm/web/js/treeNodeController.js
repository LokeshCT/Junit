var module = angular.module('cqm.controllers');

module.controller('TreeNodeController', ['$scope', '$rootScope', '$http', '$timeout', 'UIService', '$location', 'WebMetrics', function ($scope, $rootScope, $http, $timeout, UIService, $location, WebMetrics) {

    $scope.selectedNode = undefined;
    $scope.select = function (node) {
        WebMetrics.registerUserAction('CQM - ' + $scope.node.tab().label + ' Tab - ' + $scope.node.label);
        if (_.isEmpty(node.uri)) {
            $scope.toggleExpanded(node);
        } else {
            node.tab().uri = node.uri;  //change this tab uri so that when you come on to this tab next time, you will auto moved to tree node where you left before.
            $location.path(node.uri);
        }

        $scope.selectedNode = node;
        $rootScope.$broadcast(EVENT.TreeNodeNavigation, node);
    };


    $scope.$on(EVENT.TreeNodeNavigation, function (event, node) {
        $scope.node.selected = false;
        node.selected = true;
    });


    $scope.toggleExpanded = function (node) {
        node.expanded = !node.expanded;
    };

    $scope.changeStatus = function (status) {
        $scope.selectedNode = $scope.getSelectedNode();
        if (!_.isUndefined($scope.selectedNode)) {
            switch (status) {
                case NODE_STATUS.NOT_APPLICABLE:
                    $scope.selectedNode.status = NODE_STATUS.NOT_APPLICABLE;
                    break;
                case NODE_STATUS.INVALID:
                    $scope.selectedNode.status = NODE_STATUS.INVALID;
                    break;
                case NODE_STATUS.VALID:
                    $scope.selectedNode.status = NODE_STATUS.VALID;
                    break;
                case NODE_STATUS.ERROR:
                    $scope.selectedNode.status = NODE_STATUS.ERROR;
                    break;
            }
        }
    };


    $scope.$on(EVENT.NodeStatusChange, function (event, status) {
        $scope.changeStatus(status);
    });

    $scope.getSelectedNode = function () {
        var selectedNode = undefined;

        if (!_.isUndefined($scope.node)) {
            selectedNode = _.find($scope.node.children, function (child) {
                if (child.selected && child.children.length < 1) {
                    return child;
                }
            })
        }

        return selectedNode;
    }

    $scope.hasChildren = function (node) {
        return node.children.length > 0
    };

    $scope.$on('$routeChangeSuccess', function (event, currentRoute) {
        if (!_.isUndefined(currentRoute) && !_.isUndefined($scope.node) && $scope.node.uri == $location.path()) {
            WebMetrics.registerUserAction('CQM - ' + $scope.node.tab().label + ' Tab - ' + $scope.node.label);
            $scope.node.selected = true;
        }
    });

    $scope.$on(EVENT.ActionDeSelect, function (event) {
        if (!_.isUndefined($scope.selectedNode)) {
            $scope.selectedNode.selected = false;
        }
    });

}]);


