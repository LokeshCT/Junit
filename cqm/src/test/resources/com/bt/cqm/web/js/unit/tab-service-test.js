'use strict';

describe('TabServiceTest - ', function() {
    var deferred;
    var mockResponse = {"tabs":[
        {
        "id":"Quote",
        "label":"Manage Quotes",
        "uri":"/searchQuotes",
        "treeNode":{
            "id":"QuoteRootNode",
            "label":"Quote",
            "status":"Not Configured",
            "children":[
                {
                    "id":"searchQuotes",
                    "label":"View Quote",
                    "status":"Not Configured",
                    "uri":"/searchQuotes",
                    "children":[]
                },
                {
                    "id":"createQuote",
                    "label":"Create Quote",
                    "status":"Not Configured",
                    "uri":"/createQuote",
                    "children":[]
                }
            ]
        }
    }
    ]};

    beforeEach(angular.mock.module(function($provide){
        provide$configuration($provide);
    }));

    beforeEach(angular.mock.module('cqm'));

    beforeEach(inject(function($q, httpService) {
        deferred = $q.defer();
        spyOn(httpService, 'httpQParamGet').andReturn(deferred.promise);
    }));

    it('should get tabs from backend and attach behaviour', inject(function($rootScope, TabService) {

        var tabs;
        TabService.getTabs().then(function(data) {
             tabs = data["tabs"];
        });

        deferred.resolve(mockResponse);
        $rootScope.$digest();

        expect(tabs.length).toBe(1);
        expect(tabs[0].selected).toBeFalsy();
        expect(tabs[0].treeNode.tab()).toBe(tabs[0]);
        expect(tabs[0].treeNode.visible).toBeDefined();
        expect(tabs[0].treeNode.expanded).toBeFalsy();
    }));
});