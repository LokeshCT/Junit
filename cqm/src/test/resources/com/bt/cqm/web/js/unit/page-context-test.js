'use strict';

describe('PageContextTest', function() {

    beforeEach(angular.mock.module('cqm'));


    it('getter methods should throw error if PageContext not loaded', inject(function(PageContext) {
        try {
           PageContext.getSalesChannel()
        } catch(e) {
            expect(e.message).toBe("PageContext not loaded.");
        }
    }));


    it('should set PageContext', inject(function(PageContext) {
        PageContext.setContext( {name:"sales channel"}, {name: "customer"}, {name : "contract"},{name:'selectedRole'});

        expect(PageContext.getSalesChannel().name).toBe("sales channel");
        expect(PageContext.getCustomer().name).toBe("customer");
        expect(PageContext.getContract().name).toBe("contract");
        expect(PageContext.getSelectedRole().name).toBe('selectedRole');
    }));


    it('should clear PageContext', inject(function(PageContext) {
        PageContext.setContext( {name:"sales channel"}, {name: "customer"}, {name : "contract"},{name:'selectedRole'});
        PageContext.clear();

        try {
           PageContext.getCustomer();
        } catch(e) {
            expect(e.message).toBe("PageContext not loaded.");
        }
    }));

});