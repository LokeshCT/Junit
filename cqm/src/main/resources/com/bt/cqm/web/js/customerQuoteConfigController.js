'use strict';

var module = angular.module('cqm.controllers');

module.controller('CustomerQuoteConfigController', ['$scope', 'TabService', 'PageContext', 'SessionContext', 'NodeStatusService', '$rootScope', 'UserContext', function ($scope, TabService, PageContext, SessionContext, NodeStatusService, $rootScope, UserContext) {

    function identifyTabToSelect() {
        TabService.getTabs().then(function (data) {
            var tabs = data["tabs"];
            var tabId = $scope.context.subState;
            $scope.tabToSelect = TabService.findTab(tabs, tabId);
            if (_.isUndefined($scope.tabToSelect)) {
                $scope.tabToSelect = _.first(tabs);
            }
            $scope.rootNode = $scope.tabToSelect.treeNode;
        });
    };

    identifyTabToSelect();

    $scope.gotoCustomerSelection = function () {
        SessionContext.setState(STATE.CustomerSelection);
        PageContext.setCentralSiteId(undefined);
    };

    $scope.$on(EVENT.TabChange, function (event, tab) {
        $scope.rootNode = tab.treeNode;

        if (!_.isUndefined($scope.rootNode)) {
            //leafNodes = $scope.getLeafNode($scope.rootNode, leafNodes);
            var leafNodes = $scope.getAllLeafNodes($scope.rootNode);

            $scope.changeNodeStatuses(leafNodes);
        }

    });


    /*    $scope.getLeafNode = function (node, leafNodes) {

     var childNodes = node.children;
     var tempLeafNodes = leafNodes
     if (_.isUndefined(tempLeafNodes)) {
     tempLeafNodes = [];
     }

     if (_.isUndefined(childNodes) || (_.isArray(childNodes) && (childNodes.length < 1))) {
     tempLeafNodes.push(node)
     return tempLeafNodes;
     } else {
     _.each(node.children, function (child) {
     $scope.getLeafNode(child, tempLeafNodes);

     });
     }

     }*/

    $scope.getAllLeafNodes = function (node) {
        var allLeafNodes = [];
        if ($scope.isLeafNode(node)) {
            allLeafNodes.push(node);

            return allLeafNodes;
        }
        _.each(node.children, function (childNode) {
            if ($scope.isLeafNode(childNode)) {
                allLeafNodes.push(childNode);
            } else {
                _.each(childNode.children, function (childNode2) {
                    if ($scope.isLeafNode(childNode2)) {
                        allLeafNodes.push(childNode2);
                    }
                })
            }
        })

        return allLeafNodes;
    };

    $scope.isLeafNode = function (node) {
        var childNodes = node.children;

        if (_.isUndefined(childNodes) || (_.isArray(childNodes) && (childNodes.length < 1))) {
            return true;
        } else {
            return false;
        }
    };

    $scope.changeNodeStatuses = function (leafNodes) {
        _.each(leafNodes, function (aNode) {
            var promise = NodeStatusService.countRecords(aNode);

            if (!_.isUndefined(promise)) {
                promise.then(function (successData) {
                    if (_.isBoolean(successData)) {
                        if (successData) {
                            aNode.status = NODE_STATUS.VALID;
                        } else {
                            aNode.status = NODE_STATUS.INVALID;
                        }
                    }
                    else if (_.isArray(successData)) {
                        if (successData.length > 0) {
                            aNode.status = NODE_STATUS.VALID;
                        } else {
                            aNode.status = NODE_STATUS.INVALID;
                        }
                    } else if (_.isNumber(successData)) {
                        if (successData > 0) {
                            aNode.status = NODE_STATUS.VALID;
                        } else {
                            aNode.status = NODE_STATUS.INVALID;
                        }
                    } else if (_.isEmpty(successData)) {
                        aNode.status = NODE_STATUS.INVALID;
                    } else if (!_.isUndefined(successData.errorId)) {
                        aNode.status = NODE_STATUS.INVALID;
                    } else {
                        aNode.status = NODE_STATUS.VALID;
                    }

                }, function (rejectData) {
                    console.log('Promise Rejected for Fetching Record count . Reason -' + rejectData);
                    aNode.status = NODE_STATUS.INVALID;
                });
            }
        });
    };

    $scope.$on(EVENT.SubStateChanged, function (event, subState) {
        $scope.context.subState = subState;
        identifyTabToSelect();
    });

    $scope.$on(EVENT.CustomerLoaded, function (event) {
        $scope.salesChannelName = PageContext.getActualSalesChannel().name;
        $scope.customerName = PageContext.getCustomer().cusName;
        $scope.contract = PageContext.getContract().refNumber;
        $scope.roleName = PageContext.getSelectedRole().roleName;
        $scope.userType = UserContext.getUser().userType;

        if (!$rootScope.$$phase) {
            $rootScope.$digest();
        }
    });

}]);
