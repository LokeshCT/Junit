describe('UserContextTest - ', function() {

    beforeEach(angular.mock.module('cqm'));

    it('getUser should throw error if UserContext not initialized', inject(function(UserContext) {
        try {
            UserContext.getUser();
        } catch(e) {
            expect(e.message).toBe("UserContext not initialized. Use UserContext.initialize to initialize UserContext");
        }
    }));

    it('should getUser from UserContext', inject(function(UserContext) {
        UserContext.initialize({name: "User Full Name"});
        var user = UserContext.getUser();

        expect(user.name).toBe("User Full Name");

    }));

    it('should destroy UserContext', inject(function(UserContext) {
        UserContext.initialize({name: "User Full Name"});
        UserContext.destroy();

        try {
            UserContext.getUser();
        } catch(e) {
            expect(e.message).toBe("UserContext not initialized. Use UserContext.initialize to initialize UserContext");
        }
    }));

});