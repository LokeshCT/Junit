//'use strict';

if (!window.console) {
    console = { log:function () {
    } };
}

var cqmAppControllers = angular.module('cqm.controllers', ['angularFileUpload','cqm.services', 'ngGrid', 'ngRoute', 'ui.grid', 'ui.bootstrap', 'ui.select2', 'cqm.directives']);

cqmAppControllers.controller('headerController', ['$scope', '$rootScope', 'UrlConfiguration','PageContext', 'UserContext', 'SessionContext','UIService', function ($scope, $rootScope, UrlConfiguration, PageContext, UserContext, SessionContext,UIService) {

    console.log('Inside headerController');

    $scope.headerImageUri = UrlConfiguration.headerImgUri;
    $scope.menuItemList = [{ "label":"", "route":"", "class":"divider"},{ "label":"Logout", "route":"logout", "class":""}];

    $scope.salesUser = SessionContext.getUser();

    $scope.$on(EVENT.LoadedSalesUser, function(){
        $scope.salesUser = UserContext.getUser();
        if(!_.isUndefined($scope.salesUser.roles)){
            $scope.role =$scope.salesUser.roles[0];
        }
    });


    $scope.logout = function() {
        UIService.block();

        SessionContext.logout(function(data,status){
            PageContext.clear();
            UserContext.destroy();

            var cookies = document.cookie.split(";");
            for (var i = 0; i < cookies.length; i++){
                var d = new Date();
                d.setDate(d.getDate() - 1);
                var expires = "=;expires="+d;
                var spcook =  cookies[i].split("=");
                document.cookie = spcook[0] + expires;
            }

            $scope.context.state = STATE.Logout;
            UIService.unblock();
        });

    };

}]);
