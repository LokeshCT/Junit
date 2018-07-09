'use strict';

describe('HeaderControllerTest - ', function() {

    var scope, pageContext, userContext;
    var user = {'name': "user full name"};
    var salesChannel = {'name': "BT INDIA"};
    var customer = {'name': "A CUSTOMER"};
    var contract = {'name': "A CONTRACT"};

    beforeEach(angular.mock.module(function($provide) {
        provide$configuration($provide);
    }));

    beforeEach(angular.mock.module('cqm'));

    beforeEach(inject(function($rootScope, $controller, PageContext, UserContext) {
        userContext = UserContext;
        pageContext = PageContext;
        scope = $rootScope.$new();
        $controller('headerController', {$scope: scope});

        userContext.initialize(user);
        pageContext.setContext(salesChannel, customer, contract);
        scope.context = {state:"SomeState"};
    }));


    it('should clear user context and update the state after logout', inject(function() {
        expect(pageContext.exist()).toBeTruthy();
        expect(userContext.exist()).toBeTruthy();
        expect(scope.context.state).toBe("SomeState");

        scope.logout();

        expect(pageContext.exist()).toBeFalsy();
        expect(userContext.exist()).toBeFalsy();
        expect(scope.context.state).toBe(STATE.Logout);
    }));
});