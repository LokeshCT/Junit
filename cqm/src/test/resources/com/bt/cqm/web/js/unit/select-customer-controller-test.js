'use strict';
describe('SelectCustomerControllerTest - ', function() {

    var q, rootScope, scope, SalesChannelService, ContractService, controller, deferred, pageContext;

    beforeEach(angular.mock.module(function($provide) {
     provide$configuration($provide);
     }));

    beforeEach(angular.mock.module('cqm'));

    beforeEach(inject(function($q, $rootScope, $controller, UIService, PageContext, salesChannelService, contractService) {
        q = $q;
        rootScope = $rootScope;
        SalesChannelService = salesChannelService;
        ContractService = contractService;
        pageContext = PageContext;

        scope = $rootScope.$new();
        scope.salesUser = {"name":"Leela Sankar Pinjala", salesChannelList:[
            {name:"BT INDIA"},
            {name:"BT UK"}
        ]};
        scope.context = {state : ""};

        spyOn(UIService, 'block');
        spyOn(UIService, 'unblock');
        controller = $controller;

    }));

    describe("When sales channel have more than one customer & contract - ", function() {
        var customers = {itemWrapper:{customerDTO:[
            {cusId:"cus1", name:"cus1"},
            {cusId : "cus2", name: "cus2"}
        ]}};
        var contracts = [
            {id:"contr1"},
            {id:"contr2"}
        ];

        beforeEach(function() {

            deferred = q.defer();
            spyOn(SalesChannelService, 'getCustomers').andReturn(deferred.promise);

            spyOn(ContractService, 'getContracts').andCallFake(function(salesChannelName, cusId, fn) {
                fn(contracts, '200');
            });

            controller('SelectCustomerController', {$scope:scope});
        });


        it('should reset customer & contract upon sales channel change', function() {
            scope.salesChannelChange('{"name" : "BT INDIA"}');
            deferred.resolve(customers);
            rootScope.$digest();
            expect(scope.customerList.itemWrapper.customerDTO.length).toBe(2);
            //expect(scope.contractList.length).toBe(0);
        });


        it('should update salesChannel with customer list', function() {
            scope.salesChannel = '{"name":"BT INDIA"}';
            expect(scope.customerList.itemWrapper).toBeUndefined();

            scope.salesChannelChange(scope.selectedSalesChannel);

            deferred.resolve(customers);
            rootScope.$digest();
            expect(scope.customerList.itemWrapper.customerDTO.length).toBe(2);
        });


        it('should reset contract upon customer change', function() {
            scope.salesChannel = {name:"BT INDIA"};
            expect(scope.contractList.length).toBe(0);
            scope.salesChannel ='{"name":"BT AMERICAS"}';
            scope.customerChange(JSON.stringify(customers.itemWrapper.customerDTO[0]));

            expect(scope.contractList.length).toBe(2);
        });

        it('should not allow launching customer configuration if salesChannel not selected', function() {
            expect(pageContext.exist()).toBeFalsy();
            scope.launchConfiguration();
            expect(pageContext.exist()).toBeFalsy();
        });

        it('should not allow launching customer configuration if customer not selected', function() {
            expect(pageContext.exist()).toBeFalsy();
            scope.salesChannel = {name:"BT INDIA"};
            scope.launchConfiguration();
            expect(pageContext.exist()).toBeFalsy();
        });

        it('should not allow launching customer configuration if contract not selected', function() {
            expect(pageContext.exist()).toBeFalsy();
            scope.salesChannel = '{"name":"BT INDIA"}';
            scope.customerChange(JSON.stringify(customers.itemWrapper.customerDTO[0]));
            scope.launchConfiguration();
            expect(pageContext.exist()).toBeFalsy();
        });

    });

    describe("When sales channel have exactly one customer & contract - ", function() {

        var customer = {itemWrapper:{customerDTO:[
            {cusId:"cus1", name:"cus1"}
        ]}};
        var contract = [
            {id:"contr1"}
        ];

        beforeEach(function() {
            scope.salesUser = {"name":"Leela Sankar Pinjala", salesChannelList:[
                {name:"BT INDIA"}
            ]};

            deferred = q.defer();
            spyOn(SalesChannelService, 'getCustomers').andReturn(deferred.promise);
            spyOn(ContractService, 'getContracts').andCallFake(function(salesChannelName, cusId, fn) {
                fn(contract, '200');
            });

            controller('SelectCustomerController', {$scope:scope});
        });

        it('should auto default sales channel if there exist only one', function() {
            deferred.resolve(customer);
            rootScope.$digest();
            expect(scope.selectedSalesChannel).toBeDefined();
            expect(scope.selectedSalesChannel.name).toBe("BT INDIA");
        });

        it('should auto default customer if there exist only one', function() {
            deferred.resolve(customer);
            rootScope.$digest();
            expect(scope.customer).toBeDefined();
            expect(scope.customer.cusId).toBe("cus1");
        });

        it('should auto default contract if there exist only one', function() {
            deferred.resolve(customer);
            rootScope.$digest();
            expect(scope.contract).toBeDefined();
            expect(scope.contract.id).toBe("contr1");
        });

        it('should allow launching customer configuration once salesChannel, customer & contract selected', function() {
            expect(pageContext.exist()).toBeFalsy();
            deferred.resolve(customer);
            rootScope.$digest();
            scope.launchConfiguration();
            expect(pageContext.exist()).toBeTruthy();
        });
    });

    describe('When there is no contract - ', function() {
          var customer = {itemWrapper:{customerDTO:[
            {cusId:"cus1", name:"cus1"}
        ]}};
        var contract = [
        ];

        beforeEach(function() {
            scope.salesUser = {"name":"Leela Sankar Pinjala", salesChannelList:[
                {name:"BT INDIA"}
            ]};

            deferred = q.defer();
            spyOn(SalesChannelService, 'getCustomers').andReturn(deferred.promise);
            spyOn(ContractService, 'getContracts').andCallFake(function(salesChannelName, cusId, fn) {
                fn(contract, '200');
            });

            controller('SelectCustomerController', {$scope:scope});
        });

        it('should allow launching customer configuration if sales channel & customer selected', function() {
            expect(pageContext.exist()).toBeFalsy();
            deferred.resolve(customer);
            rootScope.$digest();
            scope.launchConfiguration();
            expect(pageContext.exist()).toBeTruthy();
        });

    });


});