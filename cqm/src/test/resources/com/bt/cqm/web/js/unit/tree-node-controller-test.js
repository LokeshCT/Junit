'use strict';

describe('TreeNodeControllerTest - ', function() {

    var rootScope, scope, location;

    beforeEach(angular.mock.module(function($provide) {
        provide$configuration($provide);
    }));

    beforeEach(angular.mock.module('cqm'));

    beforeEach(inject(function($rootScope, $controller, $location) {
        rootScope = $rootScope;
        scope = rootScope.$new();
        location = $location;

        $controller('TreeNodeController', {$scope: scope});
    }));


    describe('Node with children - ', function() {

        var nodeWithChildren = {
            uri:"",
            selected: false,
            expanded:false,
            children:[
                {
                    uri:"childNodeUri",
                    selected: false,
                    expanded:false,
                    children:[]
                }
            ]
        };

        beforeEach(function() {
            scope.node = nodeWithChildren;
        });

        it('should expand/collapse node upon click', function() {
            scope.select(nodeWithChildren);
            expect(nodeWithChildren.expanded).toBeTruthy();
        });

        it('hasChildren should return true', function() {
            expect(scope.hasChildren(nodeWithChildren)).toBeTruthy();
        });
    });

    describe('Node with no children - ', function() {
        var nodeWithNoChildren = {
            uri:"uri",
            selected: false,
            expanded:false,
            children:[]
        };

        var tab = {
            rootNode: nodeWithNoChildren
        };
        nodeWithNoChildren.tab = function() {
            return tab;
        };

        beforeEach(function() {
            scope.node = nodeWithNoChildren;
        });


        it('should update location with respective uri', function() {
            scope.select(nodeWithNoChildren);
            expect(tab.uri).toBe(nodeWithNoChildren.uri);
            expect(location.path()).toBe("/uri");
        });

        it('hasChildren should return false', function() {
            expect(scope.hasChildren(nodeWithNoChildren)).toBeFalsy();
        });


    });
});