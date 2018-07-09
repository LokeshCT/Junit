'use strict';

describe('CustomeQuoteConfigControllerTest - ', function() {
    var scope;
    var salesChannel = {'name': "BT INDIA"};
    var customer = {'cusName': "A CUSTOMER"};
    var contract = {'refNumber': "A CONTRACT"};
    var role ={name:'selectedRole'};
    var mockResponse = {"tabs":[
        {
            "id":"Site",
            "label":"Manage Sites",
            "uri":"/branchSite",
            "treeNode":{
                "id":"SiteRootNode",
                "label":"Site",
                "status":"Not Configured",
                "children":[
                    {
                        "id":"branchSites",
                        "label":"View Sites",
                        "status":"Not Configured",
                        "uri":"/branchSite",
                        "children":[]
                    },
                    {
                        "id":"siteContacts",
                        "label":"Contacts",
                        "status":"Not Configured",
                        "uri":"/branchSiteContact",
                        "children":[]
                    }
                ]
            }
        },
        {
            "id":"Order",
            "label":"Orders",
            "uri":"/searchOrders",
            "treeNode":{
                "id":"OrderRootNode",
                "label":"Order",
                "status":"Not Configured",
                "children":[
                    {
                        "id":"searchOrders",
                        "label":"View Orders",
                        "status":"Not Configured",
                        "uri":"/searchOrders",
                        "children":[]
                    }
                ]
            }
        }
    ]};

    beforeEach(angular.mock.module(function($provide) {
        provide$configuration($provide);
    }));

    beforeEach(angular.mock.module('cqm'));

    beforeEach(inject(function($q, $rootScope, $controller, httpService, PageContext) {
        var deferred = $q.defer();
        spyOn(httpService, 'httpQParamGet').andReturn(deferred.promise);

        PageContext.setContext(salesChannel, customer, contract,role);
        scope = $rootScope.$new();
        scope.context = {state: STATE.CustomerConfiguration, subState: "Order"};

        $controller('CustomerQuoteConfigController', {$scope: scope});

        deferred.resolve(mockResponse);
        $rootScope.$digest();
    }));

    it('should move to correct tab upon page load', function() {
        expect(scope.tabToSelect.id).toBe("Order");
        expect(scope.rootNode.id).toBe("OrderRootNode");
    });

    it('should load sales channel, customer & contract data from PageContext', function() {
          expect(scope.salesChannelName).toBe("BT INDIA");
          expect(scope.customerName).toBe("A CUSTOMER");
          expect(scope.contract).toBe("A CONTRACT");
    });

    it('should change the state upon "Choose Another Customer" button click', function() {
        expect(scope.context.state).toBe(STATE.CustomerConfiguration);
        scope.gotoCustomerSelection();
        expect(scope.context.state).toBe(STATE.CustomerSelection);
    });

});